package com.archivotemporal.launcher.apps

import com.archivotemporal.launcher.domain.AppDestination

data class InstalledApp(val destination: AppDestination) {
    val packageName: String get() = destination.packageName
    val label: String get() = destination.label
}
