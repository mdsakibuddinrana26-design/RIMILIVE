package com.example.ui.auth
import android.net.Uri
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.runtime.DisposableEffect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.border
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.core.model.User
import com.example.core.security.RolePermissions
import com.example.core.security.UserRole
import com.example.ui.components.RoleBadge
import com.example.ui.theme.GamiAdminViolet
import com.example.ui.theme.GamiBackgroundDark
import com.example.ui.theme.GamiBorderDark
import com.example.ui.theme.GamiCyanAccent
import com.example.ui.theme.GamiError
import com.example.ui.theme.GamiIndigoLight
import com.example.ui.theme.GamiIndigoPrimary
import com.example.ui.theme.GamiOwnerGold
import com.example.ui.theme.GamiSuccess
import com.example.ui.theme.GamiSurfaceDark
import com.example.ui.theme.GamiSurfaceElevated
import com.example.ui.theme.GamiTextMuted
import com.example.ui.theme.GamiTextPrimary
import com.example.ui.theme.GamiTextSecondary

@Composable
fun AuthenticatedHomeScreen(
    user: User,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Party") }
    var createRoomRequest by remember { mutableStateOf(0) }
    var showCreateRoomSetup by remember { mutableStateOf(false) }
    var inRoom by remember { mutableStateOf(false) }
    var partyHasRooms by remember { mutableStateOf(false) }
    var liveRooms by remember { mutableStateOf<List<RoomCardInfo>>(emptyList()) }
    val firestore = remember { FirebaseFirestore.getInstance() }
    DisposableEffect(Unit) {
        val listener = firestore.collection("partyRooms").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                liveRooms = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("roomId") ?: doc.id
                    if (id.isBlank()) null else RoomCardInfo(
                        id = id,
                        hostUid = doc.getString("hostUid").orEmpty(),
                        host = doc.getString("hostName") ?: "Host",
                        hostPhotoUrl = doc.getString("hostPhotoUrl").orEmpty(),
                        posterUrl = doc.getString("posterUrl").orEmpty(),
                        type = doc.getString("roomType") ?: "Party",
                        members = doc.getLong("memberCount")?.toInt() ?: 0,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }
                partyHasRooms = liveRooms.isNotEmpty()
            }
        }
        onDispose { listener.remove() }
    }
    androidx.activity.compose.BackHandler(
        enabled = selectedTab != "Party" && !showCreateRoomSetup && !inRoom
    ) {
        selectedTab = "Party"
    }
    // Keep the setup choices outside the conditional screen so they survive
    // recomposition and can be written to the room document.
    var createFriendsOnly by remember { mutableStateOf(false) }
    var createRoomType by remember { mutableStateOf("Race") }
    var createRoomLayout by remember { mutableStateOf("8-seat") }
    var selectedLayoutPreview by remember { mutableIntStateOf(0) }
    var createPosterUrl by remember { mutableStateOf("") }
    var createError by remember { mutableStateOf("") }
    var creatingRoom by remember { mutableStateOf(false) }
    var posterUploading by remember { mutableStateOf(false) }
    var posterError by remember { mutableStateOf("") }
    var chatActionMessage by remember { mutableStateOf("") }
    var countryExpanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf("All Countries") }
    var sortPopular by remember { mutableStateOf(true) }
    var profilePhotoUrl by remember { mutableStateOf("") }
    LaunchedEffect(user.email) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).get().addOnSuccessListener { doc ->
                profilePhotoUrl = doc.getString("photoUrl").orEmpty()
            }
        }
    }

    val posterPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uri == null) return@rememberLauncherForActivityResult
        if (uid.isNullOrBlank()) {
            posterError = "Please sign in before selecting a poster."
        } else {
            posterUploading = true
            posterError = ""
            val posterRef = FirebaseStorage.getInstance().reference
                .child("roomPosters/$uid/${System.currentTimeMillis()}.jpg")
            posterRef.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw (task.exception ?: IllegalStateException("Poster upload failed"))
                    }
                    posterRef.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    createPosterUrl = downloadUri.toString()
                    posterUploading = false
                }
                .addOnFailureListener { error ->
                    posterUploading = false
                    posterError = error.localizedMessage ?: "Poster upload failed."
                }
        }
    }

    androidx.activity.compose.BackHandler(enabled = showCreateRoomSetup) {
        showCreateRoomSetup = false
        creatingRoom = false
    }

    if (showCreateRoomSetup) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFCAD4D0), Color(0xFFF1F5F1), Color(0xFFDBE5E0))))
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "←",
                    color = Color(0xFF173F39),
                    fontSize = 30.sp,
                    modifier = Modifier.clickable {
                        showCreateRoomSetup = false
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Create a Room",
                    color = Color(0xFF173F39),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Room Poster",
                color = Color(0xFF173F39),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.size(width = 130.dp, height = 145.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFDAEAE3)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().clickable(enabled = !posterUploading) {
                        posterPicker.launch("image/*")
                    },
                ) {
                    if (createPosterUrl.isNotBlank()) {
                        AsyncImage(
                            model = createPosterUrl,
                            contentDescription = "Room poster",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("R", modifier = Modifier.align(Alignment.Center), color = Color(0xFF169E82), fontSize = 54.sp)
                    }
                    Text("✎", modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        .background(Color(0xB0005549), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp),
                        color = Color.White, fontSize = 21.sp)
                    Text(
                        text = when {
                            posterUploading -> "Uploading…"
                            createPosterUrl.isBlank() -> "Choose poster"
                            else -> "Change poster"
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth().background(Color(0xB008806C)).padding(5.dp),
                        color = Color.White,
                        fontSize = 12.sp, textAlign = TextAlign.Center
                    )
                }
            }
            if (posterError.isNotBlank()) {
                Text(posterError, color = Color(0xFFFF8A80), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(100.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Room Type",
                    color = Color(0xFF173F39),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Friends only",
                    color = Color(0xFF173F39),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Switch(
                    checked = createFriendsOnly,
                    onCheckedChange = { createFriendsOnly = it }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color(0x80606F69)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf(
                        "Number" to "🔢",
                        "Race" to "🏁",
                        "Chatroom" to "💬",
                        "BlockMe PK" to "⚔️",
                        "Lucky Race" to "🎲",
                        "Teen Patti" to "🃏"
                    ).forEach { (name, icon) ->
                        val selected = createRoomType == name
                        Column(
                            modifier = Modifier.width(77.dp).clickable { createRoomType = name },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(58.dp)
                                    .background(if (selected) Color(0xFF087D69) else Color(0xFF496D67),
                                        RoundedCornerShape(10.dp))
                                    .then(if (selected) Modifier.border(1.5.dp, Color(0xFF35D0A8),
                                        RoundedCornerShape(10.dp)) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 34.sp)
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(name, color = Color.White, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Room Layout", color = Color(0xFF173F39), fontSize = 17.sp,
                    modifier = Modifier.weight(1f))
                CreateRoomLayoutSelector(selectedLayoutPreview) { preview ->
                    selectedLayoutPreview = preview
                    createRoomLayout = roomLayoutForPreview(preview)
                }
            }
            if (createError.isNotBlank()) {
                Text(createError, color = Color(0xFFFF8A80), modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                enabled = !creatingRoom && !posterUploading,
                onClick = {
                    if (FirebaseAuth.getInstance().currentUser?.uid.isNullOrBlank()) {
                        createError = "Please sign in before creating a room."
                    } else {
                        creatingRoom = true
                        createError = ""
                        selectedTab = "Party"
                        showCreateRoomSetup = false
                        createRoomRequest++
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .align(Alignment.CenterHorizontally)
                    .height(52.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B68F))
            ) {
                Text(
                    text = if (creatingRoom) "Creating…" else "Create",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }


    }

    // GAMI_CREATE_ROOM_SETUP_UI

    else {
    // GAMI_HOME_ONLY_WHEN_SETUP_CLOSED
    val tabs = listOf("Follow", "Party", "Chat", "Top")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                Color(0xFF00755B), Color(0xFF004838), Color(0xFF00694D), Color(0xFF00372F)
            )))
            .statusBarsPadding()
    ) {
        // Top app header removed to maximize room/content space

    if (!inRoom && selectedTab != "Profile") {
        // Banner
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF086556), Color(0xFF139F7E)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("RIMILIVE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Play Together • Meet New People",
                    color = Color(0xFFDAFFF3),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Follow / Party / Chat / Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEach { tab ->
                Column(
                    modifier = Modifier
                        .clickable { selectedTab = tab }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tab,
                        fontSize = 17.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == tab) Color.White else Color(0xFFD4EEE7)
                    )
                    if (selectedTab == tab) {
                        Box(
                            Modifier
                                .padding(top = 5.dp)
                                .width(32.dp)
                                .height(3.dp)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(18.dp)
                                )
                        )
                    }
                }
            }
        }

        // Party discovery filters
        if (selectedTab == "Party") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { countryExpanded = !countryExpanded },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFDCE7E4))
                ) {
                    Text(
                        text = if (selectedCountry.contains("All")) "🌐 Region ▾" else selectedCountry + " ▾",
                        modifier = Modifier.padding(13.dp),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF455A55)
                    )
                }

                if (countryExpanded) {
                    DropdownMenu(
                        expanded = countryExpanded,
                        onDismissRequest = { countryExpanded = false },
                        modifier = Modifier.width(330.dp)
                    ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFE8F8F1)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Region",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF168A68),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val countries = listOf(
                                "🌐 All",
                                "🇧🇩 Bangladesh",
                                "🇸🇦 Saudi Arabia",
                                "🇦🇪 United Arab Emirates",
                                "🇶🇦 Qatar",
                                "🇰🇼 Kuwait",
                                "🇮🇳 India",
                                "🇵🇰 Pakistan",
                                "🇳🇵 Nepal",
                                "🇵🇭 Philippines",
                                "🇮🇩 Indonesia",
                                "🇲🇾 Malaysia"
                            )

                            countries.chunked(3).forEach { rowCountries ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowCountries.forEach { country ->
                                        val isSelected = selectedCountry == country ||
                                            (country == "🌐 All" && selectedCountry.contains("All"))

                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 3.dp)
                                                .clickable {
                                                    selectedCountry = country
                                                    countryExpanded = false
                                                },
                                            shape = RoundedCornerShape(18.dp),
                                            color = if (isSelected)
                                                Color(0xFF20B486)
                                            else
                                                Color.White
                                        ) {
                                            Text(
                                                text = when (country) {
                                                    "🇦🇪 United Arab Emirates" -> "🇦🇪 UAE"
                                                    "🇸🇦 Saudi Arabia" -> "🇸🇦 Saudi"
                                                    "🇵🇭 Philippines" -> "🇵🇭 Philippines"
                                                    else -> country
                                                },
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 9.dp
                                                ),
                                                textAlign = TextAlign.Center,
                                                color = if (isSelected)
                                                    Color.White
                                                else
                                                    Color(0xFF455A55),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (rowCountries.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }

            Surface(
                modifier = Modifier.weight(1f).clickable { sortPopular = !sortPopular },
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF076E5C),
                border = BorderStroke(1.dp, Color(0xFFDCE7E4))
            ) {
                Text(
                    if (sortPopular) "Popular  ▾" else "Newest  ▾",
                    modifier = Modifier.padding(13.dp),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
        }

        // Tab content
                }

        when (selectedTab) {
            
            "Explore" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Explore",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Discover rooms, people and posts",
                        fontSize = 14.sp,
                        color = Color(0xFFE0FFF3)
                    )
                }
            }

            
            "Post" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Post",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Create and share photos or short videos",
                        fontSize = 14.sp,
                        color = Color(0xFFE0FFF3)
                    )
                }
            }

            
            "Message" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Message",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your conversations will appear here",
                        fontSize = 14.sp,
                        color = Color(0xFFE0FFF3)
                    )
                }
            }

            
            "Profile" -> {
                RimiProfileScreen(
                    user = user,
                    firestore = firestore,
                    onNavigate = { selectedTab = it },
                    onLogout = onLogoutClick,
                    modifier = Modifier.weight(1f)
                )
            }

            "Follow" -> {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "People You Follow",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF086C5B)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp)) {
                            Text("No followed rooms yet", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("Follow relationships need a backend connection. Browse live rooms in Party.",
                                color = Color(0xFFE0FFF3), fontSize = 13.sp)
                            TextButton(onClick = { selectedTab = "Party" }) { Text("Browse Party") }
                        }
                    }
                }
            }

            "Party" -> {
    var roomId by remember { mutableStateOf("123456") }
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val androidContext = LocalContext.current
    val roomRef = remember(roomId) {
        firestore.collection("partyRooms").document(roomId)
    }
    val membersRef = remember(roomId) {
        roomRef.collection("members")
    }

    val currentUid = firebaseUser?.uid.orEmpty()
    val backgroundPreferences = remember(androidContext) {
        RoomBackgroundPreferences(androidContext)
    }
    var selectedRoomBackgroundId by remember(currentUid) {
        mutableStateOf(backgroundPreferences.load(currentUid)?.id)
    }
    val selectedRoomBackground = roomBackgrounds.firstOrNull {
        it.id == selectedRoomBackgroundId
    }
    val currentName = user.fullName.ifBlank {
        user.username.ifBlank { "User" }
    }

    var currentPhotoUrl by remember { mutableStateOf("") }

    LaunchedEffect(currentUid) {
        if (currentUid.isNotBlank()) {
            firestore.collection("users")
                .document(currentUid)
                .get()
                .addOnSuccessListener { doc ->
                    currentPhotoUrl = doc.getString("photoUrl").orEmpty()
                }
        }
    }

    var selectedRoomSeat by remember { mutableStateOf(1) }
    var cameraEnabled by remember { mutableStateOf(false) }
    var showLeaveRoomDialog by remember { mutableStateOf(false) }


    // PK control
    var roomHostUid by remember { mutableStateOf("") }
    var roomHostName by remember { mutableStateOf("Host") }
    var roomHostPhoto by remember { mutableStateOf("") }
    // Older room documents did not carry layout metadata. Their established
    // room is the 15-seat layout; newly created rooms always write a layout.
    var roomLayout by remember { mutableStateOf("15-seat") }
    val layoutSpec = roomLayoutSpec(roomLayout)
    var roomType by remember { mutableStateOf("Race") }
    var roomFriendsOnly by remember { mutableStateOf(false) }
    val isHost = currentUid.isNotBlank() && currentUid == roomHostUid
    var showPkMenu by remember { mutableStateOf(false) }
    var pkRunning by remember { mutableStateOf(false) }
    var pkMinutes by remember { mutableStateOf(0) }
    var pkOpponentSeat by remember { mutableStateOf<Int?>(null) }
    var hostPkScore by remember { mutableStateOf(0) }
    var opponentPkScore by remember { mutableStateOf(0) }
    var pkSecondsRemaining by remember { mutableStateOf(0) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showRoomSettingsMenu by remember { mutableStateOf(false) }
    var showRoomBackgroundMenu by remember { mutableStateOf(false) }
    var roomFeatureNotice by remember { mutableStateOf<String?>(null) }
    var chatFocusRequest by remember { mutableIntStateOf(0) }
    var showJoinRoomDialog by remember { mutableStateOf(false) }
    var joinRoomInput by remember { mutableStateOf("") }
    var joinRoomError by remember { mutableStateOf("") }

    
    var showRoomBrowser by remember { mutableStateOf(false) }
    var showNoticeDialog by remember { mutableStateOf(false) }
    var noticeText by remember { mutableStateOf("Be kind, respect each other and enjoy the room!") }
    var noticeDraft by remember { mutableStateOf(noticeText) }

    var micEnabled by remember { mutableStateOf(true) }
    var speakerEnabled by remember { mutableStateOf(true) }
    var myMicEnabled by remember(roomId) { mutableStateOf(true) }

    // GAMI_CREATE_ROOM_REQUEST_HANDLER
    LaunchedEffect(createRoomRequest) {
        if (createRoomRequest > 0 && !inRoom && currentUid.isNotBlank()) {
            val newRoomId = (100000..999999).random().toString()

            val newRoomRef = firestore
                .collection("partyRooms")
                .document(newRoomId)

            val newRoom = hashMapOf<String, Any>(
                "roomId" to newRoomId,
                "hostUid" to currentUid,
                "hostName" to currentName,
                "hostPhotoUrl" to currentPhotoUrl,
                "notice" to "Welcome to the room",
                "pkRunning" to false,
                "pkMinutes" to 0,
                "pkOpponentSeat" to 0,
                "memberCount" to 1,
                "roomType" to createRoomType,
                "friendsOnly" to createFriendsOnly,
                "layout" to createRoomLayout,
                "posterUrl" to createPosterUrl,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            newRoomRef.set(newRoom)
                .addOnSuccessListener {
                    roomId = newRoomId
                    inRoom = true
                    showRoomBrowser = false
                    creatingRoom = false
                }
                .addOnFailureListener { error ->
                    creatingRoom = false
                    createError = error.localizedMessage ?: "Could not create room."
                    showCreateRoomSetup = true
                }
        }
    }


    // Local seat state
    var myJoinedSeat by remember { mutableStateOf<Int?>(null) }
    var liveMemberCount by remember { mutableStateOf(1) }
    var occupiedSeats by remember { mutableStateOf(setOf<Int>()) }

            // Live Firestore seat locks
            DisposableEffect(roomId, roomLayout) {
                val seatLocksListener = roomRef
                    .collection("seats")
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null) {
                            occupiedSeats = snapshot.documents.mapNotNull { doc ->
                                doc.getLong("seat")?.toInt()?.takeIf { it in layoutSpec.cameraSeats || it in layoutSpec.audioSeats }
                            }.toSet()

                            val mySeatFromServer = snapshot.documents.firstOrNull { doc ->
                                doc.getString("uid") == currentUid
                            }?.getLong("seat")?.toInt()

                            myJoinedSeat = mySeatFromServer
                        }
                    }

                onDispose {
                    seatLocksListener.remove()
                }
            }

    var seatMemberNames by remember { mutableStateOf(mapOf<Int, String>()) }
    var seatMemberPhotos by remember { mutableStateOf(mapOf<Int, String>()) }
    var seatMemberCameras by remember { mutableStateOf(mapOf<Int, Boolean>()) }
    var seatMemberMics by remember { mutableStateOf(mapOf<Int, Boolean>()) }

    var chatInput by remember { mutableStateOf("") }
    var showGameMenu by remember { mutableStateOf(false) }
    var showGiftMenu by remember { mutableStateOf(false) }
    var showCoinMenu by remember { mutableStateOf(false) }
    var giftReceiverSeat by remember { mutableStateOf(1) }

    var roomMessages by remember {
        mutableStateOf(
            listOf(
                "System: Welcome to the room",
                "System: Users joining the room will appear here"
            )
        )
    }
    var latestRoomActivity by remember(roomId) {
        mutableStateOf("$currentName joined the room")
    }
    var observedMemberIds by remember(roomId) { mutableStateOf<Set<String>?>(null) }
    var observedMessageCount by remember(roomId) { mutableIntStateOf(roomMessages.size) }
    LaunchedEffect(roomId, roomMessages.size) {
        if (roomMessages.size > observedMessageCount) {
            latestRoomActivity = roomMessages.last()
        }
        observedMessageCount = roomMessages.size
    }

    // Remove user from old room when room changes or screen closes
    

    // GAMI_HOST_HEARTBEAT
    LaunchedEffect(roomId, currentUid, inRoom) {
        while (inRoom && roomId.isNotBlank() && currentUid.isNotBlank()) {

            membersRef.document(currentUid).set(
                mapOf(
                    "lastSeen" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )

            if (isHost) {
                roomRef.set(
                    mapOf(
                        "hostLastSeen" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            }

            // GAMI_HOST_ROOM_HEARTBEAT
            kotlinx.coroutines.delay(20000L)
        }
    }


    fun handoffOrCloseRoom(
        afterDone: (() -> Unit)? = null
    ) {
        if (currentUid.isBlank() || roomId.isBlank()) {
            afterDone?.invoke()
            return
        }

        membersRef.get()
            .addOnSuccessListener { snapshot ->

                val otherMembers = snapshot.documents
                    .filter { doc ->
                        doc.id != currentUid &&
                            System.currentTimeMillis() - (doc.getLong("lastSeen") ?: 0L) <= 90000L
                    }
                    .sortedWith(
                        compareBy(
                            { it.getLong("joinedAt") ?: Long.MAX_VALUE },
                            { it.id }
                        )
                    )

                val mySeatRef = myJoinedSeat?.let { seatNo ->
                    roomRef
                        .collection("seats")
                        .document(seatNo.toString())
                }

                fun cleanupSelfThenFinish() {

                    mySeatRef?.delete()

                    membersRef
                        .document(currentUid)
                        .delete()
                        .addOnCompleteListener {
                            afterDone?.invoke()
                        }
                }

                if (isHost) {

                    val nextHost = otherMembers.firstOrNull()

                    if (nextHost != null) {

                        val nextHostUid = nextHost.id

                        roomRef.update(
                            mapOf(
                                "hostUid" to nextHostUid,
                                "hostName" to nextHost.getString("name").orEmpty().ifBlank { "Host" },
                                "hostPhotoUrl" to nextHost.getString("photoUrl").orEmpty(),
                                "updatedAt" to System.currentTimeMillis()
                            )
                        ).addOnSuccessListener {
                            cleanupSelfThenFinish()
                        }.addOnFailureListener {
                            roomMessages = roomMessages + "System: Unable to hand off the host. Please try again."
                        }

                    } else {

                        roomRef.delete()
                            .addOnSuccessListener {
                                cleanupSelfThenFinish()
                            }.addOnFailureListener {
                                roomMessages = roomMessages + "System: Unable to close the room. Please try again."
                            }
                    }

                } else {
                    cleanupSelfThenFinish()
                }
            }
            .addOnFailureListener {
                afterDone?.invoke()
            }
    }

    DisposableEffect(roomId, currentUid) {
        onDispose {
            if (currentUid.isNotBlank()) {
                handoffOrCloseRoom()
            }
        }
    }

    // Keep current user in room members
    LaunchedEffect(
        roomId,
        currentUid,
        currentPhotoUrl,
        myJoinedSeat,
        cameraEnabled,
        myMicEnabled
    ) {
        if (currentUid.isNotBlank() && inRoom) {
            val memberData = hashMapOf<String, Any>(
                "uid" to currentUid,
                "name" to currentName,
                "photoUrl" to currentPhotoUrl,
                "seat" to (myJoinedSeat ?: 0),
                "cameraEnabled" to cameraEnabled,
                "micEnabled" to myMicEnabled,
                "isHost" to isHost,
                "updatedAt" to System.currentTimeMillis()
            )

            membersRef.document(currentUid)
                .set(memberData, SetOptions.merge())
        }
    }

    // Create room document only if it does not exist yet
    LaunchedEffect(roomId, currentUid, inRoom) {
        if (currentUid.isNotBlank() && inRoom) {
            roomRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    val initialRoom = hashMapOf<String, Any>(
                        "roomId" to roomId,
                        "hostUid" to currentUid,
                        "hostName" to currentName,
                        "hostPhotoUrl" to currentPhotoUrl,
                        "notice" to noticeText,
                        "pkRunning" to false,
                        "pkMinutes" to 0,
                        "pkOpponentSeat" to 0,
                        "memberCount" to 1,
                        "roomType" to roomType,
                        "friendsOnly" to roomFriendsOnly,
                        "layout" to roomLayout,
                        "updatedAt" to System.currentTimeMillis()
                    )

                    roomRef.set(initialRoom, SetOptions.merge())
                }
            }
        }
    }

    // Listen to live room members
    DisposableEffect(roomId, roomLayout) {
        val memberListener = membersRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                liveMemberCount = snapshot.size()
                val currentMemberIds = snapshot.documents.map { it.id }.toSet()
                val joinedUid = observedMemberIds?.let { previous ->
                    (currentMemberIds - previous).firstOrNull { it != currentUid }
                }
                if (inRoom && joinedUid != null) {
                    val joinedName = snapshot.documents.firstOrNull { it.id == joinedUid }
                        ?.getString("name").orEmpty().ifBlank { "Someone" }
                    latestRoomActivity = "$joinedName joined the room"
                }
                observedMemberIds = currentMemberIds

                

                    // Real Firestore room member count sync
                    if (isHost) {
                        roomRef.set(
                            mapOf(
                                "memberCount" to snapshot.size(),
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )
                    }
                seatMemberNames = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("uid")
                    val seat = doc.getLong("seat")?.toInt() ?: 0
                    val name = doc.getString("name").orEmpty()

                    if (uid != currentUid && seat in 1..layoutSpec.totalSeats) {
                        seat to name.ifBlank { "User" }
                    } else {
                        null
                    }
                }.toMap()

                seatMemberPhotos = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("uid")
                    val seat = doc.getLong("seat")?.toInt() ?: 0
                    val photoUrl = doc.getString("photoUrl").orEmpty()

                    if (
                        uid != currentUid &&
                        seat in 1..layoutSpec.totalSeats &&
                        photoUrl.isNotBlank()
                    ) {
                        seat to photoUrl
                    } else {
                        null
                    }
                }.toMap()
                seatMemberCameras = snapshot.documents.mapNotNull { doc ->
                    val seat = doc.getLong("seat")?.toInt() ?: 0
                    if (seat in layoutSpec.cameraSeats) seat to (doc.getBoolean("cameraEnabled") == true)
                    else null
                }.toMap()
                seatMemberMics = snapshot.documents.mapNotNull { doc ->
                    val seat = doc.getLong("seat")?.toInt() ?: 0
                    if (seat in 1..layoutSpec.totalSeats) seat to (doc.getBoolean("micEnabled") != false)
                    else null
                }.toMap()
            }
        }

        onDispose {
            memberListener.remove()
        }
    }

    // Listen to room changes
    DisposableEffect(roomId) {
        val listener = roomRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                snapshot.getString("hostUid")?.let {
                    roomHostUid = it
                    if (roomHostPhoto.isBlank()) {
                        firestore.collection("users").document(it).get()
                            .addOnSuccessListener { hostDoc ->
                                roomHostPhoto = hostDoc.getString("photoUrl").orEmpty()
                            }
                    }
                }
                snapshot.getString("hostName")?.let { roomHostName = it }
                snapshot.getString("hostPhotoUrl")?.let { roomHostPhoto = it }

                snapshot.getString("notice")?.let {
                    if (it != noticeText) {
                        noticeText = it
                    }
                }
                roomLayout = roomLayoutSpec(snapshot.getString("layout") ?: "15-seat").key
                snapshot.getString("roomType")?.let { roomType = it }
                snapshot.getBoolean("friendsOnly")?.let { roomFriendsOnly = it }

                snapshot.getBoolean("pkRunning")?.let {
                    pkRunning = it
                }

                snapshot.getLong("pkMinutes")?.let {
                    pkMinutes = it.toInt()
                }

                snapshot.getLong("pkOpponentSeat")?.let {
                    pkOpponentSeat =
                        if (it.toInt() == 0) null else it.toInt()
                }

                    snapshot.getLong("hostPkScore")?.let {
                        hostPkScore = it.toInt()
                    }

                    snapshot.getLong("opponentPkScore")?.let {
                        opponentPkScore = it.toInt()
                    }

                    snapshot.getBoolean("micEnabled")?.let {
                        micEnabled = it
                    }

                    snapshot.getBoolean("speakerEnabled")?.let {
                        speakerEnabled = it
                    }
            }
        }

        onDispose {
            listener.remove()
        }
    }

    

    


    // GAMI_STALE_HOST_WATCHDOG
    LaunchedEffect(roomId, currentUid, roomHostUid, inRoom) {

        while (
            inRoom &&
            roomId.isNotBlank() &&
            currentUid.isNotBlank()
        ) {
            kotlinx.coroutines.delay(10000L)

            if (
                roomHostUid.isNotBlank() &&
                roomHostUid != currentUid
            ) {

                roomRef.get()
                    .addOnSuccessListener { roomSnapshot ->

                        val currentHostUid =
                            roomSnapshot.getString("hostUid").orEmpty()

                        val hostLastSeen =
                            roomSnapshot.getTimestamp("hostLastSeen")

                        val staleForMs =
                            if (hostLastSeen != null) {
                                System.currentTimeMillis() -
                                    hostLastSeen.toDate().time
                            } else {
                                0L
                            }

                        // Host gets 90 seconds to reconnect
                        if (
                            currentHostUid.isNotBlank() &&
                            currentHostUid != currentUid &&
                            staleForMs >= 90000L
                        ) {

                            membersRef.get()
                                .addOnSuccessListener { membersSnapshot ->

                                    val now =
                                        System.currentTimeMillis()

                                    val activeMembers =
                                        membersSnapshot.documents
                                            .filter { doc ->
                                                doc.id != currentHostUid &&
                                                (
                                                    now -
                                                    (
                                                        doc.getLong("lastSeen")
                                                            ?: 0L
                                                    )
                                                ) <= 90000L
                                            }
                                            .sortedWith(
                                                compareBy(
                                                    {
                                                        it.getLong("joinedAt")
                                                            ?: Long.MAX_VALUE
                                                    },
                                                    { it.id }
                                                )
                                            )

                                    val nextHost =
                                        activeMembers.firstOrNull()

                                    // Only the elected next member attempts takeover
                                    if (
                                        nextHost != null &&
                                        nextHost.id == currentUid
                                    ) {

                                        firestore.runTransaction { transaction ->

                                            val latestRoom =
                                                transaction.get(roomRef)

                                            val latestHostUid =
                                                latestRoom
                                                    .getString("hostUid")
                                                    .orEmpty()

                                            val latestLastSeen =
                                                latestRoom
                                                    .getTimestamp("hostLastSeen")

                                            val stillStale =
                                                latestLastSeen != null &&
                                                (
                                                    System.currentTimeMillis() -
                                                    latestLastSeen.toDate().time
                                                ) >= 90000L

                                            if (
                                                latestHostUid ==
                                                    currentHostUid &&
                                                stillStale
                                            ) {

                                                transaction.update(
                                                    roomRef,
                                                    mapOf(
                                                        "hostUid" to currentUid,
                                                         "hostName" to currentName,
                                                         "hostPhotoUrl" to currentPhotoUrl,
                                                        "hostLastSeen" to
                                                            com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                                        "updatedAt" to
                                                            System.currentTimeMillis()
                                                    )
                                                )
                                            }

                                            true
                                        }
                                    }
                                }
                        }
                    }
            }
        }
    }

