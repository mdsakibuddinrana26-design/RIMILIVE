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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GamiHeader
import com.example.ui.components.GamiPasswordField
import com.example.ui.components.GamiPrimaryButton
import com.example.ui.components.GamiStatusBanner
import com.example.ui.components.GamiTextField
import com.example.ui.theme.GamiBackgroundDark
import com.example.ui.theme.GamiBorderDark
import com.example.ui.theme.GamiCyanAccent
import com.example.ui.theme.GamiIndigoLight
import com.example.ui.theme.GamiIndigoPrimary
import com.example.ui.theme.GamiSuccess
import com.example.ui.theme.GamiSurfaceDark
import com.example.ui.theme.GamiSurfaceElevated
import com.example.ui.theme.GamiTextMuted
import com.example.ui.theme.GamiTextPrimary
import com.example.ui.theme.GamiTextSecondary

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onIdentifierChange: (String) -> Unit,
    onInputCodeChange: (String) -> Unit,
    onSecurityAnswerChange: (String) -> Unit,
    onVerificationMethodChange: (RecoveryVerificationMethod) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmNewPasswordChange: (String) -> Unit,
    onStartResetClick: () -> Unit,
    onVerifyRecoveryClick: () -> Unit,
    onCompleteResetClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToLoginClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GamiSurfaceElevated)
                        .testTag("forgot_password_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = GamiTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Account Recovery",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GamiTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header
            GamiHeader(
                subtitle = "Secure multi-factor password recovery & reset"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Indicator Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecoveryStepIndicator(stepNumber = 1, label = "Identify", isActive = state.step >= 1, isCompleted = state.step > 1)
                Spacer(modifier = Modifier.width(8.dp))
                StepConnector(isCompleted = state.step > 1)
                Spacer(modifier = Modifier.width(8.dp))
                RecoveryStepIndicator(stepNumber = 2, label = "Verify", isActive = state.step >= 2, isCompleted = state.step > 2)
                Spacer(modifier = Modifier.width(8.dp))
                StepConnector(isCompleted = state.step > 2)
                Spacer(modifier = Modifier.width(8.dp))
                RecoveryStepIndicator(stepNumber = 3, label = "New Password", isActive = state.step >= 3, isCompleted = state.step > 3)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Card
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
                    if (!state.errorMessage.isNullOrEmpty()) {
                        GamiStatusBanner(
                            message = state.errorMessage,
                            isSuccess = false
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    when (state.step) {
                        1 -> {
                            // Step 1: Enter Identifier
                            Text(
                                text = "Find Your Account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamiTextPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Enter the email or username associated with your RIMI account.",
                                fontSize = 13.sp,
                                color = GamiTextSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            GamiTextField(
                                value = state.identifier,
                                onValueChange = onIdentifierChange,
                                label = "Email or Username",
                                placeholder = "e.g. user@gamilive.com",
                                leadingIcon = Icons.Default.Person,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        onStartResetClick()
                                    }
                                ),
                                testTag = "forgot_identifier_input"
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            GamiPrimaryButton(
                                text = "Continue",
                                onClick = {
                                    focusManager.clearFocus()
                                    onStartResetClick()
                                },
                                isLoading = state.isLoading,
                                enabled = !state.isLoading,
                                leadingIcon = Icons.Default.Security,
                                testTag = "forgot_continue_button"
                            )
                        }

                        2 -> {
                            // Step 2: Verification
                            Text(
                                text = "Verify Identity",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamiTextPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Verification code issued for ${state.maskedEmail}. Choose verification method below:",
                                fontSize = 13.sp,
                                color = GamiTextSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Verification Method Switcher
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GamiSurfaceElevated)
                                    .padding(4.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onVerificationMethodChange(RecoveryVerificationMethod.VERIFICATION_CODE) },
                                    color = if (state.verificationMethod == RecoveryVerificationMethod.VERIFICATION_CODE) GamiIndigoPrimary else Color.Transparent
                                ) {
                                    Text(
                                        text = "Reset Code",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.verificationMethod == RecoveryVerificationMethod.VERIFICATION_CODE) Color.White else GamiTextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onVerificationMethodChange(RecoveryVerificationMethod.SECURITY_QUESTION) },
                                    color = if (state.verificationMethod == RecoveryVerificationMethod.SECURITY_QUESTION) GamiIndigoPrimary else Color.Transparent
                                ) {
                                    Text(
                                        text = "Security Q&A",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state.verificationMethod == RecoveryVerificationMethod.SECURITY_QUESTION) Color.White else GamiTextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.verificationMethod == RecoveryVerificationMethod.VERIFICATION_CODE) {
                                // Demo hint helper for easy verification testing
                                if (!state.demoRecoveryCode.isNullOrEmpty()) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, GamiCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                        color = Color(0xFF07212F)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MarkEmailRead,
                                                contentDescription = null,
                                                tint = GamiCyanAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Recovery Code: ${state.demoRecoveryCode}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GamiCyanAccent
                                                )
                                                Text(
                                                    text = "Issued by security server (valid 15m)",
                                                    fontSize = 11.sp,
                                                    color = GamiTextMuted
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                GamiTextField(
                                    value = state.inputCode,
                                    onValueChange = onInputCodeChange,
                                    label = "6-Digit Code",
                                    placeholder = "e.g. ${state.demoRecoveryCode ?: "123456"}",
                                    leadingIcon = Icons.Default.VpnKey,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            onVerifyRecoveryClick()
                                        }
                                    ),
                                    testTag = "forgot_code_input"
                                )
                            } else {
                                // Security Question
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp)),
                                    color = GamiSurfaceElevated
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Your Security Question:",
                                            fontSize = 11.sp,
                                            color = GamiTextMuted
                                        )
                                        Text(
                                            text = state.securityQuestion.ifEmpty { "Security Question on file" },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GamiTextPrimary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                GamiTextField(
                                    value = state.inputSecurityAnswer,
                                    onValueChange = onSecurityAnswerChange,
                                    label = "Security Answer",
                                    placeholder = "Enter your secret answer",
                                    leadingIcon = Icons.Default.QuestionAnswer,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            onVerifyRecoveryClick()
                                        }
                                    ),
                                    testTag = "forgot_security_answer_input"
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            GamiPrimaryButton(
                                text = "Verify Identity",
                                onClick = {
                                    focusManager.clearFocus()
                                    onVerifyRecoveryClick()
                                },
                                isLoading = state.isLoading,
                                enabled = !state.isLoading,
                                leadingIcon = Icons.Default.CheckCircle,
                                testTag = "forgot_verify_button"
                            )
                        }

                        3 -> {
                            // Step 3: Set New Password
                            Text(
                                text = "Create New Password",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamiTextPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Your identity is verified. Enter a secure new password for your account.",
                                fontSize = 13.sp,
                                color = GamiTextSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            GamiPasswordField(
                                value = state.newPassword,
                                onValueChange = onNewPasswordChange,
                                label = "New Password (min 6 chars)",
                                placeholder = "Enter new password",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                testTag = "forgot_new_password_input"
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            GamiPasswordField(
                                value = state.confirmNewPassword,
                                onValueChange = onConfirmNewPasswordChange,
                                label = "Confirm New Password",
                                placeholder = "Re-enter new password",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        onCompleteResetClick()
                                    }
                                ),
                                testTag = "forgot_confirm_password_input"
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            GamiPrimaryButton(
                                text = "Reset Password & Save",
                                onClick = {
                                    focusManager.clearFocus()
                                    onCompleteResetClick()
                                },
                                isLoading = state.isLoading,
                                enabled = !state.isLoading,
                                leadingIcon = Icons.Default.Key,
                                testTag = "forgot_save_password_button"
                            )
                        }

                        4 -> {
                            // Step 4: Success
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = GamiSuccess,
                                modifier = Modifier.size(56.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Password Updated!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = GamiTextPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = state.successMessage ?: "Your password has been reset securely. You can now login with your new credentials.",
                                fontSize = 13.sp,
                                color = GamiTextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            GamiPrimaryButton(
                                text = "Sign In Now",
                                onClick = onBackToLoginClick,
                                leadingIcon = Icons.Default.LockOpen,
                                testTag = "forgot_success_signin_button"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RecoveryStepIndicator(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> GamiSuccess
                        isActive -> GamiIndigoPrimary
                        else -> GamiSurfaceElevated
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    color = if (isActive) Color.White else GamiTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) GamiTextPrimary else GamiTextMuted,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun StepConnector(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(2.dp)
            .background(if (isCompleted) GamiSuccess else GamiBorderDark)
    )
}
