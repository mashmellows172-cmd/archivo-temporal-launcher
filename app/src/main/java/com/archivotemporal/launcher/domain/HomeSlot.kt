package com.archivotemporal.launcher.domain

enum class SlotId {
    CONTINUE,
    MOVIES,
    TV,
    MUSIC,
    APPS,
}

data class HomeSlot(
    val id: SlotId,
    val suggestedLabel: String,
    val assignment: AppDestination?,
)
