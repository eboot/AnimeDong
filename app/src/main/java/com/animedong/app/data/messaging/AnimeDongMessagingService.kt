package com.animedong.app.data.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.animedong.app.MainActivity
import com.animedong.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM: simpan token ke Firestore + tampilkan notifikasi episode baru.
 * Tap notifikasi -> buka MainActivity dengan extra episodeId.
 * (Jadwal pengiriman push-nya jalan di server/cron terpisah, bukan di app.)
 */
class AnimeDongMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("tokens").document(token)
            .set(mapOf("createdAt" to System.currentTimeMillis()))
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        val title = msg.notification?.title ?: msg.data["title"] ?: return
        val body = msg.notification?.body ?: msg.data["body"] ?: ""
        val episodeId = msg.data["episodeId"]

        val intent = Intent(this, MainActivity::class.java).apply {
            episodeId?.let { putExtra("episodeId", it) }
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("episode_baru", "Episode Baru",
                NotificationManager.IMPORTANCE_DEFAULT)
        )
        val notif = NotificationCompat.Builder(this, "episode_baru")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notif)
    }
}
