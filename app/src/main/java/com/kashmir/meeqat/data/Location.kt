package com.kashmir.meeqat.data

data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "Asia/Kolkata"
)

object KashmirLocations {
    val locations = listOf(
        Location("Srinagar", 34.0837, 74.7973),
        Location("Jammu", 32.7266, 74.8570),
        Location("Anantnag", 33.7311, 75.1480),
        Location("Baramulla", 34.2090, 74.3439),
        Location("Sopore", 34.3030, 74.4730),
        Location("Kupwara", 34.5240, 74.2570),
        Location("Pulwama", 33.8710, 74.8936),
        Location("Budgam", 34.0240, 74.7298),
        Location("Ganderbal", 34.2370, 74.7750),
        Location("Kulgam", 33.6410, 75.0180),
        Location("Shopian", 33.7248, 74.8318),
        Location("Bandipora", 34.4150, 74.6390),
        Location("Kathua", 32.3704, 75.5224),
        Location("Udhampur", 32.9150, 75.1420),
        Location("Reasi", 33.0839, 74.8358),
        Location("Rajouri", 33.3739, 74.3154),
        Location("Poonch", 33.7739, 74.0939),
        Location("Doda", 33.1390, 75.5467),
        Location("Kishtwar", 33.3119, 75.7669),
        Location("Ramban", 33.2430, 75.2430),
        Location("Samba", 32.5627, 75.1180)
    )
    
    fun getLocationByName(name: String): Location? {
        return locations.find { it.name.equals(name, ignoreCase = true) }
    }
}