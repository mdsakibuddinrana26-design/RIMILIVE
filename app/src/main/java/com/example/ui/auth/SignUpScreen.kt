package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GamiHeader
import com.example.ui.components.GamiPrimaryButton
import com.example.ui.components.GamiStatusBanner
import com.example.ui.components.GamiTextField
import com.example.ui.components.GoogleSignInButton
import com.example.ui.theme.GamiBackgroundDark
import com.example.ui.theme.GamiBorderDark
import com.example.ui.theme.GamiCyanAccent
import com.example.ui.theme.GamiIndigoLight
import com.example.ui.theme.GamiIndigoPrimary
import com.example.ui.theme.GamiSurfaceDark
import com.example.ui.theme.GamiTextMuted
import com.example.ui.theme.GamiTextPrimary
import com.example.ui.theme.GamiTextSecondary

@Composable
fun SignUpScreen(
    state: SignUpUiState,
    onFullNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onMethodSelected: (AuthMethod) -> Unit,
    onEmailChange: (String) -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onVerifyCodeClick: () -> Unit,
    onResetCodeInput: () -> Unit,
    onGoogleSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
    onPhoneNumberChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0B24),
                        GamiBackgroundDark,
                        Color(0xFF0A0E1A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Branded Top Header
            GamiHeader(
                subtitle = "Create your profile to enter GAMI LIVE"
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main SignUp Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GamiBorderDark.copy(alpha = 0.8f),
                                GamiIndigoPrimary.copy(alpha = 0.3f),
                                GamiBorderDark.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                color = GamiSurfaceDark.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "New User Registration",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamiTextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Set up your profile and choose an option to continue",
                        fontSize = 13.sp,
                        color = GamiTextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!state.errorMessage.isNullOrEmpty()) {
                        GamiStatusBanner(
                            message = state.errorMessage,
                            isSuccess = false
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (!state.statusMessage.isNullOrEmpty()) {
                        GamiStatusBanner(
                            message = state.statusMessage,
                            isSuccess = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Full Name
                    GamiTextField(
                        value = state.fullName,
                        onValueChange = onFullNameChange,
                        label = "Full Name",
                        placeholder = "e.g. Alex Hunter",
                        leadingIcon = Icons.Default.Badge,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "signup_fullname_input"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Username
                    GamiTextField(
                        value = state.username,
                        onValueChange = onUsernameChange,
                        label = "Username",
                        placeholder = "e.g. alex_gami",
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "signup_username_input"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Google Sign-In Option
                    GoogleSignInButton(
                        onClick = onGoogleSignUpClick,
                        isLoading = state.isLoading,
                        enabled = !state.isLoading,
                        text = "Continue with Google",
                        testTag = "signup_google_button"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Divider with "OR CONTINUE WITH"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = GamiBorderDark
                        )
                        Text(
                            text = "OR SIGN UP WITH EMAIL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GamiTextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = GamiBorderDark
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Email Flow
                    if (!state.isCodeSent) {
                        GamiTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = "Email Address",
                            placeholder = "e.g. alex@gamilive.com",
                            leadingIcon = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    onSendCodeClick()
                                }
                            ),
                            testTag = "signup_email_input"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Passwordless sign-in: A sign-in link will be sent to your email address.",
                            color = GamiTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GamiPrimaryButton(
                            text = "Send Sign-In Link",
                            onClick = {
                                focusManager.clearFocus()
                                onSendCodeClick()
                            },
                            isLoading = state.isLoading,
                            enabled = !state.isLoading,
                            leadingIcon = Icons.Default.Send,
                            testTag = "signup_send_email_button"
                        )
                    } else {
                        Text(
                            text = "A sign-in link was sent to ${state.sentDestination.ifEmpty { state.email }}. Click the link in your email to sign up, or paste it below.",
                            color = GamiTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email Link input
                        GamiTextField(
                            value = state.verificationCode,
                            onValueChange = onVerificationCodeChange,
                            label = "Sign-In Link",
                            placeholder = "Paste link from your email",
                            leadingIcon = Icons.Default.Link,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    onVerifyCodeClick()
                                }
                            ),
                            testTag = "signup_email_code_input"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GamiPrimaryButton(
                            text = "Complete Registration",
                            onClick = {
                                focusManager.clearFocus()
                                onVerifyCodeClick()
                            },
                            isLoading = state.isLoading,
                            enabled = !state.isLoading,
                            leadingIcon = Icons.Default.Check,
                            testTag = "signup_verify_email_button"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Resend Link",
                                color = GamiCyanAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable(onClick = onSendCodeClick)
                                    .padding(4.dp)
                                    .testTag("signup_resend_email_code_button")
                            )

                            Text(
                                text = "Change Email",
                                color = GamiTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable(onClick = onResetCodeInput)
                                    .padding(4.dp)
                                    .testTag("signup_change_email_button")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch to Sign In
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Already have an account?",
                    color = GamiTextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign In",
                    color = GamiIndigoLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onLoginClick)
                        .padding(vertical = 8.dp)
                        .testTag("signup_goto_login_button")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
