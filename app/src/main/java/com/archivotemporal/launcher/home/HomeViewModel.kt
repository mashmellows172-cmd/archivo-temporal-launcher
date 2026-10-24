package com.archivotemporal.launcher.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archivotemporal.launcher.apps.AppCatalog
import com.archivotemporal.launcher.apps.AppLauncher
import com.archivotemporal.launcher.apps.LaunchResult
import com.archivotemporal.launcher.preferences.LauncherPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val preferences: LauncherPreferences,
    catalog: AppCatalog,
    private val launcher: AppLauncher,
    coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val scope = coroutineScope ?: viewModelScope
    private val mutableState = MutableStateFlow(HomeState())
    private val effectChannel = Channel<HomeEffect>(Channel.BUFFERED)

    val state: StateFlow<HomeState> = mutableState.asStateFlow()
    val effects: Flow<HomeEffect> = effectChannel.receiveAsFlow()

    init {
        scope.launch {
            preferences.state.collect { dispatch(HomeAction.SettingsChanged(it)) }
        }
        scope.launch {
            catalog.observeInstalledApps().collect { dispatch(HomeAction.AppsChanged(it)) }
        }
    }

    fun dispatch(action: HomeAction) {
        val result = HomeReducer.reduce(mutableState.value, action)
        mutableState.value = result.state
        if (result.effect != HomeEffect.None) {
            scope.launch { effectChannel.send(result.effect) }
        }
    }

    fun executeLaunch(packageName: String) {
        when (launcher.launch(packageName)) {
            LaunchResult.Started -> scope.launch { preferences.recordLaunch(packageName) }
            LaunchResult.NotFound, is LaunchResult.Failed -> dispatch(HomeAction.LaunchFailed)
        }
    }
}
