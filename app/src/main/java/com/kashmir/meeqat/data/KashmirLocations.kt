package com.kashmir.meeqat.data

object KashmirLocations {
    val locations = listOf(
        Location(name = "Srinagar", latitude = 34.0837, longitude = 74.7973),
        Location(name = "Jammu", latitude = 32.7266, longitude = 74.8570),
        Location(name = "Anantnag", latitude = 33.7311, longitude = 75.1482),
        Location(name = "Baramulla", latitude = 34.2091, longitude = 74.3412),
        Location(name = "Sopore", latitude = 34.3030, longitude = 74.4660),
        Location(name = "Kupwara", latitude = 34.5242, longitude = 74.2553),
        Location(name = "Pulwama", latitude = 33.8719, longitude = 74.8949),
        Location(name = "Budgam", latitude = 34.0230, longitude = 74.7350),
        Location(name = "Ganderbal", latitude = 34.2308, longitude = 74.7760),
        Location(name = "Kulgam", latitude = 33.6410, longitude = 75.0170),
        Location(name = "Shopian", latitude = 33.7248, longitude = 74.8346),
        Location(name = "Bandipora", latitude = 34.4186, longitude = 74.6398),
        Location(name = "Kathua", latitude = 32.3696, longitude = 75.5198),
        Location(name = "Udhampur", latitude = 32.9150, longitude = 75.1420),
        Location(name = "Reasi", latitude = 33.0839, longitude = 74.8360),
        Location(name = "Rajouri", latitude = 33.3730, longitude = 74.3120),
        Location(name = "Poonch", latitude = 33.7700, longitude = 74.0940),
        Location(name = "Doda", latitude = 33.1394, longitude = 75.5470),
        Location(name = "Kishtwar", latitude = 33.3100, longitude = 75.7670),
        Location(name = "Ramban", latitude = 33.2430, longitude = 75.2410),
        Location(name = "Samba", latitude = 32.5626, longitude = 75.1188),
        Location(name = "Handwara", latitude = 34.4020, longitude = 74.2730),
        Location(name = "Kargil", latitude = 34.5539, longitude = 76.1462),
        Location(name = "Leh", latitude = 34.1526, longitude = 77.5770)
    )
    
    fun getLocationByName(name: String): Location? {
        return locations.find { it.name.equals(name, ignoreCase = true) }
    }
    
    fun getLocationByCoordinates(latitude: Double, longitude: Double, tolerance: Double = 0.1): Location? {
        return locations.find { 
            kotlin.math.abs(it.latitude - latitude) < tolerance && 
            kotlin.math.abs(it.longitude - longitude) < tolerance 
        }
    }
}