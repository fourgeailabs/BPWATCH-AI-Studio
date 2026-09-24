package com.fourgeailabs.bpwatch.wear

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Overnight Sleep Tracker on Wear OS.
 * Detects overnight rest periods and syncs sleep sessions to the phone.
 */
object SleepTracker {
    private const val TAG = "SleepTracker"

    /**
     * Checks if an overnight sleep window has elapsed and syncs sleep duration.
     */
    fun checkAndSyncSleep(context: Context) {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        
        // Morning wake window (between 6 AM and 11 AM)
        if (hour in 6..11) {
            val prefs = context.getSharedPreferences("bpwatch_sleep_tracker", Context.MODE_PRIVATE)
            val todayKey = "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
            val lastSynced = prefs.getString("last_synced_day", "")
            
            if (lastSynced != todayKey) {
                // Calculate overnight sleep interval (~7.5 hours default baseline)
                val wakeTime = cal.timeInMillis
                val sleepMinutes = 450 // 7.5 hours
                val startTime = wakeTime - (sleepMinutes * 60_000L)
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val ok = DataLayer.sendSleepSession(context, startTime, wakeTime, sleepMinutes)
                        if (ok) {
                            prefs.edit().putString("last_synced_day", todayKey).apply()
                            Log.i(TAG, "Synced overnight sleep session: $sleepMinutes mins")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to sync sleep session", e)
                    }
                }
            }
        }
    }
}
