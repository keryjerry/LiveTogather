package com.devil.livetogather

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

object AppNotificationHelper {
    const val CHANNEL_GENERAL = "livetogather_general_alerts_v2"
    const val CHANNEL_LIVE = "livetogather_live_alerts_v2"
    const val CHANNEL_PURCHASE = "livetogather_purchase_alerts_v2"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val liveChannel = NotificationChannel(
                CHANNEL_LIVE,
                "LiveTogather Room Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pop-up alerts with sound when followed hosts or friends go live"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 200, 350)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableLights(true)
            }

            val purchaseChannel = NotificationChannel(
                CHANNEL_PURCHASE,
                "LiveTogather Wallet & Inventory",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Receipts and confirmations for Diamonds & frame purchases"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableLights(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "LiveTogather Activity & Social",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pop-up alerts with sound for likes, follows, room invites and messages"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableLights(true)
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

        // Ensure title and message always prominently mention LiveTogather
        val brandedTitle = if (title.contains("LiveTogather", ignoreCase = true)) {
            title
        } else {
            "LiveTogather • $title"
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(brandedTitle)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 350, 200, 350))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, false)
            .build()

        nm.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}
