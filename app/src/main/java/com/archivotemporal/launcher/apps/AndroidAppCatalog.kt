package com.archivotemporal.launcher.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.archivotemporal.launcher.domain.AppDestination
import java.text.Collator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class AndroidAppCatalog(context: Context) : AppCatalog {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    override fun observeInstalledApps(): Flow<List<InstalledApp>> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                launch { send(queryInstalledApps()) }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        appContext.registerReceiver(receiver, filter)
        try {
            send(queryInstalledApps())
            awaitCancellation()
        } finally {
            appContext.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

    fun loadIcon(packageName: String): Drawable? = try {
        packageManager.getApplicationIcon(packageName)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    @Suppress("DEPRECATION")
    private fun queryInstalledApps(): List<InstalledApp> {
        val categories = listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)
        val discovered = categories.flatMap { category ->
            val query = Intent(Intent.ACTION_MAIN).addCategory(category)
            packageManager.queryIntentActivities(query, 0).mapNotNull { activity ->
                val packageName = activity.activityInfo?.packageName ?: return@mapNotNull null
                val label = activity.loadLabel(packageManager).toString().ifBlank { packageName }
                InstalledApp(AppDestination(packageName, label, packageName))
            }
        }
        return AppCatalogPolicy.select(discovered, appContext.packageName, Collator.getInstance())
    }
}
