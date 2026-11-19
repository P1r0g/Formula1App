package com.example.formula1app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.formula1app.ui.Formula1AppTheme

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Formula1AppTheme {
                AppNavigation(
                    onExit = { finish() }
                )
            }
        }
    }
}