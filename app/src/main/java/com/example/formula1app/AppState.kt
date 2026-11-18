package com.example.formula1app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppState {

    var userProfile: UserProfile? by mutableStateOf(null)
}