androidx.activity.compose.BackHandler(enabled = inRoom || showRoomBrowser || showJoinRoomDialog) {
        when {
            showJoinRoomDialog -> showJoinRoomDialog = false
            showRoomBrowser -> showRoomBrowser = false
            inRoom -> showLeaveRoomDialog = true
        }
    }

    LaunchedEffect(pkRunning, pkMinutes) {
        if (pkRunning && pkMinutes > 0) {
            var seconds = pkMinutes * 60
            pkSecondsRemaining = seconds

            while (pkRunning && seconds > 0) {
                kotlinx.coroutines.delay(1000)
                seconds -= 1
                pkSecondsRemaining = seconds
            }

            if (pkRunning && seconds <= 0) {
                val finalMessage =
                    if (hostPkScore > opponentPkScore)
                        "System: PK ended - Host won"
                    else if (opponentPkScore > hostPkScore)
                        "System: PK ended - Seat $pkOpponentSeat won"
                    else
                        "System: PK ended - Draw"

                roomMessages = roomMessages + finalMessage
                pkRunning = false
                pkMinutes = 0
                pkSecondsRemaining = 0
                pkOpponentSeat = null
            }
        }
    }

    if (showNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showNoticeDialog = false },
            title = { Text("Notice Board") },
            text = {
                OutlinedTextField(
                    value = noticeDraft,
                    onValueChange = {
                        if (it.length <= 120) {
                            noticeDraft = it
                        }
                    },
                    label = { Text("Room announcement") },
                    supportingText = {
                        Text("${noticeDraft.length}/120")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        noticeText = noticeDraft.trim()

                        roomRef.set(
                            mapOf(
                                "notice" to noticeText,
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )

                        roomMessages = roomMessages +
                            "System: Notice updated"
                        showNoticeDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        noticeDraft = noticeText
                        showNoticeDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGameMenu) {
        AlertDialog(
            onDismissRequest = { showGameMenu = false },
            title = { Text("Games") },
            text = {
                Column {
                    TextButton(onClick = {
                        showGameMenu = false
                        if (isHost) showPkMenu = true
                        else roomMessages = roomMessages + "System: Only the host can start PK"
                    }) {
                        Text("PK Game")
                    }
                    TextButton(onClick = {
                        showGameMenu = false
                        roomFeatureNotice = "Ludo is not available yet."
                    }) {
                        Text("Ludo")
                    }
                    TextButton(onClick = {
                        showGameMenu = false
                        roomFeatureNotice = "GAMI Race is not available yet."
                    }) {
                        Text("GAMI Race")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGameMenu = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showGiftMenu) {
        var giftCoinBalance by remember(currentUid) { mutableStateOf<Long?>(null) }
        DisposableEffect(currentUid) {
            val listener = if (currentUid.isNotBlank()) {
                firestore.collection("users").document(currentUid)
                    .addSnapshotListener { doc, error ->
                        giftCoinBalance = if (error == null) doc?.getLong("coins") else null
                    }
            } else null
            onDispose { listener?.remove() }
        }
        RoomGiftSheet(
            receiverSeat = giftReceiverSeat,
            coinBalance = giftCoinBalance,
            onDismiss = { showGiftMenu = false }
        )
    }

    if (showCoinMenu) {
        AlertDialog(
            onDismissRequest = { showCoinMenu = false },
            title = { Text("Coins") },
            text = {
                Text("Coin balance and recharge will be connected later.")
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCoinMenu = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showRoomBrowser) {
        AlertDialog(
            onDismissRequest = { showRoomBrowser = false },
            title = {
                Text("Party Rooms")
            },
            text = {
                Column {
                    if (liveRooms.isEmpty()) {
                        Text("No active rooms")
                    } else {
                        liveRooms.take(15).forEach { room ->
                            val targetRoomId = room.id
                            val hostName = room.host

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        if (targetRoomId != roomId) {
                                            myJoinedSeat = null
                                            cameraEnabled = false
                                            selectedRoomSeat = 1
                                            pkRunning = false
                                            roomId = targetRoomId
                                        }

                                        inRoom = true

                                        roomMessages = roomMessages +
                                            "System: Joined room $targetRoomId"

                                        showRoomBrowser = false
                                    },
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFE7F5F2)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text(
                                        text = hostName,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF174D52)
                                    )

                                    Text(
                                        text = "Room ID: $targetRoomId",
                                        fontSize = 11.sp,
                                        color = Color(0xFF54787A)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                if (inRoom) {
                    TextButton(
                        onClick = {
                            showRoomBrowser = false
                        }
                    ) {
                        Text("Close")
                    }
                }
            }
        )
    }

    if (showJoinRoomDialog) {
        AlertDialog(
            onDismissRequest = {
                showJoinRoomDialog = false
                joinRoomError = ""
            },
            title = { Text("Join Room") },
            text = {
                Column {
                    OutlinedTextField(
                        value = joinRoomInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                joinRoomInput = it
                                joinRoomError = ""
                            }
                        },
                        label = { Text("6-digit Room ID") },
                        singleLine = true
                    )

                    if (joinRoomError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = joinRoomError,
                            color = Color.Red,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = joinRoomInput.length == 6,
                    onClick = {
                        val targetId = joinRoomInput

                        firestore.collection("partyRooms")
                            .document(targetId)
                            .get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    roomId = targetId
                                    myJoinedSeat = null
                                    selectedRoomSeat = 1
                                    cameraEnabled = false
                                    pkRunning = false
                                    inRoom = true
                                    showJoinRoomDialog = false
                                    joinRoomInput = ""
                                    joinRoomError = ""

                                    roomMessages = roomMessages +
                                        "System: Joined room $targetId"
                                } else {
                                    joinRoomError = "Room not found"
                                }
                            }
                            .addOnFailureListener {
                                joinRoomError = "Could not join room"
                            }
                    }
                ) {
                    Text("Join")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showJoinRoomDialog = false
                        joinRoomError = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMoreMenu) {
        RoomMoreSheet(
            onDismiss = { showMoreMenu = false },
            onSettings = { showMoreMenu = false; showRoomSettingsMenu = true },
            onLudo = { showMoreMenu = false; roomFeatureNotice = "Ludo is not available yet." },
            onGamiRace = {
                showMoreMenu = false
                roomFeatureNotice = "GAMI Race is not available yet."
            },
            onMusic = {
                showMoreMenu = false
                roomFeatureNotice = "Music is not available yet."
            },
            onTopUp = { showMoreMenu = false; showCoinMenu = true },
            onMessages = {
                showMoreMenu = false
                chatFocusRequest++
            },
            onBrowseRooms = { showMoreMenu = false; showRoomBrowser = true },
            onJoinRoom = { showMoreMenu = false; showJoinRoomDialog = true },
            onEditNotice = {
                showMoreMenu = false
                if (isHost) {
                    noticeDraft = noticeText
                    showNoticeDialog = true
                } else {
                    roomFeatureNotice = "Only the host can edit the room notice."
                }
            },
            onShareRoom = {
                showMoreMenu = false
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Join my RIMILIVE room: $roomId")
                }
                androidContext.startActivity(Intent.createChooser(shareIntent, "Share room"))
            }
        )
    }
    if (showRoomSettingsMenu) {
        RoomSettingsSheet(
            micOn = myMicEnabled,
            cameraOn = cameraEnabled,
            cameraAvailable = myJoinedSeat in layoutSpec.cameraSeats,
            onDismiss = { showRoomSettingsMenu = false },
            onMicrophone = { myMicEnabled = !myMicEnabled },
            onCamera = {
                if (myJoinedSeat in layoutSpec.cameraSeats) cameraEnabled = !cameraEnabled
                else {
                    showRoomSettingsMenu = false
                    roomFeatureNotice = "Join a camera seat to use the camera."
                }
            },
            onBackground = {
                showRoomSettingsMenu = false
                showRoomBackgroundMenu = true
            },
            onUnavailable = { feature ->
                showRoomSettingsMenu = false
                roomFeatureNotice = "$feature is not available yet."
            }
        )
    }
    if (inRoom && showRoomBackgroundMenu) {
        RoomBackgroundSheet(
            selectedId = selectedRoomBackgroundId,
            onSelect = { id ->
                selectedRoomBackgroundId = id
                showRoomBackgroundMenu = false
                if (!backgroundPreferences.save(currentUid, id)) {
                    roomFeatureNotice = "Background could not be saved on this device."
                }
            },
            onDismiss = { showRoomBackgroundMenu = false }
        )
    }
    roomFeatureNotice?.let { message ->
        AlertDialog(
            onDismissRequest = { roomFeatureNotice = null },
            title = { Text("Room feature") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { roomFeatureNotice = null }) { Text("OK") }
            }
        )
    }

    if (showPkMenu) {
        AlertDialog(
            onDismissRequest = { showPkMenu = false },
            title = { Text("Start PK") },
            text = {
                Column {
                    Text("Choose PK duration")
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Choose opponent")
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (2..5).forEach { seat ->
                            TextButton(
                                onClick = {
                                    pkOpponentSeat = seat
                                }
                            ) {
                                Text(
                                    if (pkOpponentSeat == seat)
                                        "Seat $seat ✓"
                                    else
                                        "Seat $seat"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Choose PK duration")

                    listOf(5, 10, 15).forEach { minutes ->
                        TextButton(
                            enabled = pkOpponentSeat != null,
                            onClick = {
                                pkMinutes = minutes
                                pkSecondsRemaining = minutes * 60
                                pkRunning = true
                                hostPkScore = 0
                                opponentPkScore = 0

                                roomRef.set(
                                    mapOf(
                                        "pkRunning" to true,
                                        "pkMinutes" to minutes,
                                        "pkOpponentSeat" to (pkOpponentSeat ?: 0),
                                        "updatedAt" to System.currentTimeMillis()
                                    ),
                                    SetOptions.merge()
                                )
                                roomMessages = roomMessages +
                                    "System: PK started with Seat $pkOpponentSeat for $minutes minutes"
                                showPkMenu = false
                            }
                        ) {
                            Text("$minutes minutes")
                        }
                    }

                    if (pkRunning) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = {
                                pkRunning = false
                                pkMinutes = 0
                                pkSecondsRemaining = 0
                                pkOpponentSeat = null

                                roomRef.set(
                                    mapOf(
                                        "pkRunning" to false,
                                        "pkMinutes" to 0,
                                        "pkOpponentSeat" to 0,
                                        "updatedAt" to System.currentTimeMillis()
                                    ),
                                    SetOptions.merge()
                                )
                                hostPkScore = 0
                                opponentPkScore = 0
                                roomMessages = roomMessages +
                                    "System: PK stopped"
                                showPkMenu = false
                            }
                        ) {
                            Text("Stop PK")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showPkMenu = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLeaveRoomDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveRoomDialog = false },
            title = { Text("Leave this room?") },
            text = { Text("Do you want to leave the party room?") },
            confirmButton = {
                TextButton(
                    onClick = {
                            showLeaveRoomDialog = false

                            handoffOrCloseRoom {
                                myJoinedSeat = null
                                selectedRoomSeat = 1
                                giftReceiverSeat = 1
                                cameraEnabled = false

                                pkRunning = false
                                pkMinutes = 0
                                pkSecondsRemaining = 0
                                pkOpponentSeat = null
                                hostPkScore = 0
                                opponentPkScore = 0

                                inRoom = false
                                roomId = "123456"
                                roomHostUid = ""

                                showMoreMenu = false
                                showPkMenu = false
                                showGiftMenu = false
                                showGameMenu = false
                                showNoticeDialog = false

                                roomMessages = roomMessages +
                                    "System: You left the room"
                            }
                        }
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLeaveRoomDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // GAMI_ROOM_ONLY_WHEN_JOINED
    fun toggleCameraSeat(seatNo: Int) {
        if (currentUid.isBlank()) {
            roomMessages = roomMessages + "System: Please sign in first"
            return
        }
        if (seatNo == 1 && !isHost) {
            roomMessages = roomMessages + "System: Only the host can use seat 1"
            return
        }
        val targetSeatRef = roomRef.collection("seats").document(seatNo.toString())
        val oldSeatNo = myJoinedSeat
        val oldSeatRef = oldSeatNo?.takeIf { it != seatNo }?.let {
            roomRef.collection("seats").document(it.toString())
        }
        firestore.runTransaction { transaction ->
            val targetSnapshot = transaction.get(targetSeatRef)
            val oldSnapshot = oldSeatRef?.let { transaction.get(it) }
            val targetOwner = targetSnapshot.getString("uid")
            if (targetSnapshot.exists() && targetOwner != currentUid) {
                throw IllegalStateException("SEAT_OCCUPIED")
            }
            if (oldSeatRef != null && oldSnapshot?.getString("uid") == currentUid) {
                transaction.delete(oldSeatRef)
            }
            if (oldSeatNo == seatNo && targetOwner == currentUid) {
                transaction.delete(targetSeatRef)
                transaction.set(
                    membersRef.document(currentUid),
                    mapOf("seat" to 0, "updatedAt" to System.currentTimeMillis()),
                    SetOptions.merge()
                )
            } else {
                transaction.set(
                    targetSeatRef,
                    mapOf(
                        "uid" to currentUid,
                        "name" to currentName,
                        "photoUrl" to currentPhotoUrl,
                        "seat" to seatNo,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                transaction.set(
                    membersRef.document(currentUid),
                    mapOf(
                        "uid" to currentUid,
                        "name" to currentName,
                        "photoUrl" to currentPhotoUrl,
                        "seat" to seatNo,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            }
            true
        }.addOnSuccessListener {
            if (oldSeatNo == seatNo) {
                myJoinedSeat = null
                cameraEnabled = false
                roomMessages = roomMessages + "System: You left camera seat $seatNo"
            } else {
                myJoinedSeat = seatNo
                cameraEnabled = false
                roomMessages = roomMessages + "System: You joined camera seat $seatNo"
            }
        }.addOnFailureListener { error ->
            val message = if (error.message?.contains("SEAT_OCCUPIED") == true) {
                "System: Seat $seatNo is already occupied"
            } else {
                "System: Could not update camera seat"
            }
            roomMessages = roomMessages + message
        }
    }

    // This is the 8-seat screen's audio action. Keep the existing 15-seat
    // click handler below unchanged while sharing the same Firestore seat locks.
    fun toggleEightSeatAudio(seatNo: Int) {
        if (seatNo !in 4..8 || currentUid.isBlank()) {
            roomMessages = roomMessages + "System: Please sign in first"
            return
        }
        selectedRoomSeat = seatNo
        giftReceiverSeat = seatNo
        val oldSeatNo = myJoinedSeat
        val target = roomRef.collection("seats").document(seatNo.toString())
        val old = oldSeatNo?.takeIf { it != seatNo }?.let {
            roomRef.collection("seats").document(it.toString())
        }
        firestore.runTransaction { transaction ->
            val targetSnapshot = transaction.get(target)
            val oldSnapshot = old?.let { transaction.get(it) }
            val owner = targetSnapshot.getString("uid")
            if (targetSnapshot.exists() && owner != currentUid) {
                throw IllegalStateException("SEAT_OCCUPIED")
            }
            if (old != null && oldSnapshot?.getString("uid") == currentUid) {
                transaction.delete(old)
            }
            if (oldSeatNo == seatNo && owner == currentUid) {
                transaction.delete(target)
                transaction.set(
                    membersRef.document(currentUid),
                    mapOf("seat" to 0, "updatedAt" to System.currentTimeMillis()),
                    SetOptions.merge()
                )
            } else {
                transaction.set(target, mapOf(
                    "uid" to currentUid, "name" to currentName, "photoUrl" to currentPhotoUrl,
                    "seat" to seatNo, "updatedAt" to System.currentTimeMillis()
                ))
                transaction.set(membersRef.document(currentUid), mapOf(
                    "uid" to currentUid, "name" to currentName, "photoUrl" to currentPhotoUrl,
                    "seat" to seatNo, "updatedAt" to System.currentTimeMillis()
                ), SetOptions.merge())
            }
            true
        }.addOnSuccessListener {
            myJoinedSeat = if (oldSeatNo == seatNo) null else seatNo
            cameraEnabled = false
            roomMessages = roomMessages + if (oldSeatNo == seatNo) {
                "System: You left seat $seatNo"
            } else {
                "System: You joined audio seat $seatNo"
            }
        }.addOnFailureListener { error ->
            roomMessages = roomMessages + if (error.message?.contains("SEAT_OCCUPIED") == true) {
                "System: Seat $seatNo is already occupied"
            } else {
                "System: Could not update audio seat"
            }
        }
    }

    fun toggleFifteenSeatAudio(seatNo: Int) {
        if (seatNo !in 6..15 || currentUid.isBlank()) {
            roomMessages = roomMessages + "System: Please sign in first"
            return
        }
        selectedRoomSeat = seatNo
        giftReceiverSeat = seatNo
        val oldSeatNo = myJoinedSeat
        val target = roomRef.collection("seats").document(seatNo.toString())
        val old = oldSeatNo?.takeIf { it != seatNo }?.let {
            roomRef.collection("seats").document(it.toString())
        }
        firestore.runTransaction { transaction ->
            val targetSnapshot = transaction.get(target)
            val oldSnapshot = old?.let { transaction.get(it) }
            val owner = targetSnapshot.getString("uid")
            if (targetSnapshot.exists() && owner != null && owner != currentUid) {
                throw IllegalStateException("SEAT_OCCUPIED")
            }
            if (old != null && oldSnapshot?.getString("uid") == currentUid) {
                transaction.delete(old)
            }
            if (oldSeatNo == seatNo) {
                if (owner == currentUid) transaction.delete(target)
                transaction.set(membersRef.document(currentUid), mapOf(
                    "seat" to 0, "updatedAt" to System.currentTimeMillis()
                ), SetOptions.merge())
            } else {
                transaction.set(target, mapOf(
                    "uid" to currentUid, "name" to currentName, "photoUrl" to currentPhotoUrl,
                    "seat" to seatNo, "updatedAt" to System.currentTimeMillis()
                ))
                transaction.set(membersRef.document(currentUid), mapOf(
                    "uid" to currentUid, "name" to currentName, "photoUrl" to currentPhotoUrl,
                    "seat" to seatNo, "updatedAt" to System.currentTimeMillis()
                ), SetOptions.merge())
            }
            true
        }.addOnSuccessListener {
            myJoinedSeat = if (oldSeatNo == seatNo) null else seatNo
            cameraEnabled = false
            roomMessages = roomMessages + if (oldSeatNo == seatNo) {
                "System: You left seat $seatNo"
            } else {
                "System: You joined seat $seatNo"
            }
        }.addOnFailureListener { error ->
            roomMessages = roomMessages + if (error.message?.contains("SEAT_OCCUPIED") == true) {
                "System: Seat $seatNo is already occupied"
            } else {
                "System: Could not update seat"
            }
        }
    }

        if (inRoom && layoutSpec.key == "8-seat") {
            EightSeatRoomScreen(
                background = selectedRoomBackground,
                state = EightSeatRoomState(
                    hostName = roomHostName,
                    hostPhoto = roomHostPhoto,
                    roomType = roomType,
                    memberCount = liveMemberCount,
                    notice = noticeText,
                    isHost = isHost,
                    mySeat = myJoinedSeat,
                    myPhoto = currentPhotoUrl,
                    occupied = occupiedSeats,
                    names = seatMemberNames,
                    photos = seatMemberPhotos,
                    memberCameras = seatMemberCameras,
                    memberMics = seatMemberMics,
                    cameraOn = cameraEnabled,
                    micOn = myMicEnabled,
                    pkRunning = pkRunning,
                    pkSeconds = pkSecondsRemaining,
                    lastMessage = latestRoomActivity,
                    chatInput = chatInput,
                    chatFocusRequest = chatFocusRequest
                ),
                actions = EightSeatRoomActions(
                    onNotice = { noticeDraft = noticeText; showNoticeDialog = true },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Join my RIMILIVE room: $roomId")
                        }
                        androidContext.startActivity(Intent.createChooser(shareIntent, "Share room"))
                    },
                    onLeave = { showLeaveRoomDialog = true },
                    onPk = { if (isHost) showPkMenu = true },
                    onCameraSeat = { seat ->
                        selectedRoomSeat = seat
                        giftReceiverSeat = seat
                        toggleCameraSeat(seat)
                    },
                    onAudioSeat = ::toggleEightSeatAudio,
                    onCameraToggle = { cameraEnabled = !cameraEnabled },
                    onMicToggle = { myMicEnabled = !myMicEnabled },
                    onChatChange = { chatInput = it },
                    onSend = {
                        val message = chatInput.trim()
                        if (message.isNotEmpty()) {
                            roomMessages = roomMessages + "You: $message"
                            chatInput = ""
                        }
                    },
                    onGame = { showGameMenu = true },
                    onGift = { showGiftMenu = true },
                    onCoin = { showCoinMenu = true },
                    onMore = { showMoreMenu = true }
                )
            )
        } else if (inRoom && layoutSpec.key == "15-seat") {
            FifteenSeatRoomScreen(
                background = selectedRoomBackground,
                state = EightSeatRoomState(
                    hostName = roomHostName,
                    hostPhoto = roomHostPhoto,
                    roomType = roomType,
                    memberCount = liveMemberCount,
                    notice = noticeText,
                    isHost = isHost,
                    mySeat = myJoinedSeat,
                    myPhoto = currentPhotoUrl,
                    occupied = occupiedSeats,
                    names = seatMemberNames,
                    photos = seatMemberPhotos,
                    memberCameras = seatMemberCameras,
                    memberMics = seatMemberMics,
                    cameraOn = cameraEnabled,
                    micOn = myMicEnabled,
                    pkRunning = pkRunning,
                    pkSeconds = pkSecondsRemaining,
                    lastMessage = latestRoomActivity,
                    chatInput = chatInput,
                    chatFocusRequest = chatFocusRequest
                ),
                actions = EightSeatRoomActions(
                    onNotice = { noticeDraft = noticeText; showNoticeDialog = true },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Join my RIMILIVE room: $roomId")
                        }
                        androidContext.startActivity(Intent.createChooser(shareIntent, "Share room"))
                    },
                    onLeave = { showLeaveRoomDialog = true },
                    onPk = { if (isHost) showPkMenu = true },
                    onCameraSeat = { seat ->
                        selectedRoomSeat = seat
                        giftReceiverSeat = seat
                        toggleCameraSeat(seat)
                    },
                    onAudioSeat = ::toggleFifteenSeatAudio,
                    onCameraToggle = { cameraEnabled = !cameraEnabled },
                    onMicToggle = { myMicEnabled = !myMicEnabled },
                    onChatChange = { chatInput = it },
                    onSend = {
                        val message = chatInput.trim()
                        if (message.isNotEmpty()) {
                            roomMessages = roomMessages + "You: $message"
                            chatInput = ""
                        }
                    },
                    onGame = { showGameMenu = true },
                    onGift = { showGiftMenu = true },
                    onCoin = { showCoinMenu = true },
                    onMore = { showMoreMenu = true }
                ),
                pkScoreLine = if (pkRunning && pkOpponentSeat != null) {
                    "Host: $hostPkScore    Seat $pkOpponentSeat: $opponentPkScore"
                } else null
            )
        } else if (inRoom) {
Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFF8A24))
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {

        // Room top info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF6A3217),
            shadowElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(58.dp),
                            shape = CircleShape,
                            color = Color(0xFF7A3A18),
                            border = BorderStroke(2.dp, Color(0xFFFFC24B))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "👤",
                                    fontSize = 30.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                                Text(
                                    text = "RIMILIVE • $roomType",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Party Room",
                                color = Color(0xFFFFD88A),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))


                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF7A3A18)
                    ) {
                        Text(
                            text = "👥 $liveMemberCount",
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 7.dp
                            ),
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        modifier = Modifier.clickable {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Join my RIMILIVE room: $roomId")
                            }
                            androidContext.startActivity(Intent.createChooser(shareIntent, "Share room"))
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF7A3A18)
                    ) {
                        Text(
                            text = "↗ Share",
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 7.dp
                            ),
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        modifier = Modifier.clickable {
                            showLeaveRoomDialog = true
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF7A3A18)
                    ) {
                        Text(
                            text = "✕",
                            modifier = Modifier.padding(
                                horizontal = 11.dp,
                                vertical = 7.dp
                            ),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Notice + PK
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        noticeDraft = noticeText
                        showNoticeDialog = true
                    },
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF6A3217)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = "🔊 Notice Board",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = noticeText,
                        color = Color(0xFF6A3217),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = isHost) {
                        showPkMenu = true
                    },
                shape = RoundedCornerShape(18.dp),
                color = if (pkRunning) Color(0xFF6A3217) else Color(0xFF6A3217)
            ) {
                Text(
                    text = if (pkRunning)
                        "⚔ PK LIVE • Seat $pkOpponentSeat • ${pkMinutes}m"
                    else
                        "⚔ PK • Host Only",
                    modifier = Modifier.padding(10.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(54.dp))

        if (pkRunning && pkOpponentSeat != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF6A3217),
                border = BorderStroke(1.dp, Color(0xFFFFC24B))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    val pkMinLeft = pkSecondsRemaining / 60
                    val pkSecLeft = pkSecondsRemaining % 60

                    Text(
                        text = "PK LIVE  •  %02d:%02d".format(
                            pkMinLeft,
                            pkSecLeft
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(54.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Host: $hostPkScore",
                            color = Color(0xFFFFD86B),
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Seat $pkOpponentSeat: $opponentPkScore",
                            color = Color(0xFF8DE6DE),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(54.dp))
        }

        // Gift counter above the seat board
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF7A3A18),
                border = BorderStroke(1.dp, Color(0xFFFFC24B))
            ) {
                Text(
                    text = "🎁  0   +",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$roomType • ${layoutSpec.totalSeats} seats / ${layoutSpec.cameraSeats.count()} cameras",
            color = Color(0xFF28565B),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val onCameraSeat = myJoinedSeat?.let { seat ->
                seat in layoutSpec.cameraSeats
            } == true
            FilterChip(
                selected = cameraEnabled,
                enabled = onCameraSeat,
                onClick = { cameraEnabled = !cameraEnabled },
                label = { Text(if (cameraEnabled) "Camera on" else "Camera off (UI)") }
            )
            FilterChip(
                selected = myMicEnabled,
                onClick = { myMicEnabled = !myMicEnabled },
                label = { Text(if (myMicEnabled) "Mic on (UI)" else "Mic muted") }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Existing room surface: host camera card with two (8-seat) or four
        // (15-seat) camera positions alongside it.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (layoutSpec.key == "8-seat") 220.dp else 192.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // HOST - biggest camera seat
            Surface(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight()
                    .clickable {
                        selectedRoomSeat = 1
                        giftReceiverSeat = 1
                         if (isHost) toggleCameraSeat(1)
                         else roomMessages = roomMessages + "System: Host seat is protected"
                    },
                shape = RoundedCornerShape(18.dp),
                color = Color(0x663B2418),
                border = BorderStroke(0.8.dp, Color(0x55FFFFFF))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (roomHostPhoto.isNotBlank()) {
                            AsyncImage(
                                model = roomHostPhoto,
                                contentDescription = "Host profile",
                                modifier = Modifier.size(58.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("👤", fontSize = 58.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = roomHostName.ifBlank { "Host" },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${if (seatMemberCameras[1] == true) "📷" else "📷 Off"}  ${if (seatMemberMics[1] == false) "🔇" else "🎙"}",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Camera seats 2-3 in the converted 8-seat room, 2-5 in 15-seat.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(2) { rowIndex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(if (layoutSpec.key == "15-seat") 2 else 1) { colIndex ->
                            val seatNo = 2 + rowIndex * (if (layoutSpec.key == "15-seat") 2 else 1) + colIndex

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        selectedRoomSeat = seatNo
                                        giftReceiverSeat = seatNo

                                        toggleCameraSeat(seatNo)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedRoomSeat == seatNo)
                                    Color(0x884F382B)
                                else
                                    Color(0x55442F25),
                                border = BorderStroke(1.dp, Color(0xFF7A3A18))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val seatPhoto =
                                            if (myJoinedSeat == seatNo)
                                                currentPhotoUrl
                                            else
                                                seatMemberPhotos[seatNo].orEmpty()

                                        if (seatPhoto.isNotBlank()) {
                                            AsyncImage(
                                                model = seatPhoto,
                                                contentDescription = "Profile",
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                if (
                                                    myJoinedSeat == seatNo ||
                                                    occupiedSeats.contains(seatNo)
                                                )
                                                    "👤"
                                                else
                                                    "+",
                                                fontSize = 24.sp,
                                                color = Color.White
                                            )
                                        }

                                        Text(
                                            when {
                                                myJoinedSeat == seatNo ->
                                                    "You • Seat $seatNo"

                                                occupiedSeats.contains(seatNo) ->
                                                    seatMemberNames[seatNo]
                                                        ?: "User"

                                                else ->
                                                    "Seat $seatNo"
                                            },
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )

                                        Text(
                                            when {
                                                myJoinedSeat == seatNo && cameraEnabled -> "📷 On"
                                                myJoinedSeat == seatNo -> "📷 Off"
                                                occupiedSeats.contains(seatNo) && seatMemberCameras[seatNo] == true -> "📷 On"
                                                occupiedSeats.contains(seatNo) -> "📷 Off"
                                                else -> "Camera"
                                            },
                                            fontSize = 11.sp,
                                            color = Color(0xFFFFD88A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AUDIO ONLY SEATS
        Text(
            text = "Audio Seats",
            color = Color(0xFF28565B),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        layoutSpec.audioSeats.toList().chunked(5).forEach { rowSeats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowSeats.forEach { seatNo ->

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(86.dp)
                            .clickable {
                        selectedRoomSeat = seatNo
                        giftReceiverSeat = seatNo
                        cameraEnabled = false

                        if (currentUid.isBlank()) {
                            roomMessages = roomMessages +
                                "System: Please sign in first"
                        } else if (myJoinedSeat == seatNo) {

                            // Leave current seat safely
                            val seatRef = roomRef
                                .collection("seats")
                                .document(seatNo.toString())

                            firestore.runTransaction { transaction ->

                                val seatSnapshot =
                                    transaction.get(seatRef)

                                val ownerUid =
                                    seatSnapshot.getString("uid")

                                if (
                                    seatSnapshot.exists() &&
                                    ownerUid == currentUid
                                ) {
                                    transaction.delete(seatRef)
                                }

                                transaction.set(
                                    membersRef.document(currentUid),
                                    mapOf(
                                        "seat" to 0,
                                        "updatedAt" to System.currentTimeMillis()
                                    ),
                                    SetOptions.merge()
                                )

                                true
                            }.addOnSuccessListener {

                                myJoinedSeat = null

                                roomMessages =
                                    roomMessages +
                                    "System: You left seat $seatNo"

                            }.addOnFailureListener {

                                roomMessages =
                                    roomMessages +
                                    "System: Could not leave seat"
                            }

                        } else {

                            // Try to lock target seat atomically
                            val targetSeatRef = roomRef
                                .collection("seats")
                                .document(seatNo.toString())

                            val oldSeatNo = myJoinedSeat

                            val oldSeatRef =
                                if (
                                    oldSeatNo != null &&
                                    oldSeatNo != seatNo
                                ) {
                                    roomRef
                                        .collection("seats")
                                        .document(oldSeatNo.toString())
                                } else {
                                    null
                                }

                            firestore.runTransaction { transaction ->

                                // ALL READS FIRST
                                val targetSnapshot =
                                    transaction.get(targetSeatRef)

                                val oldSnapshot =
                                    oldSeatRef?.let {
                                        transaction.get(it)
                                    }

                                val targetOwner =
                                    targetSnapshot.getString("uid")

                                // Another user already owns this seat
                                if (
                                    targetSnapshot.exists() &&
                                    targetOwner != null &&
                                    targetOwner != currentUid
                                ) {
                                    throw IllegalStateException(
                                        "SEAT_OCCUPIED"
                                    )
                                }

                                // Remove user's old seat lock
                                if (
                                    oldSeatRef != null &&
                                    oldSnapshot != null &&
                                    oldSnapshot.exists() &&
                                    oldSnapshot.getString("uid") == currentUid
                                ) {
                                    transaction.delete(oldSeatRef)
                                }

                                // Lock new seat
                                transaction.set(
                                    targetSeatRef,
                                    mapOf(
                                        "uid" to currentUid,
                                        "name" to currentName,
                                        "photoUrl" to currentPhotoUrl,
                                        "seat" to seatNo,
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                )

                                // Sync member document
                                transaction.set(
                                    membersRef.document(currentUid),
                                    mapOf(
                                        "uid" to currentUid,
                                        "name" to currentName,
                                        "photoUrl" to currentPhotoUrl,
                                        "seat" to seatNo,
                                        "updatedAt" to System.currentTimeMillis()
                                    ),
                                    SetOptions.merge()
                                )

                                true

                            }.addOnSuccessListener {

                                myJoinedSeat = seatNo

                                roomMessages =
                                    roomMessages +
                                    "System: You joined audio seat $seatNo"

                            }.addOnFailureListener { error ->

                                if (
                                    error.message
                                        ?.contains("SEAT_OCCUPIED") == true
                                ) {
                                    roomMessages =
                                        roomMessages +
                                        "System: Seat $seatNo is already occupied"
                                } else {
                                    roomMessages =
                                        roomMessages +
                                        "System: Could not join seat"
                                }
                            }
                        }
                    },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedRoomSeat == seatNo)
                            Color(0xFFC6E9E4)
                        else
                            Color(0xFFE5F4F1),
                        border = BorderStroke(1.dp, Color(0xFFFFC24B))
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val seatPhoto =
                                if (myJoinedSeat == seatNo)
                                    currentPhotoUrl
                                else
                                    seatMemberPhotos[seatNo].orEmpty()

                            if (seatPhoto.isNotBlank()) {
                                AsyncImage(
                                    model = seatPhoto,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    if (
                                        myJoinedSeat == seatNo ||
                                        occupiedSeats.contains(seatNo)
                                    )
                                        "👤"
                                    else
                                        "+",
                                    fontSize = 22.sp,
                                    color = Color(0xFF326A6E)
                                )
                            }

                            Text(
                                "$seatNo",
                                fontSize = 10.sp,
                                color = Color(0xFF326A6E)
                            )

                            Text(
                                when {
                                    myJoinedSeat == seatNo -> "You"

                                    occupiedSeats.contains(seatNo) ->
                                        seatMemberNames[seatNo] ?: "User"

                                    else -> "Invite"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF54787A),
                                maxLines = 1
                            )
                            if (myJoinedSeat == seatNo || occupiedSeats.contains(seatNo)) {
                                Text(
                                    if (myJoinedSeat == seatNo) {
                                        if (myMicEnabled) "Mic on" else "Muted"
                                    } else if (seatMemberMics[seatNo] == false) "Muted" else "Mic on",
                                    fontSize = 9.sp, color = Color(0xFF54787A), maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        Spacer(modifier = Modifier.height(60.dp))

        // Chat/System area - reference style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.width(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFFFC24B)
                            ) {
                                Text(
                                    text = "All",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 18.dp),
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF6A3217)
                            ) {
                                Text(
                                    text = "Chat",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 18.dp),
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF6A3217),
                            border = BorderStroke(1.dp, Color(0xFFFFC24B))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "📣  Welcome to RIMI Game Room!",
                                    color = Color(0xFFFFD36A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Warning: Pornography, vulgarity, violence, juveniles and other related situations are strictly prohibited. AI system reviews it 24 hours a day. Violations may be punished.",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF6A3217),
                        border = BorderStroke(1.dp, Color(0xFFFFC24B))
                    ) {
                        Text(
                            text = roomMessages.lastOrNull()
                                ?: "System: Welcome to the room",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = "Say something...",
                                    fontSize = 11.sp
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(28.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            modifier = Modifier.clickable {
                                val msg = chatInput.trim()
                                if (msg.isNotEmpty()) {
                                    roomMessages = roomMessages + "You: $msg"
                                    chatInput = ""
                                }
                            },
                            shape = CircleShape,
                            color = Color(0xFFFFC24B)
                        ) {
                            Text(
                                text = "➤",
                                modifier = Modifier.padding(16.dp),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    // Room actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Chat" to "💬",
                "Game" to "🎮",
                "Gift" to "🎁",
                "Coin" to "🪙"
            ).forEach { (label, icon) ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            when (label) {
                                "Chat" -> {
                                    roomMessages = roomMessages + "System: Chat ready"
                                }
                                "Game" -> showGameMenu = true
                                "Gift" -> showGiftMenu = true
                                "Coin" -> showCoinMenu = true
                            }
                        },
                    shape = CircleShape,
                    color = Color(0xFF245F63)
                ) {
                    Text(
                        text = "$icon\n$label",
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Create Room - fixed at bottom
        
        }

        

        // Bottom controls - NO Leave Room
        Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showPkMenu = true }
                        ) {
                            Surface(
                                modifier = Modifier.size(58.dp),
                                shape = CircleShape,
                                color = Color(0xFF6B3418),
                                border = BorderStroke(1.dp, Color(0xFFFFC15A))
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("PK", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("PK", color = Color.White, fontSize = 11.sp)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                roomMessages = roomMessages + "System: Message"
                            }
                        ) {
                            Surface(
                                modifier = Modifier.size(58.dp),
                                shape = CircleShape,
                                color = Color(0xFF148C72)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("💬", fontSize = 25.sp)
                                }
                            }
                            Text("Message", color = Color.White, fontSize = 11.sp)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showGameMenu = true }
                        ) {
                            Surface(
                                modifier = Modifier.size(58.dp),
                                shape = CircleShape,
                                color = Color(0xFF356FE8)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("🎮", fontSize = 25.sp)
                                }
                            }
                            Text("Game", color = Color.White, fontSize = 11.sp)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showGiftMenu = true }
                        ) {
                            Surface(
                                modifier = Modifier.size(58.dp),
                                shape = CircleShape,
                                color = Color(0xFFE84B89)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("🎁", fontSize = 25.sp)
                                }
                            }
                            Text("Gift", color = Color.White, fontSize = 11.sp)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showMoreMenu = true }
                        ) {
                            Surface(
                                modifier = Modifier.size(58.dp),
                                shape = CircleShape,
                                color = Color(0xFF5A2A12),
                                border = BorderStroke(1.dp, Color(0xFFFFC15A))
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("•••", color = Color.White, fontSize = 19.sp)
                                }
                            }
                            Text("More", color = Color.White, fontSize = 11.sp)
                        }
                    }
    }
    else {
        if (liveRooms.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No rooms are live yet", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Create a room or join with a room ID.", color = Color(0xFFD5F5EA))
                OutlinedButton(onClick = { showJoinRoomDialog = true }) {
                    Text("Join by room ID")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items((if (sortPopular) liveRooms.sortedByDescending { it.members }
                    else liveRooms.sortedByDescending { it.createdAt }).chunked(2)) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { room ->
                            RoomDiscoveryCard(room, onClick = {
                                roomId = room.id
                                myJoinedSeat = null
                                cameraEnabled = false
                                selectedRoomSeat = 1
                                inRoom = true
                            }, modifier = Modifier.weight(1f))
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

            "Chat" -> {
                CallDirectoryPanel(liveRooms, onCall = {
                    chatActionMessage = "Voice calls require a real-time media service and are not available yet."
                }, modifier = Modifier.weight(1f))
                if (chatActionMessage.isNotBlank()) {
                    Text(chatActionMessage, color = Color.White, fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 20.dp))
                }
            }

            "Top" -> {
                TopRoomsPanel(liveRooms, onOpenRooms = { selectedTab = "Party" },
                    modifier = Modifier.weight(1f))
            }
        }

        // GAMI_FINAL_BOTTOM_SPACER
            if (selectedTab !in listOf("Party", "Chat", "Top", "Profile") || inRoom ||
                (selectedTab == "Party" && !partyHasRooms)) {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Create room
        if (!inRoom && selectedTab != "Profile") Button(
            onClick = {
                    showCreateRoomSetup = true
                },
                modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 8.dp)
                .height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF057C67)
            )
        ) {
            Text(
                "🎮  Create a Room",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // One navigation component, including Profile. Subtabs are part of Home.
        if (!inRoom) RimiMainBottomBar(selectedTab = selectedTab,
            onNavigate = { selectedTab = it })
    }
    }
}
