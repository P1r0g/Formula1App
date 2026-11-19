package com.example.formula1app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.formula1app.screens.RegistrationScreen
import com.example.formula1app.ui.Formula1AppTheme

class RegistrationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Formula1AppTheme {
                RegistrationScreen(
                    onRegistrationComplete = { registeredLogin ->
                        val result = Intent()
                            .putExtra(EXTRA_LOGIN, registeredLogin)

                        setResult(Activity.RESULT_OK, result)
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_LOGIN = "login"
    }
}