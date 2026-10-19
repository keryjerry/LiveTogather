package com.devil.livetogather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL
import java.util.UUID

/**
 * WelcomeBonusManager
 *
 * Enforces strict single-device and IP verification for first-time signups:
 * 1. Checks device identifier (ANDROID_ID + secure fallback) and public/client IP against Firestore records.
 * 2. If neither has previously claimed the welcome bonus:
 *    - Records device and IP claim in 'claimed_devices' and 'claimed_ips' collections.
 *    - Grants 1 Crore (10,000,000) Diamonds to user's wallet.
 *    - Grants VIP 1 Noble Archangel Frame & VIP 1 Badge for 7 Days.
 *    - Flags welcome celebration popup for display on first entrance.
 *    - Triggers an in-app celebratory notification.
 * 3. If already claimed by this device or IP:
 *    - Bonus is withheld, preventing multi-account VIP / Diamond abuse.
 */
object WelcomeBonusManager {

    const val WELCOME_BONUS_DIAMONDS = 50_000L // 50,000 Diamonds
    const val VIP_TRIAL_DURATION_MS = 7L * 24 * 60 * 60 * 1000L // 7 Days
    const val VIP1_FRAME_ID = "frame_vip1_archangel"
    const val PREF_SHOW_WELCOME_DIALOG = "show_welcome_bonus_dialog"
    const val NOTIFICATION_CHANNEL_ID = "welcome_bonus_channel"

    /**
     * Resolves unique device identifier. Uses Settings.Secure.ANDROID_ID with robust persistent fallback.
     */
    fun getDeviceId(context: Context): String {
        return try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                androidId
            } else {
                getOrCreatePersistentDeviceId(context)
            }
        } catch (e: Exception) {
            getOrCreatePersistentDeviceId(context)
        }
    }

    private fun getOrCreatePersistentDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("livetogather_device_prefs", Context.MODE_PRIVATE)
        var id = prefs.getString("persistent_device_id", "") ?: ""
        if (id.isBlank()) {
            id = "dev_" + UUID.randomUUID().toString().replace("-", "")
            prefs.edit().putString("persistent_device_id", id).apply()
        }
        return id
    }

    /**
     * Fetches public IP asynchronously or falls back to local network IP.
     */
    suspend fun getPublicIpAddress(): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.ipify.org")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 2500
            conn.readTimeout = 2500
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                val ip = conn.inputStream.bufferedReader().use { it.readText() }.trim()
                if (ip.isNotBlank() && ip.length <= 45) {
                    return@withContext ip
                }
            }
        } catch (e: Exception) {
            // Network fallback below
        }

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress
                        if (!host.isNullOrBlank()) {
                            return@withContext host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignored
        }

        return@withContext "127.0.0.1"
    }

    private fun sanitizeForDocId(raw: String): String {
        return raw.replace("/", "_")
            .replace(".", "_")
            .replace(":", "_")
            .replace("#", "_")
            .replace("[", "_")
            .replace("]", "_")
            .trim()
    }

    /**
     * Checks eligibility and claims the welcome bonus if device & IP are first-time.
     */
    suspend fun checkAndApplyWelcomeBonus(
        context: Context,
        uid: String,
        initialProfile: UserProfile
    ): UserProfile = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val deviceId = if (initialProfile.deviceId.isNotBlank()) initialProfile.deviceId else getDeviceId(context)
        val ipAddress = getPublicIpAddress()
        val sanitizedDeviceId = sanitizeForDocId(deviceId)
        val sanitizedIp = sanitizeForDocId(ipAddress)

        // If user already claimed bonus in their profile, keep it as is
        if (initialProfile.hasClaimedWelcomeBonus) {
            return@withContext initialProfile
        }

        var isEligible = true
        try {
            // 1. Verify Device ID record
            val deviceDoc = db.collection("claimed_devices").document(sanitizedDeviceId).get().await()
            if (deviceDoc != null && deviceDoc.exists()) {
                val claimedBy = deviceDoc.getString("claimedByUid") ?: ""
                // If it was already claimed by someone else, deny bonus
                if (claimedBy.isNotBlank() && claimedBy != uid) {
                    isEligible = false
                }
            }

            // 2. Verify IP record if device was not previously recorded
            if (isEligible && sanitizedIp != "127_0_0_1") {
                val ipDoc = db.collection("claimed_ips").document(sanitizedIp).get().await()
                if (ipDoc != null && ipDoc.exists()) {
                    val claimedBy = ipDoc.getString("claimedByUid") ?: ""
                    if (claimedBy.isNotBlank() && claimedBy != uid) {
                        isEligible = false
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WelcomeBonusManager", "Verification lookup error: ${e.message}")
            // In case of offline or failure, allow deviceId check via local prefs fallback
            val localPrefs = context.getSharedPreferences("livetogather_claimed_devices", Context.MODE_PRIVATE)
            if (localPrefs.getBoolean("claimed_$sanitizedDeviceId", false)) {
                isEligible = false
            }
        }

        val now = System.currentTimeMillis()
        val updatedProfile = if (isEligible) {
            // Prepare Claimed Record
            val claimData = hashMapOf(
                "claimedByUid" to uid,
                "deviceId" to deviceId,
                "ipAddress" to ipAddress,
                "claimedAt" to now,
                "diamonds" to WELCOME_BONUS_DIAMONDS,
                "vipLevel" to 1,
                "trialDays" to 7
            )

            try {
                // Record claim in Firestore
                db.collection("claimed_devices").document(sanitizedDeviceId).set(claimData, SetOptions.merge())
                if (sanitizedIp != "127_0_0_1") {
                    db.collection("claimed_ips").document(sanitizedIp).set(claimData, SetOptions.merge())
                }
            } catch (e: Exception) {
                android.util.Log.e("WelcomeBonusManager", "Failed to write claim: ${e.message}")
            }

            // Record locally
            context.getSharedPreferences("livetogather_claimed_devices", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("claimed_$sanitizedDeviceId", true)
                .apply()

            // Prepare owned frames with VIP 1 frame added
            val updatedOwnedFrames = (initialProfile.ownedFrames + VIP1_FRAME_ID).distinct()

            // Set welcome popup flag
            val userPrefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
            userPrefs.edit().putBoolean(PREF_SHOW_WELCOME_DIALOG, true).apply()

            // Send local notification
            sendWelcomeNotification(context)

            initialProfile.copy(
                deviceId = deviceId,
                diamonds = initialProfile.diamonds + WELCOME_BONUS_DIAMONDS,
                activeFrame = VIP1_FRAME_ID,
                ownedFrames = updatedOwnedFrames,
                vipTrialStartDate = now,
                vipTrialExpiryDate = now + VIP_TRIAL_DURATION_MS,
                hasClaimedWelcomeBonus = true,
                signupIpAddress = ipAddress
            )
        } else {
            // Already claimed from this device / IP - standard free profile
            initialProfile.copy(
                deviceId = deviceId,
                signupIpAddress = ipAddress,
                hasClaimedWelcomeBonus = false
            )
        }

        return@withContext updatedProfile
    }

    /**
     * Checks if VIP 1 trial has expired and updates profile if needed.
     */
    fun checkAndExpireVipTrialIfNeeded(context: Context, profile: UserProfile): UserProfile {
        if (profile.vipTrialExpiryDate > 0L && System.currentTimeMillis() > profile.vipTrialExpiryDate) {
            // Trial has expired
            val wasVipFrameActive = profile.activeFrame == VIP1_FRAME_ID
            val updatedOwnedFrames = profile.ownedFrames.filter { it != VIP1_FRAME_ID }
            val updatedProfile = profile.copy(
                activeFrame = if (wasVipFrameActive) "" else profile.activeFrame,
                ownedFrames = updatedOwnedFrames
            )
            // Persist locally
            val prefs = context.getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
            saveProfileToPrefs(prefs, updatedProfile)

            // Persist to Firestore asynchronously
            try {
                if (profile.uid.isNotBlank()) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(profile.uid)
                        .update(
                            mapOf(
                                "activeFrame" to updatedProfile.activeFrame,
                                "ownedFrames" to updatedProfile.ownedFrames
                            )
                        )
                }
            } catch (e: Exception) {
                // Silently ignore
            }
            return updatedProfile
        }
        return profile
    }

    /**
     * Shows a system notification tray message for the welcome gift.
     */
    fun sendWelcomeNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Welcome & VIP Rewards",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for first-time signup gifts and VIP rewards"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("🎉 First-Time Welcome Gift Unlocked! 🎁")
                .setContentText("50,000 Diamonds + VIP 1 Archangel Frame & Badge (7 Days Free) are now in your wallet!")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Congratulations on signing up for the first time!\n\n💎 50,000 Diamonds added to your wallet\n👑 VIP 1 Noble Archangel Frame & Badge unlocked (7 Days)\n\nEnjoy LiveTogether to the fullest!")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1001, notification)
        } catch (e: Exception) {
            android.util.Log.e("WelcomeBonusManager", "Notification post error: ${e.message}")
        }
    }
}

