package com.devil.livetogather

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CuratedWatchPartyVideo(
    val videoId: String,
    val title: String,
    val category: String
)

object WatchPartyCinemaEngine {
    const val CINEMA_THEME_ID = "cinema_royal_blue_hd"
    const val REQUIRED_ROOM_LEVEL = 5
    const val REQUIRED_FAMILY_ROOM_LEVEL = 10

    private val FOUNDER_UNRESTRICTED_EMAILS = setOf(
        "servicesecure822@gmail.com"
    )

    val CURATED_VIDEOS = listOf(
        CuratedWatchPartyVideo("BddP6PYo2gs", "Kesariya • Bollywood Romantic Hit", "Bollywood"),
        CuratedWatchPartyVideo("gvyUuxdRdR4", "Raataan Lambiyan • Party Jukebox", "Romantic"),
        CuratedWatchPartyVideo("JGwWNGJdvx8", "Shape of You • Global Club Mix", "Party"),
        CuratedWatchPartyVideo("jfKfPfyJRdk", "Lofi Hip Hop Radio • Late Night Chill", "Lo-Fi"),
        CuratedWatchPartyVideo("YR12Z8f1Dh8", "Bollywood Non-Stop Dance Mix", "Dance")
    )

    /**
     * Founder / CEO / Admin (`servicesecure822@gmail.com`) has ZERO criteria or level restrictions.
     * Regular users unlock the Royal Blue Cinema Watch Party Room at Room Level 5+.
     */
    fun isUnrestrictedFounderOrCeo(user: UserProfile): Boolean {
        val emailLower = user.email.trim().lowercase(Locale.US)
        return emailLower == "servicesecure822@gmail.com" ||
            user.isFounder() ||
            user.isCeo() ||
            user.isOfficialAnalyzer() ||
            user.isCommunityVolunteer() ||
            user.isSafetyPatrol() ||
            user.isStaffMember()
    }

    fun canUnlockCinemaWatchParty(user: UserProfile, currentRoomLevel: Int): Boolean {
        if (isUnrestrictedFounderOrCeo(user)) return true
        return currentRoomLevel >= REQUIRED_ROOM_LEVEL
    }

    fun canUnlockFamilyRoom(user: UserProfile, currentRoomLevel: Int): Boolean {
        if (isUnrestrictedFounderOrCeo(user)) return true
        return currentRoomLevel >= REQUIRED_FAMILY_ROOM_LEVEL
    }

    fun updateFamilyRoomState(
        db: FirebaseFirestore,
        room: RoomItem,
        isFamilyActive: Boolean,
        familyMemberIds: List<String> = room.familyMemberIds,
        familyName: String = room.familyName.ifBlank { "${room.hostName.ifBlank { "Royal" }} Family" }
    ) {
        if (room.id.isBlank()) return
        val hostUid = room.hostUid.ifBlank { room.hostId }
        val ensuredMembers = (familyMemberIds + listOfNotNull(hostUid.takeIf { it.isNotBlank() })).distinct()
        val updates = mapOf(
            "isFamilyRoom" to isFamilyActive,
            "familyMemberIds" to ensuredMembers,
            "familyName" to familyName,
            "privacy" to if (isFamilyActive) "family_only" else (if (room.category == "vip_room") "vip_only" else "public")
        )
        db.collection("rooms").document(room.id).update(updates)
        if (room.category.isNotBlank()) {
            db.collection("intent_categories").document(room.category)
                .collection("rooms").document(room.id)
                .update(updates)
        }
    }

    fun isCinemaWatchPartyActive(room: RoomItem): Boolean {
        return !room.isVideoRoom && (
            room.isWatchPartyActive ||
                room.banner_image_url == CINEMA_THEME_ID ||
                room.eventThemeStyle == CINEMA_THEME_ID
            )
    }

    fun extractYouTubeVideoId(rawInput: String): String {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return ""
        val patterns = listOf(
            Regex("(?:v=|/v/|youtu\\.be/|/embed/|/shorts/)([A-Za-z0-9_-]{11})"),
            Regex("^([A-Za-z0-9_-]{11})$")
        )
        for (p in patterns) {
            val match = p.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1]
            }
        }
        return ""
    }

    fun isWelcomeWaveComment(comment: LiveRoomComment): Boolean {
        return comment.systemCommentType == "welcome_wave" ||
            (comment.message.startsWith("👋") && comment.message.contains("welcomed", ignoreCase = true))
    }

    fun isMicInviteComment(comment: LiveRoomComment): Boolean {
        return comment.systemCommentType == "mic_invite"
    }

    /**
     * Sends a real-time Firestore Mic Invitation from Host/Admin to a viewer in the room.
     * The viewer receives an instant live Accept/Decline popup and equips the mic seat upon acceptance.
     */
    fun sendMicInvitation(
        db: FirebaseFirestore,
        roomId: String,
        inviter: UserProfile,
        targetUserId: String,
        targetUserName: String,
        targetSeatIndex: Int
    ) {
        if (roomId.isBlank() || inviter.uid.isBlank() || targetUserId.isBlank()) return
        val inviteId = "micinv_${inviter.uid}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}"
        val inviterName = inviter.name.ifBlank { "Host" }
        val displaySeatNum = targetSeatIndex + 1

        val inviteComment = LiveRoomComment(
            id = inviteId,
            roomId = roomId,
            userId = inviter.uid,
            userName = inviterName,
            userPhoto = inviter.photo,
            tier = inviter.plan.ifBlank { "Standard" },
            message = "🎙️ $inviterName invited $targetUserName to take Mic #$displaySeatNum!",
            timestamp = System.currentTimeMillis(),
            vipLevel = inviter.getVipLevel(),
            isFounder = inviter.isFounder(),
            level = inviter.getEffectiveWealthLevel(),
            recipientName = targetUserName,
            recipientUid = targetUserId,
            systemCommentType = "mic_invite",
            roomLevelValue = targetSeatIndex
        )

        db.collection("rooms").document(roomId)
            .collection("live_comments").document(inviteId)
            .set(inviteComment)
            .addOnFailureListener { e ->
                Log.w("WatchPartyCinema", "Failed to send mic invitation comment: ${e.message}")
            }

        // Also send a real-time notification doc to the invited user
        val notifDoc = mapOf(
            "id" to inviteId,
            "targetUserId" to targetUserId,
            "senderId" to inviter.uid,
            "senderName" to inviterName,
            "senderPhoto" to inviter.photo,
            "type" to "mic_invite",
            "title" to "🎙️ Mic Invitation",
            "message" to "$inviterName invited you to Mic #$displaySeatNum in the live room!",
            "timestamp" to System.currentTimeMillis(),
            "isRead" to false
        )
        db.collection("users").document(targetUserId)
            .collection("notifications").document(inviteId)
            .set(notifDoc)
    }

    /**
     * Sends a real-time Firestore Welcome Wave message from the room when any user taps 👋 on an entry card.
     */
    fun sendWelcomeWave(
        db: FirebaseFirestore,
        roomId: String,
        sender: UserProfile,
        targetUserName: String,
        targetUserId: String
    ) {
        if (roomId.isBlank() || sender.uid.isBlank() || targetUserName.isBlank()) return
        val commentId = "wave_${sender.uid}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}"
        val senderName = sender.name.ifBlank { "Member" }
        val comment = LiveRoomComment(
            id = commentId,
            roomId = roomId,
            userId = sender.uid,
            userName = senderName,
            userPhoto = sender.photo,
            tier = sender.plan.ifBlank { "Standard" },
            message = "👋 $senderName welcomed $targetUserName to the room! ✨",
            timestamp = System.currentTimeMillis(),
            vipLevel = sender.getVipLevel(),
            isFounder = sender.isFounder(),
            level = sender.getEffectiveWealthLevel(),
            recipientName = targetUserName,
            recipientUid = targetUserId,
            systemCommentType = "welcome_wave"
        )
        db.collection("rooms").document(roomId)
            .collection("live_comments").document(commentId)
            .set(comment)
            .addOnFailureListener { e ->
                Log.w("WatchPartyCinema", "Failed to send welcome wave: ${e.message}")
            }
    }

    fun updateRoomWatchPartyState(
        db: FirebaseFirestore,
        room: RoomItem,
        isActive: Boolean,
        videoId: String = room.watchPartyVideoId.ifBlank { CURATED_VIDEOS.first().videoId },
        videoTitle: String = room.watchPartyVideoTitle.ifBlank { CURATED_VIDEOS.first().title },
        isPlaying: Boolean = room.watchPartyIsPlaying,
        updatedBy: String
    ) {
        if (room.id.isBlank()) return
        val updates = mutableMapOf<String, Any>(
            "isWatchPartyActive" to isActive,
            "watchPartyVideoId" to videoId,
            "watchPartyVideoTitle" to videoTitle,
            "watchPartyIsPlaying" to isPlaying,
            "watchPartyUpdatedBy" to updatedBy,
            "watchPartyUpdatedAt" to System.currentTimeMillis()
        )
        if (isActive) {
            updates["banner_image_url"] = CINEMA_THEME_ID
            updates["eventThemeStyle"] = CINEMA_THEME_ID
            updates["isPlaying"] = false
        } else {
            val fallbackBanner = getCategoryBanner(room.category).ifBlank {
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1080&q=80"
            }
            updates["banner_image_url"] = fallbackBanner
            updates["eventThemeStyle"] = ""
        }
        db.collection("rooms").document(room.id).update(updates)
        if (room.category.isNotBlank()) {
            db.collection("intent_categories").document(room.category)
                .collection("rooms").document(room.id)
                .update(updates)
        }
    }

    fun todayKey(): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    fun claimRoomTaskReward(
        context: Context,
        db: FirebaseFirestore,
        userId: String,
        roomId: String,
        taskKey: String,
        diamondBonus: Long,
        roomXpBonus: Long,
        onClaimed: () -> Unit
    ) {
        if (userId.isBlank() || roomId.isBlank()) return
        val fullKey = "${todayKey()}_${taskKey}"
        val safeRoomKey = RoomLevelUpEngine.sanitizeRoomKey(roomId)
        val userRef = db.collection("users").document(userId)

        userRef.update(
            mapOf(
                "diamonds" to FieldValue.increment(diamondBonus),
                "wealthXp" to FieldValue.increment(roomXpBonus / 2L),
                "roomXpMap.$safeRoomKey" to FieldValue.increment(roomXpBonus),
                "claimedRoomTasks" to FieldValue.arrayUnion(fullKey)
            )
        ).addOnSuccessListener {
            RoomLevelUpEngine.addRoomActivityXp(
                db = db,
                roomId = roomId,
                xpDelta = roomXpBonus
            )
            Toast.makeText(context, "🎁 Claimed +$diamondBonus 💎 & +$roomXpBonus Room XP!", Toast.LENGTH_SHORT).show()
            onClaimed()
        }.addOnFailureListener {
            Toast.makeText(context, "Could not claim reward right now. Try again.", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun buildCleanWatchCssAndJs(shouldPlay: Boolean): String {
    return """
        (function() {
          try {
            var styleId = 'lt-clean-cinema-style';
            var css = '' +
              'ytm-mobile-topbar-renderer, .mobile-topbar-header, header, #header-bar, ' +
              'ytm-watch-below-the-player, ytm-single-column-watch-next-results-renderer, ' +
              'ytm-item-section-renderer, ytm-pivot-bar-renderer, ytm-app-banner, ' +
              'ytm-autonav-bar, .ytm-autonav-bar, .watch-below-the-player, ' +
              '.related-chips-slot-wrapper, .ytp-open-app-button, .ytp-youtube-button, ' +
              '.ytm-promoted-sparkles-web-renderer, ytm-mealbar-promo-renderer, ' +
              'ytm-engagement-panel-section-list-renderer, .engagement-panel-container, ' +
              'c3-overlay, .bottom-sheet-container, ytm-bottom-sheet-renderer, ' +
              'ytm-compact-autoplay-renderer, .autonav-endscreen-countdown-container { ' +
              '  display: none !important; visibility: hidden !important; height: 0 !important; max-height: 0 !important; opacity: 0 !important; pointer-events: none !important; ' +
              '} ' +
              'html, body, ytm-app { ' +
              '  background: #000000 !important; margin: 0 !important; padding: 0 !important; ' +
              '  width: 100vw !important; height: 100vh !important; overflow: hidden !important; ' +
              '} ' +
              '#player-container-id, .player-container, #movie_player, .html5-video-player { ' +
              '  position: fixed !important; top: 0 !important; left: 0 !important; ' +
              '  width: 100vw !important; height: 100vh !important; ' +
              '  max-width: 100vw !important; max-height: 100vh !important; ' +
              '  margin: 0 !important; padding: 0 !important; ' +
              '  z-index: 99999 !important; background: #000000 !important; ' +
              '} ' +
              '.html5-video-container, video.html5-main-video, video { ' +
              '  position: fixed !important; top: 0 !important; left: 0 !important; ' +
              '  width: 100vw !important; height: 100vh !important; ' +
              '  object-fit: cover !important; background: #000000 !important; ' +
              '} ' +
              '.player-controls-top { display: none !important; }';

            var existing = document.getElementById(styleId);
            if (!existing) {
              var st = document.createElement('style');
              st.id = styleId;
              st.type = 'text/css';
              st.appendChild(document.createTextNode(css));
              (document.head || document.documentElement).appendChild(st);
            }

            window.__ltShouldPlay = $shouldPlay;
            window.applyLtPlayState = function(playNow) {
              window.__ltShouldPlay = playNow;
              var vids = document.querySelectorAll('video');
              for (var i = 0; i < vids.length; i++) {
                var v = vids[i];
                try {
                  if (playNow) {
                    v.muted = false;
                    var p = v.play();
                    if (p && typeof p.catch === 'function') {
                      p.catch(function() {
                        try { v.muted = true; v.play(); } catch (e2) {}
                      });
                    }
                  } else {
                    v.pause();
                  }
                } catch (e) {}
              }
              if (playNow) {
                var unmuteBtn = document.querySelector('.ytp-unmute, button[aria-label*="Unmute"], button[aria-label*="Tap to unmute"]');
                if (unmuteBtn) { try { unmuteBtn.click(); } catch (e) {} }
              }
            };

            if (!window.__ltCinemaInterval) {
              window.__ltCinemaInterval = setInterval(function() {
                var v = document.querySelector('video');
                if (v) {
                  if (window.__ltShouldPlay && v.paused && v.readyState >= 2) {
                    window.applyLtPlayState(true);
                  } else if (!window.__ltShouldPlay && !v.paused) {
                    v.pause();
                  }
                }
                var dismissBtns = document.querySelectorAll('button[aria-label*="No thanks"], button[aria-label*="Dismiss"], .ytm-upsell-dialog-renderer button');
                for (var j = 0; j < dismissBtns.length; j++) {
                  try { dismissBtns[j].click(); } catch (e) {}
                }
              }, 800);
            }

            window.applyLtPlayState($shouldPlay);
          } catch (err) {}
        })();
    """.trimIndent()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CleanYouTubeIFrameWebView(
    videoId: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val safeId = remember(videoId) {
        videoId.replace(Regex("[^A-Za-z0-9_-]"), "").ifBlank { WatchPartyCinemaEngine.CURATED_VIDEOS.first().videoId }
    }
    val watchUrl = remember(safeId) { "https://m.youtube.com/watch?v=$safeId" }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.BLACK)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                overScrollMode = WebView.OVER_SCROLL_NEVER
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val url = request?.url?.toString().orEmpty()
                        if (url.startsWith("intent://") || url.startsWith("vnd.youtube:") || url.startsWith("market://")) {
                            return true
                        }
                        val activeId = (view?.tag as? String)?.substringBefore("|").orEmpty()
                        return activeId.isNotBlank() && !url.contains(activeId)
                    }

                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                        super.onPageCommitVisible(view, url)
                        val activePlay = (view?.tag as? String)?.substringAfter("|")?.toBooleanStrictOrNull() ?: true
                        view?.evaluateJavascript(buildCleanWatchCssAndJs(activePlay), null)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val activePlay = (view?.tag as? String)?.substringAfter("|")?.toBooleanStrictOrNull() ?: true
                        view?.evaluateJavascript(buildCleanWatchCssAndJs(activePlay), null)
                    }
                }
                tag = "$safeId|$isPlaying"
                loadUrl(watchUrl)
            }
        },
        update = { webView ->
            val prevTag = webView.tag as? String ?: ""
            val prevParts = prevTag.split("|")
            val prevId = prevParts.getOrNull(0).orEmpty()
            val prevPlaying = prevParts.getOrNull(1)?.toBooleanStrictOrNull()
            if (prevId != safeId) {
                webView.tag = "$safeId|$isPlaying"
                webView.loadUrl("https://m.youtube.com/watch?v=$safeId")
            } else if (prevPlaying != isPlaying) {
                webView.tag = "$safeId|$isPlaying"
                webView.evaluateJavascript("if (window.applyLtPlayState) { window.applyLtPlayState($isPlaying); }", null)
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        },
        modifier = modifier
    )
}

