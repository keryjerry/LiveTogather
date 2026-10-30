@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.devil.livetogather

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import android.view.WindowManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

/**
 * Data model for selectable Coin / Diamond top-up packages.
 */
data class CoinPackageItem(
    val id: String,
    val coins: Long,
    val priceInr: String,
    val numericPrice: String,
    val title: String,
    val badge: String = ""
)

val APP_COIN_PACKAGES = listOf(
    CoinPackageItem("pack_59", 20000L, "₹59", "59", "Minimum Room Unlock Pack 💎", "MINIMUM RECHARGE"),
    CoinPackageItem("pack_49", 15000L, "₹45", "45", "Starter Pack 💎", "POPULAR"),
    CoinPackageItem("pack_149", 50000L, "₹115", "115", "Special Launch Pack 💎", "BEST VALUE"),
    CoinPackageItem("pack_299", 120000L, "₹229", "229", "Super Value Pack 💎"),
    CoinPackageItem("pack_599", 300000L, "₹450", "450", "Royal VIP Pack 💎"),
    CoinPackageItem("pack_999", 600000L, "₹899", "899", "Imperial Diamond Vault 💎", "SOVEREIGN")
)

val SUPPORT_CATEGORIES = listOf(
    "Payment Verification (Submit UTR for Coins)",
    "Payment Failed / Money Deducted",
    "Account & User ID Issues",
    "Live Room & Audio Issues",
    "General Inquiries"
)

/**
 * Data model for Payment & Support verification requests stored in Firestore collection 'payment_requests'.
 */
data class PaymentRequestTicket(
    val requestId: String = "",
    val userId: String = "",
    val userNumericId: String = "",
    val userName: String = "",
    val utrNumber: String = "",
    val coinsRequested: Long = 0L,
    val amountPaid: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val category: String = "Payment Verification (Submit UTR for Coins)",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val purchaseType: String = "coins",
    val vipLevel: Int = 0
)

fun parsePaymentRequestTicket(doc: com.google.firebase.firestore.DocumentSnapshot): PaymentRequestTicket? {
    return try {
        val status = doc.getString("status") ?: "PENDING"
        val rawTs = doc.get("timestamp")
        val ts = when (rawTs) {
            is com.google.firebase.Timestamp -> rawTs.toDate().time
            is Number -> rawTs.toLong()
            is String -> rawTs.toLongOrNull() ?: 0L
            else -> 0L
        }
        val coins = (doc.get("coinsRequested") as? Number)?.toLong()
            ?: doc.getString("coinsRequested")?.toLongOrNull()
            ?: 0L

        PaymentRequestTicket(
            requestId = doc.getString("requestId") ?: doc.id,
            userId = doc.getString("userId") ?: "",
            userNumericId = doc.getString("userNumericId") ?: "",
            userName = doc.getString("userName") ?: "",
            utrNumber = doc.getString("utrNumber") ?: "",
            coinsRequested = coins,
            amountPaid = doc.getString("amountPaid") ?: "",
            status = status,
            category = doc.getString("category") ?: "Payment Verification (Submit UTR for Coins)",
            message = doc.getString("message") ?: "",
            timestamp = ts,
            purchaseType = doc.getString("purchaseType") ?: "coins",
            vipLevel = (doc.getLong("vipLevel") ?: 0L).toInt()
        )
    } catch (e: Exception) {
        android.util.Log.e("PaymentAndSupport", "Error parsing payment request ${doc.id}: ${e.message}", e)
        null
    }
}

fun parseReportTicket(doc: com.google.firebase.firestore.DocumentSnapshot): ReportTicket? {
    return try {
        val status = doc.getString("status") ?: "PENDING"
        val rawTs = doc.get("timestamp")
        val ts = when (rawTs) {
            is com.google.firebase.Timestamp -> rawTs.toDate().time
            is Number -> rawTs.toLong()
            is String -> rawTs.toLongOrNull() ?: 0L
            else -> 0L
        }

        ReportTicket(
            reportId = doc.getString("reportId") ?: doc.id,
            reportedUid = doc.getString("reportedUid") ?: "",
            reportedUserId = doc.getString("reportedUserId") ?: "",
            reportedName = doc.getString("reportedName") ?: "",
            reportedPhoto = doc.getString("reportedPhoto") ?: "",
            reporterUid = doc.getString("reporterUid") ?: "",
            reporterUserId = doc.getString("reporterUserId") ?: "",
            reporterName = doc.getString("reporterName") ?: "",
            reason = doc.getString("reason") ?: "",
            details = doc.getString("details") ?: "",
            timestamp = ts,
            status = status
        )
    } catch (e: Exception) {
        android.util.Log.e("PaymentAndSupport", "Error parsing report ${doc.id}: ${e.message}", e)
        null
    }
}

object PaymentAndSupportManager {

    /**
     * Triggers the native Android UPI Intent Chooser with deep-link:
     * pa: "warsi.1@ptaxis"
     * pn: "LiveTogether"
     * mc: "5411"
     * tr: unique transaction reference
     * tn: "LiveTogether Coins"
     * am: package price formatted with 2 decimal places
     * cu: "INR"
     */
    fun launchUpiPayment(
        context: Context,
        amount: String,
        packageName: String? = null,
        onOpened: () -> Unit = {},
        onError: (String) -> Unit = {}
    ): Boolean {
        try {
            val cleanAmount = amount.replace("₹", "").trim()
            val upiUri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "warsi.1@ptaxis")
                .appendQueryParameter("pn", "LiveTogether")
                .appendQueryParameter("tn", "LiveTogether Coins")
                .appendQueryParameter("am", cleanAmount)
                .appendQueryParameter("cu", "INR")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Named app buttons launch only that app; the generic button opens the chooser.
            if (!packageName.isNullOrBlank()) {
                val directIntent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                    setPackage(packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(directIntent)
                    onOpened()
                    return true
                } catch (e: android.content.ActivityNotFoundException) {
                    android.util.Log.w("UPI", "UPI app $packageName is not available", e)
                    onError("NO_UPI_APP")
                    return false
                }
            }

            val pm = context.packageManager
            val resolveInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(
                    intent,
                    android.content.pm.PackageManager.ResolveInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }

            if (resolveInfo.isEmpty()) {
                onError("NO_UPI_APP")
                return false
            }

            val chooser = Intent.createChooser(intent, "Pay \u20B9$cleanAmount via UPI")
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(chooser)
            onOpened()
            return true
        } catch (e: Exception) {
            android.util.Log.e("UPI", "Launch error: ${e.message}", e)
            onError(e.localizedMessage ?: "NO_UPI_APP")
        }
        return false
    }

    /**
     * Launches a specific UPI App (PhonePe, Google Pay, Paytm) directly or falls back to generic chooser.
     */
    fun launchSpecificUpiApp(
        context: Context,
        amount: String,
        packageName: String?,
        onOpened: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchUpiPayment(
            context = context,
            amount = amount,
            packageName = packageName,
            onOpened = onOpened,
            onError = onError
        )
    }

    /**
     * Submits a payment UTR verification request or support ticket to Firestore 'payment_requests'.
     */
    fun submitPaymentVerificationRequest(
        userId: String,
        userNumericId: String,
        userName: String,
        utrNumber: String,
        packCoins: Long,
        packAmount: String,
        category: String = "Payment Verification (Submit UTR for Coins)",
        message: String = "",
        purchaseType: String = "coins",
        vipLevel: Int = 0,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val autoId = db.collection("payment_requests").document().id
        val requestData = hashMapOf(
            "requestId" to autoId,
            "userId" to userId,
            "userNumericId" to userNumericId,
            "userName" to userName,
            "utrNumber" to utrNumber.trim(),
            "coinsRequested" to packCoins,
            "amountPaid" to packAmount,
            "status" to "pending",
            "category" to category,
            "message" to message,
            "purchaseType" to purchaseType,
            "vipLevel" to vipLevel,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("payment_requests").document(autoId)
            .set(requestData)
            .addOnSuccessListener {
                // Dispatch real-time notification to Admins
                val notifId = "pay_req_$autoId"
                val adminNotif = NotificationItem(
                    id = notifId,
                    targetUserId = "admin",
                    senderId = userId,
                    senderName = userName,
                    senderPhoto = "",
                    type = "payment_utr_submitted",
                    title = "💳 New Payment UTR: $packAmount",
                    message = if (purchaseType == "vip") {
                        "$userName (#$userNumericId) submitted UTR ${utrNumber.trim()} for VIP $vipLevel ($packAmount)"
                    } else {
                        "$userName (#$userNumericId) submitted UTR ${utrNumber.trim()} for %,d Diamonds ($packAmount)".format(packCoins)
                    },
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    roomId = ""
                )
                db.collection("notifications").document(notifId).set(adminNotif)

                db.collection("users").whereEqualTo("isAdmin", true).get().addOnSuccessListener { adminsSnap ->
                    for (adminDoc in adminsSnap.documents) {
                        val adminUid = adminDoc.id
                        if (adminUid.isNotBlank() && adminUid != userId) {
                            val userNotif = adminNotif.copy(id = "${notifId}_$adminUid", targetUserId = adminUid)
                            db.collection("notifications").document(userNotif.id).set(userNotif)
                            db.collection("users").document(adminUid).collection("notifications").document(notifId).set(userNotif)
                        }
                    }
                }

                onSuccess()
            }
            .addOnFailureListener { onFailure(it.localizedMessage ?: "Submission failed") }
    }

    /**
     * Admin action: Atomically approves payment request, increments target user's wallet coins & diamonds,
     * and writes a ledger record into users/{userId}/coin_transactions.
     */
    fun approvePaymentRequest(
        request: PaymentRequestTicket,
        adminUid: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        // 1. Update payment_requests status to approved
        val reqRef = db.collection("payment_requests").document(request.requestId)
        batch.update(
            reqRef, mapOf(
                "status" to "approved",
                "approvedAt" to FieldValue.serverTimestamp(),
                "approvedBy" to adminUid
            )
        )

        // 2. Increment target user wallet coins & diamonds atomically, or activate host monetization
        val userRef = db.collection("users").document(request.userId)
        val updateMap = mutableMapOf<String, Any>()
        if (request.coinsRequested > 0L) {
            updateMap["coins"] = FieldValue.increment(request.coinsRequested)
            updateMap["diamonds"] = FieldValue.increment(request.coinsRequested)
            updateMap["hasRecharged"] = true
            updateMap["rechargeCount"] = FieldValue.increment(1)
        }
        if (request.purchaseType == "host_monetization" ||
            (request.purchaseType == "coins" && request.category.contains("Monetization", ignoreCase = true))) {
            updateMap["isHostMonetized"] = true
            updateMap["hostMonetizationDate"] = System.currentTimeMillis()
        }
        if (request.purchaseType == "vip" && request.vipLevel in 1..4) {
            val vipFrame = "vip_suite_frame_${request.vipLevel}"
            val vipChatFrame = listOf(
                "",
                "chat_frame_silver_knight",
                "chat_frame_golden_paladin",
                "chat_frame_imperial_amethyst",
                "chat_frame_celestial_dragon"
            )[request.vipLevel]
            updateMap["plan"] = "VIP ${request.vipLevel}"
            updateMap["vipLevel"] = request.vipLevel
            updateMap["activeFrame"] = vipFrame
            updateMap["activeChatFrame"] = vipChatFrame
            updateMap["activeChatBar"] = "chat_sword_vip_${request.vipLevel}"
            updateMap["activeProfileArch"] = "arch_vip_${request.vipLevel}"
            updateMap["activeProfileCover"] = "cover_vip_${request.vipLevel}"
            updateMap["activeEntranceEffect"] = "entrance_vip_${request.vipLevel}"
            updateMap["ownedFrames"] = FieldValue.arrayUnion(vipFrame)
            updateMap["ownedChatFrames"] = FieldValue.arrayUnion(vipChatFrame)
        }
        if (updateMap.isNotEmpty()) {
            batch.update(userRef, updateMap)
        }

        // 3. Create an internal wallet ledger record in users/{userId}/coin_transactions
        val ledgerRef = userRef.collection("coin_transactions").document()
        batch.set(
            ledgerRef, mapOf(
                "id" to ledgerRef.id,
                "type" to "TOP_UP_APPROVED",
                "description" to "UPI Top-Up Approved (UTR: ${request.utrNumber})",
                "coinsAdded" to request.coinsRequested,
                "amountPaid" to request.amountPaid,
                "utrNumber" to request.utrNumber,
                "timestamp" to FieldValue.serverTimestamp()
            )
        )

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.localizedMessage ?: "Approval failed") }
    }

    /**
     * Admin action: Rejects payment request.
     */
    fun rejectPaymentRequest(
        requestId: String,
        adminUid: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("payment_requests").document(requestId)
            .update(
                mapOf(
                    "status" to "rejected",
                    "rejectedAt" to FieldValue.serverTimestamp(),
                    "rejectedBy" to adminUid
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.localizedMessage ?: "Rejection failed") }
    }
}

