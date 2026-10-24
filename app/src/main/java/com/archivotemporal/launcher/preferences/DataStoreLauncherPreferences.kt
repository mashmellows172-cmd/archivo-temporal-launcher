package com.archivotemporal.launcher.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.archivotemporal.launcher.apps.AppCatalog
import com.archivotemporal.launcher.domain.SlotId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DataStoreLauncherPreferences(
    private val dataStore: DataStore<Preferences>,
    private val appCatalog: AppCatalog,
) : LauncherPreferences {
    override val state: Flow<LauncherSettings> = combine(
        dataStore.data,
        appCatalog.observeInstalledApps(),
    ) { saved, installed ->
        PreferenceSanitizer.sanitize(
            assignments = slotKeys.mapKeys { it.key.name }.mapValues { saved[it.value] },
            lastLaunchedPackage = saved[lastLaunchedKey],
            installedApps = installed.map { it.destination },
            motionMode = saved[motionModeKey]?.let { value -> MotionMode.entries.firstOrNull { it.name == value } }
                ?: MotionMode.FULL,
            textureLevel = saved[textureLevelKey]?.let { value -> TextureLevel.entries.firstOrNull { it.name == value } }
                ?: TextureLevel.LOW,
            hintsVisible = saved[hintsVisibleKey] ?: true,
        )
    }

    override suspend fun assign(slotId: SlotId, packageName: String?) {
        dataStore.edit { saved ->
            val key = slotKeys.getValue(slotId)
            if (packageName != null && PreferenceSanitizer.isValidPackageName(packageName)) {
                slotKeys.forEach { (otherSlot, otherKey) ->
                    if (otherSlot != slotId && saved[otherKey] == packageName) saved.remove(otherKey)
                }
                saved[key] = packageName
            } else {
                saved.remove(key)
            }
        }
    }

    override suspend fun recordLaunch(packageName: String) {
        if (!PreferenceSanitizer.isValidPackageName(packageName)) return
        dataStore.edit { it[lastLaunchedKey] = packageName }
    }

    override suspend fun setMotionMode(mode: MotionMode) {
        dataStore.edit { it[motionModeKey] = mode.name }
    }

    override suspend fun setTextureLevel(level: TextureLevel) {
        dataStore.edit { it[textureLevelKey] = level.name }
    }

    override suspend fun setHintsVisible(visible: Boolean) {
        dataStore.edit { it[hintsVisibleKey] = visible }
    }

    private companion object {
        val slotKeys = SlotId.entries.associateWith { stringPreferencesKey("slot_${it.name.lowercase()}") }
        val lastLaunchedKey = stringPreferencesKey("last_launched_package")
        val motionModeKey = stringPreferencesKey("motion_mode")
        val textureLevelKey = stringPreferencesKey("texture_level")
        val hintsVisibleKey = booleanPreferencesKey("hints_visible")
    }
}
