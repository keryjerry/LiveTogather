@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.devil.livetogather

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import android.view.WindowManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var initialDeepLinkRoomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialDeepLinkRoomId = extractRoomId(intent)
        val imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                coil.memory.MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .components {
                add(DataUriFetcher.Factory(this@MainActivity))
                add(DataUriFetcher.StringFactory(this@MainActivity))
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)
        setContent {
            LiveTogatherApp(initialRoomId = initialDeepLinkRoomId)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val roomId = extractRoomId(intent)
        if (!roomId.isNullOrBlank()) {
            pendingDeepLinkRoomId.value = roomId
        }
    }

    private fun extractRoomId(intent: Intent?): String? {
        if (intent == null) return null
        return intent.getStringExtra("ROOM_ID")
            ?: intent.getStringExtra("extra_room_id")
            ?: intent.getStringExtra("roomId")
            ?: intent.data?.getQueryParameter("roomId")
    }

    companion object {
        val pendingDeepLinkRoomId = mutableStateOf<String?>(null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTogatherApp(initialRoomId: String? = null) {
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
        val savedUserId = prefs.getString("profile_user_id", "") ?: ""
        val initialUserId = if (savedUserId.isNotBlank() && savedUserId.length == 8 && savedUserId.all { it.isDigit() }) {
            savedUserId
        } else {
            (10000000 + kotlin.math.abs(currentUserId.hashCode()) % 90000000).toString()
        }
        mutableStateOf(
            UserProfile(
                uid = currentUserId,
                userId = initialUserId,
                email = auth.currentUser?.email?.takeIf { it.isNotBlank() } ?: prefs.getString("profile_email", "") ?: "",
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
                plan = prefs.getString("profile_plan", "Free") ?: "Free",
                locationName = prefs.getString("profile_location", "New Town, Kolkata") ?: "New Town, Kolkata",
                latitude = prefs.getString("profile_latitude", "22.5726")?.toDoubleOrNull() ?: 22.5726,
                longitude = prefs.getString("profile_longitude", "88.3639")?.toDoubleOrNull() ?: 88.3639,
                isLocationSet = prefs.getBoolean("profile_is_location_set", false),
                locationPrivacy = prefs.getString("profile_location_privacy", "public") ?: "public",
                maxDistanceKm = prefs.getInt("profile_max_distance", 53),
                showFurtherAway = prefs.getBoolean("profile_show_further", true),
                interestedIn = prefs.getString("profile_interested_in", "Women") ?: "Women",
                minAge = prefs.getInt("profile_min_age", 21),
                maxAge = prefs.getInt("profile_max_age", 29),
                current_room = prefs.getString("profile_current_room", "") ?: "",
                dailySwipeCount = prefs.getInt("profile_daily_swipe_count", 0),
                lastSwipeDate = prefs.getString("profile_last_swipe_date", "") ?: "",
                hostTrialStartDate = prefs.getLong("profile_host_trial_start", 0L),
                activeFrame = prefs.getString("profile_active_frame", "") ?: "",
                ownedFrames = prefs.getStringSet("profile_owned_frames", emptySet())?.toList() ?: emptyList(),
                diamonds = prefs.getLong("profile_diamonds", 0L),
                coins = prefs.getLong("profile_coins", prefs.getLong("profile_diamonds", 0L)),
                vipTrialStartDate = prefs.getLong("profile_vip_trial_start", 0L),
                vipTrialExpiryDate = prefs.getLong("profile_vip_trial_expiry", 0L),
                hasClaimedWelcomeBonus = prefs.getBoolean("profile_has_claimed_welcome_bonus", false),
                signupIpAddress = prefs.getString("profile_signup_ip", "") ?: "",
                isAdmin = isUserWhitelistedAdmin(auth.currentUser?.email ?: prefs.getString("profile_email", "") ?: ""),
                wealthLevel = prefs.getInt("profile_wealth_level", 1),
                wealthXp = prefs.getLong("profile_wealth_xp", 0L),
                charmLevel = prefs.getInt("profile_charm_level", 1),
                charmXp = prefs.getLong("profile_charm_xp", 0L),
                pkLevel = prefs.getInt("profile_pk_level", 1),
                pkWins = prefs.getInt("profile_pk_wins", 0),
                pkScore = prefs.getLong("profile_pk_score", 0L),
                emailPrivacy = prefs.getString("profile_email_privacy", "only_me") ?: "only_me",
                hasPasswordSet = prefs.getBoolean("profile_has_password_set", false)
            )
        )
    }

    // Real-time synchronization of Admin Role, 8-digit User ID, Wallet Coins/Diamonds, and Wealth/Charm/PK Levels
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            db.collection("users").document(currentUserId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val currentAuthEmail = auth.currentUser?.email?.trim() ?: currentUser.email.trim()
                        val isWhitelisted = isUserWhitelistedAdmin(currentAuthEmail, currentUserId)
                        val adminFlag = isWhitelisted
                        val updatedCoins = snapshot.getLong("coins") ?: snapshot.getLong("diamonds") ?: currentUser.coins
                        val updatedDiamonds = snapshot.getLong("diamonds") ?: currentUser.diamonds
                        val firestoreUserId = snapshot.getString("userId") ?: ""
                        val finalUserId = if (firestoreUserId.isNotBlank() && firestoreUserId.length == 8 && firestoreUserId.all { it.isDigit() }) {
                            firestoreUserId
                        } else currentUser.userId

                        val updatedWealthXp = snapshot.getLong("wealthXp") ?: currentUser.wealthXp
                        val updatedWealthLvl = snapshot.getLong("wealthLevel")?.toInt() ?: LevelBadgeSystem.calculateLevelFromXp(updatedWealthXp)
                        val updatedCharmXp = snapshot.getLong("charmXp") ?: currentUser.charmXp
                        val updatedCharmLvl = snapshot.getLong("charmLevel")?.toInt() ?: LevelBadgeSystem.calculateLevelFromXp(updatedCharmXp)
                        val updatedPkWins = (snapshot.getLong("pkWins") ?: currentUser.pkWins.toLong()).toInt()
                        val updatedPkScore = snapshot.getLong("pkScore") ?: currentUser.pkScore
                        val rawPkLvl = snapshot.getLong("pkLevel")?.toInt()
                        val updatedPkLvl = rawPkLvl?.takeIf { it > 1 } ?: LevelBadgeSystem.calculatePkLevel(updatedPkWins)

                        val updatedEmail = auth.currentUser?.email?.takeIf { it.isNotBlank() } ?: snapshot.getString("email") ?: currentUser.email
                        val updatedEmailPrivacy = snapshot.getString("emailPrivacy") ?: currentUser.emailPrivacy
                        val updatedHasPasswordSet = snapshot.getBoolean("hasPasswordSet") ?: currentUser.hasPasswordSet
                        val updatedActiveFrame = snapshot.getString("activeFrame") ?: currentUser.activeFrame

                        if (isWhitelisted && snapshot.getBoolean("isAdmin") != true) {
                            db.collection("users").document(currentUserId).update("isAdmin", true)
                        } else if (!isWhitelisted && snapshot.getBoolean("isAdmin") == true) {
                            db.collection("users").document(currentUserId).update("isAdmin", false)
                        }

                        if (currentUser.isAdmin != adminFlag || currentUser.coins != updatedCoins || currentUser.diamonds != updatedDiamonds ||
                            currentUser.userId != finalUserId || currentUser.wealthLevel != updatedWealthLvl || currentUser.wealthXp != updatedWealthXp ||
                            currentUser.charmLevel != updatedCharmLvl || currentUser.charmXp != updatedCharmXp ||
                            currentUser.pkLevel != updatedPkLvl || currentUser.pkWins != updatedPkWins || currentUser.pkScore != updatedPkScore ||
                            currentUser.email != updatedEmail || currentUser.emailPrivacy != updatedEmailPrivacy || currentUser.hasPasswordSet != updatedHasPasswordSet || currentUser.activeFrame != updatedActiveFrame) {
                            currentUser = currentUser.copy(
                                isAdmin = adminFlag,
                                coins = updatedCoins,
                                diamonds = updatedDiamonds,
                                userId = finalUserId,
                                wealthLevel = updatedWealthLvl,
                                wealthXp = updatedWealthXp,
                                charmLevel = updatedCharmLvl,
                                charmXp = updatedCharmXp,
                                pkLevel = updatedPkLvl,
                                pkWins = updatedPkWins,
                                pkScore = updatedPkScore,
                                email = updatedEmail,
                                emailPrivacy = updatedEmailPrivacy,
                                hasPasswordSet = updatedHasPasswordSet,
                                activeFrame = updatedActiveFrame
                            )
                            prefs.edit()
                                .putString("profile_active_frame", updatedActiveFrame)
                                .putBoolean("profile_is_admin", adminFlag)
                                .putLong("profile_coins", updatedCoins)
                                .putLong("profile_diamonds", updatedDiamonds)
                                .putString("profile_user_id", finalUserId)
                                .putInt("profile_wealth_level", updatedWealthLvl)
                                .putLong("profile_wealth_xp", updatedWealthXp)
                                .putInt("profile_charm_level", updatedCharmLvl)
                                .putLong("profile_charm_xp", updatedCharmXp)
                                .putInt("profile_pk_level", updatedPkLvl)
                                .putInt("profile_pk_wins", updatedPkWins)
                                .putLong("profile_pk_score", updatedPkScore)
                                .putString("profile_email", updatedEmail)
                                .putString("profile_email_privacy", updatedEmailPrivacy)
                                .putBoolean("profile_has_password_set", updatedHasPasswordSet)
                                .apply()
                        }
                    }
                }
        }
    }

    // Auto-check and expire VIP 1 7-Day trial if expired
    LaunchedEffect(Unit) {
        val verifiedProfile = WelcomeBonusManager.checkAndExpireVipTrialIfNeeded(context, currentUser)
        if (verifiedProfile != currentUser) {
            currentUser = verifiedProfile
        }
    }

    // First-Time Sign Up Welcome Bonus & 7-Day VIP 1 Dialog State
    var showWelcomeBonusDialog by remember {
        mutableStateOf(prefs.getBoolean(WelcomeBonusManager.PREF_SHOW_WELCOME_DIALOG, false))
    }

    // Auto-generate unique 8-digit User ID if missing or non-8-digit and persist to Firestore + Prefs
    LaunchedEffect(currentUser.userId, currentUserId) {
        if (currentUserId.isNotBlank()) {
            val validUserId = if (currentUser.userId.isNotBlank() && currentUser.userId.length == 8 && currentUser.userId.all { it.isDigit() }) {
                currentUser.userId
            } else {
                (10000000 + kotlin.math.abs(currentUserId.hashCode()) % 90000000).toString()
            }
            if (currentUser.userId != validUserId) {
                currentUser = currentUser.copy(userId = validUserId)
                saveProfileToPrefs(prefs, currentUser)
            }
            db.collection("users").document(currentUserId).update("userId", validUserId)
        }
    }

    // In-App Auto-Update System
    var availableAppUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var updateDownloadProgress by remember { mutableFloatStateOf(-1f) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AppUpdateManager.checkForUpdate(
            context = context,
            onUpdateAvailable = { updateInfo ->
                availableAppUpdate = updateInfo
            }
        )
    }

    // Top Search Bar State (Dual-Mode: Room vs User)
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf("room") } // "room" or "user"
    val searchRoomResults = remember { mutableStateListOf<RoomItem>() }
    val searchUserResults = remember { mutableStateListOf<UserProfile>() }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery, searchMode) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            searchRoomResults.clear()
            searchUserResults.clear()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        if (searchMode == "room") {
            db.collection("rooms").whereEqualTo("roomId", q).get()
                .addOnSuccessListener { exactDocs ->
                    if (exactDocs != null && !exactDocs.isEmpty) {
                        val list = exactDocs.documents.mapNotNull { parseRoomFromDoc(it) }
                        searchRoomResults.clear()
                        searchRoomResults.addAll(list)
                        isSearching = false
                    } else {
                        db.collection("rooms").limit(60).get()
                            .addOnSuccessListener { snapshot ->
                                val list = mutableListOf<RoomItem>()
                                for (doc in snapshot.documents) {
                                    val r = parseRoomFromDoc(doc)
                                    if (r.id.contains(q, ignoreCase = true) ||
                                        r.roomId.contains(q, ignoreCase = true) ||
                                        r.title.contains(q, ignoreCase = true) ||
                                        r.roomName.contains(q, ignoreCase = true) ||
                                        r.hostName.contains(q, ignoreCase = true)
                                    ) {
                                        list.add(r)
                                    }
                                }
                                searchRoomResults.clear()
                                searchRoomResults.addAll(list)
                                isSearching = false
                            }
                            .addOnFailureListener { isSearching = false }
                    }
                }
                .addOnFailureListener {
                    db.collection("rooms").limit(60).get()
                        .addOnSuccessListener { snapshot ->
                            val list = mutableListOf<RoomItem>()
                            for (doc in snapshot.documents) {
                                val r = parseRoomFromDoc(doc)
                                if (r.id.contains(q, ignoreCase = true) ||
                                    r.roomId.contains(q, ignoreCase = true) ||
                                    r.title.contains(q, ignoreCase = true) ||
                                    r.roomName.contains(q, ignoreCase = true) ||
                                    r.hostName.contains(q, ignoreCase = true)
                                ) {
                                    list.add(r)
                                }
                            }
                            searchRoomResults.clear()
                            searchRoomResults.addAll(list)
                            isSearching = false
                        }
                        .addOnFailureListener { isSearching = false }
                }
        } else {
            db.collection("users").whereEqualTo("userId", q).get()
                .addOnSuccessListener { exactDocs ->
                    if (exactDocs != null && !exactDocs.isEmpty) {
                        val list = exactDocs.documents.mapNotNull { parseUserProfileFromDoc(it) }
                        searchUserResults.clear()
                        searchUserResults.addAll(list)
                        isSearching = false
                    } else {
                        db.collection("users").limit(60).get()
                            .addOnSuccessListener { snapshot ->
                                val list = mutableListOf<UserProfile>()
                                for (doc in snapshot.documents) {
                                    val u = parseUserProfileFromDoc(doc)
                                    if (u.uid.contains(q, ignoreCase = true) ||
                                        u.userId.contains(q, ignoreCase = true) ||
                                        u.name.contains(q, ignoreCase = true) ||
                                        u.city.contains(q, ignoreCase = true)
                                    ) {
                                        list.add(u)
                                    }
                                }
                                searchUserResults.clear()
                                searchUserResults.addAll(list)
                                isSearching = false
                            }
                            .addOnFailureListener { isSearching = false }
                    }
                }
                .addOnFailureListener {
                    db.collection("users").limit(60).get()
                        .addOnSuccessListener { snapshot ->
                            val list = mutableListOf<UserProfile>()
                            for (doc in snapshot.documents) {
                                val u = parseUserProfileFromDoc(doc)
                                if (u.uid.contains(q, ignoreCase = true) ||
                                    u.userId.contains(q, ignoreCase = true) ||
                                    u.name.contains(q, ignoreCase = true) ||
                                    u.city.contains(q, ignoreCase = true)
                                ) {
                                    list.add(u)
                                }
                            }
                            searchUserResults.clear()
                            searchUserResults.addAll(list)
                            isSearching = false
                        }
                        .addOnFailureListener { isSearching = false }
                }
        }
    }

    var currentScreen by remember { mutableStateOf("discover") }
    var discoverTab by remember { mutableStateOf("cards") } // "cards" or "rooms"
    var selectedCategory by remember { mutableStateOf("all") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLocationSetupDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    // Background GPS Auto-Detect Engine (silent background update like Tinder without blocking UI or dialog popups)
    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            silentlyUpdateGpsLocation(context, currentUserId, currentUser, db, prefs) { updated ->
                currentUser = updated
            }
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) {
                silentlyUpdateGpsLocation(context, currentUserId, currentUser, db, prefs) { updated ->
                    currentUser = updated
                }
            } else {
                backgroundLocationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showAdminDashboardModal by remember { mutableStateOf(false) }
    var adminDashboardInitialTab by remember { mutableIntStateOf(0) }
    var showPostMomentDialog by remember { mutableStateOf(false) }
    var momentToEdit by remember { mutableStateOf<MomentItem?>(null) }
    var momentToDelete by remember { mutableStateOf<MomentItem?>(null) }
    var showDirectMessageDialog by remember { mutableStateOf<UserProfile?>(null) }
    var showCommentsMoment by remember { mutableStateOf<MomentItem?>(null) }
    var activeRoomDeck by remember { mutableStateOf<RoomItem?>(null) }
    var activeAudioRoom by remember { mutableStateOf<RoomItem?>(null) }
    var isAudioRoomMinimized by remember { mutableStateOf(false) }

    LaunchedEffect(activeAudioRoom?.id) {
        if (activeAudioRoom != null) {
            isAudioRoomMinimized = false
        }
    }

    // Automatic Live Room Deep-Linking from FCM Notification Intent
    val pendingDeepLink = MainActivity.pendingDeepLinkRoomId.value ?: initialRoomId
    LaunchedEffect(pendingDeepLink) {
        if (!pendingDeepLink.isNullOrBlank()) {
            val targetKey = pendingDeepLink.trim()
            db.collection("rooms").document(targetKey).get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val r = parseRoomFromDoc(doc)
                        activeAudioRoom = r
                        MainActivity.pendingDeepLinkRoomId.value = null
                    } else {
                        db.collection("rooms").whereEqualTo("roomId", targetKey).limit(1).get()
                            .addOnSuccessListener { snap ->
                                if (snap != null && !snap.isEmpty) {
                                    val r = parseRoomFromDoc(snap.documents[0])
                                    activeAudioRoom = r
                                }
                                MainActivity.pendingDeepLinkRoomId.value = null
                            }
                            .addOnFailureListener {
                                MainActivity.pendingDeepLinkRoomId.value = null
                            }
                    }
                }
                .addOnFailureListener {
                    MainActivity.pendingDeepLinkRoomId.value = null
                }
        }
    }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var createRoomInitialCategory by remember { mutableStateOf("free_tonight") }
    var activeCategoryHub by remember { mutableStateOf<IntentCategory?>(null) }
    var matchedPartner by remember { mutableStateOf<UserProfile?>(null) }
    var activeChatPartner by remember { mutableStateOf<UserProfile?>(null) }
    var showMainSubscriptionModal by remember { mutableStateOf(false) }
    var subscriptionPaywallNotice by remember { mutableStateOf<String?>(null) }
    var showHostTrialOfferDialog by remember { mutableStateOf(false) }

    // Real-time Lists from Firestore
    val discoverProfiles = remember { mutableStateListOf<UserProfile>() }
    val incomingSwipes = remember { mutableStateListOf<SwipeRecord>() }
    val matches = remember { mutableStateListOf<MatchItem>() }
    val moments = remember { mutableStateListOf<MomentItem>() }
    val notifications = remember { mutableStateListOf<NotificationItem>() }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val roomsList = remember { mutableStateListOf<RoomItem>() }

    // Real-Time Cloud Firestore Refresh Engine (Zero Dummy Data, Direct Source.SERVER)
    val refreshEngine = remember { FirestoreRefreshEngine(context, db, prefs) }
    val coroutineScope = rememberCoroutineScope()
    val triggerRefresh: () -> Unit = {
        coroutineScope.launch {
            refreshEngine.refreshAll(
                currentUserId = currentUserId,
                deviceId = deviceId,
                currentUser = currentUser,
                onUpdateUser = { updated -> currentUser = updated },
                discoverProfiles = discoverProfiles,
                roomsList = roomsList,
                moments = moments,
                notifications = notifications,
                incomingSwipes = incomingSwipes,
                matches = matches
            )
        }
    }

    // --- Firebase Authentication & Firestore Setup ---
    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            Log.d("Auth", "No active user session in MainActivity, redirecting to LoginActivity")
            val loginIntent = Intent(context, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(loginIntent)
            return@LaunchedEffect
        } else {
            val authUid = auth.currentUser!!.uid
            currentUserId = authUid
            prefs.edit().putString("user_uid", authUid).apply()
            currentUser = currentUser.copy(uid = authUid)
        }

        // Initialize Moments Feed from local cache or seed
        val cachedMoments = loadMomentsFromPrefs(prefs)
        if (cachedMoments.isNotEmpty()) {
            moments.clear()
            moments.addAll(cachedMoments)
        } else if (moments.isEmpty()) {
            moments.addAll(DEFAULT_MOMENTS_SEED)
            saveMomentsToPrefs(prefs, DEFAULT_MOMENTS_SEED)
        }


        // Initialize Rooms & Real-Time Firestore Snapshot Listener (Paginated limit for instant app launch)
        roomsList.clear()

        db.collection("rooms").limit(50).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("Firestore", "Rooms snapshot error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fetchedRooms = snapshot.documents.mapNotNull { doc ->
                    val r = parseRoomFromDoc(doc)
                    if (r != null) {
                        val docRoomId = doc.getString("roomId") ?: ""
                        if (docRoomId.isBlank() || docRoomId.length != 8 || !docRoomId.all { it.isDigit() }) {
                            doc.reference.update("roomId", r.roomId)
                        }
                    }
                    r
                }
                roomsList.clear()
                roomsList.addAll(fetchedRooms)
                Log.d("Firestore", "Loaded ${fetchedRooms.size} real-time rooms from Firestore")
            }
        }
    }

    // --- Real-Time Firestore Listeners ---
    LaunchedEffect(currentUserId, currentUser.isAdmin) {
        // 1. Sync Current User Profile
        val userDoc = db.collection("users").document(currentUserId)
        userDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("Firestore", "User listener error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val fresh = parseUserProfileFromDoc(snapshot)
                currentUser = fresh.copy(deviceId = deviceId)
                // Cache to SharedPreferences
                saveProfileToPrefs(prefs, currentUser)
                val currentDocUserId = snapshot.getString("userId") ?: ""
                if (currentDocUserId.isBlank() || currentDocUserId.length != 8 || !currentDocUserId.all { it.isDigit() }) {
                    userDoc.update("userId", fresh.userId)
                }
                val authEmail = auth.currentUser?.email ?: ""
                if (authEmail.isNotBlank() && (snapshot.getString("email").isNullOrBlank())) {
                    userDoc.update("email", authEmail)
                }
            } else {
                // If profile doesn't exist for currentUserId yet, check if there's a profile for this deviceId
                // (e.g. from prior installation before reinstall)
                if (deviceId.isNotEmpty()) {
                    db.collection("users").whereEqualTo("deviceId", deviceId).limit(1).get()
                        .addOnSuccessListener { prevDocs ->
                            if (prevDocs != null && !prevDocs.isEmpty) {
                                val prevProfile = parseUserProfileFromDoc(prevDocs.documents[0])
                                val restored = prevProfile.copy(uid = currentUserId, deviceId = deviceId)
                                currentUser = restored
                                saveProfileToPrefs(prefs, restored)
                                userDoc.set(restored, SetOptions.merge())
                                Log.d("Firestore", "Restored existing profile from previous install for device $deviceId")
                                return@addOnSuccessListener
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

        // 2. Discover Profiles Listener (Real-Time Real Users - Paginated for instant app opening)
        db.collection("users").limit(50).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("Firestore", "Discover profiles error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { parseUserProfileFromDoc(it) }
                    .filter { it.uid.isNotBlank() && it.uid != currentUserId && !currentUser.blockedUserIds.contains(it.uid) }
                discoverProfiles.clear()
                discoverProfiles.addAll(list)
            }
        }

        // 3. Notifications Listener (Real-Time Bell & Status Bar)
        val notifTargets = if (currentUser.isAdmin) listOf(currentUserId, "admin") else listOf(currentUserId)
        db.collection("notifications")
            .whereIn("targetUserId", notifTargets)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("Firestore", "Notifications listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val rawItems = snapshot.documents.mapNotNull { it.toObject(NotificationItem::class.java) }
                        .sortedByDescending { it.timestamp }

                    val items = rawItems.filter { notif ->
                        val isSelf = notif.senderId == currentUserId
                        val isAdminType = notif.type.contains("report") || notif.type.contains("payment") || notif.type.contains("utr") || notif.type.contains("pay_req")
                        if (!currentUser.isAdmin && isAdminType) {
                            false // Regular user should never see admin reports/payments in their bell
                        } else if (isSelf && isAdminType) {
                            false // Submitter should never see their own submission in the notification bell
                        } else {
                            true
                        }
                    }

                    for (dc in snapshot.documentChanges) {
                        if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val notif = dc.document.toObject(NotificationItem::class.java)
                            val isSelf = notif.senderId == currentUserId
                            val isAdminType = notif.type.contains("report") || notif.type.contains("payment") || notif.type.contains("utr") || notif.type.contains("pay_req")
                            val shouldShowSystem = if (isAdminType) {
                                currentUser.isAdmin && !isSelf
                            } else {
                                !isSelf
                            }
                            if (shouldShowSystem && !notif.isRead && (System.currentTimeMillis() - notif.timestamp) < 60_000L) {
                                AppNotificationHelper.showSystemNotification(
                                    context = context,
                                    title = notif.title.ifBlank { "LiveTogether Alert" },
                                    message = notif.message,
                                    roomId = notif.roomId,
                                    isLiveAlert = notif.type.contains("LIVE", ignoreCase = true) || notif.type.contains("host_live", ignoreCase = true)
                                )
                            }
                        }
                    }

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

        // 6. Moments Feed Listener (Real-Time Moments - Paginated for instant app opening)
        db.collection("moments").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(30).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("Firestore", "Moments listener error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fetched = snapshot.documents.map { parseMomentFromDoc(it) }
                if (fetched.isNotEmpty()) {
                    val map = linkedMapOf<String, MomentItem>()
                    // 1. Add server moments
                    fetched.forEach { map[it.id] = it }
                    // 2. Preserve any newly posted local moments not yet returned by server
                    moments.forEach { if (!map.containsKey(it.id)) map[it.id] = it }
                    val sorted = map.values.sortedByDescending { it.timestamp }
                    moments.clear()
                    moments.addAll(sorted)
                    saveMomentsToPrefs(prefs, moments)
                }
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

    // Filtered Discover Profiles (Real-Time Proximity & Attribute Matching with Robust Fallback)
    val filteredProfiles = remember(discoverProfiles, selectedCategory, currentUser) {
        val filtered = discoverProfiles.filter { profile ->
            val matchesCategory = selectedCategory == "all" || profile.activeIntent == selectedCategory
            val candLat = if (profile.latitude != 0.0) profile.latitude else (CITY_COORDINATES[profile.city]?.first ?: 22.5726)
            val candLon = if (profile.longitude != 0.0) profile.longitude else (CITY_COORDINATES[profile.city]?.second ?: 88.3639)
            val realDist = calculateDistanceKm(currentUser.latitude, currentUser.longitude, candLat, candLon)
            val matchesDistance = currentUser.showFurtherAway || realDist <= currentUser.maxDistanceKm
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
        // Fallback: If strict filters yield 0 results, show discoverProfiles so registered users are always discoverable!
        if (filtered.isNotEmpty()) filtered else discoverProfiles
    }

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Never bounce back to discover if the keyboard is open!
    // If keyboard is open, pressing back dismisses keyboard & clears focus cleanly, staying on the current screen.
    BackHandler(enabled = currentScreen != "discover") {
        if (isKeyboardOpen) {
            keyboardController?.hide()
            focusManager.clearFocus()
        } else {
            currentScreen = "discover"
        }
    }

    val isAccountSuspended = currentUser.isSuspended && (currentUser.suspensionExpiry == 0L || System.currentTimeMillis() < currentUser.suspensionExpiry)

    if (isAccountSuspended) {
        val expiryText = if (currentUser.suspensionExpiry <= 0L || (currentUser.suspensionExpiry - System.currentTimeMillis()) > (365L * 24 * 60 * 60 * 1000L)) {
            "Permanent Suspension"
        } else {
            val remHours = ((currentUser.suspensionExpiry - System.currentTimeMillis()) / (60 * 60 * 1000L)).coerceAtLeast(1)
            "$remHours hours remaining"
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandDark)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCard),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("🛑", fontSize = 56.sp)
                    Text(
                        text = "ACCOUNT SUSPENDED",
                        color = Color.Red,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Your account has been restricted by LiveTogether administration due to violation of community guidelines.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    HorizontalDivider(color = BrandBorder, thickness = 1.dp)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Reason: ${currentUser.suspensionReason.ifBlank { "Violation of Community Guidelines" }}",
                            color = BrandGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Duration: $expiryText",
                            color = Color(0xFFFF9800),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "User ID: ${if (currentUser.userId.isNotBlank()) currentUser.userId else currentUser.uid.take(8)}",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            auth.signOut()
                            prefs.edit().clear().apply()
                            val intent = Intent(context, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(BrandDark)) {
        Scaffold(
            containerColor = BrandDark,
            topBar = {
                if (activeAudioRoom == null || isAudioRoomMinimized) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandCard)
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            // Real-Time Cloud Firestore Server Refresh Action
                            val infiniteTransition = rememberInfiniteTransition(label = "refreshAnim")
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing)
                                ),
                                label = "refreshRotation"
                            )
                            IconButton(
                                onClick = triggerRefresh,
                                enabled = !refreshEngine.isRefreshing
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Cloud Firestore",
                                    modifier = Modifier.rotate(if (refreshEngine.isRefreshing) rotation else 0f),
                                    tint = if (refreshEngine.isRefreshing) BrandCoral else Color.White
                                )
                            }

                            // Sleek Dual-Mode Search Button
                            IconButton(onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) {
                                    searchQuery = ""
                                    searchRoomResults.clear()
                                    searchUserResults.clear()
                                }
                            }) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (isSearchExpanded) BrandCoral else Color.White
                                )
                            }

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

                            // Discovery Filters (Manual distance, age & city filter)
                            if (currentScreen == "discover") {
                                IconButton(onClick = { showSettingsDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Discovery Filters",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Profile Settings Gear Icon (Visible exclusively on real main profile screen)
                            if (currentScreen == "profile") {
                                IconButton(onClick = { showSettingsDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Profile Settings",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Sleek, Minimal Search Bar & Toggle Drawer
                    AnimatedVisibility(
                        visible = isSearchExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrandCard)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            // Dual-Mode Pill Toggle ("Room" vs "User")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = BrandDark,
                                    border = BorderStroke(1.dp, BrandBorder)
                                ) {
                                    Row(modifier = Modifier.padding(2.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (searchMode == "room") BrandCoral else Color.Transparent)
                                                .clickable { searchMode = "room" }
                                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "🎙️ Room",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (searchMode == "room") Color.White else Color.Gray
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (searchMode == "user") BrandCoral else Color.Transparent)
                                                .clickable { searchMode = "user" }
                                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = "👤 User",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (searchMode == "user") Color.White else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }

                            // Search Input Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                placeholder = {
                                    Text(
                                        text = if (searchMode == "room") "Search 8-digit Room ID or Name..." else "Search 8-digit User ID or Username...",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = BrandCoral,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = BrandDark,
                                    unfocusedContainerColor = BrandDark,
                                    focusedBorderColor = BrandCoral,
                                    unfocusedBorderColor = BrandBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            // Live Firestore Search Results List
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BrandDark),
                                    border = BorderStroke(1.dp, BrandBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isSearching) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = BrandCoral,
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    } else if (searchMode == "room") {
                                        if (searchRoomResults.isEmpty()) {
                                            Text(
                                                text = "No live rooms found for \"$searchQuery\"",
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        } else {
                                            LazyColumn(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 240.dp)
                                                    .padding(6.dp)
                                            ) {
                                                items(searchRoomResults) { room ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                if ((room.category == "vip_room" || room.isVipRoom) && (!currentUser.isVipOrPlatinum() && currentUser.getVipLevel() < 1)) {
                                                                    Toast.makeText(context, "👑 VIP Access Only: This room is reserved for VIP Level 1+ members!", Toast.LENGTH_LONG).show()
                                                                } else {
                                                                    isSearchExpanded = false
                                                                    activeAudioRoom = room
                                                                }
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(CircleShape)
                                                                .background(BrandCard),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(room.emoji.ifBlank { "🎙️" }, fontSize = 16.sp)
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = room.title.ifBlank { room.roomName.ifBlank { "Live Room" } },
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = "ID: ${room.roomId.ifBlank { room.id }} • Host: ${room.hostName}",
                                                                color = Color.Gray,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                        Button(
                                                            onClick = {
                                                                if ((room.category == "vip_room" || room.isVipRoom) && (!currentUser.isVipOrPlatinum() && currentUser.getVipLevel() < 1)) {
                                                                    Toast.makeText(context, "👑 VIP Access Only: This room is reserved for VIP Level 1+ members!", Toast.LENGTH_LONG).show()
                                                                } else {
                                                                    isSearchExpanded = false
                                                                    activeAudioRoom = room
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("Join", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if (searchUserResults.isEmpty()) {
                                            Text(
                                                text = "No users found for \"$searchQuery\"",
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        } else {
                                            LazyColumn(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 240.dp)
                                                    .padding(6.dp)
                                            ) {
                                                items(searchUserResults) { user ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                isSearchExpanded = false
                                                                showDirectMessageDialog = user
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        AsyncImage(
                                                            model = user.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80" },
                                                            contentDescription = user.name,
                                                            modifier = Modifier.size(36.dp).clip(CircleShape),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = user.name,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = "User ID: ${user.userId.ifBlank { user.uid.take(8) }} • ${user.city}",
                                                                color = Color.Gray,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                        Button(
                                                            onClick = {
                                                                isSearchExpanded = false
                                                                showDirectMessageDialog = user
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = BrandCardSubtle),
                                                            border = BorderStroke(1.dp, BrandBorder),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                            shape = RoundedCornerShape(12.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("View", fontSize = 11.sp, color = Color.White)
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
            }
        },
        bottomBar = {
                if (activeAudioRoom == null || isAudioRoomMinimized) {
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
            }
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = refreshEngine.isRefreshing,
                onRefresh = triggerRefresh,
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
                                            val intentCategoriesCount = remember { CATEGORY_LIST.filter { it.id != "all" }.size }
                                            Text(
                                                "🏛️ Intent Rooms ($intentCategoriesCount)",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            if (discoverTab == "cards") {
                                val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

                                DiscoverScreenContent(
                                    profiles = filteredProfiles,
                                    selectedCategory = selectedCategory,
                                    currentUser = currentUser,
                                    onSelectCategory = { selectedCategory = it },
                                    onPass = { profile ->
                                        val isToday = currentUser.lastSwipeDate == todayDateStr
                                        val currentDailyCount = if (isToday) currentUser.dailySwipeCount else 0

                                        if (!currentUser.isPaidSubscriber() && currentDailyCount >= 15) {
                                            subscriptionPaywallNotice = "Daily Free Limit Reached (15/15 Swipes) 🔒\nUpgrade your membership for unlimited swipes & instant matching!"
                                            showMainSubscriptionModal = true
                                        } else {
                                            val newCount = currentDailyCount + 1
                                            currentUser = currentUser.copy(
                                                dailySwipeCount = newCount,
                                                lastSwipeDate = todayDateStr
                                            )
                                            saveProfileToPrefs(prefs, currentUser)
                                            db.collection("users").document(currentUserId).update(
                                                mapOf(
                                                    "dailySwipeCount" to newCount,
                                                    "lastSwipeDate" to todayDateStr
                                                )
                                            )
                                            recordSwipe(db, currentUserId, profile.uid, "pass")
                                            val remaining = maxOf(0, 15 - newCount)
                                            if (!currentUser.isPaidSubscriber()) {
                                                Toast.makeText(context, "Passed ${profile.name} ($remaining free swipes left today)", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Passed ${profile.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onSuperStar = { profile ->
                                        if (!currentUser.isPaidSubscriber()) {
                                            subscriptionPaywallNotice = "⭐ SuperStar is a Premium Feature!\nUpgrade your membership to send SuperStars, stand out, and get noticed instantly."
                                            showMainSubscriptionModal = true
                                        } else {
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
                                        }
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
                                        val isToday = currentUser.lastSwipeDate == todayDateStr
                                        val currentDailyCount = if (isToday) currentUser.dailySwipeCount else 0

                                        if (!currentUser.isPaidSubscriber() && currentDailyCount >= 15) {
                                            subscriptionPaywallNotice = "Daily Free Limit Reached (15/15 Swipes) 🔒\nUpgrade your membership for unlimited swipes & instant matching!"
                                            showMainSubscriptionModal = true
                                        } else {
                                            val newCount = currentDailyCount + 1
                                            currentUser = currentUser.copy(
                                                dailySwipeCount = newCount,
                                                lastSwipeDate = todayDateStr
                                            )
                                            saveProfileToPrefs(prefs, currentUser)
                                            db.collection("users").document(currentUserId).update(
                                                mapOf(
                                                    "dailySwipeCount" to newCount,
                                                    "lastSwipeDate" to todayDateStr
                                                )
                                            )
                                            recordSwipe(db, currentUserId, profile.uid, "like")
                                            checkAndHandleMutualMatch(
                                                db = db,
                                                currentUser = currentUser,
                                                partner = profile,
                                                onMatch = { matchedPartner = profile }
                                            )
                                            val remaining = maxOf(0, 15 - newCount)
                                            if (!currentUser.isPaidSubscriber()) {
                                                Toast.makeText(context, "Liked ${profile.name}! ($remaining free swipes left today)", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            } else {
                                // 2-Column Clean Intent Category Hubs Grid Layout
                                IntentRoomsGrid(
                                    categories = CATEGORY_LIST.filter { it.id != "all" },
                                    catalogRooms = emptyList(),
                                    rooms = roomsList,
                                    allUsers = discoverProfiles + currentUser,
                                    onCreateRoom = {
                                        createRoomInitialCategory = "free_tonight"
                                        showCreateRoomDialog = true
                                    },
                                    onOpenCategoryHub = { category ->
                                        if (category.id == "vip_room" && (!currentUser.isVipOrPlatinum() && currentUser.getVipLevel() < 1)) {
                                            Toast.makeText(context, "👑 VIP Access Only: VIP Room is reserved for VIP Level 1+ members!", Toast.LENGTH_LONG).show()
                                        } else {
                                            activeCategoryHub = category
                                        }
                                    },
                                    onJoinRoom = { room ->
                                        if ((room.category == "vip_room" || room.isVipRoom) && (!currentUser.isVipOrPlatinum() && currentUser.getVipLevel() < 1)) {
                                            Toast.makeText(context, "👑 VIP Access Only: This room is reserved for VIP Level 1+ members!", Toast.LENGTH_LONG).show()
                                            return@IntentRoomsGrid
                                        }
                                        val targetHost = room.hostUid.ifBlank { room.hostId }
                                        val isHostCandidate = currentUserId.isNotBlank() && currentUserId == targetHost
                                        val canActuallyHost = !isHostCandidate || currentUser.canHostRoom()

                                        if (!isHostCandidate && (!room.isHostLive || room.statusMessage.equals("OFFLINE", ignoreCase = true) || room.statusMessage.equals("CLOSED", ignoreCase = true))) {
                                            Toast.makeText(context, "⏳ The host (${room.hostName.ifBlank { "Host" }}) is currently offline. They will be live soon!", Toast.LENGTH_SHORT).show()
                                            return@IntentRoomsGrid
                                        }
                                        // 1. Register presence in rooms/{roomId}/members/{userId}

                                        if (isHostCandidate && !canActuallyHost) {
                                            subscriptionPaywallNotice = "👑 Host Access Criteria: Minimum Platinum Membership Required!\nYour 7-day launch trial has ended. Upgrade to Platinum (₹249/mo) or VIP to host."
                                            showMainSubscriptionModal = true
                                        }

                                        val isHost = isHostCandidate && canActuallyHost
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
                                            val hostUpdates = mutableMapOf<String, Any>(
                                                "isLive" to true,
                                                "isHostLive" to true,
                                                "isEnded" to false,
                                                "endedAt" to 0L,
                                                "statusMessage" to "LIVE NOW"
                                            )
                                            if (room.hostId.isBlank()) {
                                                hostUpdates["hostId"] = currentUserId
                                                hostUpdates["hostUid"] = currentUserId
                                                hostUpdates["hostName"] = currentUser.name
                                                hostUpdates["hostPhotoUrl"] = currentUser.photo
                                            }
                                            db.collection("rooms").document(room.id).update(hostUpdates)
                                            if (room.category.isNotBlank()) {
                                                db.collection("intent_categories").document(room.category)
                                                    .collection("rooms").document(room.id).update(hostUpdates)
                                            }
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
                            currentUser = currentUser,
                            onOpenSubscription = {
                                subscriptionPaywallNotice = "👑 Unlock Who Liked You!\nUpgrade your membership to see photos, names, and match back instantly."
                                showMainSubscriptionModal = true
                            },
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
                                val idx = moments.indexOfFirst { it.id == moment.id }
                                if (idx != -1) {
                                    val isLiked = moment.likedByUsers.contains(currentUserId)
                                    val updatedList = if (isLiked) moment.likedByUsers - currentUserId else moment.likedByUsers + currentUserId
                                    moments[idx] = moment.copy(
                                        likedByUsers = updatedList,
                                        likes = updatedList.size
                                    )
                                }
                                toggleMomentLike(db, moment, currentUserId)
                            },
                            onCommentMoment = { moment ->
                                showCommentsMoment = moment
                            },
                            onEditMoment = { moment ->
                                momentToEdit = moment
                            },
                            onDeleteMoment = { moment ->
                                momentToDelete = moment
                            }
                        )
                    }

                    "profile" -> {
                        ProfileScreenContent(
                            currentUser = currentUser,
                            userMoments = moments.filter {
                                it.userId == currentUserId || it.userId == currentUser.uid || (currentUser.name.isNotBlank() && it.userName == currentUser.name)
                            },
                            db = db,
                            currentUserId = currentUserId,
                            onShareMomentClick = { showPostMomentDialog = true },
                            onEditMoment = { moment ->
                                momentToEdit = moment
                            },
                            onDeleteMoment = { moment ->
                                momentToDelete = moment
                            },
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
                            onOpenLocationSetup = { showLocationSetupDialog = true },
                            onOpenPrivacyPolicy = { showPrivacyPolicy = true },
                            onLogout = {
                                try {
                                    auth.signOut()
                                } catch (_: Exception) {}
                                prefs.edit().clear().apply()
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                (context as? android.app.Activity)?.finish()
                            },
                            onDeleteAccount = {
                                val uidToDelete = currentUserId
                                try {
                                    auth.signOut()
                                } catch (_: Exception) {}
                                prefs.edit().clear().apply()
                                if (uidToDelete.isNotBlank()) {
                                    db.collection("users").document(uidToDelete).delete()
                                }
                                Toast.makeText(context, "Account deleted", Toast.LENGTH_LONG).show()
                                val intent = Intent(context, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                                (context as? android.app.Activity)?.finish()
                            }
                        )
                    }
                }
            }
        }

        // --- Dialogs & Modals ---

        // Dedicated Full Privacy Policy Screen
        if (showPrivacyPolicy) {
            PrivacyPolicyScreen(
                onBack = { showPrivacyPolicy = false }
            )
        }

        // 0. Advanced Real-time Audio or Video Hangout Room Dialog
        if (activeAudioRoom != null) {
            val room = activeAudioRoom!!
            val onRoomDismissAction = {
                isAudioRoomMinimized = false
                if (room.id.isNotBlank()) {
                    // If leaving user is the room host, mark room offline (NEVER delete room document)
                    val targetHost = room.hostUid.ifBlank { room.hostId }
                    if (currentUserId.isNotBlank() && targetHost == currentUserId) {
                        val isVideoType = room.isVideoRoom || room.roomType == "video" || room.roomType == "video_pk" || room.category == "video_stream" || room.category == "video_pk"
                        val updateMap: Map<String, Any> = if (isVideoType) {
                            mapOf(
                                "isHostLive" to false,
                                "isLive" to true,
                                "statusMessage" to "OFFLINE",
                                "isPlaying" to false,
                                "isEnded" to false,
                                "endedAt" to 0L
                            )
                        } else {
                            mapOf(
                                "isHostLive" to false,
                                "statusMessage" to "Host Away • Open Hangout",
                                "isPlaying" to false
                            )
                        }
                        db.collection("rooms").document(room.id).update(updateMap)
                        if (room.category.isNotBlank()) {
                            db.collection("intent_categories").document(room.category)
                                .collection("rooms").document(room.id).update(updateMap)
                        }
                    }

                    // Remove presence from rooms/{roomId}/members/{currentUserId}
                    if (currentUserId.isNotBlank()) {
                        db.collection("rooms").document(room.id)
                            .collection("members").document(currentUserId)
                            .delete()
                        if (room.category.isNotBlank()) {
                            db.collection("intent_categories").document(room.category)
                                .collection("rooms").document(room.id)
                                .collection("members").document(currentUserId)
                                .delete()
                        }
                    }
                }
                currentUser = currentUser.copy(current_room = "")
                saveProfileToPrefs(prefs, currentUser)
                if (currentUserId.isNotBlank()) {
                    db.collection("users").document(currentUserId).update("current_room", "")
                }
                AudioPlayerManager.stop()
                activeAudioRoom = null
            }

            // Real-time listener for current active room to support instant mode conversion (Audio <-> Video)
            DisposableEffect(room.id) {
                val reg = db.collection("rooms").document(room.id)
                    .addSnapshotListener { snap, err ->
                        if (err == null && snap != null && snap.exists()) {
                            val updated = parseRoomFromDoc(snap)
                            if (updated != null && (updated.roomType != room.roomType || updated.isVideoRoom != room.isVideoRoom || updated.title != room.title)) {
                                activeAudioRoom = updated
                            }
                        }
                    }
                onDispose {
                    reg.remove()
                }
            }

            if (room.roomType == "video" || room.roomType == "video_pk" || room.isVideoRoom) {
                VideoHangoutRoomDialog(
                    initialRoom = room,
                    currentUser = currentUser,
                    allProfiles = discoverProfiles,
                    db = db,
                    onDismiss = onRoomDismissAction,
                    onDirectChat = { partner ->
                        showDirectMessageDialog = partner
                    },
                    isMinimized = isAudioRoomMinimized,
                    onMinimize = { isAudioRoomMinimized = true },
                    onMaximize = { isAudioRoomMinimized = false },
                    onConvertToAudio = { converted ->
                        activeAudioRoom = converted
                    }
                )
            } else {
                AudioHangoutRoomDialog(
                    initialRoom = room,
                    currentUser = currentUser,
                    allProfiles = discoverProfiles,
                    db = db,
                    onDismiss = onRoomDismissAction,
                    onDirectChat = { partner ->
                        showDirectMessageDialog = partner
                    },
                    isMinimized = isAudioRoomMinimized,
                    onMinimize = { isAudioRoomMinimized = true },
                    onMaximize = { isAudioRoomMinimized = false },
                    onConvertToVideo = { converted ->
                        activeAudioRoom = converted
                    }
                )
            }
        }

        // 0.05 Category Hub / Lobby Dialog (Category-Scoped Sub-Rooms Grid)
        if (activeCategoryHub != null) {
            val cat = activeCategoryHub!!
            val bannerUrl = getCategoryBanner(cat.id)
            CategoryHubLobbyDialog(
                category = cat,
                categoryBannerUrl = bannerUrl,
                allRooms = roomsList,
                allUsers = discoverProfiles + currentUser,
                currentUser = currentUser,
                onDismiss = { activeCategoryHub = null },
                onCreateRoom = {
                    val isVipCat = cat.id == "vip_room"
                    val isUserVip = currentUser.isVipOrPlatinum() || currentUser.getVipLevel() >= 1
                    if (isVipCat && !isUserVip) {
                        Toast.makeText(context, "👑 VIP Room requires VIP Level 1+ membership to create!", Toast.LENGTH_SHORT).show()
                        return@CategoryHubLobbyDialog
                    }
                    createRoomInitialCategory = cat.id
                    activeCategoryHub = null
                    showCreateRoomDialog = true
                },
                onJoinRoom = { room ->
                    val isVipRoomCategory = room.category == "vip_room" || room.isVipRoom
                    val isUserVip = currentUser.isVipOrPlatinum() || currentUser.getVipLevel() >= 1
                    if (isVipRoomCategory && !isUserVip) {
                        Toast.makeText(context, "👑 VIP Access Only: This room is reserved for VIP Level 1+ members!", Toast.LENGTH_LONG).show()
                        return@CategoryHubLobbyDialog
                    }
                    val targetHost = room.hostUid.ifBlank { room.hostId }
                    val isHostCandidate = currentUserId.isNotBlank() && currentUserId == targetHost
                    val canActuallyHost = !isHostCandidate || currentUser.canHostRoom()

                    if (!isHostCandidate && (!room.isHostLive || room.statusMessage.equals("OFFLINE", ignoreCase = true) || room.statusMessage.equals("CLOSED", ignoreCase = true))) {
                        Toast.makeText(context, "⏳ The host (${room.hostName.ifBlank { "Host" }}) is currently offline. They will be live soon!", Toast.LENGTH_SHORT).show()
                        return@CategoryHubLobbyDialog
                    }

                    if (isHostCandidate && !canActuallyHost) {
                        subscriptionPaywallNotice = "👑 Host Access Criteria: Minimum Platinum Membership Required!\nYour 7-day launch trial has ended. Upgrade to Platinum (₹249/mo) or VIP to host."
                        showMainSubscriptionModal = true
                    }

                    val isHost = isHostCandidate && canActuallyHost
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
                        val hostUpdates = mutableMapOf<String, Any>(
                            "isLive" to true,
                            "isHostLive" to true,
                            "isEnded" to false,
                            "endedAt" to 0L,
                            "statusMessage" to "LIVE NOW"
                        )
                        if (room.hostId.isBlank()) {
                            hostUpdates["hostId"] = currentUserId
                            hostUpdates["hostUid"] = currentUserId
                            hostUpdates["hostName"] = currentUser.name
                            hostUpdates["hostPhotoUrl"] = currentUser.photo
                        }
                        db.collection("rooms").document(room.id).update(hostUpdates)
                        if (room.category.isNotBlank()) {
                            db.collection("intent_categories").document(room.category)
                                .collection("rooms").document(room.id).update(hostUpdates)
                        }
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

                    activeCategoryHub = null
                    val isVideo = cat.id == "video_stream" || room.category == "video_stream" || room.roomType == "video" || room.roomType == "video_pk" || room.isVideoRoom
                    val targetRoom = if (isVideo) room.copy(roomType = "video") else room.copy(roomType = "audio")
                    activeAudioRoom = targetRoom
                }
            )
        }

        // Subscription Paywall Modal
        if (showMainSubscriptionModal) {
            SubscriptionPlansModal(
                currentPlan = currentUser.plan,
                customNotice = subscriptionPaywallNotice,
                onDismiss = {
                    showMainSubscriptionModal = false
                    subscriptionPaywallNotice = null
                },
                onSelectPlan = { plan ->
                    currentUser = currentUser.copy(plan = plan.title)
                    saveProfileToPrefs(prefs, currentUser)
                    db.collection("users").document(currentUserId).update("plan", plan.title)
                    Toast.makeText(context, "Upgraded to ${plan.title}! 🎉", Toast.LENGTH_SHORT).show()
                    showMainSubscriptionModal = false
                    subscriptionPaywallNotice = null
                }
            )
        }

        // 7 Days Free Trial Host Offer Dialog
        if (showHostTrialOfferDialog) {
            val daysLeft = currentUser.getHostTrialDaysLeft()
            AlertDialog(
                onDismissRequest = { showHostTrialOfferDialog = false },
                containerColor = BrandCard,
                icon = { Text("🎉", fontSize = 36.sp) },
                title = {
                    Text(
                        "7 Days Free Trial For App Launch!",
                        color = BrandGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = BrandCoral.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, BrandCoral)
                        ) {
                            Text(
                                text = "Launch Offer • $daysLeft Days Remaining",
                                color = BrandCoral,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Enjoy 100% free room hosting during our launch celebration! You can create live rooms, stream music, and speak on stage.\n\nNote: After 7 days, a minimum Platinum recharge will be mandatory to continue hosting live rooms.",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showHostTrialOfferDialog = false
                            showCreateRoomDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Got It! Create Room 🎙️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showHostTrialOfferDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // 0.1 Create Custom Room Dialog
        if (showCreateRoomDialog) {
            CreateRoomDialog(
                currentUser = currentUser,
                initialCategory = createRoomInitialCategory,
                onDismiss = { showCreateRoomDialog = false },
                onCreate = { title, categoryId, themeUrl, roomType ->
                    val existingHostRoom = roomsList.firstOrNull { r ->
                        val rHost = r.hostUid.ifBlank { r.hostId }
                        rHost == currentUserId && (r.category == categoryId || (categoryId == "video_stream" && (r.category == "video_stream" || r.roomType == "video" || r.isVideoRoom)))
                    }
                    val generatedRoomCode = (10000000..99999999).random().toString()
                    val roomId = existingHostRoom?.id?.ifBlank { null } ?: generatedRoomCode
                    val actualRoomType = if (categoryId == "video_stream") "video" else roomType
                    val catEmoji = if (actualRoomType == "video_pk") "⚔️" else if (actualRoomType == "video") "📹" else (CATEGORY_LIST.find { it.id == categoryId }?.label?.take(2)?.trim() ?: "🎙️")
                    val finalBanner = if (themeUrl.isNotBlank()) themeUrl else getCategoryBanner(categoryId)
                    val newRoom = RoomItem(
                        id = roomId,
                        roomId = roomId,
                        title = title,
                        roomName = title,
                        category = categoryId,
                        emoji = catEmoji,
                        banner_image_url = finalBanner,
                        roomType = actualRoomType,
                        videoLayout = if (roomType == "video_pk") "2p" else "1p",
                        hostId = currentUserId,
                        hostUid = currentUserId,
                        hostName = currentUser.name,
                        hostPhotoUrl = currentUser.photo,
                        nowPlayingTitle = CURATED_HANGOUT_TRACKS[0].title,
                        nowPlayingArtist = CURATED_HANGOUT_TRACKS[0].artist,
                        nowPlayingUrl = CURATED_HANGOUT_TRACKS[0].url,
                        nowPlayingArtworkUrl = CURATED_HANGOUT_TRACKS[0].artworkUrl,
                        currentPlayingTrack = CURATED_HANGOUT_TRACKS[0].title,
                        playbackPosition = 0L,
                        isPlaying = true,
                        isCustom = true,
                        isHostLive = true,
                        isLive = true,
                        statusMessage = "LIVE NOW",
                        privacy = if (categoryId == "vip_room") "vip_only" else "public",
                        createdAt = System.currentTimeMillis(),
                        trackStartedAt = System.currentTimeMillis()
                    )
                    // Save to Firestore rooms/{roomId}
                    db.collection("rooms").document(roomId).set(newRoom)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Room $roomId successfully created in Firestore")
                            // Notify all followers about host going live
                            sendHostLiveNotification(
                                db = db,
                                hostId = currentUserId,
                                hostName = currentUser.name,
                                hostPhoto = currentUser.photo,
                                room = newRoom
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Failed to create room $roomId: ${e.message}")
                            Toast.makeText(context, "Error saving room: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    // Save to intent_categories/{categoryId}/rooms/{roomId}
                    db.collection("intent_categories").document(categoryId)
                        .collection("rooms").document(roomId).set(newRoom)

                    // Register host member in rooms/{roomId}/members/{userId}
                    val hostMember = RoomMember(
                        uid = currentUserId,
                        userId = currentUser.userId,
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
                    if (roomsList.none { it.id == roomId }) {
                        roomsList.add(0, newRoom)
                    }
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
                onOpenLocationSetup = { showLocationSetupDialog = true },
                onOpenPrivacyPolicy = { showPrivacyPolicy = true },
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

        // Location Setup & Onboarding Dialog
        if (showLocationSetupDialog) {
            LocationSetupDialog(
                currentUser = currentUser,
                onDismiss = { showLocationSetupDialog = false },
                onSaveLocation = { updated ->
                    currentUser = updated
                    saveProfileToPrefs(prefs, updated)
                    db.collection("users").document(currentUserId).set(updated, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(context, "Location updated in real-time! 📍", Toast.LENGTH_SHORT).show()
                        }
                    showLocationSetupDialog = false
                }
            )
        }

        // 3. Real-Time Notification Center Dialog
        if (showNotificationsDialog) {
            NotificationsDialog(
                notifications = notifications,
                onDismiss = { showNotificationsDialog = false },
                onOpenRoom = { rId ->
                    db.collection("rooms").document(rId).get()
                        .addOnSuccessListener { doc ->
                            if (doc != null && doc.exists()) {
                                activeAudioRoom = parseRoomFromDoc(doc)
                            }
                        }
                },
                onOpenAdmin = { tab ->
                    if (currentUser.isOfficialAdmin(auth.currentUser?.email)) {
                        adminDashboardInitialTab = tab
                        showAdminDashboardModal = true
                    }
                },
                onMarkAllRead = {
                    val toDelete = notifications.toList()
                    notifications.clear()
                    toDelete.forEach { notif ->
                        if (notif.id.isNotBlank()) {
                            db.collection("notifications").document(notif.id).delete()
                            db.collection("users").document(currentUser.uid).collection("notifications").document(notif.id).delete()
                        }
                    }
                }
            )
        }

        if (showAdminDashboardModal && currentUser.isOfficialAdmin(auth.currentUser?.email)) {
            AdminApprovalDashboardModal(
                adminUser = currentUser.copy(isAdmin = true),
                initialTab = adminDashboardInitialTab,
                onDismiss = { showAdminDashboardModal = false }
            )
        }

        // 4. Post Moment Dialog
        if (showPostMomentDialog) {
            PostMomentDialog(
                currentUser = currentUser,
                onDismiss = { showPostMomentDialog = false },
                onPost = { mediaUrls, caption ->
                    val firstMedia = mediaUrls.firstOrNull() ?: ""
                    val newMoment = MomentItem(
                        id = UUID.randomUUID().toString(),
                        userId = currentUserId,
                        userName = currentUser.name.ifBlank { "You" },
                        userPhoto = currentUser.photo,
                        mediaUrl = firstMedia,
                        mediaUrls = mediaUrls,
                        caption = caption,
                        timestamp = System.currentTimeMillis()
                    )
                    // Optimistic update: immediately show in moments feed and profile
                    val existingIdx = moments.indexOfFirst { it.id == newMoment.id }
                    if (existingIdx == -1) {
                        moments.add(0, newMoment)
                    } else {
                        moments[existingIdx] = newMoment
                    }
                    saveMomentsToPrefs(prefs, moments)
                    showPostMomentDialog = false
                    Toast.makeText(context, "Moment shared successfully! 🎉", Toast.LENGTH_SHORT).show()

                    // Sync to Cloud Firestore
                    db.collection("moments").document(newMoment.id).set(newMoment)
                        .addOnSuccessListener {
                            Log.d("Moments", "Moment synced to Firestore: ${newMoment.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Moments", "Firestore write failed: ${e.message}")
                        }
                }
            )
        }

        // 4b. Edit Moment Dialog
        if (momentToEdit != null) {
            val target = momentToEdit!!
            EditMomentDialog(
                moment = target,
                onDismiss = { momentToEdit = null },
                onSave = { newCaption, newUrls ->
                    val firstMedia = newUrls.firstOrNull() ?: target.mediaUrl
                    val updated = target.copy(
                        caption = newCaption,
                        mediaUrls = newUrls,
                        mediaUrl = firstMedia
                    )
                    val idx = moments.indexOfFirst { it.id == target.id }
                    if (idx != -1) {
                        moments[idx] = updated
                    }
                    saveMomentsToPrefs(prefs, moments)
                    momentToEdit = null
                    Toast.makeText(context, "Moment updated successfully! ✨", Toast.LENGTH_SHORT).show()

                    // Sync to Cloud Firestore
                    db.collection("moments").document(target.id).set(updated, SetOptions.merge())
                }
            )
        }

        // 4c. Delete Moment Dialog
        if (momentToDelete != null) {
            val target = momentToDelete!!
            AlertDialog(
                onDismissRequest = { momentToDelete = null },
                title = { Text("Delete Moment?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete this post?", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = target.id
                            moments.removeAll { it.id == id }
                            saveMomentsToPrefs(prefs, moments)
                            momentToDelete = null
                            Toast.makeText(context, "Moment deleted", Toast.LENGTH_SHORT).show()

                            // Delete from Cloud Firestore
                            db.collection("moments").document(id).delete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { momentToDelete = null }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = BrandCard,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // 5. Direct Message Routing to Advanced 1-on-1 Chat Screen
        if (showDirectMessageDialog != null) {
            val target = showDirectMessageDialog!!
            showDirectMessageDialog = null
            activeChatPartner = target
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

        // 7. Mutual Match Celebration Dialog with Connected Hearts Animation
        if (matchedPartner != null) {
            MutualMatchCelebrationDialog(
                currentUser = currentUser,
                partner = matchedPartner!!,
                onChat = {
                    val partner = matchedPartner
                    matchedPartner = null
                    activeChatPartner = partner
                },
                onDismiss = { matchedPartner = null }
            )
        }

        // 8. Active 1-on-1 Chat Room (Advanced Intimacy & Media Engine)
        if (activeChatPartner != null) {
            Advanced1v1ChatScreen(
                currentUserId = currentUserId,
                currentUser = currentUser,
                partner = activeChatPartner!!,
                db = db,
                onDismiss = { activeChatPartner = null },
                onUpdateCurrentUser = { updated ->
                    currentUser = updated
                    saveProfileToPrefs(prefs, updated)
                }
            )
        }

        // 9. In-App Auto-Update Notification Dialog
        if (availableAppUpdate != null) {
            AppUpdateDialog(
                updateInfo = availableAppUpdate!!,
                downloadProgress = updateDownloadProgress,
                isDownloading = isDownloadingUpdate,
                onUpdateClick = { info ->
                    isDownloadingUpdate = true
                    updateDownloadProgress = 0f
                    AppUpdateManager.downloadAndInstallApk(
                        context = context,
                        apkUrl = info.apkUrl,
                        onProgress = { progress ->
                            updateDownloadProgress = progress
                        },
                        onSuccess = {
                            isDownloadingUpdate = false
                            updateDownloadProgress = 1f
                        },
                        onError = { err ->
                            isDownloadingUpdate = false
                            updateDownloadProgress = -1f
                            Toast.makeText(context, "Update error: $err", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                onDismiss = {
                    if (!availableAppUpdate!!.isForceUpdate) {
                        availableAppUpdate = null
                    }
                }
            )
        }

        // 10. First-Time Sign Up Welcome Bonus & 7-Day VIP 1 Celebration Dialog
        if (showWelcomeBonusDialog) {
            WelcomeBonusCelebrationDialog(
                onClaim = {
                    prefs.edit().putBoolean(WelcomeBonusManager.PREF_SHOW_WELCOME_DIALOG, false).apply()
                    showWelcomeBonusDialog = false
                }
            )
        }
    }
}

// -------------------------------------------------------------
// 1. INTENT ROOMS 2-COLUMN GRID (REQUIREMENT 1)
// -------------------------------------------------------------
@Composable
fun IntentRoomsGrid(
    categories: List<IntentCategory> = CATEGORY_LIST.filter { it.id != "all" },
    catalogRooms: List<RoomItem> = emptyList(),
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
                    text = "Explore ${categories.size} curated category hubs to meet active members and join live rooms.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onCreateRoom,
                colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+ Host 🎙️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                val bannerUrl = getCategoryBanner(category.id)
                val isVipCategory = category.id == "vip_room"

                // Dynamic real-time active users in this category
                val activeCount = allUsers.count {
                    it.activeIntent == category.id || it.current_room == category.id || it.current_room.startsWith(category.id)
                }

                // Sub-rooms created under this category
                val categoryRooms = rooms.filter {
                    if (category.id == "video_stream") {
                        it.category == "video_stream" || it.roomType == "video" || it.roomType == "video_pk" || it.isVideoRoom
                    } else {
                        it.category == category.id
                    }
                }
                val subRoomsCount = categoryRooms.size
                val activeRoomsCount = categoryRooms.count { it.isHostLive }
                val isAnyRoomLive = activeRoomsCount > 0
                val totalRoomViewers = categoryRooms.filter { it.isHostLive }.sumOf { maxOf(1, it.activeUserCount) }
                val totalOnlineUsers = maxOf(activeCount, totalRoomViewers)
                val isAnyAudioPlaying = categoryRooms.any { it.isHostLive && it.isPlaying }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(224.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = if (isVipCategory) 1.5.dp else 1.dp,
                            brush = if (isVipCategory) Brush.sweepGradient(listOf(BrandGold, Color(0xFFFFA000), BrandGold)) else Brush.linearGradient(listOf(BrandBorder, BrandBorder)),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { onOpenCategoryHub(category) }
                ) {
                    // Fixed curated system background image (Read-only, system managed)
                    AsyncImage(
                        model = bannerUrl,
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
                            // Room Live / Sub-rooms status
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isAnyRoomLive) Color(0xFFFF1744).copy(alpha = 0.90f) else Color.Black.copy(alpha = 0.70f))
                                    .border(0.8.dp, if (isAnyRoomLive) BrandGold else Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isAnyRoomLive) Color.White else BrandGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAnyRoomLive) "🔴 $activeRoomsCount Live" else if (subRoomsCount > 0) "🎙️ $subRoomsCount Rooms" else "✨ Lounge",
                                    color = Color.White,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Real-time online active members & viewers
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .border(0.8.dp, if (isVipCategory) BrandGold else BrandGreen.copy(alpha = 0.6f), RoundedCornerShape(50))
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isVipCategory) "👑 VIP 1+" else "👥 $totalOnlineUsers Online",
                                    color = if (isVipCategory) BrandGold else Color.White,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Bottom Title, Colorful Large Equalizer & Enter Hub Button
                        Column {
                            Text(
                                text = category.label,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // 13 LIVE INTENT CATEGORIES VIBRANT COLORFUL LARGE EQUALIZER
                            CategoryEqualizerThemeVisualizer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                isLive = isAnyRoomLive,
                                isPlaying = isAnyAudioPlaying
                            )

                            // Status Line
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAnyRoomLive) "🔴 $activeRoomsCount Live Stream${if (activeRoomsCount > 1) "s" else ""}" else if (subRoomsCount > 0) "🎙️ $subRoomsCount Created" else "✨ Open Lounge",
                                    color = if (isAnyRoomLive) BrandGreen else BrandGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🔥 $totalOnlineUsers Active",
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

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
                    val candLat = if (currentProfile.latitude != 0.0) currentProfile.latitude else (CITY_COORDINATES[currentProfile.city]?.first ?: 22.5726)
                    val candLon = if (currentProfile.longitude != 0.0) currentProfile.longitude else (CITY_COORDINATES[currentProfile.city]?.second ?: 88.3639)
                    val realDist = calculateDistanceKm(currentUser.latitude, currentUser.longitude, candLat, candLon)
                    val locationLabel = if (currentProfile.city.equals(currentUser.city, ignoreCase = true)) {
                        "📍 In your city (${currentProfile.city}) • $realDist km away"
                    } else {
                        "📍 ${currentProfile.locationName.ifBlank { currentProfile.city }} • $realDist km away"
                    }
                    Text(
                        text = locationLabel,
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
    currentUser: UserProfile,
    onOpenSubscription: () -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Who Liked You", color = BrandCoral, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (!currentUser.isPaidSubscriber()) {
                Surface(
                    color = BrandGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, BrandGold)
                ) {
                    Text(
                        "👑 VIP FEATURE",
                        color = BrandGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (!currentUser.isPaidSubscriber()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(BrandGold, BrandCoral)))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔒", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Secret Admirers (${likerProfiles.size})",
                            color = BrandGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Free members cannot see who liked or visited their profile. Upgrade your membership to reveal photos, names, and match back instantly!",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenSubscription,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Unlock Who Liked You 👑", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        if (likerProfiles.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No incoming likes yet. Keep your profile active!", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(likerProfiles) { liker ->
                    if (!currentUser.isPaidSubscriber()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSubscription() },
                            colors = CardDefaults.cardColors(containerColor = BrandCard),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BrandBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(BrandDark)
                                        .border(1.dp, BrandGold.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔒", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Someone Liked You", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Upgrade to reveal profile", color = BrandGold, fontSize = 11.sp)
                                    Text("📍 Near you in ${liker.city}", color = Color.Gray, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = onOpenSubscription,
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, BrandGold),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Unlock 🔒", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
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
}

// -------------------------------------------------------------
// CHATS SCREEN CONTENT WITH INBOX BLOCK / UNBLOCK
// -------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsScreenContent(
    matches: List<MatchItem>,
    currentUserId: String,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onOpenChat: (UserProfile) -> Unit,
    onToggleBlock: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val partnerMap = remember { mutableStateMapOf<String, UserProfile>() }
    var selectedActionMatch by remember { mutableStateOf<Pair<MatchItem, UserProfile>?>(null) }
    val archivedMatchIds = remember { mutableStateListOf<String>() }

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

    val visibleMatches = matches.filter { !archivedMatchIds.contains(it.matchId) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Matches & Inbox", color = BrandCoral, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (archivedMatchIds.isNotEmpty()) {
                TextButton(onClick = { archivedMatchIds.clear() }) {
                    Text("Show Archived (${archivedMatchIds.size})", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (visibleMatches.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No messages yet. Like back profiles to start chatting!", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val companion = UserProfile(
                                uid = "companion_maya_vip",
                                userId = "maya_bestie_vip",
                                name = "Maya (Bestie 🌸)",
                                photo = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
                                gender = "female",
                                city = "Mumbai"
                            )
                            onOpenChat(companion)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("✨ Chat with Maya (Intimacy & Media Demo)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibleMatches) { match ->
                    val pId = match.users.firstOrNull { it != currentUserId }
                    val partner = partnerMap[pId]
                    if (partner != null) {
                        val isBlocked = currentUser.blockedUserIds.contains(partner.uid)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onOpenChat(partner) },
                                    onLongClick = { selectedActionMatch = Pair(match, partner) }
                                ),
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
                                    Text(
                                        text = match.lastMessage.ifBlank { "Tap to send a message" },
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { selectedActionMatch = Pair(match, partner) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Inbox Action Dialog (Archive, Clear Chat, Delete Conversation, Block, Report)
    if (selectedActionMatch != null) {
        val (selMatch, selPartner) = selectedActionMatch!!
        val isBlocked = currentUser.blockedUserIds.contains(selPartner.uid)

        AlertDialog(
            onDismissRequest = { selectedActionMatch = null },
            containerColor = BrandCard,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = selPartner.photo,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(selPartner.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Archive Option
                    TextButton(
                        onClick = {
                            archivedMatchIds.add(selMatch.matchId)
                            selectedActionMatch = null
                            Toast.makeText(context, "Chat archived", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("📁", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Archive Chat", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    // Clear Chat History Option
                    TextButton(
                        onClick = {
                            db.collection("matches").document(selMatch.matchId).collection("messages").get()
                                .addOnSuccessListener { sn ->
                                    for (d in sn.documents) { d.reference.delete() }
                                    db.collection("matches").document(selMatch.matchId).update("lastMessage", "")
                                    Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                                }
                            selectedActionMatch = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("🧹", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Clear Chat History", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    // Block / Unblock Option
                    TextButton(
                        onClick = {
                            onToggleBlock(selPartner)
                            selectedActionMatch = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isBlocked) "✅" else "🚫", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (isBlocked) "Unblock User" else "Block User",
                                color = if (isBlocked) BrandGreen else Color(0xFFFF5252),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Report User Option
                    TextButton(
                        onClick = {
                            val reportData = hashMapOf(
                                "reporterId" to currentUserId,
                                "reporterName" to currentUser.name,
                                "reportedUserId" to selPartner.uid,
                                "reportedUserName" to selPartner.name,
                                "reason" to "Reported from inbox",
                                "timestamp" to System.currentTimeMillis()
                            )
                            db.collection("reports").add(reportData)
                            Toast.makeText(context, "Report submitted. Thank you.", Toast.LENGTH_SHORT).show()
                            selectedActionMatch = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("🚨", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Report User", color = Color(0xFFFF9100), fontSize = 14.sp)
                        }
                    }

                    HorizontalDivider(color = BrandBorder, thickness = 0.5.dp)

                    // Delete Conversation Option
                    TextButton(
                        onClick = {
                            db.collection("matches").document(selMatch.matchId).collection("messages").get()
                                .addOnSuccessListener { sn ->
                                    for (d in sn.documents) { d.reference.delete() }
                                    db.collection("matches").document(selMatch.matchId).delete()
                                    Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show()
                                }
                            selectedActionMatch = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("🗑️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Conversation", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedActionMatch = null }) {
                    Text("Close", color = Color.Gray)
                }
            }
        )
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
    onCommentMoment: (MomentItem) -> Unit,
    onEditMoment: (MomentItem) -> Unit = {},
    onDeleteMoment: (MomentItem) -> Unit = {}
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
            val listState = rememberLazyListState()
            LaunchedEffect(moments.size) {
                if (moments.isNotEmpty()) {
                    listState.animateScrollToItem(0)
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(moments, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BrandCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = item.userPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80" },
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

                                if (item.userId == currentUserId) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onEditMoment(item) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Post",
                                                tint = BrandCoral,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(2.dp))
                                        IconButton(
                                            onClick = { onDeleteMoment(item) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Post",
                                                tint = Color.Red.copy(alpha = 0.85f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.caption, color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            val displayPhotos = if (item.mediaUrls.isNotEmpty()) item.mediaUrls 
                                else if (item.mediaUrl.isNotBlank()) listOf(item.mediaUrl) 
                                else emptyList()

                            if (displayPhotos.size == 1) {
                                AsyncImage(
                                    model = displayPhotos.first(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (displayPhotos.size > 1) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                ) {
                                    items(displayPhotos) { photoUrl ->
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillParentMaxWidth(0.82f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
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
    onOpenLocationSetup: () -> Unit = {},
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onShareMomentClick: () -> Unit = {},
    onEditMoment: (MomentItem) -> Unit = {},
    onDeleteMoment: (MomentItem) -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {}
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
    var showCoinStoreModal by remember { mutableStateOf(false) }
    var showVipStatusDialog by remember { mutableStateOf(false) }
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showHelpDeskModal by remember { mutableStateOf(false) }
    var showAdminDashboardModal by remember { mutableStateOf(false) }
    var helpDeskPrefilledPackage by remember { mutableStateOf<CoinPackageItem?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showLuxuryStoreModal by remember { mutableStateOf(false) }
    var showLuxuryAvatarModal by remember { mutableStateOf(false) }
    var showVoiceVerificationModal by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingPhoto = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    // 1. Immediately compress & convert to Cloud Firestore Data URI (Base64 JPEG, ~18KB)
                    val firestoreDataUri = ImageUploadManager.convertImageToFirestoreDataUri(context, uri)
                    if (!firestoreDataUri.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            photoUrl = firestoreDataUri
                            val updatedProfile = currentUser.copy(uid = currentUserId, photo = firestoreDataUri)
                            onUpdateProfile(updatedProfile)
                            Toast.makeText(context, "Profile picture updated! 📸", Toast.LENGTH_SHORT).show()
                        }

                        // Persist directly to Cloud Firestore document
                        db.collection("users").document(currentUserId)
                            .set(mapOf("photo" to firestoreDataUri), SetOptions.merge())
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Could not process image. Please try another.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Throwable) {
                    Log.e("Profile", "Image processing failed: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        isUploadingPhoto = false
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF7F4FD),
                        Color(0xFFECE5F8),
                        Color(0xFFE2DAF2)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. TOP STATUS & WALLET BAR (Handle, Diamonds, Coins & Settings)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "@${(name.ifBlank { "member" }).lowercase().replace(" ", "_")}",
                        color = Color(0xFF1B1433),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Diamonds Wallet Pill
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFE2D6F6)),
                            shadowElevation = 1.dp,
                            modifier = Modifier.clickable { showCoinStoreModal = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💎", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = String.format("%,d", currentUser.diamonds),
                                    color = Color(0xFF7E22CE),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("+", color = Color(0xFF9333EA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Coins Wallet Pill
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFE2D6F6)),
                            shadowElevation = 1.dp,
                            modifier = Modifier.clickable { showCoinStoreModal = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = String.format("%,d", currentUser.coins),
                                    color = Color(0xFFB45309),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Settings Hexagon Button
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color(0xFFE2D6F6), CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF1B1433), modifier = Modifier.size(17.dp))
                        }
                    }
                }
            }

            // 2. SOULMATE / COUPLE DUAL-AVATAR HEADER & VOICE INTRO (MATCHING REFERENCE IMAGES)
            item {
                val hasSoulmate = currentUser.bestFriends.isNotEmpty()
                val partnerUid = currentUser.bestFriends.firstOrNull() ?: ""

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFE8DEF8)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Real Voice Verification & Top Badges Row (Zero Dummy)
                        val isVoiceVerified = currentUser.voiceVerificationStatus == "verified" || currentUser.voiceIntroUrl.isNotBlank()
                        val effWealthLvl = currentUser.getEffectiveWealthLevel()
                        val wealthTier = currentUser.getWealthTierInfo()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Voice Verification Card (Real audio prompt, Male/Female verification, Public/Private)
                            Surface(
                                color = if (isVoiceVerified) Color(0xFFF5EEFD) else Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isVoiceVerified) Color(0xFFD8B4FE) else Color(0xFFFECACA)),
                                modifier = Modifier.clickable {
                                    showVoiceVerificationModal = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(if (isVoiceVerified) Color(0xFF7E22CE) else Color(0xFFEF4444)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (isVoiceVerified) "▶" else "🎙️", fontSize = 9.sp, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isVoiceVerified) "Verified Voice (20s)" else "Voice Verification",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isVoiceVerified) Color(0xFF7E22CE) else Color(0xFFDC2626)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isVoiceVerified) "🌐" else "🔒",
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            // Dynamic Real Relationship / Tier Badges (Zero Dummy)
                            if (hasSoulmate) {
                                Surface(
                                    color = Color(0xFFFDF2F8),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(0.8.dp, Color(0xFFFBCFE8))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("❤️", fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Soulmate Bond", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE185D))
                                    }
                                }
                            } else {
                                Surface(
                                    color = Color(0xFFFFFBEB),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(0.8.dp, Color(0xFFFDE68A))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(wealthTier.badgeIcon, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("${wealthTier.tierName} • Lv.$effWealthLvl", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Avatar Display: Dual Avatars with 3D Interlocking Ring if Partner, or Single Luxury Centered
                        if (hasSoulmate) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // User Avatar
                                Box(contentAlignment = Alignment.Center) {
                                    FlowingAvatarFrame(
                                        photoUrl = photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80" },
                                        frameId = currentUser.activeFrame,
                                        avatarSize = 58.dp,
                                        frameSize = 104.dp,
                                        vipLevel = currentUser.getVipLevel(),
                                        showMicEffect = true,
                                        onClick = { showLuxuryAvatarModal = true }
                                    )
                                    // Camera edit pill
                                    IconButton(
                                        onClick = { if (!isUploadingPhoto) showLuxuryAvatarModal = true },
                                        modifier = Modifier
                                            .size(22.dp)
                                            .align(Alignment.BottomEnd)
                                            .background(Color(0xFF7E22CE), CircleShape)
                                            .border(1.dp, Color.White, CircleShape)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(11.dp))
                                    }
                                }

                                // 3D Interlocking Wedding Rings Connector (Animated pulse)
                                Box(
                                    modifier = Modifier
                                        .offset(y = (-4).dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = CircleShape,
                                        border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                                        shadowElevation = 3.dp,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("💍", fontSize = 18.sp)
                                        }
                                    }
                                }

                                // Connected Partner Avatar (Real best friend from Firestore)
                                Box(contentAlignment = Alignment.Center) {
                                    FlowingAvatarFrame(
                                        photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
                                        frameId = "frame_golden_lion",
                                        avatarSize = 58.dp,
                                        frameSize = 104.dp,
                                        vipLevel = 1,
                                        onClick = {
                                            Toast.makeText(context, "Partner Profile ($partnerUid)", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    Surface(
                                        color = Color(0xFF10B981),
                                        shape = CircleShape,
                                        border = BorderStroke(1.5.dp, Color.White),
                                        modifier = Modifier
                                            .size(12.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                    ) {}
                                }
                            }
                        } else {
                            // Single Luxury Centered Avatar
                            Box(contentAlignment = Alignment.Center) {
                                FlowingAvatarFrame(
                                    photoUrl = photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80" },
                                    frameId = currentUser.activeFrame,
                                    avatarSize = 72.dp,
                                    frameSize = 128.dp,
                                    vipLevel = currentUser.getVipLevel(),
                                    showMicEffect = true,
                                    onClick = { showLuxuryAvatarModal = true }
                                )

                                if (isUploadingPhoto) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.55f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF7E22CE),
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }

                                // Online Green Dot
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                        .border(2.dp, Color.White, CircleShape)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-8).dp, y = 8.dp)
                                )

                                // Photo Change Camera Button
                                IconButton(
                                    onClick = { if (!isUploadingPhoto) showLuxuryAvatarModal = true },
                                    enabled = !isUploadingPhoto,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .align(Alignment.BottomEnd)
                                        .background(Color(0xFF7E22CE), CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Upload Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // User Name & Verification Checkmark (Real Verified Shield)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = name,
                                color = Color(0xFF1B1433),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            if (isVoiceVerified) {
                                // Real Gold Shield Verified Badge (Matching reference image)
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🛡️", fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Verified",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    color = Color(0xFFF3F4F6),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Standard",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp).padding(2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // Gender & Age Pill
                            Surface(
                                color = if (gender.contains("woman", ignoreCase = true) || gender.contains("female", ignoreCase = true)) Color(0xFFFCE7F3) else Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${if (gender.contains("woman", ignoreCase = true)) "♀" else "♂"} $age",
                                    color = if (gender.contains("woman", ignoreCase = true)) Color(0xFFDB2777) else Color(0xFF0284C7),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 8-digit User ID Chip with Click-to-Copy
                        val displayProfileUserId = remember(currentUser.userId, currentUser.uid) {
                            val uidVal = currentUser.uid.ifBlank { currentUserId }
                            if (currentUser.userId.isNotBlank() && currentUser.userId.length == 8 && currentUser.userId.all { it.isDigit() }) {
                                currentUser.userId
                            } else {
                                (10000000 + kotlin.math.abs(uidVal.hashCode()) % 90000000).toString()
                            }
                        }

                        Surface(
                            color = Color(0xFFF3E8FF),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.8.dp, Color(0xFFD8B4FE)),
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("User ID", displayProfileUserId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "User ID $displayProfileUserId copied! 📋", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ID: $displayProfileUserId",
                                    color = Color(0xFF6B21A8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy User ID",
                                    tint = Color(0xFF6B21A8),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // REALISTIC 3D METALLIC LEVEL BADGES (ZERO CARTOONS)
                        val effWealthLvlVal = currentUser.getEffectiveWealthLevel()
                        val effCharmLvlVal = currentUser.getEffectiveCharmLevel()
                        val pkLvlVal = currentUser.getEffectivePkLevel()
                        val vipLvlVal = currentUser.getVipLevel()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1. 3D Wealth Level Badge (Metallic Purple & Gold Trim)
                            Surface(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000)))),
                                shadowElevation = 2.dp,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF5B21B6))))
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_wealth_shield_3d),
                                        contentDescription = "Wealth Level",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Lv.$effWealthLvlVal",
                                        color = Color(0xFFFFD700),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // 2. 3D Charm Level Badge (Metallic Rose Gold & Ruby)
                            Surface(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFFF472B6), Color(0xFFEC4899)))),
                                shadowElevation = 2.dp,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(Brush.horizontalGradient(listOf(Color(0xFFDB2777), Color(0xFF9D174D))))
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_charm_heart_3d),
                                        contentDescription = "Charm Level",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Lv.$effCharmLvlVal",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // 3. 3D PK Swords Rank Badge (Metallic Obsidian & Amber Flames)
                            Surface(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))),
                                shadowElevation = 2.dp,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_pk_flame_vs),
                                        contentDescription = "PK Rank",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Lv.$pkLvlVal",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // 4. VIP Imperial Crown Badge (Gold & Imperial Maroon)
                            if (vipLvlVal > 0) {
                                Surface(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .background(Brush.horizontalGradient(listOf(Color(0xFFB45309), Color(0xFF78350F))))
                                            .padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("👑", fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "VIP $vipLvlVal",
                                            color = Color(0xFFFFD700),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Counters (Following, Followers, Moments, Visitors)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9F6FE), RoundedCornerShape(16.dp))
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${currentUser.followingCount}", color = Color(0xFF1B1433), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Text(text = "Following", color = Color(0xFF7C6D93), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFFE2D6F6)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${currentUser.followerCount}", color = Color(0xFF1B1433), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Text(text = "Followers", color = Color(0xFF7C6D93), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFFE2D6F6)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${userMoments.size}", color = Color(0xFF1B1433), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Text(text = "Moments", color = Color(0xFF7C6D93), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // 3. REALISTIC 3D PRESTIGE MEDALS & USER LEVEL PROGRESS CARDS (ZERO DUMMY)
            item {
                val effWealthLvlVal = currentUser.getEffectiveWealthLevel()
                val effWealthXp = currentUser.getEffectiveWealthXp()
                val wealthTier = currentUser.getWealthTierInfo()
                val (minWealthXp, maxWealthXp) = LevelBadgeSystem.getXpThresholds(effWealthLvlVal)
                val wealthProgress = if (maxWealthXp > minWealthXp) {
                    ((effWealthXp - minWealthXp).toFloat() / (maxWealthXp - minWealthXp).toFloat()).coerceIn(0f, 1f)
                } else 1f

                val effCharmLvl = currentUser.getEffectiveCharmLevel()
                val effCharmXp = currentUser.getEffectiveCharmXp()
                val charmTier = currentUser.getCharmTierInfo()
                val (minCharmXp, maxCharmXp) = LevelBadgeSystem.getXpThresholds(effCharmLvl)
                val charmProgress = if (maxCharmXp > minCharmXp) {
                    ((effCharmXp - minCharmXp).toFloat() / (maxCharmXp - minCharmXp).toFloat()).coerceIn(0f, 1f)
                } else 1f

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Wealth Level Card with Real 3D Metallic Badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.2.dp, Brush.horizontalGradient(wealthTier.gradientColors)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = wealthTier.badgeDrawableRes),
                                        contentDescription = wealthTier.tierName,
                                        modifier = Modifier.size(46.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Wealth Level • Lv.$effWealthLvlVal",
                                            color = Color(0xFF1B1433),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${wealthTier.crownEmoji} ${wealthTier.tierName}",
                                            color = Color(0xFFB45309),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "%,d / %,d XP".format(effWealthXp, maxWealthXp),
                                    color = Color(0xFF7C6D93),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { wealthProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = wealthTier.gradientColors.first(),
                                trackColor = Color(0xFFE2D6F6)
                            )
                        }
                    }

                    // Charm Level Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.2.dp, Brush.horizontalGradient(charmTier.gradientColors)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = charmTier.badgeDrawableRes),
                                        contentDescription = charmTier.tierName,
                                        modifier = Modifier.size(46.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Charm Level • Lv.$effCharmLvl",
                                            color = Color(0xFF1B1433),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${charmTier.crownEmoji} ${charmTier.tierName}",
                                            color = Color(0xFFBE185D),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "%,d / %,d XP".format(effCharmXp, maxCharmXp),
                                    color = Color(0xFF7C6D93),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { charmProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = charmTier.gradientColors.first(),
                                trackColor = Color(0xFFFBCFE8)
                            )
                        }
                    }
                }
            }

            // 4. CLOSE FRIENDS SECTION (1/4 to 6/9 GRID)
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE8DEF8)),
                    shadowElevation = 1.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👥 Close Friends (${currentUser.bestFriends.size}/4)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1B1433)
                            )
                            Text(
                                text = "View all ❯",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7E22CE)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (currentUser.bestFriends.isEmpty()) {
                            Surface(
                                color = Color(0xFFF9F6FE),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(0.8.dp, Color(0xFFE2D6F6)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Toast.makeText(context, "Connect with friends in live lounges to add Besties!", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🌼", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("No close friends added yet", color = Color(0xFF1B1433), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Chat & hangout together to forge Bestie & Soulmate bonds!", color = Color(0xFF7C6D93), fontSize = 10.sp)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentUser.bestFriends.take(3).forEachIndexed { idx, friendId ->
                                    val icon = if (idx == 0) "🌼" else if (idx == 1) "🪐" else "⏳"
                                    val tag = if (idx == 0) "Bestie" else "Soulmate"
                                    Surface(
                                        color = Color(0xFFF9F6FE),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2D6F6)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                Toast.makeText(context, "Best Friend: $friendId", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(icon, fontSize = 22.sp)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(friendId.take(7), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B1433), maxLines = 1)
                                            Surface(
                                                color = if (tag == "Bestie") Color(0xFF0D9488) else Color(0xFFD97706),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Text(tag, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. MENU & SERVICES GRID (FEATURING THE DEDICATED STORE!)
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE8DEF8)),
                    shadowElevation = 1.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "⚡ Menu & Services",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1B1433)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // STORE BUTTON (PROMINENTLY HIGHLIGHTED WITH GOLD TRIM)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(14.dp))
                                    .clickable { showLuxuryStoreModal = true }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text("🏪", fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("Store", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFB45309))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // WALLET & TOP-UP BUTTON
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF3E8FF))
                                    .border(1.dp, Color(0xFFD8B4FE), RoundedCornerShape(14.dp))
                                    .clickable { showCoinStoreModal = true }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text("💼", fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B21A8))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // VIP STATUS BUTTON
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFDF2F8))
                                    .border(1.dp, Color(0xFFFBCFE8), RoundedCornerShape(14.dp))
                                    .clickable { showVipStatusDialog = true }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text("👑", fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("VIP Club", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE185D))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // REWARDS BUTTON
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF0FDF4))
                                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp))
                                    .clickable {
                                        Toast.makeText(context, "🎁 Daily Rewards active! Keep chatting to earn diamonds.", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text("🎁", fontSize = 22.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("Rewards", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                    }
                }
            }

        // 5. Action Buttons (Instagram Style: Edit Profile, Share Profile, VIP Club)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit Profile Button (Opens Popup Dialog)
                Button(
                    onClick = {
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
                        isEditing = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCardSubtle),
                    border = BorderStroke(1.dp, BrandBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.3f).height(38.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Edit Profile", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Share Profile Button (Replaces duplicate Settings button with native share intent)
                Button(
                    onClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "LiveTogather Profile: ${currentUser.name}")
                                putExtra(Intent.EXTRA_TEXT, "Connect with ${currentUser.name} on LiveTogather! Join the hangout: https://livetogather.app/profile/${currentUser.uid}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Profile"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCardSubtle),
                    border = BorderStroke(1.dp, BrandBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = BrandGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // VIP Club Pass
                Button(
                    onClick = { showVipStatusDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCardSubtle),
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.1f).height(38.dp)
                ) {
                    Text("👑 VIP Club", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. Professional & Lifestyle Attributes
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BrandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("👑 ABOUT & LIFESTYLE", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)

                    val locPrivacyLabel = when (currentUser.locationPrivacy) {
                        "only_me" -> "🔒 Only Me"
                        "friends" -> "👥 Friends"
                        else -> "🌐 Public"
                    }
                    ProfileAttributeRow(
                        icon = "📍",
                        label = "Location",
                        value = "${currentUser.city.ifBlank { "Kolkata" }} ($locPrivacyLabel)"
                    )
                    ProfileAttributeRow(icon = "💼", label = "Profession", value = profession.ifBlank { "Not specified" })
                    ProfileAttributeRow(icon = "🎓", label = "Education", value = education.ifBlank { "Graduate" })
                    ProfileAttributeRow(icon = "💬", label = "Looking for", value = interestedIn.ifBlank { "Everyone" })
                    ProfileAttributeRow(icon = "🗣️", label = "Languages", value = currentUser.languages.ifBlank { "English, Hindi" })

                    if (hobbies.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("🏷️ Interests & Passions", color = BrandCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            hobbies.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(4).forEach { tag ->
                                Surface(
                                    color = BrandDark,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(0.5.dp, BrandBorder)
                                ) {
                                    Text(
                                        text = tag,
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6b. Settings & Legal Card (Under user details card)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BrandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "⚙️ SETTINGS & LEGAL",
                        color = BrandCoral,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        color = BrandCardSubtle,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, BrandBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPrivacyPolicy() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandCoral.copy(alpha = 0.15f))
                                    .border(1.dp, BrandCoral.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Privacy Policy",
                                    tint = BrandCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Privacy Policy",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "How AYAD Creations protects your data",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Account Security: Set / Change Password
                    Surface(
                        color = BrandCardSubtle,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, BrandBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSetPasswordDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandCyan.copy(alpha = 0.15f))
                                    .border(1.dp, BrandCyan.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔐", fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (currentUser.hasPasswordSet) "Change Login Password" else "Set Login Password",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (currentUser.hasPasswordSet) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = BrandGreen.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                "Active",
                                                color = BrandGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentUser.hasPasswordSet) "Update your login password for email access" else "Set a password to log in with email on any device",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // 1. Coin Store & Top-Up
                    Surface(
                        color = BrandCardSubtle,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, BrandBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCoinStoreModal = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandGold.copy(alpha = 0.15f))
                                    .border(1.dp, BrandGold.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💎", fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Coin Store & Top-Up",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Direct UPI payment with instant UTR verification",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // 2. Help & Support Desk (UTR Submission & Tickets)
                    Surface(
                        color = BrandCardSubtle,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, BrandBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                helpDeskPrefilledPackage = null
                                showHelpDeskModal = true
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BrandCoral.copy(alpha = 0.15f))
                                    .border(1.dp, BrandCoral.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎧", fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Help & Support Desk",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Verify payment UTR, report issues & view ticket status",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // 3. Admin Approval Dashboard (Always accessible & visible in Profile)
                    var pendingPayments by remember { mutableIntStateOf(0) }
                    var pendingReports by remember { mutableIntStateOf(0) }

                    LaunchedEffect(Unit) {
                        db.collection("payment_requests")
                            .addSnapshotListener { snapshot, _ ->
                                if (snapshot != null) {
                                    pendingPayments = snapshot.documents.count { doc ->
                                        val s = doc.getString("status") ?: "PENDING"
                                        s.equals("PENDING", ignoreCase = true)
                                    }
                                }
                            }
                        db.collection("reports")
                            .addSnapshotListener { snapshot, _ ->
                                if (snapshot != null) {
                                    pendingReports = snapshot.documents.count { doc ->
                                        val s = doc.getString("status") ?: "PENDING"
                                        s.equals("PENDING", ignoreCase = true)
                                    }
                                }
                            }
                    }

                    val isOwnerAdmin = currentUser.isOfficialAdmin(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email)
                    if (isOwnerAdmin) {
                        val totalPending = pendingPayments + pendingReports

                        Surface(
                            color = BrandCardSubtle,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, BrandGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAdminDashboardModal = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(BrandGold.copy(alpha = 0.2f))
                                        .border(1.2.dp, BrandGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🛡️", fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Admin Approvals Dashboard",
                                            color = BrandGold,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (totalPending > 0) BrandCoral else BrandGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(0.6.dp, if (totalPending > 0) BrandCoral else BrandGold)
                                        ) {
                                            Text(
                                                text = if (totalPending > 0) "$totalPending PENDING" else "ADMIN",
                                                color = if (totalPending > 0) Color.White else BrandGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (totalPending > 0) {
                                            "🚨 $pendingPayments payments & $pendingReports reports pending"
                                        } else {
                                            "Review & approve pending UPI top-up requests"
                                        },
                                        color = if (totalPending > 0) BrandCoral else Color.LightGray,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = if (totalPending > 0) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = BrandGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
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
                        .clickable { showVipStatusDialog = true },
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
                                        if (currentUser.isVipTrialActive()) "VIP 1 Trial Active ⏳" else "VIP Membership Center",
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
                                            if (currentUser.isVipTrialActive()) "TRIAL (${currentUser.getVipTrialDaysLeft()}D LEFT)" else currentUser.plan.uppercase(),
                                            color = BrandGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    if (currentUser.isVipTrialActive()) "Tap to see real-time countdown timer. 50,000 Diamonds never expire!" else "Tap to view subscription tiers, purchase passes & unlock privileges",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (currentUser.isVipTrialActive()) "Manage VIP & View Countdown ➔" else "Explore Subscription Tiers",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "MY POSTED MOMENTS (${userMoments.size})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onShareMomentClick) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Post Moment", color = BrandCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (userMoments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandCard)
                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                            .clickable { onShareMomentClick() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No moments posted yet", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Tap here to share your first moment! 📸", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                } else {
                    // Instagram 3-Column Square Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        userMoments.chunked(3).forEach { rowMoments ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowMoments.forEach { moment ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, BrandBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = moment.mediaUrl.ifBlank { "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=600&q=80" },
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                                    )
                                                )
                                        )
                                        // Edit & Delete chips on top-right
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.75f))
                                                    .clickable { onEditMoment(moment) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(10.dp))
                                            }
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.75f))
                                                    .clickable { onDeleteMoment(moment) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                        // Likes count at bottom left
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Favorite, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(10.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("${moment.likes}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                repeat(3 - rowMoments.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
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

    if (showVipStatusDialog) {
        VipStatusAndPurchaseDialog(
            currentUser = currentUser,
            onDismiss = { showVipStatusDialog = false },
            onOpenCoinStore = {
                showVipStatusDialog = false
                showCoinStoreModal = true
            },
            onOpenSubscriptionPlans = {
                showVipStatusDialog = false
                showSubscriptionModal = true
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

    if (showLuxuryStoreModal) {
        LuxuryStoreDialog(
            currentUser = currentUser,
            db = db,
            onDismiss = { showLuxuryStoreModal = false },
            onOpenTopUp = { showCoinStoreModal = true },
            onProfileUpdated = { updated -> onUpdateProfile(updated) }
        )
    }

    if (showLuxuryAvatarModal) {
        LuxuryAvatarPickerDialog(
            currentPhotoUrl = photoUrl,
            userLevel = currentUser.level,
            isVip = currentUser.isVipOrPlatinum(),
            onAvatarSelected = { preset ->
                photoUrl = preset.imageUrl
                val updatedProfile = currentUser.copy(uid = currentUserId, photo = preset.imageUrl)
                onUpdateProfile(updatedProfile)
                db.collection("users").document(currentUserId)
                    .set(mapOf("photo" to preset.imageUrl), SetOptions.merge())
                showLuxuryAvatarModal = false
            },
            onRequestCustomUpload = {
                showLuxuryAvatarModal = false
                imagePickerLauncher.launch("image/*")
            },
            onDismiss = { showLuxuryAvatarModal = false }
        )
    }

    if (showVoiceVerificationModal) {
        VoiceVerificationDialog(
            currentUser = currentUser,
            db = db,
            onDismiss = { showVoiceVerificationModal = false },
            onVerified = { updated ->
                onUpdateProfile(updated)
            }
        )
    }

    if (showCoinStoreModal) {
        CoinStoreTopUpDialog(
            currentUser = currentUser,
            onDismiss = { showCoinStoreModal = false },
            onOpenHelpDeskWithPackage = { pack ->
                helpDeskPrefilledPackage = pack
                showHelpDeskModal = true
            }
        )
    }

    if (showHelpDeskModal) {
        HelpDeskModal(
            currentUser = currentUser,
            prefilledPackage = helpDeskPrefilledPackage,
            onDismiss = {
                showHelpDeskModal = false
                helpDeskPrefilledPackage = null
            }
        )
    }

    if (showAdminDashboardModal && currentUser.isOfficialAdmin(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email)) {
        AdminApprovalDashboardModal(
            adminUser = currentUser.copy(isAdmin = true),
            onDismiss = { showAdminDashboardModal = false }
        )
    }

    if (showSetPasswordDialog) {
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isNewPassVisible by remember { mutableStateOf(false) }
        var isConfirmPassVisible by remember { mutableStateOf(false) }
        var isSaving by remember { mutableStateOf(false) }
        val authEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.takeIf { it.isNotBlank() } ?: currentUser.email

        AlertDialog(
            onDismissRequest = { if (!isSaving) showSetPasswordDialog = false },
            containerColor = Color(0xFF1E1724),
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔐", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentUser.hasPasswordSet) "Change Login Password" else "Set Login Password",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (authEmail.isNotBlank())
                            "You can log in to LiveTogather using your email ($authEmail) and this password on any device."
                        else
                            "Create a secure password to log in to LiveTogather anytime.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password (min 6 chars)", color = Color.Gray, fontSize = 12.sp) },
                        visualTransformation = if (isNewPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        trailingIcon = {
                            IconButton(onClick = { isNewPassVisible = !isNewPassVisible }) {
                                Icon(
                                    imageVector = if (isNewPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color.Gray
                                )
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandGold,
                            unfocusedBorderColor = BrandBorder,
                            cursorColor = BrandGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", color = Color.Gray, fontSize = 12.sp) },
                        visualTransformation = if (isConfirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPassVisible = !isConfirmPassVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color.Gray
                                )
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandGold,
                            unfocusedBorderColor = BrandBorder,
                            cursorColor = BrandGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (newPassword.isNotBlank() && newPassword.length < 6) {
                        Text(
                            text = "⚠️ Password must be at least 6 characters",
                            color = BrandCoral,
                            fontSize = 11.sp
                        )
                    } else if (newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                        Text(
                            text = "⚠️ Passwords do not match",
                            color = BrandCoral,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pass = newPassword.trim()
                        val confirm = confirmPassword.trim()
                        if (pass.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (pass != confirm) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        if (firebaseUser != null) {
                            firebaseUser.updatePassword(pass)
                                .addOnSuccessListener {
                                    db.collection("users").document(currentUserId)
                                        .update(mapOf("hasPasswordSet" to true))
                                    val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putBoolean("profile_has_password_set", true).apply()
                                    onUpdateProfile(currentUser.copy(hasPasswordSet = true))
                                    isSaving = false
                                    showSetPasswordDialog = false
                                    Toast.makeText(context, "Login password saved successfully! 🔐", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { err ->
                                    isSaving = false
                                    Toast.makeText(context, "Error: ${err.localizedMessage ?: "Could not update password"}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            isSaving = false
                            Toast.makeText(context, "User session not found. Please log in again.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isSaving && newPassword.length >= 6 && newPassword == confirmPassword,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Save Password", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSetPasswordDialog = false },
                    enabled = !isSaving
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Modal Edit Profile Popup Dialog
    if (isEditing) {
        Dialog(
            onDismissRequest = { isEditing = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            val view = LocalView.current
            SideEffect {
                var parent = view.parent
                while (parent != null && parent !is android.app.Dialog && parent !is DialogWindowProvider) {
                    parent = parent.parent
                }
                (parent as? DialogWindowProvider)?.window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = BrandDark),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BrandBorder),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.88f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✏️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Edit Royal Profile",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isKeyboardOpen) {
                                Button(
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCardSubtle),
                                    border = BorderStroke(1.dp, BrandBorder),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("⌨️ Done", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            IconButton(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    isEditing = false
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }
                    }

                    HorizontalDivider(
                        color = BrandBorder,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Form Fields
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Photo Change Row inside Popup
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandCard)
                                .padding(10.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, BrandCoral, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                if (isUploadingPhoto) {
                                    CircularProgressIndicator(
                                        color = BrandCoral,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Profile Picture", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("High-resolution photo for royal status", color = Color.Gray, fontSize = 10.sp)
                            }
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                enabled = !isUploadingPhoto,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name", color = Color.Gray, fontSize = 11.sp) },
                            colors = outlinedFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text("Age", color = Color.Gray, fontSize = 11.sp) },
                                colors = outlinedFieldColors(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City", color = Color.Gray, fontSize = 11.sp) },
                                colors = outlinedFieldColors(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Manual Location & Area selector shortcut
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandCardSubtle)
                                .border(1.dp, BrandBorder, RoundedCornerShape(8.dp))
                                .clickable { onOpenLocationSetup() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Exact Real Location & Area", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Select city, neighborhood & coordinates", color = Color.Gray, fontSize = 9.sp)
                                }
                            }
                            Text("Change ✏️", color = BrandCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = profession,
                            onValueChange = { profession = it },
                            label = { Text("Profession", color = Color.Gray, fontSize = 11.sp) },
                            colors = outlinedFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = education,
                            onValueChange = { education = it },
                            label = { Text("Education", color = Color.Gray, fontSize = 11.sp) },
                            colors = outlinedFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = hobbies,
                            onValueChange = { hobbies = it },
                            label = { Text("Interests & Hobbies (comma-separated)", color = Color.Gray, fontSize = 11.sp) },
                            colors = outlinedFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio / Status Quote", color = Color.Gray, fontSize = 11.sp) },
                            colors = outlinedFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            })
                        )

                        Text("Your Gender", color = BrandCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                            else Brush.linearGradient(listOf(BrandCard, BrandCard))
                                        )
                                        .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                                        .clickable {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            gender = optVal
                                        }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(opt, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text("Interested In", color = BrandCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                            else Brush.linearGradient(listOf(BrandCard, BrandCard))
                                        )
                                        .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                                        .clickable {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            interestedIn = optVal
                                        }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(opt, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (isKeyboardOpen) {
                            OutlinedButton(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGold),
                                border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text("⌨️ Close Keyboard To Review & Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Buttons: Cancel and Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
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
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                            border = BorderStroke(1.dp, Color.Gray),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                val parsedAge = age.toIntOrNull() ?: currentUser.age
                                val rawPhoto = photoUrl.ifEmpty { currentUser.photo }
                                val safePhoto = if (rawPhoto.startsWith("file://") || rawPhoto.startsWith("content://")) {
                                    try {
                                        ImageUploadManager.convertImageToFirestoreDataUri(context, Uri.parse(rawPhoto)) ?: rawPhoto
                                    } catch (e: Throwable) {
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
                                onUpdateProfile(updated)
                                isEditing = false
                                Toast.makeText(context, "Profile updated successfully! ✅", Toast.LENGTH_SHORT).show()
                                db.collection("users").document(currentUserId)
                                    .set(updated, SetOptions.merge())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Changes", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(
    count: String,
    label: String,
    highlightColor: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = highlightColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProfileAttributeRow(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = Color.LightGray, fontSize = 12.sp)
        }
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MutualMatchCelebrationDialog(
    currentUser: UserProfile,
    partner: UserProfile,
    onChat: () -> Unit,
    onDismiss: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "heartPulse")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(28.dp),
                color = BrandDark,
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(BrandCoral, BrandGold, BrandOrange)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Celebration Headline
                    Text(
                        text = "IT'S A MATCH! 🎉",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandGold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "You and ${partner.name} liked each other!",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Animated Connected Hearts Arena
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val travel = (1f - progress.value) * 120f

                        // Left Heart moving right
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = BrandCoral,
                            modifier = Modifier
                                .size(50.dp)
                                .offset(x = (-travel).dp, y = (-20).dp)
                                .graphicsLayer {
                                    alpha = if (progress.value < 0.8f) 0.9f else (1f - (progress.value - 0.8f) * 5f).coerceAtLeast(0f)
                                }
                        )

                        // Right Heart moving left
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = BrandOrange,
                            modifier = Modifier
                                .size(50.dp)
                                .offset(x = travel.dp, y = (-20).dp)
                                .graphicsLayer {
                                    alpha = if (progress.value < 0.8f) 0.9f else (1f - (progress.value - 0.8f) * 5f).coerceAtLeast(0f)
                                }
                        )

                        // Connected Pulsing Fusion Heart in the Center (Appears once hearts merge)
                        if (progress.value >= 0.7f) {
                            val mergeAlpha = ((progress.value - 0.7f) / 0.3f).coerceIn(0f, 1f)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = heartScale
                                        scaleY = heartScale
                                        alpha = mergeAlpha
                                    }
                            ) {
                                // Halo glow
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(BrandCoral.copy(alpha = 0.45f), Color.Transparent)
                                            )
                                        )
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Connected Hearts",
                                        tint = BrandCoral,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.width(-16.dp))
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Connected Hearts",
                                        tint = BrandGold,
                                        modifier = Modifier.size(46.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Connected Avatars with Linking Bridge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = currentUser.photo,
                                contentDescription = currentUser.name,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, BrandCoral, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Connecting Beam / Heart Chain
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "❤️ Connected ❤️",
                                color = BrandGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(2.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(BrandCoral, BrandGold, BrandOrange)
                                        )
                                    )
                            )
                        }

                        // Partner Avatar
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = partner.photo,
                                contentDescription = partner.name,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, BrandOrange, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // Send Instant Message Button
                    Button(
                        onClick = onChat,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Instant Message 💬",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Keep Exploring TextButton
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Keep Swiping 🔥",
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionPlansModal(
    currentPlan: String,
    customNotice: String? = null,
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

                if (!customNotice.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = BrandCoral.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandCoral.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = customNotice,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
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
// LOCATION SETUP & ONBOARDING DIALOG
// -------------------------------------------------------------
@Composable
fun LocationSetupDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onSaveLocation: (UserProfile) -> Unit
) {
    var selectedCity by remember { mutableStateOf(currentUser.city.ifBlank { "Kolkata" }) }
    var locationArea by remember { mutableStateOf(currentUser.locationName.ifBlank { "New Town" }) }
    var currentLat by remember { mutableDoubleStateOf(currentUser.latitude) }
    var currentLon by remember { mutableDoubleStateOf(currentUser.longitude) }
    var isDetectingLocation by remember { mutableStateOf(false) }
    var locationPrivacy by remember { mutableStateOf(currentUser.locationPrivacy.ifBlank { "public" }) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen = WindowInsets.isImeVisible

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            isDetectingLocation = true
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val loc: Location? = if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } else {
                    locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if (loc != null) {
                    currentLat = loc.latitude
                    currentLon = loc.longitude
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Local Area"
                            val subLocality = addr.subLocality ?: addr.featureName ?: locality
                            selectedCity = locality
                            locationArea = "$subLocality, $locality"
                        }
                    } catch (e: Exception) {
                        Log.w("LocationSetup", "Geocoder error: ${e.message}")
                    }
                    Toast.makeText(context, "📍 Real GPS Location Detected!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Location signal acquired. Selected $selectedCity.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("LocationSetup", "Location detection error: ${e.message}")
                Toast.makeText(context, "Using $selectedCity coordinates.", Toast.LENGTH_SHORT).show()
            } finally {
                isDetectingLocation = false
            }
        } else {
            Toast.makeText(context, "Location permission not granted. You can pick your city below.", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
                focusManager.clearFocus()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandDark),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BrandBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(if (isKeyboardOpen) 0.95f else 0.85f)
                .imePadding()
                .navigationBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Real Location & Area",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Required for real-time dating & distance matching",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isKeyboardOpen) {
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCardSubtle),
                                border = BorderStroke(1.dp, BrandBorder),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("⌨️ Done", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        IconButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onDismiss()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }
                }

                HorizontalDivider(color = BrandBorder, modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Auto GPS Detection Button
                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCardSubtle),
                        border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        if (isDetectingLocation) {
                            CircularProgressIndicator(color = BrandOrange, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detecting Real GPS...", color = Color.White, fontSize = 12.sp)
                        } else {
                            Text("📍 Auto-Detect My Live Location (GPS)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Selected City", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    // City selector pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CITY_COORDINATES.keys.toList()) { city ->
                            val isSelected = selectedCity.equals(city, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                        else Brush.linearGradient(listOf(BrandCard, BrandCard))
                                    )
                                    .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                                    .clickable {
                                        selectedCity = city
                                        val coords = CITY_COORDINATES[city] ?: Pair(22.5726, 88.3639)
                                        currentLat = coords.first
                                        currentLon = coords.second
                                        val popularAreas = POPULAR_AREAS_BY_CITY[city]
                                        if (!popularAreas.isNullOrEmpty()) {
                                            locationArea = "${popularAreas.first()}, $city"
                                        } else {
                                            locationArea = city
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = city,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Popular Neighborhoods / Areas
                    val areas = POPULAR_AREAS_BY_CITY[selectedCity] ?: listOf("Central", "North", "South", "East", "West")
                    Text("Select Area in $selectedCity", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        areas.forEach { area ->
                            val isSelected = locationArea.contains(area, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandGold.copy(alpha = 0.2f) else BrandCardSubtle)
                                    .border(1.dp, if (isSelected) BrandGold else BrandBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        locationArea = "$area, $selectedCity"
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = area,
                                    color = if (isSelected) BrandGold else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Specific Neighborhood / Street input
                    OutlinedTextField(
                        value = locationArea,
                        onValueChange = { locationArea = it },
                        label = { Text("Exact Area / Neighborhood", color = Color.Gray, fontSize = 11.sp) },
                        placeholder = { Text("e.g. New Town Action Area 1", color = Color.DarkGray, fontSize = 11.sp) },
                        colors = outlinedFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Current Coordinates summary badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandCardSubtle)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Real-Time Coordinates for Distance Calculation", color = Color.LightGray, fontSize = 10.sp)
                                Text(
                                    "Lat: ${String.format(Locale.US, "%.4f", currentLat)}, Lon: ${String.format(Locale.US, "%.4f", currentLon)}",
                                    color = BrandGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Location Privacy Selector (Public, Friends, Only Me)
                    Text("🔒 Location Privacy", color = BrandGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val privacyOptions = listOf(
                            Triple("public", "🌐 Public", "Everyone"),
                            Triple("friends", "👥 Friends", "Only friends"),
                            Triple("only_me", "🔒 Only Me", "Private")
                        )
                        privacyOptions.forEach { (key, label, desc) ->
                            val isSel = locationPrivacy == key
                            Surface(
                                color = if (isSel) Color(0xFF7E22CE) else BrandCardSubtle,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isSel) BrandGold else BrandBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { locationPrivacy = key }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSel) Color.White else Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = desc,
                                        color = if (isSel) Color(0xFFFDE68A) else Color.Gray,
                                        fontSize = 8.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        val updated = currentUser.copy(
                            city = selectedCity.trim().ifBlank { "Kolkata" },
                            locationName = locationArea.trim().ifBlank { selectedCity },
                            latitude = if (currentLat != 0.0) currentLat else (CITY_COORDINATES[selectedCity]?.first ?: 22.5726),
                            longitude = if (currentLon != 0.0) currentLon else (CITY_COORDINATES[selectedCity]?.second ?: 88.3639),
                            isLocationSet = true,
                            locationPrivacy = locationPrivacy
                        )
                        onSaveLocation(updated)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm & Set Location", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
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
    onSave: (UserProfile) -> Unit,
    onOpenLocationSetup: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {}
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
    var locationPrivacy by remember { mutableStateOf(user.locationPrivacy.ifBlank { "public" }) }

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

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
                focusManager.clearFocus()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            containerColor = Color(0xFFF5F5F7),
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
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
                                socialMedia = socialMedia,
                                locationPrivacy = locationPrivacy
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenLocationSetup() },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Location & Area", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Tap to change real GPS location & area.", color = Color.Gray, fontSize = 11.sp)
                                }
                                Text("📍 $locationName ✏️", color = BrandCoral, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Location Privacy Controls (Public / Friends / Only Me)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Location Privacy", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Who can see your location", color = Color.Gray, fontSize = 10.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(
                                        "public" to "🌐 Public",
                                        "friends" to "👥 Friends",
                                        "only_me" to "🔒 Only Me"
                                    ).forEach { (key, lbl) ->
                                        val isSel = locationPrivacy == key
                                        Surface(
                                            color = if (isSel) Color(0xFF7E22CE) else Color(0xFFF3F4F6),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.clickable { locationPrivacy = key }
                                        ) {
                                            Text(
                                                text = lbl,
                                                color = if (isSel) Color.White else Color(0xFF4B5563),
                                                fontSize = 10.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
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

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Legal & Policies", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        onOpenPrivacyPolicy()
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = BrandCoral,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Privacy Policy",
                                            color = Color.Black,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "How AYAD Creations protects your data",
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
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
    onOpenRoom: ((String) -> Unit)? = null,
    onOpenAdmin: ((Int) -> Unit)? = null,
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
                        Text("Wipe All 🗑️", color = BrandCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (notif.type.contains("report")) {
                                            onOpenAdmin?.invoke(1)
                                            onDismiss()
                                        } else if (notif.type.contains("payment") || notif.type.contains("utr") || notif.type.contains("pay_req")) {
                                            onOpenAdmin?.invoke(0)
                                            onDismiss()
                                        } else if (notif.roomId.isNotBlank()) {
                                            onOpenRoom?.invoke(notif.roomId)
                                            onDismiss()
                                        }
                                    },
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
    onPost: (List<String>, String) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    val pickedMediaUrls = remember { mutableStateListOf<String>() }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val multiMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isUploading = true
            uploadStatusText = "Uploading 1/${uris.size}..."
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val batch = uris.take(6)
                    val newUrls = mutableListOf<String>()
                    batch.forEachIndexed { index, uri ->
                        withContext(Dispatchers.Main) {
                            uploadStatusText = "Uploading ${index + 1}/${batch.size}..."
                        }
                        val uploadedUrl = ImageUploadManager.uploadImage(context, uri, "moments")
                        if (!uploadedUrl.isNullOrBlank()) {
                            newUrls.add(uploadedUrl)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (newUrls.isNotEmpty()) {
                            pickedMediaUrls.addAll(newUrls)
                            Toast.makeText(context, "${newUrls.size} photos ready to share! 📸", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Photo upload failed. Try another photo.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Throwable) {
                    Log.e("Moments", "Image upload failed: ${e.message}", e)
                } finally {
                    withContext(Dispatchers.Main) {
                        isUploading = false
                        uploadStatusText = ""
                    }
                }
            }
        }
    }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val canPost = !isUploading && (pickedMediaUrls.isNotEmpty() || caption.isNotBlank())

    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
                focusManager.clearFocus()
            } else if (!isUploading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isUploading && !isKeyboardOpen,
            dismissOnClickOutside = !isUploading
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Share a Moment", color = BrandCoral, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (pickedMediaUrls.isNotEmpty()) {
                        Text("${pickedMediaUrls.size} Photo${if (pickedMediaUrls.size > 1) "s" else ""}", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("What's on your mind? (Optional or type message...)", color = Color.Gray, fontSize = 12.sp) },
                    colors = outlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (pickedMediaUrls.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BrandCardSubtle)
                            .border(1.dp, BrandBorder, RoundedCornerShape(14.dp))
                            .clickable(enabled = !isUploading) {
                                multiMediaPickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = BrandCoral,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                                Text(uploadStatusText.ifEmpty { "Uploading..." }, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Select Photos (Multiple supported)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Choose from Gallery / Camera", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                } else {
                    Column {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        ) {
                            items(pickedMediaUrls) { url ->
                                Box(
                                    modifier = Modifier
                                        .size(130.dp, 140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.75f))
                                            .clickable(enabled = !isUploading) {
                                                pickedMediaUrls.remove(url)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            if (pickedMediaUrls.size < 6) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp, 140.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BrandCardSubtle)
                                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                                            .clickable(enabled = !isUploading) {
                                                multiMediaPickerLauncher.launch("image/*")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("+ Add More", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        if (isUploading) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = BrandCoral, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(uploadStatusText.ifEmpty { "Uploading photos..." }, color = BrandGold, fontSize = 11.sp)
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
                            if (canPost) {
                                val finalCaption = caption.trim().ifEmpty { "✨ Shared a moment" }
                                val finalUrls = if (pickedMediaUrls.isNotEmpty()) pickedMediaUrls.toList()
                                    else listOf("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=600&q=80")
                                onPost(finalUrls, finalCaption)
                            }
                        },
                        enabled = canPost,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandCoral,
                            contentColor = Color.White,
                            disabledContainerColor = BrandCoral.copy(alpha = 0.35f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isUploading) "Uploading..." else "Post Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// EDIT MOMENT DIALOG
// -------------------------------------------------------------
@Composable
fun EditMomentDialog(
    moment: MomentItem,
    onDismiss: () -> Unit,
    onSave: (String, List<String>) -> Unit
) {
    var caption by remember { mutableStateOf(moment.caption) }
    val initialUrls = if (moment.mediaUrls.isNotEmpty()) moment.mediaUrls
        else if (moment.mediaUrl.isNotBlank()) listOf(moment.mediaUrl)
        else emptyList()
    val pickedMediaUrls = remember { mutableStateListOf<String>().apply { addAll(initialUrls) } }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val multiMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isUploading = true
            uploadStatusText = "Uploading 1/${uris.size}..."
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val remainingSlots = (6 - pickedMediaUrls.size).coerceAtLeast(0)
                    val batch = uris.take(remainingSlots)
                    val newUrls = mutableListOf<String>()
                    batch.forEachIndexed { index, uri ->
                        withContext(Dispatchers.Main) {
                            uploadStatusText = "Uploading ${index + 1}/${batch.size}..."
                        }
                        val uploadedUrl = ImageUploadManager.uploadImage(context, uri, "moments")
                        if (!uploadedUrl.isNullOrBlank()) {
                            newUrls.add(uploadedUrl)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (newUrls.isNotEmpty()) {
                            pickedMediaUrls.addAll(newUrls)
                            Toast.makeText(context, "${newUrls.size} photos added! 📸", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Photo upload failed. Try another photo.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Throwable) {
                    Log.e("Moments", "Image edit upload failed: ${e.message}", e)
                } finally {
                    withContext(Dispatchers.Main) {
                        isUploading = false
                        uploadStatusText = ""
                    }
                }
            }
        }
    }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
                focusManager.clearFocus()
            } else if (!isUploading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isUploading,
            dismissOnClickOutside = !isUploading
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✏️ Edit Moment", color = BrandCoral, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("Update caption...", color = Color.Gray, fontSize = 12.sp) },
                    colors = outlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (pickedMediaUrls.isNotEmpty()) {
                    Column {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        ) {
                            items(pickedMediaUrls) { url ->
                                Box(
                                    modifier = Modifier
                                        .size(130.dp, 140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.75f))
                                            .clickable(enabled = !isUploading) {
                                                pickedMediaUrls.remove(url)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            if (pickedMediaUrls.size < 6) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp, 140.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BrandCardSubtle)
                                            .border(1.dp, BrandBorder, RoundedCornerShape(12.dp))
                                            .clickable(enabled = !isUploading) {
                                                multiMediaPickerLauncher.launch("image/*")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("+ Add More", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        if (isUploading) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = BrandCoral, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(uploadStatusText.ifEmpty { "Uploading photos..." }, color = BrandGold, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BrandCardSubtle)
                            .border(1.dp, BrandBorder, RoundedCornerShape(14.dp))
                            .clickable(enabled = !isUploading) {
                                multiMediaPickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = BrandCoral,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp
                                )
                                Text(uploadStatusText.ifEmpty { "Uploading..." }, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Add Photos", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isUploading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, BrandBorder)
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            if (!isUploading) {
                                onSave(caption.trim(), pickedMediaUrls.toList())
                            }
                        },
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandCoral,
                            contentColor = Color.White,
                            disabledContainerColor = BrandCoral.copy(alpha = 0.35f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(if (isUploading) "Saving..." else "Save Changes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
    isUnlocked: Boolean = true,
    currentUserDiamonds: Long = 0L,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    onUnlockAndSend: ((String) -> Unit)? = null
) {
    var messageText by remember { mutableStateOf("") }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
                focusManager.clearFocus()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, if (!isUnlocked) BrandGold else BrandBorder)
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
                        Text(
                            text = if (!isUnlocked) "🎁 5,000 💎 Gift Required to Unlock" else "Send a direct note",
                            color = if (!isUnlocked) BrandGold else BrandOrange,
                            fontSize = 11.sp,
                            fontWeight = if (!isUnlocked) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                if (!isUnlocked) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandGold.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🎁 Direct Chat Initiation Gift",
                                color = BrandGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Unlocking a direct chat requires gifting 5,000 💎 Diamonds to ${targetUser.name}.\nYour balance: %,d 💎".format(currentUserDiamonds),
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
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
                                if (!isUnlocked && onUnlockAndSend != null) {
                                    onUnlockAndSend(messageText.trim())
                                } else {
                                    onSend(messageText.trim())
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isUnlocked) BrandGold else BrandCoral),
                        modifier = Modifier.weight(if (!isUnlocked) 1.6f else 1f)
                    ) {
                        Text(
                            text = if (!isUnlocked) "Unlock & Send 💎" else "Send",
                            fontWeight = FontWeight.Bold,
                            color = if (!isUnlocked) BrandDark else Color.White
                        )
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

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
                focusManager.clearFocus()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
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
                                    model = comment.userPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80" },
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
                                    userName = currentUser.name.ifBlank { "You" },
                                    userPhoto = currentUser.photo,
                                    text = cText
                                )
                                comments.add(newComment)
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
    val isDummy = partner.uid.startsWith("dummy_")
    if (isDummy) {
        val matchId = listOf(currentUser.uid, partner.uid).sorted().joinToString("_")
        val match = MatchItem(
            matchId = matchId,
            users = listOf(currentUser.uid, partner.uid),
            matchedIntent = currentUser.activeIntent,
            lastMessage = "It's a Match! Say hello."
        )
        db.collection("matches").document(matchId).set(match)
        onMatch()
        return
    }

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

fun sendHostLiveNotification(
    db: FirebaseFirestore,
    hostId: String,
    hostName: String,
    hostPhoto: String,
    room: RoomItem
) {
    if (hostId.isBlank()) return
    db.collection("users").document(hostId).collection("followers")
        .get()
        .addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                val followerUid = doc.id
                if (followerUid.isNotBlank() && followerUid != hostId) {
                    val notif = NotificationItem(
                        id = UUID.randomUUID().toString(),
                        targetUserId = followerUid,
                        senderId = hostId,
                        senderName = hostName,
                        senderPhoto = hostPhoto,
                        type = "LIVE",
                        title = "🔴 $hostName is LIVE!",
                        message = "Tap to join '${room.title.ifBlank { room.roomName }}' (Room ID: ${room.roomId})",
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        roomId = room.id
                    )
                    db.collection("notifications").document(notif.id).set(notif)
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e("MainActivity", "Failed to query followers for live notification: ${e.message}")
        }
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

fun silentlyUpdateGpsLocation(
    context: Context,
    userId: String,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    prefs: android.content.SharedPreferences,
    onProfileUpdated: (UserProfile) -> Unit
) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return@launch
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!hasFine && !hasCoarse) return@launch

            val loc: Location? = if (hasFine) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            if (loc != null) {
                processAndPersistGpsLocation(context, loc, userId, currentUser, db, prefs, onProfileUpdated)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val provider = if (hasFine) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
                    locationManager.getCurrentLocation(provider, null, context.mainExecutor) { freshLoc ->
                        if (freshLoc != null) {
                            processAndPersistGpsLocation(context, freshLoc, userId, currentUser, db, prefs, onProfileUpdated)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("MainActivity", "getCurrentLocation fallback notice: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w("MainActivity", "Silent background GPS error: ${e.message}")
        }
    }
}

private fun processAndPersistGpsLocation(
    context: Context,
    loc: Location,
    userId: String,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    prefs: android.content.SharedPreferences,
    onProfileUpdated: (UserProfile) -> Unit
) {
    var detectedCity = currentUser.city
    var detectedArea = currentUser.locationName

    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
            val subLocality = addr.subLocality ?: addr.featureName ?: locality
            if (!locality.isNullOrBlank()) {
                detectedCity = locality
                detectedArea = if (!subLocality.isNullOrBlank() && subLocality != locality) {
                    "$subLocality, $locality"
                } else {
                    locality
                }
            }
        }
    } catch (e: Exception) {
        Log.w("MainActivity", "Geocoder background notice: ${e.message}")
    }

    val updatedUser = currentUser.copy(
        latitude = loc.latitude,
        longitude = loc.longitude,
        city = detectedCity.ifBlank { "Kolkata" },
        locationName = detectedArea.ifBlank { detectedCity.ifBlank { "Kolkata" } },
        isLocationSet = true
    )

    kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
        onProfileUpdated(updatedUser)
        saveProfileToPrefs(prefs, updatedUser)
    }

    if (userId.isNotBlank()) {
        db.collection("users").document(userId).set(
            mapOf(
                "latitude" to loc.latitude,
                "longitude" to loc.longitude,
                "city" to updatedUser.city,
                "locationName" to updatedUser.locationName,
                "isLocationSet" to true
            ),
            SetOptions.merge()
        )
    }
}