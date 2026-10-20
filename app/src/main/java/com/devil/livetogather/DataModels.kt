package com.devil.livetogather

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class IntentCategory(val id: String, val label: String)

val CATEGORY_LIST = listOf(
    IntentCategory("all", "🔥 All Profiles"),
    IntentCategory("vip_room", "👑 VIP Room"),
    IntentCategory("free_tonight", "🍷 Free Tonight"),
    IntentCategory("free_tomorrow", "🌙 Free Tomorrow"),
    IntentCategory("free_weekend", "🎉 Free in Weekend"),
    IntentCategory("ready_travel", "🧳 Ready for Travelling"),
    IntentCategory("travel_partner", "✈️ Travel Partner"),
    IntentCategory("hangout_partner", "🤝 Hangout Partner"),
    IntentCategory("free_party", "🥳 Free for Party"),
    IntentCategory("join_party", "🍸 Join Party"),
    IntentCategory("going_club", "🕺 Going to Club"),
    IntentCategory("plan_trip", "🗺️ Lets Plan Holiday Trip"),
    IntentCategory("lets_movie", "🍿 Lets Go For Movie"),
    IntentCategory("lets_cafe", "☕ Lets Go Cafe"),
    IntentCategory("lets_dinner", "🍽️ Lets Go For Dinner/Lunch")
)

data class RoomItem(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("emoji") @set:PropertyName("emoji") var emoji: String = "🎙️",
    @get:PropertyName("banner_image_url") @set:PropertyName("banner_image_url") var banner_image_url: String = "",
    @get:PropertyName("activeUserCount") @set:PropertyName("activeUserCount") var activeUserCount: Int = 0,
    @get:PropertyName("hostId") @set:PropertyName("hostId") var hostId: String = "",
    @get:PropertyName("hostName") @set:PropertyName("hostName") var hostName: String = "",
    @get:PropertyName("hostPhotoUrl") @set:PropertyName("hostPhotoUrl") var hostPhotoUrl: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "",
    @get:PropertyName("nowPlayingTitle") @set:PropertyName("nowPlayingTitle") var nowPlayingTitle: String = "",
    @get:PropertyName("nowPlayingArtist") @set:PropertyName("nowPlayingArtist") var nowPlayingArtist: String = "",
    @get:PropertyName("nowPlayingUrl") @set:PropertyName("nowPlayingUrl") var nowPlayingUrl: String = "",
    @get:PropertyName("nowPlayingArtworkUrl") @set:PropertyName("nowPlayingArtworkUrl") var nowPlayingArtworkUrl: String = "",
    @get:PropertyName("currentPlayingTrack") @set:PropertyName("currentPlayingTrack") var currentPlayingTrack: String = "",
    @get:PropertyName("playbackPosition") @set:PropertyName("playbackPosition") var playbackPosition: Long = 0L,
    @get:PropertyName("isPlaying") @set:PropertyName("isPlaying") var isPlaying: Boolean = false,
    @get:PropertyName("isCustom") @set:PropertyName("isCustom") var isCustom: Boolean = true,
    @get:PropertyName("isHostLive") @set:PropertyName("isHostLive") var isHostLive: Boolean = true,
    @get:PropertyName("scheduledTime") @set:PropertyName("scheduledTime") var scheduledTime: String = "",
    @get:PropertyName("statusMessage") @set:PropertyName("statusMessage") var statusMessage: String = "",
    @get:PropertyName("privacy") @set:PropertyName("privacy") var privacy: String = "public", // "public", "vip_only", "private"
    @get:PropertyName("bannedUserIds") @set:PropertyName("bannedUserIds") var bannedUserIds: List<String> = emptyList(),
    @get:PropertyName("kickedUserIds") @set:PropertyName("kickedUserIds") var kickedUserIds: List<String> = emptyList(),
    @get:PropertyName("adminUserIds") @set:PropertyName("adminUserIds") var adminUserIds: List<String> = emptyList(),
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("roomId") @set:PropertyName("roomId") var roomId: String = "",
    @get:PropertyName("roomName") @set:PropertyName("roomName") var roomName: String = "",
    @get:PropertyName("hostUid") @set:PropertyName("hostUid") var hostUid: String = "",
    @get:PropertyName("isLive") @set:PropertyName("isLive") var isLive: Boolean = true,
    @get:PropertyName("announcement") @set:PropertyName("announcement") var announcement: String = "",
    @get:PropertyName("isLocked") @set:PropertyName("isLocked") var isLocked: Boolean = false
) {
    val isVipRoom: Boolean get() = privacy.equals("vip_only", ignoreCase = true)
}

data class MicSeat(
    @get:PropertyName("seatIndex") @set:PropertyName("seatIndex") var seatIndex: Int = 0,         // 0 to 4 (Mic 1, Mic 2, Mic 3, Mic 4, Mic 5)
    @get:PropertyName("label") @set:PropertyName("label") var label: String = "Mic 1",            // e.g. "Host 👑", "Mic 2", "Mic 3", "Mic 4", "Mic 5"
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",                       // Occupant UID, empty if open
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",                    // Occupant display name
    @get:PropertyName("photo") @set:PropertyName("photo") var photo: String = "",                 // Occupant avatar URL
    @get:PropertyName("isMuted") @set:PropertyName("isMuted") var isMuted: Boolean = false,       // Seat mic muted
    @get:PropertyName("isLocked") @set:PropertyName("isLocked") var isLocked: Boolean = false,    // Seat locked by host
    @get:PropertyName("isTalking") @set:PropertyName("isTalking") var isTalking: Boolean = false, // Live audio speech animation
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "speaker"              // "host" or "speaker"
)

fun parseRoomFromDoc(doc: DocumentSnapshot): RoomItem {
    val id = doc.getString("roomId") ?: doc.getString("id") ?: doc.id
    val title = doc.getString("roomName") ?: doc.getString("title") ?: ""
    val emoji = doc.getString("emoji") ?: "🎙️"
    val bannerUrl = doc.getString("banner_image_url") ?: ""
    val activeCount = (doc.getLong("activeUserCount") ?: 0L).toInt()
    val hostId = doc.getString("hostUid") ?: doc.getString("hostId") ?: ""
    val hostName = doc.getString("hostName") ?: ""
    val hostPhotoUrl = doc.getString("hostPhotoUrl") ?: ""
    val category = doc.getString("category") ?: ""
    val nowPlayingTitle = doc.getString("nowPlayingTitle") ?: ""
    val nowPlayingArtist = doc.getString("nowPlayingArtist") ?: ""
    val nowPlayingUrl = doc.getString("nowPlayingUrl") ?: ""
    val nowPlayingArtworkUrl = doc.getString("nowPlayingArtworkUrl") ?: ""
    val currentPlayingTrack = doc.getString("currentPlayingTrack") ?: ""
    val playbackPosition = doc.getLong("playbackPosition") ?: 0L
    val isPlaying = doc.getBoolean("isPlaying") ?: doc.getBoolean("playing") ?: false
    val isCustom = doc.getBoolean("isCustom") ?: doc.getBoolean("custom") ?: true
    val isHostLive = doc.getBoolean("isLive") ?: doc.getBoolean("isHostLive") ?: doc.getBoolean("hostLive") ?: true
    val scheduledTime = doc.getString("scheduledTime") ?: ""
    val statusMessage = doc.getString("statusMessage") ?: ""
    val privacy = doc.getString("privacy") ?: "public"
    @Suppress("UNCHECKED_CAST")
    val bannedUserIds = (doc.get("bannedUserIds") as? List<String>) ?: emptyList()
    @Suppress("UNCHECKED_CAST")
    val kickedUserIds = (doc.get("kickedUserIds") as? List<String>) ?: emptyList()
    @Suppress("UNCHECKED_CAST")
    val adminUserIds = (doc.get("adminUserIds") as? List<String>) ?: emptyList()
    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
    val rawRoomId = doc.getString("roomId") ?: ""
    val roomId = if (rawRoomId.isNotBlank() && rawRoomId.length == 8 && rawRoomId.all { it.isDigit() }) {
        rawRoomId
    } else if (id.length == 8 && id.all { it.isDigit() }) {
        id
    } else {
        (10000000 + kotlin.math.abs(id.hashCode()) % 90000000).toString()
    }
    val roomName = doc.getString("roomName") ?: title
    val hostUid = doc.getString("hostUid") ?: hostId
    val isLive = doc.getBoolean("isLive") ?: isHostLive

    val announcement = doc.getString("announcement") ?: ""
    val isLocked = doc.getBoolean("isLocked") ?: doc.getBoolean("locked") ?: false

    return RoomItem(
        id = id,
        title = title,
        emoji = emoji,
        banner_image_url = bannerUrl,
        activeUserCount = activeCount,
        hostId = hostId,
        hostName = hostName,
        hostPhotoUrl = hostPhotoUrl,
        category = category,
        nowPlayingTitle = nowPlayingTitle,
        nowPlayingArtist = nowPlayingArtist,
        nowPlayingUrl = nowPlayingUrl,
        nowPlayingArtworkUrl = nowPlayingArtworkUrl,
        currentPlayingTrack = currentPlayingTrack,
        playbackPosition = playbackPosition,
        isPlaying = isPlaying,
        isCustom = isCustom,
        isHostLive = isHostLive,
        scheduledTime = scheduledTime,
        statusMessage = statusMessage,
        privacy = privacy,
        bannedUserIds = bannedUserIds,
        kickedUserIds = kickedUserIds,
        adminUserIds = adminUserIds,
        createdAt = createdAt,
        roomId = roomId,
        roomName = roomName,
        hostUid = hostUid,
        isLive = isLive,
        announcement = announcement,
        isLocked = isLocked
    )
}

val CATEGORY_BANNERS = mapOf(
    "vip_room" to "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1200&q=80",
    "free_tonight" to "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?auto=format&fit=crop&w=800&q=80",
    "free_tomorrow" to "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80",
    "free_weekend" to "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=800&q=80",
    "ready_travel" to "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80",
    "travel_partner" to "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
    "hangout_partner" to "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=800&q=80",
    "free_party" to "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80",
    "join_party" to "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?auto=format&fit=crop&w=800&q=80",
    "going_club" to "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=800&q=80",
    "plan_trip" to "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=800&q=80",
    "lets_movie" to "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=800&q=80",
    "lets_cafe" to "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=800&q=80",
    "lets_dinner" to "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=800&q=80"
)

fun getCategoryBanner(categoryId: String): String {
    return CATEGORY_BANNERS[categoryId] ?: "https://images.unsplash.com/photo-1545128485-c400e7702796?auto=format&fit=crop&w=800&q=80"
}

data class SoundFxItem(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String
)

data class RoomCategoryTheme(
    val categoryId: String,
    val displayName: String,
    val emoji: String,
    val consoleTitle: String,
    val consoleSubtitle: String,
    val consoleType: String,
    val seatTypeLabel: String,
    val seatEmoji: String,
    val soundEffects: List<SoundFxItem>
)