/**
 * Full-Width Edge-to-Edge Clean Synced YouTube Cinema Player + Dedicated Slim Control Bar
 * Zero m.youtube.com chrome, zero overlapping Treasure Chest, plus native Fullscreen Mode.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CinemaWatchPartyVideoBox(
    room: RoomItem,
    canControlVideo: Boolean,
    currentUserName: String,
    currentRoomStaySeconds: Long,
    onTogglePlay: () -> Unit,
    onOpenVideoPicker: () -> Unit,
    onClaimTreasureChest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val videoId = room.watchPartyVideoId.ifBlank { WatchPartyCinemaEngine.CURATED_VIDEOS.first().videoId }
    val videoTitle = room.watchPartyVideoTitle.ifBlank { WatchPartyCinemaEngine.CURATED_VIDEOS.first().title }
    val isPlaying = room.watchPartyIsPlaying
    var isFullScreen by remember(room.id) { mutableStateOf(false) }

    // Real-time 3-minute (180s) Treasure Chest cycle countdown
    var chestCooldownSec by remember(room.id) { mutableLongStateOf(180L) }
    LaunchedEffect(room.id) {
        while (true) {
            delay(1000L)
            if (chestCooldownSec > 0L) chestCooldownSec -= 1L
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "chest_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chest_glow"
    )

    // Bleed 12.dp left + 12.dp right out of parent Column padding so the player sits 100% Edge-to-Edge
    Column(
        modifier = modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val extraWidthPx = 24.dp.roundToPx()
                val targetWidth = if (constraints.hasBoundedWidth) constraints.maxWidth + extraWidthPx else constraints.maxWidth
                val placeable = measurable.measure(
                    constraints.copy(minWidth = targetWidth, maxWidth = targetWidth)
                )
                layout(placeable.width, placeable.height) {
                    placeable.place(0, 0)
                }
            }
            .background(Color.Black)
    ) {
        // 1. PURE EDGE-TO-EDGE FULL-BLEED VIDEO SCREEN (Zero overlapping UI or YouTube web chrome)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(Color.Black)
        ) {
            // Render inline player only when fullscreen dialog is closed (prevents double audio)
            if (!isFullScreen) {
                CleanYouTubeIFrameWebView(
                    videoId = videoId,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF050B1E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎬 Playing in Full Screen...",
                        color = Color(0xFFFFE57F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Synced Pause Status Overlay
            if (!isPlaying && !isFullScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88050D26))
                        .clickable(enabled = canControlVideo) { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color(0xCC081538),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0x99FFD700))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${room.watchPartyUpdatedBy.ifBlank { currentUserName.ifBlank { "Host" } }} paused the video",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Subtle Bottom-Right Fullscreen Icon right on the edge-to-edge player
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 8.dp)
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xB3000000))
                    .border(0.9.dp, Color(0x99FFFFFF), RoundedCornerShape(8.dp))
                    .clickable { isFullScreen = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Full Screen Video",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // 2. SLEEK EDGE-TO-EDGE CINEMA CONTROL BAR (Treasure Chest + Title + Search + Play/Pause + Fullscreen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF061130), Color(0xFF0B1F52), Color(0xFF061130))
                    )
                )
                .border(0.6.dp, Color(0x6638BDF8), RectangleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Compact Treasure Chest (zero video overlap!)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (chestCooldownSec == 0L) Color(0xFFFFD700).copy(alpha = 0.22f + 0.18f * glowAlpha)
                        else Color(0x990A1942)
                    )
                    .border(
                        width = 0.8.dp,
                        color = if (chestCooldownSec == 0L) Color(0xFFFFD700) else Color(0x66FFD700),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable {
                        if (chestCooldownSec == 0L) {
                            chestCooldownSec = 180L
                            onClaimTreasureChest()
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_room_treasure_chest_hd),
                    contentDescription = "Treasure Chest",
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
                val mins = chestCooldownSec / 60
                val secs = chestCooldownSec % 60
                Text(
                    text = if (chestCooldownSec == 0L) "CLAIM!" else String.format(Locale.US, "%02d:%02d", mins, secs),
                    color = if (chestCooldownSec == 0L) Color(0xFFFFE57F) else Color(0xFFFFE57F),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Center: Clean Now Playing Title
            Text(
                text = "▶ $videoTitle",
                color = Color(0xFFE0F2FE),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Right: Play/Pause + Global YouTube Search + Fullscreen
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (canControlVideo) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1D4ED8))
                            .border(0.9.dp, Color(0xAA93C5FD), CircleShape)
                            .clickable { onTogglePlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause Synced Video",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF1E3A8A))
                            .border(0.9.dp, Color(0xCCFFD700), RoundedCornerShape(50))
                            .clickable { onOpenVideoPicker() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Global YouTube Search",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Search",
                            color = Color(0xFFFFE57F),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F295E))
                        .border(0.9.dp, Color(0xAA38BDF8), CircleShape)
                        .clickable { isFullScreen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expand Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }

    // 3. DEDICATED FULL-SCREEN VIEWER DIALOG (No Activity restart, Agora voice stays 100% live!)
    if (isFullScreen) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                CleanYouTubeIFrameWebView(
                    videoId = videoId,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Overlay Bar inside Fullscreen (Title + Host Controls + Exit Fullscreen)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xCC000000), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 34.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎬 $videoTitle",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (canControlVideo) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC1D4ED8))
                                    .border(1.dp, Color(0xAA93C5FD), CircleShape)
                                    .clickable { onTogglePlay() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC1E3A8A))
                                    .border(1.dp, Color(0xCCFFD700), CircleShape)
                                    .clickable {
                                        isFullScreen = false
                                        onOpenVideoPicker()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Video",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xCCDC2626),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                            modifier = Modifier.clickable { isFullScreen = false }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FullscreenExit,
                                    contentDescription = "Exit Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Exit",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 10 Photorealistic HD 3D Royal Blue Velvet Sofa Mic Seats (2 Rows x 5)
 * Mic icon, Lock icon, or Occupied User Avatar sits cleanly INSIDE the center of each plush sofa chair.
 */
