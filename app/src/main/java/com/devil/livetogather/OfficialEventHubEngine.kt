package com.devil.livetogather

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
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
// 0. 11 APPROVED HIGH-DEFINITION OFFICIAL EVENT ROOM THEMES
// -------------------------------------------------------------
data class EventThemeOption(
    val id: String,
    val label: String,
    val drawableRes: Int,
    val accentColor: Color
)

val OFFICIAL_EVENT_HD_THEMES: List<EventThemeOption> = listOf(
    EventThemeOption("event_theme_celestial_gold", "Celestial Gold", R.drawable.event_theme_celestial_gold, Color(0xFFFFD700)),
    EventThemeOption("event_theme_cyber_neon", "Cyber Neon", R.drawable.event_theme_cyber_neon, Color(0xFF00F0FF)),
    EventThemeOption("event_theme_emerald_palace", "Emerald Palace", R.drawable.event_theme_emerald_palace, Color(0xFF10B981)),
    EventThemeOption("event_theme_crimson_phoenix", "Crimson Phoenix", R.drawable.event_theme_crimson_phoenix, Color(0xFFEF4444)),
    EventThemeOption("event_theme_sapphire_aurora", "Sapphire Aurora", R.drawable.event_theme_sapphire_aurora, Color(0xFF38BDF8)),
    EventThemeOption("event_theme_rose_gold", "Rose Gold Gala", R.drawable.event_theme_rose_gold, Color(0xFFF472B6)),
    EventThemeOption("event_theme_obsidian_diamond", "Obsidian Diamond", R.drawable.event_theme_obsidian_diamond, Color(0xFFE2E8F0)),
    EventThemeOption("event_theme_arabian_gold", "Arabian Palace", R.drawable.event_theme_arabian_gold, Color(0xFFF59E0B)),
    EventThemeOption("event_theme_amethyst_nebula", "Amethyst Nebula", R.drawable.event_theme_amethyst_nebula, Color(0xFFA855F7)),
    EventThemeOption("event_theme_midnight_aurora", "Midnight Aurora", R.drawable.event_theme_midnight_aurora, Color(0xFF818CF8)),
    EventThemeOption("event_theme_ocean_teal", "Ocean Teal", R.drawable.event_theme_ocean_teal, Color(0xFF2DD4BF))
)

fun getEventThemeDrawableRes(themeId: String): Int {
    if (themeId.isBlank()) return 0
    OFFICIAL_EVENT_HD_THEMES.firstOrNull { it.id.equals(themeId, ignoreCase = true) }?.let { return it.drawableRes }
    return when (themeId.lowercase()) {
        "gold", "celestial_gold" -> R.drawable.event_theme_celestial_gold
        "cyan", "cyber_neon" -> R.drawable.event_theme_cyber_neon
        "emerald", "liquid_emerald" -> R.drawable.event_theme_emerald_palace
        "crimson", "sunset_crimson" -> R.drawable.event_theme_crimson_phoenix
        else -> 0
    }
}

