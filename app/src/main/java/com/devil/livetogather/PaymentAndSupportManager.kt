package com.devil.livetogather

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

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
    CoinPackageItem("pack_149", 50000L, "₹149", "149", "Special Launch Pack 💎", "BEST VALUE"),
    CoinPackageItem("pack_49", 15000L, "₹49", "49", "Starter Pack 💎"),
    CoinPackageItem("pack_299", 120000L, "₹299", "299", "Super Value Pack 💎"),
    CoinPackageItem("pack_599", 300000L, "₹599", "599", "Royal VIP Pack 💎"),
    CoinPackageItem("pack_999", 600000L, "₹999", "999", "Imperial Diamond Vault 💎")
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
    val timestamp: Long = System.currentTimeMillis()
)

object PaymentAndSupportManager {

    /**
     * Triggers the native Android UPI Intent Chooser with deep-link:
     * pa: "warsi.1@ptaxis"
     * pn: "LiveTogether"
     * tn: "LiveTogether Coins"
     * am: package price
     * cu: "INR"
     */
    fun launchUpiPayment(
        context: Context,
        amount: String,
        onOpened: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            val cleanAmount = amount.replace("₹", "").trim()
            val upiUri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "warsi.1@ptaxis")
                .appendQueryParameter("pn", "LiveTogether")
                .appendQueryParameter("tn", "LiveTogether Coins")
                .appendQueryParameter("am", cleanAmount)
                .appendQueryParameter("cu", "INR")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, upiUri)
            val chooser = Intent.createChooser(intent, "Pay ₹$cleanAmount via UPI")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            onOpened()
        } catch (e: android.content.ActivityNotFoundException) {
            android.util.Log.e("UPI", "No UPI App found: ${e.message}", e)
            onError("NO_UPI_APP")
        } catch (e: Exception) {
            android.util.Log.e("UPI", "Launch error: ${e.message}", e)
            onError(e.localizedMessage ?: "No supported UPI payment app found")
        }
    }

    /**
     * Launches a specific UPI App (PhonePe, Google Pay, Paytm) directly or generic chooser.
     */
    fun launchSpecificUpiApp(
        context: Context,
        amount: String,
        packageName: String?,
        onOpened: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
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
                if (!packageName.isNullOrBlank()) {
                    setPackage(packageName)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onOpened()
        } catch (e: android.content.ActivityNotFoundException) {
            onError("APP_NOT_INSTALLED")
        } catch (e: Exception) {
            android.util.Log.e("UPI", "Launch error: ${e.message}", e)
            onError(e.localizedMessage ?: "Failed to open payment app")
        }
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
            "status" to "PENDING",
            "category" to category,
            "message" to message,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("payment_requests").document(autoId)
            .set(requestData)
            .addOnSuccessListener { onSuccess() }
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

        // 1. Update payment_requests status to APPROVED
        val reqRef = db.collection("payment_requests").document(request.requestId)
        batch.update(
            reqRef, mapOf(
                "status" to "APPROVED",
                "approvedAt" to FieldValue.serverTimestamp(),
                "approvedBy" to adminUid
            )
        )

        // 2. Increment target user wallet coins & diamonds atomically
        val userRef = db.collection("users").document(request.userId)
        batch.update(
            userRef, mapOf(
                "coins" to FieldValue.increment(request.coinsRequested),
                "diamonds" to FieldValue.increment(request.coinsRequested)
            )
        )

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
                    "status" to "REJECTED",
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
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = BrandCard,
            border = BorderStroke(1.5.dp, BrandGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
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
    var showQrCode by remember { mutableStateOf(false) }
    val cleanAmount = selectedPackage.numericPrice.ifBlank { selectedPackage.priceInr.replace("₹", "").trim() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = BrandDark,
            border = BorderStroke(1.5.dp, BrandGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UPI Payment Checkout",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "User ID: #${currentUser.userId.ifBlank { currentUser.uid.take(6) }}",
                            color = BrandGold,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(BrandCard, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Package Details Card
                Surface(
                    color = BrandCard,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "%,d Diamonds".format(selectedPackage.coins),
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // 1. One-Tap UPI Apps
                Text(
                    text = "1. Pay with Installed UPI App",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val upiApps = listOf(
                    Triple("PhonePe", "com.phonepe.app", Color(0xFF5F259F)),
                    Triple("Google Pay", "com.google.android.apps.nbu.paisa.user", Color(0xFF1A73E8)),
                    Triple("Paytm", "net.one97.paytm", Color(0xFF00B9F5)),
                    Triple("Other UPI", null, Color(0xFFF57C00))
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        upiApps.take(2).forEach { (name, pkg, col) ->
                            Button(
                                onClick = {
                                    PaymentAndSupportManager.launchSpecificUpiApp(
                                        context = context,
                                        amount = cleanAmount,
                                        packageName = pkg,
                                        onOpened = {
                                            Toast.makeText(context, "Opening $name... Complete payment and enter 12-digit UTR below.", Toast.LENGTH_LONG).show()
                                        },
                                        onError = {
                                            Toast.makeText(context, "$name is not installed. You can copy the UPI ID or scan QR code below!", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = col),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        upiApps.drop(2).forEach { (name, pkg, col) ->
                            Button(
                                onClick = {
                                    PaymentAndSupportManager.launchSpecificUpiApp(
                                        context = context,
                                        amount = cleanAmount,
                                        packageName = pkg,
                                        onOpened = {
                                            Toast.makeText(context, "Opening $name... Complete payment and enter 12-digit UTR below.", Toast.LENGTH_LONG).show()
                                        },
                                        onError = {
                                            Toast.makeText(context, "$name is not installed. You can copy the UPI ID or scan QR code below!", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = col),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 2. Direct Transfer / QR Code
                Text(
                    text = "2. Or Pay via UPI ID / QR Code",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    color = BrandCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BrandBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Official UPI ID", color = Color.Gray, fontSize = 10.sp)
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
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Copy UPI", color = BrandDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // QR Code Toggle
                        OutlinedButton(
                            onClick = { showQrCode = !showQrCode },
                            border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(34.dp)
                        ) {
                            Text(
                                text = if (showQrCode) "▲ Hide QR Code" else "▼ Show QR Code (Scan to Pay ₹$cleanAmount)",
                                color = BrandGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (showQrCode) {
                            val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=260x260&data=" +
                                Uri.encode("upi://pay?pa=warsi.1@ptaxis&pn=LiveTogether&am=$cleanAmount&cu=INR&tn=LiveTogether%20Coins")
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = qrUrl,
                                    contentDescription = "Scan to Pay ₹$cleanAmount",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(6.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Scan with any UPI app on any phone to pay ₹$cleanAmount",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // 3. Step 2: 12-Digit UTR Verification
                Text(
                    text = "3. Enter 12-Digit UTR to Add Coins",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = utrInput,
                    onValueChange = { if (it.length <= 12) utrInput = it.filter { c -> c.isDigit() } },
                    label = { Text("12-Digit UTR / UPI Ref (e.g. 4238xxxxxxxx)", fontSize = 11.sp) },
                    placeholder = { Text("Enter 12 digits from receipt", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGold,
                        unfocusedBorderColor = BrandBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val trimmed = utrInput.trim()
                        if (trimmed.length < 10) {
                            Toast.makeText(context, "Please enter a valid 12-digit UTR number from your payment receipt!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        PaymentAndSupportManager.submitPaymentVerificationRequest(
                            userId = currentUser.uid,
                            userNumericId = currentUser.userId.ifBlank { currentUser.uid.take(6) },
                            userName = currentUser.name,
                            utrNumber = trimmed,
                            packCoins = selectedPackage.coins,
                            packAmount = selectedPackage.priceInr,
                            onSuccess = {
                                isSubmitting = false
                                Toast.makeText(context, "✅ UTR #$trimmed submitted successfully! Coins will be credited to your account.", Toast.LENGTH_LONG).show()
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = BrandDark, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Verify & Credit %,d Diamonds 💎".format(selectedPackage.coins),
                            color = BrandDark,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Text(
                    text = "🔒 100% Secure • Official Account: warsi.1@ptaxis • Live Firestore Sync",
                    color = Color.Gray,
                    fontSize = 9.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
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
                            try {
                                val ts = doc.getTimestamp("timestamp")?.toDate()?.time
                                    ?: doc.getLong("timestamp") ?: 0L
                                PaymentRequestTicket(
                                    requestId = doc.getString("requestId") ?: doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    userNumericId = doc.getString("userNumericId") ?: "",
                                    userName = doc.getString("userName") ?: "",
                                    utrNumber = doc.getString("utrNumber") ?: "",
                                    coinsRequested = doc.getLong("coinsRequested") ?: 0L,
                                    amountPaid = doc.getString("amountPaid") ?: "",
                                    status = doc.getString("status") ?: "PENDING",
                                    category = doc.getString("category") ?: "Payment Verification (Submit UTR for Coins)",
                                    message = doc.getString("message") ?: "",
                                    timestamp = ts
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }.sortedByDescending { it.timestamp }
                        myTickets.addAll(list)
                    }
                }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = BrandDark,
            border = BorderStroke(1.5.dp, BrandBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandCard)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandCoral.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎧", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Help & Support Desk",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Account ID: #${currentUser.userId.ifBlank { currentUser.uid.take(6) }}",
                                color = BrandGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(BrandDark, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
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
                            .fillMaxSize()
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandCoral,
                                    unfocusedBorderColor = BrandBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Submit Button
                        Button(
                            onClick = {
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
                                val displayId = currentUser.userId.ifBlank {
                                    (kotlin.math.abs(currentUser.uid.hashCode() % 900000) + 100000).toString()
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
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pendingRequests = remember { mutableStateListOf<PaymentRequestTicket>() }
    var processingReqId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("payment_requests")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    pendingRequests.clear()
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val ts = doc.getTimestamp("timestamp")?.toDate()?.time
                                ?: doc.getLong("timestamp") ?: 0L
                            PaymentRequestTicket(
                                requestId = doc.getString("requestId") ?: doc.id,
                                userId = doc.getString("userId") ?: "",
                                userNumericId = doc.getString("userNumericId") ?: "",
                                userName = doc.getString("userName") ?: "",
                                utrNumber = doc.getString("utrNumber") ?: "",
                                coinsRequested = doc.getLong("coinsRequested") ?: 0L,
                                amountPaid = doc.getString("amountPaid") ?: "",
                                status = doc.getString("status") ?: "PENDING",
                                category = doc.getString("category") ?: "",
                                message = doc.getString("message") ?: "",
                                timestamp = ts
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.timestamp }
                    pendingRequests.addAll(list)
                }
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = BrandDark,
            border = BorderStroke(1.5.dp, BrandGold)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandCard)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Admin Approvals Dashboard",
                                color = BrandGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${pendingRequests.size} Pending Payment Approvals",
                                color = if (pendingRequests.isNotEmpty()) BrandCoral else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(BrandDark, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

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
    var selectedPackageToVerify by remember { mutableStateOf<CoinPackageItem?>(null) }
    var showPostPaymentDialog by remember { mutableStateOf(false) }

    if (showPostPaymentDialog) {
        PostPaymentVerificationDialog(
            selectedPackage = selectedPackageToVerify,
            onDismiss = {
                showPostPaymentDialog = false
                onDismiss()
            },
            onSubmitUtrClick = { pack ->
                showPostPaymentDialog = false
                onDismiss()
                if (pack != null) {
                    onOpenHelpDeskWithPackage(pack)
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = BrandDark,
            border = BorderStroke(1.5.dp, BrandGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                .background(BrandGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🪙", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Coin Store & Top-Up", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Direct UPI payments with instant UTR verification", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(BrandCard, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

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
                            Text("Current Wallet Balance", color = Color.Gray, fontSize = 10.sp)
                            Text(
                                text = "%,d Diamonds / Coins".format(currentUser.diamonds),
                                color = BrandCyan,
                                fontSize = 15.sp,
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

                Text("Select Coin Package to Pay with UPI", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                // Package List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                    selectedPackageToVerify = pack
                                    PaymentAndSupportManager.launchUpiPayment(
                                        context = context,
                                        amount = pack.numericPrice,
                                        onOpened = {
                                            showPostPaymentDialog = true
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, "No UPI app found. You can submit payment in Help Desk.", Toast.LENGTH_LONG).show()
                                            onDismiss()
                                            onOpenHelpDeskWithPackage(pack)
                                        }
                                    )
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
                                                text = "%,d Coins".format(pack.coins),
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
                                        selectedPackageToVerify = pack
                                        PaymentAndSupportManager.launchUpiPayment(
                                            context = context,
                                            amount = pack.numericPrice,
                                            onOpened = {
                                                showPostPaymentDialog = true
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, "No UPI app found. You can submit payment in Help Desk.", Toast.LENGTH_LONG).show()
                                                onDismiss()
                                                onOpenHelpDeskWithPackage(pack)
                                            }
                                        )
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
            }
        }
    }
}
