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
    val description: String = "",
    val drawableRes: Int = 0
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

    // --- B. MIC LINKS (PHOTOREALISTIC 3D CGI JEWELS & FRACTAL ELECTRIC PLASMA CONNECTORS) ---
    val micLinks = listOf(
        StoreItem(
            id = "mic_link_ruby_crown",
            name = "Royal Imperial Ruby Crown",
            category = "mic_link",
            subCategory = "Couple Bond",
            diamondPrice = 45000L,
            rankTier = "S",
            durationDays = 30, // 30 Days (Monthly Luxury Bond)
            iconEmoji = "👑",
            isIntimacyBondOnly = true,
            primaryColor = 0xFFFF1744,
            secondaryColor = 0xFFFFD700,
            description = "Photorealistic 3D Faceted Ruby Heart & 24K Gold Diamond Imperial Crown with Crackling Plasma.",
            drawableRes = R.drawable.photoreal_link_ruby_crown
        ),
        StoreItem(
            id = "mic_link_twin_besties",
            name = "Twin Besties Crystal",
            category = "mic_link",
            subCategory = "Bestie Bond",
            diamondPrice = 25000L,
            rankTier = "S",
            durationDays = 30, // 30 Days (Monthly Bestie Bond)
            iconEmoji = "💙",
            isIntimacyBondOnly = true,
            primaryColor = 0xFF00E5FF,
            secondaryColor = 0xFFFF4081,
            description = "Interlocking 3D Aquamarine & Rose Quartz Hearts with Platinum Filigree & Dual Plasma.",
            drawableRes = R.drawable.photoreal_link_twin_besties
        ),
        StoreItem(
            id = "mic_link_cyberpunk_reactor",
            name = "Cyberpunk Plasma Reactor",
            category = "mic_link",
            subCategory = "Cyber Electro",
            diamondPrice = 35000L,
            rankTier = "S",
            durationDays = 30, // 30 Days (Monthly Cyber Electro)
            iconEmoji = "⚡",
            isIntimacyBondOnly = true,
            primaryColor = 0xFF00F5FF,
            secondaryColor = 0xFFA855F7,
            description = "3D Amethyst Plasma Core in Brushed Titanium Exoskeleton & High-Voltage Electric Discharge.",
            drawableRes = R.drawable.photoreal_link_cyberpunk_reactor
        ),
        StoreItem(
            id = "mic_link_celestial_wings",
            name = "Celestial Angel Wings",
            category = "mic_link",
            subCategory = "Soulmate Bond",
            diamondPrice = 49999L,
            rankTier = "S",
            durationDays = 30, // 30 Days (Monthly Soulmate Bond)
            iconEmoji = "✨",
            isIntimacyBondOnly = true,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFE0F7FA,
            description = "3D Prismatic Cut Diamond Heart with Majestic 24K Gold Feathered Angel Wings & Solar Beams.",
            drawableRes = R.drawable.photoreal_link_celestial_wings
        ),
        StoreItem(
            id = "mic_link_emerald_infinity",
            name = "Emerald Infinity Sovereign",
            category = "mic_link",
            subCategory = "Eternal Bond",
            diamondPrice = 39999L,
            rankTier = "S",
            durationDays = 30, // 30 Days (Monthly Eternal Bond)
            iconEmoji = "💚",
            isIntimacyBondOnly = true,
            primaryColor = 0xFF00E676,
            secondaryColor = 0xFFFFD700,
            description = "Glowing 3D Colombian Emerald Heart with 24K Gold Infinity Mobius Ribbon & Bio-Electric Arcs.",
            drawableRes = R.drawable.photoreal_link_emerald_infinity
        )
    )

    // --- C. AVATAR FRAMES (ALL REAL FRAMES DYNAMICALLY MAPPED FROM AVATAR_FRAMES_CATALOG) ---
    val avatarFrames: List<StoreItem> = AVATAR_FRAMES_CATALOG.map { frame ->
        StoreItem(
            id = frame.id,
            name = frame.name,
            category = "avatar_frame",
            subCategory = if (frame.vipLevel > 0) "VIP Frame" else "Avatar Frame",
            diamondPrice = frame.cost,
            rankTier = when {
                frame.vipLevel >= 5 -> "S"
                frame.vipLevel >= 3 -> "A"
                frame.vipLevel >= 1 -> "B"
                frame.cost >= 50000 -> "C"
                frame.cost >= 20000 -> "D"
                else -> "E"
            },
            durationDays = 30, // 30 Days (1 Month)
            iconEmoji = if (frame.vipLevel > 0) "👑" else "✨",
            primaryColor = frame.auraColors.firstOrNull() ?: 0xFFFFD700,
            secondaryColor = frame.auraColors.getOrNull(1) ?: 0xFFFF5722,
            description = frame.description
        )
    }

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