// -------------------------------------------------------------
// 1. UNIQUE MIRRORED CRYSTAL WAVE GALA EQUALIZER (CENTER-AXIS SYMMETRIC + PEAK CAPS)
// -------------------------------------------------------------
@Composable
fun DenseGalaAudioEqualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    styleTheme: String = "gold"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dense_eq")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1150, easing = LinearEasing), RepeatMode.Restart),
        label = "eq_phase"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq_pulse"
    )

    val palette = when (styleTheme.lowercase()) {
        "cyan", "cyber_neon", "event_theme_cyber_neon" -> listOf(Color(0xFF00F0FF), Color(0xFF38BDF8), Color(0xFFFF2A6D))
        "emerald", "liquid_emerald", "event_theme_emerald_palace" -> listOf(Color(0xFF00FFA3), Color(0xFF10B981), Color(0xFFFDE047))
        "crimson", "sunset_crimson", "event_theme_crimson_phoenix" -> listOf(Color(0xFFFF3366), Color(0xFFF59E0B), Color(0xFFFDE047))
        "event_theme_sapphire_aurora", "event_theme_midnight_aurora" -> listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFE879F9))
        "event_theme_rose_gold" -> listOf(Color(0xFFF472B6), Color(0xFFFBBF24), Color(0xFFFECDD3))
        "event_theme_ocean_teal" -> listOf(Color(0xFF2DD4BF), Color(0xFF06B6D4), Color(0xFFFDE047))
        "event_theme_amethyst_nebula" -> listOf(Color(0xFFC084FC), Color(0xFFEC4899), Color(0xFF38BDF8))
        "event_theme_obsidian_diamond" -> listOf(Color(0xFFFFFFFF), Color(0xFF94A3B8), Color(0xFF38BDF8))
        else -> listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFEC4899), Color(0xFFA855F7))
    }

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val totalHeight = size.height
        if (totalWidth <= 0f || totalHeight <= 0f) return@Canvas

        val centerY = totalHeight * 0.5f
        val maxHalfH = (totalHeight * 0.44f).coerceAtLeast(2.dp.toPx())
        val step = totalWidth / barCount.toFloat()
        val barWidth = (step * 0.54f).coerceAtLeast(1.8.dp.toPx())

        // Glowing center laser horizon line
        drawLine(
            brush = Brush.horizontalGradient(palette),
            start = androidx.compose.ui.geometry.Offset(0f, centerY),
            end = androidx.compose.ui.geometry.Offset(totalWidth, centerY),
            strokeWidth = 0.9.dp.toPx(),
            alpha = 0.55f
        )

        for (i in 0 until barCount) {
            val norm = i.toFloat() / (barCount - 1).coerceAtLeast(1).toFloat()
            // Bell-curve concert envelope so center frequencies swell dramatically
            val bell = kotlin.math.sin(norm * Math.PI).toFloat().coerceIn(0.22f, 1f)
            val w1 = kotlin.math.sin(phase * 1.4f + norm * 4.2 * Math.PI).toFloat()
            val w2 = kotlin.math.cos(phase * 2.1f - norm * 6.5 * Math.PI).toFloat()
            val w3 = kotlin.math.sin(phase * 0.9f + norm * 2.4 * Math.PI).toFloat()
            val amp = (((w1 * 0.5f + w2 * 0.32f + w3 * 0.18f) + 1f) * 0.5f) * bell * pulse
            val halfH = (amp * maxHalfH).coerceIn(1.5.dp.toPx(), maxHalfH)

            val x = i * step + (step - barWidth) * 0.5f
            val colorIdx = (((norm + phase / (2 * Math.PI).toFloat()) % 1f) * palette.size)
                .toInt().coerceIn(0, palette.size - 1)
            val barColor = palette[colorIdx]

            // Soft outer neon halo behind each symmetric capsule
            drawRoundRect(
                color = barColor.copy(alpha = 0.28f),
                topLeft = androidx.compose.ui.geometry.Offset(x - 0.8.dp.toPx(), centerY - halfH - 0.8.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(barWidth + 1.6.dp.toPx(), (halfH + 0.8.dp.toPx()) * 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth, barWidth)
            )

            // Crisp symmetric mirrored capsule bar (radiates up & down from center)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(barColor, Color.White.copy(alpha = 0.92f), barColor),
                    startY = centerY - halfH,
                    endY = centerY + halfH
                ),
                topLeft = androidx.compose.ui.geometry.Offset(x, centerY - halfH),
                size = androidx.compose.ui.geometry.Size(barWidth, halfH * 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth, barWidth)
            )

            // Floating crystal peak dots on tall harmonic peaks
            if (amp > 0.58f) {
                val dotRadius = (barWidth * 0.45f).coerceIn(1.dp.toPx(), 2.2.dp.toPx())
                val dotOffset = (halfH + 2.dp.toPx()).coerceAtMost(centerY - dotRadius)
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = dotRadius,
                    center = androidx.compose.ui.geometry.Offset(x + barWidth * 0.5f, centerY - dotOffset)
                )
                drawCircle(
                    color = barColor.copy(alpha = 0.85f),
                    radius = dotRadius,
                    center = androidx.compose.ui.geometry.Offset(x + barWidth * 0.5f, centerY + dotOffset)
                )
            }
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
    val db = remember { FirebaseFirestore.getInstance() }
    var showSchedulingDialog by remember { mutableStateOf(false) }
    var insufficientDiamondsRoom by remember { mutableStateOf<RoomItem?>(null) }
    var hubSearchQuery by remember { mutableStateOf("") }
    var selectedHubTab by remember { mutableStateOf("live") } // "live", "upcoming", "ended"

    // Live 1-second ticking clock for real-time Upcoming event countdown timers
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            nowMs = System.currentTimeMillis()
        }
    }

    // Real-time Firestore listener on official_events schedule collection (Zero dummy data!)
    val scheduledEventsList = remember { mutableStateListOf<Map<String, Any>>() }
    DisposableEffect(Unit) {
        val reg = db.collection("official_events")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    scheduledEventsList.clear()
                    scheduledEventsList.addAll(snap.documents.mapNotNull { doc ->
                        doc.data?.toMutableMap()?.apply { put("docId", doc.id) }
                    })
                }
            }
        onDispose { reg.remove() }
    }

    val isUserMonetizedHost = currentUser.isHostMonetized || currentUser.isAdmin || currentUser.isFounder() || currentUser.isOfficialAdmin()

    // 100% Real-Time Firestore Synced Official Gala Rooms
    val allGalaRooms = remember(allRooms.map { "${it.id}_${it.isHostLive}_${it.isLive}_${it.statusMessage}_${it.activeUserCount}_${it.title}_${it.eventThemeStyle}" }) {
        allRooms.filter {
            (it.category.equals("official_event", ignoreCase = true) ||
             it.category.equals("mega_star_gala", ignoreCase = true) ||
             it.title.contains("Gala", ignoreCase = true) ||
             it.title.contains("Official Event", ignoreCase = true)) &&
            !it.statusMessage.equals("DELETED", ignoreCase = true)
        }
    }

    val liveGalaRooms = remember(allGalaRooms, hubSearchQuery) {
        allGalaRooms.filter {
            !it.statusMessage.equals("CLOSED", ignoreCase = true) &&
            !it.statusMessage.equals("ENDED", ignoreCase = true) &&
            (hubSearchQuery.isBlank() ||
                it.title.contains(hubSearchQuery, ignoreCase = true) ||
                it.hostName.contains(hubSearchQuery, ignoreCase = true) ||
                it.id.contains(hubSearchQuery, ignoreCase = true))
        }
    }

    val upcomingEvents = remember(scheduledEventsList.toList(), nowMs, hubSearchQuery) {
        scheduledEventsList.filter { ev ->
            val startAt = (ev["scheduledStartAt"] as? Number)?.toLong() ?: 0L
            val status = (ev["status"] as? String) ?: "UPCOMING"
            val title = (ev["title"] as? String) ?: ""
            val host = (ev["hostName"] as? String) ?: ""
            status.equals("UPCOMING", ignoreCase = true) && startAt > nowMs &&
                (hubSearchQuery.isBlank() || title.contains(hubSearchQuery, true) || host.contains(hubSearchQuery, true))
        }.sortedBy { (it["scheduledStartAt"] as? Number)?.toLong() ?: Long.MAX_VALUE }
    }

    val endedGalaRooms = remember(allGalaRooms, hubSearchQuery) {
        allGalaRooms.filter {
            (it.statusMessage.equals("CLOSED", ignoreCase = true) || it.statusMessage.equals("ENDED", ignoreCase = true)) &&
            (hubSearchQuery.isBlank() ||
                it.title.contains(hubSearchQuery, ignoreCase = true) ||
                it.hostName.contains(hubSearchQuery, ignoreCase = true) ||
                it.id.contains(hubSearchQuery, ignoreCase = true))
        }
    }

    val filteredGalaRooms = liveGalaRooms
    val hostOwnGalaRoom = remember(allGalaRooms, currentUser.uid) {
        allGalaRooms.firstOrNull {
            !it.statusMessage.equals("CLOSED", ignoreCase = true) &&
            ((it.hostUid.isNotBlank() && it.hostUid == currentUser.uid) ||
             (it.hostId.isNotBlank() && it.hostId == currentUser.uid))
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

                        // ROLE-BASED CONDITIONAL ACCESS: "🎙️ My Event" (existing/active host) vs "+ Organize" (new event)
                        Button(
                            onClick = {
                                if (hostOwnGalaRoom != null) {
                                    onJoinRoom(hostOwnGalaRoom)
                                } else if (isUserMonetizedHost) {
                                    showSchedulingDialog = true
                                } else {
                                    Toast.makeText(context, "👑 Only verified & monetized hosts can organize Official Gala Events!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hostOwnGalaRoom != null) Color(0xFF10B981) else Color(0xFFF59E0B)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (hostOwnGalaRoom != null) "🎙️ My Event" else "+ Organize",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real-time Search Field (filters across Live Now, Upcoming, and Ended tabs)
                OutlinedTextField(
                    value = hubSearchQuery,
                    onValueChange = { hubSearchQuery = it },
                    placeholder = { Text("🔍 Search Event room, host or ID...", color = Color.Gray, fontSize = 12.sp) },
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
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3 CATEGORIZED EVENT HUB TABS: Live Now | Upcoming | Ended
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("live", "🔴 Live (${liveGalaRooms.size})", Color(0xFFEF4444)),
                        Triple("upcoming", "⏳ Upcoming (${upcomingEvents.size})", Color(0xFFF59E0B)),
                        Triple("ended", "🏁 Ended (${endedGalaRooms.size})", Color(0xFF94A3B8))
                    ).forEach { (tabKey, tabLabel, tabColor) ->
                        val isSelected = selectedHubTab == tabKey
                        Surface(
                            color = if (isSelected) tabColor.copy(alpha = 0.24f) else Color(0xFF151024),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, if (isSelected) tabColor else Color(0xFF3B2F50)),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable { selectedHubTab = tabKey }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = tabLabel,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedHubTab) {
                    "live" -> {
                        if (liveGalaRooms.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🌟", fontSize = 36.sp)
                                    Text(if (hubSearchQuery.isBlank()) "No Official Event Rooms Live Right Now" else "No Matching Live Events Found", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Verified hosts can tap '+ Organize' above to launch the 15-Mic Gala Stage!", color = Color.Gray, fontSize = 11.sp, maxLines = 2)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(liveGalaRooms, key = { it.id }) { r ->
                                    val roomTheme = if (r.eventThemeStyle.isNotBlank()) r.eventThemeStyle else "event_theme_celestial_gold"
                                    val themeObj = OFFICIAL_EVENT_HD_THEMES.firstOrNull { it.id.equals(roomTheme, true) }
                                    val cardBorder = themeObj?.accentColor ?: when (roomTheme.lowercase()) {
                                        "cyan", "cyber_neon" -> Color(0xFF00F0FF)
                                        "emerald", "liquid_emerald" -> Color(0xFF00FFA3)
                                        "crimson", "sunset_crimson" -> Color(0xFFFF3366)
                                        else -> Color(0xFFF59E0B)
                                    }
                                    val themeDrawable = getEventThemeDrawableRes(roomTheme)

                                    Surface(
                                        color = Color(0xFF130E20),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.1.dp, cardBorder.copy(alpha = 0.65f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // 200,000 DIAMONDS GATE CHECK
                                                val isOwner = currentUser.uid.isNotBlank() && (r.hostUid == currentUser.uid || r.hostId == currentUser.uid)
                                                val isStaff = currentUser.isAdmin || currentUser.isFounder() || currentUser.isOfficialAdmin()
                                                if (currentUser.diamonds < 200000L && !isOwner && !isStaff) {
                                                    insufficientDiamondsRoom = r
                                                } else {
                                                    onJoinRoom(r)
                                                }
                                            }
                                    ) {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            // HD Event Theme Banner Backdrop
                                            if (themeDrawable != 0) {
                                                Image(
                                                    painter = painterResource(id = themeDrawable),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.matchParentSize(),
                                                    alpha = 0.34f
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(Color(0xAA0A0714), Color(0xDD0A0714))
                                                        )
                                                    )
                                            )

                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .border(1.4.dp, cardBorder, RoundedCornerShape(12.dp))
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
                                                                Text("🔴 LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                                                            }
                                                            Surface(color = cardBorder.copy(alpha = 0.22f), shape = RoundedCornerShape(4.dp)) {
                                                                Text(
                                                                    text = if (r.roomType.equals("video", true)) "📹 Audio + Video" else "🎙️ 15-Mic Audio",
                                                                    color = cardBorder,
                                                                    fontSize = 8.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(r.title.ifBlank { "Mega Stars Gala Event" }, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("Host: ${r.hostName.ifBlank { "Verified Star" }} • 👥 ${r.activeUserCount} online", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                                    }

                                                    Surface(
                                                        color = cardBorder,
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Enter ➔", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // DISTINCT MEGASTAR GALA EQUALIZER
                                                DenseGalaAudioEqualizer(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(16.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color.Black.copy(alpha = 0.55f))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp),
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

                    "upcoming" -> {
                        if (upcomingEvents.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("⏳", fontSize = 36.sp)
                                    Text("No Upcoming Scheduled Events", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Tap '+ Organize' above to schedule an Upcoming Official Event with a live countdown!", color = Color.Gray, fontSize = 11.sp, maxLines = 2)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(upcomingEvents, key = { (it["docId"] as? String) ?: it.hashCode().toString() }) { ev ->
                                    val docId = (ev["docId"] as? String) ?: ""
                                    val title = (ev["title"] as? String).orEmpty().ifBlank { "Upcoming Gala Event" }
                                    val hostUid = (ev["hostUid"] as? String).orEmpty()
                                    val hostName = (ev["hostName"] as? String).orEmpty().ifBlank { "Official Host" }
                                    val hostPhoto = (ev["hostPhotoUrl"] as? String).orEmpty()
                                    val themeId = (ev["eventThemeStyle"] as? String).orEmpty().ifBlank { "event_theme_celestial_gold" }
                                    val roomType = (ev["roomType"] as? String).orEmpty().ifBlank { "audio" }
                                    val startAt = (ev["scheduledStartAt"] as? Number)?.toLong() ?: nowMs
                                    val remSec = ((startAt - nowMs) / 1000L).coerceAtLeast(0L)
                                    val hrs = remSec / 3600L
                                    val mins = (remSec % 3600L) / 60L
                                    val secs = remSec % 60L
                                    val countdownStr = String.format("%02d:%02d:%02d", hrs, mins, secs)
                                    val themeObj = OFFICIAL_EVENT_HD_THEMES.firstOrNull { it.id.equals(themeId, true) }
                                    val accent = themeObj?.accentColor ?: Color(0xFFF59E0B)
                                    val drawableRes = getEventThemeDrawableRes(themeId)
                                    val isEventOwner = currentUser.uid.isNotBlank() && hostUid == currentUser.uid

                                    Surface(
                                        color = Color(0xFF140F22),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            if (drawableRes != 0) {
                                                Image(
                                                    painter = painterResource(id = drawableRes),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.matchParentSize(),
                                                    alpha = 0.36f
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(Brush.verticalGradient(listOf(Color(0x990A0714), Color(0xDD0A0714))))
                                            )

                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                    AsyncImage(
                                                        model = hostPhoto.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100" },
                                                        contentDescription = hostName,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .border(1.2.dp, accent, RoundedCornerShape(12.dp))
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                                            Surface(color = Color(0xFFF59E0B), shape = RoundedCornerShape(4.dp)) {
                                                                Text("⏳ UPCOMING", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                                                            }
                                                            Text(
                                                                text = if (roomType.equals("video", true)) "📹 Audio + Video" else "🎙️ 15-Mic Audio",
                                                                color = accent,
                                                                fontSize = 8.5.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("Host: $hostName", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        color = Color.Black.copy(alpha = 0.65f),
                                                        shape = RoundedCornerShape(8.dp),
                                                        border = BorderStroke(0.8.dp, accent.copy(alpha = 0.5f))
                                                    ) {
                                                        Text(
                                                            text = "⏱️ Starts in $countdownStr",
                                                            color = Color(0xFFFDE047),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                        )
                                                    }

                                                    if (isEventOwner) {
                                                        Button(
                                                            onClick = {
                                                                val newRoomId = UUID.randomUUID().toString()
                                                                val numericId = (10000000 + kotlin.math.abs(newRoomId.hashCode()) % 90000000).toString()
                                                                val launchedRoom = RoomItem(
                                                                    id = newRoomId,
                                                                    roomId = numericId,
                                                                    title = title,
                                                                    roomName = title,
                                                                    hostUid = currentUser.uid,
                                                                    hostId = currentUser.uid,
                                                                    hostName = currentUser.name.ifBlank { hostName },
                                                                    hostPhotoUrl = currentUser.photo.ifBlank { hostPhoto },
                                                                    category = "official_event",
                                                                    roomType = roomType,
                                                                    eventThemeStyle = themeId,
                                                                    banner_image_url = themeId,
                                                                    activeUserCount = 1,
                                                                    isHostLive = true,
                                                                    isLive = true,
                                                                    statusMessage = "LIVE",
                                                                    createdAt = System.currentTimeMillis()
                                                                )
                                                                val roomMap = hashMapOf(
                                                                    "id" to launchedRoom.id,
                                                                    "roomId" to launchedRoom.roomId,
                                                                    "title" to launchedRoom.title,
                                                                    "roomName" to launchedRoom.roomName,
                                                                    "hostUid" to launchedRoom.hostUid,
                                                                    "hostId" to launchedRoom.hostId,
                                                                    "hostName" to launchedRoom.hostName,
                                                                    "hostPhotoUrl" to launchedRoom.hostPhotoUrl,
                                                                    "category" to "official_event",
                                                                    "roomType" to roomType,
                                                                    "eventThemeStyle" to themeId,
                                                                    "banner_image_url" to themeId,
                                                                    "activeUserCount" to 1,
                                                                    "isHostLive" to true,
                                                                    "isLive" to true,
                                                                    "statusMessage" to "LIVE",
                                                                    "totalSeats" to 15,
                                                                    "minDiamondsRequired" to 200000L,
                                                                    "createdAt" to launchedRoom.createdAt
                                                                )
                                                                db.collection("rooms").document(launchedRoom.id).set(roomMap)
                                                                    .addOnSuccessListener {
                                                                        val batch = db.batch()
                                                                        (0..14).forEach { idx ->
                                                                            val seat = if (idx == 0) MicSeat(seatIndex = 0, label = "Host 👑", uid = currentUser.uid, name = currentUser.name, photo = currentUser.photo, role = "host")
                                                                            else MicSeat(seatIndex = idx, label = "Mic ${idx + 1}", uid = "", name = "", photo = "", role = "speaker")
                                                                            batch.set(db.collection("rooms").document(launchedRoom.id).collection("mic_seats").document(idx.toString()), seat)
                                                                        }
                                                                        batch.commit()
                                                                        if (docId.isNotBlank()) {
                                                                            db.collection("official_events").document(docId).update(mapOf("status" to "LIVE", "roomId" to launchedRoom.id))
                                                                        }
                                                                        Toast.makeText(context, "🚀 Official Event is now LIVE!", Toast.LENGTH_SHORT).show()
                                                                        onJoinRoom(launchedRoom)
                                                                    }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                            shape = RoundedCornerShape(8.dp),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            modifier = Modifier.height(30.dp)
                                                        ) {
                                                            Text("🚀 Go Live", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                        }
                                                    } else {
                                                        Surface(
                                                            color = accent.copy(alpha = 0.22f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            border = BorderStroke(0.8.dp, accent),
                                                            modifier = Modifier.clickable {
                                                                if (docId.isNotBlank() && currentUser.uid.isNotBlank()) {
                                                                    db.collection("official_events").document(docId)
                                                                        .collection("reminders").document(currentUser.uid)
                                                                        .set(mapOf("uid" to currentUser.uid, "name" to currentUser.name, "at" to System.currentTimeMillis()))
                                                                    Toast.makeText(context, "🔔 Reminder set for $title!", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        ) {
                                                            Text("🔔 Remind Me", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
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

                    else -> {
                        // ENDED EVENTS ARCHIVE TAB
                        if (endedGalaRooms.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🏁", fontSize = 36.sp)
                                    Text("No Ended Events Yet", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Completed Mega Stars Gala events will appear here.", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(endedGalaRooms, key = { it.id }) { r ->
                                    val themeDrawable = getEventThemeDrawableRes(r.eventThemeStyle)
                                    Surface(
                                        color = Color(0xFF14111D),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(0.8.dp, Color(0xFF475569)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            if (themeDrawable != 0) {
                                                Image(
                                                    painter = painterResource(id = themeDrawable),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.matchParentSize(),
                                                    alpha = 0.22f
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    model = r.hostPhotoUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100" },
                                                    contentDescription = r.hostName,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                                        Surface(color = Color(0xFF475569), shape = RoundedCornerShape(4.dp)) {
                                                            Text("🏁 ENDED", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                                                        }
                                                    }
                                                    Text(r.title.ifBlank { "Mega Stars Gala" }, color = Color(0xFFE2E8F0), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("Host: ${r.hostName.ifBlank { "Official Host" }}", color = Color.Gray, fontSize = 10.sp)
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
    val scrollState = rememberScrollState()

    var eventTitle by remember { mutableStateOf("Mega Stars Celebration Gala 🌟") }
    var selectedStage by remember { mutableStateOf("voice_15") } // "voice_15" or "video"
    var selectedTheme by remember { mutableStateOf("event_theme_celestial_gold") }
    var launchMode by remember { mutableStateOf("live_now") } // "live_now" or "schedule"
    var scheduleDelayMinutes by remember { mutableStateOf(60L) } // 30, 60, 180, 360
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("👑 Organize Official Gala Event", color = Color(0xFFF59E0B), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Text("15-Mic Dual-Mode Stage & 11 HD Themes", color = Color.Gray, fontSize = 10.5.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Title Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("1. Event Title", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. Launch Mode (Go Live Now vs Schedule Upcoming)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("2. Launch Timing", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = if (launchMode == "live_now") Color(0xFFEF4444).copy(alpha = 0.28f) else Color(0xFF1B142B),
                                border = BorderStroke(1.dp, if (launchMode == "live_now") Color(0xFFEF4444) else Color(0xFF3B2F50)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).clickable { launchMode = "live_now" }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("🔴 Go Live Now", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text("Start 15-Mic Stage immediately", color = Color(0xFFFCA5A5), fontSize = 8.5.sp)
                                }
                            }

                            Surface(
                                color = if (launchMode == "schedule") Color(0xFFF59E0B).copy(alpha = 0.28f) else Color(0xFF1B142B),
                                border = BorderStroke(1.dp, if (launchMode == "schedule") Color(0xFFF59E0B) else Color(0xFF3B2F50)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).clickable { launchMode = "schedule" }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("⏳ Schedule Upcoming", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text("Live HH:MM:SS Countdown", color = Color(0xFFFBBF24), fontSize = 8.5.sp)
                                }
                            }
                        }

                        if (launchMode == "schedule") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(30L to "+30 Min", 60L to "+1 Hr", 180L to "+3 Hr", 360L to "+6 Hr").forEach { (mins, label) ->
                                    val isSel = scheduleDelayMinutes == mins
                                    Surface(
                                        color = if (isSel) Color(0xFFF59E0B).copy(alpha = 0.3f) else Color(0xFF1B142B),
                                        border = BorderStroke(1.dp, if (isSel) Color(0xFFF59E0B) else Color(0xFF3B2F50)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).clickable { scheduleDelayMinutes = mins }
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSel) Color(0xFFFDE047) else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Dual-Mode Stage Format (Audio vs Audio+Video)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("3. Dual-Mode Stage Format", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = if (selectedStage == "voice_15") Color(0xFF8B5CF6).copy(alpha = 0.3f) else Color(0xFF1B142B),
                                border = BorderStroke(1.2.dp, if (selectedStage == "voice_15") Color(0xFF8B5CF6) else Color(0xFF3B2F50)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).clickable { selectedStage = "voice_15" }
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎙️", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("15-Mic Audio", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        Text("Royal Amphitheater", color = Color(0xFFA78BFA), fontSize = 8.5.sp)
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
                                        Text("Audio + Video", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        Text("15-Mic Video Stage", color = Color(0xFF00F0FF), fontSize = 8.5.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 4. 11 Approved HD Event Background Themes Carousel
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("4. Select HD Event Theme (11 Exclusive Designs)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(OFFICIAL_EVENT_HD_THEMES, key = { it.id }) { theme ->
                                val isSel = selectedTheme == theme.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) theme.accentColor else Color(0xFF3B2F50)),
                                    color = Color(0xFF1B142B),
                                    modifier = Modifier
                                        .width(86.dp)
                                        .height(118.dp)
                                        .clickable { selectedTheme = theme.id }
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Image(
                                            painter = painterResource(id = theme.drawableRes),
                                            contentDescription = theme.label,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.72f))
                                                .padding(vertical = 4.dp, horizontal = 3.dp)
                                        ) {
                                            Text(
                                                text = theme.label,
                                                color = if (isSel) theme.accentColor else Color.White,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        if (isSel) {
                                            Surface(
                                                color = theme.accentColor,
                                                shape = CircleShape,
                                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("✓", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    }
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
                            Text("⚡ Live Features: 15 Interactive Mics • Real-Time PK Scoreboard • Event Gifting Leaderboard", color = Color(0xFFFBBF24), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("🔒 Entry Gate: 2,00,000 💎 VIP requirement (Hosts & Admins enter directly).", color = Color.Gray, fontSize = 8.5.sp)
                        }
                    }
                }

                // Confirm Launch / Schedule Button
                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        isSubmitting = true
                        val resolvedTitle = eventTitle.ifBlank { "Mega Stars Gala Event" }
                        val resolvedRoomType = if (selectedStage == "video") "video" else "audio"

                        if (launchMode == "schedule") {
                            val eventDocId = UUID.randomUUID().toString()
                            val scheduledStartAt = System.currentTimeMillis() + (scheduleDelayMinutes * 60_000L)
                            val scheduleData = hashMapOf(
                                "id" to eventDocId,
                                "title" to resolvedTitle,
                                "hostUid" to currentUser.uid,
                                "hostName" to currentUser.name.ifBlank { "Official Host" },
                                "hostPhotoUrl" to currentUser.photo,
                                "roomType" to resolvedRoomType,
                                "eventThemeStyle" to selectedTheme,
                                "status" to "UPCOMING",
                                "scheduledStartAt" to scheduledStartAt,
                                "createdAt" to System.currentTimeMillis()
                            )
                            db.collection("official_events").document(eventDocId).set(scheduleData)
                                .addOnSuccessListener {
                                    isSubmitting = false
                                    Toast.makeText(context, "⏳ Official Event Scheduled! Check the Upcoming tab.", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                                .addOnFailureListener { e ->
                                    isSubmitting = false
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            val newRoomId = UUID.randomUUID().toString()
                            val numericId = (10000000 + kotlin.math.abs(newRoomId.hashCode()) % 90000000).toString()

                            val newRoom = RoomItem(
                                id = newRoomId,
                                roomId = numericId,
                                title = resolvedTitle,
                                roomName = resolvedTitle,
                                hostUid = currentUser.uid,
                                hostId = currentUser.uid,
                                hostName = currentUser.name.ifBlank { "Host" },
                                hostPhotoUrl = currentUser.photo,
                                category = "official_event",
                                roomType = resolvedRoomType,
                                eventThemeStyle = selectedTheme,
                                banner_image_url = selectedTheme,
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
                                "banner_image_url" to selectedTheme,
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
                        Text(
                            text = if (launchMode == "schedule") "⏳ Schedule Official Event" else "🚀 Launch 15-Mic Official Event Stage",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. 15-MIC ROYAL AMPHITHEATER STAGE SECTION (1 HOST + 14 GUEST MICS, DUAL-MODE AUDIO/VIDEO)
// -------------------------------------------------------------
@Composable
fun FifteenMicOfficialGalaStageSection(
    seats: List<MicSeat>,
    currentUserId: String,
    isHost: Boolean,
    isLocalTalking: Boolean,
    roomType: String = "audio",
    themeStyle: String = "event_theme_celestial_gold",
    onToggleRoomMode: (() -> Unit)? = null,
    allProfiles: List<UserProfile> = emptyList(),
    userActiveFrame: String = "",
    userActiveMicEffect: String = "",
    activeEmojiReactions: Map<String, String> = emptyMap(),
    onSeatClick: (MicSeat) -> Unit,
    onProfileClick: ((MicSeat) -> Unit)? = null
) {
    fun seatAt(idx: Int): MicSeat = seats.find { it.seatIndex == idx } ?: MicSeat(
        seatIndex = idx,
        label = if (idx == 0) "Host 👑" else "Mic ${idx + 1}",
        uid = "",
        name = "",
        photo = "",
        role = if (idx == 0) "host" else "speaker"
    )

    val isVideoMode = roomType.equals("video", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // TOP STAGE ROW: Seat 0 (Main Host Crown) + Dual-Mode Indicator/Toggle + Custom MegaStar Equalizer
        Surface(
            color = Color.Black.copy(alpha = 0.28f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Seat 0 (Main Host)
                val hostSeat = seatAt(0)
                MicSeatView(
                    seat = hostSeat,
                    isCurrentSelf = hostSeat.uid.isNotEmpty() && hostSeat.uid == currentUserId,
                    isLocalTalking = isLocalTalking,
                    isHost = isHost,
                    allProfiles = allProfiles,
                    userActiveFrame = userActiveFrame,
                    userActiveMicEffect = userActiveMicEffect,
                    reactionEmoji = activeEmojiReactions[hostSeat.uid],
                    categorySeatEmoji = if (isVideoMode) "📹" else "👑",
                    categorySeatLabel = if (isVideoMode) "Video Host" else "Host",
                    onClick = { onSeatClick(hostSeat) },
                    onProfileClick = onProfileClick?.let { callback -> { callback(hostSeat) } }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Dual-Mode Audio/Video Control + MegaStar Gala Equalizer
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (isVideoMode) Color(0xFF06B6D4).copy(alpha = 0.28f) else Color(0xFFF59E0B).copy(alpha = 0.26f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(0.9.dp, if (isVideoMode) Color(0xFF22D3EE) else Color(0xFFFBBF24))
                        ) {
                            Text(
                                text = if (isVideoMode) "📹 VIDEO + 15-MIC STAGE" else "🎙️ 15-MIC GALA STAGE",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (isHost && onToggleRoomMode != null) {
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable { onToggleRoomMode() }
                            ) {
                                Text(
                                    text = if (isVideoMode) "🎙️ Switch Audio" else "📹 Switch Video",
                                    color = Color(0xFFFDE047),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    DenseGalaAudioEqualizer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.42f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        barCount = 36,
                        styleTheme = themeStyle
                    )
                }
            }
        }

        // 14 GUEST MICS IN 3 BALANCED ROWS (5 + 5 + 4 = 14 Guest Mics, Total = 15 Interactive Mics)
        val guestRows = listOf(1..5, 6..10, 11..14)
        guestRows.forEach { range ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (idx in range) {
                    val seat = seatAt(idx)
                    MicSeatView(
                        seat = seat,
                        isCurrentSelf = seat.uid.isNotEmpty() && seat.uid == currentUserId,
                        isLocalTalking = isLocalTalking,
                        isHost = isHost,
                        allProfiles = allProfiles,
                        userActiveFrame = userActiveFrame,
                        userActiveMicEffect = userActiveMicEffect,
                        reactionEmoji = activeEmojiReactions[seat.uid],
                        categorySeatEmoji = "🎙️",
                        categorySeatLabel = "Mic $idx",
                        onClick = { onSeatClick(seat) },
                        onProfileClick = onProfileClick?.let { callback -> { callback(seat) } }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. REAL-TIME EVENT GIFTING LEADERBOARD & PK STRIP (100% FIRESTORE)
// -------------------------------------------------------------
@Composable
fun OfficialEventLeaderboardStrip(
    roomId: String,
    isPkActive: Boolean,
    pkBlueScore: Long,
    pkRedScore: Long,
    onOpenGiftSheet: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val contributors = remember { mutableStateListOf<Map<String, Any>>() }
    var showFullLeaderboard by remember { mutableStateOf(false) }

    DisposableEffect(roomId) {
        if (roomId.isBlank()) return@DisposableEffect onDispose {}
        val reg = db.collection("rooms").document(roomId)
            .collection("event_leaderboard")
            .orderBy("totalDiamonds", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(15)
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    contributors.clear()
                    contributors.addAll(snap.documents.mapNotNull { it.data })
                }
            }
        onDispose { reg.remove() }
    }

    val totalEventDiamonds = contributors.sumOf { (it["totalDiamonds"] as? Number)?.toLong() ?: 0L }

    Surface(
        color = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.9.dp, Color(0xFFF59E0B).copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { showFullLeaderboard = true }
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🏆 Event Rank", color = Color(0xFFFDE047), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("💎 %,d".format(totalEventDiamonds), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Top 3 live avatars (100% real Firestore contributors, zero dummy data)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (contributors.isEmpty()) {
                        Text("Send gift to lead 🎁", color = Color(0xFFCBD5E1), fontSize = 9.sp)
                    } else {
                        contributors.take(3).forEachIndexed { idx, item ->
                            val photo = (item["photo"] as? String).orEmpty()
                            val medalColor = when (idx) {
                                0 -> Color(0xFFFFD700)
                                1 -> Color(0xFFE2E8F0)
                                else -> Color(0xFFCD7F32)
                            }
                            AsyncImage(
                                model = photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80" },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(1.2.dp, medalColor, CircleShape)
                            )
                        }
                    }
                    Surface(
                        color = Color(0xFFEC4899),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { onOpenGiftSheet() }
                    ) {
                        Text("🎁 Boost", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                    }
                }
            }

            // Live Event PK Progress Bar (synced to Firestore pkHost1Score / pkHost2Score)
            if (isPkActive) {
                val totalPk = (pkBlueScore + pkRedScore).coerceAtLeast(1L)
                val blueFrac = (pkBlueScore.toFloat() / totalPk.toFloat()).coerceIn(0.12f, 0.88f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFEF4444)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(blueFrac)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF06B6D4)))),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("  🔵 %,d".format(pkBlueScore), color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        Text("%,d 🔴  ".format(pkRedScore), color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    if (showFullLeaderboard) {
        Dialog(onDismissRequest = { showFullLeaderboard = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF100B1C),
                border = BorderStroke(1.2.dp, Color(0xFFF59E0B))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆 Live Event Gifting Leaderboard", color = Color(0xFFFDE047), fontSize = 14.sp, fontWeight = FontWeight.Black)
                        IconButton(onClick = { showFullLeaderboard = false }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    if (contributors.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Text("No event gifts recorded yet. Be the #1 contributor! 🎁", color = Color.Gray, fontSize = 11.5.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(contributors.indices.toList()) { idx ->
                                val item = contributors[idx]
                                val name = (item["name"] as? String).orEmpty().ifBlank { "Supporter" }
                                val photo = (item["photo"] as? String).orEmpty()
                                val diamonds = (item["totalDiamonds"] as? Number)?.toLong() ?: 0L
                                val rankBadge = when (idx) {
                                    0 -> "🥇"
                                    1 -> "🥈"
                                    2 -> "🥉"
                                    else -> "#${idx + 1}"
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.06f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(rankBadge, color = Color(0xFFFDE047), fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(30.dp))
                                        AsyncImage(
                                            model = photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80" },
                                            contentDescription = name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(32.dp).clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("💎 %,d".format(diamonds), color = Color(0xFFFBBF24), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
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
