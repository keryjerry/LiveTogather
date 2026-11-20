package com.devil.livetogather

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val MAX_MOMENT_REEL_DURATION_MS = 90_000L // Strict 1.5 minutes (90 seconds) maximum for Reels!

fun isInstagramMomentVideo(url: String): Boolean {
    val lower = url.lowercase()
    val path = Uri.parse(url).lastPathSegment.orEmpty().substringBefore('?').lowercase()
    return listOf(".mp4", ".mov", ".webm", ".mkv", ".3gp").any { path.endsWith(it) || lower.contains(it) } ||
        lower.contains("moments_videos") || lower.contains("video%2f")
}

/**
 * Validates that a selected local video URI does not exceed 1.5 minutes (90 seconds).
 * Returns Pair(isValid, durationSeconds).
 */
fun validateReelVideoDuration(context: android.content.Context, uri: Uri): Pair<Boolean, Int> {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        val durationSec = ((durationMs + 500L) / 1000L).toInt()
        (durationMs <= MAX_MOMENT_REEL_DURATION_MS) to durationSec
    } catch (_: Throwable) {
        true to 0
    } finally {
        try {
            retriever.release()
        } catch (_: Throwable) {}
    }
}

/**
 * 100% Edge-to-Edge Transparent-Header Instagram-Style Moments & Reels Experience.
 * - ZERO white or black header bars: floating transparent top controls directly over the feed / reels!
 * - ZERO boxes, cards, or side white margins: photos & videos stretch 100% full-bleed left-to-right!
 * - Full Instagram Feed logic: Double-tap to Like with heart burst, swipeable multi-photo carousel,
 *   Like / Comment / Share / Bookmark bar, inline username + caption, and Reels vertical pager!
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstagramMomentsScreen(
    moments: List<MomentItem>,
    currentUserId: String,
    onOpenProfile: (String) -> Unit = {},
    onPostClick: () -> Unit,
    onLikeMoment: (MomentItem) -> Unit,
    onCommentMoment: (MomentItem) -> Unit,
    onEditMoment: (MomentItem) -> Unit = {},
    onDeleteMoment: (MomentItem) -> Unit = {}
) {
    val context = LocalContext.current
    var activeMode by remember { mutableStateOf("feed") } // "feed" or "reels"
    var fullScreenReelIndex by remember { mutableStateOf<Int?>(null) }
    var fullScreenPhotoGallery by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }
    val savedMomentIds = remember { mutableStateListOf<String>() }

    // Extract all Reel (video) moments for vertical Reels feed
    val reelMoments = remember(moments.toList()) {
        moments.filter { item ->
            val media = if (item.mediaUrls.isNotEmpty()) item.mediaUrls
            else if (item.mediaUrl.isNotBlank()) listOf(item.mediaUrl)
            else emptyList()
            media.any { isInstagramMomentVideo(it) }
        }
    }

    val isReelsMode = activeMode == "reels"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isReelsMode) Color.Black else Color.White)
    ) {
        // =====================================================================
        // 1. FULL-SCREEN EDGE-TO-EDGE CONTENT (Feed Mode OR Vertical Reels Mode)
        // =====================================================================
        if (isReelsMode) {
            if (reelMoments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0B12))
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Reels yet",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Share your first Reel video (up to 1.5 minutes)!",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onPostClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2E63)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Reel (Max 1.5 min)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                InstagramVerticalReelsPager(
                    reelMoments = reelMoments,
                    initialIndex = 0,
                    currentUserId = currentUserId,
                    savedMomentIds = savedMomentIds,
                    onOpenProfile = onOpenProfile,
                    onLikeMoment = onLikeMoment,
                    onCommentMoment = onCommentMoment,
                    onShareMoment = { shareMomentExternally(context, it) }
                )
            }
        } else {
            // FULL-BLEED EDGE-TO-EDGE INSTAGRAM FEED (ZERO side margins, ZERO card boxes!)
            if (moments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No moments shared yet", color = Color(0xFF151726), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Tap + at the top right to share photos or a 1.5 min Reel!", color = Color(0xFF8B90A7), fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            } else {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentPadding = PaddingValues(bottom = 104.dp)
                ) {
                    // Transparent status-bar spacer so first post clears floating transparent overlay cleanly,
                    // while scrolled posts glide seamlessly UNDER the transparent status bar!
                    item(key = "transparent_top_spacer") {
                        Spacer(modifier = Modifier.statusBarsPadding().height(48.dp))
                    }

                    items(moments, key = { it.id.ifBlank { "${it.userId}_${it.timestamp}" } }) { item ->
                        InstagramFullBleedPostItem(
                            item = item,
                            currentUserId = currentUserId,
                            isSaved = item.id in savedMomentIds,
                            onToggleSave = {
                                if (item.id in savedMomentIds) savedMomentIds.remove(item.id)
                                else savedMomentIds.add(item.id)
                            },
                            onOpenProfile = onOpenProfile,
                            onLikeMoment = onLikeMoment,
                            onCommentMoment = onCommentMoment,
                            onShareMoment = { shareMomentExternally(context, item) },
                            onEditMoment = onEditMoment,
                            onDeleteMoment = onDeleteMoment,
                            onOpenPhotoGallery = { urls, page ->
                                fullScreenPhotoGallery = urls to page
                            },
                            onOpenReelFullScreen = {
                                val idx = reelMoments.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
                                fullScreenReelIndex = idx
                            }
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 2. 100% TRANSPARENT FLOATING TOP OVERLAY (ZERO white or black header bar!)
        // =====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.Transparent)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Feed / Reels Clean Switcher (Transparent glass pill)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { activeMode = "feed" }
                ) {
                    Text(
                        text = "Moments",
                        color = if (isReelsMode) Color.White.copy(alpha = 0.72f) else Color(0xFF111322),
                        fontSize = if (!isReelsMode) 19.sp else 16.sp,
                        fontWeight = if (!isReelsMode) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (!isReelsMode) Color(0xFFFF2E63) else Color.Transparent)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { activeMode = "reels" }
                ) {
                    Text(
                        text = "Reels",
                        color = if (isReelsMode) Color.White else Color(0xFF7B8098),
                        fontSize = if (isReelsMode) 19.sp else 16.sp,
                        fontWeight = if (isReelsMode) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isReelsMode) Color.White else Color.Transparent)
                    )
                }
            }

            // Right: Clean transparent-backdrop Create (+) trigger
            Surface(
                color = if (isReelsMode) Color.Black.copy(alpha = 0.35f) else Color(0xFF111322).copy(alpha = 0.08f),
                shape = CircleShape,
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onPostClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Moment or Reel",
                        tint = if (isReelsMode) Color.White else Color(0xFF111322),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    // Full-Screen Vertical Reels Viewer launched from Feed video tap
    if (fullScreenReelIndex != null && reelMoments.isNotEmpty()) {
        Dialog(
            onDismissRequest = { fullScreenReelIndex = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                InstagramVerticalReelsPager(
                    reelMoments = reelMoments,
                    initialIndex = fullScreenReelIndex!!.coerceIn(0, reelMoments.lastIndex),
                    currentUserId = currentUserId,
                    savedMomentIds = savedMomentIds,
                    onOpenProfile = { uid ->
                        fullScreenReelIndex = null
                        onOpenProfile(uid)
                    },
                    onLikeMoment = onLikeMoment,
                    onCommentMoment = onCommentMoment,
                    onShareMoment = { shareMomentExternally(context, it) }
                )
                IconButton(
                    onClick = { fullScreenReelIndex = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Reel", tint = Color.White)
                }
            }
        }
    }

    // Full-Screen Edge-to-Edge Photo Viewer
    fullScreenPhotoGallery?.let { (mediaUrls, initialPage) ->
        Dialog(
            onDismissRequest = { fullScreenPhotoGallery = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { mediaUrls.size })
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { p ->
                    AsyncImage(
                        model = mediaUrls[p],
                        contentDescription = "Full photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(
                    onClick = { fullScreenPhotoGallery = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(14.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstagramFullBleedPostItem(
    item: MomentItem,
    currentUserId: String,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onLikeMoment: (MomentItem) -> Unit,
    onCommentMoment: (MomentItem) -> Unit,
    onShareMoment: () -> Unit,
    onEditMoment: (MomentItem) -> Unit,
    onDeleteMoment: (MomentItem) -> Unit,
    onOpenPhotoGallery: (List<String>, Int) -> Unit,
    onOpenReelFullScreen: () -> Unit
) {
    val isLikedByMe = item.likedByUsers.contains(currentUserId)
    val displayMedia = remember(item.mediaUrls, item.mediaUrl) {
        if (item.mediaUrls.isNotEmpty()) item.mediaUrls
        else if (item.mediaUrl.isNotBlank()) listOf(item.mediaUrl)
        else emptyList()
    }

    // Double-Tap Heart Animation State (True Instagram UX!)
    var showDoubleTapHeart by remember { mutableStateOf(false) }
    val heartScale by animateFloatAsState(
        targetValue = if (showDoubleTapHeart) 1.15f else 0.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "igHeartScale"
    )
    val scope = rememberCoroutineScope()
    val triggerDoubleTapLike = {
        if (!isLikedByMe) onLikeMoment(item)
        showDoubleTapHeart = true
        scope.launch {
            delay(650L)
            showDoubleTapHeart = false
        }
    }

    // Pure Full-Bleed Column — ZERO Card Box, ZERO Horizontal Side Strips!
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // 1. Instagram Author Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    if (item.userId.isNotBlank()) onOpenProfile(item.userId)
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(
                            1.8.dp,
                            Brush.sweepGradient(listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF), Color(0xFFF58529))),
                            CircleShape
                        )
                        .padding(2.5.dp)
                ) {
                    AsyncImage(
                        model = item.userPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120" },
                        contentDescription = item.userName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.userName.ifBlank { "LiveTogather User" },
                        color = Color(0xFF111322),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatRelativeTime(item.timestamp),
                        color = Color(0xFF8B90A7),
                        fontSize = 11.sp
                    )
                }
            }

            if (item.userId == currentUserId) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onEditMoment(item) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF474C66), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onDeleteMoment(item) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // 2. FULL-BLEED LEFT-TO-RIGHT EDGE-TO-EDGE MEDIA (0.dp Horizontal Padding — Just Like Instagram!)
        if (displayMedia.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f) // Tall Instagram 4:5 portrait impact, touching exact left & right edges!
                    .background(Color(0xFFF5F5F8))
                    .pointerInput(item.id, isLikedByMe) {
                        detectTapGestures(
                            onDoubleTap = { triggerDoubleTapLike() },
                            onTap = {
                                if (displayMedia.size == 1 && isInstagramMomentVideo(displayMedia.first())) {
                                    onOpenReelFullScreen()
                                } else if (displayMedia.size == 1) {
                                    onOpenPhotoGallery(displayMedia, 0)
                                }
                            }
                        )
                    }
            ) {
                if (displayMedia.size == 1) {
                    val mediaUrl = displayMedia.first()
                    if (isInstagramMomentVideo(mediaUrl)) {
                        InstagramInlineReelCard(
                            videoUrl = mediaUrl,
                            onOpenFullScreen = onOpenReelFullScreen
                        )
                    } else {
                        AsyncImage(
                            model = mediaUrl,
                            contentDescription = item.caption,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Swipeable Multi-Photo/Video Instagram Carousel
                    val pagerState = rememberPagerState(pageCount = { displayMedia.size })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val mediaUrl = displayMedia[page]
                        if (isInstagramMomentVideo(mediaUrl)) {
                            InstagramInlineReelCard(
                                videoUrl = mediaUrl,
                                onOpenFullScreen = onOpenReelFullScreen
                            )
                        } else {
                            AsyncImage(
                                model = mediaUrl,
                                contentDescription = item.caption,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onOpenPhotoGallery(displayMedia, page) }
                            )
                        }
                    }

                    // Top-Right Carousel Counter Pill ("1/4")
                    Surface(
                        color = Color.Black.copy(alpha = 0.62f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${displayMedia.size}",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }

                    // Bottom-Center Carousel Dots
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        repeat(displayMedia.size) { i ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == i) 7.dp else 5.5.dp)
                                    .clip(CircleShape)
                                    .background(if (pagerState.currentPage == i) Color.White else Color.White.copy(alpha = 0.45f))
                            )
                        }
                    }
                }

                // Double-Tap Heart Burst Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = showDoubleTapHeart,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.94f),
                        modifier = Modifier
                            .size(96.dp)
                            .scale(heartScale)
                    )
                }
            }
        }

        // 3. Instagram Action Row (Like, Comment, Share on Left; Bookmark on Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = if (isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLikedByMe) Color(0xFFFF2E63) else Color(0xFF111322),
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onLikeMoment(item) }
                )
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = Color(0xFF111322),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onCommentMoment(item) }
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Share",
                    tint = Color(0xFF111322),
                    modifier = Modifier
                        .size(23.dp)
                        .clickable { onShareMoment() }
                )
            }

            Icon(
                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Save",
                tint = Color(0xFF111322),
                modifier = Modifier
                    .size(25.dp)
                    .clickable(onClick = onToggleSave)
            )
        }

        // 4. Likes Count + Inline Bold Username & Caption + Comments Link
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 14.dp)
        ) {
            if (item.likes > 0) {
                Text(
                    text = "${item.likes} ${if (item.likes == 1) "like" else "likes"}",
                    color = Color(0xFF111322),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
            }

            if (item.caption.isNotBlank()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF111322))) {
                            append(item.userName.ifBlank { "User" })
                            append("  ")
                        }
                        withStyle(SpanStyle(color = Color(0xFF222536))) {
                            append(item.caption)
                        }
                    },
                    fontSize = 13.5.sp,
                    lineHeight = 18.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = if (item.commentCount > 0) "View all ${item.commentCount} comments" else "Add a comment...",
                color = Color(0xFF8B90A7),
                fontSize = 12.5.sp,
                modifier = Modifier.clickable { onCommentMoment(item) }
            )
        }

        // Razor-thin edge-to-edge Instagram hairline separator
        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.6.dp)
    }
}

@Composable
private fun InstagramInlineReelCard(
    videoUrl: String,
    onOpenFullScreen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E14))
            .clickable(onClick = onOpenFullScreen),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.parse(videoUrl))
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        mp.setVolume(0f, 0f) // Muted inline preview in Feed; tap for full-screen sound Reel!
                        start()
                    }
                }
            },
            onRelease = { it.stopPlayback() }
        )

        // Subtle Reel badge top-left
        Surface(
            color = Color.Black.copy(alpha = 0.55f),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("REEL", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
            }
        }

        // Tap-to-watch with sound pill bottom-right
        Surface(
            color = Color.Black.copy(alpha = 0.58f),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.VolumeOff, contentDescription = "Tap for sound", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstagramVerticalReelsPager(
    reelMoments: List<MomentItem>,
    initialIndex: Int,
    currentUserId: String,
    savedMomentIds: MutableList<String>,
    onOpenProfile: (String) -> Unit,
    onLikeMoment: (MomentItem) -> Unit,
    onCommentMoment: (MomentItem) -> Unit,
    onShareMoment: (MomentItem) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { reelMoments.size })
    var isMuted by remember { mutableStateOf(false) }

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        key = { reelMoments[it].id.ifBlank { it.toString() } }
    ) { page ->
        val item = reelMoments[page]
        val isActivePage = pagerState.currentPage == page
        val videoUrl = remember(item) {
            val all = if (item.mediaUrls.isNotEmpty()) item.mediaUrls else listOf(item.mediaUrl)
            all.firstOrNull { isInstagramMomentVideo(it) } ?: all.firstOrNull().orEmpty()
        }
        val isLiked = item.likedByUsers.contains(currentUserId)
        var showHeart by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(item.id, isLiked) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (!isLiked) onLikeMoment(item)
                            showHeart = true
                            scope.launch {
                                delay(650L)
                                showHeart = false
                            }
                        },
                        onTap = { isMuted = !isMuted }
                    )
                }
        ) {
            if (isActivePage && videoUrl.isNotBlank()) {
                var mediaPlayerRef by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
                LaunchedEffect(isMuted) {
                    val vol = if (isMuted) 0f else 1f
                    try {
                        mediaPlayerRef?.setVolume(vol, vol)
                    } catch (_: Throwable) {}
                }
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(Uri.parse(videoUrl))
                            setOnPreparedListener { mp ->
                                mediaPlayerRef = mp
                                mp.isLooping = true
                                val vol = if (isMuted) 0f else 1f
                                mp.setVolume(vol, vol)
                                start()
                            }
                        }
                    },
                    onRelease = {
                        mediaPlayerRef = null
                        it.stopPlayback()
                    }
                )
            }

            // Bottom subtle dark vignette for legible text over video
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))))
            )

            // Right-Side Floating Instagram Reels Actions
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 104.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onLikeMoment(item) }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFFF2E63) else Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Text("${item.likes}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onCommentMoment(item) }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment", tint = Color.White, modifier = Modifier.size(28.dp))
                    Text("${item.commentCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp).clickable { onShareMoment(item) }
                )

                val isSaved = item.id in savedMomentIds
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp).clickable {
                        if (isSaved) savedMomentIds.remove(item.id) else savedMomentIds.add(item.id)
                    }
                )

                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp).clickable { isMuted = !isMuted }
                )
            }

            // Bottom-Left Author Info & Caption
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.78f)
                    .padding(start = 14.dp, bottom = 102.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { if (item.userId.isNotBlank()) onOpenProfile(item.userId) }
                ) {
                    AsyncImage(
                        model = item.userPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120" },
                        contentDescription = item.userName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(36.dp).clip(CircleShape).border(1.5.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.userName.ifBlank { "Creator" },
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (item.caption.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.caption,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showHeart,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(104.dp))
            }
        }
    }
}

private fun shareMomentExternally(context: android.content.Context, item: MomentItem) {
    try {
        val shareText = buildString {
            append("${item.userName} shared a Moment on LiveTogather")
            if (item.caption.isNotBlank()) append(": \"${item.caption}\"")
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Moment"))
    } catch (_: Throwable) {
        Toast.makeText(context, "Could not open share sheet.", Toast.LENGTH_SHORT).show()
    }
}
