package com.archivotemporal.launcher.home

import com.archivotemporal.launcher.apps.AppCatalog
import com.archivotemporal.launcher.apps.AppLauncher
import com.archivotemporal.launcher.apps.InstalledApp
import com.archivotemporal.launcher.apps.LaunchResult
import com.archivotemporal.launcher.domain.AppDestination
import com.archivotemporal.launcher.domain.HomeSlots
import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.preferences.LauncherPreferences
import com.archivotemporal.launcher.preferences.LauncherSettings
import com.archivotemporal.launcher.preferences.MotionMode
import com.archivotemporal.launcher.preferences.TextureLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeViewModelTest {
    private val alpha = AppDestination("com.example.alpha", "Alpha", null)

    @Test
    fun repository_updates_replace_persistent_state() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val preferences = FakePreferences(settings())
            val catalog = FakeCatalog()
            val model = HomeViewModel(preferences, catalog, FakeLauncher(), scope)

            preferences.current.value = settings(lastLaunched = alpha)
            catalog.current.value = listOf(InstalledApp(alpha))

            assertEquals(alpha, model.state.value.settings.lastLaunched)
            assertEquals(HomeFocus.Slot(SlotId.APPS), model.state.value.focus)
            assertEquals(listOf(InstalledApp(alpha)), model.state.value.installedApps)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun select_emits_launch_once_without_storing_it_in_state() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val model = HomeViewModel(FakePreferences(settings(lastLaunched = alpha)), FakeCatalog(), FakeLauncher(), scope)

            model.dispatch(HomeAction.Select)

            assertEquals(HomeEffect.LaunchPackage(alpha.packageName), model.effects.first())
            assertNull(withTimeoutOrNull(50) { model.effects.first() })
            assertEquals(HomeFocus.Slot(SlotId.CONTINUE), model.state.value.focus)
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun failed_launch_emits_notice_and_success_records_continue_history() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val preferences = FakePreferences(settings(lastLaunched = alpha))
            val launcher = FakeLauncher(LaunchResult.NotFound)
            val model = HomeViewModel(preferences, FakeCatalog(), launcher, scope)

            model.executeLaunch(alpha.packageName)
            assertEquals(HomeEffect.ShowNotice("Aplicación no disponible"), model.effects.first())
            assertEquals(emptyList<String>(), preferences.recorded)

            launcher.nextResult = LaunchResult.Started
            model.executeLaunch(alpha.packageName)
            assertEquals(listOf(alpha.packageName), preferences.recorded)
        } finally {
            scope.cancel()
        }
    }

    private fun settings(lastLaunched: AppDestination? = null) = LauncherSettings(
        slots = HomeSlots.sanitize(emptyList(), setOf(alpha.packageName)),
        lastLaunched = lastLaunched,
        motionMode = MotionMode.FULL,
        textureLevel = TextureLevel.LOW,
        hintsVisible = true,
    )

    private class FakeCatalog : AppCatalog {
        val current = MutableStateFlow<List<InstalledApp>>(emptyList())
        override fun observeInstalledApps(): Flow<List<InstalledApp>> = current
    }

    private class FakeLauncher(var nextResult: LaunchResult = LaunchResult.Started) : AppLauncher {
        override fun launch(packageName: String): LaunchResult = nextResult
    }

    private class FakePreferences(initial: LauncherSettings) : LauncherPreferences {
        val current = MutableStateFlow(initial)
        val recorded = mutableListOf<String>()
        override val state: Flow<LauncherSettings> = current
        override suspend fun assign(slotId: SlotId, packageName: String?) = Unit
        override suspend fun recordLaunch(packageName: String) { recorded += packageName }
        override suspend fun setMotionMode(mode: MotionMode) = Unit
        override suspend fun setTextureLevel(level: TextureLevel) = Unit
        override suspend fun setHintsVisible(visible: Boolean) = Unit
    }
}
