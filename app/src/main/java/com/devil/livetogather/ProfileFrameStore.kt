package com.devil.livetogather

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

/** Store settings for the "Profile Frame -> Monthly Card" category. */
object ProfileFrameStore {
    /** Price (diamonds, 30 days) per frame - tiered by artwork quality, 2.6 lakh up to 7.5 lakh. */
    private val PRICES = mapOf(
        "amethyst_purple" to 260_000L,
        "emerald_green" to 260_000L,
        "sapphire_blue" to 280_000L,
        "sakura_pink" to 300_000L,
        "solar_gold" to 320_000L,
        "royal_rose" to 350_000L,
        "golden_lotus" to 380_000L,
        "blooming_flowers" to 420_000L,
        "barfile_pahadi" to 450_000L,
        "heaven_angelic" to 500_000L,
        "atlantis_oceanic" to 550_000L,
        "glacial_diamond" to 600_000L,
        "cosmic_gravity" to 650_000L,
        "solar_phoenix" to 700_000L,
        "ice_butterfly" to 750_000L
    )
    fun priceOf(id: String): Long = PRICES[id] ?: 260_000L
    const val DURATION_DAYS = 30
    private const val DAY_MS = 24L * 60 * 60 * 1000
    fun durationMs() = DURATION_DAYS * DAY_MS
}

@Composable
fun ProfileFrameMonthlyCardSection(
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onProfileUpdated: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var busy by remember { mutableStateOf(false) }
    val founder = currentUser.isFounder()
    val worn = currentUser.getEffectiveProfileBorderFrame()
    val ref = db.collection("users").document(currentUser.uid)

    fun setWorn(id: String) {
        val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("profile_active_border_frame", id).apply()
        onProfileUpdated(currentUser.copy(activeProfileBorderFrame = id))
        ref.set(mapOf("activeProfileBorderFrame" to id), com.google.firebase.firestore.SetOptions.merge())
        Toast.makeText(context, if (id == "none") "Frame removed" else "Frame applied to your profile ✨", Toast.LENGTH_SHORT).show()
    }

    fun buy(theme: ProfileBorderTheme) {
        if (busy) return
        busy = true
        val price = ProfileFrameStore.priceOf(theme.id)
        db.runTransaction { tr ->
            val snap = tr.get(ref)
            val diamonds = snap.getLong("diamonds") ?: 0L
            if (!founder && diamonds < price) throw IllegalStateException("NO_DIAMONDS")
            val now = System.currentTimeMillis()
            val old = ((snap.get("profileFrameExpiries") as? Map<*, *>)?.get(theme.id) as? Number)?.toLong() ?: 0L
            val newExp = maxOf(now, old) + ProfileFrameStore.durationMs()
            val newDiamonds = if (founder) diamonds else diamonds - price
            tr.update(ref, mapOf(
                "diamonds" to newDiamonds,
                "profileFrameExpiries.${theme.id}" to newExp,
                "activeProfileBorderFrame" to theme.id
            ))
            Pair(newDiamonds, newExp)
        }.addOnSuccessListener { (newDiamonds, newExp) ->
            busy = false
            val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("profile_diamonds", newDiamonds)
                .putString("profile_active_border_frame", theme.id)
                .apply()
            onProfileUpdated(currentUser.copy(
                diamonds = newDiamonds,
                profileFrameExpiries = currentUser.profileFrameExpiries + (theme.id to newExp),
                activeProfileBorderFrame = theme.id
            ))
            Toast.makeText(context, "✨ ${theme.name} purchased for ${ProfileFrameStore.DURATION_DAYS} days & applied!", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { e ->
            busy = false
            val msg = if (e.message?.contains("NO_DIAMONDS") == true)
                "Insufficient Diamonds 💎 (Need ${String.format("%,d", price)})" else "Purchase failed: ${e.message}"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "🖼️ Profile Frame Monthly Card — ${ProfileFrameStore.DURATION_DAYS} days validity. Buy once, wear / unwear anytime until it expires.",
            color = Color(0xFFBFB3D9), fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(ProfileBorderRegistry.allBorders) { theme ->
                val owned = currentUser.ownsProfileFrame(theme.id)
                val isWorn = worn == theme.id
                val days = currentUser.getProfileFrameRemainingDays(theme.id)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14101F)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(if (isWorn) 2.dp else 1.dp, if (isWorn) theme.accentColor else Color(0xFF2E2648))
                ) {
                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.fillMaxWidth().aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(12.dp)).background(Color(0xFF0B0914))
                        ) {
                            LuxuryProfileBorderOverlay(frameId = theme.id, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(theme.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            if (owned && !founder) "$days days left" else "${ProfileFrameStore.DURATION_DAYS} days",
                            color = theme.accentColor, fontSize = 11.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        val label = when {
                            isWorn -> "Unwear"
                            owned -> "Wear"
                            else -> "💎 ${String.format("%,d", ProfileFrameStore.priceOf(theme.id))}"
                        }
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (owned) Brush.horizontalGradient(listOf(Color(0xFF7E22CE), Color(0xFF9333EA)))
                                    else Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6F00)))
                                )
                                .clickable(enabled = !busy) {
                                    when {
                                        isWorn -> setWorn("none")
                                        owned -> setWorn(theme.id)
                                        else -> buy(theme)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
