package com.archivotemporal.launcher.apps

import kotlinx.coroutines.flow.Flow

interface AppCatalog {
    fun observeInstalledApps(): Flow<List<InstalledApp>>
}
