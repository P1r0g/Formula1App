package com.example.formula1app

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import com.example.formula1app.data.AppState
import java.util.Locale

@Composable
fun AppLocalization(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val languageCode = AppState.languageCode

    val localizedContext = remember(
        context,
        configuration,
        languageCode
    ) {
        val localizedConfiguration = Configuration(configuration).apply {
            setLocale(Locale.forLanguageTag(languageCode))
        }

        context.createConfigurationContext(localizedConfiguration)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        LocalResources provides localizedContext.resources,
        content = content
    )
}