package com.kashmir.meeqat.utils

import com.kashmir.meeqat.data.Location
import com.kashmir.meeqat.data.PrayerTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class PrayerCalculator {
    
    companion object {
        private const val INVALID_TIME = "-----"
        
        // Calculation method parameters
        private val calculationMethods = mapOf(
            "KARACHI" to CalculationMethod(18.0, false, 18.0, false, 0.0),
            "ISNA" to CalculationMethod(15.0, false, 15.0, false, 0.0),
            "MWL" to CalculationMethod(18.0, false, 17.0, false, 0.0),
            "MAKKAH" to CalculationMethod(18.5, false, 90.0, true, 0.0),
            "EGYPT" to CalculationMethod(19.5, false, 17.5, false, 0.0),
            "KASHMIR_CUSTOM" to CalculationMethod(18.0, false, 18.0, false, 0.0)
        )
    }
    
    data class CalculationMethod(
        val fajrAngle: Double,
        val maghribIsMinutes: Boolean,
        val ishaAngle: Double,
        val ishaIsMinutes: Boolean,
        val maghribMinutes: Double
    )
    
    fun calculatePrayerTimes(
        location: Location, 
        date: Date = Date(),
        method: String = "KARACHI"
    ): PrayerTime {
        val calc = calculationMethods[method] ?: calculationMethods["KARACHI"]!!
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        // Calculate Julian day
        val calendar = Calendar.getInstance().apply { time = date }
        val julianDay = calculateJulianDay(calendar)
        
        // Calculate equation of time and solar declination
        val eqTime = calculateEquationOfTime(julianDay)
        val solarDec = calculateSolarDeclination(julianDay)
        
        // Calculate prayer times
        val times = mutableMapOf<String, String>()
        
        try {
            // Sunrise and sunset
            val sunrise = calculateSunTime(location, julianDay, true, solarDec, eqTime)
            val sunset = calculateSunTime(location, julianDay, false, solarDec, eqTime)
            
            times["sunrise"] = timeFormat.format(Date((sunrise * 60 * 1000).toLong()))
            times["maghrib"] = timeFormat.format(Date((sunset * 60 * 1000).toLong()))
            
            // Fajr
            val fajrTime = calculatePrayerTime(location, julianDay, calc.fajrAngle, true, solarDec, eqTime)
            times["fajr"] = if (fajrTime.isNaN()) INVALID_TIME else timeFormat.format(Date((fajrTime * 60 * 1000).toLong()))
            
            // Dhuhr (solar noon + 1 minute)
            val dhuhrTime = calculateSolarNoon(location, julianDay, eqTime) + 1.0/60.0
            times["dhuhr"] = timeFormat.format(Date((dhuhrTime * 60 * 1000).toLong()))
            
            // Asr (Shafi method - shadow length = object length + shadow at noon)
            val asrTime = calculateAsrTime(location, julianDay, 1, solarDec, eqTime)
            times["asr"] = if (asrTime.isNaN()) INVALID_TIME else timeFormat.format(Date((asrTime * 60 * 1000).toLong()))
            
            // Isha
            val ishaTime = if (calc.ishaIsMinutes) {
                sunset + calc.ishaAngle / 60.0
            } else {
                calculatePrayerTime(location, julianDay, calc.ishaAngle, false, solarDec, eqTime)
            }
            times["isha"] = if (ishaTime.isNaN()) INVALID_TIME else timeFormat.format(Date((ishaTime * 60 * 1000).toLong()))
            
        } catch (e: Exception) {
            // Return default times if calculation fails
            return PrayerTime(
                date = date,
                latitude = location.latitude,
                longitude = location.longitude,
                fajr = "05:30",
                sunrise = "06:45",
                dhuhr = "12:30",
                asr = "15:30",
                maghrib = "18:15",
                isha = "19:45",
                calculationMethod = method
            )
        }
        
        return PrayerTime(
            date = date,
            latitude = location.latitude,
            longitude = location.longitude,
            fajr = times["fajr"] ?: "05:30",
            sunrise = times["sunrise"] ?: "06:45",
            dhuhr = times["dhuhr"] ?: "12:30",
            asr = times["asr"] ?: "15:30",
            maghrib = times["maghrib"] ?: "18:15",
            isha = times["isha"] ?: "19:45",
            calculationMethod = method
        )
    }
    
    private fun calculateJulianDay(calendar: Calendar): Double {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val a = (14 - month) / 12
        val y = year - a
        val m = month + 12 * a - 3
        
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045.0
    }
    
    private fun calculateEquationOfTime(julianDay: Double): Double {
        val n = julianDay - 2451545.0
        val l = (280.460 + 0.9856474 * n) % 360
        val g = toRadians((357.528 + 0.9856003 * n) % 360)
        val lambda = toRadians(l + 1.915 * sin(g) + 0.020 * sin(2 * g))
        
        val alpha = atan2(cos(toRadians(23.439)) * sin(lambda), cos(lambda))
        val eqTime = 4 * (l - toDegrees(alpha))
        
        return if (eqTime > 20) eqTime - 1440 else if (eqTime < -20) eqTime + 1440 else eqTime
    }
    
    private fun calculateSolarDeclination(julianDay: Double): Double {
        val n = julianDay - 2451545.0
        val l = toRadians((280.460 + 0.9856474 * n) % 360)
        val g = toRadians((357.528 + 0.9856003 * n) % 360)
        val lambda = l + toRadians(1.915 * sin(g) + 0.020 * sin(2 * g))
        
        return asin(sin(toRadians(23.439)) * sin(lambda))
    }
    
    private fun calculateSolarNoon(location: Location, julianDay: Double, eqTime: Double): Double {
        val noon = 720 - 4 * location.longitude - eqTime
        return noon / 60.0
    }
    
    private fun calculateSunTime(
        location: Location,
        julianDay: Double,
        sunrise: Boolean,
        solarDec: Double,
        eqTime: Double
    ): Double {
        val lat = toRadians(location.latitude)
        val hourAngle = acos(-tan(lat) * tan(solarDec))
        
        val timeOffset = if (sunrise) -toDegrees(hourAngle) else toDegrees(hourAngle)
        val time = 720 + 4 * (location.longitude + timeOffset) - eqTime
        
        return time / 60.0
    }
    
    private fun calculatePrayerTime(
        location: Location,
        julianDay: Double,
        angle: Double,
        morning: Boolean,
        solarDec: Double,
        eqTime: Double
    ): Double {
        val lat = toRadians(location.latitude)
        val decRad = solarDec
        val angleRad = toRadians(angle)
        
        val argument = (sin(angleRad) - sin(decRad) * sin(lat)) / (cos(decRad) * cos(lat))
        
        if (argument < -1 || argument > 1) {
            return Double.NaN
        }
        
        val hourAngle = acos(argument)
        val timeOffset = if (morning) -toDegrees(hourAngle) else toDegrees(hourAngle)
        val time = 720 + 4 * (location.longitude + timeOffset) - eqTime
        
        return time / 60.0
    }
    
    private fun calculateAsrTime(
        location: Location,
        julianDay: Double,
        shadowFactor: Int,
        solarDec: Double,
        eqTime: Double
    ): Double {
        val lat = toRadians(location.latitude)
        val decRad = solarDec
        
        val angle = atan(1.0 / (shadowFactor + tan(abs(lat - decRad))))
        val argument = (sin(angle) - sin(decRad) * sin(lat)) / (cos(decRad) * cos(lat))
        
        if (argument < -1 || argument > 1) {
            return Double.NaN
        }
        
        val hourAngle = acos(argument)
        val time = 720 + 4 * (location.longitude + toDegrees(hourAngle)) - eqTime
        
        return time / 60.0
    }
    
    fun getNextPrayerInfo(prayerTime: PrayerTime): Pair<String, Long> {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val now = Calendar.getInstance()
        val currentTime = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        
        val prayers = listOf(
            "Fajr" to prayerTime.fajr,
            "Sunrise" to prayerTime.sunrise,
            "Dhuhr" to prayerTime.dhuhr,
            "Asr" to prayerTime.asr,
            "Maghrib" to prayerTime.maghrib,
            "Isha" to prayerTime.isha
        )
        
        for ((name, timeStr) in prayers) {
            if (timeStr != INVALID_TIME) {
                try {
                    val time = timeFormat.parse(timeStr)
                    val cal = Calendar.getInstance().apply { 
                        time = time
                        set(Calendar.YEAR, now.get(Calendar.YEAR))
                        set(Calendar.MONTH, now.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                    }
                    val prayerMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                    
                    if (prayerMinutes > currentTime) {
                        val remainingMs = (prayerMinutes - currentTime) * 60 * 1000L
                        return name to remainingMs
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        }
        
        // If no prayer found for today, return tomorrow's Fajr
        val tomorrow = Calendar.getInstance().apply { 
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 30)
        }
        val remainingMs = tomorrow.timeInMillis - System.currentTimeMillis()
        return "Fajr" to remainingMs
    }
}