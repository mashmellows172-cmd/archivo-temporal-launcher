package com.archivotemporal.launcher.preferences

import com.archivotemporal.launcher.domain.AppDestination
import com.archivotemporal.launcher.domain.HomeSlots
import com.archivotemporal.launcher.domain.SlotId

object PreferenceSanitizer {
    private val packagePattern = Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+")

    fun sanitize(
        assignments: Map<String, String?>,
        lastLaunchedPackage: String?,
        installedApps: List<AppDestination>,
        motionMode: MotionMode = MotionMode.FULL,
        textureLevel: TextureLevel = TextureLevel.LOW,
        hintsVisible: Boolean = true,
    ): LauncherSettings {
        val installedByPackage = installedApps
            .filter { isValidPackageName(it.packageName) }
            .associateBy { it.packageName }
        val orderedAssignments = SlotId.entries.map { slotId ->
            assignments[slotId.name]?.let(installedByPackage::get)
        }
        val slots = HomeSlots.sanitize(
            orderedAssignments.map { it ?: AppDestination("", "", null) },
            installedByPackage.keys,
        )
        return LauncherSettings(
            slots = slots,
            lastLaunched = lastLaunchedPackage?.let(installedByPackage::get),
            motionMode = motionMode,
            textureLevel = textureLevel,
            hintsVisible = hintsVisible,
        )
    }

    fun isValidPackageName(packageName: String): Boolean = packagePattern.matches(packageName)
}
