package com.devil.livetogather

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class IntentCategory(val id: String, val label: String)

val ADMIN_WHITELIST_EMAILS = setOf(
    "service.secure822@gmail.com",
    "servicesecure822@gmail.com",
    "devilgroupcontact@gmail.com"
)

fun isUserWhitelistedAdmin(email: String?, uid: String? = null): Boolean {
    val cleanEmail = email?.lowercase()?.trim() ?: ""
    if (cleanEmail.isNotBlank() && ADMIN_WHITELIST_EMAILS.contains(cleanEmail)) {
        return true
    }
    return false
}

val CATEGORY_LIST = listOf(
    IntentCategory("all", "🔥 All Profiles"),
    IntentCategory("video_stream", "📹 Video Stream"),
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
    @get:PropertyName("isLocked") @set:PropertyName("isLocked") var isLocked: Boolean = false,
    @get:PropertyName("trackStartedAt") @set:PropertyName("trackStartedAt") var trackStartedAt: Long = 0L,
    // Real-Time Live Room PK Battle Arena
    @get:PropertyName("isPkActive") @set:PropertyName("isPkActive") var isPkActive: Boolean = false,
    @get:PropertyName("pkHost1Uid") @set:PropertyName("pkHost1Uid") var pkHost1Uid: String = "",
    @get:PropertyName("pkHost1Name") @set:PropertyName("pkHost1Name") var pkHost1Name: String = "",
    @get:PropertyName("pkHost1Photo") @set:PropertyName("pkHost1Photo") var pkHost1Photo: String = "",
    @get:PropertyName("pkHost1Score") @set:PropertyName("pkHost1Score") var pkHost1Score: Long = 0L,
    @get:PropertyName("pkHost2Uid") @set:PropertyName("pkHost2Uid") var pkHost2Uid: String = "",
    @get:PropertyName("pkHost2Name") @set:PropertyName("pkHost2Name") var pkHost2Name: String = "",
    @get:PropertyName("pkHost2Photo") @set:PropertyName("pkHost2Photo") var pkHost2Photo: String = "",
    @get:PropertyName("pkHost2Score") @set:PropertyName("pkHost2Score") var pkHost2Score: Long = 0L,
    @get:PropertyName("pkStartTime") @set:PropertyName("pkStartTime") var pkStartTime: Long = 0L,
    @get:PropertyName("pkDurationSec") @set:PropertyName("pkDurationSec") var pkDurationSec: Int = 300,
    @get:PropertyName("pkStatus") @set:PropertyName("pkStatus") var pkStatus: String = "idle",
    @get:PropertyName("pkWinnerUid") @set:PropertyName("pkWinnerUid") var pkWinnerUid: String = "",
    // Room Mode: "audio" (10-mic stage), "video" (1-4 people video streaming), "video_pk" (1v1 split video battle)
    @get:PropertyName("roomType") @set:PropertyName("roomType") var roomType: String = "audio",
    @get:PropertyName("videoLayout") @set:PropertyName("videoLayout") var videoLayout: String = "1p"
) {
    val isVipRoom: Boolean get() = privacy.equals("vip_only", ignoreCase = true)
    val isVideoRoom: Boolean get() = roomType == "video" || roomType == "video_pk"
}

data class LiveEmojiReaction(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("userName") @set:PropertyName("userName") var userName: String = "",
    @get:PropertyName("userPhoto") @set:PropertyName("userPhoto") var userPhoto: String = "",
    @get:PropertyName("emoji") @set:PropertyName("emoji") var emoji: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = 0L
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
    val id = doc.id.ifBlank { doc.getString("roomId") ?: doc.getString("id") ?: "" }
    val titleField = doc.getString("title")
    val roomNameField = doc.getString("roomName")
    val title = if (!titleField.isNullOrBlank()) titleField else (if (!roomNameField.isNullOrBlank()) roomNameField else "Live Hangout")
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
    val isHostLive = doc.getBoolean("isHostLive") ?: doc.getBoolean("hostLive") ?: doc.getBoolean("isLive") ?: true
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
    val isLive = doc.getBoolean("isLive") ?: true

    val announcement = doc.getString("announcement") ?: ""
    val isLocked = doc.getBoolean("isLocked") ?: doc.getBoolean("locked") ?: false
    val trackStartedAt = doc.getLong("trackStartedAt") ?: 0L

    val isPkActive = doc.getBoolean("isPkActive") ?: false
    val pkHost1Uid = doc.getString("pkHost1Uid") ?: ""
    val pkHost1Name = doc.getString("pkHost1Name") ?: ""
    val pkHost1Photo = doc.getString("pkHost1Photo") ?: ""
    val pkHost1Score = doc.getLong("pkHost1Score") ?: 0L
    val pkHost2Uid = doc.getString("pkHost2Uid") ?: ""
    val pkHost2Name = doc.getString("pkHost2Name") ?: ""
    val pkHost2Photo = doc.getString("pkHost2Photo") ?: ""
    val pkHost2Score = doc.getLong("pkHost2Score") ?: 0L
    val pkStartTime = doc.getLong("pkStartTime") ?: 0L
    val pkDurationSec = (doc.getLong("pkDurationSec") ?: 300L).toInt()
    val pkStatus = doc.getString("pkStatus") ?: "idle"
    val pkWinnerUid = doc.getString("pkWinnerUid") ?: ""
    val roomType = doc.getString("roomType") ?: "audio"
    val videoLayout = doc.getString("videoLayout") ?: "1p"

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
        isLocked = isLocked,
        trackStartedAt = trackStartedAt,
        isPkActive = isPkActive,
        pkHost1Uid = pkHost1Uid,
        pkHost1Name = pkHost1Name,
        pkHost1Photo = pkHost1Photo,
        pkHost1Score = pkHost1Score,
        pkHost2Uid = pkHost2Uid,
        pkHost2Name = pkHost2Name,
        pkHost2Photo = pkHost2Photo,
        pkHost2Score = pkHost2Score,
        pkStartTime = pkStartTime,
        pkDurationSec = pkDurationSec,
        pkStatus = pkStatus,
        pkWinnerUid = pkWinnerUid,
        roomType = roomType,
        videoLayout = videoLayout
    )
}

val CATEGORY_BANNERS = mapOf(
    "video_stream" to "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=1200&q=80",
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
    SubscriptionPlanItem("weekly", "Weekly Pass", "₹65", "/ wk", "Basic Matching & Daily Swipes"),
    SubscriptionPlanItem("monthly", "Monthly Pass", "₹159", "/ mo", "Unlimited Likes & Swipes"),
    SubscriptionPlanItem("platinum_dating", "Platinum Dating Pass", "₹199", "/ mo", "Profile Boost & Priority Swipes"),
    SubscriptionPlanItem("dating_vip", "Dating VIP Pass", "₹319", "/ mo", "Direct Msg + Visitors (Dating Only)", isPopular = true)
)

