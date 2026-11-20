package com.example.formula1app.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface JolpicaApi {

    @GET("current/driverstandings/")
    @Headers("User-Agent: Formula1App/1.0")
    suspend fun getDriverStandings(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): JolpicaStandingsResponse
}

object JolpicaClient {
    private const val BASE_URL = "https://api.jolpi.ca/ergast/f1/"

    private val json = Json { ignoreUnknownKeys = true }

    val api: JolpicaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(JolpicaApi::class.java)
    }
}