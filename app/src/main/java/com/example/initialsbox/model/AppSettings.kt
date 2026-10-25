package com.example.initialsbox.model

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val liveGeneration: Boolean = true,
    val haptics: Boolean = true,
    val animations: Boolean = true,
    val confirmClear: Boolean = true,
    val confirmDeleteHistory: Boolean = true,
    val restoreLastText: Boolean = true
)
