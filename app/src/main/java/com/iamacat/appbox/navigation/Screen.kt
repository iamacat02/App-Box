package com.iamacat.appbox.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object ManageSections : Screen("manage_sections")
    data object Settings : Screen("settings")
    data object AddApps : Screen("add_apps/{sectionId}") {
        const val ARG_SECTION_ID = "sectionId"
        fun createRoute(sectionId: String): String = "add_apps/$sectionId"
    }
}
