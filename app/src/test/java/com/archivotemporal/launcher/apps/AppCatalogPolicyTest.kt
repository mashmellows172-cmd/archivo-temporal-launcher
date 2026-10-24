package com.archivotemporal.launcher.apps

import com.archivotemporal.launcher.domain.AppDestination
import java.text.Collator
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppCatalogPolicyTest {
    @Test
    fun select_excludes_launcher_and_duplicate_packages() {
        val apps = listOf(
            InstalledApp(AppDestination("com.archivotemporal.launcher", "Archivo Temporal", null)),
            InstalledApp(AppDestination("com.example.video", "Video", null)),
            InstalledApp(AppDestination("com.example.video", "Duplicate", null)),
        )

        val selected = AppCatalogPolicy.select(apps, "com.archivotemporal.launcher", Collator.getInstance(Locale.US))

        assertEquals(listOf("com.example.video"), selected.map { it.packageName })
        assertEquals("Video", selected.single().label)
    }

    @Test
    fun select_sorts_labels_with_locale_collator() {
        val apps = listOf(
            InstalledApp(AppDestination("com.example.z", "Zeta", null)),
            InstalledApp(AppDestination("com.example.a", "Álbum", null)),
            InstalledApp(AppDestination("com.example.b", "Beta", null)),
        )

        val selected = AppCatalogPolicy.select(apps, "com.archivotemporal.launcher", Collator.getInstance(Locale.forLanguageTag("es")))

        assertEquals(listOf("Álbum", "Beta", "Zeta"), selected.map { it.label })
    }
}
