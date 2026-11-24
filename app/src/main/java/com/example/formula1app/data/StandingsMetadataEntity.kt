package com.example.formula1app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "standings_metadata")
data class StandingsMetadataEntity(
    @PrimaryKey val id: Int = 1,
    val season: String?,
    val round: String?,
    val nextOffset: Int?
)