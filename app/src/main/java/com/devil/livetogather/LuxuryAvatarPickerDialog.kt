package com.devil.livetogather

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

/**
 * High-Definition Studio Avatar Picker Dialog.
 *
 * Implements the Ahchat-inspired Avatar Privilege System:
 * - 20 Realistic HD Studio Portraits (10 Female, 10 Male, NO cartoons).
 * - Categorized by VIP Royalty, Studio Aesthetic, Urban Glamour, Executive & Charisma.
 * - Privilege Gate: Personal phone gallery upload is unlocked at Level 2 or VIP status.
 * - Standard Level 1 users select from luxury HD studio presets to preserve top-tier aesthetic.
 */
@Composable
fun LuxuryAvatarPickerDialog(
    currentPhotoUrl: String,
    userLevel: Int,
    isVip: Boolean,
    onAvatarSelected: (LuxuryAvatarPreset) -> Unit,
    onRequestCustomUpload: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedGender by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showPrivilegeInfoDialog by remember { mutableStateOf(false) }

    val canUpload = remember(userLevel, isVip) {
        LuxuryAvatarCatalog.canUploadCustomAvatar(userLevel, isVip)
    }

    val filteredPresets = remember(selectedGender, selectedCategory) {
        LuxuryAvatarCatalog.presets.filter { preset ->
            val matchGender = when (selectedGender) {
                "Women" -> preset.gender.equals("Female", ignoreCase = true)
                "Men" -> preset.gender.equals("Male", ignoreCase = true)
                else -> true
            }
            val matchCategory = if (selectedCategory == "All") true else preset.category.equals(selectedCategory, ignoreCase = true)
            matchGender && matchCategory
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            color = Color(0xFF0F0F1A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color(0xFFFFD700).copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👑", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Luxury Studio Avatars",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "HD Realistic Portraits • 0% Cartoons",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Gender Toggle Filter (All / Women / Men)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E2F))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("All", "Women", "Men").forEach { genderOption ->
                        val isSelected = selectedGender == genderOption
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(
                                        listOf(Color(0xFFFF3366), Color(0xFFFF7700))
                                    ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { selectedGender = genderOption }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (genderOption) {
                                    "Women" -> "👩 Women (10)"
                                    "Men" -> "👨 Men (10)"
                                    else -> "🌟 All (20)"
                                },
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips Scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(LuxuryAvatarCatalog.categories) { cat ->
                        val isCatSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCatSelected) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFF1E1E2F),
                            border = BorderStroke(
                                1.dp,
                                if (isCatSelected) Color(0xFFFFD700) else Color(0xFF2A2A3E)
                            ),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isCatSelected) Color(0xFFFFD700) else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Gallery Upload Privilege Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (canUpload) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFF2A1B38),
                    border = BorderStroke(
                        1.dp,
                        if (canUpload) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFFF80AB).copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (canUpload) {
                                onRequestCustomUpload()
                            } else {
                                showPrivilegeInfoDialog = true
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (canUpload) Color(0xFF10B981) else Color(0xFFFF3366),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (canUpload) Icons.Default.CameraAlt else Icons.Default.Lock,
                                        contentDescription = "Upload",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (canUpload) "Upload Personal Photo from Gallery" else "Custom Gallery Upload (Locked)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (canUpload) "Unlocked for Level 2 & VIP members" else "Unlocks at Level 2 or VIP • Preserves Luxury Look",
                                    color = if (canUpload) Color(0xFF34D399) else Color(0xFFFF80AB),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (canUpload) Color(0xFF10B981) else Color(0xFFFFD700).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (canUpload) "UPLOAD" else "LV.2 / VIP",
                                color = if (canUpload) Color.White else Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar Grid (3-columns, High-Definition, Realistic)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredPresets, key = { it.id }) { preset ->
                        val isCurrent = currentPhotoUrl == preset.imageUrl

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF181828))
                                .border(
                                    width = if (isCurrent) 2.5.dp else 1.dp,
                                    color = if (isCurrent) Color(0xFFFFD700) else Color(0xFF26263B),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    onAvatarSelected(preset)
                                    Toast.makeText(
                                        context,
                                        "Applied: ${preset.name} ✨",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(6.dp)
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                AsyncImage(
                                    model = preset.imageUrl,
                                    contentDescription = preset.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )

                                // Category Badge
                                Surface(
                                    shape = RoundedCornerShape(bottomStart = 8.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    modifier = Modifier.align(Alignment.BottomStart)
                                ) {
                                    Text(
                                        text = preset.badge,
                                        color = Color(0xFFFFD700),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }

                                if (isCurrent) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = 2.dp, y = (-2).dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.Black,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = preset.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = preset.category,
                                color = Color.Gray,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // Privilege Information Dialog (Explaining Level 2 & VIP unlocks)
    if (showPrivilegeInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPrivilegeInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔒", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Level 2 Privilege Required",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "To guarantee 100% realistic, luxury presentation across all voice lounges and chat rooms, personal gallery uploads are exclusively unlocked for Level 2+ members and VIPs.",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "⭐ How to Unlock:\n• Spend time chatting or host a room (Levels up in 90% of normal time!)\n• Or activate VIP to unlock instant custom uploads + 3D laurel frames.",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "In the meantime, feel free to choose any of our 20 Luxury HD Studio Avatars!",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showPrivilegeInfoDialog = false }
                ) {
                    Text("Got It! Pick a Studio Avatar", color = Color(0xFFFF3366), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
