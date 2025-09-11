package com.kashmir.meeqat.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "prayer_times")
data class PrayerTime(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val latitude: Double,
    val longitude: Double,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val calculationMethod: String = "KARACHI"
)