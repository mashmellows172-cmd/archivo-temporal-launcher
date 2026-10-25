package com.archivotemporal.launcher

import android.content.Context
import android.os.Bundle
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.archivotemporal.launcher.apps.AndroidAppCatalog
import com.archivotemporal.launcher.apps.AndroidAppLauncher
import com.archivotemporal.launcher.character.MissMinutesController
import com.archivotemporal.launcher.character.Notice
import com.archivotemporal.launcher.home.HomeAction
import com.archivotemporal.launcher.home.HomeEffect
import com.archivotemporal.launcher.home.HomeFocus
import com.archivotemporal.launcher.home.HomeViewModel
import com.archivotemporal.launcher.navigation.LauncherNavigator
import com.archivotemporal.launcher.navigation.Screen
import com.archivotemporal.launcher.preferences.DataStoreLauncherPreferences
import com.archivotemporal.launcher.ui.apps.AppsScreen
import com.archivotemporal.launcher.ui.home.HomeScreen
import com.archivotemporal.launcher.ui.library.LibraryScreen
import com.archivotemporal.launcher.ui.search.SearchScreen
import com.archivotemporal.launcher.ui.settings.SettingsScreen
import com.archivotemporal.launcher.ui.theme.ArchiveTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Context.archiveDataStore by preferencesDataStore("archive_settings")

class GuideViewModel : ViewModel() {
    val controller = MissMinutesController(viewModelScope)
}

class NavigationViewModel : ViewModel() {
    val navigator = LauncherNavigator()
}

class MainActivity : ComponentActivity() {
    private val catalog by lazy { AndroidAppCatalog(applicationContext) }
    private val preferences by lazy { DataStoreLauncherPreferences(applicationContext.archiveDataStore, catalog) }
    private val launcher by lazy { AndroidAppLauncher(applicationContext) }
    private val homeViewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(preferences, catalog, launcher) as T
        }
    }
    private val guideViewModel: GuideViewModel by viewModels()
    private val navigationViewModel: NavigationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDecorFitsSystemWindows(false)
        window.decorView.post { window.insetsController?.hide(WindowInsets.Type.systemBars()) }
        setContent {
            val state by homeViewModel.state.collectAsState()
            val guide by guideViewModel.controller.state.collectAsState()
            val screen by navigationViewModel.navigator.screen.collectAsState()
            val controller = guideViewModel.controller

            LaunchedEffect(state.settings.motionMode) { controller.setMotionMode(state.settings.motionMode) }
            LaunchedEffect(state.focus) {
                val slot = (state.focus as? HomeFocus.Slot)?.id
                if (slot != null && controller.state.value.focusedSlot != slot) {
                    controller.onFocusChanged(slot, (slot.ordinal + 0.5f) / 5f)
                }
            }
            LaunchedEffect(homeViewModel) {
                withContext(Dispatchers.Main.immediate) {
                    homeViewModel.effects.collect { effect ->
                        when (effect) {
                            HomeEffect.None -> Unit
                            is HomeEffect.LaunchPackage -> {
                                controller.onLaunchStarted()
                                homeViewModel.executeLaunch(effect.packageName)
                            }
                            is HomeEffect.OpenScreen -> navigationViewModel.navigator.open(effect.screen)
                            is HomeEffect.ShowNotice -> {
                                controller.onNotice(Notice(effect.message))
                            }
                        }
                    }
                }
            }
            ArchiveTheme {
                val dispatchHome: (HomeAction) -> Unit = { action ->
                    homeViewModel.dispatch(action)
                    val requested = homeViewModel.state.value.screen
                    if (requested != com.archivotemporal.launcher.home.LauncherScreen.HOME) {
                        navigationViewModel.navigator.open(requested)
                    }
                }
                val backHome = {
                    navigationViewModel.navigator.back()
                    homeViewModel.dispatch(HomeAction.Back)
                    Unit
                }
                when (val destination = screen) {
                    Screen.Home -> HomeScreen(state, dispatchHome, guide, guide.notice?.message)
                    Screen.Applications -> AppsScreen(
                        apps = state.installedApps,
                        onAssign = { navigationViewModel.navigator.open(Screen.Library(it.packageName)) },
                        onBack = backHome,
                        texture = state.settings.textureLevel,
                    )
                    Screen.Search -> SearchScreen(
                        apps = state.installedApps,
                        onLaunch = { homeViewModel.executeLaunch(it.packageName) },
                        onBack = backHome,
                        texture = state.settings.textureLevel,
                    )
                    is Screen.Library -> LibraryScreen(
                        slots = state.settings.slots,
                        apps = state.installedApps,
                        candidatePackage = destination.candidatePackage,
                        onAssign = { slot, packageName -> lifecycleScope.launch { preferences.assign(slot, packageName) } },
                        onBack = backHome,
                        texture = state.settings.textureLevel,
                    )
                    Screen.Settings -> SettingsScreen(
                        settings = state.settings,
                        onMotionMode = { lifecycleScope.launch { preferences.setMotionMode(it) } },
                        onTextureLevel = { lifecycleScope.launch { preferences.setTextureLevel(it) } },
                        onHintsVisible = { lifecycleScope.launch { preferences.setHintsVisible(it) } },
                        onBack = backHome,
                    )
                }
            }
        }
    }
}
