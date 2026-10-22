package com.devil.livetogather

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * =========================================================================
 * ROOM GIFTING & BACKPACK ENGINE
 * -------------------------------------------------------------------------
 * Features:
 * 1. 25 Realistic 3D Gifts (1 to 50 Diamonds) with transparent cutouts.
 * 2. 🎒 Backpack Tab (Inventory): Send stored gifts with 0 diamond cost.
 * 3. 🎯 Target Selector: "All Mics" (सबको) vs Individual Seated User Mic.
 * 4. 🔢 Quantity / Combo Multipliers: 1x, 10x, 20x, 100x.
 * 5. 🚀 Flight & Firecracker Burst Animation: Travels to mic, explodes with sparks.
 * =========================================================================
 */

// -------------------------------------------------------------------------
// 1. ROOM 3D GIFTS SECTION (STORE TAB)
// -------------------------------------------------------------------------
@Composable
fun RoomMicroGiftsSection(
    currentDiamonds: Long,
    seatedUsers: List<MicSeat>,
    onSendGift: (gift: RoomGiftItem, quantity: Int, targetSeatIndex: Int, targetUserName: String) -> Unit,
    onGoToRecharge: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedGift by remember { mutableStateOf(ROOM_GIFT_CATALOG[0]) } // Default: Single Rose
    var selectedQuantity by remember { mutableIntStateOf(1) } // 1, 10, 20, 100
    var selectedTargetSeatIndex by remember { mutableIntStateOf(-1) } // -1 = All Mics
    var selectedTargetName by remember { mutableStateOf("All Mics") }

    val occupiedSeats = remember(seatedUsers) {
        seatedUsers.filter { it.uid.isNotBlank() }
    }

    val multiplier = if (selectedTargetSeatIndex == -1) {
        maxOf(1, occupiedSeats.size)
    } else {
        1
    }
    val totalCost = selectedGift.cost * selectedQuantity * multiplier
    val canAfford = currentDiamonds >= totalCost

    val filteredGifts = remember(selectedCategory) {
        when (selectedCategory) {
            "romantic" -> ROOM_GIFT_CATALOG.filter { it.category == "romantic" }
            "sweet" -> ROOM_GIFT_CATALOG.filter { it.category == "sweet" || it.category == "floral" || it.category == "cute" }
            "party" -> ROOM_GIFT_CATALOG.filter { it.category == "party" || it.category == "magic" || it.category == "music" }
            "vip" -> ROOM_GIFT_CATALOG.filter { it.category == "vip" || it.category == "luxury" || it.category == "royal" || it.category == "divine" }
            else -> ROOM_GIFT_CATALOG
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ROW 1: TARGET MIC SELECTOR ("All Mics" vs Specific Seated Users)
        TargetMicSelectorRow(
            occupiedSeats = occupiedSeats,
            selectedTargetIndex = selectedTargetSeatIndex,
            onSelectTarget = { seatIndex, name ->
                selectedTargetSeatIndex = seatIndex
                selectedTargetName = name
            }
        )

        // ROW 2: CATEGORY FILTER CHIPS
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf(
                "all" to "🔥 All (25)",
                "romantic" to "🌹 Romance",
                "sweet" to "🍫 Sweets",
                "party" to "💥 Bombs & Party",
                "vip" to "👑 VIP & Royal"
            )
            items(categories) { (catId, label) ->
                val isSelected = selectedCategory == catId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BrandGold else Color.White.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            if (isSelected) BrandGold else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedCategory = catId }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) BrandDark else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ROW 3: COMPACT 4-COLUMN 3D GIFTS GRID
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(filteredGifts) { gift ->
                val isSelected = selectedGift.id == gift.id
                Compact3DGiftCard(
                    gift = gift,
                    isSelected = isSelected,
                    onClick = { selectedGift = gift }
                )
            }
        }

        // ROW 4: BOTTOM QUANTITY & ACTION BAR
        BottomGiftActionBar(
            currentDiamonds = currentDiamonds,
            totalCost = totalCost,
            selectedQuantity = selectedQuantity,
            multiplier = multiplier,
            selectedTargetName = selectedTargetName,
            canAfford = canAfford,
            onSelectQuantity = { selectedQuantity = it },
            onSend = {
                if (canAfford) {
                    onSendGift(selectedGift, selectedQuantity, selectedTargetSeatIndex, selectedTargetName)
                } else {
                    onGoToRecharge()
                }
            }
        )
    }
}

