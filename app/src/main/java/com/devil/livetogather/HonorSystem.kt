package com.devil.livetogather

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import java.util.UUID

// -------------------------------------------------------------
// 1. HONOR & LUXURY BADGE DATA MODELS
// -------------------------------------------------------------

enum class BadgeCategory(val id: String, val title: String, val icon: String) {
    ALL("all", "All Honors", "✨"),
    CROWNS("crowns", "Overhead Crowns", "👑"),
    ROYAL_HONOR("royal_honor", "Honor Levels", "🛡️"),
    WEALTH("wealth", "Wealth", "💰"),
    CHARM("charm", "Charm", "💖"),
    PK("pk", "PK Battle", "⚔️"),
    SOCIAL("social", "Social & Clan", "🤝"),
    LOYALTY("loyalty", "Loyalty", "🌟")
}

data class HonorBadgeItem(
    val id: String,
    val title: String,
    val category: BadgeCategory,
    val description: String,
    val unlockRequirement: String,
    val drawableRes: Int,
    val isCrown: Boolean = false,
    val minLevel: Int = 1,
    val minDiamonds: Long = 0L,
    val minPkWins: Int = 0,
    val isCoupleOnly: Boolean = false,
    val glowColor: Long = 0xFFFFD700
)

// -------------------------------------------------------------
// 2. 100% REAL 3D MASTER CATALOG (ALL 34 REALISTIC BADGES)
// -------------------------------------------------------------

object HonorCatalog {

    // A. 4 Overhead Floating Crowns
    val crowns = listOf(
        HonorBadgeItem(
            id = "crown_sovereign_emperor",
            title = "Sovereign Emperor Crown",
            category = BadgeCategory.CROWNS,
            description = "3D floating gold and ruby imperial crown bobbing majestically above the avatar.",
            unlockRequirement = "Reach Wealth Level 70+ or host active Room with 50+ viewers.",
            drawableRes = R.drawable.crown_sovereign_emperor,
            isCrown = true,
            minLevel = 70,
            glowColor = 0xFFFFD700
        ),
        HonorBadgeItem(
            id = "crown_phoenix_flame",
            title = "Phoenix Flame Halo",
            category = BadgeCategory.CROWNS,
            description = "3D blazing phoenix fire and obsidian halo for undefeated PK battle champions.",
            unlockRequirement = "Win 10+ consecutive PK Battles or reach PK Level 40.",
            drawableRes = R.drawable.crown_phoenix_flame,
            isCrown = true,
            minLevel = 40,
            minPkWins = 50,
            glowColor = 0xFFFF5722
        ),
        HonorBadgeItem(
            id = "crown_celestial_star",
            title = "Celestial Starlight Diadem",
            category = BadgeCategory.CROWNS,
            description = "Diamond and sapphire starlight diadem radiating shimmering crystalline particles.",
            unlockRequirement = "Reach Charm Level 50+ from gift diamonds received.",
            drawableRes = R.drawable.crown_celestial_star,
            isCrown = true,
            minLevel = 50,
            glowColor = 0xFF00E5FF
        ),
        HonorBadgeItem(
            id = "crown_romance_tiara",
            title = "Romance Eternal Tiara",
            category = BadgeCategory.CROWNS,
            description = "Rose-gold and heart ruby tiara shared between verified intimate couple partners.",
            unlockRequirement = "Form an active Couple Bond and reach Love Zone Level 10.",
            drawableRes = R.drawable.crown_romance_tiara,
            isCrown = true,
            isCoupleOnly = true,
            minLevel = 10,
            glowColor = 0xFFE91E63
        )
    )

