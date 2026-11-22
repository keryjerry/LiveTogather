package com.devil.livetogather

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Modular Automated Level-Up, Milestone & Recharge Reward Assistant Service.
 *
 * Monitors user Wealth Level, Charm Level, Recharge Count, and Live Room Activity.
 * Automatically dispatches official LiveTogather Assistant messages into the user's Inbox
 * (`users/{uid}/notifications`) containing equippable time-limited 3D avatar frames or mic effects
 * (24 hours / 3 days) without touching billing or core room logic.
 */
object ProgressionAutomationService {

    private const val TAG = "ProgressionAutomation"
    private const val PREFS_NAME = "livetogather_progression_automation"

    data class MilestoneRewardRule(
        val key: String,
        val title: String,
        val description: String,
        val rewardItemId: String,
        val rewardItemType: String, // "avatar_frame" or "mic_effect"
        val rewardItemName: String,
        val durationHours: Int,     // 24 (24h) or 72 (3 Days)
        val condition: (UserProfile) -> Boolean
    )

    private val milestoneRules: List<MilestoneRewardRule> = listOf(
        // Approved Phase 3 Wealth Level Milestones (Exponential Bigo-Style Progression)
        MilestoneRewardRule(
            key = "wealth_lv_10",
            title = "🛡️ Wealth Level 10 Unlocked!",
            description = "Congratulations! You reached Wealth Lv.10 (Bronze Vanguard). LiveTogather Official Assistant gifted you a free 24-Hour Aurora Lights 3D Frame!",
            rewardItemId = "frame_aurora_lights",
            rewardItemType = "avatar_frame",
            rewardItemName = "Aurora Lights 3D Frame",
            durationHours = 24,
            condition = { it.getEffectiveWealthLevel() >= 10 }
        ),
        MilestoneRewardRule(
            key = "wealth_lv_20",
            title = "⚔️ Wealth Level 20 Silver Sentinel Milestone!",
            description = "Amazing milestone! You unlocked a free 3-Day Golden Amber Aura 3D Mic Effect. Tap Equip Now to shine on stage!",
            rewardItemId = "mic_effect_golden_amber_aura",
            rewardItemType = "mic_effect",
            rewardItemName = "Golden Amber Aura Mic Effect",
            durationHours = 72,
            condition = { it.getEffectiveWealthLevel() >= 20 }
        ),
        MilestoneRewardRule(
            key = "wealth_lv_30",
            title = "👑 Wealth Level 30 Gold Monarch Achieved!",
            description = "Welcome to Gold Monarch! LiveTogather Official Assistant awarded you a free 3-Day Phoenix Fire 3D Frame!",
            rewardItemId = "frame_phoenix_fire",
            rewardItemType = "avatar_frame",
            rewardItemName = "Phoenix Fire 3D Frame",
            durationHours = 72,
            condition = { it.getEffectiveWealthLevel() >= 30 }
        ),
        MilestoneRewardRule(
            key = "wealth_lv_50",
            title = "💠 Wealth Level 50 Platinum Archon Prestige!",
            description = "Elite Platinum Archon unlocked! Claim & equip your free 3-Day Cyber Neon Electric 3D Mic Effect right now!",
            rewardItemId = "mic_effect_cyber_neon_electric",
            rewardItemType = "mic_effect",
            rewardItemName = "Cyber Neon Electric Mic Effect",
            durationHours = 72,
            condition = { it.getEffectiveWealthLevel() >= 50 }
        ),
        MilestoneRewardRule(
            key = "wealth_lv_50_frame",
            title = "🐉 Wealth Level 50 Bonus 3D Frame!",
            description = "Platinum Archon Bonus! Your free 3-Day Crimson Dragon 3D Profile Frame is ready to equip!",
            rewardItemId = "frame_crimson_dragon",
            rewardItemType = "avatar_frame",
            rewardItemName = "Crimson Dragon 3D Frame",
            durationHours = 72,
            condition = { it.getEffectiveWealthLevel() >= 50 }
        ),
        MilestoneRewardRule(
            key = "wealth_lv_70",
            title = "💎 Wealth Level 70 Diamond Celestial!",
            description = "Diamond Celestial tier reached! Claim & equip your free 3-Day Diamond Crystal Vortex 3D Mic Effect!",
            rewardItemId = "mic_effect_diamond_crystal_vortex",
            rewardItemType = "mic_effect",
            rewardItemName = "Diamond Crystal Vortex Mic Effect",
            durationHours = 72,
            condition = { it.getEffectiveWealthLevel() >= 70 }
        ),
        MilestoneRewardRule(
            key = "wealth_lv_85",
            title = "🔥 Wealth Level 85 Master Phoenix!",
            description = "Mythic Master Phoenix unlocked! Claim & equip your free 3-Day Sovereign Gold Wings 3D Mic Effect!",
            rewardItemId = "mic_effect_sovereign_gold_wings",
            rewardItemType = "mic_effect",
            rewardItemName = "Sovereign Gold Wings Mic Effect",
            durationHours = 72,
            condition = { it.getEffectiveWealthLevel() >= 85 }
        ),
        MilestoneRewardRule(
            key = "wealth_lv_100",
            title = "👑 Wealth Level 100 Sovereign Emperor Legend!",
            description = "Ultimate platform prestige! Claim & equip your exclusive 7-Day Ruby Emperor 3D Profile Frame!",
            rewardItemId = "frame_ruby_emperor",
            rewardItemType = "avatar_frame",
            rewardItemName = "Ruby Emperor 3D Frame",
            durationHours = 168,
            condition = { it.getEffectiveWealthLevel() >= 100 }
        ),

        // Charm Level Milestones
        MilestoneRewardRule(
            key = "charm_lv_10",
            title = "🌹 Charm Level 10 Star Unlocked!",
            description = "Everyone loves your vibe! LiveTogather Official Assistant sent you a free 24-Hour Rose Seraph 3D Frame!",
            rewardItemId = "frame_rose_seraph",
            rewardItemType = "avatar_frame",
            rewardItemName = "Rose Seraph 3D Frame",
            durationHours = 24,
            condition = { it.getEffectiveCharmLevel() >= 10 }
        ),
        MilestoneRewardRule(
            key = "charm_lv_20",
            title = "✨ Charm Level 20 Icon Reward!",
            description = "You're glowing! Enjoy a free 3-Day Solar Particle Ring 3D Mic Effect from LiveTogather Official Assistant!",
            rewardItemId = "mic_effect_solar_particle_ring",
            rewardItemType = "mic_effect",
            rewardItemName = "Solar Particle Ring Mic Effect",
            durationHours = 72,
            condition = { it.getEffectiveCharmLevel() >= 20 }
        ),
        MilestoneRewardRule(
            key = "charm_lv_50",
            title = "👼 Charm Level 50 Superstar Reward!",
            description = "Superstar milestone reached! Claim & equip your free 3-Day Angel Wings 3D Profile Frame!",
            rewardItemId = "frame_angel_wings",
            rewardItemType = "avatar_frame",
            rewardItemName = "Angel Wings 3D Frame",
            durationHours = 72,
            condition = { it.getEffectiveCharmLevel() >= 50 }
        ),

        // Recharge Milestones
        MilestoneRewardRule(
            key = "recharge_first",
            title = "🌙 First Recharge Bonus Gift!",
            description = "Thank you for your recharge! LiveTogather Official Assistant gifted you a free 24-Hour Moon Celestial 3D Frame. Tap to equip!",
            rewardItemId = "frame_moon_celestial",
            rewardItemType = "avatar_frame",
            rewardItemName = "Moon Celestial 3D Frame",
            durationHours = 24,
            condition = { it.rechargeCount >= 1 || it.hasRecharged }
        ),
        MilestoneRewardRule(
            key = "recharge_3",
            title = "🔥 3-Recharge VIP Appreciation!",
            description = "Loyalty rewarded! Claim & equip your free 3-Day Ruby Energy Flame 3D Mic Effect right now!",
            rewardItemId = "mic_effect_ruby_energy_flame",
            rewardItemType = "mic_effect",
            rewardItemName = "Ruby Energy Flame Mic Effect",
            durationHours = 72,
            condition = { it.rechargeCount >= 3 }
        ),
        MilestoneRewardRule(
            key = "recharge_5",
            title = "⚡ 5-Recharge Royal Vault Bonus!",
            description = "Thank you for being a VIP supporter! Claim your free 3-Day Cyber Neon 3D Frame immediately!",
            rewardItemId = "frame_cyber_neon",
            rewardItemType = "avatar_frame",
            rewardItemName = "Cyber Neon 3D Frame",
            durationHours = 72,
            condition = { it.rechargeCount >= 5 }
        ),

        // Active Room Presence Milestone
        MilestoneRewardRule(
            key = "active_room_30m",
            title = "🎙️ Active Host & Listener Reward!",
            description = "You spent 30+ minutes live in rooms! Enjoy a free 24-Hour Emerald Celestial Wave 3D Mic Effect!",
            rewardItemId = "mic_effect_emerald_celestial_wave",
            rewardItemType = "mic_effect",
            rewardItemName = "Emerald Celestial Wave Mic Effect",
            durationHours = 24,
            condition = { it.totalRoomTimeSeconds >= 1800L }
        ),

        // Official VIP 1–5 Service Milestone Notifications (Replaces deprecated Dating Passes)
        MilestoneRewardRule(
            key = "vip_service_tier_1",
            title = "👑 VIP 1 Silver Archangel Activated!",
            description = "Welcome to LiveTogather VIP 1! Your Silver Archangel VIP privileges and bonus 3-Day Aurora Lights 3D Frame are ready!",
            rewardItemId = "frame_aurora_lights",
            rewardItemType = "avatar_frame",
            rewardItemName = "Aurora Lights 3D Frame",
            durationHours = 72,
            condition = { it.getVipLevel() >= 1 }
        ),
        MilestoneRewardRule(
            key = "vip_service_tier_3",
            title = "👑 VIP 3 Imperial Obsidian Activated!",
            description = "Welcome to VIP 3! Enjoy your Imperial Obsidian privileges plus a bonus 3-Day Sovereign Gold Wings Mic Effect!",
            rewardItemId = "mic_effect_sovereign_gold_wings",
            rewardItemType = "mic_effect",
            rewardItemName = "Sovereign Gold Wings Mic Effect",
            durationHours = 72,
            condition = { it.getVipLevel() >= 3 }
        ),
        MilestoneRewardRule(
            key = "vip_service_tier_5",
            title = "👑 VIP 5 Celestial Sapphire Sovereign!",
            description = "Maximum VIP 5 Prestige unlocked! Your Celestial Sapphire suite and bonus 3-Day Ruby Emperor 3D Frame are active!",
            rewardItemId = "frame_ruby_emperor",
            rewardItemType = "avatar_frame",
            rewardItemName = "Ruby Emperor 3D Frame",
            durationHours = 72,
            condition = { it.getVipLevel() >= 5 }
        )
    )

