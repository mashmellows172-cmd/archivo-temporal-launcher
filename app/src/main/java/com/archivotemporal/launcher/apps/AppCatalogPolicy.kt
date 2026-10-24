package com.archivotemporal.launcher.apps

import java.text.Collator

object AppCatalogPolicy {
    fun select(apps: List<InstalledApp>, ownPackage: String, collator: Collator): List<InstalledApp> =
        apps.asSequence()
            .filter { it.packageName != ownPackage }
            .distinctBy { it.packageName }
            .sortedWith { first, second ->
                val labelOrder = collator.compare(first.label, second.label)
                if (labelOrder != 0) labelOrder else first.packageName.compareTo(second.packageName)
            }
            .toList()
}
