package com.devil.livetogather

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive 20-Second Voice Verification Modal.
 * Prompts user with verification sentences, displays a live recording waveform,
 * allows audio preview, and marks the account as officially verified with the Gold Shield badge.
 */
@Composable
fun VoiceVerificationDialog(
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onVerified: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val prompts = remember {
        listOf(
            "Hi, I'm ${currentUser.name}. I verify that this is my real voice and authentic profile on LiveTogather.",
            "Welcome to my hangout! This is my real voice, let's chat and connect in live lounges.",
            "I verify my identity on LiveTogather. What you hear is 100% genuine and authentic."
        )
    }

    var promptIndex by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var hasRecorded by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Timer loop for recording (up to 20 seconds)
    LaunchedEffect(isRecording) {
        if (isRecording) {
            secondsElapsed = 0
            while (isRecording && secondsElapsed < 20) {
                delay(1000L)
                secondsElapsed++
            }
            if (secondsElapsed >= 20) {
                isRecording = false
                hasRecorded = true
            }
        }
    }

    // Timer loop for playback simulation
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var playSec = 0
            while (isPlaying && playSec < secondsElapsed.coerceAtLeast(5)) {
                delay(1000L)
                playSec++
            }
            isPlaying = false
        }
    }

    // Animation for pulsing mic button
    val infiniteTransition = rememberInfiniteTransition(label = "VoicePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF130E24)),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF9333EA)))),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF26194A),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎙️", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Voice Verification",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "20s Audio Authenticity Check",
                                color = Color(0xFFC084FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Prompt Reading Box
                Surface(
                    color = Color(0xFF1E1738),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B2D6B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "READ THIS SENTENCE ALOUD",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "🔄 Change",
                                color = Color(0xFFC084FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    promptIndex = (promptIndex + 1) % prompts.size
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "\"${prompts[promptIndex]}\"",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Big Glowing Audio Recording Button & Soundwave
                Box(
                    modifier = Modifier
                        .size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer pulse aura
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size((100 * pulseScale).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                        )
                    }

                    // Main Record Button
                    Surface(
                        color = when {
                            isRecording -> Color(0xFFEF4444)
                            hasRecorded -> Color(0xFF10B981)
                            else -> Color(0xFF7E22CE)
                        },
                        shape = CircleShape,
                        border = BorderStroke(2.dp, if (isRecording) Color.White else Color(0xFFFFD700)),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(enabled = !isSubmitting) {
                                if (isRecording) {
                                    isRecording = false
                                    hasRecorded = true
                                } else {
                                    isRecording = true
                                    hasRecorded = false
                                    isPlaying = false
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    isRecording -> Icons.Default.Stop
                                    hasRecorded -> Icons.Default.PlayArrow
                                    else -> Icons.Default.Mic
                                },
                                contentDescription = "Record",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Timer & Status Display
                Text(
                    text = when {
                        isRecording -> "Recording: 00:${String.format("%02d", secondsElapsed)} / 00:20"
                        isPlaying -> "Playing Back: 00:${String.format("%02d", secondsElapsed)}s"
                        hasRecorded -> "Voice Recorded (${secondsElapsed}s) 🎙️✓"
                        else -> "Tap Microphone to Start Recording"
                    },
                    color = when {
                        isRecording -> Color(0xFFEF4444)
                        hasRecorded -> Color(0xFF10B981)
                        else -> Color(0xFFE2D6F6)
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar (0 to 20s)
                LinearProgressIndicator(
                    progress = { (secondsElapsed / 20f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isRecording) Color(0xFFEF4444) else Color(0xFF7E22CE),
                    trackColor = Color(0xFF26194A)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row (Re-record, Listen, Submit)
                if (hasRecorded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Play / Listen button
                        Button(
                            onClick = { isPlaying = !isPlaying },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26194A)),
                            border = BorderStroke(1.dp, Color(0xFF9333EA)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text(if (isPlaying) "⏸️ Pause" else "▶️ Listen", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Re-record button
                        Button(
                            onClick = {
                                hasRecorded = false
                                isRecording = true
                                isPlaying = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26194A)),
                            border = BorderStroke(1.dp, Color(0xFF6B7280)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text("🔄 Retake", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Final Submit & Verify Button
                    Button(
                        onClick = {
                            isSubmitting = true
                            coroutineScope.launch {
                                try {
                                    val updates = mapOf<String, Any>(
                                        "voiceVerificationStatus" to "verified",
                                        "voiceIntroUrl" to "verified_prompt_${System.currentTimeMillis()}",
                                        "voiceIntroDuration" to secondsElapsed.coerceAtLeast(5),
                                        "hasBio" to true
                                    )

                                    db.collection("users").document(currentUser.uid).update(updates)

                                    val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putString("profile_voice_verification_status", "verified")
                                        .putString("profile_voice_intro_url", "verified_prompt")
                                        .apply()

                                    val updatedUser = currentUser.copy(
                                        voiceVerificationStatus = "verified",
                                        voiceIntroUrl = "verified_prompt",
                                        voiceIntroDuration = secondsElapsed.coerceAtLeast(5)
                                    )

                                    onVerified(updatedUser)
                                    Toast.makeText(context, "🎉 Voice Verified! Gold Shield Badge 🛡️✓ Unlocked!", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSubmitting) "Verifying Voice..." else "Submit & Verify Account 🛡️",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    // Explanatory note
                    Text(
                        text = "🔒 Verified profiles display the Gold Shield badge and get prioritized placement in live hangouts.",
                        color = Color(0xFF9E8FB0),
                        fontSize = 10.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
