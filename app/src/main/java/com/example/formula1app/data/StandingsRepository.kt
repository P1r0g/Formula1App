package com.example.formula1app.data

import com.example.formula1app.network.DriverStandingDto
import com.example.formula1app.network.JolpicaApi
import com.example.formula1app.network.JolpicaClient
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class StandingsPage(
    val standings: List<DriverStanding>,
    val season: String?,
    val round: String?,
    val nextOffset: Int?
)

class StandingsRepository(
    private val database: Formula1Database,
    private val api: JolpicaApi = JolpicaClient.api
) {
    private val dao = database.standingsDao()

    fun observeStandings(): Flow<StandingsPage> = dao.observeStandings()
        .map { rows -> rows.toStandingsPage() }
        .distinctUntilChanged()

    suspend fun readStandings(): StandingsPage =
        dao.readStandings().toStandingsPage()

    suspend fun loadSavedStandings(cachedStandings: List<DriverStanding>): Boolean =
        database.withTransaction {
            val current = readStandings()
            if (current.standings.isNotEmpty()) return@withTransaction true
            if (cachedStandings.isEmpty()) return@withTransaction false

            val snapshot = StandingsPage(
                standings = cachedStandings,
                season = null,
                round = null,
                nextOffset = null
            )
            writeIfChanged(current, snapshot)
            true
        }

    suspend fun savePage(page: StandingsPage, offset: Int): StandingsPage =
        database.withTransaction {
            val current = readStandings()

            if (offset == 0) {
                return@withTransaction writeIfChanged(current, page)
            }

            val differentRound = page.season != null &&
                    (page.season != current.season || page.round != current.round)

            if (offset != current.nextOffset || differentRound) {
                return@withTransaction current
            }

            val merged = page.copy(
                standings = current.standings + page.standings,
                season = page.season ?: current.season,
                round = page.round ?: current.round
            )

            writeIfChanged(current, merged)
        }

    private suspend fun writeIfChanged(
        current: StandingsPage,
        page: StandingsPage
    ): StandingsPage {
        val updated = page.copy(
            standings = page.standings.distinctBy { it.driverId }
        )
        if (updated == current) return current

        val rows = updated.standings.mapIndexed { index, driver ->
            DriverStandingEntity(
                driverId = driver.driverId,
                position = driver.position,
                points = driver.points,
                driverName = driver.driverName,
                teamId = driver.teamId,
                teamName = driver.teamName,
                sortOrder = index
            )
        }

        dao.deleteMissingStandings(rows.map { it.driverId })
        dao.upsertStandings(rows)
        dao.upsertMetadata(
            StandingsMetadataEntity(
                season = updated.season,
                round = updated.round,
                nextOffset = updated.nextOffset
            )
        )
        return updated
    }

    private fun List<StoredDriverStanding>.toStandingsPage(): StandingsPage {
        val firstRow = firstOrNull()
        return StandingsPage(
            standings = map { row ->
                val driver = row.standing
                DriverStanding(
                    driverId = driver.driverId,
                    position = driver.position,
                    points = driver.points,
                    driverName = driver.driverName,
                    teamId = driver.teamId,
                    teamName = driver.teamName
                )
            },
            season = firstRow?.season,
            round = firstRow?.round,
            nextOffset = firstRow?.nextOffset
        )
    }

    suspend fun loadPage(offset: Int = 0): StandingsPage {
        require(offset >= 0)

        val page = api.getDriverStandings(limit = PAGE_SIZE, offset = offset).data
        check(page.offset.toIntOrNull() == offset) {
            "Jolpica returned offset ${page.offset} for requested offset $offset"
        }

        val total = page.total.toIntOrNull()
        check(total != null && total >= 0) { "Invalid Jolpica total: ${page.total}" }

        val standings = page.standingsTable.standingsLists.firstOrNull()
        val rows = standings?.driverStandings.orEmpty()
        val endOffset = offset + rows.size

        return StandingsPage(
            standings = rows.map { it.toDriverStanding() },
            season = standings?.season,
            round = standings?.round,
            nextOffset = endOffset.takeIf { rows.isNotEmpty() && it < total }
        )
    }

    private fun DriverStandingDto.toDriverStanding(): DriverStanding {
        val team = constructors.lastOrNull()
        val score = points.toDoubleOrNull()
        check(score != null && score.isFinite()) {
            "Invalid Jolpica points for ${driver.driverId}: $points"
        }

        return DriverStanding(
            driverId = driver.driverId,
            position = position?.toIntOrNull(),
            points = score,
            driverName = "${driver.givenName} ${driver.familyName}",
            teamId = team?.constructorId.orEmpty(),
            teamName = team?.name ?: "—"
        )
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}