@Composable
fun RoyalBlueSofaTenMicStage(
    seats: List<MicSeat>,
    currentUserId: String,
    isLocalTalking: Boolean,
    allProfiles: List<UserProfile>,
    activeEmojiReactions: Map<String, String>,
    userActiveFrame: String = "",
    userActiveMicEffect: String = "",
    onSeatClick: (MicSeat) -> Unit,
    modifier: Modifier = Modifier
) {
    val tenSeats = (0 until 10).map { idx ->
        seats.find { it.seatIndex == idx } ?: MicSeat(
            seatIndex = idx,
            label = "${idx + 1}",
            uid = "",
            name = "",
            photo = "",
            role = if (idx == 0) "host" else "speaker"
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf(tenSeats.subList(0, 5), tenSeats.subList(5, 10)).forEach { rowSeats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                rowSeats.forEach { seat ->
                    val isOccupied = seat.uid.isNotBlank()
                    val isTalkingNow = isOccupied && (seat.isTalking || (seat.uid == currentUserId && isLocalTalking)) && !seat.isMuted
                    val occupantProfile = remember(seat.uid, allProfiles) {
                        if (seat.uid.isNotBlank()) allProfiles.find { it.uid == seat.uid } else null
                    }
                    val reactionEmoji = if (seat.uid.isNotBlank()) activeEmojiReactions[seat.uid] else null

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSeatClick(seat) }
                            .padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Real Photorealistic HD 3D Royal Blue Velvet Sofa Armchair
                            Image(
                                painter = painterResource(id = R.drawable.ic_cinema_sofa_royal_blue),
                                contentDescription = "Royal Blue Sofa Seat ${seat.seatIndex + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            // Mic / Lock / User Avatar + Equipped Frame & Mic Effect centered RIGHT INSIDE the Sofa Chair
                            if (isOccupied) {
                                val isCurrentSelf = seat.uid == currentUserId
                                val rawSeatFrame = if (isCurrentSelf && userActiveFrame.isNotBlank()) {
                                    userActiveFrame
                                } else if (occupantProfile?.activeFrame?.isNotBlank() == true) {
                                    occupantProfile.activeFrame
                                } else {
                                    ""
                                }
                                val seatFrameId = if (rawSeatFrame.startsWith("mic_link_") || getAvatarFrameItem(rawSeatFrame) == null) "" else rawSeatFrame
                                val frameItem = getAvatarFrameItem(seatFrameId)
                                val seatVipLvl = occupantProfile?.getVipLevel() ?: (frameItem?.vipLevel ?: 0)
                                val seatMicEffectId = if (isCurrentSelf) {
                                    userActiveMicEffect
                                } else {
                                    occupantProfile?.activeMicEffect ?: ""
                                }
                                val photoUrl = seat.photo.ifBlank { occupantProfile?.photo ?: "" }
                                    .ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" }

                                val isInAppMusicPlaying by AudioPlayerManager.isPlayingState
                                val isEffectActiveNow = isTalkingNow || (isInAppMusicPlaying && (seat.seatIndex == 0 || isCurrentSelf))
                                FlowingAvatarFrame(
                                    photoUrl = photoUrl,
                                    frameId = seatFrameId,
                                    avatarSize = 34.dp,
                                    isSpeaking = isEffectActiveNow,
                                    vipLevel = seatVipLvl,
                                    showAnimation = true,
                                    showAuraAnimation = false,
                                    showVipBadge = false,
                                    showMicEffect = seatMicEffectId.isNotBlank() && isEffectActiveNow,
                                    micEffectId = seatMicEffectId
                                )

                                // Mic Mute / Active badge on bottom-right of sofa avatar (Red Crossed-Out Mic when muted)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 7.dp, bottom = 8.dp)
                                        .size(17.dp)
                                        .clip(CircleShape)
                                        .background(if (seat.isMuted) Color(0xFFDC2626) else Color(0xFF10B981))
                                        .border(1.2.dp, if (seat.isMuted) Color(0xFFFFCDD2) else Color(0xFF07143A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (seat.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = if (seat.isMuted) "Muted" else "Active Mic",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            } else {
                                // Empty, Locked, or Muted Sofa -> Distinct RED Lock or RED Crossed-Out Mic or Open Mic
                                val isLockedSeat = seat.isLocked
                                val isMutedSeat = seat.isMuted
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isLockedSeat -> Color(0xE67F1D1D)
                                                isMutedSeat -> Color(0xE6991B1B)
                                                else -> Color(0xAA07143A)
                                            }
                                        )
                                        .border(
                                            width = if (isLockedSeat || isMutedSeat) 1.3.dp else 0.9.dp,
                                            color = when {
                                                isLockedSeat -> Color(0xFFFF5252)
                                                isMutedSeat -> Color(0xFFFF5252)
                                                else -> Color(0x8893C5FD)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isLockedSeat -> Icons.Default.Lock
                                            isMutedSeat -> Icons.Default.MicOff
                                            else -> Icons.Default.Mic
                                        },
                                        contentDescription = when {
                                            isLockedSeat -> "Locked Mic"
                                            isMutedSeat -> "Muted Mic"
                                            else -> "Take Mic"
                                        },
                                        tint = when {
                                            isLockedSeat -> Color(0xFFFFD700)
                                            isMutedSeat -> Color.White
                                            else -> Color(0xFFE0F2FE)
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (!reactionEmoji.isNullOrBlank()) {
                                Text(
                                    text = reactionEmoji,
                                    fontSize = 18.sp,
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                        }

                        // Seat number + User name pill right under sofa
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xA6050E2A))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isOccupied) Color(0xFF2563EB) else Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${seat.seatIndex + 1}",
                                    color = Color.White,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isOccupied) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = seat.name.ifBlank { "Guest" },
                                    color = Color(0xFFFFF3C4),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 48.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Auto-Cycling Room Task Reward Banner.
 * Shows for 20 seconds, then automatically hides for 6 minutes (360 seconds) so it never hogs screen space permanently.
 * Users can also tap the small '×' icon to dismiss it immediately.
 */
@Composable
fun RoomTaskRewardBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            isVisible = true
            delay(20_000L) // Show for 20 seconds
            isVisible = false
            delay(360_000L) // Stay hidden for 6 minutes (5-10 min interval) before briefly reappearing
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
        exit = fadeOut(tween(300)) + shrinkVertically(tween(300)),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xD9111827),
            border = BorderStroke(0.8.dp, Color(0x6694A3B8)),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "📋", fontSize = 12.sp)
                Text(
                    text = "Complete room task for rewards",
                    color = Color(0xFFE2E8F0),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Room Tasks",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { isVisible = false },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hide Banner",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Real-Time Firestore Room Tasks Dialog (Stay 5m / Welcome Wave / Gift)
 */
@Composable
fun RoomTaskRewardsDialog(
    roomId: String,
    currentUser: UserProfile,
    currentRoomStaySeconds: Long,
    hasWavedInRoom: Boolean,
    db: FirebaseFirestore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var claimedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    val today = remember { WatchPartyCinemaEngine.todayKey() }

    LaunchedEffect(currentUser.uid) {
        if (currentUser.uid.isNotBlank()) {
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener { snap ->
                @Suppress("UNCHECKED_CAST")
                val list = (snap.get("claimedRoomTasks") as? List<String>) ?: emptyList()
                claimedKeys = list.toSet()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0B1A42),
            border = BorderStroke(1.4.dp, Color(0xAAFFD700)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁 Daily Room Tasks",
                        color = Color(0xFFFFE57F),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                val stayReady = currentRoomStaySeconds >= 300L
                val stayClaimed = "${today}_stay_5m" in claimedKeys
                RoomTaskRowItem(
                    title = "Stay 5 Minutes in Room",
                    subtitle = "+2 💎 & +50 Room XP (${(currentRoomStaySeconds / 60)}/5 min)",
                    isReady = stayReady,
                    isClaimed = stayClaimed,
                    onClaim = {
                        WatchPartyCinemaEngine.claimRoomTaskReward(
                            context, db, currentUser.uid, roomId, "stay_5m", 2L, 50L
                        ) { claimedKeys = claimedKeys + "${today}_stay_5m" }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                val waveClaimed = "${today}_welcome_wave" in claimedKeys
                RoomTaskRowItem(
                    title = "Send a Welcome Wave (👋)",
                    subtitle = "+1 💎 & +25 Room XP (Tap 👋 on any room entry)",
                    isReady = hasWavedInRoom,
                    isClaimed = waveClaimed,
                    onClaim = {
                        WatchPartyCinemaEngine.claimRoomTaskReward(
                            context, db, currentUser.uid, roomId, "welcome_wave", 1L, 25L
                        ) { claimedKeys = claimedKeys + "${today}_welcome_wave" }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                val dailyCheckInClaimed = "${today}_room_checkin" in claimedKeys
                RoomTaskRowItem(
                    title = "Daily Royal Room Check-In",
                    subtitle = "+1 💎 & +30 Room XP",
                    isReady = true,
                    isClaimed = dailyCheckInClaimed,
                    onClaim = {
                        WatchPartyCinemaEngine.claimRoomTaskReward(
                            context, db, currentUser.uid, roomId, "room_checkin", 1L, 30L
                        ) { claimedKeys = claimedKeys + "${today}_room_checkin" }
                    }
                )
            }
        }
    }
}

@Composable
private fun RoomTaskRowItem(
    title: String,
    subtitle: String,
    isReady: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF13275C),
        border = BorderStroke(0.8.dp, Color(0x4493C5FD)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF93C5FD), fontSize = 10.5.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClaim,
                enabled = isReady && !isClaimed,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF1E1000),
                    disabledContainerColor = Color(0xFF1E3A8A),
                    disabledContentColor = Color(0xAAFFFFFF)
                ),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = when {
                        isClaimed -> "Claimed ✓"
                        isReady -> "Claim"
                        else -> "In Progress"
                    },
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Full-Screen Global YouTube Panel
 * Opens full-page m.youtube.com -> user searches & taps any video -> "▶ Play in Room" button lights up ->
 * tapping Play immediately plays that video cleanly edge-to-edge in the room!
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeVideoPickerDialog(
    currentVideoTitle: String,
    onSelectVideo: (videoId: String, title: String) -> Unit,
    onDismiss: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var selectedVideoId by remember { mutableStateOf("") }
    var selectedVideoTitle by remember { mutableStateOf("") }

    // Continuously track SPA URL & title changes inside m.youtube.com so tapping ANY video is always detected
    LaunchedEffect(webViewRef) {
        while (true) {
            delay(400L)
            val wv = webViewRef ?: continue
            val currentUrl = wv.url.orEmpty()
            val extractedId = if (
                currentUrl.contains("/watch") ||
                currentUrl.contains("/shorts/") ||
                currentUrl.contains("youtu.be/")
            ) {
                WatchPartyCinemaEngine.extractYouTubeVideoId(currentUrl)
            } else {
                ""
            }
            if (extractedId.isNotBlank()) {
                selectedVideoId = extractedId
                val cleanTitle = wv.title.orEmpty()
                    .replace(" - YouTube", "", ignoreCase = true)
                    .trim()
                if (cleanTitle.isNotBlank() && !cleanTitle.equals("YouTube", ignoreCase = true)) {
                    selectedVideoTitle = cleanTitle
                } else if (selectedVideoTitle.isBlank()) {
                    selectedVideoTitle = "YouTube Video"
                }
            } else if (currentUrl.contains("youtube.com") && !currentUrl.contains("/watch") && !currentUrl.contains("/shorts/")) {
                selectedVideoId = ""
                selectedVideoTitle = ""
            }
        }
    }

    Dialog(
        onDismissRequest = {
            webViewRef?.stopLoading()
            webViewRef?.loadUrl("about:blank")
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF061130))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Sleek Top Navigation Bar (Back + Home + Title + Close)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF071333), Color(0xFF0F255E), Color(0xFF071333))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val wv = webViewRef
                            if (wv != null && wv.canGoBack()) {
                                wv.goBack()
                            } else {
                                wv?.stopLoading()
                                wv?.loadUrl("about:blank")
                                onDismiss()
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { webViewRef?.loadUrl("https://m.youtube.com") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "YouTube Home",
                            tint = Color(0xFFFFE57F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🎬 YouTube Global Search",
                        color = Color(0xFFFFE57F),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Text(
                        text = "Now: $currentVideoTitle",
                        color = Color(0xFF93C5FD),
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        webViewRef?.stopLoading()
                        webViewRef?.loadUrl("about:blank")
                        onDismiss()
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close YouTube",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. FULL-PAGE YOUTUBE PANEL (Browse, Search & Tap Any Video)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.BLACK)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString().orEmpty()
                                    // Keep everything inside this full-screen YouTube panel; never jump to external YouTube app
                                    return !(url.startsWith("http://") || url.startsWith("https://"))
                                }

                                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                    super.doUpdateVisitedHistory(view, url, isReload)
                                    val u = url.orEmpty()
                                    val vid = if (u.contains("/watch") || u.contains("/shorts/") || u.contains("youtu.be/")) {
                                        WatchPartyCinemaEngine.extractYouTubeVideoId(u)
                                    } else ""
                                    if (vid.isNotBlank()) {
                                        selectedVideoId = vid
                                        val clean = view?.title.orEmpty()
                                            .replace(" - YouTube", "", ignoreCase = true)
                                            .trim()
                                        if (clean.isNotBlank() && !clean.equals("YouTube", ignoreCase = true)) {
                                            selectedVideoTitle = clean
                                        }
                                    }
                                }
                            }
                            webViewRef = this
                            loadUrl("https://m.youtube.com")
                        }
                    },
                    onRelease = { webView ->
                        webView.stopLoading()
                        webView.loadUrl("about:blank")
                        webView.destroy()
                        if (webViewRef === webView) webViewRef = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. BOTTOM "PLAY IN ROOM" ACTION BAR (Lights up brightly as soon as user taps ANY video!)
            Surface(
                color = if (selectedVideoId.isNotBlank()) Color(0xFF0C1E4F) else Color(0xFF081433),
                border = BorderStroke(
                    1.dp,
                    if (selectedVideoId.isNotBlank()) Color(0xFFFFD700) else Color(0x5538BDF8)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedVideoId.isNotBlank()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Selected Video Ready:",
                                color = Color(0xFFFFE57F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedVideoTitle.ifBlank { "YouTube Video" },
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val vid = selectedVideoId
                                val title = selectedVideoTitle.ifBlank { "YouTube Watch Party" }
                                webViewRef?.stopLoading()
                                webViewRef?.loadUrl("about:blank")
                                onSelectVideo(vid, title)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color(0xFF0A1430)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play in Room",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Play in Room",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else {
                        Text(
                            text = "🔍 Search & tap any video on YouTube — then tap '▶ Play in Room'",
                            color = Color(0xFFBAE6FD),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2-Step Entry Action Pill attached beside Audio Room entrance cards:
 * Step 1: Shows 👋 Wave -> taps to send real-time Welcome message to the newcomer.
 * Step 2 (for Host/Admin/Founder/Staff): Transforms in the exact same slot into a small 🎙️ Mic Invite icon
 *         -> taps to send a real-time Mic Seat Invitation popup to the viewer!
 */
@Composable
fun WelcomeWaveActionButton(
    hasWaved: Boolean,
    hasInvitedMic: Boolean,
    canInviteToMic: Boolean,
    onWaveClick: () -> Unit,
    onInviteMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showMicInvite = hasWaved && canInviteToMic && !hasInvitedMic
    val isCompleted = (hasWaved && !canInviteToMic) || hasInvitedMic

    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(
            width = 0.9.dp,
            color = when {
                isCompleted -> Color(0x884ADE80)
                showMicInvite -> Color(0xCCFFD700)
                else -> Color(0xCC93C5FD)
            }
        ),
        modifier = modifier.clickable(enabled = !isCompleted) {
            if (!hasWaved) onWaveClick() else if (showMicInvite) onInviteMicClick()
        }
    ) {
        Row(
            modifier = Modifier
                .background(
                    when {
                        isCompleted -> Brush.horizontalGradient(listOf(Color(0xCC14532D), Color(0xCC166534)))
                        showMicInvite -> Brush.horizontalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF0EA5E9)))
                        else -> Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF2563EB)))
                    }
                )
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (showMicInvite) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Invite to Mic",
                    tint = Color(0xFFFFE57F),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Mic",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            } else {
                Text(
                    text = when {
                        hasInvitedMic -> "✓ Invited"
                        hasWaved -> "✓ Hi"
                        else -> "👋"
                    },
                    color = Color.White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Real-Time Incoming Mic Invitation Popup for the invited viewer.
 * Tapping "Accept & Take Mic" immediately equips the viewer onto the invited mic seat!
 */
@Composable
fun MicInviteReceivedDialog(
    inviterName: String,
    inviterPhoto: String,
    seatIndex: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Dialog(onDismissRequest = onDecline) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0A173B),
            border = BorderStroke(1.5.dp, Color(0xCCFFD700)),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFFFFD700)))
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (inviterPhoto.isNotBlank()) {
                        AsyncImage(
                            model = inviterPhoto,
                            contentDescription = inviterName,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF1E3A8A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🎙️ Mic Invitation!",
                    color = Color(0xFFFFE57F),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$inviterName invited you to join Mic #${seatIndex + 1} on stage!",
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDecline,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Decline", color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.weight(1.25f)
                    ) {
                        Text("Accept & Take Mic", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Subtle Royal Blue & Gold comment strip showing when Host/Admin invites a viewer to a mic seat.
 */
@Composable
fun MicInviteCommentCard(
    comment: LiveRoomComment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC11265C),
        border = BorderStroke(0.9.dp, Color(0x8838BDF8)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFFFE57F), fontWeight = FontWeight.Bold)) {
                        append(comment.userName.ifBlank { "Host" })
                    }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f))) {
                        append(" invited ")
                    }
                    withStyle(SpanStyle(color = Color(0xFF7DD3FC), fontWeight = FontWeight.Bold)) {
                        append(comment.recipientName.ifBlank { "Guest" })
                    }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f))) {
                        append(" to Mic #${comment.roomLevelValue + 1}! 🎙️")
                    }
                },
                fontSize = 11.5.sp,
                lineHeight = 14.5.sp
            )
        }
    }
}

/**
 * Crisp Royal Blue & Gold Welcome Wave comment card when someone welcomes a newcomer to the room.
 */
@Composable
fun WelcomeWaveCommentCard(
    comment: LiveRoomComment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC0D2258),
        border = BorderStroke(0.9.dp, Color(0x88FFD700)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "👋", fontSize = 14.sp)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFFFE57F), fontWeight = FontWeight.Bold)) {
                        append(comment.userName.ifBlank { "Member" })
                    }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f))) {
                        append(" welcomed ")
                    }
                    withStyle(SpanStyle(color = Color(0xFF7DD3FC), fontWeight = FontWeight.Bold)) {
                        append(comment.recipientName.ifBlank { "New Guest" })
                    }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.95f))) {
                        append(" to the room! ✨")
                    }
                },
                fontSize = 11.5.sp,
                lineHeight = 14.5.sp
            )
        }
    }
}


/**
 * Floating Round "YouTube Player" Button with Animated Music/Video Equalizer
 * Placed above the right-side floating Gift/Game/Store stack.
 * Tapping converts between Normal Audio Room <-> YouTube Watch Party Room (or shows Room Level 5 popup).
 */
@Composable
fun FloatingYouTubePlayerButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "yt_eq_anim")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq3"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        if (isActive) {
                            listOf(Color(0xFFDC2626), Color(0xFFFF1744), Color(0xFFB91C1C))
                        } else {
                            listOf(Color(0xFF1E3A8A), Color(0xFFDC2626), Color(0xFF7C3AED))
                        }
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = if (isActive) Color(0xFFFFD700) else Color(0xCCFFFFFF),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "YouTube Player",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(1.dp))
                // Live Animated Video/Music Equalizer Bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height((10f * bar1).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFFFE57F))
                    )
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height((10f * bar2).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White)
                    )
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height((10f * bar3).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF38BDF8))
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isActive) Color(0xFFDC2626) else Color(0xCC061130))
                .border(0.7.dp, Color(0xAAFFD700), RoundedCornerShape(50))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                text = if (isActive) "YT ON" else "YouTube",
                color = Color.White,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

/**
 * Popup shown when a non-eligible user tries to open YouTube Player Room below Room Level 5.
 */
