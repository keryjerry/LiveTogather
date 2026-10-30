@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.devil.livetogather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.util.Log
import android.net.Uri
import android.widget.Toast
import android.view.TextureView
import android.view.Surface
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import android.view.WindowManager
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

// -------------------------------------------------------------
// 0. SCREENSHOT-MATCHING LIVE COMMENT BADGES (Exact Replica of media_1792539716502)
// -------------------------------------------------------------

@Composable
fun SilverSwordsRankBadge(
    rank: Int = 3,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 28.dp, height = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        // Crossed swords behind extending prominently
        Text(
            text = "⚔️",
            fontSize = 20.sp,
            modifier = Modifier.offset(y = (-1).dp)
        )
        // Silver shield foreground with rank number
        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(3.dp),
            border = BorderStroke(0.8.dp, Color.White),
            modifier = Modifier.size(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFFFFF), Color(0xFFC8C8C8), Color(0xFF707070))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    color = Color(0xFF151515),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun BlueLevelBadgePill(
    level: Int,
    modifier: Modifier = Modifier
) {
    val tierInfo = LevelBadgeSystem.getLevelTierInfo(level)
    Surface(
        color = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Brush.horizontalGradient(tierInfo.gradientColors)),
        modifier = modifier.height(20.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(tierInfo.gradientColors.map { it.copy(alpha = 0.45f) })
                )
                .padding(horizontal = 6.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (level >= 10) {
                Image(
                    painter = painterResource(id = tierInfo.badgeDrawableRes),
                    contentDescription = tierInfo.tierName,
                    modifier = Modifier.size(17.dp)
                )
            } else {
                Text(
                    text = tierInfo.badgeIcon,
                    fontSize = 10.sp
                )
            }
            Text(
                text = "Lv.$level",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun SapphireCharmBadgePill(
    level: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF1E5BBF), Color(0xFF4B8DF2))
                )
            )
            .border(0.6.dp, Color(0xFF80D8FF).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFF1565C0), Color(0xFF0D47A1))
                    )
                )
                .border(0.8.dp, Color(0xFF80D8FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_charm_heart_3d),
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = "$level",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, end = 7.dp)
        )
    }
}

@Composable
fun GoldenMedalBadgePill(
    level: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFB3732E), Color(0xFFD69C4E))
                )
            )
            .border(0.6.dp, Color(0xFFFFE082).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFEE58), Color(0xFFF57F17), Color(0xFF5D3915))
                    )
                )
                .border(0.8.dp, Color(0xFFFFE082), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_wealth_shield_3d),
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = "$level",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, end = 7.dp)
        )
    }
}

