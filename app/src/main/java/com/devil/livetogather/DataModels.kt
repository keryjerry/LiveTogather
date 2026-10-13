package com.devil.livetogather

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

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
        hostId = "host_priya_sharma",
        hostName = "Priya Sharma",
        hostPhotoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "free_tomorrow",
        title = "Free Tomorrow",
        emoji = "🌙",
        hostId = "host_aarav_kapoor",
        hostName = "Aarav Kapoor",
        hostPhotoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "free_weekend",
        title = "Free in Weekend",
        emoji = "🎉",
        hostId = "host_ananya_sen",
        hostName = "Ananya Sen",
        hostPhotoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "ready_travel",
        title = "Ready for Travelling",
        emoji = "🧳",
        hostId = "host_rohit_verma",
        hostName = "Rohit Verma",
        hostPhotoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "travel_partner",
        title = "Travel Partner",
        emoji = "✈️",
        hostId = "host_sneha_patel",
        hostName = "Sneha Patel",
        hostPhotoUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "hangout_partner",
        title = "Hangout Partner",
        emoji = "🤝",
        hostId = "host_vikram_malhotra",
        hostName = "Vikram Malhotra",
        hostPhotoUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "free_party",
        title = "Free for Party",
        emoji = "🥳",
        hostId = "host_isha_roy",
        hostName = "Isha Roy",
        hostPhotoUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "join_party",
        title = "Join Party",
        emoji = "🍸",
        hostId = "host_karan_singh",
        hostName = "Karan Singh",
        hostPhotoUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "going_club",
        title = "Going to Club",
        emoji = "🕺",
        hostId = "host_tanya_mehra",
        hostName = "Tanya Mehra",
        hostPhotoUrl = "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "plan_trip",
        title = "Lets Plan Holiday Trip",
        emoji = "🗺️",
        hostId = "host_samir_joshi",
        hostName = "Samir Joshi",
        hostPhotoUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "lets_movie",
        title = "Lets Go For Movie",
        emoji = "🍿",
        hostId = "host_pooja_reddy",
        hostName = "Pooja Reddy",
        hostPhotoUrl = "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "lets_cafe",
        title = "Lets Go Cafe",
        emoji = "☕",
        hostId = "host_arjun_das",
        hostName = "Arjun Das",
        hostPhotoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
        banner_image_url = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=800&q=80"
    ),
    RoomItem(
        id = "lets_dinner",
        title = "Lets Go For Dinner/Lunch",
        emoji = "🍽️",
        hostId = "host_kavya_nair",
        hostName = "Kavya Nair",
        hostPhotoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=400&q=80",
        isHostLive = true,
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
    "Kolkata" to listOf("New Town", "Salt Lake", "Park Street", "Ballygunge", "Howrah", "Dum Dum"),
    "Mumbai" to listOf("Bandra West", "Andheri", "Juhu", "Colaba", "Powai", "Thane"),
    "Delhi" to listOf("Connaught Place", "Hauz Khas", "Saket", "Dwarka", "Vasant Kunj", "Noida"),
    "Bangalore" to listOf("Koramangala", "Indiranagar", "HSR Layout", "Whitefield", "JP Nagar"),
    "Pune" to listOf("Koregaon Park", "Viman Nagar", "Baner", "Kothrud", "Aundh", "Hinjawadi"),
    "Hyderabad" to listOf("Jubilee Hills", "Banjara Hills", "Hitec City", "Gachibowli", "Madhapur")
)