val ROOM_CATEGORY_THEMES: Map<String, RoomCategoryTheme> = mapOf(
    "vip_room" to RoomCategoryTheme(
        categoryId = "vip_room",
        displayName = "VIP Room",
        emoji = "👑",
        consoleTitle = "Royal Gramophone Console",
        consoleSubtitle = "Antique 3D Golden Gramophone • Royal Fanfare",
        consoleType = "vip_gramophone",
        seatTypeLabel = "Imperial Throne",
        seatEmoji = "👑",
        soundEffects = listOf(
            SoundFxItem("fanfare", "Fanfare", "🎺", "Royal Fanfare Trumpet"),
            SoundFxItem("hail", "Hail King", "👑", "Hail The Monarch"),
            SoundFxItem("toast", "Crystal Toast", "🥂", "Royal Champagne Toast"),
            SoundFxItem("fireworks", "Fireworks", "🎆", "Imperial Fireworks")
        )
    ),
    "lets_movie" to RoomCategoryTheme(
        categoryId = "lets_movie",
        displayName = "Lets Go For Movie",
        emoji = "🍿",
        consoleTitle = "IMAX Cinema Screen",
        consoleSubtitle = "Active Screen Projector • Cinema Surround Sound",
        consoleType = "movie_imax",
        seatTypeLabel = "IMAX Recliner",
        seatEmoji = "🍿",
        soundEffects = listOf(
            SoundFxItem("popcorn", "Popcorn", "🍿", "Movie Theater Popcorn"),
            SoundFxItem("applause", "Applause", "👏", "Audience Ovation"),
            SoundFxItem("boom", "Explosion", "💥", "Blockbuster Boom"),
            SoundFxItem("soda", "Fizzy Soda", "🥤", "Cinema Beverage")
        )
    ),
    "lets_cafe" to RoomCategoryTheme(
        categoryId = "lets_cafe",
        displayName = "Lets Go Cafe",
        emoji = "☕",
        consoleTitle = "Acoustic Guitar Lo-Fi Console",
        consoleSubtitle = "Midnight Cafe Session • Coffee Steam Visualizer",
        consoleType = "cafe_guitar",
        seatTypeLabel = "Cafe Armchair",
        seatEmoji = "☕",
        soundEffects = listOf(
            SoundFxItem("guitar", "Acoustic Strum", "🎸", "Warm Nylon Guitar Strum"),
            SoundFxItem("espresso", "Espresso", "☕", "Steaming Espresso Pour"),
            SoundFxItem("rain", "Window Rain", "🌧️", "Cozy Rain Droplets"),
            SoundFxItem("bell", "Door Chime", "🔔", "Cafe Welcome Chime")
        )
    ),
    "free_tonight" to RoomCategoryTheme(
        categoryId = "free_tonight",
        displayName = "Free Tonight",
        emoji = "🍷",
        consoleTitle = "Skyline Saxophone & Jazz Player",
        consoleSubtitle = "Rooftop Evening Vibe • Crystal Glass Jazz",
        consoleType = "skyline_jazz",
        seatTypeLabel = "Skyline Velvet Seat",
        seatEmoji = "🍷",
        soundEffects = listOf(
            SoundFxItem("sax", "Sax Solo", "🎷", "Sultry Saxophone Riff"),
            SoundFxItem("cheers", "Wine Cheers", "🥂", "Evening Wine Cheers"),
            SoundFxItem("piano", "Jazz Piano", "🎹", "Smooth Piano Chords"),
            SoundFxItem("breeze", "Night Breeze", "🌃", "Skyline Evening Breeze")
        )
    ),
    "travel_partner" to RoomCategoryTheme(
        categoryId = "travel_partner",
        displayName = "Travel Partner",
        emoji = "✈️",
        consoleTitle = "Cockpit Aviation Radar Console",
        consoleSubtitle = "30,000 Feet Cruiser • First-Class Jetsetter",
        consoleType = "aviation_radar",
        seatTypeLabel = "First-Class Recliner",
        seatEmoji = "✈️",
        soundEffects = listOf(
            SoundFxItem("chime", "Flight Chime", "🔔", "Cabin Ding-Dong Chime"),
            SoundFxItem("jet", "Jet Turbine", "✈️", "Cruising Jet Engine"),
            SoundFxItem("captain", "Captain Mic", "🎙️", "Flight Crew Notice"),
            SoundFxItem("clouds", "Turbulence", "☁️", "High Altitude Wind")
        )
    ),
    "going_club" to RoomCategoryTheme(
        categoryId = "going_club",
        displayName = "Going to Club",
        emoji = "🕺",
        consoleTitle = "Club Velocity DJ Turntable",
        consoleSubtitle = "Spinning Neon Vinyl • Cyber Nightclub",
        consoleType = "club_turntable",
        seatTypeLabel = "Cyber Club Lounge",
        seatEmoji = "🕺",
        soundEffects = listOf(
            SoundFxItem("airhorn", "Club Airhorn", "📢", "Party Siren Blast"),
            SoundFxItem("bass", "Bass Drop", "🔊", "Subwoofer Bass Punch"),
            SoundFxItem("crowd", "Crowd Roar", "🙌", "Hype Club Cheers"),
            SoundFxItem("drums", "Drum Roll", "🥁", "Build-up Roll")
        )
    ),
    "free_tomorrow" to RoomCategoryTheme(
        categoryId = "free_tomorrow",
        displayName = "Free Tomorrow",
        emoji = "🌙",
        consoleTitle = "Celestial Moon Orb Player",
        consoleSubtitle = "Glass Dome Planetarium • Midnight Blue Cosmos",
        consoleType = "moon_observatory",
        seatTypeLabel = "Observatory Pod",
        seatEmoji = "🌙",
        soundEffects = listOf(
            SoundFxItem("owl", "Night Owl", "🦉", "Gentle Forest Call"),
            SoundFxItem("star", "Shooting Star", "🌠", "Cosmic Sparkle Chime"),
            SoundFxItem("wind", "Midnight Wind", "💨", "Gentle Observatory Breeze"),
            SoundFxItem("bowl", "Celestial Bell", "🔔", "Singing Bowl Meditation")
        )
    ),
    "free_weekend" to RoomCategoryTheme(
        categoryId = "free_weekend",
        displayName = "Free in Weekend",
        emoji = "🎉",
        consoleTitle = "Tropical Sun DJ Console",
        consoleSubtitle = "Sunset Poolside Beach Club • Island Beats",
        consoleType = "sunset_beach",
        seatTypeLabel = "Poolside Cabana",
        seatEmoji = "🌴",
        soundEffects = listOf(
            SoundFxItem("wave", "Ocean Wave", "🌊", "Rhythmic Surf Crash"),
            SoundFxItem("bongo", "Bongo Drums", "🪘", "Afro-Caribbean Percussion"),
            SoundFxItem("whistle", "Party Whistle", "🥳", "Carnival Samba Whistle"),
            SoundFxItem("champagne", "Champagne Pop", "🍾", "Bottle Cork & Fizz")
        )
    ),
    "ready_travel" to RoomCategoryTheme(
        categoryId = "ready_travel",
        displayName = "Ready for Travelling",
        emoji = "🧳",
        consoleTitle = "Campfire Acoustic Radio",
        consoleSubtitle = "Alpine Expedition Camp • Woodgrain Warmth",
        consoleType = "campfire_expedition",
        seatTypeLabel = "Safari Camp Chair",
        seatEmoji = "🏕️",
        soundEffects = listOf(
            SoundFxItem("fire", "Crackling Fire", "🪵", "Warm Snapping Timber"),
            SoundFxItem("crickets", "Night Crickets", "🦗", "Wilderness Night Chorus"),
            SoundFxItem("wolf", "Wolf Howl", "🐺", "Mountain Ridge Howl"),
            SoundFxItem("strum", "Folk Strum", "🎸", "Acoustic Fingerpicking")
        )
    ),
    "hangout_partner" to RoomCategoryTheme(
        categoryId = "hangout_partner",
        displayName = "Hangout Partner",
        emoji = "🤝",
        consoleTitle = "8-Bit Gameboy Synth Console",
        consoleSubtitle = "Retro Cyber Loft • Level-Up Beats",
        consoleType = "gameboy_retro",
        seatTypeLabel = "Gamer Esports Pod",
        seatEmoji = "🎮",
        soundEffects = listOf(
            SoundFxItem("coin", "Level Up", "🪙", "8-Bit Gold Coin Chime"),
            SoundFxItem("victory", "Victory Fanfare", "🏆", "Boss Defeated Fanfare"),
            SoundFxItem("laser", "Laser Blaster", "👾", "Space Shooter Blast"),
            SoundFxItem("highfive", "High Five", "👏", "Friendship Clapping")
        )
    ),
    "free_party" to RoomCategoryTheme(
        categoryId = "free_party",
        displayName = "Free for Party",
        emoji = "🥳",
        consoleTitle = "Laser Disco Ball Soundboard",
        consoleSubtitle = "Neon Fiesta Stage • Electro Blast",
        consoleType = "neon_fiesta",
        seatTypeLabel = "Celebration Stage Pod",
        seatEmoji = "🎉",
        soundEffects = listOf(
            SoundFxItem("confetti", "Confetti Cannon", "🎊", "Explosive Confetti Pop"),
            SoundFxItem("horn", "Party Horn", "📣", "Stadium Vuvuzela Horn"),
            SoundFxItem("applause", "Stadium Applause", "👏", "Crowd Cheering Burst"),
            SoundFxItem("pop", "Champagne Cork", "🍾", "Celebration Cork Fizz")
        )
    ),
    "join_party" to RoomCategoryTheme(
        categoryId = "join_party",
        displayName = "Join Party",
        emoji = "🍸",
        consoleTitle = "Art Deco Speakeasy Jukebox",
        consoleSubtitle = "1920s Emerald Lounge • Brass Shaker Dial",
        consoleType = "speakeasy_bar",
        seatTypeLabel = "Velvet Bar Stool",
        seatEmoji = "🍸",
        soundEffects = listOf(
            SoundFxItem("shaker", "Cocktail Shaker", "🍸", "Ice & Shaker Clatter"),
            SoundFxItem("sax", "Sax Blues Solo", "🎷", "Seductive Blues Lick"),
            SoundFxItem("toast", "Crystal Toast", "🥂", "Art Deco Glass Clink"),
            SoundFxItem("ice", "Ice Rock Clink", "🧊", "Whiskey Ice Rock Tumble")
        )
    ),
    "plan_trip" to RoomCategoryTheme(
        categoryId = "plan_trip",
        displayName = "Lets Plan Holiday Trip",
        emoji = "🗺️",
        consoleTitle = "Adventure Navigation Globe",
        consoleSubtitle = "Topographic Roadtrip Radio • Highway Anthems",
        consoleType = "roadtrip_globe",
        seatTypeLabel = "Traveler Recliner",
        seatEmoji = "🧭",
        soundEffects = listOf(
            SoundFxItem("rev", "Engine Rev", "🚗", "Sporty Highway Acceleration"),
            SoundFxItem("jet", "Jet Flyby", "✈️", "High Altitude Jet Pass"),
            SoundFxItem("photo", "Camera Click", "📸", "Film Camera Shutter Flash"),
            SoundFxItem("ocean", "Seaside Horn", "🌊", "Coastal Harbor Horn")
        )
    ),
    "lets_dinner" to RoomCategoryTheme(
        categoryId = "lets_dinner",
        displayName = "Lets Go For Dinner/Lunch",
        emoji = "🍽️",
        consoleTitle = "Silver Cloche Bistro Player",
        consoleSubtitle = "Candlelit Parisian Bistro • French Romance",
        consoleType = "bistro_dining",
        seatTypeLabel = "Banquet Dining Chair",
        seatEmoji = "🍽️",
        soundEffects = listOf(
            SoundFxItem("pour", "Wine Pour", "🍷", "Swirling Wine Glass Pour"),
            SoundFxItem("toast", "Wine Toast", "🥂", "Fine Crystal Wine Clink"),
            SoundFxItem("fork", "Silver Cutlery", "🍽️", "Fine Porcelain & Silverware"),
            SoundFxItem("strings", "Violin Romance", "🎻", "Seductive Romance Chords")
        )
    )
)

