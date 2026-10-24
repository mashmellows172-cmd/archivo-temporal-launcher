package com.archivotemporal.launcher.apps

import android.content.Intent

object AppLaunchPolicy {
    fun resolve(leanbackIntent: Intent?, standardIntent: Intent?): Intent? =
        leanbackIntent ?: standardIntent

    fun resolve(leanbackIntent: Intent?, standardIntent: () -> Intent?): Intent? =
        leanbackIntent ?: standardIntent()
}
