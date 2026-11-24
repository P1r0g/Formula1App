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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update

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
    private val repository by lazy {
        StandingsRepository(Formula1Database.getInstance(application))
    }
    private val storage = StandingsStorage(application)
    private val fileStorage = StandingsFileStorage(application)
    private val connectivity = application.getSystemService(ConnectivityManager::class.java)

    private val _uiState = MutableStateFlow(StandingsUiState())
    val uiState = _uiState.asStateFlow()

    private var hasRequested = false
    private var failedOffset: Int? = null
    private var standingsObserver: Job? = null

    fun loadIfNeeded() {
        if (!hasRequested) loadPage(0, onlyIfEmpty = true)
    }

    fun refresh() = loadPage(0)

    fun loadMore() {
        _uiState.value.nextOffset?.let { loadPage(it) }
    }

    fun retry() {
        val offset = failedOffset
        loadPage(offset ?: 0, onlyIfEmpty = offset == null)
    }

    suspend fun restoreStandings(standings: List<DriverStanding>): Boolean {
        if (_uiState.value.isLoading || standings.isEmpty()) return false

        _uiState.update { it.copy(isLoading = true) }
        observeStandingsIfNeeded()

        return try {
            val snapshot = StandingsPage(
                standings = standings,
                season = null,
                round = null,
                nextOffset = null
            )
            repository.savePage(snapshot, offset = 0)
            hasRequested = true
            failedOffset = null
            _uiState.update { it.copy(error = null, showingSavedStandings = true) }
            true
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w(TAG, "Failed to restore standings backup", error)
            false
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    suspend fun readFileStatus() = fileStorage.readStatus()

    suspend fun deleteExportedFile() = fileStorage.deleteToBackup()

    suspend fun restoreExportedFile() = fileStorage.restoreFromBackup()

    private fun observeStandingsIfNeeded() {
        if (standingsObserver?.isActive == true) return

        standingsObserver = viewModelScope.launch {
            try {
                repository.observeStandings().collect { page ->
                    _uiState.update {
                        it.copy(
                            standings = page.standings,
                            season = page.season,
                            round = page.round,
                            nextOffset = page.nextOffset
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(error = StandingsError.LOAD_FAILED) }
                Log.w(TAG, "Failed to observe standings in Room", error)
            }
        }
    }

    private fun loadPage(offset: Int, onlyIfEmpty: Boolean = false) {
        if (_uiState.value.isLoading) return

        hasRequested = true
        failedOffset = null
        _uiState.update { it.copy(isLoading = true, error = null) }
        observeStandingsIfNeeded()

        viewModelScope.launch {
            var requestedOffset = offset

            try {
                if (onlyIfEmpty && loadSavedStandings()) {
                    _uiState.update { it.copy(showingSavedStandings = true) }
                    return@launch
                }

                val current = if (offset > 0) repository.readStandings() else null
                if (current != null) {
                    requestedOffset = current.nextOffset ?: return@launch
                }

                var page = repository.loadPage(requestedOffset)
                if (current != null && page.season != null &&
                    (page.season != current.season || page.round != current.round)
                ) {
                    requestedOffset = 0
                    page = repository.loadPage(requestedOffset)
                }

                val saved = repository.savePage(page, offset = requestedOffset)
                _uiState.update { it.copy(showingSavedStandings = false) }
                exportStandings(saved.standings)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                failedOffset = requestedOffset.takeUnless { onlyIfEmpty }
                val kind = if (error is IOException && !hasValidatedInternet()) {
                    StandingsError.NO_INTERNET
                } else {
                    StandingsError.LOAD_FAILED
                }

                _uiState.update {
                    it.copy(error = kind, showingSavedStandings = it.standings.isNotEmpty())
                }
                Log.w(TAG, "Failed to load offset=$requestedOffset", error)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun loadSavedStandings(): Boolean {
        val cache = withContext(Dispatchers.IO) { storage.readCache() }
        val hasSaved = repository.loadSavedStandings(cache?.standings.orEmpty())
        if (cache != null) {
            withContext(Dispatchers.IO) { storage.deleteCache() }
        }
        return hasSaved
    }

    private suspend fun exportStandings(standings: List<DriverStanding>) {
        if (standings.isEmpty()) return

        val snapshot = StandingsBackup(standings, System.currentTimeMillis())
        try {
            fileStorage.write(snapshot)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w(TAG, "Failed to export standings to Downloads", error)
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