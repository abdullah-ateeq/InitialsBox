package com.example.initialsbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.initialsbox.ui.InitialsBoxViewModel
import com.example.initialsbox.ui.navigation.Screen
import com.example.initialsbox.ui.screens.AboutScreen
import com.example.initialsbox.ui.screens.HistoryScreen
import com.example.initialsbox.ui.screens.HomeScreen
import com.example.initialsbox.ui.screens.SettingsScreen
import com.example.initialsbox.ui.screens.SplashScreen
import com.example.initialsbox.ui.theme.InitialsBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: InitialsBoxViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()

            InitialsBoxTheme(
                appTheme = state.appSettings.theme,
                dynamicColor = state.appSettings.dynamicColor
            ) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(
                            onSplashFinished = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToHistory = { navController.navigate(Screen.History.route) },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToAbout = { navController.navigate(Screen.About.route) }
                        )
                    }

                    composable(Screen.History.route) {
                        HistoryScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.About.route) {
                        AboutScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
