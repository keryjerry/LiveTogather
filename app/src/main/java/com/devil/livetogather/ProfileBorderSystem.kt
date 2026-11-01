package com.devil.livetogather

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class ProfileBorderTheme(
    val id: String,
    val name: String,
    val hindiName: String,
    val category: String,
    val drawableRes: Int,
    val accentColor: Color
)

object ProfileBorderRegistry {
    val allBorders = listOf(
        ProfileBorderTheme("ice_butterfly", "Ice Crystal Butterfly", "बर्फ की उड़ती तितलियाँ", "Fantasy Ice", R.drawable.profile_border_ice_butterfly, Color(0xFF00E5FF)),
        ProfileBorderTheme("royal_rose", "Royal Crimson Rose", "शाही लाल गुलाब", "Royal Luxury", R.drawable.profile_border_royal_rose, Color(0xFFFF1744)),
        ProfileBorderTheme("sapphire_blue", "Imperial Sapphire Ribbon", "शाही नीलम रिबन", "Royal Luxury", R.drawable.profile_border_sapphire_blue, Color(0xFF2979FF)),
        ProfileBorderTheme("barfile_pahadi", "Barfile Pahadi Glacier", "बर्फीले पहाड़ & ऑरोरा", "Nature & Mountain", R.drawable.profile_border_barfile_pahadi, Color(0xFF00B0FF)),
        ProfileBorderTheme("blooming_flowers", "Cascading Floral Garden", "शाही फूलों की बहार", "Flowers", R.drawable.profile_border_blooming_flowers, Color(0xFFFF4081)),
        ProfileBorderTheme("heaven_angelic", "Heaven Angelic Wings", "स्वर्ग एंजेलिक पंख", "Celestial", R.drawable.profile_border_heaven_angelic, Color(0xFFFFD700)),
        ProfileBorderTheme("cosmic_gravity", "Cosmic Anti-Gravity", "कॉस्मिक एंटी-ग्रेविटी", "Sci-Fi & Cosmic", R.drawable.profile_border_cosmic_gravity, Color(0xFFB388FF)),
        ProfileBorderTheme("glacial_diamond", "Glacial Diamond Palace", "डायमंड आइस पैलेस", "Fantasy Ice", R.drawable.profile_border_glacial_diamond, Color(0xFF80D8FF)),
        ProfileBorderTheme("solar_phoenix", "Blazing Solar Phoenix", "अग्नि फीनिक्स", "Mythical", R.drawable.profile_border_solar_phoenix, Color(0xFFFF6D00)),
        ProfileBorderTheme("atlantis_oceanic", "Atlantis Mermaid Pearl", "जलपरी & गहरा सागर", "Oceanic", R.drawable.profile_border_atlantis_oceanic, Color(0xFF1DE9B6)),
        ProfileBorderTheme("golden_lotus", "Sacred Golden Lotus", "पवित्र स्वर्ण कमल", "Spiritual & Zen", R.drawable.profile_border_golden_lotus, Color(0xFFFFD54F)),
        ProfileBorderTheme("amethyst_purple", "Imperial Amethyst Gold", "शाही बैंगनी नीलम", "Royal Luxury", R.drawable.profile_border_amethyst_purple, Color(0xFF7C4DFF)),
        ProfileBorderTheme("solar_gold", "24K Solar Sovereign", "अखंड सूर्य सम्राट सोना", "Royal Luxury", R.drawable.profile_border_solar_gold, Color(0xFFFFC107)),
        ProfileBorderTheme("sakura_pink", "Sakura Rose Gold", "साकुरा चेरी ब्लॉसम", "Romance & Flowers", R.drawable.profile_border_sakura_pink, Color(0xFFFF80AB)),
        ProfileBorderTheme("emerald_green", "Imperial Emerald Gold", "शाही पन्ना रत्न", "Royal Luxury", R.drawable.profile_border_emerald_green, Color(0xFF00E676))
    )

    fun getDrawableForId(id: String): Int {
        return allBorders.find { it.id.equals(id, ignoreCase = true) }?.drawableRes
            ?: R.drawable.profile_border_ice_butterfly
    }

    fun getThemeForId(id: String): ProfileBorderTheme {
        return allBorders.find { it.id.equals(id, ignoreCase = true) }
            ?: allBorders.first()
    }
}

/**
 * Profile Border Frame Picker Dialog
 * Allows Founder and VIP users to preview and select from all 15 luxury animated border themes.
 */
@Composable
fun ProfileBorderSelectorDialog(
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onBorderSelected: (String) -> Unit
) {
    val currentBorderId = currentUser.getEffectiveProfileBorderFrame()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090714)),
            color = Color(0xFF090714)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Luxury Profile Frames",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "3D Animated Full-Screen Borders (15 Themes)",
                            fontSize = 12.sp,
                            color = Color(0xFFB0A5C8)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1B172B), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ProfileBorderRegistry.allBorders) { theme ->
                        val isSelected = currentBorderId.equals(theme.id, ignoreCase = true)

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF22173B) else Color(0xFF120E22)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) theme.accentColor else Color(0xFF2E2646)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onBorderSelected(theme.id)
                                    // Persist to user profile document in Firestore
                                    db.collection("users").document(currentUser.uid)
                                        .set(mapOf("activeProfileBorderFrame" to theme.id), SetOptions.merge())
                                    onDismiss()
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(9f / 16f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF05030A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = theme.drawableRes),
                                        contentDescription = theme.name,
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(24.dp)
                                                .background(theme.accentColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Active",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = theme.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = theme.hindiName,
                                    fontSize = 11.sp,
                                    color = theme.accentColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
