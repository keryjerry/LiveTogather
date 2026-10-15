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
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis()
)

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
    val id = doc.getString("id") ?: doc.id
    val title = doc.getString("title") ?: ""
    val emoji = doc.getString("emoji") ?: "🎙️"
    val bannerUrl = doc.getString("banner_image_url") ?: ""
    val activeCount = (doc.getLong("activeUserCount") ?: 0L).toInt()
    val hostId = doc.getString("hostId") ?: ""
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
    val isHostLive = doc.getBoolean("isHostLive") ?: doc.getBoolean("hostLive") ?: true
    val scheduledTime = doc.getString("scheduledTime") ?: ""
    val statusMessage = doc.getString("statusMessage") ?: ""
    val privacy = doc.getString("privacy") ?: "public"
    @Suppress("UNCHECKED_CAST")
    val bannedUserIds = (doc.get("bannedUserIds") as? List<String>) ?: emptyList()
    @Suppress("UNCHECKED_CAST")
    val kickedUserIds = (doc.get("kickedUserIds") as? List<String>) ?: emptyList()
    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

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
        createdAt = createdAt
    )
}

val CATEGORY_BANNERS = mapOf(
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
    return CATEGORY_BANNERS[categoryId] ?: "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?auto=format&fit=crop&w=800&q=80"
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
    val activeFrame: String = "",
    val ownedFrames: List<String> = emptyList()
) {
    fun isPaidSubscriber(): Boolean {
        return plan.isNotBlank() && !plan.equals("Free", ignoreCase = true)
    }

    fun isVipOrPlatinum(): Boolean {
        return plan.contains("VIP", ignoreCase = true) || plan.contains("Platinum", ignoreCase = true)
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
        return isVipOrPlatinum() || isHostTrialActive()
    }

    fun canSwipe(todayDate: String): Boolean {
        if (isPaidSubscriber()) return true
        if (lastSwipeDate != todayDate) return true
        return dailySwipeCount < 15
    }
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
    val animationType: String = "car", // "car", "rocket", "dragon", "roses", "castle"
    val timestamp: Long = System.currentTimeMillis()
)

data class VideoGiftItem(
    val id: String,
    val name: String,
    val emoji: String,
    val cost: Long,
    val animationType: String,
    val description: String
)

val VIDEO_GIFT_CATALOG = listOf(
    VideoGiftItem("rose_storm", "Rose Storm", "🌹", 5000L, "roses", "Romantic cascading rose petals shower"),
    VideoGiftItem("supercar", "Cyber Supercar", "🏎️", 10000L, "car", "Ultra luxury roaring sports car drift"),
    VideoGiftItem("rocket", "Galactic Rocket", "🚀", 20000L, "rocket", "Cosmic space blastoff across the sky"),
    VideoGiftItem("golden_palace", "Imperial Palace", "🏰", 35000L, "castle", "Majestic golden palace fireworks"),
    VideoGiftItem("dragon", "Golden Dragon", "🐉", 50000L, "dragon", "Legendary flaming golden dragon roar")
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
    val cost: Long = 25000L,
    val drawableRes: Int
)

val AVATAR_FRAMES_CATALOG = listOf(
    AvatarFrameItem(
        id = "frame_angel_wings",
        name = "Divine Archangel Wings & Halo",
        description = "Pure celestial gold feathers with radiant diamond halo",
        cost = 25000L,
        drawableRes = R.drawable.frame_angel_wings
    ),
    AvatarFrameItem(
        id = "frame_phoenix_fire",
        name = "Blazing Phoenix Solar Crown",
        description = "Fiery mythical wings with molten embers & solar sunburst crown",
        cost = 25000L,
        drawableRes = R.drawable.frame_phoenix_fire
    ),
    AvatarFrameItem(
        id = "frame_dark_angel",
        name = "Dark Gothic Fallen Angel",
        description = "Obsidian shadow wings with crimson aura & ruby demon tiara",
        cost = 25000L,
        drawableRes = R.drawable.frame_dark_angel
    ),
    AvatarFrameItem(
        id = "frame_moon_celestial",
        name = "Celestial Moon Seraph",
        description = "Platinum lunar archangel wings with sparkling blue moon halo",
        cost = 25000L,
        drawableRes = R.drawable.frame_moon_celestial
    ),
    AvatarFrameItem(
        id = "frame_ice_valkyrie",
        name = "Ice Valkyrie Frost Crown",
        description = "Frozen glacial crystal wings with diamond frost tiara & cyan mist",
        cost = 25000L,
        drawableRes = R.drawable.frame_ice_valkyrie
    ),
    AvatarFrameItem(
        id = "frame_imperial_gold",
        name = "Imperial Golden Eagle King",
        description = "Majestic eagle golden wings with red velvet royal imperial crown",
        cost = 25000L,
        drawableRes = R.drawable.frame_imperial_gold
    ),
    AvatarFrameItem(
        id = "frame_rose_seraph",
        name = "Rose Gold Seraph of Love",
        description = "Shimmering pink & rose gold feather wings with crystal heart tiara",
        cost = 25000L,
        drawableRes = R.drawable.frame_rose_seraph
    ),
    AvatarFrameItem(
        id = "frame_amethyst_seraph",
        name = "Royal Amethyst Mystic Wings",
        description = "Deep violet royal seraph wings with glowing amethyst gem tiara",
        cost = 25000L,
        drawableRes = R.drawable.frame_amethyst_seraph
    ),
    AvatarFrameItem(
        id = "frame_emerald_valkyrie",
        name = "Emerald Crystal Valkyrie",
        description = "Vibrant jade emerald crystal wings with enchanted forest crown",
        cost = 25000L,
        drawableRes = R.drawable.frame_emerald_valkyrie
    ),
    AvatarFrameItem(
        id = "frame_sapphire_king",
        name = "Sapphire Royal Sky King",
        description = "Electric sapphire blue archangel wings with regal azure crown",
        cost = 25000L,
        drawableRes = R.drawable.frame_sapphire_king
    ),
    AvatarFrameItem(
        id = "luxury_host_frame",
        name = "Imperial Rose & Gold VIP Frame",
        description = "Original luxury golden floral wreath with ruby accents",
        cost = 25000L,
        drawableRes = R.drawable.luxury_host_frame
    )
)

fun getAvatarFrameDrawable(frameId: String): Int {
    return AVATAR_FRAMES_CATALOG.find { it.id == frameId }?.drawableRes ?: R.drawable.frame_angel_wings
}



