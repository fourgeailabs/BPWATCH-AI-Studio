package com.fourgeailabs.bpwatch.mobile.snore

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.fourgeailabs.bpwatch.mobile.monitoring.MonitoringPrefs
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Schedules the overnight snore-detection window configured in Sleep settings.
 * Uses inexact repeating alarms anchored to the user's bedtime/wake time.
 * [ensureScheduled] is idempotent and cheap: safe to call on every app start.
 */
object SnoreScheduler {
    const val ACTION_START = "com.fourgeailabs.bpwatch.mobile.SNORE_START"
    const val ACTION_STOP = "com.fourgeailabs.bpwatch.mobile.SNORE_STOP"
    const val WINDOW_HOURS = 9
    const val WINDOW_END_HOUR = 7

    private const val REQ_START = 5101
    private const val REQ_STOP = 5102

    /** True when the current local time is inside the sleep window. */
    fun inWindow(context: Context? = null, nowMs: Long = System.currentTimeMillis()): Boolean {
        val prefs = context?.let { MonitoringPrefs(it) }
        val startH = prefs?.sleepStartHour?.value ?: 22
        val startM = prefs?.sleepStartMinute?.value ?: 0
        val endH = prefs?.sleepEndHour?.value ?: 7
        val endM = prefs?.sleepEndMinute?.value ?: 0

        val now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMs), ZoneId.systemDefault())
        val currentMinutes = now.hour * 60 + now.minute
        val startMinutes = startH * 60 + startM
        val endMinutes = endH * 60 + endM

        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else {
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    /**
     * The most recently completed (or in-progress) sleep window as a
     * (start, end) pair of epoch millis.
     */
    fun lastNightWindow(context: Context? = null, nowMs: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val prefs = context?.let { MonitoringPrefs(it) }
        val startH = prefs?.sleepStartHour?.value ?: 22
        val startM = prefs?.sleepStartMinute?.value ?: 0
        val endH = prefs?.sleepEndHour?.value ?: 7
        val endM = prefs?.sleepEndMinute?.value ?: 0

        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMs), zone)
        val currentMinutes = now.hour * 60 + now.minute
        val startMinutes = startH * 60 + startM

        val evening = if (currentMinutes >= startMinutes) now.toLocalDate() else now.toLocalDate().minusDays(1)
        val start = evening.atTime(startH, startM).atZone(zone).toInstant().toEpochMilli()
        val endDay = if (startH <= endH) evening else evening.plusDays(1)
        val end = endDay.atTime(endH, endM).atZone(zone).toInstant().toEpochMilli()

        return start to end
    }

    /** (Re)arms the bedtime start and wake time stop alarms. Idempotent. */
    fun schedule(context: Context) {
        val prefs = MonitoringPrefs(context)
        val startH = prefs.sleepStartHour.value
        val startM = prefs.sleepStartMinute.value
        val endH = prefs.sleepEndHour.value
        val endM = prefs.sleepEndMinute.value

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val startPi = alarmIntent(context, ACTION_START, REQ_START)
        val stopPi = alarmIntent(context, ACTION_STOP, REQ_STOP)
        am.cancel(startPi)
        am.cancel(stopPi)
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        var nextStart = now.withHour(startH).withMinute(startM).withSecond(0).withNano(0)
        if (!nextStart.isAfter(now)) nextStart = nextStart.plusDays(1)
        var nextStop = now.withHour(endH).withMinute(endM).withSecond(0).withNano(0)
        if (!nextStop.isAfter(now)) nextStop = nextStop.plusDays(1)
        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextStart.toInstant().toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            startPi,
        )
        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextStop.toInstant().toEpochMilli(),
            AlarmManager.INTERVAL_DAY,
            stopPi,
        )
    }

    /** Cancels both alarms. */
    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(alarmIntent(context, ACTION_START, REQ_START))
        am.cancel(alarmIntent(context, ACTION_STOP, REQ_STOP))
    }

    /**
     * Re-arms the alarms on app start and after boot.
     */
    fun ensureScheduled(context: Context) {
        try {
            schedule(context)
        } catch (_: Exception) {
        }
    }

    private fun alarmIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, SnoreAlarmReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
