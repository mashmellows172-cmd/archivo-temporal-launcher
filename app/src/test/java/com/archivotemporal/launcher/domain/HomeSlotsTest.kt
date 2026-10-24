package com.archivotemporal.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class HomeSlotsTest {
    @Test
    fun sanitize_returns_exactly_five_slots_in_spec_order() {
        val slots = HomeSlots.sanitize(emptyList(), emptySet())

        assertEquals(
            listOf(SlotId.CONTINUE, SlotId.MOVIES, SlotId.TV, SlotId.MUSIC, SlotId.APPS),
            slots.map { it.id },
        )
        assertEquals(listOf("Continue", "Movies", "TV", "Music", "Apps"), slots.map { it.suggestedLabel })
        assertEquals(5, slots.size)
        assertEquals(listOf(null, null, null, null, null), slots.map { it.assignment })
    }

    @Test
    fun sanitize_removes_duplicate_packages_preserving_first_assignment() {
        val first = AppDestination("com.example.video", "First label", null)
        val duplicate = AppDestination("com.example.video", "Later label", "later-icon")
        val next = AppDestination("com.example.music", "Music", null)

        val slots = HomeSlots.sanitize(listOf(first, duplicate, next), setOf(first.packageName, next.packageName))

        assertSame(first, slots[0].assignment)
        assertNull(slots[1].assignment)
        assertSame(next, slots[2].assignment)
    }

    @Test
    fun sanitize_clears_uninstalled_assignments_without_reordering_slots() {
        val first = AppDestination("com.example.first", "First", null)
        val removed = AppDestination("com.example.removed", "Removed", null)
        val third = AppDestination("com.example.third", "Third", null)

        val slots = HomeSlots.sanitize(listOf(first, removed, third), setOf(first.packageName, third.packageName))

        assertSame(first, slots[0].assignment)
        assertNull(slots[1].assignment)
        assertSame(third, slots[2].assignment)
        assertEquals(listOf(SlotId.CONTINUE, SlotId.MOVIES, SlotId.TV), slots.take(3).map { it.id })
    }

    @Test
    fun sanitize_clears_blank_package_names_without_reordering_slots() {
        val first = AppDestination("com.example.first", "First", null)
        val blankPackage = AppDestination("  ", "Blank", null)
        val third = AppDestination("com.example.third", "Third", null)

        val slots = HomeSlots.sanitize(
            listOf(first, blankPackage, third),
            setOf(first.packageName, blankPackage.packageName, third.packageName),
        )

        assertEquals(5, slots.size)
        assertSame(first, slots[0].assignment)
        assertNull(slots[1].assignment)
        assertSame(third, slots[2].assignment)
        assertEquals(listOf(SlotId.CONTINUE, SlotId.MOVIES, SlotId.TV), slots.take(3).map { it.id })
    }
}
