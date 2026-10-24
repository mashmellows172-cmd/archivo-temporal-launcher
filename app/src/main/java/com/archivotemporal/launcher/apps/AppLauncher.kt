package com.archivotemporal.launcher.apps

interface AppLauncher {
    fun launch(packageName: String): LaunchResult
}

sealed interface LaunchResult {
    data object Started : LaunchResult
    data object NotFound : LaunchResult
    data class Failed(val cause: Exception) : LaunchResult
}
