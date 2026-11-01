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

        // 2. Execute 100% Client-Side Atomic Firestore Transaction
        return db.runTransaction { transaction ->
            val senderDocRef = db.collection("users").document(senderUid)
            val senderSnapshot = transaction.get(senderDocRef)

            val senderName = senderSnapshot.getString("name") ?: "Member"
            val senderPhoto = senderSnapshot.getString("photo") ?: ""
            val currentDiamonds = senderSnapshot.getLong("diamonds") ?: 0L
            val currentWealthXp = senderSnapshot.getLong("wealthXp") ?: 0L

            var newSenderDiamonds = currentDiamonds

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
                transaction.update(senderDocRef, "backpack", backpack)
            } else {
                // Check diamond balance
                if (currentDiamonds < totalCost) {
                    throw IllegalStateException("Insufficient diamonds! Needed: $totalCost 💎, Available: $currentDiamonds 💎")
                }
                newSenderDiamonds = currentDiamonds - totalCost
                val xpEarned = totalCost * 10L
                val newWealthXp = currentWealthXp + xpEarned
                val newWealthLevel = LevelBadgeSystem.calculateLevelFromXp(newWealthXp)

                val senderUpdates = mapOf<String, Any>(
                    "diamonds" to newSenderDiamonds,
                    "wealthXp" to newWealthXp,
                    "wealthLevel" to newWealthLevel
                )
                transaction.update(senderDocRef, senderUpdates)
            }

            // 3. Process Each Recipient (Charm XP & Monetization / Commission)
            val recipientNames = mutableListOf<String>()
            var totalHostCommission = 0.0

            distinctRecipients.forEach { recUid ->
                val recDocRef = db.collection("users").document(recUid)
                val recSnap = transaction.get(recDocRef)
                val recName = recSnap.getString("name") ?: "Host"
                recipientNames.add(recName)

                val curCharmXp = recSnap.getLong("charmXp") ?: 0L
                val charmEarned = (unitCost * quantity * 10L)
                val newCharmXp = curCharmXp + charmEarned
                val newCharmLvl = LevelBadgeSystem.calculateLevelFromXp(newCharmXp)

                val recUpdates = mutableMapOf<String, Any>(
                    "charmXp" to newCharmXp,
                    "charmLevel" to newCharmLvl
                )

                // Host commission if monetized
                val isMonetized = recSnap.getBoolean("isHostMonetized") ?: false
                if (isMonetized && !fromBackpack && unitCost > 0) {
                    val recWealthLvl = (recSnap.getLong("wealthLevel") ?: 1L).toInt()
                    val commPercent = LevelBadgeSystem.getHostCommissionPercent(recWealthLvl)
                    val giftInr = (unitCost * quantity) * 0.01
                    val earnings = giftInr * (commPercent / 100.0)
                    totalHostCommission += earnings
                    recUpdates["earningsInr"] = FieldValue.increment(earnings)
                    recUpdates["totalEarningsInr"] = FieldValue.increment(earnings)
                }

                transaction.update(recDocRef, recUpdates)
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
