package com.fourgeailabs.bpwatch.mobile.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fourgeailabs.bpwatch.R
import com.fourgeailabs.bpwatch.mobile.MainActivity
import com.fourgeailabs.bpwatch.mobile.monitoring.MonitoringPrefs
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar

class WeightReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = MonitoringPrefs(context)
        if (!prefs.weightReminderEnabled.value) return

        val today = LocalDate.now().dayOfWeek.value // 1 (Mon) .. 7 (Sun)
        val daysList = prefs.weightReminderDays.value.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (today !in daysList) {
            // Not scheduled for today, reschedule next alarm
            scheduleNext(context)
            return
        }

        showNotification(context)
        scheduleNext(context)
    }

    private fun showNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = "weight_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Weight Check Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Daily reminders to record your weight"
            }
            nm.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_log_weight", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time for your weight check")
            .setContentText("Step on your scale or check body composition to keep your health trends accurate.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "WeightReminderReceiver"
        private const val NOTIFICATION_ID = 2001
        const val ACTION_TRIGGER = "com.fourgeailabs.bpwatch.mobile.WEIGHT_REMINDER"

        fun schedule(context: Context, hour: Int, minute: Int, daysCsv: String) {
            scheduleNext(context)
        }

        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WeightReminderReceiver::class.java).apply {
                action = ACTION_TRIGGER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            alarmManager.cancel(pendingIntent)
        }

        fun scheduleNext(context: Context) {
            val prefs = MonitoringPrefs(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WeightReminderReceiver::class.java).apply {
                action = ACTION_TRIGGER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            if (!prefs.weightReminderEnabled.value) {
                alarmManager.cancel(pendingIntent)
                return
            }

            val hour = prefs.weightReminderHour.value
            val minute = prefs.weightReminderMinute.value

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent,
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent,
                    )
                }
                Log.d(TAG, "Weight reminder scheduled for ${calendar.time}")
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot schedule exact alarm: ${e.message}")
            }
        }
    }
}
