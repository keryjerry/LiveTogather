package com.devil.livetogather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object AppNotificationHelper {
    const val CHANNEL_GENERAL = "livetogather_general_alerts"
    const val CHANNEL_LIVE = "livetogather_live_alerts"
    const val CHANNEL_PURCHASE = "livetogather_purchase_alerts"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val liveChannel = NotificationChannel(
                CHANNEL_LIVE,
                "Live Room Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when followed hosts or friends go live"
                enableVibration(true)
            }

            val purchaseChannel = NotificationChannel(
                CHANNEL_PURCHASE,
                "Purchases & Inventory",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Receipts and confirmations for Diamond & frame purchases"
                enableVibration(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "Activity & Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General room invites, direct messages, and profile alerts"
                enableVibration(true)
            }

            nm.createNotificationChannel(liveChannel)
            nm.createNotificationChannel(purchaseChannel)
            nm.createNotificationChannel(generalChannel)
        }
    }

    fun showSystemNotification(
        context: Context,
        title: String,
        message: String,
        roomId: String? = null,
        isLiveAlert: Boolean = false,
        isPurchaseAlert: Boolean = false
    ) {
        ensureChannels(context)
        val channelId = when {
            isLiveAlert -> CHANNEL_LIVE
            isPurchaseAlert -> CHANNEL_PURCHASE
            else -> CHANNEL_GENERAL
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!roomId.isNullOrBlank()) {
                putExtra("ROOM_ID", roomId)
                putExtra("extra_room_id", roomId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(if (isLiveAlert) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}
