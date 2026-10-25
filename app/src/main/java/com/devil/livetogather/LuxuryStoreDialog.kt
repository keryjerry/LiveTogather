package com.devil.livetogather

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ---------------------------------------------------------------------
// LUXURY STORE DIALOG (MATCHING REFERENCE IMAGES 1, 2, 3, 4, 5)
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

    // When category changes, auto-select its first sub-category
    LaunchedEffect(selectedCategory) {
        selectedSubCategory = when (selectedCategory) {
            StoreCategory.INTIMACY -> "Ring"
            StoreCategory.AVATAR -> "Avatar Frame"
            StoreCategory.MIC_EFFECTS -> "Sound Wave"
            StoreCategory.ENTRANCE -> "VIP Vehicles"
            StoreCategory.CHAT_FRAME -> "Chat Bubble"
            StoreCategory.PROFILE_THEME -> "Themes"
        }
    }

    // Filter items according to category and sub-category
    val currentItems = remember(selectedCategory, selectedSubCategory) {
        when (selectedCategory) {
            StoreCategory.INTIMACY -> {
                when (selectedSubCategory) {
                    "Ring" -> LuxuryStoreCatalog.rings
                    "Mic Link" -> LuxuryStoreCatalog.micLinks
                    else -> LuxuryStoreCatalog.rings + LuxuryStoreCatalog.micLinks
                }
            }
            StoreCategory.AVATAR -> LuxuryStoreCatalog.avatarFrames
            StoreCategory.MIC_EFFECTS -> LuxuryStoreCatalog.micEffects
            StoreCategory.ENTRANCE -> LuxuryStoreCatalog.entrances
            else -> LuxuryStoreCatalog.allItems.filter { it.category == selectedCategory.id }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F6FE)),
            color = Color(0xFFF9F6FE)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {

                // 1. TOP HEADER (Back, "Store", Voucher & Backpack Icons)
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
                            .size(36.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFE2D6F6), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1B1433),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Store",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B1433)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Discount Coupon icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2D6F6), CircleShape)
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2D6F6), CircleShape)
                                .clickable { showInventoryDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎒", fontSize = 16.sp)
                        }
                    }
                }

                // 2. REAL-TIME BALANCE BAR (Diamonds, Coins, VIP Tokens)
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFE8DEF8)),
                    shadowElevation = 2.dp,
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
                            color = Color(0xFF7C6D93)
                        )

                        // Diamonds
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3E8FF))
                                .clickable { onOpenTopUp() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("💎", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%,d", currentUser.diamonds),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF7E22CE)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("+", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF9333EA))
                        }

                        // Coins
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7))
                                .clickable { onOpenTopUp() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("🪙", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%,d", currentUser.coins),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB45309)
                            )
                        }

                        // Crown Points
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "VIP ${currentUser.getVipLevel()}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
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
                                .background(if (isSelected) Color(0xFFEDE2FC) else Color.White)
                                .border(
                                    1.2.dp,
                                    if (isSelected) Color(0xFF9333EA) else Color(0xFFE8DEF8),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(cat.icon, fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = cat.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF7E22CE) else Color(0xFF5D4E75)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. SUB-CATEGORY TABS
                val subTabs = when (selectedCategory) {
                    StoreCategory.INTIMACY -> listOf("Ring", "Jewelry", "Love Zone", "Mic Link", "Special")
                    StoreCategory.AVATAR -> listOf("Avatar Frame", "CP Avatar Frame", "Dynamic Avatar")
                    StoreCategory.MIC_EFFECTS -> listOf("Sound Wave", "Mic Animation", "Mic Color Name")
                    StoreCategory.ENTRANCE -> listOf("VIP Vehicles", "Aircrafts", "Banners")
                    StoreCategory.CHAT_FRAME -> listOf("Chat Bubble", "VIP Glow")
                    StoreCategory.PROFILE_THEME -> listOf("Themes", "Borders")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                color = if (isSubSelected) Color(0xFF1B1433) else Color(0xFF8C7D9E)
                            )
                            if (isSubSelected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .width(24.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF7E22CE))
                                )
                            }
                        }
                    }
                }

                Divider(color = Color(0xFFE8DEF8), thickness = 0.8.dp)

                // 5. STORE ITEMS GRID (MATCHING REFERENCE IMAGES)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentItems) { item ->
                        StoreItemCard(
                            item = item,
                            currentUser = currentUser,
                            isPurchasing = isPurchasing,
                            onEquipToggle = { targetItem ->
                                if (isPurchasing) return@StoreItemCard
                                isPurchasing = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val targetFrame = if (currentUser.activeFrame == targetItem.id) "" else targetItem.id
                                        db.collection("users").document(currentUser.uid).update("activeFrame", targetFrame)
                                        val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
                                        prefs.edit().putString("profile_active_frame", targetFrame).apply()
                                        withContext(Dispatchers.Main) {
                                            val updatedUser = currentUser.copy(activeFrame = targetFrame)
                                            onProfileUpdated(updatedUser)
                                            Toast.makeText(
                                                context,
                                                if (targetFrame.isNotBlank()) "👑 Equipped ${targetItem.name}!" else "Frame removed",
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
                                        val newDiamonds = currentUser.diamonds - targetItem.diamondPrice
                                        val updates = mutableMapOf<String, Any>(
                                            "diamonds" to newDiamonds,
                                            "ownedFrames" to FieldValue.arrayUnion(targetItem.id)
                                        )

                                        // If it's a frame, also auto-equip it
                                        if (targetItem.category == "avatar_frame") {
                                            updates["activeFrame"] = targetItem.id
                                        }

                                        db.collection("users").document(currentUser.uid)
                                            .update(updates)

                                        val newActive = if (targetItem.category == "avatar_frame") targetItem.id else currentUser.activeFrame
                                        val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
                                        prefs.edit()
                                            .putLong("profile_diamonds", newDiamonds)
                                            .putStringSet("profile_owned_frames", (currentUser.ownedFrames + targetItem.id).toSet())
                                            .putString("profile_active_frame", newActive)
                                            .apply()

                                        withContext(Dispatchers.Main) {
                                            val updatedUser = currentUser.copy(
                                                diamonds = newDiamonds,
                                                activeFrame = newActive,
                                                ownedFrames = currentUser.ownedFrames + targetItem.id
                                            )
                                            onProfileUpdated(updatedUser)
                                            Toast.makeText(
                                                context,
                                                "🎉 Purchased ${targetItem.name}! Successfully equipped.",
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
}

// ---------------------------------------------------------------------
// STORE ITEM CARD (WITH TIER BADGE, DURATION, PRICE & PURCHASE PILL)
// ---------------------------------------------------------------------

@Composable
fun StoreItemCard(
    item: StoreItem,
    currentUser: UserProfile,
    isPurchasing: Boolean,
    onEquipToggle: (StoreItem) -> Unit = {},
    onPurchase: (StoreItem) -> Unit
) {
    val isOwned = currentUser.ownedFrames.contains(item.id) || currentUser.activeFrame == item.id
    val isEquipped = currentUser.activeFrame == item.id

    val infiniteTransition = rememberInfiniteTransition(label = "item_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isEquipped) Color(0xFF9333EA) else Color(0xFFE8DEF8)),
        shadowElevation = if (isEquipped) 4.dp else 1.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rank Tier Badge (S, A, B, C, D, E, F)
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
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
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.rankTier,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Visual Icon / 3D Graphics
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(item.primaryColor).copy(alpha = 0.25f),
                                Color(item.secondaryColor).copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (item.category) {
                    "avatar_frame" -> {
                        Image(
                            painter = painterResource(id = getAvatarFrameDrawable(item.id)),
                            contentDescription = item.name,
                            modifier = Modifier.size(52.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    "mic_link" -> {
                        MicLinkPreviewVisual(
                            item = item,
                            size = 52.dp
                        )
                    }
                    "ring" -> {
                        Image(
                            painter = painterResource(id = R.drawable.gift_diamond_ring),
                            contentDescription = item.name,
                            modifier = Modifier.size(44.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        Text(
                            text = item.iconEmoji,
                            fontSize = 26.sp,
                            modifier = Modifier.animateContentSize()
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
                color = Color(0xFF1B1433),
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
                    color = Color(0xFF7E22CE)
                )
            }

            Text(
                text = "${item.durationDays} Days",
                fontSize = 9.sp,
                color = Color(0xFF9E8FB0),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Action Button
            when {
                isEquipped -> {
                    Surface(
                        color = Color(0xFFEDE2FC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF9333EA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isPurchasing) { onEquipToggle(item) }
                    ) {
                        Text(
                            text = "Equipped (Tap to Remove)",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7E22CE),
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
                            text = "Equip",
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
                        color = Color(0xFFECEFF1),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Activity",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78909C),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                item.isNotForSale -> {
                    Surface(
                        color = Color(0xFFECEFF1),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Not for sale",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78909C),
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
                    // Standard Purchase Button (Orange/Gold Pill)
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
// BACKPACK / INVENTORY DIALOG (VIEW & EQUIP OWNED ITEMS)
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
            color = Color.White,
            border = BorderStroke(1.2.dp, Color(0xFFE8DEF8)),
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
                        color = Color(0xFF1B1433)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val ownedItems = remember(currentUser.ownedFrames) {
                    LuxuryStoreCatalog.allItems.filter { currentUser.ownedFrames.contains(it.id) }
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
                            val isEquipped = currentUser.activeFrame == item.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isEquipped) Color(0xFFEDE2FC) else Color(0xFFF9F6FE),
                                border = BorderStroke(1.dp, if (isEquipped) Color(0xFF9333EA) else Color(0xFFE8DEF8)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(item.iconEmoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B1433),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            if (isEquipping) return@Button
                                            isEquipping = true
                                            coroutineScope.launch(Dispatchers.IO) {
                                                try {
                                                    val newFrame = if (isEquipped) "" else item.id
                                                    db.collection("users").document(currentUser.uid)
                                                        .update("activeFrame", newFrame)
                                                    withContext(Dispatchers.Main) {
                                                        onProfileUpdated(currentUser.copy(activeFrame = newFrame))
                                                        Toast.makeText(
                                                            context,
                                                            if (newFrame.isNotBlank()) "Equipped ${item.name}!" else "Unequipped!",
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
                                            containerColor = if (isEquipped) Color(0xFFE0E0E0) else Color(0xFF9333EA)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = if (isEquipped) "Unequip" else "Equip",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEquipped) Color.DarkGray else Color.White
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