@Composable
fun YouTubeLevelRequiredDialog(
    currentRoomLevel: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0A173B),
            border = BorderStroke(1.5.dp, Color(0xCCFFD700)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFF1D4ED8))))
                        .border(1.5.dp, Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFFFFE57F),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "You need minimum Level 5",
                    color = Color(0xFFFFE57F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Current Room Level: Lv.$currentRoomLevel\nReach Room Level 5 via room gifting & activity to unlock the YouTube Cinema Player!",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                DynamicRoomLevelBadge(roomLevel = currentRoomLevel, onClick = {})
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

fun getRoomCrestBadgeDrawableRes(roomLevel: Int): Int {
    val safeLevel = roomLevel.coerceAtLeast(1)
    return when {
        safeLevel >= 50 -> R.drawable.badge_hd_tier7_sovereign
        safeLevel >= 35 -> R.drawable.badge_hd_tier5_diamond
        safeLevel >= 20 -> R.drawable.badge_hd_tier6_master
        safeLevel >= 10 -> R.drawable.badge_hd_tier3_gold
        safeLevel >= 5 -> R.drawable.ic_wealth_shield_3d
        else -> R.drawable.ic_wealth_shield_3d
    }
}

/**
 * Dynamic Auto-Evolving 3D Room Crest Level Badge placed directly under the top-left Room Header Box.
 * Uses a dedicated 3D Winged Royal Shield Crest (never a plain star emoji!) and evolves with Room Level.
 */
@Composable
fun DynamicRoomLevelBadge(
    roomLevel: Int,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val safeLevel = roomLevel.coerceAtLeast(1)
    val badgeRes = getRoomCrestBadgeDrawableRes(safeLevel)
    val (tierName, gradientColors, borderColor) = when {
        safeLevel >= 50 -> Triple(
            "Sovereign",
            listOf(Color(0xFF281236), Color(0xFF4C1D95), Color(0xFF1F1630)),
            Color(0xFFFFD700)
        )
        safeLevel >= 35 -> Triple(
            "Diamond",
            listOf(Color(0xFF0C2442), Color(0xFF0369A1), Color(0xFF0A1D36)),
            Color(0xFF7DD3FC)
        )
        safeLevel >= 20 -> Triple(
            "Imperial",
            listOf(Color(0xFF3B1219), Color(0xFF991B1B), Color(0xFF291014)),
            Color(0xFFFFD700)
        )
        safeLevel >= 10 -> Triple(
            "Royal",
            listOf(Color(0xFF2A1842), Color(0xFF6D28D9), Color(0xFF1D112E)),
            Color(0xFFFDE047)
        )
        safeLevel >= 5 -> Triple(
            "Cinema",
            listOf(Color(0xFF132347), Color(0xFF1D4ED8), Color(0xFF0F1B38)),
            Color(0xFFFFD700)
        )
        else -> Triple(
            "Room",
            listOf(Color(0xFF1D1829), Color(0xFF2E2414), Color(0xFF181522)),
            Color(0xFFF5C451)
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 6.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(gradientColors))
                .padding(start = 4.dp, end = 9.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = painterResource(id = badgeRes),
                contentDescription = "Room Level Crest",
                modifier = Modifier.size(16.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "$tierName Lv.$safeLevel",
                color = Color(0xFFFFE57F),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Complete 4-Screen Obsidian & Royal Gold Mobile Room Profile & Command Hub
 * (Opened when clicking Top-Left Room Header Box in ANY Audio Room, YouTube Room, or Video Room across all categories).
 * 100% Real-Time Cloud Firestore Data Binding — Zero Dummy Numbers, Zero Fake Badges, Zero Placeholders.
 */
@Composable
fun RoomInfoOnlyDialog(
    room: RoomItem,
    displayRoomId: String,
    roomPhotoUrl: String,
    currentRoomLevel: Int,
    hostFollowerCount: Int,
    targetHostId: String,
    canEditRoom: Boolean,
    db: FirebaseFirestore,
    onOpenEditRoomSettings: () -> Unit,
    onDismiss: () -> Unit,
    currentUser: UserProfile? = null,
    onPickRoomPhoto: (() -> Unit)? = null,
    onOpenRoomBonus: (() -> Unit)? = null,
    onOpenRoomGames: (() -> Unit)? = null,
    onRoomUpdated: ((RoomItem) -> Unit)? = null
) {
    val context = LocalContext.current

    // Navigation state: Top tabs ("profile", "followers", "events", "room") & Sub-screens ("main", "room_level", "badge_rules", "blocked_list", "backpack", "blind_date")
    var selectedTopTab by remember { mutableStateOf("profile") }
    var activeSubScreen by remember { mutableStateOf("main") }

    // Inline editors for Host/Admin (100% real-time Firestore updates)
    var showEditTitleModal by remember { mutableStateOf(false) }
    var showEditAnnouncementModal by remember { mutableStateOf(false) }
    var showEditTagModal by remember { mutableStateOf(false) }
    var showEditCountryModal by remember { mutableStateOf(false) }

    // Real-time room fields synced directly from Firestore rooms/{roomId}
    var liveTitle by remember(room.id) { mutableStateOf(room.title.ifBlank { room.roomName.ifBlank { "Live Room" } }) }
    var livePhoto by remember(room.id) { mutableStateOf(roomPhotoUrl) }
    var liveAnnouncement by remember(room.id) { mutableStateOf(room.announcement) }
    var liveTag by remember(room.id) { mutableStateOf("Chat") }
    var liveCountry by remember(room.id) { mutableStateOf("") }
    var liveThemeName by remember(room.id) { mutableStateOf("") }
    var liveEnterPermission by remember(room.id) {
        mutableStateOf(if (room.isFamilyRoom || room.privacy.equals("family_only", true)) "Family" else "Everyone")
    }
    var liveMaxMics by remember(room.id) { mutableStateOf(10) }
    var liveIsWatchParty by remember(room.id) { mutableStateOf(room.isWatchPartyActive) }
    var liveMicPermission by remember(room.id) { mutableStateOf("Everyone") }
    var liveEquippedBadge by remember(room.id) { mutableStateOf("") }
    var liveKickedIds by remember(room.id) { mutableStateOf(room.kickedUserIds) }
    var liveBannedIds by remember(room.id) { mutableStateOf(room.bannedUserIds) }
    var liveActiveUserCount by remember(room.id) { mutableStateOf(room.activeUserCount) }

    // Real-time Room Level & -10% Daily XP Counters from Firestore rooms/{roomId}/games/room_level_meta
    var liveRoomXp by remember(room.id) { mutableLongStateOf(0L) }
    var liveRoomLevel by remember(room.id, currentRoomLevel) { mutableStateOf(currentRoomLevel.coerceAtLeast(1)) }
    var liveDailyDiamondExp by remember(room.id) { mutableLongStateOf(0L) }
    var liveDailyGoldExp by remember(room.id) { mutableLongStateOf(0L) }
    var liveDailyMicExp by remember(room.id) { mutableLongStateOf(0L) }
    var liveDailyNewMicExp by remember(room.id) { mutableLongStateOf(0L) }

    // Real-time Followers from Firestore users/{targetHostId}/followers
    var followersList by remember(targetHostId) { mutableStateOf<List<FollowerRecord>>(emptyList()) }
    var liveFollowerCount by remember(targetHostId, hostFollowerCount) { mutableStateOf(hostFollowerCount.coerceAtLeast(0)) }
    var isLoadingFollowers by remember(targetHostId) { mutableStateOf(true) }

    // Real-time Room Gift Events count for Blind Date / Match Data & Events tab
    var recentGiftEventsCount by remember(room.id) { mutableStateOf(0) }
    var recentRoomGiftsTotalDiamonds by remember(room.id) { mutableLongStateOf(0L) }
    var blockedUserProfiles by remember(room.id) { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    // 1. Live Firestore Snapshot Listener on rooms/{roomId}
    androidx.compose.runtime.DisposableEffect(room.id) {
        if (room.id.isBlank()) return@DisposableEffect onDispose {}
        val roomReg = db.collection("rooms").document(room.id)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    val t = snap.getString("title")?.takeIf { it.isNotBlank() }
                        ?: snap.getString("roomName")?.takeIf { it.isNotBlank() }
                        ?: room.title.ifBlank { "Live Room" }
                    liveTitle = t
                    val p = snap.getString("hostPhotoUrl")?.takeIf { it.isNotBlank() } ?: roomPhotoUrl
                    livePhoto = p
                    liveAnnouncement = snap.getString("announcement") ?: ""
                    val rawTag = snap.getString("roomTag")?.takeIf { it.isNotBlank() }
                        ?: snap.getString("category")?.replace("_", " ")?.replaceFirstChar { it.uppercase() }
                        ?: "Chat"
                    liveTag = rawTag
                    val c = snap.getString("country")?.takeIf { it.isNotBlank() } ?: ""
                    if (c.isNotBlank()) liveCountry = c

                    val isWp = snap.getBoolean("isWatchPartyActive") ?: false
                    liveIsWatchParty = isWp
                    val isFam = snap.getBoolean("isFamilyRoom") ?: (snap.getString("privacy") == "family_only")
                    val priv = snap.getString("privacy") ?: "public"
                    liveEnterPermission = when {
                        isFam || priv.equals("family_only", true) -> "Family"
                        priv.equals("vip_only", true) -> "VIP Only"
                        else -> "Everyone"
                    }
                    liveMaxMics = (snap.getLong("maxMicSeats") ?: 10L).toInt().coerceIn(8, 15)
                    liveMicPermission = snap.getString("micPermission")?.takeIf { it.isNotBlank() } ?: "Everyone"
                    liveEquippedBadge = snap.getString("equippedRoomBadge") ?: ""

                    val banner = snap.getString("banner_image_url") ?: ""
                    val customTheme = snap.getString("themeName") ?: ""
                    liveThemeName = when {
                        customTheme.isNotBlank() -> customTheme
                        isWp || banner == WatchPartyCinemaEngine.CINEMA_THEME_ID -> "YouTube Cinema"
                        banner.startsWith("data:image") -> "Custom Theme"
                        room.category == "vip_room" -> "Royal VIP Gold"
                        room.isOfficialEvent -> "Official Event"
                        else -> "Classic Lounge"
                    }

                    @Suppress("UNCHECKED_CAST")
                    liveKickedIds = (snap.get("kickedUserIds") as? List<String>) ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    liveBannedIds = (snap.get("bannedUserIds") as? List<String>) ?: emptyList()
                    liveActiveUserCount = (snap.getLong("activeUserCount") ?: room.activeUserCount.toLong()).toInt()

                    val docXp = snap.getLong("roomActivityXp") ?: 0L
                    val docLvl = (snap.getLong("roomLevel") ?: 1L).toInt()
                    if (docXp > liveRoomXp) liveRoomXp = docXp
                    val calcLvl = RoomLevelUpEngine.calculateOfficialRoomPersonalLevel(liveRoomXp)
                    liveRoomLevel = maxOf(liveRoomLevel, docLvl, calcLvl)
                }
            }

        // 2. Live Firestore Snapshot Listener on rooms/{roomId}/games/room_level_meta (-10% XP System)
        val metaReg = db.collection("rooms").document(room.id)
            .collection("games").document("room_level_meta")
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    val mXp = snap.getLong("roomActivityXp") ?: 0L
                    val mLvl = (snap.getLong("roomLevel") ?: 1L).toInt()
                    liveRoomXp = maxOf(liveRoomXp, mXp)
                    liveRoomLevel = maxOf(
                        liveRoomLevel,
                        mLvl,
                        RoomLevelUpEngine.calculateOfficialRoomPersonalLevel(liveRoomXp)
                    )
                    val todayKey = RoomLevelUpEngine.getTodayDateKey()
                    val isToday = snap.getString("dailyDateKey") == todayKey
                    liveDailyDiamondExp = if (isToday) (snap.getLong("dailyDiamondExp") ?: 0L) else 0L
                    liveDailyGoldExp = if (isToday) (snap.getLong("dailyGoldExp") ?: 0L) else 0L
                    liveDailyMicExp = if (isToday) (snap.getLong("dailyMicExp") ?: 0L) else 0L
                    liveDailyNewMicExp = if (isToday) (snap.getLong("dailyNewUserMicExp") ?: 0L) else 0L
                }
            }

        // 3. Live Gift Events Listener for real-time Room Match / Blind Date & Event activity
        val giftReg = db.collection("rooms").document(room.id)
            .collection("gift_events")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    recentGiftEventsCount = snap.size()
                    var sum = 0L
                    for (doc in snap.documents) {
                        sum += (doc.getLong("diamondCost") ?: doc.getLong("price") ?: 0L)
                    }
                    recentRoomGiftsTotalDiamonds = sum
                }
            }

        onDispose {
            roomReg.remove()
            metaReg.remove()
            giftReg.remove()
        }
    }

    // 4. Live Room Followers (rooms/{room.id}/followers — strictly separate from User Followers) & Host Country
    androidx.compose.runtime.DisposableEffect(room.id, targetHostId) {
        if (targetHostId.isNotBlank()) {
            db.collection("users").document(targetHostId).get().addOnSuccessListener { hostDoc ->
                if (liveCountry.isBlank()) {
                    val hostCountry = hostDoc?.getString("country")?.takeIf { it.isNotBlank() }
                        ?: Locale.getDefault().displayCountry.ifBlank { "India" }
                    liveCountry = hostCountry
                }
            }
        }
        if (room.id.isBlank()) {
            isLoadingFollowers = false
            return@DisposableEffect onDispose {}
        }
        val folReg = db.collection("rooms").document(room.id).collection("followers")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val realDocs = snap.documents.filter { doc ->
                        val id = doc.id
                        id.isNotBlank() &&
                            !id.startsWith("companion_", ignoreCase = true) &&
                            !id.startsWith("dummy_", ignoreCase = true) &&
                            !id.startsWith("seed_", ignoreCase = true) &&
                            !id.startsWith("bot_", ignoreCase = true)
                    }
                    liveFollowerCount = realDocs.size
                    followersList = realDocs.mapNotNull { doc ->
                        try {
                            doc.toObject(FollowerRecord::class.java)
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
                isLoadingFollowers = false
            }
        onDispose { folReg.remove() }
    }

    // Resolve names of kicked/banned users from Firestore when Blocked List is opened
    LaunchedEffect(liveKickedIds, liveBannedIds, activeSubScreen) {
        val allBlocked = (liveKickedIds + liveBannedIds).distinct()
        if (allBlocked.isEmpty()) {
            blockedUserProfiles = emptyList()
        } else if (activeSubScreen == "blocked_list") {
            val loaded = mutableListOf<Pair<String, String>>()
            allBlocked.take(30).forEach { uid ->
                db.collection("users").document(uid).get().addOnSuccessListener { uDoc ->
                    val uName = uDoc?.getString("name")?.takeIf { it.isNotBlank() } ?: "User (${uid.take(6)})"
                    loaded.add(uid to uName)
                    blockedUserProfiles = loaded.toList()
                }
            }
        }
    }

    val nextLevelTargetXp = remember(liveRoomLevel) {
        RoomLevelUpEngine.getNextRoomLevelThreshold(liveRoomLevel)
    }
    val currentLevelStartXp = remember(liveRoomLevel) {
        RoomLevelUpEngine.getThresholdForRoomLevel(liveRoomLevel)
    }
    val levelSpan = (nextLevelTargetXp - currentLevelStartXp).coerceAtLeast(1L)
    val levelProgress = ((liveRoomXp - currentLevelStartXp).coerceAtLeast(0L).toFloat() / levelSpan.toFloat()).coerceIn(0f, 1f)
    val levelProgressPercent = (levelProgress * 100f).toInt().coerceIn(0, 100)

    // Obsidian & Royal Gold Theme Palette (matching the 4-Screen Mobile Design)
    val obsidianBg = Color(0xFF0E0D15)
    val cardObsidian = Color(0xFF181624)
    val rowObsidian = Color(0xFF1E1C2E)
    val goldPrimary = Color(0xFFF5C451)
    val goldBorder = Color(0x66F5C451)
    val subtleBorder = Color(0x26FFFFFF)
    val mutedText = Color(0xFFA3A0B8)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = obsidianBg,
            border = BorderStroke(1.4.dp, goldBorder),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(640.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                // =========================================================================
                // SUB-SCREEN 3: ROOM LEVEL (-10% FASTER XP SYSTEM)
                // =========================================================================
                if (activeSubScreen == "room_level") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activeSubScreen = "main" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Room Level",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top 3D Gold Shield Banner Card
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = cardObsidian,
                                border = BorderStroke(1.2.dp, goldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF282012), Color(0xFF181624), Color(0xFF211A30))
                                            )
                                        )
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // 3D Gold Shield Crest
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1F192E),
                                        border = BorderStroke(1.5.dp, goldPrimary)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Image(
                                                painter = painterResource(id = getRoomCrestBadgeDrawableRes(liveRoomLevel)),
                                                contentDescription = "Room Level Crest",
                                                modifier = Modifier.size(34.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Lv.$liveRoomLevel",
                                                color = goldPrimary,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$levelProgressPercent%",
                                                color = goldPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Next: Lv.${(liveRoomLevel + 1).coerceAtMost(100)}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        // Progress Bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(Color(0xFF2C283E))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(levelProgress.coerceAtLeast(0.04f))
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(Color(0xFFFFE082), Color(0xFFF5C451), Color(0xFF9333EA))
                                                        )
                                                    )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "$liveRoomXp / $nextLevelTargetXp EXP",
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        // Ways to Level Up (-10% Target) Card
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = cardObsidian,
                                border = BorderStroke(1.dp, goldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Ways to Level Up (-10% Target)",
                                            color = goldPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Today's upper limit: ${RoomLevelUpEngine.DAILY_UPPER_LIMIT_EXP} Exp",
                                            color = mutedText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // 1. Cost diamonds in the room
                                    RoomLevelTaskProgressItem(
                                        title = "Cost diamonds in the room",
                                        subtitle = "1 diamond / 1 Exp",
                                        currentExp = liveDailyDiamondExp,
                                        maxExp = RoomLevelUpEngine.DAILY_DIAMOND_CAP_EXP,
                                        goldColor = goldPrimary
                                    )

                                    // 2. Cost golds in the room
                                    RoomLevelTaskProgressItem(
                                        title = "Cost golds in the room",
                                        subtitle = "9 golds / 1 Exp",
                                        currentExp = liveDailyGoldExp,
                                        maxExp = RoomLevelUpEngine.DAILY_GOLD_CAP_EXP,
                                        goldColor = goldPrimary
                                    )

                                    // 3. Take mic
                                    RoomLevelTaskProgressItem(
                                        title = "Take mic",
                                        subtitle = "9 mins / 40 Exp",
                                        currentExp = liveDailyMicExp,
                                        maxExp = RoomLevelUpEngine.DAILY_MIC_CAP_EXP,
                                        goldColor = goldPrimary
                                    )

                                    // 4. Users with 'New' tag take mic
                                    RoomLevelTaskProgressItem(
                                        title = "Users with 'New' tag take mic",
                                        subtitle = "9 mins / 40 Exp",
                                        currentExp = liveDailyNewMicExp,
                                        maxExp = RoomLevelUpEngine.DAILY_NEW_USER_MIC_CAP_EXP,
                                        goldColor = goldPrimary
                                    )
                                }
                            }
                        }

                        // Privilege & Reward Card
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = cardObsidian,
                                border = BorderStroke(1.dp, goldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Privilege & Reward",
                                        color = goldPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val milestones = listOf(
                                            Triple(5, "▶️", "Lv.5\nYouTube"),
                                            Triple(6, "🛡️", "Lv.6\nBadge"),
                                            Triple(10, "👨‍👩‍👧‍👦", "Lv.10\nFamily"),
                                            Triple(21, "👑", "Lv.21\nRoyal")
                                        )
                                        milestones.forEach { (reqLvl, icon, label) ->
                                            val unlocked = liveRoomLevel >= reqLvl
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (unlocked) Color(0xFF282012) else rowObsidian,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (unlocked) goldPrimary else subtleBorder
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(text = icon, fontSize = 18.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = label,
                                                        color = if (unlocked) goldPrimary else mutedText,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center,
                                                        lineHeight = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // =========================================================================
                // SUB-SCREEN 4: ROOM BADGE RULES & REAL-TIME BADGE EQUIP
                // =========================================================================
                else if (activeSubScreen == "badge_rules") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activeSubScreen = "main" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Room Badge Rule",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Real-Time Room Badge Status Card (Zero Fake Badges — strictly checks liveRoomLevel & real room gifts!)
                        item {
                            val hasUnlockedLv6Badge = liveRoomLevel >= 6
                            val hasUnlockedDiamondBadge = recentRoomGiftsTotalDiamonds >= 1000L
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardObsidian,
                                border = BorderStroke(1.2.dp, goldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "🛡️ Active Room Badge Status (Real-Time)",
                                        color = goldPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = when {
                                            liveEquippedBadge.isNotBlank() -> "Equipped Badge: $liveEquippedBadge"
                                            hasUnlockedLv6Badge -> "Unlocked: Lv.$liveRoomLevel Honor Crest Badge"
                                            else -> "Current Room Level: Lv.$liveRoomLevel • Reach Lv.6 or receive 1,000+ Diamond Gifts in room to unlock your first Room Badge."
                                        },
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                    if (canEditRoom && (hasUnlockedLv6Badge || hasUnlockedDiamondBadge)) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (hasUnlockedLv6Badge) {
                                                val badgeId = "Lv.$liveRoomLevel Crest"
                                                Button(
                                                    onClick = {
                                                        db.collection("rooms").document(room.id)
                                                            .update("equippedRoomBadge", badgeId)
                                                        Toast.makeText(context, "🛡️ Equipped $badgeId!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = goldPrimary,
                                                        contentColor = Color.Black
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Wear $badgeId", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            if (hasUnlockedDiamondBadge) {
                                                val badgeId = "Diamond Star Crest"
                                                Button(
                                                    onClick = {
                                                        db.collection("rooms").document(room.id)
                                                            .update("equippedRoomBadge", badgeId)
                                                        Toast.makeText(context, "💎 Equipped $badgeId!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF282012),
                                                        contentColor = goldPrimary
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Wear Diamond Crest", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // The 4 Official Room Badge Rule Cards (Screen 4 exact match)
                        val rules = listOf(
                            "1. What is room badge?" to "Room badge is a core asset of the room. It represents the room's various achievements in LiveTogather and highlights the room's honor.",
                            "2. How can I get the room badge?" to "By 2 ways you can get a room badge: The first way is to achieve various room goals (such as diamond gifts received in your room) step by step to light up each achievement room badge. The second way is to join in official activities to get an honor room badge.",
                            "3. Where are my room badges displayed?" to "The room badges your room is wearing will be displayed in the room badge wall, room profile, room ranking, and room card.",
                            "4. How can I manage my room badge?" to "You can manage and wear room badges from your finished room activities and achievements. You can wear only 1 favorite room badge at a time, prioritized on room cards. Also, if you are the Patriarch of your family, you can manage the separate room badge of your Family Room."
                        )
                        items(rules) { (title, body) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardObsidian,
                                border = BorderStroke(1.dp, goldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = title,
                                        color = goldPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = body,
                                        color = Color.White.copy(alpha = 0.88f),
                                        fontSize = 12.2.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
                // =========================================================================
                // SUB-SCREEN: BLOCKED LIST (KICK-OUT & BANNED USERS)
                // =========================================================================
                else if (activeSubScreen == "blocked_list") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activeSubScreen = "main" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Blocked list (Kick Out)",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    val allBlockedIds = (liveKickedIds + liveBannedIds).distinct()
                    if (allBlockedIds.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No users are currently kicked out or blocked in this room.",
                                color = mutedText,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allBlockedIds) { blockedUid ->
                                val resolvedName = blockedUserProfiles.find { it.first == blockedUid }?.second
                                    ?: "User ID: ${blockedUid.take(8)}"
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = rowObsidian,
                                    border = BorderStroke(1.dp, subtleBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = resolvedName,
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (liveBannedIds.contains(blockedUid)) "Banned from Room" else "Kicked Out",
                                                color = Color(0xFFFF6B6B),
                                                fontSize = 11.sp
                                            )
                                        }
                                        if (canEditRoom) {
                                            Button(
                                                onClick = {
                                                    val newKicked = liveKickedIds - blockedUid
                                                    val newBanned = liveBannedIds - blockedUid
                                                    db.collection("rooms").document(room.id).update(
                                                        mapOf(
                                                            "kickedUserIds" to newKicked,
                                                            "bannedUserIds" to newBanned
                                                        )
                                                    )
                                                    Toast.makeText(context, "Unblocked $resolvedName", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = goldPrimary,
                                                    contentColor = Color.Black
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text("Unblock", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // =========================================================================
                // SUB-SCREEN: ROOM BACKPACK (REAL-TIME OWNED ITEMS FROM FIRESTORE)
                // =========================================================================
                else if (activeSubScreen == "backpack") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activeSubScreen = "main" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Room backpack",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    val frames = currentUser?.ownedFrames.orEmpty()
                    val effects = currentUser?.ownedMicEffects.orEmpty()
                    val links = currentUser?.ownedMicLinks.orEmpty()
                    val allOwned = frames.map { "Avatar Frame: $it" } +
                        effects.map { "Mic Effect: $it" } +
                        links.map { "Mic Connector: $it" }

                    if (allOwned.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Your Room Backpack is currently empty.\nUnlocked frames, mic effects, and room items will appear here in real time.",
                                color = mutedText,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allOwned) { itemLabel ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = rowObsidian,
                                    border = BorderStroke(1.dp, goldBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "🎒 $itemLabel", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = "Owned", color = goldPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                // =========================================================================
                // SUB-SCREEN: BLIND DATE DATA (REAL-TIME ROOM INTERACTION STATS)
                // =========================================================================
                else if (activeSubScreen == "blind_date") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { activeSubScreen = "main" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Blind date data",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = cardObsidian,
                            border = BorderStroke(1.dp, goldBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Real-Time Room Match & Gift Activity", color = goldPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                Text("• Live Room Gifts Sent: $recentGiftEventsCount", color = Color.White, fontSize = 13.sp)
                                Text("• Total Room Gift Diamonds: $recentRoomGiftsTotalDiamonds 💎", color = Color.White, fontSize = 13.sp)
                                Text("• Active Members in Room: $liveActiveUserCount", color = Color.White, fontSize = 13.sp)
                                Text("• Room Level: Lv.$liveRoomLevel ($liveRoomXp EXP)", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
                // =========================================================================
                // MAIN SCREEN: TOP BAR TABS + FULL 15-ROW SCROLLABLE ROOM PROFILE SHEET
                // =========================================================================
                else {
                    // Top Bar: ✕ | Profile | Follower (Count) | Events | Room
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = mutedText
                            )
                        }

                        val tabs = listOf(
                            "profile" to "Profile",
                            "followers" to "Follower ($liveFollowerCount)",
                            "events" to "Events",
                            "room" to "Room"
                        )
                        tabs.forEach { (tabKey, tabLabel) ->
                            val isSelected = selectedTopTab == tabKey
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { selectedTopTab = tabKey }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = tabLabel,
                                        color = if (isSelected) goldPrimary else mutedText,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    if (tabKey == "followers" && liveFollowerCount > 0) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFF3B30))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(if (isSelected) 32.dp else 0.dp)
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isSelected) goldPrimary else Color.Transparent)
                                )
                            }
                        }
                    }

                    // TAB 1: PROFILE (SCREENS 1 & 2 — COMPLETE 15-ROW MOBILE LAYOUT)
                    if (selectedTopTab == "profile") {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Room Header Card (Photo with Camera Icon + Room Title with Edit Icon + ID with Copy + Real Room Badge)
                            item {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = cardObsidian,
                                    border = BorderStroke(1.dp, goldBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Room Rounded-Square Avatar with Camera Icon Overlay
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .border(1.4.dp, goldPrimary, RoundedCornerShape(16.dp))
                                                .clickable(enabled = canEditRoom) {
                                                    if (onPickRoomPhoto != null) {
                                                        onPickRoomPhoto()
                                                    } else {
                                                        onDismiss()
                                                        onOpenEditRoomSettings()
                                                    }
                                                }
                                        ) {
                                            AsyncImage(
                                                model = livePhoto.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                contentDescription = "Room Photo",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            // Bottom-right Camera overlay
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(4.dp)
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xCC000000))
                                                    .border(0.8.dp, goldPrimary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "📷", fontSize = 11.sp)
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = liveTitle,
                                                    color = Color.White,
                                                    fontSize = 15.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (canEditRoom) {
                                                    Text(
                                                        text = "✏️",
                                                        fontSize = 14.sp,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .clickable { showEditTitleModal = true }
                                                            .padding(4.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            // Room ID + Copy Button
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.clickable {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                    clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Room ID", displayRoomId))
                                                    Toast.makeText(context, "Copied Room ID: $displayRoomId", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text(
                                                    text = "ID: $displayRoomId",
                                                    color = mutedText,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(text = "📋", fontSize = 11.sp)
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Real-time 3D Room Shield Crest Badge (strictly reflects real liveRoomLevel / liveEquippedBadge)
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF282012),
                                                border = BorderStroke(1.dp, goldPrimary),
                                                modifier = Modifier.clickable { activeSubScreen = "badge_rules" }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = getRoomCrestBadgeDrawableRes(liveRoomLevel)),
                                                        contentDescription = "Room Badge",
                                                        modifier = Modifier.size(16.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                    Text(
                                                        text = if (liveEquippedBadge.isNotBlank()) liveEquippedBadge else "Lv.$liveRoomLevel",
                                                        color = goldPrimary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Level Progress Card (LV.current -> LV.next with ? Help button opening Screen 3)
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = cardObsidian,
                                    border = BorderStroke(1.dp, goldBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activeSubScreen = "room_level" }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Level",
                                                color = goldPrimary,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, mutedText, CircleShape)
                                                    .clickable { activeSubScreen = "room_level" },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "?",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "LV.$liveRoomLevel",
                                                color = goldPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color(0xFF2C283E))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(levelProgress.coerceAtLeast(0.05f))
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(50))
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(Color(0xFFFFE082), Color(0xFFF5C451), Color(0xFF9333EA))
                                                            )
                                                        )
                                                )
                                            }
                                            Text(
                                                text = "LV.${(liveRoomLevel + 1).coerceAtMost(100)}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "$liveRoomXp / $nextLevelTargetXp EXP (-10%)",
                                            color = Color.White.copy(alpha = 0.88f),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // Row 1: Bonus
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Bonus",
                                    trailingContent = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFF3B30))
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = mutedText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (onOpenRoomBonus != null) {
                                            onDismiss()
                                            onOpenRoomBonus()
                                        } else {
                                            activeSubScreen = "room_level"
                                        }
                                    }
                                )
                            }

                            // Row 2: Room backpack
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Room backpack",
                                    valueText = "",
                                    onClick = { activeSubScreen = "backpack" }
                                )
                            }

                            // Row 3: Blind date data
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Blind date data",
                                    valueText = if (recentGiftEventsCount > 0) "$recentGiftEventsCount gifts" else "",
                                    onClick = { activeSubScreen = "blind_date" }
                                )
                            }

                            // Row 4: Tag
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Tag",
                                    valueText = liveTag,
                                    onClick = {
                                        if (canEditRoom) {
                                            showEditTagModal = true
                                        }
                                    }
                                )
                            }

                            // Row 5: Announcement
                            item {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = rowObsidian,
                                    border = BorderStroke(1.dp, goldBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (canEditRoom) {
                                                showEditAnnouncementModal = true
                                            }
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Announcement",
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = mutedText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (liveAnnouncement.isNotBlank()) {
                                                "✨📢 $liveAnnouncement 📢✨"
                                            } else if (canEditRoom) {
                                                "Tap to set real-time room broadcast announcement"
                                            } else {
                                                "No room announcement set"
                                            },
                                            color = if (liveAnnouncement.isNotBlank()) goldPrimary else mutedText,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Row 6: Room badge
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Room badge",
                                    trailingContent = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = getRoomCrestBadgeDrawableRes(liveRoomLevel)),
                                                contentDescription = "Room Badge",
                                                modifier = Modifier.size(20.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Text(
                                                text = if (liveEquippedBadge.isNotBlank()) liveEquippedBadge else "Lv.$liveRoomLevel",
                                                color = goldPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = mutedText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    onClick = { activeSubScreen = "badge_rules" }
                                )
                            }

                            // Row 7: Follower (Real-Time Count inside Profile list)
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Follower",
                                    valueText = liveFollowerCount.toString(),
                                    showChevron = false,
                                    onClick = { selectedTopTab = "followers" }
                                )
                            }

                            // Row 8: Country (Real-Time Country from Firestore)
                            item {
                                val displayCountry = liveCountry.ifBlank { "India" }
                                val flag = when {
                                    displayCountry.equals("India", true) -> "🇮🇳"
                                    displayCountry.equals("United States", true) || displayCountry.equals("USA", true) -> "🇺🇸"
                                    displayCountry.equals("United Kingdom", true) || displayCountry.equals("UK", true) -> "🇬🇧"
                                    displayCountry.equals("UAE", true) -> "🇦🇪"
                                    else -> "🌍"
                                }
                                ObsidianRoomSettingRow(
                                    label = "Country",
                                    valueText = "$flag $displayCountry",
                                    showChevron = canEditRoom,
                                    onClick = {
                                        if (canEditRoom) {
                                            showEditCountryModal = true
                                        }
                                    }
                                )
                            }

                            // Row 9: Theme
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Theme",
                                    valueText = liveThemeName.ifBlank { "Classic Lounge" },
                                    onClick = {
                                        if (canEditRoom) {
                                            onDismiss()
                                            onOpenEditRoomSettings()
                                        }
                                    }
                                )
                            }

                            // Row 10: Room enter permission (Everyone / Family)
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Room enter permission",
                                    valueText = liveEnterPermission,
                                    onClick = {
                                        if (canEditRoom) {
                                            val enableFamily = liveEnterPermission != "Family"
                                            if (enableFamily && liveRoomLevel < WatchPartyCinemaEngine.REQUIRED_FAMILY_ROOM_LEVEL &&
                                                (currentUser == null || !WatchPartyCinemaEngine.isUnrestrictedFounderOrCeo(currentUser))
                                            ) {
                                                Toast.makeText(
                                                    context,
                                                    "Family Room permission unlocks at Room Level 10 (Current: Lv.$liveRoomLevel)",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                val newPriv = if (enableFamily) "family_only" else "public"
                                                liveEnterPermission = if (enableFamily) "Family" else "Everyone"
                                                WatchPartyCinemaEngine.updateFamilyRoomState(
                                                    db = db,
                                                    room = room,
                                                    isFamilyActive = enableFamily,
                                                    familyMemberIds = room.familyMemberIds
                                                )
                                                onRoomUpdated?.invoke(room.copy(isFamilyRoom = enableFamily, privacy = newPriv))
                                                Toast.makeText(
                                                    context,
                                                    "Room enter permission set to: $liveEnterPermission",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                )
                            }

                            // Row 11: Number of Mic (10 / 12 / 15)
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Number of Mic",
                                    valueText = "$liveMaxMics",
                                    onClick = {
                                        if (canEditRoom) {
                                            val nextMics = when (liveMaxMics) {
                                                10 -> 12
                                                12 -> 15
                                                else -> 10
                                            }
                                            liveMaxMics = nextMics
                                            db.collection("rooms").document(room.id).update("maxMicSeats", nextMics)
                                            if (room.category.isNotBlank()) {
                                                db.collection("intent_categories").document(room.category)
                                                    .collection("rooms").document(room.id).update("maxMicSeats", nextMics)
                                            }
                                            Toast.makeText(context, "Number of Mics updated to $nextMics", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }

                            // Row 12: Mode (💬 Chat / 🎬 YouTube)
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Mode",
                                    trailingContent = {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFF282012),
                                            border = BorderStroke(1.dp, goldPrimary)
                                        ) {
                                            Text(
                                                text = if (liveIsWatchParty) "🎬 YouTube"
                                                else if (liveRoomLevel < WatchPartyCinemaEngine.REQUIRED_ROOM_LEVEL) "💬 Chat • 🔒 YouTube Lv.5"
                                                else "💬 Chat",
                                                color = goldPrimary,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (canEditRoom) {
                                            val targetWp = !liveIsWatchParty
                                            if (targetWp && liveRoomLevel < WatchPartyCinemaEngine.REQUIRED_ROOM_LEVEL) {
                                                Toast.makeText(
                                                    context,
                                                    "🔒 YouTube Room is locked! Reach Room Level 5 to unlock (Current: Lv.$liveRoomLevel)",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                liveIsWatchParty = targetWp
                                                val updater = currentUser?.name?.ifBlank { "Host" } ?: "Host"
                                                WatchPartyCinemaEngine.updateRoomWatchPartyState(
                                                    db = db,
                                                    room = room,
                                                    isActive = targetWp,
                                                    updatedBy = updater
                                                )
                                                onRoomUpdated?.invoke(room.copy(isWatchPartyActive = targetWp))
                                                Toast.makeText(
                                                    context,
                                                    if (targetWp) "🎬 Switched Room Mode to YouTube!" else "💬 Switched Room Mode to Chat!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                )
                            }

                            // Row 13: Mic permission (Everyone / Family)
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Mic permission",
                                    valueText = liveMicPermission,
                                    onClick = {
                                        if (canEditRoom) {
                                            val nextPerm = if (liveMicPermission == "Everyone") "Family" else "Everyone"
                                            liveMicPermission = nextPerm
                                            db.collection("rooms").document(room.id).update("micPermission", nextPerm)
                                            Toast.makeText(context, "Mic permission set to: $nextPerm", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }

                            // Row 14: Blocked list (Kick Out)
                            item {
                                val blockedCount = (liveKickedIds + liveBannedIds).distinct().size
                                ObsidianRoomSettingRow(
                                    label = "Blocked list (Kick Out)",
                                    valueText = if (blockedCount > 0) "View Blocked ($blockedCount)" else "View Blocked",
                                    onClick = { activeSubScreen = "blocked_list" }
                                )
                            }

                            // Row 15: Room Games / Party
                            item {
                                ObsidianRoomSettingRow(
                                    label = "Room Games / Party",
                                    valueText = "",
                                    onClick = {
                                        if (onOpenRoomGames != null) {
                                            onDismiss()
                                            onOpenRoomGames()
                                        } else {
                                            onDismiss()
                                            onOpenEditRoomSettings()
                                        }
                                    }
                                )
                            }
                        }
                    }
                    // TAB 2: FOLLOWER (REAL-TIME FOLLOWERS LIST FROM FIRESTORE)
                    else if (selectedTopTab == "followers") {
                        if (isLoadingFollowers) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Loading real-time followers...", color = mutedText, fontSize = 12.5.sp)
                            }
                        } else if (followersList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No followers yet.", color = mutedText, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(followersList) { follower ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = rowObsidian,
                                        border = BorderStroke(1.dp, subtleBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            AsyncImage(
                                                model = follower.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                contentDescription = follower.name,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, goldPrimary, CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Text(
                                                text = follower.name.ifBlank { "Member" },
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // TAB 3: EVENTS (REAL-TIME ROOM EVENTS & DAILY TASKS)
                    else if (selectedTopTab == "events") {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardObsidian,
                                border = BorderStroke(1.dp, goldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🎉 Live Room Activity & Events", color = goldPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        text = "• Today's Room XP Earned: ${liveDailyDiamondExp + liveDailyGoldExp + liveDailyMicExp + liveDailyNewMicExp} / ${RoomLevelUpEngine.DAILY_UPPER_LIMIT_EXP} Exp",
                                        color = Color.White,
                                        fontSize = 12.5.sp
                                    )
                                    Text(
                                        text = "• Live Room Gift Events: $recentGiftEventsCount ($recentRoomGiftsTotalDiamonds 💎)",
                                        color = Color.White,
                                        fontSize = 12.5.sp
                                    )
                                    Button(
                                        onClick = { activeSubScreen = "room_level" },
                                        colors = ButtonDefaults.buttonColors(containerColor = goldPrimary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("View -10% Room Level-Up Tasks", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    }
                                }
                            }
                        }
                    }
                    // TAB 4: ROOM (REAL-TIME ROOM SUMMARY & FULL SETTINGS TRIGGER)
                    else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardObsidian,
                                border = BorderStroke(1.dp, goldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🏠 Real-Time Room Status", color = goldPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("• Room Name: $liveTitle", color = Color.White, fontSize = 12.5.sp)
                                    Text("• Room ID: $displayRoomId", color = Color.White, fontSize = 12.5.sp)
                                    Text("• Active Mode: ${if (liveIsWatchParty) "YouTube Cinema" else "Chat / Audio"}", color = Color.White, fontSize = 12.5.sp)
                                    Text("• Enter Permission: $liveEnterPermission • Mic Seats: $liveMaxMics", color = Color.White, fontSize = 12.5.sp)
                                    if (canEditRoom) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                onDismiss()
                                                onOpenEditRoomSettings()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = goldPrimary, contentColor = Color.Black),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Open Advanced Host Controls", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Inline Real-Time Room Name Edit Dialog (Saves directly to Firestore rooms/{roomId})
    if (showEditTitleModal) {
        var newTitleInput by remember { mutableStateOf(liveTitle) }
        Dialog(onDismissRequest = { showEditTitleModal = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardObsidian,
                border = BorderStroke(1.2.dp, goldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Edit Room Name", color = goldPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    OutlinedTextField(
                        value = newTitleInput,
                        onValueChange = { newTitleInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = goldPrimary,
                            unfocusedBorderColor = subtleBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                val clean = newTitleInput.trim()
                                if (clean.isNotBlank()) {
                                    liveTitle = clean
                                    db.collection("rooms").document(room.id).update(
                                        mapOf("title" to clean, "roomName" to clean)
                                    )
                                    if (room.category.isNotBlank()) {
                                        db.collection("intent_categories").document(room.category)
                                            .collection("rooms").document(room.id)
                                            .update(mapOf("title" to clean, "roomName" to clean))
                                    }
                                    onRoomUpdated?.invoke(room.copy(title = clean, roomName = clean))
                                    Toast.makeText(context, "Room name updated!", Toast.LENGTH_SHORT).show()
                                }
                                showEditTitleModal = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = goldPrimary, contentColor = Color.Black)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Inline Real-Time Announcement Edit Dialog
    if (showEditAnnouncementModal) {
        var newAnnInput by remember { mutableStateOf(liveAnnouncement) }
        Dialog(onDismissRequest = { showEditAnnouncementModal = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardObsidian,
                border = BorderStroke(1.2.dp, goldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Edit Room Announcement", color = goldPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    OutlinedTextField(
                        value = newAnnInput,
                        onValueChange = { newAnnInput = it },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = goldPrimary,
                            unfocusedBorderColor = subtleBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                val clean = newAnnInput.trim()
                                liveAnnouncement = clean
                                db.collection("rooms").document(room.id).update("announcement", clean)
                                if (room.category.isNotBlank()) {
                                    db.collection("intent_categories").document(room.category)
                                        .collection("rooms").document(room.id).update("announcement", clean)
                                }
                                onRoomUpdated?.invoke(room.copy(announcement = clean))
                                Toast.makeText(context, "Announcement updated!", Toast.LENGTH_SHORT).show()
                                showEditAnnouncementModal = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = goldPrimary, contentColor = Color.Black)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Inline Real-Time Tag Selector Dialog
    if (showEditTagModal) {
        val availableTags = listOf("Chat", "Music", "Dating", "Party", "Games", "Cinema")
        Dialog(onDismissRequest = { showEditTagModal = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardObsidian,
                border = BorderStroke(1.2.dp, goldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Room Tag", color = goldPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    availableTags.forEach { tagOption ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (liveTag.equals(tagOption, true)) Color(0xFF282012) else rowObsidian,
                            border = BorderStroke(1.dp, if (liveTag.equals(tagOption, true)) goldPrimary else subtleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    liveTag = tagOption
                                    db.collection("rooms").document(room.id).update("roomTag", tagOption)
                                    Toast.makeText(context, "Tag set to $tagOption", Toast.LENGTH_SHORT).show()
                                    showEditTagModal = false
                                }
                        ) {
                            Text(
                                text = tagOption,
                                color = if (liveTag.equals(tagOption, true)) goldPrimary else Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Inline Real-Time Country Selector Dialog (Auto-Synced to Navigation/GPS Country with Full World Country List)
    if (showEditCountryModal) {
        val detectedRoomCountry = remember(currentUser?.latitude, currentUser?.longitude) {
            val fromGeocoder = try {
                val u = currentUser
                if (u != null && (u.latitude != 0.0 || u.longitude != 0.0)) {
                    @Suppress("DEPRECATION")
                    val addrs = android.location.Geocoder(context, java.util.Locale.ENGLISH)
                        .getFromLocation(u.latitude, u.longitude, 1)
                    addrs?.firstOrNull()?.countryName?.takeIf { it.isNotBlank() }
                } else null
            } catch (_: Exception) {
                null
            }
            val fromTelephony = try {
                val tm = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
                val iso = tm?.networkCountryIso?.takeIf { it.isNotBlank() } ?: tm?.simCountryIso?.takeIf { it.isNotBlank() }
                if (!iso.isNullOrBlank()) java.util.Locale("", iso).getDisplayCountry(java.util.Locale.ENGLISH).takeIf { it.isNotBlank() } else null
            } catch (_: Exception) {
                null
            }
            fromGeocoder ?: fromTelephony ?: java.util.Locale.getDefault().getDisplayCountry(java.util.Locale.ENGLISH).ifBlank { "India" }
        }

        val worldCountries = remember(detectedRoomCountry) {
            listOf(
                "India" to "🇮🇳",
                "United States" to "🇺🇸",
                "United Kingdom" to "🇬🇧",
                "United Arab Emirates" to "🇦🇪",
                "Saudi Arabia" to "🇸🇦",
                "Canada" to "🇨🇦",
                "Australia" to "🇦🇺",
                "Singapore" to "🇸🇬",
                "Malaysia" to "🇲🇾",
                "Nepal" to "🇳🇵",
                "Bangladesh" to "🇧🇩",
                "Pakistan" to "🇵🇰",
                "Indonesia" to "🇮🇩",
                "Philippines" to "🇵🇭",
                "Germany" to "🇩🇪",
                "France" to "🇫🇷",
                "Japan" to "🇯🇵",
                "South Korea" to "🇰🇷",
                "Brazil" to "🇧🇷",
                "South Africa" to "🇿🇦"
            ).sortedByDescending { it.first.equals(detectedRoomCountry, ignoreCase = true) }
        }

        LaunchedEffect(detectedRoomCountry) {
            if (canEditRoom && detectedRoomCountry.isNotBlank() && !liveCountry.equals(detectedRoomCountry, ignoreCase = true)) {
                liveCountry = detectedRoomCountry
                db.collection("rooms").document(room.id).update("country", detectedRoomCountry)
            }
        }

        Dialog(onDismissRequest = { showEditCountryModal = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardObsidian,
                border = BorderStroke(1.2.dp, goldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Room Country (Auto-Navigation)", color = goldPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = "📍 Navigation Location: $detectedRoomCountry (Locked to real GPS region)",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(worldCountries) { (cName, cFlag) ->
                            val isCurrentNavCountry = cName.equals(detectedRoomCountry, ignoreCase = true)
                            val isSelected = liveCountry.equals(cName, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF282012) else rowObsidian,
                                border = BorderStroke(1.dp, if (isSelected) goldPrimary else subtleBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isCurrentNavCountry) {
                                            liveCountry = detectedRoomCountry
                                            db.collection("rooms").document(room.id).update("country", detectedRoomCountry)
                                            Toast.makeText(context, "📍 Country synced via Navigation: $detectedRoomCountry", Toast.LENGTH_SHORT).show()
                                            showEditCountryModal = false
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "📍 Navigation Locked: You are currently in $detectedRoomCountry. Other countries cannot be selected.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$cFlag  $cName",
                                        color = if (isCurrentNavCountry) Color.White else mutedText.copy(alpha = 0.55f),
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrentNavCountry) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isCurrentNavCountry) "📍 Auto GPS Active" else "🔒 Region Locked",
                                        color = if (isCurrentNavCountry) goldPrimary else mutedText.copy(alpha = 0.5f),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = { showEditCountryModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = goldPrimary, contentColor = Color.Black)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObsidianRoomSettingRow(
    label: String,
    valueText: String = "",
    showChevron: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E1C2E),
        border = BorderStroke(1.dp, Color(0x26FFFFFF)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (trailingContent != null) {
                trailingContent()
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (valueText.isNotBlank()) {
                        Text(
                            text = valueText,
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (showChevron) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFA3A0B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomLevelTaskProgressItem(
    title: String,
    subtitle: String,
    currentExp: Long,
    maxExp: Long,
    goldColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = goldColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$currentExp / $maxExp",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        val progress = (currentExp.toFloat() / maxExp.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF2C283E))
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceAtLeast(0.03f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(goldColor)
                )
            }
        }
    }
}

/**
 * Right-Side Animated 4-Square Round Button ("ROOMS") replacing the standalone YouTube button.
 * Opens the compact 2x2 Square Room Conversion Hub (Standard 12-Mic Audio Room, YouTube Cinema Room, Family Room Lv.10+, Video Stream).
 */
@Composable
fun FloatingFourSquareRoomHubButton(
    isCinemaActive: Boolean,
    isFamilyRoomActive: Boolean,
    isVideoRoomActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "four_square_hub_anim")
    val sq1Scale by infiniteTransition.animateFloat(
        initialValue = 0.84f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sq1_scale"
    )
    val sq2Scale by infiniteTransition.animateFloat(
        initialValue = 1.06f,
        targetValue = 0.84f,
        animationSpec = infiniteRepeatable(tween(900, delayMillis = 220, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sq2_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hub_glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A),
                            Color(0xFF090D16)
                        )
                    )
                )
                .border(
                    width = 1.6.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFFD700).copy(alpha = glowAlpha),
                            Color(0xFF38BDF8).copy(alpha = glowAlpha),
                            Color(0xFFF43F5E).copy(alpha = glowAlpha),
                            Color(0xFF10B981).copy(alpha = glowAlpha),
                            Color(0xFFFFD700).copy(alpha = glowAlpha)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.5.dp)
                            .scale(sq1Scale)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFF59E0B))))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.5.dp)
                            .scale(sq2Scale)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFDC2626))))
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.5.dp)
                            .scale(sq2Scale)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF7DD3FC), Color(0xFF0284C7))))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.5.dp)
                            .scale(sq1Scale)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF6EE7B7), Color(0xFF059669))))
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xE60B1120),
            border = BorderStroke(0.7.dp, Color(0xCCFFD700))
        ) {
            Text(
                text = "ROOMS",
                color = Color(0xFFFFE57F),
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
            )
        }
    }
}

/**
 * High-Definition 3D-Styled Animated Studio Condenser Microphone Icon for Standard Audio Room (12-Mic Stage).
 */
@Composable
fun HdAnimatedAudioMicIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hd_mic_anim")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "mic_ring_pulse"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "mic_float_y"
    )

    Box(
        modifier = modifier
            .size(46.dp)
            .offset(y = floatY.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(ringPulse)
                .clip(CircleShape)
                .border(1.dp, Color(0x66FBBF24), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0x44F59E0B),
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(1.2.dp, Color(0xCCFBBF24), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(28.dp)) {
                val w = size.width
                val h = size.height
                // Left & Right Sonic Wave Arcs
                drawArc(
                    color = Color(0xFF38BDF8),
                    startAngle = 135f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(w * 0.06f, h * 0.22f),
                    size = Size(w * 0.35f, h * 0.42f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFF38BDF8),
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(w * 0.59f, h * 0.22f),
                    size = Size(w * 0.35f, h * 0.42f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                // 3D Golden Metallic Condenser Capsule
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFF3B0), Color(0xFFF59E0B), Color(0xFFB45309))
                    ),
                    topLeft = Offset(w * 0.34f, h * 0.12f),
                    size = Size(w * 0.32f, h * 0.46f),
                    cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
                )
                // Grille mesh lines
                for (frac in listOf(0.24f, 0.34f, 0.44f)) {
                    drawLine(
                        color = Color(0x8878350F),
                        start = Offset(w * 0.38f, h * frac),
                        end = Offset(w * 0.62f, h * frac),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                // U-Cradle Mount & Base
                drawArc(
                    color = Color(0xFFFDE047),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.25f, h * 0.30f),
                    size = Size(w * 0.50f, h * 0.40f),
                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(w * 0.50f, h * 0.70f),
                    end = Offset(w * 0.50f, h * 0.88f),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFFFDE047),
                    start = Offset(w * 0.33f, h * 0.88f),
                    end = Offset(w * 0.67f, h * 0.88f),
                    strokeWidth = 2.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * High-Definition 3D Glossy YouTube Cinema Play Emblem + Animated Equalizer Bars.
 */
@Composable
fun HdAnimatedYouTubeCinemaIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hd_yt_anim")
    val eq1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 11f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "yt_eq1"
    )
    val eq2 by infiniteTransition.animateFloat(
        initialValue = 11f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(360, delayMillis = 110, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "yt_eq2"
    )
    val eq3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(460, delayMillis = 200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "yt_eq3"
    )

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0x55EF4444),
                        Color(0xFF2A1225),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(1.2.dp, Color(0xCCF87171), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(width = 26.dp, height = 18.dp)) {
                val w = size.width
                val h = size.height
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFF4B4B), Color(0xFFDC2626), Color(0xFF991B1B))
                    ),
                    cornerRadius = CornerRadius(w * 0.24f, w * 0.24f)
                )
                val playPath = Path().apply {
                    moveTo(w * 0.39f, h * 0.25f)
                    lineTo(w * 0.39f, h * 0.75f)
                    lineTo(w * 0.69f, h * 0.50f)
                    close()
                }
                drawPath(path = playPath, color = Color.White)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.height(12.dp)
            ) {
                Box(modifier = Modifier.width(2.5.dp).height(eq1.dp).clip(RoundedCornerShape(50)).background(Color(0xFFFDE047)))
                Box(modifier = Modifier.width(2.5.dp).height(eq2.dp).clip(RoundedCornerShape(50)).background(Color(0xFF38BDF8)))
                Box(modifier = Modifier.width(2.5.dp).height(eq3.dp).clip(RoundedCornerShape(50)).background(Color(0xFFFB7185)))
                Box(modifier = Modifier.width(2.5.dp).height(eq1.dp).clip(RoundedCornerShape(50)).background(Color(0xFF34D399)))
            }
        }
    }
}

