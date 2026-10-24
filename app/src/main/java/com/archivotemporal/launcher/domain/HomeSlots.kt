package com.archivotemporal.launcher.domain

object HomeSlots {
    private val definitions = listOf(
        SlotId.CONTINUE to "Continue",
        SlotId.MOVIES to "Movies",
        SlotId.TV to "TV",
        SlotId.MUSIC to "Music",
        SlotId.APPS to "Apps",
    )

    fun sanitize(
        assignments: List<AppDestination>,
        installedPackages: Set<String>,
    ): List<HomeSlot> {
        val seenPackages = mutableSetOf<String>()
        return definitions.mapIndexed { index, (id, suggestedLabel) ->
            val assignment = assignments.getOrNull(index)
            val sanitizedAssignment = assignment?.takeIf {
                it.packageName.isNotBlank() &&
                    it.packageName in installedPackages &&
                    seenPackages.add(it.packageName)
            }
            HomeSlot(id, suggestedLabel, sanitizedAssignment)
        }
    }
}
