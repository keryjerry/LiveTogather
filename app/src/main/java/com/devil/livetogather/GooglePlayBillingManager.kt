package com.devil.livetogather

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Google Play Billing Manager for LiveTogather.
 * Handles official in-app purchases (IAP) for Diamonds, Host Monetization, and VIP subscriptions.
 * Fully compliant with Google Play Billing Library 7.x standards.
 */
object GooglePlayBillingManager : PurchasesUpdatedListener, BillingClientStateListener {

    private const val TAG = "GooglePlayBilling"

    private var billingClient: BillingClient? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Product IDs mapping: CoinPackageItem.id -> Google Play Product ID
    // 7 Consumable Diamond Packs + 1 Host Monetization + 5 VIP Subscriptions (13 active SKUs)
    val INAPP_PRODUCT_IDS = listOf(
        "pack_49",
        "pack_59",
        "pack_149",
        "pack_299",
        "pack_599",
        "pack_999",
        "pack_1999",
        "pkg_host_monetization_599",
        "vip_package_1",
        "vip_package_2",
        "vip_package_3",
        "vip_package_4",
        "vip_package_5"
    )

    private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetailsMap: StateFlow<Map<String, ProductDetails>> = _productDetailsMap.asStateFlow()

    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady: StateFlow<Boolean> = _isBillingReady.asStateFlow()

    private var currentPurchasingUserId: String = ""
    private var currentPurchasingPackage: CoinPackageItem? = null
    private var onPurchaseSuccessCallback: (() -> Unit)? = null
    private var onPurchaseErrorCallback: ((String) -> Unit)? = null

    /**
     * Initialize BillingClient on Application or Screen launch.
     */
    fun initialize(context: Context) {
        if (billingClient != null && billingClient?.isReady == true) {
            Log.d(TAG, "BillingClient is already initialized and ready.")
            return
        }

        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .enablePrepaidPlans()
            .build()

        billingClient = BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .enableAutoServiceReconnection()
            .build()

        startConnection()
    }

    private fun startConnection() {
        val client = billingClient ?: return
        if (client.isReady) {
            _isBillingReady.value = true
            queryInAppProducts()
            return
        }

        client.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.i(TAG, "Google Play Billing setup successful.")
            _isBillingReady.value = true
            queryInAppProducts()
        } else {
            Log.e(TAG, "Google Play Billing setup failed with code: ${billingResult.responseCode} - ${billingResult.debugMessage}")
            _isBillingReady.value = false
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Google Play Billing service disconnected. Attempting to reconnect...")
        _isBillingReady.value = false
        // Exponential retry
        coroutineScope.launch {
            delay(2000)
            startConnection()
        }
    }

