package com.archivotemporal.launcher

import android.content.Context
import android.os.Bundle
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.tv.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.key
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
import com.archivotemporal.launcher.home.LauncherScreen
import com.archivotemporal.launcher.preferences.DataStoreLauncherPreferences
import com.archivotemporal.launcher.ui.home.HomeScreen
import com.archivotemporal.launcher.ui.theme.ArchiveColor
import com.archivotemporal.launcher.ui.theme.ArchiveTheme
import com.archivotemporal.launcher.ui.theme.ArchiveType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

private val Context.archiveDataStore by preferencesDataStore("archive_settings")

class GuideViewModel : ViewModel() {
    val controller = MissMinutesController(viewModelScope)
}

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels {
        val appContext = applicationContext
        val catalog = AndroidAppCatalog(appContext)
        val preferences = DataStoreLauncherPreferences(appContext.archiveDataStore, catalog)
        val launcher = AndroidAppLauncher(appContext)
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(preferences, catalog, launcher) as T
        }
    }
    private val guideViewModel: GuideViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDecorFitsSystemWindows(false)
        window.decorView.post { window.insetsController?.hide(WindowInsets.Type.systemBars()) }
        setContent {
            val state by homeViewModel.state.collectAsState()
            val guide by guideViewModel.controller.state.collectAsState()
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
                            is HomeEffect.OpenScreen -> Unit
                            is HomeEffect.ShowNotice -> {
                                controller.onNotice(Notice(effect.message))
                            }
                        }
                    }
                }
            }
            ArchiveTheme {
                if (state.screen == LauncherScreen.HOME) {
                    HomeScreen(state, homeViewModel::dispatch, guide, guide.notice?.message)
                } else {
                    Box(Modifier.fillMaxSize().onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyDown && (it.key == Key.Back || it.key == Key.Escape)) {
                            homeViewModel.dispatch(HomeAction.Back)
                            true
                        } else false
                    }.focusable()) {
                        Text(state.screen.name, color = ArchiveColor.cream, style = ArchiveType.heading)
                    }
                }
            }
        }
    }
}
