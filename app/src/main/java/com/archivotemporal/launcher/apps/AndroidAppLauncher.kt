package com.archivotemporal.launcher.apps

import android.content.Context
import android.content.Intent

class AndroidAppLauncher(context: Context) : AppLauncher {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    override fun launch(packageName: String): LaunchResult = try {
        val intent = AppLaunchPolicy.resolve(packageManager.getLeanbackLaunchIntentForPackage(packageName)) {
            packageManager.getLaunchIntentForPackage(packageName)
        } ?: return LaunchResult.NotFound

        appContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        LaunchResult.Started
    } catch (error: Exception) {
        LaunchResult.Failed(error)
    }
}
