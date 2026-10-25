package com.archivotemporal.launcher.navigation

sealed interface Screen {
    data object Home : Screen
    data object Applications : Screen
    data object Search : Screen
    data class Library(val candidatePackage: String? = null) : Screen
    data object Settings : Screen
}
