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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


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
    standingsViewModel: StandingsViewModel, onExit: () -> Unit
) {
    val lifecycleLogger = remember { ScreenLifecycleLogger() }
    val backStack = rememberNavBackStack(AppDestination.Start)
    val context = LocalContext.current
    var storagePermissionRequested by rememberSaveable { mutableStateOf(false) }
    var waitingForStoragePermission by rememberSaveable { mutableStateOf(false) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        waitingForStoragePermission = false
        if (AppState.isSignedIn) standingsViewModel.loadIfNeeded()
    }

    var initialLogin by rememberSaveable {
        mutableStateOf(AppState.userProfile?.login.orEmpty())
    }

    var loginFormVersion by rememberSaveable {
        mutableStateOf(0)
    }

    LaunchedEffect(AppState.isSignedIn) {
        val hasProtectedScreens = backStack.any { destination ->
            destination == AppDestination.Content || destination == AppDestination.Settings
        }

        if (!AppState.isSignedIn && hasProtectedScreens) {
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
        enabled = backStack.size == 1, onBack = onExit
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
                    StartScreen(initialLogin = initialLogin, onOpenRegistration = {
                        if (backStack.lastOrNull() == AppDestination.Start) {
                            backStack.add(AppDestination.Registration)
                        }
                    }, onLoginSuccess = {
                        if (backStack.lastOrNull() == AppDestination.Start) {
                            AppState.isSignedIn = true
                            backStack.clear()
                            backStack.add(AppDestination.Content)
                        }
                    })
                }
            }

            entry<AppDestination.Registration> {
                lifecycleLogger.Observe("RegistrationScreen")

                RegistrationScreen(onRegistrationComplete = { registeredLogin ->
                    if (backStack.lastOrNull() == AppDestination.Registration) {
                        initialLogin = registeredLogin
                        loginFormVersion++
                        navigateBack()
                    }
                }, onBack = {
                    if (backStack.lastOrNull() == AppDestination.Registration) {
                        navigateBack()
                    }
                })
            }

            entry<AppDestination.Content> {
                lifecycleLogger.Observe("ContentScreen")

                if (!AppState.isSignedIn) return@entry

                val state by standingsViewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(standingsViewModel) {
                    if (waitingForStoragePermission) return@LaunchedEffect

                    val needsStoragePermission =
                        Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED

                    if (needsStoragePermission && !storagePermissionRequested) {
                        storagePermissionRequested = true
                        waitingForStoragePermission = true
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
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
                    })
            }

            entry<AppDestination.Settings> {
                lifecycleLogger.Observe("SettingsScreen")
                if (!AppState.isSignedIn) return@entry

                val state by standingsViewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(standingsViewModel) {
                    standingsViewModel.loadIfNeeded()
                }

                SettingsScreen(
                    standingsViewModel = standingsViewModel, onBack = {
                        if (backStack.lastOrNull() == AppDestination.Settings) {
                            navigateBack()
                        }
                    })
            }
        })
}