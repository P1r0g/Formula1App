package com.example.formula1app.data

import com.example.formula1app.network.DriverStandingDto
import com.example.formula1app.network.JolpicaApi
import com.example.formula1app.network.JolpicaClient

data class StandingsPage(
    val standings: List<DriverStanding>,
    val season: String?,
    val round: String?,
    val nextOffset: Int?
)

class StandingsRepository(
    private val api: JolpicaApi = JolpicaClient.api
) {
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