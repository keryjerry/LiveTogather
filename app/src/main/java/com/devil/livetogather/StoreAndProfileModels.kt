package com.devil.livetogather

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------
// 1. STORE ITEM & CATEGORY DATA MODELS
// ---------------------------------------------------------------------

enum class StoreCategory(val id: String, val title: String, val icon: String) {
    INTIMACY("intimacy", "Intimacy", "💍"),
    AVATAR("avatar", "Avatar", "👑"),
    MIC_EFFECTS("mic_effects", "Mic Effects", "🌀"),
    ENTRANCE("entrance", "Entrance", "🚀"),
    CHAT_FRAME("chat_frame", "Chat Frame", "💬"),
    PROFILE_THEME("profile_theme", "Profile", "🖼️")
}

data class StoreItem(
    val id: String,
    val name: String,
    val category: String, // "ring", "mic_link", "avatar_frame", "mic_effect", "entrance", "chat_frame", "profile_theme"
    val subCategory: String, // "Ring", "Mic Link", "Love Zone", "Avatar Frame", "Sound Wave", etc.
    val diamondPrice: Long,
    val coinPrice: Long = 0L,
    val rankTier: String = "D", // "S", "A", "B", "C", "D", "E", "F"
    val durationDays: Int = 7, // 1, 7, 30, 365
    val iconEmoji: String = "✨",
    val previewImageUrl: String = "",
    val isActivityOnly: Boolean = false,
    val isNotForSale: Boolean = false,
    val isIntimacyBondOnly: Boolean = false,
    val primaryColor: Long = 0xFFFFD700,
    val secondaryColor: Long = 0xFF00E5FF,
    val description: String = ""
)

// ---------------------------------------------------------------------
// 2. PREDEFINED CATALOG OF STORE ITEMS (MATCHING REFERENCE IMAGES)
// ---------------------------------------------------------------------

object LuxuryStoreCatalog {

    // --- A. RINGS (INTIMACY) ---
    val rings = listOf(
        StoreItem(
            id = "ring_blue_heart",
            name = "Blue Heart",
            category = "ring",
            subCategory = "Ring",
            diamondPrice = 480000L,
            rankTier = "D",
            durationDays = 7,
            iconEmoji = "💙",
            primaryColor = 0xFF00B0FF,
            secondaryColor = 0xFF80D8FF,
            description = "Twin sapphire heart diamonds bonded in platinum."
        ),
        StoreItem(
            id = "ring_golden_diamond",
            name = "Golden Diamond",
            category = "ring",
            subCategory = "Ring",
            diamondPrice = 12000000L,
            rankTier = "S",
            durationDays = 7,
            iconEmoji = "👑",
            isActivityOnly = true,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFF1744,
            description = "Sovereign 24k gold crown ring with radiant crimson ruby."
        ),
        StoreItem(
            id = "ring_marry_me",
            name = "Marry Me.",
            category = "ring",
            subCategory = "Ring",
            diamondPrice = 150000L,
            rankTier = "E",
            durationDays = 7,
            iconEmoji = "💍",
            primaryColor = 0xFF00E5FF,
            secondaryColor = 0xFFE0F7FA,
            description = "Solitaire diamond ring for true romantic commitment."
        ),
        StoreItem(
            id = "ring_sun_and_moon",
            name = "Sun and Moon",
            category = "ring",
            subCategory = "Ring",
            diamondPrice = 6000000L,
            rankTier = "A",
            durationDays = 7,
            iconEmoji = "☀️",
            isActivityOnly = true,
            primaryColor = 0xFFFFB300,
            secondaryColor = 0xFFE1BEE7,
            description = "Interlocking golden celestial rings with angel wings."
        ),
        StoreItem(
            id = "ring_purple_crush",
            name = "Purple Crush",
            category = "ring",
            subCategory = "Ring",
            diamondPrice = 6000000L,
            rankTier = "A",
            durationDays = 7,
            iconEmoji = "💜",
            isActivityOnly = true,
            primaryColor = 0xFFE040FB,
            secondaryColor = 0xFF8E24AA,
            description = "Amethyst love banner with celestial prince and princess."
        ),
        StoreItem(
            id = "ring_ruby_rose",
            name = "Red Ruby Rose",
            category = "ring",
            subCategory = "Ring",
            diamondPrice = 3500000L,
            rankTier = "S",
            durationDays = 7,
            iconEmoji = "🌹",
            primaryColor = 0xFFFF1744,
            secondaryColor = 0xFFFFD700,
            description = "Carved red ruby rose ring wrapped in royal golden thorns."
        )
    )

