package com.example.formula1app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.formula1app.data.AppState
import com.example.formula1app.data.StandingsViewModel
import com.example.formula1app.screens.ContentScreen
import com.example.formula1app.screens.RegistrationScreen
import com.example.formula1app.screens.SettingsScreen
import com.example.formula1app.screens.StartScreen
import kotlinx.serialization.Serializable

/*
Старая навигация из лабораторной 1.

StartActivity -> RegistrationActivity:
registrationLauncher.launch(
    Intent(this@StartActivity, RegistrationActivity::class.java)
)

StartActivity -> ContentActivity:
startActivity(Intent(this@StartActivity, ContentActivity::class.java))
finish()

RegistrationActivity -> StartActivity с результатом:
val result = Intent().putExtra(EXTRA_LOGIN, registeredLogin)
setResult(Activity.RESULT_OK, result)
finish()

ContentActivity -> SettingsActivity:
startActivity(Intent(this@ContentActivity, SettingsActivity::class.java))

Возврат из регистрации или настроек:
finish()
*/

@Serializable
sealed interface AppDestination : NavKey {

    @Serializable
    data object Start : AppDestination

    @Serializable
    data object Registration : AppDestination

    @Serializable
    data object Content : AppDestination

    @Serializable
    data object Settings : AppDestination
}

@Composable
fun AppNavigation(
    standingsViewModel: StandingsViewModel,
    onExit: () -> Unit
) {
    val lifecycleLogger = remember { ScreenLifecycleLogger() }
    val backStack = rememberNavBackStack(AppDestination.Start)

    var initialLogin by rememberSaveable {
        mutableStateOf(AppState.userProfile?.login.orEmpty())
    }

    var loginFormVersion by rememberSaveable {
        mutableStateOf(0)
    }

    // После завершения процесса профиль теряется, поэтому возвращаемся ко входу.
    LaunchedEffect(AppState.userProfile) {
        val hasProtectedScreens = backStack.any { destination ->
            destination == AppDestination.Content ||
                    destination == AppDestination.Settings
        }

        if (AppState.userProfile == null && hasProtectedScreens) {
            backStack.clear()
            backStack.add(AppDestination.Start)
        }
    }

    val navigateBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        } else {
            onExit()
        }
    }

    BackHandler(
        enabled = backStack.size == 1,
        onBack = onExit
    )

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = navigateBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>()
        ),
        entryProvider = entryProvider<NavKey> {
            entry<AppDestination.Start> {
                lifecycleLogger.Observe("StartScreen")

                key(loginFormVersion) {
                    StartScreen(
                        initialLogin = initialLogin,
                        onOpenRegistration = {
                            if (backStack.lastOrNull() == AppDestination.Start) {
                                backStack.add(AppDestination.Registration)
                            }
                        },
                        onLoginSuccess = {
                            if (backStack.lastOrNull() == AppDestination.Start) {
                                backStack.clear()
                                backStack.add(AppDestination.Content)
                            }
                        }
                    )
                }
            }

            entry<AppDestination.Registration> {
                lifecycleLogger.Observe("RegistrationScreen")

                RegistrationScreen(
                    onRegistrationComplete = { registeredLogin ->
                        if (backStack.lastOrNull() == AppDestination.Registration) {
                            initialLogin = registeredLogin
                            loginFormVersion++
                            navigateBack()
                        }
                    },
                    onBack = {
                        if (backStack.lastOrNull() == AppDestination.Registration) {
                            navigateBack()
                        }
                    }
                )
            }

            entry<AppDestination.Content> {
                lifecycleLogger.Observe("ContentScreen")

                val state by standingsViewModel.uiState.collectAsStateWithLifecycle()
                val hasProfile = AppState.userProfile != null

                LaunchedEffect(standingsViewModel, hasProfile) {
                    if (hasProfile) {
                        standingsViewModel.loadIfNeeded()
                    }
                }

                ContentScreen(
                    state = state,
                    onRefresh = standingsViewModel::refresh,
                    onLoadMore = standingsViewModel::loadMore,
                    onRetry = standingsViewModel::retry,
                    onOpenSettings = {
                        if (backStack.lastOrNull() == AppDestination.Content) {
                            backStack.add(AppDestination.Settings)
                        }
                    }
                )
            }

            entry<AppDestination.Settings> {
                lifecycleLogger.Observe("SettingsScreen")

                SettingsScreen(
                    standingsViewModel = standingsViewModel,
                    onBack = {
                        if (backStack.lastOrNull() == AppDestination.Settings) {
                            navigateBack()
                        }
                    }
                )
            }
        }
    )
}