fun getCategoryTheme(categoryId: String): RoomCategoryTheme {
    return ROOM_CATEGORY_THEMES[categoryId] ?: ROOM_CATEGORY_THEMES["free_tonight"]!!
}

// All hardcoded dummy rooms removed - rooms now originate 100% dynamically from Firestore
val INTENT_ROOMS_CATALOG: List<RoomItem> = emptyList()

data class SubscriptionPlanItem(
    val id: String = "",
    val title: String = "",
    val price: String = "",
    val duration: String = "",
    val tagline: String = "",
    val isPopular: Boolean = false
)

val SUBSCRIPTION_PLANS = listOf(
    SubscriptionPlanItem("weekly", "Weekly Pass", "₹79", "/ wk", "Basic Matching"),
    SubscriptionPlanItem("monthly", "Monthly Pass", "₹199", "/ mo", "Unlimited Likes"),
    SubscriptionPlanItem("platinum", "PLATINUM", "₹249", "/ mo", "Profile Boost"),
    SubscriptionPlanItem("vip", "VIP LEVEL ★", "₹399", "/ mo", "Direct Msg + Visitors", isPopular = true)
)

data class UserProfile(
    val uid: String = "",
    val userId: String = "",
    val deviceId: String = "",
    val name: String = "",
    val age: Int = 24,
    val gender: String = "Woman",
    val city: String = "Kolkata",
    val photo: String = "",
    val bio: String = "",
    val activeIntent: String = "free_tonight",
    val hobbies: String = "",
    val interests: String = "",
    val profession: String = "Designer",
    val education: String = "Graduate",
    val plan: String = "Free",
    val distanceKm: Int = 12,

    // Real-time Intent Room tracking
    val current_room: String = "",

    // Settings & Discovery attributes (Screenshots 2, 3, 4)
    val hasBio: Boolean = true,
    val lookingFor: String = "Long-term relationship",
    val openTo: String = "New connections",
    val languages: String = "English, Hindi, Bengali",
    val zodiac: String = "Leo",
    val familyPlans: String = "Open to kids",
    val communicationStyle: String = "Big time texter",
    val loveStyle: String = "Quality time",
    val pets: String = "Dog lover",
    val drinking: String = "Socially",
    val smoking: String = "No",
    val workout: String = "Active",
    val socialMedia: String = "@maya_livetogather",

    val locationName: String = "New Town, West Bengal",
    val latitude: Double = 22.5726,
    val longitude: Double = 88.3639,
    val isLocationSet: Boolean = false,
    val maxDistanceKm: Int = 53,
    val showFurtherAway: Boolean = true,
    val interestedIn: String = "Women",
    val minAge: Int = 21,
    val maxAge: Int = 29,
    val showOutsideAge: Boolean = true,
    val visibilityMode: String = "Standard", // "Standard" or "Incognito"
    val enableDiscovery: Boolean = true,
    val myMove: Boolean = false,
    val photoVerifiedChat: Boolean = false,

    val blockedUserIds: List<String> = emptyList(),
    val followerCount: Int = 0,
    val followingCount: Int = 0,

    // Daily swipe limits & App Launch 7-Day Host Trial
    val dailySwipeCount: Int = 0,
    val lastSwipeDate: String = "",
    val hostTrialStartDate: Long = 0L,

    // Diamond Economy & Avatar Frames System
    val diamonds: Long = 0L,
    val coins: Long = 0L,
    val activeFrame: String = "",
    val ownedFrames: List<String> = emptyList(),
    val frameExpiries: Map<String, Long> = emptyMap(),

    // First-Time Sign Up Welcome Bonus & 7-Day VIP 1 Trial
    val vipTrialStartDate: Long = 0L,
    val vipTrialExpiryDate: Long = 0L,
    val hasClaimedWelcomeBonus: Boolean = false,
    val signupIpAddress: String = "",

    // Administration & Role Control
    val isAdmin: Boolean = false,

    // Suspension & Moderation Control
    val isSuspended: Boolean = false,
    val suspensionExpiry: Long = 0L,
    val suspensionReason: String = ""
) {
    fun isPaidSubscriber(): Boolean {
        return plan.isNotBlank() && !plan.equals("Free", ignoreCase = true)
    }

    fun isVipOrPlatinum(): Boolean {
        return plan.contains("VIP", ignoreCase = true) || plan.contains("Platinum", ignoreCase = true)
    }

    fun isVipTrialActive(): Boolean {
        if (vipTrialExpiryDate == 0L) return false
        return System.currentTimeMillis() <= vipTrialExpiryDate
    }

    fun getVipTrialDaysLeft(): Int {
        if (vipTrialExpiryDate == 0L) return 0
        val remainingMs = vipTrialExpiryDate - System.currentTimeMillis()
        if (remainingMs <= 0L) return 0
        val days = (remainingMs / (1000L * 60 * 60 * 24)).toInt()
        return maxOf(1, days + 1)
    }

    fun isHostTrialActive(): Boolean {
        if (hostTrialStartDate == 0L) return true
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000L
        return (System.currentTimeMillis() - hostTrialStartDate) < sevenDaysMs
    }

    fun getHostTrialDaysLeft(): Int {
        if (hostTrialStartDate == 0L) return 7
        val elapsedDays = (System.currentTimeMillis() - hostTrialStartDate) / (1000L * 60 * 60 * 24)
        return maxOf(0, (7 - elapsedDays).toInt())
    }

    fun canHostRoom(): Boolean {
        return isVipOrPlatinum() || isHostTrialActive() || isVipTrialActive()
    }

    fun canSwipe(todayDate: String): Boolean {
        if (isPaidSubscriber() || isVipTrialActive()) return true
        if (lastSwipeDate != todayDate) return true
        return dailySwipeCount < 15
    }

    fun getVipLevel(): Int {
        val isTrialActive = isVipTrialActive()
        val frame = AVATAR_FRAMES_CATALOG.find { it.id == activeFrame }
        if (frame != null && frame.vipLevel > 0) {
            if (frame.vipLevel == 1 && vipTrialExpiryDate > 0L && !isTrialActive && !isPaidSubscriber()) {
                // Trial expired and user did not buy a permanent subscription
            } else {
                return frame.vipLevel
            }
        }
        if (plan.contains("VIP 6", ignoreCase = true)) return 6
        if (plan.contains("VIP 5", ignoreCase = true)) return 5
        if (plan.contains("VIP 4", ignoreCase = true)) return 4
        if (plan.contains("VIP 3", ignoreCase = true)) return 3
        if (plan.contains("VIP 2", ignoreCase = true)) return 2
        if (plan.contains("VIP", ignoreCase = true)) return 1
        if (plan.contains("Platinum", ignoreCase = true)) return 2
        if (isTrialActive) return 1
        return 0
    }
}

