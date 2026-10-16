package com.devil.livetogather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LiveTogatherFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: " + remoteMessage.from)

        val data = remoteMessage.data
        val roomId = data["roomId"] ?: data["room_id"] ?: ""

        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: "🔴 Live Room Alert!"
        val body = remoteMessage.notification?.body
            ?: data["body"]
            ?: data["message"]
            ?: "A host you follow is now live! Tap to join."

        sendSystemNotification(title, body, roomId)
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

    private fun sendSystemNotification(title: String, body: String, roomId: String) {
        val channelId = "livetogather_live_alerts"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Live Room Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when followed hosts go live"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ROOM_ID", roomId)
            putExtra("extra_room_id", roomId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