@Composable
fun LevelBadgePill(
    level: Int,
    modifier: Modifier = Modifier,
    showTierName: Boolean = false
) {
    val tierInfo = LevelBadgeSystem.getLevelTierInfo(level)
    Surface(
        color = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.8.dp, Brush.linearGradient(tierInfo.gradientColors)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .background(Brush.horizontalGradient(tierInfo.gradientColors.map { it.copy(alpha = 0.35f) }))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (level >= 10) {
                Image(
                    painter = painterResource(id = tierInfo.badgeDrawableRes),
                    contentDescription = tierInfo.tierName,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = tierInfo.badgeIcon,
                    fontSize = 9.sp
                )
            }
            Text(
                text = "Lv.$level",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (showTierName) {
                Text(
                    text = "• ${tierInfo.tierName}",
                    color = BrandGold,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GrandBroadEntranceBanner(
    userName: String,
    level: Int,
    userProfile: UserProfile? = null,
    actionText: String = "",
    modifier: Modifier = Modifier
) {
    val isApexFounder = userProfile?.isFounder() == true
    val effVip = if (isApexFounder) 10 else (userProfile?.getVipLevel() ?: if (level >= 70) 5 else 1)

    OrnateLuxuryEntranceCard(
        userName = userName,
        avatarUrl = userProfile?.photo ?: "",
        userProfile = userProfile,
        level = level,
        charm = userProfile?.getEffectiveCharmLevel() ?: 50,
        pkLevel = userProfile?.getEffectivePkLevel() ?: 1,
        medal = userProfile?.followerCount ?: 50,
        isFounder = isApexFounder,
        vipLevel = effVip,
        actionText = actionText,
        modifier = modifier
    )
}

// -------------------------------------------------------------
// 0B. LIVE ROOM PK BATTLE ARENA (1v1 COMPETITION, TUG-OF-WAR, MVP PODIUM)
// -------------------------------------------------------------
@Composable
fun LiveRoomPkBattleArena(
    room: RoomItem,
    currentUser: UserProfile,
    isHost: Boolean,
    blueSupporters: List<PkSupporter>,
    redSupporters: List<PkSupporter>,
    onBoostBlue: () -> Unit,
    onBoostRed: () -> Unit,
    onEndPk: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(room.pkStartTime) {
        while (true) {
            delay(1000L)
            currentTimeMs = System.currentTimeMillis()
        }
    }

    val totalDurationMs = (room.pkDurationSec.coerceAtLeast(60)) * 1000L
    val elapsedMs = (currentTimeMs - room.pkStartTime).coerceAtLeast(0L)
    val remainingSec = ((totalDurationMs - elapsedMs) / 1000L).coerceAtLeast(0L)
    val isPkEnded = remainingSec <= 0L || room.pkStatus == "ended"

    val timerFormatted = String.format("%02d:%02d", remainingSec / 60, remainingSec % 60)

    val scoreBlue = room.pkHost1Score.coerceAtLeast(0L)
    val scoreRed = room.pkHost2Score.coerceAtLeast(0L)
    val totalScore = scoreBlue + scoreRed
    val targetRatio = if (totalScore == 0L) 0.5f else (scoreBlue.toFloat() / totalScore.toFloat()).coerceIn(0.12f, 0.88f)
    val animatedBlueRatio by animateFloatAsState(targetValue = targetRatio, animationSpec = tween(500), label = "pk_bar")

    val pulseTransition = rememberInfiniteTransition(label = "pk_flame_pulse")
    val flameScale by pulseTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "flame_scale"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D1E).copy(alpha = 0.96f)),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF0091EA), Color(0xFFFF1744)))),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP HEADER: Fiery Timer & Team Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BLUE TEAM", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFFFF9100), Color(0xFFFF1744))))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 11.sp, modifier = Modifier.scale(flameScale))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPkEnded) "PK OVER" else "PK $timerFormatted",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("RED TEAM", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF1744))
                    )
                    if (isHost) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { onEndPk() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("End", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TOP 3 MVP SUPPORTERS PODIUM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Blue Top 3
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val blueTop = (0..2).map { idx -> blueSupporters.getOrNull(idx) }
                    blueTop.forEachIndexed { i, supp ->
                        val medal = when (i) {
                            0 -> "🥇"
                            1 -> "🥈"
                            else -> "🥉"
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(medal, fontSize = 9.sp)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0D47A1))
                                    .border(1.dp, Color(0xFF00E5FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (supp != null && supp.photo.isNotBlank()) {
                                    AsyncImage(
                                        model = supp.photo,
                                        contentDescription = supp.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = supp?.name?.take(1) ?: "${i + 1}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Points summary
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("%,d".format(scoreBlue), color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    Text(" : ", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("%,d".format(scoreRed), color = Color(0xFFFF5252), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }

                // Red Top 3
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val redTop = (0..2).map { idx -> redSupporters.getOrNull(idx) }
                    redTop.forEachIndexed { i, supp ->
                        val medal = when (i) {
                            0 -> "🥇"
                            1 -> "🥈"
                            else -> "🥉"
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(medal, fontSize = 9.sp)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFB71C1C))
                                    .border(1.dp, Color(0xFFFF1744), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (supp != null && supp.photo.isNotBlank()) {
                                    AsyncImage(
                                        model = supp.photo,
                                        contentDescription = supp.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = supp?.name?.take(1) ?: "${i + 1}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // DYNAMIC TUG-OF-WAR PROGRESS BAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF211428))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(animatedBlueRatio)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0277BD), Color(0xFF00E5FF))))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f - animatedBlueRatio)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFF1744), Color(0xFFFF9100))))
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_pk_flame_vs),
                    contentDescription = "VS Flame",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // HOST 1 (BLUE) vs HOST 2 (RED)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Host 1
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF00E5FF), CircleShape)
                    ) {
                        AsyncImage(
                            model = room.pkHost1Photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                            contentDescription = room.pkHost1Name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = room.pkHost1Name.ifBlank { "Host 1" },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isPkEnded) {
                        if (scoreBlue > scoreRed) {
                            Text("🏆 VICTORY!", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        } else if (scoreRed > scoreBlue) {
                            Text("💀 DEFEAT", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        } else {
                            Text("🤝 DRAW", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Button(
                            onClick = onBoostBlue,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0091EA)),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp).padding(top = 2.dp)
                        ) {
                            Text("⚡ Boost Blue", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Center VS Emblem
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pk_flame_vs),
                        contentDescription = "VS Flame",
                        modifier = Modifier
                            .size(42.dp)
                            .scale(flameScale)
                    )
                }

                // Host 2
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFFF1744), CircleShape)
                    ) {
                        AsyncImage(
                            model = room.pkHost2Photo.ifEmpty { "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d" },
                            contentDescription = room.pkHost2Name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = room.pkHost2Name.ifBlank { "Host 2" },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isPkEnded) {
                        if (scoreRed > scoreBlue) {
                            Text("🏆 VICTORY!", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        } else if (scoreBlue > scoreRed) {
                            Text("💀 DEFEAT", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        } else {
                            Text("🤝 DRAW", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Button(
                            onClick = onBoostRed,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp).padding(top = 2.dp)
                        ) {
                            Text("🔥 Boost Red", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. CREATE CUSTOM ROOM DIALOG
// -------------------------------------------------------------
@Composable
fun CreateRoomDialog(
    currentUser: UserProfile,
    initialCategory: String = "free_tonight",
    existingRoomTitles: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onCreate: (title: String, categoryId: String, themeUrl: String, roomType: String) -> Unit
) {
    var roomTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(if (initialCategory.isNotBlank()) initialCategory else CATEGORY_LIST.getOrNull(1)?.id ?: "free_tonight") }
    var selectedRoomType by remember { mutableStateOf(if (initialCategory == "video_stream") "video" else "audio") }
    var selectedTheme by remember { mutableStateOf(ROOM_THEME_PRESETS[0].imageUrl) }
    var showVideoSafetyGuidelinesModal by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler {
        if (isKeyboardOpen) {
            keyboardController?.hide()
            focusManager.clearFocus()
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent = view.parent
            while (parent != null && parent !is android.app.Dialog && parent !is DialogWindowProvider) {
                parent = parent.parent
            }
            (parent as? DialogWindowProvider)?.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🎙️ Create Hangout Room",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Host your own live audio intent lounge",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Room Title Field
                    Text("Room Title", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = roomTitle,
                        onValueChange = { roomTitle = it },
                        placeholder = { Text("e.g. Saturday Rooftop Vibe Lounge", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandCoral,
                            unfocusedBorderColor = BrandBorder,
                            focusedContainerColor = BrandDark,
                            unfocusedContainerColor = BrandDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        trailingIcon = {
                            if (roomTitle.isNotEmpty()) {
                                IconButton(onClick = { roomTitle = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    )

                    val isDuplicateTitle = roomTitle.trim().isNotEmpty() && existingRoomTitles.any { it.trim().equals(roomTitle.trim(), ignoreCase = true) }
                    if (isDuplicateTitle) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ This name is already in use, try different.",
                            color = Color(0xFFFF5252),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                Spacer(modifier = Modifier.height(14.dp))

                // Room Type Selector (Audio / Video / Video PK)
                Text("Select Room Type", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                val canHostPk = true
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val roomTypes = listOf(
                        Triple("audio", "🎙️ Audio Lounge", true),
                        Triple("video", "📹 Video Stream", true)
                    )
                    roomTypes.forEach { (typeKey, typeLabel, _) ->
                        val isSelected = selectedRoomType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        if (typeKey == "video") Brush.horizontalGradient(listOf(Color(0xFF00C853), Color(0xFF00E5FF)))
                                        else Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                    } else Brush.linearGradient(listOf(BrandDark, BrandDark))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White.copy(alpha = 0.8f) else BrandBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedRoomType = typeKey
                                    if (typeKey == "video" && selectedCategory != "video_stream") {
                                        selectedCategory = "video_stream"
                                    } else if (typeKey == "audio" && selectedCategory == "video_stream") {
                                        selectedCategory = "free_tonight"
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = typeLabel,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Intent Category Selector
                Text("Select Intent Category", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CATEGORY_LIST.filter { it.id != "all" }) { cat ->
                        val isSelected = selectedCategory == cat.id
                        val isVipCat = cat.id == "vip_room"
                        val isUserVip = currentUser.isVipOrPlatinum() || currentUser.getVipLevel() >= 1

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) {
                                        if (isVipCat) Brush.horizontalGradient(listOf(BrandGold, Color(0xFFFFA000)))
                                        else Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                    } else Brush.linearGradient(listOf(BrandDark, BrandDark))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent
                                    else if (isVipCat) BrandGold.copy(alpha = 0.6f)
                                    else BrandBorder,
                                    RoundedCornerShape(50)
                                )
                                .clickable {
                                    if (isVipCat && !isUserVip) {
                                        Toast.makeText(context, "👑 VIP Room can only be created by VIP Level 1+ members!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        selectedCategory = cat.id
                                        if (cat.id == "video_stream") {
                                            selectedRoomType = "video"
                                        } else if (selectedRoomType == "video") {
                                            selectedRoomType = "audio"
                                        }
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cat.label,
                                    color = if (isVipCat && !isSelected) BrandGold else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isVipCat && !isUserVip) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🔒", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Theme Preset Selector
                Text("Choose Luxury Atmosphere Theme", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ROOM_THEME_PRESETS) { theme ->
                        val isSelected = selectedTheme == theme.imageUrl
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedTheme = theme.imageUrl }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp, 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) BrandCoral else BrandBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = if (theme.imageUrl == "vip_royal_stage") R.drawable.vip_royal_stage else theme.imageUrl,
                                    contentDescription = theme.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theme.name,
                                color = if (isSelected) BrandCoral else Color.Gray,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Launch Room Button
                Button(
                    onClick = {
                        val isVipCat = selectedCategory == "vip_room"
                        val isUserVip = currentUser.isVipOrPlatinum() || currentUser.getVipLevel() >= 1
                        if (isVipCat && !isUserVip) {
                            Toast.makeText(context, "👑 VIP Room requires VIP Level 1+ membership to create!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedRoomType == "video" || selectedRoomType == "video_pk") {
                            showVideoSafetyGuidelinesModal = true
                        } else {
                            val finalCategory = selectedCategory
                            val finalTitle = roomTitle.trim().ifEmpty { "${currentUser.name}'s Hangout" }
                            val isDup = existingRoomTitles.any { it.trim().equals(finalTitle, ignoreCase = true) }
                            if (isDup) {
                                Toast.makeText(context, "This name is already in use, try different.", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            onCreate(finalTitle, finalCategory, selectedTheme, selectedRoomType)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch Room As Host 👑", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Live Video Streaming Community Guidelines & 18+ Compliance Modal
        if (showVideoSafetyGuidelinesModal) {
            AlertDialog(
                onDismissRequest = { showVideoSafetyGuidelinesModal = false },
                containerColor = Color(0xFF1B141E),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video Community Guidelines",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFF1744).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF1744).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🔞", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Strictly 18+ Only. Zero tolerance for adult, sexual, or nude content.",
                                    color = Color(0xFFFF5252),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "By starting a live video broadcast, you agree to comply with our community safety rules:",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("• No nudity, pornography, or sexually explicit acts.", color = Color.White, fontSize = 12.sp)
                            Text("• No harassment, hate speech, bullying, or violence.", color = Color.White, fontSize = 12.sp)
                            Text("• No dangerous activities, weapons, or illegal substances.", color = Color.White, fontSize = 12.sp)
                            Text("• 24/7 automated and administrative moderation is active.", color = Color.White, fontSize = 12.sp)
                            Text("• Violations result in IMMEDIATE stream shutdown and PERMANENT account termination.", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showVideoSafetyGuidelinesModal = false
                            val finalCategory = if (selectedRoomType == "video") "video_stream" else selectedCategory
                            val finalTitle = roomTitle.trim().ifEmpty { "${currentUser.name}'s Hangout" }
                            onCreate(finalTitle, finalCategory, selectedTheme, selectedRoomType)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Agree & Start Stream 📹", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVideoSafetyGuidelinesModal = false }) {
                        Text("Decline & Cancel", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            )
        }
    }
}
}

// -------------------------------------------------------------
// 1B. EDIT ROOM DIALOG (HOST EDIT TITLE, CATEGORY, THEME, STATUS)
// -------------------------------------------------------------
@Composable
fun EditRoomDialog(
    room: RoomItem,
    db: FirebaseFirestore,
    currentUser: UserProfile? = null,
    onDismiss: () -> Unit,
    onRoomUpdated: (RoomItem) -> Unit
) {
    val context = LocalContext.current
    var roomTitle by remember { mutableStateOf(room.title) }
    var selectedCategory by remember { mutableStateOf(room.category.ifEmpty { "free_tonight" }) }
    var selectedTheme by remember { mutableStateOf(room.banner_image_url.ifEmpty { ROOM_THEME_PRESETS[0].imageUrl }) }
    var statusMessage by remember { mutableStateOf(room.statusMessage) }
    var isSaving by remember { mutableStateOf(false) }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler {
        if (isKeyboardOpen) {
            keyboardController?.hide()
            focusManager.clearFocus()
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent = view.parent
            while (parent != null && parent !is android.app.Dialog && parent !is DialogWindowProvider) {
                parent = parent.parent
            }
            (parent as? DialogWindowProvider)?.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 640.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "✏️ Edit Hangout Room",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Update title, category hub & luxury atmosphere",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Room Title
                Text("Room Title", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = roomTitle,
                    onValueChange = { roomTitle = it },
                    placeholder = { Text("e.g. Saturday Rooftop Vibe Lounge", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandCoral,
                        unfocusedBorderColor = BrandBorder,
                        focusedContainerColor = BrandDark,
                        unfocusedContainerColor = BrandDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Intent Category Selector
                Text("Select Intent Category", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CATEGORY_LIST.filter { it.id != "all" }) { cat ->
                        val isSelected = selectedCategory == cat.id
                        val isVipCat = cat.id == "vip_room"
                        val isUserVip = currentUser?.let { it.isVipOrPlatinum() || it.getVipLevel() >= 1 } ?: true

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isSelected) {
                                        if (isVipCat) Brush.horizontalGradient(listOf(BrandGold, Color(0xFFFFA000)))
                                        else Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                    } else Brush.linearGradient(listOf(BrandDark, BrandDark))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent
                                    else if (isVipCat) BrandGold.copy(alpha = 0.6f)
                                    else BrandBorder,
                                    RoundedCornerShape(50)
                                )
                                .clickable {
                                    if (isVipCat && !isUserVip) {
                                        Toast.makeText(context, "👑 VIP Room can only be selected by VIP Level 1+ members!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        selectedCategory = cat.id
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cat.label,
                                    color = if (isVipCat && !isSelected) BrandGold else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isVipCat && !isUserVip) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🔒", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Theme Selector
                Text("Choose Atmosphere Theme", color = BrandCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ROOM_THEME_PRESETS) { theme ->
                        val isSelected = selectedTheme == theme.imageUrl
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedTheme = theme.imageUrl }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp, 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) BrandCoral else BrandBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = if (theme.imageUrl == "vip_royal_stage") R.drawable.vip_royal_stage else theme.imageUrl,
                                    contentDescription = theme.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theme.name,
                                color = if (isSelected) BrandCoral else Color.Gray,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Message / Topic
                Text("Status / Topic Notice", color = BrandGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = statusMessage,
                    onValueChange = { statusMessage = it },
                    placeholder = { Text("e.g. Chill Music & Late Night Chats", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandGold,
                        unfocusedBorderColor = BrandBorder,
                        focusedContainerColor = BrandDark,
                        unfocusedContainerColor = BrandDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Cancel / Save Changes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, BrandBorder)
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val finalTitle = roomTitle.trim().ifEmpty { room.title }
                            val updatedPrivacy = if (selectedCategory == "vip_room") "vip_only" else room.privacy
                            isSaving = true
                            val updates = mapOf(
                                "title" to finalTitle,
                                "category" to selectedCategory,
                                "banner_image_url" to selectedTheme,
                                "statusMessage" to statusMessage.trim(),
                                "privacy" to updatedPrivacy
                            )
                            db.collection("rooms").document(room.id).update(updates)
                                .addOnSuccessListener {
                                    val updated = room.copy(
                                        title = finalTitle,
                                        category = selectedCategory,
                                        banner_image_url = selectedTheme,
                                        statusMessage = statusMessage.trim(),
                                        privacy = updatedPrivacy
                                    )
                                    onRoomUpdated(updated)
                                    Toast.makeText(context, "Room updated successfully! 🚀", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    Toast.makeText(context, "Failed to update room: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier.weight(1.5f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(50),
                        enabled = !isSaving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSaving) "Saving..." else "Save Changes 💾", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
}

// -------------------------------------------------------------
// 1C. ROOM DELETE CONFIRMATION POPUP DIALOG
// -------------------------------------------------------------
@Composable
fun RoomDeleteConfirmationDialog(
    roomTitle: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠️", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete Room Permanently?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Text(
                text = "Are you sure you want to permanently delete \"$roomTitle\"? All live mic stages, chat messages, and member sessions will be ended. This action cannot be undone.",
                color = Color.LightGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Yes, Delete Room 🗑️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BrandBorder)
            ) {
                Text("Cancel", color = Color.White, fontSize = 12.sp)
            }
        },
        containerColor = BrandCard,
        shape = RoundedCornerShape(20.dp)
    )
}

// -------------------------------------------------------------
// 2. DENSE RGB ANIMATED EQUALIZER (35 bars, multi-color spectrum, bottom-aligned)
// -------------------------------------------------------------
@Composable
fun MusicVisualizerWave(isPlaying: Boolean, modifier: Modifier = Modifier, barCount: Int = 18) {
    val infiniteTransition = rememberInfiniteTransition(label = "dense_eq_wave")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1950, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val spectrumColors = remember {
        listOf(
            Color(0xFFFF2A6D), // Hot pink
            Color(0xFFFF5E62), // Coral
            Color(0xFFFF9966), // Orange
            Color(0xFFFFD700), // Gold
            Color(0xFF00E676), // Green
            Color(0xFF00E5FF), // Cyan
            Color(0xFF00B0FF), // Deep Sky Blue
            Color(0xFF7C4DFF), // Violet
            Color(0xFFE040FB)  // Magenta
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(1.2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val freqWeight = 0.75f + (i.toFloat() / barCount.toFloat()) * 0.95f
            val s1 = kotlin.math.sin(phase1 * freqWeight + i * 0.42f)
            val s2 = kotlin.math.cos(phase2 * 1.25f + i * 0.68f)
            val normalized = ((s1 + s2 + 2f) / 4f).coerceIn(0.12f, 1.0f)

            val minH = 2.5f
            val maxH = 20f
            val barHeightDp = if (isPlaying) (minH + normalized * (maxH - minH)) else minH
            val barColor = spectrumColors[i % spectrumColors.size]

            Box(
                modifier = Modifier
                    .width(1.8.dp)
                    .height(barHeightDp.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(barColor.copy(alpha = if (isPlaying) pulseGlow else 0.35f))
            )
        }
    }
}

// -------------------------------------------------------------
// 2B. LARGE CATEGORY EQUALIZER THEME VISUALIZER (OUTSIDE 13 GRIDS)
// -------------------------------------------------------------
@Composable
fun CategoryEqualizerThemeVisualizer(
    modifier: Modifier = Modifier,
    isLive: Boolean = true,
    isPlaying: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cat_equalizer")

    val b1 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = if (isLive || isPlaying) 28f else 16f,
        animationSpec = infiniteRepeatable(tween(340, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val b2 by infiniteTransition.animateFloat(
        initialValue = 24f,
        targetValue = if (isLive || isPlaying) 10f else 8f,
        animationSpec = infiniteRepeatable(tween(270, easing = LinearEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val b3 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = if (isLive || isPlaying) 30f else 18f,
        animationSpec = infiniteRepeatable(tween(430, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val b4 by infiniteTransition.animateFloat(
        initialValue = 26f,
        targetValue = if (isLive || isPlaying) 8f else 10f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "b4"
    )
    val b5 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = if (isLive || isPlaying) 29f else 15f,
        animationSpec = infiniteRepeatable(tween(460, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b5"
    )
    val b6 by infiniteTransition.animateFloat(
        initialValue = 22f,
        targetValue = if (isLive || isPlaying) 11f else 8f,
        animationSpec = infiniteRepeatable(tween(290, easing = LinearEasing), RepeatMode.Reverse),
        label = "b6"
    )
    val b7 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = if (isLive || isPlaying) 27f else 14f,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b7"
    )
    val b8 by infiniteTransition.animateFloat(
        initialValue = 19f,
        targetValue = if (isLive || isPlaying) 9f else 12f,
        animationSpec = infiniteRepeatable(tween(330, easing = LinearEasing), RepeatMode.Reverse),
        label = "b8"
    )

    val bars = listOf(b1, b2, b3, b4, b5, b6, b7, b8, b2, b5, b1, b6, b3, b7, b4, b8)
    val neonBrushes = listOf(
        Brush.verticalGradient(listOf(Color(0xFFFF5E62), Color(0xFFFFD700))), // Coral -> Gold
        Brush.verticalGradient(listOf(Color(0xFF00F2FE), Color(0xFF4FACFE))), // Cyan -> Sky Blue
        Brush.verticalGradient(listOf(Color(0xFFFF0844), Color(0xFFFFB199))), // Magenta -> Rose
        Brush.verticalGradient(listOf(Color(0xFF00F5A0), Color(0xFF00D9F5))), // Emerald -> Mint
        Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFFEC4899))), // Purple -> Pink
        Brush.verticalGradient(listOf(Color(0xFFFF9966), Color(0xFFFF5E62)))  // Orange -> Coral
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .border(0.6.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEachIndexed { idx, h ->
            val brush = neonBrushes[idx % neonBrushes.size]
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(h.dp)
                    .clip(RoundedCornerShape(50))
                    .background(brush)
            )
        }
    }
}

// -------------------------------------------------------------
// 2C. CATEGORY THEMED PLAYER CONSOLE (14 TAILORED ROOM VISUALIZERS + 4 SOUND FX PADS)
// -------------------------------------------------------------
@Composable
fun CategoryThemedPlayerConsole(
    room: RoomItem,
    isPlayingNow: Boolean = AudioPlayerManager.isPlayingState.value,
    isBuffering: Boolean = AudioPlayerManager.isBufferingState.value,
    currentArtwork: String = "",
    formattedTime: String = "",
    currentPosMs: Int = 0,
    totalDurationMs: Int = 0,
    onTogglePlay: () -> Unit,
    onSelectTrack: () -> Unit,
    onTriggerSoundFx: (SoundFxItem) -> Unit = {},
    onMinimize: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = getCategoryTheme(room.category)
    val curPlaying by AudioPlayerManager.isPlayingState
    val curBuffering by AudioPlayerManager.isBufferingState
    val curFormattedTime by AudioPlayerManager.formattedTimeState
    val curPos by AudioPlayerManager.currentPositionMsState
    val curDur by AudioPlayerManager.durationMsState

    val effectivePlaying = if (isPlayingNow) isPlayingNow else curPlaying
    val effectiveBuffering = if (isBuffering) isBuffering else curBuffering
    val effectiveTime = formattedTime.ifEmpty { curFormattedTime }
    val effectivePos = if (currentPosMs > 0) currentPosMs else curPos
    val effectiveDur = if (totalDurationMs > 0) totalDurationMs else curDur

    val infiniteTransition = rememberInfiniteTransition(label = "console_anim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BrandCard),
        border = BorderStroke(1.dp, BrandBorder.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rotating Theme Disc (Vinyl / Artwork)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(listOf(BrandCoral, BrandGold, BrandCoral)))
                        .border(1.dp, BrandGold.copy(alpha = 0.7f), CircleShape)
                        .graphicsLayer(rotationZ = if (effectivePlaying) rotationAngle else 0f),
                    contentAlignment = Alignment.Center
                ) {
                    val songArt = if (currentArtwork.isNotBlank()) currentArtwork
                        else if (room.nowPlayingArtworkUrl.isNotBlank()) room.nowPlayingArtworkUrl
                        else ""
                    if (songArt.isNotBlank()) {
                        AsyncImage(
                            model = songArt,
                            contentDescription = "Track Artwork",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(theme.emoji, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Track Info & Status
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (room.nowPlayingTitle.isNotBlank()) room.nowPlayingTitle else "${theme.displayName} Lounge",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (effectiveBuffering) "⏳" else if (effectivePlaying) "● LIVE" else "PAUSED",
                            color = if (effectivePlaying) BrandGreen else Color.Gray,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (room.nowPlayingArtist.isNotBlank()) room.nowPlayingArtist else theme.consoleTitle,
                        color = BrandGold.copy(alpha = 0.85f),
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Equalizer Waveform Animation
                MusicVisualizerWave(
                    isPlaying = effectivePlaying,
                    modifier = Modifier.width(50.dp).height(20.dp).padding(horizontal = 2.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Select Track Button
                IconButton(
                    onClick = onSelectTrack,
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .border(0.8.dp, BrandBorder, CircleShape)
                ) {
                    Icon(Icons.Default.QueueMusic, contentDescription = "Select Song", tint = BrandCyan, modifier = Modifier.size(15.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Play/Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (effectivePlaying) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                            else Brush.horizontalGradient(listOf(BrandDark, Color(0xFF281E22)))
                        )
                        .border(1.dp, if (effectivePlaying) BrandGold else BrandBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (effectivePlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (effectivePlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (onMinimize != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            .border(0.8.dp, BrandGold.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloseFullscreen,
                            contentDescription = "Minimize Player",
                            tint = BrandGold,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Thin Playback Progress Bar + Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val progress = if (effectiveDur > 0) (effectivePos.toFloat() / effectiveDur.toFloat()).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = BrandCoral,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = effectiveTime,
                    color = if (effectivePlaying) BrandGold else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. TRACK SELECTOR DIALOG (REAL ARTIST/SONG SEARCH & CURATED STREAMS)
// -------------------------------------------------------------
@Composable
fun TrackSelectorDialog(
    onDismiss: () -> Unit,
    onSelectTrack: (title: String, artist: String, category: String, url: String, artworkUrl: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val searchResults = remember { mutableStateListOf<HangoutTrack>() }
    var selectedGenre by remember { mutableStateOf("All") }
    val genreList = listOf("All", "Lo-Fi Chill", "Acoustic Romantic", "Cafe Jazz", "Party Beats", "EDM Club")

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Debounced search query handler
    LaunchedEffect(searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.isEmpty()) {
            searchResults.clear()
            isSearching = false
        } else {
            delay(400)
            isSearching = true
            val results = MusicSearchEngine.searchTracks(trimmed)
            searchResults.clear()
            searchResults.addAll(results)
            isSearching = false
        }
    }

    val filteredTracks = remember(selectedGenre) {
        if (selectedGenre == "All") CURATED_HANGOUT_TRACKS
        else CURATED_HANGOUT_TRACKS.filter { it.category == selectedGenre }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Local Audio Track"
            onSelectTrack(fileName, "Device Storage", "Local MP3", uri.toString(), "")
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎵 Global Music Engine", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Text(
                    "Search any artist or song worldwide. Real streams synced in real-time.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar for Any Artist / Song
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search artist or song (e.g. Arijit, Diljit, Drake)...", color = Color.Gray, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = BrandGold, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandGold,
                        unfocusedBorderColor = BrandBorder,
                        focusedContainerColor = BrandDark,
                        unfocusedContainerColor = BrandDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (searchQuery.isNotBlank()) {
                    // Search Results View
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSearching) "Searching streams..." else "Found ${searchResults.size} tracks:",
                            color = BrandGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSearching) {
                            CircularProgressIndicator(color = BrandCoral, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (!isSearching && searchResults.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tracks found for \"$searchQuery\". Try another artist name.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BrandDark)
                                        .border(0.5.dp, BrandBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            onSelectTrack(track.title, track.artist, track.category, track.url, track.artworkUrl)
                                            onDismiss()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        if (track.artworkUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = track.artworkUrl,
                                                contentDescription = track.title,
                                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(6.dp)).background(BrandCard),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(track.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            val durLabel = if (track.durationSec > 0) " • ⏳ ${track.durationSec / 60}:${String.format("%02d", track.durationSec % 60)}" else ""
                                            Text("👤 ${track.artist} • 🏷️ ${track.category}$durLabel", color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    val isTrackPlaying = AudioPlayerManager.isPlayingState.value && AudioPlayerManager.currentTrackUrlState.value == track.url
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isTrackPlaying) BrandGreen else BrandCoral),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isTrackPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isTrackPlaying) "Pause" else "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Default Curated Stations View
                    // Music Genre Filter Pills
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(genreList) { genre ->
                            val isSelected = selectedGenre == genre
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                        else Brush.linearGradient(listOf(BrandDark, BrandDark))
                                    )
                                    .border(1.dp, if (isSelected) Color.Transparent else BrandBorder, RoundedCornerShape(50))
                                    .clickable { selectedGenre = genre }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(genre, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pick Local File Button
                    OutlinedButton(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandCyan),
                        border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Audio from Device (.mp3)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BrandBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Curated Stations (${filteredTracks.size}):", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTracks) { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrandDark)
                                    .clickable {
                                        onSelectTrack(track.title, track.artist, track.category, track.url, track.artworkUrl)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    if (track.artworkUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = track.artworkUrl,
                                            contentDescription = track.title,
                                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).background(BrandCard),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(track.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("👤 ${track.artist} • 🏷️ ${track.category}", color = Color.Gray, fontSize = 10.sp)
                                    }
                                }

                                val isTrackPlaying = AudioPlayerManager.isPlayingState.value && AudioPlayerManager.currentTrackUrlState.value == track.url
                                Box(
                                    modifier = Modifier.size(28.dp).clip(CircleShape).background(if (isTrackPlaying) BrandGreen else BrandCoral),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isTrackPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isTrackPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
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

// -------------------------------------------------------------
// 4. SEND MESSAGE REQUEST DIALOG (FOR STANDARD / FREE USERS)
// -------------------------------------------------------------
@Composable
fun SendMessageRequestDialog(
    targetUser: RoomMember,
    onDismiss: () -> Unit,
    onSend: (message: String) -> Unit
) {
    var messageText by remember { mutableStateOf("Hey! Would love to connect and chat!") }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler {
        if (isKeyboardOpen) {
            keyboardController?.hide()
            focusManager.clearFocus()
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent = view.parent
            while (parent != null && parent !is android.app.Dialog && parent !is DialogWindowProvider) {
                parent = parent.parent
            }
            (parent as? DialogWindowProvider)?.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrandBorder, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp)
                ) {
                    Text(
                        text = "💬 Message Request to ${targetUser.name}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Standard members can send a message request. Once accepted, 1-on-1 direct chat unlocks!",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Write your message...", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandCoral,
                            unfocusedBorderColor = BrandBorder,
                            focusedContainerColor = BrandDark,
                            unfocusedContainerColor = BrandDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    onSend(messageText.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Send Request 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4B. ALL ROOM VIEWERS & MODERATION DIALOG
// -------------------------------------------------------------
@Composable
fun AllRoomViewersDialog(
    viewers: List<RoomMember>,
    currentUser: UserProfile,
    isHost: Boolean,
    onSelectMember: (RoomMember) -> Unit,
    onKickOut: (RoomMember) -> Unit,
    onBan: (RoomMember) -> Unit,
    onInviteToVideo: ((RoomMember) -> Unit)? = null,
    isVideoRoom: Boolean = false,
    onDismiss: () -> Unit
) {
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .border(1.dp, BrandBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "👥 Active Viewers (${viewers.size})",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isVideoRoom || onInviteToVideo != null) "Real-time participants in this video room" else "Real-time listeners in this audio room",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (viewers.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active listeners yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewers, key = { it.uid }) { member ->
                            val isVip = member.tier.contains("VIP", true)
                            val isPlat = member.tier.contains("Platinum", true)
                            val isSelf = member.uid == currentUser.uid

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelf) BrandCardSubtle else BrandDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectMember(member)
                                        onDismiss()
                                    }
                                    .border(
                                        0.5.dp,
                                        if (isSelf) BrandCoral.copy(alpha = 0.5f) else BrandBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    1.5.dp,
                                                    if (isVip) BrandGold else if (isPlat) BrandCyan else BrandCoral,
                                                    CircleShape
                                                )
                                        ) {
                                            AsyncImage(
                                                model = member.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                contentDescription = member.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = if (isSelf) "${member.name} (You)" else member.name,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (member.role == "host") "👑 Host" else if (member.role == "co_host") "📹 Co-Host" else if (isVip) "★ VIP Member" else if (isPlat) "♦ Platinum" else if (isVideoRoom || onInviteToVideo != null) "Viewer" else "Listener",
                                                color = if (member.role == "host" || isVip) BrandGold else if (member.role == "co_host" || isPlat) BrandCyan else Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = BrandCard,
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(0.5.dp, BrandBorder),
                                            modifier = Modifier.clickable {
                                                onSelectMember(member)
                                                onDismiss()
                                            }
                                        ) {
                                            Text("Profile", color = BrandGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }

                                        if (isHost && !isSelf) {
                                            if (onInviteToVideo != null) {
                                                Button(
                                                    onClick = { onInviteToVideo(member) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Text("Invite 📹", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { onKickOut(member) },
                                                border = BorderStroke(1.dp, Color.Yellow),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text("Kick", color = Color.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { onBan(member) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text("Ban", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
    }
}

// -------------------------------------------------------------
// 5. MEMBER PROFILE SHEET / DIALOG
// -------------------------------------------------------------
@Composable
fun RoomMemberProfileSheet(
    member: RoomMember,
    userProfile: UserProfile?,
    currentUser: UserProfile,
    isHost: Boolean,
    roomId: String,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onLeaveRoom: () -> Unit,
    onLike: () -> Unit,
    onDirectMessage: () -> Unit,
    onRequestMessage: () -> Unit,
    onKickOut: (RoomMember) -> Unit,
    onBan: (RoomMember) -> Unit,
    onBlock: (RoomMember) -> Unit,
    onTagMember: (String) -> Unit,
    roomAdminUserIds: List<String> = emptyList(),
    onToggleAdmin: ((RoomMember) -> Unit)? = null,
    onInviteToVideo: ((RoomMember) -> Unit)? = null,
    onOpenGiftCenter: ((RoomMember) -> Unit)? = null,
    showRoomActions: Boolean = true
) {
    val context = LocalContext.current
    val isVipOrPlatinum = currentUser.isVipOrPlatinum()
    val isMemberVip = member.tier.contains("VIP", ignoreCase = true)
    val isMemberPlat = member.tier.contains("Platinum", ignoreCase = true)
    val isSelf = member.uid == currentUser.uid
    val isCurrentAdmin = isHost || (currentUser.uid in roomAdminUserIds)
    var showReportDialog by remember { mutableStateOf(false) }
    var showFullProfileDialog by remember(member.uid) { mutableStateOf(false) }

    var isFollowingMember by remember { mutableStateOf(false) }
    var memberFollowerCount by remember { mutableStateOf(userProfile?.followerCount ?: 0) }
    var memberAdminRooms by remember { mutableStateOf<List<String>>(userProfile?.adminInRoomIds ?: emptyList()) }
    var showAdminRoomsDialog by remember { mutableStateOf(false) }
    var showProfileGiftPicker by remember { mutableStateOf(false) }
    var isSendingProfileGift by remember { mutableStateOf(false) }
    var isSendingMicLinkRequest by remember { mutableStateOf(false) }
    var hasSentMicLinkRequest by remember(member.uid, roomId) { mutableStateOf(false) }
    var isBestFriend by remember { mutableStateOf(currentUser.bestFriends.contains(member.uid)) }
    LaunchedEffect(currentUser.bestFriends, member.uid) {
        isBestFriend = currentUser.bestFriends.contains(member.uid)
    }

    DisposableEffect(member.uid, currentUser.uid) {
        val targetUid = member.uid.ifEmpty { currentUser.uid }
        if (targetUid.isBlank()) {
            return@DisposableEffect onDispose {}
        }
        val countDoc = db.collection("users").document(targetUid)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    memberFollowerCount = snapshot.size()
                }
            }
        val userDoc = db.collection("users").document(targetUid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val rooms = (snapshot.get("adminInRoomIds") as? List<String>) ?: emptyList()
                    memberAdminRooms = rooms
                }
            }
        val followDoc = if (!isSelf && member.uid.isNotBlank()) {
            db.collection("users").document(member.uid)
                .collection("followers").document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    isFollowingMember = snapshot != null && snapshot.exists()
                }
        } else null

        onDispose {
            countDoc.remove()
            userDoc.remove()
            followDoc?.remove()
        }
    }

    fun sendProfileGift(gift: VideoGiftItem) {
        if (currentUser.uid.isBlank() || member.uid.isBlank() || member.uid == currentUser.uid) {
            Toast.makeText(context, "This profile cannot receive a gift.", Toast.LENGTH_SHORT).show()
            return
        }
        if (isSendingProfileGift) {
            return
        }
        isSendingProfileGift = true
        val senderRef = db.collection("users").document(currentUser.uid)
        val recipientRef = db.collection("users").document(member.uid)
        val receivedGiftRef = recipientRef.collection("received_gifts").document()
        db.runTransaction { transaction ->
            val senderSnapshot = transaction.get(senderRef)
            val recipientSnapshot = transaction.get(recipientRef)
            if (!senderSnapshot.exists() || !recipientSnapshot.exists()) {
                throw IllegalStateException("Profile is no longer available.")
            }

            val balance = senderSnapshot.getLong("diamonds") ?: 0L
            if (balance < gift.cost) return@runTransaction false

            val wealthXp = (senderSnapshot.getLong("wealthXp") ?: currentUser.getEffectiveWealthXp()) + gift.cost * 10L
            val charmXp = (recipientSnapshot.getLong("charmXp") ?: 0L) + gift.cost * 10L

            transaction.update(
                senderRef,
                mapOf(
                    "diamonds" to balance - gift.cost,
                    "wealthXp" to wealthXp,
                    "wealthLevel" to LevelBadgeSystem.calculateLevelFromXp(wealthXp),
                    "diamondsSpentCount" to FieldValue.increment(gift.cost)
                )
            )
            transaction.update(
                recipientRef,
                mapOf(
                    "charmXp" to charmXp,
                    "charmLevel" to LevelBadgeSystem.calculateLevelFromXp(charmXp)
                )
            )
            transaction.set(
                receivedGiftRef,
                mapOf(
                    "giftId" to gift.id,
                    "giftName" to gift.name,
                    "giftEmoji" to gift.emoji,
                    "cost" to gift.cost,
                    "senderId" to currentUser.uid,
                    "senderName" to currentUser.name,
                    "receiverId" to member.uid,
                    "timestamp" to System.currentTimeMillis()
                )
            )
            true
        }.addOnSuccessListener { sent ->
            isSendingProfileGift = false
            if (sent) {
                val notificationId = "profile_gift_${UUID.randomUUID()}"
                val notification = NotificationItem(
                    id = notificationId,
                    targetUserId = member.uid,
                    senderId = currentUser.uid,
                    senderName = currentUser.name,
                    senderPhoto = currentUser.photo,
                    type = "gift_received",
                    title = "${gift.emoji} Gift received!",
                    message = "${currentUser.name} sent you ${gift.name}.",
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    roomId = roomId
                )
                db.collection("users").document(member.uid).collection("notifications")
                    .document(notificationId).set(notification)
                db.collection("notifications").document(notificationId).set(notification)
                showProfileGiftPicker = false
                Toast.makeText(context, "${gift.emoji} ${gift.name} sent to ${member.name}!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "You need ${gift.cost} diamonds to send this gift.", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { error ->
            isSendingProfileGift = false
            Log.e("RoomMemberProfile", "Failed to send gift to ${member.uid}", error)
            Toast.makeText(context, "Gift could not be sent: ${error.localizedMessage ?: "Please try again."}", Toast.LENGTH_LONG).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        val isMemberFounder = userProfile?.isFounder() == true || (isSelf && currentUser.isFounder()) || member.name.contains("Devil", ignoreCase = true)
        val memberVipLvlForCard = if (isSelf) currentUser.getVipLevel() else (userProfile?.getVipLevel() ?: if (isMemberVip) 1 else 0)

        VipMiniCardFrame(
            isFounder = isMemberFounder,
            vipLevel = memberVipLvlForCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isSelf) "Your Profile in Room" else "User Profile & Actions",
                        color = BrandCoral,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3D Level Badges & Progress Cards in RoomMemberProfileSheet
                val targetProfileForLevels = userProfile ?: UserProfile(uid = member.uid, name = member.name)
                val targetWealthLvl = targetProfileForLevels.getEffectiveWealthLevel()
                val targetWealthXp = targetProfileForLevels.getEffectiveWealthXp()
                val targetWealthTier = targetProfileForLevels.getWealthTierInfo()
                val (tMinWealthXp, tMaxWealthXp) = LevelBadgeSystem.getXpThresholds(targetWealthLvl)
                val targetWealthProgress = if (tMaxWealthXp > tMinWealthXp) {
                    ((targetWealthXp - tMinWealthXp).toFloat() / (tMaxWealthXp - tMinWealthXp).toFloat()).coerceIn(0f, 1f)
                } else 1f

                val targetCharmLvl = targetProfileForLevels.getEffectiveCharmLevel()
                val targetCharmXp = targetProfileForLevels.getEffectiveCharmXp()
                val targetCharmTier = targetProfileForLevels.getCharmTierInfo()
                val (tMinCharmXp, tMaxCharmXp) = LevelBadgeSystem.getXpThresholds(targetCharmLvl)
                val targetCharmProgress = if (tMaxCharmXp > tMinCharmXp) {
                    ((targetCharmXp - tMinCharmXp).toFloat() / (tMaxCharmXp - tMinCharmXp).toFloat()).coerceIn(0f, 1f)
                } else 1f

                val memberFrameId = if (isSelf) currentUser.activeFrame else (userProfile?.activeFrame ?: "")
                val memberVipLvl = if (isSelf) currentUser.getVipLevel() else (userProfile?.getVipLevel() ?: if (isMemberVip) 1 else 0)

                // User Identity Header: Avatar (Left) + Details (Center) + 3D Prestige Badge (Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        FlowingAvatarFrame(
                            photoUrl = member.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                            frameId = memberFrameId,
                            avatarSize = 58.dp,
                            frameSize = 98.dp,
                            vipLevel = memberVipLvl
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            val ageText = userProfile?.age?.let { ", $it" } ?: ""
                            Text(
                                text = "${if (isSelf) "You (${member.name})" else member.name}$ageText",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(BrandGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Online • Live", color = Color.LightGray, fontSize = 11.sp)
                            }

                            // Role & Tier Badges
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                if (member.role == "host" || isHost && isSelf) {
                                    Text(
                                        text = "👑 Host",
                                        color = BrandGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(BrandGold.copy(alpha = 0.2f), RoundedCornerShape(50))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }

                                if (member.uid in roomAdminUserIds) {
                                    Text(
                                        text = "🛡️ Admin",
                                        color = Color(0xFF4FC3F7),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(Color(0xFF0288D1).copy(alpha = 0.25f), RoundedCornerShape(50))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }

                                Text(
                                    text = if (isMemberVip) "VIP 1" else if (isMemberPlat) "PLAT" else "Member",
                                    color = if (isMemberVip) BrandGold else if (isMemberPlat) BrandCyan else Color.LightGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                                        .border(0.5.dp, BrandBorder, RoundedCornerShape(50))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                                val memberOfficialBadge = userProfile?.getOfficialRoleBadgeRes()
                                if (memberOfficialBadge != null) {
                                    Image(
                                        painter = painterResource(id = memberOfficialBadge),
                                        contentDescription = userProfile.getOfficialRoleTitle(),
                                        modifier = Modifier
                                            .height(20.dp)
                                            .padding(start = 2.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }

                    // Large 3D Digital Badge (Lv.10+)
                    if (targetWealthLvl >= 10) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Image(
                                painter = painterResource(id = targetWealthTier.badgeDrawableRes),
                                contentDescription = targetWealthTier.tierName,
                                modifier = Modifier.size(68.dp)
                            )
                            Surface(
                                color = Color(0xFF1E171C),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Brush.horizontalGradient(targetWealthTier.gradientColors)),
                                modifier = Modifier.offset(y = (-5).dp)
                            ) {
                                Text(
                                    text = "LV.$targetWealthLvl",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showFullProfileDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.75f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGold)
                ) {
                    Text("View Full Profile", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Wealth Level Card (3D Shield Icon)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E171C)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Brush.horizontalGradient(targetWealthTier.gradientColors)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (targetWealthLvl >= 10) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_wealth_shield_3d),
                                            contentDescription = "Wealth Shield",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(targetWealthTier.gradientColors)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(targetWealthTier.badgeIcon, fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Wealth Level • Lv.$targetWealthLvl",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = targetWealthTier.tierName,
                                            color = BrandGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "%,d / %,d XP".format(targetWealthXp, tMaxWealthXp),
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { targetWealthProgress },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                color = targetWealthTier.gradientColors.first(),
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }

                    // Charm Level Card (3D Heart Icon)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181528)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Brush.horizontalGradient(targetCharmTier.gradientColors)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (targetCharmLvl >= 10) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_charm_heart_3d),
                                            contentDescription = "Charm Heart",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(targetCharmTier.gradientColors)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("💖", fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Charm Level • Lv.$targetCharmLvl",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = targetCharmTier.tierName,
                                            color = Color(0xFFFF80AB),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "%,d / %,d XP".format(targetCharmXp, tMaxCharmXp),
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { targetCharmProgress },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                color = targetCharmTier.gradientColors.first(),
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }

                // If member is admin in rooms, show count and clickable chip to view list of rooms
                if (memberAdminRooms.isNotEmpty()) {
                    Surface(
                        color = Color(0xFF0D47A1).copy(alpha = 0.35f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFF4FC3F7).copy(alpha = 0.7f)),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { showAdminRoomsDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ Admin in ${memberAdminRooms.size} Hangout Rooms (View List) ❯",
                                color = Color(0xFF4FC3F7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Location Privacy Shield: User's location (city, state, distance) is hidden unless both users are mutual Best Friends
                val isMutualBestFriend = currentUser.isMutualBestFriend(userProfile ?: UserProfile(uid = member.uid))
                if (isSelf) {
                    val locationStr = userProfile?.locationName?.ifBlank { userProfile.city } ?: userProfile?.city ?: "Kolkata"
                    Text(
                        text = "📍 $locationStr (Your Location) • 👥 $memberFollowerCount Followers",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (isMutualBestFriend) {
                    val locationStr = userProfile?.locationName?.ifBlank { userProfile.city } ?: userProfile?.city ?: "Kolkata"
                    val distKm = if (userProfile != null && currentUser.latitude != 0.0 && userProfile.latitude != 0.0) {
                        calculateDistanceKm(currentUser.latitude, currentUser.longitude, userProfile.latitude, userProfile.longitude)
                    } else 0
                    val distText = if (distKm > 0) " • $distKm km away" else ""
                    Text(
                        text = "📍 $locationStr$distText • ⭐ Mutual Best Friend • 👥 $memberFollowerCount Followers",
                        color = Color(0xFF81C784),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "📍 Location Hidden 🔒 (Best Friends Only) • 👥 $memberFollowerCount Followers",
                        color = Color(0xFFFFB74D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // 8-digit User ID Chip with Click-to-Copy
                val memberUserId = remember(userProfile?.userId, member.userId, member.uid, currentUser.userId, currentUser.uid) {
                    if (member.uid == currentUser.uid) {
                        currentUser.userId
                    } else {
                        val raw = userProfile?.userId?.ifBlank { member.userId } ?: member.userId
                        if (raw.isNotBlank() && raw.all { it.isDigit() } && raw.length == 8) {
                            raw
                        } else if (member.uid.length == 8 && member.uid.all { it.isDigit() }) {
                            member.uid
                        } else {
                            (10000000 + kotlin.math.abs(member.uid.hashCode()) % 90000000).toString()
                        }
                    }
                }
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.padding(top = 6.dp).clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("User ID", memberUserId)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "User ID $memberUserId copied! 📋", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "User ID: $memberUserId",
                            color = BrandGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy User ID",
                            tint = BrandGold,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                // Registered Email Display & Admin Verified tag
                val targetEmail = if (isSelf) {
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email?.takeIf { it.isNotBlank() } ?: currentUser.email
                } else {
                    userProfile?.email ?: ""
                }
                val targetEmailPrivacy = if (isSelf) currentUser.emailPrivacy else (userProfile?.emailPrivacy ?: "only_me")
                val isViewerAdmin = currentUser.isOfficialAdmin(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email)

                if (targetEmail.isNotBlank()) {
                    Surface(
                        color = Color(0xFF1E1728),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.8.dp, if (isViewerAdmin) BrandCyan else BrandBorder),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📧", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            if (isSelf || isViewerAdmin || targetEmailPrivacy == "public") {
                                Text(
                                    text = targetEmail,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (isViewerAdmin && !isSelf) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "[🛡️ Admin Verified]",
                                        color = BrandCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Email: Hidden 🔒 (Private)",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                val bioText = userProfile?.bio?.ifEmpty { "Excited to meet and hangout with awesome people!" }
                    ?: "Excited to meet and hangout with awesome people!"
                Text(
                    text = "\"$bioText\"",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subscription Level Perks Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMemberVip) Color(0xFF2C2210) else if (isMemberPlat) Color(0xFF102830) else BrandCardSubtle
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isMemberVip) BrandGold.copy(alpha = 0.6f) else if (isMemberPlat) BrandCyan.copy(alpha = 0.6f) else BrandBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isMemberVip) "👑 VIP LEVEL BENEFITS" else if (isMemberPlat) "♦ PLATINUM BENEFITS" else "🌟 STANDARD BENEFITS",
                            color = if (isMemberVip) BrandGold else if (isMemberPlat) BrandCyan else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isMemberVip) "• Direct 1-on-1 Chats • Priority Stage Mic • Golden Halo Crown"
                            else if (isMemberPlat) "• Profile Boost • Cyan Halo Ring • Extended Reach"
                            else "• Message Requests • Public Room Audio & Chat Access",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isSelf) {
                    Button(
                        onClick = {
                            if (onOpenGiftCenter != null) {
                                onOpenGiftCenter(member)
                            } else {
                                showProfileGiftPicker = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Gift", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!isSelf && showRoomActions && roomId.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            if (isSendingMicLinkRequest || hasSentMicLinkRequest) return@OutlinedButton
                            isSendingMicLinkRequest = true
                            val requestId = UUID.randomUUID().toString()
                            db.collection("intimacy_requests").document(requestId)
                                .set(
                                    mapOf(
                                        "id" to requestId,
                                        "requestType" to "mic_link",
                                        "roomId" to roomId,
                                        "senderUid" to currentUser.uid,
                                        "senderName" to currentUser.name,
                                        "senderPhoto" to currentUser.photo,
                                        "recipientUid" to member.uid,
                                        "recipientName" to member.name,
                                        "recipientPhoto" to member.photoUrl,
                                        "giftId" to "mic_link_request",
                                        "giftName" to "Intimacy Mic Link",
                                        "giftEmoji" to "💞",
                                        "bondType" to "couple",
                                        "status" to "pending",
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                )
                                .addOnSuccessListener {
                                    isSendingMicLinkRequest = false
                                    hasSentMicLinkRequest = true
                                    Toast.makeText(context, "Mic-link request sent to ${member.name}.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { error ->
                                    isSendingMicLinkRequest = false
                                    Log.e("RoomMemberProfile", "Failed to send mic-link request to ${member.uid}", error)
                                    Toast.makeText(context, "Could not send request: ${error.localizedMessage ?: "Please try again."}", Toast.LENGTH_LONG).show()
                                }
                        },
                        enabled = !isSendingMicLinkRequest && !hasSentMicLinkRequest,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF4081))
                    ) {
                        Text(
                            text = when {
                                isSendingMicLinkRequest -> "Sending mic-link request…"
                                hasSentMicLinkRequest -> "Mic-link request sent"
                                else -> "💞 Request Intimacy Mic Link"
                            },
                            color = Color(0xFFFF80AB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 1. Follow / Unfollow interactive button (Always Available for All Profiles)
                Button(
                    onClick = {
                        val newFollow = !isFollowingMember
                        isFollowingMember = newFollow
                        if (newFollow) {
                            memberFollowerCount += 1
                            if (member.uid.isNotBlank() && member.uid != currentUser.uid) {
                                val record = FollowerRecord(
                                    uid = currentUser.uid,
                                    name = currentUser.name,
                                    photo = currentUser.photo,
                                    timestamp = System.currentTimeMillis()
                                )
                                db.collection("users").document(member.uid)
                                    .collection("followers").document(currentUser.uid).set(record)
                                db.collection("users").document(currentUser.uid)
                                    .collection("following").document(member.uid).set(
                                        mapOf(
                                            "hostUid" to member.uid,
                                            "hostName" to member.name,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                    )
                                db.collection("users").document(member.uid)
                                    .update("followerCount", FieldValue.increment(1))
                                db.collection("users").document(currentUser.uid)
                                    .update("followingCount", FieldValue.increment(1))
                                try {
                                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                                        .subscribeToTopic("host_${member.uid}")
                                } catch (e: Exception) {
                                    Log.w("AudioHangoutRoom", "FCM topic subscribe failed: ${e.message}")
                                }
                                sendNotification(
                                    db = db,
                                    targetUserId = member.uid,
                                    sender = currentUser,
                                    type = "follow",
                                    title = "🌟 New Follower!",
                                    message = "${currentUser.name} started following you!"
                                )
                            } else {
                                db.collection("users").document(currentUser.uid)
                                    .update("followerCount", FieldValue.increment(1))
                            }
                            Toast.makeText(context, "🌟 Following ${member.name}! ($memberFollowerCount)", Toast.LENGTH_SHORT).show()
                        } else {
                            memberFollowerCount = (memberFollowerCount - 1).coerceAtLeast(0)
                            if (member.uid.isNotBlank() && member.uid != currentUser.uid) {
                                db.collection("users").document(member.uid)
                                    .collection("followers").document(currentUser.uid).delete()
                                db.collection("users").document(currentUser.uid)
                                    .collection("following").document(member.uid).delete()
                                db.collection("users").document(member.uid)
                                    .update("followerCount", FieldValue.increment(-1))
                                db.collection("users").document(currentUser.uid)
                                    .update("followingCount", FieldValue.increment(-1))
                                try {
                                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                                        .unsubscribeFromTopic("host_${member.uid}")
                                } catch (e: Exception) {
                                    Log.w("AudioHangoutRoom", "FCM topic unsubscribe failed: ${e.message}")
                                }
                            } else {
                                db.collection("users").document(currentUser.uid)
                                    .update("followerCount", FieldValue.increment(-1))
                            }
                            Toast.makeText(context, "Unfollowed ${member.name}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowingMember) BrandDark else BrandCoral
                    ),
                    border = BorderStroke(1.dp, if (isFollowingMember) BrandGold else Color.Transparent),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFollowingMember) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isFollowingMember) BrandGold else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFollowingMember) "✓ Following ($memberFollowerCount)" else "+ Follow ($memberFollowerCount)",
                        color = if (isFollowingMember) BrandGold else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!isSelf) {
                    Button(
                        onClick = {
                            val willBeBestFriend = !isBestFriend
                            isBestFriend = willBeBestFriend
                            if (willBeBestFriend) {
                                db.collection("users").document(currentUser.uid)
                                    .update("bestFriends", FieldValue.arrayUnion(member.uid))
                                Toast.makeText(context, "⭐ Added ${member.name} to Best Friends! (Location unmasked if mutual)", Toast.LENGTH_SHORT).show()
                            } else {
                                db.collection("users").document(currentUser.uid)
                                    .update("bestFriends", FieldValue.arrayRemove(member.uid))
                                Toast.makeText(context, "Removed ${member.name} from Best Friends", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBestFriend) Color(0xFF311B92) else Color(0xFF212121)
                        ),
                        border = BorderStroke(1.dp, if (isBestFriend) BrandGold else Color.Gray.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text(
                            text = if (isBestFriend) "⭐ Best Friend (Location Unlocked if Mutual)" else "☆ Add to Best Friends (Location Privacy)",
                            color = if (isBestFriend) BrandGold else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!isSelf) {
                    // Action Buttons: Like & Chat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onLike()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Like / Match", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                if (isVipOrPlatinum) {
                                    onDirectMessage()
                                } else {
                                    onRequestMessage()
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVipOrPlatinum) BrandCyan else BrandCardSubtle
                            ),
                            shape = RoundedCornerShape(50),
                            border = if (!isVipOrPlatinum) BorderStroke(1.dp, BrandCoral.copy(alpha = 0.5f)) else null
                        ) {
                            Icon(
                                imageVector = if (isVipOrPlatinum) Icons.Default.Chat else Icons.Default.Send,
                                contentDescription = null,
                                tint = if (isVipOrPlatinum) Color.Black else BrandCoral,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Send Message",
                                color = if (isVipOrPlatinum) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (showRoomActions) OutlinedButton(
                        onClick = {
                            onTagMember(member.name)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, BrandCoral.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("@", color = BrandCoral, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tag @${member.name.substringBefore(' ')} in Room Chat", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isHost && !isSelf && onInviteToVideo != null && member.role != "host" && member.role != "co_host") {
                        Button(
                            onClick = {
                                onInviteToVideo(member)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📹 Invite to Video Stage", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (isCurrentAdmin && !isSelf) {
                        val isTargetHost = member.role == "host"
                        val isTargetAdmin = member.uid in roomAdminUserIds

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isHost) "👑 HOST MODERATION & ADMIN CONTROLS" else "🛡️ ROOM ADMIN CONTROLS",
                                    color = BrandGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                if (isHost) {
                                    val isMemberAdmin = member.uid in roomAdminUserIds
                                    Button(
                                        onClick = {
                                            onToggleAdmin?.invoke(member)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isMemberAdmin) Color(0xFF3E2723) else Color(0xFF1B5E20)
                                        ),
                                        border = BorderStroke(1.dp, if (isMemberAdmin) BrandCoral else BrandGreen),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.fillMaxWidth().height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isMemberAdmin) "🛡️ Remove Admin Privileges" else "👑 Appoint as Room Admin (${roomAdminUserIds.size}/10)",
                                            color = if (isMemberAdmin) BrandCoral else BrandGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                val isTargetUserFounder = userProfile?.isFounder() == true
                                val isCurUserFounder = currentUser.isFounder()
                                val canModerateTarget = (isHost || currentUser.isFounder() || currentUser.isCeo() || currentUser.isStaffMember())

                                if (isTargetUserFounder && !isCurUserFounder) {
                                    Surface(
                                        color = Color(0xFF3E1200),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("👑 LiveTogather Founder • Supreme Immunity Active", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else if ((!isTargetHost || canModerateTarget) && (!isTargetAdmin || isHost || canModerateTarget)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onKickOut(member)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("👢 Kick Out", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                onBan(member)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF210000)),
                                            border = BorderStroke(1.dp, Color.Red),
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("🔨 Ban User", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Report User button (Accessible to all members to report abusive behavior)
                    Button(
                        onClick = { showReportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1515)),
                        border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(34.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("🚨 Report User to Admins", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = {
                            onBlock(member)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Text("🚫 Block ${member.name.substringBefore(' ')}", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (showRoomActions) Button(
                    onClick = {
                        onDismiss()
                        onLeaveRoom()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🚪 Leave Room", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showReportDialog) {
            ReportUserDialog(
                reportedMember = member,
                reportedUserId = if (member.userId.isNotBlank()) member.userId else (userProfile?.userId ?: member.uid.take(8)),
                currentUser = currentUser,
                db = db,
                roomId = roomId,
                onDismiss = { showReportDialog = false }
            )
        }

        if (showAdminRoomsDialog) {
            Dialog(onDismissRequest = { showAdminRoomsDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161214)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF4FC3F7).copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🛡️ Rooms Administered by ${member.name}",
                            color = Color(0xFF4FC3F7),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total: ${memberAdminRooms.size} Hangout Rooms",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(memberAdminRooms) { rId ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🎙️", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Hangout Room ID: $rId", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Role: Verified Room Admin 🛡️", color = Color(0xFF4FC3F7), fontSize = 9.sp)
                                        }
                                    }

                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { showAdminRoomsDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

            }
        }

        if (showProfileGiftPicker) {
            AlertDialog(
                onDismissRequest = { showProfileGiftPicker = false },
                title = { Text("Send a gift to ${member.name}", color = Color.White) },
                text = {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(VIDEO_GIFT_CATALOG) { gift ->
                            OutlinedButton(
                                onClick = { sendProfileGift(gift) },
                                enabled = !isSendingProfileGift,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (isSendingProfileGift) "Sending…" else "${gift.emoji} ${gift.name} • %,d 💎".format(gift.cost),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProfileGiftPicker = false }) {
                        Text("Cancel", color = Color.LightGray)
                    }
                },
                containerColor = BrandCard,
                shape = RoundedCornerShape(18.dp)
            )
        }

        if (showFullProfileDialog) {
            UserFullProfileDialog(
                profile = userProfile ?: UserProfile(
                    uid = member.uid,
                    userId = member.userId,
                    name = member.name,
                    photo = member.photoUrl
                ),
                viewer = currentUser,
                onDismiss = { showFullProfileDialog = false }
            )
        }
    }
}

@Composable
private fun UserFullProfileDialog(profile: UserProfile, viewer: UserProfile, onDismiss: () -> Unit) {
    val isSelf = profile.uid == viewer.uid
    val isViewerAdmin = viewer.isOfficialAdmin(
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
    )
    val canSeeEmail = isSelf || isViewerAdmin || profile.emailPrivacy == "public"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = BrandDark) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Full Profile", color = BrandGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close profile", tint = Color.White)
                    }
                }
                AsyncImage(
                    model = profile.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                    contentDescription = "${profile.name} profile photo",
                    modifier = Modifier.size(112.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(profile.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    listOf(profile.age.toString(), profile.gender).filter { it.isNotBlank() }.joinToString(" • "),
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                FullProfileInfoRow("Location", profile.getDisplayLocation(viewer))
                if (profile.bio.isNotBlank()) FullProfileInfoRow("About", profile.bio)
                if (profile.profession.isNotBlank()) FullProfileInfoRow("Profession", profile.profession)
                if (profile.education.isNotBlank()) FullProfileInfoRow("Education", profile.education)
                if (profile.interests.isNotBlank()) FullProfileInfoRow("Interests", profile.interests)
                if (profile.hobbies.isNotBlank()) FullProfileInfoRow("Hobbies", profile.hobbies)
                if (profile.languages.isNotBlank()) FullProfileInfoRow("Languages", profile.languages)
                if (profile.zodiac.isNotBlank()) FullProfileInfoRow("Zodiac", profile.zodiac)
                FullProfileInfoRow("Followers", profile.followerCount.toString())
                if (profile.email.isNotBlank()) {
                    FullProfileInfoRow(
                        "Email",
                        if (canSeeEmail) profile.email else "Hidden by privacy settings"
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = BrandCoral)) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FullProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            .background(BrandCard, RoundedCornerShape(12.dp)).padding(12.dp)
    ) {
        Text(label, color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, color = Color.White, fontSize = 13.sp)
    }
}

// -------------------------------------------------------------
// HELPER: BROADCAST LIVE NOTIFICATION TO ALL FOLLOWERS
// -------------------------------------------------------------
fun broadcastLiveNotificationToFollowers(
    db: FirebaseFirestore,
    hostId: String,
    hostName: String,
    hostPhoto: String,
    room: RoomItem
) {
    if (hostId.isBlank()) return
    db.collection("users").document(hostId).collection("followers").get()
        .addOnSuccessListener { followersSnapshot ->
            if (followersSnapshot != null && !followersSnapshot.isEmpty) {
                for (doc in followersSnapshot.documents) {
                    val followerId = doc.getString("uid") ?: doc.id
                    if (followerId.isNotBlank() && followerId != hostId) {
                        val notifId = UUID.randomUUID().toString()
                        val notif = NotificationItem(
                            id = notifId,
                            targetUserId = followerId,
                            senderId = hostId,
                            senderName = hostName,
                            senderPhoto = hostPhoto,
                            title = "🔴 $hostName is LIVE NOW!",
                            message = "$hostName just went LIVE in '${room.title}'! Tap to join.",
                            type = "host_live",
                            timestamp = System.currentTimeMillis(),
                            isRead = false,
                            roomId = room.id
                        )
                        db.collection("users").document(followerId)
                            .collection("notifications").document(notifId).set(notif)
                        db.collection("notifications").document(notifId).set(notif)
                    }
                }
            }
        }
}

// -------------------------------------------------------------
// ROOM SETTINGS & CONTROLS DIALOG (PRIVACY, SCHEDULE, THEME, MODERATION)
// -------------------------------------------------------------
// ROOM SETTINGS & CONTROLS DIALOG (PRIVACY, SCHEDULE, THEME, MODERATION)
// -------------------------------------------------------------
@Composable
fun RoomSettingsDialog(
    room: RoomItem,
    isHost: Boolean,
    currentUser: UserProfile,
    activeMembers: List<RoomMember>,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onUpdateTheme: (String) -> Unit,
    onKickMember: ((RoomMember) -> Unit)? = null,
    onBanMember: ((RoomMember) -> Unit)? = null,
    onEditRoom: (() -> Unit)? = null,
    onDeleteRoom: (() -> Unit)? = null,
    onConvertToVideo: ((RoomItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var announcementText by remember { mutableStateOf(room.announcement) }
    var isRoomLocked by remember { mutableStateOf(room.isLocked) }
    var isRoomVip by remember { mutableStateOf(room.privacy == "vip_only" || room.isVipRoom) }
    var feedbackText by remember { mutableStateOf("") }
    var isSendingFeedback by remember { mutableStateOf(false) }
    var scheduledTimeText by remember { mutableStateOf(room.scheduledTime) }
    var selectedPrivacy by remember { mutableStateOf(room.privacy) }
    var isUploadingTheme by remember { mutableStateOf(false) }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingTheme = true
            coroutineScope.launch {
                val themeUrl = ImageUploadManager.uploadImage(context, uri, "rooms") ?: uri.toString()
                onUpdateTheme(themeUrl)
                db.collection("rooms").document(room.id).update("banner_image_url", themeUrl)
                Toast.makeText(context, "Theme updated from gallery!", Toast.LENGTH_SHORT).show()
                isUploadingTheme = false
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141720)),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️ Room Settings & Controls", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 00. ROOM MODE CONVERSION: AUDIO LOUNGE -> VIDEO STREAM (HOST & ADMIN)
                    val canConvertMode = isHost || currentUser.isAdmin || currentUser.isOfficialAdmin(currentUser.uid) || currentUser.uid in room.adminUserIds
                    if (canConvertMode) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("📹 Room Mode Conversion", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Switch broadcast mode to live Video Stream (1P-4P dynamic camera grid). All viewers and listeners transition seamlessly.", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val finalTitle = room.title.ifBlank { "${room.hostName}'s Video Stream" }
                                            val updates = mapOf<String, Any>(
                                                "roomType" to "video",
                                                "isVideoRoom" to true,
                                                "title" to finalTitle
                                            )
                                            db.collection("rooms").document(room.id).update(updates)
                                            if (room.category.isNotBlank()) {
                                                db.collection("intent_categories").document(room.category)
                                                    .collection("rooms").document(room.id).update(updates)
                                            }
                                            val sysComment = LiveRoomComment(
                                                id = UUID.randomUUID().toString(),
                                                roomId = room.id,
                                                userId = currentUser.uid,
                                                userName = "🔄 SYSTEM",
                                                message = "Room mode converted to Video Stream by ${currentUser.name}",
                                                timestamp = System.currentTimeMillis()
                                            )
                                            db.collection("rooms").document(room.id).collection("live_comments").document(sysComment.id).set(sysComment)
                                            Toast.makeText(context, "📹 Converting room to Video Stream...", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                            onConvertToVideo?.invoke(room.copy(roomType = "video", title = finalTitle))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E)),
                                        border = BorderStroke(1.dp, Color(0xFF3F4B6B)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Text("🔄 Convert to Video Stream 📹", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 0. ROOM ANNOUNCEMENT (PINNED BANNER & BROADCAST IN CHAT)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📢 Room Announcement", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                    if (isHost && announcementText.isNotBlank()) {
                                        Text(
                                            text = "Clear",
                                            color = Color(0xFFFF5252),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                announcementText = ""
                                                db.collection("rooms").document(room.id).update("announcement", "")
                                                Toast.makeText(context, "Announcement cleared", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Pinned at the top of the room and broadcast to all occupants.", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                if (isHost) {
                                    OutlinedTextField(
                                        value = announcementText,
                                        onValueChange = { announcementText = it },
                                        placeholder = { Text("Write announcement (e.g. Welcome to our live stage! 🎉)", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = BrandGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                            focusedContainerColor = Color(0xFF141720),
                                            unfocusedContainerColor = Color(0xFF141720)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            if (announcementText.isNotBlank()) {
                                                db.collection("rooms").document(room.id).update("announcement", announcementText.trim())
                                                val comment = LiveRoomComment(
                                                    id = UUID.randomUUID().toString(),
                                                    roomId = room.id,
                                                    userId = currentUser.uid,
                                                    userName = "📢 ANNOUNCEMENT",
                                                    message = announcementText.trim(),
                                                    timestamp = System.currentTimeMillis()
                                                )
                                                db.collection("rooms").document(room.id)
                                                    .collection("live_comments").document(comment.id).set(comment)
                                                Toast.makeText(context, "Announcement updated & broadcasted! 📢", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A281E)),
                                        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Text("Broadcast Announcement 📢", color = BrandGold, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                    }
                                } else {
                                    Text(
                                        text = if (room.announcement.isNotBlank()) room.announcement else "No active announcement at this moment.",
                                        color = if (room.announcement.isNotBlank()) BrandGold else Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // 0B. PK BATTLE ARENA CONTROLS (HOST ONLY)
                    if (isHost) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_pk_flame_vs),
                                                contentDescription = "PK Battle",
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("⚔️ Live PK Battle Arena", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (room.isPkActive) {
                                            Surface(
                                                color = Color(0xFFFF1744).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(0.8.dp, Color(0xFFFF1744))
                                            ) {
                                                Text("ACTIVE 🔥", color = Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Start a 5-minute Tug-of-War PK challenge against a co-host or guest!", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    if (!room.isPkActive) {
                                        Button(
                                            onClick = {
                                                onDismiss()
                                                val candidate = activeMembers.firstOrNull { it.uid != currentUser.uid }
                                                val oppUid = candidate?.uid ?: "guest_challenger"
                                                val oppName = candidate?.name ?: "Challenger Host"
                                                val oppPhoto = candidate?.photoUrl ?: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d"
                                                val now = System.currentTimeMillis()
                                                db.collection("rooms").document(room.id).update(
                                                    mapOf(
                                                        "isPkActive" to true,
                                                        "pkHost1Uid" to room.hostUid,
                                                        "pkHost1Name" to room.hostName,
                                                        "pkHost1Photo" to room.hostPhotoUrl,
                                                        "pkHost1Score" to 0L,
                                                        "pkHost2Uid" to oppUid,
                                                        "pkHost2Name" to oppName,
                                                        "pkHost2Photo" to oppPhoto,
                                                        "pkHost2Score" to 0L,
                                                        "pkStartTime" to now,
                                                        "pkDurationSec" to 300,
                                                        "pkStatus" to "active",
                                                        "pkWinnerUid" to ""
                                                    )
                                                )
                                                db.collection("rooms").document(room.id).collection("pk_supporters").get().addOnSuccessListener { snaps ->
                                                    for (d in snaps.documents) d.reference.delete()
                                                }
                                                Toast.makeText(context, "⚔️ 5-Minute PK Battle Started! 🔥", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF331E26)),
                                            border = BorderStroke(1.dp, Color(0xFF8F384C)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(40.dp)
                                        ) {
                                            Text("⚔️ Start 5-Minute PK Battle", color = Color(0xFFFF859E), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                onDismiss()
                                                db.collection("rooms").document(room.id).update(
                                                    mapOf(
                                                        "isPkActive" to false,
                                                        "pkStatus" to "ended"
                                                    )
                                                )
                                                Toast.makeText(context, "🛑 PK Battle Ended", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252A36)),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(40.dp)
                                        ) {
                                            Text("🛑 End PK Battle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 1. LIVE / OFFLINE BROADCAST STATUS
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("🔴 Live Broadcast Status", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            db.collection("rooms").document(room.id).update(
                                                mapOf(
                                                    "isHostLive" to true,
                                                    "statusMessage" to "LIVE NOW"
                                                )
                                            )
                                            broadcastLiveNotificationToFollowers(
                                                db = db,
                                                hostId = room.hostId,
                                                hostName = room.hostName,
                                                hostPhoto = room.hostPhotoUrl,
                                                room = room
                                            )
                                            Toast.makeText(context, "Room is now LIVE! Followers notified.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (room.isHostLive) Color(0xFF1C3829) else Color(0xFF202433)
                                        ),
                                        border = BorderStroke(1.dp, if (room.isHostLive) Color(0xFF34D399) else Color.White.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("🔴 Go Live", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (room.isHostLive) Color(0xFF34D399) else Color.LightGray)
                                    }

                                    Button(
                                        onClick = {
                                            db.collection("rooms").document(room.id).update(
                                                mapOf(
                                                    "isHostLive" to false,
                                                    "statusMessage" to "Host will be live soon"
                                                )
                                            )
                                            Toast.makeText(context, "Room status set to 'Host will be live soon'", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!room.isHostLive) Color(0xFF2A281E) else Color(0xFF202433)
                                        ),
                                        border = BorderStroke(1.dp, if (!room.isHostLive) BrandGold else Color.White.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Text("⏳ Set Offline", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!room.isHostLive) BrandGold else Color.LightGray)
                                    }
                                }
                            }
                        }
                    }

                    // 2. SCHEDULE TIMING
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("⏰ Live Timing Schedule", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Notify users when you will be live next (e.g. Tonight 9:00 PM)", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = scheduledTimeText,
                                        onValueChange = { scheduledTimeText = it },
                                        placeholder = { Text("e.g. Tonight 9:30 PM", color = Color.Gray, fontSize = 11.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = BrandGold,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                            focusedContainerColor = Color(0xFF141720),
                                            unfocusedContainerColor = Color(0xFF141720)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            db.collection("rooms").document(room.id).update("scheduledTime", scheduledTimeText.trim())
                                            Toast.makeText(context, "Schedule timing saved!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E)),
                                        border = BorderStroke(1.dp, Color(0xFF3F4B6B)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 3. ROOM PRIVACY CONTROLS
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("🔒 Room Access & Privacy", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                listOf(
                                    "public" to "🌐 Public (Open to everyone)",
                                    "vip_only" to "⭐ VIP & Platinum Only",
                                    "private" to "🔒 Invite Only"
                                ).forEach { (key, label) ->
                                    val isSelected = selectedPrivacy == key
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFF262C3E) else Color.Transparent)
                                            .clickable {
                                                selectedPrivacy = key
                                                db.collection("rooms").document(room.id).update("privacy", key)
                                                Toast.makeText(context, "Privacy set to: $label", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 8.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedPrivacy = key
                                                db.collection("rooms").document(room.id).update("privacy", key)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = BrandGold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(label, color = if (isSelected) Color.White else Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                if (isHost) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Lock Room Toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("🔒 Lock Room", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (isRoomLocked) "Room is currently locked 🔒" else "Room is open for everyone",
                                                color = if (isRoomLocked) Color(0xFFFF8A80) else Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Switch(
                                            checked = isRoomLocked,
                                            onCheckedChange = { locked ->
                                                isRoomLocked = locked
                                                db.collection("rooms").document(room.id).update("isLocked", locked)
                                                Toast.makeText(context, if (locked) "Room is now locked 🔒" else "Room is now unlocked 🔓", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFFE53935)
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Room Premium (VIP) Toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("⭐ Room Premium (VIP)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (isRoomVip) "Exclusive VIP room mode enabled ⭐" else "Standard room mode",
                                                color = if (isRoomVip) BrandGold else Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Switch(
                                            checked = isRoomVip,
                                            onCheckedChange = { vip ->
                                                isRoomVip = vip
                                                db.collection("rooms").document(room.id).update(
                                                    mapOf(
                                                        "isVipRoom" to vip,
                                                        "privacy" to if (vip) "vip_only" else "public"
                                                    )
                                                )
                                                Toast.makeText(context, if (vip) "Room set to Premium VIP ⭐" else "Room set to Standard Mode", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.Black,
                                                checkedTrackColor = BrandGold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. ROOM THEME CUSTOMIZER (LOCAL STORAGE + PRESETS)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎨 Room Theme & Background", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                    Button(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E)),
                                        border = BorderStroke(1.dp, Color(0xFF3F4B6B)),
                                        shape = RoundedCornerShape(50),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pick from Phone", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(ROOM_THEME_PRESETS) { preset ->
                                        val isCurrent = room.banner_image_url == preset.imageUrl
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {
                                                onUpdateTheme(preset.imageUrl)
                                                db.collection("rooms").document(room.id).update("banner_image_url", preset.imageUrl)
                                                Toast.makeText(context, "Theme set to: ${preset.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            AsyncImage(
                                                model = if (preset.imageUrl == "vip_royal_stage") R.drawable.vip_royal_stage else preset.imageUrl,
                                                contentDescription = preset.name,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(
                                                        2.dp,
                                                        if (isCurrent) BrandGold else Color.Transparent,
                                                        RoundedCornerShape(10.dp)
                                                    ),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = preset.name.take(10),
                                                color = if (isCurrent) BrandGold else Color.Gray,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. MEMBER MODERATION (KICK & BAN)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("🛡️ Room Member Moderation", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Manage active members in your room", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))

                                val nonHostMembers = activeMembers.filter { it.uid != room.hostId }
                                if (nonHostMembers.isEmpty()) {
                                    Text("No other members currently in this room.", color = Color.Gray, fontSize = 11.sp)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        nonHostMembers.forEach { member ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF141720), RoundedCornerShape(10.dp))
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    AsyncImage(
                                                        model = member.photoUrl,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(32.dp).clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(member.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        Text(member.tier, color = BrandGold, fontSize = 9.sp)
                                                    }
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    // Kick Button
                                                    OutlinedButton(
                                                        onClick = {
                                                            onKickMember?.invoke(member)
                                                            db.collection("rooms").document(room.id)
                                                                .collection("members").document(member.uid).delete()
                                                            db.collection("rooms").document(room.id)
                                                                .update("kickedUserIds", FieldValue.arrayUnion(member.uid))
                                                            Toast.makeText(context, "${member.name} kicked out.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.6f)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("Kick", color = Color(0xFFFFB74D), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    // Ban Button
                                                    Button(
                                                        onClick = {
                                                            onBanMember?.invoke(member)
                                                            db.collection("rooms").document(room.id)
                                                                .collection("members").document(member.uid).delete()
                                                            db.collection("rooms").document(room.id)
                                                                .update("bannedUserIds", FieldValue.arrayUnion(member.uid))
                                                            Toast.makeText(context, "${member.name} banned.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1820)),
                                                        border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.8f)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("Ban", color = Color(0xFFFF859E), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. CLEAR ROOM CHAT (HOST ONLY)
                    if (isHost) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("🗑️ Clear Room Chat", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Permanently delete all live messages from this room in real-time.", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            db.collection("rooms").document(room.id)
                                                .collection("live_comments")
                                                .get()
                                                .addOnSuccessListener { snapshot ->
                                                    val batch = db.batch()
                                                    for (doc in snapshot.documents) {
                                                        batch.delete(doc.reference)
                                                    }
                                                    batch.commit().addOnSuccessListener {
                                                        Toast.makeText(context, "Room chat cleared successfully!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1820)),
                                        border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF859E), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Clear All Messages", color = Color(0xFFFF859E), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 7. EDIT ROOM DETAILS (HOST ONLY)
                    if (isHost && onEditRoom != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("✏️ Edit Room Information", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Change room title, category hub, luxury atmosphere wallpaper, or status.", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onEditRoom()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E)),
                                        border = BorderStroke(1.dp, Color(0xFF3F4B6B)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Edit Room Details", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 8. DELETE ROOM PERMANENTLY (HOST ONLY)
                    if (isHost && onDeleteRoom != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF251318)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("🚨 Danger Zone: Delete Room", color = Color(0xFFFF5252), fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Permanently delete this room from Firestore. All live members will be disconnected.", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onDeleteRoom()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A151D)),
                                        border = BorderStroke(1.dp, Color(0xFFE53935)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(38.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF859E), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Delete Room Permanently 🗑️", color = Color(0xFFFF859E), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 9. FEEDBACK & SUPPORT SECTION (OPEN TO ALL USERS)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F2C)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("💬 Feedback & Support", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Have feedback or reporting an issue? Send a direct note to the LiveTogather support team.", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = feedbackText,
                                    onValueChange = { feedbackText = it },
                                    placeholder = { Text("Write your feedback or bug report here...", color = Color.Gray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = BrandGold,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                        focusedContainerColor = Color(0xFF141720),
                                        unfocusedContainerColor = Color(0xFF141720)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (feedbackText.isNotBlank()) {
                                            isSendingFeedback = true
                                            val feedbackDoc = mapOf(
                                                "userId" to currentUser.uid,
                                                "userName" to currentUser.name,
                                                "roomId" to room.id,
                                                "roomTitle" to room.title,
                                                "message" to feedbackText.trim(),
                                                "timestamp" to FieldValue.serverTimestamp()
                                            )
                                            db.collection("feedback").add(feedbackDoc)
                                                .addOnSuccessListener {
                                                    feedbackText = ""
                                                    isSendingFeedback = false
                                                    Toast.makeText(context, "Feedback submitted! Thank you 🙏", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    isSendingFeedback = false
                                                    Toast.makeText(context, "Failed to send feedback: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    },
                                    enabled = feedbackText.isNotBlank() && !isSendingFeedback,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E)),
                                    border = BorderStroke(1.dp, Color(0xFF3F4B6B)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(38.dp)
                                ) {
                                    Text(
                                        text = if (isSendingFeedback) "Submitting..." else "Submit Feedback 📨",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
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
// -------------------------------------------------------------
// 5.5 ROOM EXIT & IN-APP FLOATING PIP (MINI-PLAYER) SYSTEM
// -------------------------------------------------------------
@Composable
fun RoomExitOptionsDialog(
    roomTitle: String = "",
    isHost: Boolean,
    onKeep: () -> Unit,
    onExit: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1423)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, BrandGold.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(listOf(BrandCoral, BrandGold, Color(0xFFFF5252))))
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Exit Options",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Room Session Options",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = roomTitle,
                    color = BrandGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Option 1: KEEP ROOM (Mini-Player In-App PIP)
                Button(
                    onClick = onKeep,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCard),
                    border = BorderStroke(1.2.dp, BrandCyan.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PictureInPicture, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Keep in Background (Mini-Player)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Stay seated on mic while exploring app", color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: EXIT ROOM
                Button(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(if (isHost) "Exit Room (Host)" else "Exit Room", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(if (isHost) "Room stays live if Admin present" else "Vacate seat & leave", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FloatingAudioRoomPIP(
    room: RoomItem,
    currentUser: UserProfile,
    isHost: Boolean,
    isMicMuted: Boolean,
    isOnStage: Boolean,
    activeMemberCount: Int,
    onToggleMute: () -> Unit,
    onMaximize: () -> Unit,
    onExit: () -> Unit
) {
    var offsetX by remember { mutableStateOf(40f) }
    var offsetY by remember { mutableStateOf(200f) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18101E).copy(alpha = 0.95f)),
            border = BorderStroke(1.5.dp, Brush.sweepGradient(listOf(BrandCoral, BrandGold, BrandCoral))),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(width = 135.dp, height = 135.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(10f, 650f)
                        offsetY = (offsetY + dragAmount.y).coerceIn(40f, 1500f)
                    }
                }
                .clickable { onMaximize() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header row: LIVE pill and Close / Power button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Red.copy(alpha = 0.85f), RoundedCornerShape(50))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    // Exit button
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onExit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Room",
                            tint = Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Center: Theme or Host photo with animated equalizer ring
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(listOf(BrandCoral, BrandGold, BrandCoral)))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = room.hostPhotoUrl.ifBlank { room.banner_image_url }.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                        contentDescription = room.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Room Title
                Text(
                    text = room.title.ifBlank { "Live Lounge" },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Bottom row: Viewers count and Mic toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 $activeMemberCount",
                        color = BrandGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (isOnStage) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isMicMuted) Color.Red.copy(alpha = 0.8f) else BrandGreen)
                                .clickable { onToggleMute() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Toggle Mic",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    } else {
                        // Maximize hint icon
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Maximize",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. ADVANCED AUDIO HANGOUT ROOM MAIN SCREEN / DIALOG
// -------------------------------------------------------------
@Composable
fun AudioHangoutRoomDialog(
    initialRoom: RoomItem,
    currentUser: UserProfile,
    allProfiles: List<UserProfile>,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onDirectChat: (UserProfile) -> Unit,
    isMinimized: Boolean = false,
    onMinimize: () -> Unit = {},
    onMaximize: () -> Unit = {},
    onConvertToVideo: ((RoomItem) -> Unit)? = null
) {
    val context = LocalContext.current

    // Live Room State from Firestore
    var room by remember { mutableStateOf(initialRoom) }

    if (room.id.isBlank()) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    val activeMembers = remember { mutableStateListOf<RoomMember>() }

    // Modals
    var showExitOrKeepDialog by remember { mutableStateOf(false) }
    var showTrackSelector by remember { mutableStateOf(false) }
    var showRoomSettingsDialog by remember { mutableStateOf(false) }
    var showEditRoomDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAllViewersDialog by remember { mutableStateOf(false) }
    var showShareInviteDialog by remember { mutableStateOf(false) }
    var showCommentInputDialog by remember { mutableStateOf(false) }
    var showInRoomInboxDialog by remember { mutableStateOf(false) }
    var isFollowingHost by remember { mutableStateOf(false) }
    var showFollowCelebrationDialog by remember { mutableStateOf(false) }
    var hostFollowerCount by remember { mutableStateOf(0) }
    var selectedMemberForSheet by remember { mutableStateOf<RoomMember?>(null) }
    var giftCenterRecipient by remember { mutableStateOf<RoomMember?>(null) }
    var selectedMemberForRequest by remember { mutableStateOf<RoomMember?>(null) }
    var entranceNotificationUser by remember { mutableStateOf<String?>(null) }
    val knownMemberIds = remember { mutableStateListOf<String>() }

    // Floating Draggable Music Player PIP State
    var musicPlayerOffset by remember { mutableStateOf(Offset(0f, 420f)) }
    var isMusicPlayerMinimized by remember { mutableStateOf(true) }

    val displayRoomId = remember(room.roomId, room.id) {
        val raw = room.roomId
        if (raw.isNotBlank() && raw.all { it.isDigit() } && raw.length == 8) {
            raw
        } else if (room.id.length == 8 && room.id.all { it.isDigit() }) {
            room.id
        } else {
            (10000000 + kotlin.math.abs(room.id.hashCode()) % 90000000).toString()
        }
    }

    // Auto-sync 8-digit numeric Room ID to Firestore if missing or non-8-digit
    LaunchedEffect(displayRoomId, room.id) {
        if (room.id.isBlank()) return@LaunchedEffect
        if (room.roomId != displayRoomId) {
            room = room.copy(roomId = displayRoomId)
            try {
                db.collection("rooms").document(room.id).update("roomId", displayRoomId)
                if (room.category.isNotBlank()) {
                    db.collection("intent_categories").document(room.category)
                        .collection("rooms").document(room.id).update("roomId", displayRoomId)
                }
            } catch (e: Exception) {
                Log.w("AudioHangoutRoom", "Error syncing roomId to firestore: ${e.message}")
            }
        }
    }

    // Diamond Wallet & Equipped Frame States
    var userDiamonds by remember { mutableStateOf(currentUser.diamonds) }
    var activeUserFrame by remember { mutableStateOf(currentUser.activeFrame) }
    var userOwnedFrames by remember { mutableStateOf(currentUser.ownedFrames) }
    var userActiveMicEffect by remember { mutableStateOf(currentUser.activeMicEffect) }
    var userOwnedMicEffects by remember { mutableStateOf(currentUser.ownedMicEffects) }
    var userHasRecharged by remember { mutableStateOf(currentUser.hasRecharged) }
    var userRechargeCount by remember { mutableStateOf(currentUser.rechargeCount) }
    var userTotalRoomTimeSeconds by remember { mutableStateOf(currentUser.totalRoomTimeSeconds) }
    var showGuestLimitExpiredDialog by remember { mutableStateOf(false) }
    var showGiftStoreDialog by remember { mutableStateOf(false) }
    var showLuxuryStoreDialog by remember { mutableStateOf(false) }
    var intimacyStoreItemToSend by remember { mutableStateOf<StoreItem?>(null) }
    var isSendingIntimacyStoreItem by remember { mutableStateOf(false) }
    var activeGiftEvent by remember { mutableStateOf<GiftEvent?>(null) }

    // Real-time listener for current user diamonds and active frame
    LaunchedEffect(currentUser.uid) {
        if (currentUser.uid.isNotBlank()) {
            db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snap, _ ->
                    if (snap != null && snap.exists()) {
                        userDiamonds = snap.getLong("diamonds") ?: 0L
                        activeUserFrame = snap.getString("activeFrame") ?: ""
                        userActiveMicEffect = snap.getString("activeMicEffect") ?: ""
                        userHasRecharged = snap.getBoolean("hasRecharged") ?: false
                        userRechargeCount = snap.getLong("rechargeCount")?.toInt() ?: 0
                        userTotalRoomTimeSeconds = snap.getLong("totalRoomTimeSeconds") ?: 0L
                        @Suppress("UNCHECKED_CAST")
                        userOwnedFrames = (snap.get("ownedFrames") as? List<String>) ?: emptyList()
                        @Suppress("UNCHECKED_CAST")
                        userOwnedMicEffects = (snap.get("ownedMicEffects") as? List<String>) ?: emptyList()
                    }
                }
        }
    }

    // Live Room Time Tracking & Free 1-Hour Guest Limit for Unrecharged Users
    LaunchedEffect(currentUser.uid, userHasRecharged, userRechargeCount, userDiamonds, currentUser.diamonds) {
        val isFirstTimeFreeGuest = !userHasRecharged && !currentUser.hasRecharged
            && userRechargeCount == 0 && currentUser.rechargeCount == 0
            && userDiamonds <= 0L && currentUser.diamonds <= 0L
            && currentUser.getVipLevel() == 0 && !currentUser.isVipOrPlatinum()
            && !currentUser.isFounder() && !currentUser.isCeo() && !currentUser.isStaffMember()
            && room.hostId != currentUser.uid && room.hostUid != currentUser.uid

        while (true) {
            delay(10000L) // every 10 seconds
            val newTime = userTotalRoomTimeSeconds + 10L
            userTotalRoomTimeSeconds = newTime
            try {
                db.collection("users").document(currentUser.uid)
                    .update("totalRoomTimeSeconds", FieldValue.increment(10L))
            } catch (_: Exception) {}

            AutomatedRewardManager.checkRoomTimeRewards(
                context = context,
                profile = currentUser.copy(totalRoomTimeSeconds = newTime, diamonds = userDiamonds),
                db = db,
                onUpdateProfile = { userDiamonds = it.diamonds }
            )

            // STRICTLY apply 1-hour limit ONLY to first-time free guests with 0 coins, 0 recharge, no VIP
            if (isFirstTimeFreeGuest && newTime >= 3600L) {
                showGuestLimitExpiredDialog = true
                break
            }
        }
    }

    // Real-time listener for room video gift events
    LaunchedEffect(room.id) {
        if (room.id.isBlank()) return@LaunchedEffect
        db.collection("rooms").document(room.id)
            .collection("gift_events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val latest = snapshot.documents[0].toObject(GiftEvent::class.java)
                    if (latest != null) {
                        val now = System.currentTimeMillis()
                        if (now - latest.timestamp < 15000L && latest.id != activeGiftEvent?.id) {
                            activeGiftEvent = latest
                        }
                    }
                }
            }
    }

    // 5-Mic Live Stage States
    val micSeats = remember { mutableStateListOf<MicSeat>() }
    var selectedSeatForAdmin by remember { mutableStateOf<MicSeat?>(null) }
    var showInviteListenersDialog by remember { mutableStateOf<MicSeat?>(null) }
    var selectedOwnSeatForControl by remember { mutableStateOf<MicSeat?>(null) }
    var isLocalTalking by remember { mutableStateOf(false) }
    var pendingSeatToJoin by remember { mutableStateOf<MicSeat?>(null) }
    var showEmojiTray by remember { mutableStateOf(false) }
    var activeMicLinkDesignId by remember { mutableStateOf("mic_link_ruby_crown") }
    var showMicLinkSelectorDialog by remember { mutableStateOf(false) }
    val activeEmojiReactions = remember { mutableStateMapOf<String, String>() }

    // Real-time listener for live floating emoji reactions
    LaunchedEffect(room.id) {
        if (room.id.isBlank()) return@LaunchedEffect
        db.collection("rooms").document(room.id)
            .collection("emoji_reactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val now = System.currentTimeMillis()
                    for (doc in snapshot.documents) {
                        val reaction = doc.toObject(LiveEmojiReaction::class.java)
                        if (reaction != null && (now - reaction.timestamp) < 5000L && reaction.userId.isNotBlank()) {
                            activeEmojiReactions[reaction.userId] = reaction.emoji
                        }
                    }
                }
            }
    }

    // Real-time listener for active Intimacy Bonds between seated users
    val activeIntimacyBonds = remember { mutableStateListOf<IntimacyBond>() }
    DisposableEffect(room.id) {
        if (room.id.isBlank()) return@DisposableEffect onDispose {}
        val listener = db.collection("intimacy_bonds")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AudioHangoutRoom", "Failed to listen for active intimacy bonds", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bonds = snapshot.documents.mapNotNull { it.toObject(IntimacyBond::class.java) }
                    activeIntimacyBonds.clear()
                    activeIntimacyBonds.addAll(bonds)

                    // Auto-ignite link connector if two seated users share an active bond
                    val seatedUids = micSeats.map { it.uid }.filter { it.isNotBlank() }
                    for (bond in bonds) {
                        if (seatedUids.contains(bond.user1Id) && seatedUids.contains(bond.user2Id)) {
                            activeMicLinkDesignId = bond.activeMicLinkId.ifBlank { "mic_link_ruby_crown" }
                            break
                        }
                    }
                }
            }
        onDispose { listener.remove() }
    }

    // Real-Time Listener for Incoming Intimacy Gifting Proposals
    var pendingIntimacyRequest by remember { mutableStateOf<Map<String, Any>?>(null) }
    DisposableEffect(currentUser.uid, room.id) {
        if (currentUser.uid.isBlank() || room.id.isBlank()) return@DisposableEffect onDispose {}
        val listener = db.collection("intimacy_requests")
            .whereEqualTo("recipientUid", currentUser.uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, err ->
                if (err != null) {
                    Log.e("AudioHangoutRoom", "Failed to listen for intimacy requests", err)
                    return@addSnapshotListener
                }
                pendingIntimacyRequest = snapshot?.documents
                    ?.firstOrNull { it.getString("roomId") == room.id }
                    ?.data
            }
        onDispose { listener.remove() }
    }

    fun triggerEmojiReaction(emoji: String) {
        if (room.id.isBlank() || currentUser.uid.isBlank()) return
        val reaction = LiveEmojiReaction(
            id = UUID.randomUUID().toString(),
            userId = currentUser.uid,
            userName = currentUser.name,
            userPhoto = currentUser.photo,
            emoji = emoji,
            timestamp = System.currentTimeMillis()
        )
        activeEmojiReactions[currentUser.uid] = emoji
        db.collection("rooms").document(room.id)
            .collection("emoji_reactions").document(reaction.id)
            .set(reaction)
    }

    // PK Battle Arena Real-Time State
    val pkBlueSupporters = remember { mutableStateListOf<PkSupporter>() }
    val pkRedSupporters = remember { mutableStateListOf<PkSupporter>() }
    var selectedPkTeamForBoost by remember { mutableStateOf("blue") }

    LaunchedEffect(room.id, room.isPkActive) {
        if (room.id.isBlank() || !room.isPkActive) return@LaunchedEffect
        db.collection("rooms").document(room.id)
            .collection("pk_supporters")
            .orderBy("points", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val team = doc.getString("team") ?: "blue"
                        val uid = doc.getString("uid") ?: ""
                        val name = doc.getString("name") ?: ""
                        val photo = doc.getString("photo") ?: ""
                        val points = doc.getLong("points") ?: 0L
                        Pair(team, PkSupporter(uid = uid, name = name, photo = photo, points = points))
                    }
                    pkBlueSupporters.clear()
                    pkBlueSupporters.addAll(list.filter { it.first == "blue" }.map { it.second }.take(3))
                    pkRedSupporters.clear()
                    pkRedSupporters.addAll(list.filter { it.first == "red" }.map { it.second }.take(3))
                }
            }
    }

    fun startPkBattleWithOpponent(opponentUid: String, opponentName: String, opponentPhoto: String) {
        val now = System.currentTimeMillis()
        val curHostId = room.hostUid.ifBlank { room.hostId }
        val curHostName = room.hostName.ifBlank { "Host" }
        val curHostPhoto = room.hostPhotoUrl
        val updates = mapOf<String, Any>(
            "isPkActive" to true,
            "pkHost1Uid" to curHostId,
            "pkHost1Name" to curHostName,
            "pkHost1Photo" to curHostPhoto,
            "pkHost1Score" to 0L,
            "pkHost2Uid" to opponentUid,
            "pkHost2Name" to opponentName,
            "pkHost2Photo" to opponentPhoto,
            "pkHost2Score" to 0L,
            "pkStartTime" to now,
            "pkDurationSec" to 300,
            "pkStatus" to "active",
            "pkWinnerUid" to ""
        )
        db.collection("rooms").document(room.id).update(updates)
        db.collection("rooms").document(room.id).collection("pk_supporters").get().addOnSuccessListener { snaps ->
            for (d in snaps.documents) {
                d.reference.delete()
            }
        }
        val pkNotice = LiveRoomComment(
            id = UUID.randomUUID().toString(),
            roomId = room.id,
            userId = "system_notice",
            userName = "⚔️ PK ARENA",
            userPhoto = curHostPhoto,
            tier = "VIP",
            message = "🔥 PK BATTLE STARTED! $curHostName [BLUE] VS $opponentName [RED]! Send gifts to support!",
            timestamp = now
        )
        db.collection("rooms").document(room.id).collection("live_comments").document(pkNotice.id).set(pkNotice)
        Toast.makeText(context, "⚔️ 5-Minute PK Battle Started! Let's Go! 🔥", Toast.LENGTH_LONG).show()
    }

    fun endPkBattle() {
        val curHostPhoto = room.hostPhotoUrl
        val winnerUid = if (room.pkHost1Score > room.pkHost2Score) room.pkHost1Uid
                        else if (room.pkHost2Score > room.pkHost1Score) room.pkHost2Uid
                        else ""
        val winnerName = if (winnerUid == room.pkHost1Uid) room.pkHost1Name else if (winnerUid == room.pkHost2Uid) room.pkHost2Name else "Draw"
        val updates = mapOf<String, Any>(
            "isPkActive" to false,
            "pkStatus" to "ended",
            "pkWinnerUid" to winnerUid
        )
        db.collection("rooms").document(room.id).update(updates)

        // Real-Time PK Battle Win Award & Swords Level Progression
        if (winnerUid.isNotBlank() && winnerUid != "guest_challenger") {
            val winScore = if (winnerUid == room.pkHost1Uid) room.pkHost1Score else room.pkHost2Score
            val userRef = db.collection("users").document(winnerUid)
            userRef.get().addOnSuccessListener { winSnap ->
                if (winSnap != null && winSnap.exists()) {
                    val currentWins = (winSnap.getLong("pkWins") ?: 0L).toInt() + 1
                    val currentScore = (winSnap.getLong("pkScore") ?: 0L) + winScore
                    val newPkLvl = LevelBadgeSystem.calculatePkLevel(currentWins)
                    userRef.update(
                        mapOf(
                            "pkWins" to currentWins,
                            "pkScore" to currentScore,
                            "pkLevel" to newPkLvl
                        )
                    )
                }
            }
        }

        val endNotice = LiveRoomComment(
            id = UUID.randomUUID().toString(),
            roomId = room.id,
            userId = "system_notice",
            userName = "⚔️ PK ARENA",
            userPhoto = curHostPhoto,
            tier = "VIP",
            message = "🏆 PK Battle Finished! Winner: $winnerName! Amazing fight!",
            timestamp = System.currentTimeMillis()
        )
        db.collection("rooms").document(room.id).collection("live_comments").document(endNotice.id).set(endNotice)
        Toast.makeText(context, "🛑 PK Battle Ended", Toast.LENGTH_SHORT).show()
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingSeatToJoin?.let { seat ->
                joinMicSeat(db, room.id, seat.seatIndex, currentUser, micSeats)
                Toast.makeText(context, "You are now on Mic ${seat.seatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                pendingSeatToJoin = null
            }
        } else {
            if (pendingSeatToJoin != null) {
                Toast.makeText(context, "Microphone permission is required to speak on stage", Toast.LENGTH_SHORT).show()
                pendingSeatToJoin = null
            }
        }
    }

    // Proactively request RECORD_AUDIO permission upon entering room so microphone is authorized
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(activeMembers.size) {
        if (knownMemberIds.isEmpty()) {
            knownMemberIds.addAll(activeMembers.map { it.uid })
        } else {
            val newArrivals = activeMembers.filter { !knownMemberIds.contains(it.uid) && it.uid != currentUser.uid }
            if (newArrivals.isNotEmpty()) {
                val newest = newArrivals.last()
                entranceNotificationUser = newest.name
                knownMemberIds.addAll(newArrivals.map { it.uid })
                delay(3500)
                entranceNotificationUser = null
            }
        }
    }

    val hostCandidateId = room.hostUid.ifBlank { room.hostId }
    val isUserHost = currentUser.uid.isNotBlank() && (currentUser.uid == hostCandidateId)
    val isUserAdmin = isUserHost || (currentUser.uid.isNotBlank() && currentUser.uid in room.adminUserIds)
    val isMuted by AudioPlayerManager.isMutedState
    val localPlaying by AudioPlayerManager.isPlayingState

    val executeHostOrUserExit: () -> Unit = {
        if (isUserHost) {
            val hasAdminInRoom = activeMembers.any { it.uid != currentUser.uid && it.uid in room.adminUserIds }
            if (hasAdminInRoom) {
                val adminManagedUpdates = mapOf<String, Any>(
                    "isHostLive" to false,
                    "statusMessage" to "Host stepped away • Room managed by Admin"
                )
                db.collection("rooms").document(room.id).update(adminManagedUpdates)
                if (room.category.isNotBlank()) {
                    db.collection("intent_categories").document(room.category)
                        .collection("rooms").document(room.id).update(adminManagedUpdates)
                }
                db.collection("rooms").document(room.id).collection("mic_seats").document("0")
                    .set(
                        MicSeat(
                            seatIndex = 0,
                            label = "Host 👑",
                            uid = "",
                            name = "",
                            photo = "",
                            role = "host"
                        )
                    )
                Toast.makeText(context, "Host stepped away. Room remains live under Admin supervision! 🛡️", Toast.LENGTH_SHORT).show()
            } else {
                val closeUpdates = mapOf<String, Any>(
                    "isLive" to false,
                    "isHostLive" to false,
                    "isEnded" to true,
                    "endedAt" to System.currentTimeMillis(),
                    "statusMessage" to "Room session closed by host",
                    "isPlaying" to false
                )
                db.collection("rooms").document(room.id).update(closeUpdates)
                if (room.category.isNotBlank()) {
                    db.collection("intent_categories").document(room.category)
                        .collection("rooms").document(room.id).update(closeUpdates)
                }
                // Clear members & all mic seats so no ghost viewers or speakers persist across sessions
                db.collection("rooms").document(room.id).collection("members").get()
                    .addOnSuccessListener { snaps ->
                        for (doc in snaps.documents) {
                            doc.reference.delete()
                        }
                    }
                db.collection("rooms").document(room.id).collection("mic_seats").get()
                    .addOnSuccessListener { snaps ->
                        for (doc in snaps.documents) {
                            doc.reference.delete()
                        }
                    }
                // Clear live_comments so comments are wiped completely on room close
                db.collection("rooms").document(room.id).collection("live_comments").get()
                    .addOnSuccessListener { snaps ->
                        if (snaps != null && !snaps.isEmpty) {
                            val batch = db.batch()
                            for (doc in snaps.documents) {
                                batch.delete(doc.reference)
                            }
                            batch.commit()
                        }
                    }
                Toast.makeText(context, "Room session closed. 👋", Toast.LENGTH_SHORT).show()
                onDismiss()
            }
        } else {
            val mySeat = micSeats.find { it.uid == currentUser.uid }
            if (mySeat != null) {
                vacateMicSeat(db, room.id, mySeat.seatIndex, mySeat.isLocked)
            }
        }
        onDismiss()
    }

    BackHandler {
        showExitOrKeepDialog = true
    }

    // Proactively activate room and host presence in Firestore when host enters
    LaunchedEffect(isUserHost, room.id) {
        if (room.id.isNotBlank() && isUserHost) {
            val hostUpdates = mutableMapOf<String, Any>(
                "isLive" to true,
                "isHostLive" to true,
                "isEnded" to false,
                "endedAt" to 0L,
                "statusMessage" to "LIVE NOW"
            )
            if (room.hostId.isBlank() || room.hostId == currentUser.uid) {
                hostUpdates["hostId"] = currentUser.uid
                hostUpdates["hostUid"] = currentUser.uid
                hostUpdates["hostName"] = currentUser.name
                hostUpdates["hostPhotoUrl"] = currentUser.photo
            }
            db.collection("rooms").document(room.id).update(hostUpdates)
            if (room.category.isNotBlank()) {
                db.collection("intent_categories").document(room.category)
                    .collection("rooms").document(room.id).update(hostUpdates)
            }
            // Purge stale/ghost members left behind from previous closed sessions
            val now = System.currentTimeMillis()
            db.collection("rooms").document(room.id).collection("members").get()
                .addOnSuccessListener { snaps ->
                    for (doc in snaps.documents) {
                        val joinedAt = doc.getLong("joinedAt") ?: 0L
                        if (doc.id != currentUser.uid && (joinedAt == 0L || (now - joinedAt > 7_200_000L))) {
                            doc.reference.delete()
                        }
                    }
                }
            // Purge stale live comments from previous sessions when host starts/re-opens room
            db.collection("rooms").document(room.id).collection("live_comments").get()
                .addOnSuccessListener { snaps ->
                    if (snaps != null && !snaps.isEmpty) {
                        val batch = db.batch()
                        for (doc in snaps.documents) {
                            batch.delete(doc.reference)
                        }
                        batch.commit()
                    }
                }
        }
    }

    // Automated Room Entry Welcome Message in Live Comments
    LaunchedEffect(room.id, currentUser.uid) {
        if (room.id.isBlank()) return@LaunchedEffect
        if (isUserHost) {
            delay(500)
        }
        val isFounder = currentUser.isFounder()
        val isCeo = currentUser.isCeo()
        val currentVipLvl = currentUser.getVipLevel()
        val welcomeDoc = LiveRoomComment(
            id = UUID.randomUUID().toString(),
            roomId = room.id,
            userId = currentUser.uid,
            userName = currentUser.name,
            userPhoto = currentUser.photo,
            tier = if (currentVipLvl > 0) "VIP $currentVipLvl" else if (currentUser.isVipOrPlatinum()) "Platinum" else "Standard",
            message = when {
                currentVipLvl == 4 -> "VIP 4 Celestial Dragon Sovereign has arrived in Celestial Chariot! 🐉✨"
                currentVipLvl == 3 -> "VIP 3 Imperial Amethyst Monarch has arrived in Royal Carriage! 🔮✨"
                currentVipLvl == 2 -> "VIP 2 Solar Phoenix Paladin has arrived in Golden Chariot! ☀️✨"
                currentVipLvl == 1 -> "VIP 1 Frost Silver Knight has arrived in Platinum Chariot! ❄️✨"
                isFounder -> "LiveTogather Founder has entered the room. Supreme Authority Active."
                isCeo -> "LiveTogather CEO has entered the room. Supreme Leadership Active."
                else -> "entered the room"
            },
            timestamp = System.currentTimeMillis(),
            chatFrameId = currentUser.activeChatFrame,
            vipLevel = currentVipLvl,
            isFounder = isFounder
        )
        db.collection("rooms").document(room.id)
            .collection("live_comments").document(welcomeDoc.id)
            .set(welcomeDoc)
    }

    // Kick & Ban Auto-Dismissal Enforcement (Real-time Firestore sync)
    LaunchedEffect(room.bannedUserIds, room.kickedUserIds) {
        if (room.bannedUserIds.contains(currentUser.uid)) {
            Toast.makeText(context, "You have been banned from this room by the host or admin.", Toast.LENGTH_LONG).show()
            onDismiss()
        } else if (room.kickedUserIds.contains(currentUser.uid)) {
            Toast.makeText(context, "You have been kicked out of this room.", Toast.LENGTH_LONG).show()
            onDismiss()
        }
    }

    val currentMember = remember(currentUser.uid, currentUser.userId, room.id, isUserHost) {
        RoomMember(
            uid = currentUser.uid,
            userId = currentUser.userId,
            name = currentUser.name,
            photoUrl = currentUser.photo,
            joinedAt = System.currentTimeMillis(),
            role = if (isUserHost) "host" else "listener",
            tier = if (currentUser.getVipLevel() > 0) "VIP" else if (currentUser.isVipOrPlatinum()) "Platinum" else "Standard"
        )
    }

    // Immediately guarantee currentUser is present locally in activeMembers
    LaunchedEffect(currentMember) {
        if (activeMembers.none { it.uid == currentUser.uid }) {
            activeMembers.add(currentMember)
        }
    }

    // Ensure current user is registered in rooms/{roomId}/members in Firestore, Agora Voice joined and cleaned up on exit
    DisposableEffect(displayRoomId, currentUser.uid) {
        if (room.id.isBlank()) return@DisposableEffect onDispose {}
        db.collection("rooms").document(room.id)
            .collection("members").document(currentUser.uid).set(currentMember)

        val agoraUid = (currentUser.userId.toIntOrNull() ?: (currentUser.uid.hashCode() and 0x7FFFFFFF)).coerceAtLeast(1)
        val initialMute = !isUserHost
        com.devil.livetogather.agora.AgoraVoiceManager.joinRoom(
            context = context,
            roomId = displayRoomId,
            uid = agoraUid,
            startMuted = initialMute
        )

        onDispose {
            if (room.id.isNotBlank()) {
                db.collection("rooms").document(room.id)
                    .collection("members").document(currentUser.uid).delete()
            }
            com.devil.livetogather.agora.AgoraVoiceManager.leaveRoom()
        }
    }

    val myActiveMicSeat = micSeats.find { it.uid == currentUser.uid }
    val isUserOnStage = if (isUserHost) true else (myActiveMicSeat != null)
    val isMicMutedState = if (isUserHost) (myActiveMicSeat?.isMuted ?: false) else (myActiveMicSeat?.isMuted ?: true)

    LaunchedEffect(isUserOnStage, isMicMutedState) {
        val shouldMute = !isUserOnStage || isMicMutedState
        com.devil.livetogather.agora.AgoraVoiceManager.muteLocalAudio(shouldMute)
    }

    val targetHostId = hostCandidateId.ifBlank { activeMembers.firstOrNull { it.role == "host" }?.uid ?: if (isUserHost) currentUser.uid else "" }
    val hostMember = activeMembers.find { it.uid == targetHostId || it.role == "host" }
    val hostFromProfiles = allProfiles.find { it.uid == targetHostId }
    val hostName = room.hostName.ifBlank { hostMember?.name ?: hostFromProfiles?.name ?: if (isUserHost) currentUser.name else "Host" }
    val hostPhoto = room.hostPhotoUrl.ifBlank { "" }
        .ifEmpty { hostMember?.photoUrl.orEmpty() }
        .ifEmpty { hostFromProfiles?.photo.orEmpty() }
        .ifEmpty { if (isUserHost) currentUser.photo else "" }
        .ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80" }

    val hostMemberObj = remember(targetHostId, hostName, hostPhoto, hostMember) {
        hostMember ?: RoomMember(
            uid = targetHostId.ifEmpty { "host" },
            name = hostName,
            photoUrl = hostPhoto,
            role = "host"
        )
    }

    val filteredActiveAudience = activeMembers.filter {
        it.uid != targetHostId && it.uid !in room.kickedUserIds && it.uid !in room.bannedUserIds
    }
    val displayAudience = filteredActiveAudience
    val totalDisplayCount = (if (hostName.isNotBlank() || targetHostId.isNotBlank()) 1 else 0) + displayAudience.size

    // Synchronize host photo and name to room document if not yet saved in Firestore
    LaunchedEffect(room.id, hostPhoto, isUserHost) {
        if (room.id.isBlank()) return@LaunchedEffect
        if (room.hostPhotoUrl.isBlank() && hostPhoto.isNotBlank() && isUserHost) {
            db.collection("rooms").document(room.id).update(
                mapOf(
                    "hostPhotoUrl" to hostPhoto,
                    "hostName" to hostName
                )
            )
        }
    }

    // Host Follower Real-Time Listener
    DisposableEffect(targetHostId, currentUser.uid) {
        if (targetHostId.isBlank() || targetHostId == currentUser.uid) {
            return@DisposableEffect onDispose {}
        }
        val followDoc = db.collection("users").document(targetHostId)
            .collection("followers").document(currentUser.uid)
            .addSnapshotListener { snapshot, _ ->
                isFollowingHost = snapshot != null && snapshot.exists()
            }
        val countListener = db.collection("users").document(targetHostId)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    hostFollowerCount = snapshot.size()
                }
            }
        onDispose {
            followDoc.remove()
            countListener.remove()
        }
    }

    // 1. Real-Time Room Metadata Listener (Rooms/{roomId})
    DisposableEffect(room.id) {
        if (room.id.isBlank()) return@DisposableEffect onDispose {}
        val roomListener = db.collection("rooms").document(room.id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val updated = parseRoomFromDoc(snapshot)
                    // Real-Time Mode Conversion Check (Audio -> Video)
                    if (updated.roomType == "video" || updated.roomType == "video_pk" || updated.isVideoRoom) {
                        com.devil.livetogather.agora.AgoraVoiceManager.leaveRoom()
                        onConvertToVideo?.invoke(updated)
                        return@addSnapshotListener
                    }
                    val isLive = snapshot.getBoolean("isLive") ?: (snapshot.getBoolean("isHostLive") ?: updated.isLive)
                    val isEnded = snapshot.getBoolean("isEnded") ?: false
                    val isHostLive = snapshot.getBoolean("isHostLive") ?: updated.isHostLive
                    val endedAt = snapshot.getLong("endedAt") ?: 0L
                    val hasAdminPresent = activeMembers.any { it.uid != room.hostId && it.uid in room.adminUserIds }
                    val isRoomClosed = !isLive || isEnded || (!isHostLive && !hasAdminPresent)

                    if (isRoomClosed && !isUserHost) {
                        Toast.makeText(context, "Room is closed • Host is offline", Toast.LENGTH_LONG).show()
                        AudioPlayerManager.pause()
                        onDismiss()
                        return@addSnapshotListener
                    }
                    room = updated

                    // Audio Sync: If room has track and is playing
                    if (updated.isPlaying && updated.nowPlayingUrl.isNotBlank()) {
                        val trackStartedAt = snapshot.getLong("trackStartedAt") ?: updated.trackStartedAt
                        val elapsedMs = if (trackStartedAt > 0L) {
                            (System.currentTimeMillis() - trackStartedAt).toInt().coerceAtLeast(0)
                        } else {
                            updated.playbackPosition.toInt().coerceAtLeast(0)
                        }

                        if (AudioPlayerManager.currentTrackUrlState.value != updated.nowPlayingUrl || !AudioPlayerManager.isPlayingState.value) {
                            AudioPlayerManager.play(context, updated.nowPlayingUrl, updated.nowPlayingTitle, updated.nowPlayingArtworkUrl, initialSeekMs = elapsedMs)
                        } else {
                            val currentLocalPos = AudioPlayerManager.currentPositionMsState.value
                            val dur = AudioPlayerManager.durationMsState.value
                            val targetPos = if (dur > 0) (elapsedMs % dur) else elapsedMs
                            if (kotlin.math.abs(currentLocalPos - targetPos) > 4000) {
                                AudioPlayerManager.seekTo(targetPos)
                            }
                        }
                    } else if (!updated.isPlaying && AudioPlayerManager.isPlayingState.value) {
                        AudioPlayerManager.pause()
                    }
                } else if (snapshot != null && !snapshot.exists() && !isUserHost && (System.currentTimeMillis() - room.createdAt > 15_000L)) {
                    Toast.makeText(context, "This room is no longer active", Toast.LENGTH_SHORT).show()
                    AudioPlayerManager.pause()
                    onDismiss()
                }
            }

        onDispose {
            roomListener.remove()
        }
    }

    // 2. Real-Time Members Presence Listener (Rooms/{roomId}/members)
    DisposableEffect(room.id) {
        if (room.id.isBlank()) return@DisposableEffect onDispose {}
        val membersListener = db.collection("rooms").document(room.id)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val now = System.currentTimeMillis()
                    val list = snapshot.documents.mapNotNull { doc ->
                        val m = doc.toObject(RoomMember::class.java)
                        if (m != null) {
                            if (m.joinedAt > 0L && (now - m.joinedAt > 7_200_000L)) {
                                if (isUserHost) {
                                    doc.reference.delete()
                                }
                                null
                            } else {
                                m
                            }
                        } else null
                    }
                    activeMembers.clear()
                    activeMembers.addAll(list)
                    if (activeMembers.none { it.uid == currentUser.uid }) {
                        activeMembers.add(currentMember)
                    }
                }
            }

        onDispose {
            membersListener.remove()
        }
    }

    var commentInputText by remember { mutableStateOf("") }
    val liveComments = remember { mutableStateListOf<LiveRoomComment>() }
    val roomSessionStartMs = remember(room.id) { System.currentTimeMillis() - 2000L }

    // 3. Real-Time Live Comments Listener (Rooms/{roomId}/live_comments)
    DisposableEffect(room.id) {
        if (room.id.isBlank()) return@DisposableEffect onDispose {}
        val commentsListener = db.collection("rooms").document(room.id)
            .collection("live_comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(25)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(LiveRoomComment::class.java) }

                        // Show all live comments in the active room

                    liveComments.clear()
                    liveComments.addAll(list)
                }
            }
        onDispose {
            commentsListener.remove()
            if (false) {
                db.collection("rooms").document(room.id).collection("live_comments").get()
                    .addOnSuccessListener { snaps ->
                        if (snaps != null && !snaps.isEmpty) {
                            val batch = db.batch()
                            for (doc in snaps.documents) {
                                batch.delete(doc.reference)
                            }
                            batch.commit()
                        }
                    }
            }
        }
    }

    // 4. Real-Time 10-Mic Seats Listener (rooms/{roomId}/mic_seats) - PURE READ-ONLY
    DisposableEffect(room.id) {
        if (room.id.isBlank()) return@DisposableEffect onDispose {}
        val micSeatsRef = db.collection("rooms").document(room.id).collection("mic_seats")
        val micListener = micSeatsRef.orderBy("seatIndex", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val seats = snapshot.documents.mapNotNull { doc ->
                        val s = doc.toObject(MicSeat::class.java)
                        if (s != null) {
                            val rawMuted = doc.getBoolean("isMuted") ?: doc.getBoolean("muted") ?: s.isMuted
                            val rawLocked = doc.getBoolean("isLocked") ?: doc.getBoolean("locked") ?: s.isLocked
                            val rawTalking = doc.getBoolean("isTalking") ?: doc.getBoolean("talking") ?: s.isTalking
                            s.copy(isMuted = rawMuted, isLocked = rawLocked, isTalking = rawTalking)
                        } else null
                    }

                    val fullList = (0..11).map { idx ->
                        seats.find { it.seatIndex == idx } ?: MicSeat(
                            seatIndex = idx,
                            label = if (idx == 0) "Host 👑" else "Mic ${idx + 1}",
                            uid = if (idx == 0 && targetHostId.isNotEmpty()) targetHostId else "",
                            name = if (idx == 0) hostName else "",
                            photo = if (idx == 0) hostPhoto else "",
                            role = if (idx == 0) "host" else "speaker"
                        )
                    }

                    // Real-time seat validation: instant cleanup for departed, offline, or dummy users
                    val seenUids = mutableSetOf<String>()
                    val cleanedList = fullList.map { seat ->
                        val isDummy = seat.uid.startsWith("companion_") || seat.uid == "companion_maya_vip" || seat.name.contains("Maya (Bestie", true)
                        val isOccupantActive = seat.uid.isBlank() || (!isDummy && (
                            seat.uid == targetHostId ||
                            seat.uid == currentUser.uid ||
                            activeMembers.any { it.uid == seat.uid }
                        ))
                        if (seat.uid.isNotBlank() && (!isOccupantActive || isDummy)) {
                            // Seat occupant is offline, departed, or dummy -> immediately clear and free mic seat
                            if (isUserHost || isUserAdmin || seat.uid == currentUser.uid || isDummy) {
                                vacateMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                            }
                            seat.copy(uid = "", name = "", photo = "", isTalking = false, isMuted = false)
                        } else if (seat.uid.isNotBlank()) {
                            if (seenUids.contains(seat.uid)) {
                                seat.copy(uid = "", name = "", photo = "", isTalking = false, isMuted = false)
                            } else {
                                seenUids.add(seat.uid)
                                seat
                            }
                        } else {
                            seat
                        }
                    }

                    micSeats.clear()
                    micSeats.addAll(cleanedList)
                }
            }

        onDispose {
            micListener.remove()
            val mySeat = micSeats.find { it.uid == currentUser.uid }
            if (mySeat != null) {
                micSeatsRef.document(mySeat.seatIndex.toString()).set(
                    MicSeat(
                        seatIndex = mySeat.seatIndex,
                        label = "Mic ${mySeat.seatIndex + 1}",
                        uid = "",
                        name = "",
                        photo = "",
                        isMuted = false,
                        isLocked = mySeat.isLocked,
                        isTalking = false,
                        role = "speaker"
                    )
                )
            }
        }
    }

    // Host-only one-time initialization of 12 mic seats (Clean, outside of snapshot listener)
    LaunchedEffect(isUserHost, room.id) {
        if (!isUserHost || room.id.isBlank()) return@LaunchedEffect
        try {
            val micSeatsRef = db.collection("rooms").document(room.id).collection("mic_seats")
            micSeatsRef.get().addOnSuccessListener { snap ->
                if (snap != null && snap.isEmpty) {
                    val defaultSeats = (0..11).map { idx ->
                        if (idx == 0) {
                            MicSeat(seatIndex = 0, label = "Host 👑", uid = currentUser.uid, name = currentUser.name, photo = currentUser.photo, role = "host")
                        } else {
                            MicSeat(seatIndex = idx, label = "Mic ${idx + 1}", uid = "", name = "", photo = "", role = "speaker")
                        }
                    }
                    defaultSeats.forEach { s ->
                        micSeatsRef.document(s.seatIndex.toString()).set(s)
                    }
                } else if (snap != null) {
                    val existingIndices = snap.documents.mapNotNull { it.getLong("seatIndex")?.toInt() }.toSet()
                    for (idx in 0..11) {
                        if (!existingIndices.contains(idx)) {
                            val newSeat = if (idx == 0) {
                                MicSeat(seatIndex = 0, label = "Host 👑", uid = currentUser.uid, name = currentUser.name, photo = currentUser.photo, role = "host")
                            } else {
                                MicSeat(seatIndex = idx, label = "Mic ${idx + 1}", uid = "", name = "", photo = "", role = "speaker")
                            }
                            micSeatsRef.document(idx.toString()).set(newSeat)
                        }
                    }
                    val seat0Doc = snap.documents.find { it.getLong("seatIndex")?.toInt() == 0 }
                    val seat0Uid = seat0Doc?.getString("uid") ?: ""
                    if (seat0Uid.isBlank() || seat0Uid != currentUser.uid) {
                        micSeatsRef.document("0").set(
                            MicSeat(
                                seatIndex = 0,
                                label = "Host 👑",
                                uid = currentUser.uid,
                                name = currentUser.name,
                                photo = currentUser.photo,
                                isMuted = false,
                                isLocked = false,
                                isTalking = false,
                                role = "host"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("AudioHangoutRoom", "Error seeding mic seats: ${e.message}")
        }
    }

    // Speech volume animation for current user via Agora's native volume engine (no mic hardware contention)
    val myCurrentMicSeat = micSeats.find { it.uid == currentUser.uid }
    val isCurrentUserActiveSpeaker = if (isUserHost) !isMicMutedState else (myCurrentMicSeat != null && !myCurrentMicSeat.isMuted)

    DisposableEffect(isCurrentUserActiveSpeaker) {
        if (!isCurrentUserActiveSpeaker) {
            isLocalTalking = false
            com.devil.livetogather.agora.AgoraVoiceManager.onLocalTalkingStateChanged = null
            onDispose { }
        } else {
            com.devil.livetogather.agora.AgoraVoiceManager.onLocalTalkingStateChanged = { talking ->
                if (isLocalTalking != talking) {
                    isLocalTalking = talking
                }
            }
            onDispose {
                com.devil.livetogather.agora.AgoraVoiceManager.onLocalTalkingStateChanged = null
                isLocalTalking = false
            }
        }
    }

    // Sync speech detection to Firestore for remote participants
    LaunchedEffect(isLocalTalking, myCurrentMicSeat?.seatIndex) {
        if (room.id.isBlank()) return@LaunchedEffect
        val seatIdx = myCurrentMicSeat?.seatIndex ?: return@LaunchedEffect
        try {
            db.collection("rooms").document(room.id).collection("mic_seats").document(seatIdx.toString())
                .update("isTalking", isLocalTalking)
        } catch (e: Exception) {}
    }

    var isVipBypassed by remember { mutableStateOf(false) }
    val isVipRoomCategory = (room.category == "vip_room" || room.isVipRoom) && !isUserHost && !isVipBypassed
    val isUserVip = currentUser.isVipOrPlatinum() || currentUser.getVipLevel() >= 1

    if (isVipRoomCategory && !isUserVip) {
        Dialog(
            onDismissRequest = { isVipBypassed = true },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF191224)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, BrandGold.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(listOf(BrandGold, Color(0xFFFFA000), BrandGold)))
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "VIP Audio Lounge",
                        color = BrandGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Welcome to the VIP Royal Audio Lounge! Experience the luxury gramophone console, ambient chandelier decor, and high-fidelity stage seats.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { isVipBypassed = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Enter Lounge 👑", color = BrandDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        return
    }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    if (isMinimized) {
        val mySeat = micSeats.find { it.uid == currentUser.uid }
        val isSelfOnStage = mySeat != null
        val isSelfMicMuted = mySeat?.isMuted ?: true
        FloatingAudioRoomPIP(
            room = room,
            currentUser = currentUser,
            isHost = isUserHost,
            isMicMuted = isSelfMicMuted,
            isOnStage = isSelfOnStage,
            activeMemberCount = activeMembers.size,
            onToggleMute = {
                if (isSelfOnStage && mySeat != null) {
                    toggleMuteMicSeat(db, room.id, mySeat.seatIndex, isSelfMicMuted)
                }
            },
            onMaximize = onMaximize,
            onExit = {
                executeHostOrUserExit()
            }
        )
        return
    }

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                showExitOrKeepDialog = true
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent = view.parent
            while (parent != null && parent !is android.app.Dialog && parent !is DialogWindowProvider) {
                parent = parent.parent
            }
            (parent as? DialogWindowProvider)?.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0C10))
        ) {
            // Ambient Luxury Wallpaper in Background (Full Room Ambiance - 100% Edge-to-Edge)
            val isVipRoom = room.isVipRoom || room.category.equals("vip_room", ignoreCase = true) || room.title.contains("VIP", ignoreCase = true) || room.privacy.equals("vip_only", ignoreCase = true) || room.banner_image_url == "vip_royal_stage"
            if (isVipRoom) {
                Image(
                    painter = painterResource(id = R.drawable.vip_royal_stage),
                    contentDescription = "VIP Royal Stage Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = if (room.banner_image_url == "vip_royal_stage") R.drawable.vip_royal_stage else room.banner_image_url.ifEmpty { "https://images.unsplash.com/photo-1578683010236-d716f9a3f461" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            // Subtle dark contrast wash to ensure room controls pop while keeping wallpaper 100% vibrant
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    // Compact Luxury Room Navigation Bar (Optimizes vertical space & Transparent Edge-to-Edge)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Section: Host Avatar Pill + Room Title & Share
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Host Avatar Pill (Enhanced with Square Avatar, Follower Count & Prominent Follow Button)
                                Surface(
                                    color = Color.Black.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                                    modifier = Modifier.clickable { selectedMemberForSheet = hostMemberObj }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 4.dp, top = 3.dp, bottom = 3.dp, end = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Square Host Profile with rounded corners
                                        Box(contentAlignment = Alignment.Center) {
                                            AsyncImage(
                                                model = hostPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                contentDescription = hostName,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.2.dp, BrandGold, RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.widthIn(max = 110.dp)) {
                                            Text(
                                                text = hostName,
                                                color = Color.White,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "👥 $hostFollowerCount • ID: $displayRoomId",
                                                color = BrandGold,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (isFollowingHost) Color(0x334CAF50) else if (isUserHost) Color(0x33FFB300) else BrandCoral,
                                            shape = RoundedCornerShape(8.dp),
                                            border = if (isFollowingHost) BorderStroke(1.dp, Color(0xFF4CAF50)) else if (isUserHost) BorderStroke(0.8.dp, BrandGold.copy(alpha = 0.6f)) else null,
                                            modifier = Modifier.clickable {
                                                if (isUserHost) {
                                                    Toast.makeText(context, "👑 You are the Host ($hostFollowerCount followers)", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    val newFollowing = !isFollowingHost
                                                    isFollowingHost = newFollowing
                                                    if (newFollowing) {
                                                        hostFollowerCount += 1
                                                        if (targetHostId.isNotBlank() && targetHostId != currentUser.uid) {
                                                            val record = FollowerRecord(
                                                                uid = currentUser.uid,
                                                                name = currentUser.name,
                                                                photo = currentUser.photo,
                                                                timestamp = System.currentTimeMillis()
                                                            )
                                                            db.collection("users").document(targetHostId).collection("followers").document(currentUser.uid).set(record)
                                                            db.collection("users").document(currentUser.uid).collection("following").document(targetHostId).set(
                                                                mapOf("hostUid" to targetHostId, "hostName" to hostName, "timestamp" to System.currentTimeMillis())
                                                            )
                                                            db.collection("users").document(targetHostId).update("followerCount", FieldValue.increment(1))
                                                            db.collection("users").document(currentUser.uid).update("followingCount", FieldValue.increment(1))
                                                        }
                                                        Toast.makeText(context, "🌟 Following $hostName!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        hostFollowerCount = (hostFollowerCount - 1).coerceAtLeast(0)
                                                        if (targetHostId.isNotBlank() && targetHostId != currentUser.uid) {
                                                            db.collection("users").document(targetHostId).collection("followers").document(currentUser.uid).delete()
                                                            db.collection("users").document(currentUser.uid).collection("following").document(targetHostId).delete()
                                                            db.collection("users").document(targetHostId).update("followerCount", FieldValue.increment(-1))
                                                            db.collection("users").document(currentUser.uid).update("followingCount", FieldValue.increment(-1))
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                if (!isFollowingHost && !isUserHost) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                }
                                                Text(
                                                    text = if (isFollowingHost) "✓ Following" else if (isUserHost) "👑 Host" else "Follow",
                                                    color = if (isFollowingHost) Color(0xFF81C784) else if (isUserHost) BrandGold else Color.White,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { showShareInviteDialog = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = BrandGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            // Right Section (Viewers, PK, Settings, Exit) - Well-spaced, clean & never cut off
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(end = 2.dp)
                            ) {
                                // 1. Real-Time Viewers Avatar Stack & Count Badge
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                                        .clickable { showAllViewersDialog = true }
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val previewMembers = activeMembers.filter { it.uid != targetHostId && (System.currentTimeMillis() - it.joinedAt < 7_200_000L) }.take(3)
                                    if (previewMembers.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy((-7).dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            previewMembers.forEach { m ->
                                                AsyncImage(
                                                    model = m.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                    contentDescription = m.name,
                                                    modifier = Modifier
                                                        .size(19.dp)
                                                        .clip(CircleShape)
                                                        .border(1.dp, BrandGold.copy(alpha = 0.8f), CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "Live Viewers",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = "${activeMembers.size}",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (isUserHost) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (room.isPkActive) Color(0xFFFF1744) else BrandCard.copy(alpha = 0.85f))
                                            .border(1.dp, if (room.isPkActive) BrandGold else Color(0xFFFF5252).copy(alpha = 0.8f), CircleShape)
                                            .clickable {
                                                if (room.isPkActive) {
                                                    endPkBattle()
                                                } else {
                                                    val opponentSeat = micSeats.firstOrNull { it.seatIndex > 0 && it.uid.isNotBlank() }
                                                    if (opponentSeat != null) {
                                                        startPkBattleWithOpponent(opponentSeat.uid, opponentSeat.name, opponentSeat.photo)
                                                    } else {
                                                        val oppMember = activeMembers.firstOrNull { it.uid != currentUser.uid }
                                                        if (oppMember != null) {
                                                            startPkBattleWithOpponent(oppMember.uid, oppMember.name, oppMember.photoUrl)
                                                        } else {
                                                            startPkBattleWithOpponent("guest_challenger", "Challenger Host", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d")
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (room.isPkActive) "🔥" else "⚔️",
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showRoomSettingsDialog = true },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Room Options & Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // Room Power / Exit Options Button (Clean sharp icon)
                                IconButton(
                                    onClick = { showExitOrKeepDialog = true },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Room Power / Exit Options",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    // Public Room Chat & Audio Controls Bar (Sleek Single Row - Zero Giant Gap)
                    Surface(
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.35f)
                                        )
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 1. SMALL CIRCULAR COMMENT BUTTON
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                        .clickable { showCommentInputDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubble,
                                        contentDescription = "Comment",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // 2. SELF MIC BUTTON
                                val mySeat = micSeats.find { it.uid == currentUser.uid }
                                val isSelfOnStage = mySeat != null
                                val isSelfMicMuted = mySeat?.isMuted ?: true

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelfMicMuted) Color(0xFF281518)
                                            else if (isLocalTalking) BrandGreen
                                            else Color.Black.copy(alpha = 0.55f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelfMicMuted) Color(0xFFFF5252).copy(alpha = 0.85f)
                                            else if (isLocalTalking) BrandGreen
                                            else Color.White.copy(alpha = 0.25f),
                                            CircleShape
                                        )
                                        .clickable {
                                            if (isSelfOnStage) {
                                                val s = mySeat!!
                                                val newMute = !s.isMuted
                                                val idx = micSeats.indexOfFirst { it.seatIndex == s.seatIndex }
                                                if (idx != -1) {
                                                    micSeats[idx] = micSeats[idx].copy(isMuted = newMute)
                                                }
                                                toggleMuteMicSeat(db, room.id, s.seatIndex, s.isMuted)
                                                Toast.makeText(context, if (newMute) "Microphone Muted 🔇" else "Microphone Live 🎙️", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val openSeat = micSeats.firstOrNull { it.seatIndex > 0 && it.uid.isEmpty() && !it.isLocked }
                                                if (openSeat != null) {
                                                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                                    if (hasPerm) {
                                                        joinMicSeat(db, room.id, openSeat.seatIndex, currentUser, micSeats)
                                                        Toast.makeText(context, "You took Mic ${openSeat.seatIndex + 1}! Live now 🎙️", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        pendingSeatToJoin = openSeat
                                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                } else if (isUserHost) {
                                                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                                    if (hasPerm) {
                                                        joinMicSeat(db, room.id, 0, currentUser, micSeats)
                                                        Toast.makeText(context, "Host Mic Active 🎙️", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        pendingSeatToJoin = MicSeat(seatIndex = 0, label = "Host 👑", role = "host")
                                                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                } else {
                                                    Toast.makeText(context, "All stage microphones are currently occupied", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelfMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = "Self Mic",
                                        tint = if (isSelfMicMuted) Color(0xFFFF8A80) else Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // 3. ROOM VOLUME BUTTON
                                val isRoomAudioMuted by AudioPlayerManager.isMutedState
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                    .background(
                                        if (isRoomAudioMuted) Color(0xFF251A1D)
                                        else Color.Black.copy(alpha = 0.55f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isRoomAudioMuted) Color.Red.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.25f),
                                        CircleShape
                                    )
                                    .clickable {
                                        val newMute = AudioPlayerManager.toggleMute()
                                        Toast.makeText(context, if (newMute) "Room Audio Muted 🔇" else "Room Audio On 🔊", Toast.LENGTH_SHORT).show()
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRoomAudioMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Room Volume",
                                        tint = if (isRoomAudioMuted) Color(0xFFFF8A80) else BrandCyan,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // 4. INBOX BUTTON
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .border(1.dp, BrandGold.copy(alpha = 0.6f), CircleShape)
                                        .clickable { showInRoomInboxDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mail,
                                        contentDescription = "Messages Inbox",
                                        tint = BrandGold,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // 5. LIVE EMOJI REACTION BUTTON
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (showEmojiTray) BrandGold.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.55f))
                                        .border(1.dp, if (showEmojiTray) BrandGold else Color.White.copy(alpha = 0.25f), CircleShape)
                                        .clickable { showEmojiTray = !showEmojiTray },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("😊", fontSize = 17.sp)
                                }

                                // 6. LUXURY STORE BUTTON (Avatar Frames, Rings, Mic Links)
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF7E22CE).copy(alpha = 0.85f))
                                        .border(1.2.dp, BrandGold, CircleShape)
                                        .clickable { showLuxuryStoreDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🛍️", fontSize = 18.sp)
                                }

                                // 7. GIFT STORE BUTTON
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.sweepGradient(
                                                listOf(
                                                    Color(0xFFFF1744),
                                                    Color(0xFFFFB300),
                                                    Color(0xFFFF4081),
                                                    Color(0xFFFF1744)
                                                )
                                            )
                                        )
                                        .border(1.2.dp, BrandGold, CircleShape)
                                        .clickable { showGiftStoreDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎁", fontSize = 19.sp)
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    // 0. LIVE STREAMING ENTRANCE MARQUEE / BANNER
                    AnimatedVisibility(
                        visible = entranceNotificationUser != null,
                        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    ) {
                        val arrivalName = entranceNotificationUser ?: ""
                        val arrivalProfile = allProfiles.find { it.name.equals(arrivalName, true) }
                        val arrivalLevel = arrivalProfile?.getEffectiveWealthLevel()
                            ?: if (arrivalName.contains("Yasmin", true) || arrivalName.contains("Warsi", true)) 35
                            else if (arrivalName.contains("Devil", true)) 70
                            else 1

                        GrandBroadEntranceBanner(
                            userName = arrivalName,
                            level = arrivalLevel,
                            userProfile = arrivalProfile
                        )
                    }

                    // Pinned Room Announcement Banner
                    if (room.announcement.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandGold.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isUserAdmin) {
                                        showRoomSettingsDialog = true
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📢", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = room.announcement,
                                    color = BrandGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isUserAdmin) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", color = Color.LightGray, fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2.4 LIVE ROOM PK BATTLE ARENA (1v1 REAL-TIME ARENA)
                    if (room.isPkActive) {
                        LiveRoomPkBattleArena(
                            room = room,
                            currentUser = currentUser,
                            isHost = isUserHost,
                            blueSupporters = pkBlueSupporters,
                            redSupporters = pkRedSupporters,
                            onBoostBlue = {
                                selectedPkTeamForBoost = "blue"
                                showGiftStoreDialog = true
                            },
                            onBoostRed = {
                                selectedPkTeamForBoost = "red"
                                showGiftStoreDialog = true
                            },
                            onEndPk = {
                                endPkBattle()
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // ---------------------------------------------------------
                    // 2.5 STAGE SECTION (VIP ROYAL CANVAS OR 10-MIC STAGE)
                    // ---------------------------------------------------------
                    val stageSeats = if (micSeats.isNotEmpty()) micSeats else (0..9).map { idx ->
                        if (idx == 0) MicSeat(seatIndex = 0, label = "Host 👑", uid = targetHostId, name = hostName, photo = hostPhoto, role = "host")
                        else MicSeat(seatIndex = idx, label = "Mic ${idx + 1}", uid = "", name = "", photo = "", role = "speaker")
                    }

                    val canStaffModerate = isUserAdmin || currentUser.isStaffMember() || currentUser.isFounder() || currentUser.isCeo() || isUserHost
                    val handleSeatClick: (MicSeat) -> Unit = { seat ->
                        val currentSeat = micSeats.find { it.uid == currentUser.uid }
                        if (seat.uid == currentUser.uid) {
                            selectedOwnSeatForControl = seat
                        } else if (seat.uid.isEmpty()) {
                            // Vacant mic seat!
                            if (seat.isLocked && !canStaffModerate) {
                                Toast.makeText(context, "This microphone is locked by the host 🔒", Toast.LENGTH_SHORT).show()
                            } else if (currentSeat != null) {
                                // User (Host or Speaker) is ALREADY seated -> 1-TAP INSTANT MOVE TO THIS MIC!
                                if (seat.isLocked && !canStaffModerate) {
                                    Toast.makeText(context, "This microphone is locked by the host 🔒", Toast.LENGTH_SHORT).show()
                                } else {
                                    joinMicSeat(db, room.id, seat.seatIndex, currentUser, micSeats)
                                    Toast.makeText(context, "Moved to Mic ${seat.seatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                                }
                            } else if (canStaffModerate) {
                                // Host/Admin/Staff not yet seated -> open admin options (Take mic, lock, invite)
                                selectedSeatForAdmin = seat
                            } else {
                                // Audience member tapping empty mic -> take mic
                                val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    joinMicSeat(db, room.id, seat.seatIndex, currentUser, micSeats)
                                    Toast.makeText(context, "You are now live on Mic ${seat.seatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                                } else {
                                    pendingSeatToJoin = seat
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        } else if (canStaffModerate) {
                            // Occupied mic seat and user is host/admin/staff -> open moderation & speaker relocation options
                            selectedSeatForAdmin = seat
                        } else {
                            // Occupied mic seat and user is audience -> view speaker profile
                            val memberProfile = allProfiles.find { it.uid == seat.uid }
                            selectedMemberForSheet = RoomMember(
                                uid = seat.uid,
                                userId = memberProfile?.userId ?: "",
                                name = seat.name,
                                photoUrl = seat.photo,
                                role = seat.role
                            )
                        }
                    }



                    if (isVipRoom) {
                        VipRoyalStageCanvas(
                            seats = stageSeats,
                            currentUserId = currentUser.uid,
                            isHost = isUserAdmin,
                            isLocalTalking = isLocalTalking,
                            allProfiles = allProfiles,
                            userActiveFrame = activeUserFrame,
                            userActiveMicEffect = userActiveMicEffect,
                            activeEmojiReactions = activeEmojiReactions,
                            activeMicLinkId = activeMicLinkDesignId,
                            activeIntimacyBonds = activeIntimacyBonds,
                            onSeatClick = handleSeatClick,
                            onProfileClick = { seat ->
                                val profile = allProfiles.find { it.uid == seat.uid }
                                selectedMemberForSheet = RoomMember(
                                    uid = seat.uid,
                                    userId = profile?.userId ?: "",
                                    name = seat.name,
                                    photoUrl = seat.photo,
                                    role = seat.role
                                )
                            }
                        )
                    } else {
                        TenMicStageSection(
                            seats = stageSeats,
                            currentUserId = currentUser.uid,
                            isHost = isUserAdmin,
                            isLocalTalking = isLocalTalking,
                            allProfiles = allProfiles,
                            userActiveFrame = activeUserFrame,
                            userActiveMicEffect = userActiveMicEffect,
                            activeEmojiReactions = activeEmojiReactions,
                            categorySeatEmoji = "🎙️",
                            categorySeatLabel = "Mic",
                            activeMicLinkId = activeMicLinkDesignId,
                            activeIntimacyBonds = activeIntimacyBonds,
                            onOpenMicLinkSelector = { showMicLinkSelectorDialog = true },
                            onSeatClick = handleSeatClick,
                            onProfileClick = { seat ->
                                val profile = allProfiles.find { it.uid == seat.uid }
                                selectedMemberForSheet = RoomMember(
                                    uid = seat.uid,
                                    userId = profile?.userId ?: "",
                                    name = seat.name,
                                    photoUrl = seat.photo,
                                    role = seat.role
                                )
                            },
                            onQuickToggleMute = {
                                myCurrentMicSeat?.let { seat ->
                                    toggleMuteMicSeat(db, room.id, seat.seatIndex, seat.isMuted)
                                }
                            },
                            onQuickLeaveStage = {
                                myCurrentMicSeat?.let { seat ->
                                    vacateMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                                    Toast.makeText(context, "You stepped down from the stage", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ---------------------------------------------------------
                    // 4. LIVE TRANSPARENT COMMENTS STREAM (FLOATING OVER WALLPAPER)
                    // ---------------------------------------------------------
                    val chatListState = rememberLazyListState()
                    LaunchedEffect(liveComments.size) {
                        if (liveComments.isNotEmpty()) {
                            chatListState.animateScrollToItem(liveComments.size - 1)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (liveComments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "💬 Live Room Chat\nBe the first to say hello to the host and room!",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                state = chatListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(liveComments) { comment ->
                                    val commenterProfile = (if (comment.userId == currentUser.uid) currentUser else null)
                                        ?: allProfiles.find { it.uid == comment.userId || it.name.equals(comment.userName, true) }
                                    val isCommenterHost = comment.userId == room.hostId || (room.hostName.isNotBlank() && comment.userName.equals(room.hostName, true))
                                    val isCommenterAdmin = room.adminUserIds.contains(comment.userId) || commenterProfile?.isAdmin == true

                                    val commenterLevel = commenterProfile?.getEffectiveWealthLevel()
                                        ?: if (comment.userId == currentUser.uid) currentUser.getEffectiveWealthLevel()
                                        else if (comment.userName.contains("Yasmin", true) || comment.userName.contains("Warsi", true)) 35
                                        else if (comment.userName.contains("Devil", true)) 70
                                        else if (comment.tier.contains("VIP", true)) 31
                                        else if (comment.tier.contains("Platinum", true)) 20
                                        else 17

                                    val commenterCharm = commenterProfile?.getEffectiveCharmLevel()
                                        ?: if (comment.userId == currentUser.uid) currentUser.getEffectiveCharmLevel()
                                        else if (commenterLevel >= 70) 70
                                        else if (commenterLevel >= 35) 35
                                        else 15

                                    val commenterPkLevel = commenterProfile?.getEffectivePkLevel()
                                        ?: if (comment.userId == currentUser.uid) currentUser.getEffectivePkLevel()
                                        else 1

                                    val commenterMedal = (commenterProfile?.followerCount?.takeIf { it > 0 })
                                        ?: if (comment.userId == currentUser.uid && currentUser.followerCount > 0) currentUser.followerCount
                                        else if (commenterLevel >= 70) 99
                                        else if (commenterLevel >= 35) 28
                                        else 11

                                    val isCommentFounder = comment.isFounder || commenterProfile?.isFounder() == true || (comment.userId == currentUser.uid && currentUser.isFounder())
                                    val commentVipLevel = if (comment.vipLevel in 1..4) comment.vipLevel else (commenterProfile?.getVipLevel() ?: if (comment.userId == currentUser.uid) currentUser.getVipLevel() else 0)
                                    val isEntranceMessage = comment.message.contains("entered the room") || comment.message.startsWith("✨ Welcome") || comment.message.contains("to the room!") || comment.message.contains("has arrived in")

                                    if (isEntranceMessage) {
                                        VipRoomChatBarEntry(
                                            userName = comment.userName,
                                            avatarUrl = comment.userPhoto.ifEmpty { commenterProfile?.photo ?: "" },
                                            vipLevel = commentVipLevel,
                                            isFounder = isCommentFounder,
                                            actionText = comment.message,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    } else {
                                        val activeF = commenterProfile?.activeFrame ?: ""
                                        val commenterAvatarFrame = if (activeF.isNotBlank()) activeF else if (isCommentFounder) "frame_livetogather_founder" else ""

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedMemberForSheet = RoomMember(
                                                        uid = comment.userId,
                                                        name = comment.userName,
                                                        photoUrl = comment.userPhoto,
                                                        tier = comment.tier
                                                    )
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            // 1. User Avatar (Clean circular photo only, no frame in comments)
                                            AsyncImage(
                                                model = comment.userPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                contentDescription = "User Avatar",
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, Color(0x33FFFFFF), CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))

                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                // Line 1: User Display Name & Host/Admin Badge
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = comment.userName,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    if (isCommenterHost) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(17.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(
                                                                    Brush.linearGradient(
                                                                        listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
                                                                    )
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Home,
                                                                contentDescription = "Host",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                        }
                                                    } else if (isCommenterAdmin) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(17.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(
                                                                    Brush.linearGradient(
                                                                        listOf(Color(0xFF1E88E5), Color(0xFF42A5F5))
                                                                    )
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Shield,
                                                                contentDescription = "Admin",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                // Line 2: 3D Badges Row (Founder, CEO, Official, Tier, VIP, Swords, Level, Charm, Medal)
                                                UserChatBadgesRow(
                                                    userProfile = commenterProfile,
                                                    level = commenterLevel,
                                                    charm = commenterCharm,
                                                    pkLevel = commenterPkLevel,
                                                    medal = commenterMedal,
                                                    isFounder = isCommentFounder,
                                                    isCeo = commenterProfile?.isCeo() == true,
                                                    vipLevel = commentVipLevel
                                                )

                                                // Line 3: Ornate Luxury Chat Bubble Container
                                                val commentFrameId = comment.chatFrameId.ifEmpty { commenterProfile?.activeChatFrame ?: if (comment.userId == currentUser.uid) currentUser.activeChatFrame else "" }
                                                val vipChatFrameItem = getVipChatFrame(commentFrameId, commentVipLevel, isCommentFounder)

                                                if (vipChatFrameItem != null) {
                                                    VipStyledChatBubble(
                                                        messageText = comment.message,
                                                        isMe = comment.userId == currentUser.uid,
                                                        chatFrameId = commentFrameId,
                                                        vipLevel = commentVipLevel,
                                                        isFounder = isCommentFounder && (commentVipLevel == 0 && commentFrameId.isBlank())
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(Color.Black.copy(alpha = 0.55f))
                                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = comment.message,
                                                            color = Color.White,
                                                            fontSize = 13.sp
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

                // -------------------------------------------------------------
                // IN-ROOM DRAGGABLE FLOATING PIP MUSIC EQUALIZER PLAYER
                // -------------------------------------------------------------
                val isMusicPlayingFloating = AudioPlayerManager.isPlayingState.value || room.isPlaying
                val floatingTrackUrl = AudioPlayerManager.currentTrackUrlState.value.ifEmpty { room.nowPlayingUrl }
                val floatingTrackTitle = AudioPlayerManager.currentTrackTitleState.value.ifEmpty { room.nowPlayingTitle }
                val floatingTrackArtist = room.nowPlayingArtist
                val floatingArtwork = AudioPlayerManager.currentTrackArtworkUrlState.value.ifEmpty { room.nowPlayingArtworkUrl }

                if (isVipRoom && (isMusicPlayingFloating || floatingTrackUrl.isNotBlank())) {
                    FloatingMusicEqualizerPlayer(
                        room = room,
                        isPlaying = isMusicPlayingFloating,
                        trackTitle = floatingTrackTitle,
                        trackArtist = floatingTrackArtist,
                        artworkUrl = floatingArtwork,
                        isHostOrAdmin = isUserAdmin,
                        onTogglePlay = {
                            if (!isUserAdmin) {
                                Toast.makeText(context, "Only room host or admin can control music 🎵", Toast.LENGTH_SHORT).show()
                            } else {
                                val willPlay = !isMusicPlayingFloating
                                val now = System.currentTimeMillis()
                                val curPos = AudioPlayerManager.currentPositionMsState.value
                                val trackStarted = if (willPlay) (now - curPos) else 0L
                                if (willPlay) {
                                    if (AudioPlayerManager.currentTrackUrlState.value.isNotBlank()) {
                                        AudioPlayerManager.resume(context)
                                    } else {
                                        val defaultUrl = room.nowPlayingUrl.ifEmpty { CURATED_HANGOUT_TRACKS[0].url }
                                        val defaultTitle = room.nowPlayingTitle.ifEmpty { CURATED_HANGOUT_TRACKS[0].title }
                                        val defaultArt = room.nowPlayingArtworkUrl.ifEmpty { CURATED_HANGOUT_TRACKS[0].artworkUrl }
                                        AudioPlayerManager.play(context, defaultUrl, defaultTitle, defaultArt, initialSeekMs = curPos)
                                    }
                                } else {
                                    AudioPlayerManager.pause()
                                }
                                room = room.copy(isPlaying = willPlay, trackStartedAt = trackStarted, playbackPosition = curPos.toLong())
                                db.collection("rooms").document(room.id).set(
                                    mapOf(
                                        "isPlaying" to willPlay,
                                        "trackStartedAt" to trackStarted,
                                        "playbackPosition" to curPos.toLong(),
                                        "nowPlayingTitle" to AudioPlayerManager.currentTrackTitleState.value.ifEmpty { room.nowPlayingTitle },
                                        "nowPlayingArtist" to room.nowPlayingArtist,
                                        "nowPlayingUrl" to AudioPlayerManager.currentTrackUrlState.value.ifEmpty { room.nowPlayingUrl },
                                        "nowPlayingArtworkUrl" to AudioPlayerManager.currentTrackArtworkUrlState.value.ifEmpty { room.nowPlayingArtworkUrl }
                                    ),
                                    SetOptions.merge()
                                )
                            }
                        },
                        onOpenSearch = {
                            if (!isUserAdmin) {
                                Toast.makeText(context, "Only room host or admin can select music 🎵", Toast.LENGTH_SHORT).show()
                            } else {
                                showTrackSelector = true
                            }
                        },
                        onStopMusic = {
                            if (!isUserAdmin) {
                                Toast.makeText(context, "Only room host or admin can stop music 🛑", Toast.LENGTH_SHORT).show()
                            } else {
                                AudioPlayerManager.stop()
                                room = room.copy(
                                    isPlaying = false,
                                    nowPlayingUrl = "",
                                    nowPlayingTitle = "",
                                    nowPlayingArtist = "",
                                    nowPlayingArtworkUrl = "",
                                    trackStartedAt = 0L,
                                    playbackPosition = 0L
                                )
                                db.collection("rooms").document(room.id).set(
                                    mapOf(
                                        "isPlaying" to false,
                                        "nowPlayingTitle" to "",
                                        "nowPlayingArtist" to "",
                                        "nowPlayingUrl" to "",
                                        "nowPlayingArtworkUrl" to "",
                                        "trackStartedAt" to 0L,
                                        "playbackPosition" to 0L
                                    ),
                                    SetOptions.merge()
                                )
                                Toast.makeText(context, "Music playback stopped ⏹️", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Non-VIP Rooms Floating Draggable PIP Music Player (Movable anywhere on screen)
                if (!isVipRoom) {
                    val currentArtwork = room.nowPlayingArtworkUrl.ifEmpty { AudioPlayerManager.currentTrackArtworkUrlState.value }
                    val isPlayingNow = AudioPlayerManager.isPlayingState.value
                    val isBuffering by AudioPlayerManager.isBufferingState
                    val formattedTime by AudioPlayerManager.formattedTimeState
                    val currentPosMs by AudioPlayerManager.currentPositionMsState
                    val totalDurationMs by AudioPlayerManager.durationMsState

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 56.dp, end = 8.dp)
                            .offset { IntOffset(musicPlayerOffset.x.roundToInt(), musicPlayerOffset.y.roundToInt()) }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    musicPlayerOffset = Offset(
                                        x = musicPlayerOffset.x + dragAmount.x,
                                        y = musicPlayerOffset.y + dragAmount.y
                                    )
                                }
                            }
                    ) {
                        if (isMusicPlayerMinimized) {
                            // Compact Floating Vinyl Disc Pill (Collapsed PIP)
                            Surface(
                                color = Color.Black.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.2.dp, BrandGold.copy(alpha = 0.85f)),
                                shadowElevation = 8.dp,
                                modifier = Modifier.clickable { isMusicPlayerMinimized = false }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Brush.sweepGradient(listOf(BrandCoral, BrandGold, BrandCoral))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🎵", fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = AudioPlayerManager.currentTrackTitleState.value.ifEmpty { room.nowPlayingTitle.ifEmpty { "Room Music" } },
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 110.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            if (!isUserAdmin) {
                                                Toast.makeText(context, "Only room host or admin can control music 🎵", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val willPlay = !isPlayingNow
                                                val now = System.currentTimeMillis()
                                                val curPos = AudioPlayerManager.currentPositionMsState.value
                                                val trackStarted = if (willPlay) (now - curPos) else 0L
                                                if (willPlay) {
                                                    if (AudioPlayerManager.currentTrackUrlState.value.isNotBlank()) {
                                                        AudioPlayerManager.resume(context)
                                                    } else {
                                                        val defaultUrl = room.nowPlayingUrl.ifEmpty { CURATED_HANGOUT_TRACKS[0].url }
                                                        val defaultTitle = room.nowPlayingTitle.ifEmpty { CURATED_HANGOUT_TRACKS[0].title }
                                                        val defaultArt = room.nowPlayingArtworkUrl.ifEmpty { CURATED_HANGOUT_TRACKS[0].artworkUrl }
                                                        AudioPlayerManager.play(context, defaultUrl, defaultTitle, defaultArt, initialSeekMs = curPos)
                                                    }
                                                } else {
                                                    AudioPlayerManager.pause()
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingNow) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlayingNow) "Pause" else "Play",
                                            tint = BrandGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(2.dp))
                                    IconButton(
                                        onClick = { isMusicPlayerMinimized = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "Expand Player",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Expanded Floating Draggable Console Card
                            CategoryThemedPlayerConsole(
                                room = room,
                                isPlayingNow = isPlayingNow,
                                isBuffering = isBuffering,
                                currentArtwork = currentArtwork,
                                formattedTime = formattedTime,
                                currentPosMs = currentPosMs,
                                totalDurationMs = totalDurationMs,
                                onMinimize = { isMusicPlayerMinimized = true },
                                onTogglePlay = {
                                    if (!isUserAdmin) {
                                        Toast.makeText(context, "Only room host or admin can control music 🎵", Toast.LENGTH_SHORT).show()
                                        return@CategoryThemedPlayerConsole
                                    }
                                    val willPlay = !isPlayingNow
                                    val now = System.currentTimeMillis()
                                    val curPos = AudioPlayerManager.currentPositionMsState.value
                                    val trackStarted = if (willPlay) (now - curPos) else 0L
                                    if (willPlay) {
                                        if (AudioPlayerManager.currentTrackUrlState.value.isNotBlank()) {
                                            AudioPlayerManager.resume(context)
                                        } else {
                                            val defaultUrl = room.nowPlayingUrl.ifEmpty { CURATED_HANGOUT_TRACKS[0].url }
                                            val defaultTitle = room.nowPlayingTitle.ifEmpty { CURATED_HANGOUT_TRACKS[0].title }
                                            val defaultArt = room.nowPlayingArtworkUrl.ifEmpty { CURATED_HANGOUT_TRACKS[0].artworkUrl }
                                            AudioPlayerManager.play(context, defaultUrl, defaultTitle, defaultArt, initialSeekMs = curPos)
                                        }
                                    } else {
                                        AudioPlayerManager.pause()
                                    }
                                    room = room.copy(isPlaying = willPlay, trackStartedAt = trackStarted, playbackPosition = curPos.toLong())
                                    db.collection("rooms").document(room.id).set(
                                        mapOf(
                                            "isPlaying" to willPlay,
                                            "trackStartedAt" to trackStarted,
                                            "playbackPosition" to curPos.toLong(),
                                            "nowPlayingTitle" to AudioPlayerManager.currentTrackTitleState.value.ifEmpty { room.nowPlayingTitle },
                                            "nowPlayingArtist" to room.nowPlayingArtist,
                                            "nowPlayingUrl" to AudioPlayerManager.currentTrackUrlState.value.ifEmpty { room.nowPlayingUrl },
                                            "nowPlayingArtworkUrl" to AudioPlayerManager.currentTrackArtworkUrlState.value.ifEmpty { room.nowPlayingArtworkUrl }
                                        ),
                                        SetOptions.merge()
                                    )
                                },
                                onSelectTrack = {
                                    if (!isUserAdmin) {
                                        Toast.makeText(context, "Only room host or admin can change music 🎵", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showTrackSelector = true
                                    }
                                },
                                onTriggerSoundFx = { fx ->
                                    try {
                                        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
                                        when (fx.id) {
                                            "applause", "toast", "cheers" -> toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
                                            "cheer", "horn", "horn_party" -> toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
                                            "bell", "champagne", "ice_clink" -> toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                                            else -> toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
                                        }
                                    } catch (e: Exception) {
                                        Log.w("AudioHangoutRoom", "Error playing sound fx tone: ${e.message}")
                                    }
                                    val fxComment = LiveRoomComment(
                                        id = UUID.randomUUID().toString(),
                                        roomId = room.id,
                                        userId = currentUser.uid,
                                        userName = currentUser.name,
                                        userPhoto = currentUser.photo,
                                        tier = if (currentUser.isVipOrPlatinum()) "VIP" else "Standard",
                                        message = "${fx.emoji} triggered sound fx: [${fx.name}]!",
                                        timestamp = System.currentTimeMillis()
                                    )
                                    db.collection("rooms").document(room.id)
                                        .collection("live_comments").document(fxComment.id)
                                        .set(fxComment)
                                    Toast.makeText(context, "${fx.emoji} ${fx.name}!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.widthIn(max = 350.dp)
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // FULL-SCREEN TRANSPARENT VIDEO GIFT BROADCAST ANIMATION
                // -------------------------------------------------------------
                activeGiftEvent?.let { giftEvent ->
                    FullScreenGiftAnimationOverlay(
                        giftEvent = giftEvent,
                        onDismiss = { activeGiftEvent = null }
                    )
                }

                // -------------------------------------------------------------
                // LIVE FLOATING EMOJI REACTION TRAY
                // -------------------------------------------------------------
                AnimatedVisibility(
                    visible = showEmojiTray,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp, start = 8.dp, end = 8.dp)
                ) {
                    Surface(
                        color = Color(0xFF1E171C).copy(alpha = 0.95f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, BrandGold.copy(alpha = 0.75f)),
                        shadowElevation = 10.dp
                    ) {
                        val reactionsList = listOf("✌️", "😱", "😵‍💫", "🤔", "😴", "👉", "🤮", "🤘", "😍", "😂", "🔥", "❤️", "👍", "👏", "🎉", "🚀", "💯", "🥳", "😎", "🤣")
                        LazyRow(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(reactionsList) { emo ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable {
                                            triggerEmojiReaction(emo)
                                            showEmojiTray = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emo, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // --- SUB-DIALOGS ---

        // 1. Host Track Selector Dialog
        if (showTrackSelector) {
            TrackSelectorDialog(
                onDismiss = { showTrackSelector = false },
                onSelectTrack = { title, artist, category, url, artworkUrl ->
                    val now = System.currentTimeMillis()
                    db.collection("rooms").document(room.id).set(
                        mapOf(
                            "currentPlayingTrack" to title,
                            "nowPlayingTitle" to title,
                            "nowPlayingArtist" to artist,
                            "nowPlayingUrl" to url,
                            "nowPlayingArtworkUrl" to artworkUrl,
                            "isPlaying" to true,
                            "trackStartedAt" to now,
                            "playbackPosition" to 0L
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                    room = room.copy(
                        nowPlayingTitle = title,
                        nowPlayingArtist = artist,
                        nowPlayingUrl = url,
                        nowPlayingArtworkUrl = artworkUrl,
                        isPlaying = true,
                        trackStartedAt = now,
                        playbackPosition = 0L
                    )
                    AudioPlayerManager.play(context, url, title, artworkUrl, initialSeekMs = 0)
                    Toast.makeText(context, "Streaming: $title by $artist", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 2. Member Profile Sheet Dialog
        if (selectedMemberForSheet != null) {
            val member = selectedMemberForSheet!!
            val profile = allProfiles.find { it.uid == member.uid } ?: UserProfile(
                uid = member.uid,
                name = member.name,
                photo = member.photoUrl,
                age = 23,
                city = "Kolkata",
                bio = "Music enthusiast and love deep conversations! 🎧",
                plan = if (member.tier.contains("VIP", true)) "VIP LEVEL ★" else if (member.tier.contains("Platinum", true)) "PLATINUM" else "Standard"
            )

            RoomMemberProfileSheet(
                member = member,
                userProfile = profile,
                currentUser = currentUser,
                isHost = isUserHost,
                roomId = room.id,
                db = db,
                roomAdminUserIds = room.adminUserIds,
                onToggleAdmin = { targetMember ->
                    if (targetMember.uid in room.adminUserIds) {
                        db.collection("rooms").document(room.id)
                            .update("adminUserIds", FieldValue.arrayRemove(targetMember.uid))
                        db.collection("users").document(targetMember.uid)
                            .update("adminInRoomIds", FieldValue.arrayRemove(room.id))
                        room = room.copy(adminUserIds = room.adminUserIds - targetMember.uid)
                        val revokeComment = LiveRoomComment(
                            id = UUID.randomUUID().toString(),
                            roomId = room.id,
                            userId = "system_notice",
                            userName = "🛡️ Room System",
                            userPhoto = hostPhoto,
                            tier = "Standard",
                            message = "ℹ️ ${targetMember.name} is no longer an Admin of this room.",
                            timestamp = System.currentTimeMillis()
                        )
                        db.collection("rooms").document(room.id)
                            .collection("live_comments").document(revokeComment.id).set(revokeComment)
                        Toast.makeText(context, "🛡️ Removed ${targetMember.name} from Room Admins", Toast.LENGTH_SHORT).show()
                    } else {
                        if (room.adminUserIds.size >= 10) {
                            Toast.makeText(context, "⚠️ Maximum limit of 10 Room Admins reached!", Toast.LENGTH_SHORT).show()
                        } else {
                            db.collection("rooms").document(room.id)
                                .update("adminUserIds", FieldValue.arrayUnion(targetMember.uid))
                            db.collection("users").document(targetMember.uid)
                                .update("adminInRoomIds", FieldValue.arrayUnion(room.id))
                            room = room.copy(adminUserIds = room.adminUserIds + targetMember.uid)
                            val congratsComment = LiveRoomComment(
                                id = UUID.randomUUID().toString(),
                                roomId = room.id,
                                userId = "system_congrats",
                                userName = "👑 Room Host Announcement",
                                userPhoto = hostPhoto,
                                tier = "VIP",
                                message = "🎉 Congratulations ${targetMember.name}! You have been appointed as an Admin of this room! 🛡️✨",
                                timestamp = System.currentTimeMillis()
                            )
                            db.collection("rooms").document(room.id)
                                .collection("live_comments").document(congratsComment.id).set(congratsComment)
                            Toast.makeText(context, "👑 Appointed ${targetMember.name} as Room Admin! (${room.adminUserIds.size}/10)", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDismiss = { selectedMemberForSheet = null },
                onOpenGiftCenter = { target ->
                    selectedMemberForSheet = null
                    giftCenterRecipient = target
                    showGiftStoreDialog = true
                },
                onLeaveRoom = {
                    selectedMemberForSheet = null
                    onDismiss()
                },
                onLike = {
                    recordSwipe(db, currentUser.uid, member.uid, "like")
                    checkAndHandleMutualMatch(db, currentUser, profile, onMatch = {
                        Toast.makeText(context, "🎉 It's a Match with ${profile.name}!", Toast.LENGTH_LONG).show()
                    })
                    sendNotification(
                        db = db,
                        targetUserId = member.uid,
                        sender = currentUser,
                        type = "like",
                        title = "❤️ New Room Like!",
                        message = "${currentUser.name} liked you in ${room.title}"
                    )
                    Toast.makeText(context, "Liked ${member.name}!", Toast.LENGTH_SHORT).show()
                },
                onDirectMessage = {
                    val partner = profile ?: UserProfile(uid = member.uid, name = member.name, photo = member.photoUrl)
                    onDirectChat(partner)
                },
                onRequestMessage = {
                    selectedMemberForRequest = member
                },
                onKickOut = { targetMember ->
                    room = room.copy(kickedUserIds = room.kickedUserIds + targetMember.uid)
                    db.collection("rooms").document(room.id)
                        .collection("members").document(targetMember.uid).delete()
                    db.collection("rooms").document(room.id)
                        .update("kickedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "${targetMember.name} kicked out of room.", Toast.LENGTH_SHORT).show()
                    selectedMemberForSheet = null
                },
                onBan = { targetMember ->
                    room = room.copy(bannedUserIds = room.bannedUserIds + targetMember.uid)
                    db.collection("rooms").document(room.id)
                        .collection("members").document(targetMember.uid).delete()
                    db.collection("rooms").document(room.id)
                        .update("bannedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "${targetMember.name} permanently banned from room.", Toast.LENGTH_SHORT).show()
                    selectedMemberForSheet = null
                },
                onBlock = { targetMember ->
                    db.collection("users").document(currentUser.uid)
                        .update("blockedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "Blocked ${targetMember.name}.", Toast.LENGTH_SHORT).show()
                    selectedMemberForSheet = null
                },
                onTagMember = { memberName ->
                    commentInputText = "@$memberName "
                    selectedMemberForSheet = null
                }
            )
        }

        // 3. Send Message Request Dialog (Standard/Free user flow)
        if (selectedMemberForRequest != null) {
            val target = selectedMemberForRequest!!
            SendMessageRequestDialog(
                targetUser = target,
                onDismiss = { selectedMemberForRequest = null },
                onSend = { note ->
                    val reqId = UUID.randomUUID().toString()
                    val request = MessageRequest(
                        requestId = reqId,
                        senderId = currentUser.uid,
                        senderName = currentUser.name,
                        senderPhoto = currentUser.photo,
                        targetId = target.uid,
                        targetName = target.name,
                        message = note,
                        status = "pending",
                        timestamp = System.currentTimeMillis()
                    )
                    db.collection("message_requests").document(reqId).set(request)
                    sendNotification(
                        db = db,
                        targetUserId = target.uid,
                        sender = currentUser,
                        type = "message",
                        title = "💬 Message Request Received",
                        message = "${currentUser.name}: \"$note\""
                    )
                    selectedMemberForRequest = null
                    Toast.makeText(context, "Message request sent to ${target.name}!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 4. Room Settings & Controls Dialog (Host controls: Privacy, Schedule, Theme, Kick/Ban, Edit, Delete)
        if (showRoomSettingsDialog) {
            RoomSettingsDialog(
                room = room,
                isHost = isUserAdmin,
                currentUser = currentUser,
                activeMembers = activeMembers,
                db = db,
                onDismiss = { showRoomSettingsDialog = false },
                onUpdateTheme = { newThemeUrl ->
                    room = room.copy(banner_image_url = newThemeUrl)
                },
                onKickMember = { kicked ->
                    room = room.copy(kickedUserIds = room.kickedUserIds + kicked.uid)
                },
                onBanMember = { banned ->
                    room = room.copy(bannedUserIds = room.bannedUserIds + banned.uid)
                },
                onEditRoom = { showEditRoomDialog = true },
                onDeleteRoom = { showDeleteConfirmation = true },
                onConvertToVideo = {
                    com.devil.livetogather.agora.AgoraVoiceManager.leaveRoom()
                    onConvertToVideo?.invoke(it)
                }
            )
        }

        // 4A. Edit Room Dialog (Host Only)
        if (showEditRoomDialog) {
            EditRoomDialog(
                room = room,
                db = db,
                currentUser = currentUser,
                onDismiss = { showEditRoomDialog = false },
                onRoomUpdated = { updated ->
                    room = updated
                }
            )
        }

        // 4B. Room Delete Confirmation Dialog (Host Only)
        if (showDeleteConfirmation) {
            RoomDeleteConfirmationDialog(
                roomTitle = room.title,
                onDismiss = { showDeleteConfirmation = false },
                onConfirmDelete = {
                    showDeleteConfirmation = false
                    showRoomSettingsDialog = false
                    db.collection("rooms").document(room.id).delete()
                    if (room.category.isNotBlank()) {
                        db.collection("intent_categories").document(room.category)
                            .collection("rooms").document(room.id).delete()
                    }
                    if (currentUser.current_room == room.id) {
                        db.collection("users").document(currentUser.uid).update("current_room", "")
                    }
                    Toast.makeText(context, "Room \"${room.title}\" deleted successfully", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )
        }

        // 4B. All Room Viewers & Moderation Dialog
        if (showAllViewersDialog) {
            AllRoomViewersDialog(
                viewers = activeMembers.filter { it.uid !in room.kickedUserIds && it.uid !in room.bannedUserIds },
                currentUser = currentUser,
                isHost = isUserHost,
                isVideoRoom = room.isVideoRoom,
                onSelectMember = { member ->
                    selectedMemberForSheet = member
                    showAllViewersDialog = false
                },
                onKickOut = { targetMember ->
                    room = room.copy(kickedUserIds = room.kickedUserIds + targetMember.uid)
                    db.collection("rooms").document(room.id)
                        .collection("members").document(targetMember.uid).delete()
                    db.collection("rooms").document(room.id)
                        .update("kickedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "${targetMember.name} kicked out of room.", Toast.LENGTH_SHORT).show()
                },
                onBan = { targetMember ->
                    room = room.copy(bannedUserIds = room.bannedUserIds + targetMember.uid)
                    db.collection("rooms").document(room.id)
                        .collection("members").document(targetMember.uid).delete()
                    db.collection("rooms").document(room.id)
                        .update("bannedUserIds", FieldValue.arrayUnion(targetMember.uid))
                    Toast.makeText(context, "${targetMember.name} permanently banned from room.", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showAllViewersDialog = false }
            )
        }

        // 5. Celebratory Welcome Follower Dialog
        if (showFollowCelebrationDialog) {
            Dialog(
                onDismissRequest = { showFollowCelebrationDialog = false }
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandCard),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, BrandGold, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 46.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Thank You for Following!",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You are now following $hostName. You will receive an instant real-time notification whenever $hostName goes LIVE!",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { showFollowCelebrationDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("Awesome! 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 7. Host & Admin Mic Seat Dialog
        if (selectedSeatForAdmin != null) {
            val seat = selectedSeatForAdmin!!
            val seatOccupant = allProfiles.find { it.uid == seat.uid }
            val isTargetFounder = seatOccupant?.isFounder() == true
            val isCurrentFounder = currentUser.isFounder()
            val canSovereignModerate = currentUser.isFounder() || currentUser.isCeo() || currentUser.isStaffMember()

            HostSeatAdminDialog(
                seat = seat,
                allSeats = micSeats,
                currentUserId = currentUser.uid,
                isModeratorApexOrStaff = canSovereignModerate,
                isTargetFounder = isTargetFounder,
                onDismiss = { selectedSeatForAdmin = null },
                onToggleMute = {
                    if (isTargetFounder && !isCurrentFounder) {
                        Toast.makeText(context, "🛡️ LiveTogather Founder has Supreme Immunity. Cannot mute! 👑", Toast.LENGTH_SHORT).show()
                    } else {
                        toggleMuteMicSeat(db, room.id, seat.seatIndex, seat.isMuted)
                    }
                    selectedSeatForAdmin = null
                },
                onToggleLock = {
                    toggleLockMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                    val lockMsg = if (seat.isLocked) "Mic ${seat.seatIndex + 1} unlocked 🔓" else "Mic ${seat.seatIndex + 1} locked 🔒"
                    Toast.makeText(context, lockMsg, Toast.LENGTH_SHORT).show()
                    selectedSeatForAdmin = null
                },
                onMoveSpeakerToSeat = { targetSeatIndex ->
                    relocateSpeakerSeat(db, room.id, seat, targetSeatIndex)
                    Toast.makeText(context, "Moved ${seat.name.ifBlank { "Speaker" }} to Mic ${targetSeatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                    selectedSeatForAdmin = null
                },
                onTakeMic = {
                    joinMicSeat(db, room.id, seat.seatIndex, currentUser, micSeats)
                    Toast.makeText(context, "You are now on Mic ${seat.seatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                    selectedSeatForAdmin = null
                },
                onKickToAudience = {
                    if (isTargetFounder && !isCurrentFounder) {
                        Toast.makeText(context, "🛡️ LiveTogather Founder has Supreme Immunity. Cannot remove! 👑", Toast.LENGTH_SHORT).show()
                    } else {
                        vacateMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                        Toast.makeText(context, "Removed ${seat.name} from stage", Toast.LENGTH_SHORT).show()
                    }
                    selectedSeatForAdmin = null
                },
                onKickOutOfRoom = {
                    if (isTargetFounder && !isCurrentFounder) {
                        Toast.makeText(context, "🛡️ LiveTogather Founder has Supreme Immunity. Cannot kick! 👑", Toast.LENGTH_SHORT).show()
                    } else if (seat.uid.isNotBlank() && (seat.uid != room.hostId || canSovereignModerate)) {
                        db.collection("rooms").document(room.id)
                            .update("kickedUserIds", FieldValue.arrayUnion(seat.uid))
                        vacateMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                        Toast.makeText(context, "Kicked ${seat.name} out of room", Toast.LENGTH_SHORT).show()
                    }
                    selectedSeatForAdmin = null
                },
                onBanFromRoom = {
                    if (isTargetFounder && !isCurrentFounder) {
                        Toast.makeText(context, "🛡️ LiveTogather Founder has Supreme Immunity. Cannot ban! 👑", Toast.LENGTH_SHORT).show()
                    } else if (seat.uid.isNotBlank() && (seat.uid != room.hostId || canSovereignModerate)) {
                        db.collection("rooms").document(room.id)
                            .update("bannedUserIds", FieldValue.arrayUnion(seat.uid))
                        vacateMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                        Toast.makeText(context, "Permanently banned ${seat.name} from room", Toast.LENGTH_SHORT).show()
                    }
                    selectedSeatForAdmin = null
                },
                onInviteListener = {
                    showInviteListenersDialog = seat
                    selectedSeatForAdmin = null
                },
                onViewProfile = {
                    val seatOccupant = allProfiles.find { it.uid == seat.uid }
                    selectedMemberForSheet = RoomMember(
                        uid = seat.uid,
                        userId = seatOccupant?.userId ?: "",
                        name = seat.name,
                        photoUrl = seat.photo,
                        role = seat.role
                    )
                    selectedSeatForAdmin = null
                }
            )
        }

        // 8. Invite Listener Dialog
        if (showInviteListenersDialog != null) {
            val seat = showInviteListenersDialog!!
            InviteListenerDialog(
                seat = seat,
                availableAudience = displayAudience.filter { aud -> micSeats.none { it.uid == aud.uid } },
                onDismiss = { showInviteListenersDialog = null },
                onSelectMember = { member ->
                    db.collection("rooms").document(room.id).collection("mic_seats").document(seat.seatIndex.toString())
                        .set(
                            MicSeat(
                                seatIndex = seat.seatIndex,
                                label = if (seat.seatIndex == 0) "Host 👑" else "Mic ${seat.seatIndex + 1}",
                                uid = member.uid,
                                name = member.name,
                                photo = member.photoUrl,
                                isMuted = false,
                                isLocked = false,
                                isTalking = false,
                                role = "speaker"
                            )
                        )
                    Toast.makeText(context, "Invited ${member.name} to Mic ${seat.seatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                    showInviteListenersDialog = null
                }
            )
        }

        // 9. Speaker Own Seat Control Dialog
        if (selectedOwnSeatForControl != null) {
            val seat = selectedOwnSeatForControl!!
            SpeakerSeatControlDialog(
                seat = seat,
                allSeats = micSeats,
                isTalking = isLocalTalking,
                onDismiss = { selectedOwnSeatForControl = null },
                onToggleMute = {
                    toggleMuteMicSeat(db, room.id, seat.seatIndex, seat.isMuted)
                    selectedOwnSeatForControl = null
                },
                onMoveToSeat = { targetSeatIndex ->
                    joinMicSeat(db, room.id, targetSeatIndex, currentUser, micSeats)
                    Toast.makeText(context, "Moved to Mic ${targetSeatIndex + 1}! 🎙️", Toast.LENGTH_SHORT).show()
                    selectedOwnSeatForControl = null
                },
                onLeaveStage = {
                    vacateMicSeat(db, room.id, seat.seatIndex, seat.isLocked)
                    Toast.makeText(context, "You stepped down from the stage", Toast.LENGTH_SHORT).show()
                    selectedOwnSeatForControl = null
                }
            )
        }

        // 9B. Room Share & Real-Time Followers Invite Dialog
        if (showShareInviteDialog) {
            InviteFollowersDialog(
                currentUser = currentUser,
                room = room,
                db = db,
                onDismiss = { showShareInviteDialog = false }
            )
        }

        // 7. Luxury Store Dialog (Avatar Frames, Rings, Mic Links)
        if (showMicLinkSelectorDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showMicLinkSelectorDialog = false }
            ) {
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF161026),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)),
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✨ Intimacy Mic Connectors",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = "Forged through 1v1 chat streaks & mutual proposals (Max 7 bonds/user)",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val designs = listOf(
                            Triple("mic_link_ruby_crown", "Royal Ruby Crown 👑 (Couple)", R.drawable.photoreal_link_ruby_crown),
                            Triple("mic_link_twin_besties", "Twin Besties 🌸 (Bestie)", R.drawable.photoreal_link_twin_besties),
                            Triple("mic_link_cyberpunk_reactor", "Cyber Reactor ⚡ (Cyber)", R.drawable.photoreal_link_cyberpunk_reactor),
                            Triple("mic_link_celestial_wings", "Celestial Wings ✨ (Soulmate)", R.drawable.photoreal_link_celestial_wings),
                            Triple("mic_link_emerald_infinity", "Emerald Infinity 💚 (Eternal)", R.drawable.photoreal_link_emerald_infinity)
                        )

                        designs.forEach { (id, name, res) ->
                            val isSelected = activeMicLinkDesignId == id
                            androidx.compose.material3.Surface(
                                color = if (isSelected) Color(0xFF9333EA).copy(alpha = 0.45f) else Color(0xFF221938),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF3B2D5C)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        activeMicLinkDesignId = id
                                        showMicLinkSelectorDialog = false
                                        android.widget.Toast.makeText(context, "⚡ Activated: $name on Stage!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = res),
                                        contentDescription = name,
                                        modifier = Modifier.size(36.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, color = Color.White, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        Text(if (isSelected) "Active on Stage ✓" else "Tap to switch", color = if (isSelected) Color(0xFF00E5FF) else Color.Gray, fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.TextButton(onClick = { showMicLinkSelectorDialog = false }) {
                            Text("Close", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        // Real-time Intimacy Gifting Proposal & Acceptance Modal
        if (pendingIntimacyRequest != null) {
            IntimacyGiftingAcceptanceDialog(
                requestData = pendingIntimacyRequest!!,
                currentUser = currentUser,
                db = db,
                onDismiss = { pendingIntimacyRequest = null }
            )
        }

        if (showLuxuryStoreDialog) {
            LuxuryStoreDialog(
                currentUser = currentUser.copy(
                    activeFrame = activeUserFrame,
                    ownedFrames = userOwnedFrames,
                    activeMicEffect = userActiveMicEffect,
                    ownedMicEffects = userOwnedMicEffects,
                    diamonds = userDiamonds
                ),
                db = db,
                roomId = room.id,
                onDismiss = { showLuxuryStoreDialog = false },
                onProfileUpdated = { updated ->
                    activeUserFrame = updated.activeFrame
                    userOwnedFrames = updated.ownedFrames
                    userActiveMicEffect = updated.activeMicEffect
                    userOwnedMicEffects = updated.ownedMicEffects
                    userDiamonds = updated.diamonds
                },
                onSendIntimacyItem = { item -> intimacyStoreItemToSend = item },
                onOpenTopUp = {
                    showGiftStoreDialog = true
                }
            )
        }

        if (intimacyStoreItemToSend != null) {
            val item = intimacyStoreItemToSend!!
            AlertDialog(
                onDismissRequest = {
                    if (!isSendingIntimacyStoreItem) intimacyStoreItemToSend = null
                },
                title = { Text("Send ${item.name}", color = Color.White) },
                text = {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val recipients = activeMembers.filter { it.uid.isNotBlank() && it.uid != currentUser.uid }
                        if (recipients.isEmpty()) {
                            item { Text("No other room members are available.", color = Color.LightGray) }
                        }
                        items(recipients) { recipient ->
                            OutlinedButton(
                                onClick = {
                                    if (isSendingIntimacyStoreItem) return@OutlinedButton
                                    isSendingIntimacyStoreItem = true
                                    val requestId = UUID.randomUUID().toString()
                                    val senderRef = db.collection("users").document(currentUser.uid)
                                    val requestRef = db.collection("intimacy_requests").document(requestId)
                                    db.runTransaction { transaction ->
                                        val sender = transaction.get(senderRef)
                                        if (!sender.exists()) throw IllegalStateException("Sender profile is unavailable.")
                                        val balance = sender.getLong("diamonds") ?: 0L
                                        if (balance < item.diamondPrice) return@runTransaction null
                                        val newBalance = balance - item.diamondPrice
                                        transaction.update(
                                            senderRef,
                                            mapOf(
                                                "diamonds" to newBalance,
                                                "diamondsSpentCount" to FieldValue.increment(1),
                                                "wealthXp" to FieldValue.increment(item.diamondPrice * 10L)
                                            )
                                        )
                                        val bondType = when {
                                            item.id.contains("bestie", true) -> "bestie"
                                            item.id.contains("soulmate", true) -> "soulmate"
                                            item.id.contains("eternal", true) -> "eternal"
                                            item.id.contains("cyber", true) -> "cyber"
                                            else -> "couple"
                                        }
                                        transaction.set(
                                            requestRef,
                                            mapOf(
                                                "id" to requestId,
                                                "requestType" to "mic_link",
                                                "roomId" to room.id,
                                                "senderUid" to currentUser.uid,
                                                "senderName" to currentUser.name,
                                                "senderPhoto" to currentUser.photo,
                                                "recipientUid" to recipient.uid,
                                                "recipientName" to recipient.name,
                                                "recipientPhoto" to recipient.photoUrl,
                                                "giftId" to item.id,
                                                "giftName" to item.name,
                                                "giftEmoji" to item.iconEmoji,
                                                "bondType" to bondType,
                                                "status" to "pending",
                                                "timestamp" to System.currentTimeMillis()
                                            )
                                        )
                                        newBalance
                                    }.addOnSuccessListener { updatedBalance ->
                                        isSendingIntimacyStoreItem = false
                                        if (updatedBalance != null) {
                                            userDiamonds = updatedBalance
                                            intimacyStoreItemToSend = null
                                            Toast.makeText(context, "${item.name} sent to ${recipient.name} for acceptance.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "You need ${item.diamondPrice} diamonds to send this link.", Toast.LENGTH_LONG).show()
                                        }
                                    }.addOnFailureListener { error ->
                                        isSendingIntimacyStoreItem = false
                                        Log.e("AudioHangoutRoom", "Failed to send intimacy item ${item.id}", error)
                                        Toast.makeText(context, "Could not send request: ${error.localizedMessage ?: "Please try again."}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                enabled = !isSendingIntimacyStoreItem,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${recipient.name}  •  ${recipient.role}", color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { intimacyStoreItemToSend = null },
                        enabled = !isSendingIntimacyStoreItem
                    ) { Text("Cancel", color = Color.LightGray) }
                },
                containerColor = BrandCard,
                shape = RoundedCornerShape(18.dp)
            )
        }

        // Free 1-Hour Guest Access Expired Dialog
        if (showGuestLimitExpiredDialog) {
            RoomRechargePaywallModal(
                onDismiss = {
                    showGuestLimitExpiredDialog = false
                    onDismiss()
                },
                onRecharge = {
                    showGuestLimitExpiredDialog = false
                    onDismiss()
                }
            )
        }

        // 8. Luxury Gift Sheet Dialog
        if (showGiftStoreDialog) {
            GiftStoreDialog(
                currentDiamonds = userDiamonds,
                activeFrame = activeUserFrame,
                ownedFrames = userOwnedFrames,
                currentUser = currentUser,
                roomId = room.id,
                fixedGiftRecipient = giftCenterRecipient,
                onDismiss = {
                    showGiftStoreDialog = false
                    giftCenterRecipient = null
                },
                onDiamondsRecharged = { addedDiamonds ->
                    val newTotal = userDiamonds + addedDiamonds
                    db.collection("users").document(currentUser.uid).update("diamonds", newTotal)
                    userDiamonds = newTotal
                    if (room.id.isNotBlank()) {
                        val rechargeComment = mapOf(
                            "id" to java.util.UUID.randomUUID().toString(),
                            "userName" to currentUser.name,
                            "userPhoto" to (currentUser.photo ?: ""),
                            "message" to "💎 Recharged $addedDiamonds Diamonds! ✨",
                            "timestamp" to System.currentTimeMillis(),
                            "isSystem" to true,
                            "wealthLevel" to currentUser.getEffectiveWealthLevel()
                        )
                        db.collection("rooms").document(room.id).collection("live_comments").add(rechargeComment)
                    }
                    Toast.makeText(context, "🎉 Recharged $addedDiamonds Diamonds!", Toast.LENGTH_SHORT).show()
                },
                onBuyFrame = { frameId, cost ->
                    if (userDiamonds >= cost) {
                        val newDiamonds = userDiamonds - cost
                        val newOwned = (userOwnedFrames + frameId).distinct()
                        val expiryTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L) // 1 Month (30 Days)
                        db.collection("users").document(currentUser.uid).update(
                            mapOf(
                                "diamonds" to newDiamonds,
                                "ownedFrames" to newOwned,
                                "activeFrame" to frameId,
                                "frameExpiries.$frameId" to expiryTime
                            )
                        )
                        userDiamonds = newDiamonds
                        userOwnedFrames = newOwned
                        activeUserFrame = frameId
                        val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putLong("profile_diamonds", newDiamonds)
                            .putStringSet("profile_owned_frames", newOwned.toSet())
                            .putString("profile_active_frame", frameId)
                            .apply()
                        if (room.id.isNotBlank()) {
                            val frameComment = mapOf(
                                "id" to java.util.UUID.randomUUID().toString(),
                                "userName" to currentUser.name,
                                "userPhoto" to (currentUser.photo ?: ""),
                                "message" to "👑 Just acquired & equipped a Luxury Avatar Frame! ✨",
                                "timestamp" to System.currentTimeMillis(),
                                "isSystem" to true,
                                "wealthLevel" to currentUser.getEffectiveWealthLevel()
                            )
                            db.collection("rooms").document(room.id).collection("live_comments").add(frameComment)
                        }
                        Toast.makeText(context, "👑 Frame Purchased & Equipped for 1 Month (30 Days)!", Toast.LENGTH_SHORT).show()
                        AppNotificationHelper.showSystemNotification(
                            context = context,
                            title = "👑 Avatar Frame Unlocked!",
                            message = "You purchased & equipped a frame for 30 days! Cost: %,d 💎".format(cost),
                            isPurchaseAlert = true
                        )
                        val purchaseNotifId = UUID.randomUUID().toString()
                        val purchaseNotif = NotificationItem(
                            id = purchaseNotifId,
                            targetUserId = currentUser.uid,
                            senderId = currentUser.uid,
                            senderName = "LiveTogether Store",
                            senderPhoto = currentUser.photo,
                            title = "👑 Frame Purchase Confirmed",
                            message = "You purchased a frame for 30 days (1 Month) with %,d Diamonds! Enjoy your luxury frame.".format(cost),
                            type = "PURCHASE",
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )
                        db.collection("users").document(currentUser.uid).collection("notifications").document(purchaseNotifId).set(purchaseNotif)
                        db.collection("notifications").document(purchaseNotifId).set(purchaseNotif)
                    } else {
                        Toast.makeText(context, "⚠️ Insufficient Diamonds! Needed: %,d 💎 (Available: %,d 💎). Please recharge.".format(cost, userDiamonds), Toast.LENGTH_LONG).show()
                    }
                },
                onApplyFrame = { frameId ->
                    val targetFrame = if (activeUserFrame == frameId) "" else frameId
                    db.collection("users").document(currentUser.uid).update("activeFrame", targetFrame)
                    activeUserFrame = targetFrame
                    val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("profile_active_frame", targetFrame)
                        .apply()
                    Toast.makeText(context, if (targetFrame.isEmpty()) "Frame removed" else "👑 Frame equipped!", Toast.LENGTH_SHORT).show()
                    AppNotificationHelper.showSystemNotification(
                        context = context,
                        title = "👑 Profile Look Updated",
                        message = if (targetFrame.isEmpty()) "Frame removed from profile." else "Frame equipped on profile successfully!",
                        isPurchaseAlert = false
                    )
                },
                onSendGift = { giftItem, selectedRecipientUid, selectedRecipientName ->
                    if (userDiamonds >= giftItem.cost) {
                        val newDiamonds = userDiamonds - giftItem.cost
                        val xpEarned = giftItem.cost * 10L
                        val newWealthXp = currentUser.getEffectiveWealthXp() + xpEarned
                        val newWealthLevel = LevelBadgeSystem.calculateLevelFromXp(newWealthXp)

                        val userUpdates = mutableMapOf<String, Any>(
                            "diamonds" to newDiamonds,
                            "wealthXp" to newWealthXp,
                            "wealthLevel" to newWealthLevel
                        )
                        db.collection("users").document(currentUser.uid).update(userUpdates)
                        userDiamonds = newDiamonds

                        // Also award Charm XP and Host Commission Earnings to the room host in real-time
                        val recipientHostId = selectedRecipientUid.ifBlank {
                            targetHostId.ifBlank { room.hostUid.ifBlank { room.hostId } }
                        }
                        if (recipientHostId.isNotBlank()) {
                            db.collection("users").document(recipientHostId).get().addOnSuccessListener { hostSnap ->
                                val curCharmXp = hostSnap.getLong("charmXp") ?: 0L
                                val updatedCharmXp = curCharmXp + xpEarned
                                val updatedCharmLvl = LevelBadgeSystem.calculateLevelFromXp(updatedCharmXp)

                                val isHostMonetized = hostSnap.getBoolean("isHostMonetized") ?: hostSnap.getBoolean("profile_is_host_monetized") ?: (hostSnap.getString("role")?.equals("admin", true) == true || hostSnap.getString("name")?.equals("Devil", true) == true)

                                val hostWealthLvl = (hostSnap.getLong("wealthLevel") ?: 1L).toInt()
                                val commissionPercent = LevelBadgeSystem.getHostCommissionPercent(hostWealthLvl)
                                // 10,000 diamonds = ₹100 => 1 diamond = ₹0.01
                                val giftValueInr = giftItem.cost * 0.01
                                val hostEarningsInr = giftValueInr * (commissionPercent / 100.0)

                                val hostUpdateMap = mutableMapOf<String, Any>(
                                    "charmXp" to updatedCharmXp,
                                    "charmLevel" to updatedCharmLvl
                                )
                                if (isHostMonetized) {
                                    hostUpdateMap["earningsInr"] = FieldValue.increment(hostEarningsInr)
                                    hostUpdateMap["totalEarningsInr"] = FieldValue.increment(hostEarningsInr)
                                }

                                db.collection("users").document(recipientHostId).update(hostUpdateMap)

                                if (isHostMonetized) {
                                    // Real-Time Earning Transaction Record
                                    val txId = UUID.randomUUID().toString()
                                    val tx = EarningTransaction(
                                        id = txId,
                                        type = "GIFT_COMMISSION",
                                        title = "Gift received: ${giftItem.name} 🎁",
                                        amountInr = hostEarningsInr,
                                        diamonds = giftItem.cost,
                                        senderName = currentUser.name,
                                        upiId = "",
                                        status = "CREDITED",
                                        timestamp = System.currentTimeMillis()
                                    )
                                    db.collection("users").document(recipientHostId)
                                        .collection("earning_transactions").document(txId).set(tx)
                                }
                            }
                        }

                        if (newWealthLevel > currentUser.getEffectiveWealthLevel()) {
                            Toast.makeText(context, "🎉 LEVEL UP! You reached Wealth Level $newWealthLevel! 👑", Toast.LENGTH_LONG).show()
                        }

                        // Real-Time Intimacy/Bestie Gifting Proposal Trigger
                        val isIntimacyGift = giftItem.id.contains("ring", ignoreCase = true) || giftItem.id.contains("intimacy", ignoreCase = true) || giftItem.id.contains("bestie", ignoreCase = true) || giftItem.id.contains("couple", ignoreCase = true) || giftItem.id.contains("proposal", ignoreCase = true) || giftItem.id.contains("necklace", ignoreCase = true)
                        if (isIntimacyGift) {
                            val targetUid = selectedRecipientUid.ifBlank {
                                recipientHostId.ifBlank { targetHostId.ifBlank { room.hostUid.ifBlank { room.hostId } } }
                            }
                            val selectedRecipient = micSeats.firstOrNull { it.uid == targetUid }
                            val recipientName = if (selectedRecipientUid.isNotBlank()) {
                                selectedRecipientName.ifBlank { selectedRecipient?.name.orEmpty() }
                            } else {
                                room.hostName
                            }
                            val recipientPhoto = selectedRecipient?.photo
                                ?: giftCenterRecipient?.takeIf { it.uid == targetUid }?.photoUrl
                                ?: room.hostPhotoUrl
                            if (targetUid.isNotBlank() && targetUid != currentUser.uid) {
                                val reqId = "intimacy_req_${System.currentTimeMillis()}"
                                val reqData = mapOf(
                                    "id" to reqId,
                                    "requestType" to "mic_link",
                                    "roomId" to room.id,
                                    "senderUid" to currentUser.uid,
                                    "senderName" to currentUser.name,
                                    "senderPhoto" to currentUser.photo,
                                    "recipientUid" to targetUid,
                                    "recipientName" to recipientName,
                                    "recipientPhoto" to recipientPhoto,
                                    "giftId" to giftItem.id,
                                    "giftName" to giftItem.name,
                                    "giftEmoji" to giftItem.emoji,
                                    "bondType" to (if (giftItem.id.contains("bestie")) "bestie" else if (giftItem.id.contains("soulmate")) "soulmate" else if (giftItem.id.contains("eternal")) "eternal" else "couple"),
                                    "status" to "pending",
                                    "timestamp" to System.currentTimeMillis()
                                )
                                db.collection("intimacy_requests").document(reqId).set(reqData)
                            }
                        }

                        val giftEvent = GiftEvent(
                            id = UUID.randomUUID().toString(),
                            roomId = room.id,
                            senderId = currentUser.uid,
                            senderName = currentUser.name,
                            senderPhoto = currentUser.photo,
                            giftId = giftItem.id,
                            giftName = giftItem.name,
                            giftEmoji = giftItem.emoji,
                            cost = giftItem.cost,
                            animationType = giftItem.animationType,
                            timestamp = System.currentTimeMillis(),
                            targetUserId = selectedRecipientUid,
                            targetUserName = selectedRecipientName.ifBlank { "All Mics" }
                        )
                        activeGiftEvent = giftEvent
                        db.collection("rooms").document(room.id)
                        .collection("gift_events").document(giftEvent.id)
                        .set(giftEvent)

                        // Real-Time PK Battle Score & Supporters Sync
                        if (room.isPkActive) {
                            val supportedTeam = selectedPkTeamForBoost
                            val scoreField = if (supportedTeam == "red") "pkHost2Score" else "pkHost1Score"
                            db.collection("rooms").document(room.id).update(scoreField, FieldValue.increment(giftItem.cost.toLong()))
                            db.collection("rooms").document(room.id)
                                .collection("pk_supporters").document("${supportedTeam}_${currentUser.uid}").set(
                                    mapOf(
                                        "team" to supportedTeam,
                                        "uid" to currentUser.uid,
                                        "name" to currentUser.name,
                                        "photo" to currentUser.photo,
                                        "points" to FieldValue.increment(giftItem.cost.toLong())
                                    ),
                                    SetOptions.merge()
                                )
                        }

                        showGiftStoreDialog = false
                        giftCenterRecipient = null
                    } else {
                        Toast.makeText(context, "⚠️ Insufficient Diamonds! Needed: %,d 💎 to send ${giftItem.name} (Available: %,d 💎). Please recharge.".format(giftItem.cost, userDiamonds), Toast.LENGTH_LONG).show()
                    }
                },
                micSeats = micSeats,
                onSendMicroGift = { giftItem, quantity, targetSeatIndex, targetUserName, fromBackpack ->
                    val occupiedSeats = micSeats.filter { it.uid.isNotBlank() }
                    val multiplier = if (targetSeatIndex == -1) maxOf(1, occupiedSeats.size) else 1
                    val totalCost = giftItem.cost * quantity * multiplier
                    val canProceed = fromBackpack || userDiamonds >= totalCost

                    if (canProceed) {
                        if (!fromBackpack) {
                            val newDiamonds = userDiamonds - totalCost
                            val xpEarned = totalCost * 10L
                            val newWealthXp = currentUser.getEffectiveWealthXp() + xpEarned
                            val newWealthLevel = LevelBadgeSystem.calculateLevelFromXp(newWealthXp)
                            db.collection("users").document(currentUser.uid).update(
                                mapOf(
                                    "diamonds" to newDiamonds,
                                    "wealthXp" to newWealthXp,
                                    "wealthLevel" to newWealthLevel
                                )
                            )
                            userDiamonds = newDiamonds
                        } else {
                            val curBackpack = currentUser.backpack.toMutableMap()
                            val curCount = curBackpack[giftItem.id] ?: 0
                            val deductCount = quantity * multiplier
                            val rem = maxOf(0, curCount - deductCount)
                            if (rem > 0) curBackpack[giftItem.id] = rem else curBackpack.remove(giftItem.id)
                            db.collection("users").document(currentUser.uid).update("backpack", curBackpack)
                        }

                        // Broadcast Gift Event for real-time trajectory & firecracker explosion
                        val giftEvent = GiftEvent(
                            id = UUID.randomUUID().toString(),
                            roomId = room.id,
                            senderId = currentUser.uid,
                            senderName = currentUser.name,
                            senderPhoto = currentUser.photo,
                            giftId = giftItem.id,
                            giftName = giftItem.name,
                            giftEmoji = giftItem.emoji,
                            cost = totalCost,
                            animationType = giftItem.animationType,
                            timestamp = System.currentTimeMillis(),
                            targetUserId = if (targetSeatIndex >= 0) micSeats.find { it.seatIndex == targetSeatIndex }?.uid ?: "" else "",
                            targetUserName = targetUserName,
                            quantity = quantity,
                            targetMicSeatIndex = targetSeatIndex,
                            drawableResId = giftItem.drawableResId,
                            fromBackpack = fromBackpack
                        )
                        activeGiftEvent = giftEvent
                        db.collection("rooms").document(room.id)
                            .collection("gift_events").document(giftEvent.id)
                            .set(giftEvent)

                        // Real-Time PK Battle Score sync
                        if (room.isPkActive) {
                            val supportedTeam = selectedPkTeamForBoost
                            val scoreField = if (supportedTeam == "red") "pkHost2Score" else "pkHost1Score"
                            db.collection("rooms").document(room.id).update(scoreField, FieldValue.increment(totalCost.toLong()))
                        }

                        showGiftStoreDialog = false
                    } else {
                        Toast.makeText(context, "⚠️ Insufficient Diamonds! Needed: %,d 💎".format(totalCost), Toast.LENGTH_SHORT).show()
                    }
                },
                onClaimStarterGifts = {
                    val starterPack = mapOf(
                        "gift_single_rose" to 5,
                        "gift_firecracker_bomb" to 1
                    )
                    db.collection("users").document(currentUser.uid).update("backpack", starterPack)
                    Toast.makeText(context, "🎉 Starter Gifts Claimed! 5x Roses & 1x Firecracker Bomb added to your Backpack!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Room Exit or Keep (Mini-Player PIP) Options Dialog
        if (showExitOrKeepDialog) {
            RoomExitOptionsDialog(
                roomTitle = room.title,
                isHost = isUserHost,
                onDismiss = { showExitOrKeepDialog = false },
                onKeep = {
                    showExitOrKeepDialog = false
                    onMinimize()
                },
                onExit = {
                    showExitOrKeepDialog = false
                    executeHostOrUserExit()
                }
            )
        }

        // -------------------------------------------------------------
        // IN-ROOM COMMENT INPUT OVERLAY (Native imePadding auto-floating above keyboard)
        // -------------------------------------------------------------
        if (showCommentInputDialog) {
            Dialog(
                onDismissRequest = {
                    showCommentInputDialog = false
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                val inputView = LocalView.current
                SideEffect {
                    var parent = inputView.parent
                    while (parent != null && parent !is android.app.Dialog && parent !is DialogWindowProvider) {
                        parent = parent.parent
                    }
                    (parent as? DialogWindowProvider)?.window?.setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showCommentInputDialog = false
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                        .systemBarsPadding()
                        .imePadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* absorb click */ }
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        color = Color(0xFF1B1418),
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            // Quick Emojis Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val quickEmojis = listOf("❤️", "🔥", "👏", "🎉", "👑", "🌹", "😂", "✨")
                                quickEmojis.forEach { emoji ->
                                    Text(
                                        text = emoji,
                                        fontSize = 20.sp,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable {
                                                commentInputText += emoji
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            // Input Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .border(1.dp, BrandBorder.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val focusRequester = remember { FocusRequester() }
                                LaunchedEffect(Unit) {
                                    delay(100)
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                }

                                BasicTextField(
                                    value = commentInputText,
                                    onValueChange = { commentInputText = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester)
                                        .padding(vertical = 10.dp),
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(BrandCoral),
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Send,
                                        capitalization = KeyboardCapitalization.Sentences
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (commentInputText.isNotBlank()) {
                                                val isFirstTimeFreeGuest = !userHasRecharged && !currentUser.hasRecharged
                                                    && userRechargeCount == 0 && currentUser.rechargeCount == 0
                                                    && userDiamonds <= 0L && currentUser.diamonds <= 0L
                                                    && currentUser.getVipLevel() == 0 && !currentUser.isVipOrPlatinum()
                                                    && !currentUser.isFounder() && !currentUser.isCeo() && !currentUser.isStaffMember()
                                                    && room.hostId != currentUser.uid && room.hostUid != currentUser.uid
                                                if (isFirstTimeFreeGuest && userTotalRoomTimeSeconds >= 3600L) {
                                                    android.widget.Toast.makeText(context, "🚫 1-hour free room access expired. Recharge ₹59 to chat.", android.widget.Toast.LENGTH_LONG).show()
                                                    showGuestLimitExpiredDialog = true
                                                    return@KeyboardActions
                                                }
                                                val msg = commentInputText.trim()
                                                val badWord = AbuseFilterEngine.findAbusiveWord(msg)
                                                if (badWord != null) {
                                                    android.widget.Toast.makeText(context, "🚫 Blocked: Inappropriate word ($badWord) is prohibited in LiveTogather!", android.widget.Toast.LENGTH_SHORT).show()
                                                    return@KeyboardActions
                                                }
                                                val commentDoc = LiveRoomComment(
                                                    id = UUID.randomUUID().toString(),
                                                    roomId = room.id,
                                                    userId = currentUser.uid,
                                                    userName = currentUser.name,
                                                    userPhoto = currentUser.photo,
                                                    tier = if (currentUser.getVipLevel() > 0) "VIP" else if (currentUser.isVipOrPlatinum()) "Platinum" else "Standard",
                                                    message = msg,
                                                    timestamp = System.currentTimeMillis(),
                                                    chatFrameId = currentUser.activeChatFrame,
                                                    vipLevel = currentUser.getVipLevel(),
                                                    isFounder = currentUser.isFounder()
                                                )
                                                db.collection("rooms").document(room.id)
                                                    .collection("live_comments").document(commentDoc.id)
                                                    .set(commentDoc)
                                                commentInputText = ""
                                                showCommentInputDialog = false
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                            }
                                        }
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (commentInputText.isEmpty()) {
                                            Text(
                                                text = "Say something nice...",
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )

                                IconButton(
                                    onClick = {
                                        if (commentInputText.isNotBlank()) {
                                            val isFirstTimeFreeGuest = !userHasRecharged && !currentUser.hasRecharged
                                                    && userRechargeCount == 0 && currentUser.rechargeCount == 0
                                                    && userDiamonds <= 0L && currentUser.diamonds <= 0L
                                                    && currentUser.getVipLevel() == 0 && !currentUser.isVipOrPlatinum()
                                                    && !currentUser.isFounder() && !currentUser.isCeo() && !currentUser.isStaffMember()
                                                    && room.hostId != currentUser.uid && room.hostUid != currentUser.uid
                                            if (isFirstTimeFreeGuest && userTotalRoomTimeSeconds >= 3600L) {
                                                android.widget.Toast.makeText(context, "🚫 1-hour free room access expired. Recharge ₹59 to chat.", android.widget.Toast.LENGTH_LONG).show()
                                                showGuestLimitExpiredDialog = true
                                                return@IconButton
                                            }
                                            val msg = commentInputText.trim()
                                            val badWord = AbuseFilterEngine.findAbusiveWord(msg)
                                            if (badWord != null) {
                                                android.widget.Toast.makeText(context, "🚫 Blocked: Inappropriate word ($badWord) is prohibited in LiveTogather!", android.widget.Toast.LENGTH_SHORT).show()
                                                return@IconButton
                                            }
                                            val commentDoc = LiveRoomComment(
                                                id = UUID.randomUUID().toString(),
                                                roomId = room.id,
                                                userId = currentUser.uid,
                                                userName = currentUser.name,
                                                userPhoto = currentUser.photo,
                                                tier = if (currentUser.getVipLevel() > 0) "VIP" else if (currentUser.isVipOrPlatinum()) "Platinum" else "Standard",
                                                message = msg,
                                                timestamp = System.currentTimeMillis(),
                                                chatFrameId = currentUser.activeChatFrame,
                                                vipLevel = currentUser.getVipLevel(),
                                                isFounder = currentUser.isFounder()
                                            )
                                            db.collection("rooms").document(room.id)
                                                .collection("live_comments").document(commentDoc.id)
                                                .set(commentDoc)
                                            commentInputText = ""
                                            showCommentInputDialog = false
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(BrandCoral, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // IN-ROOM INBOX & DIRECT CHAT DIALOG
        // -------------------------------------------------------------
        if (showInRoomInboxDialog) {
            InRoomInboxDialog(
                currentUser = currentUser,
                allProfiles = allProfiles,
                activeMembers = activeMembers,
                db = db,
                onDismiss = { showInRoomInboxDialog = false }
            )
        }
    }
}

// -------------------------------------------------------------
// IN-ROOM INBOX & DIRECT MESSAGING DIALOG COMPONENT
// -------------------------------------------------------------
@Composable
fun InRoomInboxDialog(
    currentUser: UserProfile,
    allProfiles: List<UserProfile>,
    activeMembers: List<RoomMember>,
    db: FirebaseFirestore,
    onDismiss: () -> Unit
) {
    var selectedPartner by remember { mutableStateOf<UserProfile?>(null) }
    var selectedMatchId by remember { mutableStateOf<String?>(null) }
    var selectedActionMatch by remember { mutableStateOf<Pair<MatchItem, UserProfile>?>(null) }
    var selectedProfileMember by remember { mutableStateOf<RoomMember?>(null) }
    var selectedProfileMatchId by remember { mutableStateOf<String?>(null) }
    val archivedMatchIds = remember { mutableStateListOf<String>() }
    val matches = remember { mutableStateListOf<MatchItem>() }
    var activeTab by remember { mutableStateOf(0) } // 0: Messages, 1: Room Members
    val context = LocalContext.current

    // Listen to matches for the current user
    DisposableEffect(currentUser.uid) {
        if (currentUser.uid.isBlank()) return@DisposableEffect onDispose {}
        val listener = db.collection("matches")
            .whereArrayContains("users", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MatchItem::class.java)?.copy(matchId = doc.id)
                    }.sortedByDescending { it.timestamp }
                    matches.clear()
                    matches.addAll(list)
                }
            }
        onDispose { listener.remove() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
                .imePadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.82f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* absorb clicks */ },
                color = Color(0xFF140E14),
                tonalElevation = 8.dp
            ) {
                if (selectedPartner == null) {
                    // CONVERSATIONS / INBOX LIST VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 14.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(BrandGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mail,
                                        contentDescription = "Inbox",
                                        tint = BrandGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "In-Room Messages & Inbox",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Chat live without leaving audio room",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Tab Row (Chats vs Room Members)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (activeTab == 0) BrandCoral else Color.White.copy(alpha = 0.08f))
                                    .clickable { activeTab = 0 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Recent Chats (${matches.size})",
                                    color = if (activeTab == 0) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (activeTab == 1) BrandCoral else Color.White.copy(alpha = 0.08f))
                                    .clickable { activeTab = 1 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val roomOthers = activeMembers.filter { it.uid != currentUser.uid }
                                Text(
                                    text = "Room Members (${roomOthers.size})",
                                    color = if (activeTab == 1) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)

                        if (activeTab == 0) {
                            // RECENT CHATS LIST
                            if (matches.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("📬", fontSize = 42.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "No direct messages yet",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tap on 'Room Members' above to start a live 1-on-1 chat!",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                val visibleMatches = matches.filter { !archivedMatchIds.contains(it.matchId) }
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(visibleMatches) { match ->
                                        val otherUid = match.users.firstOrNull { it != currentUser.uid } ?: ""
                                        val partner = allProfiles.find { it.uid == otherUid }
                                            ?: UserProfile(uid = otherUid, name = "Live User")
                                        val isOnlineInRoom = activeMembers.any { it.uid == otherUid }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White.copy(alpha = 0.05f))
                                                .clickable {
                                                    selectedPartner = partner
                                                    selectedMatchId = match.matchId
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box {
                                                AsyncImage(
                                                    model = partner.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .border(1.dp, BrandBorder, CircleShape)
                                                        .clickable {
                                                            selectedProfileMember = RoomMember(
                                                                uid = partner.uid,
                                                                userId = partner.userId,
                                                                name = partner.name,
                                                                photoUrl = partner.photo
                                                            )
                                                            selectedProfileMatchId = match.matchId
                                                        },
                                                    contentScale = ContentScale.Crop
                                                )
                                                if (isOnlineInRoom) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(11.dp)
                                                            .clip(CircleShape)
                                                            .background(BrandGreen)
                                                            .border(1.5.dp, Color(0xFF140E14), CircleShape)
                                                            .align(Alignment.BottomEnd)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = partner.name,
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.clickable {
                                                            selectedProfileMember = RoomMember(
                                                                uid = partner.uid,
                                                                userId = partner.userId,
                                                                name = partner.name,
                                                                photoUrl = partner.photo
                                                            )
                                                            selectedProfileMatchId = match.matchId
                                                        }
                                                    )
                                                    if (isOnlineInRoom) {
                                                        Text(
                                                            text = "In Room 🎙️",
                                                            color = BrandGreen,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = match.lastMessage.ifBlank { "Tap to chat" },
                                                    color = Color.LightGray,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            IconButton(
                                                onClick = { selectedActionMatch = Pair(match, partner) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = Color.LightGray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // ROOM MEMBERS TAB
                            val roomOthers = activeMembers.filter { it.uid != currentUser.uid }
                            if (roomOthers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No other members in room yet",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(roomOthers) { member ->
                                        val partner = allProfiles.find { it.uid == member.uid }
                                            ?: UserProfile(uid = member.uid, name = member.name, photo = member.photoUrl)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White.copy(alpha = 0.05f))
                                                .clickable {
                                                    val mId = if (currentUser.uid < member.uid) "${currentUser.uid}_${member.uid}" else "${member.uid}_${currentUser.uid}"
                                                    selectedPartner = partner
                                                    selectedMatchId = mId
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = member.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, BrandBorder, CircleShape)
                                                    .clickable {
                                                        selectedProfileMember = member
                                                        selectedProfileMatchId = listOf(currentUser.uid, member.uid).sorted().joinToString("_")
                                                    },
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = member.name,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.clickable {
                                                        selectedProfileMember = member
                                                        selectedProfileMatchId = listOf(currentUser.uid, member.uid).sorted().joinToString("_")
                                                    }
                                                )
                                                Text(
                                                    text = if (member.role == "host") "Room Host 👑" else "Listener 🎧",
                                                    color = if (member.role == "host") BrandGold else Color.Gray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(BrandCoral)
                                                    .clickable {
                                                        val mId = if (currentUser.uid < member.uid) "${currentUser.uid}_${member.uid}" else "${member.uid}_${currentUser.uid}"
                                                        selectedPartner = partner
                                                        selectedMatchId = mId
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                                            ) {
                                                Text("Chat 💬", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 1-ON-1 DIRECT CHAT SCREEN
                    val partner = selectedPartner!!
                    val matchId = selectedMatchId ?: if (currentUser.uid < partner.uid) "${currentUser.uid}_${partner.uid}" else "${partner.uid}_${currentUser.uid}"
                    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
                    var messageInput by remember { mutableStateOf("") }
                    val listState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()

                    DisposableEffect(matchId) {
                        val msgListener = db.collection("matches").document(matchId)
                            .collection("messages")
                            .orderBy("timestamp", Query.Direction.ASCENDING)
                            .limitToLast(50)
                            .addSnapshotListener { snapshot, error ->
                                if (error != null) return@addSnapshotListener
                                if (snapshot != null) {
                                    val list = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
                                    chatMessages.clear()
                                    chatMessages.addAll(list)
                                    if (list.isNotEmpty()) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(list.size - 1)
                                        }
                                    }
                                }
                            }
                        onDispose { msgListener.remove() }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Chat Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    selectedPartner = null
                                    selectedMatchId = null
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            AsyncImage(
                                model = partner.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = partner.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Live In-Room Chat",
                                    color = BrandGreen,
                                    fontSize = 10.sp
                                )
                            }
                            var showChatMenu by remember { mutableStateOf(false) }
                            val isBlocked = currentUser.blockedUserIds.contains(partner.uid)
                            Box {
                                IconButton(
                                    onClick = { showChatMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showChatMenu,
                                    onDismissRequest = { showChatMenu = false },
                                    modifier = Modifier.background(BrandCard)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🧹 Clear Chat History", color = Color.White, fontSize = 13.sp) },
                                        onClick = {
                                            showChatMenu = false
                                            db.collection("matches").document(matchId).collection("messages").get()
                                                .addOnSuccessListener { sn ->
                                                    for (d in sn.documents) { d.reference.delete() }
                                                    db.collection("matches").document(matchId).update("lastMessage", "")
                                                    chatMessages.clear()
                                                    Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (isBlocked) "✅ Unblock User" else "🚫 Block User",
                                                color = if (isBlocked) BrandGreen else Color(0xFFFF5252),
                                                fontSize = 13.sp
                                            )
                                        },
                                        onClick = {
                                            showChatMenu = false
                                            val updatedBlocked = if (isBlocked) {
                                                currentUser.blockedUserIds - partner.uid
                                            } else {
                                                currentUser.blockedUserIds + partner.uid
                                            }
                                            db.collection("users").document(currentUser.uid)
                                                .update("blockedUserIds", updatedBlocked)
                                            Toast.makeText(
                                                context,
                                                if (isBlocked) "${partner.name} unblocked" else "${partner.name} blocked",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🚨 Report User", color = Color(0xFFFF9100), fontSize = 13.sp) },
                                        onClick = {
                                            showChatMenu = false
                                            val reportData = hashMapOf(
                                                "reporterId" to currentUser.uid,
                                                "reporterName" to currentUser.name,
                                                "reportedUserId" to partner.uid,
                                                "reportedUserName" to partner.name,
                                                "reason" to "Reported from in-room chat",
                                                "timestamp" to System.currentTimeMillis()
                                            )
                                            db.collection("reports").add(reportData)
                                            Toast.makeText(context, "Report submitted to moderators. Thank you.", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    HorizontalDivider(color = BrandBorder, thickness = 0.5.dp)
                                    DropdownMenuItem(
                                        text = { Text("🗑️ Delete Conversation", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            showChatMenu = false
                                            db.collection("matches").document(matchId).collection("messages").get()
                                                .addOnSuccessListener { sn ->
                                                    for (d in sn.documents) { d.reference.delete() }
                                                    db.collection("matches").document(matchId).delete()
                                                    selectedPartner = null
                                                    selectedMatchId = null
                                                    Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)

                        // Messages History List
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chatMessages) { msg ->
                                val isMe = msg.senderId == currentUser.uid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 260.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 14.dp,
                                                    topEnd = 14.dp,
                                                    bottomStart = if (isMe) 14.dp else 2.dp,
                                                    bottomEnd = if (isMe) 2.dp else 14.dp
                                                )
                                            )
                                            .background(
                                                if (isMe) Brush.linearGradient(listOf(BrandCoral, Color(0xFFFF4081)))
                                                else Brush.linearGradient(listOf(Color(0xFF2B2129), Color(0xFF382A35)))
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = msg.text,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Message Input Field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, BrandBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = messageInput,
                                    onValueChange = { messageInput = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 8.dp),
                                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                    cursorBrush = SolidColor(BrandCoral),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (messageInput.isNotBlank()) {
                                                val text = messageInput.trim()
                                                val badWord = AbuseFilterEngine.findAbusiveWord(text)
                                                if (badWord != null) {
                                                    android.widget.Toast.makeText(context, "🚫 Blocked: Inappropriate word ($badWord) is prohibited in LiveTogather!", android.widget.Toast.LENGTH_SHORT).show()
                                                    return@KeyboardActions
                                                }
                                                val newMsg = ChatMessage(senderId = currentUser.uid, text = text, timestamp = System.currentTimeMillis())
                                                db.collection("matches").document(matchId).collection("messages").add(newMsg)
                                                db.collection("matches").document(matchId).set(
                                                    mapOf(
                                                        "matchId" to matchId,
                                                        "users" to listOf(currentUser.uid, partner.uid),
                                                        "lastMessage" to text,
                                                        "timestamp" to System.currentTimeMillis()
                                                    ),
                                                    SetOptions.merge()
                                                )
                                                messageInput = ""
                                            }
                                        }
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (messageInput.isEmpty()) {
                                            Text("Type a message...", color = Color.Gray, fontSize = 13.sp)
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (messageInput.isNotBlank()) {
                                        val text = messageInput.trim()
                                        val badWord = AbuseFilterEngine.findAbusiveWord(text)
                                        if (badWord != null) {
                                            android.widget.Toast.makeText(context, "🚫 Blocked: Inappropriate word ($badWord) is prohibited in LiveTogather!", android.widget.Toast.LENGTH_SHORT).show()
                                            return@IconButton
                                        }
                                        val newMsg = ChatMessage(senderId = currentUser.uid, text = text, timestamp = System.currentTimeMillis())
                                        db.collection("matches").document(matchId).collection("messages").add(newMsg)
                                        db.collection("matches").document(matchId).set(
                                            mapOf(
                                                "matchId" to matchId,
                                                "users" to listOf(currentUser.uid, partner.uid),
                                                "lastMessage" to text,
                                                "timestamp" to System.currentTimeMillis()
                                            ),
                                            SetOptions.merge()
                                        )
                                        messageInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BrandCoral, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        // Inbox Action Dialog for Recent Chats
        if (selectedActionMatch != null) {
            val (selMatch, selPartner) = selectedActionMatch!!
            val isBlocked = currentUser.blockedUserIds.contains(selPartner.uid)

            AlertDialog(
                onDismissRequest = { selectedActionMatch = null },
                containerColor = Color(0xFF1B141E),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = selPartner.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(selPartner.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Archive Option
                        TextButton(
                            onClick = {
                                archivedMatchIds.add(selMatch.matchId)
                                selectedActionMatch = null
                                Toast.makeText(context, "Chat archived", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("📁", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Archive Chat", color = Color.White, fontSize = 14.sp)
                            }
                        }

                        // Clear Chat History Option
                        TextButton(
                            onClick = {
                                db.collection("matches").document(selMatch.matchId).collection("messages").get()
                                    .addOnSuccessListener { sn ->
                                        for (d in sn.documents) { d.reference.delete() }
                                        db.collection("matches").document(selMatch.matchId).update("lastMessage", "")
                                        Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                                    }
                                selectedActionMatch = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("🧹", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Clear Chat History", color = Color.White, fontSize = 14.sp)
                            }
                        }

                        // Block / Unblock Option
                        TextButton(
                            onClick = {
                                val updatedBlocked = if (isBlocked) {
                                    currentUser.blockedUserIds - selPartner.uid
                                } else {
                                    currentUser.blockedUserIds + selPartner.uid
                                }
                                db.collection("users").document(currentUser.uid)
                                    .update("blockedUserIds", updatedBlocked)
                                Toast.makeText(
                                    context,
                                    if (isBlocked) "${selPartner.name} unblocked" else "${selPartner.name} blocked",
                                    Toast.LENGTH_SHORT
                                ).show()
                                selectedActionMatch = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isBlocked) "✅" else "🚫", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    if (isBlocked) "Unblock User" else "Block User",
                                    color = if (isBlocked) BrandGreen else Color(0xFFFF5252),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Report User Option
                        TextButton(
                            onClick = {
                                val reportData = hashMapOf(
                                    "reporterId" to currentUser.uid,
                                    "reporterName" to currentUser.name,
                                    "reportedUserId" to selPartner.uid,
                                    "reportedUserName" to selPartner.name,
                                    "reason" to "Reported from in-room inbox",
                                    "timestamp" to System.currentTimeMillis()
                                )
                                db.collection("reports").add(reportData)
                                Toast.makeText(context, "Report submitted. Thank you.", Toast.LENGTH_SHORT).show()
                                selectedActionMatch = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("🚨", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Report User", color = Color(0xFFFF9100), fontSize = 14.sp)
                            }
                        }

                        HorizontalDivider(color = BrandBorder, thickness = 0.5.dp)

                        // Delete Conversation Option
                        TextButton(
                            onClick = {
                                db.collection("matches").document(selMatch.matchId).collection("messages").get()
                                    .addOnSuccessListener { sn ->
                                        for (d in sn.documents) { d.reference.delete() }
                                        db.collection("matches").document(selMatch.matchId).delete()
                                        Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show()
                                    }
                                selectedActionMatch = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("🗑️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Delete Conversation", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { selectedActionMatch = null }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            )
        }

        if (selectedProfileMember != null) {
            val member = selectedProfileMember!!
            val profile = allProfiles.find { it.uid == member.uid }
                ?: UserProfile(uid = member.uid, userId = member.userId, name = member.name, photo = member.photoUrl)
            RoomMemberProfileSheet(
                member = member,
                userProfile = profile,
                currentUser = currentUser,
                isHost = false,
                roomId = "",
                db = db,
                onDismiss = { selectedProfileMember = null },
                onLeaveRoom = { selectedProfileMember = null },
                onLike = { recordSwipe(db, currentUser.uid, member.uid, "like") },
                onDirectMessage = {
                    selectedProfileMember = null
                    selectedPartner = profile
                    selectedMatchId = selectedProfileMatchId
                        ?: listOf(currentUser.uid, member.uid).sorted().joinToString("_")
                },
                onRequestMessage = {
                    selectedProfileMember = null
                    selectedPartner = profile
                    selectedMatchId = selectedProfileMatchId
                        ?: listOf(currentUser.uid, member.uid).sorted().joinToString("_")
                },
                onKickOut = {},
                onBan = {},
                onBlock = {
                    db.collection("users").document(currentUser.uid)
                        .update("blockedUserIds", FieldValue.arrayUnion(member.uid))
                },
                onTagMember = {},
                showRoomActions = false
            )
        }
    }
}
}

// -------------------------------------------------------------
// 7. CATEGORY SUB-ROOM CARD (SLEEK SLENDER HORIZONTAL ROW - IMAGE 1 STYLE)
// -------------------------------------------------------------
@Composable
fun CategorySubRoomCard(
    room: RoomItem,
    activeCount: Int,
    hostUser: UserProfile? = null,
    currentUser: UserProfile = UserProfile(),
    onJoin: () -> Unit
) {
    val isUserHostOfThisRoom = currentUser.uid.isNotBlank() && (currentUser.uid == room.hostUid || currentUser.uid == room.hostId)
    val cardPhoto = room.hostPhotoUrl.ifEmpty { room.banner_image_url.ifEmpty { "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3" } }

    val infiniteTransition = rememberInfiniteTransition(label = "sub_room_card_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_pulse"
    )

    Surface(
        color = Color(0xFF141722),
        shape = RoundedCornerShape(16.dp),
        border = if (room.isHostLive) {
            BorderStroke(
                1.2.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF2A6D).copy(alpha = pulseAlpha),
                        Color(0xFF00E5FF).copy(alpha = pulseAlpha),
                        Color(0xFFFFD700).copy(alpha = pulseAlpha * 0.75f)
                    )
                )
            )
        } else {
            BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJoin() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 0. BLINKING VERTICAL ACCENT LINE (Pulsing live indicator bar)
            if (room.isHostLive) {
                Box(
                    modifier = Modifier
                        .width(3.2.dp)
                        .height(46.dp)
                        .clip(RoundedCornerShape(1.6.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFF2A6D).copy(alpha = pulseAlpha),
                                    Color(0xFF00E5FF).copy(alpha = pulseAlpha)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.width(7.dp))
            }

            // 1. LEFT: Square Cover Image with rounded corners & subtle LIVE tag
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (room.isHostLive) Color(0xFFFF1744).copy(alpha = pulseAlpha) else Color.White.copy(alpha = 0.12f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                AsyncImage(
                    model = cardPhoto,
                    contentDescription = room.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (room.isHostLive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color(0xFFE91E63).copy(alpha = 0.88f * pulseAlpha))
                            .padding(vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 2. MIDDLE: Room Title, Host Crown + Real Badges, Status/Music
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Room Title
                Text(
                    text = "${room.title.ifBlank { room.roomName }} ${room.emoji}",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Real Badges & Host Crown Row (Matching Reference Image 1)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isUserHostOfThisRoom) "👑 You" else "👑 ${room.hostName.ifBlank { "Host" }.take(8)}",
                        color = BrandGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    val pkLevel = hostUser?.pkLevel ?: 1
                    SilverSwordsRankBadge(rank = pkLevel, modifier = Modifier.scale(0.72f).offset(x = (-3).dp))

                    val wealthLvl = hostUser?.wealthLevel ?: 1
                    BlueLevelBadgePill(level = wealthLvl, modifier = Modifier.scale(0.75f).offset(x = (-6).dp))

                    val charmLvl = hostUser?.charmLevel ?: 1
                    SapphireCharmBadgePill(level = charmLvl, modifier = Modifier.scale(0.75f).offset(x = (-9).dp))
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Live status or now playing track
                Text(
                    text = if (room.isPlaying && room.nowPlayingTitle.isNotBlank()) "🎶 ${room.nowPlayingTitle.take(22)}" else if (room.isHostLive) "🔴 Live Broadcast" else "⏳ Host will be live soon",
                    color = if (room.isHostLive) Color(0xFF34D399) else Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. RIGHT: 18-20 Line Slender Equalizer Wave + Live Viewers Count (Matching Reference Image 1)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                MusicVisualizerWave(
                    isPlaying = room.isHostLive,
                    modifier = Modifier.height(20.dp),
                    barCount = 18
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("📶", fontSize = 8.5.sp)
                    Text(
                        text = "$activeCount",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. CATEGORY HUB / LOBBY DIALOG (COMPACT SLIM HEADER + TOP PROMINENT HOSTS + SLENDER LIST)
// -------------------------------------------------------------
@Composable
fun CategoryHubLobbyDialog(
    category: IntentCategory,
    categoryBannerUrl: String,
    allRooms: List<RoomItem>,
    allUsers: List<UserProfile>,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: (RoomItem) -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val liveCategoryRooms = remember { mutableStateListOf<RoomItem>() }

    LaunchedEffect(category.id) {
        db.collection("rooms")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("CategoryHub", "Error listening to category rooms: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetched = snapshot.documents.mapNotNull { parseRoomFromDoc(it) }
                    liveCategoryRooms.clear()
                    liveCategoryRooms.addAll(fetched)
                }
            }
    }

    val categoryRooms = remember(
        liveCategoryRooms.map { "${it.id}_${it.title}_${it.roomName}_${it.isHostLive}_${it.isLive}_${it.activeUserCount}_${it.roomType}" },
        allRooms.map { "${it.id}_${it.title}_${it.roomName}_${it.isHostLive}_${it.isLive}_${it.activeUserCount}_${it.roomType}" },
        category.id,
        currentUser.uid
    ) {
        val map = linkedMapOf<String, RoomItem>()
        fun matchesCategory(r: RoomItem): Boolean {
            if (r.id.isBlank() || r.title.isBlank()) return false
            if (r.statusMessage.equals("DELETED", ignoreCase = true) || r.statusMessage.equals("CLOSED", ignoreCase = true)) {
                return false
            }
            // Strict Available/Live Filter (Offline rooms of other hosts are not listed)
            val isHostOfThisRoom = currentUser.uid.isNotBlank() && (r.hostUid == currentUser.uid || r.hostId == currentUser.uid)
            val isRoomAvailable = r.isHostLive || r.isLive || isHostOfThisRoom
            if (!isRoomAvailable) {
                return false
            }

            if (category.id == "video_stream") {
                // In Video Stream: ONLY real video rooms!
                val isVideo = (r.category == "video_stream" || r.roomType == "video" || r.roomType == "video_pk" || r.isVideoRoom) && r.roomType != "audio"
                return isVideo
            } else {
                // In Audio categories: ONLY rooms of this category in audio mode!
                return r.category == category.id && r.roomType != "video" && r.roomType != "video_pk"
            }
        }
        allRooms.filter { matchesCategory(it) }.forEach { map[it.id] = it }
        liveCategoryRooms.filter { matchesCategory(it) }.forEach { map[it.id] = it }
        map.values.toList()
    }

    // Dynamic Multi-Tier Real-Time Auto-Ranking Comparator (NO DUMMY DATA)
    // 1. Live status (Live on top)
    // 2. Active viewers volume (Real-time Firestore user count)
    // 3. Host wealth & charm level
    // 4. Creation / update timestamp
    val sortedCategoryRooms = remember(categoryRooms, allUsers) {
        categoryRooms.sortedWith(
            compareByDescending<RoomItem> { it.isHostLive }
                .thenByDescending { room ->
                    allUsers.count { it.current_room == room.id }.coerceAtLeast(room.activeUserCount)
                }
                .thenByDescending { room ->
                    val hostUid = room.hostUid.ifBlank { room.hostId }
                    val host = allUsers.find { it.uid == hostUid }
                    (host?.wealthLevel ?: 1) + (host?.charmLevel ?: 1) + (host?.followerCount ?: 0) / 10
                }
                .thenByDescending { it.createdAt }
        )
    }

    // Top Popular Star Hosts Horizontal Carousel (With Real User Frames, NO DUMMY FRAMES)
    val topPopularHosts = remember(sortedCategoryRooms, allUsers) {
        val hostsFromRooms = sortedCategoryRooms.mapNotNull { room ->
            val hostUid = room.hostUid.ifBlank { room.hostId }
            val hostUser = allUsers.find { it.uid == hostUid } ?: UserProfile(
                uid = hostUid,
                name = room.hostName,
                photo = room.hostPhotoUrl
            )
            hostUser to room
        }.distinctBy { it.first.uid }

        hostsFromRooms.sortedWith(
            compareByDescending<Pair<UserProfile, RoomItem>> { it.second.isHostLive }
                .thenByDescending { it.first.followerCount }
                .thenByDescending { it.first.wealthLevel + it.first.charmLevel }
                .thenByDescending { allUsers.count { u -> u.current_room == it.second.id } }
        )
    }

    val activeCategoryCount = remember(allUsers, category.id) {
        allUsers.count { it.activeIntent == category.id || it.current_room.contains(category.id) }
    }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(
        onDismissRequest = {
            if (isKeyboardOpen) {
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Scaffold(
            containerColor = Color(0xFF10121A),
            topBar = {
                // Compact Slim Single-Line Top Bar (NO huge banner wasting vertical space)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = category.label,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🟢 $activeCategoryCount Online",
                                color = Color(0xFF34D399),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Compact, Stylish "+ Create" Pill Button
                    Surface(
                        color = Color(0xFF262C3E),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFF3F4B6B)),
                        modifier = Modifier
                            .height(32.dp)
                            .clickable { onCreateRoom() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Text("Create", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                // 1. TOP POPULAR STAR HOSTS HORIZONTAL CAROUSEL (PROMINENT LARGE AVATARS)
                if (topPopularHosts.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🌟 Top Star Hosts",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${topPopularHosts.size} Hosts",
                                    color = BrandGold,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                            ) {
                                items(topPopularHosts) { (hostUser, hostRoom) ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(76.dp)
                                            .clickable { onJoinRoom(hostRoom) }
                                    ) {
                                        // Real User Avatar Frame lookup (NO dummy frames!)
                                        // Clean Large Circular Host Avatar (NO avatar frames here as per user request!)
                                        Box(
                                            modifier = Modifier.size(72.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = hostUser.photo.ifEmpty { hostRoom.hostPhotoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" } },
                                                contentDescription = hostUser.name,
                                                modifier = Modifier
                                                    .size(66.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        2.5.dp,
                                                        if (hostRoom.isHostLive) Brush.sweepGradient(listOf(BrandCoral, BrandGold, BrandOrange, BrandCoral)) else Brush.linearGradient(listOf(Color(0xFF3F4B6B), Color(0xFF262C3E))),
                                                        CircleShape
                                                    ),
                                                contentScale = ContentScale.Crop
                                            )

                                            if (hostRoom.isHostLive) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .offset(y = 2.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFFF1744))
                                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "LIVE",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Text(
                                            text = hostUser.name.ifBlank { hostRoom.hostName.ifBlank { "Host" } },
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = if (hostRoom.isHostLive) "🔴 LIVE" else hostRoom.title.take(12),
                                            color = if (hostRoom.isHostLive) Color(0xFF34D399) else Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. ACTIVE ROOMS SECTION HEADER
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎙️ Live & Active Rooms (${sortedCategoryRooms.size})",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto-ranked",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 9.5.sp
                        )
                    }
                }

                // 3. SLENDER HORIZONTAL ROOM ROWS (Reference Image 1 Style)
                if (sortedCategoryRooms.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF141722)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🎙️", fontSize = 38.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No Live Rooms Active",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Be the first to host a hangout in ${category.label}!",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onCreateRoom,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3E)),
                                    border = BorderStroke(1.dp, Color(0xFF3F4B6B)),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Room Now 🎙️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(sortedCategoryRooms) { room ->
                        val activeCount = allUsers.count { it.current_room == room.id }.coerceAtLeast(room.activeUserCount)
                        val hostUser = allUsers.find { it.uid == room.hostUid || it.uid == room.hostId }
                        CategorySubRoomCard(
                            room = room,
                            activeCount = activeCount,
                            hostUser = hostUser,
                            currentUser = currentUser,
                            onJoin = { onJoinRoom(room) }
                        )
                    }

                    // 4. Sleek bottom button: "+ Host Another Lounge"
                    item {
                        Surface(
                            color = Color(0xFF141722),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCreateRoom() }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+ Host Another Lounge in ${category.label}",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}
// -------------------------------------------------------------
// 8. 5-MIC LIVE AUDIO STAGE COMPONENTS & HELPERS
// -------------------------------------------------------------

fun joinMicSeat(
    db: FirebaseFirestore,
    roomId: String,
    seatIndex: Int,
    user: UserProfile,
    allSeats: List<MicSeat>
) {
    if (roomId.isBlank()) return
    val batch = db.batch()
    val micSeatsRef = db.collection("rooms").document(roomId).collection("mic_seats")

    // Atomically vacate ALL previous seats occupied by this user in the same commit
    allSeats.filter { it.uid == user.uid && it.seatIndex != seatIndex }.forEach { prevSeat ->
        batch.set(
            micSeatsRef.document(prevSeat.seatIndex.toString()),
            MicSeat(
                seatIndex = prevSeat.seatIndex,
                label = if (prevSeat.seatIndex == 0) "Host 👑" else "Mic ${prevSeat.seatIndex + 1}",
                uid = "",
                name = "",
                photo = "",
                isMuted = false,
                isLocked = prevSeat.isLocked,
                isTalking = false,
                role = if (prevSeat.seatIndex == 0) "host" else "speaker"
            )
        )
    }

    val isUserHost = seatIndex == 0 || allSeats.find { it.seatIndex == 0 }?.uid == user.uid || (user.uid.isNotBlank() && allSeats.any { it.uid == user.uid && it.role == "host" })
    batch.set(
        micSeatsRef.document(seatIndex.toString()),
        MicSeat(
            seatIndex = seatIndex,
            label = if (seatIndex == 0) "Host 👑" else "Mic ${seatIndex + 1}",
            uid = user.uid,
            name = user.name,
            photo = user.photo,
            isMuted = false,
            isLocked = false,
            isTalking = false,
            role = if (seatIndex == 0 || isUserHost) "host" else "speaker"
        )
    )
    batch.commit()
}

fun relocateSpeakerSeat(
    db: FirebaseFirestore,
    roomId: String,
    fromSeat: MicSeat,
    targetSeatIndex: Int
) {
    if (roomId.isBlank()) return
    val batch = db.batch()
    val micSeatsRef = db.collection("rooms").document(roomId).collection("mic_seats")

    // Atomically vacate source seat
    batch.set(
        micSeatsRef.document(fromSeat.seatIndex.toString()),
        MicSeat(
            seatIndex = fromSeat.seatIndex,
            label = if (fromSeat.seatIndex == 0) "Host 👑" else "Mic ${fromSeat.seatIndex + 1}",
            uid = "",
            name = "",
            photo = "",
            isMuted = false,
            isLocked = fromSeat.isLocked,
            isTalking = false,
            role = if (fromSeat.seatIndex == 0) "host" else "speaker"
        )
    )

    // Atomically occupy target destination seat
    batch.set(
        micSeatsRef.document(targetSeatIndex.toString()),
        MicSeat(
            seatIndex = targetSeatIndex,
            label = if (targetSeatIndex == 0) "Host 👑" else "Mic ${targetSeatIndex + 1}",
            uid = fromSeat.uid,
            name = fromSeat.name,
            photo = fromSeat.photo,
            role = if (targetSeatIndex == 0 || fromSeat.role == "host") "host" else "speaker",
            isMuted = fromSeat.isMuted,
            isLocked = false,
            isTalking = false
        )
    )
    batch.commit()
}

fun vacateMicSeat(
    db: FirebaseFirestore,
    roomId: String,
    seatIndex: Int,
    isLocked: Boolean = false
) {
    db.collection("rooms").document(roomId).collection("mic_seats").document(seatIndex.toString())
        .set(
            MicSeat(
                seatIndex = seatIndex,
                label = if (seatIndex == 0) "Host 👑" else "Mic ${seatIndex + 1}",
                uid = "",
                name = "",
                photo = "",
                isMuted = false,
                isLocked = isLocked,
                isTalking = false,
                role = if (seatIndex == 0) "host" else "speaker"
            )
        )
}

fun toggleMuteMicSeat(
    db: FirebaseFirestore,
    roomId: String,
    seatIndex: Int,
    currentMuted: Boolean
) {
    val newMute = !currentMuted
    db.collection("rooms").document(roomId).collection("mic_seats").document(seatIndex.toString())
        .set(
            mapOf(
                "isMuted" to newMute,
                "muted" to newMute
            ),
            SetOptions.merge()
        )
}

fun toggleLockMicSeat(
    db: FirebaseFirestore,
    roomId: String,
    seatIndex: Int,
    currentLocked: Boolean
) {
    val newLock = !currentLocked
    db.collection("rooms").document(roomId).collection("mic_seats").document(seatIndex.toString())
        .set(
            mapOf(
                "isLocked" to newLock,
                "locked" to newLock
            ),
            SetOptions.merge()
        )
}

// -------------------------------------------------------------
// 2.5 REAL 10-MIC LIVE AUDIO STAGE (CLEAN TRANSPARENT 2 ROWS OF 5)
// -------------------------------------------------------------
@Composable
fun TenMicStageSection(
    seats: List<MicSeat>,
    currentUserId: String,
    isHost: Boolean,
    isLocalTalking: Boolean,
    allProfiles: List<UserProfile> = emptyList(),
    userActiveFrame: String = "",
    userActiveMicEffect: String = "",
    activeEmojiReactions: Map<String, String> = emptyMap(),
    categorySeatEmoji: String = "🎙️",
    categorySeatLabel: String = "Seat",
    activeMicLinkId: String = "mic_link_ruby_crown",
    activeIntimacyBonds: List<IntimacyBond> = emptyList(),
    onOpenMicLinkSelector: () -> Unit = {},
    onSeatClick: (MicSeat) -> Unit,
    onQuickToggleMute: () -> Unit,
    onQuickLeaveStage: () -> Unit,
    onProfileClick: ((MicSeat) -> Unit)? = null
) {
    val mySeat = seats.find { it.uid == currentUserId }
    val seat0 = seats.find { it.seatIndex == 0 }
    val seat1 = seats.find { it.seatIndex == 1 }
    val seatedUids = seats.map { it.uid }.filter { it.isNotBlank() }.toSet()
    val activeSeatedBonds = activeIntimacyBonds.filter { bond ->
        bond.status == "active" && seatedUids.contains(bond.user1Id) && seatedUids.contains(bond.user2Id)
    }
    val linkedUserIds = activeSeatedBonds.flatMap { listOf(it.user1Id, it.user2Id) }.toSet()
    val seat0And1Bond = if (seat0?.uid?.isNotBlank() == true && seat1?.uid?.isNotBlank() == true) {
        activeSeatedBonds.firstOrNull { bond ->
            (bond.user1Id == seat0.uid && bond.user2Id == seat1.uid) ||
                (bond.user1Id == seat1.uid && bond.user2Id == seat0.uid)
        }
    } else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {


        // Row 1 (4 Mic Seats: 0 to 3 with StageMicLinkOverlay between Seat 0 & 1)
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val totalW = maxWidth
            val seat0X = totalW * 0.12f
            val seat1X = totalW * 0.37f
            val centerY = 32.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..3) {
                    val seat = seats.find { it.seatIndex == i } ?: MicSeat(
                        seatIndex = i,
                        label = if (i == 0) "Host 👑" else "Mic ${i + 1}",
                        uid = "",
                        name = "",
                        photo = "",
                        role = if (i == 0) "host" else "speaker"
                    )
                    MicSeatView(
                        seat = seat,
                        isCurrentSelf = seat.uid.isNotEmpty() && seat.uid == currentUserId,
                        isLocalTalking = isLocalTalking,
                        isHost = isHost,
                        isIntimacyLinked = seat.uid in linkedUserIds,
                        allProfiles = allProfiles,
                        userActiveFrame = userActiveFrame,
                        userActiveMicEffect = userActiveMicEffect,
                        reactionEmoji = activeEmojiReactions[seat.uid],
                        categorySeatEmoji = "🎙️",
                        categorySeatLabel = "Mic",
                        onClick = { onSeatClick(seat) },
                        onProfileClick = onProfileClick?.let { callback -> { callback(seat) } }
                    )
                }
            }

            // Real-Time Active Mic Link Overlay (between Seat 0 & Seat 1)
            if (seat0And1Bond != null) {
                val linkDesignId = seat0And1Bond.activeMicLinkId.ifBlank { activeMicLinkId }
                val (dRes, pCol, sCol) = when (linkDesignId) {
                    "mic_link_twin_besties" -> Triple(R.drawable.photoreal_link_twin_besties, Color(0xFF00E5FF), Color(0xFFFF4081))
                    "mic_link_cyberpunk_reactor" -> Triple(R.drawable.photoreal_link_cyberpunk_reactor, Color(0xFF00F5FF), Color(0xFFA855F7))
                    "mic_link_celestial_wings" -> Triple(R.drawable.photoreal_link_celestial_wings, Color(0xFFFFD700), Color(0xFFE0F7FA))
                    "mic_link_emerald_infinity" -> Triple(R.drawable.photoreal_link_emerald_infinity, Color(0xFF00E676), Color(0xFFFFD700))
                    else -> Triple(R.drawable.photoreal_link_ruby_crown, Color(0xFFFF1744), Color(0xFFFFD700))
                }

                val density = androidx.compose.ui.platform.LocalDensity.current
                val seat0XPx = with(density) { seat0X.toPx() }
                val seat1XPx = with(density) { seat1X.toPx() }
                val centerYPx = with(density) { centerY.toPx() }

                StageMicLinkOverlay(
                    startOffset = androidx.compose.ui.geometry.Offset(seat0XPx, centerYPx),
                    endOffset = androidx.compose.ui.geometry.Offset(seat1XPx, centerYPx),
                    drawableRes = dRes,
                    primaryColor = pCol,
                    secondaryColor = sCol,
                    emblemSize = 34.dp,
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2 (4 Mic Seats: 4 to 7)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 4..7) {
                val seat = seats.find { it.seatIndex == i } ?: MicSeat(
                    seatIndex = i,
                    label = "Mic ${i + 1}",
                    uid = "",
                    name = "",
                    photo = "",
                    role = "speaker"
                )
                MicSeatView(
                    seat = seat,
                    isCurrentSelf = seat.uid.isNotEmpty() && seat.uid == currentUserId,
                    isLocalTalking = isLocalTalking,
                    isHost = isHost,
                    isIntimacyLinked = seat.uid in linkedUserIds,
                    allProfiles = allProfiles,
                    userActiveFrame = userActiveFrame,
                    userActiveMicEffect = userActiveMicEffect,
                    reactionEmoji = activeEmojiReactions[seat.uid],
                    categorySeatEmoji = "🎙️",
                    categorySeatLabel = "Mic",
                    onClick = { onSeatClick(seat) },
                    onProfileClick = onProfileClick?.let { callback -> { callback(seat) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3 (4 Mic Seats: 8 to 11)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 8..11) {
                val seat = seats.find { it.seatIndex == i } ?: MicSeat(
                    seatIndex = i,
                    label = "Mic ${i + 1}",
                    uid = "",
                    name = "",
                    photo = "",
                    role = "speaker"
                )
                MicSeatView(
                    seat = seat,
                    isCurrentSelf = seat.uid.isNotEmpty() && seat.uid == currentUserId,
                    isLocalTalking = isLocalTalking,
                    isHost = isHost,
                    isIntimacyLinked = seat.uid in linkedUserIds,
                    allProfiles = allProfiles,
                    userActiveFrame = userActiveFrame,
                    userActiveMicEffect = userActiveMicEffect,
                    reactionEmoji = activeEmojiReactions[seat.uid],
                    categorySeatEmoji = "🎙️",
                    categorySeatLabel = "Mic",
                    onClick = { onSeatClick(seat) },
                    onProfileClick = onProfileClick?.let { callback -> { callback(seat) } }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 2.6 VIP ROYAL STAGE CANVAS (EXACT SOFA & CHAIR COORDINATE MAPPING)
// -------------------------------------------------------------
@Composable
fun VipRoyalStageCanvas(
    seats: List<MicSeat>,
    currentUserId: String,
    isHost: Boolean,
    isLocalTalking: Boolean,
    allProfiles: List<UserProfile> = emptyList(),
    userActiveFrame: String = "",
    userActiveMicEffect: String = "",
    activeEmojiReactions: Map<String, String> = emptyMap(),
    activeMicLinkId: String = "mic_link_ruby_crown",
    activeIntimacyBonds: List<IntimacyBond> = emptyList(),
    onSeatClick: (MicSeat) -> Unit,
    onProfileClick: ((MicSeat) -> Unit)? = null
) {
    val seatedUids = seats.map { it.uid }.filter { it.isNotBlank() }.toSet()
    val activeSeatedBonds = activeIntimacyBonds.filter { bond ->
        bond.status == "active" && seatedUids.contains(bond.user1Id) && seatedUids.contains(bond.user2Id)
    }
    val linkedUserIds = activeSeatedBonds.flatMap { listOf(it.user1Id, it.user2Id) }.toSet()

    // 10 Seats mapped to the shifted-up curved velvet sofa, armchairs, and round table:
    // Seat 0: Host 👑 (Front Center Throne Chair)
    // Seat 1: Center Round Velvet Table
    // Seat 2: Left Single Armchair
    // Seat 3: Right Single Armchair
    // Seat 4: Left Side Table
    // Seat 5: Right Side Table
    // Seats 6..9: Curved Velvet Sofa Upper Arc (from left to right)
    val seatPositions = remember {
        listOf(
            0 to (0.50f to 0.80f), // 0: Host Prime Throne Chair
            1 to (0.50f to 0.50f), // 1: Center Round Table
            2 to (0.16f to 0.68f), // 2: Left Armchair
            3 to (0.84f to 0.68f), // 3: Right Armchair
            4 to (0.16f to 0.42f), // 4: Left Side Table
            5 to (0.84f to 0.42f), // 5: Right Side Table
            6 to (0.28f to 0.22f), // 6: Sofa Far Left
            7 to (0.41f to 0.14f), // 7: Sofa Mid Left
            8 to (0.59f to 0.14f), // 8: Sofa Mid Right
            9 to (0.72f to 0.22f)  // 9: Sofa Far Right
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(265.dp)
            .padding(horizontal = 4.dp)
    ) {
        val stageWidth = maxWidth
        val stageHeight = maxHeight
        val seatBoxSize = 64.dp

        for ((idx, pos) in seatPositions) {
            val (xRatio, yRatio) = pos
            val seat = seats.find { it.seatIndex == idx } ?: MicSeat(
                seatIndex = idx,
                label = if (idx == 0) "Host 👑" else "Mic ${idx + 1}",
                uid = "",
                name = "",
                photo = "",
                role = if (idx == 0) "host" else "speaker"
            )

            val xOffset = (stageWidth * xRatio) - (seatBoxSize / 2)
            val yOffset = (stageHeight * yRatio) - (seatBoxSize / 2)

            Box(
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .size(seatBoxSize),
                contentAlignment = Alignment.Center
            ) {
                MicSeatView(
                    seat = seat,
                    isCurrentSelf = seat.uid.isNotEmpty() && seat.uid == currentUserId,
                    isLocalTalking = isLocalTalking,
                    isHost = isHost,
                    isIntimacyLinked = seat.uid in linkedUserIds,
                    allProfiles = allProfiles,
                    userActiveFrame = userActiveFrame,
                    reactionEmoji = activeEmojiReactions[seat.uid],
                    categorySeatEmoji = if (idx == 0) "👑" else "🎙️",
                    categorySeatLabel = if (idx == 0) "Host" else "Seat",
                    onClick = { onSeatClick(seat) },
                    onProfileClick = onProfileClick?.let { callback -> { callback(seat) } }
                )
            }
        }

        // Real-Time Active Mic Link Overlay in VIP Room (Between Seat 0 Host and Seat 1 Mic 2)
        val seat0 = seats.find { it.seatIndex == 0 }
        val seat1 = seats.find { it.seatIndex == 1 }
        val seat0And1Bond = if (seat0?.uid?.isNotBlank() == true && seat1?.uid?.isNotBlank() == true) {
            activeSeatedBonds.firstOrNull { bond ->
                (bond.user1Id == seat0.uid && bond.user2Id == seat1.uid) ||
                    (bond.user1Id == seat1.uid && bond.user2Id == seat0.uid)
            }
        } else null
        if (seat0And1Bond != null) {
            val linkDesignId = seat0And1Bond.activeMicLinkId.ifBlank { activeMicLinkId }
            val (dRes, pCol, sCol) = when (linkDesignId) {
                "mic_link_twin_besties" -> Triple(R.drawable.photoreal_link_twin_besties, Color(0xFF00E5FF), Color(0xFFFF4081))
                "mic_link_cyberpunk_reactor" -> Triple(R.drawable.photoreal_link_cyberpunk_reactor, Color(0xFF00F5FF), Color(0xFFA855F7))
                "mic_link_celestial_wings" -> Triple(R.drawable.photoreal_link_celestial_wings, Color(0xFFFFD700), Color(0xFFE0F7FA))
                "mic_link_emerald_infinity" -> Triple(R.drawable.photoreal_link_emerald_infinity, Color(0xFF00E676), Color(0xFFFFD700))
                else -> Triple(R.drawable.photoreal_link_ruby_crown, Color(0xFFFF1744), Color(0xFFFFD700))
            }

            val density = androidx.compose.ui.platform.LocalDensity.current
            val startX = with(density) { (stageWidth * 0.50f).toPx() }
            val startY = with(density) { (stageHeight * 0.80f).toPx() }
            val endX = with(density) { (stageWidth * 0.50f).toPx() }
            val endY = with(density) { (stageHeight * 0.50f).toPx() }

            StageMicLinkOverlay(
                startOffset = androidx.compose.ui.geometry.Offset(startX, startY),
                endOffset = androidx.compose.ui.geometry.Offset(endX, endY),
                drawableRes = dRes,
                primaryColor = pCol,
                secondaryColor = sCol,
                emblemSize = 36.dp,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

// -------------------------------------------------------------
// 2.7 IN-ROOM DRAGGABLE FLOATING PIP MUSIC EQUALIZER PLAYER
// -------------------------------------------------------------
@Composable
fun FloatingMusicEqualizerPlayer(
    room: RoomItem,
    isPlaying: Boolean,
    trackTitle: String,
    trackArtist: String,
    artworkUrl: String,
    isHostOrAdmin: Boolean,
    onTogglePlay: () -> Unit,
    onOpenSearch: () -> Unit,
    onStopMusic: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(24f) }
    var offsetY by remember { mutableFloatStateOf(580f) }

    // Live Animated Equalizer Rhythm Bars
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars_transition")
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse), label = "b1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 16f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(440, easing = LinearEasing), RepeatMode.Reverse), label = "b2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 7f, targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse), label = "b3"
    )
    val bar4Height by infiniteTransition.animateFloat(
        initialValue = 14f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse), label = "b4"
    )

    Surface(
        color = Color(0xFF140B22).copy(alpha = 0.94f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(BrandGold, BrandCoral))),
        shadowElevation = 10.dp,
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .clickable { onOpenSearch() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Dancing Equalizer
            Row(
                modifier = Modifier
                    .height(22.dp)
                    .width(18.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val b1 = if (isPlaying) bar1Height else 4f
                val b2 = if (isPlaying) bar2Height else 8f
                val b3 = if (isPlaying) bar3Height else 14f
                val b4 = if (isPlaying) bar4Height else 6f

                Box(modifier = Modifier.width(3.dp).height(b1.dp).clip(RoundedCornerShape(2.dp)).background(BrandGold))
                Box(modifier = Modifier.width(3.dp).height(b2.dp).clip(RoundedCornerShape(2.dp)).background(BrandCoral))
                Box(modifier = Modifier.width(3.dp).height(b3.dp).clip(RoundedCornerShape(2.dp)).background(BrandGold))
                Box(modifier = Modifier.width(3.dp).height(b4.dp).clip(RoundedCornerShape(2.dp)).background(BrandCoral))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Artwork thumbnail (if present)
            if (artworkUrl.isNotBlank()) {
                AsyncImage(
                    model = artworkUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(0.5.dp, BrandGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Track title & artist
            Column(modifier = Modifier.widthIn(max = 135.dp)) {
                Text(
                    text = trackTitle.ifEmpty { "VIP Music Engine 🎵" },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (trackArtist.isNotBlank()) trackArtist else "Tap to change song 🔍",
                    color = BrandGold,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Play / Pause Toggle Button
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(26.dp)
                    .background(BrandGold.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = BrandGold,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Close / Stop Button
            IconButton(
                onClick = onStopMusic,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Stop",
                    tint = Color.LightGray,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun BoxScope.MicSeatEmojiReactionOverlay(emoji: String) {
    val scaleAnim = remember(emoji) { Animatable(0.3f) }
    val offsetYAnim = remember(emoji) { Animatable(0f) }
    val alphaAnim = remember(emoji) { Animatable(1f) }

    LaunchedEffect(emoji) {
        launch {
            scaleAnim.animateTo(1.35f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            scaleAnim.animateTo(1.0f, animationSpec = tween(400))
        }
        launch {
            offsetYAnim.animateTo(-44f, animationSpec = tween(2200, easing = LinearOutSlowInEasing))
        }
        launch {
            delay(1400)
            alphaAnim.animateTo(0f, animationSpec = tween(800))
        }
    }

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = offsetYAnim.value.dp)
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                alpha = alphaAnim.value
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 28.sp
        )
    }
}

@Composable
fun MicSeatView(
    seat: MicSeat,
    isCurrentSelf: Boolean,
    isLocalTalking: Boolean,
    isHost: Boolean,
    isIntimacyLinked: Boolean = false,
    allProfiles: List<UserProfile> = emptyList(),
    userActiveFrame: String = "",
    userActiveMicEffect: String = "",
    reactionEmoji: String? = null,
    categorySeatEmoji: String = "🎙️",
    categorySeatLabel: String = "Mic",
    onClick: () -> Unit,
    onProfileClick: (() -> Unit)? = null
) {
    val isSpeaking = if (isCurrentSelf) (isLocalTalking && !seat.isMuted) else (seat.isTalking && !seat.isMuted)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_${seat.seatIndex}")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale_${seat.seatIndex}"
    )

    val seatOccupantProfile = if (seat.uid.isNotEmpty()) allProfiles.find { it.uid == seat.uid } else null
    val seatFrameId = if (isCurrentSelf && userActiveFrame.isNotBlank()) {
        userActiveFrame
    } else if (seatOccupantProfile?.activeFrame?.isNotBlank() == true) {
        seatOccupantProfile.activeFrame
    } else {
        ""
    }
    val shouldShowFrame = seat.uid.isNotEmpty() && seatFrameId.isNotBlank()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
            .padding(vertical = 1.dp)
    ) {
        val seatBoxSize = if (shouldShowFrame) 62.dp else 52.dp

        Box(
            modifier = Modifier
                .size(seatBoxSize)
                .then(if (seat.uid.isNotEmpty() && onProfileClick != null) Modifier.clickable { onProfileClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (seat.uid.isNotEmpty()) {
                val frameItem = getAvatarFrameItem(seatFrameId)
                val seatVipLvl = seatOccupantProfile?.getVipLevel() ?: (frameItem?.vipLevel ?: 0)

                val seatMicEffectId = if (isCurrentSelf) {
                    userActiveMicEffect
                } else {
                    seatOccupantProfile?.activeMicEffect ?: ""
                }

                FlowingAvatarFrame(
                    photoUrl = seat.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                    frameId = seatFrameId,
                    avatarSize = 48.dp,
                    isSpeaking = isSpeaking,
                    vipLevel = seatVipLvl,
                    showAnimation = false,
                    showVipBadge = false,
                    showMicEffect = seatMicEffectId.isNotBlank() && isSpeaking,
                    micEffectId = seatMicEffectId
                )

                if (isIntimacyLinked) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 1.dp, y = (-1).dp)
                            .size(17.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B1027))
                            .border(1.dp, Color(0xFFFF80AB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💞", fontSize = 9.sp, lineHeight = 10.sp)
                    }
                }

                // Mic Mute / Unmute Status Badge at bottom-right with subtle voice pulse animation
                val isPulsing = isSpeaking && !seat.isMuted
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 1.dp, y = 1.dp)
                        .size(if (isPulsing) 15.dp else 13.dp)
                        .graphicsLayer(
                            scaleX = if (isPulsing) pulseScale else 1f,
                            scaleY = if (isPulsing) pulseScale else 1f
                        )
                        .clip(CircleShape)
                        .background(if (seat.isMuted) Color(0xFFD32F2F) else BrandGreen)
                        .border(1.dp, if (isPulsing) BrandGold else BrandDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (seat.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(if (isPulsing) 9.dp else 8.dp)
                    )
                }
            } else if (seat.isLocked) {
                // Locked Seat (Matching red-tinted circle with red lock icon)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C1014).copy(alpha = 0.65f))
                        .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFFFF8A80),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Empty & Unlocked Seat: Clean microphone on stand with distinct glow
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            if (seat.seatIndex == 0) BrandGold.copy(alpha = 0.7f) else BrandCoral.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Take Mic",
                        tint = if (seat.seatIndex == 0) BrandGold else BrandCoral,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 3. Floating Live Bouncy Emoji Reaction Overlay
            if (!reactionEmoji.isNullOrBlank()) {
                MicSeatEmojiReactionOverlay(emoji = reactionEmoji)
            }
        }

        Spacer(modifier = Modifier.height(if (shouldShowFrame) 6.dp else 2.dp))

        // Label / Subtitle
        if (seat.uid.isNotEmpty()) {
            val occupantName = seatOccupantProfile?.name?.takeIf { it.isNotBlank() }
                ?: seat.name.takeIf { it.isNotBlank() }
                ?: (if (seat.seatIndex == 0) "Host" else "Speaker")
            Text(
                text = occupantName,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(62.dp)
                    .then(if (onProfileClick != null) Modifier.clickable { onProfileClick() } else Modifier)
            )
            Text(
                text = if (seat.seatIndex == 0) "Host 👑" else if (isCurrentSelf) "You" else "Mic ${seat.seatIndex + 1}",
                color = if (seat.seatIndex == 0) BrandGold else BrandCoral,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            val officialRoleBadge = seatOccupantProfile?.getOfficialRoleBadgeRes()
            if (officialRoleBadge != null) {
                Image(
                    painter = painterResource(id = officialRoleBadge),
                    contentDescription = seatOccupantProfile.getOfficialRoleTitle(),
                    modifier = Modifier
                        .height(18.dp)
                        .padding(top = 1.dp),
                    contentScale = ContentScale.Fit
                )
            }
        } else if (seat.isLocked) {
            Text(
                text = "Mic ${seat.seatIndex + 1} 🔒",
                color = Color(0xFFFF8A80),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.width(60.dp)
            )
        } else {
            Text(
                text = if (seat.seatIndex == 0) "Host 👑" else "Mic ${seat.seatIndex + 1}",
                color = if (seat.seatIndex == 0) BrandGold else Color.White.copy(alpha = 0.85f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.width(60.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// REAL-TIME INTIMACY GIFTING ACCEPTANCE MODAL
// -------------------------------------------------------------
@Composable
fun IntimacyGiftingAcceptanceDialog(
    requestData: Map<String, Any>,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reqId = requestData["id"] as? String ?: ""
    val senderUid = requestData["senderUid"] as? String ?: ""
    val senderName = requestData["senderName"] as? String ?: "A friend"
    val senderPhoto = requestData["senderPhoto"] as? String ?: ""
    val giftId = requestData["giftId"] as? String ?: ""
    val giftName = requestData["giftName"] as? String ?: "Intimacy Gift"
    val giftEmoji = requestData["giftEmoji"] as? String ?: "💖"
    val bondType = requestData["bondType"] as? String ?: "couple"
    var isSubmitting by remember(reqId) { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1128)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFFFF4081), BrandGold, Color(0xFFFF4081)))),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Ring Emoji
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF4081).copy(alpha = 0.2f),
                    border = BorderStroke(1.5.dp, BrandGold),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(giftEmoji, fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "💖 Real-Time Intimacy Proposal",
                    color = BrandGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$senderName sent you a $giftName $giftEmoji! Accept to activate official $bondType intimacy bond and stage mic connector.",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Decline
                    Button(
                        onClick = {
                            if (isSubmitting || reqId.isBlank()) return@Button
                            isSubmitting = true
                            db.collection("intimacy_requests").document(reqId)
                                .update("status", "declined")
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Proposal declined.", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                                .addOnFailureListener { error ->
                                    isSubmitting = false
                                    Log.e("IntimacyRequest", "Failed to decline request $reqId", error)
                                    Toast.makeText(context, "Could not decline request: ${error.localizedMessage ?: "Please try again."}", Toast.LENGTH_LONG).show()
                                }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332040)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text("Decline", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Accept & Activate
                    Button(
                        onClick = {
                            if (isSubmitting || reqId.isBlank() || senderUid.isBlank() || currentUser.uid.isBlank()) return@Button
                            isSubmitting = true
                            val bondId = listOf(senderUid, currentUser.uid).sorted().joinToString("_")
                            val activeMicLink = when {
                                giftId in LuxuryStoreCatalog.micLinks.map { it.id } -> giftId
                                bondType.equals("bestie", ignoreCase = true) -> "mic_link_twin_besties"
                                bondType.equals("soulmate", ignoreCase = true) -> "mic_link_celestial_wings"
                                bondType.equals("eternal", ignoreCase = true) -> "mic_link_emerald_infinity"
                                else -> "mic_link_ruby_crown"
                            }

                            val newBond = IntimacyBond(
                                bondId = bondId,
                                user1Id = senderUid,
                                user2Id = currentUser.uid,
                                user1Name = senderName,
                                user2Name = currentUser.name,
                                user1Photo = senderPhoto,
                                user2Photo = currentUser.photo,
                                bondType = bondType,
                                intimacyLevel = 2,
                                intimacyXp = 500L,
                                streakDays = 3,
                                activeMicLinkId = activeMicLink,
                                status = "active",
                                createdAt = System.currentTimeMillis()
                            )

                            val batch = db.batch()
                            batch.set(db.collection("intimacy_bonds").document(bondId), newBond)
                            batch.update(db.collection("intimacy_requests").document(reqId), "status", "accepted")
                            batch.update(db.collection("users").document(senderUid), "activeIntimacyBadge", giftName)
                            batch.update(db.collection("users").document(currentUser.uid), "activeIntimacyBadge", giftName)

                            batch.commit().addOnSuccessListener {
                                Toast.makeText(context, "💖 Intimacy Bond Activated in Real-Time!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }.addOnFailureListener { error ->
                                isSubmitting = false
                                Log.e("IntimacyRequest", "Failed to activate request $reqId", error)
                                Toast.makeText(context, "Could not activate mic link: ${error.localizedMessage ?: "Please try again."}", Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text("Accept & Connect 💖", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HostSeatAdminDialog(
    seat: MicSeat,
    allSeats: List<MicSeat> = emptyList(),
    currentUserId: String,
    isModeratorApexOrStaff: Boolean = false,
    isTargetFounder: Boolean = false,
    onDismiss: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleLock: () -> Unit,
    onTakeMic: (() -> Unit)? = null,
    onMoveSpeakerToSeat: ((targetSeatIndex: Int) -> Unit)? = null,
    onKickToAudience: () -> Unit,
    onKickOutOfRoom: () -> Unit = {},
    onBanFromRoom: () -> Unit = {},
    onInviteListener: () -> Unit,
    onViewProfile: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161214)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BrandBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎙️ Mic ${seat.seatIndex + 1} Admin Controls",
                        color = BrandGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Quick Lock / Unlock Action (Only for guest seats 1..9, seat 0 is host seat)
                if (seat.seatIndex != 0) {
                    Button(
                        onClick = onToggleLock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (seat.isLocked) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(
                            imageVector = if (seat.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (seat.isLocked) "🔓 Unlock Mic (Allow Anyone)" else "🔒 Lock Mic (Prevent Users)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (seat.uid.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(10.dp)
                    ) {
                        AsyncImage(
                            model = seat.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                            contentDescription = seat.name,
                            modifier = Modifier.size(44.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = seat.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (seat.seatIndex == 0) "Room Host 👑" else "Speaker (Mic ${seat.seatIndex + 1})",
                                color = if (seat.seatIndex == 0) BrandGold else BrandCoral,
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (seat.isMuted) "Status: Muted 🔇" else "Status: Live 🎙️",
                                color = if (seat.isMuted) Color.Red else BrandGreen,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mute / Unmute Button
                    Button(
                        onClick = onToggleMute,
                        colors = ButtonDefaults.buttonColors(containerColor = if (seat.isMuted) BrandGreen else Color(0xFF424242)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text(
                            text = if (seat.isMuted) "🎙️ Unmute Speaker" else "🔇 Mute Speaker",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Move Speaker to Another Mic (Host & Admin can change any speaker's mic position)
                    val availableEmptySeats = allSeats.filter { it.seatIndex != seat.seatIndex && it.uid.isEmpty() && !it.isLocked }
                    if (onMoveSpeakerToSeat != null && availableEmptySeats.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        var isMovePickerOpen by remember { mutableStateOf(false) }

                        Button(
                            onClick = { isMovePickerOpen = !isMovePickerOpen },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMovePickerOpen) "▲ Cancel Mic Move" else "🔄 Move to Another Mic",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isMovePickerOpen) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Select Destination Mic for ${seat.name.ifBlank { "Speaker" }}:",
                                color = BrandGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableEmptySeats.forEach { emptySeat ->
                                    Button(
                                        onClick = {
                                            onMoveSpeakerToSeat(emptySeat.seatIndex)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = if (emptySeat.seatIndex == 0) "👑 Host" else "Mic ${emptySeat.seatIndex + 1}",
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Moderation on other speakers
                    if (isTargetFounder) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFF3E1200),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD700)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("👑 LiveTogather Founder - Supreme Immunity Active", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if ((seat.seatIndex != 0 || isModeratorApexOrStaff) && seat.uid != currentUserId) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onKickToAudience,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text(
                                text = if (seat.seatIndex == 0) "🚫 Move Host to Audience" else "🚫 Move Speaker to Audience",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onKickOutOfRoom,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("👢 Kick Room", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onBanFromRoom,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF210000)),
                                border = BorderStroke(1.dp, Color.Red),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("🔨 Ban User", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = onViewProfile,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandBorder),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("👤 View Profile & Admin Controls", color = Color.White, fontSize = 11.sp)
                        }
                    }
                } else {
                    // Empty seat controls
                    Text(
                        text = if (seat.isLocked) "🔒 This microphone is currently LOCKED." else "This microphone seat is currently empty.",
                        color = if (seat.isLocked) Color(0xFFFF8A80) else Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (onTakeMic != null) {
                        Button(
                            onClick = onTakeMic,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("🎙️ Take this Mic (Sit Here)", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = onInviteListener,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("📨 Invite Listener to this Mic", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun InviteListenerDialog(
    seat: MicSeat,
    availableAudience: List<RoomMember>,
    onDismiss: () -> Unit,
    onSelectMember: (RoomMember) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161214)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BrandBorder),
            modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "📨 Invite Audience to Mic ${seat.seatIndex + 1}",
                    color = BrandCoral,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a listener to invite to this microphone seat:",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (availableAudience.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No listeners available in audience right now.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableAudience, key = { it.uid }) { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable { onSelectMember(member) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = member.photoUrl.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                                    contentDescription = member.name,
                                    modifier = Modifier.size(38.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = member.tier,
                                        color = if (member.tier.contains("VIP", true)) BrandGold else Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(BrandCoral)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Invite 🎙️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SpeakerSeatControlDialog(
    seat: MicSeat,
    allSeats: List<MicSeat> = emptyList(),
    isTalking: Boolean,
    onDismiss: () -> Unit,
    onToggleMute: () -> Unit,
    onMoveToSeat: ((targetSeatIndex: Int) -> Unit)? = null,
    onLeaveStage: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161214)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BrandBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎙️ Your Microphone (${if (seat.seatIndex == 0) "Host Mic" else "Mic " + (seat.seatIndex + 1)})",
                    color = BrandCoral,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (seat.isMuted) "Your microphone is currently MUTED 🔇" else if (isTalking) "You are currently SPEAKING 🟢" else "Your microphone is LIVE 🎙️",
                    color = if (seat.isMuted) Color(0xFFFF8A80) else BrandGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onToggleMute,
                    colors = ButtonDefaults.buttonColors(containerColor = if (seat.isMuted) BrandGreen else Color(0xFF424242)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(
                        text = if (seat.isMuted) "🎙️ Unmute Microphone" else "🔇 Mute Microphone",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Change Mic Position (Move to another empty mic)
                val availableEmptySeats = allSeats.filter { it.seatIndex != seat.seatIndex && it.uid.isEmpty() && !it.isLocked }
                if (onMoveToSeat != null && availableEmptySeats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    var isMovePickerOpen by remember { mutableStateOf(false) }

                    Button(
                        onClick = { isMovePickerOpen = !isMovePickerOpen },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMovePickerOpen) "▲ Cancel Move" else "🔄 Change Mic Position",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isMovePickerOpen) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select New Mic Position:",
                            color = BrandGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableEmptySeats.forEach { emptySeat ->
                                Button(
                                    onClick = {
                                        onMoveToSeat(emptySeat.seatIndex)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = if (emptySeat.seatIndex == 0) "👑 Host" else "Mic ${emptySeat.seatIndex + 1}",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (seat.seatIndex != 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onLeaveStage,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text(
                            text = "🚪 Step Down / Leave Stage",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. LUXURY GIFT & DIAMOND STORE DIALOG
// -------------------------------------------------------------
@Composable
fun GiftStoreDialog(
    currentDiamonds: Long,
    activeFrame: String,
    ownedFrames: List<String>,
    currentUser: UserProfile,
    roomId: String,
    onDismiss: () -> Unit,
    onDiamondsRecharged: (Long) -> Unit,
    onBuyFrame: (frameId: String, cost: Long) -> Unit,
    onApplyFrame: (frameId: String) -> Unit,
    onSendGift: (VideoGiftItem, String, String) -> Unit,
    micSeats: List<MicSeat> = emptyList(),
    fixedGiftRecipient: RoomMember? = null,
    onSendMicroGift: ((gift: RoomGiftItem, quantity: Int, targetSeatIndex: Int, targetUserName: String, fromBackpack: Boolean) -> Unit)? = null,
    onClaimStarterGifts: (() -> Unit)? = null
) {
    var selectedTab by remember(fixedGiftRecipient?.uid) {
        mutableStateOf(if (fixedGiftRecipient != null) 2 else 0)
    } // 0: 3D Gifts, 1: Backpack, 2: Video Gifts, 3: Diamonds
    var localBackpack by remember(currentUser.backpack) { mutableStateOf(currentUser.backpack) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard.copy(alpha = 0.98f)),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, BrandGold.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 620.dp)
                .padding(vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎁", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🎁 Luxury Gift Store",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Diamond balance chip
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF2B1D0E))
                                .border(1.dp, BrandGold, RoundedCornerShape(50))
                                .clickable { selectedTab = 3 }
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💎", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentDiamonds",
                                color = BrandGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs (5 Tabs with horizontal scroll)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("🎁 3D Gifts", "🎒 Backpack", "🎬 Video Gifts", "💎 Recharge")
                    items(tabs.size) { index ->
                        val title = tabs[index]
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(BrandCoral, BrandOrange))
                                    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { selectedTab = index }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        0 -> RoomMicroGiftsSection(
                            currentDiamonds = currentDiamonds,
                            seatedUsers = micSeats,
                            onSendGift = { gift, qty, seatIdx, targetName ->
                                onSendMicroGift?.invoke(gift, qty, seatIdx, targetName, false)
                            },
                            onGoToRecharge = { selectedTab = 3 }
                        )
                        1 -> BackpackSection(
                            currentUser = currentUser.copy(backpack = localBackpack),
                            seatedUsers = micSeats,
                            onSendBackpackGift = { gift, qty, seatIdx, targetName ->
                                onSendMicroGift?.invoke(gift, qty, seatIdx, targetName, true)
                            },
                            onClaimStarterGifts = {
                                val starterPack = mapOf(
                                    "gift_single_rose" to 5,
                                    "gift_firecracker_bomb" to 1
                                )
                                localBackpack = starterPack
                                onClaimStarterGifts?.invoke()
                            }
                        )
                        2 -> VideoGiftsSection(
                            currentDiamonds = currentDiamonds,
                            seatedUsers = micSeats,
                            fixedGiftRecipient = fixedGiftRecipient,
                            onSendGift = { gift, recipientUid, recipientName ->
                                onSendGift(gift, recipientUid, recipientName)
                                onDismiss()
                            },
                            onGoToRecharge = { selectedTab = 3 }
                        )
                        3 -> DiamondsRechargeSection(
                            currentUser = currentUser,
                            onRecharge = { diamonds -> onDiamondsRecharged(diamonds) }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: DIAMONDS RECHARGE
// -------------------------------------------------------------
@Composable
private fun DiamondsRechargeSection(
    currentUser: UserProfile,
    onRecharge: (Long) -> Unit
) {
    val context = LocalContext.current
    var checkoutPackage by remember { mutableStateOf<CoinPackageItem?>(null) }
    var showHelpDesk by remember { mutableStateOf(false) }

    if (checkoutPackage != null) {
        UpiPaymentCheckoutModal(
            currentUser = currentUser,
            selectedPackage = checkoutPackage!!,
            onDismiss = { checkoutPackage = null },
            onSuccess = { checkoutPackage = null }
        )
    }

    if (showHelpDesk) {
        HelpDeskModal(
            currentUser = currentUser,
            prefilledPackage = checkoutPackage ?: APP_COIN_PACKAGES[0],
            onDismiss = { showHelpDesk = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Special Featured Offer Banner (User Request: Rs 149 = 50,000 Diamonds)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF26190B)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, BrandGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandGold)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🔥 BEST VALUE OFFER",
                            color = BrandDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "Save 60%",
                        color = BrandGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "50,000 Diamonds",
                                color = BrandGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "Special Room Launch Promo Pack",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            val pack = APP_COIN_PACKAGES.find { it.id == "pack_149" } ?: APP_COIN_PACKAGES[0]
                            checkoutPackage = pack
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "₹149",
                            color = BrandDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        if (!currentUser.isHostMonetized) {
            Surface(
                color = Color(0xFF22162B),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(BrandGold, Color(0xFFFF5E62)))),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        checkoutPackage = CoinPackageItem(
                            id = "pkg_host_monetization_599",
                            coins = 0L,
                            priceInr = "₹599",
                            numericPrice = "599",
                            title = "Host Monetization Lifetime Activation 🚀",
                            badge = "OFFICIAL"
                        )
                    }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Activate Host Monetization • ₹599", color = BrandGold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("₹1,499", color = Color.Gray, fontSize = 10.sp, textDecoration = TextDecoration.LineThrough)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("60% OFF", color = Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Earn real cash INR on gifts received in your rooms!", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                    Surface(
                        color = BrandGold,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Activate ➔", color = BrandDark, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                    }
                }
            }
        }

        Text(
            text = "More Diamond Packs",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        val packs = listOf(
            Triple(15000L, "₹49", "Starter Pack 💎"),
            Triple(120000L, "₹299", "Super Value 💎"),
            Triple(300000L, "₹599", "Royal VIP Pack 💎")
        )

        packs.forEach { (diamonds, price, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, BrandBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "%,d Diamonds".format(diamonds),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = label,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        val numAmount = price.replace("₹", "").trim()
                        val pack = APP_COIN_PACKAGES.find { it.coins == diamonds }
                            ?: CoinPackageItem("pack_$diamonds", diamonds, price, numAmount, label)
                        checkoutPackage = pack
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = price,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Direct Help Desk UTR button
        Surface(
            color = BrandCard,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    checkoutPackage = APP_COIN_PACKAGES[0]
                    showHelpDesk = true
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎧", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Already Paid via UPI?", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Submit 12-digit UTR in Help Desk to verify & credit", color = BrandGold, fontSize = 10.sp)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = BrandGold, modifier = Modifier.size(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "🔒 Direct UPI payment via GPay, PhonePe, Paytm, BHIM • Submit UTR for instant credit",
            color = Color.Gray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// -------------------------------------------------------------
// TAB 3: VIDEO GIFTS CATALOG (Compact 3-Column Grid + Live Animation)
// -------------------------------------------------------------
@Composable
private fun VideoGiftsSection(
    currentDiamonds: Long,
    seatedUsers: List<MicSeat> = emptyList(),
    fixedGiftRecipient: RoomMember? = null,
    onSendGift: (VideoGiftItem, String, String) -> Unit,
    onGoToRecharge: () -> Unit
) {
    var selectedCategory by remember(fixedGiftRecipient?.uid) {
        mutableStateOf(if (fixedGiftRecipient != null) "intimacy" else "3d")
    }
    val visibleGifts = remember(selectedCategory) {
        VIDEO_GIFT_CATALOG.filter { it.category == selectedCategory }
    }
    var selectedGift by remember { mutableStateOf(VIDEO_GIFT_CATALOG.first { it.category == "3d" }) }
    var selectedTargetSeatIndex by remember { mutableIntStateOf(-1) } // -1 = All Mics
    var selectedTargetName by remember { mutableStateOf("All Mics") }

    LaunchedEffect(selectedCategory) {
        if (selectedGift.category != selectedCategory) {
            selectedGift = visibleGifts.firstOrNull() ?: VIDEO_GIFT_CATALOG.first()
        }
    }

    val occupiedSeats = remember(seatedUsers) {
        seatedUsers.filter { it.uid.isNotBlank() }
    }

    val canAfford = currentDiamonds >= selectedGift.cost

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ROW 1: TARGET SELECTOR ("All Mics" vs Specific Seated Users / Co-Hosts)
        if (fixedGiftRecipient != null) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1528)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎯 Sending to ${fixedGiftRecipient.name}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            TargetMicSelectorRow(
                occupiedSeats = occupiedSeats,
                selectedTargetIndex = selectedTargetSeatIndex,
                onSelectTarget = { seatIndex, name ->
                    selectedTargetSeatIndex = seatIndex
                    selectedTargetName = name
                }
            )
        }

        // ROW 2: Balance Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1528))
                .border(1.dp, BrandGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💎", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "%,d Diamonds".format(currentDiamonds),
                    color = BrandGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "+ Recharge",
                color = BrandCoral,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onGoToRecharge() }
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("3d" to "✨ 3D Gifts", "intimacy" to "💞 Intimacy Gifts")) { (category, label) ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandGold,
                        selectedLabelColor = BrandDark
                    )
                )
            }
        }

        // ROW 3: Compact 3-Column Grid of Video Gifts (with selection highlight!)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(visibleGifts) { gift ->
                val canBuy = currentDiamonds >= gift.cost
                val isSelected = selectedGift.id == gift.id

                CompactVideoGiftThumbnailBox(
                    gift = gift,
                    canAfford = canBuy,
                    isSelected = isSelected,
                    onClick = {
                        selectedGift = gift
                    }
                )
            }
        }

        // ROW 4: BOTTOM SEND ACTION BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1B1325))
                .border(1.dp, BrandGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Selected: ${selectedGift.name}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cost: %,d 💎 • To: ${fixedGiftRecipient?.name ?: selectedTargetName}".format(selectedGift.cost),
                    color = BrandGold,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    if (canAfford) {
                        val targetSeat = occupiedSeats.firstOrNull { it.seatIndex == selectedTargetSeatIndex }
                        onSendGift(
                            selectedGift,
                            fixedGiftRecipient?.uid ?: targetSeat?.uid.orEmpty(),
                            fixedGiftRecipient?.name ?: targetSeat?.name ?: selectedTargetName
                        )
                    } else {
                        onGoToRecharge()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) BrandCoral else Color(0xFF55382A)
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (canAfford) "SEND 🎁" else "+ Recharge",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 10. FULL-SCREEN TRANSPARENT VIDEO GIFT BROADCAST ANIMATION
// -------------------------------------------------------------
private class GiftVideoTextureView(
    context: Context,
    private val rawResId: Int,
    private val onFinished: () -> Unit,
    private val onFailure: (String) -> Unit
) : TextureView(context), TextureView.SurfaceTextureListener {
    private var player: MediaPlayer? = null
    private var videoSurface: Surface? = null

    init {
        isOpaque = false
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: android.graphics.SurfaceTexture, width: Int, height: Int) {
        releasePlayback()
        val surface = Surface(surfaceTexture)
        val mediaPlayer = MediaPlayer()
        videoSurface = surface
        player = mediaPlayer
        try {
            mediaPlayer.setSurface(surface)
            mediaPlayer.setDataSource(
                context,
                Uri.parse("android.resource://${context.packageName}/$rawResId")
            )
            mediaPlayer.setOnPreparedListener { it.start() }
            mediaPlayer.setOnCompletionListener {
                releasePlayback()
                onFinished()
            }
            mediaPlayer.setOnErrorListener { _, what, extra ->
                val message = "Gift video playback failed (what=$what, extra=$extra)"
                Log.e("GiftVideo", message)
                releasePlayback()
                onFailure(message)
                true
            }
            mediaPlayer.prepareAsync()
        } catch (error: java.io.IOException) {
            handlePlaybackFailure(error)
        } catch (error: IllegalArgumentException) {
            handlePlaybackFailure(error)
        } catch (error: IllegalStateException) {
            handlePlaybackFailure(error)
        } catch (error: SecurityException) {
            handlePlaybackFailure(error)
        }
    }

    override fun onSurfaceTextureSizeChanged(
        surface: android.graphics.SurfaceTexture,
        width: Int,
        height: Int
    ) = Unit

    override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
        releasePlayback()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) = Unit

    fun release() {
        surfaceTextureListener = null
        releasePlayback()
    }

    private fun handlePlaybackFailure(error: Exception) {
        Log.e("GiftVideo", "Unable to initialize gift video playback", error)
        releasePlayback()
        onFailure(error.localizedMessage ?: "Unable to play gift video.")
    }

    private fun releasePlayback() {
        player?.release()
        player = null
        videoSurface?.release()
        videoSurface = null
    }
}

@Composable
fun FullScreenGiftAnimationOverlay(
    giftEvent: GiftEvent,
    onDismiss: () -> Unit,
    isVideoRoom: Boolean = false
) {
    val isVideo = giftEvent.animationType == "video_mp4"
    val activeDuration = 3800L

    val infiniteTransition = rememberInfiniteTransition(label = "gift_anim")

    // Dynamic travel animation values
    val carOffset by infiniteTransition.animateFloat(
        initialValue = -350f,
        targetValue = 350f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "car_offset"
    )

    val rocketOffsetY by infiniteTransition.animateFloat(
        initialValue = 300f,
        targetValue = -350f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "rocket_offset"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dragon_pulse"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotate_ring"
    )

    var overlayAlpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(giftEvent.id) {
        overlayAlpha = 1f
        if (!isVideo) {
            delay(activeDuration)
            overlayAlpha = 0f
            delay(450)
            onDismiss()
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = overlayAlpha,
        animationSpec = tween(350),
        label = "overlay_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isVideo) Modifier else Modifier.graphicsLayer(alpha = animatedAlpha))
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
            // 1. Top Sender Announcement Banner
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.82f))
                        .border(1.5.dp, BrandGold, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, BrandGold, CircleShape)
                    ) {
                        AsyncImage(
                            model = giftEvent.senderPhoto.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                            contentDescription = giftEvent.senderName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "👑 ${giftEvent.senderName} sent ${giftEvent.giftName} ${giftEvent.giftEmoji}!",
                            color = BrandGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Room-Wide Broadcast • %,d 💎".format(giftEvent.cost),
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // 2. Full-Screen Transparent Animation Visuals
            when (giftEvent.animationType) {
                "micro_gift", "firecracker_burst" -> {
                    MicroGiftFlightAndBurstOverlay(
                        giftEvent = giftEvent,
                        onFinished = onDismiss,
                        isVideoRoom = isVideoRoom
                    )
                }
                "video_mp4" -> {
                    val rawResId = when (giftEvent.giftId) {
                        "lamborghini_mp4" -> R.raw.lamborgini_car
                        "eagle_entry_mp4" -> R.raw.eagle_entry
                        "king_lion_mp4" -> R.raw.king_lion_entry
                        "terminator_dragon_mp4" -> R.raw.terminator_entry
                        "king_entry_mp4" -> R.raw.king_entry
                        "diamond_ring_proposal_mp4" -> R.raw.diamond_ring_proposal
                        "golden_super_car_mp4" -> R.raw.golden_super_car
                        "princess_ruby_necklace_mp4" -> R.raw.princess_ruby_necklace
                        "rose_proposal_mp4" -> R.raw.rose_proposal
                        "treasure_for_you_mp4" -> R.raw.treasure_for_you
                        "founded_treasure_mp4" -> R.raw.founded_treasure
                        "heaven_ship_mp4" -> R.raw.heaven_ship
                        "royal_jet_mp4" -> R.raw.royal_jet
                        else -> VIDEO_GIFT_CATALOG.find { it.id == giftEvent.giftId }?.rawResId ?: R.raw.lamborgini_car
                    }
                    key(giftEvent.id) {
                        AndroidView(
                            factory = { ctx ->
                                GiftVideoTextureView(
                                    context = ctx,
                                    rawResId = rawResId,
                                    onFinished = onDismiss,
                                    onFailure = { onDismiss() }
                                )
                            },
                            modifier = Modifier.fillMaxSize().graphicsLayer(
                                compositingStrategy = CompositingStrategy.Offscreen,
                                blendMode = BlendMode.Screen
                            ),
                            update = { it.isOpaque = false },
                            onRelease = { it.release() }
                        )
                    }
                }

                "car" -> {
                    // Cyber Supercar Drifting across the screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = carOffset.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏎️💨", fontSize = 72.sp)
                            Text(
                                text = "⚡ CYBER SUPERCAR DRIFT! ⚡",
                                color = BrandGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                "rocket" -> {
                    // Galactic Rocket Launching upwards
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = rocketOffsetY.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚀", fontSize = 76.sp)
                            Text("🔥", fontSize = 28.sp)
                            Text(
                                text = "✨ GALACTIC ROCKET BLASTOFF! ✨",
                                color = BrandCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                "dragon" -> {
                    // Golden Flaming Dragon with Rotating Aura
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(240.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .graphicsLayer(rotationZ = rotateAngle)
                                .border(2.dp, Brush.sweepGradient(listOf(BrandGold, Color.Transparent, BrandCoral, BrandGold)), CircleShape)
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                        ) {
                            Text("🐉", fontSize = 80.sp)
                            Text(
                                text = "🔥 GOLDEN DRAGON ROAR! 🔥",
                                color = BrandGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                "roses" -> {
                    // Rose Storm Cascading shower
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    ) {
                        Text("🌹 🌹 🌹", fontSize = 48.sp)
                        Text("🌹 🌹 🌹 🌹", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💖 ROSE PETAL SHOWER! 💖",
                            color = Color(0xFFFF4081),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                "lion" -> {
                    // Roaring Lion King with solar shockwave & golden flames
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(280.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .graphicsLayer(scaleX = pulseScale * 1.15f, scaleY = pulseScale * 1.15f)
                                .border(3.dp, Brush.radialGradient(listOf(BrandGold, Color(0xFFFF5722), Color.Transparent)), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .graphicsLayer(rotationZ = rotateAngle)
                                .border(2.5.dp, Brush.sweepGradient(listOf(BrandGold, BrandCoral, BrandGold)), CircleShape)
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                        ) {
                            Text("🦁", fontSize = 88.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚡ MAJESTIC ROARING LION KING! ⚡",
                                color = BrandGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                "jet" -> {
                    // Supersonic Private Falcon Jet zooming across screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = carOffset.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💨💨", fontSize = 32.sp)
                                Text("✈️", fontSize = 80.sp)
                            }
                            Text(
                                text = "🚀 SUPERSONIC PRIVATE JET ARRIVAL! 🚀",
                                color = BrandCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                "yacht" -> {
                    // Billionaire High-Seas Mega Yacht
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    ) {
                        Text("🌊 🛥️ 🌊", fontSize = 68.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💎 BILLIONAIRE MEGA YACHT CRUISE! 💎",
                            color = Color(0xFF00E5FF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                "crown" -> {
                    // Royal Crown Coronation
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(260.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .graphicsLayer(rotationZ = -rotateAngle)
                                .border(2.dp, Brush.sweepGradient(listOf(BrandGold, Color(0xFFFFD700), Color.Transparent, BrandGold)), CircleShape)
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                        ) {
                            Text("✨ 👑 ✨", fontSize = 72.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "👑 IMPERIAL CROWN CORONATION! 👑",
                                color = BrandGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                "castle" -> {
                    // Imperial Palace Fireworks
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    ) {
                        Text("🎆 🏰 🎆", fontSize = 64.sp)
                        Text(
                            text = "👑 IMPERIAL PALACE CELEBRATION! 👑",
                            color = BrandGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                else -> {
                    // Default luxury gift presentation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    ) {
                        Text(giftEvent.giftEmoji, fontSize = 72.sp)
                        Text(
                            text = "✨ ${giftEvent.giftName.uppercase()}! ✨",
                            color = BrandGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

// -------------------------------------------------------------
// INVITE FOLLOWERS DIALOG (ROOM SHARE & REAL-TIME NOTIFICATIONS)
// -------------------------------------------------------------
@Composable
fun InviteFollowersDialog(
    currentUser: UserProfile,
    room: RoomItem,
    db: com.google.firebase.firestore.FirebaseFirestore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val followers = remember { mutableStateListOf<UserProfile>() }
    val selectedUids = remember { mutableStateListOf<String>() }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser.uid) {
        db.collection("users").document(currentUser.uid).collection("followers").get()
            .addOnSuccessListener { snapshot ->
                val uids = snapshot.documents.mapNotNull { it.getString("uid") ?: it.id }.filter { it.isNotBlank() && it != currentUser.uid }
                if (uids.isEmpty()) {
                    db.collection("users").limit(30).get()
                        .addOnSuccessListener { usersSnap ->
                            val list = usersSnap.documents.mapNotNull { parseUserProfileFromDoc(it) }
                                .filter { it.uid != currentUser.uid }
                            followers.clear()
                            followers.addAll(list)
                            isLoading = false
                        }
                        .addOnFailureListener { isLoading = false }
                } else {
                    val list = mutableListOf<UserProfile>()
                    var count = 0
                    for (fUid in uids) {
                        db.collection("users").document(fUid).get()
                            .addOnSuccessListener { doc ->
                                if (doc != null && doc.exists()) {
                                    list.add(parseUserProfileFromDoc(doc))
                                }
                                count++
                                if (count == uids.size) {
                                    followers.clear()
                                    followers.addAll(list)
                                    isLoading = false
                                }
                            }
                            .addOnFailureListener {
                                count++
                                if (count == uids.size) {
                                    followers.clear()
                                    followers.addAll(list)
                                    isLoading = false
                                }
                            }
                    }
                }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    val filteredFollowers = remember(followers, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) followers
        else followers.filter {
            it.name.contains(q, ignoreCase = true) ||
            it.userId.contains(q, ignoreCase = true) ||
            it.city.contains(q, ignoreCase = true)
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(24.dp),
            color = BrandDark,
            border = BorderStroke(1.5.dp, BrandGold)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📢 Invite Followers to Room",
                            color = BrandGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Room: ${room.title} (ID: ${room.roomId})",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or 8-digit ID...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp)) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Select All / Deselect All Toggle Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedUids.size} Selected",
                        color = BrandCoral,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(
                        onClick = {
                            if (selectedUids.size == filteredFollowers.size) {
                                selectedUids.clear()
                            } else {
                                selectedUids.clear()
                                selectedUids.addAll(filteredFollowers.map { it.uid })
                            }
                        }
                    ) {
                        Text(
                            text = if (selectedUids.size == filteredFollowers.size && filteredFollowers.isNotEmpty()) "Deselect All" else "Select All",
                            color = BrandGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Followers List
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandGold, modifier = Modifier.size(32.dp))
                        }
                    } else if (filteredFollowers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No followers found.", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredFollowers, key = { it.uid }) { follower ->
                                val isSelected = selectedUids.contains(follower.uid)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) BrandGold.copy(alpha = 0.15f) else BrandCard
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isSelected) BrandGold else BrandBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) selectedUids.remove(follower.uid)
                                            else selectedUids.add(follower.uid)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedUids.add(follower.uid)
                                                else selectedUids.remove(follower.uid)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = BrandGold,
                                                checkmarkColor = BrandDark
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        coil.compose.AsyncImage(
                                            model = follower.photo.ifEmpty { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80" },
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(follower.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                "ID: ${if (follower.userId.isNotBlank()) follower.userId else follower.uid.take(8)}",
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Send Invite Button
                Button(
                    onClick = {
                        if (selectedUids.isEmpty()) {
                            Toast.makeText(context, "Select at least 1 friend to invite!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSending = true
                        var sentCount = 0
                        for (targetUid in selectedUids) {
                            val notifId = UUID.randomUUID().toString()
                            val notif = NotificationItem(
                                id = notifId,
                                targetUserId = targetUid,
                                senderId = currentUser.uid,
                                senderName = currentUser.name,
                                senderPhoto = currentUser.photo,
                                title = "🎙️ Room Invite from ${currentUser.name}",
                                message = "${currentUser.name} invited you to join '${room.title}'! Tap to join.",
                                type = "room_invite",
                                timestamp = System.currentTimeMillis(),
                                isRead = false,
                                roomId = room.id
                            )
                            db.collection("users").document(targetUid)
                                .collection("notifications").document(notifId).set(notif)
                            db.collection("notifications").document(notifId).set(notif)
                            sentCount++
                        }
                        isSending = false
                        Toast.makeText(context, "✅ Room invitations sent to $sentCount friends! 🚀", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    enabled = !isSending && selectedUids.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = BrandDark, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Share, contentDescription = null, tint = BrandDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Invitations (${selectedUids.size})",
                            color = BrandDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// REPORT USER DIALOG (ADMIN REVIEW & TICKET CREATION)
// -------------------------------------------------------------
@Composable
fun ReportUserDialog(
    reportedMember: RoomMember,
    reportedUserId: String,
    currentUser: UserProfile,
    db: com.google.firebase.firestore.FirebaseFirestore,
    roomId: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reasons = listOf(
        "Abusive Speech / Harassment",
        "Inappropriate Content / Nudity",
        "Spam / Scams / Commercial Ads",
        "Underage User (<18)",
        "Impersonation / Fake Profile",
        "Other Violation"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var detailsText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isKeyboardOpen) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = !isKeyboardOpen,
            usePlatformDefaultWidth = false
        )
    ) {
        val view = LocalView.current
        SideEffect {
            var parent = view.parent
            while (parent != null && parent !is android.app.Dialog && parent !is DialogWindowProvider) {
                parent = parent.parent
            }
            (parent as? DialogWindowProvider)?.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .imePadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
            shape = RoundedCornerShape(24.dp),
            color = BrandDark,
            border = BorderStroke(1.5.dp, Color.Red.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Report User",
                            color = Color.Red,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reporting: ${reportedMember.name} (ID: ${if (reportedUserId.isNotBlank()) reportedUserId else reportedMember.uid.take(8)})",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Select Violation Reason:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedReason == reason),
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color.Red,
                                unselectedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(reason, color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Additional Details (Optional):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (isKeyboardOpen) {
                        TextButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("Hide Keyboard ⬇️", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = detailsText,
                    onValueChange = { detailsText = it },
                    placeholder = { Text("Describe what happened...", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            isSubmitting = true
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            val reportId = UUID.randomUUID().toString()
                            val ticket = ReportTicket(
                                reportId = reportId,
                                reportedUid = reportedMember.uid,
                                reportedUserId = reportedUserId,
                                reportedName = reportedMember.name,
                                reporterUid = currentUser.uid,
                                reporterUserId = currentUser.userId,
                                reporterName = currentUser.name,
                                reason = selectedReason,
                                details = detailsText.trim(),
                                timestamp = System.currentTimeMillis(),
                                status = "PENDING"
                            )
                            db.collection("reports").document(reportId).set(ticket)
                                .addOnSuccessListener {
                                    // Send real-time notification to Admins
                                    val notifId = "report_$reportId"
                                    val adminNotif = NotificationItem(
                                        id = notifId,
                                        targetUserId = "admin",
                                        senderId = currentUser.uid,
                                        senderName = currentUser.name,
                                        senderPhoto = currentUser.photo,
                                        type = "user_reported",
                                        title = "🚨 User Report: ${reportedMember.name}",
                                        message = "${currentUser.name} reported ${reportedMember.name} (#${if (reportedUserId.isNotBlank()) reportedUserId else reportedMember.uid.take(8)}) for $selectedReason: ${detailsText.trim()}",
                                        timestamp = System.currentTimeMillis(),
                                        isRead = false,
                                        roomId = roomId
                                    )
                                    db.collection("notifications").document(notifId).set(adminNotif)

                                    db.collection("users").whereEqualTo("isAdmin", true).get().addOnSuccessListener { adminsSnap ->
                                        for (adminDoc in adminsSnap.documents) {
                                            val adminUid = adminDoc.id
                                            if (adminUid.isNotBlank() && adminUid != currentUser.uid) {
                                                val userNotif = adminNotif.copy(id = "${notifId}_$adminUid", targetUserId = adminUid)
                                                db.collection("notifications").document(userNotif.id).set(userNotif)
                                                db.collection("users").document(adminUid).collection("notifications").document(notifId).set(userNotif)
                                            }
                                        }
                                    }

                                    isSubmitting = false
                                    Toast.makeText(context, "🛡️ Report submitted to Admins. Thank you for keeping LiveTogether safe!", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                                .addOnFailureListener { e ->
                                    isSubmitting = false
                                    Toast.makeText(context, "Failed to submit report: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).height(42.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Submit Report", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
