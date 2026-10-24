package com.archivotemporal.launcher.home

import com.archivotemporal.launcher.apps.InstalledApp
import com.archivotemporal.launcher.domain.HomeSlots
import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.preferences.LauncherSettings
import com.archivotemporal.launcher.preferences.MotionMode
import com.archivotemporal.launcher.preferences.TextureLevel

enum class UtilityId { HOME, SEARCH, LIBRARY, SETTINGS }

enum class LauncherScreen { HOME, APPS, SEARCH, LIBRARY, SETTINGS }

sealed interface HomeFocus {
    data class Slot(val id: SlotId) : HomeFocus
    data class Utility(val id: UtilityId) : HomeFocus
}

data class HomeState(
    val settings: LauncherSettings = LauncherSettings(
        slots = HomeSlots.sanitize(emptyList(), emptySet()),
        lastLaunched = null,
        motionMode = MotionMode.FULL,
        textureLevel = TextureLevel.LOW,
        hintsVisible = true,
    ),
    val installedApps: List<InstalledApp> = emptyList(),
    val screen: LauncherScreen = LauncherScreen.HOME,
    val focus: HomeFocus = HomeFocus.Slot(SlotId.APPS),
    val lastRowSlot: SlotId = SlotId.APPS,
    val initialized: Boolean = false,
)