    // B. 5 Royal Honor Level Insignias
    val royalHonors = listOf(
        HonorBadgeItem(
            id = "badge_honor_knight",
            title = "Knight of Honor (Lv.1)",
            category = BadgeCategory.ROYAL_HONOR,
            description = "Polished silver and cobalt knight helm with silver wings and sapphire core.",
            unlockRequirement = "Gift 10,000 total diamonds or reach Honor Level 1.",
            drawableRes = R.drawable.badge_honor_knight,
            minLevel = 1,
            minDiamonds = 10000L,
            glowColor = 0xFF2979FF
        ),
        HonorBadgeItem(
            id = "badge_honor_baron",
            title = "Baron of Honor (Lv.2)",
            category = BadgeCategory.ROYAL_HONOR,
            description = "Rose gold and emerald shield with ornate filigree and dual golden scepters.",
            unlockRequirement = "Gift 50,000 total diamonds or reach Honor Level 2.",
            drawableRes = R.drawable.badge_honor_baron,
            minLevel = 2,
            minDiamonds = 50000L,
            glowColor = 0xFF00E676
        ),
        HonorBadgeItem(
            id = "badge_honor_earl",
            title = "Earl of Honor (Lv.3)",
            category = BadgeCategory.ROYAL_HONOR,
            description = "24K solid gold lion medallion with radiant ruby sunburst flares.",
            unlockRequirement = "Gift 150,000 total diamonds or reach Honor Level 3.",
            drawableRes = R.drawable.badge_honor_earl,
            minLevel = 3,
            minDiamonds = 150000L,
            glowColor = 0xFFFFD700
        ),
        HonorBadgeItem(
            id = "badge_honor_grand_duke",
            title = "Grand Duke of Honor (Lv.4)",
            category = BadgeCategory.ROYAL_HONOR,
            description = "Platinum and deep amethyst purple shield with angelic diamond wings and crystal orbs.",
            unlockRequirement = "Gift 500,000 total diamonds or reach Honor Level 4.",
            drawableRes = R.drawable.badge_honor_grand_duke,
            minLevel = 4,
            minDiamonds = 500000L,
            glowColor = 0xFFA855F7
        ),
        HonorBadgeItem(
            id = "badge_honor_supreme_emperor",
            title = "Supreme Emperor of Honor (Lv.5)",
            category = BadgeCategory.ROYAL_HONOR,
            description = "Dual celestial dragon and phoenix crest around blazing incandescent diamond star.",
            unlockRequirement = "Gift 1,500,000 total diamonds or reach Honor Level 5.",
            drawableRes = R.drawable.badge_honor_supreme_emperor,
            minLevel = 5,
            minDiamonds = 1500000L,
            glowColor = 0xFFFF1744
        )
    )

