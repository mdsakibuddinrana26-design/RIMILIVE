package com.example.ui.auth

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ProfileGreen = Color(0xFF13C9A3)
private val ProfileGreenDark = Color(0xFF08A98A)
private val ProfileText = Color(0xFF17202A)
private val ProfileGray = Color(0xFF7C8796)
private val ProfileBorder = Color(0xFFE4ECE9)

@Composable
fun CompleteProfileScreen(
    initialName: String = "",
    onContinue: (String, String, String, Uri?) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialName) }
    var gender by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) photoUri = uri
    }

    val bitmap = remember(photoUri) {
        photoUri?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFB8F4E4).copy(alpha = 0.48f),
                        Color(0xFFE7FAF5).copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.08f),
                    radius = size.width * 0.70f
                )
            )

            drawCircle(
                color = Color(0xFFD9F8EF).copy(alpha = 0.55f),
                radius = 35.dp.toPx(),
                center = Offset(size.width * 0.08f, size.height * 0.45f)
            )

            drawCircle(
                color = Color(0xFFFFE5D2).copy(alpha = 0.45f),
                radius = 42.dp.toPx(),
                center = Offset(size.width * 0.95f, size.height * 0.55f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "GAMI LIVE",
                color = ProfileGreenDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Complete Your Profile",
                color = ProfileText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tell us a little about yourself",
                color = ProfileGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2FAF7))
                    .clickable { photoLauncher.launch("image/*") }
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Add Photo",
                            tint = ProfileGreenDark,
                            modifier = Modifier.size(34.dp)
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "Add Photo",
                            color = ProfileGreenDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ProfileText,
                    unfocusedTextColor = ProfileText,
                    focusedBorderColor = ProfileGreen,
                    unfocusedBorderColor = ProfileBorder,
                    focusedLabelColor = ProfileGreenDark,
                    unfocusedLabelColor = ProfileGray,
                    cursorColor = ProfileGreenDark,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Gender",
                modifier = Modifier.fillMaxWidth(),
                color = ProfileText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                FilterChip(
                    selected = gender == "Male",
                    onClick = { gender = "Male" },
                    label = {
                        Text(
                            text = "Male",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDDF8F1),
                        selectedLabelColor = ProfileGreenDark,
                        containerColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = gender == "Male",
                        borderColor = ProfileBorder,
                        selectedBorderColor = ProfileGreen
                    )
                )

                FilterChip(
                    selected = gender == "Female",
                    onClick = { gender = "Female" },
                    label = {
                        Text(
                            text = "Female",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDDF8F1),
                        selectedLabelColor = ProfileGreenDark,
                        containerColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = gender == "Female",
                        borderColor = ProfileBorder,
                        selectedBorderColor = ProfileGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Date of Birth",
                modifier = Modifier.fillMaxWidth(),
                color = ProfileText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = birthDay,
                    onValueChange = { input ->
                        birthDay = input.filter { it.isDigit() }.take(2)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "DD",
                            color = ProfileGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProfileText,
                        unfocusedTextColor = ProfileText,
                        focusedBorderColor = ProfileGreen,
                        unfocusedBorderColor = ProfileBorder,
                        cursorColor = ProfileGreenDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = birthMonth,
                    onValueChange = { input ->
                        birthMonth = input.filter { it.isDigit() }.take(2)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "MM",
                            color = ProfileGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProfileText,
                        unfocusedTextColor = ProfileText,
                        focusedBorderColor = ProfileGreen,
                        unfocusedBorderColor = ProfileBorder,
                        cursorColor = ProfileGreenDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { input ->
                        birthYear = input.filter { it.isDigit() }.take(4)
                    },
                    modifier = Modifier.weight(1.4f),
                    placeholder = {
                        Text(
                            "YYYY",
                            color = ProfileGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ProfileText,
                        unfocusedTextColor = ProfileText,
                        focusedBorderColor = ProfileGreen,
                        unfocusedBorderColor = ProfileBorder,
                        cursorColor = ProfileGreenDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            Button(
                onClick = {
                    onContinue(
                        name.trim(),
                        gender,
                        "${birthDay.padStart(2, '0')}/${birthMonth.padStart(2, '0')}/$birthYear",
                        photoUri
                    )
                },
                enabled = name.isNotBlank() &&
                        gender.isNotBlank() &&
                        birthDay.length == 2 &&
                        birthMonth.length == 2 &&
                        birthYear.length == 4 &&
                        (birthDay.toIntOrNull() ?: 0) in 1..31 &&
                        (birthMonth.toIntOrNull() ?: 0) in 1..12 &&
                        (birthYear.toIntOrNull() ?: 0) in 1900..2100,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileGreen,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFB9EADF)
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