fun parseUserProfileFromDoc(doc: DocumentSnapshot): UserProfile {
    val uid = doc.getString("uid") ?: doc.id
    val rawUserId = doc.getString("userId") ?: doc.getString("user_id") ?: ""
    val userId = if (rawUserId.isNotBlank() && rawUserId.length == 8 && rawUserId.all { it.isDigit() }) {
        rawUserId
    } else if (uid.length == 8 && uid.all { it.isDigit() }) {
        uid
    } else {
        (10000000 + kotlin.math.abs(uid.hashCode()) % 90000000).toString()
    }
    val deviceId = doc.getString("deviceId") ?: ""
    val name = doc.getString("name") ?: ""
    val age = (doc.getLong("age") ?: 24L).toInt()
    val gender = doc.getString("gender") ?: "Woman"
    val city = doc.getString("city") ?: "Kolkata"
    val photo = doc.getString("photo") ?: ""
    val bio = doc.getString("bio") ?: ""
    val activeIntent = doc.getString("activeIntent") ?: "free_tonight"
    val hobbies = doc.getString("hobbies") ?: ""
    val interests = doc.getString("interests") ?: ""
    val profession = doc.getString("profession") ?: "Designer"
    val education = doc.getString("education") ?: "Graduate"
    val plan = doc.getString("plan") ?: "Free"
    val distanceKm = (doc.getLong("distanceKm") ?: 12L).toInt()
    val current_room = doc.getString("current_room") ?: ""
    val hasBio = doc.getBoolean("hasBio") ?: true
    val lookingFor = doc.getString("lookingFor") ?: "Long-term relationship"
    val openTo = doc.getString("openTo") ?: "New connections"
    val languages = doc.getString("languages") ?: "English, Hindi, Bengali"
    val zodiac = doc.getString("zodiac") ?: "Leo"
    val familyPlans = doc.getString("familyPlans") ?: "Open to kids"
    val communicationStyle = doc.getString("communicationStyle") ?: "Big time texter"
    val loveStyle = doc.getString("loveStyle") ?: "Quality time"
    val pets = doc.getString("pets") ?: "Dog lover"
    val drinking = doc.getString("drinking") ?: "Socially"
    val smoking = doc.getString("smoking") ?: "No"
    val workout = doc.getString("workout") ?: "Active"
    val socialMedia = doc.getString("socialMedia") ?: "@maya_livetogather"
    val locationName = doc.getString("locationName") ?: "New Town, West Bengal"
    val latitude = doc.getDouble("latitude") ?: 22.5726
    val longitude = doc.getDouble("longitude") ?: 88.3639
    val isLocationSet = doc.getBoolean("isLocationSet") ?: doc.getBoolean("locationSet") ?: false
    val maxDistanceKm = (doc.getLong("maxDistanceKm") ?: 53L).toInt()
    val showFurtherAway = doc.getBoolean("showFurtherAway") ?: true
    val interestedIn = doc.getString("interestedIn") ?: "Everyone"
    val minAge = (doc.getLong("minAge") ?: 21L).toInt()
    val maxAge = (doc.getLong("maxAge") ?: 29L).toInt()
    val showOutsideAge = doc.getBoolean("showOutsideAge") ?: true
    val visibilityMode = doc.getString("visibilityMode") ?: "Standard"
    val enableDiscovery = doc.getBoolean("enableDiscovery") ?: true
    val myMove = doc.getBoolean("myMove") ?: false
    val photoVerifiedChat = doc.getBoolean("photoVerifiedChat") ?: false
    @Suppress("UNCHECKED_CAST")
    val blockedUserIds = (doc.get("blockedUserIds") as? List<String>) ?: emptyList()
    val followerCount = (doc.getLong("followerCount") ?: 0L).toInt()
    val followingCount = (doc.getLong("followingCount") ?: 0L).toInt()
    val dailySwipeCount = (doc.getLong("dailySwipeCount") ?: 0L).toInt()
    val lastSwipeDate = doc.getString("lastSwipeDate") ?: ""
    val hostTrialStartDate = doc.getLong("hostTrialStartDate") ?: 0L
    val diamonds = doc.getLong("diamonds") ?: 0L
    val coins = doc.getLong("coins") ?: diamonds
    val activeFrame = doc.getString("activeFrame") ?: ""
    @Suppress("UNCHECKED_CAST")
    val ownedFrames = (doc.get("ownedFrames") as? List<String>) ?: emptyList()
    val vipTrialStartDate = doc.getLong("vipTrialStartDate") ?: 0L
    val vipTrialExpiryDate = doc.getLong("vipTrialExpiryDate") ?: 0L
    val hasClaimedWelcomeBonus = doc.getBoolean("hasClaimedWelcomeBonus") ?: false
    val signupIpAddress = doc.getString("signupIpAddress") ?: ""
    val isAdmin = doc.getBoolean("isAdmin") ?: false

    @Suppress("UNCHECKED_CAST")
    val rawExpiries = doc.get("frameExpiries") as? Map<String, Any>
    val frameExpiries: Map<String, Long> = rawExpiries?.mapNotNull { (k, v) ->
        val l = (v as? Number)?.toLong()
        if (l != null) k to l else null
    }?.toMap() ?: emptyMap()
    val isSuspended = doc.getBoolean("isSuspended") ?: false
    val suspensionExpiry = doc.getLong("suspensionExpiry") ?: 0L
    val suspensionReason = doc.getString("suspensionReason") ?: ""

    return UserProfile(
        uid = uid,
        userId = userId,
        deviceId = deviceId,
        name = name,
        age = age,
        gender = gender,
        city = city,
        photo = photo,
        bio = bio,
        activeIntent = activeIntent,
        hobbies = hobbies,
        interests = interests,
        profession = profession,
        education = education,
        plan = plan,
        distanceKm = distanceKm,
        current_room = current_room,
        hasBio = hasBio,
        lookingFor = lookingFor,
        openTo = openTo,
        languages = languages,
        zodiac = zodiac,
        familyPlans = familyPlans,
        communicationStyle = communicationStyle,
        loveStyle = loveStyle,
        pets = pets,
        drinking = drinking,
        smoking = smoking,
        workout = workout,
        socialMedia = socialMedia,
        locationName = locationName,
        latitude = latitude,
        longitude = longitude,
        isLocationSet = isLocationSet,
        maxDistanceKm = maxDistanceKm,
        showFurtherAway = showFurtherAway,
        interestedIn = interestedIn,
        minAge = minAge,
        maxAge = maxAge,
        showOutsideAge = showOutsideAge,
        visibilityMode = visibilityMode,
        enableDiscovery = enableDiscovery,
        myMove = myMove,
        photoVerifiedChat = photoVerifiedChat,
        blockedUserIds = blockedUserIds,
        followerCount = followerCount,
        followingCount = followingCount,
        dailySwipeCount = dailySwipeCount,
        lastSwipeDate = lastSwipeDate,
        hostTrialStartDate = hostTrialStartDate,
        diamonds = diamonds,
        coins = coins,
        activeFrame = activeFrame,
        ownedFrames = ownedFrames,
        frameExpiries = frameExpiries,
        vipTrialStartDate = vipTrialStartDate,
        vipTrialExpiryDate = vipTrialExpiryDate,
        hasClaimedWelcomeBonus = hasClaimedWelcomeBonus,
        signupIpAddress = signupIpAddress,
        isAdmin = isAdmin,
        isSuspended = isSuspended,
        suspensionExpiry = suspensionExpiry,
        suspensionReason = suspensionReason
    )
}

fun saveProfileToPrefs(prefs: SharedPreferences, user: UserProfile) {
    prefs.edit()
        .putString("user_uid", user.uid)
        .putString("profile_user_id", user.userId)
        .putString("profile_device_id", user.deviceId)
        .putString("profile_name", user.name)
        .putInt("profile_age", user.age)
        .putString("profile_gender", user.gender)
        .putString("profile_city", user.city)
        .putString("profile_photo", user.photo)
        .putString("profile_active_intent", user.activeIntent)
        .putString("profile_bio", user.bio)
        .putString("profile_hobbies", user.hobbies)
        .putString("profile_interests", user.interests)
        .putString("profile_profession", user.profession)
        .putString("profile_education", user.education)
        .putString("profile_plan", user.plan)
        .putString("profile_location", user.locationName)
        .putString("profile_latitude", user.latitude.toString())
        .putString("profile_longitude", user.longitude.toString())
        .putBoolean("profile_is_location_set", user.isLocationSet)
        .putInt("profile_max_distance", user.maxDistanceKm)
        .putBoolean("profile_show_further", user.showFurtherAway)
        .putString("profile_interested_in", user.interestedIn)
        .putInt("profile_min_age", user.minAge)
        .putInt("profile_max_age", user.maxAge)
        .putString("profile_current_room", user.current_room)
        .putInt("profile_daily_swipe_count", user.dailySwipeCount)
        .putString("profile_last_swipe_date", user.lastSwipeDate)
        .putLong("profile_host_trial_start", user.hostTrialStartDate)
        .putString("profile_active_frame", user.activeFrame)
        .putStringSet("profile_owned_frames", user.ownedFrames.toSet())
        .putLong("profile_diamonds", user.diamonds)
        .putLong("profile_coins", user.coins)
        .putLong("profile_vip_trial_start", user.vipTrialStartDate)
        .putLong("profile_vip_trial_expiry", user.vipTrialExpiryDate)
        .putBoolean("profile_has_claimed_welcome_bonus", user.hasClaimedWelcomeBonus)
        .putString("profile_signup_ip", user.signupIpAddress)
        .putBoolean("profile_is_admin", user.isAdmin)
        .putBoolean("profile_is_suspended", user.isSuspended)
        .putLong("profile_suspension_expiry", user.suspensionExpiry)
        .putString("profile_suspension_reason", user.suspensionReason)
        .apply()
}