    // --- B. MIC LINKS (INTIMACY CONNECTORS BETWEEN LIVE MIC SEATS) ---
    val micLinks = listOf(
        StoreItem(
            id = "link_druid_witch",
            name = "Druid Witch",
            category = "mic_link",
            subCategory = "Mic Link",
            diamondPrice = 99999L,
            rankTier = "E",
            durationDays = 1,
            iconEmoji = "🌸",
            isIntimacyBondOnly = true,
            primaryColor = 0xFF81C784,
            secondaryColor = 0xFFFFD54F,
            description = "Enchanted blooming lily connector wave."
        ),
        StoreItem(
            id = "link_jazz_couple",
            name = "Jazz Couple",
            category = "mic_link",
            subCategory = "Mic Link",
            diamondPrice = 99999L,
            rankTier = "D",
            durationDays = 1,
            iconEmoji = "🎸",
            isIntimacyBondOnly = true,
            primaryColor = 0xFFFFB74D,
            secondaryColor = 0xFFFF7043,
            description = "Acoustic guitar melodic equalizer connector."
        ),
        StoreItem(
            id = "link_adventurer",
            name = "Adventurer",
            category = "mic_link",
            subCategory = "Mic Link",
            diamondPrice = 99999L,
            rankTier = "D",
            durationDays = 1,
            iconEmoji = "🧭",
            isIntimacyBondOnly = true,
            primaryColor = 0xFF4FC3F7,
            secondaryColor = 0xFF0288D1,
            description = "Nautical compass oceanic wave connector."
        ),
        StoreItem(
            id = "link_gangster_love",
            name = "Gangster Love",
            category = "mic_link",
            subCategory = "Mic Link",
            diamondPrice = 99999L,
            rankTier = "D",
            durationDays = 1,
            iconEmoji = "💵",
            isIntimacyBondOnly = true,
            primaryColor = 0xFF00E676,
            secondaryColor = 0xFFB9F6CA,
            description = "Cash currency bundles with neon green pulse beam."
        ),
        StoreItem(
            id = "link_vampire_love",
            name = "Vampire Love",
            category = "mic_link",
            subCategory = "Mic Link",
            diamondPrice = 99999L,
            rankTier = "D",
            durationDays = 1,
            iconEmoji = "🌹",
            isIntimacyBondOnly = true,
            primaryColor = 0xFFFF2A6D,
            secondaryColor = 0xFF9C27B0,
            description = "Crimson enchanted rose with magenta frequency wave."
        ),
        StoreItem(
            id = "link_cyberpunk",
            name = "Cyberpunk",
            category = "mic_link",
            subCategory = "Mic Link",
            diamondPrice = 99999L,
            rankTier = "D",
            durationDays = 1,
            iconEmoji = "💜",
            isIntimacyBondOnly = true,
            primaryColor = 0xFF7C4DFF,
            secondaryColor = 0xFF00E5FF,
            description = "Purple crystal heart flanked by electric holographic wings."
        ),
        StoreItem(
            id = "link_green_elven",
            name = "Green Elven",
            category = "mic_link",
            subCategory = "Mic Link",
            diamondPrice = 99999L,
            rankTier = "D",
            durationDays = 1,
            iconEmoji = "💚",
            isIntimacyBondOnly = true,
            primaryColor = 0xFF00E676,
            secondaryColor = 0xFFE0F2F1,
            description = "Emerald elven heart with feathered silver wings."
        ),
        StoreItem(
            id = "link_royal_court",
            name = "Royal Court",
            category = "mic_link",
            subCategory = "Mic Link",
            diamondPrice = 99999L,
            rankTier = "C",
            durationDays = 1,
            iconEmoji = "👑",
            isIntimacyBondOnly = true,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFF1744,
            description = "Golden royal crown encasing a glowing heart."
        ),
        StoreItem(
            id = "link_blue_love",
            name = "Blue Love",
            category = "mic_link",
            subCategory = "Mic Link",
            diamondPrice = 2160L,
            rankTier = "D",
            durationDays = 1,
            iconEmoji = "💙",
            isNotForSale = true,
            primaryColor = 0xFF00E5FF,
            secondaryColor = 0xFF2979FF,
            description = "Pure sapphire heart with electro-cardiac wave."
        )
    )

