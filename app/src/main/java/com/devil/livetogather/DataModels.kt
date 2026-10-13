package com.devil.livetogather

data class IntentCategory(val id: String, val label: String)

val CATEGORY_LIST = listOf(
    IntentCategory("all", "🔥 All Profiles"),
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
    val id: String = "",
    val title: String = "",
    val emoji: String = "",
    val banner_image_url: String = "",
    val activeUserCount: Int = 0,
    val hostId: String = "",
    val hostName: String = "",
    val hostPhotoUrl: String = "",
    val category: String = "",
    val nowPlayingTitle: String = "",
    val nowPlayingArtist: String = "",
    val nowPlayingUrl: String = "",
    val nowPlayingArtworkUrl: String = "",
    val currentPlayingTrack: String = "",
    val playbackPosition: Long = 0L,
    val isPlaying: Boolean = false,
    val isCustom: Boolean = false,
    val isHostLive: Boolean = true,
    val scheduledTime: String = "",
    val statusMessage: String = "",
    val privacy: String = "public", // "public", "vip_only", "private"
    val bannedUserIds: List<String> = emptyList(),
    val kickedUserIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

val INTENT_ROOMS_CATALOG = listOf(
    RoomItem(
        id = "free_tonight",
        title = "Free Tonight",
        emoji = "🍷",
        banner_image_url = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "free_tomorrow",
        title = "Free Tomorrow",
        emoji = "🌙",
        banner_image_url = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "free_weekend",
        title = "Free in Weekend",
        emoji = "🎉",
        banner_image_url = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "ready_travel",
        title = "Ready for Travelling",
        emoji = "🧳",
        banner_image_url = "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "travel_partner",
        title = "Travel Partner",
        emoji = "✈️",
        banner_image_url = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "hangout_partner",
        title = "Hangout Partner",
        emoji = "🤝",
        banner_image_url = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "free_party",
        title = "Free for Party",
        emoji = "🥳",
        banner_image_url = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "join_party",
        title = "Join Party",
        emoji = "🍸",
        banner_image_url = "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "going_club",
        title = "Going to Club",
        emoji = "🕺",
        banner_image_url = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "plan_trip",
        title = "Lets Plan Holiday Trip",
        emoji = "🗺️",
        banner_image_url = "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "lets_movie",
        title = "Lets Go For Movie",
        emoji = "🍿",
        banner_image_url = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "lets_cafe",
        title = "Lets Go Cafe",
        emoji = "☕",
        banner_image_url = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "lets_dinner",
        title = "Lets Go For Dinner/Lunch",
        emoji = "🍽️",
        banner_image_url = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80"
    )
)

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
    val plan: String = "VIP LEVEL ★",
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
    val followingCount: Int = 0
) {
    fun isVipOrPlatinum(): Boolean {
        return plan.contains("VIP", ignoreCase = true) || plan.contains("Platinum", ignoreCase = true)
    }
}

data class FollowerRecord(
    val uid: String = "",
    val name: String = "",
    val photo: String = "",
    val timestamp: Long = System.currentTimeMillis()
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
    val isRead: Boolean = false
)

data class RoomMember(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val role: String = "listener", // "host" or "listener"
    val tier: String = "Standard" // "VIP", "Platinum", "Standard"
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
    // 1. Bollywood & Romantic (Full Length 3 to 5 mins)
    HangoutTrack(
        id = "track_1",
        title = "Nashe Si Chadh Gayi",
        artist = "Arijit Singh & Vishal-Shekhar",
        category = "Acoustic Romantic",
        url = "https://aac.saavncdn.com/574/e045a751c0975c2014cf625206184950_160.mp4",
        artworkUrl = "https://c.saavncdn.com/574/Best-of-Arijit-Singh-Hindi-2019-20190715123118-500x500.jpg",
        durationSec = 238
    ),
    HangoutTrack(
        id = "track_2",
        title = "Main Rang Sharbaton Ka",
        artist = "Arijit Singh & Chinmayi Sripada",
        category = "Acoustic Romantic",
        url = "https://aac.saavncdn.com/495/e1c4df3bf87bb9be4a9ef19ec31a3962_160.mp4",
        artworkUrl = "https://c.saavncdn.com/495/Phata-Poster-Nikhla-Hero-Hindi-2013-500x500.jpg",
        durationSec = 282
    ),

    // 2. Lo-Fi Chill & Sleep (Full Length)
    HangoutTrack(
        id = "track_3",
        title = "Lofi Midnight Reverie",
        artist = "Chillhop Sessions",
        category = "Lo-Fi Chill",
        url = "https://freepd.com/music/City%20Sunshine.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?auto=format&fit=crop&w=500&q=80",
        durationSec = 205
    ),
    HangoutTrack(
        id = "track_4",
        title = "Coffee Break Chill Cafe",
        artist = "FM STAR Lounge",
        category = "Lo-Fi Chill",
        url = "https://freepd.com/music/Sweet%20Treat.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=500&q=80",
        durationSec = 192
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
    RoomThemePreset("theme_neon_lounge", "Neon Velvet Lounge", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80"),
    RoomThemePreset("theme_rooftop_sunset", "Rooftop Sunset Coral", "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?auto=format&fit=crop&w=800&q=80"),
    RoomThemePreset("theme_midnight_club", "Midnight Cyber Club", "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=800&q=80"),
    RoomThemePreset("theme_acoustic_cafe", "Candlelit Acoustic Cafe", "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=800&q=80"),
    RoomThemePreset("theme_coastal_drive", "Coastal Night Drive", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80"),
    RoomThemePreset("theme_party_vibes", "Golden Hour Festival", "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=800&q=80")
)

