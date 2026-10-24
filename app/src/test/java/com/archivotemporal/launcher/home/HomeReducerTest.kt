package com.archivotemporal.launcher.home

import com.archivotemporal.launcher.domain.AppDestination
import com.archivotemporal.launcher.domain.HomeSlots
import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.preferences.LauncherSettings
import com.archivotemporal.launcher.preferences.MotionMode
import com.archivotemporal.launcher.preferences.TextureLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeReducerTest {
    private val alpha = AppDestination("com.example.alpha", "Alpha", null)

    @Test
    fun left_and_right_stop_at_the_row_edges() {
        val first = state(focus = HomeFocus.Slot(SlotId.CONTINUE))
        val last = state(focus = HomeFocus.Slot(SlotId.APPS))

        assertEquals(HomeFocus.Slot(SlotId.CONTINUE), HomeReducer.reduce(first, HomeAction.Left).state.focus)
        assertEquals(HomeFocus.Slot(SlotId.APPS), HomeReducer.reduce(last, HomeAction.Right).state.focus)
    }

    @Test
    fun horizontal_input_moves_one_slot_at_a_time() {
        val fromMovies = state(focus = HomeFocus.Slot(SlotId.MOVIES))

        assertEquals(HomeFocus.Slot(SlotId.CONTINUE), HomeReducer.reduce(fromMovies, HomeAction.Left).state.focus)
        assertEquals(HomeFocus.Slot(SlotId.TV), HomeReducer.reduce(fromMovies, HomeAction.Right).state.focus)
    }

    @Test
    fun down_from_utility_restores_the_prior_row_slot() {
        val onMusic = state(focus = HomeFocus.Slot(SlotId.MUSIC))

        val onUtility = HomeReducer.reduce(onMusic, HomeAction.Up).state
        val returned = HomeReducer.reduce(onUtility, HomeAction.Down).state

        assertEquals(HomeFocus.Utility(UtilityId.SETTINGS), onUtility.focus)
        assertEquals(SlotId.MUSIC, onUtility.lastRowSlot)
        assertEquals(HomeFocus.Slot(SlotId.MUSIC), returned.focus)
    }

    @Test
    fun up_and_down_traverse_the_utility_rail() {
        val initial = state(focus = HomeFocus.Utility(UtilityId.SETTINGS))
        val up = HomeReducer.reduce(initial, HomeAction.Up).state
        val down = HomeReducer.reduce(up, HomeAction.Down).state

        assertEquals(HomeFocus.Utility(UtilityId.LIBRARY), up.focus)
        assertEquals(HomeFocus.Utility(UtilityId.SETTINGS), down.focus)
        assertEquals(HomeFocus.Utility(UtilityId.HOME), HomeReducer.reduce(state(focus = HomeFocus.Utility(UtilityId.HOME)), HomeAction.Up).state.focus)
    }

    @Test
    fun selecting_an_assigned_slot_requests_its_package() {
        val initial = state(focus = HomeFocus.Slot(SlotId.MOVIES), assignments = listOf(null, alpha))

        val result = HomeReducer.reduce(initial, HomeAction.Select)

        assertEquals(initial, result.state)
        assertEquals(HomeEffect.LaunchPackage(alpha.packageName), result.effect)
    }

    @Test
    fun selecting_an_empty_slot_opens_library_for_assignment() {
        val initial = state(focus = HomeFocus.Slot(SlotId.TV))

        val result = HomeReducer.reduce(initial, HomeAction.Select)

        assertEquals(LauncherScreen.LIBRARY, result.state.screen)
        assertEquals(HomeEffect.OpenScreen(LauncherScreen.LIBRARY), result.effect)
        assertEquals(HomeFocus.Slot(SlotId.TV), result.state.focus)
    }

    @Test
    fun selecting_empty_apps_slot_opens_apps_screen() {
        val initial = state(focus = HomeFocus.Slot(SlotId.APPS))

        val result = HomeReducer.reduce(initial, HomeAction.Select)

        assertEquals(LauncherScreen.APPS, result.state.screen)
        assertEquals(HomeEffect.OpenScreen(LauncherScreen.APPS), result.effect)
    }

    @Test
    fun continue_launches_last_successfully_opened_app() {
        val initial = state(focus = HomeFocus.Slot(SlotId.CONTINUE), lastLaunched = alpha)

        val result = HomeReducer.reduce(initial, HomeAction.Select)

        assertEquals(HomeEffect.LaunchPackage(alpha.packageName), result.effect)
    }

    @Test
    fun continue_without_history_shows_notice() {
        val initial = state(focus = HomeFocus.Slot(SlotId.CONTINUE))

        val result = HomeReducer.reduce(initial, HomeAction.Select)

        assertEquals(HomeEffect.ShowNotice("Aún no hay una aplicación reciente"), result.effect)
        assertEquals(initial, result.state)
    }

    @Test
    fun selecting_a_utility_opens_the_matching_screen_and_back_restores_focus() {
        val initial = state(focus = HomeFocus.Utility(UtilityId.SEARCH), lastRowSlot = SlotId.MOVIES)

        val opened = HomeReducer.reduce(initial, HomeAction.Select)
        val returned = HomeReducer.reduce(opened.state, HomeAction.Back)

        assertEquals(LauncherScreen.SEARCH, opened.state.screen)
        assertEquals(HomeEffect.OpenScreen(LauncherScreen.SEARCH), opened.effect)
        assertEquals(LauncherScreen.HOME, returned.state.screen)
        assertEquals(HomeFocus.Utility(UtilityId.SEARCH), returned.state.focus)
        assertEquals(HomeEffect.OpenScreen(LauncherScreen.HOME), returned.effect)
    }

    @Test
    fun back_on_home_does_not_exit_launcher() {
        val initial = state()

        val result = HomeReducer.reduce(initial, HomeAction.Back)

        assertEquals(initial, result.state)
        assertEquals(HomeEffect.None, result.effect)
    }

    @Test
    fun launch_failure_requests_a_one_shot_notice() {
        val initial = state()

        val result = HomeReducer.reduce(initial, HomeAction.LaunchFailed)

        assertEquals(initial, result.state)
        assertEquals(HomeEffect.ShowNotice("Aplicación no disponible"), result.effect)
    }

    @Test
    fun rapid_opposite_input_replaces_focus_immediately() {
        var current = state(focus = HomeFocus.Slot(SlotId.TV))

        current = HomeReducer.reduce(current, HomeAction.Right).state
        assertEquals(HomeFocus.Slot(SlotId.MUSIC), current.focus)
        current = HomeReducer.reduce(current, HomeAction.Left).state
        assertEquals(HomeFocus.Slot(SlotId.TV), current.focus)
        current = HomeReducer.reduce(current, HomeAction.Left).state
        assertEquals(HomeFocus.Slot(SlotId.MOVIES), current.focus)
    }

    @Test
    fun first_settings_update_chooses_continue_only_with_history() {
        val withHistory = HomeReducer.reduce(HomeState(), HomeAction.SettingsChanged(settings(lastLaunched = alpha)))
        val withoutHistory = HomeReducer.reduce(HomeState(), HomeAction.SettingsChanged(settings()))

        assertEquals(HomeFocus.Slot(SlotId.CONTINUE), withHistory.state.focus)
        assertEquals(HomeFocus.Slot(SlotId.APPS), withoutHistory.state.focus)
    }

    @Test
    fun subsequent_settings_updates_keep_the_current_focus() {
        val initial = state(focus = HomeFocus.Slot(SlotId.MUSIC))

        val result = HomeReducer.reduce(initial, HomeAction.SettingsChanged(settings(lastLaunched = alpha)))

        assertEquals(HomeFocus.Slot(SlotId.MUSIC), result.state.focus)
    }

    @Test
    fun input_before_settings_load_keeps_the_user_selected_focus() {
        val moved = HomeReducer.reduce(HomeState(), HomeAction.Left).state

        val loaded = HomeReducer.reduce(moved, HomeAction.SettingsChanged(settings(lastLaunched = alpha))).state

        assertEquals(HomeFocus.Slot(SlotId.MUSIC), loaded.focus)
    }

    private fun state(
        focus: HomeFocus = HomeFocus.Slot(SlotId.APPS),
        lastRowSlot: SlotId = (focus as? HomeFocus.Slot)?.id ?: SlotId.APPS,
        assignments: List<AppDestination?> = emptyList(),
        lastLaunched: AppDestination? = null,
    ) = HomeState(
        settings = settings(assignments, lastLaunched),
        focus = focus,
        lastRowSlot = lastRowSlot,
        initialized = true,
    )

    private fun settings(
        assignments: List<AppDestination?> = emptyList(),
        lastLaunched: AppDestination? = null,
    ) = LauncherSettings(
        slots = HomeSlots.sanitize(assignments.map { it ?: AppDestination("", "", null) }, setOf(alpha.packageName)),
        lastLaunched = lastLaunched,
        motionMode = MotionMode.FULL,
        textureLevel = TextureLevel.LOW,
        hintsVisible = true,
    )
}
