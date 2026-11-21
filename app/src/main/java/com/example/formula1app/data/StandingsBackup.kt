package com.example.formula1app.data

import kotlinx.serialization.Serializable

@Serializable
data class StandingsBackup(
    val standings: List<DriverStanding>,
    val createdAtMillis: Long
)