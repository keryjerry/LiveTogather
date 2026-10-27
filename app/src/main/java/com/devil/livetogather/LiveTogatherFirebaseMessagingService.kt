package com.devil.livetogather

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LiveTogatherFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: " + remoteMessage.from)

        val data = remoteMessage.data
        val roomId = data["roomId"] ?: data["room_id"] ?: ""

        val rawTitle = remoteMessage.notification?.title
            ?: data["title"]
            ?: "Live Room Alert!"
        val rawBody = remoteMessage.notification?.body
            ?: data["body"]
            ?: data["message"]
            ?: "Greetings from LiveTogather! A host you follow is now live. Tap to join!"

        val isLive = rawTitle.contains("LIVE", ignoreCase = true) || rawBody.contains("LIVE", ignoreCase = true)
        val isPurchase = rawTitle.contains("Diamond", ignoreCase = true) || rawTitle.contains("Coin", ignoreCase = true)

        // Show loud sound + vibrating pop-up notification even when app is closed / in background
        AppNotificationHelper.showSystemNotification(
            context = applicationContext,
            title = rawTitle,
            message = rawBody,
            roomId = roomId.ifBlank { null },
            isLiveAlert = isLive,
            isPurchaseAlert = isPurchase
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Registration Token: " + token)
        val prefs = getSharedPreferences("livetogather_user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        val uid = prefs.getString("user_uid", "") ?: ""
        if (uid.isNotBlank()) {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("users").document(uid).update("fcmToken", token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload new FCM token: " + e.message)
            }
        }
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
