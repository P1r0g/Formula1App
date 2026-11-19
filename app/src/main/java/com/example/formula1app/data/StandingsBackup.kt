package com.example.formula1app.data

data class StandingsBackup(
    val standings: List<DriverStanding>,
    val createdAtMillis: Long
)