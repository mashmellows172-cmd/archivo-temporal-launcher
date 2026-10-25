package com.archivotemporal.launcher.navigation

import com.archivotemporal.launcher.home.LauncherScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherNavigator(initialScreen: Screen = Screen.Home) {
    private val mutableScreen = MutableStateFlow(initialScreen)
    val screen: StateFlow<Screen> = mutableScreen.asStateFlow()

    fun open(screen: Screen) {
        mutableScreen.value = screen
    }

    fun open(screen: LauncherScreen) {
        open(
            when (screen) {
                LauncherScreen.HOME -> Screen.Home
                LauncherScreen.APPS -> Screen.Applications
                LauncherScreen.SEARCH -> Screen.Search
                LauncherScreen.LIBRARY -> Screen.Library()
                LauncherScreen.SETTINGS -> Screen.Settings
            },
        )
    }

    fun back(): Boolean {
        if (mutableScreen.value == Screen.Home) return false
        mutableScreen.value = Screen.Home
        return true
    }
}
