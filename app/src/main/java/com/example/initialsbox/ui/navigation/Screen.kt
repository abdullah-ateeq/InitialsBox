package com.example.initialsbox.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object History : Screen("history")
    object Settings : Screen("settings")
    object About : Screen("about")
}