    // --- C. AVATAR FRAMES ---
    val avatarFrames = listOf(
        StoreItem(
            id = "frame_golden_lion",
            name = "Golden Lion",
            category = "avatar_frame",
            subCategory = "Avatar Frame",
            diamondPrice = 46560L,
            rankTier = "A",
            durationDays = 1,
            iconEmoji = "🦁",
            isNotForSale = true,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFF5722,
            description = "Majestic crowned lion head with golden feathered crest."
        ),
        StoreItem(
            id = "frame_shock",
            name = "Shock",
            category = "avatar_frame",
            subCategory = "Avatar Frame",
            diamondPrice = 2000L,
            rankTier = "F",
            durationDays = 1,
            iconEmoji = "⚡",
            primaryColor = 0xFF00E5FF,
            secondaryColor = 0xFF2979FF,
            description = "Electric cyan lightning spark ring."
        ),
        StoreItem(
            id = "frame_wooo",
            name = "Wooo",
            category = "avatar_frame",
            subCategory = "Avatar Frame",
            diamondPrice = 3200L,
            rankTier = "E",
            durationDays = 1,
            iconEmoji = "🐺",
            primaryColor = 0xFF7986CB,
            secondaryColor = 0xFF3F51B5,
            description = "Silver howling phantom wolf with sharp ice claws."
        ),
        StoreItem(
            id = "frame_apple",
            name = "Apple",
            category = "avatar_frame",
            subCategory = "Avatar Frame",
            diamondPrice = 2000L,
            rankTier = "F",
            durationDays = 1,
            iconEmoji = "🍎",
            primaryColor = 0xFFFF5252,
            secondaryColor = 0xFF66BB6A,
            description = "Ripe crimson apple frame with glossy fresh leaf."
        ),
        StoreItem(
            id = "frame_honey_bear",
            name = "Honey Bear",
            category = "avatar_frame",
            subCategory = "Avatar Frame",
            diamondPrice = 16560L,
            rankTier = "C",
            durationDays = 1,
            iconEmoji = "🐻",
            isNotForSale = true,
            primaryColor = 0xFFFFB74D,
            secondaryColor = 0xFF8D6E63,
            description = "Adorable 3D plush honey bear paws frame."
        ),
        StoreItem(
            id = "frame_little_seal",
            name = "Little Seal",
            category = "avatar_frame",
            subCategory = "Avatar Frame",
            diamondPrice = 16560L,
            rankTier = "C",
            durationDays = 1,
            iconEmoji = "🦭",
            isNotForSale = true,
            primaryColor = 0xFFECEFF1,
            secondaryColor = 0xFFB0BEC5,
            description = "Fluffy white arctic seal pup head frame."
        ),
        StoreItem(
            id = "frame_vip3_gold",
            name = "VIP3 Royal Sovereign",
            category = "avatar_frame",
            subCategory = "Avatar Frame",
            diamondPrice = 88000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "👑",
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFF1744,
            description = "Ultimate VIP3 golden ribbon filigree with sparkling diamonds."
        )
    )