/**
 * High-Definition 3D Human Family Silhouette Group Icon (Parents + Center Member + Golden Crown Aura).
 */
@Composable
fun HdAnimatedHumanFamilyIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hd_family_anim")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fam_ring"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fam_float"
    )

    Box(
        modifier = modifier
            .size(46.dp)
            .offset(y = floatY.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(ringScale)
                .clip(CircleShape)
                .border(1.dp, Color(0x66FBBF24), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0x44F59E0B),
                            Color(0xFF2E1065),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(1.2.dp, Color(0xCCFBBF24), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(30.dp)) {
                val w = size.width
                val h = size.height

                // Top Golden Family Crown
                val crownPath = Path().apply {
                    moveTo(w * 0.36f, h * 0.22f)
                    lineTo(w * 0.43f, h * 0.14f)
                    lineTo(w * 0.50f, h * 0.20f)
                    lineTo(w * 0.57f, h * 0.14f)
                    lineTo(w * 0.64f, h * 0.22f)
                    close()
                }
                drawPath(path = crownPath, color = Color(0xFFFDE047))

                // Left Adult Human Silhouette (Father / Member 1 - Royal Sky Blue 3D Gradient)
                drawCircle(
                    brush = Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFF0284C7))),
                    radius = w * 0.105f,
                    center = Offset(w * 0.26f, h * 0.38f)
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF7DD3FC), Color(0xFF0369A1))),
                    topLeft = Offset(w * 0.11f, h * 0.52f),
                    size = Size(w * 0.30f, h * 0.28f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
                )

                // Right Adult Human Silhouette (Mother / Member 2 - Rose Pink 3D Gradient)
                drawCircle(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFCE7F3), Color(0xFFDB2777))),
                    radius = w * 0.105f,
                    center = Offset(w * 0.74f, h * 0.38f)
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFF472B6), Color(0xFF9D174D))),
                    topLeft = Offset(w * 0.59f, h * 0.52f),
                    size = Size(w * 0.30f, h * 0.28f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
                )

                // Center Foreground Human Silhouette (Golden Family Leader / Child)
                drawCircle(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFF59E0B))),
                    radius = w * 0.12f,
                    center = Offset(w * 0.50f, h * 0.43f)
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFDE047), Color(0xFFB45309))),
                    topLeft = Offset(w * 0.32f, h * 0.57f),
                    size = Size(w * 0.36f, h * 0.30f),
                    cornerRadius = CornerRadius(w * 0.15f, w * 0.15f)
                )
            }
        }
    }
}

