package com.gorkemoji.remindme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = 1

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel("reminder_channel", "Reminder Channel", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "This notification contains important announcement, etc."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setContentTitle(R.string.reminder.toString())
            .setContentText(R.string.its_time.toString())
            .setSmallIcon(R.drawable.ic_alarm_flat)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(notificationID, notification)
    }
}
