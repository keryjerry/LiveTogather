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
    val currentPlayingTrack: String = "",
    val playbackPosition: Long = 0L,
    val isPlaying: Boolean = false,
    val isCustom: Boolean = false,
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

    val blockedUserIds: List<String> = emptyList()
) {
    fun isVipOrPlatinum(): Boolean {
        return plan.contains("VIP", ignoreCase = true) || plan.contains("Platinum", ignoreCase = true)
    }
}

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
    val url: String = ""
)

val CURATED_HANGOUT_TRACKS = listOf(
    // 1. Lo-Fi Chill
    HangoutTrack("track_1", "Tokyo Sunset Beat", "Tokyo Sunset", "Lo-Fi Chill", "https://assets.mixkit.co/music/preview/mixkit-chill-bro-494.mp3"),
    HangoutTrack("track_2", "Sleepy Cat Dreams", "Lo-Fi Dreamer", "Lo-Fi Chill", "https://assets.mixkit.co/music/preview/mixkit-sleepy-cat-135.mp3"),
    HangoutTrack("track_3", "Sunday Coffee Mellow", "Chillhop Cafe", "Lo-Fi Chill", "https://assets.mixkit.co/music/preview/mixkit-serene-view-443.mp3"),

    // 2. Acoustic Romantic
    HangoutTrack("track_4", "Velvet Night Acoustic", "Velvet Guitar", "Acoustic Romantic", "https://assets.mixkit.co/music/preview/mixkit-serene-view-443.mp3"),
    HangoutTrack("track_5", "Aura of Harmony", "Aura Harmony", "Acoustic Romantic", "https://assets.mixkit.co/music/preview/mixkit-sleepy-cat-135.mp3"),

    // 3. Cafe Jazz & Lounge
    HangoutTrack("track_6", "Midnight Espresso Groove", "Espresso Lounge", "Cafe Jazz", "https://assets.mixkit.co/music/preview/mixkit-chill-bro-494.mp3"),
    HangoutTrack("track_7", "Paris Morning Sax", "Smooth Sax Quartet", "Cafe Jazz", "https://assets.mixkit.co/music/preview/mixkit-serene-view-443.mp3"),

    // 4. Party Beats
    HangoutTrack("track_8", "Tech House Warmup", "Neon Rhythm", "Party Beats", "https://assets.mixkit.co/music/preview/mixkit-tech-house-vibes-130.mp3"),
    HangoutTrack("track_9", "Sunset Rooftop Groove", "Miami Beats", "Party Beats", "https://assets.mixkit.co/music/preview/mixkit-driving-ambition-32.mp3"),

    // 5. EDM Club
    HangoutTrack("track_10", "Driving Ambition Synth", "Synth Dream", "EDM Club", "https://assets.mixkit.co/music/preview/mixkit-driving-ambition-32.mp3"),
    HangoutTrack("track_11", "Cyber Club Afterhours", "Electro Drift", "EDM Club", "https://assets.mixkit.co/music/preview/mixkit-tech-house-vibes-130.mp3")
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

