package com.example.data.repository

import android.net.Uri
import android.util.Log
import com.example.core.database.dao.SecurityAuditDao
import com.example.core.database.dao.UserDao
import com.example.core.database.entity.SecurityAuditEntity
import com.example.core.database.entity.UserEntity
import com.example.core.firebase.await
import com.example.core.model.AuditEventType
import com.example.core.model.User
import com.example.core.security.PasswordSecurity
import com.example.core.security.SecurityPolicy
import com.example.core.security.UserRole
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val auditDao: SecurityAuditDao,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    /*
     * Do not seed this flow with null.  A StateFlow(null) makes the UI treat the
     * first, synchronous value as "signed out" while Firebase is still
     * restoring its persisted session.  The auth listener publishes the first
     * definitive result (a local profile or null) instead.
     */
    private val _currentUserFlow = MutableSharedFlow<User?>(
        replay = 1,
        extraBufferCapacity = 1
    )
    override val currentUserFlow: Flow<User?> = _currentUserFlow.asSharedFlow()
    private var lastPublishedUser: User? = null
    private val initialDataReady = CompletableDeferred<Unit>()
    private val restorationMutex = Mutex()

    private var pendingEmailForSignIn: String? = null

    private fun savePendingEmail(email: String) {
        pendingEmailForSignIn = email
        try {
            firebaseAuth.app.applicationContext
                .getSharedPreferences("gami_auth_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("email_for_sign_in", email)
                .apply()
        } catch (_: Exception) {
        }
    }

    private fun getPendingEmail(): String? {
        return pendingEmailForSignIn ?: try {
            firebaseAuth.app.applicationContext
                .getSharedPreferences("gami_auth_prefs", android.content.Context.MODE_PRIVATE)
                .getString("email_for_sign_in", null)
        } catch (_: Exception) {
            null
        }
    }

    init {
        // Synchronize persistent Firebase Authentication session state
        firebaseAuth.addAuthStateListener { auth ->
            val fbUser = auth.currentUser
            CoroutineScope(Dispatchers.IO).launch {
                // A fresh install may still be seeding the built-in local
                // profiles when Firebase invokes this listener.
                initialDataReady.await()
                /*
                 * Firebase is the source of truth for session existence, but
                 * the app's role/profile data remains local. Only hydrate from
                 * an authenticated Firebase identity; never replace existing
                 * profile fields or create a pretend Firebase session.
                 */
                // Anonymous sessions are intentionally not treated as an
                // authenticated identity and are never turned into profiles.
                val local = fbUser
                    ?.takeUnless { it.isAnonymous }
                    ?.let { hydrateFirebaseUser(it) }
                if (local != null) {
                    userDao.updateLastLogin(local.id, System.currentTimeMillis())
                    lastPublishedUser = local.toDomainModel()
                    _currentUserFlow.emit(lastPublishedUser)
                } else {
                    lastPublishedUser = null
                    _currentUserFlow.emit(null)
                }
            }
        }
    }

    /**
     * Resolves Firebase identity to the local profile without replacing any
     * profile fields.  The existing Room schema has no Firebase UID column, so
     * a private UID->local-id mapping is kept in app preferences for identities
     * (notably Facebook accounts) that do not expose an email address.
     */
    private suspend fun hydrateFirebaseUser(firebaseUser: FirebaseUser): UserEntity? =
        restorationMutex.withLock {
            val prefs = firebaseAuth.app.applicationContext.getSharedPreferences(
                "gami_auth_identity",
                android.content.Context.MODE_PRIVATE
            )
            val mappedId = prefs.getLong("uid_${firebaseUser.uid}", 0L)
            val byMappedId = mappedId.takeIf { it > 0 }?.let { userDao.findById(it) }
            val email = firebaseUser.email?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
            val existing = byMappedId ?: email?.let { userDao.findByEmail(it) }
            if (existing != null) {
                if (mappedId != existing.id) {
                    prefs.edit().putLong("uid_${firebaseUser.uid}", existing.id).apply()
                }
                return@withLock existing
            }

            // A Firebase user without an email still needs a local profile for
            // Home/profile routing. This is a local, stable identifier only;
            // it is never used as a Firebase credential or email address.
            val localEmail = email ?: "firebase-${firebaseUser.uid}@local.invalid"
            val provider = firebaseUser.providerData
                .firstOrNull { !it.providerId.isNullOrBlank() }
                ?.providerId
                ?: "firebase"
            val baseUsername = "firebase_${firebaseUser.uid}"
                .replace(Regex("[^A-Za-z0-9_]"), "_")
                .take(40)
                .ifBlank { "firebase_user" }
            var username = baseUsername
            var suffix = 1
            while (userDao.findByUsername(username) != null) {
                username = "${baseUsername.take(35)}_$suffix"
                suffix++
            }
            val displayName = firebaseUser.displayName?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: email?.substringBefore("@")
                ?: "Firebase User"
            val entity = UserEntity(
                username = username,
                email = localEmail,
                fullName = displayName,
                passwordSalt = PasswordSecurity.generateSalt(),
                passwordHash = "",
                role = UserRole.USER.name,
                securityQuestion = "$provider Authentication",
                securityAnswerHash = "",
                isEmailVerified = firebaseUser.isEmailVerified,
                lastLoginAt = System.currentTimeMillis()
            )
            val insertedId = userDao.insert(entity)
            val created = entity.copy(id = insertedId)
            prefs.edit().putLong("uid_${firebaseUser.uid}", insertedId).apply()
            created
        }

    override suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        try {
        val count = userDao.countUsers()
        if (count == 0) {
            // 1. Dedicated System Owner Account (Server/Cloud Architecture Blueprint)
            val ownerSalt = PasswordSecurity.generateSalt()
            val ownerEntity = UserEntity(
                username = "system_owner",
                email = "owner@gamilive.com",
                fullName = "GAMI System Owner",
                passwordSalt = ownerSalt,
                passwordHash = PasswordSecurity.hashPassword("admin1234", ownerSalt),
                role = UserRole.OWNER.name,
                securityQuestion = "What is the primary cloud project name?",
                securityAnswerHash = PasswordSecurity.hashPassword("gamilive-cloud", ownerSalt),
                isEmailVerified = true
            )
            userDao.insert(ownerEntity)

            // 2. Dedicated Administrator Account (For future Admin Panel)
            val adminSalt = PasswordSecurity.generateSalt()
            val adminEntity = UserEntity(
                username = "gami_admin",
                email = "admin@gamilive.com",
                fullName = "GAMI Live Admin",
                passwordSalt = adminSalt,
                passwordHash = PasswordSecurity.hashPassword("admin1234", adminSalt),
                role = UserRole.ADMIN.name,
                securityQuestion = "What is the administrative security token code?",
                securityAnswerHash = PasswordSecurity.hashPassword("gami-alpha", adminSalt),
                isEmailVerified = true
            )
            userDao.insert(adminEntity)

            // 3. Normal Standard User Account (Strictly UserRole.USER)
            val userSalt = PasswordSecurity.generateSalt()
            val regularUserEntity = UserEntity(
                username = "live_streamer",
                email = "user@gamilive.com",
                fullName = "Alex Rivers",
                passwordSalt = userSalt,
                passwordHash = PasswordSecurity.hashPassword("user1234", userSalt),
                role = UserRole.USER.name,
                securityQuestion = "What was your first favorite video game?",
                securityAnswerHash = PasswordSecurity.hashPassword("zelda", userSalt),
                isEmailVerified = true
            )
            userDao.insert(regularUserEntity)

            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.SECURITY_PRIVILEGE_CHECK.name,
                    targetIdentifier = "SYSTEM_INITIALIZATION",
                    roleLevel = UserRole.OWNER.name,
                    detail = "Database initialized with Owner, Admin, and Standard User tiers. Permission isolation active."
                )
            )
        }
        } finally {
            initialDataReady.complete(Unit)
        }
    }

    override suspend fun login(identifier: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()
        if (trimmed.isEmpty() || password.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Email/Username and password cannot be empty."))
        }

        val localUser = userDao.findByEmailOrUsername(trimmed)
        val emailToUse = localUser?.email ?: if (trimmed.contains("@")) trimmed else null

        // If local account credentials are valid, authenticate immediately without triggering Firebase Recaptcha
        if (localUser != null && PasswordSecurity.verifyPassword(password, localUser.passwordSalt, localUser.passwordHash)) {
            userDao.updateLastLogin(localUser.id, System.currentTimeMillis())
            val domainUser = localUser.toDomainModel()
            lastPublishedUser = domainUser
            _currentUserFlow.emit(domainUser)

            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.LOGIN_SUCCESS.name,
                    targetIdentifier = domainUser.email,
                    roleLevel = domainUser.role.name,
                    detail = "User successfully authenticated (${domainUser.role.label})"
                )
            )
            return@withContext Result.success(domainUser)
        }

        if (emailToUse == null) {
            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.LOGIN_FAILED.name,
                    targetIdentifier = trimmed,
                    roleLevel = UserRole.USER.name,
                    detail = "Failed login attempt: Account not found"
                )
            )
            return@withContext Result.failure(IllegalArgumentException("Invalid username/email or password."))
        }

        try {
            // 1. Firebase Authentication: Sign in with email and password
            val authResult = firebaseAuth.signInWithEmailAndPassword(emailToUse, password).await()
            val firebaseUser = authResult.user

            val domainUser = if (localUser != null) {
                userDao.updateLastLogin(localUser.id, System.currentTimeMillis())
                localUser.toDomainModel()
            } else {
                val displayName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: emailToUse.substringBefore("@")
                val salt = PasswordSecurity.generateSalt()
                val newEntity = UserEntity(
                    username = emailToUse.substringBefore("@"),
                    email = emailToUse,
                    fullName = displayName,
                    passwordSalt = salt,
                    passwordHash = PasswordSecurity.hashPassword(password, salt),
                    role = UserRole.USER.name,
                    securityQuestion = "Firebase Authentication",
                    securityAnswerHash = "",
                    isEmailVerified = firebaseUser?.isEmailVerified ?: false,
                    lastLoginAt = System.currentTimeMillis()
                )
                val insertedId = userDao.insert(newEntity)
                newEntity.copy(id = insertedId).toDomainModel()
            }

            lastPublishedUser = domainUser
            _currentUserFlow.emit(domainUser)

            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.LOGIN_SUCCESS.name,
                    targetIdentifier = domainUser.email,
                    roleLevel = domainUser.role.name,
                    detail = "User successfully authenticated via Firebase Authentication (${domainUser.role.label})"
                )
            )

            Result.success(domainUser)
        } catch (e: FirebaseAuthInvalidUserException) {
            // Check if this is a pre-seeded account or local account that should be synced
            if (localUser != null && PasswordSecurity.verifyPassword(password, localUser.passwordSalt, localUser.passwordHash)) {
                try {
                    firebaseAuth.createUserWithEmailAndPassword(emailToUse, password).await()
                } catch (_: Exception) {
                }
                userDao.updateLastLogin(localUser.id, System.currentTimeMillis())
                val domainUser = localUser.toDomainModel()
                lastPublishedUser = domainUser
                _currentUserFlow.emit(domainUser)
                auditDao.recordAuditLog(
                    SecurityAuditEntity(
                        eventType = AuditEventType.LOGIN_SUCCESS.name,
                        targetIdentifier = domainUser.email,
                        roleLevel = domainUser.role.name,
                        detail = "User authenticated with role ${domainUser.role.label}"
                    )
                )
                return@withContext Result.success(domainUser)
            }
            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.LOGIN_FAILED.name,
                    targetIdentifier = emailToUse,
                    roleLevel = UserRole.USER.name,
                    detail = "Failed login attempt: Account not found in Firebase"
                )
            )
            Result.failure(IllegalArgumentException("No account found with this email or username."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            if (localUser != null && PasswordSecurity.verifyPassword(password, localUser.passwordSalt, localUser.passwordHash)) {
                userDao.updateLastLogin(localUser.id, System.currentTimeMillis())
                val domainUser = localUser.toDomainModel()
                lastPublishedUser = domainUser
                _currentUserFlow.emit(domainUser)
                auditDao.recordAuditLog(
                    SecurityAuditEntity(
                        eventType = AuditEventType.LOGIN_SUCCESS.name,
                        targetIdentifier = domainUser.email,
                        roleLevel = domainUser.role.name,
                        detail = "User authenticated with role ${domainUser.role.label}"
                    )
                )
                return@withContext Result.success(domainUser)
            }
            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.LOGIN_FAILED.name,
                    targetIdentifier = emailToUse,
                    roleLevel = localUser?.role ?: UserRole.USER.name,
                    detail = "Failed login attempt: Incorrect credentials"
                )
            )
            Result.failure(IllegalArgumentException("Invalid username/email or password."))
        } catch (e: Exception) {
            if (localUser != null && PasswordSecurity.verifyPassword(password, localUser.passwordSalt, localUser.passwordHash)) {
                userDao.updateLastLogin(localUser.id, System.currentTimeMillis())
                val domainUser = localUser.toDomainModel()
                lastPublishedUser = domainUser
                _currentUserFlow.emit(domainUser)
                return@withContext Result.success(domainUser)
            }
            val errorMsg = when (e) {
                is FirebaseNetworkException -> "Network connection error. Please check your internet connection."
                else -> e.localizedMessage ?: "Authentication failed."
            }
            Result.failure(IllegalArgumentException(errorMsg))
        }
    }

    override suspend fun signInWithGoogle(idToken: String?, email: String?, name: String?): Result<User> = withContext(Dispatchers.IO) {
        try {
            val targetEmail = email?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
                ?: firebaseAuth.currentUser?.email?.trim()?.lowercase()
                ?: return@withContext Result.failure(IllegalArgumentException("Google account email was not provided."))
            val displayName = name?.trim()?.takeIf { it.isNotBlank() }
                ?: firebaseAuth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                ?: targetEmail.substringBefore("@")
            val username = targetEmail.substringBefore("@").replace(".", "_")

            var firebaseUid: String? = null

            // 1. Firebase Authentication with Google
            if (!idToken.isNullOrBlank()) {
                try {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(credential).await()
                    firebaseUid = authResult.user?.uid
                    Log.d("AuthRepositoryImpl", "Successfully authenticated with Firebase Auth via Google credential: $firebaseUid")
                } catch (e: Exception) {
                    Log.w("AuthRepositoryImpl", "Google ID token credential sign in note: ${e.message}")
                }
            }

            // A Firebase session is established only by the real Google
            // credential above; never substitute an anonymous user.
            val activeFbUser = firebaseAuth.currentUser
            if (activeFbUser != null) firebaseUid = activeFbUser.uid

            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()
            } catch (_: Exception) {
            }

            // 2. Synchronize with local user database (Room)
            var localUser = userDao.findByEmail(targetEmail)
            if (localUser == null) {
                val salt = PasswordSecurity.generateSalt()
                val newEntity = UserEntity(
                    username = username,
                    email = targetEmail,
                    fullName = displayName,
                    passwordSalt = salt,
                    passwordHash = "",
                    role = UserRole.USER.name,
                    securityQuestion = "Google Authentication",
                    securityAnswerHash = "",
                    isEmailVerified = true,
                    lastLoginAt = System.currentTimeMillis()
                )
                val id = userDao.insert(newEntity)
                localUser = newEntity.copy(id = id)
            } else {
                userDao.updateLastLogin(localUser.id, System.currentTimeMillis())
            }

            val domainUser = localUser.toDomainModel()
            lastPublishedUser = domainUser
            _currentUserFlow.emit(domainUser)

            val auditDetail = if (firebaseUid != null) {
                "User successfully authenticated with Firebase Authentication (UID: $firebaseUid, provider: google.com)"
            } else {
                "User successfully authenticated via Google Sign-In"
            }

            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.LOGIN_SUCCESS.name,
                    targetIdentifier = targetEmail,
                    roleLevel = domainUser.role.name,
                    detail = auditDetail
                )
            )

            Result.success(domainUser)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException(e.localizedMessage ?: "Google Sign-In failed."))
        }
    }

    override suspend fun signInWithGoogleCredential(
        idToken: String,
        email: String?,
        name: String?
    ): Result<User> = signInWithGoogle(idToken = idToken, email = email, name = name)

    override suspend fun signInWithFacebook(
        token: String?,
        email: String?,
        name: String?,
        firebaseUid: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            var effectiveUid = firebaseUid

            // 1. If Facebook Access Token is provided, authenticate with Firebase FacebookAuthProvider
            if (!token.isNullOrBlank()) {
                try {
                    val credential = FacebookAuthProvider.getCredential(token)
                    val authResult = firebaseAuth.signInWithCredential(credential).await()
                    effectiveUid = authResult.user?.uid ?: effectiveUid
                    Log.d("AuthRepositoryImpl", "Successfully authenticated with Firebase Auth via Facebook credential: $effectiveUid")
                } catch (credEx: Exception) {
                    Log.w("AuthRepositoryImpl", "Facebook credential sign-in note: ${credEx.message}")
                }
            }

            // 2. Resolve user attributes from Firebase currentUser or parameters
            val activeFbUser = firebaseAuth.currentUser
            if (effectiveUid == null && activeFbUser != null) {
                effectiveUid = activeFbUser.uid
            }

            val targetEmail = email?.trim()?.lowercase()
                ?: activeFbUser?.email?.trim()?.lowercase()
                ?: return@withContext Result.failure(IllegalArgumentException("Facebook account email was not provided."))
            val displayName = name?.trim()?.takeIf { it.isNotBlank() }
                ?: activeFbUser?.displayName?.trim()?.takeIf { it.isNotBlank() }
                ?: "Facebook User"
            val username = targetEmail.substringBefore("@").replace(".", "_") + "_fb"

            // Only a real Facebook credential may establish a Firebase
            // session; do not replace it with anonymous authentication.
            if (activeFbUser != null) effectiveUid = activeFbUser.uid

            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()
            } catch (_: Exception) {
            }

            // 3. Synchronize with local user database (Room)
            var localUser = userDao.findByEmail(targetEmail)
            if (localUser == null) {
                val salt = PasswordSecurity.generateSalt()
                val newEntity = UserEntity(
                    username = username,
                    email = targetEmail,
                    fullName = displayName,
                    passwordSalt = salt,
                    passwordHash = "",
                    role = UserRole.USER.name,
                    securityQuestion = "Facebook Authentication",
                    securityAnswerHash = "",
                    isEmailVerified = true,
                    lastLoginAt = System.currentTimeMillis()
                )
                val id = userDao.insert(newEntity)
                localUser = newEntity.copy(id = id)
            } else {
                userDao.updateLastLogin(localUser.id, System.currentTimeMillis())
            }

            val domainUser = localUser.toDomainModel()
            lastPublishedUser = domainUser
            _currentUserFlow.emit(domainUser)

            val auditDetail = if (effectiveUid != null) {
                "User successfully authenticated with Firebase Authentication (UID: $effectiveUid, provider: facebook.com)"
            } else {
                "User successfully authenticated via Facebook Sign-In"
            }

            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.LOGIN_SUCCESS.name,
                    targetIdentifier = targetEmail,
                    roleLevel = domainUser.role.name,
                    detail = auditDetail
                )
            )

            Result.success(domainUser)
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Facebook sign-in failed", e)
            Result.failure(IllegalArgumentException(e.localizedMessage ?: "Facebook Sign-In failed."))
        }
    }

    override suspend fun sendEmailOtpOrLink(email: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your email address."))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }

        try {
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://gami-live-1d0ec.firebaseapp.com?email=$cleanEmail")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    "com.example",
                    true,
                    "21"
                )
                .build()

            firebaseAuth.sendSignInLinkToEmail(cleanEmail, actionCodeSettings).await()

            savePendingEmail(cleanEmail)

            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.PASSWORD_RESET_REQUESTED.name,
                    targetIdentifier = cleanEmail,
                    roleLevel = UserRole.USER.name,
                    detail = "Passwordless email sign-in link dispatched via Firebase to $cleanEmail"
                )
            )

            Result.success("Sign-in link sent to $cleanEmail")
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Firebase sendSignInLinkToEmail failure: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ->
                    "Email link sign-in is not enabled in Firebase Console. Please ensure Email Link provider is enabled."
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network error connecting to Firebase Authentication. Please check your internet connection."
                else -> e.message ?: "Failed to dispatch email sign-in link."
            }
            Result.failure(IllegalArgumentException(errorMessage))
        }
    }

    override suspend fun verifyEmailOtpOrLink(
        email: String,
        code: String,
        username: String?,
        fullName: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        var cleanEmail = email.trim().lowercase()
        val cleanInput = code.trim()

        if (cleanEmail.isBlank()) {
            cleanEmail = getPendingEmail() ?: ""
        }

        if (cleanInput.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please provide the sign-in link from your email."))
        }

        // If email is still missing, attempt extracting from url query parameter
        if (cleanEmail.isBlank()) {
            try {
                val uri = Uri.parse(cleanInput)
                cleanEmail = uri.getQueryParameter("email")?.trim()?.lowercase() ?: ""
                if (cleanEmail.isBlank()) {
                    val continueUrl = uri.getQueryParameter("continueUrl")
                    if (!continueUrl.isNullOrBlank()) {
                        cleanEmail = Uri.parse(continueUrl).getQueryParameter("email")?.trim()?.lowercase() ?: ""
                    }
                }
            } catch (_: Exception) {
            }
        }

        if (cleanEmail.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please provide your email address to complete sign-in."))
        }

        try {
            val isLink = firebaseAuth.isSignInWithEmailLink(cleanInput)
            val linkToUse = when {
                isLink -> cleanInput
                cleanInput.startsWith("http") -> cleanInput
                cleanInput.contains("oobCode=") -> "https://gami-live-1d0ec.firebaseapp.com/__/auth/action?$cleanInput"
                else -> cleanInput
            }

            val authResult = firebaseAuth.signInWithEmailLink(cleanEmail, linkToUse).await()
            val firebaseUser = authResult.user

            val chosenUsername = username?.trim()?.takeIf { it.isNotBlank() } ?: cleanEmail.substringBefore("@").replace(".", "_")
            val chosenFullName = fullName?.trim()?.takeIf { it.isNotBlank() }
                ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                ?: cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

            if (firebaseUser != null && firebaseUser.displayName.isNullOrBlank()) {
                try {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(chosenFullName)
                        .build()
                    firebaseUser.updateProfile(profileUpdate).await()
                } catch (_: Exception) {
                }
            }

            var localUser = userDao.findByEmail(cleanEmail)
            if (localUser == null) {
                val salt = PasswordSecurity.generateSalt()
                val newEntity = UserEntity(
                    username = chosenUsername,
                    email = cleanEmail,
                    fullName = chosenFullName,
                    passwordSalt = salt,
                    passwordHash = "",
                    role = UserRole.USER.name,
                    securityQuestion = "",
                    securityAnswerHash = "",
                    isEmailVerified = true,
                    lastLoginAt = System.currentTimeMillis()
                )
                val id = userDao.insert(newEntity)
                localUser = newEntity.copy(id = id)
            } else {
                userDao.updateLastLogin(localUser.id, System.currentTimeMillis())
            }

            val domainUser = localUser.toDomainModel()
            lastPublishedUser = domainUser
            _currentUserFlow.emit(domainUser)

            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.LOGIN_SUCCESS.name,
                    targetIdentifier = cleanEmail,
                    roleLevel = domainUser.role.name,
                    detail = "User successfully authenticated via Firebase email link sign-in"
                )
            )

            Result.success(domainUser)
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Failed to authenticate via email link: ${e.message}", e)
            val errorMsg = when {
                e.message?.contains("expired", ignoreCase = true) == true ->
                    "The sign-in link has expired. Please request a new link."
                e.message?.contains("invalid", ignoreCase = true) == true ->
                    "The sign-in link is invalid or has already been used."
                else -> e.message ?: "Failed to sign in with email link."
            }
            Result.failure(IllegalArgumentException(errorMsg))
        }
    }

    override suspend fun signUp(
        username: String,
        email: String,
        fullName: String,
        password: String,
        securityQuestion: String,
        securityAnswer: String
    ): Result<User> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val cleanEmail = email.trim().lowercase()
        val cleanFullName = fullName.trim()
        val cleanQuestion = securityQuestion.trim()
        val cleanAnswer = securityAnswer.trim().lowercase()

        if (cleanUsername.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Username must be at least 3 characters long."))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.isNotEmpty() && password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        if (userDao.findByEmail(cleanEmail) != null) {
            return@withContext Result.failure(IllegalArgumentException("An account with this email already exists."))
        }
        if (userDao.findByUsername(cleanUsername) != null) {
            return@withContext Result.failure(IllegalArgumentException("This username is already taken."))
        }

        val strictlyEnforcedRole = SecurityPolicy.enforceNormalRegistrationRole()
        val salt = PasswordSecurity.generateSalt()
        val passwordHash = if (password.isNotEmpty()) PasswordSecurity.hashPassword(password, salt) else ""
        val answerHash = if (cleanAnswer.isNotEmpty()) PasswordSecurity.hashPassword(cleanAnswer, salt) else ""

        try {
            if (password.isNotEmpty()) {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, password).await()
                val firebaseUser = authResult.user
                try {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanFullName)
                        .build()
                    firebaseUser?.updateProfile(profileUpdate)?.await()
                } catch (pe: Exception) {
                    Log.w("AuthRepository", "Firebase profile update non-fatal: ${pe.message}")
                }
            }

            val newUserEntity = UserEntity(
                username = cleanUsername,
                email = cleanEmail,
                fullName = cleanFullName,
                passwordSalt = salt,
                passwordHash = passwordHash,
                role = strictlyEnforcedRole.name,
                securityQuestion = cleanQuestion,
                securityAnswerHash = answerHash,
                isEmailVerified = true
            )

            val insertedId = userDao.insert(newUserEntity)
            val createdUser = newUserEntity.copy(id = insertedId).toDomainModel()
            lastPublishedUser = createdUser
            _currentUserFlow.emit(createdUser)

            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.SIGN_UP_USER.name,
                    targetIdentifier = createdUser.email,
                    roleLevel = createdUser.role.name,
                    detail = "New user registered with standard permissions."
                )
            )

            Result.success(createdUser)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(IllegalArgumentException("An account with this email already exists in Firebase."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(IllegalArgumentException("Password is too weak. Please use at least 6 characters."))
        } catch (e: Exception) {
            if (e is FirebaseNetworkException || e.message?.contains("API key", ignoreCase = true) == true) {
                val newUserEntity = UserEntity(
                    username = cleanUsername,
                    email = cleanEmail,
                    fullName = cleanFullName,
                    passwordSalt = salt,
                    passwordHash = passwordHash,
                    role = strictlyEnforcedRole.name,
                    securityQuestion = cleanQuestion,
                    securityAnswerHash = answerHash,
                    isEmailVerified = false
                )
                val insertedId = userDao.insert(newUserEntity)
                val createdUser = newUserEntity.copy(id = insertedId).toDomainModel()
                lastPublishedUser = createdUser
                _currentUserFlow.emit(createdUser)
                auditDao.recordAuditLog(
                    SecurityAuditEntity(
                        eventType = AuditEventType.SIGN_UP_USER.name,
                        targetIdentifier = createdUser.email,
                        roleLevel = createdUser.role.name,
                        detail = "New user registered locally with standard permissions."
                    )
                )
                return@withContext Result.success(createdUser)
            }
            Result.failure(IllegalArgumentException(e.localizedMessage ?: "Sign up failed."))
        }
    }

    override suspend fun requestPasswordReset(identifier: String): Result<PasswordResetSession> = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your registered email or username."))
        }

        val userEntity = userDao.findByEmailOrUsername(trimmed)
        val emailToUse = userEntity?.email ?: if (trimmed.contains("@")) trimmed else null

        if (emailToUse == null) {
            return@withContext Result.failure(IllegalArgumentException("No account found matching '$trimmed'."))
        }

        var firebaseDispatched = false
        try {
            firebaseAuth.sendPasswordResetEmail(emailToUse).await()
            firebaseDispatched = true
        } catch (e: FirebaseAuthInvalidUserException) {
            if (userEntity == null) {
                return@withContext Result.failure(IllegalArgumentException("No account found matching '$trimmed'."))
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Firebase sendPasswordResetEmail: ${e.message}")
        }

        val recoveryCode = PasswordSecurity.generateRecoveryCode()
        val expiresAt = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes validity
        if (userEntity != null) {
            userDao.setResetCode(userEntity.id, recoveryCode, expiresAt)
        }

        auditDao.recordAuditLog(
            SecurityAuditEntity(
                eventType = AuditEventType.PASSWORD_RESET_REQUESTED.name,
                targetIdentifier = emailToUse,
                roleLevel = userEntity?.role ?: UserRole.USER.name,
                detail = if (firebaseDispatched) {
                    "Firebase password reset email dispatched and local recovery session prepared."
                } else {
                    "Recovery session prepared for account password reset."
                }
            )
        )

        Result.success(
            PasswordResetSession(
                identifier = trimmed,
                email = emailToUse,
                securityQuestion = userEntity?.securityQuestion ?: "Security Question",
                demoRecoveryCode = recoveryCode
            )
        )
    }

    override suspend fun verifyResetCode(identifier: String, code: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()
        val cleanCode = code.trim()

        val user = userDao.findByEmailOrUsername(trimmed)
            ?: return@withContext Result.failure(IllegalArgumentException("Account not found."))

        if (user.activeResetCode.isNullOrEmpty() || user.resetCodeExpiresAt == null) {
            return@withContext Result.failure(IllegalArgumentException("No active reset request found for this account."))
        }

        if (System.currentTimeMillis() > user.resetCodeExpiresAt) {
            return@withContext Result.failure(IllegalArgumentException("Recovery code has expired. Please request a new one."))
        }

        if (user.activeResetCode != cleanCode) {
            return@withContext Result.failure(IllegalArgumentException("Invalid verification code."))
        }

        Result.success(true)
    }

    override suspend fun verifySecurityAnswer(identifier: String, answer: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()
        val cleanAnswer = answer.trim().lowercase()

        val user = userDao.findByEmailOrUsername(trimmed)
            ?: return@withContext Result.failure(IllegalArgumentException("Account not found."))

        val matches = PasswordSecurity.verifyPassword(cleanAnswer, user.passwordSalt, user.securityAnswerHash)
        if (!matches) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect security answer."))
        }

        Result.success(true)
    }

    override suspend fun resetPassword(identifier: String, newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()
        if (newPassword.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("New password must be at least 6 characters long."))
        }

        val user = userDao.findByEmailOrUsername(trimmed)
            ?: return@withContext Result.failure(IllegalArgumentException("Account not found."))

        try {
            val currentFbUser = firebaseAuth.currentUser
            if (currentFbUser != null && currentFbUser.email.equals(user.email, ignoreCase = true)) {
                currentFbUser.updatePassword(newPassword).await()
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Firebase updatePassword non-fatal: ${e.message}")
        }

        val newSalt = PasswordSecurity.generateSalt()
        val newHash = PasswordSecurity.hashPassword(newPassword, newSalt)
        userDao.updatePassword(user.id, newHash, newSalt)

        auditDao.recordAuditLog(
            SecurityAuditEntity(
                eventType = AuditEventType.PASSWORD_RESET_SUCCESS.name,
                targetIdentifier = user.email,
                roleLevel = user.role,
                detail = "Password successfully reset and credentials updated."
            )
        )

        Result.success(Unit)
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        val current = lastPublishedUser
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            Log.w("AuthRepository", "Firebase signOut non-fatal: ${e.message}")
        }
        lastPublishedUser = null
        _currentUserFlow.emit(null)
        if (current != null) {
            auditDao.recordAuditLog(
                SecurityAuditEntity(
                    eventType = AuditEventType.SECURITY_PRIVILEGE_CHECK.name,
                    targetIdentifier = current.email,
                    roleLevel = current.role.name,
                    detail = "User logged out from Firebase Authentication."
                )
            )
        }
    }
}
