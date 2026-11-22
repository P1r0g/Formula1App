package com.example.formula1app.data

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class StandingsError {
    NO_INTERNET,
    LOAD_FAILED
}

data class StandingsUiState(
    val standings: List<DriverStanding> = emptyList(),
    val season: String? = null,
    val round: String? = null,
    val nextOffset: Int? = null,
    val isLoading: Boolean = false,
    val error: StandingsError? = null,
    val showingSavedStandings: Boolean = false
)

class StandingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StandingsRepository()
    private val storage = StandingsStorage(application)
    private val fileStorage = StandingsFileStorage(application)
    private val connectivity = application.getSystemService(ConnectivityManager::class.java)

    private val _uiState = MutableStateFlow(StandingsUiState())
    val uiState = _uiState.asStateFlow()

    private var hasRequested = false
    private var failedOffset: Int? = null

    fun loadIfNeeded() {
        if (!hasRequested) loadPage(0)
    }

    fun refresh() {
        loadPage(0)
    }

    fun loadMore() {
        _uiState.value.nextOffset?.let { loadPage(it) }
    }

    fun retry() {
        failedOffset?.let { loadPage(it) }
    }

    fun restoreStandings(standings: List<DriverStanding>): Boolean {
        if (_uiState.value.isLoading) return false

        hasRequested = true
        failedOffset = null
        _uiState.value = StandingsUiState(
            standings = standings,
            showingSavedStandings = true
        )
        return true
    }

    private fun loadPage(offset: Int) {
        val initial = _uiState.value
        if (initial.isLoading) return

        val firstRequest = !hasRequested
        hasRequested = true
        failedOffset = null
        _uiState.value = initial.copy(isLoading = true, error = null)

        viewModelScope.launch {
            var previous = initial
            var requestedOffset = offset

            try {
                if (firstRequest) {
                    val saved = withContext(Dispatchers.IO) { storage.readCache() }
                    if (saved != null && saved.standings.isNotEmpty()) {
                        previous = previous.copy(
                            standings = saved.standings,
                            showingSavedStandings = true
                        )
                        _uiState.value = previous.copy(isLoading = true)
                        Log.d(TAG, "Restored cache, rows=${saved.standings.size}")
                    }
                }

                var page = repository.loadPage(requestedOffset)

                if (requestedOffset > 0 && page.season != null &&
                    (page.season != previous.season || page.round != previous.round)
                ) {
                    requestedOffset = 0
                    page = repository.loadPage(requestedOffset)
                }

                val standings = if (requestedOffset == 0) {
                    page.standings
                } else {
                    previous.standings + page.standings
                }

                val state = StandingsUiState(
                    standings = standings.distinctBy { it.driverId },
                    season = page.season ?: previous.season.takeIf { requestedOffset > 0 },
                    round = page.round ?: previous.round.takeIf { requestedOffset > 0 },
                    nextOffset = page.nextOffset
                )

                if (state.standings.isNotEmpty()) {
                    val snapshot = StandingsBackup(state.standings, System.currentTimeMillis())

                    try {
                        withContext(Dispatchers.IO) {
                            storage.saveCache(snapshot)
                        }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        Log.w(TAG, "Failed to save standings cache", error)
                    }

                    try {
                        fileStorage.write(snapshot)
                        Log.d(TAG, "Exported standings to Downloads, rows=${snapshot.standings.size}")
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        Log.w(TAG, "Failed to export standings to Downloads", error)
                    }
                }

                _uiState.value = state
                Log.d(TAG, "Loaded offset=$requestedOffset, rows=${page.standings.size}")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                failedOffset = requestedOffset
                val kind = if (error is IOException && !hasValidatedInternet()) {
                    StandingsError.NO_INTERNET
                } else {
                    StandingsError.LOAD_FAILED
                }
                _uiState.value = previous.copy(error = kind)
                Log.w(TAG, "Failed to load offset=$requestedOffset", error)
            }
        }
    }

    private fun hasValidatedInternet(): Boolean {
        val manager = connectivity ?: return false
        val network = manager.activeNetwork ?: return false
        return manager.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }

    private companion object {
        const val TAG = "F1Standings"
    }
}