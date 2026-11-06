package com.devil.livetogather

import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

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
    roomId: String = "",
    onDismiss: () -> Unit,
    onOpenTopUp: () -> Unit = {},
    onProfileUpdated: (UserProfile) -> Unit = {},
    onSendIntimacyItem: ((StoreItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(StoreCategory.INTIMACY) }
    var selectedSubCategory by remember { mutableStateOf("Proposal Ring") }
    var showInventoryDialog by remember { mutableStateOf(false) }
    var isPurchasing by remember { mutableStateOf(false) }
    var previewVideoItem by remember { mutableStateOf<StoreItem?>(null) }
    var itemToSendIntimacyProposal by remember { mutableStateOf<StoreItem?>(null) }

    // When category changes, auto-select its first sub-category
    LaunchedEffect(selectedCategory) {
        selectedSubCategory = when (selectedCategory) {
            StoreCategory.INTIMACY -> "Proposal Ring"
            StoreCategory.AVATAR -> "Avatar Frame"
            StoreCategory.PROFILE_FRAME -> "Monthly Card"
            StoreCategory.MIC_EFFECTS -> "All Effects"
            StoreCategory.ENTRANCE -> "All Entrances"
            StoreCategory.HONOR_BADGES -> "All Honors"
        }
    }

    // Filter items according to category and sub-category
    val currentItems = remember(selectedCategory, selectedSubCategory) {
        when (selectedCategory) {
            StoreCategory.INTIMACY -> {
                when (selectedSubCategory) {
                    "Proposal Ring", "Ring" -> LuxuryStoreCatalog.rings.filter { it.subCategory == "Proposal Ring" || it.subCategory == "Ring" }
                    "Jewelry" -> LuxuryStoreCatalog.rings.filter { it.subCategory == "Jewelry" }
                    "Love Zone" -> LuxuryStoreCatalog.rings.filter { it.subCategory == "Love Zone" }
                    "Mic Link" -> LuxuryStoreCatalog.micLinks
                    "All Intimacy", "Special" -> LuxuryStoreCatalog.rings + LuxuryStoreCatalog.micLinks
                    else -> LuxuryStoreCatalog.rings + LuxuryStoreCatalog.micLinks
                }
            }
            StoreCategory.AVATAR -> {
                when (selectedSubCategory) {
                    "Avatar Frame" -> LuxuryStoreCatalog.avatarFrames
                    "CP Avatar Frame" -> LuxuryStoreCatalog.avatarFrames.filter { it.rankTier == "S" }
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
            StoreCategory.PROFILE_FRAME -> emptyList()
            StoreCategory.ENTRANCE -> {
                when (selectedSubCategory) {
                    "All Entrances" -> LuxuryStoreCatalog.entrances
                    "Mythic Rides" -> LuxuryStoreCatalog.entrances.filter { it.subCategory == "Mythic Rides" }
                    "Aircrafts" -> LuxuryStoreCatalog.entrances.filter { it.subCategory == "Aircrafts" }
                    "Banners" -> LuxuryStoreCatalog.entrances.filter { it.subCategory == "Banners" }
                    else -> LuxuryStoreCatalog.entrances
                }
            }
            StoreCategory.HONOR_BADGES -> emptyList()
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
                    StoreCategory.INTIMACY -> listOf("Proposal Ring", "Jewelry", "Love Zone", "Mic Link", "All Intimacy")
                    StoreCategory.AVATAR -> listOf("Avatar Frame", "CP Avatar Frame", "Dynamic Avatar")
                    StoreCategory.PROFILE_FRAME -> listOf("Monthly Card")
                    StoreCategory.MIC_EFFECTS -> listOf("All Effects", "Sound Wave", "Mic Animation")
                    StoreCategory.ENTRANCE -> listOf("All Entrances", "Mythic Rides", "Aircrafts", "Banners")
                    StoreCategory.HONOR_BADGES -> listOf("All Honors")
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
                if (selectedCategory == StoreCategory.PROFILE_FRAME) {
                    ProfileFrameMonthlyCardSection(
                        currentUser = currentUser,
                        db = db,
                        onProfileUpdated = onProfileUpdated,
                        modifier = Modifier.weight(1f)
                    )
                } else if (selectedCategory == StoreCategory.HONOR_BADGES) {
                    HonorPavilionContent(
                        currentUser = currentUser,
                        db = db,
                        onOpenTopUp = onOpenTopUp,
                        onProfileUpdated = onProfileUpdated,
                        modifier = Modifier.weight(1f)
                    )
                } else LazyVerticalGrid(
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
                            onSendIntimacyItem = { item ->
                                if (onSendIntimacyItem != null) {
                                    onSendIntimacyItem.invoke(item)
                                } else {
                                    itemToSendIntimacyProposal = item
                                }
                            },
                            onEquipToggle = { targetItem ->
                                if (isPurchasing) return@StoreItemCard
                                isPurchasing = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val isMicEffect = targetItem.category == "mic_effect"
                                        val isMicLink = targetItem.category == "mic_link" || targetItem.subCategory.contains("Mic Link", ignoreCase = true) || targetItem.id.startsWith("mic_link_")
                                        val isAvatarFrame = targetItem.category == "avatar_frame"
                                        val isRing = targetItem.category == "ring" || targetItem.category == "proposal_ring"

                                        val targetEquipId = when {
                                            isMicEffect -> if (currentUser.activeMicEffect == targetItem.id) "" else targetItem.id
                                            isMicLink -> if (currentUser.activeMicLinkId == targetItem.id || currentUser.activeIntimacyBadge == targetItem.id) "" else targetItem.id
                                            isAvatarFrame -> if (currentUser.activeFrame == targetItem.id) "" else targetItem.id
                                            isRing -> if (currentUser.activeIntimacyBadge == targetItem.id) "" else targetItem.id
                                            else -> ""
                                        }
                                        val fieldName = when {
                                            isMicEffect -> "activeMicEffect"
                                            isMicLink -> "activeMicLinkId"
                                            isAvatarFrame -> "activeFrame"
                                            isRing -> "activeIntimacyBadge"
                                            else -> ""
                                        }

                                        if (fieldName.isNotBlank()) {
                                            val updates = mutableMapOf<String, Any>(fieldName to targetEquipId)
                                            if (isMicLink) {
                                                updates["activeIntimacyBadge"] = targetEquipId
                                            }
                                            db.collection("users").document(currentUser.uid).update(updates)
                                            val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
                                            when {
                                                isMicEffect -> prefs.edit().putString("profile_active_mic_effect", targetEquipId).apply()
                                                isMicLink -> prefs.edit().putString("profile_active_mic_link", targetEquipId).putString("profile_active_intimacy_badge", targetEquipId).apply()
                                                isAvatarFrame -> prefs.edit().putString("profile_active_frame", targetEquipId).apply()
                                                isRing -> prefs.edit().putString("profile_active_intimacy_badge", targetEquipId).apply()
                                            }
                                            withContext(Dispatchers.Main) {
                                                val updatedUser = when {
                                                    isMicEffect -> currentUser.copy(activeMicEffect = targetEquipId)
                                                    isMicLink -> currentUser.copy(activeMicLinkId = targetEquipId, activeIntimacyBadge = targetEquipId)
                                                    isAvatarFrame -> currentUser.copy(activeFrame = targetEquipId)
                                                    isRing -> currentUser.copy(activeIntimacyBadge = targetEquipId)
                                                    else -> currentUser
                                                }
                                                onProfileUpdated(updatedUser)
                                                Toast.makeText(
                                                    context,
                                                    if (targetEquipId.isNotBlank()) "✨ Wearing ${targetItem.name}!" else "${targetItem.name} removed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
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
                                        } else if (targetItem.category == "mic_link") {
                                            val newOwned = currentUser.ownedMicLinks.toMutableList().apply {
                                                if (!contains(targetItem.id)) add(targetItem.id)
                                            }
                                            val newExpiries = currentUser.micLinkExpiries.toMutableMap().apply {
                                                put(targetItem.id, expiry)
                                            }
                                            updates["ownedMicLinks"] = FieldValue.arrayUnion(targetItem.id)
                                            updates["activeMicLinkId"] = targetItem.id
                                            updates["activeIntimacyBadge"] = targetItem.id
                                            updates["micLinkExpiries"] = newExpiries

                                            updatedUser = currentUser.copy(
                                                diamonds = newDiamonds,
                                                diamondsSpentCount = newSpentCount,
                                                ownedMicLinks = newOwned,
                                                activeMicLinkId = targetItem.id,
                                                activeIntimacyBadge = targetItem.id,
                                                micLinkExpiries = newExpiries
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

                                        if (roomId.isNotBlank()) {
                                            val purchaseComment = mapOf(
                                                "id" to java.util.UUID.randomUUID().toString(),
                                                "userName" to currentUser.name,
                                                "userPhoto" to (currentUser.photo ?: ""),
                                                "message" to "💎 Just unlocked ${targetItem.name}! ✨",
                                                "timestamp" to System.currentTimeMillis(),
                                                "isSystem" to true,
                                                "wealthLevel" to currentUser.getEffectiveWealthLevel()
                                            )
                                            db.collection("rooms").document(roomId).collection("live_comments").add(purchaseComment)
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

    // INTIMACY RING RECIPIENT SELECTOR DIALOG (SUGGESTIONS FOR FOLLOWING & CLOSE FRIENDS)
    itemToSendIntimacyProposal?.let { ringItem ->
        IntimacyRecipientSelectorDialog(
            ringItem = ringItem,
            currentUser = currentUser,
            db = db,
            roomId = roomId,
            onDismiss = { itemToSendIntimacyProposal = null },
            onProposalSent = { recipientName, updatedBal ->
                itemToSendIntimacyProposal = null
                onProfileUpdated(currentUser.copy(diamonds = updatedBal))
            }
        )
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
    onSendIntimacyItem: ((StoreItem) -> Unit)? = null,
    onPurchase: (StoreItem) -> Unit
) {
    val now = System.currentTimeMillis()
    val isMicEffect = item.category == "mic_effect"
    val isMicLink = item.category == "mic_link" || item.subCategory.contains("Mic Link", ignoreCase = true) || item.id.startsWith("mic_link_")
    val isAvatarFrame = item.category == "avatar_frame"
    val isRing = item.category == "ring" || item.category == "proposal_ring"
    val isEquippable = isMicEffect || isAvatarFrame || isMicLink
    val isIntimacySendable = isRing && !isMicLink
    val expiry = when {
        isMicEffect -> currentUser.micEffectExpiries[item.id] ?: 0L
        isMicLink -> currentUser.micLinkExpiries[item.id] ?: 0L
        isAvatarFrame -> currentUser.frameExpiries[item.id] ?: 0L
        else -> 0L
    }
    val isExpired = expiry > 0L && now > expiry
    val isSupremeAccess = currentUser.isFounder()
    val isOwned = isSupremeAccess || (isEquippable && !isExpired && (when {
        isMicEffect -> currentUser.ownedMicEffects.contains(item.id) || currentUser.activeMicEffect == item.id
        isMicLink -> currentUser.ownedMicLinks.contains(item.id) || currentUser.activeMicLinkId == item.id || currentUser.activeIntimacyBadge == item.id
        isAvatarFrame -> currentUser.ownedFrames.contains(item.id) || currentUser.activeFrame == item.id
        else -> false
    }))
    val isEquipped = when {
        isMicEffect -> currentUser.activeMicEffect == item.id
        isMicLink -> currentUser.activeMicLinkId == item.id || currentUser.activeIntimacyBadge == item.id
        isAvatarFrame -> currentUser.activeFrame == item.id
        else -> false
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
                        val thumb = if (item.drawableRes != 0) item.drawableRes else R.drawable.thumb_mic_effect_golden_amber_aura
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(item.primaryColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = thumb),
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
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
                        border = BorderStroke(1.2.dp, Color(0xFFFFD700)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isPurchasing) { onEquipToggle(item) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓ ", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text(
                                text = "Wearing",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                textAlign = TextAlign.Center
                            )
                        }
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
                            text = "Wear",
                            fontSize = 10.sp,
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
                isIntimacySendable -> {
                    Surface(
                        color = Color(0xFFE040FB),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isPurchasing) {
                                onSendIntimacyItem?.invoke(item)
                            }
                    ) {
                        Text(
                            text = "Send 💍",
                            fontSize = 9.5.sp,
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

                val ownedItems = remember(currentUser.ownedFrames, currentUser.ownedMicEffects, currentUser.ownedMicLinks, currentUser.activeMicEffect, currentUser.activeMicLinkId) {
                    LuxuryStoreCatalog.allItems.filter { item ->
                        val isML = item.category == "mic_link" || item.subCategory.contains("Mic Link", ignoreCase = true) || item.id.startsWith("mic_link_")
                        when {
                            item.category == "avatar_frame" -> currentUser.ownedFrames.contains(item.id) || currentUser.activeFrame == item.id
                            item.category == "mic_effect" -> currentUser.ownedMicEffects.contains(item.id) || currentUser.activeMicEffect == item.id
                            isML -> currentUser.ownedMicLinks.contains(item.id) || currentUser.activeMicLinkId == item.id || currentUser.activeIntimacyBadge == item.id
                            else -> false
                        }
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
                            val isMicLink = item.category == "mic_link" || item.subCategory.contains("Mic Link", ignoreCase = true) || item.id.startsWith("mic_link_")
                            val isAvatarFrame = item.category == "avatar_frame"
                            val isEquipped = when {
                                isMicEffect -> currentUser.activeMicEffect == item.id
                                isMicLink -> currentUser.activeMicLinkId == item.id || currentUser.activeIntimacyBadge == item.id
                                isAvatarFrame -> currentUser.activeFrame == item.id
                                else -> false
                            }
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
                                    val thumb = if (item.drawableRes != 0) {
                                        item.drawableRes
                                    } else if (isMicEffect) {
                                        R.drawable.thumb_mic_effect_golden_amber_aura
                                    } else {
                                        R.drawable.thumb_diamond_ring_proposal
                                    }
                                    Image(
                                        painter = painterResource(id = thumb),
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(if (isMicEffect) CircleShape else RoundedCornerShape(8.dp))
                                            .border(1.dp, if (isEquipped) Color(0xFFFFD700) else Color(0xFF382B59), if (isMicEffect) CircleShape else RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
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
                                                    val fieldToUpdate = when {
                                                        isMicEffect -> "activeMicEffect"
                                                        isMicLink -> "activeMicLinkId"
                                                        isAvatarFrame -> "activeFrame"
                                                        else -> ""
                                                    }
                                                    if (fieldToUpdate.isNotBlank()) {
                                                        val updates = mutableMapOf<String, Any>(fieldToUpdate to targetEquipId)
                                                        if (isMicLink) {
                                                            updates["activeIntimacyBadge"] = targetEquipId
                                                        }
                                                        db.collection("users").document(currentUser.uid)
                                                            .update(updates)
                                                        val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
                                                        when {
                                                            isMicEffect -> prefs.edit().putString("profile_active_mic_effect", targetEquipId).apply()
                                                            isMicLink -> prefs.edit().putString("profile_active_mic_link", targetEquipId).putString("profile_active_intimacy_badge", targetEquipId).apply()
                                                            isAvatarFrame -> prefs.edit().putString("profile_active_frame", targetEquipId).apply()
                                                        }
                                                        withContext(Dispatchers.Main) {
                                                            val updated = when {
                                                                isMicEffect -> currentUser.copy(activeMicEffect = targetEquipId)
                                                                isMicLink -> currentUser.copy(activeMicLinkId = targetEquipId, activeIntimacyBadge = targetEquipId)
                                                                isAvatarFrame -> currentUser.copy(activeFrame = targetEquipId)
                                                                else -> currentUser
                                                            }
                                                            onProfileUpdated(updated)
                                                            Toast.makeText(
                                                                context,
                                                                if (targetEquipId.isNotBlank()) "Equipped ${item.name}!" else "Unequipped!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
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


// ---------------------------------------------------------------------
// INTIMACY RECIPIENT SELECTOR DIALOG
// Real-time suggestions for Following, Close Intimacy Friends, and Search
// ---------------------------------------------------------------------

data class IntimacyRecipientItem(
    val uid: String,
    val name: String,
    val photo: String,
    val relationType: String = "Following" // "Following", "Close Friend"
)

@Composable
fun IntimacyRecipientSelectorDialog(
    ringItem: StoreItem,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    roomId: String = "",
    onDismiss: () -> Unit,
    onProposalSent: (recipientName: String, updatedBalance: Long) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val friendSuggestions = remember { mutableStateListOf<IntimacyRecipientItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    // Load following list & close intimacy friends
    DisposableEffect(currentUser.uid) {
        if (currentUser.uid.isBlank()) return@DisposableEffect onDispose {}

        val seenUids = mutableSetOf(currentUser.uid)
        val list = mutableListOf<IntimacyRecipientItem>()

        // 1. Fetch active intimacy bonds (Close Friends)
        val bondsReg = db.collection("intimacy_bonds")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    for (doc in snap.documents) {
                        val u1 = doc.getString("user1Id") ?: ""
                        val u2 = doc.getString("user2Id") ?: ""
                        val otherUid = if (u1 == currentUser.uid) u2 else if (u2 == currentUser.uid) u1 else ""
                        if (otherUid.isNotBlank() && !seenUids.contains(otherUid)) {
                            seenUids.add(otherUid)
                            val otherName = if (u1 == currentUser.uid) doc.getString("user2Name") ?: "Friend" else doc.getString("user1Name") ?: "Friend"
                            val otherPhoto = if (u1 == currentUser.uid) doc.getString("user2Photo") ?: "" else doc.getString("user1Photo") ?: ""
                            list.add(0, IntimacyRecipientItem(uid = otherUid, name = otherName, photo = otherPhoto, relationType = "Close Friend"))
                        }
                    }
                    friendSuggestions.clear()
                    friendSuggestions.addAll(list)
                }
            }

        // 2. Fetch Following list
        val followReg = db.collection("users").document(currentUser.uid)
            .collection("following")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    for (doc in snap.documents) {
                        val targetUid = doc.getString("hostUid") ?: doc.getString("uid") ?: doc.id
                        if (targetUid.isNotBlank() && !seenUids.contains(targetUid)) {
                            seenUids.add(targetUid)
                            val targetName = doc.getString("hostName") ?: doc.getString("name") ?: "Friend"
                            val targetPhoto = doc.getString("hostPhoto") ?: doc.getString("photo") ?: ""
                            list.add(IntimacyRecipientItem(uid = targetUid, name = targetName, photo = targetPhoto, relationType = "Following"))
                        }
                    }
                    friendSuggestions.clear()
                    friendSuggestions.addAll(list)
                }
                isLoading = false
            }

        onDispose {
            bondsReg.remove()
            followReg.remove()
        }
    }

    val filteredFriends = remember(searchQuery, friendSuggestions) {
        if (searchQuery.isBlank()) {
            friendSuggestions
        } else {
            friendSuggestions.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = { if (!isSending) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF140F24),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF4081)))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with ring preview
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
                                .background(Color(0xFF281C3F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ringItem.iconEmoji.ifBlank { "💍" }, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Send ${ringItem.name}",
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "💎 ${formatCompactDiamonds(ringItem.diamondPrice)} Diamonds",
                                color = Color(0xFFFF8DA1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search friends or following...", color = Color.Gray, fontSize = 11.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF1D1630),
                        unfocusedContainerColor = Color(0xFF1D1630)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Suggestions (Following & Close Friends)",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFFD700), modifier = Modifier.size(30.dp))
                    }
                } else if (filteredFriends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No users matched \"$searchQuery\"" else "No friends or followed users found yet.\nFollow members to send them intimacy rings!",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFriends) { friend ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF1F1735),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Color(0xFFFFD700), CircleShape)
                                    ) {
                                        AsyncImage(
                                            model = friend.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80" },
                                            contentDescription = friend.name,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = friend.name,
                                            color = Color.White,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (friend.relationType == "Close Friend") "💕 Close Friend" else "⭐ Following",
                                                color = if (friend.relationType == "Close Friend") Color(0xFFFF4081) else Color(0xFFFFD700),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Send Ring Button
                                    Button(
                                        onClick = {
                                            if (isSending) return@Button
                                            if (currentUser.bestFriends.contains(friend.uid)) {
                                                Toast.makeText(context, "You are already soulmates with this person", Toast.LENGTH_LONG).show()
                                                onDismiss()
                                                return@Button
                                            }
                                            if (currentUser.diamonds < ringItem.diamondPrice) {
                                                Toast.makeText(context, "Insufficient diamonds! Need ${ringItem.diamondPrice} 💎", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }

                                            isSending = true
                                            val senderRef = db.collection("users").document(currentUser.uid)
                                            val requestId = java.util.UUID.randomUUID().toString()
                                            val requestRef = db.collection("intimacy_requests").document(requestId)

                                            val bondType = when {
                                                ringItem.id.contains("bestie", true) -> "bestie"
                                                ringItem.id.contains("soulmate", true) -> "soulmate"
                                                ringItem.id.contains("eternal", true) -> "eternal"
                                                else -> "couple"
                                            }

                                            db.runTransaction { transaction ->
                                                val sortedKey = listOf(currentUser.uid, friend.uid).sorted().joinToString("_")
                                                val b1 = transaction.get(db.collection("intimacy_bonds").document(sortedKey))
                                                val b2 = transaction.get(db.collection("intimacy_bonds").document("bond_$sortedKey"))
                                                if (b1.exists() || b2.exists()) {
                                                    throw IllegalStateException("ALREADY_SOULMATES")
                                                }
                                                val senderSnap = transaction.get(senderRef)
                                                if (!senderSnap.exists()) throw IllegalStateException("Sender profile unavailable")
                                                val bal = senderSnap.getLong("diamonds") ?: 0L
                                                if (bal < ringItem.diamondPrice) return@runTransaction null
                                                val newBal = bal - ringItem.diamondPrice
                                                transaction.update(
                                                    senderRef,
                                                    mapOf(
                                                        "diamonds" to newBal,
                                                        "wealthXp" to FieldValue.increment(ringItem.diamondPrice * 10L)
                                                    )
                                                )
                                                transaction.set(
                                                    requestRef,
                                                    mapOf(
                                                        "id" to requestId,
                                                        "requestType" to "ring_intimacy",
                                                        "roomId" to roomId,
                                                        "senderUid" to currentUser.uid,
                                                        "senderId" to currentUser.uid,
                                                        "senderName" to currentUser.name,
                                                        "senderPhoto" to (currentUser.photo ?: ""),
                                                        "recipientUid" to friend.uid,
                                                        "receiverId" to friend.uid,
                                                        "recipientName" to friend.name,
                                                        "recipientPhoto" to friend.photo,
                                                        "giftId" to ringItem.id,
                                                        "giftName" to ringItem.name,
                                                        "giftEmoji" to "💍",
                                                        "bondType" to bondType,
                                                        "status" to "pending",
                                                        "timestamp" to System.currentTimeMillis()
                                                    )
                                                )
                                                val notifId = java.util.UUID.randomUUID().toString()
                                                transaction.set(
                                                    db.collection("notifications").document(notifId),
                                                    mapOf(
                                                        "id" to notifId,
                                                        "targetUserId" to friend.uid,
                                                        "senderId" to currentUser.uid,
                                                        "senderName" to currentUser.name,
                                                        "senderPhoto" to (currentUser.photo ?: ""),
                                                        "type" to "intimacy_request",
                                                        "title" to "💍 Intimacy Proposal Ring!",
                                                        "message" to "${currentUser.name} sent you a proposal with ${ringItem.name}!",
                                                        "timestamp" to System.currentTimeMillis(),
                                                        "isRead" to false,
                                                        "giftId" to ringItem.id,
                                                        "giftName" to ringItem.name
                                                    )
                                                )
                                                newBal
                                            }.addOnSuccessListener { updatedBal ->
                                                isSending = false
                                                if (updatedBal != null) {
                                                    Toast.makeText(context, "💍 Sent ${ringItem.name} to ${friend.name}!", Toast.LENGTH_SHORT).show()
                                                    onProposalSent(friend.name, updatedBal)
                                                    onDismiss()
                                                } else {
                                                    Toast.makeText(context, "Insufficient diamonds!", Toast.LENGTH_SHORT).show()
                                                }
                                            }.addOnFailureListener { err ->
                                                isSending = false
                                                if (err.message?.contains("ALREADY_SOULMATES") == true) {
                                                    Toast.makeText(context, "You are already soulmates with this person", Toast.LENGTH_LONG).show()
                                                    onDismiss()
                                                } else {
                                                    Toast.makeText(context, "Failed: ${err.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF4081))),
                                                    shape = RoundedCornerShape(12.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isSending) "..." else "Send 💍",
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
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