    // C. 25 Master Grid Badges
    val masterGridBadges = listOf(
        // Row 1: Wealth Tiers
        HonorBadgeItem("badge_wealth_bronze", "Bronze Citizen", BadgeCategory.WEALTH, "Bronze citizen wealth insignia.", "Reach Wealth Level 1.", R.drawable.badge_wealth_bronze, minLevel = 1, glowColor = 0xFFCD7F32),
        HonorBadgeItem("badge_wealth_silver", "Silver Noble", BadgeCategory.WEALTH, "Silver shield noble wealth insignia.", "Reach Wealth Level 20.", R.drawable.badge_wealth_silver, minLevel = 20, glowColor = 0xFFC0C0C0),
        HonorBadgeItem("badge_wealth_gold", "Gold Monarch", BadgeCategory.WEALTH, "24K Gold royal monarch insignia.", "Reach Wealth Level 40.", R.drawable.badge_wealth_gold, minLevel = 40, glowColor = 0xFFFFD700),
        HonorBadgeItem("badge_wealth_diamond", "Diamond King", BadgeCategory.WEALTH, "Brilliant cyan diamond king insignia.", "Reach Wealth Level 70.", R.drawable.badge_wealth_diamond, minLevel = 70, glowColor = 0xFF00E5FF),
        HonorBadgeItem("badge_cosmic_overlord_100", "Cosmic Overlord 100", BadgeCategory.WEALTH, "Legendary cosmic titan level 100 insignia.", "Reach Wealth Level 100.", R.drawable.badge_cosmic_overlord_100, minLevel = 100, glowColor = 0xFFC084FC),

        // Row 2: Charm / Host Tiers
        HonorBadgeItem("badge_charm_spark", "Spark Broadcaster", BadgeCategory.CHARM, "Broadcaster initiation charm badge.", "Reach Charm Level 1.", R.drawable.badge_charm_spark, minLevel = 1, glowColor = 0xFFFF4081),
        HonorBadgeItem("badge_charm_rising", "Rising Star Host", BadgeCategory.CHARM, "Rising creator star charm badge.", "Reach Charm Level 20.", R.drawable.badge_charm_rising, minLevel = 20, glowColor = 0xFFFF80AB),
        HonorBadgeItem("badge_charm_golden_mic", "Golden Siren Mic", BadgeCategory.CHARM, "Golden wing broadcaster microphone badge.", "Reach Charm Level 40.", R.drawable.badge_golden_siren_mic, minLevel = 40, glowColor = 0xFFFFD700),
        HonorBadgeItem("badge_charm_diamond_diva", "Diamond Diva Host", BadgeCategory.CHARM, "Diamond crystalline broadcaster badge.", "Reach Charm Level 70.", R.drawable.badge_charm_diamond_diva, minLevel = 70, glowColor = 0xFF00E5FF),
        HonorBadgeItem("badge_charm_mythic_idol", "Mythic Idol Host", BadgeCategory.CHARM, "Mythical celestial broadcaster idol badge.", "Reach Charm Level 100.", R.drawable.badge_charm_mythic_idol, minLevel = 100, glowColor = 0xFFFF1744),

        // Row 3: PK Warlord Tiers
        HonorBadgeItem("badge_pk_first_blood", "PK First Blood", BadgeCategory.PK, "Earned after winning your first PK Battle.", "Win 1 PK Battle.", R.drawable.badge_pk_first_blood, minPkWins = 1, glowColor = 0xFFFF5252),
        HonorBadgeItem("badge_pk_streak_3", "PK 3x Streak", BadgeCategory.PK, "Dual crossed battle blades streak badge.", "Win 3 PK battles in a row.", R.drawable.badge_pk_streak_3, minPkWins = 3, glowColor = 0xFFFF6E40),
        HonorBadgeItem("badge_pk_rampage", "PK Rampage", BadgeCategory.PK, "Flaming battle axes badge.", "Win 10 PK battles total.", R.drawable.badge_pk_rampage, minPkWins = 10, glowColor = 0xFFFF3D00),
        HonorBadgeItem("badge_pk_dominator", "PK Dominator", BadgeCategory.PK, "Golden skull gladiator dominance badge.", "Win 25 PK battles total.", R.drawable.badge_pk_dominator, minPkWins = 25, glowColor = 0xFFFFD600),
        HonorBadgeItem("badge_pk_unstoppable_warlord", "Unstoppable Warlord", BadgeCategory.PK, "Blood-crimson battle warlord crest.", "Win 50 PK battles total.", R.drawable.badge_pk_unstoppable_warlord, minPkWins = 50, glowColor = 0xFFFF1744),

        // Row 4: Social / Clan Tiers
        HonorBadgeItem("badge_social_bestie", "Bestie Bond", BadgeCategory.SOCIAL, "Heart locket best friend insignia.", "Establish verified Bestie bond with a friend.", R.drawable.badge_social_bestie, glowColor = 0xFFFF4081),
        HonorBadgeItem("badge_social_true_love", "True Love Couple", BadgeCategory.SOCIAL, "Rose gold twin hearts couple insignia.", "Form active verified Couple Bond.", R.drawable.badge_social_true_love, isCoupleOnly = true, glowColor = 0xFFE91E63),
        HonorBadgeItem("badge_clan_warrior", "Clan Warrior", BadgeCategory.SOCIAL, "Bronze clan warrior shield.", "Join an active Clan.", R.drawable.badge_clan_warrior, glowColor = 0xFF795548),
        HonorBadgeItem("badge_clan_elite", "Clan Elite Guardian", BadgeCategory.SOCIAL, "Silver and ruby clan guardian crest.", "Reach Level 10 inside your Clan.", R.drawable.badge_clan_elite, minLevel = 10, glowColor = 0xFFE53935),
        HonorBadgeItem("badge_imperial_clan_shield", "Imperial Clan Overlord", BadgeCategory.SOCIAL, "Golden lion imperial clan overlord shield.", "Become Clan Leader of Level 5 Clan.", R.drawable.badge_imperial_clan_shield, minLevel = 25, glowColor = 0xFFFFD700),

        // Row 5: Loyalty / Honor Tiers
        HonorBadgeItem("badge_loyalty_7day", "7-Day Loyal Pioneer", BadgeCategory.LOYALTY, "Bronze wreath 7-day loyalty badge.", "Login 7 consecutive days.", R.drawable.badge_loyalty_7day, glowColor = 0xFF8D6E63),
        HonorBadgeItem("badge_loyalty_30day", "30-Day Devoted Guardian", BadgeCategory.LOYALTY, "Silver winged 30-day devotion badge.", "Active on app for 30 days.", R.drawable.badge_loyalty_30day, glowColor = 0xFFB0BEC5),
        HonorBadgeItem("badge_loyalty_100day", "100-Day Centennial VIP", BadgeCategory.LOYALTY, "Gold wreath centennial diamond star badge.", "Active on app for 100 days.", R.drawable.badge_loyalty_100day, glowColor = 0xFFFFD700),
        HonorBadgeItem("badge_loyalty_room_master", "Room Master Architect", BadgeCategory.LOYALTY, "Golden key room host architect badge.", "Host 20+ live audio rooms.", R.drawable.badge_loyalty_room_master, glowColor = 0xFF00E5FF),
        HonorBadgeItem("badge_loyalty_vip_guardian", "VIP Guardian Shield", BadgeCategory.LOYALTY, "Platinum royal guardian protection shield.", "Maintain active VIP membership.", R.drawable.badge_loyalty_vip_guardian, glowColor = 0xFFA855F7)
    )

