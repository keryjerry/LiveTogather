package com.devil.livetogather

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID

object GiftTransactionService {
    fun sendGift(
        roomId: String,
        giftId: String,
        recipientUids: List<String>,
        quantity: Int,
        fromBackpack: Boolean,
        targetMicSeatIndex: Int = -1
    ): Task<GiftSendResult> {
        val distinctRecipients = recipientUids.filter { it.isNotBlank() }.distinct()
        require(distinctRecipients.isNotEmpty()) { "A gift needs at least one recipient." }

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val senderUid = auth.currentUser?.uid ?: ""
        if (senderUid.isBlank()) {
            return Tasks.forException(IllegalStateException("User is not signed in."))
        }

        // 1. Resolve gift metadata directly from local catalogs (zero network lag)
        val roomGift = ROOM_GIFT_CATALOG.find { it.id == giftId }
        val videoGift = VIDEO_GIFT_CATALOG.find { it.id == giftId }
        val giftName = roomGift?.name ?: videoGift?.name ?: "Luxury Gift"
        val giftEmoji = roomGift?.emoji ?: videoGift?.emoji ?: "🎁"
        val unitCost = roomGift?.cost ?: videoGift?.cost ?: 1L
        val animType = roomGift?.animationType ?: videoGift?.animationType ?: "micro_gift"
        val drawableResId = roomGift?.drawableResId ?: videoGift?.thumbnailResId ?: 0

        val totalCost = if (fromBackpack) 0L else unitCost * quantity * distinctRecipients.size
        val eventId = UUID.randomUUID().toString()
        val notificationsToDispatch = mutableListOf<NotificationItem>()
        val creatorHistoryToDispatch = mutableListOf<Map<String, Any>>()

        // 2. Execute 100% Client-Side Atomic Firestore Transaction
        // FIRESTORE RULE: ALL READS (transaction.get) MUST BE EXECUTED BEFORE ANY WRITES (update/set)
        return db.runTransaction { transaction ->
            // --- STEP A: READ SENDER ---
            val senderDocRef = db.collection("users").document(senderUid)
            val senderSnapshot = transaction.get(senderDocRef)

            // --- STEP B: READ ALL RECIPIENTS (READS MUST PRECEDE ALL WRITES) ---
            val recipientDataList = distinctRecipients.map { recUid ->
                val recDocRef = db.collection("users").document(recUid)
                val recSnap = transaction.get(recDocRef)
                Triple(recUid, recDocRef, recSnap)
            }

            // --- STEP C: PROCESS SENDER DATA & BALANCE CHECKS ---
            val senderName = senderSnapshot.getString("name") ?: "Member"
            val senderPhoto = senderSnapshot.getString("photo") ?: ""
            val currentDiamonds = senderSnapshot.getLong("diamonds")
                ?: senderSnapshot.getLong("coins")
                ?: 0L
            val currentWealthXp = senderSnapshot.getLong("wealthXp") ?: 0L

            var newSenderDiamonds = currentDiamonds
            var senderBackpackToUpdate: Map<String, Int>? = null
            var senderFieldUpdates: Map<String, Any>? = null

            if (fromBackpack) {
                // Deduct from sender's backpack inventory
                val rawBackpack = senderSnapshot.get("backpack") as? Map<*, *> ?: emptyMap<Any, Any>()
                val backpack = rawBackpack.entries.associate { (k, v) ->
                    k.toString() to ((v as? Number)?.toInt() ?: 0)
                }.toMutableMap()

                val availableCount = backpack[giftId] ?: 0
                val neededCount = quantity * distinctRecipients.size
                if (availableCount < neededCount) {
                    throw IllegalStateException("Not enough gifts in backpack! Owned: $availableCount, Needed: $neededCount")
                }
                backpack[giftId] = availableCount - neededCount
                senderBackpackToUpdate = backpack
            } else {
                // Check diamond balance
                if (currentDiamonds < totalCost) {
                    throw IllegalStateException("Insufficient diamonds! Needed: $totalCost 💎, Available: $currentDiamonds 💎")
                }
                newSenderDiamonds = currentDiamonds - totalCost
                val xpEarned = totalCost * 10L
                val newWealthXp = currentWealthXp + xpEarned
                val oldWealthLvl = (senderSnapshot.getLong("wealthLevel") ?: 1L).toInt()
                val newWealthLevel = LevelBadgeSystem.calculateLevelFromXp(newWealthXp)

                senderFieldUpdates = mapOf<String, Any>(
                    "diamonds" to newSenderDiamonds,
                    "wealthXp" to newWealthXp,
                    "wealthLevel" to newWealthLevel,
                    "level" to newWealthLevel,
                    "activeWealthBadgeId" to LevelBadgeSystem.getTierBadgeId(newWealthLevel)
                )
                if (newWealthLevel > oldWealthLvl) {
                    val lvlNotifId = UUID.randomUUID().toString()
                    notificationsToDispatch.add(
                        NotificationItem(
                            id = lvlNotifId,
                            targetUserId = senderUid,
                            senderId = "system_livetogather",
                            senderName = "LiveTogather Official",
                            senderPhoto = "",
                            type = "level_up",
                            title = "⭐ Level Up! Wealth Lv.$newWealthLevel",
                            message = "Congratulations! You leveled up to Wealth Level $newWealthLevel (${LevelBadgeSystem.getLevelTierInfo(newWealthLevel).tierName}). New level badges unlocked!",
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )
                    )
                }
            }

            // --- STEP D: PROCESS RECIPIENT DATA & CALCULATE COMMISSIONS ---
            val recipientNames = mutableListOf<String>()
            var totalHostCommission = 0.0
            val recipientWriteList = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Map<String, Any>>>()
            notificationsToDispatch.clear()
            creatorHistoryToDispatch.clear()

            recipientDataList.forEach { (recUid, recDocRef, recSnap) ->
                val recName = recSnap.getString("name") ?: "Host"
                recipientNames.add(recName)

                val oldCharmLvl = (recSnap.getLong("charmLevel") ?: 1L).toInt()
                val curCharmXp = recSnap.getLong("charmXp") ?: 0L
                val charmEarned = (unitCost * quantity * 10L)
                val newCharmXp = curCharmXp + charmEarned
                val newCharmLvl = LevelBadgeSystem.calculateLevelFromXp(newCharmXp)

                val recUpdates = mutableMapOf<String, Any>(
                    "charmXp" to newCharmXp,
                    "charmLevel" to newCharmLvl
                )
                if (newCharmLvl > oldCharmLvl) {
                    val charmLvlNotifId = UUID.randomUUID().toString()
                    notificationsToDispatch.add(
                        NotificationItem(
                            id = charmLvlNotifId,
                            targetUserId = recUid,
                            senderId = "system_livetogather",
                            senderName = "LiveTogather Official",
                            senderPhoto = "",
                            type = "level_up",
                            title = "🌸 Level Up! Charm Lv.$newCharmLvl",
                            message = "Congratulations! You reached Charm Level $newCharmLvl (${LevelBadgeSystem.getLevelTierInfo(newCharmLvl).tierName})!",
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )
                    )
                }

                val grossDiamonds = unitCost * quantity
                val isMonetized = recSnap.getBoolean("isHostMonetized") ?: false

                if (isMonetized && !fromBackpack && unitCost > 0) {
                    // 1. Monetized Creator: 5% to 8% level-based cut, real INR calculation + diamond credit
                    val recWealthLvl = (recSnap.getLong("wealthLevel") ?: 1L).toInt()
                    val commPercent = LevelBadgeSystem.getHostCommissionPercent(recWealthLvl)
                    val creatorDiamonds = (grossDiamonds * (commPercent / 100.0)).toLong()
                    val giftInr = grossDiamonds * 0.01
                    val earnings = giftInr * (commPercent / 100.0)
                    totalHostCommission += earnings
                    recUpdates["earningsInr"] = FieldValue.increment(earnings)
                    recUpdates["totalEarningsInr"] = FieldValue.increment(earnings)
                    recUpdates["diamonds"] = FieldValue.increment(creatorDiamonds)

                    val notifId = UUID.randomUUID().toString()
                    notificationsToDispatch.add(
                        NotificationItem(
                            id = notifId,
                            targetUserId = recUid,
                            senderId = senderUid,
                            senderName = senderName,
                            senderPhoto = senderPhoto,
                            type = "creator_earning",
                            title = "💰 Creator Gift Earning!",
                            message = "$senderName gifted you $quantity x $giftName ($giftEmoji)! You earned ₹${String.format(java.util.Locale.US, "%.2f", earnings)} (${commPercent}% level cut, +$creatorDiamonds 💎).",
                            timestamp = System.currentTimeMillis(),
                            isRead = false,
                            roomId = roomId,
                            giftId = giftId,
                            giftName = giftName,
                            diamondsReceived = creatorDiamonds,
                            earningsInr = earnings,
                            giftEmoji = giftEmoji,
                            quantity = quantity
                        )
                    )

                    creatorHistoryToDispatch.add(
                        mapOf(
                            "id" to notifId,
                            "targetUserId" to recUid,
                            "senderId" to senderUid,
                            "senderName" to senderName,
                            "senderPhoto" to senderPhoto,
                            "giftId" to giftId,
                            "giftName" to giftName,
                            "giftEmoji" to giftEmoji,
                            "quantity" to quantity,
                            "grossDiamonds" to grossDiamonds,
                            "creatorDiamonds" to creatorDiamonds,
                            "commissionPercent" to commPercent,
                            "earningsInr" to earnings,
                            "timestamp" to System.currentTimeMillis(),
                            "roomId" to roomId
                        )
                    )
                } else if (!fromBackpack && unitCost > 0) {
                    // 2. Normal (non-monetized) user: 70% deduction, 30% credited directly to wallet (no percentage shown in message!)
                    val netDiamonds = (grossDiamonds * 0.30).toLong()
                    recUpdates["diamonds"] = FieldValue.increment(netDiamonds)

                    val notifId = UUID.randomUUID().toString()
                    notificationsToDispatch.add(
                        NotificationItem(
                            id = notifId,
                            targetUserId = recUid,
                            senderId = senderUid,
                            senderName = senderName,
                            senderPhoto = senderPhoto,
                            type = "gift_reward",
                            title = "🎁 You Have Been Rewarded!",
                            message = "You received $quantity x $giftName from $senderName and got %,d 💎 in your wallet.".format(netDiamonds),
                            timestamp = System.currentTimeMillis(),
                            isRead = false,
                            roomId = roomId,
                            giftId = giftId,
                            giftName = giftName,
                            diamondsReceived = netDiamonds,
                            earningsInr = 0.0,
                            giftEmoji = giftEmoji,
                            quantity = quantity
                        )
                    )
                }

                recipientWriteList.add(recDocRef to recUpdates)
            }

            // --- STEP E: EXECUTE ALL WRITES (NOW STRICTLY AFTER ALL READS) ---
            if (senderBackpackToUpdate != null) {
                transaction.update(senderDocRef, "backpack", senderBackpackToUpdate)
            } else if (senderFieldUpdates != null) {
                transaction.update(senderDocRef, senderFieldUpdates)
            }

            recipientWriteList.forEach { (recRef, updates) ->
                transaction.update(recRef, updates)
            }

            // 3.5 Record real-time Received Gift on every recipient's profile (users/{recUid}/received_gifts)
            val nowTs = System.currentTimeMillis()
            distinctRecipients.forEach { recUid ->
                val receivedGiftDocId = if (distinctRecipients.size == 1) eventId else "${eventId}_${recUid}"
                val receivedGiftRef = db.collection("users").document(recUid)
                    .collection("received_gifts").document(receivedGiftDocId)
                val perRecipientCost = if (fromBackpack) 0L else (unitCost * quantity)
                transaction.set(
                    receivedGiftRef,
                    mapOf(
                        "id" to receivedGiftDocId,
                        "giftId" to giftId,
                        "giftName" to giftName,
                        "giftEmoji" to giftEmoji,
                        "quantity" to quantity,
                        "cost" to if (distinctRecipients.size == 1 && !fromBackpack) totalCost else perRecipientCost,
                        "diamondValue" to (unitCost * quantity),
                        "senderId" to senderUid,
                        "senderUid" to senderUid,
                        "senderName" to senderName,
                        "senderPhoto" to senderPhoto,
                        "receiverId" to recUid,
                        "roomId" to roomId,
                        "timestamp" to nowTs
                    )
                )
            }

            // 4. Construct Gift Event
            val giftEvent = GiftEvent(
                id = eventId,
                roomId = roomId,
                senderId = senderUid,
                senderName = senderName,
                senderPhoto = senderPhoto,
                giftId = giftId,
                giftName = giftName,
                giftEmoji = giftEmoji,
                cost = unitCost,
                animationType = animType,
                timestamp = System.currentTimeMillis(),
                targetUserId = if (distinctRecipients.size == 1) distinctRecipients[0] else "",
                targetUserName = if (distinctRecipients.size == 1) recipientNames[0] else "All Mics",
                quantity = quantity,
                targetMicSeatIndex = targetMicSeatIndex,
                drawableResId = drawableResId,
                fromBackpack = fromBackpack,
                receiverUserIds = distinctRecipients,
                receiverNames = recipientNames,
                receiverDiamondPayout = 0L,
                receiverEarningsInr = totalHostCommission,
                receiverRate = 0.0
            )

            // 5. Post to rooms/{roomId}/gift_events so all room members immediately receive real-time animation
            val roomGiftEventRef = db.collection("rooms").document(roomId)
                .collection("gift_events").document(eventId)
            transaction.set(roomGiftEventRef, giftEvent)

            GiftSendResult(
                event = giftEvent,
                senderDiamonds = newSenderDiamonds,
                duplicate = false
            )
        }.addOnSuccessListener { result ->
            val event = result.event
            // 1. Post to rooms/{roomId}/live_comments so live room chat immediately displays who gifted whom
            val commentId = UUID.randomUUID().toString()
            val giftComment = LiveRoomComment(
                id = commentId,
                roomId = roomId,
                userId = event.senderId,
                userName = event.senderName,
                userPhoto = event.senderPhoto,
                tier = "Gift",
                message = "🎁 ${event.senderName} sent ${quantity}x $giftName to ${event.targetUserName} ($totalCost 💎)",
                timestamp = System.currentTimeMillis(),
                vipLevel = 0,
                isGiftComment = true,
                giftId = giftId,
                giftName = giftName,
                giftEmoji = giftEmoji,
                giftCost = totalCost,
                giftQuantity = quantity,
                recipientName = event.targetUserName,
                recipientUid = event.targetUserId
            )
            db.collection("rooms").document(roomId)
                .collection("live_comments").document(commentId)
                .set(giftComment)

            // 2. Real-time Official Inbox System Messages (saved to users/{uid}/notifications for Inbox Rewards & Level-Up threads — NOT global notification bar!)
            notificationsToDispatch.forEach { notif ->
                db.collection("users").document(notif.targetUserId)
                    .collection("notifications").document(notif.id)
                    .set(notif)
            }

            // 3. Creator earnings history records (real money INR breakdown)
            creatorHistoryToDispatch.forEach { hist ->
                val targetUid = hist["targetUserId"] as? String ?: ""
                val histId = hist["id"] as? String ?: UUID.randomUUID().toString()
                if (targetUid.isNotBlank()) {
                    db.collection("users").document(targetUid)
                        .collection("gift_earnings_history").document(histId)
                        .set(hist)
                }
            }
        }.addOnFailureListener { error ->
            Log.e("GiftTransactionService", "Direct Firestore gift transaction failed", error)
        }
    }
}

data class GiftSendResult(
    val event: GiftEvent,
    val senderDiamonds: Long?,
    val duplicate: Boolean
)
