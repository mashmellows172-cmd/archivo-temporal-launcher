package com.archivotemporal.launcher.character

import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.preferences.MotionMode

data class Notice(val message: String)

enum class MissMinutesPhase {
    Idle,
    Moving,
    Pointing,
    Confirming,
    Notifying,
    Sleeping,
}

data class MissMinutesState(
    val phase: MissMinutesPhase = MissMinutesPhase.Idle,
    val focusedSlot: SlotId? = null,
    val targetX: Float = 0.5f,
    val transitionMillis: Int = 0,
    val notice: Notice? = null,
    val motionMode: MotionMode = MotionMode.FULL,
)
