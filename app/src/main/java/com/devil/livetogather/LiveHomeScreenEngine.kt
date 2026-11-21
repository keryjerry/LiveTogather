package com.devil.livetogather

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

// -------------------------------------------------------------
// LIVE TOGETHER INTERACTIVE HOME SCREEN (MATCHING HOME_SCREEN_MOCKUP.HTML)
// -------------------------------------------------------------

@Composable
fun LiveTogetherHomeScreen(
    allUsers: List<UserProfile>,
    rooms: List<RoomItem>,
    currentUser: UserProfile,
    moments: List<MomentItem> = emptyList(),
    unreadNotificationsCount: Int = 0,
    onCapsuleExpandChange: (Boolean) -> Unit = {},
    onJoinRoom: (RoomItem) -> Unit,
    onCreateRoom: () -> Unit,
    onOpenCategoryHub: (IntentCategory) -> Unit,
    onSelectUserProfile: (UserProfile) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onPostMoment: () -> Unit = {},
    onLikeMoment: (MomentItem) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Active Game Selection Dialog state (Ludo, Carrom, Quiz)
    var selectedGameForRoomList by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    // Dual-Mode Search Dialog State (Room ID vs User ID)
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchMode by remember { mutableStateOf("room") }
    var searchQuery by remember { mutableStateOf("") }

    var previousIndex by remember { mutableIntStateOf(listState.firstVisibleItemIndex) }
    var previousOffset by remember { mutableIntStateOf(listState.firstVisibleItemScrollOffset) }

    // Auto expand/shrink capsule during scrolling (Responsive scroll-direction detection)
    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset, listState.isScrollInProgress)
        }.collect { (currentIndex, currentOffset, isScrolling) ->
            if (currentIndex == 0 && currentOffset < 60) {
                // At top of home: full expand
                onCapsuleExpandChange(true)
            } else if (isScrolling) {
                // Scrolling down -> Shrink capsule (collapse to mini pill)
                if (currentIndex > previousIndex || (currentIndex == previousIndex && currentOffset > previousOffset + 12)) {
                    onCapsuleExpandChange(false)
                }
                // Scrolling up -> Expand capsule
                else if (currentIndex < previousIndex || (currentIndex == previousIndex && currentOffset < previousOffset - 12)) {
                    onCapsuleExpandChange(true)
                }
            }
            previousIndex = currentIndex
            previousOffset = currentOffset
        }
    }

    // STRICT USER RULE: Only Voice Party rooms can host games!
    val roomsSnapshotKey = rooms.map {
        "${it.id}_${it.isHostLive}_${it.isLive}_${it.statusMessage}_${it.activeUserCount}_${it.category}_${it.roomType}"
    }
    val voiceRooms = remember(roomsSnapshotKey) {
        rooms.filter {
            !it.statusMessage.equals("DELETED", ignoreCase = true) &&
            it.category != "video_stream" && it.roomType != "video" && it.roomType != "video_pk" && !it.isVideoRoom
        }
    }
    val videoRooms = remember(roomsSnapshotKey) {
        rooms.filter {
            !it.statusMessage.equals("DELETED", ignoreCase = true) &&
            (it.category == "video_stream" || it.roomType == "video" || it.roomType == "video_pk" || it.isVideoRoom)
        }
    }

    // STRICT REAL-TIME COUNTERS (100% FIREBASE LIVE SYNC, NO DUMMY NUMBERS)
    val ghostTimeoutMs = 10 * 60 * 1000L // 10 minutes without host/admin heartbeat = ghost room
    LaunchedEffect(roomsSnapshotKey) {
        val now = System.currentTimeMillis()
        rooms.forEach { r ->
            val markedLive = r.isHostLive || r.isLive
            val notClosed = !r.statusMessage.equals("OFFLINE", ignoreCase = true) &&
                !r.statusMessage.equals("CLOSED", ignoreCase = true) &&
                !r.statusMessage.equals("DELETED", ignoreCase = true)
            val isStaleHeartbeat = r.lastHostHeartbeat > 0L && (now - r.lastHostHeartbeat > ghostTimeoutMs)
            if (markedLive && notClosed && isStaleHeartbeat && r.id.isNotBlank()) {
                val closeMap = mapOf<String, Any>(
                    "isLive" to false,
                    "isHostLive" to false,
                    "isEnded" to true,
                    "endedAt" to now,
                    "activeUserCount" to 0,
                    "isPlaying" to false,
                    "watchPartyIsPlaying" to false,
                    "watchPartyVideoId" to "",
                    "watchPartyVideoTitle" to "",
                    "watchPartyPositionSec" to 0L,
                    "statusMessage" to "Room is closed"
                )
                val fs = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                fs.collection("rooms").document(r.id).update(closeMap)
                if (r.category.isNotBlank()) {
                    fs.collection("intent_categories").document(r.category)
                        .collection("rooms").document(r.id).update(closeMap)
                }
            }
        }
    }

    val liveVoiceRooms = remember(roomsSnapshotKey) {
        val now = System.currentTimeMillis()
        rooms.filter {
            (it.isHostLive || it.isLive) &&
            !it.statusMessage.equals("OFFLINE", ignoreCase = true) &&
            !it.statusMessage.equals("CLOSED", ignoreCase = true) &&
            !it.statusMessage.equals("DELETED", ignoreCase = true) &&
            !(it.lastHostHeartbeat > 0L && (now - it.lastHostHeartbeat > ghostTimeoutMs)) &&
            it.category != "video_stream" && it.roomType != "video" && it.roomType != "video_pk" && !it.isVideoRoom
        }
    }
    val liveVideoRooms = remember(roomsSnapshotKey) {
        val now = System.currentTimeMillis()
        rooms.filter {
            (it.isHostLive || it.isLive) &&
            !it.statusMessage.equals("OFFLINE", ignoreCase = true) &&
            !it.statusMessage.equals("CLOSED", ignoreCase = true) &&
            !it.statusMessage.equals("DELETED", ignoreCase = true) &&
            !(it.lastHostHeartbeat > 0L && (now - it.lastHostHeartbeat > ghostTimeoutMs)) &&
            (it.category == "video_stream" || it.roomType == "video" || it.roomType == "video_pk" || it.isVideoRoom)
        }
    }
    val realTimeRoomsCount = liveVoiceRooms.size + liveVideoRooms.size
    val usersSnapshotKey = allUsers.map { "${it.uid}_${it.isUserOnline()}_${it.current_room}" }
    val realTimeUsersCount = remember(usersSnapshotKey, realTimeRoomsCount) {
        val onlineCount = allUsers.count { it.isUserOnline() }
        if (onlineCount > 0) onlineCount else if (realTimeRoomsCount > 0) 1 else 0
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF070712))) {
        // Clean minimalist midnight-indigo & aurora luxury background (100% edge-to-edge)
        Image(
            painter = painterResource(id = R.drawable.home_clean_luxury_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x28070712),
                            Color(0x140A0714),
                            Color(0x38070712),
                            Color(0x66070712)
                        )
                    )
                )
        )
        
        // MAIN SCROLLABLE CONTENT (SPANS 100% EDGE-TO-EDGE FROM THE VERY TOP UNDER STATUS BAR)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. TOP FLOATING BUBBLES STAGE (Placed directly inside top item so every rising bubble is 100% clickable!)
            item {
                FreeFloatingAvatarStage(
                    profiles = allUsers,
                    rooms = rooms,
                    onSelectUserProfile = onSelectUserProfile,
                    onJoinRoom = onJoinRoom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }

            // 1.5 SINGLE EVENT HUB BANNER (12 PM - 6 AM OPERATIONAL, RUNNING TOP-RIGHT HOST TICKER)
            if (isEventHubVisibleNow()) {
                item {
                    EventHubSingleHomeBanner(
                        allUsers = allUsers,
                        rooms = rooms,
                        currentUser = currentUser,
                        onEnterHub = {
                            val eventCat = IntentCategory("official_event", "🏆 Official Event Hub")
                            onOpenCategoryHub(eventCat)
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // 2. LARGE PARTY BOXES: VOICE PARTY & VIDEO STREAMING (WITH REAL PARTY THUMBNAIL PREVIEWS & EQUALIZERS)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "PARTY ROOMS",
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "🔴 $realTimeRoomsCount Live",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 2A. VOICE PARTY BOX (WITH PARTY THEME BACKGROUND PREVIEW)
                    VoicePartyBigBox(
                        modifier = Modifier.weight(1f),
                        activeRoomsCount = liveVoiceRooms.size,
                        onClick = {
                            val voicePartyCat = IntentCategory("party_rooms", "🎉 Party Rooms")
                            onOpenCategoryHub(voicePartyCat)
                        }
                    )

                    // 2B. VIDEO STREAMING BOX (WITH PARTY STREAM VIDEO PREVIEW)
                    VideoStreamingBigBox(
                        modifier = Modifier.weight(1f),
                        activeStreamsCount = liveVideoRooms.size,
                        onClick = {
                            val videoCat = CATEGORY_LIST.firstOrNull { it.id == "video_stream" }
                                ?: IntentCategory("video_stream", "📹 Video Streaming")
                            onOpenCategoryHub(videoCat)
                        }
                    )
                }
            }

            // 3. MIDDLE SECTION: SMALLER GRIDS (BLIND DATE & TRUTH/DARE)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Blind Date Tile
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E2238))
                            .border(1.dp, Color(0xFFEC4899).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .clickable {
                                val cat = CATEGORY_LIST.firstOrNull { it.id == "free_tonight" } ?: CATEGORY_LIST[0]
                                onOpenCategoryHub(cat)
                            }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEC4899).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎭", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Blind Date", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("1-on-1 Speed Match", color = Color(0xFFF472B6), fontSize = 9.sp)
                            }
                        }
                    }

                    // Truth & Dare Tile
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E2238))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .clickable {
                                val cat = CATEGORY_LIST.firstOrNull { it.id == "free_party" } ?: CATEGORY_LIST[0]
                                onOpenCategoryHub(cat)
                            }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🍾", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Truth & Dare", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Spin & Challenge", color = Color(0xFFFBBF24), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // 4. BOTTOM SECTION: PLAY TOGETHER (CLICK OPENS ROOMS LIST MODAL / EMPTY STATE)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PLAY TOGETHER (IN-ROOM MATCHES)",
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text("3 Games Ready", color = Color(0xFF818CF8), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val games = listOf(
                        Triple("🎲", "Ludo Club", "4-Player 15x15 Real-time Board • Voice Party"),
                        Triple("🎯", "Carrom Clash", "Proportional Physics & Teakwood Sound • Voice Party"),
                        Triple("🧠", "Quiz Battle", "Live Buzzer • 4-Seat Multi-Trivia Arena • Voice Party")
                    )

                    games.forEach { (emoji, title, desc) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1E2238), Color(0xFF111424))
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                .clickable {
                                    // OPENS GAME ROOMS LIST MODAL AS REQUESTED BY USER!
                                    selectedGameForRoomList = Triple(emoji, title, desc)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF312E81)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        Text(desc, color = Color(0xFF94A3B8), fontSize = 9.5.sp)
                                    }
                                }
                                Button(
                                    onClick = {
                                        selectedGameForRoomList = Triple(emoji, title, desc)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("View Rooms", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 5. INTENT CATEGORIES SECTION
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(
                        "EXPLORE INTENT HUBS",
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CATEGORY_LIST.filter { it.id != "all" }) { category ->
                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .clickable { onOpenCategoryHub(category) }
                            ) {
                                AsyncImage(
                                    model = getCategoryBanner(category.id),
                                    contentDescription = category.label,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("✨ Hub", color = BrandGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text(
                                            category.label,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        CategoryEqualizerThemeVisualizer(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            isLive = true,
                                            isPlaying = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. SECTION 4 FROM MOCKUP: USER COMMUNITY MOMENTS FEED (FULL SOCIAL FEED!)
            item {
                CommunityMomentsFeedSection(
                    currentUser = currentUser,
                    moments = moments,
                    onPostClick = onPostMoment,
                    onLikeMoment = onLikeMoment,
                    onSelectProfile = onSelectUserProfile
                )
            }

            // Footer caught up marker
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✨ You're all caught up with recent moments! ✨",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // DUAL-MODE SEARCH MODAL DIALOG (ROOM ID vs USER ID)
        if (showSearchDialog) {
            val filteredRooms = remember(rooms, searchQuery) {
                if (searchQuery.isBlank()) rooms.take(20)
                else rooms.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.id.contains(searchQuery, ignoreCase = true) ||
                    it.hostName.contains(searchQuery, ignoreCase = true)
                }
            }
            val filteredUsers = remember(allUsers, searchQuery) {
                if (searchQuery.isBlank()) allUsers.take(20)
                else allUsers.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.uid.contains(searchQuery, ignoreCase = true) ||
                    it.bio.contains(searchQuery, ignoreCase = true)
                }
            }
            DualModeSearchDialog(
                searchMode = searchMode,
                onSearchModeChange = { searchMode = it },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isSearching = false,
                searchRoomResults = filteredRooms,
                searchUserResults = filteredUsers,
                currentUser = currentUser,
                onDismiss = { showSearchDialog = false },
                onJoinRoom = { r ->
                    showSearchDialog = false
                    onJoinRoom(r)
                },
                onSelectUser = { u ->
                    showSearchDialog = false
                    onSelectUserProfile(u)
                }
            )
        }

        // GAME ROOMS LIST MODAL DIALOG (SHOWS LIVE GAME ROOMS OR "NO ROOMS PLAYING" EMPTY STATE)
        if (selectedGameForRoomList != null) {
            val game = selectedGameForRoomList!!
            GameRoomsListModalDialog(
                gameEmoji = game.first,
                gameTitle = game.second,
                allRooms = rooms,
                onDismiss = { selectedGameForRoomList = null },
                onJoinRoom = { room ->
                    selectedGameForRoomList = null
                    onJoinRoom(room)
                },
                onCreateVoiceGameRoom = {
                    selectedGameForRoomList = null
                    onCreateRoom()
                }
            )
        }
    }
}

// -------------------------------------------------------------
// 100% FULLY TRANSPARENT TOP HEADER OVERLAY (NO BLACK BOX!)
// -------------------------------------------------------------
@Composable
fun AppTopHeaderOverlay(
    liveUsersCount: Int,
    roomsCount: Int,
    coins: Long,
    unreadNotificationsCount: Int,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blinkAnim = rememberInfiniteTransition(label = "headerOnlineBlink")
    val blinkAlpha by blinkAnim.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    // Real-Time Notification Bell Ringing Animation + Blinking Badge
    val bellAnim = rememberInfiniteTransition(label = "bellNotificationAnim")
    val bellRotation by bellAnim.animateFloat(
        initialValue = if (unreadNotificationsCount > 0) -15f else 0f,
        targetValue = if (unreadNotificationsCount > 0) 15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bellRotation"
    )
    val badgePulseScale by bellAnim.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgePulse"
    )
    val badgeAlpha by bellAnim.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding & Real-Time Live Counters
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF6366F1)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("LT", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Live Together",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = blinkAlpha))
                        )
                    }
                    Text(
                        text = "● $liveUsersCount Users Online • $roomsCount Active Rooms",
                        color = Color(0xFFF472B6),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right Action Controls: Coins, Single Search, Single Notification Bell
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Coin Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "🪙 $coins",
                        color = Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Single Search Button - Sleek Golden Magnifying Glass
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onOpenSearch),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Single Notification Bell Button with Animated Ring & Dynamic Number Badge (28dp icon, 42dp touch target)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clickable(onClick = onOpenNotifications),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = if (unreadNotificationsCount > 0) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer {
                                if (unreadNotificationsCount > 0) {
                                    rotationZ = bellRotation
                                }
                            }
                    )
                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .graphicsLayer {
                                    scaleX = badgePulseScale
                                    scaleY = badgePulseScale
                                    alpha = badgeAlpha
                                }
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFFF2A55))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotificationsCount > 99) "99+" else "$unreadNotificationsCount",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DUAL-MODE SEARCH MODAL DIALOG (ROOM ID VS USER ID TOGGLE)
// -------------------------------------------------------------
@Composable
fun DualModeSearchDialog(
    searchMode: String,
    onSearchModeChange: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearching: Boolean,
    searchRoomResults: List<RoomItem>,
    searchUserResults: List<UserProfile>,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onJoinRoom: (RoomItem) -> Unit,
    onSelectUser: (UserProfile) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFF6366F1)))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with title and close icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🔍 Discover & Search",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // DUAL-MODE TOGGLE PILL: "🎙️ Room ID" vs "👤 User ID"
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1B1433),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .then(
                                        if (searchMode == "room")
                                            Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFE11D48))))
                                        else Modifier.background(Color.Transparent)
                                    )
                                    .clickable { onSearchModeChange("room") }
                                    .padding(horizontal = 18.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🎙️ Room ID",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (searchMode == "room") Color.White else Color(0xFF94A3B8)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .then(
                                        if (searchMode == "user")
                                            Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))))
                                        else Modifier.background(Color.Transparent)
                                    )
                                    .clickable { onSearchModeChange("user") }
                                    .padding(horizontal = 18.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "👤 User ID",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (searchMode == "user") Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Input Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        placeholder = {
                            Text(
                                text = if (searchMode == "room") "Enter 8-digit Room ID or Room Name..." else "Enter 8-digit User ID or Username...",
                                fontSize = 11.5.sp,
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = if (searchMode == "room") Color(0xFFEC4899) else Color(0xFF6366F1),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
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
                            focusedContainerColor = Color(0xFF17102B),
                            unfocusedContainerColor = Color(0xFF17102B),
                            focusedBorderColor = if (searchMode == "room") Color(0xFFEC4899) else Color(0xFF6366F1),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Results Container
                    if (isSearching) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFEC4899),
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (searchQuery.isBlank()) {
                        Text(
                            text = if (searchMode == "room") "💡 Enter a Room ID or Name to join live parties" else "💡 Enter a User ID (e.g. 8-digit ID) or name to find friends",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (searchMode == "room") {
                        if (searchRoomResults.isEmpty()) {
                            Text(
                                text = "No live rooms found for \"$searchQuery\"",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                            ) {
                                items(searchRoomResults) { room ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1A1333))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2E1065)),
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
                                                fontSize = 12.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "ID: ${room.roomId.ifBlank { room.id }} • Host: ${room.hostName}",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        }
                                        Button(
                                            onClick = { onJoinRoom(room) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
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
                        // User Search Mode
                        if (searchUserResults.isEmpty()) {
                            Text(
                                text = "No users found for \"$searchQuery\"",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                            ) {
                                items(searchUserResults) { user ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1A1333))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(36.dp),
                                            contentAlignment = Alignment.BottomEnd
                                        ) {
                                            AsyncImage(
                                                model = user.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80" },
                                                contentDescription = user.name,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Surface(
                                                color = if (user.isUserOnline()) Color(0xFF10B981) else Color(0xFF64748B),
                                                shape = CircleShape,
                                                border = BorderStroke(1.dp, Color.Black),
                                                modifier = Modifier.size(9.dp)
                                            ) {}
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = user.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "ID: ${user.userId.ifBlank { user.uid.take(8) }} • ${if (user.isUserOnline()) "🟢 Online" else "⚪ Offline"}",
                                                color = if (user.isUserOnline()) Color(0xFF34D399) else Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        }
                                        Button(
                                            onClick = { onSelectUser(user) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
// GAME ROOMS LIST MODAL DIALOG (SHOWS LIVE ROOMS OR EMPTY NOTIFICATION)
// -------------------------------------------------------------
@Composable
fun GameRoomsListModalDialog(
    gameEmoji: String,
    gameTitle: String,
    allRooms: List<RoomItem>,
    onDismiss: () -> Unit,
    onJoinRoom: (RoomItem) -> Unit,
    onCreateVoiceGameRoom: () -> Unit
) {
    // Only Voice rooms! Video rooms strictly prohibited for games!
    val liveGameRooms = remember(allRooms, gameTitle) {
        val keyword = when {
            gameTitle.contains("Ludo", ignoreCase = true) -> "ludo"
            gameTitle.contains("Carrom", ignoreCase = true) -> "carrom"
            gameTitle.contains("Quiz", ignoreCase = true) -> "quiz"
            else -> gameTitle.lowercase().trim()
        }
        allRooms.filter {
            it.isHostLive &&
            !it.statusMessage.equals("OFFLINE", true) &&
            !it.statusMessage.equals("CLOSED", true) &&
            it.category != "video_stream" && !it.isVideoRoom &&
            (it.title.contains(keyword, ignoreCase = true) ||
             it.roomName.contains(keyword, ignoreCase = true) ||
             it.category.contains(keyword, ignoreCase = true) ||
             it.nowPlayingTitle.contains(keyword, ignoreCase = true))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFF59E0B)))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(gameEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "$gameTitle Match Rooms",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                Text(
                                    if (liveGameRooms.isNotEmpty()) "🟢 ${liveGameRooms.size} Active Voice Game Rooms" else "No game rooms live right now",
                                    color = if (liveGameRooms.isNotEmpty()) Color(0xFF10B981) else Color(0xFFF472B6),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (liveGameRooms.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(liveGameRooms) { room ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF1E1738))
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2E1065)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(gameEmoji, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                room.title.ifBlank { room.roomName.ifBlank { "$gameTitle Room" } },
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                "Host: ${room.hostName} • 👥 ${room.activeUserCount} Playing",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { onJoinRoom(room) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                        shape = RoundedCornerShape(50),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Join Game", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty State as explicitly instructed by user!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1738)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(gameEmoji, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "There are no rooms playing $gameTitle right now",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Be the first host to start a live $gameTitle party!",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.5.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onCreateVoiceGameRoom,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text("🎙️ Start a Voice Game Room", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// FLOATING SLIM EXPANDABLE NEON CAPSULE PILL (ROTATING & BLINKING NEON GLOW)
// -------------------------------------------------------------
@Composable
fun AnimatedExpandableNeonCapsule(
    currentScreen: String,
    currentUser: UserProfile,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNavigate: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onOpenMoments: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Width animation: 165dp (mini) <-> 304dp (expanded)
    val targetWidth = if (isExpanded) 304.dp else 165.dp
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
        label = "capsuleWidth"
    )

    // EXACT ROTATING NEON CONIC SWEEP BORDER (3.5s linear infinite, from home_screen_mockup.html)
    val infiniteTransition = rememberInfiniteTransition(label = "capsuleNeonLoop")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    // RHYTHMIC BLINKING NEON LIGHT (Exact Light ON / OFF strobe: "लाइट ऑन-ऑफ जिस तरह से होता है")
    val blinkLight by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                0.12f at 0
                1.0f at 200
                1.0f at 450
                0.15f at 580
                0.12f at 800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blinkNeonLight"
    )

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 10.dp)
            .width(animatedWidth)
            .height(52.dp)
            .shadow(
                elevation = (4 + 14 * blinkLight).dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0xFFFF007F).copy(alpha = blinkLight),
                ambientColor = Color(0xFF00E5FF).copy(alpha = blinkLight)
            )
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onToggleExpand)
            .drawBehind {
                // Continuous Conic Gradient Spin (neon-border-spinner) with rhythmic ON/OFF blink
                rotate(spinAngle) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFFFF007F).copy(alpha = blinkLight),
                                Color(0xFF00E5FF).copy(alpha = blinkLight),
                                Color(0xFFA855F7).copy(alpha = blinkLight),
                                Color(0xFFFFD700).copy(alpha = blinkLight),
                                Color(0xFFFF007F).copy(alpha = blinkLight)
                            )
                        ),
                        radius = size.maxDimension
                    )
                }
            }
            .padding(2.dp) // Distinct 2px neon border thickness
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0D061C))
            .border(
                1.2.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF007F).copy(alpha = blinkLight),
                        Color(0xFF00E5FF).copy(alpha = blinkLight),
                        Color(0xFFFFD700).copy(alpha = blinkLight)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
    ) {
        // Inner capsule dock body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Permanent Dense Dancing Equalizer (13 multi-colored bars)
                DenseDancingEqualizerRow(modifier = Modifier.fillMaxWidth().height(7.dp))

                // Capsule Navigation Tabs Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Home
                    CapsuleTabItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        isSelected = currentScreen == "discover",
                        isExpanded = isExpanded,
                        activeColor = Color(0xFFF472B6),
                        onClick = { onNavigate("discover") }
                    )

                    // 2. Moment (Scrolls to Moments or opens Moment modal)
                    CapsuleTabItem(
                        icon = Icons.Default.CameraAlt,
                        label = "Moment",
                        isSelected = currentScreen == "moments",
                        isExpanded = isExpanded,
                        activeColor = Color(0xFFF472B6),
                        onClick = onOpenMoments
                    )

                    // 3. Live (Center Highlighted Elevated Button with permanent "Go Live" label)
                    Box(
                        modifier = Modifier
                            .offset(y = (-2).dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onCreateRoom)
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFEC4899), Color(0xFFE11D48), Color(0xFF6366F1))
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.95f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Go Live",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "Go Live",
                                color = Color(0xFFFFD700),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }

                    // 4. Chat
                    CapsuleTabItem(
                        icon = Icons.Default.ChatBubble,
                        label = "Chat",
                        isSelected = currentScreen == "chats" || currentScreen == "messages",
                        isExpanded = isExpanded,
                        activeColor = Color(0xFFF472B6),
                        hasBadge = true,
                        onClick = { onNavigate("chats") }
                    )

                    // 5. Profile
                    Box(
                        modifier = Modifier
                            .clickable { onNavigate("profile") }
                            .padding(horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEC4899))),
                                        shape = CircleShape
                                    )
                            ) {
                                if (currentUser.photo.isNotBlank()) {
                                    AsyncImage(
                                        model = currentUser.photo,
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                            if (isExpanded) {
                                Text(
                                    "Profile",
                                    color = if (currentScreen == "profile") Color(0xFFF472B6) else Color(0xFF94A3B8),
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
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
// DENSE DANCING EQUALIZER ROW (13 MULTI-COLORED BARS)
// -------------------------------------------------------------
@Composable
fun DenseDancingEqualizerRow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "denseEqLoop")
    val heights = (0 until 13).map { i ->
        val duration = 240 + (i * 35) % 200
        val target = if (i % 2 == 0) 7f else 9f
        infiniteTransition.animateFloat(
            initialValue = 2f,
            targetValue = target,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "eq$i"
        )
    }

    val colors = listOf(
        Color(0xFFF472B6), Color(0xFF22D3EE), Color(0xFFFACC15),
        Color(0xFFC084FC), Color(0xFFF472B6), Color(0xFF67E8F9),
        Color(0xFF818CF8), Color(0xFFF472B6), Color(0xFF22D3EE),
        Color(0xFFFDE047), Color(0xFFC084FC), Color(0xFFF472B6),
        Color(0xFF22D3EE)
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEachIndexed { idx, anim ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .width(2.dp)
                    .height(anim.value.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors[idx % colors.size])
            )
        }
    }
}

@Composable
fun CapsuleTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isExpanded: Boolean,
    activeColor: Color,
    hasBadge: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) activeColor else Color(0xFF94A3B8),
                    modifier = Modifier.size(15.dp)
                )
                if (hasBadge) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color(0xFFF43F5E))
                    )
                }
            }
            if (isExpanded) {
                Text(
                    text = label,
                    color = if (isSelected) activeColor else Color(0xFF94A3B8),
                    fontSize = 7.5.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 4: COMMUNITY MOMENTS FEED (MATCHING MOCKUP SECTION 4)
// -------------------------------------------------------------
@Composable
fun CommunityMomentsFeedSection(
    currentUser: UserProfile,
    moments: List<MomentItem>,
    onPostClick: () -> Unit,
    onLikeMoment: (MomentItem) -> Unit,
    onSelectProfile: (UserProfile) -> Unit
) {
    var selectedTab by remember { mutableStateOf("Latest") }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📸", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "COMMUNITY MOMENTS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFEC4899).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFEC4899).copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text("Feed", color = Color(0xFFF472B6), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Latest vs Trending filter pills
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selectedTab == "Latest") Color(0xFF4F46E5) else Color(0xFF1E293B))
                        .clickable { selectedTab = "Latest" }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Latest", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selectedTab == "Trending") Color(0xFF4F46E5) else Color(0xFF1E293B))
                        .clickable { selectedTab = "Trending" }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Trending", color = if (selectedTab == "Trending") Color.White else Color(0xFF94A3B8), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Post Creation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1A102F))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .clickable(onClick = onPostClick)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFEC4899), CircleShape)
                ) {
                    if (currentUser.photo.isNotBlank()) {
                        AsyncImage(
                            model = currentUser.photo,
                            contentDescription = "Me",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text("Share what's on your mind...", color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEC4899).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFEC4899).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Display Moments (Strictly real moments from Firestore, NO DUMMY SEEDS)
        val displayMoments = remember(moments) {
            moments.filter { !it.id.startsWith("seed_") && !it.id.startsWith("dummy") }
        }

        if (displayMoments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No community moments posted yet. Tap + to share your moment!",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        displayMoments.forEach { moment ->
            CommunityMomentCard(
                moment = moment,
                currentUserId = currentUser.uid,
                onLike = { onLikeMoment(moment) },
                onSelectProfile = {
                    onSelectProfile(UserProfile(uid = moment.userId, name = moment.userName, photo = moment.userPhoto))
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CommunityMomentCard(
    moment: MomentItem,
    currentUserId: String,
    onLike: () -> Unit,
    onSelectProfile: () -> Unit
) {
    var isLiked by remember(moment.likedByUsers, currentUserId) {
        mutableStateOf(moment.likedByUsers.contains(currentUserId))
    }
    var currentLikes by remember(moment.likes) { mutableStateOf(moment.likes) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF130E26))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row (Avatar, Name, VIP, Follow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onSelectProfile)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF59E0B))),
                                CircleShape
                            )
                    ) {
                        AsyncImage(
                            model = moment.userPhoto.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                            contentDescription = moment.userName,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                moment.userName.ifBlank { "User" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEC4899).copy(alpha = 0.2f))
                                    .border(0.6.dp, Color(0xFFEC4899).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                            ) {
                                Text("VIP", color = Color(0xFFF472B6), fontSize = 7.5.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Text("Active Now • Live Room", color = Color(0xFF94A3B8), fontSize = 8.5.sp)
                    }
                }

                Button(
                    onClick = onSelectProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899).copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("+ Follow", color = Color(0xFFF472B6), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Caption
            if (moment.caption.isNotBlank()) {
                Text(
                    text = moment.caption,
                    color = Color(0xFFE2E8F0),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            // Photo image card
            val photoUrl = moment.mediaUrl.ifBlank {
                moment.mediaUrls.firstOrNull() ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80"
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF030712))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Moment Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("📍 Live Moment", color = Color(0xFFE2E8F0), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Engagement Footer (Likes, Comments, Share, Gift)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Like button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isLiked = !isLiked
                            currentLikes = if (isLiked) currentLikes + 1 else maxOf(0, currentLikes - 1)
                            onLike()
                        }
                    ) {
                        Text(if (isLiked) "❤️" else "🤍", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$currentLikes", color = if (isLiked) Color(0xFFF43F5E) else Color(0xFF94A3B8), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Comments
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💬", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${moment.commentCount}", color = Color(0xFF94A3B8), fontSize = 9.5.sp)
                    }

                    // Share
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", fontSize = 12.sp)
                    }
                }

                // Send Gift pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFF59E0B).copy(alpha = 0.25f), Color(0xFFEC4899).copy(alpha = 0.25f))
                            )
                        )
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(50))
                        .clickable { onLike() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎁", fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Send Gift", color = Color(0xFFFBBF24), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TOP FREE FLOATING AVATAR STAGE (~380DP DRIFTING ALL THE WAY TO THE TOP GLASS EDGE)
// -------------------------------------------------------------
@Composable
fun FreeFloatingAvatarStage(
    profiles: List<UserProfile>,
    rooms: List<RoomItem>,
    onSelectUserProfile: (UserProfile) -> Unit,
    onJoinRoom: (RoomItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floatingBubblesStage")

    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stageBlink"
    )

    // CONTINUOUS UPWARD DRIFT FROM 350DP (BELOW PARTY BOXES) ALL THE WAY TO -70DP (PAST THE TOP STATUS BAR!)
    val drift1 by infiniteTransition.animateFloat(
        initialValue = 350f, targetValue = -70f,
        animationSpec = infiniteRepeatable(tween(7200, easing = LinearEasing), RepeatMode.Restart), label = "d1"
    )
    val drift2 by infiniteTransition.animateFloat(
        initialValue = 350f, targetValue = -70f,
        animationSpec = infiniteRepeatable(tween(8800, delayMillis = 1100, easing = LinearEasing), RepeatMode.Restart), label = "d2"
    )
    val drift3 by infiniteTransition.animateFloat(
        initialValue = 350f, targetValue = -70f,
        animationSpec = infiniteRepeatable(tween(7900, delayMillis = 2200, easing = LinearEasing), RepeatMode.Restart), label = "d3"
    )
    val drift4 by infiniteTransition.animateFloat(
        initialValue = 350f, targetValue = -70f,
        animationSpec = infiniteRepeatable(tween(9400, delayMillis = 600, easing = LinearEasing), RepeatMode.Restart), label = "d4"
    )
    val drift5 by infiniteTransition.animateFloat(
        initialValue = 350f, targetValue = -70f,
        animationSpec = infiniteRepeatable(tween(7500, delayMillis = 3200, easing = LinearEasing), RepeatMode.Restart), label = "d5"
    )
    val drift6 by infiniteTransition.animateFloat(
        initialValue = 350f, targetValue = -70f,
        animationSpec = infiniteRepeatable(tween(8500, delayMillis = 1700, easing = LinearEasing), RepeatMode.Restart), label = "d6"
    )

    // STRICT REAL-TIME USER PROFILES ONLY (Prioritize LIVE users in rooms first, zero dummy profiles)
    val liveRoomByUser = remember(rooms.map { "${it.id}_${it.isLive}_${it.isHostLive}_${it.statusMessage}" }, profiles.map { "${it.uid}_${it.current_room}" }) {
        val activeRooms = rooms.filter {
            (it.isHostLive || it.isLive) &&
            !it.statusMessage.equals("OFFLINE", ignoreCase = true) &&
            !it.statusMessage.equals("CLOSED", ignoreCase = true) &&
            !it.statusMessage.equals("DELETED", ignoreCase = true)
        }
        val map = mutableMapOf<String, RoomItem>()
        profiles.forEach { p ->
            val r = activeRooms.firstOrNull {
                (p.current_room.isNotBlank() && (it.id == p.current_room || it.roomId == p.current_room)) ||
                (it.hostUid.isNotBlank() && it.hostUid == p.uid) ||
                (it.hostId.isNotBlank() && it.hostId == p.uid)
            }
            if (r != null) map[p.uid] = r
        }
        map
    }

    val realProfiles = remember(profiles, liveRoomByUser) {
        profiles.filter { profile ->
            profile.photo.isNotBlank() &&
            profile.name.isNotBlank() &&
            !profile.uid.startsWith("seed_") &&
            !profile.uid.startsWith("dummy") &&
            !profile.uid.startsWith("mock")
        }.distinctBy { it.uid }
            .sortedByDescending { if (liveRoomByUser.containsKey(it.uid)) 2 else if (it.isUserOnline()) 1 else 0 }
            .take(6)
    }

    val drifts = listOf(drift1, drift2, drift3, drift4, drift5, drift6)
    val xPositions = listOf(0.06f, 0.22f, 0.40f, 0.58f, 0.74f, 0.88f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            // NO clip! Allows avatars to drift all the way up through status bar and top glass edge!
    ) {
        val stageWidth = maxWidth

        realProfiles.forEachIndexed { idx, profile ->
            val yDrift = drifts[idx % drifts.size]
            val xPos = stageWidth * xPositions[idx % xPositions.size] - 22.dp

            val activeRoom = liveRoomByUser[profile.uid]
            val isUserLive = activeRoom != null

            Column(
                modifier = Modifier
                    .offset(x = xPos, y = yDrift.dp)
                    .clickable {
                        if (activeRoom != null) {
                            onJoinRoom(activeRoom)
                        } else {
                            onSelectUserProfile(profile)
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(
                            1.8.dp,
                            Brush.sweepGradient(listOf(Color(0xFFEC4899), Color(0xFFF59E0B), Color(0xFF38BDF8), Color(0xFFEC4899))),
                            CircleShape
                        )
                        .shadow(8.dp, CircleShape, spotColor = Color(0xFFEC4899))
                ) {
                    AsyncImage(
                        model = profile.photo,
                        contentDescription = profile.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isUserLive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE11D48))
                                .padding(horizontal = 4.dp, vertical = 0.5.dp)
                        ) {
                            Text("LIVE", color = Color.White, fontSize = 6.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    } else if (profile.isUserOnline()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = blinkAlpha))
                                .border(1.dp, Color.Black, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    profile.name.take(6),
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 2A. VOICE PARTY BIG BOX (WITH REAL PARTY THUMBNAIL PREVIEW)
// -------------------------------------------------------------
@Composable
fun VoicePartyBigBox(
    modifier: Modifier = Modifier,
    activeRoomsCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(138.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.2.dp, Color(0xFF818CF8).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        // High quality party celebration theme background image
        AsyncImage(
            model = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
            contentDescription = "Party Crowd",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E1035).copy(alpha = 0.82f),
                            Color(0xFF0F0B1E).copy(alpha = 0.94f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎙️", fontSize = 22.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF6366F1))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("VOICE", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Column {
                Text("Voice Party", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                Text("$activeRoomsCount Active Rooms", color = Color(0xFFC7D2FE), fontSize = 10.sp)
            }

            // Running Equalizer Visualizer
            CategoryEqualizerThemeVisualizer(
                modifier = Modifier.fillMaxWidth().height(22.dp),
                isLive = true,
                isPlaying = true
            )

            Text("Browse Categories →", color = Color(0xFFA5B4FC), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// 2B. VIDEO STREAMING BIG BOX (WITH VIDEO STREAM PARTY PREVIEW)
// -------------------------------------------------------------
@Composable
fun VideoStreamingBigBox(
    modifier: Modifier = Modifier,
    activeStreamsCount: Int,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "videoPreviewWave")
    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wavePulse"
    )

    Box(
        modifier = modifier
            .height(138.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.2.dp, Color(0xFFF43F5E).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        // High quality live concert/video streaming DJ party preview image
        AsyncImage(
            model = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            contentDescription = "Video Stream Preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient dark overlay with animated glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF500724).copy(alpha = 0.80f * wavePulse),
                            Color(0xFF0F0B1E).copy(alpha = 0.94f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📹", fontSize = 22.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE11D48))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("STREAM", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Column {
                Text("Video Streaming", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                Text("$activeStreamsCount Live Streams", color = Color(0xFFFECDD3), fontSize = 10.sp)
            }

            // Video Sound Equalizer Visualizer
            CategoryEqualizerThemeVisualizer(
                modifier = Modifier.fillMaxWidth().height(22.dp),
                isLive = true,
                isPlaying = true
            )

            Text("Watch Live Streams →", color = Color(0xFFFDA4AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// EVENT HUB VIDEO THEME BACKGROUND (LOOPING MUTED CYBER ARENA)
// -------------------------------------------------------------
@Composable
fun EventHubVideoBackground(
    modifier: Modifier = Modifier,
    rawResId: Int = R.raw.gift_cyber_arena
) {
    AndroidView(
        factory = { context ->
            TextureView(context).apply {
                isOpaque = false
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    private var player: MediaPlayer? = null
                    private var videoSurface: Surface? = null

                    override fun onSurfaceTextureAvailable(surfaceTexture: android.graphics.SurfaceTexture, width: Int, height: Int) {
                        try {
                            val surface = Surface(surfaceTexture)
                            videoSurface = surface
                            val mp = MediaPlayer().apply {
                                setSurface(surface)
                                setDataSource(context, Uri.parse("android.resource://${context.packageName}/$rawResId"))
                                isLooping = true
                                setVolume(0f, 0f) // Keep muted for background ambiance
                                setOnPreparedListener { it.start() }
                                setOnErrorListener { _, _, _ -> true }
                                prepareAsync()
                            }
                            player = mp
                        } catch (e: Exception) {
                            android.util.Log.e("EventHubVideo", "Failed to start background video", e)
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(surfaceTexture: android.graphics.SurfaceTexture, width: Int, height: Int) {}

                    override fun onSurfaceTextureDestroyed(surfaceTexture: android.graphics.SurfaceTexture): Boolean {
                        try {
                            player?.stop()
                            player?.release()
                            player = null
                            videoSurface?.release()
                            videoSurface = null
                        } catch (e: Exception) {
                            // ignore
                        }
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: android.graphics.SurfaceTexture) {}
                }
            }
        },
        modifier = modifier
    )
}

// -------------------------------------------------------------
// EVENT HUB CYBER WAVE VISUALIZER (DISTINCT NEON OSCILLOGRAPH)
// -------------------------------------------------------------
@Composable
fun EventHubCyberWaveVisualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_oscillograph")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2100, easing = LinearEasing), RepeatMode.Restart),
        label = "phase2"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        if (width <= 0 || height <= 0) return@Canvas

        val path1 = androidx.compose.ui.graphics.Path()
        val path2 = androidx.compose.ui.graphics.Path()
        val pointCount = 60
        val dx = width / pointCount

        for (i in 0..pointCount) {
            val x = i * dx
            val normX = i.toFloat() / pointCount.toFloat()
            val envelope = kotlin.math.sin(normX * Math.PI).toFloat()

            val y1 = midY + kotlin.math.sin(phase1 + normX * 4 * Math.PI).toFloat() * (height * 0.44f) * envelope
            val y2 = midY + kotlin.math.cos(phase2 + normX * 3.2 * Math.PI).toFloat() * (height * 0.38f) * envelope

            if (i == 0) {
                path1.moveTo(x, y1)
                path2.moveTo(x, y2)
            } else {
                path1.lineTo(x, y1)
                path2.lineTo(x, y2)
            }

            // Draw glowing harmonic nodes on peaks
            if (i % 8 == 4) {
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = glowAlpha),
                    radius = 2.8.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y1)
                )
            }
        }

        // Wave 1: Laser Cyan glowing stroke
        drawPath(
            path = path1,
            color = Color(0xFF00F0FF).copy(alpha = 0.95f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2.dp.toPx())
        )

        // Wave 2: Hot Electric Pink glowing stroke
        drawPath(
            path = path2,
            color = Color(0xFFFF2A6D).copy(alpha = 0.85f * glowAlpha),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
        )
    }
}

// -------------------------------------------------------------
// 1.5 SINGLE EVENT HUB BANNER (FULL WIDTH, 12 PM - 6 AM OPERATIONAL, RUNNING TOP-RIGHT HOST TICKER)
// -------------------------------------------------------------
@Composable
fun EventHubSingleHomeBanner(
    allUsers: List<UserProfile>,
    rooms: List<RoomItem>,
    currentUser: UserProfile,
    onEnterHub: () -> Unit
) {
    val isLiveBroadcast = isEventHubLiveBroadcastNow() // after 5:00 PM
    val infiniteTransition = rememberInfiniteTransition(label = "event_hub_banner")
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseBorder"
    )

    // Filter authentic real-time hosts for the running ticker
    val topRealHosts = remember(allUsers, rooms) {
        val real = allUsers.filter { !it.uid.startsWith("seed_") && !it.uid.startsWith("dummy") }
        real.sortedWith(
            compareByDescending<UserProfile> { it.isHostMonetized || it.isAdmin || it.email.contains("servicesecure822", ignoreCase = true) }
                .thenByDescending { it.wealthLevel + it.charmLevel }
                .thenByDescending { it.followerCount }
        ).take(6)
    }

    // Countdown calculation to 5:00 PM
    val countdownText = remember {
        val cal = java.util.Calendar.getInstance()
        val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(java.util.Calendar.MINUTE)
        val currentSecond = cal.get(java.util.Calendar.SECOND)
        if (currentHour < 17) {
            val totalSecondsRemaining = ((16 - currentHour) * 3600) + ((59 - currentMinute) * 60) + (60 - currentSecond)
            val h = totalSecondsRemaining / 3600
            val m = (totalSecondsRemaining % 3600) / 60
            val s = totalSecondsRemaining % 60
            String.format("%02dh : %02dm : %02ds", h, m, s)
        } else {
            "🔴 ARENA ON AIR"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFEC4899).copy(alpha = pulseBorder),
                        Color(0xFF8B5CF6).copy(alpha = pulseBorder),
                        Color(0xFF3B82F6).copy(alpha = pulseBorder * 0.8f)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onEnterHub)
    ) {
        // Active Looping Video Theme Background Preview
        EventHubVideoBackground(
            modifier = Modifier.matchParentSize(),
            rawResId = R.raw.gift_cyber_arena
        )

        // Dark cyber gradient scrim for sharp legibility of content
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xD91F0A3D),
                            Color(0xF20F051D)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            
            // TOP ROW: Left Badges & Title, and Right RUNNING HOST TICKER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFFEC4899).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.8.dp, Color(0xFFEC4899).copy(alpha = 0.6f))
                        ) {
                            Text(
                                "🏆 OFFICIAL EVENT",
                                color = Color(0xFFF472B6),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (isLiveBroadcast) {
                            Surface(
                                color = Color(0xFFEF4444),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    "🔴 LIVE",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        "Mega Stars Celebration Gala 🌟",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: RUNNING HOST TICKER (4-5 Top Real Hosts Scrolling)
                if (topRealHosts.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .border(0.8.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👑 TOP HOSTS", color = Color(0xFFFBBF24), fontSize = 7.5.sp, fontWeight = FontWeight.Black)
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(topRealHosts) { host ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20))
                                            .background(Color(0xFF1F1D36))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.BottomEnd) {
                                            AsyncImage(
                                                model = host.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100" },
                                                contentDescription = host.name,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, Color(0xFFF59E0B), CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(Color(0xFFEF4444), CircleShape)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            host.name.ifBlank { "Host" }.take(6),
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // MIDDLE ROW: Structure & Countdown + Enter Hub Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "12 PM - 5 PM Booking • 5 PM - 6 AM Live Arena",
                        color = Color(0xFFC4B5FD),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Starts 5 PM: $countdownText",
                        color = if (isLiveBroadcast) Color(0xFF34D399) else Color(0xFFF472B6),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.7f)),
                    modifier = Modifier.clickable(onClick = onEnterHub)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Enter Hub →",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTTOM ROW: Unique Mirrored Center-Axis Crystal Wave Equalizer
            DenseGalaAudioEqualizer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                barCount = 34,
                styleTheme = "event_theme_celestial_gold"
            )
        }
    }
}
