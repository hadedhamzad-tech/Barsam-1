package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.util.PersianUtils
import java.util.Calendar

data class ReminderTime(
    val hour: Int,
    val minute: Int
) {
    fun toPersianFormatted(): String {
        val hStr = hour.toString().padStart(2, '0')
        val mStr = minute.toString().padStart(2, '0')
        return "${PersianUtils.toPersianDigits(hStr)}:${PersianUtils.toPersianDigits(mStr)}"
    }

    fun toTimeString(): String {
        val hStr = hour.toString().padStart(2, '0')
        val mStr = minute.toString().padStart(2, '0')
        return "$hStr:$mStr"
    }

    companion object {
        fun fromString(str: String): ReminderTime? {
            val parts = str.split(":")
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull()
                val m = parts[1].toIntOrNull()
                if (h != null && m != null && h in 0..23 && m in 0..59) {
                    return ReminderTime(h, m)
                }
            }
            return null
        }
    }
}

data class ReminderPreferences(
    val isEnabled: Boolean = true,
    val times: List<ReminderTime> = listOf(ReminderTime(9, 0))
)

object NotificationHelper {
    const val CHANNEL_ID = "physics_daily_reminders"
    const val NOTIFICATION_ID = 1001
    private const val PREFS_NAME = "physics_notification_prefs"
    private const val KEY_ENABLED = "daily_reminder_enabled"
    private const val KEY_TIMES = "daily_reminder_times_csv"

    const val MAX_REMINDERS = 3

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "یادآور روزانه برسام و فیزیک"
            val descriptionText = "یادآوری فرمول‌های کتاب درسی و حل مسائل کوتاه فیزیک"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDailyNotification(context: Context, reminder: PhysicsReminder? = null) {
        createNotificationChannel(context)

        val item = reminder ?: PhysicsReminderData.getRandomReminder()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("opened_from_reminder", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigText = buildString {
            append("📐 فرمول / عبارت:\n")
            append(item.formulaOrProblem)
            append("\n\n💡 نکته و تحلیل:\n")
            append(item.explanation)
            append("\n\n📚 بخش: ")
            append(item.category)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(item.title)
            .setContentText("${item.formulaOrProblem} — ${item.explanation}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Android 13 permission not granted
        }
    }

    /**
     * Reschedules all configured alarms.
     * Handles exact alarms where allowed, and resilient daily repeat.
     */
    fun rescheduleAllAlarms(context: Context) {
        val prefs = getPreferences(context)
        // First cancel any existing alarms (slots 0, 1, 2)
        cancelAllAlarms(context, updatePrefs = false)

        if (!prefs.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        prefs.times.take(MAX_REMINDERS).forEachIndexed { index, time ->
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.example.physicslab.ACTION_REMINDER_ALARM"
                putExtra("slot_index", index)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                100 + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, time.hour)
                set(Calendar.MINUTE, time.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
            } catch (_: SecurityException) {
                try {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } catch (_: Exception) { }
            } catch (_: Exception) { }
        }
    }

    fun cancelAllAlarms(context: Context, updatePrefs: Boolean = true) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        for (index in 0 until MAX_REMINDERS) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.example.physicslab.ACTION_REMINDER_ALARM"
                putExtra("slot_index", index)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                100 + index,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        if (updatePrefs) {
            val current = getPreferences(context)
            savePreferences(context, current.copy(isEnabled = false))
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    fun savePreferences(context: Context, prefs: ReminderPreferences) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val csv = prefs.times.take(MAX_REMINDERS).joinToString(",") { it.toTimeString() }
        sp.edit()
            .putBoolean(KEY_ENABLED, prefs.isEnabled)
            .putString(KEY_TIMES, csv)
            .apply()
    }

    fun getPreferences(context: Context): ReminderPreferences {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = sp.getBoolean(KEY_ENABLED, true)
        val csv = sp.getString(KEY_TIMES, null)

        val times = if (!csv.isNullOrEmpty()) {
            csv.split(",")
                .mapNotNull { ReminderTime.fromString(it.trim()) }
                .take(MAX_REMINDERS)
        } else {
            // Backward compatibility with single-hour keys:
            val legacyHour = sp.getInt("daily_reminder_hour", 9)
            val legacyMinute = sp.getInt("daily_reminder_minute", 0)
            listOf(ReminderTime(legacyHour, legacyMinute))
        }

        val effectiveTimes = if (times.isEmpty()) listOf(ReminderTime(9, 0)) else times
        return ReminderPreferences(isEnabled = isEnabled, times = effectiveTimes)
    }
}
