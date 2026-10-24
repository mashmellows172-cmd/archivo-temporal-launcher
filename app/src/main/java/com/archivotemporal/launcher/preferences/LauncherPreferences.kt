package com.archivotemporal.launcher.preferences

import com.archivotemporal.launcher.domain.AppDestination
import com.archivotemporal.launcher.domain.HomeSlot
import com.archivotemporal.launcher.domain.SlotId
import kotlinx.coroutines.flow.Flow

enum class MotionMode { FULL, REDUCED }

enum class TextureLevel { OFF, LOW, MEDIUM }

data class LauncherSettings(
    val slots: List<HomeSlot>,
    val lastLaunched: AppDestination?,
    val motionMode: MotionMode,
    val textureLevel: TextureLevel,
    val hintsVisible: Boolean,
)

interface LauncherPreferences {
    val state: Flow<LauncherSettings>

    suspend fun assign(slotId: SlotId, packageName: String?)

    suspend fun recordLaunch(packageName: String)

    suspend fun setMotionMode(mode: MotionMode)

    suspend fun setTextureLevel(level: TextureLevel)

    suspend fun setHintsVisible(visible: Boolean)
}
