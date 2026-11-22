package com.devil.livetogather

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Modern, sober, non-copyright LiveTogather Inbox palette
private val InboxTopLavender = Color(0xFFEDE7FF)
private val InboxTopMist = Color(0xFFF6F3FF)
private val InboxSurfaceWhite = Color(0xFFFFFFFF)
private val InboxTextPrimary = Color(0xFF151726)
private val InboxTextSecondary = Color(0xFF8B90A7)
private val InboxDividerHairline = Color(0xFFF0F1F7)
private val InboxAccentIndigo = Color(0xFF7C3AED)
private val InboxAccentCoral = Color(0xFFFF3B6B)
private val InboxUnreadRed = Color(0xFFFF3B5C)

private data class ActiveRoomCarouselUser(
    val uid: String,
    val name: String,
    val photo: String,
    val activeFrame: String,
    val vipLevel: Int,
    val room: RoomItem
)

/**
 * Modern, clean, borderless Inbox & Contacts Screen for LiveTogather.
 * Features:
 * - Clean Chats / Contacts top tabs + pill search bar + quick add-contact button
 * - Horizontal Active Room Users carousel enclosed in animated 3D FlowingAvatarFrames
 * - Seamless official rows (LiveTogather Official, Event Hub, VIP Service) with verified badges & unread counters
 * - Real-time personal chats with 3D frames, online/live-room indicators, and clean right-aligned timestamps
 * - Interactive Official Assistant Thread with gift thumbnails & one-tap Equip for 24h/3d rewards
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernInboxScreen(
    matches: List<MatchItem>,
    roomsList: List<RoomItem>,
    allProfiles: List<UserProfile>,
    currentUserId: String,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onOpenChat: (UserProfile) -> Unit,
    onJoinRoom: (RoomItem) -> Unit,
    onOpenEventHub: () -> Unit,
    onOpenVipService: () -> Unit,
    onUserUpdated: (UserProfile) -> Unit,
    onToggleBlock: (UserProfile) -> Unit
) {
    val context = LocalContext.current

    // Header Tab & Search States
    var activeTab by remember { mutableStateOf("chats") } // "chats" or "contacts"
    var searchQuery by remember { mutableStateOf("") }
    var chatFilter by remember { mutableStateOf("All") } // "All", "Official", "Unread"
    var showAddContactDialog by remember { mutableStateOf(false) }

    // Partner profile cache for matches & contacts
    val partnerMap = remember { mutableStateMapOf<String, UserProfile>() }
    val followingProfiles = remember { mutableStateListOf<UserProfile>() }
    val activeBondPartnerIds = remember { mutableStateListOf<String>() }
    val archivedMatchIds = remember { mutableStateListOf<String>() }

    // Dialog states
    var selectedActionMatch by remember { mutableStateOf<Pair<MatchItem, UserProfile>?>(null) }
    var showOfficialAssistantThread by remember { mutableStateOf(false) }
    var officialThreadInitialTab by remember { mutableStateOf("all") }

    // Real-time Official System & Reward Notifications from Firestore (`users/{uid}/notifications`)
    val officialSystemMessages = remember { mutableStateListOf<NotificationItem>() }
    DisposableEffect(currentUserId) {
        if (currentUserId.isBlank()) return@DisposableEffect onDispose {}
        val notifReg = db.collection("users").document(currentUserId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val parsed = snap.documents.mapNotNull { d ->
                        d.toObject(NotificationItem::class.java)?.let { item ->
                            if (item.id.isBlank()) item.copy(id = d.id) else item
                        }
                    }
                    officialSystemMessages.clear()
                    officialSystemMessages.addAll(parsed)
                }
            }

        // Listen to active intimacy bonds to display heart indicator next to bonded partners
        val bondsReg = db.collection("intimacy_bonds")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val partnerIds = snap.documents.mapNotNull { d ->
                        val u1 = d.getString("user1Id") ?: ""
                        val u2 = d.getString("user2Id") ?: ""
                        when {
                            u1 == currentUserId && u2.isNotBlank() -> u2
                            u2 == currentUserId && u1.isNotBlank() -> u1
                            else -> null
                        }
                    }.distinct()
                    activeBondPartnerIds.clear()
                    activeBondPartnerIds.addAll(partnerIds)
                }
            }

        // Listen to following list for Contacts tab
        val followingReg = db.collection("users").document(currentUserId)
            .collection("following")
            .limit(80)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val uids = snap.documents.map { it.id }.filter { it.isNotBlank() && it != currentUserId }
                    uids.forEach { uid ->
                        val cached = allProfiles.firstOrNull { it.uid == uid } ?: partnerMap[uid]
                        if (cached != null) {
                            partnerMap[uid] = cached
                            if (followingProfiles.none { it.uid == uid }) followingProfiles.add(cached)
                        } else {
                            db.collection("users").document(uid).get().addOnSuccessListener { d ->
                                if (d != null && d.exists()) {
                                    val parsed = parseUserProfileFromDoc(d).copy(uid = d.id)
                                    partnerMap[uid] = parsed
                                    if (followingProfiles.none { it.uid == uid }) followingProfiles.add(parsed)
                                }
                            }
                        }
                    }
                }
            }

        onDispose {
            notifReg.remove()
            bondsReg.remove()
            followingReg.remove()
        }
    }

    // Populate match partner profiles in real-time
    LaunchedEffect(matches, allProfiles) {
        allProfiles.forEach { p ->
            if (p.uid.isNotBlank()) partnerMap[p.uid] = p
        }
        matches.forEach { match ->
            val pId = match.users.firstOrNull { it != currentUserId }
            if (pId != null && !partnerMap.containsKey(pId)) {
                db.collection("users").document(pId).get().addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        partnerMap[pId] = parseUserProfileFromDoc(doc).copy(uid = doc.id)
                    }
                }
            }
        }
    }

    // Real-time Official Assistant Messages (Gift Rewards + Level Ups + Assistant Time-Limited Rewards)
    val inboxPrefs = remember(context) { context.getSharedPreferences("livetogather_inbox_seen", android.content.Context.MODE_PRIVATE) }
    val locallySeenNotifIds = remember(currentUserId) {
        mutableStateListOf<String>().apply {
            val saved = inboxPrefs.getStringSet("seen_ids_$currentUserId", emptySet()) ?: emptySet()
            addAll(saved)
        }
    }
    val officialInboxTypes = remember {
        setOf("gift_reward", "creator_earning", "gift_received", "level_up", "assistant_reward", "system")
    }
    val officialMessages = officialSystemMessages
        .filter { it.type.lowercase() in officialInboxTypes }
        .sortedByDescending { it.timestamp }
    val officialUnreadCount = officialMessages.count { msg ->
        !msg.isRead && msg.id !in locallySeenNotifIds
    }
    val latestOfficialMsg = officialMessages.firstOrNull()

    val markAllOfficialMessagesSeen = {
        val unseen = officialMessages.filter { (!it.isRead || it.id !in locallySeenNotifIds) && it.id.isNotBlank() }
        if (unseen.isNotEmpty()) {
            val newIds = unseen.map { it.id }
            newIds.forEach { id -> if (id !in locallySeenNotifIds) locallySeenNotifIds.add(id) }
            inboxPrefs.edit().putStringSet("seen_ids_$currentUserId", locallySeenNotifIds.toSet()).apply()

            // Immediately update in-memory list so unread badge clears synchronously to 0
            for (i in officialSystemMessages.indices) {
                val item = officialSystemMessages[i]
                if (item.id in newIds && !item.isRead) {
                    officialSystemMessages[i] = item.copy(isRead = true)
                }
            }

            // Persist isRead = true to Firestore via WriteBatch with merge
            if (currentUserId.isNotBlank()) {
                val batch = db.batch()
                unseen.take(400).forEach { msg ->
                    val docRef = db.collection("users").document(currentUserId)
                        .collection("notifications").document(msg.id)
                    batch.set(docRef, mapOf("isRead" to true), SetOptions.merge())
                }
                batch.commit()
            }
        }
    }

    // Also auto-mark any newly synced message as seen while Official Assistant thread is open!
    LaunchedEffect(showOfficialAssistantThread, officialMessages.size) {
        if (showOfficialAssistantThread) {
            markAllOfficialMessagesSeen()
        }
    }

    // Live rooms lookup map (uid -> active RoomItem)
    val liveRooms = remember(roomsList) {
        roomsList.filter { r ->
            (r.isLive || r.isHostLive) &&
                !r.statusMessage.equals("OFFLINE", ignoreCase = true) &&
                !r.statusMessage.equals("CLOSED", ignoreCase = true) &&
                !r.statusMessage.equals("DELETED", ignoreCase = true)
        }
    }
    val roomById = remember(liveRooms) { liveRooms.associateBy { it.id } }

    // Active room users carousel (100% real live room hosts + members currently inside active rooms)
    val activeRoomUsers = remember(liveRooms, allProfiles, currentUserId) {
        val result = mutableListOf<ActiveRoomCarouselUser>()
        val seenUids = mutableSetOf<String>()
        // 1. Live room hosts
        liveRooms.forEach { room ->
            val hUid = room.hostUid.ifBlank { room.hostId }
            if (hUid.isNotBlank() && hUid != currentUserId && seenUids.add(hUid)) {
                val prof = allProfiles.firstOrNull { it.uid == hUid }
                result.add(
                    ActiveRoomCarouselUser(
                        uid = hUid,
                        name = prof?.name?.ifBlank { room.hostName } ?: room.hostName.ifBlank { room.title },
                        photo = prof?.photo?.ifBlank { room.hostPhotoUrl } ?: room.hostPhotoUrl,
                        activeFrame = prof?.activeFrame ?: "",
                        vipLevel = prof?.getVipLevel() ?: 0,
                        room = room
                    )
                )
            }
        }
        // 2. Other active users whose current_room points to a live room
        allProfiles.forEach { prof ->
            if (prof.uid.isNotBlank() && prof.uid != currentUserId && prof.current_room.isNotBlank() && seenUids.add(prof.uid)) {
                val r = roomById[prof.current_room]
                if (r != null) {
                    result.add(
                        ActiveRoomCarouselUser(
                            uid = prof.uid,
                            name = prof.name,
                            photo = prof.photo,
                            activeFrame = prof.activeFrame,
                            vipLevel = prof.getVipLevel(),
                            room = r
                        )
                    )
                }
            }
        }
        result.take(15)
    }

    // Filtered matches
    val queryLower = searchQuery.trim().lowercase()
    val visibleMatches = matches
        .filter { !archivedMatchIds.contains(it.matchId) }
        .sortedByDescending { it.timestamp }
        .filter { match ->
            val pId = match.users.firstOrNull { it != currentUserId }
            val partner = partnerMap[pId]
            if (partner == null) false
            else if (queryLower.isBlank()) true
            else partner.name.lowercase().contains(queryLower) ||
                partner.userId.lowercase().contains(queryLower) ||
                match.lastMessage.lowercase().contains(queryLower)
        }

    // Contacts list (deduplicated from following + matches + bestFriends)
    val contactsList = remember(followingProfiles.size, matches.size, partnerMap.size, queryLower) {
        val combined = linkedMapOf<String, UserProfile>()
        followingProfiles.forEach { if (it.uid.isNotBlank() && it.uid != currentUserId) combined[it.uid] = it }
        matches.forEach { m ->
            val pId = m.users.firstOrNull { it != currentUserId }
            if (pId != null) partnerMap[pId]?.let { combined[pId] = it }
        }
        currentUser.bestFriends.forEach { bfUid ->
            if (bfUid != currentUserId) partnerMap[bfUid]?.let { combined[bfUid] = it }
        }
        combined.values
            .filter { p ->
                queryLower.isBlank() ||
                    p.name.lowercase().contains(queryLower) ||
                    p.userId.lowercase().contains(queryLower)
            }
            .sortedWith(
                compareByDescending<UserProfile> { it.current_room.isNotBlank() && roomById.containsKey(it.current_room) }
                    .thenByDescending { it.isUserOnline() }
                    .thenBy { it.name.lowercase() }
            )
    }

    // Live pulsing animation for active room indicator
    val pulseTransition = rememberInfiniteTransition(label = "inboxLivePulse")
    val livePulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "livePulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InboxSurfaceWhite)
    ) {
        // =====================================================================
        // 1. CLEAN LUMINOUS TOP HEADER (Chats | Contacts | Search Pill | Add)
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(InboxTopLavender, InboxTopMist, InboxSurfaceWhite)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chats Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { activeTab = "chats" }
                        .padding(end = 18.dp, top = 4.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = "Chats",
                        color = if (activeTab == "chats") InboxTextPrimary else InboxTextSecondary,
                        fontSize = if (activeTab == "chats") 19.sp else 16.5.sp,
                        fontWeight = if (activeTab == "chats") FontWeight.ExtraBold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (activeTab == "chats") InboxAccentIndigo else Color.Transparent)
                    )
                }

                // Contacts Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { activeTab = "contacts" }
                        .padding(end = 14.dp, top = 4.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = "Contacts",
                        color = if (activeTab == "contacts") InboxTextPrimary else InboxTextSecondary,
                        fontSize = if (activeTab == "contacts") 19.sp else 16.5.sp,
                        fontWeight = if (activeTab == "contacts") FontWeight.ExtraBold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (activeTab == "contacts") InboxAccentIndigo else Color.Transparent)
                    )
                }

                // Modern Rounded Pill Search Bar
                Surface(
                    color = Color.White.copy(alpha = 0.86f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color(0xFFE6E1F9)),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = InboxTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text("Search", color = InboxTextSecondary, fontSize = 13.sp)
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(color = InboxTextPrimary, fontSize = 13.sp),
                                cursorBrush = SolidColor(InboxAccentIndigo),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = InboxTextSecondary,
                                modifier = Modifier
                                    .size(15.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Quick Add Contact Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { showAddContactDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Contact",
                        tint = InboxTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // =====================================================================
        // 2. MAIN SCROLLABLE BODY (Chats Tab vs Contacts Tab)
        // =====================================================================
        if (activeTab == "chats") {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // A. HORIZONTAL ACTIVE ROOM USERS CAROUSEL WITH 3D PROFILE FRAMES
                if (activeRoomUsers.isNotEmpty() && queryLower.isBlank()) {
                    item(key = "active_room_carousel") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 10.dp)
                        ) {
                            Text(
                                text = "Active users in rooms",
                                color = InboxTextPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(activeRoomUsers, key = { it.uid }) { user ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(74.dp)
                                            .clickable { onJoinRoom(user.room) }
                                    ) {
                                        Box(
                                            modifier = Modifier.size(72.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (user.activeFrame.isNotBlank()) {
                                                FlowingAvatarFrame(
                                                    photoUrl = user.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=160" },
                                                    frameId = user.activeFrame,
                                                    avatarSize = 52.dp,
                                                    frameSize = 72.dp,
                                                    vipLevel = user.vipLevel,
                                                    showMicEffect = false
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(62.dp)
                                                        .clip(CircleShape)
                                                        .border(
                                                            2.dp,
                                                            Brush.sweepGradient(
                                                                listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFC084FC), Color(0xFF38BDF8))
                                                            ),
                                                            CircleShape
                                                        )
                                                        .padding(3.dp)
                                                ) {
                                                    AsyncImage(
                                                        model = user.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=160" },
                                                        contentDescription = user.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                    )
                                                }
                                            }

                                            // Tiny live equalizer badge at bottom-right
                                            Surface(
                                                color = Color(0xFFEC4899).copy(alpha = livePulseAlpha),
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(1.2.dp, Color.White),
                                                modifier = Modifier.align(Alignment.BottomEnd)
                                            ) {
                                                Text(
                                                    text = "LIVE",
                                                    color = Color.White,
                                                    fontSize = 7.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = user.name.ifBlank { "User" },
                                            color = InboxTextPrimary,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // B. CHATLIST SECTION HEADER + FILTER TOGGLE
                item(key = "chatlist_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Chatlist",
                                color = InboxTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (archivedMatchIds.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Archived (${archivedMatchIds.size})",
                                    color = InboxAccentIndigo,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { archivedMatchIds.clear() }
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    chatFilter = when (chatFilter) {
                                        "All" -> "Official"
                                        "Official" -> "Unread"
                                        else -> "All"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Filter",
                                tint = InboxAccentIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = chatFilter,
                                color = InboxAccentIndigo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Sort",
                                tint = InboxTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // C. UNIFIED RECENCY-SORTED CHATLIST (Most Recent Message ALWAYS #1 TOP!)
                val showOfficialRows = queryLower.isBlank() ||
                    "livetogather official".contains(queryLower) ||
                    "event hub".contains(queryLower) ||
                    "vip service".contains(queryLower)
                val includeOfficialAssistant = showOfficialRows &&
                    (chatFilter == "All" || chatFilter == "Official" || (chatFilter == "Unread" && officialUnreadCount > 0))

                // Merge personal chats + LiveTogather Official strictly sorted by latest message timestamp DESC
                // so ANY new incoming/outgoing message immediately rises to the #1 TOP position!
                val timelineItems: List<Pair<String, Long>> = buildList {
                    if (includeOfficialAssistant) {
                        add("official_assistant" to (latestOfficialMsg?.timestamp ?: 0L))
                    }
                    if (chatFilter != "Official") {
                        visibleMatches.forEach { match ->
                            val key = "match_${match.matchId.ifBlank { match.users.joinToString("_") }}"
                            add(key to match.timestamp)
                        }
                    }
                }.sortedByDescending { it.second }

                items(timelineItems, key = { it.first }) { (entryKey, _) ->
                    if (entryKey == "official_assistant") {
                        val previewText = when {
                            latestOfficialMsg == null -> "[Official Assistant] Rewards, gifts & level-ups"
                            latestOfficialMsg.rewardItemId.isNotBlank() -> "[Reward] ${latestOfficialMsg.title}"
                            latestOfficialMsg.giftId.isNotBlank() -> "[Gift] ${latestOfficialMsg.message}"
                            latestOfficialMsg.type.equals("level_up", true) -> "[Level Up] ${latestOfficialMsg.title}"
                            else -> latestOfficialMsg.message
                        }
                        val latestGiftThumb = latestOfficialMsg?.giftId?.takeIf { it.isNotBlank() }?.let { giftThumbnailResourceId(it) } ?: 0

                        SeamlessOfficialChatRow(
                            title = "LiveTogather Official",
                            subtitle = previewText,
                            timestampText = latestOfficialMsg?.timestamp?.takeIf { it > 0L }?.let { formatInboxTime(it) } ?: "Now",
                            unreadCount = officialUnreadCount,
                            showOfficialPill = true,
                            trailingThumbnailRes = latestGiftThumb,
                            avatarContent = {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899)))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Official",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            },
                            onClick = {
                                officialThreadInitialTab = "all"
                                showOfficialAssistantThread = true
                                markAllOfficialMessagesSeen()
                            }
                        )
                    } else {
                        val targetMatch = visibleMatches.firstOrNull { m ->
                            "match_${m.matchId.ifBlank { m.users.joinToString("_") }}" == entryKey
                        }
                        val pId = targetMatch?.users?.firstOrNull { it != currentUserId }
                        val partner = partnerMap[pId]
                        if (targetMatch != null && partner != null) {
                            val isBlocked = currentUser.blockedUserIds.contains(partner.uid)
                            val isBonded = partner.uid in activeBondPartnerIds
                            val partnerRoom = roomById[partner.current_room] ?: liveRooms.firstOrNull {
                                it.hostUid == partner.uid || it.hostId == partner.uid
                            }

                            SeamlessPersonalChatRow(
                                partner = partner,
                                lastMessage = targetMatch.lastMessage.ifBlank { "Say hello 👋" },
                                timestampText = formatInboxTime(targetMatch.timestamp),
                                isBlocked = isBlocked,
                                isBonded = isBonded,
                                isLiveInRoom = partnerRoom != null,
                                onClick = { onOpenChat(partner) },
                                onLongClick = { selectedActionMatch = Pair(targetMatch, partner) }
                            )
                        }
                    }
                }

                // D. OFFICIAL SERVICE SHORTCUTS (Event Hub & VIP Service)
                if (showOfficialRows && (chatFilter == "All" || chatFilter == "Official")) {
                    item(key = "row_event_hub") {
                        SeamlessOfficialChatRow(
                            title = "Event Hub",
                            subtitle = "[Activity] Official Gala Tournaments & PK Arenas",
                            timestampText = "Live",
                            unreadCount = 0,
                            showOfficialPill = false,
                            avatarContent = {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Event Hub",
                                        tint = Color.White,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            },
                            onClick = onOpenEventHub
                        )
                    }

                    item(key = "row_vip_service") {
                        SeamlessOfficialChatRow(
                            title = "VIP Service",
                            subtitle = "[VIP 1–5] Official VIP Tiers, 3D Frames & Concierge",
                            timestampText = if (currentUser.getVipLevel() in 1..5) "VIP ${currentUser.getVipLevel()}" else "VIP 1–5",
                            unreadCount = 0,
                            showOfficialPill = false,
                            avatarContent = {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(listOf(Color(0xFFE6C16E), Color(0xFFB38632)))
                                        )
                                        .border(1.5.dp, Color(0xFFFFF3C4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("👑", fontSize = 13.sp, lineHeight = 13.sp)
                                        Text(
                                            text = "VIP",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            lineHeight = 11.sp
                                        )
                                    }
                                }
                            },
                            onClick = onOpenVipService
                        )
                    }
                }

                if (chatFilter != "Official" && visibleMatches.isEmpty()) {
                    item(key = "no_chats_placeholder") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (queryLower.isNotBlank()) "No matching chats found" else "No personal chats yet",
                                color = InboxTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + icon above or join an active room to start chatting!",
                                color = InboxTextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            // =================================================================
            // 3. CONTACTS TAB (Clean searchable real-time contacts + Room Jump)
            // =================================================================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item(key = "contacts_add_shortcut") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddContactDialog = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(InboxTopLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = InboxAccentIndigo, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Find & Add Contact", color = InboxTextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                            Text("Search by User ID or name to message instantly", color = InboxTextSecondary, fontSize = 12.sp)
                        }
                    }
                    HorizontalDivider(color = InboxDividerHairline, thickness = 0.8.dp, modifier = Modifier.padding(start = 74.dp))
                }

                if (contactsList.isEmpty()) {
                    item(key = "empty_contacts") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No contacts yet. Follow users or tap Add Contact above!", color = InboxTextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(contactsList, key = { it.uid }) { contact ->
                        val contactRoom = roomById[contact.current_room] ?: liveRooms.firstOrNull {
                            it.hostUid == contact.uid || it.hostId == contact.uid
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenChat(contact) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                                if (contact.activeFrame.isNotBlank()) {
                                    FlowingAvatarFrame(
                                        photoUrl = contact.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120" },
                                        frameId = contact.activeFrame,
                                        avatarSize = 40.dp,
                                        frameSize = 56.dp,
                                        vipLevel = contact.getVipLevel(),
                                        showMicEffect = false
                                    )
                                } else {
                                    AsyncImage(
                                        model = contact.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120" },
                                        contentDescription = contact.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(46.dp).clip(CircleShape)
                                    )
                                }
                                if (contact.isUserOnline()) {
                                    Box(
                                        modifier = Modifier
                                            .size(11.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                            .border(1.5.dp, Color.White, CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name.ifBlank { "LiveTogather User" },
                                    color = InboxTextPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (contactRoom != null) "🎙️ In Room: ${contactRoom.title.ifBlank { contactRoom.roomName }}"
                                    else if (contact.isUserOnline()) "Online now • ID: ${contact.userId.ifBlank { contact.uid.take(8) }}"
                                    else "ID: ${contact.userId.ifBlank { contact.uid.take(8) }}",
                                    color = if (contactRoom != null) InboxAccentCoral else InboxTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (contactRoom != null) {
                                Surface(
                                    color = InboxAccentCoral.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .clickable { onJoinRoom(contactRoom) }
                                        .padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "Join 🎙️",
                                        color = InboxAccentCoral,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            Surface(
                                color = InboxTopLavender,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.clickable { onOpenChat(contact) }
                            ) {
                                Text(
                                    text = "Chat",
                                    color = InboxAccentIndigo,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = InboxDividerHairline, thickness = 0.8.dp, modifier = Modifier.padding(start = 84.dp))
                    }
                }
            }
        }
    }

    // =========================================================================
    // 4. OFFICIAL ASSISTANT, GIFT REWARDS & LEVEL-UP THREAD DIALOG
    // =========================================================================
    if (showOfficialAssistantThread) {
        OfficialAssistantInboxDialog(
            messages = officialMessages,
            initialTab = officialThreadInitialTab,
            currentUser = currentUser,
            db = db,
            onUserUpdated = onUserUpdated,
            onOpenSenderChat = { senderUid ->
                db.collection("users").document(senderUid).get().addOnSuccessListener { d ->
                    if (d != null && d.exists()) {
                        showOfficialAssistantThread = false
                        onOpenChat(parseUserProfileFromDoc(d).copy(uid = d.id))
                    }
                }
            },
            onDismiss = { showOfficialAssistantThread = false }
        )
    }

    // =========================================================================
    // 5. QUICK ADD CONTACT DIALOG (Search by User ID / Name)
    // =========================================================================
    if (showAddContactDialog) {
        QuickAddContactDialog(
            currentUserId = currentUserId,
            currentUser = currentUser,
            allProfiles = allProfiles,
            db = db,
            onOpenChat = { partner ->
                showAddContactDialog = false
                onOpenChat(partner)
            },
            onDismiss = { showAddContactDialog = false }
        )
    }

    // =========================================================================
    // 6. LONG-PRESS CONVERSATION ACTION SHEET (Archive / Clear / Delete / Block)
    // =========================================================================
    if (selectedActionMatch != null) {
        val (selMatch, selPartner) = selectedActionMatch!!
        val isBlocked = currentUser.blockedUserIds.contains(selPartner.uid)
        AlertDialog(
            onDismissRequest = { selectedActionMatch = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = selPartner.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100" },
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(selPartner.name, color = InboxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(
                        onClick = {
                            archivedMatchIds.add(selMatch.matchId)
                            selectedActionMatch = null
                            Toast.makeText(context, "Archived chat with ${selPartner.name}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Archive Conversation", color = InboxTextPrimary, modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(
                        onClick = {
                            db.collection("matches").document(selMatch.matchId).collection("messages")
                                .get().addOnSuccessListener { snap ->
                                    snap.documents.forEach { it.reference.delete() }
                                }
                            db.collection("matches").document(selMatch.matchId).update("lastMessage", "")
                            selectedActionMatch = null
                            Toast.makeText(context, "Chat cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Messages", color = InboxTextPrimary, modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(
                        onClick = {
                            onToggleBlock(selPartner)
                            selectedActionMatch = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isBlocked) "Unblock User" else "Block User",
                            color = InboxUnreadRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedActionMatch = null }) {
                    Text("Cancel", color = InboxAccentIndigo, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SeamlessOfficialChatRow(
    title: String,
    subtitle: String,
    timestampText: String,
    unreadCount: Int,
    showOfficialPill: Boolean,
    trailingThumbnailRes: Int = 0,
    avatarContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                avatarContent()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = InboxTextPrimary,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showOfficialPill) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFF3EEFF),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, InboxAccentIndigo.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "Official",
                                color = InboxAccentIndigo,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = InboxTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timestampText,
                    color = InboxTextSecondary,
                    fontSize = 11.5.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (trailingThumbnailRes != 0) {
                        Image(
                            painter = painterResource(id = trailingThumbnailRes),
                            contentDescription = "Gift",
                            modifier = Modifier.size(22.dp).clip(RoundedCornerShape(5.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                .clip(CircleShape)
                                .background(InboxUnreadRed)
                                .padding(horizontal = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadCount > 99) "99+" else "$unreadCount",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = InboxDividerHairline, thickness = 0.8.dp, modifier = Modifier.padding(start = 84.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SeamlessPersonalChatRow(
    partner: UserProfile,
    lastMessage: String,
    timestampText: String,
    isBlocked: Boolean,
    isBonded: Boolean,
    isLiveInRoom: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(58.dp),
                contentAlignment = Alignment.Center
            ) {
                if (partner.activeFrame.isNotBlank()) {
                    FlowingAvatarFrame(
                        photoUrl = partner.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120" },
                        frameId = partner.activeFrame,
                        avatarSize = 40.dp,
                        frameSize = 58.dp,
                        vipLevel = partner.getVipLevel(),
                        showMicEffect = false
                    )
                } else {
                    AsyncImage(
                        model = partner.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120" },
                        contentDescription = partner.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(50.dp).clip(CircleShape)
                    )
                }

                if (isLiveInRoom) {
                    Surface(
                        color = InboxAccentCoral,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.2.dp, Color.White),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else if (partner.isUserOnline()) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(1.8.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = partner.name.ifBlank { "User" },
                        color = InboxTextPrimary,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    BlueLevelBadgePill(level = partner.getEffectiveWealthLevel())
                    if (isBonded) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Intimacy Bond",
                            tint = InboxAccentCoral,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (isBlocked) {
                        Text(
                            text = "Blocked",
                            color = InboxUnreadRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = lastMessage,
                    color = InboxTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timestampText,
                    color = InboxTextSecondary,
                    fontSize = 11.5.sp
                )
                if (isLiveInRoom) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🎙️ In Room",
                        color = InboxAccentCoral,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        HorizontalDivider(color = InboxDividerHairline, thickness = 0.8.dp, modifier = Modifier.padding(start = 86.dp))
    }
}

/**
 * Full-Screen Official Assistant, Gift Rewards & Automated Level-Up Inbox Thread.
 * Supports:
 * - Exact Gift Thumbnails on gift notifications
 * - One-Tap "Equip Now ✨" on time-limited 24h / 3-day 3D Profile Frames & Mic Effects
 */