    val allBadges: List<HonorBadgeItem> = crowns + royalHonors + masterGridBadges

    fun findBadge(id: String): HonorBadgeItem? = allBadges.find { it.id == id }
}

// -------------------------------------------------------------
// 3. REAL-TIME FIREBASE AUTOMATION & GREETING NOTIFICATION ENGINE
// -------------------------------------------------------------

object HonorBadgeManager {

    /**
     * Checks user's levels, diamonds, streaks, and automatically unlocks badges in Firestore.
     * When unlocked:
     * 1. Updates `unlockedBadges` in Firestore `users/{uid}`
     * 2. Writes an official Inbox Notification with Greetings & Reward
     * 3. Triggers local high-priority Android notification
     */
    fun checkAndUnlockBadges(
        context: Context,
        profile: UserProfile,
        db: FirebaseFirestore,
        onProfileUpdated: (UserProfile) -> Unit
    ) {
        val uid = profile.uid
        if (uid.isBlank()) return

        val unlockedNow = profile.unlockedBadges.toMutableSet()
        val newlyUnlockedList = mutableListOf<HonorBadgeItem>()

        val effWealth = LevelBadgeSystem.getEffectiveWealthLevel(profile)
        val effCharm = LevelBadgeSystem.getEffectiveCharmLevel(profile)
        val effDiamonds = profile.diamondsSpentCount.toLong() * 100L // or total diamonds
        val effPkWins = profile.pkWins

        val isSupremeAccess = profile.isFounder() || profile.isOfficialAdmin() || effWealth >= 100 || effCharm >= 100

        HonorCatalog.allBadges.forEach { badge ->
            if (!unlockedNow.contains(badge.id)) {
                var canUnlock = isSupremeAccess || isBadgeUnlocked(badge, profile)
                when (badge.category) {
                    BadgeCategory.CROWNS -> {
                        when (badge.id) {
                            "crown_sovereign_emperor" -> canUnlock = effWealth >= 70
                            "crown_phoenix_flame" -> canUnlock = effPkWins >= 25 || profile.pkLevel >= 30
                            "crown_celestial_star" -> canUnlock = effCharm >= 50
                            "crown_romance_tiara" -> canUnlock = profile.bestFriends.isNotEmpty() || profile.unlockedBadges.contains("badge_social_true_love")
                        }
                    }
                    BadgeCategory.ROYAL_HONOR -> {
                        val reqLevel = badge.minLevel
                        canUnlock = profile.honorLevel >= reqLevel || effDiamonds >= badge.minDiamonds || effWealth >= (reqLevel * 15)
                    }
                    BadgeCategory.WEALTH -> {
                        canUnlock = effWealth >= badge.minLevel
                    }
                    BadgeCategory.CHARM -> {
                        canUnlock = effCharm >= badge.minLevel
                    }
                    BadgeCategory.PK -> {
                        canUnlock = effPkWins >= badge.minPkWins
                    }
                    BadgeCategory.SOCIAL -> {
                        if (badge.isCoupleOnly) canUnlock = profile.bestFriends.isNotEmpty() || profile.unlockedBadges.contains("badge_social_true_love")
                        else if (badge.id == "badge_social_bestie") canUnlock = profile.bestFriends.isNotEmpty()
                        else if (badge.id == "badge_clan_warrior") canUnlock = effWealth >= 5
                        else if (badge.id == "badge_clan_elite") canUnlock = effWealth >= 15
                        else if (badge.id == "badge_imperial_clan_shield") canUnlock = effWealth >= 25
                    }
                    BadgeCategory.LOYALTY -> {
                        if (badge.id == "badge_loyalty_7day") canUnlock = effWealth >= 3
                        else if (badge.id == "badge_loyalty_30day") canUnlock = effWealth >= 10
                        else if (badge.id == "badge_loyalty_100day") canUnlock = effWealth >= 25
                        else if (badge.id == "badge_loyalty_room_master") canUnlock = profile.totalRoomTimeSeconds >= 7200L
                        else if (badge.id == "badge_loyalty_vip_guardian") canUnlock = profile.getVipLevel() > 0
                    }
                    BadgeCategory.ALL -> {}
                }

                if (canUnlock) {
                    unlockedNow.add(badge.id)
                    newlyUnlockedList.add(badge)
                }
            }
        }

        if (newlyUnlockedList.isNotEmpty()) {
            val updatedBadgesList = unlockedNow.toList()

            // 1. Update Firestore User Document
            val userUpdates = mutableMapOf<String, Any>(
                "unlockedBadges" to updatedBadgesList
            )

            // If user has no active crown and unlocked one, auto-equip
            val newCrown = newlyUnlockedList.find { it.isCrown }
            if (newCrown != null && profile.activeCrown.isBlank()) {
                userUpdates["activeCrown"] = newCrown.id
            }

            db.collection("users").document(uid).update(userUpdates)

            // 2. Dispatch Official Inbox Notifications with Greetings
            newlyUnlockedList.forEach { badge ->
                val notifId = UUID.randomUUID().toString()
                val bonusGiftDiamonds = when {
                    badge.isCrown -> 10L
                    badge.category == BadgeCategory.ROYAL_HONOR -> 15L
                    badge.minLevel >= 50 -> 8L
                    else -> 3L
                }

                val officialNotif = NotificationItem(
                    id = notifId,
                    targetUserId = uid,
                    senderId = "system_livetogather_official",
                    senderName = "LiveTogather Official Team 👑",
                    senderPhoto = "",
                    type = "badge_unlocked_greeting",
                    title = "🎉 बधाई हो! ${badge.title} बैज अनलॉक हुआ!",
                    message = "प्रिय ${profile.name}, आपकी असाधारण प्रतिष्ठा के लिए आपको '${badge.title}' बैज से सम्मानित किया गया है! इसके साथ आपको $bonusGiftDiamonds बोनस डायमंड्स 💎 उपहार स्वरूप दिए गए हैं। यह बैज अब आपकी प्रोफ़ाइल और चैट में सक्रिय है।",
                    diamondsReceived = bonusGiftDiamonds,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )

                // Write to root notifications collection (real-time bell icon)
                db.collection("notifications").document(notifId).set(officialNotif)

                // Write to user private inbox
                db.collection("users").document(uid)
                    .collection("notifications").document(notifId).set(officialNotif)

                // Increment user's diamonds directly
                db.collection("users").document(uid)
                    .update("diamonds", FieldValue.increment(bonusGiftDiamonds))

                // Show local Android notification
                AppNotificationHelper.showSystemNotification(
                    context = context,
                    title = "🎉 New 3D Badge Unlocked: ${badge.title}!",
                    message = "LiveTogather awarded you $bonusGiftDiamonds bonus diamonds 💎!",
                    isPurchaseAlert = true
                )
            }

            // Update local memory state
            onProfileUpdated(
                profile.copy(
                    unlockedBadges = updatedBadgesList,
                    activeCrown = if (newCrown != null && profile.activeCrown.isBlank()) newCrown.id else profile.activeCrown
                )
            )
        }
    }
}