// -------------------------------------------------------------------------
// 2. BACKPACK SECTION (INVENTORY TAB)
// -------------------------------------------------------------------------
@Composable
fun BackpackSection(
    currentUser: UserProfile,
    seatedUsers: List<MicSeat>,
    onSendBackpackGift: (gift: RoomGiftItem, quantity: Int, targetSeatIndex: Int, targetUserName: String) -> Unit,
    onClaimStarterGifts: () -> Unit
) {
    val context = LocalContext.current
    var selectedTargetSeatIndex by remember { mutableIntStateOf(-1) }
    var selectedTargetName by remember { mutableStateOf("All Mics") }
    var selectedQuantity by remember { mutableIntStateOf(1) }

    val occupiedSeats = remember(seatedUsers) {
        seatedUsers.filter { it.uid.isNotBlank() }
    }

    // Match gifts user owns with ROOM_GIFT_CATALOG
    val backpackMap = currentUser.backpack
    val ownedGifts = remember(backpackMap) {
        ROOM_GIFT_CATALOG.filter { (backpackMap[it.id] ?: 0) > 0 }
    }

    var selectedBackpackGift by remember(ownedGifts) {
        mutableStateOf(ownedGifts.firstOrNull() ?: ROOM_GIFT_CATALOG[0])
    }

    val availableCount = backpackMap[selectedBackpackGift.id] ?: 0
    val multiplier = if (selectedTargetSeatIndex == -1) maxOf(1, occupiedSeats.size) else 1
    val requiredCount = selectedQuantity * multiplier
    val hasEnough = availableCount >= requiredCount

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Target Selector
        TargetMicSelectorRow(
            occupiedSeats = occupiedSeats,
            selectedTargetIndex = selectedTargetSeatIndex,
            onSelectTarget = { seatIndex, name ->
                selectedTargetSeatIndex = seatIndex
                selectedTargetName = name
            }
        )

        if (ownedGifts.isEmpty()) {
            // EMPTY BACKPACK WELCOME CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF221730)),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, BrandGold.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎒", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your Backpack is Empty",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Collect daily gifts, win lucky spins, or claim your starter pack below to send gifts with 0 Diamonds!",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onClaimStarterGifts,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "🎁 Claim Free Starter Gifts (+5 Roses, +1 Bomb)",
                            color = BrandDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        } else {
            // GRID OF OWNED GIFTS
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(ownedGifts) { gift ->
                    val isSelected = selectedBackpackGift.id == gift.id
                    val count = backpackMap[gift.id] ?: 0

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(108.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF38234D) else Color(0xFF1E1529))
                            .border(
                                width = if (isSelected) 2.dp else 0.8.dp,
                                color = if (isSelected) BrandGold else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedBackpackGift = gift }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = gift.drawableResId),
                                contentDescription = gift.name,
                                modifier = Modifier.size(52.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = gift.name,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "Free (0 💎)",
                                color = BrandCyan,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quantity Badge in top-right corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.horizontalGradient(listOf(BrandCoral, BrandOrange)))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "x$count",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // BOTTOM BAR FOR BACKPACK
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1C1327))
                    .border(1.dp, BrandGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 5, 10).forEach { qty ->
                        val isQSel = selectedQuantity == qty
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isQSel) BrandGold else Color.White.copy(alpha = 0.08f))
                                .clickable { selectedQuantity = qty }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${qty}x",
                                color = if (isQSel) BrandDark else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Send Button
                Button(
                    onClick = {
                        if (hasEnough) {
                            onSendBackpackGift(selectedBackpackGift, selectedQuantity, selectedTargetSeatIndex, selectedTargetName)
                        } else {
                            Toast.makeText(context, "⚠️ You only have $availableCount in backpack!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasEnough) BrandCoral else Color.Gray
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (hasEnough) "Send Free 🎁 ($requiredCount/$availableCount)" else "Not Enough ($availableCount left)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 3. TARGET MIC SELECTOR ROW (All Mics vs Individual Seats)
// -------------------------------------------------------------------------
@Composable
fun TargetMicSelectorRow(
    occupiedSeats: List<MicSeat>,
    selectedTargetIndex: Int,
    onSelectTarget: (seatIndex: Int, targetName: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF171022))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Target:",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(6.dp))

        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // "All Mics" Chip
            item {
                val isAllSelected = selectedTargetIndex == -1
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isAllSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange)) else androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.08f)))
                        .clickable { onSelectTarget(-1, "All Mics") }
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "All Mics (${maxOf(1, occupiedSeats.size)})",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = if (isAllSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }

            // Individual Seated Users Chips
            items(occupiedSeats) { seat ->
                val isSelected = selectedTargetIndex == seat.seatIndex
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BrandGold else Color.White.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            if (isSelected) BrandGold else Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectTarget(seat.seatIndex, seat.name) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        AsyncImage(
                            model = seat.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                            contentDescription = seat.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (seat.seatIndex == 0) "Host (${seat.name.take(7)})" else seat.name.take(7),
                        color = if (isSelected) BrandDark else Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 4. COMPACT 3D GIFT CARD (Transparent Cutout Item)
// -------------------------------------------------------------------------
@Composable
private fun Compact3DGiftCard(
    gift: RoomGiftItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF332048) else Color(0xFF1E1529))
            .border(
                width = if (isSelected) 2.dp else 0.8.dp,
                color = if (isSelected) BrandGold else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Transparent 3D Cutout Image
            Image(
                painter = painterResource(id = gift.drawableResId),
                contentDescription = gift.name,
                modifier = Modifier
                    .size(54.dp)
                    .graphicsLayer(scaleX = if (isSelected) 1.08f else 1f, scaleY = if (isSelected) 1.08f else 1f),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = gift.name,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💎", fontSize = 8.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${gift.cost}",
                    color = BrandGold,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(BrandGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = BrandDark,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// 5. BOTTOM QUANTITY & SEND ACTION BAR
// -------------------------------------------------------------------------
@Composable
private fun BottomGiftActionBar(
    currentDiamonds: Long,
    totalCost: Long,
    selectedQuantity: Int,
    multiplier: Int,
    selectedTargetName: String,
    canAfford: Boolean,
    onSelectQuantity: (Int) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1B1325))
            .border(1.dp, BrandGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quantity Combo Chips (1x, 10x, 20x, 100x)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(1, 10, 20, 100).forEach { qty ->
                val isSelected = selectedQuantity == qty
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BrandGold else Color.White.copy(alpha = 0.08f))
                        .clickable { onSelectQuantity(qty) }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${qty}x",
                        color = if (isSelected) BrandDark else Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Send Button & Total Calculation
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total: %,d 💎".format(totalCost),
                    color = BrandGold,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (multiplier > 1) {
                    Text(
                        text = "($multiplier Mics)",
                        color = Color.LightGray,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) BrandCoral else Color(0xFF55382A)
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (canAfford) "SEND 🎁" else "+ Recharge",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// 6. REALISTIC 3D MICRO-GIFT FLIGHT & FIRECRACKER BURST OVERLAY
// -------------------------------------------------------------------------
@Composable
fun MicroGiftFlightAndBurstOverlay(
    giftEvent: GiftEvent,
    onFinished: () -> Unit
) {
    // Slower, dramatic, highly visible flight & celebration duration (4.2 seconds)
    val duration = 4200L
    val transition = rememberInfiniteTransition(label = "micro_anim")

    // Rotation angle for burst sparkles
    val burstRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "burst_rot"
    )

    // Pulse scale for celebration
    val burstPulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "burst_pulse"
    )

    // Progression of flight (0.0f -> 1.0f)
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(giftEvent.id) {
        val startTime = SystemClock.uptimeMillis()
        while (true) {
            val elapsed = SystemClock.uptimeMillis() - startTime
            val p = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            progress = p
            if (p >= 1f) {
                onFinished()
                break
            }
            delay(16)
        }
    }

    // Determine drawable
    val drawableId = remember(giftEvent.giftId) {
        giftEvent.drawableResId.takeIf { it != 0 }
            ?: ROOM_GIFT_CATALOG.find { it.id == giftEvent.giftId }?.drawableResId
            ?: R.drawable.gift_single_rose
    }

    // Target positions: If "All Mics" (-1), fan out to all 8 seats; if single mic, target that seat
    val targetCoordinates = remember(giftEvent.targetMicSeatIndex) {
        if (giftEvent.targetMicSeatIndex == -1) {
            listOf(
                Pair(-115f, -240f), // Mic 0 (Host)
                Pair(-38f, -240f),  // Mic 1
                Pair(38f, -240f),   // Mic 2
                Pair(115f, -240f),  // Mic 3
                Pair(-115f, -140f), // Mic 4
                Pair(-38f, -140f),  // Mic 5
                Pair(38f, -140f),   // Mic 6
                Pair(115f, -140f)   // Mic 7
            )
        } else {
            val single = when (giftEvent.targetMicSeatIndex) {
                0 -> Pair(-115f, -240f)
                1 -> Pair(-38f, -240f)
                2 -> Pair(38f, -240f)
                3 -> Pair(115f, -240f)
                4 -> Pair(-115f, -140f)
                5 -> Pair(-38f, -140f)
                6 -> Pair(38f, -140f)
                7 -> Pair(115f, -140f)
                else -> Pair(0f, -200f)
            }
            listOf(single)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        // 1. TOP PROMINENT CELEBRATION ALERT BANNER
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.90f))
                    .border(1.5.dp, BrandGold, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, BrandGold, CircleShape)
                ) {
                    AsyncImage(
                        model = giftEvent.senderPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                        contentDescription = giftEvent.senderName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "🎉 ${giftEvent.senderName} gifted ${giftEvent.giftName} x${giftEvent.quantity}!",
                        color = BrandGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "To: ${giftEvent.targetUserName} • ${if (giftEvent.fromBackpack) "🎒 From Backpack" else "💎 ${giftEvent.cost}"}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = giftEvent.giftName,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // 2. FLIGHT TRAJECTORY (0.0f to 0.45f) & MULTI-TARGET FIRECRACKER BURST (0.45f to 1.0f)
        val isBurstPhase = progress > 0.45f

        if (!isBurstPhase) {
            val flightP = (progress / 0.45f).coerceIn(0f, 1f)
            val startX = 260f
            val startY = 560f

            targetCoordinates.forEach { (targetX, targetY) ->
                val ctrlX = (startX + targetX) / 2f + 40f
                val ctrlY = 80f
                val currentX = (1 - flightP) * (1 - flightP) * startX + 2 * (1 - flightP) * flightP * ctrlX + flightP * flightP * targetX
                val currentY = (1 - flightP) * (1 - flightP) * startY + 2 * (1 - flightP) * flightP * ctrlY + flightP * flightP * targetY

                Box(
                    modifier = Modifier
                        .offset(x = currentX.dp, y = currentY.dp)
                        .graphicsLayer(
                            rotationZ = (flightP * 20f),
                            scaleX = (0.65f + flightP * 0.45f),
                            scaleY = (0.65f + flightP * 0.45f)
                        )
                ) {
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = giftEvent.giftName,
                        modifier = Modifier.size(if (targetCoordinates.size > 1) 60.dp else 90.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        } else {
            // Burst Phase: Firecracker explosion over EACH target user profile seat!
            val burstP = ((progress - 0.45f) / 0.55f).coerceIn(0f, 1f)
            val burstAlpha = (1f - (burstP * burstP)).coerceIn(0f, 1f)

            targetCoordinates.forEach { (targetX, targetY) ->
                Box(
                    modifier = Modifier
                        .offset(x = targetX.dp, y = targetY.dp)
                        .graphicsLayer(alpha = burstAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    // Expanding Firecracker Sparkler Rays
                    Box(
                        modifier = Modifier
                            .size(if (targetCoordinates.size > 1) 120.dp else 190.dp)
                            .graphicsLayer(rotationZ = burstRotation, scaleX = burstPulse, scaleY = burstPulse)
                            .border(
                                2.dp,
                                Brush.sweepGradient(listOf(BrandGold, Color(0xFFFF4081), Color(0xFFFF9800), BrandGold)),
                                CircleShape
                            )
                    )

                    // Sparkler Starburst Emojis
                    Text("💥 ✨", fontSize = if (targetCoordinates.size > 1) 22.sp else 34.sp)

                    // Central 3D Gift Scaled Up During Blast
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = giftEvent.giftName,
                        modifier = Modifier
                            .size(if (targetCoordinates.size > 1) 64.dp else 100.dp)
                            .graphicsLayer(scaleX = (1.0f + burstP * 0.35f), scaleY = (1.0f + burstP * 0.35f)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
