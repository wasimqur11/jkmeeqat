package com.kashmir.meeqat.services

import android.content.Context
import androidx.work.*
import com.kashmir.meeqat.data.PrayerTime
import com.kashmir.meeqat.ui.settings.SettingsActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PrayerNotificationManager(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    private val sharedPreferences = context.getSharedPreferences("meeqat_preferences", Context.MODE_PRIVATE)
    
    fun scheduleNotificationsForDay(prayerTime: PrayerTime) {
        // Cancel existing notifications
        cancelAllNotifications()
        
        val prayers = listOf(
            "Fajr" to prayerTime.fajr,
            "Dhuhr" to prayerTime.dhuhr,
            "Asr" to prayerTime.asr,
            "Maghrib" to prayerTime.maghrib,
            "Isha" to prayerTime.isha
        )
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val today = Calendar.getInstance()
        
        prayers.forEach { (prayerName, prayerTimeStr) ->
            if (isPrayerNotificationEnabled(prayerName) && prayerTimeStr != "-----") {
                try {
                    val prayerDate = timeFormat.parse(prayerTimeStr)
                    val prayerCalendar = Calendar.getInstance().apply {
                        time = prayerDate
                        set(Calendar.YEAR, today.get(Calendar.YEAR))
                        set(Calendar.MONTH, today.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
                    }
                    
                    val currentTime = System.currentTimeMillis()
                    val prayerTimeMs = prayerCalendar.timeInMillis
                    
                    // Only schedule if prayer time is in the future
                    if (prayerTimeMs > currentTime) {
                        val delay = prayerTimeMs - currentTime
                        scheduleNotification(prayerName, prayerTimeStr, delay)
                    }
                } catch (e: Exception) {
                    // Skip invalid time formats
                }
            }
        }
    }
    
    private fun scheduleNotification(prayerName: String, prayerTime: String, delayMillis: Long) {
        val inputData = Data.Builder()
            .putString("prayer_name", prayerName)
            .putString("prayer_time", prayerTime)
            .build()
        
        val notificationWork = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("prayer_notification_$prayerName")
            .build()
        
        workManager.enqueue(notificationWork)
    }
    
    private fun isPrayerNotificationEnabled(prayerName: String): Boolean {
        val prefKey = when (prayerName) {
            "Fajr" -> "fajr_notification"
            "Dhuhr" -> "dhuhr_notification"
            "Asr" -> "asr_notification"
            "Maghrib" -> "maghrib_notification"
            "Isha" -> "isha_notification"
            else -> return false
        }
        return sharedPreferences.getBoolean(prefKey, true)
    }
    
    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag("prayer_notification_Fajr")
        workManager.cancelAllWorkByTag("prayer_notification_Dhuhr")
        workManager.cancelAllWorkByTag("prayer_notification_Asr")
        workManager.cancelAllWorkByTag("prayer_notification_Maghrib")
        workManager.cancelAllWorkByTag("prayer_notification_Isha")
    }
    
    fun scheduleNotificationsForNextDays(prayerTimes: List<PrayerTime>) {
        prayerTimes.forEach { prayerTime ->
            scheduleNotificationsForDay(prayerTime)
        }
    }
}