@Composable
private fun OfficialAssistantInboxDialog(
    messages: List<NotificationItem>,
    initialTab: String,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onUserUpdated: (UserProfile) -> Unit,
    onOpenSenderChat: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(initialTab) } // "all", "rewards", "gifts"
    var equippingNotifId by remember { mutableStateOf<String?>(null) }
    val locallyClaimedIds = remember { mutableStateListOf<String>() }

    val filtered = messages.filter { msg ->
        val t = msg.type.lowercase()
        when (selectedFilter) {
            "gifts" -> t == "gift_reward" || t == "creator_earning" || t == "gift_received" || msg.giftId.isNotBlank()
            "rewards" -> t == "assistant_reward" || t == "level_up" || msg.rewardItemId.isNotBlank()
            else -> true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7FB))
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = InboxTextPrimary)
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(InboxAccentIndigo, InboxAccentCoral))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("LiveTogather Official", color = InboxTextPrimary, fontSize = 15.5.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = InboxTopLavender, shape = RoundedCornerShape(50)) {
                            Text(
                                "Assistant",
                                color = InboxAccentIndigo,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text("Automated Rewards, Gift Settlements & Level Ups", color = InboxTextSecondary, fontSize = 11.sp)
                }
            }

            // Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all" to "All Messages", "rewards" to "⭐ Rewards & Level Up", "gifts" to "🎁 Gift Receipts").forEach { (key, label) ->
                    val isSel = selectedFilter == key
                    Surface(
                        color = if (isSel) InboxAccentIndigo else Color(0xFFF1F1F8),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { selectedFilter = key }
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color.White else InboxTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = InboxDividerHairline, thickness = 1.dp)

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = InboxAccentIndigo.copy(alpha = 0.5f), modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No messages in this section yet", color = InboxTextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Gift receipts, level-up alerts, and free 24h / 3-day 3D frame & mic effect rewards appear right here automatically!",
                            color = InboxTextSecondary,
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id.ifBlank { it.timestamp.toString() } }) { msg ->
                        val hasRewardItem = msg.rewardItemId.isNotBlank()
                        val isClaimed = msg.rewardClaimed || msg.id in locallyClaimedIds
                        val giftThumbRes = if (msg.giftId.isNotBlank()) giftThumbnailResourceId(msg.giftId) else 0
                        val rewardThumbRes = when {
                            msg.rewardItemId.startsWith("mic_effect_") -> getMicEffectThumbRes(msg.rewardItemId)
                            msg.rewardItemId.isNotBlank() -> LuxuryStoreCatalog.allItems.firstOrNull { it.id == msg.rewardItemId }?.drawableRes ?: 0
                            else -> 0
                        }
                        val previewRes = if (giftThumbRes != 0) giftThumbRes else rewardThumbRes

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (hasRewardItem) InboxAccentIndigo.copy(alpha = 0.25f) else Color(0xFFECECF4)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!hasRewardItem && msg.senderId.isNotBlank() && msg.senderId != "system_livetogather" && msg.senderId != currentUser.uid) {
                                        onOpenSenderChat(msg.senderId)
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (previewRes != 0) {
                                        Image(
                                            painter = painterResource(id = previewRes),
                                            contentDescription = msg.giftName.ifBlank { msg.rewardItemName },
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF4F1FF)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(InboxTopLavender),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when {
                                                    hasRewardItem -> "✨"
                                                    msg.type.equals("level_up", true) -> "⭐"
                                                    else -> "🎁"
                                                },
                                                fontSize = 22.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = msg.title.ifBlank { "LiveTogather Official" },
                                                color = InboxTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = formatInboxTime(msg.timestamp),
                                                color = InboxTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = msg.message,
                                            color = Color(0xFF474C66),
                                            fontSize = 12.5.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }

                                // Instant Time-Limited Reward Claim & Equip Action Bar
                                if (hasRewardItem) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val durationLabel = if (msg.rewardDurationHours >= 72) "${msg.rewardDurationHours / 24} Days Free" else "${msg.rewardDurationHours.coerceAtLeast(24)}h Free"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF6F3FF))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = msg.rewardItemName.ifBlank { msg.rewardItemId },
                                                color = InboxTextPrimary,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "⏱️ $durationLabel • Instant 3D Equip",
                                                color = InboxAccentIndigo,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (equippingNotifId != null) return@Button
                                                equippingNotifId = msg.id
                                                ProgressionAutomationService.equipInboxReward(
                                                    context = context,
                                                    currentUser = currentUser,
                                                    notification = msg,
                                                    db = db,
                                                    onUserUpdated = onUserUpdated
                                                ) { ok ->
                                                    equippingNotifId = null
                                                    if (ok) locallyClaimedIds.add(msg.id)
                                                }
                                            },
                                            enabled = equippingNotifId != msg.id,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isClaimed) Color(0xFF10B981) else InboxAccentIndigo
                                            ),
                                            shape = RoundedCornerShape(50),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            if (equippingNotifId == msg.id) {
                                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                                            } else {
                                                Text(
                                                    text = if (isClaimed) "Equipped ✅" else "Equip Now ✨",
                                                    color = Color.White,
                                                    fontSize = 11.5.sp,
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
            }
        }
    }
}

