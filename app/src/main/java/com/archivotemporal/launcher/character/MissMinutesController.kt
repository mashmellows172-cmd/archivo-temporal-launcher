package com.archivotemporal.launcher.character

import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.preferences.MotionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MissMinutesController(
    private val scope: CoroutineScope,
    initialMotionMode: MotionMode = MotionMode.FULL,
) {
    private val mutableState = MutableStateFlow(MissMinutesState(motionMode = initialMotionMode))
    val state: StateFlow<MissMinutesState> = mutableState.asStateFlow()

    private var transitionJob: Job? = null
    private var sleepJob: Job? = null
    private var pendingFocus: PendingFocus? = null

    init {
        scheduleSleep()
    }

    fun onFocusChanged(slotId: SlotId, targetX: Float) {
        val safeTarget = targetX.coerceIn(0f, 1f)
        val previous = mutableState.value
        if (previous.phase == MissMinutesPhase.Notifying) {
            pendingFocus = PendingFocus(slotId, safeTarget)
            return
        }

        cancelTransitions()
        if (previous.motionMode == MotionMode.REDUCED) {
            mutableState.value = previous.copy(focusedSlot = slotId, targetX = safeTarget)
            beginPointing()
        } else {
            val duration = MissMinutesMotion.movementMillis(kotlin.math.abs(safeTarget - previous.targetX))
            mutableState.value = previous.copy(
                focusedSlot = slotId,
                targetX = safeTarget,
                phase = MissMinutesPhase.Moving,
                transitionMillis = duration,
                notice = null,
            )
            transitionJob = scope.launch {
                delay(duration.toLong())
                beginPointing()
            }
        }
    }

    fun onLaunchStarted() {
        if (mutableState.value.phase == MissMinutesPhase.Notifying) return
        cancelTransitions()
        mutableState.value = mutableState.value.copy(
            phase = MissMinutesPhase.Confirming,
            transitionMillis = MissMinutesMotion.CONFIRMING_MILLIS.toInt(),
            notice = null,
        )
        transitionJob = scope.launch {
            delay(MissMinutesMotion.CONFIRMING_MILLIS)
            becomeIdle()
        }
    }

    fun onNotice(notice: Notice) {
        val previous = mutableState.value
        if (previous.phase == MissMinutesPhase.Moving) {
            previous.focusedSlot?.let { slotId ->
                pendingFocus = PendingFocus(slotId, previous.targetX)
            }
        }
        cancelTransitions()
        mutableState.value = previous.copy(
            phase = MissMinutesPhase.Notifying,
            transitionMillis = MissMinutesMotion.NOTICE_MILLIS.toInt(),
            notice = notice,
        )
        transitionJob = scope.launch {
            delay(MissMinutesMotion.NOTICE_MILLIS)
            finishNotice()
        }
    }

    fun setMotionMode(mode: MotionMode) {
        val previous = mutableState.value
        if (previous.motionMode == mode) return
        mutableState.value = previous.copy(motionMode = mode)
        if (mode == MotionMode.REDUCED && previous.phase == MissMinutesPhase.Moving) {
            cancelTransitions()
            beginPointing()
        }
    }

    private fun beginPointing() {
        sleepJob?.cancel()
        mutableState.value = mutableState.value.copy(
            phase = MissMinutesPhase.Pointing,
            transitionMillis = MissMinutesMotion.POINTING_MILLIS,
            notice = null,
        )
        transitionJob = scope.launch {
            delay(MissMinutesMotion.POINTING_MILLIS.toLong())
            becomeIdle()
        }
    }

    private fun becomeIdle() {
        transitionJob = null
        mutableState.value = mutableState.value.copy(
            phase = MissMinutesPhase.Idle,
            transitionMillis = 0,
            notice = null,
        )
        scheduleSleep()
    }

    private fun finishNotice() {
        transitionJob = null
        val queuedFocus = pendingFocus
        pendingFocus = null
        if (queuedFocus == null) {
            becomeIdle()
        } else {
            mutableState.value = mutableState.value.copy(
                phase = MissMinutesPhase.Idle,
                transitionMillis = 0,
                notice = null,
            )
            onFocusChanged(queuedFocus.slotId, queuedFocus.targetX)
        }
    }

    private fun scheduleSleep() {
        sleepJob?.cancel()
        sleepJob = scope.launch {
            delay(MissMinutesMotion.SLEEP_AFTER_MILLIS)
            if (mutableState.value.phase == MissMinutesPhase.Idle) {
                mutableState.value = mutableState.value.copy(phase = MissMinutesPhase.Sleeping)
            }
        }
    }

    private fun cancelTransitions() {
        transitionJob?.cancel()
        transitionJob = null
        sleepJob?.cancel()
        sleepJob = null
    }

    private data class PendingFocus(val slotId: SlotId, val targetX: Float)
}