val DEFAULT_DUMMY_PROFILES = listOf(
    UserProfile(
        uid = "dummy_priya",
        name = "Priya Sharma",
        age = 23,
        gender = "Woman",
        city = "Kolkata",
        locationName = "New Town, Kolkata",
        latitude = 22.5850,
        longitude = 88.4600,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
        bio = "Host of Free Tonight 🍷 Singer, traveler, and coffee enthusiast.",
        profession = "Live Streamer & Vocalist",
        education = "Degree in Performing Arts",
        hobbies = "Singing, Acoustic Guitar, Traveling, Coffee",
        interests = "Music, Late Night Hangouts, Photography",
        plan = "VIP LEVEL ★",
        followerCount = 142,
        followingCount = 38,
        lookingFor = "Meaningful friendships & vibes",
        languages = "Hindi, English, Bengali",
        interestedIn = "Men"
    ),
    UserProfile(
        uid = "dummy_ananya",
        name = "Ananya Sen",
        age = 24,
        gender = "Woman",
        city = "Mumbai",
        locationName = "Bandra West, Mumbai",
        latitude = 19.0596,
        longitude = 72.8295,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=80",
        bio = "Fashion stylist & weekend foodie. Always up for good conversations and road trips 🚗✨",
        profession = "Fashion Stylist & Creative Director",
        education = "Masters in Fashion Communication",
        hobbies = "Fashion, Photography, Food Tours, Pilates",
        interests = "Art, Cafes, Film, Travel",
        plan = "VIP LEVEL ★",
        followerCount = 289,
        followingCount = 64,
        lookingFor = "Creative minds & dating",
        languages = "English, Hindi, Bengali",
        interestedIn = "Men"
    ),
    UserProfile(
        uid = "dummy_rohan",
        name = "Rohan Verma",
        age = 26,
        gender = "Man",
        city = "Delhi",
        locationName = "Hauz Khas, Delhi",
        latitude = 28.5494,
        longitude = 77.2001,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
        bio = "Electronic music producer & DJ. Let's talk synthesizers and hidden rooftop spots 🎧",
        profession = "Music Producer & DJ",
        education = "Sound Engineering Graduate",
        hobbies = "Audio Synthesis, Vinyl Collecting, Gaming, Cycling",
        interests = "Nightlife, Music Production, Concerts",
        plan = "Platinum Elite",
        followerCount = 512,
        followingCount = 92,
        lookingFor = "Music collabs & deep connections",
        languages = "Hindi, English, Punjabi",
        interestedIn = "Women"
    ),
    UserProfile(
        uid = "dummy_sneha",
        name = "Sneha Patel",
        age = 23,
        gender = "Woman",
        city = "Bangalore",
        locationName = "Indiranagar, Bangalore",
        latitude = 12.9784,
        longitude = 77.6408,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80",
        bio = "Software architect by day, bookworm & stargazer by night 🌌📚",
        profession = "Cloud Architect",
        education = "B.Tech Computer Science",
        hobbies = "Reading Sci-Fi, Badminton, Trekking, Coding",
        interests = "Tech, Stargazing, Poetry, Coffee",
        plan = "VIP LEVEL ★",
        followerCount = 95,
        followingCount = 41,
        lookingFor = "Intellectual talks & companionship",
        languages = "English, Hindi, Gujarati",
        interestedIn = "Men"
    ),
    UserProfile(
        uid = "dummy_kabir",
        name = "Kabir Mehra",
        age = 27,
        gender = "Man",
        city = "Pune",
        locationName = "Koregaon Park, Pune",
        latitude = 18.5362,
        longitude = 73.8958,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=800&q=80",
        bio = "Crossfit athlete, calisthenics fanatic, and solo backpacker 🏔️ Always seeking the next summit.",
        profession = "Fitness Trainer & Adventure Guide",
        education = "Sports Science Certification",
        hobbies = "Highland Trekking, Calisthenics, Rock Climbing, Vlogging",
        interests = "Fitness, Adventure, Outdoor Life",
        plan = "VIP LEVEL ★",
        followerCount = 340,
        followingCount = 76,
        lookingFor = "Fitness buddies & travel partner",
        languages = "Hindi, English, Marathi",
        interestedIn = "Women"
    ),
    UserProfile(
        uid = "dummy_diya",
        name = "Diya Mehta",
        age = 25,
        gender = "Woman",
        city = "Kolkata",
        locationName = "Park Street, Kolkata",
        latitude = 22.5510,
        longitude = 88.3524,
        isLocationSet = true,
        photo = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80",
        bio = "Graphic designer & plant mom 🌱 Loves museum visits, sunset walks, and indie vinyl records.",
        profession = "Brand Identity Designer",
        education = "Bachelor of Fine Arts",
        hobbies = "Watercolor Painting, Pottery, Gardening, Indie Cinema",
        interests = "Art Galleries, Plant Care, Design, Indie Music",
        plan = "VIP LEVEL ★",
        followerCount = 210,
        followingCount = 55,
        lookingFor = "Authentic connection & shared laughter",
        languages = "English, Hindi, Bengali",
        interestedIn = "Men"
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


