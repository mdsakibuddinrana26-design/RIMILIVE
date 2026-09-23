package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.User
import com.example.core.security.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.PasswordResetSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthScreen {
    LOGIN,
    SIGN_UP,
    FORGOT_PASSWORD,
    AUTHENTICATED
}

enum class AuthMethod {
    EMAIL,
    GOOGLE,
    FACEBOOK
}

data class LoginUiState(
    val selectedMethod: AuthMethod = AuthMethod.EMAIL,
    val phoneNumber: String = "",
    val email: String = "",
    val verificationCode: String = "",
    val isCodeSent: Boolean = false,
    val sentDestination: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
    val identifier: String = "",
    val password: String = ""
)

data class SignUpUiState(
    val fullName: String = "",
    val username: String = "",
    val selectedMethod: AuthMethod = AuthMethod.EMAIL,
    val phoneNumber: String = "",
    val email: String = "",
    val verificationCode: String = "",
    val isCodeSent: Boolean = false,
    val sentDestination: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)

enum class RecoveryVerificationMethod {
    VERIFICATION_CODE,
    SECURITY_QUESTION
}

data class ForgotPasswordUiState(
    val step: Int = 1, // 1: Identifier, 2: Verify Code/Question, 3: New Password, 4: Success
    val identifier: String = "",
    val maskedEmail: String = "",
    val securityQuestion: String = "",
    val demoRecoveryCode: String? = null,
    val verificationMethod: RecoveryVerificationMethod = RecoveryVerificationMethod.VERIFICATION_CODE,
    val inputCode: String = "",
    val inputSecurityAnswer: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isVerified: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class MainAuthUiState(
    val currentScreen: AuthScreen = AuthScreen.LOGIN,
    val currentUser: User? = null,
    val isRestoringSession: Boolean = true,
    val loginState: LoginUiState = LoginUiState(),
    val signUpState: SignUpUiState = SignUpUiState(),
    val forgotPasswordState: ForgotPasswordUiState = ForgotPasswordUiState(),
    val globalMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainAuthUiState())
    val uiState: StateFlow<MainAuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        viewModelScope.launch {
            repository.currentUserFlow.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        currentUser = user,
                        isRestoringSession = false,
                        currentScreen = if (user != null) AuthScreen.AUTHENTICATED else current.currentScreen
                    )
                }
            }
        }
    }

    // Navigation
    fun navigateTo(screen: AuthScreen) {
        _uiState.update {
            it.copy(
                currentScreen = screen,
                loginState = it.loginState.copy(errorMessage = null),
                signUpState = it.signUpState.copy(errorMessage = null),
                forgotPasswordState = it.forgotPasswordState.copy(errorMessage = null)
            )
        }
    }

    // Login actions
    fun onLoginMethodSelected(method: AuthMethod) {
        _uiState.update {
            it.copy(
                loginState = it.loginState.copy(
                    selectedMethod = method,
                    errorMessage = null,
                    statusMessage = null,
                    isCodeSent = false,
                    verificationCode = ""
                )
            )
        }
    }

    fun onLoginPhoneNumberChanged(value: String) {
        _uiState.update { it.copy(loginState = it.loginState.copy(phoneNumber = value, errorMessage = null)) }
    }

    fun onLoginEmailChanged(value: String) {
        _uiState.update { it.copy(loginState = it.loginState.copy(email = value, errorMessage = null)) }
    }

    fun onLoginVerificationCodeChanged(value: String) {
        _uiState.update { it.copy(loginState = it.loginState.copy(verificationCode = value, errorMessage = null)) }
    }

    fun onLoginResetCodeInput() {
        _uiState.update {
            it.copy(
                loginState = it.loginState.copy(
                    isCodeSent = false,
                    verificationCode = "",
                    statusMessage = null,
                    errorMessage = null
                )
            )
        }
    }

    fun sendLoginVerificationCode(activity: android.app.Activity? = null) {
        val state = _uiState.value.loginState
        val destination = state.email.trim()

        if (destination.isBlank()) {
            _uiState.update {
                it.copy(
                    loginState = it.loginState.copy(
                        errorMessage = "Please enter your email address."
                    )
                )
            }
            return
        }

        _uiState.update { it.copy(loginState = it.loginState.copy(isLoading = true, errorMessage = null, statusMessage = null)) }

        viewModelScope.launch {
            val result = repository.sendEmailOtpOrLink(destination)

            result.fold(
                onSuccess = { _ ->
                    _uiState.update {
                        it.copy(
                            loginState = it.loginState.copy(
                                isLoading = false,
                                isCodeSent = true,
                                sentDestination = destination,
                                statusMessage = "Sign-in link sent to $destination. Please check your inbox and click the link or paste it below.",
                                errorMessage = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            loginState = it.loginState.copy(
                                isLoading = false,
                                isCodeSent = false,
                                errorMessage = error.message ?: "Failed to send sign-in link."
                            )
                        )
                    }
                }
            )
        }
    }

    fun verifyLoginCode() {
        val state = _uiState.value.loginState
        if (state.verificationCode.isBlank()) {
            _uiState.update {
                it.copy(loginState = it.loginState.copy(errorMessage = "Please enter or paste the sign-in link from your email."))
            }
            return
        }

        _uiState.update { it.copy(loginState = it.loginState.copy(isLoading = true, errorMessage = null)) }

        viewModelScope.launch {
            val result = repository.verifyEmailOtpOrLink(state.email, state.verificationCode)

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            loginState = LoginUiState(),
                            currentScreen = AuthScreen.AUTHENTICATED
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            loginState = it.loginState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Sign-in failed. Please check the link."
                            )
                        )
                    }
                }
            )
        }
    }

    fun signInWithEmailLink(emailLink: String) {
        val emailToUse = _uiState.value.loginState.email.trim().ifEmpty {
            _uiState.value.loginState.sentDestination.trim()
        }

        _uiState.update {
            it.copy(
                loginState = it.loginState.copy(
                    isLoading = true,
                    errorMessage = null,
                    statusMessage = "Authenticating with email sign-in link..."
                )
            )
        }

        viewModelScope.launch {
            val result = repository.verifyEmailOtpOrLink(
                email = emailToUse,
                code = emailLink
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            loginState = LoginUiState(),
                            signUpState = SignUpUiState(),
                            currentScreen = AuthScreen.AUTHENTICATED
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            loginState = it.loginState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Failed to authenticate via email link."
                            )
                        )
                    }
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String? = null, email: String? = null, name: String? = null) {
        _uiState.update { it.copy(loginState = it.loginState.copy(isLoading = true, errorMessage = null)) }

        viewModelScope.launch {
            val result = repository.signInWithGoogle(idToken, email, name)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            loginState = LoginUiState(),
                            currentScreen = AuthScreen.AUTHENTICATED
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            loginState = it.loginState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Google Sign-In failed."
                            )
                        )
                    }
                }
            )
        }
    }

    fun signInWithGoogleCredential(idToken: String, email: String? = null, name: String? = null) {
        signInWithGoogle(idToken = idToken, email = email, name = name)
    }

    fun signInWithFacebook(
        token: String? = null,
        email: String? = null,
        name: String? = null,
        firebaseUid: String? = null
    ) {
        _uiState.update { it.copy(loginState = it.loginState.copy(isLoading = true, errorMessage = null)) }

        viewModelScope.launch {
            val result = repository.signInWithFacebook(
                token = token,
                email = email,
                name = name,
                firebaseUid = firebaseUid
            )
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            loginState = LoginUiState(),
                            currentScreen = AuthScreen.AUTHENTICATED
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            loginState = it.loginState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Facebook Sign-In failed."
                            )
                        )
                    }
                }
            )
        }
    }

    fun handleFacebookAuthError(error: String) {
        _uiState.update {
            it.copy(
                loginState = it.loginState.copy(
                    isLoading = false,
                    errorMessage = error
                )
            )
        }
    }

    fun setLoginLoading(isLoading: Boolean) {
        _uiState.update {
            it.copy(
                loginState = it.loginState.copy(
                    isLoading = isLoading,
                    errorMessage = if (isLoading) null else it.loginState.errorMessage
                )
            )
        }
    }

    // Compatibility methods for existing flows and tests
    fun onLoginIdentifierChanged(value: String) {
        onLoginMethodSelected(AuthMethod.EMAIL)
        onLoginEmailChanged(value)
    }

    fun onLoginPasswordChanged(value: String) {
        // Password fields removed
    }

    fun login() {
        sendLoginVerificationCode()
    }

    // Sign Up actions
    fun onSignUpFullNameChanged(value: String) {
        _uiState.update { it.copy(signUpState = it.signUpState.copy(fullName = value, errorMessage = null)) }
    }

    fun onSignUpUsernameChanged(value: String) {
        _uiState.update { it.copy(signUpState = it.signUpState.copy(username = value, errorMessage = null)) }
    }

    fun onSignUpMethodSelected(method: AuthMethod) {
        _uiState.update {
            it.copy(
                signUpState = it.signUpState.copy(
                    selectedMethod = method,
                    errorMessage = null,
                    statusMessage = null,
                    isCodeSent = false,
                    verificationCode = ""
                )
            )
        }
    }

    fun onSignUpPhoneNumberChanged(value: String) {
        // No-op compatibility
    }

    fun onSignUpEmailChanged(value: String) {
        _uiState.update { it.copy(signUpState = it.signUpState.copy(email = value, errorMessage = null)) }
    }

    fun onSignUpVerificationCodeChanged(value: String) {
        _uiState.update { it.copy(signUpState = it.signUpState.copy(verificationCode = value, errorMessage = null)) }
    }

    fun onSignUpResetCodeInput() {
        _uiState.update {
            it.copy(
                signUpState = it.signUpState.copy(
                    isCodeSent = false,
                    verificationCode = "",
                    statusMessage = null,
                    errorMessage = null
                )
            )
        }
    }

    fun sendSignUpVerificationCode(activity: android.app.Activity? = null) {
        val state = _uiState.value.signUpState
        val destination = state.email.trim()

        if (destination.isBlank()) {
            _uiState.update {
                it.copy(
                    signUpState = it.signUpState.copy(
                        errorMessage = "Please enter your email address."
                    )
                )
            }
            return
        }

        _uiState.update { it.copy(signUpState = it.signUpState.copy(isLoading = true, errorMessage = null, statusMessage = null)) }

        viewModelScope.launch {
            val result = repository.sendEmailOtpOrLink(destination)

            result.fold(
                onSuccess = { _ ->
                    _uiState.update {
                        it.copy(
                            signUpState = it.signUpState.copy(
                                isLoading = false,
                                isCodeSent = true,
                                sentDestination = destination,
                                statusMessage = "Sign-in link sent to $destination. Please check your inbox and click the link or paste it below.",
                                errorMessage = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            signUpState = it.signUpState.copy(
                                isLoading = false,
                                isCodeSent = false,
                                errorMessage = error.message ?: "Failed to send sign-in link."
                            )
                        )
                    }
                }
            )
        }
    }

    fun verifySignUpCode() {
        val state = _uiState.value.signUpState
        if (state.verificationCode.isBlank()) {
            _uiState.update {
                it.copy(signUpState = it.signUpState.copy(errorMessage = "Please enter or paste the sign-in link from your email."))
            }
            return
        }

        _uiState.update { it.copy(signUpState = it.signUpState.copy(isLoading = true, errorMessage = null)) }

        viewModelScope.launch {
            val result = repository.verifyEmailOtpOrLink(
                email = state.email,
                code = state.verificationCode,
                username = state.username.takeIf { it.isNotBlank() },
                fullName = state.fullName.takeIf { it.isNotBlank() }
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            signUpState = SignUpUiState(),
                            currentScreen = AuthScreen.AUTHENTICATED
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            signUpState = it.signUpState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Registration failed."
                            )
                        )
                    }
                }
            )
        }
    }

    fun signUpWithGoogle() {
        val state = _uiState.value.signUpState
        _uiState.update { it.copy(signUpState = it.signUpState.copy(isLoading = true, errorMessage = null)) }

        viewModelScope.launch {
            val result = repository.signInWithGoogle(
                name = state.fullName.takeIf { it.isNotBlank() }
            )
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            signUpState = SignUpUiState(),
                            currentScreen = AuthScreen.AUTHENTICATED
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            signUpState = it.signUpState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Google Sign-In failed."
                            )
                        )
                    }
                }
            )
        }
    }

    // Forgot Password actions
    fun onForgotIdentifierChanged(value: String) {
        _uiState.update { it.copy(forgotPasswordState = it.forgotPasswordState.copy(identifier = value, errorMessage = null)) }
    }

    fun onForgotInputCodeChanged(value: String) {
        _uiState.update { it.copy(forgotPasswordState = it.forgotPasswordState.copy(inputCode = value, errorMessage = null)) }
    }

    fun onForgotSecurityAnswerChanged(value: String) {
        _uiState.update { it.copy(forgotPasswordState = it.forgotPasswordState.copy(inputSecurityAnswer = value, errorMessage = null)) }
    }

    fun onForgotVerificationMethodChanged(method: RecoveryVerificationMethod) {
        _uiState.update { it.copy(forgotPasswordState = it.forgotPasswordState.copy(verificationMethod = method, errorMessage = null)) }
    }

    fun onForgotNewPasswordChanged(value: String) {
        _uiState.update { it.copy(forgotPasswordState = it.forgotPasswordState.copy(newPassword = value, errorMessage = null)) }
    }

    fun onForgotConfirmNewPasswordChanged(value: String) {
        _uiState.update { it.copy(forgotPasswordState = it.forgotPasswordState.copy(confirmNewPassword = value, errorMessage = null)) }
    }

    fun startPasswordReset() {
        val state = _uiState.value.forgotPasswordState
        if (state.identifier.isBlank()) {
            _uiState.update {
                it.copy(forgotPasswordState = it.forgotPasswordState.copy(errorMessage = "Please enter your email or username."))
            }
            return
        }

        _uiState.update { it.copy(forgotPasswordState = it.forgotPasswordState.copy(isLoading = true, errorMessage = null)) }

        viewModelScope.launch {
            val result = repository.requestPasswordReset(state.identifier)
            result.fold(
                onSuccess = { session ->
                    val masked = maskEmail(session.email)
                    _uiState.update {
                        it.copy(
                            forgotPasswordState = it.forgotPasswordState.copy(
                                step = 2,
                                isLoading = false,
                                maskedEmail = masked,
                                securityQuestion = session.securityQuestion,
                                demoRecoveryCode = session.demoRecoveryCode,
                                errorMessage = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            forgotPasswordState = it.forgotPasswordState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Account recovery request failed."
                            )
                        )
                    }
                }
            )
        }
    }

    fun verifyRecovery() {
        val state = _uiState.value.forgotPasswordState
        _uiState.update { it.copy(forgotPasswordState = it.forgotPasswordState.copy(isLoading = true, errorMessage = null)) }

        viewModelScope.launch {
            val result = if (state.verificationMethod == RecoveryVerificationMethod.VERIFICATION_CODE) {
                if (state.inputCode.isBlank()) {
                    _uiState.update {
                        it.copy(forgotPasswordState = it.forgotPasswordState.copy(isLoading = false, errorMessage = "Please enter the 6-digit verification code."))
                    }
                    return@launch
                }
                repository.verifyResetCode(state.identifier, state.inputCode)
            } else {
                if (state.inputSecurityAnswer.isBlank()) {
                    _uiState.update {
                        it.copy(forgotPasswordState = it.forgotPasswordState.copy(isLoading = false, errorMessage = "Please answer your security question."))
                    }
                    return@launch
                }
                repository.verifySecurityAnswer(state.identifier, state.inputSecurityAnswer)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            forgotPasswordState = it.forgotPasswordState.copy(
                                step = 3,
                                isVerified = true,
                                isLoading = false,
                                errorMessage = null
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            forgotPasswordState = it.forgotPasswordState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Verification failed."
                            )
                        )
                    }
                }
            )
        }
    }

    fun completePasswordReset() {
        val state = _uiState.value.forgotPasswordState
        if (state.newPassword.length < 6) {
            _uiState.update {
                it.copy(forgotPasswordState = it.forgotPasswordState.copy(errorMessage = "Password must be at least 6 characters."))
            }
            return
        }

        if (state.newPassword != state.confirmNewPassword) {
            _uiState.update {
                it.copy(forgotPasswordState = it.forgotPasswordState.copy(errorMessage = "Passwords do not match."))
            }
            return
        }

        _uiState.update { it.copy(forgotPasswordState = it.forgotPasswordState.copy(isLoading = true, errorMessage = null)) }

        viewModelScope.launch {
            val result = repository.resetPassword(state.identifier, state.newPassword)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            forgotPasswordState = it.forgotPasswordState.copy(
                                step = 4,
                                isLoading = false,
                                errorMessage = null,
                                successMessage = "Your password has been successfully reset! You can now sign in."
                            ),
                            loginState = it.loginState.copy(
                                identifier = state.identifier,
                                password = ""
                            )
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            forgotPasswordState = it.forgotPasswordState.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Failed to reset password."
                            )
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update {
                it.copy(
                    currentUser = null,
                    currentScreen = AuthScreen.LOGIN
                )
            }
        }
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email
        val name = parts[0]
        val domain = parts[1]
        val maskedName = if (name.length > 2) {
            name.first() + "***" + name.last()
        } else {
            name.first() + "***"
        }
        return "$maskedName@$domain"
    }

    companion object {
        fun provideFactory(repository: AuthRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(repository) as T
                }
            }
        }
    }
}
