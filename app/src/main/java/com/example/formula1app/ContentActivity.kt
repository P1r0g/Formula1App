package com.example.formula1app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.formula1app.data.AppState
import com.example.formula1app.screens.ContentScreen
import com.example.formula1app.ui.Formula1AppTheme

class ContentActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Formula1AppTheme {
                ContentScreen(
                    standings = AppState.standings,
                    onOpenSettings = {
                        startActivity(
                            Intent(
                                this@ContentActivity,
                                SettingsActivity::class.java
                            )
                        )
                    }
                )
            }
        }
    }
}