/**
 * High-Definition 3D Video Stream Camera Lens Icon + Live Pulsing Recording Dot.
 */
@Composable
fun HdAnimatedVideoCameraIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hd_cam_anim")
    val liveScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.30f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cam_live_dot"
    )

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0x4410B981),
                        Color(0xFF064E3B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(1.2.dp, Color(0xCC34D399), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFF6EE7B7), Color(0xFF059669))),
                topLeft = Offset(w * 0.10f, h * 0.25f),
                size = Size(w * 0.56f, h * 0.50f),
                cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
            )
            val lensPath = Path().apply {
                moveTo(w * 0.68f, h * 0.38f)
                lineTo(w * 0.90f, h * 0.26f)
                lineTo(w * 0.90f, h * 0.74f)
                lineTo(w * 0.68f, h * 0.62f)
                close()
            }
            drawPath(
                path = lensPath,
                brush = Brush.linearGradient(listOf(Color(0xFF6EE7B7), Color(0xFF047857)))
            )
            drawCircle(
                color = Color(0xFF064E3B),
                radius = w * 0.14f,
                center = Offset(w * 0.38f, h * 0.50f)
            )
            drawCircle(
                color = Color(0xFF34D399),
                radius = w * 0.07f,
                center = Offset(w * 0.38f, h * 0.50f)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(6.dp)
                .scale(liveScale)
                .clip(CircleShape)
                .background(Color(0xFFF43F5E))
        )
    }
}

