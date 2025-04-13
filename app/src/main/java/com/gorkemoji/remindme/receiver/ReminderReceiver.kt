package com.gorkemoji.remindme.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.gorkemoji.remindme.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = intent.getIntExtra("notificationID", 0)

        val channel = NotificationChannel("reminder_channel", "Reminder Channel", NotificationManager.IMPORTANCE_HIGH).apply { description = "This notification contains important announcements, etc." }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setContentTitle(context.getString(R.string.reminder))
            .setContentText(context.getString(R.string.its_time))
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationID, notification)
    }
}