/**
 * Dialog shown to user upon returning from UPI payment app.
 */
@Composable
fun PostPaymentVerificationDialog(
    selectedPackage: CoinPackageItem?,
    onDismiss: () -> Unit,
    onSubmitUtrClick: (CoinPackageItem?) -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = BrandCard,
            border = BorderStroke(1.5.dp, BrandGold),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(BrandGold.copy(alpha = 0.2f))
                        .border(1.dp, BrandGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💸", fontSize = 30.sp)
                }

                Text(
                    text = "Payment Completed?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Submit your 12-digit UTR number in Help Desk to credit your coins instantly.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                if (selectedPackage != null) {
                    Surface(
                        color = BrandDark,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.5.dp, BrandBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedPackage.title,
                                    color = BrandGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "%,d Coins".format(selectedPackage.coins),
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = selectedPackage.priceInr,
                                color = BrandGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Official UPI ID Copy Strip
                Surface(
                    color = BrandDark,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Official UPI ID", color = Color.Gray, fontSize = 9.sp)
                            Text("warsi.1@ptaxis", color = BrandGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("UPI ID", "warsi.1@ptaxis")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "UPI ID copied: warsi.1@ptaxis 📋", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy UPI ID", tint = BrandGold, modifier = Modifier.size(15.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                        border = BorderStroke(1.dp, BrandBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Later", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onSubmitUtrClick(selectedPackage)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.6f)
                    ) {
                        Text("Submit UTR ➔", color = BrandDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Complete, self-contained UPI Payment Checkout Modal.
 * Provides One-Tap UPI App triggers (PhonePe, GPay, Paytm, Any UPI),
 * Copyable official UPI ID (warsi.1@ptaxis), dynamic QR code, and
 * direct 12-digit UTR submission into Firestore 'payment_requests'.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiPaymentCheckoutModal(
    currentUser: UserProfile,
    selectedPackage: CoinPackageItem,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    var utrInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val cleanAmount = selectedPackage.numericPrice.ifBlank { selectedPackage.priceInr.replace("₹", "").trim() }
    val qrData = "upi://pay?pa=warsi.1@ptaxis&pn=LiveTogether&am=$cleanAmount&cu=INR&tn=LiveTogether%20Coins"
    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + Uri.encode(qrData)

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
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
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(22.dp),
            color = BrandDark,
            border = BorderStroke(1.5.dp, BrandGold)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // PINNED TOP BAR (Always visible, cannot be scrolled away, Back & Close always ready)
                val isMonetizationPkg = selectedPackage.id == "pkg_host_monetization_599" || selectedPackage.title.contains("Monetization", ignoreCase = true)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E172E))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onDismiss() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BrandCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = BrandGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isMonetizationPkg) "🚀 Host Monetization" else "💎 Diamond Recharge",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val displayUserId = if (currentUser.userId.isNotBlank()) currentUser.userId else (10000000 + kotlin.math.abs(currentUser.uid.hashCode()) % 90000000).toString()
                            Text(
                                text = "User ID: #$displayUserId • Tap ← to return",
                                color = BrandGold,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .background(BrandCard, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = BrandBorder.copy(alpha = 0.5f), thickness = 1.dp)

                // FULLY SCROLLABLE BODY (Smooth scrolling, generous bottom padding so Submit is easily reachable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {

                // Selected Package Card
                Surface(
                    color = BrandCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isMonetizationPkg = selectedPackage.id == "pkg_host_monetization_599" || selectedPackage.title.contains("Monetization", ignoreCase = true)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isMonetizationPkg) "🚀" else "💎", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isMonetizationPkg) "Host Monetization Lifetime Access" else "%,d Diamonds".format(selectedPackage.coins),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedPackage.title,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = selectedPackage.priceInr,
                            color = BrandGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // 1. Scanner QR Code (Center & Prominent)
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    AsyncImage(
                        model = qrUrl,
                        contentDescription = "Scan to Pay ₹$cleanAmount",
                        modifier = Modifier
                            .size(175.dp)
                            .padding(8.dp)
                    )
                }

                Text(
                    text = "Scan QR code with PhonePe, Google Pay, Paytm or any UPI App to Pay ₹$cleanAmount",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.92f)
                )

                // 2. Official UPI ID Strip
                Surface(
                    color = BrandCard,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Official UPI ID", color = Color.Gray, fontSize = 9.5.sp)
                            Text("warsi.1@ptaxis", color = BrandGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("UPI ID", "warsi.1@ptaxis"))
                                Toast.makeText(context, "UPI ID copied: warsi.1@ptaxis", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("📋 Copy UPI", color = BrandDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 3. 1-Tap Direct UPI App Triggers
                Text("Select UPI App to Pay ₹$cleanAmount:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // PhonePe Button
                    Button(
                        onClick = {
                            PaymentAndSupportManager.launchUpiPayment(
                                context = context,
                                amount = cleanAmount,
                                packageName = "com.phonepe.app",
                                onOpened = {
                                    Toast.makeText(context, "Opening PhonePe... Complete payment and enter UTR below.", Toast.LENGTH_LONG).show()
                                },
                                onError = {
                                    Toast.makeText(context, "PhonePe is not installed on this phone. Please scan QR or try another app.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5F259F)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("PhonePe", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Google Pay Button
                    Button(
                        onClick = {
                            PaymentAndSupportManager.launchUpiPayment(
                                context = context,
                                amount = cleanAmount,
                                packageName = "com.google.android.apps.nbu.paisa.user",
                                onOpened = {
                                    Toast.makeText(context, "Opening Google Pay... Complete payment and enter UTR below.", Toast.LENGTH_LONG).show()
                                },
                                onError = {
                                    Toast.makeText(context, "Google Pay is not installed on this phone. Please scan QR or try another app.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("GPay", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Paytm Button
                    Button(
                        onClick = {
                            PaymentAndSupportManager.launchUpiPayment(
                                context = context,
                                amount = cleanAmount,
                                packageName = "net.one97.paytm",
                                onOpened = {
                                    Toast.makeText(context, "Opening Paytm... Complete payment and enter UTR below.", Toast.LENGTH_LONG).show()
                                },
                                onError = {
                                    Toast.makeText(context, "Paytm is not installed on this phone. Please scan QR or try another app.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B9F5)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("Paytm", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Other UPI Apps Button
                    Button(
                        onClick = {
                            PaymentAndSupportManager.launchUpiPayment(
                                context = context,
                                amount = cleanAmount,
                                onOpened = {
                                    Toast.makeText(context, "Opening UPI app... Complete payment and enter UTR below.", Toast.LENGTH_LONG).show()
                                },
                                onError = {
                                    Toast.makeText(context, "No UPI app found on this phone. Please scan the QR code above or copy UPI ID!", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF231B28)),
                        border = BorderStroke(1.dp, BrandGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("Other UPI", color = BrandGold, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = BrandBorder.copy(alpha = 0.4f), thickness = 0.8.dp)

                // 4. Enter 12-Digit UTR
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enter 12-Digit UTR after payment:",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
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

                    OutlinedTextField(
                        value = utrInput,
                        onValueChange = { input ->
                            val digits = input.filter { c -> c.isDigit() }.take(12)
                            utrInput = digits
                            if (digits.length == 12) {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        },
                        label = { Text("12-Digit UTR / UPI Ref (from receipt)", fontSize = 11.sp) },
                        placeholder = { Text("e.g. 4238xxxxxxxx", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        trailingIcon = {
                            if (utrInput.isNotEmpty() || isKeyboardOpen) {
                                IconButton(onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Hide Keyboard",
                                        tint = BrandGold
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGold,
                            unfocusedBorderColor = BrandBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            val trimmed = utrInput.trim()
                            if (trimmed.length < 10) {
                                Toast.makeText(context, "Please enter a valid 12-digit UTR number from your payment receipt!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSubmitting = true
                            val isMonetizationPkg = selectedPackage.id == "pkg_host_monetization_599" || selectedPackage.title.contains("Monetization", ignoreCase = true)
                            val vipLevel = selectedPackage.id
                                .removePrefix("vip_package_")
                                .toIntOrNull()
                                ?.takeIf { it in 1..4 } ?: 0
                            val purchaseType = when {
                                isMonetizationPkg -> "host_monetization"
                                vipLevel > 0 -> "vip"
                                else -> "coins"
                            }
                            val finalNumericId = if (currentUser.userId.isNotBlank()) currentUser.userId else (10000000 + kotlin.math.abs(currentUser.uid.hashCode()) % 90000000).toString()
                            PaymentAndSupportManager.submitPaymentVerificationRequest(
                                userId = currentUser.uid,
                                userNumericId = finalNumericId,
                                userName = currentUser.name,
                                utrNumber = trimmed,
                                packCoins = selectedPackage.coins,
                                packAmount = selectedPackage.priceInr,
                                category = when {
                                    isMonetizationPkg -> "Host Monetization Activation"
                                    vipLevel > 0 -> "VIP $vipLevel Suite Purchase"
                                    else -> "Payment Verification (Submit UTR for Coins)"
                                },
                                message = when {
                                    isMonetizationPkg -> "Host Monetization ₹599 paid"
                                    vipLevel > 0 -> "VIP $vipLevel suite purchase"
                                    else -> ""
                                },
                                purchaseType = purchaseType,
                                vipLevel = vipLevel,
                                onSuccess = {
                                    isSubmitting = false
                                    if (isMonetizationPkg) {
                                        Toast.makeText(context, "✅ UTR #$trimmed submitted. Host Monetization is pending admin approval.", Toast.LENGTH_LONG).show()
                                    } else if (vipLevel > 0) {
                                        Toast.makeText(context, "✅ UTR #$trimmed submitted. VIP $vipLevel is pending admin approval.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "✅ UTR #$trimmed submitted. Diamonds are pending admin approval.", Toast.LENGTH_LONG).show()
                                    }
                                    onSuccess()
                                    onDismiss()
                                },
                                onFailure = { err ->
                                    isSubmitting = false
                                    Toast.makeText(context, "Submission error: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = BrandDark, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            val isMonetizationPkg = selectedPackage.id == "pkg_host_monetization_599" || selectedPackage.title.contains("Monetization", ignoreCase = true)
                            Text(
                                text = if (isMonetizationPkg) "Verify & Activate Host Monetization (₹599) 🚀" else "Verify & Credit %,d Diamonds 💎".format(selectedPackage.coins),
                                color = BrandDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Text(
                    text = "🔒 100% Secure • Official Account: warsi.1@ptaxis • Live Firestore Sync",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Generous bottom spacer so UTR and Submit can be scrolled way up above keyboard
                Spacer(modifier = Modifier.height(350.dp))
            }
        }
    }
}
}

/**
 * Help & Support Desk Modal (User Side).
 * Contains Category selection, 12-Digit UTR input, and a Live Tickets Status viewer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpDeskModal(
    currentUser: UserProfile,
    prefilledPackage: CoinPackageItem? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(SUPPORT_CATEGORIES[0]) }
    var selectedPackage by remember { mutableStateOf(prefilledPackage ?: APP_COIN_PACKAGES[0]) }
    var utrInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Submit Ticket, 1: My Tickets
    var isSubmitting by remember { mutableStateOf(false) }

    // User's Tickets state
    val myTickets = remember { mutableStateListOf<PaymentRequestTicket>() }

    LaunchedEffect(currentUser.uid) {
        if (currentUser.uid.isNotBlank()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("payment_requests")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        myTickets.clear()
                        val list = snapshot.documents.mapNotNull { doc ->
                            parsePaymentRequestTicket(doc)
                        }.sortedByDescending { it.timestamp }
                        myTickets.addAll(list)
                    }
                }
        }
    }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
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
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(24.dp),
            color = BrandDark,
            border = BorderStroke(1.5.dp, BrandBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // PINNED TOP BAR (Always visible, cannot be scrolled away, Back & Close always ready)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandCard)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onDismiss() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BrandDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = BrandGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Help & Support Desk",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val displayId = if (currentUser.userId.isNotBlank() && currentUser.userId.length == 8 && currentUser.userId.all { it.isDigit() }) {
                                currentUser.userId
                            } else {
                                (10000000 + kotlin.math.abs(currentUser.uid.hashCode()) % 90000000).toString()
                            }
                            Text(
                                text = "Account ID: #$displayId • Tap ← to return",
                                color = BrandGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .background(BrandDark, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                // Tabs (Submit Ticket vs My Tickets)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandCardSubtle)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    listOf("Submit Ticket", "My Tickets (${myTickets.size})").forEachIndexed { idx, title ->
                        val isSelected = selectedTab == idx
                        Surface(
                            color = if (isSelected) BrandCoral else Color.Transparent,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = idx }
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Content
                if (selectedTab == 0) {
                    // TAB 0: SUBMIT TICKET
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Category Selector
                        Text("Select Support Category", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SUPPORT_CATEGORIES.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    color = if (isSelected) BrandCoral.copy(alpha = 0.15f) else BrandCard,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isSelected) BrandCoral else BrandBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCategory = cat }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (cat.startsWith("Payment")) "💳" else "📌",
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cat,
                                            color = if (isSelected) Color.White else Color.LightGray,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // When Category is Payment Verification (Submit UTR for Coins)
                        val isPaymentCat = selectedCategory == SUPPORT_CATEGORIES[0] || selectedCategory == SUPPORT_CATEGORIES[1]

                        if (isPaymentCat) {
                            Text("Select Coin Package Recharged", color = BrandGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                APP_COIN_PACKAGES.take(3).forEach { pack ->
                                    val isSelected = selectedPackage.id == pack.id
                                    Surface(
                                        color = if (isSelected) BrandGold.copy(alpha = 0.2f) else BrandCard,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, if (isSelected) BrandGold else BrandBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedPackage = pack }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(pack.priceInr, color = if (isSelected) BrandGold else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text("%,d 💎".format(pack.coins), color = Color.Gray, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            // Official UPI ID Copyable Card
                            Surface(
                                color = BrandCard,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Official UPI ID", color = Color.Gray, fontSize = 10.sp)
                                        Text("warsi.1@ptaxis", color = BrandGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("UPI ID", "warsi.1@ptaxis")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "UPI ID copied: warsi.1@ptaxis 📋", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Copy UPI", color = BrandDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            var showQrCodeInHelpDesk by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showQrCodeInHelpDesk) "📷 Scan to Pay via GPay / PhonePe / Paytm" else "💡 Scan QR with any phone to pay",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                                TextButton(
                                    onClick = { showQrCodeInHelpDesk = !showQrCodeInHelpDesk },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (showQrCodeInHelpDesk) "Hide QR" else "Show QR Code 📲",
                                        color = BrandGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (showQrCodeInHelpDesk) {
                                val cleanPrice = selectedPackage.numericPrice.ifBlank { selectedPackage.priceInr.replace("₹", "").trim() }
                                val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=260x260&data=" +
                                    Uri.encode("upi://pay?pa=warsi.1@ptaxis&pn=LiveTogether&am=$cleanPrice&cu=INR&tn=LiveTogether%20Coins")
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = qrUrl,
                                        contentDescription = "Scan to Pay ${selectedPackage.priceInr}",
                                        modifier = Modifier
                                            .size(140.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .padding(6.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Scan with Google Pay, PhonePe, Paytm or BHIM on any phone",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // 12-Digit UTR Input Field
                            OutlinedTextField(
                                value = utrInput,
                                onValueChange = { input ->
                                    // Strictly digits only, max length 12
                                    val digitsOnly = input.filter { it.isDigit() }.take(12)
                                    utrInput = digitsOnly
                                },
                                label = { Text("12-Digit UPI UTR / Ref Number *") },
                                placeholder = { Text("e.g. 429182749102") },
                                supportingText = {
                                    Text(
                                        text = "${utrInput.length}/12 Digits ${if (utrInput.length == 12) "✅ Valid UTR" else "(Must be 12 digits)"}",
                                        color = if (utrInput.length == 12) BrandGreen else Color.Gray,
                                        fontSize = 11.sp
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }),
                                trailingIcon = {
                                    if (utrInput.isNotEmpty() || isKeyboardOpen) {
                                        IconButton(onClick = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Hide Keyboard",
                                                tint = BrandGold
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGold,
                                    unfocusedBorderColor = BrandBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Issue description for other categories
                            OutlinedTextField(
                                value = messageInput,
                                onValueChange = { messageInput = it },
                                label = { Text("Describe Your Issue / Query *") },
                                placeholder = { Text("Provide details about what you need assistance with...") },
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandCoral,
                                    unfocusedBorderColor = BrandBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (isKeyboardOpen) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                ) {
                                    Text("Hide Keyboard ⬇️", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (isPaymentCat) {
                                    if (utrInput.length != 12) {
                                        Toast.makeText(context, "Please enter a valid 12-digit UTR number", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                } else {
                                    if (messageInput.trim().length < 5) {
                                        Toast.makeText(context, "Please describe your query (min 5 characters)", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                }

                                isSubmitting = true
                                val displayId = if (currentUser.userId.isNotBlank() && currentUser.userId.length == 8 && currentUser.userId.all { it.isDigit() }) {
                                    currentUser.userId
                                } else {
                                    (10000000 + kotlin.math.abs(currentUser.uid.hashCode()) % 90000000).toString()
                                }

                                PaymentAndSupportManager.submitPaymentVerificationRequest(
                                    userId = currentUser.uid,
                                    userNumericId = displayId,
                                    userName = currentUser.name.ifBlank { "User #$displayId" },
                                    utrNumber = utrInput.trim(),
                                    packCoins = if (isPaymentCat) selectedPackage.coins else 0L,
                                    packAmount = if (isPaymentCat) selectedPackage.priceInr else "",
                                    category = selectedCategory,
                                    message = messageInput.trim(),
                                    onSuccess = {
                                        isSubmitting = false
                                        Toast.makeText(context, "✅ Request submitted successfully! Admins will verify shortly.", Toast.LENGTH_LONG).show()
                                        utrInput = ""
                                        messageInput = ""
                                        selectedTab = 1 // Switch to My Tickets
                                    },
                                    onFailure = { err ->
                                        isSubmitting = false
                                        Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPaymentCat) BrandGold else BrandCoral),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = BrandDark, modifier = Modifier.size(22.dp))
                            } else {
                                Text(
                                    text = if (isPaymentCat) "Submit UTR for Verification ➔" else "Submit Support Ticket ➔",
                                    color = if (isPaymentCat) BrandDark else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Generous bottom spacer so UTR and Submit can be scrolled way up above keyboard
                        Spacer(modifier = Modifier.height(350.dp))
                    }
                } else {
                    // TAB 1: MY TICKETS
                    if (myTickets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎫", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No Tickets Submitted Yet", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Submit a UTR verification request or support ticket anytime.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(myTickets) { ticket ->
                                TicketCard(ticket = ticket)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: PaymentRequestTicket) {
    val dateStr = remember(ticket.timestamp) {
        if (ticket.timestamp > 0) {
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(ticket.timestamp))
        } else "Just now"
    }

    val (badgeText, badgeBg, badgeTextColor) = when (ticket.status.uppercase()) {
        "APPROVED" -> Triple("✅ APPROVED", BrandGreen.copy(alpha = 0.2f), BrandGreen)
        "REJECTED" -> Triple("❌ REJECTED", Color.Red.copy(alpha = 0.2f), Color.Red)
        else -> Triple("⏳ PENDING", BrandGold.copy(alpha = 0.2f), BrandGold)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BrandCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BrandBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.category,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            if (ticket.utrNumber.isNotBlank()) {
                Surface(
                    color = BrandDark,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, BrandBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("UTR: ${ticket.utrNumber}", color = BrandGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        if (ticket.amountPaid.isNotBlank()) {
                            Text("${ticket.amountPaid} • %,d 💎".format(ticket.coinsRequested), color = BrandGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (ticket.message.isNotBlank()) {
                Text(
                    text = ticket.message,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = dateStr,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * In-App Admin Approval Dashboard (Admin Side Only).
 * Listens in real-time to 'payment_requests' where status == 'PENDING'.
 * Displays User ID, Name, Amount, Coins, 12-Digit UTR with copy button, and Approve/Reject buttons.
 */
@Composable
fun AdminApprovalDashboardModal(
    adminUser: UserProfile,
    initialTab: Int = 0,
    onUpdateCurrentUser: (UserProfile) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pendingRequests = remember { mutableStateListOf<PaymentRequestTicket>() }
    val pendingReports = remember { mutableStateListOf<ReportTicket>() }
    var selectedAdminTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0: Payments, 1: User Reports
    var processingReqId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        // 1. Pending Payments Listener
        db.collection("payment_requests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("AdminApproval", "Payment requests error: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        parsePaymentRequestTicket(doc)
                    }.filter { it.status.equals("PENDING", ignoreCase = true) }
                     .sortedByDescending { it.timestamp }
                    pendingRequests.clear()
                    pendingRequests.addAll(list)
                    android.util.Log.d("AdminApproval", "Loaded ${pendingRequests.size} pending payments")
                }
            }

        // 2. Pending User Reports Listener
        db.collection("reports")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("AdminApproval", "Reports error: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        parseReportTicket(doc)
                    }.filter { it.status.equals("PENDING", ignoreCase = true) }
                     .sortedByDescending { it.timestamp }
                    pendingReports.clear()
                    pendingReports.addAll(list)
                    android.util.Log.d("AdminApproval", "Loaded ${pendingReports.size} pending reports")
                }
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .imePadding(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF140F20),
            border = BorderStroke(1.dp, Color(0xFF3B2F50))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Sleek dark amethyst, clean styling)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E172F))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF31214B))
                                .border(1.dp, Color(0xFF6D28D9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Admin Approvals Dashboard",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedAdminTab == 0) "${pendingRequests.size} Pending Payment Approvals"
                                       else if (selectedAdminTab == 1) "${pendingReports.size} Pending User Reports"
                                       else "Founder, CEO & Staff Power Hub",
                                color = Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF261D3A), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedAdminTab,
                    containerColor = Color(0xFF161024),
                    contentColor = Color(0xFFA78BFA)
                ) {
                    Tab(
                        selected = selectedAdminTab == 0,
                        onClick = { selectedAdminTab = 0 },
                        text = {
                            Text(
                                text = "Payments (${pendingRequests.size})",
                                fontWeight = if (selectedAdminTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAdminTab == 0) BrandGold else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedAdminTab == 1,
                        onClick = { selectedAdminTab = 1 },
                        text = {
                            Text(
                                text = "Reports (${pendingReports.size})",
                                fontWeight = if (selectedAdminTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAdminTab == 1) Color.Red else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedAdminTab == 2,
                        onClick = { selectedAdminTab = 2 },
                        text = {
                            Text(
                                text = "⚡ Founder & Staff",
                                fontWeight = if (selectedAdminTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAdminTab == 2) Color(0xFFFFD700) else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                if (selectedAdminTab == 0) {
                    // TAB 0: PAYMENTS (UNTOUCHED LOGIC)
                    if (pendingRequests.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✨", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("All Caught Up!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("No pending UPI payment requests at this moment.", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingRequests, key = { it.requestId }) { req ->
                                AdminApprovalCard(
                                    req = req,
                                    isProcessing = processingReqId == req.requestId,
                                    onApprove = {
                                        processingReqId = req.requestId
                                        PaymentAndSupportManager.approvePaymentRequest(
                                            request = req,
                                            adminUid = adminUser.uid,
                                            onSuccess = {
                                                processingReqId = null
                                                Toast.makeText(context, "✅ Approved! %,d Coins credited to User #${req.userNumericId}".format(req.coinsRequested), Toast.LENGTH_SHORT).show()
                                            },
                                            onFailure = { err ->
                                                processingReqId = null
                                                Toast.makeText(context, "Approval error: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    onReject = {
                                        processingReqId = req.requestId
                                        PaymentAndSupportManager.rejectPaymentRequest(
                                            requestId = req.requestId,
                                            adminUid = adminUser.uid,
                                            onSuccess = {
                                                processingReqId = null
                                                Toast.makeText(context, "❌ Request #${req.requestId.take(6)} Rejected", Toast.LENGTH_SHORT).show()
                                            },
                                            onFailure = { err ->
                                                processingReqId = null
                                                Toast.makeText(context, "Rejection error: $err", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                } else if (selectedAdminTab == 1) {
                    // TAB 1: USER REPORTS & SUSPENSION
                    if (pendingReports.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🛡️", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Zero Pending Reports", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("LiveTogether community is peaceful and safe!", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingReports, key = { it.reportId }) { report ->
                                AdminReportCard(
                                    ticket = report,
                                    onSuspend = { hours ->
                                        val durationMs = when (hours) {
                                            24 -> 24L * 60 * 60 * 1000L
                                            48 -> 48L * 60 * 60 * 1000L
                                            72 -> 72L * 60 * 60 * 1000L
                                            else -> 3650L * 24 * 60 * 60 * 1000L // 10 years permanent
                                        }
                                        val expiry = System.currentTimeMillis() + durationMs
                                        val updates = mapOf(
                                            "isSuspended" to true,
                                            "suspensionExpiry" to expiry,
                                            "suspensionReason" to report.reason
                                        )
                                        val db = FirebaseFirestore.getInstance()
                                        db.collection("users").document(report.reportedUid).update(updates)
                                        db.collection("reports").document(report.reportId).update("status", "RESOLVED_SUSPENDED")
                                        Toast.makeText(context, "User #${if (report.reportedUserId.isNotBlank()) report.reportedUserId else report.reportedUid.take(8)} suspended (${if (hours > 0) "${hours}h" else "Permanent"}).", Toast.LENGTH_SHORT).show()
                                    },
                                    onDismiss = {
                                        val db = FirebaseFirestore.getInstance()
                                        db.collection("reports").document(report.reportId).update("status", "DISMISSED")
                                        Toast.makeText(context, "Report dismissed.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                } else if (selectedAdminTab == 2) {
                    // TAB 2: FOUNDER, CEO & OFFICIAL STAFF POWER HUB
                    AdminStaffAndPowerHubSection(adminUser = adminUser, onUpdateCurrentUser = onUpdateCurrentUser)
                }
            }
        }
    }
}

@Composable
private fun AdminReportCard(
    ticket: ReportTicket,
    onSuspend: (hours: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val dateStr = remember(ticket.timestamp) {
        if (ticket.timestamp > 0) {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ticket.timestamp))
        } else "Just now"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BrandCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: Reported User & Reporter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reported: ${ticket.reportedName}",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "User ID: ${if (ticket.reportedUserId.isNotBlank()) ticket.reportedUserId else ticket.reportedUid.take(8)}",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
                Text(dateStr, color = Color.Gray, fontSize = 11.sp)
            }

            HorizontalDivider(color = BrandBorder, thickness = 0.8.dp)

            // Reason & Reporter
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Reason: ${ticket.reason}",
                    color = BrandGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (ticket.details.isNotBlank()) {
                    Text(
                        text = "Details: \"${ticket.details}\"",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Reported by: ${ticket.reporterName} (ID: ${if (ticket.reporterUserId.isNotBlank()) ticket.reporterUserId else ticket.reporterUid.take(8)})",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons: 24h, 48h, 72h, Permanent, Dismiss
            Text("Admin Action:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onSuspend(24) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D)),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text("24h Ban", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onSuspend(48) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF9800)),
                    border = BorderStroke(1.dp, Color(0xFFFF9800)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text("48h Ban", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onSuspend(72) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF4511E)),
                    border = BorderStroke(1.dp, Color(0xFFF4511E)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(34.dp)
                ) {
                    Text("72h Ban", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onSuspend(-1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.2f).height(34.dp)
                ) {
                    Text("Perm Ban 🛑", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(32.dp)
            ) {
                Text("Dismiss Report (No Action)", color = Color.LightGray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AdminApprovalCard(
    req: PaymentRequestTicket,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    val dateStr = remember(req.timestamp) {
        if (req.timestamp > 0) {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(req.timestamp))
        } else "Just now"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BrandCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BrandBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // User Header & ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = req.userName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = BrandGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, BrandGold)
                        ) {
                            Text(
                                text = "ID: #${req.userNumericId}",
                                color = BrandGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "UID: ${req.userId.take(12)}...",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Text(dateStr, color = Color.Gray, fontSize = 10.sp)
            }

            // Amount Paid & Coins Requested
            Surface(
                color = BrandDark,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BrandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Amount Paid", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = req.amountPaid.ifBlank { "Direct UPI" },
                            color = BrandGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Coins Requested", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = "%,d 💎".format(req.coinsRequested),
                            color = BrandGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // 12-Digit UTR Number Box with Quick Copy
            Surface(
                color = BrandCardSubtle,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("UTR Number", req.utrNumber)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "UTR ${req.utrNumber} copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("12-Digit UTR Reference Number", color = Color.LightGray, fontSize = 10.sp)
                        Text(
                            text = req.utrNumber.ifBlank { "Not provided" },
                            color = BrandGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Copy", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy UTR",
                            tint = BrandGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (req.message.isNotBlank()) {
                Text(
                    text = "User note: ${req.message}",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            // Action Buttons (Approve vs Reject)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onReject,
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onApprove,
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.4f)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Approve & Credit ➔", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Standalone Coin Store & Top-Up Dialog accessible from Profile/Settings.
 */
@Composable
fun CoinStoreTopUpDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onOpenHelpDeskWithPackage: (CoinPackageItem) -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    var checkoutPackage by remember { mutableStateOf<CoinPackageItem?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Diamond Wallet, 1: Earning Wallet

    // Live User Profile Financials
    var liveDiamonds by remember { mutableLongStateOf(currentUser.diamonds) }
    var liveEarningsInr by remember { mutableDoubleStateOf(currentUser.earningsInr) }
    var liveTotalEarningsInr by remember { mutableDoubleStateOf(currentUser.totalEarningsInr) }
    var liveWithdrawnInr by remember { mutableDoubleStateOf(currentUser.withdrawnInr) }
    var liveUpiId by remember { mutableStateOf(currentUser.upiId) }
    var liveIsHostMonetized by remember { mutableStateOf(currentUser.isHostMonetized) }

    LaunchedEffect(currentUser.uid) {
        if (currentUser.uid.isNotBlank()) {
            db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snap, _ ->
                    if (snap != null && snap.exists()) {
                        liveDiamonds = snap.getLong("diamonds") ?: liveDiamonds
                        liveEarningsInr = snap.getDouble("earningsInr") ?: (snap.getLong("earningsInr")?.toDouble() ?: liveEarningsInr)
                        liveTotalEarningsInr = snap.getDouble("totalEarningsInr") ?: (snap.getLong("totalEarningsInr")?.toDouble() ?: liveTotalEarningsInr)
                        liveWithdrawnInr = snap.getDouble("withdrawnInr") ?: (snap.getLong("withdrawnInr")?.toDouble() ?: liveWithdrawnInr)
                        val savedUpi = snap.getString("upiId")
                        if (!savedUpi.isNullOrBlank()) {
                            liveUpiId = savedUpi
                        }
                        val isMon = snap.getBoolean("isHostMonetized") ?: snap.getBoolean("profile_is_host_monetized") ?: (currentUser.isOfficialAdmin() || currentUser.isHostMonetized)
                        liveIsHostMonetized = isMon
                    }
                }
        }
    }

    // Real-Time Earning Transactions Listener (ZERO dummy data)
    val earningTransactions = remember { mutableStateListOf<EarningTransaction>() }
    DisposableEffect(currentUser.uid) {
        if (currentUser.uid.isBlank()) return@DisposableEffect onDispose {}
        val listener = db.collection("users").document(currentUser.uid)
            .collection("earning_transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snaps, _ ->
                if (snaps != null) {
                    val list = snaps.documents.mapNotNull { it.toObject(EarningTransaction::class.java) }
                    earningTransactions.clear()
                    earningTransactions.addAll(list)
                }
            }
        onDispose { listener.remove() }
    }

    // Host Commission Level & Tier
    val hostWealthLvl = currentUser.getEffectiveWealthLevel()
    val commissionPercent = LevelBadgeSystem.getHostCommissionPercent(hostWealthLvl)

    // UPI Withdrawal Form States
    var withdrawAmountText by remember { mutableStateOf("") }
    var withdrawUpiId by remember(liveUpiId) { mutableStateOf(liveUpiId) }
    var isSubmittingWithdrawal by remember { mutableStateOf(false) }

    if (checkoutPackage != null) {
        UpiPaymentCheckoutModal(
            currentUser = currentUser,
            selectedPackage = checkoutPackage!!,
            onDismiss = { checkoutPackage = null },
            onSuccess = {
                checkoutPackage = null
                onDismiss()
            }
        )
    } else {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.95f),
                shape = RoundedCornerShape(24.dp),
                color = BrandDark,
                border = BorderStroke(1.5.dp, BrandGold)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // PINNED TOP BAR (Always visible, cannot be scrolled away, Back & Close always ready)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E172E))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onDismiss() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(BrandCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = BrandGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (selectedTab == 0) "Diamond & Coin Wallet" else "Host Earning Wallet",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selectedTab == 0) "Recharge diamonds • Instant UTR credit" else "Real cash host earnings & UPI withdrawal",
                                    color = BrandGold,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(BrandCard, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    HorizontalDivider(color = BrandBorder.copy(alpha = 0.5f), thickness = 1.dp)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                // 2. Dual-Wallet Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E172E))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) BrandGold else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Diamond Wallet",
                                color = if (selectedTab == 0) BrandDark else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) BrandGold else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💵", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Earning Wallet",
                                color = if (selectedTab == 1) BrandDark else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // 3. Tab Contents
                if (selectedTab == 0) {
                    // ==========================================
                    // TAB 0: 💎 DIAMOND WALLET
                    // ==========================================

                    // Balance Strip
                    Surface(
                        color = BrandCard,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Current Diamond Balance", color = Color.Gray, fontSize = 10.sp)
                                Text(
                                    text = "%,d Diamonds".format(liveDiamonds),
                                    color = BrandCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenHelpDeskWithPackage(APP_COIN_PACKAGES[0])
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCardSubtle),
                                border = BorderStroke(1.dp, BrandGold),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Submit UTR", color = BrandGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Select Recharge Package to Pay with UPI", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    // Package List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!liveIsHostMonetized) {
                            item {
                                Surface(
                                    color = Color(0xFF22162B),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(BrandGold, Color(0xFFFF5E62)))),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTab = 1 }
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
                                                Text("Convert room gifts into direct cash income (INR)", color = Color.LightGray, fontSize = 10.sp)
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
                        }

                        items(APP_COIN_PACKAGES) { pack ->
                            Surface(
                                color = BrandCard,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (pack.badge.isNotBlank()) BrandGold else BrandBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        checkoutPackage = pack
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💎", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "%,d Diamonds".format(pack.coins),
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (pack.badge.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = BrandGold,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            pack.badge,
                                                            color = BrandDark,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(pack.title, color = Color.Gray, fontSize = 11.sp)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            checkoutPackage = pack
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(pack.priceInr, color = BrandDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "🔒 Supported UPI Apps: Google Pay, PhonePe, Paytm, BHIM, etc.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // ==========================================
                    // TAB 1: 💵 EARNING WALLET (HOST COMMISSIONS & UPI WITHDRAWAL)
                    // ==========================================
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 0. Host Monetization Status & Activation Card
                        item {
                            if (!liveIsHostMonetized) {
                                Surface(
                                    color = Color(0xFF23152B),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(BrandGold, Color(0xFFFF5E62), Color(0xFFFF9966)))),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🚀", fontSize = 26.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "To Start Monetization",
                                                        color = BrandGold,
                                                        fontSize = 17.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Text(
                                                        text = "Official Host Earning Subscription",
                                                        color = Color.LightGray,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            Surface(
                                                color = Color(0xFFFF5E62).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(20.dp),
                                                border = BorderStroke(1.dp, Color(0xFFFF5E62))
                                            ) {
                                                Text(
                                                    text = "NOT ACTIVE",
                                                    color = Color(0xFFFF5E62),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        // Pricing Banner with ₹1,499 struck through & Launch Offer ₹599
                                        Surface(
                                            color = Color(0xFF150F1E),
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Regular Price: ₹1,499",
                                                        color = Color.Gray,
                                                        fontSize = 12.sp,
                                                        textDecoration = TextDecoration.LineThrough,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "Launch Offer: ",
                                                            color = Color.White,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "₹599",
                                                            color = Color(0xFF00E676),
                                                            fontSize = 20.sp,
                                                            fontWeight = FontWeight.ExtraBold
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    color = Color(0xFFFF5252).copy(alpha = 0.25f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFFF5252))
                                                ) {
                                                    Text(
                                                        text = "SAVE ₹900 (60% OFF)",
                                                        color = Color(0xFFFF8A80),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Subscribe now to start monetizing your live audio and video streaming rooms! All diamond gifts sent by your audience convert to real cash (INR) with instant withdrawals to UPI.",
                                            color = Color(0xFFDDDDDD),
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        )

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF150F1E), RoundedCornerShape(12.dp))
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("💰", fontSize = 13.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Real Cash Income: Gift diamonds convert to ₹ INR", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("👑", fontSize = 13.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("5% - 8% Host Commission on all room gifts", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("⚡", fontSize = 13.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Instant UPI Withdrawals to PhonePe, Google Pay, Paytm", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                checkoutPackage = CoinPackageItem(
                                                    id = "pkg_host_monetization_599",
                                                    coins = 0L,
                                                    priceInr = "₹599",
                                                    numericPrice = "599",
                                                    title = "Host Monetization Lifetime Activation 🚀",
                                                    badge = "OFFICIAL"
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Subscribe to Start Monetization • ₹599", color = BrandDark, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("₹1,499", color = BrandDark.copy(alpha = 0.55f), fontSize = 11.sp, textDecoration = TextDecoration.LineThrough)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("➔", color = BrandDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    color = Color(0xFF13251A),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("✅", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Host Monetization Active 🚀", color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                            Text("You are receiving real cash commissions on all room gifts with instant UPI withdrawals.", color = Color.LightGray, fontSize = 10.5.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (!liveIsHostMonetized) {
                            item {
                                Surface(
                                    color = Color(0xFF1B142B),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("🔒", fontSize = 36.sp)
                                        Text(
                                            "Earning & Payment History Locked",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Subscribe to Start Monetization above for ₹599 to unlock your cash balance, commission earnings, withdrawal requests, and real-time transaction history.",
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            // 1. Available Earnings Balance Card
                            item {
                                Surface(
                                    color = Color(0xFF1B142B),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(BrandGold, Color(0xFFFFB74D)))),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Available Cash Balance", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            Text(
                                                text = "₹ ${"%.2f".format(liveEarningsInr)}",
                                                color = Color(0xFF00E676),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }

                                        Surface(
                                            color = BrandGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(20.dp),
                                            border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("👑", fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Commission: ${commissionPercent.toInt()}%",
                                                    color = BrandGold,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Total Lifetime Earned", color = Color.Gray, fontSize = 10.sp)
                                            Text("₹ ${"%.2f".format(liveTotalEarningsInr)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Total Withdrawn", color = Color.Gray, fontSize = 10.sp)
                                            Text("₹ ${"%.2f".format(liveWithdrawnInr)}", color = Color(0xFFFF9100), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Commission Rate Tier Information
                        item {
                            Surface(
                                color = Color(0xFF141221),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💡", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (hostWealthLvl >= 70) {
                                            "Imperial Host (Lv.$hostWealthLvl): Highest 8% commission active on every gift sent in your room!"
                                        } else if (hostWealthLvl >= 50) {
                                            "Super Host (Lv.$hostWealthLvl): 7% commission active! Reach Level 70 for 8% commission!"
                                        } else {
                                            "Host (Lv.$hostWealthLvl): 5% commission. Reach Level 50 for 7% and Level 70 for 8% commission!"
                                        },
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }

                        // 3. Instant UPI Withdrawal Form
                        item {
                            Surface(
                                color = BrandCard,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BrandBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("💸 Request Instant UPI Withdrawal", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Amount Input
                                    OutlinedTextField(
                                        value = withdrawAmountText,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() || it == '.' }) {
                                                withdrawAmountText = input
                                            }
                                        },
                                        label = { Text("Withdrawal Amount (₹)", fontSize = 11.sp) },
                                        placeholder = { Text("Min. ₹50", fontSize = 12.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandGold,
                                            unfocusedBorderColor = BrandBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick Amount Chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(100, 500, 1000).forEach { amt ->
                                            Surface(
                                                color = BrandCardSubtle,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                                modifier = Modifier.clickable { withdrawAmountText = amt.toString() }
                                            ) {
                                                Text("₹$amt", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                            }
                                        }
                                        Surface(
                                            color = BrandGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, BrandGold),
                                            modifier = Modifier.clickable {
                                                if (liveEarningsInr > 0) {
                                                    withdrawAmountText = "%.2f".format(liveEarningsInr)
                                                }
                                            }
                                        ) {
                                            Text("All", color = BrandGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // UPI ID Input
                                    OutlinedTextField(
                                        value = withdrawUpiId,
                                        onValueChange = { withdrawUpiId = it },
                                        label = { Text("UPI ID (VPA)", fontSize = 11.sp) },
                                        placeholder = { Text("e.g. mobile@paytm or name@oksbi", fontSize = 12.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandGold,
                                            unfocusedBorderColor = BrandBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val withdrawAmountVal = withdrawAmountText.toDoubleOrNull() ?: 0.0
                                    val canWithdraw = liveIsHostMonetized &&
                                            withdrawAmountVal >= 50.0 &&
                                            withdrawAmountVal <= liveEarningsInr &&
                                            withdrawUpiId.contains("@") &&
                                            !isSubmittingWithdrawal

                                    Button(
                                        onClick = {
                                            if (!liveIsHostMonetized) {
                                                checkoutPackage = CoinPackageItem(
                                                    id = "pkg_host_monetization_599",
                                                    coins = 0L,
                                                    priceInr = "₹599",
                                                    numericPrice = "599",
                                                    title = "Host Monetization Lifetime Activation 🚀",
                                                    badge = "OFFICIAL"
                                                )
                                                return@Button
                                            }
                                            if (!canWithdraw) return@Button
                                            isSubmittingWithdrawal = true
                                            val reqId = UUID.randomUUID().toString()
                                            val cleanUpi = withdrawUpiId.trim()

                                            // 1. Deduct from earnings & record to withdrawn in Firestore
                                            db.collection("users").document(currentUser.uid).update(
                                                mapOf(
                                                    "earningsInr" to FieldValue.increment(-withdrawAmountVal),
                                                    "withdrawnInr" to FieldValue.increment(withdrawAmountVal),
                                                    "upiId" to cleanUpi
                                                )
                                            )

                                            // 2. Write to withdrawal_requests collection
                                            val reqDoc = mapOf(
                                                "id" to reqId,
                                                "userId" to currentUser.uid,
                                                "userNumericId" to currentUser.userId,
                                                "userName" to currentUser.name,
                                                "userPhoto" to currentUser.photo,
                                                "amountInr" to withdrawAmountVal,
                                                "upiId" to cleanUpi,
                                                "status" to "PENDING",
                                                "timestamp" to System.currentTimeMillis()
                                            )
                                            db.collection("withdrawal_requests").document(reqId).set(reqDoc)

                                            // 3. Write to earning_transactions
                                            val tx = EarningTransaction(
                                                id = reqId,
                                                type = "WITHDRAWAL",
                                                title = "UPI Withdrawal Request",
                                                amountInr = withdrawAmountVal,
                                                diamonds = 0L,
                                                senderName = currentUser.name,
                                                upiId = cleanUpi,
                                                status = "PENDING",
                                                timestamp = System.currentTimeMillis()
                                            )
                                            db.collection("users").document(currentUser.uid)
                                                .collection("earning_transactions").document(reqId).set(tx)
                                                .addOnSuccessListener {
                                                    isSubmittingWithdrawal = false
                                                    withdrawAmountText = ""
                                                    Toast.makeText(context, "✅ Withdrawal request for ₹%.2f submitted! Processing to $cleanUpi".format(withdrawAmountVal), Toast.LENGTH_LONG).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    isSubmittingWithdrawal = false
                                                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        },
                                        enabled = canWithdraw || !liveIsHostMonetized,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!liveIsHostMonetized) Color(0xFFFF9966) else BrandGold,
                                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isSubmittingWithdrawal) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrandDark, strokeWidth = 2.dp)
                                        } else {
                                            Text(
                                                text = if (!liveIsHostMonetized) "Activate Monetization (₹599) to Withdraw 🚀"
                                                else if (withdrawAmountVal > liveEarningsInr) "Insufficient Balance"
                                                else if (withdrawAmountVal in 0.01..49.99) "Min. Withdrawal ₹50"
                                                else if (!withdrawUpiId.contains("@") && withdrawAmountVal >= 50.0) "Enter Valid UPI ID (@)"
                                                else if (withdrawAmountVal >= 50.0) "Withdraw ₹%.2f to UPI".format(withdrawAmountVal)
                                                else "Request UPI Withdrawal",
                                                color = if (canWithdraw || !liveIsHostMonetized) BrandDark else Color.LightGray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Real-Time Transaction & Payment History (PURE REAL-TIME FIRESTORE, ZERO DUMMY DATA)
                        item {
                            Text("📜 Transaction & Payment History", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        if (earningTransactions.isEmpty()) {
                            item {
                                Surface(
                                    color = BrandCard,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BrandBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("📦", fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("No transactions yet", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Host live audio rooms and receive gifts to earn real cash commissions!", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        } else {
                            items(earningTransactions) { tx ->
                                val isCredit = tx.type == "GIFT_COMMISSION"
                                val dateStr = remember(tx.timestamp) {
                                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
                                }

                                Surface(
                                    color = BrandCard,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BrandBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isCredit) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFF9100).copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(if (isCredit) "🎁" else "🏦", fontSize = 16.sp)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = tx.title.ifBlank { if (isCredit) "Gift Commission Received" else "UPI Withdrawal" },
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (tx.upiId.isNotBlank()) "To: ${tx.upiId} • $dateStr" else dateStr,
                                                    color = Color.Gray,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (isCredit) "+ ₹${"%.2f".format(tx.amountInr)}" else "- ₹${"%.2f".format(tx.amountInr)}",
                                                color = if (isCredit) Color(0xFF00E676) else Color(0xFFFF9100),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Surface(
                                                color = if (tx.status == "APPROVED" || tx.status == "CREDITED") Color(0xFF00E676).copy(alpha = 0.2f)
                                                else if (tx.status == "REJECTED") Color(0xFFFF5252).copy(alpha = 0.2f)
                                                else Color(0xFFFF9100).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = tx.status,
                                                    color = if (tx.status == "APPROVED" || tx.status == "CREDITED") Color(0xFF00E676)
                                                    else if (tx.status == "REJECTED") Color(0xFFFF5252)
                                                    else Color(0xFFFF9100),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
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
    }
}
}
}
    }

/**
 * VipStatusAndPurchaseDialog
 *
 * Displays active VIP trial real-time countdown (Strictly 3 Days / 72 Hours).
 * Clearly conveys that all 50,000 diamonds are PERMANENT and never expire.
 * Allows users to renew/upgrade VIP status or purchase VIP animated frames using diamonds/recharge.
 */
@Composable
fun VipStatusAndPurchaseDialog(
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onOpenCoinStore: () -> Unit,
    onOpenSubscriptionPlans: () -> Unit
) {
    val now = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now.longValue = System.currentTimeMillis()
        }
    }

    val isTrialActive = currentUser.vipTrialExpiryDate > 0L && now.longValue <= currentUser.vipTrialExpiryDate
    val remainingMs = maxOf(0L, currentUser.vipTrialExpiryDate - now.longValue)
    val daysLeft = (remainingMs / (1000 * 60 * 60 * 24)).toInt()
    val hoursLeft = ((remainingMs / (1000 * 60 * 60)) % 24).toInt()
    val minutesLeft = ((remainingMs / (1000 * 60)) % 60).toInt()
    val secondsLeft = ((remainingMs / 1000) % 60).toInt()

    val brandDark = Color(0xFF0D0A11)
    val brandCard = Color(0xFF171222)
    val brandGold = Color(0xFFFFD700)
    val brandCoral = Color(0xFFFF5E62)
    val brandCyan = Color(0xFF00E5FF)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(22.dp),
            color = brandDark,
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(brandGold, brandCoral)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(brandGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isTrialActive) "VIP 1 Trial Active" else "VIP Membership & Frames",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isTrialActive) "Exclusive 3-Day Welcome Access" else "Upgrade for exclusive royal privileges",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(brandCard, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Countdown Card (If trial is active)
                if (isTrialActive) {
                    Surface(
                        color = Color(0xFF1F162E),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, brandGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⏳ VIP TRIAL EXPIRATION COUNTDOWN",
                                color = brandGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )

                            // Digital Countdown Blocks
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CountdownUnitBlock(value = "%02d".format(daysLeft), label = "DAYS")
                                Text(":", color = brandGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                CountdownUnitBlock(value = "%02d".format(hoursLeft), label = "HOURS")
                                Text(":", color = brandGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                CountdownUnitBlock(value = "%02d".format(minutesLeft), label = "MINS")
                                Text(":", color = brandGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                CountdownUnitBlock(value = "%02d".format(secondsLeft), label = "SECS")
                            }

                            Text(
                                text = "Your VIP 1 Noble Archangel Frame & Badge auto-expire after 3 days.",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Permanent Diamonds Guarantee Card (Crucial for user peace of mind)
                Surface(
                    color = Color(0xFF131D24),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, brandCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💎", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Diamonds Never Expire!",
                                color = brandCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "All diamonds (including 50,000 welcome bonus) are 100% permanent in your wallet. Only the VIP 1 Frame & Badge expire.",
                                color = Color(0xFFBBDEFB),
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // VIP Benefits List
                Surface(
                    color = brandCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("👑 VIP Perks & Privileges", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exclusive Flowing Animated Archangel Avatar Frame", color = Color.LightGray, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VIP 1 Gold Royal Badge in Room & Profile", color = Color.LightGray, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚀", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlimited Audio Room Swipes & Host Privileges", color = Color.LightGray, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Priority Mic Seating & Entrance Animation", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }

                // Wallet Diamonds Quick Check
                Surface(
                    color = Color(0xFF1E1428),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, brandGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Wallet Balance", color = Color.Gray, fontSize = 10.sp)
                            Text(
                                text = "%,d Diamonds".format(currentUser.diamonds),
                                color = brandGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenCoinStore()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = brandGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("+ Recharge", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Primary Action Button (Renew / Upgrade Plan)
                Button(
                    onClick = {
                        onDismiss()
                        onOpenSubscriptionPlans()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandCoral),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTrialActive) "Extend / Upgrade VIP Plan ➔" else "Unlock VIP Membership ➔",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownUnitBlock(value: String, label: String) {
    Surface(
        color = Color(0xFF140D21),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = Color(0xFFFFD700), fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(text = label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}


// ---------------------------------------------------------------------
// TAB 2: FOUNDER, CEO & OFFICIAL STAFF POWER HUB
// ---------------------------------------------------------------------

@Composable
fun AdminStaffAndPowerHubSection(
    adminUser: UserProfile,
    onUpdateCurrentUser: (UserProfile) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
    var searchQuery by remember { mutableStateOf(adminUser.email.ifBlank { adminUser.userId }) }
    var isSearching by remember { mutableStateOf(false) }
    // Default immediately to the logged-in adminUser so founder profile is ready instantly!
    var searchedUser by remember { mutableStateOf<UserProfile?>(adminUser) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var diamondDelta by remember { mutableStateOf("20000") }
    var isProcessing by remember { mutableStateOf(false) }

    val frameOptions = remember {
        listOf(
            "frame_livetogather_founder" to "👑 Founder Flowing Wings Frame",
            "frame_imperial_gold" to "⚡ Imperial Gold Wings",
            "frame_angel_wings" to "🪽 Celestial Angel Wings",
            "frame_dark_angel" to "🖤 Black Velvet Royale",
            "frame_ice_valkyrie" to "💎 Eternal Diamond Prism",
            "frame_phoenix_fire" to "🔥 Solar Eclipse Inferno"
        )
    }
    var selectedFrameId by remember { mutableStateOf(frameOptions[0].first) }

    val micEffectOptions = remember {
        listOf(
            "mic_effect_sovereign_gold_wings" to "👑 Sovereign Gold Wings (Video)",
            "mic_effect_ruby_energy_flame" to "🔥 Ruby Energy Flame (Video)",
            "mic_effect_cyber_neon_electric" to "⚡ Cyber Neon Electric (Video)",
            "mic_effect_diamond_crystal_vortex" to "💎 Diamond Crystal Vortex (Video)",
            "mic_effect_emerald_celestial_wave" to "💚 Emerald Celestial Wave (Video)",
            "mic_effect_golden_amber_aura" to "✨ Golden Amber Aura (Video)"
        )
    }
    var selectedMicEffectId by remember { mutableStateOf(micEffectOptions[0].first) }

    val validityOptions = listOf(7 to "7 Days", 10 to "10 Days", 30 to "30 Days", 90 to "90 Days", 0 to "Lifetime")
    var selectedValidityDays by remember { mutableIntStateOf(30) }

    // REAL-TIME FIRESTORE LISTENER ON SEARCHED USER:
    // The exact millisecond Firestore updates, searchedUser is updated in real time!
    DisposableEffect(searchedUser?.uid) {
        val targetUid = searchedUser?.uid
        if (targetUid.isNullOrBlank()) return@DisposableEffect onDispose {}

        val listener = db.collection("users").document(targetUid).addSnapshotListener { snap, err ->
            if (snap != null && snap.exists()) {
                val fresh = parseUserProfileFromDoc(snap).copy(uid = snap.id)
                searchedUser = fresh
                if (snap.id == adminUser.uid) {
                    onUpdateCurrentUser(fresh)
                }
            }
        }
        onDispose { listener.remove() }
    }

    fun executeSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) {
            Toast.makeText(context, "Enter 8-digit ID or Email", Toast.LENGTH_SHORT).show()
            return
        }
        isSearching = true
        searchError = null

        val queryTask = if (q.contains("@")) {
            db.collection("users").whereEqualTo("email", q.lowercase()).limit(1).get()
        } else if (q.length == 8 && q.all { it.isDigit() }) {
            db.collection("users").whereEqualTo("userId", q).limit(1).get()
        } else {
            db.collection("users").document(q).get()
        }

        queryTask.addOnSuccessListener { result ->
            isSearching = false
            if (result is com.google.firebase.firestore.QuerySnapshot) {
                if (!result.isEmpty) {
                    val doc = result.documents[0]
                    val parsed = parseUserProfileFromDoc(doc).copy(uid = doc.id)
                    searchedUser = parsed
                } else {
                    searchedUser = null
                    searchError = "No user found with '$q'"
                }
            } else if (result is com.google.firebase.firestore.DocumentSnapshot) {
                if (result.exists()) {
                    val parsed = parseUserProfileFromDoc(result).copy(uid = result.id)
                    searchedUser = parsed
                } else {
                    searchedUser = null
                    searchError = "No user found with '$q'"
                }
            }
        }.addOnFailureListener { err ->
            isSearching = false
            searchedUser = null
            searchError = "Search error: ${err.message}"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 150.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Founders Shortcuts Row (No yellow borders)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B142B)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF3B2F50))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👑 Quick Load Account", color = Color(0xFFA78BFA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                searchedUser = adminUser
                                searchQuery = adminUser.email.ifBlank { adminUser.userId }
                                Toast.makeText(context, "Loaded your Account (${adminUser.name})", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("👤 Load My Account (${adminUser.name.ifBlank { "Founder" }})", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                searchQuery = "service.secure822@gmail.com"
                                executeSearch("service.secure822@gmail.com")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C1D95)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text("👑 Founder Email", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Search Input Bar (No yellow borders)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B142B)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF3B2F50))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔍 Search User by 8-Digit ID or Email", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("e.g. 52418932 or email", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA78BFA),
                                unfocusedBorderColor = Color(0xFF3B2F50)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { executeSearch(searchQuery) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            if (isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Search", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    if (searchError != null) {
                        Text(text = searchError!!, color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Target User Details & Super Power Hub
        if (searchedUser != null) {
            val target = searchedUser!!

            // User Identity Card (Sleek dark design, no yellow borders)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140F20)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFF4C3A66))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FlowingAvatarFrame(
                                photoUrl = target.photo,
                                frameId = target.activeFrame,
                                avatarSize = 56.dp,
                                frameSize = 84.dp,
                                showAnimation = true
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(target.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                    if (target.isFounder()) {
                                        Text("👑 Founder", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    } else if (target.isCeo()) {
                                        Text("💎 CEO", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("ID: ${target.userId} | ${target.email}", color = Color.LightGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("💎 %,d Diamonds".format(target.diamonds), color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    Text("🪙 %,d Coins".format(target.coins), color = Color(0xFFFFB74D), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Current Role Badge Preview
                        val roleBadgeRes = target.getOfficialRoleBadgeRes()
                        if (roleBadgeRes != null) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF4C3A66)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(id = roleBadgeRes),
                                            contentDescription = "Badge",
                                            modifier = Modifier.height(28.dp)
                                        )
                                        Text(target.getOfficialRoleTitle(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("● REAL-TIME ACTIVE", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            // 1-Click Official Appointments Panel (Instant Real-time)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B142B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B2F50))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("👑 1-Click Leadership & Staff Appointments", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Clicking instantly assigns official role, 3D winged badges and frames in real time.", color = Color.LightGray, fontSize = 10.5.sp)

                        // Founder & CEO Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isProcessing = true
                                    val updates = mapOf(
                                        "officialRole" to "founder",
                                        "activeFrame" to "frame_livetogather_founder",
                                        "ownedFrames" to FieldValue.arrayUnion("frame_livetogather_founder"),
                                        "isAdmin" to true
                                    )
                                    db.collection("users").document(target.uid).set(updates, SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(officialRole = "founder", activeFrame = "frame_livetogather_founder", isAdmin = true)
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "👑 Appointed as LiveTogather Founder! Badge & Wings Frame active.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text("👑 Appoint Founder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    isProcessing = true
                                    val updates = mapOf(
                                        "officialRole" to "ceo",
                                        "isAdmin" to true
                                    )
                                    db.collection("users").document(target.uid).set(updates, SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(officialRole = "ceo", isAdmin = true)
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "💎 Appointed as LiveTogather CEO! Badge active.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text("💎 Appoint CEO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Staff Appointments Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    db.collection("users").document(target.uid).set(mapOf("officialRole" to "analyzer"), SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(officialRole = "analyzer")
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "🔍 Appointed Official Analyzer! Badge activated.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                border = BorderStroke(1.dp, Color(0xFF00F0FF)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("🔍 Analyzer", color = Color(0xFF00F0FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    db.collection("users").document(target.uid).set(mapOf("officialRole" to "volunteer"), SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(officialRole = "volunteer")
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "🤝 Appointed Community Volunteer! Badge activated.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                border = BorderStroke(1.dp, Color(0xFF00FFA3)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("🤝 Volunteer", color = Color(0xFF00FFA3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    db.collection("users").document(target.uid).set(mapOf("officialRole" to "safety_patrol"), SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(officialRole = "safety_patrol")
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "🛡️ Appointed Safety Patrol! Badge activated.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                border = BorderStroke(1.dp, Color(0xFFFF5252)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("🛡️ Safety", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Dismiss from Position (पदमुक्ति) Button
                        Button(
                            onClick = {
                                isProcessing = true
                                val newFrame = if (target.activeFrame == "frame_livetogather_founder") "" else target.activeFrame
                                val updates = mapOf(
                                    "officialRole" to "",
                                    "activeFrame" to newFrame
                                )
                                db.collection("users").document(target.uid).set(updates, SetOptions.merge())
                                    .addOnSuccessListener {
                                        isProcessing = false
                                        val updated = target.copy(officialRole = "", activeFrame = newFrame)
                                        searchedUser = updated
                                        if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                        Toast.makeText(context, "⚠️ Position dismissed! Badges revoked.", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        isProcessing = false
                                        Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text("⚠️ Dismiss from Position (पदमुक्ति / Revoke Badge)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Wallet Super Power (Inject / Deduct Diamonds & Coins - Instant Real-Time)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B142B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B2F50))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("💎 Wallet Power (Direct Diamonds & Coins Injection)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        // Amount Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("5000", "20000", "50000", "100000").forEach { preset ->
                                OutlinedButton(
                                    onClick = {
                                        diamondDelta = preset
                                    },
                                    border = BorderStroke(1.dp, Color(0xFF4C3A66)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Text("+$preset", fontSize = 10.sp, color = Color(0xFFA78BFA))
                                }
                            }
                        }

                        // Amount Input
                        OutlinedTextField(
                            value = diamondDelta,
                            onValueChange = { diamondDelta = it },
                            label = { Text("Amount", color = Color.Gray, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFA78BFA),
                                unfocusedBorderColor = Color(0xFF3B2F50)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Diamond Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val amt = diamondDelta.toLongOrNull() ?: 0L
                                    if (amt <= 0) return@Button
                                    isProcessing = true
                                    val newDiamonds = target.diamonds + amt
                                    db.collection("users").document(target.uid)
                                        .set(mapOf("diamonds" to newDiamonds), SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(diamonds = newDiamonds)
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "💎 Added %,d Diamonds!".format(amt), Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("+ Inject 💎", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val amt = diamondDelta.toLongOrNull() ?: 0L
                                    if (amt <= 0) return@Button
                                    isProcessing = true
                                    val newDiamonds = maxOf(0L, target.diamonds - amt)
                                    db.collection("users").document(target.uid)
                                        .set(mapOf("diamonds" to newDiamonds), SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(diamonds = newDiamonds)
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "💎 Deducted %,d Diamonds!".format(amt), Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("- Deduct 💎", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Coin Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val amt = diamondDelta.toLongOrNull() ?: 0L
                                    if (amt <= 0) return@Button
                                    isProcessing = true
                                    val newCoins = target.coins + amt
                                    db.collection("users").document(target.uid)
                                        .set(mapOf("coins" to newCoins), SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(coins = newCoins)
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "🪙 Added %,d Coins!".format(amt), Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("+ Inject 🪙", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val amt = diamondDelta.toLongOrNull() ?: 0L
                                    if (amt <= 0) return@Button
                                    isProcessing = true
                                    val newCoins = maxOf(0L, target.coins - amt)
                                    db.collection("users").document(target.uid)
                                        .set(mapOf("coins" to newCoins), SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(coins = newCoins)
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "🪙 Deducted %,d Coins!".format(amt), Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("- Deduct 🪙", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Frame & 3D Video Mic Effect Power Panel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B142B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B2F50))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🪽 Avatar Frame & 3D Video Mic Effect Granter", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        // Frame Selection
                        Text("Select Profile Frame:", color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        frameOptions.chunked(2).forEach { rowFrames ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowFrames.forEach { (fid, label) ->
                                    val isSelected = selectedFrameId == fid
                                    OutlinedButton(
                                        onClick = { selectedFrameId = fid },
                                        border = BorderStroke(1.2.dp, if (isSelected) Color(0xFF8B5CF6) else Color(0xFF3B2F50)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) Color(0xFF6D28D9).copy(alpha = 0.25f) else Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text(label, fontSize = 9.5.sp, color = if (isSelected) Color.White else Color.Gray, maxLines = 1)
                                    }
                                }
                            }
                        }

                        // Validity Days Selection
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Validity Period:", color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            validityOptions.forEach { (days, label) ->
                                val isSelected = selectedValidityDays == days
                                OutlinedButton(
                                    onClick = { selectedValidityDays = days },
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF8B5CF6) else Color(0xFF3B2F50)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) Color(0xFF6D28D9) else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f).height(30.dp)
                                ) {
                                    Text(label, fontSize = 9.sp, color = if (isSelected) Color.White else Color.Gray)
                                }
                            }
                        }

                        // Frame Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isProcessing = true
                                    val now = System.currentTimeMillis()
                                    val expiry = if (selectedValidityDays > 0) now + (selectedValidityDays * 86400000L) else 0L
                                    val updates = mapOf(
                                        "activeFrame" to selectedFrameId,
                                        "ownedFrames" to FieldValue.arrayUnion(selectedFrameId),
                                        "frameExpiries.$selectedFrameId" to expiry
                                    )
                                    db.collection("users").document(target.uid).set(updates, SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(activeFrame = selectedFrameId)
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "🎁 Frame granted and equipped in real time!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("🎁 Grant Frame", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    db.collection("users").document(target.uid).set(mapOf("activeFrame" to ""), SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(activeFrame = "")
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "❌ Frame revoked", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                border = BorderStroke(1.dp, Color(0xFFFF5252)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("❌ Revoke Frame", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Mic Effect Selection
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Select 3D Video Mic Effect:", color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        micEffectOptions.chunked(2).forEach { rowEffects ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowEffects.forEach { (mid, label) ->
                                    val isSelected = selectedMicEffectId == mid
                                    val tRes = getMicEffectThumbRes(mid)
                                    OutlinedButton(
                                        onClick = { selectedMicEffectId = mid },
                                        border = BorderStroke(1.2.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF3B2F50)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = tRes),
                                                contentDescription = label,
                                                modifier = Modifier.size(22.dp).clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Text(label, fontSize = 8.5.sp, color = if (isSelected) Color.White else Color.Gray, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        // Mic Effect Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isProcessing = true
                                    val now = System.currentTimeMillis()
                                    val expiry = if (selectedValidityDays > 0) now + (selectedValidityDays * 86400000L) else 0L
                                    val updates = mapOf(
                                        "activeMicEffect" to selectedMicEffectId,
                                        "ownedMicEffects" to FieldValue.arrayUnion(selectedMicEffectId),
                                        "micEffectExpiries.$selectedMicEffectId" to expiry
                                    )
                                    db.collection("users").document(target.uid).set(updates, SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(activeMicEffect = selectedMicEffectId)
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "✨ 3D Mic Effect granted and equipped!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("✨ Grant Mic Effect", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    db.collection("users").document(target.uid).set(mapOf("activeMicEffect" to ""), SetOptions.merge())
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            val updated = target.copy(activeMicEffect = "")
                                            searchedUser = updated
                                            if (target.uid == adminUser.uid) onUpdateCurrentUser(updated)
                                            Toast.makeText(context, "❌ Mic Effect revoked", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                },
                                border = BorderStroke(1.dp, Color(0xFFFF5252)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("❌ Revoke Mic Effect", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
