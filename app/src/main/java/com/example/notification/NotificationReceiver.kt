package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            // Re-schedule all alarms after phone reboot
            NotificationHelper.rescheduleAllAlarms(context)
            return
        }

        // Alarm fired
        val prefs = NotificationHelper.getPreferences(context)
        if (prefs.isEnabled) {
            val reminder = PhysicsReminderData.getRandomReminder()
            NotificationHelper.showDailyNotification(context, reminder)
            // Schedule next day occurrence for exact alarms
            NotificationHelper.rescheduleAllAlarms(context)
        }
    }
}