/**
 * High-End Celebratory Welcome Dialog for First-Time Sign Up Bonus.
 */
@Composable
fun WelcomeBonusCelebrationDialog(
    onClaim: () -> Unit
) {
    val brandDark = Color(0xFF0B090A)
    val brandCard = Color(0xFF161214)
    val brandCoral = Color(0xFFFF5E62)
    val brandOrange = Color(0xFFFF9966)
    val brandGold = Color(0xFFFFD700)
    val brandCyan = Color(0xFF00E5FF)
    val brandBorder = Color(0xFF2A2226)

    Dialog(
        onDismissRequest = onClaim,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = brandCard,
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(brandGold, brandCoral, brandOrange))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Celebration Icon Badge
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            Brush.radialGradient(listOf(brandGold.copy(alpha = 0.35f), Color.Transparent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎁", fontSize = 42.sp)
                }

                Text(
                    text = "🎉 Welcome Gift Unlocked!",
                    color = brandGold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "बधाई हो! पहली बार साइन-अप करने पर आपका एक्सक्लूसिव वेलकम गिफ़्ट वॉलेट में जोड़ दिया गया है:",
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                // Gift Highlight Cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brandDark.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                        .border(1.dp, brandBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Diamonds Reward
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "💎", fontSize = 26.sp)
                        Column {
                            Text(
                                text = "50,000 Diamonds",
                                color = brandCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "50,000 डायमंड आपके वॉलेट में क्रेडिट हो गए हैं",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    HorizontalDivider(color = brandBorder)

                    // 2. VIP 1 Frame & Badge Reward
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "👑", fontSize = 26.sp)
                        Column {
                            Text(
                                text = "VIP 1 Archangel Frame & Badge",
                                color = brandGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "7 दिनों के लिए मुफ़्त अनलॉक (Valid for 7 Days)",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Important Note about Single-Device / Single-IP Claim
                Text(
                    text = "🛡️ सुरक्षा सूचना: यह उपहार प्रति डिवाइस एवं नेटवर्क आईपी पर केवल एक बार ही मान्य है।",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )

                // Claim & Continue Button
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = brandCoral),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Claim & Enter LiveTogether ➔",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
