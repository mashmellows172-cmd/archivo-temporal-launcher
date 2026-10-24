package com.archivotemporal.launcher.character

import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.preferences.MotionMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MissMinutesControllerTest {
    // These tests catch missing transitions, stale jobs, wrong targets and lost feedback.
    @Test fun focus_change_moves_then_points_at_latest_target() = runTest {
        val controller = MissMinutesController(backgroundScope)
        val phases = mutableListOf<MissMinutesPhase>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.state.collect { phases += it.phase }
        }
        controller.onFocusChanged(SlotId.MUSIC, 0.8f)
        runCurrent()
        assertEquals(MissMinutesPhase.Moving, controller.state.value.phase)
        assertTrue(controller.state.value.transitionMillis in 220..360)
        advanceTimeBy(controller.state.value.transitionMillis.toLong())
        runCurrent()
        assertEquals(MissMinutesPhase.Pointing, controller.state.value.phase)
        assertTrue(controller.state.value.transitionMillis in 160..220)
        advanceTimeBy(controller.state.value.transitionMillis.toLong())
        runCurrent()
        assertEquals(listOf(MissMinutesPhase.Idle, MissMinutesPhase.Moving,
            MissMinutesPhase.Pointing, MissMinutesPhase.Idle), phases)
        assertEquals(SlotId.MUSIC, controller.state.value.focusedSlot)
        assertEquals(0.8f, controller.state.value.targetX)
    }

    @Test fun rapid_focus_changes_cancel_stale_target() = runTest {
        val controller = MissMinutesController(backgroundScope)
        val pointedSlots = mutableListOf<SlotId?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.state.collect {
                if (it.phase == MissMinutesPhase.Pointing) pointedSlots += it.focusedSlot
            }
        }
        controller.onFocusChanged(SlotId.CONTINUE, 0.1f)
        runCurrent()
        advanceTimeBy(100)
        controller.onFocusChanged(SlotId.APPS, 0.9f)
        runCurrent()
        advanceTimeBy(600)
        runCurrent()
        assertEquals(listOf(SlotId.APPS), pointedSlots)
        assertEquals(0.9f, controller.state.value.targetX)
        assertEquals(MissMinutesPhase.Idle, controller.state.value.phase)
    }

    @Test fun confirm_returns_to_idle_after_timeout() = runTest {
        val controller = MissMinutesController(backgroundScope)
        controller.onFocusChanged(SlotId.TV, 0.5f)
        controller.onLaunchStarted()
        runCurrent()
        assertEquals(MissMinutesPhase.Confirming, controller.state.value.phase)
        advanceTimeBy(900)
        runCurrent()
        assertEquals(MissMinutesPhase.Idle, controller.state.value.phase)
        assertEquals(SlotId.TV, controller.state.value.focusedSlot)
    }

    @Test fun system_notice_preempts_idle_and_focus_motion() = runTest {
        for (moving in listOf(false, true)) {
            val controller = MissMinutesController(backgroundScope)
            if (moving) controller.onFocusChanged(SlotId.MOVIES, 0.3f)
            val notice = Notice("Aplicación no disponible")
            controller.onNotice(notice)
            runCurrent()
            assertEquals(MissMinutesPhase.Notifying, controller.state.value.phase)
            assertEquals(notice, controller.state.value.notice)
            advanceTimeBy(700)
            runCurrent()
            assertEquals(MissMinutesPhase.Notifying, controller.state.value.phase)
            advanceTimeBy(2_000)
            runCurrent()
            assertEquals(MissMinutesPhase.Idle, controller.state.value.phase)
            assertNull(controller.state.value.notice)
        }
    }

    @Test fun reduced_motion_skips_walk_cycle_but_updates_position() = runTest {
        val controller = MissMinutesController(backgroundScope, MotionMode.REDUCED)
        controller.onFocusChanged(SlotId.APPS, 0.9f)
        runCurrent()
        assertEquals(MissMinutesPhase.Pointing, controller.state.value.phase)
        assertEquals(0.9f, controller.state.value.targetX)
        assertEquals(MotionMode.REDUCED, controller.state.value.motionMode)
        advanceTimeBy(220)
        runCurrent()
        assertEquals(MissMinutesPhase.Idle, controller.state.value.phase)
    }

    @Test fun switching_to_reduced_motion_cancels_walk_without_losing_feedback() = runTest {
        val controller = MissMinutesController(backgroundScope)
        controller.onFocusChanged(SlotId.APPS, 0.9f)
        runCurrent()
        advanceTimeBy(80)
        controller.setMotionMode(MotionMode.REDUCED)
        runCurrent()
        assertEquals(MissMinutesPhase.Pointing, controller.state.value.phase)
        assertEquals(0.9f, controller.state.value.targetX)
        advanceTimeBy(600)
        runCurrent()
        assertEquals(MissMinutesPhase.Idle, controller.state.value.phase)
    }

    @Test fun longer_distance_takes_longer_within_motion_budget() = runTest {
        val near = MissMinutesController(backgroundScope)
        val far = MissMinutesController(backgroundScope)
        near.onFocusChanged(SlotId.TV, 0.55f)
        far.onFocusChanged(SlotId.APPS, 1f)
        runCurrent()
        assertTrue(near.state.value.transitionMillis in 220..360)
        assertTrue(far.state.value.transitionMillis in 220..360)
        assertTrue(near.state.value.transitionMillis < far.state.value.transitionMillis)
    }

    @Test fun inactivity_sleeps_and_new_focus_wakes_character() = runTest {
        val controller = MissMinutesController(backgroundScope)
        runCurrent()
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(MissMinutesPhase.Sleeping, controller.state.value.phase)
        controller.onFocusChanged(SlotId.MUSIC, 0.7f)
        runCurrent()
        assertEquals(MissMinutesPhase.Moving, controller.state.value.phase)
    }

    @Test fun focus_during_notice_is_remembered_and_runs_after_notice() = runTest {
        val controller = MissMinutesController(backgroundScope)
        controller.onNotice(Notice("Sin conexión"))
        controller.onFocusChanged(SlotId.APPS, 0.9f)
        runCurrent()
        assertEquals(MissMinutesPhase.Notifying, controller.state.value.phase)
        assertEquals(0.5f, controller.state.value.targetX)
        advanceTimeBy(2_500)
        runCurrent()
        assertEquals(MissMinutesPhase.Moving, controller.state.value.phase)
        assertEquals(SlotId.APPS, controller.state.value.focusedSlot)
        assertEquals(0.9f, controller.state.value.targetX)
        assertTrue(controller.state.value.transitionMillis in 220..360)
        advanceTimeBy(controller.state.value.transitionMillis.toLong())
        runCurrent()
        assertEquals(MissMinutesPhase.Pointing, controller.state.value.phase)
        advanceTimeBy(controller.state.value.transitionMillis.toLong())
        runCurrent()
        assertEquals(MissMinutesPhase.Idle, controller.state.value.phase)
    }
}
