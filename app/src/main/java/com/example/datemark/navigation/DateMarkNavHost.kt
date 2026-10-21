package com.example.datemark.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.datemark.ui.DateMarkViewModel
import com.example.datemark.ui.components.DateMarkBottomBar
import com.example.datemark.ui.screens.AboutScreen
import com.example.datemark.ui.screens.AddEditDateScreen
import com.example.datemark.ui.screens.DateDetailScreen
import com.example.datemark.ui.screens.HomeScreen
import com.example.datemark.ui.screens.SettingsScreen
import com.example.datemark.ui.screens.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object DateDetail : Screen("date_detail")
    object AddEditDate : Screen("add_edit_date")
    object Settings : Screen("settings")
    object About : Screen("about")
}

@Composable
fun DateMarkNavHost(
    viewModel: DateMarkViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Settings.route,
        Screen.About.route
    )

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                DateMarkBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    uiState = uiState,
                    onSearchChange = viewModel::onSearchQueryChange,
                    onFilterChange = viewModel::onFilterChange,
                    onSortChange = viewModel::onSortChange,
                    onDateClick = { id ->
                        viewModel.onSelectDateMark(id)
                        navController.navigate(Screen.DateDetail.route)
                    },
                    onAddClick = {
                        viewModel.prepareAddDate()
                        navController.navigate(Screen.AddEditDate.route)
                    }
                )
            }

            composable(Screen.DateDetail.route) {
                DateDetailScreen(
                    item = uiState.selectedDetailItem,
                    confirmDeleteSetting = uiState.settings.confirmDelete,
                    hapticEnabled = uiState.settings.hapticFeedback,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id ->
                        viewModel.prepareEditDate(id)
                        navController.navigate(Screen.AddEditDate.route)
                    },
                    onDeleteConfirm = { id ->
                        viewModel.deleteDate(id) {
                            navController.popBackStack()
                        }
                    },
                    getShareText = viewModel::getShareText
                )
            }

            composable(Screen.AddEditDate.route) {
                AddEditDateScreen(
                    uiState = uiState,
                    onLabelChange = viewModel::onLabelChange,
                    onDateChange = viewModel::onSelectedDateChange,
                    onNoteChange = viewModel::onNoteChange,
                    onRecurrenceChange = viewModel::onRecurrenceChange,
                    onSaveClick = {
                        viewModel.saveDate {
                            navController.popBackStack()
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = uiState.settings,
                    today = viewModel.getToday(),
                    onThemeChange = viewModel::updateTheme,
                    onDynamicColorChange = viewModel::updateDynamicColor,
                    onHapticChange = viewModel::updateHapticFeedback,
                    onAnimationsChange = viewModel::updateAnimations,
                    onConfirmDeleteChange = viewModel::updateConfirmDelete,
                    onDateFormatChange = viewModel::updateDateFormat,
                    onDefaultSortChange = viewModel::updateDefaultSort,
                    onResetSettings = viewModel::resetSettings,
                    onClearAllDates = viewModel::clearAllDates
                )
            }

            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
