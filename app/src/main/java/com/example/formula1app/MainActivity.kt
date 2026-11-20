package com.example.formula1app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.formula1app.ui.Formula1AppTheme
import androidx.activity.viewModels
import com.example.formula1app.data.StandingsViewModel

class MainActivity : BaseActivity() {

    private val standingsViewModel: StandingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Formula1AppTheme {
                AppNavigation(
                    standingsViewModel = standingsViewModel,
                    onExit = { finish() }
                )
            }
        }
    }
}