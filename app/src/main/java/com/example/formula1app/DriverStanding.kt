package com.example.formula1app

data class DriverStanding(
    val driverId: String,
    val position: Int,
    val points: Double,
    val driverName: String,
    val teamId: String,
    val teamName: String
)