data class FollowerRecord(
    val uid: String = "",
    val name: String = "",
    val photo: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class GiftEvent(
    val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val giftId: String = "",
    val giftName: String = "",
    val giftEmoji: String = "🎁",
    val cost: Long = 0L,
    val animationType: String = "car", // "car", "lion", "dragon", "rocket", "jet", "yacht", "roses", "castle", "crown"
    val timestamp: Long = System.currentTimeMillis()
)

data class VideoGiftItem(
    val id: String,
    val name: String,
    val emoji: String,
    val cost: Long,
    val animationType: String,
    val description: String,
    val rawResId: Int = 0,
    val thumbnailResId: Int = 0
)

val VIDEO_GIFT_CATALOG = listOf(
    VideoGiftItem("lamborghini_mp4", "Cyber Lamborghini HD", "🏎️", 20000L, "video_mp4", "Real HD Lamborghini drift video overlay", R.raw.lamborgini_car, R.drawable.thumb_lamborgini_car),
    VideoGiftItem("king_entry_mp4", "King Entry Coronation", "👑", 50000L, "video_mp4", "Royal King Coronation HD video overlay", R.raw.king_entry, R.drawable.thumb_king_entry),
    VideoGiftItem("rose_storm", "Rose Storm", "🌹", 5000L, "roses", "Romantic cascading rose petals shower"),
    VideoGiftItem("supercar", "Cyber Supercar", "🏎️", 10000L, "car", "Ultra luxury roaring sports car drift"),
    VideoGiftItem("roaring_lion", "Roaring Lion King", "🦁", 15000L, "lion", "Fierce majestic lion with solar shockwave"),
    VideoGiftItem("rocket", "Galactic Rocket", "🚀", 20000L, "rocket", "Cosmic space blastoff across the sky"),
    VideoGiftItem("luxury_jet", "Private Falcon Jet", "✈️", 25000L, "jet", "Supersonic luxury jet crossing screen"),
    VideoGiftItem("golden_palace", "Imperial Palace", "🏰", 35000L, "castle", "Majestic golden palace fireworks"),
    VideoGiftItem("luxury_yacht", "Billionaire Yacht", "🛥️", 40000L, "yacht", "High-seas mega luxury yacht cruise"),
    VideoGiftItem("dragon", "Golden Dragon", "🐉", 50000L, "dragon", "Legendary flaming golden dragon roar"),
    VideoGiftItem("crown_coronation", "Imperial Crown", "👑", 60000L, "crown", "Royal diamond crown coronation shower")
)

data class SwipeRecord(
    val fromUserId: String = "",
    val toUserId: String = "",
    val action: String = "", // "like", "pass", "superstar"
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MatchItem(
    val matchId: String = "",
    val users: List<String> = emptyList(),
    val matchedIntent: String = "",
    val lastMessage: String = "It's a Match! Say hello.",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class CommentItem(
    val id: String = "",
    val momentId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MomentItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val mediaUrl: String = "",
    val mediaUrls: List<String> = emptyList(),
    val caption: String = "",
    val likes: Int = 0,
    val likedByUsers: List<String> = emptyList(),
    val commentCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String = "",
    val targetUserId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val type: String = "like", // "like", "superstar", "match", "message"
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val roomId: String = ""
)

data class RoomMember(
    val uid: String = "",
    val userId: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val role: String = "listener", // "host" or "listener"
    val tier: String = "Standard" // "VIP", "Platinum", "Standard"
)

data class ReportTicket(
    val reportId: String = "",
    val reportedUid: String = "",
    val reportedUserId: String = "",
    val reportedName: String = "",
    val reportedPhoto: String = "",
    val reporterUid: String = "",
    val reporterUserId: String = "",
    val reporterName: String = "",
    val reason: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // PENDING, RESOLVED, DISMISSED
)

data class MessageRequest(
    val requestId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val targetId: String = "",
    val targetName: String = "",
    val message: String = "",
    val status: String = "pending", // "pending", "accepted", "rejected"
    val timestamp: Long = System.currentTimeMillis()
)

data class LiveRoomComment(
    val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val tier: String = "Standard",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class HangoutTrack(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val category: String = "Lo-Fi Chill",
    val url: String = "",
    val artworkUrl: String = "",
    val durationSec: Int = 0
)

val CURATED_HANGOUT_TRACKS = listOf(
    // 1. Lo-Fi Chill & Hangout Vibes (Guaranteed High-Reliability MP3 Stream)
    HangoutTrack(
        id = "track_1",
        title = "Lofi Midnight Reverie",
        artist = "Chillhop Sessions",
        category = "Lo-Fi Chill",
        url = "https://freepd.com/music/City%20Sunshine.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?auto=format&fit=crop&w=500&q=80",
        durationSec = 205
    ),
    HangoutTrack(
        id = "track_2",
        title = "Coffee Break Chill Cafe",
        artist = "FM STAR Lounge",
        category = "Lo-Fi Chill",
        url = "https://freepd.com/music/Sweet%20Treat.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=500&q=80",
        durationSec = 192
    ),

    // 2. Cafe Jazz & Lounge (Full Length)
    HangoutTrack(
        id = "track_3",
        title = "Soulful Jazz Whispers",
        artist = "Velvet Sax Quartet",
        category = "Cafe Jazz",
        url = "https://freepd.com/music/Bossa%20Antigua.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?auto=format&fit=crop&w=500&q=80",
        durationSec = 246
    ),
    HangoutTrack(
        id = "track_4",
        title = "Gentle Sunset Bossa",
        artist = "Lounge Collective",
        category = "Cafe Jazz",
        url = "https://freepd.com/music/Late%20Night%20Coffee.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=500&q=80",
        durationSec = 228
    ),

    // 3. Cafe Jazz & Lounge (Full Length)
    HangoutTrack(
        id = "track_5",
        title = "Soulful Jazz Whispers",
        artist = "Velvet Sax Quartet",
        category = "Cafe Jazz",
        url = "https://freepd.com/music/Bossa%20Antigua.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?auto=format&fit=crop&w=500&q=80",
        durationSec = 246
    ),
    HangoutTrack(
        id = "track_6",
        title = "Gentle Sunset Bossa",
        artist = "Lounge Collective",
        category = "Cafe Jazz",
        url = "https://freepd.com/music/Late%20Night%20Coffee.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=500&q=80",
        durationSec = 228
    ),

    // 4. Party Beats & Punjabi (Full Length)
    HangoutTrack(
        id = "track_7",
        title = "Sauda Khara Khara",
        artist = "Diljit Dosanjh & Sukhbir",
        category = "Party Beats",
        url = "https://aac.saavncdn.com/393/454ca18e69d76378e9bf845ba0aaefb4_160.mp4",
        artworkUrl = "https://c.saavncdn.com/393/Good-Newwz-Hindi-2019-20200810145942-500x500.jpg",
        durationSec = 224
    ),
    HangoutTrack(
        id = "track_8",
        title = "G.O.A.T Party Anthem",
        artist = "Diljit Dosanjh",
        category = "Party Beats",
        url = "https://aac.saavncdn.com/023/1ce0bb2189d701ee6ef413f13bb10427_160.mp4",
        artworkUrl = "https://c.saavncdn.com/023/G-O-A-T-Punjabi-2020-20200729181146-500x500.jpg",
        durationSec = 219
    ),

    // 5. EDM Club
    HangoutTrack(
        id = "track_9",
        title = "Faded Midnight Pulse",
        artist = "Cyber Club Project",
        category = "EDM Club",
        url = "https://freepd.com/music/Digital%20Solitude.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=500&q=80",
        durationSec = 210
    ),
    HangoutTrack(
        id = "track_10",
        title = "Electronic Horizons",
        artist = "Bassline Pulse",
        category = "EDM Club",
        url = "https://freepd.com/music/Galactic%20Rapids.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=500&q=80",
        durationSec = 198
    )
)

data class RoomThemePreset(
    val id: String,
    val name: String,
    val imageUrl: String
)

val ROOM_THEME_PRESETS = listOf(
    RoomThemePreset("theme_dubai_marina", "🏙️ Dubai Marina Night", "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_paris_eiffel", "🗼 Paris Eiffel Skyline", "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_las_vegas", "🎰 Las Vegas Neon Club", "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_goa_beach", "🏖️ Goa Beach Sunset", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_ibiza_club", "🎧 Ibiza Party Lounge", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_tokyo_cyber", "🌃 Tokyo Cyberpunk", "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_bali_villa", "🌴 Bali Tropical Villa", "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_santorini", "🌅 Santorini Caldera", "https://images.unsplash.com/photo-1570077188670-e3a8d69ac5ff?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_swiss_chalet", "🏔️ Swiss Alps Chalet", "https://images.unsplash.com/photo-1502784444187-359ac186c5bb?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_new_york", "🗽 NYC Times Square", "https://images.unsplash.com/photo-1534430480872-3498386e7856?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_london_bridge", "🌉 London Tower Bridge", "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_maldives_villa", "🌊 Maldives Ocean Villa", "https://images.unsplash.com/photo-1514282401047-d79a71a590e8?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_monaco_yacht", "🛥️ Monaco VIP Yacht", "https://images.unsplash.com/photo-1569263979104-865ab7cd8d17?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_rio_carnival", "🎭 Rio Carnival Fiesta", "https://images.unsplash.com/photo-1517457373958-b7bdd4587205?auto=format&fit=crop&w=1080&q=80"),
    RoomThemePreset("theme_sydney_opera", "🎆 Sydney Opera Harbour", "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=1080&q=80")
)

val DEFAULT_MOMENTS_SEED = listOf(
    MomentItem(
        id = "moment_seed_1",
        userId = "host_maya",
        userName = "Maya",
        userPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
        mediaUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=600&q=80",
        caption = "Late night coffee & planning the next weekend trip! ☕🚗",
        likes = 12,
        likedByUsers = listOf("user_1", "user_2"),
        commentCount = 3,
        timestamp = System.currentTimeMillis() - 3600000L
    ),
    MomentItem(
        id = "moment_seed_2",
        userId = "user_rahul",
        userName = "Rahul",
        userPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
        mediaUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=800&q=80",
        caption = "Concert vibes tonight! Who else is around? 🎶🎸",
        likes = 8,
        likedByUsers = listOf("user_3"),
        commentCount = 1,
        timestamp = System.currentTimeMillis() - 7200000L
    ),
    MomentItem(
        id = "moment_seed_3",
        userId = "user_priya",
        userName = "Priya",
        userPhoto = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=80",
        mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
        caption = "Sunset by the bay 🌅 Peaceful evenings hit differently.",
        likes = 24,
        likedByUsers = listOf("user_1", "user_4", "user_5"),
        commentCount = 5,
        timestamp = System.currentTimeMillis() - 14400000L
    )
)

/**
 * Calculates real-world geographic distance between two coordinates in kilometers using Haversine formula.
 */
fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
    if ((lat1 == 0.0 && lon1 == 0.0) || (lat2 == 0.0 && lon2 == 0.0)) return 1
    val earthRadius = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return Math.round(earthRadius * c).toInt().coerceAtLeast(1)
}

val CITY_COORDINATES = mapOf(
    "Kolkata" to Pair(22.5726, 88.3639),
    "Howrah" to Pair(22.5958, 88.2636),
    "Darjeeling" to Pair(27.0410, 88.2663),
    "Siliguri" to Pair(26.7271, 88.4354),
    "Kalimpong" to Pair(27.0667, 88.4667),
    "Gangtok" to Pair(27.3389, 88.6065),
    "Durgapur" to Pair(23.5204, 87.3119),
    "Asansol" to Pair(23.6739, 86.9524),
    "Ranchi" to Pair(23.3441, 85.3096),
    "Patna" to Pair(25.5941, 85.1376),
    "Bhubaneswar" to Pair(20.2961, 85.8245),
    "Guwahati" to Pair(26.1445, 91.7362),
    "Shillong" to Pair(25.5788, 91.8933),
    "Mumbai" to Pair(19.0760, 72.8777),
    "Delhi" to Pair(28.6139, 77.2090),
    "Bangalore" to Pair(12.9716, 77.5946),
    "Bengaluru" to Pair(12.9716, 77.5946),
    "Pune" to Pair(18.5204, 73.8567),
    "Hyderabad" to Pair(17.3850, 78.4867),
    "Chennai" to Pair(13.0827, 80.2707),
    "Ahmedabad" to Pair(23.0225, 72.5714),
    "Jaipur" to Pair(26.9124, 75.7873),
    "Lucknow" to Pair(26.8467, 80.9462),
    "Chandigarh" to Pair(30.7333, 76.7794),
    "Goa" to Pair(15.2993, 74.1240)
)

val POPULAR_AREAS_BY_CITY = mapOf(
    "Kolkata" to listOf("New Town", "Salt Lake", "Park Street", "Ballygunge", "Alipore", "Dum Dum"),
    "Howrah" to listOf("Shibpur", "Belur", "Bally", "Salkia", "Mandirtala"),
    "Darjeeling" to listOf("Mall Road", "Chauk Bazaar", "Batasia", "Lebong", "Ghum"),
    "Siliguri" to listOf("Sevoke Road", "Pradhan Nagar", "Matigara", "Hakim Para", "College Para"),
    "Kalimpong" to listOf("Rishi Road", "Deolo Hill", "Upper Cart Road", "Tenth Mile"),
    "Gangtok" to listOf("MG Marg", "Deorali", "Tadong", "Development Area"),
    "Durgapur" to listOf("City Centre", "Benachity", "Bidhannagar", "Steel Township"),
    "Asansol" to listOf("Ushagram", "Burnpur", "Kalyanpur", "Sen Raleigh"),
    "Ranchi" to listOf("Morabadi", "Lalpur", "Doranda", "Kanke Road", "Hinoo"),
    "Patna" to listOf("Kankarbagh", "Boring Road", "Bailey Road", "Fraser Road", "Patliputra"),
    "Bhubaneswar" to listOf("Saheed Nagar", "Patia", "Nayapalli", "Jayadev Vihar", "Khandagiri"),
    "Guwahati" to listOf("GS Road", "Zoo Road", "Ulubari", "Beltola", "Christian Basti"),
    "Shillong" to listOf("Police Bazar", "Laitumkhrah", "Labal", "Mawlai"),
    "Mumbai" to listOf("Bandra West", "Andheri", "Juhu", "Colaba", "Powai", "Thane"),
    "Delhi" to listOf("Connaught Place", "Hauz Khas", "Saket", "Dwarka", "Vasant Kunj", "Noida"),
    "Bangalore" to listOf("Koramangala", "Indiranagar", "HSR Layout", "Whitefield", "JP Nagar"),
    "Pune" to listOf("Koregaon Park", "Viman Nagar", "Baner", "Kothrud", "Aundh", "Hinjawadi"),
    "Hyderabad" to listOf("Jubilee Hills", "Banjara Hills", "Hitec City", "Gachibowli", "Madhapur")
)

val DEFAULT_DUMMY_PROFILES = listOf(
    // 7 Women Profiles (West Bengal & Neighboring States: Bengali, Nepali, Regional)
    UserProfile(
        uid = "dummy_rituparna",
        name = "Rituparna Mukherjee",
        age = 24,
        gender = "Woman",
        city = "Kolkata",
        locationName = "Salt Lake, Kolkata",
        latitude = 22.5868,
        longitude = 88.4178,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?auto=format&fit=crop&w=800&q=80",
        bio = "Classical Kathak dancer & Rabindra Sadan tea addict ☕ Monsoon poetry, vintage books, and acoustic rhythms.",
        profession = "Digital Marketer & Kathak Artist",
        education = "Masters in Mass Communication",
        hobbies = "Kathak, Pottery, Vinyl Records, Street Photography",
        interests = "Performing Arts, Culture, Cafes",
        activeIntent = "free_tonight",
        plan = "VIP LEVEL ★",
        followerCount = 312,
        followingCount = 48,
        lookingFor = "Meaningful friendships & chai dates",
        languages = "Bengali, Hindi, English",
        interestedIn = "Men",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_tenzing",
        name = "Tenzing Lhamo",
        age = 23,
        gender = "Woman",
        city = "Darjeeling",
        locationName = "Mall Road, Darjeeling",
        latitude = 27.0410,
        longitude = 88.2663,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80",
        bio = "Darjeeling tea lover, indie acoustic guitarist, and mountain hiker 🏔️ Let's watch sunrise at Tiger Hill.",
        profession = "Cafe Manager & Musician",
        education = "B.A. English Literature",
        hobbies = "Acoustic Guitar, Mountain Trekking, Baking, Songwriting",
        interests = "Indie Folk, Tea Culture, Mountains",
        activeIntent = "free_weekend",
        plan = "VIP LEVEL ★",
        followerCount = 285,
        followingCount = 42,
        lookingFor = "Music collabs & mountain vibes",
        languages = "Nepali, Hindi, English",
        interestedIn = "Men",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_pooja",
        name = "Pooja Dasgupta",
        age = 25,
        gender = "Woman",
        city = "Kolkata",
        locationName = "Ballygunge, Kolkata",
        latitude = 22.5280,
        longitude = 88.3655,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=800&q=80",
        bio = "Architect designing cozy heritage spaces. Street photography, jazz records, and Kolkata phuchka lover 🎷",
        profession = "Heritage Architect",
        education = "B.Arch Jadavpur University",
        hobbies = "Watercolor Painting, Cycling, Architecture, Cafes",
        interests = "Design, Heritage, Jazz, Art",
        activeIntent = "free_tomorrow",
        plan = "VIP LEVEL ★",
        followerCount = 420,
        followingCount = 65,
        lookingFor = "Creative minds & deep conversations",
        languages = "Bengali, English, Hindi",
        interestedIn = "Men",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_anamika",
        name = "Anamika Chettri",
        age = 22,
        gender = "Woman",
        city = "Siliguri",
        locationName = "Sevoke Road, Siliguri",
        latitude = 26.7271,
        longitude = 88.4354,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
        bio = "Botanical illustrator 🌸 Exploring Himalayan forest trails and baking sourdough on rainy afternoons.",
        profession = "Visual Artist & Freelancer",
        education = "Fine Arts Graduate",
        hobbies = "Botanical Art, Camping, Baking, Reading",
        interests = "Nature, Illustration, Indie Music",
        activeIntent = "lets_cafe",
        plan = "VIP LEVEL ★",
        followerCount = 194,
        followingCount = 31,
        lookingFor = "Road trips & quiet moments",
        languages = "Nepali, Bengali, English",
        interestedIn = "Men",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_deboshree",
        name = "Deboshree Banerjee",
        age = 26,
        gender = "Woman",
        city = "Kolkata",
        locationName = "New Town, Kolkata",
        latitude = 22.5850,
        longitude = 88.4600,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=800&q=80",
        bio = "Fintech analyst by day, theater artist on weekends 🎭 Satyajit Ray cinephile & rainy drive companion.",
        profession = "Product Manager",
        education = "MBA Finance",
        hobbies = "Theater Acting, Screenwriting, Badminton, Travel",
        interests = "Cinema, Theater, Tech, Driving",
        activeIntent = "ready_travel",
        plan = "VIP LEVEL ★",
        followerCount = 378,
        followingCount = 59,
        lookingFor = "Intellectual chats & romance",
        languages = "Bengali, Hindi, English",
        interestedIn = "Men",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_pema",
        name = "Pema Doma Sherpa",
        age = 23,
        gender = "Woman",
        city = "Gangtok",
        locationName = "MG Marg, Gangtok",
        latitude = 27.3389,
        longitude = 88.6065,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80",
        bio = "Backpacker, eco-tour guide & noodle enthusiast 🍜 High mountain passes, foggy evenings, and stargazing.",
        profession = "Adventure Travel Coordinator",
        education = "Tourism & Hospitality Management",
        hobbies = "Highland Trekking, Wildlife Photography, Ukulele",
        interests = "Travel, Himalayan Ecology, Stargazing",
        activeIntent = "travel_partner",
        plan = "VIP LEVEL ★",
        followerCount = 265,
        followingCount = 38,
        lookingFor = "Travel buddies & genuine hearts",
        languages = "Nepali, Tibetan, Hindi, English",
        interestedIn = "Men",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_ipsita",
        name = "Ipsita Ray",
        age = 24,
        gender = "Woman",
        city = "Bhubaneswar",
        locationName = "Saheed Nagar, Bhubaneswar",
        latitude = 20.2961,
        longitude = 85.8245,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=80",
        bio = "Ceramic sculptor & beach sunset watcher 🌊 Coffee connoisseur looking for spontaneous adventures.",
        profession = "Ceramic Designer & Stylist",
        education = "B.Des Lifestyle Accessories",
        hobbies = "Pottery, Sculpting, Beach Jogging, Scuba",
        interests = "Art, Coastal Life, Design, Spontaneous Trips",
        activeIntent = "free_party",
        plan = "VIP LEVEL ★",
        followerCount = 432,
        followingCount = 74,
        lookingFor = "Artistic souls & dating",
        languages = "Odia, Hindi, English, Bengali",
        interestedIn = "Men",
        showFurtherAway = true,
        showOutsideAge = true
    ),

    // 8 Men Profiles (West Bengal & Neighboring States: Bengali, Nepali, Regional)
    UserProfile(
        uid = "dummy_subhojit",
        name = "Subhojit Ganguly",
        age = 26,
        gender = "Man",
        city = "Kolkata",
        locationName = "Park Street, Kolkata",
        latitude = 22.5510,
        longitude = 88.3524,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=800&q=80",
        bio = "Bassist in an indie alt-rock band 🎸 College Street book hunter, vinyl lover, and midnight espresso fan.",
        profession = "Sound Engineer & Session Musician",
        education = "Diploma in Audio Engineering",
        hobbies = "Bass Guitar, Live Sound Mixing, Chess, Vinyl Collecting",
        interests = "Rock, Jazz, Indie Music, Nightlife",
        activeIntent = "free_tonight",
        plan = "VIP LEVEL ★",
        followerCount = 512,
        followingCount = 88,
        lookingFor = "Music lovers & concert buddies",
        languages = "Bengali, Hindi, English",
        interestedIn = "Women",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_karan",
        name = "Karan Thapa",
        age = 25,
        gender = "Man",
        city = "Darjeeling",
        locationName = "Chauk Bazaar, Darjeeling",
        latitude = 27.0360,
        longitude = 88.2627,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=800&q=80",
        bio = "Mountain biker & specialty coffee roaster ☕ Born in the mist, chasing high-altitude summits & acoustic tunes.",
        profession = "Coffee Roaster & Barista",
        education = "Degree in Hospitality",
        hobbies = "Downhill Biking, Pour-over Coffee, Acoustic Guitar",
        interests = "Mountain Sports, Coffee, Outdoor Living",
        activeIntent = "free_weekend",
        plan = "VIP LEVEL ★",
        followerCount = 389,
        followingCount = 45,
        lookingFor = "Adventure partner & coffee dates",
        languages = "Nepali, Hindi, English",
        interestedIn = "Women",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_anirban",
        name = "Anirban Chatterjee",
        age = 27,
        gender = "Man",
        city = "Howrah",
        locationName = "Shibpur, Howrah",
        latitude = 22.5697,
        longitude = 88.3180,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=800&q=80",
        bio = "Android developer & weekend riverfront cyclist 🚴 Exploring Princep Ghat sunsets & indie podcasts.",
        profession = "Senior Mobile Engineer",
        education = "B.Tech Computer Science",
        hobbies = "Cycling, Building Side Apps, Ghat Walks, Gadgets",
        interests = "Tech, Startups, Podcasts, Photography",
        activeIntent = "lets_movie",
        plan = "VIP LEVEL ★",
        followerCount = 276,
        followingCount = 39,
        lookingFor = "Real conversations & laughs",
        languages = "Bengali, English, Hindi",
        interestedIn = "Women",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_pratik",
        name = "Pratik Tamang",
        age = 24,
        gender = "Man",
        city = "Kalimpong",
        locationName = "Rishi Road, Kalimpong",
        latitude = 27.0667,
        longitude = 88.4667,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80",
        bio = "Landscape photographer & drone pilot 📷 You bring the stories, I'll capture the memories.",
        profession = "Documentary Filmmaker",
        education = "Film & Television Studies",
        hobbies = "Drone Flying, Mountain Expeditions, Editing, Rock Music",
        interests = "Filmmaking, Nature, Travel, Photography",
        activeIntent = "travel_partner",
        plan = "VIP LEVEL ★",
        followerCount = 490,
        followingCount = 61,
        lookingFor = "Creative spark & friendship",
        languages = "Nepali, Hindi, English",
        interestedIn = "Women",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_debjit",
        name = "Debjit Roy",
        age = 25,
        gender = "Man",
        city = "Durgapur",
        locationName = "City Centre, Durgapur",
        latitude = 23.5204,
        longitude = 87.3119,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
        bio = "Calisthenics athlete & fitness trainer 🏋️‍♂️ Sunrise sprints, street food evenings. Looking for positive energy.",
        profession = "Strength & Conditioning Coach",
        education = "B.Sc Physical Education",
        hobbies = "Calisthenics, Running, Nutrition, Swimming",
        interests = "Fitness, Health, Sports, Music",
        activeIntent = "free_tomorrow",
        plan = "VIP LEVEL ★",
        followerCount = 615,
        followingCount = 92,
        lookingFor = "Active lifestyle & good vibes",
        languages = "Bengali, Hindi, English",
        interestedIn = "Women",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_rohan",
        name = "Rohan Hembram",
        age = 26,
        gender = "Man",
        city = "Ranchi",
        locationName = "Morabadi, Ranchi",
        latitude = 23.3441,
        longitude = 85.3096,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=800&q=80",
        bio = "Wildlife researcher & forest trail camper 🌿 Motorbike expeditions and tribal folk music enthusiast.",
        profession = "Wildlife Biologist",
        education = "M.Sc Forestry & Wildlife",
        hobbies = "Motorcycle Touring, Birdwatching, Camping, Flute",
        interests = "Wildlife, Forestry, Eco-Trekking",
        activeIntent = "hangout_partner",
        plan = "VIP LEVEL ★",
        followerCount = 345,
        followingCount = 47,
        lookingFor = "Nature lover & travel companion",
        languages = "Hindi, English, Bengali",
        interestedIn = "Women",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_arindam",
        name = "Arindam Sen",
        age = 28,
        gender = "Man",
        city = "Kolkata",
        locationName = "Alipore, Kolkata",
        latitude = 22.5312,
        longitude = 88.3284,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=800&q=80",
        bio = "Chef & culinary writer 🍳 Exploring Kolkata's hidden street recipes and hosting weekend rooftop dinners.",
        profession = "Executive Sous Chef",
        education = "Culinary Arts Institute",
        hobbies = "Culinary Innovation, Wine Tasting, Tennis, Gardening",
        interests = "Gastronomy, Hospitality, Travel",
        activeIntent = "lets_dinner",
        plan = "VIP LEVEL ★",
        followerCount = 412,
        followingCount = 73,
        lookingFor = "Foodie dates & warm bonds",
        languages = "Bengali, Hindi, English",
        interestedIn = "Women",
        showFurtherAway = true,
        showOutsideAge = true
    ),
    UserProfile(
        uid = "dummy_devendra",
        name = "Devendra Sharma",
        age = 26,
        gender = "Man",
        city = "Patna",
        locationName = "Kankarbagh, Patna",
        latitude = 25.5941,
        longitude = 85.1376,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?auto=format&fit=crop&w=800&q=80",
        bio = "Civil engineer, passionate Urdu shayari writer & standup comedy buff 🎤 Always ready for road trips.",
        profession = "Structural Consultant",
        education = "B.Tech Civil Engineering",
        hobbies = "Writing Poetry, Standup Comedy, Badminton, Road Trips",
        interests = "Literature, Comedy, Travel, Podcasts",
        activeIntent = "plan_trip",
        plan = "VIP LEVEL ★",
        followerCount = 298,
        followingCount = 41,
        lookingFor = "Poetic minds & honest connection",
        languages = "Hindi, English, Maithili",
        interestedIn = "Women",
        showFurtherAway = true,
        showOutsideAge = true
    )
)

/**
 * Resiliently parses a MomentItem from a Firestore DocumentSnapshot.
 * Handles schema variations, missing fields, Long-to-Int conversions, and fallback to doc.id.
 */
fun parseMomentFromDoc(doc: DocumentSnapshot): MomentItem {
    return try {
        val parsed = doc.toObject(MomentItem::class.java)
        val validId = parsed?.id?.ifBlank { null } ?: doc.getString("id")?.ifBlank { null } ?: doc.id
        val validUserId = parsed?.userId?.ifBlank { null } ?: doc.getString("userId") ?: doc.getString("uid") ?: ""
        val validUserName = parsed?.userName?.ifBlank { null } ?: doc.getString("userName") ?: doc.getString("authorName") ?: doc.getString("name") ?: "User"
        val validUserPhoto = parsed?.userPhoto?.ifBlank { null } ?: doc.getString("userPhoto") ?: doc.getString("photo") ?: ""
        val validMediaUrl = parsed?.mediaUrl?.ifBlank { null } ?: doc.getString("mediaUrl") ?: doc.getString("imageUrl") ?: doc.getString("photoUrl") ?: ""
        
        val validMediaUrls = if (parsed != null && parsed.mediaUrls.isNotEmpty()) {
            parsed.mediaUrls
        } else {
            @Suppress("UNCHECKED_CAST")
            val listFromDoc = doc.get("mediaUrls") as? List<String>
            if (!listFromDoc.isNullOrEmpty()) listFromDoc
            else if (validMediaUrl.isNotBlank()) listOf(validMediaUrl)
            else emptyList()
        }
        val effectiveMediaUrl = validMediaUrl.ifBlank { validMediaUrls.firstOrNull() ?: "" }

        val validCaption = parsed?.caption?.ifBlank { null } ?: doc.getString("caption") ?: doc.getString("text") ?: ""
        val validLikes = if (parsed != null && parsed.likes > 0) parsed.likes else (doc.getLong("likes") ?: 0L).toInt()
        @Suppress("UNCHECKED_CAST")
        val validLikedBy = if (parsed != null && parsed.likedByUsers.isNotEmpty()) parsed.likedByUsers else (doc.get("likedByUsers") as? List<String>) ?: emptyList()
        val validComments = if (parsed != null && parsed.commentCount > 0) parsed.commentCount else (doc.getLong("commentCount") ?: 0L).toInt()
        val validTimestamp = if (parsed != null && parsed.timestamp > 0) parsed.timestamp else (doc.getLong("timestamp") ?: doc.getLong("createdAt") ?: System.currentTimeMillis())

        MomentItem(
            id = validId,
            userId = validUserId,
            userName = validUserName,
            userPhoto = validUserPhoto,
            mediaUrl = effectiveMediaUrl,
            mediaUrls = validMediaUrls,
            caption = validCaption,
            likes = validLikes,
            likedByUsers = validLikedBy,
            commentCount = validComments,
            timestamp = validTimestamp
        )
    } catch (e: Exception) {
        Log.w("Moments", "Safe fallback parser for doc ${doc.id}: ${e.message}")
        val validId = doc.getString("id")?.ifBlank { null } ?: doc.id
        val validUserId = doc.getString("userId") ?: doc.getString("uid") ?: ""
        val validUserName = doc.getString("userName") ?: doc.getString("authorName") ?: doc.getString("name") ?: "User"
        val validUserPhoto = doc.getString("userPhoto") ?: doc.getString("photo") ?: ""
        val validMediaUrl = doc.getString("mediaUrl") ?: doc.getString("imageUrl") ?: doc.getString("photoUrl") ?: ""
        @Suppress("UNCHECKED_CAST")
        val validMediaUrls = (doc.get("mediaUrls") as? List<String>) ?: (if (validMediaUrl.isNotBlank()) listOf(validMediaUrl) else emptyList())
        val effectiveMediaUrl = validMediaUrl.ifBlank { validMediaUrls.firstOrNull() ?: "" }

        val validCaption = doc.getString("caption") ?: doc.getString("text") ?: ""
        val validLikes = (doc.getLong("likes") ?: 0L).toInt()
        @Suppress("UNCHECKED_CAST")
        val validLikedBy = (doc.get("likedByUsers") as? List<String>) ?: emptyList()
        val validComments = (doc.getLong("commentCount") ?: 0L).toInt()
        val validTimestamp = doc.getLong("timestamp") ?: doc.getLong("createdAt") ?: System.currentTimeMillis()

        MomentItem(
            id = validId,
            userId = validUserId,
            userName = validUserName,
            userPhoto = validUserPhoto,
            mediaUrl = effectiveMediaUrl,
            mediaUrls = validMediaUrls,
            caption = validCaption,
            likes = validLikes,
            likedByUsers = validLikedBy,
            commentCount = validComments,
            timestamp = validTimestamp
        )
    }
}

fun saveMomentsToPrefs(prefs: SharedPreferences, moments: List<MomentItem>) {
    try {
        val jsonArray = JSONArray()
        moments.take(50).forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("userId", item.userId)
                put("userName", item.userName)
                put("userPhoto", item.userPhoto)
                put("mediaUrl", item.mediaUrl)
                val mediaUrlsArray = JSONArray()
                item.mediaUrls.forEach { mediaUrlsArray.put(it) }
                put("mediaUrls", mediaUrlsArray)
                put("caption", item.caption)
                put("likes", item.likes)
                put("commentCount", item.commentCount)
                put("timestamp", item.timestamp)
                val likedByArray = JSONArray()
                item.likedByUsers.forEach { likedByArray.put(it) }
                put("likedByUsers", likedByArray)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("cached_moments_json", jsonArray.toString()).apply()
    } catch (e: Exception) {
        Log.w("MomentsCache", "Failed to cache moments: ${e.message}")
    }
}

fun loadMomentsFromPrefs(prefs: SharedPreferences): List<MomentItem> {
    return try {
        val raw = prefs.getString("cached_moments_json", null) ?: return emptyList()
        val jsonArray = JSONArray(raw)
        val list = mutableListOf<MomentItem>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val likedByList = mutableListOf<String>()
            val likedByArray = obj.optJSONArray("likedByUsers")
            if (likedByArray != null) {
                for (j in 0 until likedByArray.length()) {
                    likedByList.add(likedByArray.getString(j))
                }
            }
            val mediaUrlsList = mutableListOf<String>()
            val mediaUrlsArray = obj.optJSONArray("mediaUrls")
            if (mediaUrlsArray != null) {
                for (j in 0 until mediaUrlsArray.length()) {
                    mediaUrlsList.add(mediaUrlsArray.getString(j))
                }
            }
            val singleMedia = obj.optString("mediaUrl", "")
            if (mediaUrlsList.isEmpty() && singleMedia.isNotBlank()) {
                mediaUrlsList.add(singleMedia)
            }
            list.add(
                MomentItem(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    userId = obj.optString("userId", ""),
                    userName = obj.optString("userName", "User"),
                    userPhoto = obj.optString("userPhoto", ""),
                    mediaUrl = singleMedia.ifBlank { mediaUrlsList.firstOrNull() ?: "" },
                    mediaUrls = mediaUrlsList,
                    caption = obj.optString("caption", ""),
                    likes = obj.optInt("likes", 0),
                    likedByUsers = likedByList,
                    commentCount = obj.optInt("commentCount", 0),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
        list
    } catch (e: Exception) {
        Log.w("MomentsCache", "Failed to load moments cache: ${e.message}")
        emptyList()
    }
}

data class AvatarFrameItem(
    val id: String,
    val name: String,
    val description: String,
    val cost: Long = 75000L,
    val drawableRes: Int,
    val isAnimated: Boolean = true,
    val vipLevel: Int = 0,
    val auraColors: List<Long> = listOf(0xFFFFD700, 0xFFFF7043, 0xFFFFD700)
)

val AVATAR_FRAMES_CATALOG = listOf(
    // -------------------------------------------------------------
    // VIP TIER FRAMES (VIP 1 to VIP 5: 2.5 Lakh to 50 Lakh Diamonds)
    // -------------------------------------------------------------
    AvatarFrameItem(
        id = "frame_vip1_archangel",
        name = "VIP 1 • Noble Archangel",
        description = "Platinum celestial wings with rotating halo & diamond dust",
        cost = 250000L,
        drawableRes = R.drawable.frame_vip1_archangel,
        isAnimated = true,
        vipLevel = 1,
        auraColors = listOf(0xFFE0E0E0, 0xFFFFFFFF, 0xFFB0BEC5, 0xFFE0E0E0)
    ),
    AvatarFrameItem(
        id = "frame_vip2_sunburst",
        name = "VIP 2 • Imperial Solar Gold",
        description = "Radiant golden wings with pulsating solar sunburst crown",
        cost = 500000L,
        drawableRes = R.drawable.frame_vip2_sunburst,
        isAnimated = true,
        vipLevel = 2,
        auraColors = listOf(0xFFFFD700, 0xFFFFA000, 0xFFFFE082, 0xFFFFD700)
    ),
    AvatarFrameItem(
        id = "frame_vip3_sapphire",
        name = "VIP 3 • Sapphire Sky King",
        description = "Electric royal sapphire crystal wings with spinning diamond ring",
        cost = 1000000L,
        drawableRes = R.drawable.frame_vip3_sapphire,
        isAnimated = true,
        vipLevel = 3,
        auraColors = listOf(0xFF00E5FF, 0xFF2979FF, 0xFF80D8FF, 0xFF00E5FF)
    ),
    AvatarFrameItem(
        id = "frame_vip4_amethyst",
        name = "VIP 4 • Cyan Divine Blade",
        description = "Radiant cyan crystal wings with twin swords & diamond crown",
        cost = 2500000L,
        drawableRes = R.drawable.frame_vip4_amethyst,
        isAnimated = true,
        vipLevel = 4,
        auraColors = listOf(0xFF00E5FF, 0xFF1DE9B6, 0xFFE0F2F1, 0xFF00E5FF)
    ),
    AvatarFrameItem(
        id = "frame_vip5_god_emperor",
        name = "VIP 5 • Emerald Realm Knight",
        description = "Royal emerald gold wings with twin broadswords & emerald crown",
        cost = 5000000L,
        drawableRes = R.drawable.frame_vip5_god_emperor,
        isAnimated = true,
        vipLevel = 5,
        auraColors = listOf(0xFF00E676, 0xFFFFD700, 0xFF00C853, 0xFF00E676)
    ),
    AvatarFrameItem(
        id = "frame_vip6_imperial_eagle",
        name = "VIP 6 • Imperial Supreme Eagles",
        description = "Dual golden imperial eagles with massive spreading wings & high crown",
        cost = 10000000L,
        drawableRes = R.drawable.frame_vip6_imperial_eagle,
        isAnimated = true,
        vipLevel = 6,
        auraColors = listOf(0xFFFFD700, 0xFF00C853, 0xFFFFFFFF, 0xFFFFD700)
    ),

    // -------------------------------------------------------------
    // FLOWING ANIMATED LUXURY FRAMES (75,000 to 200,000 Diamonds)
    // -------------------------------------------------------------
    AvatarFrameItem(
        id = "frame_phoenix_fire",
        name = "Blazing Phoenix Solar Crown",
        description = "Fiery mythical wings with molten embers & solar sunburst crown",
        cost = 75000L,
        drawableRes = R.drawable.frame_phoenix_fire,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFF5722, 0xFFFF9800, 0xFFFFEB3B, 0xFFFF5722)
    ),
    AvatarFrameItem(
        id = "frame_angel_wings",
        name = "Divine Celestial Wings",
        description = "Pure celestial gold feathers with radiant diamond halo",
        cost = 75000L,
        drawableRes = R.drawable.frame_angel_wings,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFFD700, 0xFFFFFFFF, 0xFFFFD700)
    ),
    AvatarFrameItem(
        id = "frame_emerald_valkyrie",
        name = "Emerald Crystal Valkyrie",
        description = "Vibrant jade emerald crystal wings with enchanted forest crown",
        cost = 80000L,
        drawableRes = R.drawable.frame_emerald_valkyrie,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFF00E676, 0xFF1DE9B6, 0xFF69F0AE, 0xFF00E676)
    ),
    AvatarFrameItem(
        id = "frame_ice_valkyrie",
        name = "Glacial Frost Valkyrie",
        description = "Frozen glacial crystal wings with diamond frost tiara & cyan mist",
        cost = 85000L,
        drawableRes = R.drawable.frame_ice_valkyrie,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFF80D8FF, 0xFF00B0FF, 0xFFE0F7FA, 0xFF80D8FF)
    ),
    AvatarFrameItem(
        id = "frame_crimson_dragon",
        name = "Crimson Flame Seraph",
        description = "Ruby red demon wings with blazing embers & crimson tiara",
        cost = 85000L,
        drawableRes = R.drawable.frame_dark_angel,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFF1744, 0xFFFF5722, 0xFFFF8A80, 0xFFFF1744)
    ),
    AvatarFrameItem(
        id = "frame_cyber_neon",
        name = "Cyber Neon Matrix",
        description = "Electric cyan & magenta lasers with holographic 360 ring",
        cost = 90000L,
        drawableRes = R.drawable.frame_sapphire_king,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFF00E5FF, 0xFFFF007F, 0xFF76FF03, 0xFF00E5FF)
    ),
    AvatarFrameItem(
        id = "frame_dark_angel",
        name = "Dark Gothic Fallen Angel",
        description = "Obsidian shadow wings with crimson aura & ruby demon tiara",
        cost = 95000L,
        drawableRes = R.drawable.frame_dark_angel,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFD50000, 0xFF212121, 0xFFFF1744, 0xFFD50000)
    ),
    AvatarFrameItem(
        id = "luxury_host_frame",
        name = "Imperial Rose Luxury Wreath",
        description = "Original luxury golden floral wreath with ruby accents",
        cost = 95000L,
        drawableRes = R.drawable.luxury_host_frame,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFFD700, 0xFFFF4081, 0xFFFFD700)
    ),
    AvatarFrameItem(
        id = "frame_moon_celestial",
        name = "Celestial Moon Seraph",
        description = "Platinum lunar archangel wings with sparkling blue moon halo",
        cost = 100000L,
        drawableRes = R.drawable.frame_moon_celestial,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFB388FF, 0xFF8C9EFF, 0xFFEDE7F6, 0xFFB388FF)
    ),
    AvatarFrameItem(
        id = "frame_rose_seraph",
        name = "Rose Gold Seraph of Love",
        description = "Shimmering pink & rose gold feather wings with crystal heart tiara",
        cost = 110000L,
        drawableRes = R.drawable.frame_rose_seraph,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFF4081, 0xFFFF80AB, 0xFFFFE4E1, 0xFFFF4081)
    ),
    AvatarFrameItem(
        id = "frame_mystic_violet",
        name = "Mystic Amethyst Dream",
        description = "Enchanted twilight violet seraph wings with starry mist",
        cost = 115000L,
        drawableRes = R.drawable.frame_amethyst_seraph,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFF9C27B0, 0xFFBA68C8, 0xFFE1BEE7, 0xFF9C27B0)
    ),
    AvatarFrameItem(
        id = "frame_ocean_poseidon",
        name = "Poseidon Ocean Emperor",
        description = "Deep ocean sapphire waves with shimmering pearl trident crown",
        cost = 120000L,
        drawableRes = R.drawable.frame_sapphire_king,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFF0091EA, 0xFF00B8D4, 0xFF84FFFF, 0xFF0091EA)
    ),
    AvatarFrameItem(
        id = "frame_aurora_lights",
        name = "Northern Aurora Borealis",
        description = "Curtains of emerald green & violet cosmic aurora waves",
        cost = 130000L,
        drawableRes = R.drawable.frame_emerald_valkyrie,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFF00E676, 0xFF7C4DFF, 0xFF18FFFF, 0xFF00E676)
    ),
    AvatarFrameItem(
        id = "frame_thunder_zeus",
        name = "Thunder Zeus Lightning",
        description = "Electric golden lightning bolts with Olympus storm crown",
        cost = 140000L,
        drawableRes = R.drawable.frame_imperial_gold,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFFEA00, 0xFFFFD600, 0xFFFFFFFF, 0xFFFFEA00)
    ),
    AvatarFrameItem(
        id = "frame_black_gold_vip",
        name = "Black Velvet Royale",
        description = "Matte obsidian velvet with gilded 24K gold feather trims",
        cost = 150000L,
        drawableRes = R.drawable.frame_dark_angel,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFFD700, 0xFF212121, 0xFFFFC107, 0xFFFFD700)
    ),
    AvatarFrameItem(
        id = "frame_ruby_emperor",
        name = "Royal Ruby Sovereign",
        description = "Deep crimson Burmese rubies embedded in ornate golden filigree",
        cost = 160000L,
        drawableRes = R.drawable.luxury_host_frame,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFF1744, 0xFFC2185B, 0xFFFF5252, 0xFFFF1744)
    ),
    AvatarFrameItem(
        id = "frame_diamond_glacier",
        name = "Eternal Diamond Prism",
        description = "Pure brilliant cut diamonds refracting full rainbow beams",
        cost = 170000L,
        drawableRes = R.drawable.frame_ice_valkyrie,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFE0F7FA, 0xFF80DEEA, 0xFFFFFFFF, 0xFFE0F7FA)
    ),
    AvatarFrameItem(
        id = "frame_solar_flare",
        name = "Solar Eclipse Inferno",
        description = "Blazing solar corona prominence with rotating molten fire halo",
        cost = 180000L,
        drawableRes = R.drawable.frame_phoenix_fire,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFF3D00, 0xFFFF9100, 0xFFFFD600, 0xFFFF3D00)
    ),
    AvatarFrameItem(
        id = "frame_dark_void",
        name = "Dark Void Nebula",
        description = "Deep space violet singularity vortex with orbiting micro stars",
        cost = 190000L,
        drawableRes = R.drawable.frame_amethyst_seraph,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFF651FFF, 0xFF3D5AFE, 0xFFB388FF, 0xFF651FFF)
    ),
    AvatarFrameItem(
        id = "frame_golden_peacock",
        name = "Golden Peacock Majesty",
        description = "Iridescent royal golden peacock crest with emerald eye gems",
        cost = 200000L,
        drawableRes = R.drawable.frame_imperial_gold,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFFD700, 0xFF00C853, 0xFF00B0FF, 0xFFFFD700)
    )
)

fun getAvatarFrameDrawable(frameId: String): Int {
    return AVATAR_FRAMES_CATALOG.find { it.id == frameId }?.drawableRes ?: R.drawable.frame_angel_wings
}

fun getAvatarFrameItem(frameId: String): AvatarFrameItem? {
    return AVATAR_FRAMES_CATALOG.find { it.id == frameId }
}