@Composable
private fun QuickAddContactDialog(
    currentUserId: String,
    currentUser: UserProfile,
    allProfiles: List<UserProfile>,
    db: FirebaseFirestore,
    onOpenChat: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) {
            searchResults = allProfiles.filter { it.uid != currentUserId }.take(8)
            return@LaunchedEffect
        }
        val local = allProfiles.filter {
            it.uid != currentUserId && (
                it.name.contains(q, ignoreCase = true) ||
                    it.userId.contains(q, ignoreCase = true) ||
                    it.uid.equals(q, ignoreCase = true)
                )
        }
        searchResults = local
        if (local.isEmpty()) {
            isSearching = true
            db.collection("users").whereEqualTo("userId", q).limit(5).get()
                .addOnSuccessListener { snap ->
                    isSearching = false
                    if (snap != null && !snap.isEmpty) {
                        searchResults = snap.documents.map { parseUserProfileFromDoc(it).copy(uid = it.id) }
                            .filter { it.uid != currentUserId }
                    }
                }
                .addOnFailureListener { isSearching = false }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Add Contact", color = InboxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Enter User ID or Name...", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = InboxAccentIndigo, modifier = Modifier.size(22.dp))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults, key = { it.uid }) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenChat(user) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100" },
                                    contentDescription = user.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.name, color = InboxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("ID: ${user.userId.ifBlank { user.uid.take(8) }}", color = InboxTextSecondary, fontSize = 11.sp)
                                }
                                TextButton(onClick = {
                                    // Follow & open chat immediately
                                    val now = System.currentTimeMillis()
                                    db.collection("users").document(currentUserId)
                                        .collection("following").document(user.uid)
                                        .set(mapOf("uid" to user.uid, "name" to user.name, "photo" to user.photo, "timestamp" to now), SetOptions.merge())
                                    Toast.makeText(context, "Added ${user.name} to Contacts!", Toast.LENGTH_SHORT).show()
                                    onOpenChat(user)
                                }) {
                                    Text("Add +", color = InboxAccentIndigo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = InboxTextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

private fun formatInboxTime(timestampMs: Long): String {
    if (timestampMs <= 0L) return ""
    val now = Calendar.getInstance()
    val cal = Calendar.getInstance().apply { timeInMillis = timestampMs }
    return if (now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    ) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestampMs))
    } else {
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestampMs))
    }
}
