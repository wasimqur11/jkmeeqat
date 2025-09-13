package com.kashmir.meeqat.utils

import com.kashmir.meeqat.data.Location
import kotlin.math.*

object QiblaCalculator {
    
    // Kaaba coordinates
    private const val KAABA_LATITUDE = 21.4225
    private const val KAABA_LONGITUDE = 39.8262
    
    fun calculateQiblaDirection(location: Location): Double {
        val lat1 = Math.toRadians(location.latitude)
        val lng1 = Math.toRadians(location.longitude)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val lng2 = Math.toRadians(KAABA_LONGITUDE)
        
        val dLng = lng2 - lng1
        
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        
        val bearing = atan2(y, x)
        
        // Convert to degrees and normalize to 0-360
        val bearingDegrees = Math.toDegrees(bearing)
        return (bearingDegrees + 360) % 360
    }
    
    fun getQiblaDirectionText(angle: Double): String {
        return when {
            angle < 22.5 || angle >= 337.5 -> "${angle.toInt()}° North"
            angle < 67.5 -> "${angle.toInt()}° Northeast"
            angle < 112.5 -> "${angle.toInt()}° East"
            angle < 157.5 -> "${angle.toInt()}° Southeast"
            angle < 202.5 -> "${angle.toInt()}° South"
            angle < 247.5 -> "${angle.toInt()}° Southwest"
            angle < 292.5 -> "${angle.toInt()}° West"
            angle < 337.5 -> "${angle.toInt()}° Northwest"
            else -> "${angle.toInt()}° North"
        }
    }
    
    fun calculateDistance(location: Location): Double {
        val lat1 = Math.toRadians(location.latitude)
        val lng1 = Math.toRadians(location.longitude)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val lng2 = Math.toRadians(KAABA_LONGITUDE)
        
        val dLat = lat2 - lat1
        val dLng = lng2 - lng1
        
        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2.0 * atan2(sqrt(a), sqrt(1 - a))
        
        // Earth radius in kilometers
        val earthRadius = 6371.0
        return earthRadius * c
    }
}