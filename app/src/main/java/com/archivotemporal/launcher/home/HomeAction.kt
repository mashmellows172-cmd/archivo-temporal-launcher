package com.archivotemporal.launcher.home

import com.archivotemporal.launcher.apps.InstalledApp
import com.archivotemporal.launcher.preferences.LauncherSettings

sealed interface HomeAction {
    data object Left : HomeAction
    data object Right : HomeAction
    data object Up : HomeAction
    data object Down : HomeAction
    data object Select : HomeAction
    data object Back : HomeAction
    data object LaunchFailed : HomeAction
    data class SettingsChanged(val settings: LauncherSettings) : HomeAction
    data class AppsChanged(val apps: List<InstalledApp>) : HomeAction
}
