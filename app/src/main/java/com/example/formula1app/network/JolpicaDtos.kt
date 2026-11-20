package com.example.formula1app.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JolpicaStandingsResponse(
    @SerialName("MRData") val data: StandingsPageDto
)

@Serializable
data class StandingsPageDto(
    val limit: String,
    val offset: String,
    val total: String,
    @SerialName("StandingsTable") val standingsTable: StandingsTableDto
)

@Serializable
data class StandingsTableDto(
    @SerialName("StandingsLists") val standingsLists: List<SeasonStandingsDto>
)

@Serializable
data class SeasonStandingsDto(
    val season: String,
    val round: String,
    @SerialName("DriverStandings") val driverStandings: List<DriverStandingDto>
)

@Serializable
data class DriverStandingDto(
    val position: String? = null,
    val points: String,
    @SerialName("Driver") val driver: DriverDto,
    @SerialName("Constructors") val constructors: List<ConstructorDto>
)

@Serializable
data class DriverDto(
    val driverId: String,
    val givenName: String,
    val familyName: String
)

@Serializable
data class ConstructorDto(
    val constructorId: String,
    val name: String
)