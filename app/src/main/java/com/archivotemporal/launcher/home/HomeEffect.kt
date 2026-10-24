package com.archivotemporal.launcher.home

sealed interface HomeEffect {
    data object None : HomeEffect
    data class LaunchPackage(val packageName: String) : HomeEffect
    data class OpenScreen(val screen: LauncherScreen) : HomeEffect
    data class ShowNotice(val message: String) : HomeEffect
}

data class ReduceResult<S, E>(val state: S, val effect: E)