data class PkSupporter(
    val uid: String = "",
    val name: String = "",
    val photo: String = "",
    val points: Long = 0L,
    val rank: Int = 1
)

data class LevelTierInfo(
    val tierName: String,
    val level: Int,
    val badgeIcon: String,
    val gradientColors: List<Color>,
    val crownEmoji: String,
    val minXp: Long,
    val maxXp: Long,
    val badgeDrawableRes: Int = R.drawable.badge_level_bronze
)

object LevelBadgeSystem {
    // 90% Progression Scaling: LiveTogather users reach milestone levels 10% faster (90 days vs 100 days)
    private fun scale90(baseXp: Long): Long = (baseXp * 90L) / 100L

    fun getXpThresholds(level: Int): Pair<Long, Long> {
        val safeLvl = level.coerceIn(1, 120)
        val rawMinXp = when {
            safeLvl <= 1 -> 0L
            safeLvl <= 5 -> (safeLvl - 1) * 250L
            safeLvl <= 10 -> 1000L + (safeLvl - 5) * 800L
            safeLvl <= 15 -> 5000L + (safeLvl - 10) * 1400L
            safeLvl <= 20 -> 12000L + (safeLvl - 15) * 2000L
            safeLvl <= 30 -> 22000L + (safeLvl - 20) * 2800L
            safeLvl <= 50 -> 50000L + (safeLvl - 30) * 7500L
            safeLvl <= 75 -> 200000L + (safeLvl - 50) * 10000L
            safeLvl <= 99 -> 450000L + (safeLvl - 75) * 45000L
            else -> 1530000L + (safeLvl - 100) * 100000L
        }
        val minXp = if (rawMinXp == 0L) 0L else scale90(rawMinXp)

        val nextLevel = safeLvl + 1
        val rawNextMinXp = when {
            nextLevel <= 5 -> (nextLevel - 1) * 250L
            nextLevel <= 10 -> 1000L + (nextLevel - 5) * 800L
            nextLevel <= 15 -> 5000L + (nextLevel - 10) * 1400L
            nextLevel <= 20 -> 12000L + (nextLevel - 15) * 2000L
            nextLevel <= 30 -> 22000L + (nextLevel - 20) * 2800L
            nextLevel <= 50 -> 50000L + (nextLevel - 30) * 7500L
            nextLevel <= 75 -> 200000L + (nextLevel - 50) * 10000L
            nextLevel <= 99 -> 450000L + (nextLevel - 75) * 45000L
            else -> 1530000L + (nextLevel - 100) * 100000L
        }
        val nextMinXp = scale90(rawNextMinXp)
        return Pair(minXp, nextMinXp)
    }

    fun calculateLevelFromXp(xp: Long): Int {
        if (xp <= 0L) return 1
        for (lvl in 100 downTo 1) {
            val (minXp, _) = getXpThresholds(lvl)
            if (xp >= minXp) return lvl
        }
        return 1
    }

    fun getLevelTierInfo(level: Int): LevelTierInfo {
        val safeLvl = level.coerceAtLeast(1)
        val (minXp, maxXp) = getXpThresholds(safeLvl)
        return when {
            safeLvl <= 15 -> LevelTierInfo(
                tierName = "Bronze Explorer",
                level = safeLvl,
                badgeIcon = "🛡️",
                gradientColors = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)),
                crownEmoji = "🥉",
                minXp = minXp,
                maxXp = maxXp,
                badgeDrawableRes = R.drawable.badge_level_bronze
            )
            safeLvl <= 30 -> LevelTierInfo(
                tierName = "Sapphire Knight",
                level = safeLvl,
                badgeIcon = "⚡",
                gradientColors = listOf(Color(0xFF1565C0), Color(0xFF00E5FF)),
                crownEmoji = "🥈",
                minXp = minXp,
                maxXp = maxXp,
                badgeDrawableRes = R.drawable.badge_level_sapphire
            )
            safeLvl <= 50 -> LevelTierInfo(
                tierName = "Royal Star VIP",
                level = safeLvl,
                badgeIcon = "👑",
                gradientColors = listOf(Color(0xFF6A1B9A), Color(0xFFFFD700)),
                crownEmoji = "⭐",
                minXp = minXp,
                maxXp = maxXp,
                badgeDrawableRes = R.drawable.badge_level_royal
            )
            safeLvl <= 75 -> LevelTierInfo(
                tierName = "Crimson Dragon",
                level = safeLvl,
                badgeIcon = "🐉",
                gradientColors = listOf(Color(0xFFB71C1C), Color(0xFFFF6D00)),
                crownEmoji = "🔥",
                minXp = minXp,
                maxXp = maxXp,
                badgeDrawableRes = R.drawable.badge_level_dragon
            )
            safeLvl <= 99 -> LevelTierInfo(
                tierName = "Cyber Diamond",
                level = safeLvl,
                badgeIcon = "💎",
                gradientColors = listOf(Color(0xFF00838F), Color(0xFF80DEEA)),
                crownEmoji = "💠",
                minXp = minXp,
                maxXp = maxXp,
                badgeDrawableRes = R.drawable.badge_level_diamond
            )
            else -> LevelTierInfo(
                tierName = "Sovereign Emperor",
                level = safeLvl,
                badgeIcon = "🔥",
                gradientColors = listOf(Color(0xFFFF8F00), Color(0xFFFFD700), Color(0xFFFF1744)),
                crownEmoji = "👑",
                minXp = minXp,
                maxXp = maxXp,
                badgeDrawableRes = R.drawable.badge_level_emperor
            )
        }
    }

    fun isSpecialHighTierUser(name: String): Boolean {
        val lower = name.lowercase()
        return lower.contains("yasmin") || lower.contains("warsi") || lower.contains("devil")
    }

    fun getEffectiveWealthLevel(user: UserProfile): Int {
        val lower = user.name.lowercase()
        return when {
            lower.contains("yasmin") || lower.contains("warsi") -> maxOf(35, user.wealthLevel)
            lower.contains("devil") -> maxOf(70, user.wealthLevel)
            user.wealthLevel > 1 -> user.wealthLevel
            user.wealthXp > 0L -> calculateLevelFromXp(user.wealthXp)
            else -> 1
        }
    }

    fun getEffectiveWealthXp(user: UserProfile): Long {
        val lower = user.name.lowercase()
        return when {
            lower.contains("yasmin") || lower.contains("warsi") -> maxOf(65000L, user.wealthXp)
            lower.contains("devil") -> maxOf(380000L, user.wealthXp)
            else -> user.wealthXp
        }
    }

    fun getEffectiveCharmLevel(user: UserProfile): Int {
        val lower = user.name.lowercase()
        return when {
            lower.contains("yasmin") || lower.contains("warsi") -> maxOf(32, user.charmLevel)
            lower.contains("devil") -> maxOf(55, user.charmLevel)
            user.charmLevel > 1 -> user.charmLevel
            user.charmXp > 0L -> calculateLevelFromXp(user.charmXp)
            else -> 1
        }
    }

    fun getEffectiveCharmXp(user: UserProfile): Long {
        val lower = user.name.lowercase()
        return when {
            lower.contains("yasmin") || lower.contains("warsi") -> maxOf(52000L, user.charmXp)
            lower.contains("devil") -> maxOf(240000L, user.charmXp)
            else -> user.charmXp
        }
    }

    fun getHostCommissionPercent(level: Int): Double {
        return when {
            level > 70 -> 8.0   // 8% for Level > 70
            level >= 50 -> 7.0  // 7% for Level 50..70
            else -> 5.0         // 5% for Level < 50
        }
    }

    fun calculatePkLevel(pkWins: Int): Int {
        return when {
            pkWins >= 1500 -> 100
            pkWins >= 1000 -> 80
            pkWins >= 700  -> 65
            pkWins >= 450  -> 50
            pkWins >= 250  -> 40
            pkWins >= 100  -> 30
            pkWins >= 50   -> 20
            pkWins >= 30   -> 10
            pkWins >= 12   -> 5
            pkWins >= 5    -> 3
            pkWins >= 1    -> 2
            else           -> 1
        }
    }
}

