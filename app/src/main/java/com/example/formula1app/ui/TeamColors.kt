package com.example.formula1app.ui

import androidx.compose.ui.graphics.Color

fun teamColor(teamId: String): Color = when (teamId) {
    "red_bull" -> Color(0xFF3671C6)
    "ferrari" -> Color(0xFFED1131)
    "mercedes" -> Color(0xFF27F4D2)
    "mclaren" -> Color(0xFFFF8000)
    "aston_martin" -> Color(0xFF229971)
    "alpine" -> Color(0xFFFF87BC)
    "williams" -> Color(0xFF64C4FF)
    "rb" -> Color(0xFF6692FF)
    "audi" -> Color(0xFFF50537)
    "cadillac" -> Color(0xFFA4A4A4)
    "haas" -> Color(0xFFB6BABD)
    else -> Color.Gray
}