    /**
     * Query available in-app product details (pricing, currency, localized titles) from Google Play Console.
     * Categorizes queries into INAPP and SUBS to avoid type-mismatch failure across batches.
     */
    fun queryInAppProducts() {
        val client = billingClient
        if (client == null || !client.isReady) {
            Log.w(TAG, "BillingClient is not ready to query product details.")
            return
        }

        // 1. In-app Coin Top-ups & Host Monetization (7 Diamond Packs + 1 Monetization SKU)
        val inAppProductIds = listOf(
            "pack_49",
            "pack_59",
            "pack_149",
            "pack_299",
            "pack_599",
            "pack_999",
            "pack_1999",
            "pkg_host_monetization_599"
        )
        val inAppProductList = inAppProductIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id.trim())
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(inAppProductList).build()
        ) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list = queryProductDetailsResult.productDetailsList ?: emptyList()
                val unfetched = queryProductDetailsResult.unfetchedProductList ?: emptyList()
                val current = _productDetailsMap.value.toMutableMap()
                list.forEach { current[it.productId] = it }
                _productDetailsMap.value = current
                Log.d(TAG, "Loaded ${list.size} INAPP coin products from Google Play: ${list.map { it.productId }}; unfetched=${unfetched.map { "${it.productId}:${it.statusCode}" }}")
            } else {
                Log.e(TAG, "Failed to query INAPP products: ${billingResult.responseCode} - ${billingResult.debugMessage}")
            }
        }

        // 2. VIP Dressing Suites as INAPP (if created as managed In-App products)
        val vipProductIds = listOf(
            "vip_package_1",
            "vip_package_2",
            "vip_package_3",
            "vip_package_4",
            "vip_package_5"
        )
        val vipInAppList = vipProductIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id.trim())
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(vipInAppList).build()
        ) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list = queryProductDetailsResult.productDetailsList ?: emptyList()
                if (list.isNotEmpty()) {
                    val current = _productDetailsMap.value.toMutableMap()
                    list.forEach { current[it.productId] = it }
                    _productDetailsMap.value = current
                    Log.d(TAG, "Loaded ${list.size} VIP INAPP products from Google Play: ${list.map { it.productId }}")
                }
            }
        }

        // 3. VIP Dressing Suites as SUBS (if created as Subscriptions in Play Console)
        val vipSubList = vipProductIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id.trim())
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(vipSubList).build()
        ) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list = queryProductDetailsResult.productDetailsList ?: emptyList()
                if (list.isNotEmpty()) {
                    val current = _productDetailsMap.value.toMutableMap()
                    list.forEach { current[it.productId] = it }
                    _productDetailsMap.value = current
                    Log.d(TAG, "Loaded ${list.size} VIP SUBS products from Google Play: ${list.map { it.productId }}")
                }
            }
        }
    }

    /**
     * Launch Google Play Billing Purchase Flow for the specified CoinPackageItem.
     */
    fun launchPurchaseFlow(
        activity: Activity,
        currentUser: UserProfile,
        packageItem: CoinPackageItem,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val targetActivity = if (!activity.isFinishing && !activity.isDestroyed) {
            activity
        } else {
            MainActivity.currentActivity ?: activity
        }

        val client = billingClient
        if (client == null || !client.isReady) {
            initialize(targetActivity)
            coroutineScope.launch {
                var attempts = 0
                while ((billingClient == null || billingClient?.isReady != true) && attempts < 15) {
                    delay(250)
                    attempts++
                }
                withContext(Dispatchers.Main) {
                    val readyClient = billingClient
                    if (readyClient != null && readyClient.isReady) {
                        launchPurchaseFlow(targetActivity, currentUser, packageItem, onSuccess, onError)
                    } else {
                        onError("Google Play Store is currently not connected. Please check your internet or Google account.")
                    }
                }
            }
            return
        }

        currentPurchasingUserId = currentUser.uid
        currentPurchasingPackage = packageItem
        onPurchaseSuccessCallback = onSuccess
        onPurchaseErrorCallback = onError

        val cleanId = packageItem.id.trim()
        // Match product details from Google Play query
        val productDetails = _productDetailsMap.value[cleanId]
        if (productDetails == null) {
            Log.w(TAG, "ProductDetails for $cleanId not yet cached. Querying directly from Google Play...")
            val isVip = cleanId.startsWith("vip_package_")
            val primaryType = if (isVip) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP
            val fallbackType = if (isVip) BillingClient.ProductType.INAPP else BillingClient.ProductType.SUBS

            val singlePrimaryList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(cleanId)
                    .setProductType(primaryType)
                    .build()
            )
            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(singlePrimaryList).build()
            ) { result, queryProductDetailsResult ->
                val details = queryProductDetailsResult.productDetailsList?.firstOrNull()
                val primaryUnfetched = queryProductDetailsResult.unfetchedProductList?.firstOrNull()
                if (result.responseCode == BillingClient.BillingResponseCode.OK && details != null) {
                    val updated = _productDetailsMap.value.toMutableMap()
                    updated[cleanId] = details
                    _productDetailsMap.value = updated
                    launchBillingInternal(targetActivity, details, currentUser)
                } else {
                    // Fallback attempt with alternate product type
                    val singleFallbackList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(cleanId)
                            .setProductType(fallbackType)
                            .build()
                    )
                    client.queryProductDetailsAsync(
                        QueryProductDetailsParams.newBuilder().setProductList(singleFallbackList).build()
                    ) { fallbackResult, fallbackQueryProductDetailsResult ->
                        val fallbackDetails = fallbackQueryProductDetailsResult.productDetailsList?.firstOrNull()
                        if (fallbackResult.responseCode == BillingClient.BillingResponseCode.OK && fallbackDetails != null) {
                            val updated = _productDetailsMap.value.toMutableMap()
                            updated[cleanId] = fallbackDetails
                            _productDetailsMap.value = updated
                            launchBillingInternal(targetActivity, fallbackDetails, currentUser)
                        } else {
                            coroutineScope.launch(Dispatchers.Main) {
                                val unfetchedStatus = primaryUnfetched?.statusCode
                                val unfetchedHint = when (unfetchedStatus) {
                                    UnfetchedProduct.StatusCode.PRODUCT_NOT_FOUND -> "PRODUCT_NOT_FOUND"
                                    UnfetchedProduct.StatusCode.NO_ELIGIBLE_OFFER -> "NO_ELIGIBLE_OFFER"
                                    UnfetchedProduct.StatusCode.INVALID_PRODUCT_ID_FORMAT -> "INVALID_ID"
                                    else -> unfetchedStatus?.toString() ?: "EMPTY"
                                }
                                val errorDetail = when (result.responseCode) {
                                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
                                        "Product ($cleanId) is unavailable in Google Play Store."
                                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                                        "Google Play Billing is unavailable. Ensure you are signed into Google Play Store on this device."
                                    BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                                        "Google Play configuration error (Code 5: DEVELOPER_ERROR for $cleanId)."
                                    else ->
                                        "Product not available in Google Play Store ($cleanId) [Code: ${result.responseCode}, Status: $unfetchedHint]. If testing via APK, upload this version to Play Console Internal/Closed Testing."
                                }
                                Log.e(TAG, "Product query failed for $cleanId: primaryCode=${result.responseCode} (${result.debugMessage}), primaryUnfetched=$unfetchedStatus, fallbackCode=${fallbackResult.responseCode}")
                                onError(errorDetail)
                            }
                        }
                    }
                }
            }
            return
        }

        launchBillingInternal(targetActivity, productDetails, currentUser)
    }

    private fun launchBillingInternal(
        activity: Activity,
        productDetails: ProductDetails,
        currentUser: UserProfile
    ) {
        coroutineScope.launch(Dispatchers.Main) {
            val client = billingClient ?: return@launch
            val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)

            // Billing Library 8.0+: Pass offerToken for BOTH Subscriptions AND One-Time Products with Purchase Options & Offers!
            if (productDetails.productType == BillingClient.ProductType.SUBS) {
                val offerToken = productDetails.subscriptionOfferDetails
                    ?.firstOrNull { !it.offerToken.isNullOrBlank() }
                    ?.offerToken
                if (!offerToken.isNullOrBlank()) {
                    productDetailsParamsBuilder.setOfferToken(offerToken)
                }
            } else {
                val oneTimeOfferToken = productDetails.oneTimePurchaseOfferDetailsList
                    ?.firstOrNull { !it.offerToken.isNullOrBlank() }
                    ?.offerToken
                    ?: productDetails.oneTimePurchaseOfferDetails?.offerToken
                if (!oneTimeOfferToken.isNullOrBlank()) {
                    productDetailsParamsBuilder.setOfferToken(oneTimeOfferToken)
                }
            }

            val productDetailsParamsList = listOf(productDetailsParamsBuilder.build())

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .setObfuscatedAccountId(currentUser.uid)
                .build()

            val targetAct = if (!activity.isFinishing && !activity.isDestroyed) activity else (MainActivity.currentActivity ?: activity)
            val flowResult = client.launchBillingFlow(targetAct, billingFlowParams)
            if (flowResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e(TAG, "Failed to launch billing flow. Code: ${flowResult.responseCode} - ${flowResult.debugMessage}")
                onPurchaseErrorCallback?.invoke("Could not open Google Play payment dialog (Code: ${flowResult.responseCode}${if (flowResult.debugMessage.isNotBlank()) ": ${flowResult.debugMessage}" else ""})")
            }
        }
    }

    /**
     * Listener callback invoked by Google Play Billing when purchases are updated.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled the purchase.")
                onPurchaseErrorCallback?.invoke("Purchase cancelled.")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.w(TAG, "Item already owned. Consuming and completing verification...")
                queryAndConsumePurchases()
            }
            else -> {
                Log.e(TAG, "Purchase failed: ${billingResult.responseCode} - ${billingResult.debugMessage}")
                onPurchaseErrorCallback?.invoke(billingResult.debugMessage.ifBlank { "Payment could not be completed (Code: ${billingResult.responseCode})" })
            }
        }
    }

    /**
     * Handles successful purchase: Verifies state, grants items in Firestore, and consumes purchase.
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            coroutineScope.launch {
                // 1. Credit User in Firebase Firestore
                val userId = currentPurchasingUserId.ifBlank { purchase.accountIdentifiers?.obfuscatedAccountId ?: "" }
                val pkg = currentPurchasingPackage

                if (userId.isNotBlank()) {
                    creditUserPurchaseInFirestore(userId, purchase, pkg)
                }

                // 2. Acknowledge VIP Subscriptions vs Consume One-Time Diamond Packs
                val isSubscriptionPurchase = pkg?.id?.startsWith("vip_package_") == true ||
                    purchase.products.any { it.startsWith("vip_package_") }
                if (isSubscriptionPurchase) {
                    acknowledgeSubscriptionPurchase(purchase)
                } else {
                    consumePurchase(purchase)
                }

                withContext(Dispatchers.Main) {
                    onPurchaseSuccessCallback?.invoke()
                    onPurchaseSuccessCallback = null
                    onPurchaseErrorCallback = null
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase is pending user approval (e.g. offline bank/cash payment at convenience store).")
        }
    }

    private suspend fun acknowledgeSubscriptionPurchase(purchase: Purchase) {
        val client = billingClient ?: return
        if (purchase.isAcknowledged) return
        val ackParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val ackResult = client.acknowledgePurchase(ackParams)
        if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.i(TAG, "Subscription acknowledged successfully: ${purchase.purchaseToken}")
        } else {
            Log.w(TAG, "Failed to acknowledge subscription: ${ackResult.responseCode} - ${ackResult.debugMessage}")
        }
    }

    private suspend fun consumePurchase(purchase: Purchase) {
        val client = billingClient ?: return
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val consumeResult = client.consumePurchase(consumeParams)
        if (consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.i(TAG, "Purchase consumed successfully: ${purchase.purchaseToken}")
        } else {
            Log.w(TAG, "Failed to consume purchase: ${consumeResult.billingResult.responseCode}")
        }
    }

    /**
     * Queries existing active purchases and consumes any unconsumed items.
     */
    fun queryAndConsumePurchases() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                for (p in purchases) {
                    if (p.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        coroutineScope.launch {
                            consumePurchase(p)
                        }
                    }
                }
            }
        }
    }

    /**
     * Credits Diamonds or Host Monetization to the User's Firestore account securely.
     */
    private suspend fun creditUserPurchaseInFirestore(
        userId: String,
        purchase: Purchase,
        packageItem: CoinPackageItem?
    ) = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)

            val isMonetization = packageItem?.id == "pkg_host_monetization_599" ||
                    packageItem?.title?.contains("Monetization", ignoreCase = true) == true ||
                    purchase.products.contains("pkg_host_monetization_599")

            val vipLevel = when {
                packageItem?.id?.startsWith("vip_package_") == true -> packageItem.id.removePrefix("vip_package_").toIntOrNull() ?: 1
                purchase.products.any { it.startsWith("vip_package_") } -> purchase.products.first { it.startsWith("vip_package_") }.removePrefix("vip_package_").toIntOrNull() ?: 1
                else -> 0
            }

            val diamondsToAdd = packageItem?.coins ?: when {
                purchase.products.contains("pack_49") -> 15000L
                purchase.products.contains("pack_59") -> 20000L
                purchase.products.contains("pack_149") -> 60000L
                purchase.products.contains("pack_299") -> 135000L
                purchase.products.contains("pack_599") -> 310000L
                purchase.products.contains("pack_999") -> 600000L
                purchase.products.contains("pack_1999") -> 1500000L
                else -> 0L
            }

            val batch = db.batch()

            if (isMonetization) {
                batch.update(
                    userRef,
                    mapOf(
                        "isHostMonetized" to true,
                        "profile_is_host_monetized" to true,
                        "hostMonetizationActivatedAt" to System.currentTimeMillis()
                    )
                )
            } else if (vipLevel in 1..5) {
                val now = System.currentTimeMillis()
                val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
                val expiry = now + thirtyDaysMs
                val vipUpdates = getVipSuiteUpdates(vipLevel, equip = true).toMutableMap()
                vipUpdates["vip_level"] = vipLevel
                vipUpdates["profile_vip_level"] = vipLevel
                vipUpdates["profile_plan"] = "VIP $vipLevel"
                vipUpdates["vip_expires_at"] = expiry
                vipUpdates["hasRecharged"] = true
                vipUpdates["vipExpiries.vip_$vipLevel"] = expiry
                batch.update(userRef, vipUpdates)
            } else if (diamondsToAdd > 0) {
                batch.update(
                    userRef,
                    mapOf(
                        "diamonds" to FieldValue.increment(diamondsToAdd),
                        "coins" to FieldValue.increment(diamondsToAdd),
                        "hasRecharged" to true,
                        "rechargeCount" to FieldValue.increment(1)
                    )
                )
            }

            // Record official Google Play Transaction in subcollection
            val txRef = userRef.collection("earning_transactions").document()
            val txData = mapOf(
                "id" to txRef.id,
                "title" to (packageItem?.title ?: "Google Play Purchase"),
                "amountInr" to (packageItem?.numericPrice?.toDoubleOrNull() ?: 0.0),
                "diamonds" to diamondsToAdd,
                "senderName" to "Google Play Billing",
                "upiId" to "Google Play Order: ${purchase.orderId ?: purchase.purchaseToken.take(16)}",
                "timestamp" to System.currentTimeMillis(),
                "orderId" to (purchase.orderId ?: ""),
                "purchaseToken" to purchase.purchaseToken,
                "purchaseTime" to purchase.purchaseTime,
                "type" to "TOP_UP_GOOGLE_PLAY",
                "status" to "SUCCESS"
            )
            batch.set(txRef, txData)

            // Record in global payment requests for admin transparency
            val globalReqRef = db.collection("payment_requests").document("GP_${purchase.orderId ?: System.currentTimeMillis()}")
            val globalData = mapOf(
                "requestId" to globalReqRef.id,
                "userId" to userId,
                "userName" to (packageItem?.title ?: "Google Play In-App Purchase"),
                "coinsRequested" to diamondsToAdd,
                "amountPaid" to (packageItem?.priceInr ?: "Google Play"),
                "status" to "APPROVED",
                "approvedBy" to "GOOGLE_PLAY_BILLING_AUTO",
                "category" to if (isMonetization) "Host Monetization (Google Play)" else "Diamond Recharge (Google Play)",
                "orderId" to (purchase.orderId ?: ""),
                "timestamp" to System.currentTimeMillis()
            )
            batch.set(globalReqRef, globalData)

            batch.commit()
            Log.i(TAG, "Successfully credited Google Play purchase in Firestore for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error crediting Google Play purchase in Firestore: ${e.message}", e)
        }
    }
}
