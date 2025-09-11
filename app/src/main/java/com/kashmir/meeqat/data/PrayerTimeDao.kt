package com.kashmir.meeqat.data

import androidx.room.*
import java.util.*

@Dao
interface PrayerTimeDao {
    
    @Query("SELECT * FROM prayer_times WHERE date = :date AND latitude = :latitude AND longitude = :longitude LIMIT 1")
    suspend fun getPrayerTimeForDate(date: Date, latitude: Double, longitude: Double): PrayerTime?
    
    @Query("SELECT * FROM prayer_times WHERE date BETWEEN :startDate AND :endDate AND latitude = :latitude AND longitude = :longitude ORDER BY date ASC")
    suspend fun getPrayerTimesForRange(startDate: Date, endDate: Date, latitude: Double, longitude: Double): List<PrayerTime>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(prayerTime: PrayerTime): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(prayerTimes: List<PrayerTime>)
    
    @Update
    suspend fun updatePrayerTime(prayerTime: PrayerTime)
    
    @Delete
    suspend fun deletePrayerTime(prayerTime: PrayerTime)
    
    @Query("DELETE FROM prayer_times WHERE date < :cutoffDate")
    suspend fun deleteOldPrayerTimes(cutoffDate: Date)
    
    @Query("DELETE FROM prayer_times")
    suspend fun deleteAllPrayerTimes()
}