package com.archivotemporal.launcher.preferences

import com.archivotemporal.launcher.domain.AppDestination
import com.archivotemporal.launcher.domain.SlotId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreferenceSanitizerTest {
    private val alpha = AppDestination("com.example.alpha", "Alpha", "alpha")
    private val beta = AppDestination("com.example.beta", "Beta", null)
    private val gamma = AppDestination("com.example.gamma", "Gamma", null)
    private val delta = AppDestination("com.example.delta", "Delta", null)
    private val epsilon = AppDestination("com.example.epsilon", "Epsilon", null)

    @Test
    fun duplicate_package_keeps_its_first_slot_and_leaves_the_later_slot_empty() {
        val settings = PreferenceSanitizer.sanitize(
            assignments = mapOf("CONTINUE" to alpha.packageName, "MOVIES" to alpha.packageName, "TV" to beta.packageName),
            lastLaunchedPackage = null,
            installedApps = listOf(alpha, beta),
        )

        assertEquals(listOf(alpha, null, beta, null, null), settings.slots.map { it.assignment })
    }

    @Test
    fun blank_and_malformed_packages_are_removed_even_if_catalog_contains_them() {
        val malformed = AppDestination("com.example/broken", "Broken", null)
        val settings = PreferenceSanitizer.sanitize(
            assignments = mapOf("CONTINUE" to "  ", "MOVIES" to malformed.packageName, "TV" to beta.packageName),
            lastLaunchedPackage = "  ",
            installedApps = listOf(malformed, beta),
        )

        assertEquals(listOf(null, null, beta, null, null), settings.slots.map { it.assignment })
        assertNull(settings.lastLaunched)
    }

    @Test
    fun more_than_five_entries_keep_only_the_five_known_slots_in_order() {
        val settings = PreferenceSanitizer.sanitize(
            assignments = linkedMapOf(
                "EXTRA" to beta.packageName,
                "APPS" to epsilon.packageName,
                "MUSIC" to delta.packageName,
                "TV" to gamma.packageName,
                "MOVIES" to beta.packageName,
                "CONTINUE" to alpha.packageName,
            ),
            lastLaunchedPackage = null,
            installedApps = listOf(alpha, beta, gamma, delta, epsilon),
        )

        assertEquals(listOf(SlotId.CONTINUE, SlotId.MOVIES, SlotId.TV, SlotId.MUSIC, SlotId.APPS), settings.slots.map { it.id })
        assertEquals(listOf(alpha, beta, gamma, delta, epsilon), settings.slots.map { it.assignment })
        assertEquals(5, settings.slots.size)
    }

    @Test
    fun unknown_slot_id_cannot_claim_a_package_from_a_known_slot() {
        val settings = PreferenceSanitizer.sanitize(
            assignments = linkedMapOf("OTHER" to alpha.packageName, "CONTINUE" to alpha.packageName),
            lastLaunchedPackage = null,
            installedApps = listOf(alpha),
        )

        assertEquals(alpha, settings.slots.single { it.id == SlotId.CONTINUE }.assignment)
    }

    @Test
    fun removed_last_launched_package_is_cleared_without_changing_valid_favorites() {
        val settings = PreferenceSanitizer.sanitize(
            assignments = mapOf("MOVIES" to alpha.packageName),
            lastLaunchedPackage = "com.example.removed",
            installedApps = listOf(alpha),
        )

        assertNull(settings.lastLaunched)
        assertEquals(alpha, settings.slots.single { it.id == SlotId.MOVIES }.assignment)
    }

    @Test
    fun installed_last_launched_package_resolves_to_current_catalog_metadata() {
        val settings = PreferenceSanitizer.sanitize(
            assignments = emptyMap(),
            lastLaunchedPackage = alpha.packageName,
            installedApps = listOf(alpha),
        )

        assertEquals(alpha, settings.lastLaunched)
    }
}
