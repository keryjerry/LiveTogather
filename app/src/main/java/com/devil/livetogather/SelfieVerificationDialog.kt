package com.devil.livetogather

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Real-Time Liveness Selfie Verification Dialog.
 *
 * Implements:
 * 1. "Verify Yourself" with camera icon.
 * 2. Short liveness face movement guidance ("चारों तरफ फेस घुमाएं / Slowly turn face left, right & smile").
 * 3. 100% Private selfie capture: strictly used by system/admin for gender & identity verification,
 *    NEVER displayed publicly on user's profile.
 * 4. Submits request in "pending" state.
 * 5. Admin / system approval updates state to "verified" and displays Green Tick "Verified User" / "You are verified".
 */
@Composable
fun SelfieVerificationDialog(
    currentUser: UserProfile,
    db: FirebaseFirestore,
    onDismiss: () -> Unit,
    onVerifiedSubmitted: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val currentStatus = currentUser.verificationStatus.ifBlank { "unverified" }
    val isAlreadyVerified = currentStatus.equals("verified", ignoreCase = true)
    val isPending = currentStatus.equals("pending", ignoreCase = true)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            Toast.makeText(context, "Selfie captured! 📸 Review & Submit", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        capturedBitmap = bmp
                        Toast.makeText(context, "Selfie loaded! 📸 Review & Submit", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            color = Color(0xFF0F0B18)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = when {
                                isAlreadyVerified -> Color(0xFF10B981).copy(alpha = 0.2f)
                                isPending -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                else -> Color(0xFFFF4081).copy(alpha = 0.2f)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when {
                                        isAlreadyVerified -> Icons.Default.CheckCircle
                                        isPending -> Icons.Default.HourglassTop
                                        else -> Icons.Default.CameraAlt
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isAlreadyVerified -> Color(0xFF34D399)
                                        isPending -> Color(0xFFFBBF24)
                                        else -> Color(0xFFFF4081)
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Verify Yourself",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Liveness Identity & Gender Check",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when {
                    // 1. ALREADY VERIFIED
                    isAlreadyVerified -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF064E3B).copy(alpha = 0.4f),
                            border = BorderStroke(1.5.dp, Color(0xFF10B981))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(72.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF10B981)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = Color.White,
                                            modifier = Modifier.size(42.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "You Are Verified!",
                                    color = Color(0xFF34D399),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your profile is permanently verified with a Green Tick badge (Verified User). This verified badge is visible to everyone across all rooms and chat lists.",
                                    color = Color(0xFFD1FAE5),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                ) {
                                    Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    // 2. PENDING ADMIN APPROVAL
                    isPending -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF451A03).copy(alpha = 0.45f),
                            border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(72.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFF59E0B)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassTop,
                                            contentDescription = "Pending",
                                            tint = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Verification Pending",
                                    color = Color(0xFFFBBF24),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your selfie has been submitted safely and is under review. As soon as admin or system approves, your Green Tick 'Verified User' badge will be activated automatically.",
                                    color = Color(0xFFFEF3C7),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // If admin or testing, allow instant approval
                                if (currentUser.isAdmin) {
                                    Button(
                                        onClick = {
                                            isSubmitting = true
                                            coroutineScope.launch {
                                                try {
                                                    db.collection("users").document(currentUser.uid)
                                                        .update("verificationStatus", "verified")
                                                    db.collection("verification_requests").document(currentUser.uid)
                                                        .update("status", "approved")
                                                    val updated = currentUser.copy(verificationStatus = "verified")
                                                    onVerifiedSubmitted(updated)
                                                    Toast.makeText(context, "Verification approved! Green Tick is now active ✅", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                } finally {
                                                    isSubmitting = false
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        enabled = !isSubmitting
                                    ) {
                                        Text("⚡ Admin Quick-Approve", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                ) {
                                    Text("Close", color = Color(0xFFFBBF24), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // 3. UNVERIFIED / CAPTURE STEP
                    else -> {
                        // Oval Camera Frame / Guidance Box
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(120.dp))
                                .background(Color(0xFF1A1528))
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            Color(0xFFFF4081),
                                            Color(0xFFFF9100),
                                            Color(0xFF7C3AED),
                                            Color(0xFFFF4081)
                                        )
                                    ),
                                    shape = RoundedCornerShape(120.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (capturedBitmap != null) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "Captured Selfie",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Green check badge overlay
                                Surface(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .align(Alignment.BottomCenter)
                                        .offset(y = (-12).dp),
                                    shape = CircleShape,
                                    color = Color(0xFF10B981),
                                    border = BorderStroke(2.dp, Color.White)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(60.dp),
                                        shape = CircleShape,
                                        color = Color(0xFFFF4081).copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Camera",
                                                tint = Color(0xFFFF4081),
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "चारों तरफ फेस घुमाएं",
                                        color = Color(0xFFFFD700),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Turn face slowly left, right & smile",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Concise explanatory Privacy Guarantee card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E172E),
                            border = BorderStroke(1.dp, Color(0xFF3B2D54))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("🔒", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Private Verification Only",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "यह सेल्फी सिर्फ हमारे इंटरनल वेरिफिकेशन (लड़का/लड़की) के लिए है। यह एप्लीकेशन पर किसी को भी दिखाई नहीं देगी।",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (capturedBitmap == null) {
                            // Primary Button: Open Camera
                            Button(
                                onClick = {
                                    try {
                                        cameraLauncher.launch(null)
                                    } catch (e: Exception) {
                                        galleryFallbackLauncher.launch("image/*")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(Color(0xFFFF3366), Color(0xFFFF7700))
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Take Verification Selfie",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = { galleryFallbackLauncher.launch("image/*") }
                            ) {
                                Text("Or select from gallery", color = Color(0xFFFF80AB), fontSize = 12.sp)
                            }
                        } else {
                            // Review & Submit State
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            cameraLauncher.launch(null)
                                        } catch (e: Exception) {
                                            galleryFallbackLauncher.launch("image/*")
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFF4081)),
                                    enabled = !isSubmitting
                                ) {
                                    Text("Retake 🔄", color = Color(0xFFFF80AB), fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        isSubmitting = true
                                        coroutineScope.launch {
                                            try {
                                                val dataUri = withContext(Dispatchers.IO) {
                                                    val stream = ByteArrayOutputStream()
                                                    capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                                                    val bytes = stream.toByteArray()
                                                    val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                                    "data:image/jpeg;base64,$b64"
                                                }

                                                val now = System.currentTimeMillis()
                                                val batch = db.batch()
                                                val userRef = db.collection("users").document(currentUser.uid)
                                                batch.update(
                                                    userRef,
                                                    mapOf(
                                                        "verificationStatus" to "pending",
                                                        "verificationSelfieUrl" to dataUri,
                                                        "verificationTimestamp" to now
                                                    )
                                                )

                                                val reqRef = db.collection("verification_requests").document(currentUser.uid)
                                                batch.set(
                                                    reqRef,
                                                    mapOf(
                                                        "uid" to currentUser.uid,
                                                        "name" to currentUser.name,
                                                        "gender" to currentUser.gender,
                                                        "userPhoto" to currentUser.photo,
                                                        "selfieUrl" to dataUri,
                                                        "status" to "pending",
                                                        "timestamp" to now
                                                    )
                                                )

                                                batch.commit().addOnSuccessListener {
                                                    val updated = currentUser.copy(
                                                        verificationStatus = "pending",
                                                        verificationSelfieUrl = dataUri,
                                                        verificationTimestamp = now
                                                    )
                                                    onVerifiedSubmitted(updated)
                                                    Toast.makeText(
                                                        context,
                                                        "Selfie submitted! Verification pending approval ⏳",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }.addOnFailureListener { err ->
                                                    Toast.makeText(
                                                        context,
                                                        "Submission error: ${err.localizedMessage}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isSubmitting = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1.5f).height(48.dp),
                                    enabled = !isSubmitting
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("Submit for Verification 🚀", color = Color.White, fontWeight = FontWeight.Bold)
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