data class UserProfile(
    val uid: String = "",
    val userId: String = "",
    val email: String = "",
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
    val nameLower: String = "",
    val activeFrame: String = "",
    val activeMicEffect: String = "",
    val ownedMicEffects: List<String> = emptyList(),
    val ownedFrames: List<String> = emptyList(),
    val frameExpiries: Map<String, Long> = emptyMap(),
    val micEffectExpiries: Map<String, Long> = emptyMap(),
    val totalRoomTimeSeconds: Long = 0L,
    val hasRecharged: Boolean = false,
    val rechargeCount: Int = 0,
    val diamondsSpentCount: Int = 0,
    val backpack: Map<String, Int> = emptyMap(), // giftId -> quantity

    // First-Time Sign Up Welcome Bonus & 7-Day VIP 1 Trial
    val vipTrialStartDate: Long = 0L,
    val vipTrialExpiryDate: Long = 0L,
    val hasClaimedWelcomeBonus: Boolean = false,
    val signupIpAddress: String = "",

    // Administration & Role Control
    val isAdmin: Boolean = false,
    val bestFriends: List<String> = emptyList(),
    val adminInRoomIds: List<String> = emptyList(),

    // Suspension & Moderation Control
    val isSuspended: Boolean = false,
    val suspensionExpiry: Long = 0L,
    val suspensionReason: String = "",

    // Level-Up Prestige & Broadcaster Charm System
    val wealthLevel: Int = 1,
    val wealthXp: Long = 0L,
    val charmLevel: Int = 1,
    val charmXp: Long = 0L,

    // Real-Time PK Battle Swords Level & Progression
    val pkLevel: Int = 1,
    val pkWins: Int = 0,
    val pkScore: Long = 0L,

    // Host Earning Wallet & Commission System (7% for Lv.50-70, 8% for Lv.70+)
    val earningsInr: Double = 0.0,
    val totalEarningsInr: Double = 0.0,
    val withdrawnInr: Double = 0.0,
    val upiId: String = "",
    val isHostMonetized: Boolean = false,
    val hostMonetizationDate: Long = 0L,
    val emailPrivacy: String = "only_me",
    val locationPrivacy: String = "public", // "public", "friends", "only_me"
    val hasPasswordSet: Boolean = false,

    // Real-Time 20s Voice Verification & Voice Intro System
    val voiceVerificationStatus: String = "unverified", // "unverified", "pending", "verified"
    val voiceIntroUrl: String = "",
    val voiceIntroDuration: Int = 20,
    val isVoicePrivate: Boolean = false,

    // LiveTogather Appointed Staff & Super Power Role
    // Values: "analyzer", "volunteer", "safety_patrol", or ""
    val officialRole: String = ""
) {
    val level: Int get() = maxOf(wealthLevel, charmLevel, pkLevel)
    fun getEffectiveWealthLevel(): Int = LevelBadgeSystem.getEffectiveWealthLevel(this)
    fun getEffectiveWealthXp(): Long = LevelBadgeSystem.getEffectiveWealthXp(this)
    fun getEffectiveCharmLevel(): Int = LevelBadgeSystem.getEffectiveCharmLevel(this)
    fun getEffectiveCharmXp(): Long = LevelBadgeSystem.getEffectiveCharmXp(this)
    fun getEffectivePkLevel(): Int = LevelBadgeSystem.calculatePkLevel(pkWins)
    fun getWealthTierInfo(): LevelTierInfo = LevelBadgeSystem.getLevelTierInfo(getEffectiveWealthLevel())
    fun getCharmTierInfo(): LevelTierInfo = LevelBadgeSystem.getLevelTierInfo(getEffectiveCharmLevel())

    fun isOfficialAdmin(authEmail: String? = null): Boolean {
        val checkEmail = email.ifBlank { authEmail ?: "" }
        return isUserWhitelistedAdmin(checkEmail, uid)
    }

    fun isFounder(): Boolean {
        val clean = email.lowercase().trim()
        return officialRole.equals("founder", ignoreCase = true) ||
               clean == "service.secure822@gmail.com" ||
               clean == "servicesecure822@gmail.com"
    }

    fun isCeo(): Boolean {
        val clean = email.lowercase().trim()
        return officialRole.equals("ceo", ignoreCase = true) || isFounder()
    }

    fun isStaffMember(): Boolean = officialRole.isNotBlank() || isOfficialAdmin(email)
    fun isOfficialAnalyzer(): Boolean = officialRole.equals("analyzer", ignoreCase = true)
    fun isCommunityVolunteer(): Boolean = officialRole.equals("volunteer", ignoreCase = true)
    fun isSafetyPatrol(): Boolean = officialRole.equals("safety_patrol", ignoreCase = true)

    fun getOfficialRoleBadgeRes(): Int? = when (officialRole.lowercase()) {
        "founder" -> R.drawable.badge_livetogather_founder
        "ceo" -> R.drawable.badge_livetogather_ceo
        "analyzer" -> R.drawable.badge_official_analyzer
        "volunteer" -> R.drawable.badge_official_volunteer
        "safety_patrol" -> R.drawable.badge_official_safety_patrol
        else -> if (isFounder()) R.drawable.badge_livetogather_founder
                else if (isCeo()) R.drawable.badge_livetogather_ceo
                else if (isOfficialAdmin(email)) R.drawable.badge_livetogather_founder else null
    }

    fun getOfficialRoleTitle(): String = when (officialRole.lowercase()) {
        "founder" -> "LiveTogather Founder"
        "ceo" -> "LiveTogather CEO"
        "analyzer" -> "Official Analyzer"
        "volunteer" -> "Community Volunteer"
        "safety_patrol" -> "Safety Patrol"
        else -> if (isFounder()) "LiveTogather Founder"
                else if (isCeo()) "LiveTogather CEO"
                else if (isOfficialAdmin(email)) "Supreme Founder" else ""
    }

    fun getOfficialEntranceReason(): String = when (officialRole.lowercase()) {
        "founder" -> "LiveTogather Founder has entered the room. Supreme Authority Active."
        "ceo" -> "LiveTogather CEO has entered the room. Supreme Leadership Active."
        "analyzer" -> "Entered room to inspect audio quality and audit lounge standards."
        "volunteer" -> "Entered room to welcome members and guide community interactions."
        "safety_patrol" -> "Entered room to monitor conduct standards and safeguard community rules."
        else -> if (isFounder()) "LiveTogather Founder has entered the room. Supreme Authority Active."
                else "Entered room as LiveTogather Appointed Staff."
    }

    fun isMutualBestFriend(otherUser: UserProfile?): Boolean {
        if (otherUser == null) return false
        if (otherUser.uid == this.uid) return true
        return this.bestFriends.contains(otherUser.uid) && otherUser.bestFriends.contains(this.uid)
    }

    fun getDisplayLocation(viewerUser: UserProfile?): String {
        if (viewerUser != null && viewerUser.uid == this.uid) {
            val loc = locationName.ifBlank { city }.ifBlank { "LiveTogather" }
            val privTag = when (locationPrivacy) {
                "only_me" -> "(Only Me 🔒)"
                "friends" -> "(Friends 👥)"
                else -> "(Public 🌐)"
            }
            return "📍 $loc $privTag"
        }
        return when (locationPrivacy) {
            "only_me" -> "📍 Private 🔒"
            "friends" -> if (viewerUser != null && isMutualBestFriend(viewerUser)) {
                val loc = locationName.ifBlank { city }.ifBlank { "LiveTogather" }
                "📍 $loc"
            } else {
                "📍 Friends Only 🔒"
            }
            else -> {
                val loc = locationName.ifBlank { city }.ifBlank { "LiveTogather" }
                "📍 $loc"
            }
        }
    }

    fun isDatingSubscription(): Boolean {
        val p = plan.lowercase().trim()
        return p.contains("dating") || p.contains("match") || p.contains("pass") || p.contains("weekly") || p.contains("monthly") || p == "vip level ★" || p == "vip"
    }

    fun isPaidSubscriber(): Boolean {
        return plan.isNotBlank() && !plan.equals("Free", ignoreCase = true)
    }

    fun isDatingVip(): Boolean {
        return (plan.contains("VIP", ignoreCase = true) || plan == "dating_vip") && isDatingSubscription()
    }

    fun isVipOrPlatinum(): Boolean {
        // Dating subscriptions are strictly for dating/matching and NEVER grant broadcast VIP status
        if (isDatingSubscription()) return false
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

        // Dating subscriptions strictly DO NOT grant any broadcast VIP level or broadcast crown
        if (isDatingSubscription()) {
            return if (isTrialActive) 1 else 0
        }

        if (plan.contains("VIP 6", ignoreCase = true)) return 6
        if (plan.contains("VIP 5", ignoreCase = true)) return 5
        if (plan.contains("VIP 4", ignoreCase = true)) return 4
        if (plan.contains("VIP 3", ignoreCase = true)) return 3
        if (plan.contains("VIP 2", ignoreCase = true)) return 2
        if (plan.contains("VIP 1", ignoreCase = true)) return 1
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
    val email = doc.getString("email") ?: ""
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
    val rawPlan = doc.getString("plan") ?: "Free"
    val plan = if (rawPlan.equals("VIP LEVEL ★", ignoreCase = true) || rawPlan.equals("vip", ignoreCase = true)) {
        "Dating VIP Pass"
    } else rawPlan
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
    val nameLower = doc.getString("nameLower") ?: name.lowercase().trim()
    val activeFrame = doc.getString("activeFrame") ?: ""
    @Suppress("UNCHECKED_CAST")
    val ownedFrames = (doc.get("ownedFrames") as? List<String>) ?: emptyList()
    val activeMicEffect = doc.getString("activeMicEffect") ?: ""
    @Suppress("UNCHECKED_CAST")
    val ownedMicEffects = (doc.get("ownedMicEffects") as? List<String>) ?: emptyList()
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

    @Suppress("UNCHECKED_CAST")
    val rawMicExpiries = doc.get("micEffectExpiries") as? Map<String, Any>
    val micEffectExpiries: Map<String, Long> = rawMicExpiries?.mapNotNull { (k, v) ->
        val l = (v as? Number)?.toLong()
        if (l != null) k to l else null
    }?.toMap() ?: emptyMap()

    val totalRoomTimeSeconds = doc.getLong("totalRoomTimeSeconds") ?: 0L
    val hasRecharged = doc.getBoolean("hasRecharged") ?: false
    val rechargeCount = (doc.getLong("rechargeCount") ?: 0L).toInt()
    val diamondsSpentCount = (doc.getLong("diamondsSpentCount") ?: 0L).toInt()
    val isSuspended = doc.getBoolean("isSuspended") ?: false
    val suspensionExpiry = doc.getLong("suspensionExpiry") ?: 0L
    val suspensionReason = doc.getString("suspensionReason") ?: ""

    val wealthLevel = (doc.getLong("wealthLevel") ?: 1L).toInt()
    val wealthXp = doc.getLong("wealthXp") ?: 0L
    val charmLevel = (doc.getLong("charmLevel") ?: 1L).toInt()
    val charmXp = doc.getLong("charmXp") ?: 0L

    val pkWins = (doc.getLong("pkWins") ?: 0L).toInt()
    val pkScore = doc.getLong("pkScore") ?: 0L
    val rawPkLvl = doc.getLong("pkLevel")?.toInt()
    val pkLevel = rawPkLvl?.takeIf { it > 1 } ?: LevelBadgeSystem.calculatePkLevel(pkWins)

    val earningsInr = doc.getDouble("earningsInr") ?: (doc.getLong("earningsInr")?.toDouble() ?: 0.0)
    val totalEarningsInr = doc.getDouble("totalEarningsInr") ?: (doc.getLong("totalEarningsInr")?.toDouble() ?: earningsInr)
    val withdrawnInr = doc.getDouble("withdrawnInr") ?: (doc.getLong("withdrawnInr")?.toDouble() ?: 0.0)
    val upiId = doc.getString("upiId") ?: ""
    val isHostMonetized = doc.getBoolean("isHostMonetized") ?: (isAdmin || name.contains("Devil", ignoreCase = true))
    val hostMonetizationDate = doc.getLong("hostMonetizationDate") ?: 0L
    val emailPrivacy = doc.getString("emailPrivacy") ?: "only_me"
    val locationPrivacy = doc.getString("locationPrivacy") ?: "public"
    val hasPasswordSet = doc.getBoolean("hasPasswordSet") ?: false
    val officialRole = doc.getString("officialRole") ?: ""

    return UserProfile(
        uid = uid,
        userId = userId,
        email = email,
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
        nameLower = nameLower,
        activeFrame = activeFrame,
        ownedFrames = ownedFrames,
        activeMicEffect = activeMicEffect,
        ownedMicEffects = ownedMicEffects,
        frameExpiries = frameExpiries,
        micEffectExpiries = micEffectExpiries,
        totalRoomTimeSeconds = totalRoomTimeSeconds,
        hasRecharged = hasRecharged,
        rechargeCount = rechargeCount,
        diamondsSpentCount = diamondsSpentCount,
        vipTrialStartDate = vipTrialStartDate,
        vipTrialExpiryDate = vipTrialExpiryDate,
        hasClaimedWelcomeBonus = hasClaimedWelcomeBonus,
        signupIpAddress = signupIpAddress,
        isAdmin = isAdmin,
        isSuspended = isSuspended,
        suspensionExpiry = suspensionExpiry,
        suspensionReason = suspensionReason,
        wealthLevel = wealthLevel,
        wealthXp = wealthXp,
        charmLevel = charmLevel,
        charmXp = charmXp,
        pkLevel = pkLevel,
        pkWins = pkWins,
        pkScore = pkScore,
        earningsInr = earningsInr,
        totalEarningsInr = totalEarningsInr,
        withdrawnInr = withdrawnInr,
        upiId = upiId,
        isHostMonetized = isHostMonetized,
        hostMonetizationDate = hostMonetizationDate,
        emailPrivacy = emailPrivacy,
        hasPasswordSet = hasPasswordSet,
        locationPrivacy = locationPrivacy,
        officialRole = officialRole
    )
}

fun saveProfileToPrefs(prefs: SharedPreferences, user: UserProfile) {
    prefs.edit()
        .putString("user_uid", user.uid)
        .putString("profile_user_id", user.userId)
        .putString("profile_email", user.email)
        .putString("profile_device_id", user.deviceId)
        .putString("profile_name", user.name)
        .putInt("profile_age", user.age)
        .putString("profile_gender", user.gender)
        .putString("profile_city", user.city)
        .putString("profile_location_privacy", user.locationPrivacy)
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
        .putInt("profile_wealth_level", user.wealthLevel)
        .putLong("profile_wealth_xp", user.wealthXp)
        .putInt("profile_charm_level", user.charmLevel)
        .putLong("profile_charm_xp", user.charmXp)
        .putInt("profile_pk_level", user.pkLevel)
        .putInt("profile_pk_wins", user.pkWins)
        .putLong("profile_pk_score", user.pkScore)
        .putString("profile_earnings_inr", user.earningsInr.toString())
        .putString("profile_total_earnings_inr", user.totalEarningsInr.toString())
        .putString("profile_withdrawn_inr", user.withdrawnInr.toString())
        .putString("profile_upi_id", user.upiId)
        .putBoolean("profile_is_host_monetized", user.isHostMonetized)
        .putLong("profile_host_monetization_date", user.hostMonetizationDate)
        .putString("profile_email_privacy", user.emailPrivacy)
        .putBoolean("profile_has_password_set", user.hasPasswordSet)
        .apply()
}

data class EarningTransaction(
    val id: String = "",
    val type: String = "GIFT_COMMISSION", // "GIFT_COMMISSION", "WITHDRAWAL"
    val title: String = "",
    val amountInr: Double = 0.0,
    val diamonds: Long = 0L,
    val senderName: String = "",
    val upiId: String = "",
    val status: String = "CREDITED", // "CREDITED", "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

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
    val animationType: String = "micro_gift", // "micro_gift", "firecracker_burst", "car", "rocket", "video_mp4"
    val timestamp: Long = System.currentTimeMillis(),
    val targetUserId: String = "",
    val targetUserName: String = "All Mics",
    val quantity: Int = 1,
    val targetMicSeatIndex: Int = -1, // -1 for All Mics, or 0..7 for specific mic seat
    val drawableResId: Int = 0,
    val fromBackpack: Boolean = false
)

data class RoomGiftItem(
    val id: String,
    val name: String,
    val hindiName: String,
    val emoji: String,
    val cost: Long,
    val category: String, // "romantic", "sweet", "party", "vip", "royal"
    val drawableResId: Int,
    val animationType: String = "micro_gift",
    val description: String
)

val ROOM_GIFT_CATALOG = listOf(
    // Tier 1: 1 - 5 Diamonds
    RoomGiftItem("gift_single_rose", "Single Red Rose", "सिंगल रेड रोज़", "🌹", 1L, "romantic", R.drawable.gift_single_rose, "micro_gift", "Blooming red velvet rose with golden stem"),
    RoomGiftItem("gift_lucky_clover", "Lucky Gold Clover", "लकी गोल्ड क्लोवर", "🍀", 2L, "good_luck", R.drawable.gift_lucky_clover, "micro_gift", "24K solid gold 4-leaf clover"),
    RoomGiftItem("gift_swirl_lollipop", "Swirl Candy Lollipop", "कैंडी लॉलीपॉप", "🍭", 3L, "sweet", R.drawable.gift_swirl_lollipop, "micro_gift", "Crystal swirl candy lollipop on stick"),
    RoomGiftItem("gift_floating_kiss", "Floating Kiss & Hearts", "फ्लाईंग किस & हार्ट्स", "💋", 4L, "romantic", R.drawable.gift_floating_kiss, "micro_gift", "3D glossy red lips blowing neon floating hearts"),
    RoomGiftItem("gift_crystal_lily", "Crystal Glowing Lily", "क्रिस्टल ग्लोइंग लिली", "🪷", 5L, "floral", R.drawable.gift_crystal_lily, "micro_gift", "Luminous faceted crystal white lily"),

    // Tier 2: 6 - 15 Diamonds
    RoomGiftItem("gift_coffee_cup", "Hot Heart Coffee", "हॉट कॉफ़ी कप", "☕", 6L, "sweet", R.drawable.gift_coffee_cup, "micro_gift", "Steaming porcelain coffee mug with heart latte art"),
    RoomGiftItem("gift_perfume_bottle", "French Amber Perfume", "फ्रेंच अंबर परफ्यूम", "✨", 7L, "luxury", R.drawable.gift_perfume_bottle, "micro_gift", "Crystal perfume bottle spraying gold mist"),
    RoomGiftItem("gift_rose_bouquet", "99 Red Roses Bouquet", "99 रोज़ेस बुके", "💐", 8L, "romantic", R.drawable.gift_rose_bouquet, "micro_gift", "99 luxury velvet roses with gold foil ribbon"),
    RoomGiftItem("gift_heart_chocolates", "Heart Velvet Chocolates", "हार्ट चॉकलेट बॉक्स", "🍫", 10L, "sweet", R.drawable.gift_heart_chocolates, "micro_gift", "Red velvet heart box with gold-leaf Belgian truffles"),
    RoomGiftItem("gift_teddy_heart", "Teddy with Ruby Heart", "टेडी बेयर विथ रूबी हार्ट", "🧸", 10L, "cute", R.drawable.gift_teddy_heart, "micro_gift", "Fluffy mohair teddy holding glowing ruby heart"),
    RoomGiftItem("gift_firecracker_bomb", "Celebration Firecracker", "पटाखा सेलिब्रेशन बॉम्ब", "💥", 12L, "party", R.drawable.gift_firecracker_bomb, "firecracker_burst", "Golden firecracker bomb with sparkling fuse"),
    RoomGiftItem("gift_chocolate_bomb", "Molten Chocolate Bomb", "चॉकलेट बॉम्ब", "💣", 15L, "sweet", R.drawable.gift_chocolate_bomb, "micro_gift", "Belgian chocolate sphere cracking with molten caramel"),

    // Tier 3: 18 - 30 Diamonds
    RoomGiftItem("gift_magic_wand", "Magic Star Wand", "मैजिक स्टार वैंड", "🪄", 18L, "magic", R.drawable.gift_magic_wand, "micro_gift", "Golden star wand casting dynamic stardust spiral"),
    RoomGiftItem("gift_golden_mic", "Vintage Golden Mic", "गोल्डन माइक", "🎙️", 20L, "music", R.drawable.gift_golden_mic, "micro_gift", "Solid 24k gold classic studio vocal microphone"),
    RoomGiftItem("gift_pearl_necklace", "Royal Pearl Necklace", "रॉयल पर्ल नेकलेस", "📿", 22L, "luxury", R.drawable.gift_pearl_necklace, "micro_gift", "Lustrous South Sea pearls in navy velvet case"),
    RoomGiftItem("gift_acoustic_guitar", "Acoustic Rosewood Guitar", "एकोस्टिक गिटार", "🎸", 24L, "music", R.drawable.gift_acoustic_guitar, "micro_gift", "Handcrafted polished rosewood acoustic guitar"),
    RoomGiftItem("gift_golden_crown", "Imperial Gold Crown", "इम्पीरियल रॉयल क्राउन", "👑", 25L, "vip", R.drawable.gift_golden_crown, "micro_gift", "Solid gold crown with rubies & emeralds"),
    RoomGiftItem("gift_champagne_pop", "Popping Champagne", "शैम्पेन बॉटल पॉप", "🍾", 28L, "party", R.drawable.gift_champagne_pop, "micro_gift", "Popping vintage champagne bottle with erupting bubbles"),
    RoomGiftItem("gift_diamond_ring", "Solitaire Diamond Ring", "सॉलिटेयर डायमंड रिंग", "💍", 30L, "vip", R.drawable.gift_diamond_ring, "micro_gift", "Brilliant-cut diamond ring in midnight blue velvet box"),

    // Tier 4: 32 - 50 Diamonds
    RoomGiftItem("gift_gold_watch", "Swiss Chrono Gold Watch", "गोल्डन स्पोर्ट्स वॉच", "⌚", 32L, "luxury", R.drawable.gift_gold_watch, "micro_gift", "18k gold tourbillon chronograph sports watch"),
    RoomGiftItem("gift_fireworks_rocket", "Fireworks Sky Rocket", "रॉकेट फायरवर्क्स", "🚀", 35L, "party", R.drawable.gift_fireworks_rocket, "firecracker_burst", "Multi-shot festive fireworks launcher battery"),
    RoomGiftItem("gift_sports_car", "Neon Exotic Supercar", "नियॉन स्पोर्ट्स कार", "🏎️", 40L, "luxury", R.drawable.gift_sports_car, "car", "Candy-apple red supercar with cyan neon underglow"),
    RoomGiftItem("gift_angel_wings", "Luminous Angel Wings", "गोल्डन एंजेल विंग्स", "🪽", 42L, "divine", R.drawable.gift_angel_wings, "micro_gift", "Giant 3D feathered wings of glowing golden light"),
    RoomGiftItem("gift_crystal_carriage", "Crystal Royal Carriage", "क्रिस्टल बग्गी कैरिज", "🪞", 45L, "royal", R.drawable.gift_crystal_carriage, "micro_gift", "Cinderella-style glowing crystal carriage"),
    RoomGiftItem("gift_golden_phoenix", "Mythical Golden Phoenix", "गोल्डन फीनिक्स", "🦅", 50L, "royal", R.drawable.gift_golden_phoenix, "micro_gift", "Legendary golden firebird with blazing flame aura")
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
    // 1. Cyber Lamborghini (Existing 3D MP4)
    VideoGiftItem("lamborghini_mp4", "Cyber Lamborghini", "🏎️", 30000L, "video_mp4", "Crystal ice luxury hypercar roaring drift", R.raw.lamborgini_car, R.drawable.thumb_lamborgini_car),
    // 2. Fire Phoenix Eagle (Existing 3D MP4)
    VideoGiftItem("eagle_entry_mp4", "Fire Phoenix Eagle", "🦅", 35000L, "video_mp4", "Blazing golden fire phoenix flight", R.raw.eagle_entry, R.drawable.thumb_eagle_entry),
    // 3. Diamond Proposal Ring (NEW 3D MP4)
    VideoGiftItem("diamond_ring_proposal_mp4", "Diamond Proposal Ring", "💍", 40000L, "video_mp4", "Sparkling solitaire diamond ring with prism dispersion", R.raw.diamond_ring_proposal, R.drawable.thumb_diamond_ring_proposal),
    // 4. Blazing Fire Lion (Existing 3D MP4)
    VideoGiftItem("king_lion_mp4", "Blazing Fire Lion", "🦁", 45000L, "video_mp4", "Majestic roaring solar flame lion", R.raw.king_lion_entry, R.drawable.thumb_king_lion_entry),
    // 5. Golden Supercar Drift (NEW 3D MP4)
    VideoGiftItem("golden_super_car_mp4", "Golden Supercar Drift", "🏎️", 50000L, "video_mp4", "High-speed 3D gold sports car power drift", R.raw.golden_super_car, R.drawable.thumb_golden_super_car),
    // 6. Cyber Mecha Dragon (Existing 3D MP4)
    VideoGiftItem("terminator_dragon_mp4", "Cyber Mecha Dragon", "🐉", 60000L, "video_mp4", "Futuristic mecha dragon lightning burst", R.raw.terminator_entry, R.drawable.thumb_terminator_entry),
    // 7. Princess Ruby Necklace (NEW 3D MP4)
    VideoGiftItem("princess_ruby_necklace_mp4", "Princess Ruby Necklace", "💎", 65000L, "video_mp4", "Royal pigeon-blood ruby & diamond necklace", R.raw.princess_ruby_necklace, R.drawable.thumb_princess_ruby_necklace),
    // 8. Rose For Proposal (NEW 3D MP4 - 75k Diamond)
    VideoGiftItem("rose_proposal_mp4", "Rose For Proposal", "🌹", 75000L, "video_mp4", "Romantic blooming velvet rose with gold dew drops", R.raw.rose_proposal, R.drawable.thumb_rose_proposal),
    // 9. Treasure For You (NEW 3D MP4)
    VideoGiftItem("treasure_for_you_mp4", "Treasure For You", "📦", 80000L, "video_mp4", "Royal diamond crown and glittering gold vault", R.raw.treasure_for_you, R.drawable.thumb_treasure_for_you),
    // 10. Imperial King Palace (Existing 3D MP4)
    VideoGiftItem("king_entry_mp4", "Imperial King Palace", "👑", 85000L, "video_mp4", "Royal King Coronation palace walk", R.raw.king_entry, R.drawable.thumb_king_entry),
    // 11. Imperial Lost Treasure (NEW 3D MP4)
    VideoGiftItem("founded_treasure_mp4", "Imperial Lost Treasure", "👑", 90000L, "video_mp4", "Ancient royal gold chest opening with gems and coins", R.raw.founded_treasure, R.drawable.thumb_founded_treasure),
    // 12. Take You On Heaven Ship (NEW 3D MP4 - 100k Diamond)
    VideoGiftItem("heaven_ship_mp4", "Take You On Heaven Ship", "🛥️", 100000L, "video_mp4", "Billionaire mega yacht cruising with sky fireworks", R.raw.heaven_ship, R.drawable.thumb_heaven_ship),
    // 13. Will Take You On Jet (NEW 3D MP4 - 120k Diamond)
    VideoGiftItem("royal_jet_mp4", "Will Take You On Jet", "✈️", 120000L, "video_mp4", "Supersonic luxury private jet soaring into sunset", R.raw.royal_jet, R.drawable.thumb_royal_jet)
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
    val timestamp: Long = System.currentTimeMillis(),
    val isUnlocked: Boolean = false,
    val unlockedBy: String = "",
    val unlockGiftAmount: Long = 0L,
    val mediaSharingAllowed: Map<String, Boolean> = emptyMap(),
    val mediaPermissionRequested: List<String> = emptyList(),
    val intimacyLevel: Int = 1,
    val intimacyXp: Long = 0L,
    val chatStreakDays: Int = 1,
    val lastChatDate: String = ""
)

data class ChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "text", // "text", "image", "audio", "proposal", "unlock_gift"
    val mediaUrl: String = "",
    val audioUrl: String = "",
    val audioDurationSec: Int = 0,
    val giftAmount: Long = 0L,
    val proposalBondType: String = "",
    val proposalStatus: String = "" // "pending", "accepted", "declined"
)

