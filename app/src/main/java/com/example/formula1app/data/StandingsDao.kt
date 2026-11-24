package com.example.formula1app.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class StoredDriverStanding(
    @Embedded val standing: DriverStandingEntity,
    val season: String?,
    val round: String?,
    val nextOffset: Int?
)

@Dao
interface StandingsDao {
    @Query(READ_STANDINGS)
    fun observeStandings(): Flow<List<StoredDriverStanding>>

    @Query(READ_STANDINGS)
    suspend fun readStandings(): List<StoredDriverStanding>

    @Upsert
    suspend fun upsertStandings(standings: List<DriverStandingEntity>)

    @Upsert
    suspend fun upsertMetadata(metadata: StandingsMetadataEntity)

    @Query("DELETE FROM driver_standings WHERE driverId NOT IN (:driverIds)")
    suspend fun deleteMissingStandings(driverIds: List<String>)
}

private const val READ_STANDINGS = """
    SELECT driver_standings.*, standings_metadata.season, standings_metadata.round,
           standings_metadata.nextOffset
    FROM driver_standings
    LEFT JOIN standings_metadata ON standings_metadata.id = 1
    ORDER BY driver_standings.sortOrder, driver_standings.driverId
"""