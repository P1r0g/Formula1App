package com.example.formula1app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.formula1app.data.AppState
import com.example.formula1app.screens.StartScreen
import com.example.formula1app.ui.Formula1AppTheme

class StartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var initialLogin by rememberSaveable {
                mutableStateOf(AppState.userProfile?.login.orEmpty())
            }

            var loginFormVersion by rememberSaveable {
                mutableStateOf(0)
            }

            val registrationLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data
                        ?.getStringExtra(RegistrationActivity.EXTRA_LOGIN)
                        ?.let { registeredLogin ->
                            initialLogin = registeredLogin
                            loginFormVersion++
                        }
                }
            }

            Formula1AppTheme {
                key(loginFormVersion) {
                    StartScreen(
                        initialLogin = initialLogin,
                        onOpenRegistration = {
                            registrationLauncher.launch(
                                Intent(
                                    this@StartActivity,
                                    RegistrationActivity::class.java
                                )
                            )
                        },
                        onLoginSuccess = {
                            startActivity(
                                Intent(
                                    this@StartActivity,
                                    ContentActivity::class.java
                                )
                            )
                            finish()
                        }
                    )
                }
            }
        }
    }
}