// -------------------------------------------------------------
// 4. OVERHEAD 3D FLOATING CROWN COMPOSABLE
// -------------------------------------------------------------

@Composable
fun OverheadCrownBadge(
    crownId: String,
    modifier: Modifier = Modifier,
    crownSize: Dp = 38.dp,
    enableBobbing: Boolean = true
) {
    if (crownId.isBlank()) return

    val crownItem = remember(crownId) { HonorCatalog.findBadge(crownId) } ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "CrownBobbing")
    val bobbingOffsetY by if (enableBobbing) {
        infiniteTransition.animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "CrownBobbingY"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = modifier
            .offset(y = bobbingOffsetY.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = crownItem.drawableRes),
            contentDescription = crownItem.title,
            modifier = Modifier
                .size(crownSize),
            contentScale = ContentScale.Fit
        )
    }
}

// -------------------------------------------------------------
// -------------------------------------------------------------
// BADGE & CROWN UNLOCK ACCESS CHECK (FOUNDER / ADMIN / LEVEL 100)
// -------------------------------------------------------------

fun isBadgeUnlocked(badge: HonorBadgeItem, profile: UserProfile): Boolean {
    // Supreme Founder access: all badges and crowns freely unlocked
    if (profile.isFounder()) return true
    if (profile.unlockedBadges.contains(badge.id)) return true

    val effWealth = LevelBadgeSystem.getEffectiveWealthLevel(profile)
    val effCharm = LevelBadgeSystem.getEffectiveCharmLevel(profile)
    val effPkWins = profile.pkWins

    // Max level 100 players have unlocked the entire pantheon of badges
    if (effWealth >= 100 || effCharm >= 100 || profile.level >= 100) return true

    return when (badge.category) {
        BadgeCategory.CROWNS -> {
            when (badge.id) {
                "crown_sovereign_emperor" -> effWealth >= 70
                "crown_phoenix_flame" -> effPkWins >= 25 || profile.pkLevel >= 30
                "crown_celestial_star" -> effCharm >= 50
                "crown_romance_tiara" -> profile.bestFriends.isNotEmpty() || profile.unlockedBadges.contains("badge_social_true_love")
                else -> effWealth >= 50
            }
        }
        BadgeCategory.ROYAL_HONOR -> profile.honorLevel >= badge.minLevel || effWealth >= (badge.minLevel * 15)
        BadgeCategory.WEALTH -> effWealth >= badge.minLevel
        BadgeCategory.CHARM -> effCharm >= badge.minLevel
        BadgeCategory.PK -> effPkWins >= badge.minPkWins || profile.pkLevel >= badge.minLevel
        BadgeCategory.SOCIAL -> {
            if (badge.isCoupleOnly) profile.bestFriends.isNotEmpty() || profile.unlockedBadges.contains("badge_social_true_love")
            else if (badge.id == "badge_social_bestie") profile.bestFriends.isNotEmpty()
            else if (badge.id == "badge_clan_warrior") effWealth >= 5
            else if (badge.id == "badge_clan_elite") effWealth >= 15
            else if (badge.id == "badge_imperial_clan_shield") effWealth >= 25
            else true
        }
        BadgeCategory.LOYALTY -> {
            if (badge.id == "badge_loyalty_7day") effWealth >= 3
            else if (badge.id == "badge_loyalty_30day") effWealth >= 10
            else if (badge.id == "badge_loyalty_100day") effWealth >= 25
            else if (badge.id == "badge_loyalty_room_master") profile.totalRoomTimeSeconds >= 7200L
            else if (badge.id == "badge_loyalty_vip_guardian") profile.getVipLevel() > 0
            else true
        }
        BadgeCategory.ALL -> false
    }
}

