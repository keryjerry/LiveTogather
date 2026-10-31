package com.devil.livetogather

import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
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
        require(recipientUids.isNotEmpty()) { "A gift needs at least one recipient." }
        val request = mapOf(
            "roomId" to roomId,
            "giftId" to giftId,
            "recipientUids" to recipientUids.distinct(),
            "quantity" to quantity,
            "fromBackpack" to fromBackpack,
            "targetMicSeatIndex" to targetMicSeatIndex,
            "eventId" to UUID.randomUUID().toString()
        )

        return FirebaseFunctions.getInstance("us-central1")
            .getHttpsCallable("sendLiveGift")
            .call(request)
            .continueWith { task ->
                val response = task.result?.data as? Map<*, *>
                    ?: throw IllegalStateException("Gift service returned an invalid response.")
                val eventData = response["event"] as? Map<*, *>
                    ?: throw IllegalStateException("Gift service did not return the saved gift event.")
                val senderDiamonds = (response["senderDiamonds"] as? Number)?.toLong()
                GiftSendResult(
                    event = eventData.toGiftEvent(),
                    senderDiamonds = senderDiamonds,
                    duplicate = response["duplicate"] as? Boolean ?: false
                )
            }
    }

    private fun Map<*, *>.toGiftEvent(): GiftEvent {
        fun text(key: String) = this[key] as? String ?: ""
        fun number(key: String) = (this[key] as? Number)?.toLong() ?: 0L

        return GiftEvent(
            id = text("id"),
            roomId = text("roomId"),
            senderId = text("senderId"),
            senderName = text("senderName"),
            senderPhoto = text("senderPhoto"),
            giftId = text("giftId"),
            giftName = text("giftName"),
            giftEmoji = text("giftEmoji").ifBlank { "🎁" },
            cost = number("cost"),
            animationType = text("animationType"),
            timestamp = number("timestamp"),
            targetUserId = text("targetUserId"),
            targetUserName = text("targetUserName"),
            quantity = (this["quantity"] as? Number)?.toInt() ?: 1,
            targetMicSeatIndex = (this["targetMicSeatIndex"] as? Number)?.toInt() ?: -1,
            fromBackpack = this["fromBackpack"] as? Boolean ?: false,
            receiverUserIds = (this["receiverUserIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            receiverNames = (this["receiverNames"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            receiverDiamondPayout = number("receiverDiamondPayout"),
            receiverEarningsInr = (this["receiverEarningsInr"] as? Number)?.toDouble() ?: 0.0,
            receiverRate = (this["receiverRate"] as? Number)?.toDouble() ?: 0.0
        )
    }
}

data class GiftSendResult(
    val event: GiftEvent,
    val senderDiamonds: Long?,
    val duplicate: Boolean
)
