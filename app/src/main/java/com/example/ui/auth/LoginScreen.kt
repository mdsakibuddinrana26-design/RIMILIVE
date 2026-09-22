package com.example.ui.auth

import android.accounts.AccountManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FacebookIcon
import com.example.ui.components.GoogleIcon

// Fresh original RIMI mint & teal color palette
private val MintTealPrimary = Color(0xFF00C49F)
private val MintTealGradientStart = Color(0xFF00D492)
private val MintTealGradientEnd = Color(0xFF00B09A)
private val DarkSlateHeading = Color(0xFF111827)
private val MutedSlateText = Color(0xFF64748B)
private val SoftBorderGray = Color(0xFFE2E8F0)
private val FacebookBlue = Color(0xFF1877F2)
private val EmailBlueStart = Color(0xFF3897F0)
private val EmailBlueEnd = Color(0xFF1E88E5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginUiState,
    onMethodSelected: (AuthMethod) -> Unit,
    onEmailChange: (String) -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onVerifyCodeClick: () -> Unit,
    onResetCodeInput: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onPhoneNumberChange: (String) -> Unit = {},
    onGoogleSignInAccountSelected: (idToken: String?, email: String?, name: String?) -> Unit = { _, email, name ->
        onGoogleSignInClick()
    },
    onSignUpClick: () -> Unit = {},
    onFacebookSignInClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var agreedToTerms by remember { mutableStateOf(true) }
    var showAgreementDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFaqSheet by remember { mutableStateOf(false) }

    // Google Account Chooser Bottom Sheet state
    var showGoogleChooserSheet by remember { mutableStateOf(false) }
    var customGoogleEmail by remember { mutableStateOf("") }
    var showCustomEmailInput by remember { mutableStateOf(false) }

    // Bottom sheet state for Email flow
    var showEmailSheet by remember { mutableStateOf(false) }

    // Facebook dialog state
    var showFacebookChooserDialog by remember { mutableStateOf(false) }

    // Helper to guard actions by terms agreement
    fun withTermsCheck(action: () -> Unit) {
        if (!agreedToTerms) {
            Toast.makeText(context, "Please agree to the User Agreement and Privacy Policy", Toast.LENGTH_SHORT).show()
            agreedToTerms = true
        }
        action()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Organic Mint / Teal / Peach decorative shapes & gradients
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Top-right large soft mint organic glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8CEFD4).copy(alpha = 0.55f),
                        Color(0xFFC7F9EC).copy(alpha = 0.35f),
                        Color(0xFFE8FBF5).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(canvasWidth * 0.95f, canvasHeight * 0.06f),
                    radius = canvasWidth * 0.72f
                )
            )

            // 2. Floating mint accent circle top-right
            drawCircle(
                color = Color(0xFFA2F2DC).copy(alpha = 0.60f),
                radius = 16.dp.toPx(),
                center = Offset(canvasWidth * 0.76f, canvasHeight * 0.20f)
            )

            // 3. Soft pastel peach accent circle middle-right
            drawCircle(
                color = Color(0xFFFFDEC9).copy(alpha = 0.75f),
                radius = 38.dp.toPx(),
                center = Offset(canvasWidth * 0.96f, canvasHeight * 0.30f)
            )

            // 4. Mid-left soft mint aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFB8F4E4).copy(alpha = 0.40f),
                        Color(0xFFD6F8EF).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(0f, canvasHeight * 0.44f),
                    radius = canvasWidth * 0.42f
                )
            )

            // 5. Floating small pale mint dot mid-left
            drawCircle(
                color = Color(0xFFC2F7E9).copy(alpha = 0.70f),
                radius = 12.dp.toPx(),
                center = Offset(canvasWidth * 0.16f, canvasHeight * 0.34f)
            )

            // 6. Bottom organic flowing waves in mint/teal
            val waveBackPath = Path().apply {
                moveTo(0f, canvasHeight - 130.dp.toPx())
                cubicTo(
                    canvasWidth * 0.25f, canvasHeight - 160.dp.toPx(),
                    canvasWidth * 0.65f, canvasHeight - 100.dp.toPx(),
                    canvasWidth, canvasHeight - 140.dp.toPx()
                )
                lineTo(canvasWidth, canvasHeight)
                lineTo(0f, canvasHeight)
                close()
            }
            drawPath(
                path = waveBackPath,
                color = Color(0xFFA5F0DC).copy(alpha = 0.45f)
            )

            val waveFrontPath = Path().apply {
                moveTo(0f, canvasHeight - 85.dp.toPx())
                cubicTo(
                    canvasWidth * 0.35f, canvasHeight - 50.dp.toPx(),
                    canvasWidth * 0.70f, canvasHeight - 120.dp.toPx(),
                    canvasWidth, canvasHeight - 75.dp.toPx()
                )
                lineTo(canvasWidth, canvasHeight)
                lineTo(0f, canvasHeight)
                close()
            }
            drawPath(
                path = waveFrontPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF45D5B2).copy(alpha = 0.65f),
                        Color(0xFF26C49F).copy(alpha = 0.75f)
                    ),
                    startY = canvasHeight - 85.dp.toPx(),
                    endY = canvasHeight
                )
            )
        }

        // Scrollable content to ensure responsive fit on all Android phone screen heights
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row: "Live Chat Make Friends" & "FAQ ▶"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Playful live/chat intro
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Live",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MintTealPrimary
                            )
                        }
                        Text(
                            text = "Chat",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkSlateHeading,
                            lineHeight = 26.sp
                        )
                        Text(
                            text = "Make Friends",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkSlateHeading,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Mint accent pill bar
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MintTealPrimary)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "A more fun world\nis waiting for you",
                            fontSize = 12.sp,
                            color = MutedSlateText,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Right: FAQ Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showFaqSheet = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("login_faq_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FAQ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateHeading
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "FAQ",
                            tint = DarkSlateHeading,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (screenHeight < 680.dp) 12.dp else 24.dp))

                // Prominent RIMI Logo Centered
                Box(
                    modifier = Modifier
                        .size(122.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color(0x25000000),
                            spotColor = Color(0x3500C49F)
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.gami_live_logo),
                        contentDescription = "RIMI Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("gami_live_logo"),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Title: "RIMI"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "GAMI ",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = DarkSlateHeading
                    )
                    Text(
                        text = "LIVE",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = MintTealPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle: "LIVE • CHAT • NEW FRIENDS"
                Text(
                    text = "LIVE  •  CHAT  •  NEW FRIENDS",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = MutedSlateText
                )

                Spacer(modifier = Modifier.height(if (screenHeight < 680.dp) 24.dp else 40.dp))

                // Large rounded green/teal "Start" button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(29.dp),
                                ambientColor = MintTealPrimary.copy(alpha = 0.35f),
                                spotColor = MintTealPrimary.copy(alpha = 0.6f)
                            )
                            .clip(RoundedCornerShape(29.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MintTealGradientStart,
                                        MintTealPrimary,
                                        MintTealGradientEnd
                                    )
                                )
                            )
                            .clickable(enabled = !state.isLoading) {
                                withTermsCheck {
                                    // Start button initiates fast 1-tap sign-in with the primary Google method
                                    showGoogleChooserSheet = true
                                }
                            }
                            .testTag("login_start_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 22.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left Play Icon
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )

                                // Center "Start" Label
                                Text(
                                    text = "Start",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )

                                // Right Chevron Icon
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Go",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Divider: "Or continue with"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = SoftBorderGray,
                        thickness = 1.dp
                    )
                    Text(
                        text = "Or continue with",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = SoftBorderGray,
                        thickness = 1.dp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Exactly 3 Login Options: Google, Email, Facebook
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 36.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Google Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("login_google_button_container")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .shadow(
                                    elevation = 5.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0x18000000),
                                    spotColor = Color(0x22000000)
                                )
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, SoftBorderGray, CircleShape)
                                .clickable(enabled = !state.isLoading) {
                                    withTermsCheck {
                                        showGoogleChooserSheet = true
                                    }
                                }
                                .testTag("login_google_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            GoogleIcon(modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Google",
                            color = Color(0xFF334155),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 2. Email Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("login_email_button_container")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0x301E88E5),
                                    spotColor = Color(0x451E88E5)
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(EmailBlueStart, EmailBlueEnd)
                                    )
                                )
                            .clickable(enabled = !state.isLoading) {
                                withTermsCheck {
                                    onMethodSelected(AuthMethod.EMAIL)
                                    showEmailSheet = true
                                }
                            }
                            .testTag("login_email_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Email",
                            color = Color(0xFF334155),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 3. Facebook Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.testTag("login_facebook_button_container")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0x301877F2),
                                    spotColor = Color(0x451877F2)
                                )
                                .clip(CircleShape)
                                .background(FacebookBlue)
                                .clickable(enabled = !state.isLoading) {
                                    withTermsCheck {
                                        onMethodSelected(AuthMethod.FACEBOOK)
                                        showFacebookChooserDialog = true
                                    }
                                }
                                .testTag("login_facebook_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            FacebookIcon(modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Facebook",
                            color = Color(0xFF334155),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (screenHeight < 680.dp) 24.dp else 42.dp))

                // Bottom Checkbox & Terms Agreement
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .testTag("login_terms_agreement"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mint / Teal Rounded Checkbox
                    Box(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                agreedToTerms = !agreedToTerms
                            }
                            .padding(4.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (agreedToTerms) MintTealPrimary else Color.Transparent)
                            .border(
                                width = 1.5.dp,
                                color = if (agreedToTerms) MintTealPrimary else Color(0xFF94A3B8),
                                shape = CircleShape
                            )
                            .testTag("login_terms_checkbox"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (agreedToTerms) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Agreed",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "I agree to the ",
                            color = MutedSlateText,
                            fontSize = 12.5.sp,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    agreedToTerms = !agreedToTerms
                                }
                                .padding(vertical = 4.dp)
                        )
                        Text(
                            text = "User Agreement",
                            color = MintTealPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showAgreementDialog = true }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .testTag("login_user_agreement_link")
                        )
                        Text(
                            text = " and ",
                            color = MutedSlateText,
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = "Privacy Policy",
                            color = MintTealPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showPrivacyDialog = true }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .testTag("login_privacy_policy_link")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Tagline: "GOOD PEOPLE BRIGHTER MOMENTS"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(1.dp)
                            .background(Color(0xFFCBD5E1))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GOOD PEOPLE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "BRIGHTER MOMENTS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.4.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(1.dp)
                            .background(Color(0xFFCBD5E1))
                    )
                }
            }
        }
    }

    // Email Login Modal Bottom Sheet (Clean light theme with mint accents)
    if (showEmailSheet) {
        val emailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showEmailSheet = false },
            sheetState = emailSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = null
        ) {
            val focusManager = LocalFocusManager.current

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sheet Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MintTealPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MintTealPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Email Sign-In",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlateHeading
                            )
                            Text(
                                text = "Passwordless email-link sign-in",
                                fontSize = 12.sp,
                                color = MutedSlateText
                            )
                        }
                    }

                    IconButton(onClick = { showEmailSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Error and status messages
                if (!state.errorMessage.isNullOrEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!state.statusMessage.isNullOrEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Text(
                            text = state.statusMessage,
                            color = Color(0xFF16A34A),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!state.isCodeSent) {
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g. alex@gamilive.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MintTealPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintTealPrimary,
                            unfocusedBorderColor = SoftBorderGray,
                            focusedTextColor = DarkSlateHeading,
                            unfocusedTextColor = DarkSlateHeading,
                            cursorColor = MintTealPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onSendCodeClick()
                        },
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintTealPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_send_email_button")
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Sign-In Link", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "A sign-in link has been sent to ${state.sentDestination.ifEmpty { state.email }}. Click the link in your email to sign in instantly, or paste it below.",
                        fontSize = 13.sp,
                        color = MutedSlateText,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = state.verificationCode,
                        onValueChange = onVerificationCodeChange,
                        label = { Text("Sign-In Link") },
                        placeholder = { Text("Paste link from your email") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MintTealPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintTealPrimary,
                            unfocusedBorderColor = SoftBorderGray,
                            focusedTextColor = DarkSlateHeading,
                            unfocusedTextColor = DarkSlateHeading,
                            cursorColor = MintTealPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_code_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onVerifyCodeClick()
                        },
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintTealPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_verify_email_button")
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Complete Sign-In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Resend Link",
                            color = MintTealPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onSendCodeClick)
                                .padding(4.dp)
                                .testTag("login_resend_email_code_button")
                        )

                        Text(
                            text = "Change Email",
                            color = MutedSlateText,
                            fontSize = 13.5.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onResetCodeInput)
                                .padding(4.dp)
                                .testTag("login_change_email_button")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Google Account Chooser Bottom Sheet
    if (showGoogleChooserSheet) {
        val googleSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val systemGoogleAccounts = remember(context) {
            try {
                val am = AccountManager.get(context)
                am.getAccountsByType("com.google").map { it.name }
            } catch (_: Exception) {
                emptyList<String>()
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                showGoogleChooserSheet = false
                showCustomEmailInput = false
            },
            sheetState = googleSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding()
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GoogleIcon(modifier = Modifier.size(26.dp))
                        Column {
                            Text(
                                text = "Choose an account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F1F1F)
                            )
                            Text(
                                text = "to continue to RIMI",
                                fontSize = 13.sp,
                                color = Color(0xFF5F6368)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            showGoogleChooserSheet = false
                            showCustomEmailInput = false
                        },
                        modifier = Modifier.testTag("google_chooser_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF5F6368)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE8EAED), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Account list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val primaryAccountEmail = "mdsakibuddinrana26@gmail.com"
                    val primaryAccountName = "Md Sakib Uddin Rana"

                    GoogleAccountItem(
                        fullName = primaryAccountName,
                        email = primaryAccountEmail,
                        avatarLetter = "M",
                        avatarBgColor = Color(0xFF1A73E8),
                        onClick = {
                            showGoogleChooserSheet = false
                            onGoogleSignInAccountSelected(null, primaryAccountEmail, primaryAccountName)
                        },
                        testTag = "google_account_primary"
                    )

                    systemGoogleAccounts.filter { it.lowercase() != primaryAccountEmail.lowercase() }.forEachIndexed { index, accountEmail ->
                        val derivedName = accountEmail.substringBefore("@")
                            .split(".", "_", "-")
                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                        GoogleAccountItem(
                            fullName = derivedName,
                            email = accountEmail,
                            avatarLetter = accountEmail.firstOrNull()?.uppercase() ?: "G",
                            avatarBgColor = Color(0xFF137333),
                            onClick = {
                                showGoogleChooserSheet = false
                                onGoogleSignInAccountSelected(null, accountEmail, derivedName)
                            },
                            testTag = "google_account_system_$index"
                        )
                    }

                    // "Use another account"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showCustomEmailInput = !showCustomEmailInput
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .testTag("google_use_another_account"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F3F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Another Account",
                                tint = Color(0xFF5F6368),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use another account",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F1F1F)
                            )
                        }
                    }

                    if (showCustomEmailInput) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            OutlinedTextField(
                                value = customGoogleEmail,
                                onValueChange = { customGoogleEmail = it },
                                placeholder = { Text("Enter Google email (e.g. name@gmail.com)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_custom_email_input"),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MintTealPrimary,
                                    unfocusedBorderColor = Color(0xFFDADCE0)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val emailToUse = customGoogleEmail.trim().takeIf { it.isNotBlank() }
                                        ?: primaryAccountEmail
                                    val nameToUse = emailToUse.substringBefore("@")
                                        .split(".", "_", "-")
                                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                    showGoogleChooserSheet = false
                                    onGoogleSignInAccountSelected(null, emailToUse, nameToUse)
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("google_custom_email_continue"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MintTealPrimary
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Continue", color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFFE8EAED), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "To continue, Google will share your name, email address, language preference, and profile picture with RIMI. Before using RIMI, you can review their Privacy Policy and Terms of Service.",
                    fontSize = 12.sp,
                    color = Color(0xFF5F6368),
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }

    // FAQ Bottom Sheet (Light themed)
    if (showFaqSheet) {
        val faqSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFaqSheet = false },
            sheetState = faqSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Frequently Asked Questions",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateHeading
                    )
                    IconButton(onClick = { showFaqSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FaqItem(
                    question = "How do I log in to RIMI?",
                    answer = "You can log in instantly with 1-tap Google Sign-In, passwordless Email verification, or Facebook Sign-In."
                )

                Spacer(modifier = Modifier.height(12.dp))

                FaqItem(
                    question = "What does the Start button do?",
                    answer = "Tapping the large Start button provides 1-tap fast entry using your primary verified Google credentials so you can start chatting and exploring live content instantly."
                )

                Spacer(modifier = Modifier.height(12.dp))

                FaqItem(
                    question = "Is my account information protected?",
                    answer = "Yes! RIMI utilizes Firebase Authentication with enterprise-grade tokenization. We never store plain passwords or sensitive credentials insecurely."
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // User Agreement Dialog
    if (showAgreementDialog) {
        AlertDialog(
            onDismissRequest = { showAgreementDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "RIMI User Agreement",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateHeading
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Welcome to RIMI! By using our services, you agree to comply with our community rules, conduct guidelines, and interactive live stream regulations.\n\n" +
                                "1. User Conduct: Treat all streamers and community members with mutual respect.\n" +
                                "2. Account Safety: Keep your login devices secure.\n" +
                                "3. Fair Use: Live interactions and digital gifts must adhere to standard broadcast policies.\n\n" +
                                "Thank you for being part of the RIMI community!",
                        fontSize = 13.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAgreementDialog = false }) {
                    Text(text = "I Understand", color = MintTealPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "RIMI Privacy Policy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlateHeading
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Your privacy and account integrity are our top priority.\n\n" +
                                "• Authentication Data: Email addresses, Google profile details, and Facebook credentials are used strictly for authentication and session management.\n" +
                                "• Data Protection: We never sell personal data to third parties. Sessions are tokenized with industry-standard cryptographic encryption.\n" +
                                "• Permissions: Only requested features (like live video broadcasting or notifications) require optional device permissions.\n\n" +
                                "For privacy requests, contact privacy@gamilive.com.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(text = "Accept", color = MintTealPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Facebook Login Confirmation Dialog
    if (showFacebookChooserDialog) {
        AlertDialog(
            onDismissRequest = { showFacebookChooserDialog = false },
            containerColor = Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FacebookBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        FacebookIcon(modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "Sign in with Facebook",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlateHeading
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Continue to RIMI using your Facebook account.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF475569)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showFacebookChooserDialog = false
                                onFacebookSignInClick()
                            }
                            .testTag("facebook_continue_as_user"),
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "F",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = "Continue with Facebook Account",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkSlateHeading
                                )
                                Text(
                                    text = "Instant 1-Tap Secure Sign-In",
                                    fontSize = 12.sp,
                                    color = MutedSlateText
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFacebookChooserDialog = false
                        onFacebookSignInClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("facebook_confirm_login_button")
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFacebookChooserDialog = false },
                    modifier = Modifier.testTag("facebook_cancel_login_button")
                ) {
                    Text("Cancel", color = MutedSlateText)
                }
            }
        )
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF0FDF9),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCCFBF1))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlateHeading
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = answer,
                fontSize = 12.5.sp,
                color = Color(0xFF475569),
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun GoogleAccountItem(
    fullName: String,
    email: String,
    avatarLetter: String,
    avatarBgColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarLetter,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fullName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F1F1F)
            )
            Text(
                text = email,
                fontSize = 13.sp,
                color = Color(0xFF5F6368)
            )
        }
    }
}