data class IntimacyBond(
    val bondId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val user1Name: String = "",
    val user2Name: String = "",
    val user1Photo: String = "",
    val user2Photo: String = "",
    val bondType: String = "bestie",      // "couple", "bestie", "soulmate", "eternal"
    val intimacyLevel: Int = 1,           // 1 to 100
    val intimacyXp: Long = 0L,
    val streakDays: Int = 1,
    val lastInteractionDate: String = "",
    val activeMicLinkId: String = "mic_link_ruby_crown",
    val status: String = "active",        // "active", "pending", "terminated"
    val createdAt: Long = System.currentTimeMillis()
)

data class IntimacyProposal(
    val proposalId: String = "",
    val matchId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val receiverUid: String = "",
    val receiverName: String = "",
    val receiverPhoto: String = "",
    val bondType: String = "bestie",
    val status: String = "pending",       // "pending", "accepted", "declined"
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
    val tier: String = "Standard", // "VIP", "Platinum", "Standard"
    @get:PropertyName("agoraUid") @set:PropertyName("agoraUid") var agoraUid: Int = 0
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
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?auto=format&fit=crop&w=500&q=80",
        durationSec = 372
    ),
    HangoutTrack(
        id = "track_2",
        title = "Coffee Break Chill Cafe",
        artist = "FM STAR Lounge",
        category = "Lo-Fi Chill",
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=500&q=80",
        durationSec = 425
    ),

    // 2. Cafe Jazz & Lounge (Full Length)
    HangoutTrack(
        id = "track_3",
        title = "Soulful Jazz Whispers",
        artist = "Velvet Sax Quartet",
        category = "Cafe Jazz",
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?auto=format&fit=crop&w=500&q=80",
        durationSec = 344
    ),
    HangoutTrack(
        id = "track_4",
        title = "Gentle Sunset Bossa",
        artist = "Lounge Collective",
        category = "Cafe Jazz",
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=500&q=80",
        durationSec = 302
    ),

    // 3. Ambient Hangout
    HangoutTrack(
        id = "track_5",
        title = "Midnight Velvet Groove",
        artist = "Velvet Sax Quartet",
        category = "Cafe Jazz",
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?auto=format&fit=crop&w=500&q=80",
        durationSec = 325
    ),
    HangoutTrack(
        id = "track_6",
        title = "Sunset Chill Bossa",
        artist = "Lounge Collective",
        category = "Cafe Jazz",
        url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=500&q=80",
        durationSec = 372
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
    RoomThemePreset("theme_vip_royal", "👑 VIP Royal Velvet Palace", "vip_royal_stage"),
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
    AvatarFrameItem(
        id = "frame_livetogather_founder",
        name = "LiveTogather Founder Sovereign",
        description = "Flowing 24K gold wings, ruby imperial crown & diamond-encrusted celestial bezel",
        cost = 0L,
        drawableRes = R.drawable.frame_livetogather_founder,
        isAnimated = true,
        vipLevel = 0,
        auraColors = listOf(0xFFFFD700, 0xFFFF0055, 0xFFFFEA00, 0xFFFFD700)
    ),
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






// ---------------------------------------------------------------------
// LUXURY REALISTIC HD STUDIO AVATAR CATALOG (NO CARTOONS)
// ---------------------------------------------------------------------

data class LuxuryAvatarPreset(
    val id: String,
    val name: String,
    val gender: String, // "Female" or "Male"
    val category: String, // "VIP Royalty", "Studio Aesthetic", "Urban Glamour", "Executive & Charisma"
    val imageUrl: String,
    val requiredLevel: Int = 1,
    val badge: String = "STUDIO HD"
)

object LuxuryAvatarCatalog {
    val presets: List<LuxuryAvatarPreset> = listOf(
        // FEMALE PRESETS (10 Realistic HD Studio Portraits)
        LuxuryAvatarPreset(
            id = "av_fem_1",
            name = "Aria Velvet",
            gender = "Female",
            category = "VIP Royalty",
            imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
            badge = "ROYALTY"
        ),
        LuxuryAvatarPreset(
            id = "av_fem_2",
            name = "Serena Rose",
            gender = "Female",
            category = "VIP Royalty",
            imageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80",
            badge = "EXCLUSIVE"
        ),
        LuxuryAvatarPreset(
            id = "av_fem_3",
            name = "Chloe Sapphire",
            gender = "Female",
            category = "Studio Aesthetic",
            imageUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80",
            badge = "STUDIO HD"
        ),
        LuxuryAvatarPreset(
            id = "av_fem_4",
            name = "Kiara Moon",
            gender = "Female",
            category = "Studio Aesthetic",
            imageUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=80",
            badge = "POPULAR"
        ),
        LuxuryAvatarPreset(
            id = "av_fem_5",
            name = "Natasha Gold",
            gender = "Female",
            category = "Urban Glamour",
            imageUrl = "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?auto=format&fit=crop&w=800&q=80",
            badge = "GLAMOUR"
        ),
        LuxuryAvatarPreset(
            id = "av_fem_6",
            name = "Elena Pearl",
            gender = "Female",
            category = "Urban Glamour",
            imageUrl = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=800&q=80",
            badge = "TRENDING"
        ),
        LuxuryAvatarPreset(
            id = "av_fem_7",
            name = "Zara Diamond",
            gender = "Female",
            category = "Executive & Charisma",
            imageUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=800&q=80",
            badge = "EXECUTIVE"
        ),
        LuxuryAvatarPreset(
            id = "av_fem_8",
            name = "Rhea Celestial",
            gender = "Female",
            category = "Executive & Charisma",
            imageUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=800&q=80",
            badge = "CHARISMA"
        ),
        LuxuryAvatarPreset(
            id = "av_fem_9",
            name = "Maya Divine",
            gender = "Female",
            category = "Studio Aesthetic",
            imageUrl = "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=800&q=80",
            badge = "STUDIO HD"
        ),
        LuxuryAvatarPreset(
            id = "av_fem_10",
            name = "Ananya Star",
            gender = "Female",
            category = "VIP Royalty",
            imageUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80",
            badge = "SOVEREIGN"
        ),

        // MALE PRESETS (10 Realistic HD Studio Portraits)
        LuxuryAvatarPreset(
            id = "av_male_1",
            name = "Alexander Crown",
            gender = "Male",
            category = "VIP Royalty",
            imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=800&q=80",
            badge = "ROYALTY"
        ),
        LuxuryAvatarPreset(
            id = "av_male_2",
            name = "Marcus Sterling",
            gender = "Male",
            category = "VIP Royalty",
            imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=800&q=80",
            badge = "EXCLUSIVE"
        ),
        LuxuryAvatarPreset(
            id = "av_male_3",
            name = "Leo Knight",
            gender = "Male",
            category = "Studio Aesthetic",
            imageUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=800&q=80",
            badge = "STUDIO HD"
        ),
        LuxuryAvatarPreset(
            id = "av_male_4",
            name = "Ethan Maverick",
            gender = "Male",
            category = "Studio Aesthetic",
            imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
            badge = "POPULAR"
        ),
        LuxuryAvatarPreset(
            id = "av_male_5",
            name = "Kabir Sovereign",
            gender = "Male",
            category = "Urban Glamour",
            imageUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=800&q=80",
            badge = "GLAMOUR"
        ),
        LuxuryAvatarPreset(
            id = "av_male_6",
            name = "Damian Vance",
            gender = "Male",
            category = "Urban Glamour",
            imageUrl = "https://images.unsplash.com/photo-1496345875659-11f7dd282d1d?auto=format&fit=crop&w=800&q=80",
            badge = "TRENDING"
        ),
        LuxuryAvatarPreset(
            id = "av_male_7",
            name = "Vikram Baron",
            gender = "Male",
            category = "Executive & Charisma",
            imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=800&q=80",
            badge = "EXECUTIVE"
        ),
        LuxuryAvatarPreset(
            id = "av_male_8",
            name = "Julian Drake",
            gender = "Male",
            category = "Executive & Charisma",
            imageUrl = "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?auto=format&fit=crop&w=800&q=80",
            badge = "CHARISMA"
        ),
        LuxuryAvatarPreset(
            id = "av_male_9",
            name = "Zane Phoenix",
            gender = "Male",
            category = "Studio Aesthetic",
            imageUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80",
            badge = "STUDIO HD"
        ),
        LuxuryAvatarPreset(
            id = "av_male_10",
            name = "Reyansh Emperor",
            gender = "Male",
            category = "VIP Royalty",
            imageUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=800&q=80",
            badge = "SOVEREIGN"
        )
    )

    val categories = listOf("All", "VIP Royalty", "Studio Aesthetic", "Urban Glamour", "Executive & Charisma")

    fun canUploadCustomAvatar(userLevel: Int, isVip: Boolean): Boolean {
        return userLevel >= 2 || isVip
    }
}
