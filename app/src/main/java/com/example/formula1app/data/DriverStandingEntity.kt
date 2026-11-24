package com.example.formula1app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "driver_standings")
data class DriverStandingEntity(
    @PrimaryKey val driverId: String,
    val position: Int?,
    val points: Double,
    val driverName: String,
    val teamId: String,
    val teamName: String,
    val sortOrder: Int
)