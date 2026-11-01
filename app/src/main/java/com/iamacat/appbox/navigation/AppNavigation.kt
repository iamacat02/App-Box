package com.iamacat.appbox.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iamacat.appbox.di.AppContainer
import com.iamacat.appbox.ui.screen.AddAppsScreen
import com.iamacat.appbox.ui.screen.MainScreen
import com.iamacat.appbox.ui.screen.ManageSectionsScreen
import com.iamacat.appbox.ui.screen.SettingsScreen
import com.iamacat.appbox.ui.viewmodel.AddAppsViewModel
import com.iamacat.appbox.ui.viewmodel.MainViewModel
import com.iamacat.appbox.ui.viewmodel.ManageSectionsViewModel
import com.iamacat.appbox.ui.viewmodel.SettingsViewModel

@Composable
fun AppNavigation(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            val viewModel: MainViewModel = viewModel(factory = container.viewModelFactory)
            MainScreen(
                viewModel = viewModel,
                onNavigateToAddApps = { sectionId ->
                    navController.navigate(Screen.AddApps.createRoute(sectionId))
                },
                onNavigateToManageSections = {
                    navController.navigate(Screen.ManageSections.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.AddApps.route,
            arguments = listOf(
                navArgument(Screen.AddApps.ARG_SECTION_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sectionId = backStackEntry.arguments
                ?.getString(Screen.AddApps.ARG_SECTION_ID)
                .orEmpty()
            val viewModel: AddAppsViewModel = viewModel(
                factory = container.addAppsViewModelFactory(sectionId)
            )
            AddAppsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ManageSections.route) {
            val viewModel: ManageSectionsViewModel = viewModel(factory = container.viewModelFactory)
            ManageSectionsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(factory = container.viewModelFactory)
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
