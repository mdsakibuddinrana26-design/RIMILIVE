package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.AppDatabase
import com.example.core.firebase.FirebaseAuthManager
import com.example.core.security.UserRole
import com.example.data.repository.AuthRepositoryImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirebaseAuthIntegrationTest {

    private lateinit var context: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        firebaseAuth = FirebaseAuthManager.getFirebaseAuth(context)
        val database = AppDatabase.getInstance(context)
        repository = AuthRepositoryImpl(
            userDao = database.userDao(),
            auditDao = database.securityAuditDao(),
            firebaseAuth = firebaseAuth
        )
        runBlocking {
            repository.seedInitialDataIfEmpty()
        }
    }

    @Test
    fun testFirebaseAuthInitialization() {
        assertNotNull(firebaseAuth)
    }

    @Test
    fun testLoginAndLogoutFlow() = runBlocking {
        // Authenticate with seeded demo account
        val loginResult = repository.login("user@gamilive.com", "user1234")
        assertTrue("Login should succeed", loginResult.isSuccess)
        val user = loginResult.getOrNull()
        assertNotNull(user)
        assertEquals("user@gamilive.com", user?.email)
        assertEquals(UserRole.USER, user?.role)

        // Verify current user flow is populated
        val currentUser = repository.currentUserFlow.first()
        assertNotNull(currentUser)
        assertEquals(user?.email, currentUser?.email)

        // Logout
        repository.logout()
        val afterLogoutUser = repository.currentUserFlow.first()
        assertNull("Current user should be null after logout", afterLogoutUser)
    }

    @Test
    fun testForgotPasswordRequestFlow() = runBlocking {
        val resetResult = repository.requestPasswordReset("user@gamilive.com")
        assertTrue("Password reset request should succeed", resetResult.isSuccess)
        val session = resetResult.getOrNull()
        assertNotNull(session)
        assertEquals("user@gamilive.com", session?.email)
        assertNotNull(session?.demoRecoveryCode)
    }

    @Test
    fun testGoogleSignInFlow() = runBlocking {
        val result = repository.signInWithGoogle(
            idToken = null,
            email = "mdsakibuddinrana26@gmail.com",
            name = "Md Sakib Uddin Rana"
        )
        assertTrue("Google sign in should succeed", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("mdsakibuddinrana26@gmail.com", user?.email)
        assertEquals("Md Sakib Uddin Rana", user?.fullName)
        val currentUser = repository.currentUserFlow.first()
        assertNotNull(currentUser)
        assertEquals("mdsakibuddinrana26@gmail.com", currentUser?.email)
    }

    @Test
    fun testGoogleIdOptionConfiguration() {
        val webClientId = com.example.core.auth.GoogleAuthHelper.getWebClientId(context)
        assertNotNull(webClientId)
        assertTrue(webClientId.contains("googleusercontent.com"))

        val googleIdOption = com.example.core.auth.GoogleAuthHelper.createGoogleIdOption(
            webClientId = webClientId,
            filterByAuthorizedAccounts = false,
            autoSelectEnabled = false
        )
        assertNotNull(googleIdOption)
        assertEquals(webClientId, googleIdOption.serverClientId)

        val credentialRequest = com.example.core.auth.GoogleAuthHelper.createCredentialRequest(googleIdOption)
        assertNotNull(credentialRequest)
        assertTrue(credentialRequest.credentialOptions.isNotEmpty())
    }

    @Test
    fun testFacebookSignInFlow() = runBlocking {
        val result = repository.signInWithFacebook(
            token = null,
            email = "facebook_user@example.com",
            name = "Facebook User"
        )
        assertTrue("Facebook sign in should succeed", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("facebook_user@example.com", user?.email)
        assertEquals("Facebook User", user?.fullName)

        val currentUser = repository.currentUserFlow.first()
        assertNotNull(currentUser)
        assertEquals("facebook_user@example.com", currentUser?.email)
    }

    @Test
    fun testEmailLinkValidation() = runBlocking {
        // Invalid email format test
        val invalidResult = repository.sendEmailOtpOrLink("invalid-email")
        assertTrue("Invalid email format should fail validation", invalidResult.isFailure)

        // Empty email test
        val emptyResult = repository.sendEmailOtpOrLink("   ")
        assertTrue("Empty email should fail validation", emptyResult.isFailure)

        // Verification without link test
        val emptyVerifyResult = repository.verifyEmailOtpOrLink("user@example.com", "")
        assertTrue("Empty sign-in link input should fail", emptyVerifyResult.isFailure)
    }

    @Test
    fun testFirebaseEmailLinkRecognition() {
        val sampleFirebaseLink = "https://gami-live-1d0ec.firebaseapp.com/__/auth/action?apiKey=AIzaSyA4QyvM0Xw_38042rWJb6eL95eCqUvI45Y&mode=signIn&oobCode=randomCode123456"
        val isEmailLink = firebaseAuth.isSignInWithEmailLink(sampleFirebaseLink)
        assertTrue("Firebase should recognize valid email action link", isEmailLink)
    }
}
