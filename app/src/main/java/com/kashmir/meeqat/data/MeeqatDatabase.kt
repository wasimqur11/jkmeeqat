package com.kashmir.meeqat.data

import android.content.Context
import androidx.room.*

@Database(
    entities = [PrayerTime::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MeeqatDatabase : RoomDatabase() {
    
    abstract fun prayerTimeDao(): PrayerTimeDao
    
    companion object {
        @Volatile
        private var INSTANCE: MeeqatDatabase? = null
        
        fun getDatabase(context: Context): MeeqatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeeqatDatabase::class.java,
                    "meeqat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}