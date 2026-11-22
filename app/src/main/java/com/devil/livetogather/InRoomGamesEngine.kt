package com.devil.livetogather

import com.google.firebase.firestore.FieldValue
import org.json.JSONObject
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.Send
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.*
import kotlin.random.Random

// -------------------------------------------------------------
// IN-ROOM GAMES ENGINE (HD REALISTIC CLUB EDITIONS: LUDO, CARROM, TRUTH & DARE, VIGO LUCKY 7)
// -------------------------------------------------------------

@Composable
fun InRoomGameArenaOverlay(
    roomId: String,
    gameState: InRoomGameState,
    currentUser: UserProfile,
    isHost: Boolean,
    isAdmin: Boolean,
    allProfiles: List<UserProfile>,
    liveComments: List<LiveRoomComment> = emptyList(),
    db: FirebaseFirestore,
    onClose: () -> Unit
) {
    var showEndGameConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val canEndGame = isHost || isAdmin || currentUser.isAdmin || currentUser.isOfficialAdmin(currentUser.uid)
    val isCarrom = gameState.gameType == "carrom"

    var isSoundOn by remember { mutableStateOf(true) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var carromTurn by remember { mutableStateOf("p1") }
    var carromP1Score by remember { mutableIntStateOf(0) }
    var carromP2Score by remember { mutableIntStateOf(0) }
    var carromWebView by remember { mutableStateOf<WebView?>(null) }

    // Real-time connected room participants: No offline/fake profiles!
    val p1Profile = remember(allProfiles, currentUser, gameState) {
        allProfiles.find { it.uid == gameState.startedBy } ?: currentUser
    }
    val p2Profile = remember(allProfiles, p1Profile, gameState.participants) {
        val opponentUid = gameState.participants.firstOrNull { it != p1Profile.uid }
        if (opponentUid != null) {
            allProfiles.find { it.uid == opponentUid } ?: UserProfile(name = "Player 2", uid = opponentUid)
        } else {
            null
        }
    }
    val hasTwoPlayers = p2Profile != null
    val isUserInGame = gameState.participants.contains(currentUser.uid) || gameState.startedBy == currentUser.uid

    if (isCarrom) {
        // -------------------------------------------------------------
        // DEDICATED CARROM LAYOUT: 2-PLAYER & 4-PLAYER MODES + CONFLICT-FREE CONTROLS
        // -------------------------------------------------------------
        val maxPlayers = if (gameState.maxPlayers in listOf(2, 4)) gameState.maxPlayers else 2
        val participants = gameState.participants
        val isGameWaiting = gameState.gameStage != "PLAYING"
        val betDiamonds = (gameState.playerScores["betDiamonds"] ?: 0).toLong()

        val isUserInGame = participants.contains(currentUser.uid) || gameState.startedBy == currentUser.uid
        val canJoinMatch = !isUserInGame && participants.size < maxPlayers
        val isHostOrStarter = canEndGame || gameState.startedBy == currentUser.uid

        // STRICT REAL-TIME ROSTER (ZERO DUMMY PROFILES): Slot 0..3
        val p1Uid = gameState.startedBy.ifBlank { participants.firstOrNull() ?: "" }
        val p1Profile = remember(allProfiles, p1Uid, currentUser) {
            allProfiles.find { it.uid == p1Uid } ?: currentUser
        }

        val p2Uid = participants.getOrNull(1)
        val p2Profile = remember(allProfiles, p2Uid) {
            if (!p2Uid.isNullOrBlank()) allProfiles.find { it.uid == p2Uid } ?: UserProfile(name = "Player 2", uid = p2Uid) else null
        }

        val p3Uid = if (maxPlayers == 4) participants.getOrNull(2) else null
        val p3Profile = remember(allProfiles, p3Uid) {
            if (!p3Uid.isNullOrBlank()) allProfiles.find { it.uid == p3Uid } ?: UserProfile(name = "Player 3", uid = p3Uid) else null
        }

        val p4Uid = if (maxPlayers == 4) participants.getOrNull(3) else null
        val p4Profile = remember(allProfiles, p4Uid) {
            if (!p4Uid.isNullOrBlank()) allProfiles.find { it.uid == p4Uid } ?: UserProfile(name = "Player 4", uid = p4Uid) else null
        }

        // Active Shooter Calculation & 40-Second Real-Time Turn Countdown Timer
        val currentTurnUserUid = gameState.currentTurnUserId.ifBlank { participants.firstOrNull() ?: "" }
        val isMyTurn = !isGameWaiting && (currentTurnUserUid == currentUser.uid)
        val currentShooterProfile = allProfiles.find { it.uid == currentTurnUserUid } ?: if (currentTurnUserUid == currentUser.uid) currentUser else null
        val currentShooterName = currentShooterProfile?.name ?: if (isMyTurn) "You" else "Player"

        var turnSecondsLeft by remember(gameState.currentTurnUserId, gameState.currentTurnIndex, gameState.lastShotTimestamp, isGameWaiting) {
            mutableIntStateOf(40)
        }
        LaunchedEffect(gameState.currentTurnUserId, gameState.currentTurnIndex, gameState.lastShotTimestamp, isGameWaiting, participants.size) {
            if (!isGameWaiting && participants.size >= 2) {
                turnSecondsLeft = 40
                while (turnSecondsLeft > 0) {
                    kotlinx.coroutines.delay(1000L)
                    turnSecondsLeft -= 1
                }
                // 40-second turn expired -> Auto-switch turn in Firestore to next player
                if (isMyTurn || isHostOrStarter) {
                    val nextIdx = (gameState.currentTurnIndex + 1) % participants.size
                    val nextUid = participants.getOrNull(nextIdx) ?: participants.first()
                    db.collection("rooms").document(roomId)
                        .collection("games").document("active")
                        .update(
                            mapOf(
                                "currentTurnIndex" to nextIdx,
                                "currentTurnUserId" to nextUid,
                                "timerRemaining" to 40,
                                "lastShotTimestamp" to System.currentTimeMillis()
                            )
                        )
                    carromWebView?.evaluateJavascript("if (window.forceSwitchTurn) window.forceSwitchTurn();", null)
                }
            } else {
                turnSecondsLeft = 40
            }
        }

        // MIDDLE-SCREEN EDGE-TO-EDGE CARROM CONTAINER (Zero black heads above/below; top mics stay 100% visible!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 124.dp, bottom = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.Transparent)
            ) {
                // ROW 1: TOP CONTROL BAR (TITLE, CHIP, SOUND TOGGLE, END MATCH) - ZERO OVERLAP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎯", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (maxPlayers == 4) "Carrom Pro Club (4P)" else "Carrom Pro Club (1 vs 1)",
                            color = Color(0xFFFFD700),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isGameWaiting) Color(0xFFEAB308).copy(alpha = 0.25f) else Color(0xFF10B981).copy(alpha = 0.25f))
                                .border(0.8.dp, if (isGameWaiting) Color(0xFFEAB308) else Color(0xFF10B981), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = if (isGameWaiting) "Lobby (${participants.size}/$maxPlayers)" else "LIVE",
                                color = if (isGameWaiting) Color(0xFFFDE047) else Color(0xFF34D399),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Top Right Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sound Button
                        IconButton(
                            onClick = {
                                isSoundOn = !isSoundOn
                                carromWebView?.evaluateJavascript("window.setCarromSound($isSoundOn);", null)
                                Toast.makeText(context, if (isSoundOn) "🔊 Sound ON" else "🔇 Sound OFF", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2238))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Text(if (isSoundOn) "🔊" else "🔇", fontSize = 12.sp)
                        }

                        // End Game Button (Host/Admin only)
                        if (canEndGame) {
                            Button(
                                onClick = { showEndGameConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("🛑 End", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // ROW 2: REAL-TIME PLAYER SLOTS STRIP
                if (maxPlayers == 4) {
                    // 4-PLAYER SLOTS (2 vs 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Team White: Slot 1 (⚪ P1) & Slot 3 (⚪ P3)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            CarromSlotBadge(
                                profile = p1Profile,
                                slotName = "P1",
                                coinSymbol = "⚪",
                                isTurn = !isGameWaiting && (currentTurnUserUid == p1Profile.uid),
                                score = carromP1Score
                            )
                            if (p3Profile != null) {
                                CarromSlotBadge(
                                    profile = p3Profile,
                                    slotName = "P3",
                                    coinSymbol = "⚪",
                                    isTurn = !isGameWaiting && (currentTurnUserUid == p3Profile.uid),
                                    score = carromP1Score
                                )
                            } else {
                                CarromJoinSlotButton(
                                    slotName = "+ P3",
                                    coinSymbol = "⚪",
                                    canJoin = canJoinMatch,
                                    onClick = {
                                        db.collection("rooms").document(roomId)
                                            .collection("games").document("active")
                                            .update("participants", FieldValue.arrayUnion(currentUser.uid))
                                        Toast.makeText(context, "🎯 Joined Team White (Slot 3)!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        // Center Score / Turn Indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2E1065).copy(alpha = 0.9f))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isGameWaiting) "VS" else "$carromP1Score : $carromP2Score",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Team Black: Slot 2 (⚫ P2) & Slot 4 (⚫ P4)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (p2Profile != null) {
                                CarromSlotBadge(
                                    profile = p2Profile,
                                    slotName = "P2",
                                    coinSymbol = "⚫",
                                    isTurn = !isGameWaiting && (currentTurnUserUid == p2Profile.uid),
                                    score = carromP2Score
                                )
                            } else {
                                CarromJoinSlotButton(
                                    slotName = "+ P2",
                                    coinSymbol = "⚫",
                                    canJoin = canJoinMatch,
                                    onClick = {
                                        db.collection("rooms").document(roomId)
                                            .collection("games").document("active")
                                            .update("participants", FieldValue.arrayUnion(currentUser.uid))
                                        Toast.makeText(context, "🎯 Joined Team Black (Slot 2)!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            if (p4Profile != null) {
                                CarromSlotBadge(
                                    profile = p4Profile,
                                    slotName = "P4",
                                    coinSymbol = "⚫",
                                    isTurn = !isGameWaiting && (currentTurnUserUid == p4Profile.uid),
                                    score = carromP2Score
                                )
                            } else {
                                CarromJoinSlotButton(
                                    slotName = "+ P4",
                                    coinSymbol = "⚫",
                                    canJoin = canJoinMatch,
                                    onClick = {
                                        db.collection("rooms").document(roomId)
                                            .collection("games").document("active")
                                            .update("participants", FieldValue.arrayUnion(currentUser.uid))
                                        Toast.makeText(context, "🎯 Joined Team Black (Slot 4)!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // 2-PLAYER SLOTS (1 vs 1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player 1 (⚪ White)
                        CarromSlotBadge(
                            profile = p1Profile,
                            slotName = "P1",
                            coinSymbol = "⚪",
                            isTurn = !isGameWaiting && (currentTurnUserUid == p1Profile.uid),
                            score = carromP1Score,
                            turnSecondsLeft = turnSecondsLeft
                        )

                        // Center Turn Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2E1065).copy(alpha = 0.9f))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isGameWaiting) "⏳ Waiting (${participants.size}/$maxPlayers)"
                                       else if (isMyTurn) "👉 Your Turn (${turnSecondsLeft}s)"
                                       else "⏳ ${currentShooterName.take(8)} (${turnSecondsLeft}s)",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Player 2 (⚫ Black) OR + Join Button
                        if (p2Profile != null) {
                            CarromSlotBadge(
                                profile = p2Profile,
                                slotName = "P2",
                                coinSymbol = "⚫",
                                isTurn = !isGameWaiting && (currentTurnUserUid == p2Profile.uid),
                                score = carromP2Score,
                                turnSecondsLeft = turnSecondsLeft
                            )
                        } else {
                            Button(
                                onClick = {
                                    if (canJoinMatch) {
                                        db.collection("rooms").document(roomId)
                                            .collection("games").document("active")
                                            .update("participants", FieldValue.arrayUnion(currentUser.uid))
                                        Toast.makeText(context, "🎯 Joined Carrom match as Player 2!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = canJoinMatch,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("+ Join Match", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // THE CARROM BOARD BOX: TIGHT SQUARE ASPECT RATIO (ZERO WIDE BLACK HEADS!)
                // While WAITING, render ONLY the native Join & Bet Lobby (never mount WebView over Join button!)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.91f)
                        .background(Color.Transparent)
                ) {
                    if (!isGameWaiting) {
                        CarromGameArena(
                            roomId = roomId,
                            gameState = gameState,
                            currentUser = currentUser,
                            db = db,
                            allProfiles = allProfiles,
                            liveComments = liveComments,
                            onWebViewReady = { webView -> carromWebView = webView },
                            onGameStateChange = { turn, p1, p2 ->
                                carromTurn = turn
                                carromP1Score = p1
                                carromP2Score = p2
                            }
                        )
                    }

                    // LOBBY WAITING OVERLAY (Clean native Compose Join & Bet Lobby)
                    if (isGameWaiting) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xEA0F1224))
                                .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.55f), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text("🎯", fontSize = 38.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (maxPlayers == 4) "Carrom Pro Club (4 Players)" else "Carrom Pro Match (1 vs 1)",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Players joined: ${participants.size} / $maxPlayers • ${if (betDiamonds > 0L) "💎 Bet: $betDiamonds" else "🆓 Free Play"}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                // Host/Starter Real-Time Free vs Diamond Bet Selector
                                if (isHostOrStarter) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val betOptions = listOf(0L to "🆓 Free", 50L to "💎 50", 100L to "💎 100", 500L to "💎 500", 1000L to "💎 1K")
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        betOptions.forEach { (amt, label) ->
                                            val isSelected = betDiamonds == amt
                                            Surface(
                                                color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.25f)),
                                                modifier = Modifier.clickable {
                                                    val updatedScores = gameState.playerScores.toMutableMap()
                                                    updatedScores["betDiamonds"] = amt.toInt()
                                                    db.collection("rooms").document(roomId)
                                                        .collection("games").document("active")
                                                        .update("playerScores", updatedScores)
                                                }
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) Color.Black else Color.White,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Join Button for in-room members (verifies diamond balance if Diamond Bet mode)
                                if (canJoinMatch) {
                                    Button(
                                        onClick = {
                                            if (betDiamonds > 0L && currentUser.diamonds < betDiamonds) {
                                                Toast.makeText(context, "💎 You need at least $betDiamonds diamonds to join this bet match!", Toast.LENGTH_LONG).show()
                                                return@Button
                                            }
                                            db.collection("rooms").document(roomId)
                                                .collection("games").document("active")
                                                .update("participants", FieldValue.arrayUnion(currentUser.uid))
                                            Toast.makeText(context, "🎯 You joined the Carrom match!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = if (betDiamonds > 0L) "+ Join Match (💎 $betDiamonds)" else "+ Join Free Match",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Start Match Button (Host / Starter only, strictly requires >= 2 real players)
                                if (isHostOrStarter) {
                                    if (participants.size >= 2) {
                                        Button(
                                            onClick = {
                                                if (betDiamonds > 0L) {
                                                    participants.forEach { pUid ->
                                                        db.collection("users").document(pUid)
                                                            .update("diamonds", FieldValue.increment(-betDiamonds))
                                                    }
                                                }
                                                db.collection("rooms").document(roomId)
                                                    .collection("games").document("active")
                                                    .update(
                                                        mapOf(
                                                            "status" to "ACTIVE",
                                                            "gameStage" to "PLAYING",
                                                            "currentTurnIndex" to 0,
                                                            "currentTurnUserId" to participants.first(),
                                                            "timerRemaining" to 40,
                                                            "lastShotTimestamp" to System.currentTimeMillis(),
                                                            "playerScores" to mapOf("p1" to 0, "p2" to 0, "betDiamonds" to betDiamonds.toInt())
                                                        )
                                                    )
                                                Toast.makeText(context, "🚀 Starting Carrom Match! (40s turn timer)", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                            shape = RoundedCornerShape(20.dp),
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        ) {
                                            Text("🚀 Start Match (${participants.size} Players)", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    } else {
                                        Text(
                                            "Waiting for at least 1 more player to join before starting...",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Text(
                                        if (isUserInGame) "You have joined! Waiting for Host to start the match..."
                                        else "Waiting for Host to start match...",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // MIDDLE-SCREEN CENTERED CONTAINER FOR LUDO & IN-ROOM CLUB GAMES (Top mics stay 100% visible!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 124.dp, bottom = 64.dp),
            contentAlignment = Alignment.Center
        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xE60F111E),
                            Color(0xE616192E),
                            Color(0xE60B0D17)
                        )
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFFD700),
                            Color(0xFF6366F1),
                            Color(0xFFEC4899),
                            Color(0xFFFFD700)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
                .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD700))
        ) {
        Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            // GAME ARENA TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color(0xFF1E1B4B).copy(alpha = 0.90f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            0.5.dp,
                            Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.4f), Color.Transparent))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val gameIcon = when (gameState.gameType) {
                        "ludo" -> "🎲"
                        "carrom" -> "🎯"
                        "truth_dare" -> "🍾"
                        "lucky_seven" -> "🎰"
                        else -> "🎮"
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent)))
                            .border(1.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(gameIcon, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = gameState.title.ifBlank {
                                when (gameState.gameType) {
                                    "ludo" -> "🎲 HD Ludo Club Master"
                                    "carrom" -> "🎯 HD Carrom Pro Club"
                                    "truth_dare" -> "🍾 3D Truth & Dare VIP"
                                    "lucky_seven" -> "🎰 HD Vigo Lucky 7 Club"
                                    else -> "🎮 In-Room Game Club"
                                }
                            },
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hosted by ${gameState.startedByName.ifBlank { "Host" }}",
                                color = BrandGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Turn Timer Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (gameState.timerRemaining <= 5) Color(0xFFEF4444).copy(alpha = 0.35f)
                                else Color(0xFF10B981).copy(alpha = 0.25f)
                            )
                            .border(
                                1.dp,
                                if (gameState.timerRemaining <= 5) Color(0xFFEF4444) else Color(0xFF10B981),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (gameState.timerRemaining <= 5) "⚡" else "⏱️", fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "${gameState.timerRemaining}s",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp
                        )
                    }

                    // Host/Admin: End Game Button
                    if (canEndGame) {
                        Button(
                            onClick = { showEndGameConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("🛑 END", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    } else {
                        IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }
                }
            }

            // GAME CANVAS CONTAINER (Tight wrap height, zero wasted black bars!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                when (gameState.gameType) {
                    "ludo" -> LudoGameArena(roomId, gameState, currentUser, db, allProfiles)
                    "carrom" -> CarromGameArena(roomId, gameState, currentUser, db, allProfiles, liveComments)
                    "truth_dare" -> TruthDareGameArena(roomId, gameState, currentUser, db, allProfiles)
                    "lucky_seven" -> LuckySevenGameArena(roomId, gameState, currentUser, db, allProfiles)
                    else -> LudoGameArena(roomId, gameState, currentUser, db, allProfiles)
                }
            }
        }
        }
        }
    }

    // CONFIRMATION DIALOG FOR ENDING THE GAME (AVAILABLE FOR BOTH CARROM & LUDO)
    if (showEndGameConfirm) {
        Dialog(onDismissRequest = { showEndGameConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E1B2E),
                border = BorderStroke(1.5.dp, Color(0xFFDC2626)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🛑 End In-Room Game?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Are you sure you want to end this match? All participants will be returned to the Voice Lounge seats.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEndGameConfirm = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", color = Color.White, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                showEndGameConfirm = false
                                db.collection("rooms").document(roomId)
                                    .collection("games").document("active")
                                    .update(mapOf("status" to "ENDED"))
                                db.collection("rooms").document(roomId)
                                    .update(mapOf("activeGameType" to ""))
                                Toast.makeText(context, "Game ended. Returned to Voice Lounge.", Toast.LENGTH_SHORT).show()
                                onClose()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm End", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. HD 3D REALISTIC LUDO CLUB MASTER (REAL-TIME JOIN LOBBY, 4 CORNER AVATARS, 30s TIMER, FULL RULES)
// -------------------------------------------------------------
@Composable
fun LudoGameArena(
    roomId: String,
    gameState: InRoomGameState,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    allProfiles: List<UserProfile>
) {
    val context = LocalContext.current
    val participants = gameState.participants
    val isLudoWaiting = gameState.gameStage != "PLAYING"
    val betDiamonds = (gameState.playerScores["betDiamonds"] ?: 0).toLong()
    val isUserInGame = participants.contains(currentUser.uid) || gameState.startedBy == currentUser.uid
    val canJoinLudo = !isUserInGame && participants.size < 4
    val isHostOrStarter = gameState.startedBy == currentUser.uid || currentUser.isAdmin || currentUser.isOfficialAdmin(currentUser.uid)

    // Real-time allotted corner players: 0=Red (TL), 1=Green (TR), 2=Yellow (BR), 3=Blue (BL)
    val ludoColorMeta = listOf(
        Triple("Red", Color(0xFFDC2626), "🔴"),
        Triple("Green", Color(0xFF16A34A), "🟢"),
        Triple("Yellow", Color(0xFFEAB308), "🟡"),
        Triple("Blue", Color(0xFF2563EB), "🔵")
    )
    val joinedPlayersJson = remember(participants, allProfiles, currentUser) {
        val arr = org.json.JSONArray()
        participants.take(4).forEachIndexed { idx, uid ->
            val prof = allProfiles.find { it.uid == uid } ?: if (uid == currentUser.uid) currentUser else UserProfile(uid = uid, name = "Player ${idx + 1}")
            arr.put(org.json.JSONObject().apply {
                put("uid", uid)
                put("name", prof.name.ifBlank { "Player ${idx + 1}" })
                put("photo", prof.photo)
                put("colorIdx", idx)
            })
        }
        arr.toString()
    }

    val currentLudoState by rememberUpdatedState(gameState)
    val myColorIdx = participants.indexOf(currentUser.uid)
    val activeColorIdx = if (participants.isNotEmpty()) (gameState.currentTurnIndex % participants.size) else 0
    val activeTurnUid = participants.getOrNull(activeColorIdx) ?: gameState.startedBy

    // 30-second real-time turn countdown timer with automatic Firestore turn advance
    var ludoTurnSec by remember(gameState.currentTurnIndex, gameState.lastShotTimestamp, isLudoWaiting) {
        mutableIntStateOf(30)
    }
    var ludoWebView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(gameState.currentTurnIndex, gameState.lastShotTimestamp, isLudoWaiting, participants.size) {
        if (!isLudoWaiting && participants.size >= 2) {
            ludoTurnSec = 30
            while (ludoTurnSec > 0) {
                kotlinx.coroutines.delay(1000L)
                ludoTurnSec -= 1
            }
            if (activeTurnUid == currentUser.uid || isHostOrStarter) {
                val nextIdx = (activeColorIdx + 1) % participants.size
                val nextUid = participants.getOrNull(nextIdx) ?: participants.first()
                db.collection("rooms").document(roomId)
                    .collection("games").document("active")
                    .update(
                        mapOf(
                            "currentTurnIndex" to nextIdx,
                            "currentTurnUserId" to nextUid,
                            "timerRemaining" to 30,
                            "lastShotTimestamp" to System.currentTimeMillis()
                        )
                    )
            }
        } else {
            ludoTurnSec = 30
        }
    }

    // Push real-time state into HD 3D Ludo WebView whenever Firestore or timer ticks
    LaunchedEffect(joinedPlayersJson, gameState.coinsStateJson, activeColorIdx, myColorIdx, ludoTurnSec, gameState.diceValue, ludoWebView, isLudoWaiting) {
        if (!isLudoWaiting && ludoWebView != null) {
            val safePlayers = org.json.JSONObject.quote(joinedPlayersJson)
            val safeState = org.json.JSONObject.quote(gameState.coinsStateJson)
            ludoWebView?.evaluateJavascript(
                "if (window.syncLudoState) window.syncLudoState($safePlayers, $safeState, $activeColorIdx, $myColorIdx, $ludoTurnSec, ${gameState.diceValue});",
                null
            )
        }
    }

    if (isLudoWaiting) {
        // REAL-TIME JOIN & BET LOBBY (Minimum 2 real players required before Start!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎲 HD 3D Ludo Club Lobby", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Players: ${participants.size} / 4 • ${if (betDiamonds > 0L) "💎 Bet: $betDiamonds" else "🆓 Free Play"}",
                color = Color(0xFFFFD700),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )

            // 4 Allotted Corner Color Seats Preview
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until 4) {
                    val uid = participants.getOrNull(i)
                    val prof = if (uid != null) (allProfiles.find { it.uid == uid } ?: if (uid == currentUser.uid) currentUser else UserProfile(uid = uid, name = "Player ${i + 1}")) else null
                    val (cName, cColor, cEmoji) = ludoColorMeta[i]
                    Surface(
                        color = if (prof != null) cColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, if (prof != null) cColor else Color.White.copy(alpha = 0.18f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (prof != null) {
                                AsyncImage(
                                    model = prof.photo.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde" },
                                    contentDescription = prof.name,
                                    modifier = Modifier.size(34.dp).clip(CircleShape).border(1.5.dp, cColor, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(prof.name.take(7), color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            } else {
                                Box(
                                    modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cEmoji, fontSize = 15.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(cName, color = Color.Gray, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Host Real-Time Free vs Diamond Bet Selector
            if (isHostOrStarter) {
                Spacer(modifier = Modifier.height(10.dp))
                val betOptions = listOf(0L to "🆓 Free", 50L to "💎 50", 100L to "💎 100", 500L to "💎 500", 1000L to "💎 1K")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    betOptions.forEach { (amt, label) ->
                        val isSelected = betDiamonds == amt
                        Surface(
                            color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier.clickable {
                                val updatedScores = gameState.playerScores.toMutableMap()
                                updatedScores["betDiamonds"] = amt.toInt()
                                db.collection("rooms").document(roomId)
                                    .collection("games").document("active")
                                    .update("playerScores", updatedScores)
                            }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Join Match Button for non-joined room members
            if (canJoinLudo) {
                Button(
                    onClick = {
                        if (betDiamonds > 0L && currentUser.diamonds < betDiamonds) {
                            Toast.makeText(context, "💎 Need at least $betDiamonds diamonds to join!", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        db.collection("rooms").document(roomId)
                            .collection("games").document("active")
                            .update("participants", FieldValue.arrayUnion(currentUser.uid))
                        Toast.makeText(context, "🎲 Joined Ludo Match!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (betDiamonds > 0L) "+ Join Ludo (💎 $betDiamonds)" else "+ Join Free Ludo",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Start Match Button (Host/Starter only, strictly >= 2 real players)
            if (isHostOrStarter) {
                if (participants.size >= 2) {
                    Button(
                        onClick = {
                            if (betDiamonds > 0L) {
                                participants.forEach { pUid ->
                                    db.collection("users").document(pUid)
                                        .update("diamonds", FieldValue.increment(-betDiamonds))
                                }
                            }
                            db.collection("rooms").document(roomId)
                                .collection("games").document("active")
                                .update(
                                    mapOf(
                                        "status" to "ACTIVE",
                                        "gameStage" to "PLAYING",
                                        "currentTurnIndex" to 0,
                                        "currentTurnUserId" to participants.first(),
                                        "timerRemaining" to 30,
                                        "coinsStateJson" to "",
                                        "lastShotTimestamp" to System.currentTimeMillis()
                                    )
                                )
                            Toast.makeText(context, "🎲 Starting HD 3D Ludo Match!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("🚀 Start Ludo Match (${participants.size} Players)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Waiting for at least 1 more player to join (min 2 players)...",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = if (isUserInGame) "You joined! Waiting for Host to start..." else "Tap Join to enter your color corner!",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // LIVE HD 3D REALISTIC LUDO BOARD (60 FPS WebView Canvas, zero black bars!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.84f)
                .background(Color.Transparent)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = true
                        addJavascriptInterface(object {
                            @android.webkit.JavascriptInterface
                            fun onMoveCompleted(stateJson: String, rolledDice: Int, switchTurn: Boolean) {
                                (context as? android.app.Activity)?.runOnUiThread {
                                    val liveState = currentLudoState
                                    val liveParticipants = liveState.participants
                                    if (liveParticipants.isEmpty()) return@runOnUiThread
                                    val curTurnIdx = liveState.currentTurnIndex % liveParticipants.size
                                    val curTurnUid = liveParticipants.getOrNull(curTurnIdx) ?: liveState.startedBy
                                    if (curTurnUid != currentUser.uid) return@runOnUiThread

                                    val nextIdx = if (switchTurn) (curTurnIdx + 1) % liveParticipants.size else curTurnIdx
                                    val nextUid = liveParticipants.getOrNull(nextIdx) ?: liveParticipants.first()
                                    db.collection("rooms").document(roomId)
                                        .collection("games").document("active")
                                        .update(
                                            mapOf(
                                                "coinsStateJson" to stateJson,
                                                "diceValue" to rolledDice,
                                                "currentTurnIndex" to nextIdx,
                                                "currentTurnUserId" to nextUid,
                                                "timerRemaining" to 30,
                                                "lastShotTimestamp" to System.currentTimeMillis()
                                            )
                                        )
                                }
                            }

                            @android.webkit.JavascriptInterface
                            fun onGameOver(winnerColorIdx: Int) {
                                (context as? android.app.Activity)?.runOnUiThread {
                                    val winnerUid = participants.getOrNull(winnerColorIdx) ?: ""
                                    val winnerName = allProfiles.find { it.uid == winnerUid }?.name ?: ludoColorMeta.getOrNull(winnerColorIdx)?.first ?: "Winner"
                                    if (betDiamonds > 0L && winnerUid.isNotBlank()) {
                                        val totalPot = betDiamonds * participants.size
                                        db.collection("users").document(winnerUid)
                                            .update("diamonds", FieldValue.increment(totalPot))
                                    }
                                    Toast.makeText(context, "🏆 $winnerName Wins HD Ludo Match!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }, "LudoBridge")
                        webViewClient = object : android.webkit.WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                val safePlayers = org.json.JSONObject.quote(joinedPlayersJson)
                                val safeState = org.json.JSONObject.quote(gameState.coinsStateJson)
                                view?.evaluateJavascript(
                                    "if (window.syncLudoState) window.syncLudoState($safePlayers, $safeState, $activeColorIdx, $myColorIdx, $ludoTurnSec, ${gameState.diceValue});",
                                    null
                                )
                            }
                        }
                        loadUrl("file:///android_asset/ludo_game.html")
                        ludoWebView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

data class LudoPlayerSlot(
    val profile: UserProfile,
    val colorName: String,
    val badgeColor: Color,
    val colorEmoji: String,
    val score: Int
)

@Composable
fun LudoPlayerCard(slot: LudoPlayerSlot, isTurn: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "ludo_turn")
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isTurn) slot.badgeColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.45f))
            .border(
                width = if (isTurn) pulseBorder.dp else 1.dp,
                color = if (isTurn) slot.badgeColor else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(1.2.dp, slot.badgeColor, CircleShape)
        ) {
            AsyncImage(
                model = slot.profile.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                contentDescription = slot.profile.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(slot.colorEmoji, fontSize = 9.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    slot.profile.name.take(6),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
            Text(
                "⭐ ${slot.score} pts",
                color = BrandGold,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// -------------------------------------------------------------
// 2. HD REALISTIC CARROM PRO CLUB (TOURNAMENT TEAKWOOD BOARD, 19 PIECES & ACCURATE PHYSICS)
// -------------------------------------------------------------
data class PhysicalCarromPiece(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float = 6.2f,
    val initialRadius: Float = 6.2f,
    val type: String, // "queen", "white", "black"
    var isPocketed: Boolean = false,
    var isSinking: Boolean = false
)

@Composable
fun CarromGameArena(
    roomId: String,
    gameState: InRoomGameState,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    allProfiles: List<UserProfile>,
    liveComments: List<LiveRoomComment> = emptyList(),
    onWebViewReady: (WebView) -> Unit = {},
    onGameStateChange: (turn: String, p1Score: Int, p2Score: Int) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    var isGameCommentsExpanded by remember { mutableStateOf(false) }
    var gameCommentInputText by remember { mutableStateOf("") }
    var activeWebView by remember { mutableStateOf<WebView?>(null) }

    val currentGameState by rememberUpdatedState(gameState)

    // Listen for remote shots from other players!
    var lastHandledShotTime by remember { mutableLongStateOf(0L) }
    LaunchedEffect(gameState.lastShotTimestamp, gameState.lastShotJson) {
        if (gameState.lastShotTimestamp > lastHandledShotTime && gameState.lastShotJson.isNotBlank()) {
            lastHandledShotTime = gameState.lastShotTimestamp
            try {
                val json = JSONObject(gameState.lastShotJson)
                val shooterUid = json.optString("shooterUid")
                if (shooterUid != currentUser.uid) {
                    val startX = json.optDouble("startX")
                    val startY = json.optDouble("startY")
                    val vx = json.optDouble("vx")
                    val vy = json.optDouble("vy")
                    val turn = json.optString("turn")
                    activeWebView?.evaluateJavascript(
                        "window.applyRemoteShot($startX, $startY, $vx, $vy, '$turn');",
                        null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.w("CarromGameArena", "Error applying remote shot: ${e.message}")
            }
        }
    }

    // Sync coins state from the authoritative shot finish
    LaunchedEffect(gameState.coinsStateJson, gameState.currentTurnIndex, gameState.currentTurnUserId) {
        if (gameState.coinsStateJson.isNotBlank()) {
            try {
                val safeCoins = JSONObject.quote(gameState.coinsStateJson)
                val turnName = if (gameState.currentTurnIndex % 2 == 0) "p1" else "p2"
                activeWebView?.evaluateJavascript(
                    "window.syncCoinsState($safeCoins, '$turnName');",
                    null
                )
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // Control lock: Only current turn player can shoot!
    val currentTurnUserUid = gameState.currentTurnUserId.ifBlank { gameState.participants.firstOrNull() ?: "" }
    val isMyTurn = (gameState.gameStage == "PLAYING" || gameState.gameStage == "ACTIVE") && (currentTurnUserUid == currentUser.uid)
    LaunchedEffect(isMyTurn, currentTurnUserUid, gameState.gameStage, gameState.currentTurnIndex) {
        val turnName = if (gameState.currentTurnIndex % 2 == 0) "p1" else "p2"
        activeWebView?.evaluateJavascript("window.setPlayerControl($isMyTurn, '$turnName');", null)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // High Definition Realistic Carrom WebView
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
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(false)
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                val turnName = if (gameState.currentTurnIndex % 2 == 0) "p1" else "p2"
                                view?.evaluateJavascript("window.setPlayerControl($isMyTurn, '$turnName');", null)
                                if (gameState.coinsStateJson.isNotBlank()) {
                                    view?.evaluateJavascript("window.syncCoinsState('${gameState.coinsStateJson}');", null)
                                }
                            }
                        }
                        addJavascriptInterface(object {
                            @android.webkit.JavascriptInterface
                            fun onShoot(startX: Float, startY: Float, vx: Float, vy: Float, turn: String) {
                                (context as? android.app.Activity)?.runOnUiThread {
                                    val shotData = JSONObject().apply {
                                        put("type", "shot")
                                        put("shotId", System.currentTimeMillis())
                                        put("shooterUid", currentUser.uid)
                                        put("startX", startX.toDouble())
                                        put("startY", startY.toDouble())
                                        put("vx", vx.toDouble())
                                        put("vy", vy.toDouble())
                                        put("turn", turn)
                                    }.toString()

                                    db.collection("rooms").document(roomId)
                                        .collection("games").document("active")
                                        .update(
                                            mapOf(
                                                "lastShotJson" to shotData,
                                                "lastShotTimestamp" to System.currentTimeMillis()
                                            )
                                        )
                                }
                            }

                            @android.webkit.JavascriptInterface
                            fun onGameState(turn: String, p1: Int, p2: Int, queenCover: String) {
                                (context as? android.app.Activity)?.runOnUiThread {
                                    onGameStateChange(turn, p1, p2)
                                }
                            }

                            @android.webkit.JavascriptInterface
                            fun onTurnFinished(turn: String, p1: Int, p2: Int, action: String, coinsJson: String? = null) {
                                (context as? android.app.Activity)?.runOnUiThread {
                                    val liveState = currentGameState
                                    val participants = liveState.participants
                                    val activeShooterUid = liveState.currentTurnUserId.ifBlank {
                                        participants.getOrNull(liveState.currentTurnIndex) ?: participants.firstOrNull() ?: ""
                                    }
                                    // CRITICAL: ONLY the player whose turn it currently is can advance or repeat the turn in Firestore!
                                    if (activeShooterUid != currentUser.uid) {
                                        return@runOnUiThread
                                    }

                                    if (participants.isNotEmpty()) {
                                        val nextTurnIndex = if (action == "switch") {
                                            (liveState.currentTurnIndex + 1) % participants.size
                                        } else {
                                            liveState.currentTurnIndex
                                        }
                                        val nextTurnUserId = participants.getOrNull(nextTurnIndex) ?: participants.first()
                                        val betVal = liveState.playerScores["betDiamonds"] ?: 0

                                        val updateMap = mutableMapOf<String, Any>(
                                            "currentTurnIndex" to nextTurnIndex,
                                            "currentTurnUserId" to nextTurnUserId,
                                            "timerRemaining" to 40,
                                            "lastShotTimestamp" to System.currentTimeMillis(),
                                            "lastShotJson" to "",
                                            "playerScores" to mapOf("p1" to p1, "p2" to p2, "betDiamonds" to betVal)
                                        )
                                        if (!coinsJson.isNullOrBlank()) {
                                            updateMap["coinsStateJson"] = coinsJson
                                        }

                                        db.collection("rooms").document(roomId)
                                            .collection("games").document("active")
                                            .update(updateMap)
                                    }
                                }
                            }

                            @android.webkit.JavascriptInterface
                            fun onGameOver(winner: String, p1: Int, p2: Int) {
                                (context as? android.app.Activity)?.runOnUiThread {
                                    val winningTeam = if (winner == "p1") "White Team" else "Black Team"
                                    Toast.makeText(context, "🏆 Match Finished! $winningTeam Wins! ($p1 - $p2)", Toast.LENGTH_LONG).show()
                                }
                            }
                        }, "CarromBridge")
                        loadUrl("file:///android_asset/carrom_game.html")
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        activeWebView = this
                        onWebViewReady(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // -------------------------------------------------------------
        // Collapsible Sliding Comment Box with Arrow Button (▲ / ▼)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF140D07).copy(alpha = 0.95f))
                .border(BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f)))
        ) {
            // Arrow Toggle Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isGameCommentsExpanded = !isGameCommentsExpanded }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGameCommentsExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = "Toggle Comments",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGameCommentsExpanded) "Hide Live Comments" else "💬 Live Room Comments (${liveComments.size})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = if (isGameCommentsExpanded) "▼ Tap to slide down" else "▲ Tap to slide up",
                    color = Color(0xFFFFD700).copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Animated Expanding Comment List & Input
            AnimatedVisibility(
                visible = isGameCommentsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val gameChatState = rememberLazyListState()
                    LaunchedEffect(liveComments.size) {
                        if (liveComments.isNotEmpty()) {
                            gameChatState.animateScrollToItem(liveComments.size - 1)
                        }
                    }
                    LazyColumn(
                        state = gameChatState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (liveComments.isEmpty()) {
                            item {
                                Text(
                                    text = "No comments yet in the room.",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            items(liveComments.takeLast(25)) { comment ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${comment.userName}: ",
                                        color = Color(0xFFFFD700),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = comment.message,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = gameCommentInputText,
                            onValueChange = { gameCommentInputText = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (gameCommentInputText.isEmpty()) {
                                    Text("Comment while playing...", color = Color.Gray, fontSize = 11.sp)
                                }
                                innerTextField()
                            }
                        )
                        IconButton(
                            onClick = {
                                if (gameCommentInputText.isNotBlank()) {
                                    val msg = gameCommentInputText.trim()
                                    val badWord = AbuseFilterEngine.findAbusiveWord(msg)
                                    if (badWord != null) {
                                        Toast.makeText(context, "🚫 Word ($badWord) is blocked", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val commentDoc = LiveRoomComment(
                                            id = java.util.UUID.randomUUID().toString(),
                                            roomId = roomId,
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
                                        db.collection("rooms").document(roomId)
                                            .collection("live_comments").document(commentDoc.id)
                                            .set(commentDoc)
                                        gameCommentInputText = ""
                                    }
                                }
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CarromSlotBadge(
    profile: UserProfile,
    slotName: String,
    coinSymbol: String,
    isTurn: Boolean,
    score: Int,
    turnSecondsLeft: Int = 40
) {
    val timerFraction = (turnSecondsLeft.coerceIn(0, 40) / 40f)
    val timerColor = when {
        turnSecondsLeft <= 10 -> Color(0xFFEF4444)
        turnSecondsLeft <= 20 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.78f))
            .border(
                if (isTurn) 1.8.dp else 0.8.dp,
                if (isTurn) timerColor else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier.size(30.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isTurn) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = timerColor,
                        startAngle = -90f,
                        sweepAngle = 360f * timerFraction,
                        useCenter = false,
                        style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(23.dp)
                    .clip(CircleShape)
                    .border(1.dp, if (coinSymbol == "⚪") Color.White else Color.Gray, CircleShape)
            ) {
                AsyncImage(
                    model = profile.photo.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde" },
                    contentDescription = profile.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(coinSymbol, fontSize = 9.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = profile.name.take(7),
                    color = Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Text(
                text = if (isTurn) "⏱️ ${turnSecondsLeft}s • $score" else "Score: $score",
                color = if (isTurn) timerColor else Color.LightGray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CarromJoinSlotButton(
    slotName: String,
    coinSymbol: String,
    canJoin: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E2238))
            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .clickable(enabled = canJoin) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(coinSymbol, fontSize = 9.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = slotName,
                color = Color(0xFF10B981),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CarromPlayerBadge(profile: UserProfile, isWhite: Boolean, isTurn: Boolean, score: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "carrom_turn")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse_carrom"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isTurn) Color(0xFFD97706).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.45f))
            .border(
                width = if (isTurn) pulse.dp else 1.dp,
                color = if (isTurn) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .border(1.dp, if (isWhite) Color.White else Color(0xFF64748B), CircleShape)
        ) {
            AsyncImage(
                model = profile.photo.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb" },
                contentDescription = profile.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                profile.name.take(6),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isWhite) "⚪" else "⚫", fontSize = 7.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text("$score coins", color = Color(0xFFFFD700), fontSize = 8.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// 3. 3D TRUTH & DARE VIP LOUNGE (8 LUXURY SEATS, CRYSTAL AMBER BOTTLE, INTERACTIVE CARDS)
// -------------------------------------------------------------
@Composable
fun TruthDareGameArena(
    roomId: String,
    gameState: InRoomGameState,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    allProfiles: List<UserProfile>
) {
    var bottleAngle by remember { mutableFloatStateOf(gameState.bottleAngle) }
    var isSpinning by remember { mutableStateOf(false) }
    var currentChallenge by remember { mutableStateOf<String?>(null) }
    val animatedAngle by animateFloatAsState(
        targetValue = bottleAngle,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "bottleSpin"
    )

    val seats = remember(allProfiles) {
        val list = allProfiles.take(8).toMutableList()
        while (list.size < 8) {
            list.add(UserProfile(name = "VIP ${list.size + 1}", uid = "guest_${list.size}"))
        }
        list
    }

    val selectedIndex = ((animatedAngle % 360 + 360) % 360 / 45).toInt().coerceIn(0, 7)
    val targetedPlayer = seats[selectedIndex]

    val truths = listOf(
        "Who is your secret crush in this room?",
        "What was your most embarrassing moment?",
        "What is the biggest lie you ever told?",
        "If you could trade lives with someone here, who?"
    )
    val dares = listOf(
        "Sing 2 lines of your favorite song right now on mic!",
        "Send a voice note doing a funny Bollywood dialogue!",
        "Give a grand compliment to the room Host!",
        "Change your room status to 'In Love' for 1 hour!"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP BANNER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "🎯 Targeted: ${targetedPlayer.name}",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                if (isSpinning) "🌀 Bottle Spinning..." else "🍾 Tap Bottle to Spin!",
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }

        // CIRCULAR VIP TABLE WITH 8 LUXURY SEATS
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF1E1B4B), Color(0xFF0F0B1E), Color(0xFF030712))
                    )
                )
                .border(2.dp, Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFEC4899), Color(0xFFFFD700))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // 8 Circular Profile Seats in Round Table
            seats.forEachIndexed { index, profile ->
                val angleRad = Math.toRadians((index * 45.0) - 90.0)
                val radius = 94.0
                val x = (radius * cos(angleRad)).dp
                val y = (radius * sin(angleRad)).dp
                val isSelected = index == selectedIndex && !isSpinning

                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFFFFD700) else Color(0xFF1E293B))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.photo.isNotBlank()) {
                        AsyncImage(
                            model = profile.photo,
                            contentDescription = profile.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            profile.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Central 3D Crystal Amber Glass Bottle
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .rotate(animatedAngle)
                    .clickable {
                        if (!isSpinning) {
                            isSpinning = true
                            currentChallenge = null
                            val spinDelta = 1080f + Random.nextInt(0, 360)
                            val target = bottleAngle + spinDelta
                            bottleAngle = target
                            db.collection("rooms").document(roomId)
                                .collection("games").document("active")
                                .update(mapOf("bottleAngle" to target, "timerRemaining" to 30))
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isSpinning = false
                                currentChallenge = if (Random.nextBoolean()) "TRUTH: " + truths.random() else "DARE: " + dares.random()
                            }, 2000)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Realistic Amber Glass Bottle with Highlights
                    drawRoundRect(
                        Brush.linearGradient(
                            listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFF78350F))
                        ),
                        Offset(w * 0.40f, h * 0.28f),
                        Size(w * 0.20f, h * 0.48f),
                        androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )
                    // Glass highlight streak
                    drawLine(
                        Color.White.copy(alpha = 0.6f),
                        Offset(w * 0.43f, h * 0.32f),
                        Offset(w * 0.43f, h * 0.70f),
                        strokeWidth = 2f
                    )
                    // Neck
                    drawRect(
                        Color(0xFFB45309),
                        Offset(w * 0.44f, h * 0.10f),
                        Size(w * 0.12f, h * 0.20f)
                    )
                    // Golden Cork
                    drawCircle(Color(0xFFFFD700), 5.5f, Offset(w * 0.50f, h * 0.08f))
                }
            }
        }

        // BOTTOM CHALLENGE CARD
        if (currentChallenge != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1B4B).copy(alpha = 0.9f))
                    .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            currentChallenge!!,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = { currentChallenge = null },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Done ✨", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Text(
                "Tap the bottle to spin and challenge the room!",
                color = BrandGold,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// 4. HD VIGO LUCKY 7 / CASINO DICE ARENA (GOLD FRAME VEGAS TABLE, 3D DICE SHAKER)
// -------------------------------------------------------------
@Composable
fun LuckySevenGameArena(
    roomId: String,
    gameState: InRoomGameState,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    allProfiles: List<UserProfile>
) {
    var dice1 by remember { mutableIntStateOf(3) }
    var dice2 by remember { mutableIntStateOf(4) }
    var isRolling by remember { mutableStateOf(false) }
    var selectedBet by remember { mutableStateOf<String?>("7_UP") }
    var resultText by remember { mutableStateOf("7 UP WINS! (Dice: 3 + 4 = 7)") }

    val diceSum = dice1 + dice2

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP SCORE & BETTING STATUS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💰", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Chips: 5,000", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
            Text(resultText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        // REALISTIC VEGAS GOLD-FRAMED DICE TABLE
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF065F46), Color(0xFF064E3B), Color(0xFF022C22))
                    )
                )
                .border(6.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFB45309), Color(0xFFFFD700))), RoundedCornerShape(18.dp))
                .shadow(14.dp, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 3D Dices in Center
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Casino3DDice(dice1, isRolling)
                    Casino3DDice(dice2, isRolling)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Total Sum Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(20))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Total: $diceSum ${if (diceSum == 7) "👑 LUCKY 7!" else if (diceSum > 7) "🔺 7 UP!" else "🔻 7 DOWN!"}",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // 3 BETTING ZONES BUTTONS (7 DOWN • LUCKY 7 • 7 UP)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LuckyBetButton(
                title = "🔻 7 DOWN",
                multiplier = "2X",
                isSelected = selectedBet == "7_DOWN",
                modifier = Modifier.weight(1f),
                onClick = { selectedBet = "7_DOWN" }
            )
            LuckyBetButton(
                title = "⭐ LUCKY 7",
                multiplier = "5X",
                isSelected = selectedBet == "7_LUCKY",
                modifier = Modifier.weight(1f),
                onClick = { selectedBet = "7_LUCKY" }
            )
            LuckyBetButton(
                title = "🔺 7 UP",
                multiplier = "2X",
                isSelected = selectedBet == "7_UP",
                modifier = Modifier.weight(1f),
                onClick = { selectedBet = "7_UP" }
            )
        }

        // ROLL DICE TRIGGER
        Button(
            onClick = {
                if (!isRolling) {
                    isRolling = true
                    dice1 = Random.nextInt(1, 7)
                    dice2 = Random.nextInt(1, 7)
                    val sum = dice1 + dice2
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isRolling = false
                        resultText = if (sum == 7) "⭐ LUCKY 7 WINS! (5X)" else if (sum > 7) "🔺 7 UP WINS! (2X)" else "🔻 7 DOWN WINS! (2X)"
                        db.collection("rooms").document(roomId)
                            .collection("games").document("active")
                            .update(mapOf("diceValue" to sum, "timerRemaining" to 30))
                    }, 650)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text("🎲 ROLL CASINO DICE (30s)", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        }
    }
}

@Composable
fun Casino3DDice(value: Int, isRolling: Boolean) {
    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 720f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "casinoDice"
    )

    Box(
        modifier = Modifier
            .size(46.dp)
            .rotate(rotation)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC), Color(0xFFCBD5E1))
                )
            )
            .border(2.dp, Color(0xFFDC2626), RoundedCornerShape(10.dp))
            .shadow(8.dp, RoundedCornerShape(10.dp), spotColor = Color(0xFFDC2626)),
        contentAlignment = Alignment.Center
    ) {
        val diceSymbol = when (value) {
            1 -> "⚀"
            2 -> "⚁"
            3 -> "⚂"
            4 -> "⚃"
            5 -> "⚄"
            else -> "⚅"
        }
        Text(diceSymbol, fontSize = 32.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Black)
    }
}

@Composable
fun LuckyBetButton(
    title: String,
    multiplier: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFFFFD700) else Color(0xFF1E293B))
            .border(
                1.2.dp,
                if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                color = if (isSelected) Color.Black else Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
            )
            Text(
                multiplier,
                color = if (isSelected) Color(0xFFB45309) else Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp
            )
        }
    }
}

// -------------------------------------------------------------
// GAME LAUNCHER PICKER DIALOG (HOST & ADMIN: 4 REALISTIC HD CLUB GAMES)
// -------------------------------------------------------------
@Composable
fun InRoomGameLauncherDialog(
    roomId: String,
    onDismiss: () -> Unit,
    onGameSelected: (String) -> Unit,
    onCarromSelected: ((maxPlayers: Int, betDiamonds: Long) -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF131524),
            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎮 Launch Realistic HD Game Club",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    "Synchronous multiplayer with all room members & 3D realistic visuals.",
                    color = Color.Gray,
                    fontSize = 10.5.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))

                val games = listOf(
                    Triple("ludo", "🎲 HD Ludo Club Master", "Realistic Wood & Gold Board • 4-Corner Players • 3D Tumbling Dice"),
                    Triple("carrom", "🎯 HD Carrom Pro Club", "Tournament Teakwood • 2-Player & 4-Player Match • Real-time Physics"),
                    Triple("truth_dare", "🍾 3D Truth & Dare VIP", "Specular Crystal Bottle • 8-Seat Luxury Lounge • Interactive Cards"),
                    Triple("lucky_seven", "🎰 HD Vigo Lucky 7 Club", "Vegas Gold Frame Table • 3D Rolling Dices • Real-time Multipliers")
                )

                var selectedGameType by remember { mutableStateOf<String?>(null) }
                var selectedBetDiamonds by remember { mutableLongStateOf(0L) }
                var selectedMaxPlayers by remember { mutableIntStateOf(2) }

                if (selectedGameType == null) {
                    games.forEach { (type, name, desc) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E2238))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .clickable {
                                    if (type == "carrom") {
                                        selectedGameType = type
                                    } else {
                                        onGameSelected(type)
                                        onDismiss()
                                    }
                                }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    Text(desc, color = BrandGold, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (type == "carrom") {
                                            selectedGameType = type
                                        } else {
                                            onGameSelected(type)
                                            onDismiss()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Play 🚀", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Carrom Mode & Players / Free selection
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🎯 Choose Carrom Match Mode",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Choose player count and match mode",
                            color = Color.LightGray,
                            fontSize = 10.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Player Count Selector (2 Players vs 4 Players)
                        Text(
                            "Match Format",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedMaxPlayers == 2) Color(0xFF6366F1).copy(alpha = 0.4f) else Color(0xFF1E2238))
                                    .border(
                                        if (selectedMaxPlayers == 2) 1.5.dp else 0.8.dp,
                                        if (selectedMaxPlayers == 2) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMaxPlayers = 2 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("👥 2 Players", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    Text("1 vs 1 Head-to-Head", color = Color(0xFFFFD700), fontSize = 8.5.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedMaxPlayers == 4) Color(0xFF6366F1).copy(alpha = 0.4f) else Color(0xFF1E2238))
                                    .border(
                                        if (selectedMaxPlayers == 4) 1.5.dp else 0.8.dp,
                                        if (selectedMaxPlayers == 4) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMaxPlayers = 4 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("👨‍👩‍👧‍👦 4 Players", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    Text("2 vs 2 Team Match", color = Color(0xFFFFD700), fontSize = 8.5.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Match Stakes",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val betOptions = listOf(
                            Pair("🎮 Free Match (Casual Play)", 0L),
                            Pair("💎 10 Diamonds Stake", 10L),
                            Pair("💎 50 Diamonds Stake", 50L)
                        )

                        betOptions.forEach { (label, amount) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedBetDiamonds == amount) Color(0xFF6366F1).copy(alpha = 0.4f) else Color(0xFF1E2238))
                                    .border(
                                        if (selectedBetDiamonds == amount) 1.5.dp else 0.8.dp,
                                        if (selectedBetDiamonds == amount) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedBetDiamonds = amount }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (selectedBetDiamonds == amount) {
                                        Text("✓", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { selectedGameType = null },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back", color = Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    if (onCarromSelected != null) {
                                        onCarromSelected(selectedMaxPlayers, selectedBetDiamonds)
                                    } else {
                                        onGameSelected("carrom")
                                    }
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Start Carrom 🚀", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HELPER TO LAUNCH IN-ROOM GAME IN FIRESTORE
// -------------------------------------------------------------
fun launchInRoomGame(
    roomId: String,
    gameType: String,
    currentUser: UserProfile,
    db: FirebaseFirestore,
    maxPlayers: Int = 2,
    betDiamonds: Long = 0L,
    allProfiles: List<UserProfile> = emptyList()
): InRoomGameState {
    // STRICT REAL-TIME POLICY: Zero dummy/fake opponents! Only the host/starter joins initially;
    // real players must tap Join Match, and the game stays in WAITING until >= 2 real players join & Start is tapped.
    val initialParticipants = listOf(currentUser.uid)

    val initialGameState = InRoomGameState(
        gameType = gameType,
        title = when (gameType) {
            "ludo" -> "🎲 HD Ludo Club Master"
            "carrom" -> if (maxPlayers == 4) "🎯 HD Carrom Pro (4 Players)" else "🎯 HD Carrom Pro (1 vs 1)"
            "truth_dare" -> "🍾 3D Truth & Dare VIP"
            "lucky_seven" -> "🎰 HD Vigo Lucky 7 Club"
            else -> "🎮 In-Room Game Club"
        },
        status = "ACTIVE",
        gameStage = if (gameType == "carrom" || gameType == "ludo") "WAITING" else "PLAYING",
        maxPlayers = maxPlayers,
        startedBy = currentUser.uid,
        startedByName = currentUser.name,
        startedAt = System.currentTimeMillis(),
        currentTurnIndex = 0,
        currentTurnUserId = currentUser.uid,
        timerRemaining = 40,
        diceValue = 1,
        bottleAngle = 0f,
        selectedPlayerId = "",
        playerScores = mapOf("p1" to 0, "p2" to 0, "betDiamonds" to betDiamonds.toInt()),
        participants = initialParticipants
    )
    db.collection("rooms").document(roomId)
        .collection("games").document("active")
        .set(initialGameState)
    return initialGameState
}

// -------------------------------------------------------------
// 3D ANIMATED GAME LAUNCHER BUTTON (HOVERS SEPARATE ABOVE GIFT BOX)
// -------------------------------------------------------------
@Composable
fun Animated3DGameLauncherButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "game_3d_anim")

    // Smooth breathing scale
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Subtle 3D tilt rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tilt"
    )

    // Glowing border pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Animated game preview cycling through: 🎲 -> 🎯 -> 🍾 -> 🎰 -> 🎮
    val previewIcons = remember { listOf("🎲", "🎯", "🍾", "🎰", "🎮") }
    var currentIconIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1600L)
            currentIconIndex = (currentIconIndex + 1) % previewIcons.size
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .size(42.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(13.dp), ambientColor = Color(0xFF6366F1), spotColor = Color(0xFFEC4899))
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6366F1), // Electric Indigo
                        Color(0xFF8B5CF6), // Royal Purple
                        Color(0xFFEC4899)  // Hot Pink
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFFFD700).copy(alpha = glowAlpha),
                        Color(0xFF00E5FF).copy(alpha = glowAlpha),
                        Color(0xFFFF4081).copy(alpha = glowAlpha),
                        Color(0xFFFFD700).copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(13.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = previewIcons[currentIconIndex],
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.6f))
                        .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.2f))
                },
                label = "game_icon_cycle"
            ) { icon ->
                Text(
                    text = icon,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = "GAME",
                color = Color(0xFFFFD700),
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.offset(y = (-1).dp)
            )
        }
    }
}
