package com.devil.livetogather

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object AutomatedRewardManager {

    /**
     * Check and award room time milestones:
     * - 30 minutes (1800s): 2 Free Diamonds
     * - 60 minutes (3600s): 5 Free Diamonds
     */
    fun checkRoomTimeRewards(
        context: Context,
        profile: UserProfile,
        db: FirebaseFirestore,
        onUpdateProfile: (UserProfile) -> Unit
    ) {
        val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
        val uid = profile.uid
        if (uid.isBlank()) return

        val key30m = "claimed_room_reward_30m_$uid"
        if (profile.totalRoomTimeSeconds >= 1800L && !prefs.getBoolean(key30m, false)) {
            prefs.edit().putBoolean(key30m, true).apply()
            val newDiamonds = profile.diamonds + 2L
            db.collection("users").document(uid).update("diamonds", FieldValue.increment(2L))
            onUpdateProfile(profile.copy(diamonds = newDiamonds))
            Toast.makeText(context, "🎉 30 Mins Room Time Reward: +2 Diamonds Claimed! 💎", Toast.LENGTH_LONG).show()
        }

        val key60m = "claimed_room_reward_60m_$uid"
        if (profile.totalRoomTimeSeconds >= 3600L && !prefs.getBoolean(key60m, false)) {
            prefs.edit().putBoolean(key60m, true).apply()
            val newDiamonds = profile.diamonds + 5L
            db.collection("users").document(uid).update("diamonds", FieldValue.increment(5L))
            onUpdateProfile(profile.copy(diamonds = newDiamonds))
            Toast.makeText(context, "🎉 1 Hour Room Time Reward: +5 Diamonds Claimed! 💎", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Check and award Diamond Spender milestone:
     * - 4+ diamond spends: 24h random 3D mic effect
     */
    fun checkDiamondSpendMilestones(
        context: Context,
        profile: UserProfile,
        db: FirebaseFirestore,
        onUpdateProfile: (UserProfile) -> Unit
    ) {
        val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
        val uid = profile.uid
        if (uid.isBlank()) return

        val keySpend4 = "claimed_spend_reward_4_$uid"
        if (profile.diamondsSpentCount >= 4 && !prefs.getBoolean(keySpend4, false)) {
            prefs.edit().putBoolean(keySpend4, true).apply()

            val micEffectPool = listOf(
                "mic_effect_golden_amber_aura",
                "mic_effect_solar_particle_ring",
                "mic_effect_ruby_energy_flame",
                "mic_effect_cyber_neon_electric",
                "mic_effect_diamond_crystal_vortex",
                "mic_effect_sovereign_gold_wings",
                "mic_effect_emerald_celestial_wave"
            )
            val randomEffect = micEffectPool.random()
            val now = System.currentTimeMillis()
            val expiry = now + (24L * 60L * 60L * 1000L) // 24 hours

            val newOwnedMics = profile.ownedMicEffects.toMutableList().apply {
                if (!contains(randomEffect)) add(randomEffect)
            }
            val newExpiries = profile.micEffectExpiries.toMutableMap().apply {
                put(randomEffect, expiry)
            }

            db.collection("users").document(uid).update(
                mapOf(
                    "ownedMicEffects" to FieldValue.arrayUnion(randomEffect),
                    "activeMicEffect" to randomEffect,
                    "micEffectExpiries" to newExpiries
                )
            )

            prefs.edit().putString("profile_active_mic_effect", randomEffect).apply()

            val updated = profile.copy(
                ownedMicEffects = newOwnedMics,
                activeMicEffect = randomEffect,
                micEffectExpiries = newExpiries
            )
            onUpdateProfile(updated)
            Toast.makeText(context, "🎁 Diamond Spender Reward: 24h 3D Mic Effect Unlocked & Equipped! 🎙️✨", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Check and award recharge milestones:
     * - 3 recharges: 24h starter frame
     * - 5 recharges + 2h room time: 10-day starter frame (VIP frames excluded)
     */
    fun checkRechargeMilestones(
        context: Context,
        profile: UserProfile,
        db: FirebaseFirestore,
        onUpdateProfile: (UserProfile) -> Unit
    ) {
        val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
        val uid = profile.uid
        if (uid.isBlank()) return

        val keyRecharge3 = "claimed_recharge_reward_3_$uid"
        if (profile.rechargeCount >= 3 && !prefs.getBoolean(keyRecharge3, false)) {
            prefs.edit().putBoolean(keyRecharge3, true).apply()
            val starterFrame = "frame_rose_gold_romance"
            val expiry = System.currentTimeMillis() + (24L * 60L * 60L * 1000L) // 24 hours
            val newOwned = profile.ownedFrames.toMutableList().apply { if (!contains(starterFrame)) add(starterFrame) }
            val newExpiries = profile.frameExpiries.toMutableMap().apply { put(starterFrame, expiry) }

            db.collection("users").document(uid).update(
                mapOf(
                    "ownedFrames" to FieldValue.arrayUnion(starterFrame),
                    "activeFrame" to starterFrame,
                    "frameExpiries" to newExpiries
                )
            )
            prefs.edit().putString("profile_active_frame", starterFrame).apply()
            onUpdateProfile(profile.copy(ownedFrames = newOwned, activeFrame = starterFrame, frameExpiries = newExpiries))
            Toast.makeText(context, "🎁 3-Recharge Milestone: Starter Frame Unlocked for 24 Hours! 🖼️✨", Toast.LENGTH_LONG).show()
        }

        val keyRecharge5 = "claimed_recharge_reward_5_room_2h_$uid"
        if (profile.rechargeCount >= 5 && profile.totalRoomTimeSeconds >= 7200L && !prefs.getBoolean(keyRecharge5, false)) {
            prefs.edit().putBoolean(keyRecharge5, true).apply()
            val starterFrame = "frame_cyber_neon"
            val expiry = System.currentTimeMillis() + (10L * 24L * 60L * 60L * 1000L) // 10 days
            val newOwned = profile.ownedFrames.toMutableList().apply { if (!contains(starterFrame)) add(starterFrame) }
            val newExpiries = profile.frameExpiries.toMutableMap().apply { put(starterFrame, expiry) }

            db.collection("users").document(uid).update(
                mapOf(
                    "ownedFrames" to FieldValue.arrayUnion(starterFrame),
                    "activeFrame" to starterFrame,
                    "frameExpiries" to newExpiries
                )
            )
            prefs.edit().putString("profile_active_frame", starterFrame).apply()
            onUpdateProfile(profile.copy(ownedFrames = newOwned, activeFrame = starterFrame, frameExpiries = newExpiries))
            Toast.makeText(context, "🎁 Loyal Member Milestone: Starter Frame Unlocked for 10 Days! 🌟🖼️", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun LevelUpCelebrationDialog(
    newLevel: Int,
    onClaimDiamonds: () -> Unit
) {
    Dialog(
        onDismissRequest = onClaimDiamonds,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF161224),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF5E62)))),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(76.dp).background(
                        Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.35f), Color.Transparent)),
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌟", fontSize = 42.sp)
                }

                Text(
                    text = "🎉 Level Up Milestone!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Congratulations! You reached Level $newLevel! Claim your milestone reward:",
                    fontSize = 13.sp,
                    color = Color(0xFFCFCCE3),
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = Color(0xFF231C38),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🎁 +10 Free Diamonds 💎", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }

                Button(
                    onClick = onClaimDiamonds,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5E62)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Claim Reward 💎", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun RoomRechargePaywallModal(
    onDismiss: () -> Unit,
    onRecharge: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF161224),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF5E62), Color(0xFFFF9966)))),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(76.dp).background(
                        Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.35f), Color.Transparent)),
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏳", fontSize = 42.sp)
                }

                Text(
                    text = "Free 1-Hour Guest Access Expired!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "You have completed your 1-hour free access to hangout rooms. To continue chatting, listening, and joining rooms, please recharge the minimum unlock package.",
                    fontSize = 13.sp,
                    color = Color(0xFFCFCCE3),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Surface(
                    color = Color(0xFF231C38),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Minimum Unlock Pack", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("20,000 Diamonds 💎", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                        Text("₹59", color = Color(0xFF00E5FF), fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }

                Button(
                    onClick = onRecharge,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5E62)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Recharge ₹59 to Unlock Rooms 💎", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }
    }
}
