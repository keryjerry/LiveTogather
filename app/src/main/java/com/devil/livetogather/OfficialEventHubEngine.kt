package com.devil.livetogather

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.UUID

// -------------------------------------------------------------
// 1. ULTRA-DENSE GALA AUDIO EQUALIZER (64-BAND DENSE FREQUENCY SPECTRUM)
// -------------------------------------------------------------
@Composable
fun DenseGalaAudioEqualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 48,
    styleTheme: String = "gold"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dense_eq")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing), RepeatMode.Restart),
        label = "eq_phase"
    )
    val bounceFactor by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq_bounce"
    )

    val barColors = when (styleTheme.lowercase()) {
        "cyan", "cyber_neon" -> listOf(Color(0xFF00F0FF), Color(0xFFFF2A6D), Color(0xFF38BDF8))
        "emerald", "liquid_emerald" -> listOf(Color(0xFF00FFA3), Color(0xFF10B981), Color(0xFF34D399))
        "crimson", "sunset_crimson" -> listOf(Color(0xFFFF3366), Color(0xFFF59E0B), Color(0xFFEF4444))
        else -> listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFA855F7), Color(0xFFEC4899)) // MegaStar Cosmic Gold
    }

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val maxHeight = size.height
        if (totalWidth <= 0f || maxHeight <= 0f) return@Canvas

        val barWidth = (totalWidth / barCount) * 0.72f
        val gap = (totalWidth / barCount) * 0.28f

        for (i in 0 until barCount) {
            val norm = i.toFloat() / barCount.toFloat()
            val harmonic1 = kotlin.math.sin(phase + norm * 3.5 * Math.PI).toFloat()
            val harmonic2 = kotlin.math.cos(phase * 1.8f + norm * 5.2 * Math.PI).toFloat()
            val harmonic3 = kotlin.math.sin(phase * 0.7f + norm * 2.0 * Math.PI).toFloat()
            val envelope = kotlin.math.sin(norm * Math.PI).toFloat().coerceIn(0.25f, 1f)
            val combined = ((harmonic1 + harmonic2 + harmonic3 + 3f) / 6f) * envelope * bounceFactor
            val h = (combined * maxHeight * 0.95f).coerceIn(3.dp.toPx(), maxHeight)

            val x = i * (barWidth + gap)
            val y = maxHeight - h

            val colorRatio = (norm + (phase / (2 * Math.PI).toFloat())) % 1f
            val chosenColor = barColors[(colorRatio * barColors.size).toInt().coerceIn(0, barColors.size - 1)]

            drawRoundRect(
                color = chosenColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

// -------------------------------------------------------------
// 2. DEDICATED OFFICIAL EVENT ARENA HUB MODAL (EXACT STRUCTURE)
// -------------------------------------------------------------
@Composable
fun OfficialEventArenaHubModal(
    currentUser: UserProfile,
    allRooms: List<RoomItem>,
    allProfiles: List<UserProfile> = emptyList(),
    onDismiss: () -> Unit,
    onJoinRoom: (RoomItem) -> Unit,
    onOpenTopUpStore: () -> Unit = {}
) {
    val context = LocalContext.current
    var showSchedulingDialog by remember { mutableStateOf(false) }
    var insufficientDiamondsRoom by remember { mutableStateOf<RoomItem?>(null) }
    var hubSearchQuery by remember { mutableStateOf("") }

    val isUserMonetizedHost = currentUser.isHostMonetized || currentUser.isAdmin || currentUser.isFounder() || currentUser.isOfficialAdmin()

    // 100% Real-Time Firestore Synced Official Gala Rooms
    val galaRooms = remember(allRooms.map { "${it.id}_${it.isHostLive}_${it.activeUserCount}_${it.title}_${it.eventThemeStyle}" }) {
        allRooms.filter {
            (it.category.equals("official_event", ignoreCase = true) ||
             it.category.equals("mega_star_gala", ignoreCase = true) ||
             it.title.contains("Gala", ignoreCase = true) ||
             it.title.contains("Official Event", ignoreCase = true)) &&
            !it.statusMessage.equals("DELETED", ignoreCase = true) &&
            !it.statusMessage.equals("CLOSED", ignoreCase = true)
        }
    }

    val filteredGalaRooms = remember(galaRooms, hubSearchQuery) {
        if (hubSearchQuery.isBlank()) galaRooms
        else galaRooms.filter {
            it.title.contains(hubSearchQuery, ignoreCase = true) ||
            it.hostName.contains(hubSearchQuery, ignoreCase = true) ||
            it.id.contains(hubSearchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0A0714),
            border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFFF59E0B))))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                
                // TOP HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = Color(0xFFEC4899).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color(0xFFEC4899))
                            ) {
                                Text("🏆 OFFICIAL EVENT ARENA", color = Color(0xFFF472B6), fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                            }
                            Surface(
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Text("🔒 200k 💎 Entry", color = Color(0xFFFBBF24), fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Mega Stars Celebration Gala 🌟", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TIMING & PERKS BAR
                Surface(
                    color = Color(0xFF160F26),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.8.dp, Color(0xFF3B2F50)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("⏱️ 12 PM - 5 PM (Pre-Booking) • 5 PM - 6 AM (Live Arena)", color = Color(0xFFE2E8F0), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            Text("🔥 3x Gift Multiplier • 15-Mic Royal Amphitheater", color = Color(0xFFA78BFA), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }

                        // ORGANIZE EVENT BUTTON (HOSTS ONLY)
                        Button(
                            onClick = {
                                val existingUserRoom = allRooms.firstOrNull { 
                                    ((it.hostUid.isNotBlank() && it.hostUid == currentUser.uid) || 
                                     (it.hostId.isNotBlank() && it.hostId == currentUser.uid)) &&
                                    it.category != "video_stream" && it.roomType != "video" && !it.isVideoRoom
                                }
                                if (existingUserRoom != null) {
                                    onJoinRoom(existingUserRoom.copy(roomType = "audio"))
                                } else if (isUserMonetizedHost) {
                                    showSchedulingDialog = true
                                } else {
                                    Toast.makeText(context, "👑 Only verified & monetized hosts can organize Official Gala Events!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("+ Organize", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time Search Field
                OutlinedTextField(
                    value = hubSearchQuery,
                    onValueChange = { hubSearchQuery = it },
                    placeholder = { Text("🔍 Search Mega Star Gala room or host...", color = Color.Gray, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF3B2F50),
                        focusedContainerColor = Color(0xFF130E20),
                        unfocusedContainerColor = Color(0xFF130E20)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ROOMS LIST HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎪 Live Official Event Rooms (${filteredGalaRooms.size})", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Tap to Enter", color = Color(0xFFF472B6), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (filteredGalaRooms.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🌟", fontSize = 36.sp)
                            Text(if (hubSearchQuery.isBlank()) "No Official Event Rooms Live Right Now" else "No Matching Gala Rooms Found", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Verified hosts can tap '+ Organize' above to launch the 15-Mic Gala Stage!", color = Color.Gray, fontSize = 11.sp, maxLines = 2)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredGalaRooms) { r ->
                            val roomTheme = if (r.eventThemeStyle.isNotBlank()) r.eventThemeStyle else "gold"
                            val cardBorder = when (roomTheme.lowercase()) {
                                "cyan", "cyber_neon" -> Color(0xFF00F0FF)
                                "emerald", "liquid_emerald" -> Color(0xFF00FFA3)
                                "crimson", "sunset_crimson" -> Color(0xFFFF3366)
                                else -> Color(0xFFF59E0B)
                            }

                            Surface(
                                color = Color(0xFF130E20),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // 200,000 DIAMONDS GATE CHECK
                                        val isOwner = currentUser.uid.isNotBlank() && (r.hostUid == currentUser.uid || r.hostId == currentUser.uid)
                                        val isStaff = currentUser.isAdmin || currentUser.isFounder()
                                        if (currentUser.diamonds < 200000L && !isOwner && !isStaff) {
                                            insufficientDiamondsRoom = r
                                        } else {
                                            onJoinRoom(r.copy(roomType = "audio"))
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.2.dp, cardBorder, RoundedCornerShape(12.dp))
                                        ) {
                                            AsyncImage(
                                                model = r.hostPhotoUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100" },
                                                contentDescription = r.hostName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Surface(color = Color(0xFFEF4444), shape = RoundedCornerShape(4.dp)) {
                                                    Text("LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                }
                                                Text(
                                                    text = if (r.roomType == "video") "📹 Video Arena" else "🎙️ 15-Mic Gala Stage",
                                                    color = cardBorder,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(r.title.ifBlank { "Mega Stars Gala Event" }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("Host: ${r.hostName.ifBlank { "Verified Star" }} • 👥 ${r.activeUserCount} online", color = Color.Gray, fontSize = 10.sp)
                                        }

                                        Surface(
                                            color = cardBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Enter ➔", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // DISTINCT EQUALIZER STYLE FOR EACH ROOM CARD
                                    DenseGalaAudioEqualizer(
                                        modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 4.dp, vertical = 2.dp),
                                        barCount = 36,
                                        styleTheme = roomTheme
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 200,000 DIAMONDS INSUFFICIENT POPUP
    if (insufficientDiamondsRoom != null) {
        InsufficientDiamondsDialog(
            currentUser = currentUser,
            onDismiss = { insufficientDiamondsRoom = null },
            onTopUp = {
                insufficientDiamondsRoom = null
                onOpenTopUpStore()
            }
        )
    }

    // SCHEDULING & CREATION DIALOG
    if (showSchedulingDialog) {
        OfficialEventSchedulingDialog(
            currentUser = currentUser,
            onDismiss = { showSchedulingDialog = false },
            onEventCreated = { createdRoom ->
                showSchedulingDialog = false
                onJoinRoom(createdRoom)
            }
        )
    }
}

// -------------------------------------------------------------
// 3. INSUFFICIENT DIAMONDS DIALOG (200k STRICT REQUIREMENT)
// -------------------------------------------------------------
@Composable
fun InsufficientDiamondsDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onTopUp: () -> Unit
) {
    val shortage = (200000L - currentUser.diamonds).coerceAtLeast(0L)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF140C20),
            border = BorderStroke(1.5.dp, Color(0xFFEC4899))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFEC4899).copy(alpha = 0.2f)).border(1.5.dp, Color(0xFFEC4899), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💎", fontSize = 24.sp)
                }

                Text("Insufficient Diamonds!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Text(
                    text = "You need a minimum of 2,00,000 💎 Diamonds in your wallet to enter this Official Mega Stars Gala Event.",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, Color(0xFFEC4899).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Balance:", color = Color.Gray, fontSize = 11.sp)
                        Text("%,d 💎".format(currentUser.diamonds), color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Text("Cancel", color = Color.LightGray, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onTopUp,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                    ) {
                        Text("Top Up Now 💎", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. OFFICIAL EVENT SCHEDULING & CREATION DIALOG (SEPARATE FLOW)
// -------------------------------------------------------------
@Composable
fun OfficialEventSchedulingDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onEventCreated: (RoomItem) -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var eventTitle by remember { mutableStateOf("Mega Stars Celebration Gala 2026 🌟") }
    var selectedStage by remember { mutableStateOf("voice_15") } // "voice_15" or "video"
    var selectedTheme by remember { mutableStateOf("gold") } // "gold", "neon", "emerald", "crimson"
    var selectedSlot by remember { mutableStateOf("prime") } // "prime" (5 PM - 6 AM) or "booking" (12 PM - 5 PM)
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F0B18),
            border = BorderStroke(1.2.dp, Color(0xFFF59E0B))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("👑 Organize Official Gala Event", color = Color(0xFFF59E0B), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Text("15-Mic Royal Amphitheater & Exclusive Themes", color = Color.Gray, fontSize = 10.5.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // 1. Title Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("1. इवेंट शीर्षक (Title)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF3B2F50)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2. Schedule Slot Picker
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("2. टाइम स्लॉट शेड्यूलिंग (Time Slot)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = if (selectedSlot == "prime") Color(0xFFEC4899).copy(alpha = 0.3f) else Color(0xFF1B142B),
                            border = BorderStroke(1.dp, if (selectedSlot == "prime") Color(0xFFEC4899) else Color(0xFF3B2F50)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).clickable { selectedSlot = "prime" }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("🔴 Prime-Time Arena", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("05:00 PM – 06:00 AM", color = Color(0xFFF472B6), fontSize = 9.sp)
                            }
                        }

                        Surface(
                            color = if (selectedSlot == "booking") Color(0xFFF59E0B).copy(alpha = 0.3f) else Color(0xFF1B142B),
                            border = BorderStroke(1.dp, if (selectedSlot == "booking") Color(0xFFF59E0B) else Color(0xFF3B2F50)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).clickable { selectedSlot = "booking" }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("⏳ Pre-Booking Slot", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("12:00 PM – 05:00 PM", color = Color(0xFFFBBF24), fontSize = 9.sp)
                            }
                        }
                    }
                }

                // 3. Stage Format Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("3. स्टेज फॉर्मेट (Stage Format)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = if (selectedStage == "voice_15") Color(0xFF8B5CF6).copy(alpha = 0.3f) else Color(0xFF1B142B),
                            border = BorderStroke(1.2.dp, if (selectedStage == "voice_15") Color(0xFF8B5CF6) else Color(0xFF3B2F50)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).clickable { selectedStage = "voice_15" }
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("👑", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("15-Mic Royal Stage", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text("15 Mics Amphitheater", color = Color(0xFFA78BFA), fontSize = 8.5.sp)
                                }
                            }
                        }

                        Surface(
                            color = if (selectedStage == "video") Color(0xFF00F0FF).copy(alpha = 0.3f) else Color(0xFF1B142B),
                            border = BorderStroke(1.2.dp, if (selectedStage == "video") Color(0xFF00F0FF) else Color(0xFF3B2F50)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).clickable { selectedStage = "video" }
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📹", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Spotlight Video", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text("HD Video Broadcast", color = Color(0xFF00F0FF), fontSize = 8.5.sp)
                                }
                            }
                        }
                    }
                }

                // 4. Exclusive Gala Theme Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("4. एक्सक्लूसिव गाला थीम (Exclusive Themes)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Triple("gold", "Celestial Gold", Color(0xFFF59E0B)),
                            Triple("neon", "Cyber Neon", Color(0xFF00F0FF)),
                            Triple("emerald", "Liquid Emerald", Color(0xFF00FFA3)),
                            Triple("crimson", "Sunset Crimson", Color(0xFFFF3366))
                        ).forEach { (thKey, thName, thColor) ->
                            val isSel = selectedTheme == thKey
                            Surface(
                                color = if (isSel) thColor.copy(alpha = 0.25f) else Color(0xFF1B142B),
                                border = BorderStroke(1.dp, if (isSel) thColor else Color(0xFF3B2F50)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).clickable { selectedTheme = thKey }
                            ) {
                                Text(thName, color = if (isSel) thColor else Color.Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }

                // Automated Perks reminder
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("⚡ ऑटो-एक्टिवेटेड फीचर्स: 3x गिफ्ट मल्टीप्लायर • FCM फॉलोअर अलर्ट • होम स्क्रीन टिकर", color = Color(0xFFFBBF24), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("🔒 एंट्री गेट: 2,00,000 💎 से कम बैलेंस वाले यूज़र्स को सीधे रिचार्ज पॉपअप मिलेगा।", color = Color.Gray, fontSize = 8.5.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Confirm Create Button
                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        isSubmitting = true
                        val newRoomId = UUID.randomUUID().toString()
                        val numericId = (10000000 + kotlin.math.abs(newRoomId.hashCode()) % 90000000).toString()

                        val newRoom = RoomItem(
                            id = newRoomId,
                            roomId = numericId,
                            title = eventTitle.ifBlank { "Mega Stars Gala 2026" },
                            roomName = eventTitle.ifBlank { "Mega Stars Gala 2026" },
                            hostUid = currentUser.uid,
                            hostId = currentUser.uid,
                            hostName = currentUser.name.ifBlank { "Host" },
                            hostPhotoUrl = currentUser.photo,
                            category = "official_event",
                            roomType = if (selectedStage == "video") "video" else "audio",
                            eventThemeStyle = selectedTheme,
                            activeUserCount = 1,
                            isHostLive = true,
                            isLive = true,
                            statusMessage = "LIVE",
                            createdAt = System.currentTimeMillis()
                        )

                        val initialSeats = (0..14).map { idx ->
                            if (idx == 0) {
                                MicSeat(seatIndex = 0, label = "Host 👑", uid = currentUser.uid, name = currentUser.name, photo = currentUser.photo, role = "host")
                            } else {
                                MicSeat(seatIndex = idx, label = "Mic ${idx + 1}", uid = "", name = "", photo = "", role = "speaker")
                            }
                        }

                        val roomData = hashMapOf(
                            "id" to newRoom.id,
                            "roomId" to newRoom.roomId,
                            "title" to newRoom.title,
                            "roomName" to newRoom.roomName,
                            "hostUid" to newRoom.hostUid,
                            "hostId" to newRoom.hostId,
                            "hostName" to newRoom.hostName,
                            "hostPhotoUrl" to newRoom.hostPhotoUrl,
                            "category" to newRoom.category,
                            "roomType" to newRoom.roomType,
                            "eventThemeStyle" to newRoom.eventThemeStyle,
                            "activeUserCount" to 1,
                            "isHostLive" to true,
                            "isLive" to true,
                            "statusMessage" to "LIVE",
                            "totalSeats" to 15,
                            "minDiamondsRequired" to 200000L,
                            "createdAt" to newRoom.createdAt
                        )

                        db.collection("rooms").document(newRoom.id).set(roomData)
                            .addOnSuccessListener {
                                val batch = db.batch()
                                initialSeats.forEach { seat ->
                                    val seatDoc = db.collection("rooms").document(newRoom.id).collection("mic_seats").document(seat.seatIndex.toString())
                                    batch.set(seatDoc, seat)
                                }
                                batch.commit()
                                isSubmitting = false
                                Toast.makeText(context, "🚀 Official 15-Mic Gala Stage Launched Live!", Toast.LENGTH_SHORT).show()
                                onEventCreated(newRoom)
                            }
                            .addOnFailureListener { e ->
                                isSubmitting = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                    } else {
                        Text("🚀 Launch 15-Mic Official Event Stage", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. 15-MIC ROYAL AMPHITHEATER STAGE SECTION (CLEAN 4x4 GRID - ZERO COLLISION)
// -------------------------------------------------------------
@Composable
fun FifteenMicOfficialGalaStageSection(
    seats: List<MicSeat>,
    currentUserId: String,
    isHost: Boolean,
    isLocalTalking: Boolean,
    allProfiles: List<UserProfile> = emptyList(),
    userActiveFrame: String = "",
    userActiveMicEffect: String = "",
    activeEmojiReactions: Map<String, String> = emptyMap(),
    onSeatClick: (MicSeat) -> Unit,
    onProfileClick: ((MicSeat) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Clean 4x4 Grid Layout: Rows 0..3 with 4 columns each, ample spacing, zero frame overlapping
        for (rowIndex in 0..3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0..3) {
                    val idx = rowIndex * 4 + col
                    val seat = seats.find { it.seatIndex == idx } ?: MicSeat(
                        seatIndex = idx,
                        label = if (idx == 0) "Host 👑" else "Mic ${idx + 1}",
                        uid = "",
                        name = "",
                        photo = "",
                        role = if (idx == 0) "host" else "speaker"
                    )
                    MicSeatView(
                        seat = seat,
                        isCurrentSelf = seat.uid.isNotEmpty() && seat.uid == currentUserId,
                        isLocalTalking = isLocalTalking,
                        isHost = isHost,
                        allProfiles = allProfiles,
                        userActiveFrame = userActiveFrame,
                        userActiveMicEffect = userActiveMicEffect,
                        reactionEmoji = activeEmojiReactions[seat.uid],
                        categorySeatEmoji = if (idx == 0) "👑" else "🎙️",
                        categorySeatLabel = if (idx == 0) "Host" else "Mic ${idx + 1}",
                        onClick = { onSeatClick(seat) },
                        onProfileClick = onProfileClick?.let { callback -> { callback(seat) } }
                    )
                }
            }
        }
    }
}