/**
 * Compact 2x2 Square Room Conversion Hub Dialog opened by the Right-Side 4-Square Icon.
 * Displays 4 truly square (1:1 aspect ratio) tiles with HD 3D animated icons and a clear "🔄 Switch" / "✓ Active" pill at the bottom:
 * 1. Audio Room (12-Mic Stage)
 * 2. YouTube Room (Watch Party • Lv.5+)
 * 3. Family Room (Invite Only • Lv.10+)
 * 4. Video Stream (1P–4P Live Cam)
 */
@Composable
fun RoomConversionHubDialog(
    room: RoomItem,
    currentUser: UserProfile,
    currentRoomLevel: Int,
    canModerateRoom: Boolean,
    isCinemaActive: Boolean,
    onSwitchToStandardAudio: () -> Unit,
    onSwitchToYouTubeCinema: () -> Unit,
    onToggleFamilyRoom: (Boolean) -> Unit,
    onOpenFamilyInviteDialog: () -> Unit,
    onSwitchToVideoStream: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isStandardAudioActive = !isCinemaActive && !room.isVideoRoom && !room.isFamilyRoom
    val canUnlockYouTube = WatchPartyCinemaEngine.canUnlockCinemaWatchParty(currentUser, currentRoomLevel)
    val canUnlockFamily = WatchPartyCinemaEngine.canUnlockFamilyRoom(currentUser, currentRoomLevel)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF141821),
            border = BorderStroke(1.3.dp, Color(0xFF2A3245)),
            shadowElevation = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFFFBBF24)))
                                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFFEF4444)))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF38BDF8)))
                                Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF10B981)))
                            }
                        }
                        Column {
                            Text(
                                text = "Switch Room Mode",
                                color = Color(0xFFF8FAFC),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Room Lv.$currentRoomLevel • Instant real-time conversion",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2x2 Square Grid Row 1: Audio Room (12-Mic Stage) & YouTube Cinema Room
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // TILE 1: STANDARD AUDIO ROOM (12-Mic Stage)
                    RoomModeSquareTile(
                        title = "Audio Room",
                        subtitle = "12-Mic Stage",
                        badgeText = "12 Mics",
                        badgeColor = Color(0xFFFBBF24),
                        isActive = isStandardAudioActive,
                        accentColor = Color(0xFFFBBF24),
                        iconContent = { HdAnimatedAudioMicIcon() },
                        onClick = {
                            if (!canModerateRoom) {
                                Toast.makeText(context, "Only Host or Admin can switch room modes", Toast.LENGTH_SHORT).show()
                            } else {
                                onSwitchToStandardAudio()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // TILE 2: YOUTUBE CINEMA ROOM (Lv.5+)
                    RoomModeSquareTile(
                        title = "YouTube Room",
                        subtitle = if (canUnlockYouTube) "Watch Party" else "🔒 Unlocks Lv.5",
                        badgeText = if (canUnlockYouTube) "Lv.5+" else "🔒 Lv.5",
                        badgeColor = Color(0xFF38BDF8),
                        isActive = isCinemaActive,
                        isLocked = !canUnlockYouTube,
                        accentColor = Color(0xFF38BDF8),
                        iconContent = { HdAnimatedYouTubeCinemaIcon() },
                        onClick = {
                            if (!canModerateRoom) {
                                Toast.makeText(context, "Only Host or Admin can switch room modes", Toast.LENGTH_SHORT).show()
                            } else if (!canUnlockYouTube) {
                                onSwitchToYouTubeCinema()
                                onDismiss()
                            } else {
                                onSwitchToYouTubeCinema()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2x2 Square Grid Row 2: Family Room (Lv.10+) & Video Stream
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // TILE 3: FAMILY ROOM (Invite Only • Room Level 10+)
                    RoomModeSquareTile(
                        title = "Family Room",
                        subtitle = if (canUnlockFamily) "Invite Only • Lv.10" else "🔒 Unlocks Lv.10",
                        badgeText = if (canUnlockFamily) "Lv.10+" else "🔒 Lv.10",
                        badgeColor = Color(0xFFF59E0B),
                        isActive = room.isFamilyRoom,
                        isLocked = !canUnlockFamily,
                        accentColor = Color(0xFFF59E0B),
                        iconContent = { HdAnimatedHumanFamilyIcon() },
                        onClick = {
                            if (!canModerateRoom) {
                                Toast.makeText(context, "Only Host or Admin can configure Family Room", Toast.LENGTH_SHORT).show()
                            } else if (!canUnlockFamily) {
                                Toast.makeText(
                                    context,
                                    "🔒 Family Room unlocks at Room Level 10! (Current Room Level: Lv.$currentRoomLevel)",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                onToggleFamilyRoom(!room.isFamilyRoom)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // TILE 4: LIVE VIDEO STREAM (1P-4P Camera • Room Level 5+)
                    RoomModeSquareTile(
                        title = "Video Stream",
                        subtitle = if (canUnlockYouTube) "1P–4P Live Cam" else "🔒 Unlocks Lv.5",
                        badgeText = if (canUnlockYouTube) "LIVE" else "🔒 Lv.5",
                        badgeColor = Color(0xFF10B981),
                        isActive = room.isVideoRoom,
                        isLocked = !canUnlockYouTube,
                        accentColor = Color(0xFF10B981),
                        iconContent = { HdAnimatedVideoCameraIcon() },
                        onClick = {
                            if (!canModerateRoom) {
                                Toast.makeText(context, "Only Host or Admin can switch to Video Stream", Toast.LENGTH_SHORT).show()
                            } else if (!canUnlockYouTube) {
                                Toast.makeText(
                                    context,
                                    "🔒 Video Room unlocks at Room Level 5! (Current Room Level: Lv.$currentRoomLevel)",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                onSwitchToVideoStream()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick Family Member Invite Strip when Family Room is unlocked or active
                if (canModerateRoom && (room.isFamilyRoom || canUnlockFamily)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1A2030),
                        border = BorderStroke(1.dp, Color(0x66F59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "👨‍👩‍👧‍👦 Family Members (${room.familyMemberIds.size})",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (room.isFamilyRoom) "Only invited Family Members can enter" else "Tap + Invite to manage your Family list",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.5.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0x33F59E0B),
                                border = BorderStroke(1.dp, Color(0xFFFBBF24)),
                                modifier = Modifier.clickable {
                                    onDismiss()
                                    onOpenFamilyInviteDialog()
                                }
                            ) {
                                Text(
                                    text = "+ Invite Family",
                                    color = Color(0xFFFDE047),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomModeSquareTile(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    isActive: Boolean,
    accentColor: Color,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isActive) Color(0xFF1D263B) else Color(0xFF1A2030),
        border = BorderStroke(
            width = if (isActive) 1.8.dp else 1.dp,
            color = if (isActive) accentColor else if (isLocked) Color(0xFF475569) else Color(0xFF2E374D)
        ),
        shadowElevation = if (isActive) 8.dp else 2.dp,
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // Top-left Lock Icon when locked
            if (isLocked) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xCC0F172A),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.align(Alignment.TopStart).size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Top-right badge pill
            Surface(
                shape = RoundedCornerShape(50),
                color = badgeColor.copy(alpha = 0.16f),
                border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.75f)),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(contentAlignment = Alignment.Center) {
                    iconContent()
                    if (isLocked) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xD90F172A),
                            border = BorderStroke(1.2.dp, Color(0xFFFBBF24)),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked by Level",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = if (isActive) accentColor else if (isLocked) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Bottom "Switch" / "🔒 Locked" / "✓ Active" Action Pill inside each square tile
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isActive) accentColor.copy(alpha = 0.25f) else if (isLocked) Color(0xFF1F1724) else Color(0xFF111827),
                    border = BorderStroke(
                        0.9.dp,
                        if (isActive) accentColor else if (isLocked) Color(0xFFF59E0B) else Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isActive) "✓ Active" else if (isLocked) "🔒 Locked" else "🔄 Switch",
                        color = if (isActive) Color.White else if (isLocked) Color(0xFFFDE047) else Color(0xFFE2E8F0),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Real-Time Family Room Member Invite & Access Control Dialog.
 * Allows the Host/Admin (at Room Level 10+) to add or remove users in `rooms/{roomId}.familyMemberIds`.
 */
@Composable
fun FamilyRoomMembersInviteDialog(
    room: RoomItem,
    currentUser: UserProfile,
    currentRoomLevel: Int,
    activeMembers: List<RoomMember>,
    allProfiles: List<UserProfile>,
    db: FirebaseFirestore,
    onUpdateRoom: (RoomItem) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var familyMembers by remember(room.familyMemberIds) { mutableStateOf(room.familyMemberIds) }
    var isFamilyEnabled by remember(room.isFamilyRoom) { mutableStateOf(room.isFamilyRoom) }
    val canUnlockFamily = WatchPartyCinemaEngine.canUnlockFamilyRoom(currentUser, currentRoomLevel)

    // Combine active room members + all known user profiles for easy 1-tap invitation
    val candidateUsers = remember(activeMembers, allProfiles, searchQuery) {
        val map = linkedMapOf<String, Pair<String, String>>()
        activeMembers.forEach { m ->
            if (m.uid.isNotBlank() && m.uid != room.hostUid && m.uid != room.hostId) {
                map[m.uid] = m.name.ifBlank { "Member" } to m.photoUrl
            }
        }
        allProfiles.forEach { p ->
            if (p.uid.isNotBlank() && p.uid != room.hostUid && p.uid != room.hostId && !map.containsKey(p.uid)) {
                map[p.uid] = p.name.ifBlank { "User" } to p.photo
            }
        }
        val q = searchQuery.trim().lowercase(Locale.US)
        map.entries.filter { (uid, pair) ->
            q.isBlank() || pair.first.lowercase(Locale.US).contains(q) || uid.lowercase(Locale.US).contains(q)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF141821),
            border = BorderStroke(1.3.dp, Color(0xCCF59E0B)),
            shadowElevation = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HdAnimatedHumanFamilyIcon(modifier = Modifier.size(38.dp))
                        Column {
                            Text(
                                text = "Family Room Control",
                                color = Color(0xFFFDE68A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Unlocks at Room Lv.10 • Invite-only entry",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1A2030),
                    border = BorderStroke(1.dp, Color(0x55F59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛡️ Family Room Entry Lock",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isFamilyEnabled) "ON: Only invited Family Members can enter" else "OFF: Everyone can enter the room",
                                color = if (isFamilyEnabled) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isFamilyEnabled,
                            onCheckedChange = { enable ->
                                if (!canUnlockFamily) {
                                    Toast.makeText(
                                        context,
                                        "🔒 Family Room requires Room Level 10! (Current: Lv.$currentRoomLevel)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    isFamilyEnabled = enable
                                    WatchPartyCinemaEngine.updateFamilyRoomState(
                                        db = db,
                                        room = room,
                                        isFamilyActive = enable,
                                        familyMemberIds = familyMembers
                                    )
                                    onUpdateRoom(
                                        room.copy(
                                            isFamilyRoom = enable,
                                            familyMemberIds = familyMembers,
                                            privacy = if (enable) "family_only" else "public"
                                        )
                                    )
                                    Toast.makeText(
                                        context,
                                        if (enable) "👨‍👩‍👧‍👦 Family Room Activated (Invite-Only)" else "🌐 Room is now open to everyone",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFDE047),
                                checkedTrackColor = Color(0xFFB45309)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search member to invite to Family...", color = Color(0xFF64748B), fontSize = 11.5.sp) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFBBF24),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1A2030),
                        unfocusedContainerColor = Color(0xFF1A2030)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Invited Family Members (${familyMembers.size})",
                    color = Color(0xFFFDE68A),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF111622),
                    border = BorderStroke(1.dp, Color(0xFF263044)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    if (candidateUsers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No users found", color = Color(0xFF64748B), fontSize = 11.5.sp)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(candidateUsers, key = { it.key }) { (uid, pair) ->
                                val (uName, uPhoto) = pair
                                val isMember = uid in familyMembers
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isMember) Color(0xFF232B3E) else Color(0xFF1A2030))
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AsyncImage(
                                            model = uPhoto.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                            contentDescription = uName,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, if (isMember) Color(0xFFFBBF24) else Color(0xFF475569), CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Column {
                                            Text(
                                                text = uName,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (isMember) "✓ Family Member" else "Tap to invite",
                                                color = if (isMember) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isMember) Color(0x33EF4444) else Color(0x33F59E0B),
                                        border = BorderStroke(
                                            0.9.dp,
                                            if (isMember) Color(0xFFF87171) else Color(0xFFFBBF24)
                                        ),
                                        modifier = Modifier.clickable {
                                            if (!canUnlockFamily) {
                                                Toast.makeText(
                                                    context,
                                                    "🔒 Reach Room Level 10 to invite Family Members!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@clickable
                                            }
                                            val updatedList = if (isMember) {
                                                familyMembers - uid
                                            } else {
                                                (familyMembers + uid).distinct()
                                            }
                                            familyMembers = updatedList
                                            WatchPartyCinemaEngine.updateFamilyRoomState(
                                                db = db,
                                                room = room,
                                                isFamilyActive = isFamilyEnabled,
                                                familyMemberIds = updatedList
                                            )
                                            onUpdateRoom(room.copy(familyMemberIds = updatedList))
                                            Toast.makeText(
                                                context,
                                                if (isMember) "Removed $uName from Family Room" else "👨‍👩‍👧‍👦 Invited $uName to Family Room!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    ) {
                                        Text(
                                            text = if (isMember) "Remove" else "+ Invite",
                                            color = if (isMember) Color(0xFFFCA5A5) else Color(0xFFFDE047),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
