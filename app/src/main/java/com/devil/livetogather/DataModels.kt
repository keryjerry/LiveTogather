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
    // 1. Lo-Fi Chill
    HangoutTrack(
        id = "track_1",
        title = "Lofi Chill Sleep",
        artist = "Lofi Sleep Chill",
        category = "Lo-Fi Chill",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/10/0f/e6/100fe64f-0815-a9d5-9bec-e375ddd5a733/mzaf_8225005754342783380.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/71/15/c5/7115c542-cf70-35f6-e2e9-d2131ff538a2/5055803554880.png/100x100bb.jpg"
    ),
    HangoutTrack(
        id = "track_2",
        title = "Coffee Break Chill Cafe",
        artist = "FM STAR",
        category = "Lo-Fi Chill",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/07/77/b6/0777b68b-62c2-2c65-bed2-df61bec3d6fd/mzaf_13997473709135475175.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/93/9e/20/939e2003-c7e8-3269-1ae9-b03a1dd46114/4550795098790_cover.jpg/100x100bb.jpg"
    ),

    // 2. Acoustic Romantic
    HangoutTrack(
        id = "track_3",
        title = "Main Rang Sharbaton Ka",
        artist = "Arijit Singh & Pritam",
        category = "Acoustic Romantic",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/87/16/d6/8716d618-8cd3-8ba7-cf72-49b76b64419b/mzaf_4801674308569520150.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music114/v4/02/98/a6/0298a63c-c22f-52d9-a62c-1e7c19980a3a/8901854030330.jpg/100x100bb.jpg"
    ),
    HangoutTrack(
        id = "track_4",
        title = "Binte Dil Acoustic",
        artist = "Arijit Singh",
        category = "Acoustic Romantic",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/ec/74/4a/ec744a08-3be7-9935-1cd5-01070b021c18/mzaf_16411309030678014559.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/1b/19/26/1b192691-0179-73ab-3ed7-ae2b923ca71a/8902894360005_cover.jpg/100x100bb.jpg"
    ),

    // 3. Cafe Jazz & Lounge
    HangoutTrack(
        id = "track_5",
        title = "Soulful Jazz Whispers",
        artist = "FM STAR",
        category = "Cafe Jazz",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/bc/cb/fe/bccbfe29-2fc7-00b3-87ac-f88d610675a2/mzaf_4975142373289658271.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/a4/a6/71/a4a6710d-31bb-6f4c-66e9-f5482fce04ac/4550760683020_cover.jpg/100x100bb.jpg"
    ),
    HangoutTrack(
        id = "track_6",
        title = "Gentle Jazz Whispers",
        artist = "FM STAR",
        category = "Cafe Jazz",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/00/28/6e/00286e68-84ab-b8e1-2592-3668193607cc/mzaf_16843597559467010983.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/a4/a6/71/a4a6710d-31bb-6f4c-66e9-f5482fce04ac/4550760683020_cover.jpg/100x100bb.jpg"
    ),

    // 4. Party Beats
    HangoutTrack(
        id = "track_7",
        title = "Morni",
        artist = "Diljit Dosanjh & Tru-Skool",
        category = "Party Beats",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/f5/6e/e4/f56ee414-2027-b605-7455-a24fbc433373/mzaf_3937962578481238111.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/97/86/86/97868694-9413-a543-514a-a6374469ff97/859736427250_cover.jpg/100x100bb.jpg"
    ),
    HangoutTrack(
        id = "track_8",
        title = "Sauda Khara Khara",
        artist = "Diljit Dosanjh & Sukhbir",
        category = "Party Beats",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/6a/08/eb/6a08eb69-c072-d4e0-1cb5-43659ca552eb/mzaf_7562300059952992185.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/c2/82/c6/c282c63f-876f-9c0c-14aa-204c000c1a01/8909024123752.png/100x100bb.jpg"
    ),

    // 5. EDM Club
    HangoutTrack(
        id = "track_9",
        title = "Faded Club Mix",
        artist = "Alan Walker",
        category = "EDM Club",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/ab/18/d9/ab18d9e9-01f0-27ea-3a51-6e08585a0072/mzaf_14299744081264843911.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music114/v4/0d/a3/1a/0da31af7-d0ff-9bee-c427-1b6d0336f6fc/886446321981.jpg/100x100bb.jpg"
    ),
    HangoutTrack(
        id = "track_10",
        title = "Alone",
        artist = "Alan Walker",
        category = "EDM Club",
        url = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/a2/71/3f/a2713f1c-c1a2-86b3-5ec2-633e71927fae/mzaf_1625470662823872018.plus.aac.p.m4a",
        artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music125/v4/ec/20/51/ec20512e-d0ca-4281-1b03-b24a683b87da/886446357522.jpg/100x100bb.jpg"
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

