package com.example.formula1app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.formula1app.ui.theme.Formula1AppTheme

class StartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Formula1AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Formula1App",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(text = "Стартовый экран")

                        Button(
                            onClick = {
                                val intent = Intent(
                                    this@StartActivity,
                                    RegistrationActivity::class.java
                                )
                                startActivity(intent)
                            }
                        ) {
                            Text(text = "Регистрация")
                        }
                        Button(
                            onClick = {
                                val intent = Intent(
                                    this@StartActivity,
                                    ContentActivity::class.java
                                )
                                startActivity(intent)
                                finish()
                            }
                        ) {
                            Text(text = "Открыть таблицу")
                        }
                    }
                }
            }
        }
    }
}