    /**
     * Evaluates all automated level-up, recharge, and activity milestones for [user].
     * Uses deterministic Firestore document IDs (`auto_milestone_<key>`) + local cache so
     * each milestone dispatches ONCE cleanly across devices and never duplicates.
     */
    fun evaluateAndDispatchMilestones(
        context: Context,
        user: UserProfile,
        db: FirebaseFirestore
    ) {
        val uid = user.uid
        if (uid.isBlank()) return

        // Real-time Firestore level & tier badge synchronization whenever XP crosses a threshold
        val calcWealthLvl = LevelBadgeSystem.calculateLevelFromXp(user.wealthXp)
        val calcCharmLvl = LevelBadgeSystem.calculateLevelFromXp(user.charmXp)
        val targetBadgeId = LevelBadgeSystem.getTierBadgeId(calcWealthLvl)
        if (user.wealthLevel != calcWealthLvl || user.level != calcWealthLvl || user.charmLevel != calcCharmLvl) {
            db.collection("users").document(uid).set(
                mapOf(
                    "wealthLevel" to calcWealthLvl,
                    "level" to calcWealthLvl,
                    "charmLevel" to calcCharmLvl,
                    "activeWealthBadgeId" to targetBadgeId
                ),
                SetOptions.merge()
            )
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dueRules = milestoneRules.filter { rule ->
            rule.condition(user) && !prefs.getBoolean("dispatched_${uid}_${rule.key}", false)
        }
        if (dueRules.isEmpty()) return

        dueRules.forEach { rule ->
            val docId = "auto_milestone_${rule.key}"
            val notifRef = db.collection("users").document(uid)
                .collection("notifications").document(docId)

            notifRef.get()
                .addOnSuccessListener { existing ->
                    prefs.edit().putBoolean("dispatched_${uid}_${rule.key}", true).apply()
                    if (existing != null && existing.exists()) {
                        return@addOnSuccessListener
                    }

                    val notif = NotificationItem(
                        id = docId,
                        targetUserId = uid,
                        senderId = "system_livetogather",
                        senderName = "LiveTogather Official",
                        senderPhoto = "",
                        type = "assistant_reward",
                        title = rule.title,
                        message = rule.description,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        rewardItemId = rule.rewardItemId,
                        rewardItemType = rule.rewardItemType,
                        rewardItemName = rule.rewardItemName,
                        rewardDurationHours = rule.durationHours,
                        rewardClaimed = false
                    )
                    notifRef.set(notif, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.i(TAG, "Dispatched automated assistant milestone reward: ${rule.key} -> ${rule.rewardItemId}")
                        }
                        .addOnFailureListener { err ->
                            Log.w(TAG, "Could not dispatch milestone ${rule.key}: ${err.message}")
                        }
                }
                .addOnFailureListener { err ->
                    Log.w(TAG, "Failed checking milestone ${rule.key}: ${err.message}")
                }
        }
    }

    /**
     * One-tap Claim & Equip handler invoked directly from the LiveTogather Official Assistant Inbox thread.
     * Grants the temporary 24h / 3-day 3D frame or mic effect in real-time Firestore and equips it immediately.
     */
    fun equipInboxReward(
        context: Context,
        currentUser: UserProfile,
        notification: NotificationItem,
        db: FirebaseFirestore,
        onUserUpdated: (UserProfile) -> Unit,
        onCompleted: (Boolean) -> Unit = {}
    ) {
        val uid = currentUser.uid
        val itemId = notification.rewardItemId
        if (uid.isBlank() || itemId.isBlank()) {
            onCompleted(false)
            return
        }

        val hours = notification.rewardDurationHours.coerceAtLeast(24)
        val expiryMillis = System.currentTimeMillis() + (hours.toLong() * 60L * 60L * 1000L)
        val durationLabel = if (hours >= 72) "${hours / 24} Days" else "${hours} Hours"
        val userRef = db.collection("users").document(uid)
        val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)

        val isMicEffect = notification.rewardItemType.equals("mic_effect", ignoreCase = true) ||
            itemId.startsWith("mic_effect_", ignoreCase = true)

        val updates = if (isMicEffect) {
            mapOf(
                "ownedMicEffects" to FieldValue.arrayUnion(itemId),
                "activeMicEffect" to itemId,
                "micEffectExpiries.$itemId" to expiryMillis
            )
        } else {
            mapOf(
                "ownedFrames" to FieldValue.arrayUnion(itemId),
                "activeFrame" to itemId,
                "frameExpiries.$itemId" to expiryMillis
            )
        }

        userRef.update(updates)
            .addOnSuccessListener {
                // Mark notification claimed & read in Firestore
                if (notification.id.isNotBlank()) {
                    userRef.collection("notifications").document(notification.id)
                        .update(mapOf("rewardClaimed" to true, "isRead" to true))
                }

                val updatedUser = if (isMicEffect) {
                    val newOwned = (currentUser.ownedMicEffects + itemId).distinct()
                    val newExpiries = currentUser.micEffectExpiries.toMutableMap().apply { put(itemId, expiryMillis) }
                    prefs.edit().putString("profile_active_mic_effect", itemId).apply()
                    currentUser.copy(
                        ownedMicEffects = newOwned,
                        activeMicEffect = itemId,
                        micEffectExpiries = newExpiries
                    )
                } else {
                    val newOwned = (currentUser.ownedFrames + itemId).distinct()
                    val newExpiries = currentUser.frameExpiries.toMutableMap().apply { put(itemId, expiryMillis) }
                    prefs.edit()
                        .putStringSet("profile_owned_frames", newOwned.toSet())
                        .putString("profile_active_frame", itemId)
                        .apply()
                    currentUser.copy(
                        ownedFrames = newOwned,
                        activeFrame = itemId,
                        frameExpiries = newExpiries
                    )
                }

                onUserUpdated(updatedUser)
                val displayName = notification.rewardItemName.ifBlank { itemId }
                Toast.makeText(
                    context,
                    "✨ $displayName equipped for $durationLabel!",
                    Toast.LENGTH_LONG
                ).show()
                onCompleted(true)
            }
            .addOnFailureListener { err ->
                Log.e(TAG, "Failed equipping inbox reward $itemId: ${err.message}", err)
                Toast.makeText(context, "Could not equip reward: ${err.localizedMessage ?: "Try again"}", Toast.LENGTH_SHORT).show()
                onCompleted(false)
            }
    }
}
