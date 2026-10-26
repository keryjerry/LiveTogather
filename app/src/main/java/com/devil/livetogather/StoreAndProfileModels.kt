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
    val drawableRes: Int = 0,
    val videoRes: Int = 0
)

// ---------------------------------------------------------------------
// 2. PREDEFINED CATALOG OF STORE ITEMS (MATCHING REFERENCE IMAGES)
// ---------------------------------------------------------------------

object LuxuryStoreCatalog {

    // --- A. RINGS & ROMANTIC PROPOSALS (DISTINCT REAL ITEMS & VIDEOS) ---
    val rings = listOf(
        StoreItem(
            id = "ring_diamond_proposal",
            name = "Diamond Proposal Ring",
            category = "ring",
            subCategory = "Ring",
            diamondPrice = 150000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "💍",
            drawableRes = R.drawable.thumb_diamond_ring_proposal,
            videoRes = R.raw.diamond_ring_proposal,
            primaryColor = 0xFF00E5FF,
            secondaryColor = 0xFFE0F7FA,
            description = "3D Sparkling Solitaire Diamond Proposal Ring with prism dispersion."
        ),
        StoreItem(
            id = "ring_rose_proposal",
            name = "Rose For Proposal",
            category = "ring",
            subCategory = "Love Zone",
            diamondPrice = 75000L,
            rankTier = "A",
            durationDays = 30,
            iconEmoji = "🌹",
            drawableRes = R.drawable.thumb_rose_proposal,
            videoRes = R.raw.rose_proposal,
            primaryColor = 0xFFFF1744,
            secondaryColor = 0xFFFFD700,
            description = "Romantic blooming velvet rose with golden dew drops."
        ),
        StoreItem(
            id = "ring_princess_ruby",
            name = "Princess Ruby Necklace",
            category = "ring",
            subCategory = "Jewelry",
            diamondPrice = 65000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "💎",
            drawableRes = R.drawable.thumb_princess_ruby_necklace,
            videoRes = R.raw.princess_ruby_necklace,
            primaryColor = 0xFFFF1744,
            secondaryColor = 0xFFFFD700,
            description = "Royal pigeon-blood ruby gemstone in 24k gold filigree."
        ),
        StoreItem(
            id = "ring_treasure_for_you",
            name = "Treasure For You",
            category = "ring",
            subCategory = "Jewelry",
            diamondPrice = 80000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "📦",
            drawableRes = R.drawable.thumb_treasure_for_you,
            videoRes = R.raw.treasure_for_you,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFFB300,
            description = "Glittering royal diamond crown and sparkling golden vault."
        ),
        StoreItem(
            id = "ring_founded_treasure",
            name = "Imperial Lost Treasure",
            category = "ring",
            subCategory = "Jewelry",
            diamondPrice = 90000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "👑",
            drawableRes = R.drawable.thumb_founded_treasure,
            videoRes = R.raw.founded_treasure,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFF5722,
            description = "Ancient royal gold chest opening with dazzling jewels and rubies."
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

    // --- D. MIC EFFECTS (7 REAL HD 3D TRANSPARENT MAGIC EFFECTS) ---
    val micEffects = listOf(
        StoreItem(
            id = "mic_effect_golden_amber_aura",
            name = "Golden Amber Radiant Aura",
            category = "mic_effect",
            subCategory = "Sound Wave",
            diamondPrice = 2500L,
            rankTier = "A",
            durationDays = 30,
            iconEmoji = "🌟",
            drawableRes = R.drawable.thumb_mic_effect_golden_amber_aura,
            videoRes = R.raw.mic_effect_golden_amber_aura,
            primaryColor = 0xFFFFB300,
            secondaryColor = 0xFFFFD54F,
            description = "Radiant golden amber aura with revolving solar energy."
        ),
        StoreItem(
            id = "mic_effect_solar_particle_ring",
            name = "Golden Solar Particle Ring",
            category = "mic_effect",
            subCategory = "Sound Wave",
            diamondPrice = 2800L,
            rankTier = "A",
            durationDays = 30,
            iconEmoji = "☀️",
            drawableRes = R.drawable.thumb_mic_effect_solar_particle_ring,
            videoRes = R.raw.mic_effect_solar_particle_ring,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFF9100,
            description = "Orbiting 24K gold stellar particles dancing around the mic."
        ),
        StoreItem(
            id = "mic_effect_ruby_energy_flame",
            name = "Crimson Ruby Energy Flame",
            category = "mic_effect",
            subCategory = "Sound Wave",
            diamondPrice = 3200L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "🔥",
            drawableRes = R.drawable.thumb_mic_effect_ruby_energy_flame,
            videoRes = R.raw.mic_effect_ruby_energy_flame,
            primaryColor = 0xFFFF1744,
            secondaryColor = 0xFFFF5252,
            description = "Blazing crimson ruby plasma flame vortex circling the avatar."
        ),
        StoreItem(
            id = "mic_effect_cyber_neon_electric",
            name = "Cyber Neon Electric Blue",
            category = "mic_effect",
            subCategory = "Mic Animation",
            diamondPrice = 3500L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "⚡",
            drawableRes = R.drawable.thumb_mic_effect_cyber_neon_electric,
            videoRes = R.raw.mic_effect_cyber_neon_electric,
            primaryColor = 0xFF00E5FF,
            secondaryColor = 0xFFA855F7,
            description = "High-voltage cyan and purple electric lightning rings."
        ),
        StoreItem(
            id = "mic_effect_diamond_crystal_vortex",
            name = "Prismatic Diamond Crystal Vortex",
            category = "mic_effect",
            subCategory = "Mic Animation",
            diamondPrice = 3900L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "💎",
            drawableRes = R.drawable.thumb_mic_effect_diamond_crystal_vortex,
            videoRes = R.raw.mic_effect_diamond_crystal_vortex,
            primaryColor = 0xFFE0F7FA,
            secondaryColor = 0xFF80D8FF,
            description = "Pure prismatic diamond light crystal vortex spinning gracefully."
        ),
        StoreItem(
            id = "mic_effect_sovereign_gold_wings",
            name = "Imperial Sovereign Gold Wings",
            category = "mic_effect",
            subCategory = "Mic Animation",
            diamondPrice = 4200L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "👑",
            drawableRes = R.drawable.thumb_mic_effect_sovereign_gold_wings,
            videoRes = R.raw.mic_effect_sovereign_gold_wings,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFF8F00,
            description = "Royal golden angel wings radiating celestial power around the mic."
        ),
        StoreItem(
            id = "mic_effect_emerald_celestial_wave",
            name = "Emerald Cyan Celestial Wave",
            category = "mic_effect",
            subCategory = "Mic Color Name",
            diamondPrice = 4500L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "🌊",
            drawableRes = R.drawable.thumb_mic_effect_emerald_celestial_wave,
            videoRes = R.raw.mic_effect_emerald_celestial_wave,
            primaryColor = 0xFF00E676,
            secondaryColor = 0xFF00E5FF,
            description = "Luminous emerald cyan ripple wave pulsating with voice vibrations."
        )
    )

    // --- E. ENTRANCE EFFECTS (8 REAL HD 3D VIDEO ENTRANCES) ---
    val entrances = listOf(
        StoreItem(
            id = "ent_king_palace",
            name = "Imperial King Palace",
            category = "entrance",
            subCategory = "VIP Vehicles",
            diamondPrice = 85000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "👑",
            drawableRes = R.drawable.thumb_king_entry,
            videoRes = R.raw.king_entry,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFF8F00,
            description = "Royal King Coronation palace walk with golden fireworks."
        ),
        StoreItem(
            id = "ent_fire_lion",
            name = "Blazing Fire Lion",
            category = "entrance",
            subCategory = "VIP Vehicles",
            diamondPrice = 75000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "🦁",
            drawableRes = R.drawable.thumb_king_lion_entry,
            videoRes = R.raw.king_lion_entry,
            primaryColor = 0xFFFF5722,
            secondaryColor = 0xFFFFD700,
            description = "Majestic roaring solar flame lion 3D entrance."
        ),
        StoreItem(
            id = "ent_golden_supercar",
            name = "Golden 3D Supercar",
            category = "entrance",
            subCategory = "VIP Vehicles",
            diamondPrice = 60000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "🏎️",
            drawableRes = R.drawable.thumb_golden_super_car,
            videoRes = R.raw.golden_super_car,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFFB300,
            description = "High-speed 3D gold sports car power drift entry."
        ),
        StoreItem(
            id = "ent_cyber_lamborghini",
            name = "Cyber Lamborghini",
            category = "entrance",
            subCategory = "VIP Vehicles",
            diamondPrice = 50000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "🏎️",
            drawableRes = R.drawable.thumb_lamborgini_car,
            videoRes = R.raw.lamborgini_car,
            primaryColor = 0xFFFF2A6D,
            secondaryColor = 0xFF00E5FF,
            description = "Neon futuristic cyber supercar high-speed entrance."
        ),
        StoreItem(
            id = "ent_royal_jet",
            name = "Royal Supersonic Jet",
            category = "entrance",
            subCategory = "Aircrafts",
            diamondPrice = 120000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "✈️",
            drawableRes = R.drawable.thumb_royal_jet,
            videoRes = R.raw.royal_jet,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFFFFFF,
            description = "Supersonic luxury private jet soaring into room."
        ),
        StoreItem(
            id = "ent_heaven_ship",
            name = "Heaven Sovereign Yacht",
            category = "entrance",
            subCategory = "Aircrafts",
            diamondPrice = 100000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "🛥️",
            drawableRes = R.drawable.thumb_heaven_ship,
            videoRes = R.raw.heaven_ship,
            primaryColor = 0xFF00E5FF,
            secondaryColor = 0xFFFFD700,
            description = "Billionaire mega yacht cruising with sky fireworks."
        ),
        StoreItem(
            id = "ent_fire_eagle",
            name = "Fire Phoenix Eagle",
            category = "entrance",
            subCategory = "Banners",
            diamondPrice = 45000L,
            rankTier = "A",
            durationDays = 30,
            iconEmoji = "🦅",
            drawableRes = R.drawable.thumb_eagle_entry,
            videoRes = R.raw.eagle_entry,
            primaryColor = 0xFFFF6D00,
            secondaryColor = 0xFFFFD700,
            description = "Blazing golden fire phoenix flight entrance."
        ),
        StoreItem(
            id = "ent_mecha_terminator",
            name = "Cyber Mecha Dragon",
            category = "entrance",
            subCategory = "Banners",
            diamondPrice = 65000L,
            rankTier = "S",
            durationDays = 30,
            iconEmoji = "🐉",
            drawableRes = R.drawable.thumb_terminator_entry,
            videoRes = R.raw.terminator_entry,
            primaryColor = 0xFF00F5FF,
            secondaryColor = 0xFFA855F7,
            description = "Futuristic mecha dragon lightning burst entry."
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
