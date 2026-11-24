package com.example.formula1app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DriverStandingEntity::class, StandingsMetadataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class Formula1Database : RoomDatabase() {
    abstract fun standingsDao(): StandingsDao

    companion object {
        @Volatile
        private var instance: Formula1Database? = null

        fun getInstance(context: Context): Formula1Database =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    Formula1Database::class.java,
                    "formula1.db"
                ).build().also { instance = it }
            }
    }
}