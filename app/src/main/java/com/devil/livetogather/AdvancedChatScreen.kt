package com.devil.livetogather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Advanced1v1ChatScreen(
    currentUserId: String,
    currentUser: UserProfile,
    partner: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onUpdateCurrentUser: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val matchId = remember(currentUserId, partner.uid) {
        listOf(currentUserId, partner.uid).sorted().joinToString("_")
    }

    // Real-time Chat State
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    var matchItem by remember { mutableStateOf<MatchItem?>(null) }
    var chatInput by remember { mutableStateOf("") }
    val isBlocked = currentUser.blockedUserIds.contains(partner.uid)

    // Mutual Follow State
    var isMutualFollow by remember { mutableStateOf<Boolean?>(null) }
    var amIFollowingPartner by remember { mutableStateOf(false) }
    var isPartnerFollowingMe by remember { mutableStateOf(false) }

    // Dialog & UI States
    var showChatMenu by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showReportConfirmDialog by remember { mutableStateOf(false) }
    var showProposalDialog by remember { mutableStateOf(false) }
    var showMediaTrustDialog by remember { mutableStateOf(false) }

    // Voice Note State
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceRecordDuration by remember { mutableIntStateOf(0) }
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val dataUri = "data:image/jpeg;base64,$base64"
                    val imgMsg = ChatMessage(
                        senderId = currentUserId,
                        text = "📷 Photo",
                        type = "image",
                        mediaUrl = dataUri,
                        timestamp = System.currentTimeMillis()
                    )
                    db.collection("matches").document(matchId).collection("messages").add(imgMsg)
                    db.collection("matches").document(matchId).update("lastMessage", "📷 Photo")
                    Toast.makeText(context, "Photo sent! ✨", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val started = ChatMediaEngine.startVoiceRecording(context)
            if (started) {
                isRecordingVoice = true
                voiceRecordDuration = 0
            }
        } else {
            Toast.makeText(context, "Microphone permission is required to record voice clips.", Toast.LENGTH_SHORT).show()
        }
    }

    // Timer for voice recording
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            while (isRecordingVoice) {
                delay(1000L)
                voiceRecordDuration += 1
            }
        }
    }

    // 1. Listen to Match Document (Permissions, Intimacy XP, Unlock status)
    LaunchedEffect(matchId) {
        db.collection("matches").document(matchId)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    matchItem = snap.toObject(MatchItem::class.java)
                }
            }
    }

    // 2. Listen to Chat Messages in real-time
    LaunchedEffect(matchId) {
        db.collection("matches").document(matchId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val msgs = snap.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
                    chatMessages.clear()
                    chatMessages.addAll(msgs)
                }
            }
    }

    // 3. Check Mutual Follow Status
    fun refreshFollowStatus() {
        if (partner.uid.startsWith("companion_")) {
            // For companion demo user, mutual follow is guaranteed & auto-synced
            isMutualFollow = true
            amIFollowingPartner = true
            isPartnerFollowingMe = true
            db.collection("users").document(currentUserId).collection("followers").document(partner.uid)
                .set(mapOf("uid" to partner.uid, "name" to partner.name, "timestamp" to System.currentTimeMillis()), SetOptions.merge())
            db.collection("users").document(partner.uid).collection("followers").document(currentUserId)
                .set(mapOf("uid" to currentUserId, "name" to currentUser.name, "timestamp" to System.currentTimeMillis()), SetOptions.merge())
        } else {
            ChatMediaEngine.checkMutualFollow(db, currentUserId, partner.uid) { isMutual, amIFollowing, partnerFollows ->
                isMutualFollow = isMutual
                amIFollowingPartner = amIFollowing
                isPartnerFollowingMe = partnerFollows
            }
        }
    }

    LaunchedEffect(currentUserId, partner.uid) {
        refreshFollowStatus()
    }

    val listState = rememberLazyListState()
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val isChatUnlocked = (matchItem?.isUnlocked == true) || chatMessages.any { it.type == "unlock_gift" }
    val currentIntimacyLevel = matchItem?.intimacyLevel ?: 1
    val currentStreak = matchItem?.chatStreakDays ?: 1
    val canSendMedia = matchItem?.mediaSharingAllowed?.get(currentUserId) == true
    val isHeartUnlocked = currentStreak >= 2 || (matchItem?.intimacyXp ?: 0L) >= 100L
    val isDoubleHeartUnlocked = currentIntimacyLevel >= 5

    // Pulsing heart animation
    val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandDark)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // -------------------------------------------------------------
            // 1. CHAT HEADER (BACK, AVATAR, STREAK BADGE, HEARTS & 3-DOTS)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandCard)
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = partner.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                        contentDescription = partner.name,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, if (isDoubleHeartUnlocked) Color(0xFFFF4081) else Color(0xFFFFD700), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color(0xFF00E676),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = Modifier.size(10.dp)
                    ) {}
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = partner.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        if (isBlocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "[Blocked]",
                                color = Color.Red,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Intimacy Streak & Level Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = Color(0xFF221538),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.6.dp, Color(0xFFFFD700).copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = "🔥 Day $currentStreak • Lv.$currentIntimacyLevel",
                                color = Color(0xFFFFD700),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Heart Proposal Trigger Icon (Single Heart / Double Heart)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (isHeartUnlocked) {
                                showProposalDialog = true
                            } else {
                                Toast.makeText(context, "Keep chatting to reach Level 2 and unlock Intimacy Proposal! ❤️", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDoubleHeartUnlocked) {
                        Text(
                            text = "💞",
                            fontSize = 22.sp,
                            modifier = Modifier.scale(heartScale)
                        )
                    } else if (isHeartUnlocked) {
                        Text(
                            text = "❤️",
                            fontSize = 20.sp,
                            modifier = Modifier.scale(heartScale)
                        )
                    } else {
                        Text(
                            text = "🤍",
                            fontSize = 18.sp,
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                }

                // 3-Dots Menu
                Box {
                    IconButton(onClick = { showChatMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showChatMenu,
                        onDismissRequest = { showChatMenu = false },
                        modifier = Modifier.background(Color(0xFF1E1430)),
                        properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                    ) {
                        DropdownMenuItem(
                            text = { Text("🧹 Clear Chat History", color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                showChatMenu = false
                                showClearConfirmDialog = true
                            }
                        )

                        val partnerAllowed = matchItem?.mediaSharingAllowed?.get(partner.uid) == true
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (partnerAllowed) "📷 Allow ${partner.name} Media: ON" else "📷 Allow ${partner.name} Media: OFF",
                                        color = if (partnerAllowed) Color(0xFF00E676) else Color.LightGray,
                                        fontSize = 12.5.sp
                                    )
                                }
                            },
                            onClick = {
                                showChatMenu = false
                                val newSetting = !partnerAllowed
                                ChatMediaEngine.setMediaSharingPermission(db, matchId, partner.uid, newSetting) { success ->
                                    if (success) {
                                        Toast.makeText(context, if (newSetting) "Enabled photo sharing for ${partner.name}" else "Disabled photo sharing for ${partner.name}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("🚨 Report User", color = Color(0xFFFF9100), fontSize = 13.sp) },
                            onClick = {
                                showChatMenu = false
                                showReportConfirmDialog = true
                            }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isBlocked) "✅ Unblock User" else "🚫 Block User",
                                    color = if (isBlocked) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                showChatMenu = false
                                val updatedBlocked = if (isBlocked) currentUser.blockedUserIds - partner.uid else currentUser.blockedUserIds + partner.uid
                                val updated = currentUser.copy(blockedUserIds = updatedBlocked)
                                onUpdateCurrentUser(updated)
                                db.collection("users").document(currentUserId).update("blockedUserIds", updatedBlocked)
                                Toast.makeText(context, if (isBlocked) "${partner.name} unblocked" else "${partner.name} blocked", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. CHAT MESSAGES LIST
            // -------------------------------------------------------------
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mutual Follow Warning Banner inside chat if not mutual
                if (isMutualFollow == false) {
                    item {
                        Surface(
                            color = Color(0xFF2A1B38),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔒 Mutual Follow Required", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Both you and ${partner.name} must follow each other before chatting.",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                if (!amIFollowingPartner) {
                                    Button(
                                        onClick = {
                                            ChatMediaEngine.followUser(db, currentUser, partner) { success ->
                                                if (success) {
                                                    Toast.makeText(context, "Followed ${partner.name}! 🌟", Toast.LENGTH_SHORT).show()
                                                    refreshFollowStatus()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("+ Follow ${partner.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else {
                                    Text("⏳ You followed ${partner.name}. Waiting for follow back...", color = Color(0xFFFFD700), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // 5,000 💎 Luxury First Message Unlock Card
                if (isMutualFollow == true && !isChatUnlocked) {
                    item {
                        Surface(
                            color = Color(0xFF1E1430),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("👑 First Message Exclusive Unlock", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Send a 5,000 💎 Luxury Introduction Gift to ${partner.name} to unlock permanent direct messaging.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Your Balance: ${currentUser.diamonds} 💎", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        ChatMediaEngine.unlockFirstMessageWithGift(
                                            db = db,
                                            matchId = matchId,
                                            sender = currentUser,
                                            receiver = partner,
                                            giftDiamonds = 5000L
                                        ) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            if (success) {
                                                onUpdateCurrentUser(currentUser.copy(diamonds = (currentUser.diamonds - 5000L).coerceAtLeast(0L)))
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text("🎁 Send 5,000 💎 & Unlock Chat", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Render Messages
                items(chatMessages) { msg ->
                    val isMe = msg.senderId == currentUserId

                    when (msg.type) {
                        "unlock_gift" -> {
                            // Luxury Unlock Announcement
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    color = Color(0xFF2B1D0E),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.8f)),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("👑", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(msg.text, color = Color(0xFFFFD700), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        "proposal" -> {
                            // Intimacy Proposal Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    color = Color(0xFF261033),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFFFF4081)),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("💖 ${msg.proposalBondType.uppercase()} PROPOSAL", color = Color(0xFFFF4081), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(msg.text, color = Color.White, fontSize = 12.5.sp)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        when (msg.proposalStatus) {
                                            "pending" -> {
                                                if (!isMe) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Button(
                                                            onClick = {
                                                                val prop = IntimacyProposal(
                                                                    proposalId = "prop_${msg.timestamp}",
                                                                    matchId = matchId,
                                                                    senderUid = msg.senderId,
                                                                    senderName = partner.name,
                                                                    senderPhoto = partner.photo,
                                                                    receiverUid = currentUserId,
                                                                    receiverName = currentUser.name,
                                                                    receiverPhoto = currentUser.photo,
                                                                    bondType = msg.proposalBondType
                                                                )
                                                                ChatMediaEngine.respondToProposal(
                                                                    db = db,
                                                                    matchId = matchId,
                                                                    proposalId = "prop_${msg.timestamp}",
                                                                    proposal = prop,
                                                                    accept = true,
                                                                    currentUser = currentUser
                                                                ) { success, resultMsg ->
                                                                    Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                                                            shape = RoundedCornerShape(50),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("Accept 💖", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                        }

                                                        OutlinedButton(
                                                            onClick = {
                                                                val prop = IntimacyProposal(senderUid = msg.senderId, receiverUid = currentUserId)
                                                                ChatMediaEngine.respondToProposal(db, matchId, "prop_${msg.timestamp}", prop, false, currentUser) { _, _ -> }
                                                            },
                                                            shape = RoundedCornerShape(50),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("Decline", color = Color.LightGray, fontSize = 11.sp)
                                                        }
                                                    }
                                                } else {
                                                    Text("⏳ Waiting for ${partner.name} to accept...", color = Color.White.copy(alpha = 0.6f), fontSize = 10.5.sp)
                                                }
                                            }
                                            "accepted" -> {
                                                Text("✅ Official Bond Active! ⚡ 3D Mic Link Connected", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                            else -> {
                                                Text("❌ Proposal Declined", color = Color(0xFFFF5252), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "image" -> {
                            // Photo Bubble
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(BrandCard)
                                        .border(1.dp, if (isMe) BrandCoral else Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                ) {
                                    AsyncImage(
                                        model = msg.mediaUrl,
                                        contentDescription = "Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        "audio" -> {
                            // Voice Note Audio Bubble
                            val isPlaying = currentlyPlayingUrl == msg.audioUrl
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    color = if (isMe) Color(0xFF2E1742) else Color(0xFF1E2235),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, if (isMe) Color(0xFFFF4081) else Color(0xFF00E5FF)),
                                    modifier = Modifier.widthIn(min = 160.dp, max = 240.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (isPlaying) {
                                                    ChatMediaEngine.stopVoicePlayback()
                                                    currentlyPlayingUrl = null
                                                } else {
                                                    ChatMediaEngine.playVoiceNote(
                                                        context = context,
                                                        audioSource = msg.audioUrl,
                                                        onCompletion = { currentlyPlayingUrl = null },
                                                        onError = {
                                                            currentlyPlayingUrl = null
                                                            Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                    currentlyPlayingUrl = msg.audioUrl
                                                }
                                            },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(if (isMe) Color(0xFFFF4081) else Color(0xFF00E5FF), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            // Slender Waveform Visualization
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val heights = listOf(6, 12, 16, 8, 14, 10, 16, 12, 8, 14, 6)
                                                heights.forEach { h ->
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.5.dp)
                                                            .height(if (isPlaying) (h * heartScale).dp else h.dp)
                                                            .clip(RoundedCornerShape(1.dp))
                                                            .background(if (isPlaying) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f))
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (msg.audioDurationSec > 0) "0:${String.format(Locale.getDefault(), "%02d", msg.audioDurationSec)}" else "Voice clip",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            // VIP Styled Text Bubble (Crystal beveled border & VIP badge pill)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                val effectiveFrameId = msg.chatFrameId.ifEmpty {
                                    if (isMe) currentUser.activeChatFrame else partner.activeChatFrame
                                }
                                val effectiveVipLvl = if (msg.vipLevel > 0) msg.vipLevel else if (isMe) currentUser.getEffectiveWealthLevel() else partner.getEffectiveWealthLevel()
                                val effectiveIsFounder = msg.isFounder || (if (isMe) currentUser.isFounder() else partner.isFounder())
                                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))

                                VipStyledChatBubble(
                                    messageText = msg.text,
                                    isMe = isMe,
                                    chatFrameId = effectiveFrameId,
                                    vipLevel = effectiveVipLvl,
                                    isFounder = effectiveIsFounder,
                                    timestampText = timeStr
                                )
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. WHATSAPP-STYLE BOTTOM INPUT BAR (Lifted above System Nav Bar)
            // -------------------------------------------------------------
            val doSendChatMessage = {
                val rawText = chatInput.trim()
                if (rawText.isNotBlank()) {
                    val (isAbuse, badWord) = AbuseFilterEngine.checkAbuse(rawText)
                    if (isAbuse) {
                        Toast.makeText(
                            context,
                            "🚫 Blocked: Inappropriate word ($badWord) is prohibited in LiveTogather!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Aggressive shouting / all-caps check
                        val isAllCaps = rawText.length >= 6 && rawText == rawText.uppercase() && rawText.any { it.isLetter() }
                        val isExcessivePunctuation = rawText.count { it == '!' } >= 3 || rawText.count { it == '?' } >= 4
                        if (isAllCaps || isExcessivePunctuation) {
                            Toast.makeText(
                                context,
                                "⚠️ Message sent: Please communicate calmly without aggressive shouting or all-caps.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        chatInput = ""

                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val lastDate = matchItem?.lastChatDate ?: ""
                        val isNewDay = lastDate != todayStr

                        val newStreak = if (isNewDay) (matchItem?.chatStreakDays ?: 0) + 1 else (matchItem?.chatStreakDays ?: 1)
                        val newXp = (matchItem?.intimacyXp ?: 0L) + (if (isNewDay) 50L else 5L)
                        val newLevel = (1 + (newXp / 100).toInt()).coerceAtLeast(1)

                        val newMsg = ChatMessage(
                            senderId = currentUserId,
                            text = rawText,
                            type = "text",
                            chatFrameId = currentUser.activeChatFrame,
                            vipLevel = currentUser.getEffectiveWealthLevel(),
                            isFounder = currentUser.isFounder()
                        )
                        db.collection("matches").document(matchId).collection("messages").add(newMsg)

                        db.collection("matches").document(matchId).set(
                            mapOf(
                                "lastMessage" to rawText,
                                "chatStreakDays" to newStreak,
                                "intimacyXp" to newXp,
                                "intimacyLevel" to newLevel,
                                "lastChatDate" to todayStr,
                                "timestamp" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )
                    }
                }
            }

            Surface(
                color = BrandCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                if (isBlocked) {
                    // Blocked state bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("You have blocked ${partner.name}.", color = Color.LightGray, fontSize = 12.sp)
                    }
                } else if (isMutualFollow == false || !isChatUnlocked) {
                    // Input locked until mutual follow and unlock gift
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMutualFollow == false) "Follow each other to unlock chat" else "Send 5,000 💎 gift to unlock messaging",
                            color = Color.Gray,
                            fontSize = 11.5.sp
                        )
                    }
                } else if (isRecordingVoice) {
                    // Active Voice Note Recording Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF261022))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.Red,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(12.dp)
                                    .scale(heartScale)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Recording... 0:${String.format(Locale.getDefault(), "%02d", voiceRecordDuration)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    ChatMediaEngine.cancelVoiceRecording()
                                    isRecordingVoice = false
                                    voiceRecordDuration = 0
                                }
                            ) {
                                Text("Cancel ❌", color = Color.Gray, fontSize = 11.5.sp)
                            }

                            Button(
                                onClick = {
                                    val (file, dur) = ChatMediaEngine.stopVoiceRecording()
                                    isRecordingVoice = false
                                    voiceRecordDuration = 0
                                    if (file != null && file.exists()) {
                                        val dataUri = ChatMediaEngine.convertAudioFileToBase64DataUri(file)
                                        if (dataUri.isNotBlank()) {
                                            val voiceMsg = ChatMessage(
                                                senderId = currentUserId,
                                                text = "🎙️ Voice Clip (0:${String.format(Locale.getDefault(), "%02d", dur)})",
                                                type = "audio",
                                                audioUrl = dataUri,
                                                audioDurationSec = dur,
                                                timestamp = System.currentTimeMillis()
                                            )
                                            db.collection("matches").document(matchId).collection("messages").add(voiceMsg)
                                            db.collection("matches").document(matchId).update("lastMessage", "🎙️ Voice note")
                                            Toast.makeText(context, "Voice note sent! 🎙️", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Send 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }
                        }
                    }
                } else {
                    // Standard Chat Input Bar (Attachment, Text Field, Mic & Dedicated Send Button)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Attachment Icon (Conditional Trust Media Sharing)
                        IconButton(
                            onClick = {
                                if (canSendMedia) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    showMediaTrustDialog = true
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF261B36), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach Media",
                                tint = if (canSendMedia) Color(0xFF00E5FF) else Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // 2. Rounded Text Field with Enter-to-Send
                        Surface(
                            color = Color(0xFF1B1428),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            TextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Type a message...", color = Color.Gray, fontSize = 13.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = { doSendChatMessage() }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // 3. Mic Button for Real-Time Voice Recording
                        IconButton(
                            onClick = {
                                val hasAudioPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                if (hasAudioPerm) {
                                    val started = ChatMediaEngine.startVoiceRecording(context)
                                    if (started) {
                                        isRecordingVoice = true
                                        voiceRecordDuration = 0
                                    }
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF261B36), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record Voice Clip",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // 4. Prominent Dedicated Send Button (Always Visible & Active Highlight)
                        val hasSendText = chatInput.trim().isNotEmpty()
                        IconButton(
                            onClick = { doSendChatMessage() },
                            enabled = hasSendText,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (hasSendText) Brush.linearGradient(listOf(BrandCoral, Color(0xFFFF4081)))
                                    else Brush.linearGradient(listOf(Color(0xFF261B36), Color(0xFF261B36))),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (hasSendText) Color.White else Color.Gray,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 4. SUB-DIALOGS & MODALS
        // -------------------------------------------------------------

        // Clear Chat Confirmation
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                containerColor = Color(0xFF1E1430),
                shape = RoundedCornerShape(20.dp),
                title = { Text("Clear Chat?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("All messages in this chat will be cleared.", color = Color.LightGray, fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            ChatMediaEngine.clearChatHistory(db, matchId) { success ->
                                if (success) {
                                    chatMessages.clear()
                                    Toast.makeText(context, "Chat cleared", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showClearConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral)
                    ) {
                        Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // Report User Confirmation
        if (showReportConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showReportConfirmDialog = false },
                containerColor = Color(0xFF1E1430),
                shape = RoundedCornerShape(20.dp),
                title = { Text("Report User 🚨", color = Color(0xFFFF9100), fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to report ${partner.name} for abusive or inappropriate behavior?", color = Color.LightGray, fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            ChatMediaEngine.reportUser(db, currentUserId, currentUser.name, partner.uid, partner.name, "Inappropriate chat behavior") { success ->
                                Toast.makeText(context, "Report submitted to moderators. Thank you.", Toast.LENGTH_LONG).show()
                            }
                            showReportConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100))
                    ) {
                        Text("Report", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportConfirmDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // Media Trust Review Dialog (When partner has not enabled media sharing)
        if (showMediaTrustDialog) {
            AlertDialog(
                onDismissRequest = { showMediaTrustDialog = false },
                containerColor = Color(0xFF1E1430),
                shape = RoundedCornerShape(20.dp),
                title = { Text("🔒 Media Sharing Restricted", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Your trust level is under review. You need to build more trust with ${partner.name} to unlock media sharing.",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tip: Keep chatting daily, or ask ${partner.name} to enable media permissions for you from their chat options menu.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            ChatMediaEngine.requestMediaSharingPermission(db, matchId, currentUserId, currentUser.name) {
                                Toast.makeText(context, "Permission request sent to ${partner.name} 📩", Toast.LENGTH_SHORT).show()
                            }
                            showMediaTrustDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("Request Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMediaTrustDialog = false }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            )
        }

        // Intimacy Proposal Dialog (Strict 7 Bonds Limit)
        if (showProposalDialog) {
            AlertDialog(
                onDismissRequest = { showProposalDialog = false },
                containerColor = Color(0xFF181028),
                shape = RoundedCornerShape(20.dp),
                title = { Text("💖 Propose Intimacy Bond", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Propose an official bonded relationship with ${partner.name}. When accepted, your 3D Mic Link will ignite whenever you sit on stage together!",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ Maximum Limit: Each user can only have up to 7 Intimacy Bonds in total.",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val bondTypes = listOf(
                            Triple("bestie", "Twin Besties 🌸", "Twin glowing hearts with pavé diamonds"),
                            Triple("couple", "Royal Ruby Crown 👑", "Imperial ruby heart and 24K gold crown"),
                            Triple("soulmate", "Celestial Wings ✨", "Diamond heart with radiant angel wings"),
                            Triple("eternal", "Emerald Infinity 💚", "Lush emerald gemstone in eternity loop")
                        )

                        bondTypes.forEach { (type, label, desc) ->
                            Surface(
                                onClick = {
                                    showProposalDialog = false
                                    ChatMediaEngine.sendIntimacyProposal(
                                        db = db,
                                        matchId = matchId,
                                        sender = currentUser,
                                        receiver = partner,
                                        bondType = type
                                    ) { success, resultMsg ->
                                        Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                color = Color(0xFF2B1D40),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    Text(desc, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showProposalDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}
}
