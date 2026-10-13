@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.devil.livetogather

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.Coil
import coil.ImageLoader
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

val BrandDark = Color(0xFF0B090A)
val BrandCard = Color(0xFF161214)
val BrandCardSubtle = Color(0xFF201A1D)
val BrandCoral = Color(0xFFFF5E62)
val BrandOrange = Color(0xFFFF9966)
val BrandBorder = Color(0xFF2A2226)
val BrandGold = Color(0xFFFFD700)
val BrandCyan = Color(0xFF00E5FF)
val BrandBlue = Color(0xFF3897F0)
val BrandGreen = Color(0xFF00E676)

val SETTINGS_TOPIC_OPTIONS = mapOf(
    "Interests" to listOf(
        "Travel, Cafe, Night Drives",
        "Music, Concerts & Festivals",
        "Fitness, Gym & Yoga",
        "Photography & Filmmaking",
        "Dancing & Clubbing",
        "Foodie, Cooking & Baking",
        "Movies, Series & Anime",
        "Reading, Books & Literature",
        "Gaming, Tech & Esports",
        "Trekking, Hiking & Nature",
        "Art, Painting & Design",
        "Fashion, Styling & Shopping"
    ),
    "Looking for" to listOf(
        "Long-term relationship",
        "Short-term relationship",
        "Long-term, open to short",
        "Short-term, open to long",
        "New friends & hangout",
        "Casual dating & coffee",
        "Marriage / Life partner",
        "Still figuring it out"
    ),
    "Open to..." to listOf(
        "New connections",
        "Serious commitment",
        "Monogamy",
        "Open / Non-monogamy",
        "Travel buddies",
        "Weekend party partner",
        "Exploring possibilities"
    ),
    "Add languages" to listOf(
        "English, Hindi, Bengali",
        "English, Hindi",
        "Hindi, Punjabi, English",
        "Hindi, Urdu, English",
        "Tamil, English, Hindi",
        "Telugu, English, Hindi",
        "Marathi, Hindi, English",
        "Gujarati, Hindi, English",
        "Bengali, English",
        "Spanish, English",
        "French, English",
        "German, English"
    ),
    "Zodiac" to listOf(
        "Aries ♈",
        "Taurus ♉",
        "Gemini ♊",
        "Cancer ♋",
        "Leo ♌",
        "Virgo ♍",
        "Libra ♎",
        "Scorpio ♏",
        "Sagittarius ♐",
        "Capricorn ♑",
        "Aquarius ♒",
        "Pisces ♓"
    ),
    "Education" to listOf(
        "Masters in Design",
        "Bachelors / Undergraduate",
        "Postgraduate / Masters",
        "Doctorate / PhD",
        "In College / University",
        "High School",
        "Trade / Vocational School",
        "Self-Taught / Entrepreneur"
    ),
    "Family Plans" to listOf(
        "Open to kids",
        "Want children",
        "Don't want children",
        "Have children & want more",
        "Have children & don't want more",
        "Not sure yet"
    ),
    "Communication Style" to listOf(
        "Big time texter",
        "Phone caller",
        "Video chatter",
        "Slow replier",
        "Better in person",
        "Memes are my love language"
    ),
    "Love Style" to listOf(
        "Quality time",
        "Words of affirmation",
        "Physical touch",
        "Acts of service",
        "Receiving gifts"
    ),
    "Pets" to listOf(
        "Dog lover",
        "Cat person",
        "Have dogs & cats",
        "Love all animals",
        "Birds / Reptiles",
        "Fish",
        "No pets",
        "Allergic to pets",
        "Want pets in future"
    ),
    "Drinking" to listOf(
        "Socially",
        "Never / Teetotaler",
        "On special occasions",
        "Frequently",
        "Sober / Quit drinking"
    ),
    "Smoking" to listOf(
        "No",
        "Non-smoker",
        "Social smoker",
        "Regular smoker",
        "Trying to quit",
        "Vape only"
    ),
    "Workout" to listOf(
        "Active",
        "Everyday",
        "Often (3-4 times/wk)",
        "Sometimes",
        "Never",
        "Yoga / Pilates",
        "Gym & Weightlifting",
        "Running / Cycling"
    ),
    "Social Media" to listOf(
        "@maya_livetogather",
        "Instagram: @maya_insta",
        "Snapchat: @maya_snap",
        "LinkedIn connected",
        "Prefer private"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(DataUriFetcher.Factory(this@MainActivity))
            }
            .build()
        Coil.setImageLoader(imageLoader)
        setContent {
            LiveTogatherApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTogatherApp() {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val prefs = remember { context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE) }

    val deviceId = remember {
        try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // Persistent User ID: bound directly to Firebase Auth UID or SharedPreferences
    val initialUid = auth.currentUser?.uid
        ?: prefs.getString("user_uid", null)
        ?: "user_${UUID.randomUUID().toString().take(8)}"
    var currentUserId by remember { mutableStateOf(initialUid) }

    // Hydrate initial UserProfile from SharedPreferences (ensures profile survives reinstall)
    var currentUser by remember {
        mutableStateOf(
            UserProfile(
                uid = currentUserId,
                deviceId = prefs.getString("profile_device_id", deviceId) ?: deviceId,
                name = prefs.getString("profile_name", "Maya") ?: "Maya",
                age = prefs.getInt("profile_age", 24),
                gender = prefs.getString("profile_gender", "Woman") ?: "Woman",
                city = prefs.getString("profile_city", "Kolkata") ?: "Kolkata",
                photo = prefs.getString("profile_photo", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80") ?: "",
                activeIntent = prefs.getString("profile_active_intent", "free_tonight") ?: "free_tonight",
                bio = prefs.getString("profile_bio", "Love late-night coffee and spontaneous road trips 🚗") ?: "",
                hobbies = prefs.getString("profile_hobbies", "Dancing, Movies, Photography") ?: "",
                interests = prefs.getString("profile_interests", "Travel, Cafe, Night Drives") ?: "",
                profession = prefs.getString("profile_profession", "UX Designer") ?: "",
                education = prefs.getString("profile_education", "Masters in Design") ?: "",
                plan = prefs.getString("profile_plan", "VIP LEVEL ★") ?: "VIP LEVEL ★",
                locationName = prefs.getString("profile_location", "New Town, West Bengal") ?: "New Town, West Bengal",
                maxDistanceKm = prefs.getInt("profile_max_distance", 53),
                showFurtherAway = prefs.getBoolean("profile_show_further", true),
                interestedIn = prefs.getString("profile_interested_in", "Women") ?: "Women",
                minAge = prefs.getInt("profile_min_age", 21),
                maxAge = prefs.getInt("profile_max_age", 29),
                current_room = prefs.getString("profile_current_room", "") ?: ""
            )
        )
    }

    var currentScreen by remember { mutableStateOf("discover") }
    var discoverTab by remember { mutableStateOf("cards") } // "cards" or "rooms"
    var selectedCategory by remember { mutableStateOf("all") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showPostMomentDialog by remember { mutableStateOf(false) }
    var showDirectMessageDialog by remember { mutableStateOf<UserProfile?>(null) }
    var showCommentsMoment by remember { mutableStateOf<MomentItem?>(null) }
    var activeRoomDeck by remember { mutableStateOf<RoomItem?>(null) }
    var activeAudioRoom by remember { mutableStateOf<RoomItem?>(null) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var createRoomInitialCategory by remember { mutableStateOf("free_tonight") }
    var activeCategoryHub by remember { mutableStateOf<IntentCategory?>(null) }
    var matchedPartner by remember { mutableStateOf<UserProfile?>(null) }
    var activeChatPartner by remember { mutableStateOf<UserProfile?>(null) }

    // Real-time Lists from Firestore
    val discoverProfiles = remember { mutableStateListOf<UserProfile>() }
    val incomingSwipes = remember { mutableStateListOf<SwipeRecord>() }
    val matches = remember { mutableStateListOf<MatchItem>() }
    val moments = remember { mutableStateListOf<MomentItem>() }
    val notifications = remember { mutableStateListOf<NotificationItem>() }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val roomsList = remember { mutableStateListOf<RoomItem>() }

    // --- Firebase Authentication & Firestore Setup ---
    LaunchedEffect(Unit) {
        // Authenticate anonymously so Firestore security rules don't block requests with PERMISSION_DENIED
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    val authUid = result.user?.uid
                    if (authUid != null) {
                        currentUserId = authUid
                        prefs.edit().putString("user_uid", authUid).apply()
                        currentUser = currentUser.copy(uid = authUid)
                    }
                    Log.d("Auth", "Signed in anonymously as $currentUserId")
                }
                .addOnFailureListener { e ->
                    Log.w("Auth", "Anonymous auth failed: ${e.message}")
                }
        } else {
            val authUid = auth.currentUser!!.uid
            currentUserId = authUid
            prefs.edit().putString("user_uid", authUid).apply()
            currentUser = currentUser.copy(uid = authUid)
        }

        // Initialize Rooms Catalog & Real-Time Snapshot Listener
        roomsList.clear()
        roomsList.addAll(INTENT_ROOMS_CATALOG)

        db.collection("rooms").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("Firestore", "Rooms snapshot error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                if (snapshot.isEmpty) {
                    INTENT_ROOMS_CATALOG.forEach { room ->
                        db.collection("rooms").document(room.id).set(room)
                    }
                } else {
                    val fetchedRooms = snapshot.documents.mapNotNull { it.toObject(RoomItem::class.java) }
                    if (fetchedRooms.isNotEmpty()) {
                        roomsList.clear()
                        roomsList.addAll(fetchedRooms)
                    }
                }
            }
        }
    }

    // --- Real-Time Firestore Listeners ---
    LaunchedEffect(currentUserId) {
        // 1. Sync Current User Profile
        val userDoc = db.collection("users").document(currentUserId)
        userDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("Firestore", "User listener error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)?.let {
                    currentUser = it.copy(deviceId = deviceId)
                    // Cache to SharedPreferences
                    saveProfileToPrefs(prefs, currentUser)
                }
            } else {
                // If profile doesn't exist for currentUserId yet, check if there's a profile for this deviceId
                // (e.g. from prior installation before reinstall)
                if (deviceId.isNotEmpty()) {
                    db.collection("users").whereEqualTo("deviceId", deviceId).limit(1).get()
                        .addOnSuccessListener { prevDocs ->
                            if (prevDocs != null && !prevDocs.isEmpty) {
                                val prevProfile = prevDocs.documents[0].toObject(UserProfile::class.java)
                                if (prevProfile != null) {
                                    val restored = prevProfile.copy(uid = currentUserId, deviceId = deviceId)
                                    currentUser = restored
                                    saveProfileToPrefs(prefs, restored)
                                    userDoc.set(restored, SetOptions.merge())
                                    Log.d("Firestore", "Restored existing profile from previous install for device $deviceId")
                                    return@addOnSuccessListener
                                }
                            }
                            val initial = currentUser.copy(uid = currentUserId, deviceId = deviceId)
                            currentUser = initial
                            userDoc.set(initial, SetOptions.merge())
                        }
                        .addOnFailureListener {
                            val initial = currentUser.copy(uid = currentUserId, deviceId = deviceId)
                            currentUser = initial
                            userDoc.set(initial, SetOptions.merge())
                        }
                } else {
                    userDoc.set(currentUser, SetOptions.merge())
                }
            }
        }

        // 2. Discover Profiles Listener (Real-Time Real Users)
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("Firestore", "Discover profiles error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                    .filter { it.uid != currentUserId && !it.uid.startsWith("USER_") && !currentUser.blockedUserIds.contains(it.uid) }
                discoverProfiles.clear()
                discoverProfiles.addAll(list)
            }
        }

        // 3. Notifications Listener (Real-Time Bell)
        db.collection("notifications")
            .whereEqualTo("targetUserId", currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toObject(NotificationItem::class.java) }
                        .sortedByDescending { it.timestamp }
                    notifications.clear()
                    notifications.addAll(items)
                }
            }

        // 4. Incoming Swipes Listener
        db.collection("swipes")
            .whereEqualTo("toUserId", currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(SwipeRecord::class.java) }
                    incomingSwipes.clear()
                    incomingSwipes.addAll(list)
                }
            }

        // 5. Mutual Matches Listener
        db.collection("matches")
            .whereArrayContains("users", currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(MatchItem::class.java) }
                    matches.clear()
                    matches.addAll(list)
                }
            }

        // 6. Moments Feed Listener (Real-Time Real Moments)
        db.collection("moments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(MomentItem::class.java) }
                        .filter { !it.userId.startsWith("USER_") }
                    moments.clear()
                    moments.addAll(list)
                }
            }
    }

    // Chat room listener when chat is opened
    LaunchedEffect(activeChatPartner) {
        if (activeChatPartner != null) {
            val partner = activeChatPartner!!
            val matchId = listOf(currentUserId, partner.uid).sorted().joinToString("_")
            db.collection("matches").document(matchId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val msgs = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
                        chatMessages.clear()
                        chatMessages.addAll(msgs)
                    }
                }
        }
    }

    // Filtered Discover Profiles
    val filteredProfiles = remember(discoverProfiles, selectedCategory, currentUser) {
        discoverProfiles.filter { profile ->
            if (!currentUser.enableDiscovery) return@filter false
            val matchesCategory = selectedCategory == "all" || profile.activeIntent == selectedCategory
            val matchesDistance = currentUser.showFurtherAway || profile.distanceKm <= currentUser.maxDistanceKm
            val matchesAge = currentUser.showOutsideAge || (profile.age in currentUser.minAge..currentUser.maxAge)
            val interested = currentUser.interestedIn.lowercase().trim()
            val matchesGender = when {
                interested.isEmpty() || interested == "everyone" || interested == "all" -> true
                interested in listOf("women", "female", "woman") -> profile.gender.lowercase().trim() in listOf("women", "female", "woman") || profile.gender.isBlank()
                interested in listOf("men", "male", "man") -> profile.gender.lowercase().trim() in listOf("men", "male", "man") || profile.gender.isBlank()
                else -> true
            }
            matchesCategory && matchesDistance && matchesAge && matchesGender
        }
    }

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Priority 1: When keyboard is open anywhere, system back button dismisses ONLY the keyboard
    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    // Priority 2: When keyboard is closed and user is on another screen, back button returns to Discover tab
    BackHandler(enabled = !isKeyboardOpen && currentScreen != "discover") {
        currentScreen = "discover"
    }

    Box(modifier = Modifier.fillMaxSize().background(BrandDark)) {
        Scaffold(
            containerColor = BrandDark,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandCard)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = BrandCoral,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LiveTogather",
                            color = BrandCoral,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Real-time Notification Bell with Badge
                        IconButton(onClick = { showNotificationsDialog = true }) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge(
                                            containerColor = BrandCoral,
                                            contentColor = Color.White
                                        ) {
                                            Text("$unreadNotificationsCount", fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (unreadNotificationsCount > 0) BrandOrange else Color.White
                                )
                            }
                        }

                        // Settings Gear Icon
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = BrandCard) {
                    val items = listOf(
                        Triple("discover", "Discover", Icons.Default.LocalFireDepartment),
                        Triple("likes", "Likes", Icons.Default.Favorite),
                        Triple("chats", "Chats", Icons.Default.ChatBubble),
                        Triple("moments", "Moments", Icons.Default.CameraAlt),
                        Triple("profile", "Profile", Icons.Default.Person)
                    )
                    items.forEach { (route, label, icon) ->
                        val selected = currentScreen == route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentScreen = route },
                            icon = {
                                Icon(
                                    icon,
                                    contentDescription = label,
                                    tint = if (selected) BrandCoral else Color.Gray
                                )
                            },
                            label = {
                                Text(
                                    label,
                                    fontSize = 10.sp,
                                    color = if (selected) BrandCoral else Color.Gray
                                )
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    "discover" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Discover View Toggle: Swipe Cards vs Intent Rooms Grid
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrandCard)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(BrandDark)
                                        .padding(4.dp)
                                ) {
                                    Row {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(if (discoverTab == "cards") BrandCoral else Color.Transparent)
                                                .clickable { discoverTab = "cards" }
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                "🔥 Swipe Cards",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(if (discoverTab == "rooms") BrandCoral else Color.Transparent)
                                                .clickable { discoverTab = "rooms" }
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                "🏛️ Intent Rooms (13)",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            if (discoverTab == "cards") {
                                DiscoverScreenContent(
                                    profiles = filteredProfiles,
                                    selectedCategory = selectedCategory,
                                    currentUser = currentUser,
                                    onSelectCategory = { selectedCategory = it },
                                    onPass = { profile ->
                                        recordSwipe(db, currentUserId, profile.uid, "pass")
                                        Toast.makeText(context, "Passed ${profile.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    onSuperStar = { profile ->
                                        recordSwipe(db, currentUserId, profile.uid, "superstar")
                                        sendNotification(
                                            db = db,
                                            targetUserId = profile.uid,
                                            sender = currentUser,
                                            type = "superstar",
                                            title = "⭐ SuperStar Received!",
                                            message = "${currentUser.name} gave you a SuperStar!"
                                        )
                                        Toast.makeText(context, "SuperStar sent to ${profile.name}!", Toast.LENGTH_SHORT).show()
                                    },
                                    onDirectMessage = { profile ->
                                        // Tier Rule Check: VIP / Platinum only
                                        if (currentUser.isVipOrPlatinum()) {
                                            showDirectMessageDialog = profile
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Direct messaging is exclusive to VIP or Platinum members. Like back to chat for free!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    onLike = { profile ->
                                        recordSwipe(db, currentUserId, profile.uid, "like")
                                        checkAndHandleMutualMatch(
                                            db = db,
                                            currentUser = currentUser,
                                            partner = profile,
                                            onMatch = { matchedPartner = profile }
                                        )
                                    }
                                )
                            } else {
                                // 2-Column Clean Intent Category Hubs Grid Layout
                                IntentRoomsGrid(
                                    categories = CATEGORY_LIST.filter { it.id != "all" },
                                    catalogRooms = INTENT_ROOMS_CATALOG,
                                    rooms = roomsList,
                                    allUsers = discoverProfiles + currentUser,
                                    onOpenCategoryHub = { category ->
                                        activeCategoryHub = category
                                    },
                                    onJoinRoom = { room ->
                                        // 1. Register presence in rooms/{roomId}/members/{userId}
                                        val isHost = room.hostId == currentUserId || (room.hostId.isEmpty() && room.isCustom)
                                        val member = RoomMember(
                                            uid = currentUserId,
                                            name = currentUser.name,
                                            photoUrl = currentUser.photo,
                                            joinedAt = System.currentTimeMillis(),
                                            role = if (isHost) "host" else "listener",
                                            tier = if (currentUser.isVipOrPlatinum()) {
                                                if (currentUser.plan.contains("VIP", ignoreCase = true)) "VIP" else "Platinum"
                                            } else "Standard"
                                        )
                                        db.collection("rooms").document(room.id)
                                            .collection("members").document(currentUserId)
                                            .set(member)

                                        if (isHost) {
                                            db.collection("rooms").document(room.id).update(
                                                mapOf(
                                                    "isHostLive" to true,
                                                    "statusMessage" to "LIVE NOW"
                                                )
                                            )
                                            broadcastLiveNotificationToFollowers(
                                                db = db,
                                                hostId = currentUserId,
                                                hostName = currentUser.name,
                                                hostPhoto = currentUser.photo,
                                                room = room
                                            )
                                        }

                                        // 2. Update current_room in Firestore user doc
                                        currentUser = currentUser.copy(current_room = room.id)
                                        saveProfileToPrefs(prefs, currentUser)
                                        db.collection("users").document(currentUserId)
                                            .update("current_room", room.id)
                                            .addOnSuccessListener {
                                                Log.d("Rooms", "Joined room ${room.id} as ${member.role}")
                                            }

                                        // 3. Open Audio Hangout Room Dialog
                                        activeAudioRoom = room
                                    }
                                )
                            }
                        }
                    }

                    "likes" -> {
                        LikesScreenContent(
                            swipes = incomingSwipes,
                            db = db,
                            onMatchBack = { liker ->
                                recordSwipe(db, currentUserId, liker.uid, "like")
                                checkAndHandleMutualMatch(
                                    db = db,
                                    currentUser = currentUser,
                                    partner = liker,
                                    onMatch = { matchedPartner = liker }
                                )
                            }
                        )
                    }

                    "chats" -> {
                        ChatsScreenContent(
                            matches = matches,
                            currentUserId = currentUserId,
                            currentUser = currentUser,
                            db = db,
                            onOpenChat = { partner -> activeChatPartner = partner },
                            onToggleBlock = { partner ->
                                val isBlocked = currentUser.blockedUserIds.contains(partner.uid)
                                val updatedBlocked = if (isBlocked) {
                                    currentUser.blockedUserIds - partner.uid
                                } else {
                                    currentUser.blockedUserIds + partner.uid
                                }
                                currentUser = currentUser.copy(blockedUserIds = updatedBlocked)
                                saveProfileToPrefs(prefs, currentUser)
                                db.collection("users").document(currentUserId)
                                    .update("blockedUserIds", updatedBlocked)
                                Toast.makeText(
                                    context,
                                    if (isBlocked) "${partner.name} unblocked" else "${partner.name} blocked",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }

                    "moments" -> {
                        MomentsScreenContent(
                            moments = moments,
                            currentUserId = currentUserId,
                            onPostClick = { showPostMomentDialog = true },
                            onLikeMoment = { moment ->
                                toggleMomentLike(db, moment, currentUserId)
                            },
                            onCommentMoment = { moment ->
                                showCommentsMoment = moment
                            }
                        )
                    }

                    "profile" -> {
                        ProfileScreenContent(
                            currentUser = currentUser,
                            userMoments = moments.filter { it.userId == currentUserId },
                            db = db,
                            currentUserId = currentUserId,
                            onUpdateProfile = { updated ->
                                val profileWithDevice = updated.copy(uid = currentUserId, deviceId = deviceId)
                                currentUser = profileWithDevice
                                saveProfileToPrefs(prefs, profileWithDevice)
                                db.collection("users").document(currentUserId)
                                    .set(profileWithDevice, SetOptions.merge())
                            },
                            onSelectPlan = { plan ->
                                val updated = currentUser.copy(plan = plan.title)
                                currentUser = updated
                                saveProfileToPrefs(prefs, updated)
                                db.collection("users").document(currentUserId)
                                    .update("plan", plan.title)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Subscribed to ${plan.title}!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Plan updated locally!", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onOpenSettings = { showSettingsDialog = true },
                            onLogout = {
                                prefs.edit().clear().apply()
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteAccount = {
                                prefs.edit().clear().apply()
                                db.collection("users").document(currentUserId).delete()
                                Toast.makeText(context, "Account deleted", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }
        }

        // --- Dialogs & Modals ---

        // 0. Advanced Real-time Audio Hangout Room Dialog
        if (activeAudioRoom != null) {
            val room = activeAudioRoom!!
            AudioHangoutRoomDialog(
                initialRoom = room,
                currentUser = currentUser,
                allProfiles = discoverProfiles,
                db = db,
                onDismiss = {
                    // If leaving user is the room host, mark room offline
                    if (room.hostId == currentUserId || (room.isCustom && room.hostId.isBlank())) {
                        db.collection("rooms").document(room.id).update(
                            mapOf(
                                "isHostLive" to false,
                                "statusMessage" to "Host will be live soon",
                                "isPlaying" to false
                            )
                        )
                        if (room.category.isNotBlank()) {
                            db.collection("intent_categories").document(room.category)
                                .collection("rooms").document(room.id).update(
                                    mapOf(
                                        "isHostLive" to false,
                                        "statusMessage" to "Host will be live soon",
                                        "isPlaying" to false
                                    )
                                )
                        }
                    }

                    // Remove presence from rooms/{roomId}/members/{currentUserId}
                    db.collection("rooms").document(room.id)
                        .collection("members").document(currentUserId)
                        .delete()
                    if (room.category.isNotBlank()) {
                        db.collection("intent_categories").document(room.category)
                            .collection("rooms").document(room.id)
                            .collection("members").document(currentUserId)
                            .delete()
                    }
                    currentUser = currentUser.copy(current_room = "")
                    saveProfileToPrefs(prefs, currentUser)
                    db.collection("users").document(currentUserId).update("current_room", "")
                    AudioPlayerManager.stop()
                    activeAudioRoom = null
                },
                onDirectChat = { partner ->
                    showDirectMessageDialog = partner
                }
            )
        }

        // 0.05 Category Hub / Lobby Dialog (Category-Scoped Sub-Rooms Grid)
        if (activeCategoryHub != null) {
            val cat = activeCategoryHub!!
            val bannerUrl = INTENT_ROOMS_CATALOG.find { it.id == cat.id }?.banner_image_url ?: ""
            CategoryHubLobbyDialog(
                category = cat,
                categoryBannerUrl = bannerUrl,
                allRooms = roomsList,
                allUsers = discoverProfiles + currentUser,
                currentUser = currentUser,
                onDismiss = { activeCategoryHub = null },
                onCreateRoom = {
                    createRoomInitialCategory = cat.id
                    showCreateRoomDialog = true
                },
                onJoinRoom = { room ->
                    val isHost = room.hostId == currentUserId || (room.hostId.isEmpty() && room.isCustom)
                    val member = RoomMember(
                        uid = currentUserId,
                        name = currentUser.name,
                        photoUrl = currentUser.photo,
                        joinedAt = System.currentTimeMillis(),
                        role = if (isHost) "host" else "listener",
                        tier = if (currentUser.isVipOrPlatinum()) {
                            if (currentUser.plan.contains("VIP", ignoreCase = true)) "VIP" else "Platinum"
                        } else "Standard"
                    )
                    db.collection("rooms").document(room.id)
                        .collection("members").document(currentUserId)
                        .set(member)

                    if (isHost) {
                        db.collection("rooms").document(room.id).update(
                            mapOf(
                                "isHostLive" to true,
                                "statusMessage" to "LIVE NOW"
                            )
                        )
                        broadcastLiveNotificationToFollowers(
                            db = db,
                            hostId = currentUserId,
                            hostName = currentUser.name,
                            hostPhoto = currentUser.photo,
                            room = room
                        )
                    }

                    currentUser = currentUser.copy(current_room = room.id)
                    saveProfileToPrefs(prefs, currentUser)
                    db.collection("users").document(currentUserId)
                        .update("current_room", room.id)

                    activeAudioRoom = room
                }
            )
        }

        // 0.1 Create Custom Room Dialog
        if (showCreateRoomDialog) {
            CreateRoomDialog(
                currentUser = currentUser,
                initialCategory = createRoomInitialCategory,
                onDismiss = { showCreateRoomDialog = false },
                onCreate = { title, categoryId, themeUrl ->
                    val roomId = "custom_" + UUID.randomUUID().toString().take(8)
                    val catEmoji = CATEGORY_LIST.find { it.id == categoryId }?.label?.take(2)?.trim() ?: "🎙️"
                    val newRoom = RoomItem(
                        id = roomId,
                        title = title,
                        category = categoryId,
                        emoji = catEmoji,
                        banner_image_url = themeUrl,
                        hostId = currentUserId,
                        hostName = currentUser.name,
                        hostPhotoUrl = currentUser.photo,
                        nowPlayingTitle = "",
                        nowPlayingArtist = "",
                        nowPlayingUrl = "",
                        currentPlayingTrack = "",
                        playbackPosition = 0L,
                        isPlaying = false,
                        isCustom = true,
                        createdAt = System.currentTimeMillis()
                    )
                    // Save to Firestore rooms/{roomId}
                    db.collection("rooms").document(roomId).set(newRoom)
                    // Save to intent_categories/{categoryId}/rooms/{roomId}
                    db.collection("intent_categories").document(categoryId)
                        .collection("rooms").document(roomId).set(newRoom)

                    // Register host member in rooms/{roomId}/members/{userId}
                    val hostMember = RoomMember(
                        uid = currentUserId,
                        name = currentUser.name,
                        photoUrl = currentUser.photo,
                        joinedAt = System.currentTimeMillis(),
                        role = "host",
                        tier = if (currentUser.isVipOrPlatinum()) {
                            if (currentUser.plan.contains("VIP", ignoreCase = true)) "VIP" else "Platinum"
                        } else "Standard"
                    )
                    db.collection("rooms").document(roomId).collection("members").document(currentUserId).set(hostMember)
                    db.collection("intent_categories").document(categoryId)
                        .collection("rooms").document(roomId)
                        .collection("members").document(currentUserId).set(hostMember)

                    // Update user's current_room
                    currentUser = currentUser.copy(current_room = roomId)
                    saveProfileToPrefs(prefs, currentUser)
                    db.collection("users").document(currentUserId).update("current_room", roomId)

                    showCreateRoomDialog = false
                    activeAudioRoom = newRoom
                    Toast.makeText(context, "Room \"$title\" created! You are Host 👑", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 1. Room Members Deck Dialog
        if (activeRoomDeck != null) {
            val room = activeRoomDeck!!
            RoomMembersDeckDialog(
                room = room,
                currentUser = currentUser,
                allUsers = discoverProfiles,
                onDismiss = {
                    // Leave room on dismiss
                    currentUser = currentUser.copy(current_room = "")
                    saveProfileToPrefs(prefs, currentUser)
                    db.collection("users").document(currentUserId).update("current_room", "")
                    activeRoomDeck = null
                },
                onLike = { member ->
                    recordSwipe(db, currentUserId, member.uid, "like")
                    checkAndHandleMutualMatch(
                        db = db,
                        currentUser = currentUser,
                        partner = member,
                        onMatch = { matchedPartner = member }
                    )
                    Toast.makeText(context, "Liked ${member.name}!", Toast.LENGTH_SHORT).show()
                },
                onChat = { member ->
                    if (currentUser.isVipOrPlatinum()) {
                        showDirectMessageDialog = member
                    } else {
                        Toast.makeText(
                            context,
                            "Direct messaging is exclusive to VIP or Platinum members. Like back to chat for free!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }

        // 2. Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                user = currentUser,
                onDismiss = { showSettingsDialog = false },
                onSave = { updated ->
                    currentUser = updated
                    saveProfileToPrefs(prefs, updated)
                    db.collection("users").document(currentUserId).set(updated)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Settings synced to Firestore!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Settings saved locally!", Toast.LENGTH_SHORT).show()
                        }
                    showSettingsDialog = false
                }
            )
        }

        // 3. Real-Time Notification Center Dialog
        if (showNotificationsDialog) {
            NotificationsDialog(
                notifications = notifications,
                onDismiss = { showNotificationsDialog = false },
                onMarkAllRead = {
                    notifications.forEach { notif ->
                        if (!notif.isRead) {
                            db.collection("notifications").document(notif.id).update("isRead", true)
                        }
                    }
                }
            )
        }

        // 4. Post Moment Dialog
        if (showPostMomentDialog) {
            PostMomentDialog(
                currentUser = currentUser,
                onDismiss = { showPostMomentDialog = false },
                onPost = { mediaUrl, caption ->
                    val newMoment = MomentItem(
                        id = UUID.randomUUID().toString(),
                        userId = currentUser.uid,
                        userName = currentUser.name,
                        userPhoto = currentUser.photo,
                        mediaUrl = mediaUrl,
                        caption = caption,
                        timestamp = System.currentTimeMillis()
                    )
                    db.collection("moments").document(newMoment.id).set(newMoment)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Moment uploaded successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Moment saved: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    showPostMomentDialog = false
                }
            )
        }

        // 5. Direct Message Dialog
        if (showDirectMessageDialog != null) {
            val target = showDirectMessageDialog!!
            DirectMessageDialog(
                targetUser = target,
                onDismiss = { showDirectMessageDialog = null },
                onSend = { text ->
                    val matchId = listOf(currentUserId, target.uid).sorted().joinToString("_")
                    val msg = ChatMessage(senderId = currentUserId, text = text)
                    db.collection("matches").document(matchId).collection("messages").add(msg)
                    db.collection("matches").document(matchId).set(
                        MatchItem(
                            matchId = matchId,
                            users = listOf(currentUserId, target.uid),
                            matchedIntent = currentUser.activeIntent,
                            lastMessage = text
                        )
                    )
                    sendNotification(
                        db = db,
                        targetUserId = target.uid,
                        sender = currentUser,
                        type = "message",
                        title = "New Direct Message",
                        message = "${currentUser.name}: $text"
                    )
                    showDirectMessageDialog = null
                    Toast.makeText(context, "Message sent to ${target.name}!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 6. Comments Sheet for Moments
        if (showCommentsMoment != null) {
            val moment = showCommentsMoment!!
            CommentsDialog(
                moment = moment,
                currentUser = currentUser,
                db = db,
                onDismiss = { showCommentsMoment = null }
            )
        }

        // 7. Mutual Match Celebration Dialog
        if (matchedPartner != null) {
            AlertDialog(
                onDismissRequest = { matchedPartner = null },
                containerColor = BrandCard,
                title = {
                    Text(
                        "IT'S A MATCH! 🎉",
                        color = BrandCoral,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "You and ${matchedPartner?.name} liked each other!",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.Center) {
                            AsyncImage(
                                model = currentUser.photo,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, BrandCoral, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            AsyncImage(
                                model = matchedPartner?.photo,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, BrandOrange, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val partner = matchedPartner
                            matchedPartner = null
                            activeChatPartner = partner
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral)
                    ) {
                        Text("Send Instant Message", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { matchedPartner = null }) {
                        Text("Keep Exploring", color = Color.Gray)
                    }
                }
            )
        }

        // 8. Active 1-on-1 Chat Room
        if (activeChatPartner != null) {
            val partner = activeChatPartner!!
            val matchId = listOf(currentUserId, partner.uid).sorted().joinToString("_")
            var chatInput by remember { mutableStateOf("") }
            val isBlocked = currentUser.blockedUserIds.contains(partner.uid)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandDark)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandCard)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { activeChatPartner = null }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        AsyncImage(
                            model = partner.photo,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    partner.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isBlocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "[Blocked]",
                                        color = Color.Red,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                "Intent: ${partner.activeIntent.replace('_', ' ')}",
                                color = BrandOrange,
                                fontSize = 11.sp
                            )
                        }

                        // Block / Unblock Toggle Action
                        TextButton(
                            onClick = {
                                val updatedBlocked = if (isBlocked) {
                                    currentUser.blockedUserIds - partner.uid
                                } else {
                                    currentUser.blockedUserIds + partner.uid
                                }
                                currentUser = currentUser.copy(blockedUserIds = updatedBlocked)
                                saveProfileToPrefs(prefs, currentUser)
                                db.collection("users").document(currentUserId)
                                    .update("blockedUserIds", updatedBlocked)
                                Toast.makeText(
                                    context,
                                    if (isBlocked) "${partner.name} unblocked" else "${partner.name} blocked",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Text(
                                text = if (isBlocked) "Unblock" else "Block",
                                color = if (isBlocked) BrandGreen else Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Chat Messages List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isMe = msg.senderId == currentUserId
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isMe) BrandCoral else BrandCard)
                                        .padding(horizontal = 14.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        msg.text,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Bar
                    if (isBlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrandCard)
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "You have blocked ${partner.name}.",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        val updatedBlocked = currentUser.blockedUserIds - partner.uid
                                        currentUser = currentUser.copy(blockedUserIds = updatedBlocked)
                                        saveProfileToPrefs(prefs, currentUser)
                                        db.collection("users").document(currentUserId)
                                            .update("blockedUserIds", updatedBlocked)
                                        Toast.makeText(context, "${partner.name} unblocked", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text("Unblock Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrandCard)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = {
                                    Text("Type a message...", color = Color.Gray, fontSize = 13.sp)
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        val text = chatInput.trim()
                                        chatInput = ""
                                        val newMsg = ChatMessage(senderId = currentUserId, text = text)
                                        db.collection("matches").document(matchId).collection("messages").add(newMsg)
                                        db.collection("matches").document(matchId).update("lastMessage", text)
                                        sendNotification(
                                            db = db,
                                            targetUserId = partner.uid,
                                            sender = currentUser,
                                            type = "message",
                                            title = "New Message",
                                            message = "${currentUser.name}: $text"
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = BrandCoral
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. INTENT ROOMS 2-COLUMN GRID (REQUIREMENT 1)
// -------------------------------------------------------------
@Composable
fun IntentRoomsGrid(
    categories: List<IntentCategory> = CATEGORY_LIST.filter { it.id != "all" },
    catalogRooms: List<RoomItem> = INTENT_ROOMS_CATALOG,
    rooms: List<RoomItem> = emptyList(),
    allUsers: List<UserProfile>,
    onCreateRoom: () -> Unit = {},
    onOpenCategoryHub: (IntentCategory) -> Unit = {},
    onJoinRoom: (RoomItem) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Live Intent Categories",
                    color = BrandGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Explore 13 curated category hubs to meet active members and join live rooms.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { category ->
                val catalog = catalogRooms.find { it.id == category.id }
                    ?: INTENT_ROOMS_CATALOG.find { it.id == category.id }
                    ?: RoomItem(id = category.id, title = category.label, banner_image_url = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3")

                // Dynamic real-time active users in this category
                val activeCount = allUsers.count {
                    it.activeIntent == category.id || it.current_room == category.id || it.current_room.startsWith(category.id)
                }

                // Sub-rooms created under this category
                val subRoomsCount = rooms.count { it.category == category.id && it.isCustom }
                val categoryRooms = rooms.filter { it.category == category.id || it.id == category.id }
                val isAnyRoomLive = categoryRooms.any { it.isHostLive }
                val isAnyAudioPlaying = categoryRooms.any { it.isHostLive && it.isPlaying }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(205.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, BrandBorder, RoundedCornerShape(18.dp))
                        .clickable { onOpenCategoryHub(category) }
                ) {
                    // Fixed curated system background image (Read-only, system managed)
                    AsyncImage(
                        model = catalog.banner_image_url,
                        contentDescription = category.label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Dark gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Black.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )

                    // Card Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Live active users counter chip & Sub-rooms badge / LIVE badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAnyRoomLive) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(BrandCoral.copy(alpha = 0.85f))
                                        .border(0.5.dp, BrandGold, RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .border(0.5.dp, BrandGreen.copy(alpha = 0.5f), RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(BrandGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$activeCount Active",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (subRoomsCount > 0) {
                                Text(
                                    text = "🔥 $subRoomsCount Rooms",
                                    color = BrandGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(50))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Bottom Title & Enter Hub Button
                        Column {
                            Text(
                                text = category.label,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Equalizer Wave or Status Line
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MusicVisualizerWave(isPlaying = isAnyRoomLive && isAnyAudioPlaying)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAnyRoomLive) "🔴 Room is Live" else "⏳ Host will be live soon",
                                    color = if (isAnyRoomLive) BrandGreen else Color.LightGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { onOpenCategoryHub(category) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            ) {
                                Text("Enter Hub 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. ROOM MEMBERS DECK & TIER-BASED MESSAGING (REQUIREMENTS 2 & 3)
// -------------------------------------------------------------
@Composable
fun RoomMembersDeckDialog(
    room: RoomItem,
    currentUser: UserProfile,
    allUsers: List<UserProfile>,
    onDismiss: () -> Unit,
    onLike: (UserProfile) -> Unit,
    onChat: (UserProfile) -> Unit
) {
    // Real-time active members inside this room
    val activeMembers = remember(allUsers, room.id) {
        allUsers.filter { it.current_room == room.id && it.uid != currentUser.uid }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = BrandDark,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    AsyncImage(
                        model = room.banner_image_url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, BrandDark)
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${room.title} ${room.emoji}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "🟢 ${activeMembers.size + 1} Active in Room",
                                    color = BrandGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Leave Room", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(14.dp)
            ) {
                Text(
                    text = "Active Room Members Deck",
                    color = BrandCoral,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Like to match, or VIP/Platinum members can message instantly.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (activeMembers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "You are currently the only one here! 🙋",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Other members joining ${room.title} will appear here in real time.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeMembers) { member ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandCard),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = member.photo,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${member.name}, ${member.age}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            // Tier Badge
                                            val isVip = member.plan.contains("VIP", ignoreCase = true)
                                            val isPlatinum = member.plan.contains("Platinum", ignoreCase = true)
                                            if (isVip || isPlatinum) {
                                                Text(
                                                    text = if (isVip) "VIP ★" else "PLATINUM",
                                                    color = if (isVip) BrandGold else BrandCyan,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier
                                                        .background(
                                                            (if (isVip) BrandGold else BrandCyan).copy(alpha = 0.15f),
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "📍 ${member.city} • ${member.activeIntent.replace('_', ' ')}",
                                            color = BrandOrange,
                                            fontSize = 11.sp
                                        )
                                        if (member.bio.isNotBlank()) {
                                            Text(
                                                text = member.bio,
                                                color = Color.LightGray,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Action Buttons: Chat (Tier restricted) & Like
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Chat Button with Tier Lock Check
                                        val isChatUnlocked = currentUser.isVipOrPlatinum()
                                        IconButton(
                                            onClick = { onChat(member) },
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    if (isChatUnlocked) BrandCyan.copy(alpha = 0.15f)
                                                    else Color.Gray.copy(alpha = 0.15f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (isChatUnlocked) Icons.Default.Chat else Icons.Default.Lock,
                                                contentDescription = "Chat",
                                                tint = if (isChatUnlocked) BrandCyan else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Real Like Button
                                        IconButton(
                                            onClick = { onLike(member) },
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(BrandCoral, CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = "Like",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DISCOVER SCREEN CONTENT (CARD VIEW)
// -------------------------------------------------------------
@Composable
fun DiscoverScreenContent(
    profiles: List<UserProfile>,
    selectedCategory: String,
    currentUser: UserProfile,
    onSelectCategory: (String) -> Unit,
    onPass: (UserProfile) -> Unit,
    onSuperStar: (UserProfile) -> Unit,
    onDirectMessage: (UserProfile) -> Unit,
    onLike: (UserProfile) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentProfile = profiles.getOrNull(currentIndex)

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Intent Categories Row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CATEGORY_LIST) { cat ->
                val isSelected = selectedCategory == cat.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                            else Brush.linearGradient(listOf(BrandCard, BrandCard))
                        )
                        .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                        .clickable {
                            onSelectCategory(cat.id)
                            currentIndex = 0
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat.label,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentProfile != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
            ) {
                AsyncImage(
                    model = currentProfile.photo,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Bottom Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)),
                                startY = 300f
                            )
                        )
                )

                // Info & 4 Action Buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🍷 ${currentProfile.activeIntent.replace('_', ' ').uppercase()}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(BrandCoral, RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${currentProfile.name}, ${currentProfile.age}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (currentProfile.profession.isNotBlank()) {
                        Text(
                            text = "💼 ${currentProfile.profession} • 🎓 ${currentProfile.education}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = currentProfile.bio,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                    Text(
                        text = "📍 ${currentProfile.city} • ${currentProfile.distanceKm} km away",
                        color = BrandOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Action Buttons: Pass, SuperStar, Direct Message, Like
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Pass (Dislike)
                        IconButton(
                            onClick = {
                                onPass(currentProfile)
                                currentIndex++
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                .border(1.5.dp, Color.Red, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Pass", tint = Color.Red)
                        }

                        // 2. SuperStar
                        IconButton(
                            onClick = {
                                onSuperStar(currentProfile)
                                currentIndex++
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                .border(1.5.dp, BrandGold, CircleShape)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "SuperStar", tint = BrandGold)
                        }

                        // 3. Direct Message (Tier restricted)
                        IconButton(
                            onClick = {
                                onDirectMessage(currentProfile)
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                .border(
                                    1.5.dp,
                                    if (currentUser.isVipOrPlatinum()) BrandCyan else Color.Gray,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (currentUser.isVipOrPlatinum()) Icons.Default.Chat else Icons.Default.Lock,
                                contentDescription = "Direct Message",
                                tint = if (currentUser.isVipOrPlatinum()) BrandCyan else Color.Gray
                            )
                        }

                        // 4. Like
                        IconButton(
                            onClick = {
                                onLike(currentProfile)
                                currentIndex++
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(BrandCoral, BrandOrange)),
                                    CircleShape
                                )
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.White)
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = BrandCoral,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No more profiles to discover!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Try expanding your distance or age filter in Settings, or join an Intent Room.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { currentIndex = 0 },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral)
                    ) {
                        Text("Reset Stack")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// LIKES SCREEN CONTENT
// -------------------------------------------------------------
@Composable
fun LikesScreenContent(
    swipes: List<SwipeRecord>,
    db: FirebaseFirestore,
    onMatchBack: (UserProfile) -> Unit
) {
    val likerProfiles = remember { mutableStateListOf<UserProfile>() }

    LaunchedEffect(swipes) {
        likerProfiles.clear()
        swipes.filter { it.action in listOf("like", "superstar") }.forEach { swipe ->
            db.collection("users").document(swipe.fromUserId).get().addOnSuccessListener { doc ->
                doc.toObject(UserProfile::class.java)?.let { likerProfiles.add(it) }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Who Liked You", color = BrandCoral, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (likerProfiles.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No incoming likes yet. Keep your profile active!", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(likerProfiles) { liker ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BrandCard)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = liker.photo,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${liker.name}, ${liker.age}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Intent: ${liker.activeIntent.replace('_', ' ')}", color = BrandOrange, fontSize = 11.sp)
                                Text("📍 ${liker.city}", color = Color.Gray, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onMatchBack(liker) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCoral)
                            ) {
                                Text("Match Back", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CHATS SCREEN CONTENT WITH INBOX BLOCK / UNBLOCK
// -------------------------------------------------------------
@Composable
fun ChatsScreenContent(
    matches: List<MatchItem>,
    currentUserId: String,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onOpenChat: (UserProfile) -> Unit,
    onToggleBlock: (UserProfile) -> Unit
) {
    val partnerMap = remember { mutableStateMapOf<String, UserProfile>() }

    LaunchedEffect(matches) {
        matches.forEach { match ->
            val pId = match.users.firstOrNull { it != currentUserId }
            if (pId != null && !partnerMap.containsKey(pId)) {
                db.collection("users").document(pId).get().addOnSuccessListener { doc ->
                    doc.toObject(UserProfile::class.java)?.let { partnerMap[pId] = it }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Matches & Inbox", color = BrandCoral, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (matches.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No matches yet. Like back profiles to start chatting!", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(matches) { match ->
                    val pId = match.users.firstOrNull { it != currentUserId }
                    val partner = partnerMap[pId]
                    if (partner != null) {
                        val isBlocked = currentUser.blockedUserIds.contains(partner.uid)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenChat(partner) },
                            colors = CardDefaults.cardColors(containerColor = BrandCard)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = partner.photo,
                                    contentDescription = null,
                                    modifier = Modifier.size(46.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(partner.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (isBlocked) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "[Blocked]",
                                                color = Color.Red,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(match.lastMessage, color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
                                }

                                TextButton(
                                    onClick = { onToggleBlock(partner) }
                                ) {
                                    Text(
                                        text = if (isBlocked) "Unblock" else "Block",
                                        color = if (isBlocked) BrandGreen else Color.Red.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MOMENTS SCREEN WITH UPLOAD, LIKES, AND COMMENTS
// -------------------------------------------------------------
@Composable
fun MomentsScreenContent(
    moments: List<MomentItem>,
    currentUserId: String,
    onPostClick: () -> Unit,
    onLikeMoment: (MomentItem) -> Unit,
    onCommentMoment: (MomentItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Moments Feed", color = BrandCoral, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onPostClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Post Moment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (moments.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No moments posted yet. Be the first to share one!", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(moments) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = item.userPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.size(34.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(item.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(formatRelativeTime(item.timestamp), color = Color.Gray, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.caption, color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (item.mediaUrl.isNotBlank()) {
                                AsyncImage(
                                    model = item.mediaUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isLikedByMe = item.likedByUsers.contains(currentUserId)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onLikeMoment(item) }
                                ) {
                                    Icon(
                                        imageVector = if (isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isLikedByMe) BrandCoral else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${item.likes}", color = Color.White, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onCommentMoment(item) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubble,
                                        contentDescription = "Comment",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${item.commentCount}", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PROFILE SCREEN CONTENT
// -------------------------------------------------------------
@Composable
fun ProfileScreenContent(
    currentUser: UserProfile,
    userMoments: List<MomentItem>,
    db: FirebaseFirestore,
    currentUserId: String,
    onUpdateProfile: (UserProfile) -> Unit,
    onSelectPlan: (SubscriptionPlanItem) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var name by remember(currentUser) { mutableStateOf(currentUser.name) }
    var age by remember(currentUser) { mutableStateOf(currentUser.age.toString()) }
    var city by remember(currentUser) { mutableStateOf(currentUser.city) }
    var hobbies by remember(currentUser) { mutableStateOf(currentUser.hobbies) }
    var bio by remember(currentUser) { mutableStateOf(currentUser.bio) }
    var profession by remember(currentUser) { mutableStateOf(currentUser.profession) }
    var education by remember(currentUser) { mutableStateOf(currentUser.education) }
    var photoUrl by remember(currentUser) { mutableStateOf(currentUser.photo) }
    var gender by remember(currentUser) { mutableStateOf(currentUser.gender) }
    var interestedIn by remember(currentUser) { mutableStateOf(currentUser.interestedIn) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showSubscriptionModal by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingPhoto = true
            coroutineScope.launch {
                // 1. Immediately compress & convert to Cloud Firestore Data URI (Base64 JPEG, ~20KB)
                // This ensures profile photo is stored directly in Cloud Firestore (users/{uid}.photo) and accessible globally
                val firestoreDataUri = ImageUploadManager.convertImageToFirestoreDataUri(context, uri) ?: uri.toString()
                photoUrl = firestoreDataUri
                val updatedProfile = currentUser.copy(uid = currentUserId, photo = firestoreDataUri)
                onUpdateProfile(updatedProfile)

                // Sync directly to Cloud Firestore
                db.collection("users").document(currentUserId)
                    .set(updatedProfile, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile picture saved to Cloud Firestore! 📸☁️", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Saved locally. Syncing to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                // 2. Background upload to Firebase Storage if available (upgrades to CDN URL)
                val cloudUrl = ImageUploadManager.uploadImage(context, uri, "profiles")
                if (!cloudUrl.isNullOrBlank() && cloudUrl.startsWith("http")) {
                    photoUrl = cloudUrl
                    val updatedCloud = currentUser.copy(uid = currentUserId, photo = cloudUrl)
                    onUpdateProfile(updatedCloud)
                    db.collection("users").document(currentUserId).set(updatedCloud, SetOptions.merge())
                }
                isUploadingPhoto = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(2.dp, BrandCoral, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    if (isUploadingPhoto) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = BrandCoral,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    IconButton(
                        onClick = { if (!isUploadingPhoto) imagePickerLauncher.launch("image/*") },
                        enabled = !isUploadingPhoto,
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.BottomEnd)
                            .background(BrandCoral, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Upload Photo",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("$city • $age yrs", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PLAN: ${currentUser.plan}",
                        color = BrandGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                }
            }
        }

        // Editable Form Fields
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Basic Information", color = BrandCoral, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        if (isEditing) {
                            Surface(
                                color = BrandOrange.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, BrandOrange)
                            ) {
                                Text(
                                    "Editing Mode Active",
                                    color = BrandOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name", color = Color.Gray, fontSize = 11.sp) },
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age", color = Color.Gray, fontSize = 11.sp) },
                            colors = outlinedFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City", color = Color.Gray, fontSize = 11.sp) },
                            colors = outlinedFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = profession,
                        onValueChange = { profession = it },
                        label = { Text("Profession", color = Color.Gray, fontSize = 11.sp) },
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = education,
                        onValueChange = { education = it },
                        label = { Text("Education", color = Color.Gray, fontSize = 11.sp) },
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hobbies,
                        onValueChange = { hobbies = it },
                        label = { Text("Interests & Hobbies", color = Color.Gray, fontSize = 11.sp) },
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio", color = Color.Gray, fontSize = 11.sp) },
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Gender Selection Pills
                    Text("Your Gender", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Female 👩", "Male 👨", "Non-binary 🧑").forEach { opt ->
                            val optVal = opt.substringBefore(' ').trim()
                            val isSelected = gender.equals(optVal, ignoreCase = true) || gender.equals(opt, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                        else Brush.linearGradient(listOf(BrandDark, BrandDark))
                                    )
                                    .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                                    .clickable { gender = optVal }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(opt, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Interested In Selection Pills
                    Text("Interested In Meeting", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Women 👩", "Men 👨", "Everyone 🌈").forEach { opt ->
                            val optVal = opt.substringBefore(' ').trim()
                            val isSelected = interestedIn.equals(optVal, ignoreCase = true) || interestedIn.equals(opt, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                        else Brush.linearGradient(listOf(BrandDark, BrandDark))
                                    )
                                    .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                                    .clickable { interestedIn = optVal }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(opt, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Real-Time Status / Feedback Banner
                    if (statusMessage != null) {
                        Surface(
                            color = if (statusMessage!!.startsWith("✅")) BrandGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (statusMessage!!.startsWith("✅")) BrandGreen else Color.Red),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (statusMessage!!.startsWith("✅")) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (statusMessage!!.startsWith("✅")) BrandGreen else Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = statusMessage!!,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Side-by-Side Action Buttons: Edit Profile and Save Profile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit Profile Button
                        OutlinedButton(
                            onClick = {
                                if (isEditing) {
                                    // Reset to current profile and exit edit mode
                                    name = currentUser.name
                                    age = currentUser.age.toString()
                                    city = currentUser.city
                                    profession = currentUser.profession
                                    education = currentUser.education
                                    hobbies = currentUser.hobbies
                                    bio = currentUser.bio
                                    photoUrl = currentUser.photo
                                    gender = currentUser.gender
                                    interestedIn = currentUser.interestedIn
                                    isEditing = false
                                    statusMessage = null
                                } else {
                                    isEditing = true
                                    statusMessage = null
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isEditing) Color.LightGray else BrandCoral
                            ),
                            border = BorderStroke(1.5.dp, if (isEditing) Color.Gray else BrandCoral),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(
                                if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isEditing) Color.LightGray else BrandCoral
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEditing) "Cancel Edit" else "✏️ Edit Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Save Profile Button with Instant Local Feedback & Background Firestore Sync
                        Button(
                            onClick = {
                                val parsedAge = age.toIntOrNull() ?: currentUser.age
                                val rawPhoto = photoUrl.ifEmpty { currentUser.photo }
                                val safePhoto = if (rawPhoto.startsWith("file://")) {
                                    try {
                                        ImageUploadManager.convertImageToFirestoreDataUri(context, Uri.parse(rawPhoto)) ?: rawPhoto
                                    } catch (e: Exception) {
                                        rawPhoto
                                    }
                                } else {
                                    rawPhoto
                                }
                                photoUrl = safePhoto

                                val updated = currentUser.copy(
                                    uid = currentUserId,
                                    name = name.trim().ifEmpty { currentUser.name },
                                    age = parsedAge,
                                    city = city.trim().ifEmpty { currentUser.city },
                                    profession = profession.trim(),
                                    education = education.trim(),
                                    hobbies = hobbies.trim(),
                                    bio = bio.trim(),
                                    photo = safePhoto,
                                    gender = gender,
                                    interestedIn = interestedIn
                                )
                                // Immediate optimistic UI update - avoids long spinning button
                                onUpdateProfile(updated)
                                isEditing = false
                                isSaving = false
                                statusMessage = "✅ Profile updated successfully!"
                                Toast.makeText(context, "Profile saved to Cloud Firestore! ✅", Toast.LENGTH_SHORT).show()

                                // Background cloud sync to Firestore
                                db.collection("users").document(currentUserId)
                                    .set(updated, SetOptions.merge())
                                    .addOnSuccessListener {
                                        statusMessage = "✅ Profile Synced to Firestore in Real-Time!"
                                    }
                                    .addOnFailureListener {
                                        statusMessage = "✅ Profile Saved Locally!"
                                    }
                            },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandCoral,
                                disabledContainerColor = BrandCoral.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("💾 Save Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // MEMBERSHIP SUBSCRIPTION PLAN - SINGLE CONSOLIDATED VIP CARD BOX
        item {
            Column {
                Text(
                    "MEMBERSHIP & VIP CLUB",
                    color = BrandGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { showSubscriptionModal = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCard),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(BrandGold, BrandCoral)))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        BrandGold.copy(alpha = 0.15f),
                                        BrandDark.copy(alpha = 0.90f),
                                        BrandCoral.copy(alpha = 0.15f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(BrandGold.copy(alpha = 0.35f), BrandGold.copy(alpha = 0.1f))
                                        )
                                    )
                                    .border(1.5.dp, BrandGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👑", fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "VIP Membership Center",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = BrandGold.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.6f))
                                    ) {
                                        Text(
                                            currentUser.plan.uppercase(),
                                            color = BrandGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    "Tap to view all subscription tiers, purchase passes & unlock privileges",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Explore 4 Subscription Tiers",
                                        color = BrandGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("›", color = BrandGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // MY POSTED MOMENTS
        item {
            Column {
                Text(
                    "MY POSTED MOMENTS",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (userMoments.isEmpty()) {
                    Text("No moments posted by you yet.", color = Color.Gray, fontSize = 11.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(userMoments) { moment ->
                            AsyncImage(
                                model = moment.mediaUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        // Safety & Account
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Safety & Account", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // Logout Button
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCardSubtle),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", color = Color.White, fontSize = 13.sp)
                    }

                    // Delete Account Button
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = BrandCard,
            title = { Text("Delete Account?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove your profile, matches, and moments from LiveTogather.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showSubscriptionModal) {
        SubscriptionPlansModal(
            currentPlan = currentUser.plan,
            onDismiss = { showSubscriptionModal = false },
            onSelectPlan = { plan ->
                onSelectPlan(plan)
                showSubscriptionModal = false
            }
        )
    }
}

@Composable
fun SubscriptionPlansModal(
    currentPlan: String,
    onDismiss: () -> Unit,
    onSelectPlan: (SubscriptionPlanItem) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = BrandDark,
            border = BorderStroke(1.5.dp, Brush.verticalGradient(listOf(BrandGold, BrandCoral)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BrandGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "VIP Membership Levels",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Select a tier to upgrade & unlock perks",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(BrandCard, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Plans List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(SUBSCRIPTION_PLANS) { plan ->
                        val isSelected = currentPlan.equals(plan.title, ignoreCase = true)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BrandGold.copy(alpha = 0.12f) else BrandCard
                            ),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) BrandGold else BrandBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            plan.title,
                                            color = if (isSelected) BrandGold else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        if (plan.isPopular) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = BrandCoral,
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text(
                                                    "POPULAR 🔥",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            color = BrandGreen.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(1.dp, BrandGreen)
                                        ) {
                                            Text(
                                                "ACTIVE PLAN ✓",
                                                color = BrandGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        plan.price,
                                        color = BrandCoral,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp
                                    )
                                    Text(
                                        plan.duration,
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    plan.tagline,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { onSelectPlan(plan) },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) BrandGold else BrandCoral
                                    )
                                ) {
                                    Text(
                                        if (isSelected) "Active Tier ✓" else "Choose ${plan.title} →",
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionPlanCard(
    plan: SubscriptionPlanItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BrandCard)
            .border(
                1.5.dp,
                if (isSelected) BrandGold else BrandBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    plan.title,
                    color = if (isSelected) BrandGold else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Active",
                        tint = BrandGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    plan.price,
                    color = BrandCoral,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Text(
                    plan.duration,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                plan.tagline,
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }
    }
}

// -------------------------------------------------------------
// SETTINGS DIALOG WITH TOPIC SELECTION
// -------------------------------------------------------------
@Composable
fun SettingsDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var hasBio by remember { mutableStateOf(user.hasBio) }
    var locationName by remember { mutableStateOf(user.locationName) }
    var maxDistanceKm by remember { mutableFloatStateOf(user.maxDistanceKm.toFloat()) }
    var showFurtherAway by remember { mutableStateOf(user.showFurtherAway) }
    var interestedIn by remember { mutableStateOf(user.interestedIn) }
    var ageRangeStart by remember { mutableFloatStateOf(user.minAge.toFloat()) }
    var ageRangeEnd by remember { mutableFloatStateOf(user.maxAge.toFloat()) }
    var showOutsideAge by remember { mutableStateOf(user.showOutsideAge) }
    var visibilityMode by remember { mutableStateOf(user.visibilityMode) }
    var enableDiscovery by remember { mutableStateOf(user.enableDiscovery) }
    var myMove by remember { mutableStateOf(user.myMove) }
    var photoVerifiedChat by remember { mutableStateOf(user.photoVerifiedChat) }

    var interests by remember { mutableStateOf(user.interests.ifBlank { "Travel, Cafe, Night Drives" }) }
    var lookingFor by remember { mutableStateOf(user.lookingFor) }
    var openTo by remember { mutableStateOf(user.openTo) }
    var languages by remember { mutableStateOf(user.languages) }
    var zodiac by remember { mutableStateOf(user.zodiac) }
    var education by remember { mutableStateOf(user.education) }
    var familyPlans by remember { mutableStateOf(user.familyPlans) }
    var communicationStyle by remember { mutableStateOf(user.communicationStyle) }
    var loveStyle by remember { mutableStateOf(user.loveStyle) }
    var pets by remember { mutableStateOf(user.pets) }
    var drinking by remember { mutableStateOf(user.drinking) }
    var smoking by remember { mutableStateOf(user.smoking) }
    var workout by remember { mutableStateOf(user.workout) }
    var socialMedia by remember { mutableStateOf(user.socialMedia) }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isKeyboardOpen
        )
    ) {
        Scaffold(
            containerColor = Color(0xFFF5F5F7),
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                    Text(
                        "Settings",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            val updated = user.copy(
                                hasBio = hasBio,
                                locationName = locationName,
                                maxDistanceKm = maxDistanceKm.toInt(),
                                showFurtherAway = showFurtherAway,
                                interestedIn = interestedIn,
                                minAge = ageRangeStart.toInt(),
                                maxAge = ageRangeEnd.toInt(),
                                showOutsideAge = showOutsideAge,
                                visibilityMode = visibilityMode,
                                enableDiscovery = enableDiscovery,
                                myMove = myMove,
                                photoVerifiedChat = photoVerifiedChat,
                                interests = interests,
                                lookingFor = lookingFor,
                                openTo = openTo,
                                languages = languages,
                                zodiac = zodiac,
                                education = education,
                                familyPlans = familyPlans,
                                communicationStyle = communicationStyle,
                                loveStyle = loveStyle,
                                pets = pets,
                                drinking = drinking,
                                smoking = smoking,
                                workout = workout,
                                socialMedia = socialMedia
                            )
                            onSave(updated)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black, CircleShape)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Has a Bio", color = Color.Gray, fontSize = 15.sp)
                                Switch(checked = hasBio, onCheckedChange = { hasBio = it })
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                            SettingsAttributeRow("Interests", interests, SETTINGS_TOPIC_OPTIONS["Interests"] ?: emptyList()) { interests = it }
                            SettingsAttributeRow("Looking for", lookingFor, SETTINGS_TOPIC_OPTIONS["Looking for"] ?: emptyList()) { lookingFor = it }
                            SettingsAttributeRow("Open to...", openTo, SETTINGS_TOPIC_OPTIONS["Open to..."] ?: emptyList()) { openTo = it }
                            SettingsAttributeRow("Add languages", languages, SETTINGS_TOPIC_OPTIONS["Add languages"] ?: emptyList()) { languages = it }
                            SettingsAttributeRow("Zodiac", zodiac, SETTINGS_TOPIC_OPTIONS["Zodiac"] ?: emptyList()) { zodiac = it }
                            SettingsAttributeRow("Education", education, SETTINGS_TOPIC_OPTIONS["Education"] ?: emptyList()) { education = it }
                            SettingsAttributeRow("Family Plans", familyPlans, SETTINGS_TOPIC_OPTIONS["Family Plans"] ?: emptyList()) { familyPlans = it }
                            SettingsAttributeRow("Communication Style", communicationStyle, SETTINGS_TOPIC_OPTIONS["Communication Style"] ?: emptyList()) { communicationStyle = it }
                            SettingsAttributeRow("Love Style", loveStyle, SETTINGS_TOPIC_OPTIONS["Love Style"] ?: emptyList()) { loveStyle = it }
                            SettingsAttributeRow("Pets", pets, SETTINGS_TOPIC_OPTIONS["Pets"] ?: emptyList()) { pets = it }
                            SettingsAttributeRow("Drinking", drinking, SETTINGS_TOPIC_OPTIONS["Drinking"] ?: emptyList()) { drinking = it }
                            SettingsAttributeRow("Smoking", smoking, SETTINGS_TOPIC_OPTIONS["Smoking"] ?: emptyList()) { smoking = it }
                            SettingsAttributeRow("Workout", workout, SETTINGS_TOPIC_OPTIONS["Workout"] ?: emptyList()) { workout = it }
                            SettingsAttributeRow("Social Media", socialMedia, SETTINGS_TOPIC_OPTIONS["Social Media"] ?: emptyList()) { socialMedia = it }
                        }
                    }
                }

                item {
                    Text("Discovery", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Location", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Change locations to find matches anywhere.", color = Color.Gray, fontSize = 11.sp)
                                }
                                Text(locationName, color = Color.DarkGray, fontSize = 13.sp)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Maximum Distance", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${maxDistanceKm.toInt()} km", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Slider(
                                value = maxDistanceKm,
                                onValueChange = { maxDistanceKm = it },
                                valueRange = 10f..120f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.Red,
                                    inactiveTrackColor = Color.LightGray
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show people further away if I run out of profiles to see.", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Switch(checked = showFurtherAway, onCheckedChange = { showFurtherAway = it })
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Interested In", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Row {
                                    listOf("Women", "Men", "Everyone").forEach { opt ->
                                        Text(
                                            text = opt,
                                            color = if (interestedIn == opt) Color.Red else Color.Gray,
                                            fontWeight = if (interestedIn == opt) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp,
                                            modifier = Modifier
                                                .clickable { interestedIn = opt }
                                                .padding(horizontal = 6.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Age Range", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${ageRangeStart.toInt()} - ${ageRangeEnd.toInt()}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Slider(
                                value = ageRangeEnd,
                                onValueChange = { ageRangeEnd = it },
                                valueRange = 21f..50f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.Red,
                                    inactiveTrackColor = Color.LightGray
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show people slightly out of my preferred range if I run out of profiles to see", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Switch(checked = showOutsideAge, onCheckedChange = { showOutsideAge = it })
                            }
                        }
                    }
                }

                item {
                    Text("Control my visibility", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { visibilityMode = "Standard" },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Standard", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("You will be discoverable in the card stack.", color = Color.Gray, fontSize = 11.sp)
                                }
                                if (visibilityMode == "Standard") {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Red)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { visibilityMode = "Incognito" },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Incognito", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("PLUS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.Red, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                    Text("You will be discoverable only by people you Like.", color = Color.Gray, fontSize = 11.sp)
                                }
                                if (visibilityMode == "Incognito") {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Enable Discovery", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Enable Discovery", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Switch(checked = enableDiscovery, onCheckedChange = { enableDiscovery = it })
                            }
                            Text(
                                "When turned off, your profile will be hidden from the card stack and Discovery will be disabled. People you have already Liked may still see and match with you.",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Control who messages you", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("My Move", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Message your matches first to initiate conversation. They won't be able to message you until you do.", color = Color.Gray, fontSize = 11.sp)
                                }
                                Switch(checked = myMove, onCheckedChange = { myMove = it })
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Photo Verified Chat", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Photo Verified members can enable this to only receive messages from other Photo Verified profiles.", color = Color.Gray, fontSize = 11.sp)
                                }
                                Switch(checked = photoVerifiedChat, onCheckedChange = { photoVerifiedChat = it })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SETTINGS ATTRIBUTE ROW WITH SELECTION DIALOG
// -------------------------------------------------------------
@Composable
fun SettingsAttributeRow(
    title: String,
    currentValue: String,
    options: List<String> = emptyList(),
    onValueChange: (String) -> Unit
) {
    var showSelectionDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showSelectionDialog = true }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.Black, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currentValue.ifBlank { "Select" },
                color = Color.Gray,
                fontSize = 13.sp,
                maxLines = 1
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }

    if (showSelectionDialog) {
        SettingsTopicSelectionDialog(
            title = title,
            currentValue = currentValue,
            options = options.ifEmpty { listOf(currentValue) },
            onDismiss = { showSelectionDialog = false },
            onSelect = { selected ->
                onValueChange(selected)
                showSelectionDialog = false
            }
        )
    }
}

@Composable
fun SettingsTopicSelectionDialog(
    title: String,
    currentValue: String,
    options: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var customText by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select $title",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(options) { option ->
                        val isSelected = option.equals(currentValue, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFFFFECEE) else Color.Transparent)
                                .clickable { onSelect(option) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                color = if (isSelected) Color.Red else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

                if (showCustomInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            placeholder = { Text("Enter custom $title...", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customText.isNotBlank()) {
                                    onSelect(customText.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Save")
                        }
                    }
                } else {
                    TextButton(
                        onClick = { showCustomInput = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Type Custom Option", color = Color.Red, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// NOTIFICATIONS DIALOG
// -------------------------------------------------------------
@Composable
fun NotificationsDialog(
    notifications: List<NotificationItem>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandCoral)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark all read", color = BrandOrange, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No notifications yet", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.isRead) BrandDark else BrandCardSubtle
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = notif.senderPhoto.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80" },
                                        contentDescription = null,
                                        modifier = Modifier.size(38.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(notif.title, color = BrandCoral, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(notif.message, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// POST MOMENT DIALOG
// -------------------------------------------------------------
@Composable
fun PostMomentDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onPost: (String, String) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=600&q=80") }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            coroutineScope.launch {
                val uploadedUrl = ImageUploadManager.uploadImage(context, uri, "moments")
                if (uploadedUrl != null) {
                    mediaUrl = uploadedUrl
                    Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
                isUploading = false
            }
        }
    }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isKeyboardOpen && !isUploading
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Share a Moment", color = BrandCoral, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("What's on your mind? (Cafe, Party, Trip...)", color = Color.Gray, fontSize = 12.sp) },
                    colors = outlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                        .clickable(enabled = !isUploading) { mediaPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = if (isUploading) 0.65f else 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = BrandCoral,
                                    modifier = Modifier.size(30.dp),
                                    strokeWidth = 3.dp
                                )
                                Text("Uploading to cloud...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Change Image / Pick File", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismiss, enabled = !isUploading, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            if (caption.isNotBlank() && !isUploading) {
                                onPost(mediaUrl, caption.trim())
                            }
                        },
                        enabled = caption.isNotBlank() && !isUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isUploading) "Uploading..." else "Post Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIRECT MESSAGE DIALOG
// -------------------------------------------------------------
@Composable
fun DirectMessageDialog(
    targetUser: UserProfile,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isKeyboardOpen
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = targetUser.photo,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Message ${targetUser.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Send a direct note", color = BrandOrange, fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Say something interesting...", color = Color.Gray, fontSize = 12.sp) },
                    colors = outlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSend(messageText.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Send", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMMENTS DIALOG FOR MOMENTS
// -------------------------------------------------------------
@Composable
fun CommentsDialog(
    moment: MomentItem,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit
) {
    val comments = remember { mutableStateListOf<CommentItem>() }
    var newCommentText by remember { mutableStateOf("") }

    LaunchedEffect(moment.id) {
        db.collection("moments").document(moment.id).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(CommentItem::class.java) }
                    comments.clear()
                    comments.addAll(list)
                }
            }
    }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isKeyboardOpen
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Comments (${comments.size})", color = BrandCoral, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (comments.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No comments yet. Write the first one!", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(comments) { comment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrandCardSubtle, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = comment.userPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(comment.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(comment.text, color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Write a comment...", color = Color.Gray, fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BrandDark,
                            unfocusedContainerColor = BrandDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                val cText = newCommentText.trim()
                                newCommentText = ""
                                val newComment = CommentItem(
                                    id = UUID.randomUUID().toString(),
                                    momentId = moment.id,
                                    userId = currentUser.uid,
                                    userName = currentUser.name,
                                    userPhoto = currentUser.photo,
                                    text = cText
                                )
                                db.collection("moments").document(moment.id).collection("comments").document(newComment.id).set(newComment)
                                db.collection("moments").document(moment.id).update("commentCount", moment.commentCount + 1)
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post", tint = BrandCoral)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER FUNCTIONS, PERSISTENCE & FIRESTORE ACTIONS
// -------------------------------------------------------------
fun saveProfileToPrefs(prefs: android.content.SharedPreferences, user: UserProfile) {
    prefs.edit()
        .putString("user_uid", user.uid)
        .putString("profile_device_id", user.deviceId)
        .putString("profile_name", user.name)
        .putInt("profile_age", user.age)
        .putString("profile_gender", user.gender)
        .putString("profile_city", user.city)
        .putString("profile_photo", user.photo)
        .putString("profile_active_intent", user.activeIntent)
        .putString("profile_bio", user.bio)
        .putString("profile_hobbies", user.hobbies)
        .putString("profile_interests", user.interests)
        .putString("profile_profession", user.profession)
        .putString("profile_education", user.education)
        .putString("profile_plan", user.plan)
        .putString("profile_location", user.locationName)
        .putInt("profile_max_distance", user.maxDistanceKm)
        .putBoolean("profile_show_further", user.showFurtherAway)
        .putString("profile_interested_in", user.interestedIn)
        .putInt("profile_min_age", user.minAge)
        .putInt("profile_max_age", user.maxAge)
        .putString("profile_current_room", user.current_room)
        .apply()
}

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = BrandDark,
    unfocusedContainerColor = BrandDark,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = BrandCoral,
    unfocusedBorderColor = BrandBorder
)

fun recordSwipe(db: FirebaseFirestore, fromUserId: String, toUserId: String, action: String) {
    val swipe = SwipeRecord(
        fromUserId = fromUserId,
        toUserId = toUserId,
        action = action,
        timestamp = System.currentTimeMillis()
    )
    val swipeId = "${fromUserId}_${toUserId}"
    db.collection("swipes").document(swipeId).set(swipe)
}

fun checkAndHandleMutualMatch(
    db: FirebaseFirestore,
    currentUser: UserProfile,
    partner: UserProfile,
    onMatch: () -> Unit
) {
    val reverseSwipeId = "${partner.uid}_${currentUser.uid}"
    db.collection("swipes").document(reverseSwipeId).get().addOnSuccessListener { snapshot ->
        val action = snapshot.getString("action")
        if (action == "like" || action == "superstar") {
            val matchId = listOf(currentUser.uid, partner.uid).sorted().joinToString("_")
            val match = MatchItem(
                matchId = matchId,
                users = listOf(currentUser.uid, partner.uid),
                matchedIntent = currentUser.activeIntent,
                lastMessage = "It's a Match! Say hello."
            )
            db.collection("matches").document(matchId).set(match)

            sendNotification(
                db = db,
                targetUserId = partner.uid,
                sender = currentUser,
                type = "match",
                title = "🎉 New Match!",
                message = "You and ${currentUser.name} matched!"
            )
            onMatch()
        }
    }
}

fun sendNotification(
    db: FirebaseFirestore,
    targetUserId: String,
    sender: UserProfile,
    type: String,
    title: String,
    message: String
) {
    val notif = NotificationItem(
        id = UUID.randomUUID().toString(),
        targetUserId = targetUserId,
        senderId = sender.uid,
        senderName = sender.name,
        senderPhoto = sender.photo,
        type = type,
        title = title,
        message = message,
        timestamp = System.currentTimeMillis(),
        isRead = false
    )
    db.collection("notifications").document(notif.id).set(notif)
}

fun toggleMomentLike(db: FirebaseFirestore, moment: MomentItem, currentUserId: String) {
    val isLiked = moment.likedByUsers.contains(currentUserId)
    val updatedList = if (isLiked) moment.likedByUsers - currentUserId else moment.likedByUsers + currentUserId
    val newLikesCount = updatedList.size
    db.collection("moments").document(moment.id).update(
        mapOf(
            "likedByUsers" to updatedList,
            "likes" to newLikesCount
        )
    )
}

fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return "Just now"
    val diff = System.currentTimeMillis() - timestamp
    if (diff < 0L) return "Just now"
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    return when {
        seconds < 45 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        weeks < 4 -> "${weeks}w ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

fun seedInitialCommunityProfiles(db: FirebaseFirestore) {
    // Disabled: only real registered users are used in Discover
}

fun seedInitialMoments(db: FirebaseFirestore) {
    // Disabled: only real user moments are used in Moments feed
}