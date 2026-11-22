package com.example.formula1app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.formula1app.ui.Formula1AppTheme
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.example.formula1app.data.StandingsViewModel
import com.example.formula1app.data.AppState

class MainActivity : BaseActivity() {

    private val standingsViewModel: StandingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppState.initializeStorage(applicationContext)
        enableEdgeToEdge()

        setContent {
            LaunchedEffect(Unit) { AppState.observeUserSettings() }

            if (AppState.settingsLoaded) {
                Formula1AppTheme {
                    AppNavigation(
                        standingsViewModel = standingsViewModel,
                        onExit = { finish() }
                    )
                }
            }
        }
    }
}