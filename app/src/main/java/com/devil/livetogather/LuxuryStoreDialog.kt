package com.devil.livetogather

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ---------------------------------------------------------------------
// LUXURY STORE DIALOG (DEEP MIDNIGHT DARK LUXURY THEME)
// ---------------------------------------------------------------------

@Composable
fun LuxuryStoreDialog(
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onOpenTopUp: () -> Unit = {},
    onProfileUpdated: (UserProfile) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(StoreCategory.INTIMACY) }
    var selectedSubCategory by remember { mutableStateOf("Ring") }
    var showInventoryDialog by remember { mutableStateOf(false) }
    var isPurchasing by remember { mutableStateOf(false) }
    var previewVideoItem by remember { mutableStateOf<StoreItem?>(null) }

    // When category changes, auto-select its first sub-category
    LaunchedEffect(selectedCategory) {
        selectedSubCategory = when (selectedCategory) {
            StoreCategory.INTIMACY -> "Ring"
            StoreCategory.AVATAR -> "Avatar Frame"
            StoreCategory.MIC_EFFECTS -> "All Effects"
            StoreCategory.ENTRANCE -> "All Entrances"
        }
    }

    // Filter items according to category and sub-category
    val currentItems = remember(selectedCategory, selectedSubCategory) {
        when (selectedCategory) {
            StoreCategory.INTIMACY -> {
                when (selectedSubCategory) {
                    "Ring" -> LuxuryStoreCatalog.rings.filter { it.subCategory == "Ring" }
                    "Jewelry" -> LuxuryStoreCatalog.rings.filter { it.subCategory == "Jewelry" }
                    "Love Zone" -> LuxuryStoreCatalog.rings.filter { it.subCategory == "Love Zone" }
                    "Mic Link" -> LuxuryStoreCatalog.micLinks
                    "Special" -> LuxuryStoreCatalog.micLinks
                    else -> LuxuryStoreCatalog.rings + LuxuryStoreCatalog.micLinks
                }
            }
            StoreCategory.AVATAR -> {
                when (selectedSubCategory) {
                    "Avatar Frame" -> LuxuryStoreCatalog.avatarFrames
                    "CP Avatar Frame" -> LuxuryStoreCatalog.avatarFrames.filter { it.subCategory == "VIP Frame" }
                    "Dynamic Avatar" -> LuxuryStoreCatalog.avatarFrames.filter { it.rankTier == "S" || it.rankTier == "A" }
                    else -> LuxuryStoreCatalog.avatarFrames
                }
            }
            StoreCategory.MIC_EFFECTS -> {
                when (selectedSubCategory) {
                    "All Effects" -> LuxuryStoreCatalog.micEffects
                    "Sound Wave" -> LuxuryStoreCatalog.micEffects.filter { it.subCategory == "Sound Wave" }
                    "Mic Animation" -> LuxuryStoreCatalog.micEffects.filter { it.subCategory == "Mic Animation" || it.subCategory == "Mic Color Name" }
                    else -> LuxuryStoreCatalog.micEffects
                }
            }
            StoreCategory.ENTRANCE -> {
                when (selectedSubCategory) {
                    "All Entrances" -> LuxuryStoreCatalog.entrances
                    "VIP Vehicles" -> LuxuryStoreCatalog.entrances.filter { it.subCategory == "VIP Vehicles" }
                    "Aircrafts" -> LuxuryStoreCatalog.entrances.filter { it.subCategory == "Aircrafts" }
                    "Banners" -> LuxuryStoreCatalog.entrances.filter { it.subCategory == "Banners" }
                    else -> LuxuryStoreCatalog.entrances
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0B14)),
            color = Color(0xFF0D0B14)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {

                // 1. TOP HEADER (Back, "Store 👑", Voucher & Backpack Icons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF1B172B), CircleShape)
                            .border(1.dp, Color(0xFF382B59), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Store",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Discount Coupon icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B172B))
                                .border(1.dp, Color(0xFF382B59), CircleShape)
                                .clickable {
                                    Toast.makeText(context, "No active discount coupons at this time", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏷️", fontSize = 16.sp)
                        }

                        // Backpack / My Inventory icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B172B))
                                .border(1.dp, Color(0xFFFFD700), CircleShape)
                                .clickable { showInventoryDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎒", fontSize = 16.sp)
                        }
                    }
                }

                // 2. REAL-TIME BALANCE BAR (Diamonds, Coins, VIP Tokens)
                Surface(
                    color = Color(0xFF181329),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFF382B59)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Balance:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB3A1D6)
                        )

                        // Diamonds
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2E1A47))
                                .border(1.dp, Color(0xFF5B308C), RoundedCornerShape(12.dp))
                                .clickable { onOpenTopUp() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("💎", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%,d", currentUser.diamonds),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE9D5FF)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                        }

                        // Coins
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3B2A10))
                                .border(1.dp, Color(0xFF855D18), RoundedCornerShape(12.dp))
                                .clickable { onOpenTopUp() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🪙", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%,d", currentUser.coins),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFEF08A)
                            )
                        }

                        // Crown Points
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2A203F))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text("👑", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "VIP ${currentUser.getVipLevel()}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. TOP CATEGORIES ICON CAROUSEL (Intimacy, Avatar, Profile, Entrance, Chat Frame, Mic Effects)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StoreCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF7E22CE), Color(0xFF9333EA))
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF171328), Color(0xFF1B172E))
                                        )
                                    }
                                )
                                .border(
                                    1.2.dp,
                                    if (isSelected) Color(0xFFFFD700) else Color(0xFF2E2648),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(cat.icon, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = cat.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFFA498BF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. SUB-CATEGORY TABS
                val subTabs = when (selectedCategory) {
                    StoreCategory.INTIMACY -> listOf("Ring", "Jewelry", "Love Zone", "Mic Link", "Special")
                    StoreCategory.AVATAR -> listOf("Avatar Frame", "CP Avatar Frame", "Dynamic Avatar")
                    StoreCategory.MIC_EFFECTS -> listOf("All Effects", "Sound Wave", "Mic Animation")
                    StoreCategory.ENTRANCE -> listOf("All Entrances", "VIP Vehicles", "Aircrafts", "Banners")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    subTabs.forEach { tabName ->
                        val isSubSelected = selectedSubCategory == tabName
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedSubCategory = tabName }
                                .padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = tabName,
                                fontSize = 13.sp,
                                fontWeight = if (isSubSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSubSelected) Color(0xFFFFD700) else Color(0xFF8E83A8)
                            )
                            if (isSubSelected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .width(26.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFFFD700))
                                )
                            }
                        }
                    }
                }

                Divider(color = Color(0xFF261F38), thickness = 1.dp)

                // 5. STORE ITEMS GRID (MATCHING REFERENCE IMAGES IN HIGH CONTRAST DARK LUXURY)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentItems) { item ->
                        StoreItemCard(
                            item = item,
                            currentUser = currentUser,
                            isPurchasing = isPurchasing,
                            onPreviewVideo = { previewVideoItem = it },
                            onEquipToggle = { targetItem ->
                                if (isPurchasing) return@StoreItemCard
                                isPurchasing = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val isMicEffect = targetItem.category == "mic_effect"
                                        val targetEquipId = if (isMicEffect) {
                                            if (currentUser.activeMicEffect == targetItem.id) "" else targetItem.id
                                        } else {
                                            if (currentUser.activeFrame == targetItem.id) "" else targetItem.id
                                        }
                                        val fieldName = if (isMicEffect) "activeMicEffect" else "activeFrame"
                                        db.collection("users").document(currentUser.uid).update(fieldName, targetEquipId)
                                        val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
                                        prefs.edit().putString(if (isMicEffect) "profile_active_mic_effect" else "profile_active_frame", targetEquipId).apply()
                                        withContext(Dispatchers.Main) {
                                            val updatedUser = if (isMicEffect) {
                                                currentUser.copy(activeMicEffect = targetEquipId)
                                            } else {
                                                currentUser.copy(activeFrame = targetEquipId)
                                            }
                                            onProfileUpdated(updatedUser)
                                            Toast.makeText(
                                                context,
                                                if (targetEquipId.isNotBlank()) "✨ Equipped ${targetItem.name}!" else "${targetItem.name} removed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } finally {
                                        withContext(Dispatchers.Main) {
                                            isPurchasing = false
                                        }
                                    }
                                }
                            },
                            onPurchase = { targetItem ->
                                if (isPurchasing) return@StoreItemCard

                                // Validation
                                if (currentUser.diamonds < targetItem.diamondPrice) {
                                    Toast.makeText(
                                        context,
                                        "Insufficient Diamonds 💎 (Need ${String.format("%,d", targetItem.diamondPrice)})",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onOpenTopUp()
                                    return@StoreItemCard
                                }

                                isPurchasing = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val now = System.currentTimeMillis()
                                        val durationMs = targetItem.durationDays * 24L * 60L * 60L * 1000L
                                        val expiry = now + durationMs
                                        val isMicEffect = targetItem.category == "mic_effect"
                                        val newDiamonds = currentUser.diamonds - targetItem.diamondPrice
                                        val newSpentCount = currentUser.diamondsSpentCount + 1

                                        val updates = mutableMapOf<String, Any>(
                                            "diamonds" to newDiamonds,
                                            "diamondsSpentCount" to FieldValue.increment(1)
                                        )

                                        val updatedUser: UserProfile
                                        if (isMicEffect) {
                                            val newOwned = currentUser.ownedMicEffects.toMutableList().apply {
                                                if (!contains(targetItem.id)) add(targetItem.id)
                                            }
                                            val newExpiries = currentUser.micEffectExpiries.toMutableMap().apply {
                                                put(targetItem.id, expiry)
                                            }
                                            updates["ownedMicEffects"] = FieldValue.arrayUnion(targetItem.id)
                                            updates["activeMicEffect"] = targetItem.id
                                            updates["micEffectExpiries"] = newExpiries

                                            updatedUser = currentUser.copy(
                                                diamonds = newDiamonds,
                                                diamondsSpentCount = newSpentCount,
                                                ownedMicEffects = newOwned,
                                                activeMicEffect = targetItem.id,
                                                micEffectExpiries = newExpiries
                                            )
                                        } else {
                                            val newOwned = currentUser.ownedFrames.toMutableList().apply {
                                                if (!contains(targetItem.id)) add(targetItem.id)
                                            }
                                            val newExpiries = currentUser.frameExpiries.toMutableMap().apply {
                                                put(targetItem.id, expiry)
                                            }
                                            updates["ownedFrames"] = FieldValue.arrayUnion(targetItem.id)
                                            if (targetItem.category == "avatar_frame") {
                                                updates["activeFrame"] = targetItem.id
                                            }
                                            updates["frameExpiries"] = newExpiries

                                            updatedUser = currentUser.copy(
                                                diamonds = newDiamonds,
                                                diamondsSpentCount = newSpentCount,
                                                ownedFrames = newOwned,
                                                activeFrame = if (targetItem.category == "avatar_frame") targetItem.id else currentUser.activeFrame,
                                                frameExpiries = newExpiries
                                            )
                                        }

                                        db.collection("users").document(currentUser.uid).update(updates)

                                        // Local cache update
                                        val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
                                        prefs.edit()
                                            .putLong("user_diamonds", newDiamonds)
                                            .apply()

                                        if (isMicEffect) {
                                            prefs.edit().putString("profile_active_mic_effect", targetItem.id).apply()
                                        } else if (targetItem.category == "avatar_frame") {
                                            prefs.edit().putString("profile_active_frame", targetItem.id).apply()
                                        }

                                        withContext(Dispatchers.Main) {
                                            onProfileUpdated(updatedUser)
                                            Toast.makeText(
                                                context,
                                                "🎉 Successfully acquired ${targetItem.name}!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Purchase failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } finally {
                                        withContext(Dispatchers.Main) {
                                            isPurchasing = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // INVENTORY / BACKPACK DIALOG
    if (showInventoryDialog) {
        InventoryBackpackDialog(
            currentUser = currentUser,
            db = db,
            onDismiss = { showInventoryDialog = false },
            onProfileUpdated = onProfileUpdated
        )
    }

    // 3D VIDEO PREVIEW DIALOG
    previewVideoItem?.let { item ->
        if (item.videoRes != 0) {
            StoreVideoPreviewDialog(
                videoRes = item.videoRes,
                itemName = item.name,
                onDismiss = { previewVideoItem = null }
            )
        }
    }
}

// ---------------------------------------------------------------------
// STORE ITEM CARD (WITH TIER BADGE, DURATION, PRICE & PURCHASE PILL)
// ---------------------------------------------------------------------

@Composable
fun StoreItemCard(
    item: StoreItem,
    currentUser: UserProfile,
    isPurchasing: Boolean,
    onPreviewVideo: (StoreItem) -> Unit = {},
    onEquipToggle: (StoreItem) -> Unit = {},
    onPurchase: (StoreItem) -> Unit
) {
    val now = System.currentTimeMillis()
    val expiry = if (item.category == "mic_effect") currentUser.micEffectExpiries[item.id] ?: 0L else currentUser.frameExpiries[item.id] ?: 0L
    val isExpired = expiry > 0L && now > expiry
    val isOwned = (!isExpired) && (if (item.category == "mic_effect") {
        currentUser.ownedMicEffects.contains(item.id) || currentUser.activeMicEffect == item.id
    } else {
        currentUser.ownedFrames.contains(item.id) || currentUser.activeFrame == item.id
    })
    val isEquipped = if (item.category == "mic_effect") {
        currentUser.activeMicEffect == item.id
    } else {
        currentUser.activeFrame == item.id
    }
    val hasVideo = item.videoRes != 0

    Surface(
        color = Color(0xFF161224),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.2.dp,
            if (isEquipped) Color(0xFFFFD700) else Color(0xFF2E2648)
        ),
        shadowElevation = if (isEquipped) 6.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (hasVideo) {
                    onPreviewVideo(item)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Video Preview Tag + Rank Tier Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasVideo) {
                    Surface(
                        color = Color(0xFFFF2A6D),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onPreviewVideo(item) }
                    ) {
                        Text(
                            text = "▶ 3D",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Rank Tier Badge (S, A, B, C, D, E, F)
                val badgeColor = when (item.rankTier) {
                    "S" -> Color(0xFFFF9100)
                    "A" -> Color(0xFF2979FF)
                    "B" -> Color(0xFF00E676)
                    "C" -> Color(0xFFE040FB)
                    "D" -> Color(0xFF7E57C2)
                    "E" -> Color(0xFF00B0FF)
                    else -> Color(0xFF78909C)
                }
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.size(17.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.rankTier,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Visual Icon / Live Animated Frame / 3D Graphics
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(item.primaryColor).copy(alpha = 0.28f),
                                Color(item.secondaryColor).copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (item.category) {
                    "avatar_frame" -> {
                        // LIVE FLOWING ANIMATED PREVIEW (High contrast on Dark Background)
                        FlowingAvatarFrame(
                            photoUrl = currentUser.photo,
                            frameId = item.id,
                            avatarSize = 34.dp,
                            frameSize = 58.dp,
                            showAnimation = true,
                            showVipBadge = false,
                            showMicEffect = false
                        )
                    }
                    "mic_effect" -> {
                        FlowingAvatarFrame(
                            photoUrl = currentUser.photo,
                            frameId = "",
                            avatarSize = 34.dp,
                            frameSize = 58.dp,
                            showAnimation = true,
                            showVipBadge = false,
                            showMicEffect = true,
                            micEffectId = item.id
                        )
                    }
                    "mic_link" -> {
                        MicLinkPreviewVisual(
                            item = item,
                            size = 52.dp
                        )
                    }
                    "ring" -> {
                        val res = if (item.drawableRes != 0) item.drawableRes else R.drawable.gift_diamond_ring
                        Image(
                            painter = painterResource(id = res),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    "entrance" -> {
                        if (item.drawableRes != 0) {
                            Image(
                                painter = painterResource(id = item.drawableRes),
                                contentDescription = item.name,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = item.iconEmoji,
                                fontSize = 26.sp
                            )
                        }
                    }
                    else -> {
                        val thumb = if (item.drawableRes != 0) item.drawableRes else R.drawable.thumb_diamond_ring_proposal
                        Image(
                            painter = painterResource(id = thumb),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Item Name
            Text(
                text = item.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Price in Diamonds
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("💎", fontSize = 10.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = String.format("%,d", item.diamondPrice),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700)
                )
            }

            val daysLeftText = if (isOwned && expiry > now) {
                val d = ((expiry - now) / (1000L * 60 * 60 * 24)).toInt() + 1
                "⏳ $d Days Left"
            } else {
                "${item.durationDays} Days"
            }
            Text(
                text = daysLeftText,
                fontSize = 9.sp,
                color = if (isOwned) Color(0xFFFFD700) else Color(0xFFA498BF),
                fontWeight = if (isOwned) FontWeight.Bold else FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Action Button
            when {
                isEquipped -> {
                    Surface(
                        color = Color(0xFF2A1C44),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isPurchasing) { onEquipToggle(item) }
                    ) {
                        Text(
                            text = if (item.category == "mic_effect") "Wearing • Tap to Unwear" else "Equipped (Tap to Remove)",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                isOwned -> {
                    Surface(
                        color = Color(0xFF7C4DFF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isPurchasing) { onEquipToggle(item) }
                    ) {
                        Text(
                            text = if (item.category == "mic_effect") "Wear (Equip)" else "Equip",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                item.isActivityOnly -> {
                    Surface(
                        color = Color(0xFF262035),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Activity Only",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9E95B3),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                item.isNotForSale -> {
                    Surface(
                        color = Color(0xFF262035),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Not for sale",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9E95B3),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                item.isIntimacyBondOnly -> {
                    Surface(
                        color = Color(0xFFE040FB),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isPurchasing) { onPurchase(item) }
                    ) {
                        Text(
                            text = "Intimacy Bond",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                else -> {
                    // Standard Purchase Button (Vibrant Gold/Orange Pill)
                    Surface(
                        color = Color(0xFFFF9800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isPurchasing) { onPurchase(item) }
                    ) {
                        Text(
                            text = "Buy 💎 ${item.diamondPrice}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// 3D VIDEO PREVIEW DIALOG (SMOOTH PLAYBACK OF REAL 3D MP4 ENTRANCES)
// ---------------------------------------------------------------------

@Composable
fun StoreVideoPreviewDialog(
    videoRes: Int,
    itemName: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF161224))
                    .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎬", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = itemName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            TextureView(ctx).apply {
                                isOpaque = false
                                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                    var mediaPlayer: MediaPlayer? = null
                                    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                        val s = Surface(surface)
                                        mediaPlayer = MediaPlayer().apply {
                                            setSurface(s)
                                            val videoUri = Uri.parse("android.resource://${ctx.packageName}/$videoRes")
                                            setDataSource(ctx, videoUri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                mp.setVolume(1.0f, 1.0f)
                                                mp.start()
                                            }
                                            prepareAsync()
                                        }
                                    }
                                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        return true
                                    }
                                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "✨ 3D High-Definition Live Preview",
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// BACKPACK / INVENTORY DIALOG (VIEW & EQUIP OWNED ITEMS IN DARK LUXURY)
// ---------------------------------------------------------------------

@Composable
fun InventoryBackpackDialog(
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onProfileUpdated: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isEquipping by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF171328),
            border = BorderStroke(1.2.dp, Color(0xFF382B59)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎒 My Backpack & Inventory",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val ownedItems = remember(currentUser.ownedFrames, currentUser.ownedMicEffects, currentUser.activeMicEffect) {
                    LuxuryStoreCatalog.allItems.filter { 
                        currentUser.ownedFrames.contains(it.id) || 
                        currentUser.ownedMicEffects.contains(it.id) || 
                        currentUser.activeMicEffect == it.id 
                    }
                }

                if (ownedItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎒", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your backpack is empty.\nExplore the Store to collect frames & rings!",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ownedItems) { item ->
                            val isMicEffect = item.category == "mic_effect"
                            val isEquipped = if (isMicEffect) currentUser.activeMicEffect == item.id else currentUser.activeFrame == item.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isEquipped) Color(0xFF2A1C44) else Color(0xFF201B36),
                                border = BorderStroke(
                                    1.dp,
                                    if (isEquipped) Color(0xFFFFD700) else Color(0xFF382B59)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val thumb = if (item.drawableRes != 0) item.drawableRes else R.drawable.thumb_diamond_ring_proposal
                                    Image(
                                        painter = painterResource(id = thumb),
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            if (isEquipping) return@Button
                                            isEquipping = true
                                            coroutineScope.launch(Dispatchers.IO) {
                                                try {
                                                    val targetEquipId = if (isEquipped) "" else item.id
                                                    val fieldToUpdate = if (isMicEffect) "activeMicEffect" else "activeFrame"
                                                    db.collection("users").document(currentUser.uid)
                                                        .update(fieldToUpdate, targetEquipId)
                                                    val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
                                                    prefs.edit().putString(if (isMicEffect) "profile_active_mic_effect" else "profile_active_frame", targetEquipId).apply()
                                                    withContext(Dispatchers.Main) {
                                                        val updated = if (isMicEffect) currentUser.copy(activeMicEffect = targetEquipId) else currentUser.copy(activeFrame = targetEquipId)
                                                        onProfileUpdated(updated)
                                                        Toast.makeText(
                                                            context,
                                                            if (targetEquipId.isNotBlank()) "Equipped ${item.name}!" else "Unequipped!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } finally {
                                                    withContext(Dispatchers.Main) {
                                                        isEquipping = false
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isEquipped) Color(0xFF3E3159) else Color(0xFF7E22CE)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = if (isEquipped) "Unequip" else "Equip",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEquipped) Color(0xFFFFD700) else Color.White
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