// 5. 3D BADGE INSPECTOR & LEVEL-UP DIALOG (LOCKED / UNLOCKED)
// -------------------------------------------------------------

@Composable
fun BadgeInspectorDialog(
    badge: HonorBadgeItem,
    userProfile: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onGoToRecharge: () -> Unit,
    onProfileUpdated: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val isUnlocked = isBadgeUnlocked(badge, userProfile)
    val isEquipped = if (badge.isCrown) userProfile.activeCrown == badge.id else userProfile.equippedBadges.contains(badge.id)

    val effWealth = LevelBadgeSystem.getEffectiveWealthLevel(userProfile)
    val effCharm = LevelBadgeSystem.getEffectiveCharmLevel(userProfile)
    val effPkWins = userProfile.pkWins

    // Calculate real progress
    val (curVal, targetVal) = remember(badge, userProfile) {
        when (badge.category) {
            BadgeCategory.CROWNS -> {
                when (badge.id) {
                    "crown_sovereign_emperor" -> Pair(effWealth, 70)
                    "crown_phoenix_flame" -> Pair(effPkWins, 25)
                    "crown_celestial_star" -> Pair(effCharm, 50)
                    "crown_romance_tiara" -> Pair(if (userProfile.bestFriends.isNotEmpty()) 10 else 0, 10)
                    else -> Pair(effWealth, 50)
                }
            }
            BadgeCategory.ROYAL_HONOR -> Pair(effWealth, badge.minLevel * 15)
            BadgeCategory.WEALTH -> Pair(effWealth, badge.minLevel)
            BadgeCategory.CHARM -> Pair(effCharm, badge.minLevel)
            BadgeCategory.PK -> Pair(effPkWins, badge.minPkWins)
            else -> Pair(effWealth, maxOf(1, badge.minLevel))
        }
    }

    val progressFraction = if (isUnlocked) 1.0f else (curVal.toFloat() / maxOf(1, targetVal).toFloat()).coerceIn(0f, 1f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(badge.glowColor), Color(0xFF1E0B36))), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F081E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(badge.glowColor).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(badge.glowColor).copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = badge.category.title.uppercase(),
                            color = Color(badge.glowColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grand 3D Badge Showcase with Animated Halo
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(badge.glowColor).copy(alpha = 0.45f), Color(0xFF1B0C35), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = badge.drawableRes),
                        contentDescription = badge.title,
                        modifier = Modifier
                            .size(86.dp)
                            ,
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title & Subtitle
                Text(
                    text = badge.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = badge.description,
                    color = Color(0xFFD1D5DB),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Unlock Requirements Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1A112E),
                    border = BorderStroke(0.8.dp, Color(0xFF3B2761))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = if (isUnlocked) "✅" else "🔒", fontSize = 14.sp)
                            Text(
                                text = "शर्त (Unlock Requirement):",
                                color = BrandGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = badge.unlockRequirement,
                            color = Color.White,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Real-Time Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "प्रगति (Live Progress)",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (isUnlocked) "100% (Completed)" else "$curVal / $targetVal (${(progressFraction * 100).toInt()}%)",
                                color = if (isUnlocked) Color(0xFF00E676) else Color(badge.glowColor),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(badge.glowColor),
                            trackColor = Color(0xFF2B1D47)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button: Equip or Go to Recharge
                if (isUnlocked) {
                    Button(
                        onClick = {
                            val uid = userProfile.uid
                            if (uid.isNotBlank()) {
                                val updates = mutableMapOf<String, Any>()
                                val newUnlockedList = if (!userProfile.unlockedBadges.contains(badge.id)) {
                                    userProfile.unlockedBadges + badge.id
                                } else userProfile.unlockedBadges
                                updates["unlockedBadges"] = newUnlockedList

                                if (badge.isCrown) {
                                    val newCrown = if (isEquipped) "" else badge.id
                                    updates["activeCrown"] = newCrown
                                    db.collection("users").document(uid).update(updates)
                                    val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit().putString("profile_active_crown", newCrown).apply()
                                    onProfileUpdated(userProfile.copy(activeCrown = newCrown, unlockedBadges = newUnlockedList))
                                } else {
                                    val currentList = userProfile.equippedBadges.toMutableList()
                                    if (isEquipped) {
                                        currentList.remove(badge.id)
                                    } else {
                                        if (currentList.size >= 12) currentList.removeAt(0)
                                        currentList.add(badge.id)
                                    }
                                    updates["equippedBadges"] = currentList
                                    db.collection("users").document(uid).update(updates)
                                    val prefs = context.getSharedPreferences("livetogather_user_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit().putStringSet("profile_equipped_badges", currentList.toSet()).putString("profile_equipped_badges_csv", currentList.joinToString(",")).apply()
                                    onProfileUpdated(userProfile.copy(equippedBadges = currentList, unlockedBadges = newUnlockedList))
                                }
                                Toast.makeText(context, if (isEquipped) "बैज हटाया गया" else "बैज सक्रिय किया गया! ✨", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEquipped) Color(0xFF263238) else Color(badge.glowColor)
                        )
                    ) {
                        Text(
                            text = if (isEquipped) "इक्विप हटाएँ (Unequip)" else "सक्रिय करें (Equip Now) ✨",
                            color = if (isEquipped) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            onDismiss()
                            onGoToRecharge()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "लेवल अप करें / रीचार्ज करें 💎",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. HONOR PAVILION & BADGES STORE TAB (FOR VIP MALL & PROFILE)
// -------------------------------------------------------------

@Composable
fun HonorPavilionContent(
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onOpenTopUp: () -> Unit,
    onProfileUpdated: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCat by remember { mutableStateOf(BadgeCategory.ALL) }
    var inspectingBadge by remember { mutableStateOf<HonorBadgeItem?>(null) }

    val filteredBadges = remember(selectedCat) {
        if (selectedCat == BadgeCategory.ALL) HonorCatalog.allBadges
        else HonorCatalog.allBadges.filter { it.category == selectedCat }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Category Selector Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            items(BadgeCategory.values()) { cat ->
                val isSelected = cat == selectedCat
                Surface(
                    modifier = Modifier.clickable { selectedCat = cat },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E1035),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFC084FC) else Color(0xFF38235C))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = cat.icon, fontSize = 12.sp)
                        Text(
                            text = cat.title,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Badges Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredBadges, key = { it.id }) { badge ->
                val isUnlocked = isBadgeUnlocked(badge, currentUser)
                val isEquipped = if (badge.isCrown) currentUser.activeCrown == badge.id else currentUser.equippedBadges.contains(badge.id)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { inspectingBadge = badge },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF140A28),
                    border = BorderStroke(
                        width = if (isEquipped) 1.5.dp else 0.8.dp,
                        color = when {
                            isEquipped -> Color(0xFF00E676)
                            isUnlocked -> Color(badge.glowColor)
                            else -> Color(0xFF2C194D)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge Image with Lock Overlay if locked
                        Box(
                            modifier = Modifier.size(68.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = badge.drawableRes),
                                contentDescription = badge.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(if (!isUnlocked) Modifier.alpha(0.4f) else Modifier),
                                contentScale = ContentScale.Fit
                            )

                            if (!isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            } else if (isEquipped) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Equipped",
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = badge.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (isEquipped) "सक्रिय (Equipped)" else if (isUnlocked) "अनलॉक (Owned)" else "🔒 लॉक है",
                            color = when {
                                isEquipped -> Color(0xFF00E676)
                                isUnlocked -> Color(badge.glowColor)
                                else -> Color.Gray
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    inspectingBadge?.let { badge ->
        BadgeInspectorDialog(
            badge = badge,
            userProfile = currentUser,
            db = db,
            onDismiss = { inspectingBadge = null },
            onGoToRecharge = onOpenTopUp,
            onProfileUpdated = onProfileUpdated
        )
    }
}