    // --- D. MIC EFFECTS (360° CONTINUOUS ROTATING ORBIT AURA) ---
    val micEffects = listOf(
        StoreItem(
            id = "mic_pale_blue_star",
            name = "Pale Blue Star",
            category = "mic_effect",
            subCategory = "Sound Wave",
            diamondPrice = 2160L,
            rankTier = "C",
            durationDays = 1,
            iconEmoji = "⭐",
            isNotForSale = true,
            primaryColor = 0xFF80D8FF,
            secondaryColor = 0xFF0091EA,
            description = "Ethereal crystal orbit with twinkling celestial stars."
        ),
        StoreItem(
            id = "mic_radiant_echo",
            name = "Radiant Echo",
            category = "mic_effect",
            subCategory = "Sound Wave",
            diamondPrice = 4560L,
            rankTier = "B",
            durationDays = 1,
            iconEmoji = "✨",
            isNotForSale = true,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFF6D00,
            description = "Golden solar flare vortex swirling continuously around the mic."
        ),
        StoreItem(
            id = "mic_cosmic_neon",
            name = "Cosmic Neon Ring",
            category = "mic_effect",
            subCategory = "Sound Wave",
            diamondPrice = 7200L,
            rankTier = "A",
            durationDays = 7,
            iconEmoji = "🪐",
            primaryColor = 0xFFE040FB,
            secondaryColor = 0xFF00E5FF,
            description = "Blinking dual neon rings spinning with audio energy."
        )
    )

    // --- E. ENTRANCE EFFECTS ---
    val entrances = listOf(
        StoreItem(
            id = "ent_cyber_lamborghini",
            name = "Cyber Lamborghini",
            category = "entrance",
            subCategory = "VIP Vehicles",
            diamondPrice = 30000L,
            rankTier = "S",
            durationDays = 7,
            iconEmoji = "🏎️",
            primaryColor = 0xFFFF2A6D,
            secondaryColor = 0xFF00E5FF,
            description = "Golden supercar drifts into the room with neon fireworks."
        ),
        StoreItem(
            id = "ent_royal_jet",
            name = "Royal Sovereign Jet",
            category = "entrance",
            subCategory = "VIP Vehicles",
            diamondPrice = 50000L,
            rankTier = "S",
            durationDays = 7,
            iconEmoji = "✈️",
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFFFFFF,
            description = "Private luxury jet supersonic fly-over announcement."
        )
    )

    // All Items combined
    val allItems: List<StoreItem> = rings + micLinks + avatarFrames + micEffects + entrances

    fun getItem(id: String): StoreItem? = allItems.find { it.id == id }
}

// ---------------------------------------------------------------------
// 3. CLOSE FRIEND / RELATIONSHIP MODEL
// ---------------------------------------------------------------------

data class CloseFriendItem(
    val uid: String,
    val name: String,
    val photoUrl: String,
    val relationType: String, // "Soulmate", "Bestie", "CP"
    val level: Int = 12,
    val iconEmoji: String = "🌼", // "🌼", "🪐", "🎩", "⏳"
    val themeColor: Long = 0xFF26A69A // Teal, Amber, Pink, etc.
)

// ---------------------------------------------------------------------
// 4. HONOR TITLE BADGE MODEL
// ---------------------------------------------------------------------

data class HonorTitleBadge(
    val id: String,
    val title: String,
    val gradientColors: List<Color>,
    val iconEmoji: String
)

object HonorBadgesCatalog {
    val badges = listOf(
        HonorTitleBadge("shining_star", "Shining Star", listOf(Color(0xFF2979FF), Color(0xFF00E5FF)), "🌟"),
        HonorTitleBadge("amber_star", "Amber Star", listOf(Color(0xFFFFB300), Color(0xFFFFD54F)), "⭐"),
        HonorTitleBadge("golden_star", "Golden Star", listOf(Color(0xFFFFD700), Color(0xFFFF9100)), "👑"),
        HonorTitleBadge("luminous_lily", "Luminous Lily", listOf(Color(0xFFEC407A), Color(0xFFAB47BC)), "🌸"),
        HonorTitleBadge("violet_dusk", "Violet Dusk", listOf(Color(0xFF7E57C2), Color(0xFF3F51B5)), "💎"),
        HonorTitleBadge("rising_star", "Rising Star", listOf(Color(0xFF00E676), Color(0xFF1DE9B6)), "🚀"),
        HonorTitleBadge("new_star", "New Star", listOf(Color(0xFFB0BEC5), Color(0xFF78909C)), "✨"),
        HonorTitleBadge("volunteer_lv2", "Volunteer Lv.2", listOf(Color(0xFF80CBC4), Color(0xFFFFCC80)), "🕊️")
    )
}
