package com.archivotemporal.launcher.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.archivotemporal.launcher.apps.AppCatalog
import com.archivotemporal.launcher.apps.InstalledApp
import com.archivotemporal.launcher.domain.AppDestination
import com.archivotemporal.launcher.domain.SlotId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStoreLauncherPreferencesTest {
    @Test
    fun assigning_an_existing_favorite_moves_it_to_the_selected_slot() = runBlocking {
        val alpha = InstalledApp(AppDestination("com.example.alpha", "Alpha", "icon"))
        val catalog = MutableCatalog(listOf(alpha))
        val preferences = DataStoreLauncherPreferences(MemoryDataStore(), catalog)
        preferences.assign(SlotId.CONTINUE, alpha.packageName)
        preferences.assign(SlotId.MOVIES, alpha.packageName)

        val settings = preferences.state.first()

        assertEquals(null, settings.slots[0].assignment)
        assertEquals(alpha.destination, settings.slots[1].assignment)
    }

    @Test
    fun reading_state_again_after_uninstall_clears_favorite_and_continue_target() = runBlocking {
        val alpha = InstalledApp(AppDestination("com.example.alpha", "Alpha", "icon"))
        val catalog = MutableCatalog(listOf(alpha))
        val preferences = DataStoreLauncherPreferences(MemoryDataStore(), catalog)
        preferences.assign(SlotId.MOVIES, alpha.packageName)
        preferences.recordLaunch(alpha.packageName)
        assertEquals(alpha.destination, preferences.state.first().lastLaunched)

        catalog.setApps(emptyList())
        val afterUninstall = preferences.state.first()

        assertEquals(null, afterUninstall.slots[1].assignment)
        assertEquals(null, afterUninstall.lastLaunched)
    }

    @Test
    fun display_options_are_reflected_in_subsequent_reads() = runBlocking {
        val preferences = DataStoreLauncherPreferences(MemoryDataStore(), MutableCatalog(emptyList()))

        preferences.setMotionMode(MotionMode.REDUCED)
        preferences.setTextureLevel(TextureLevel.OFF)
        preferences.setHintsVisible(false)

        val settings = preferences.state.first()
        assertEquals(MotionMode.REDUCED, settings.motionMode)
        assertEquals(TextureLevel.OFF, settings.textureLevel)
        assertEquals(false, settings.hintsVisible)
    }

    private class MutableCatalog(initial: List<InstalledApp>) : AppCatalog {
        private val apps = MutableStateFlow(initial)
        override fun observeInstalledApps(): Flow<List<InstalledApp>> = apps
        fun setApps(installed: List<InstalledApp>) { apps.value = installed }
    }

    private class MemoryDataStore : DataStore<Preferences> {
        private val saved = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = saved

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updated = transform(saved.value)
            saved.value = updated
            return updated
        }
    }
}
