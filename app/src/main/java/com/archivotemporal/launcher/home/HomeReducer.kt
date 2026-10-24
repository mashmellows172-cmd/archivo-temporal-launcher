package com.archivotemporal.launcher.home

import com.archivotemporal.launcher.domain.SlotId

object HomeReducer {
    fun reduce(state: HomeState, action: HomeAction): ReduceResult<HomeState, HomeEffect> = when (action) {
        HomeAction.Left -> moveAlongRow(state, -1)
        HomeAction.Right -> moveAlongRow(state, 1)
        HomeAction.Up -> moveVertically(state, -1)
        HomeAction.Down -> moveVertically(state, 1)
        HomeAction.Select -> select(state)
        HomeAction.Back -> if (state.screen == LauncherScreen.HOME) {
            result(state)
        } else {
            open(state, LauncherScreen.HOME)
        }
        HomeAction.LaunchFailed -> result(state, HomeEffect.ShowNotice("Aplicación no disponible"))
        is HomeAction.SettingsChanged -> {
            val initialSlot = if (action.settings.lastLaunched != null) SlotId.CONTINUE else SlotId.APPS
            val updated = if (state.initialized) {
                state.copy(settings = action.settings)
            } else {
                state.copy(
                    settings = action.settings,
                    focus = HomeFocus.Slot(initialSlot),
                    lastRowSlot = initialSlot,
                    initialized = true,
                )
            }
            result(updated)
        }
        is HomeAction.AppsChanged -> result(state.copy(installedApps = action.apps))
    }

    private fun moveAlongRow(state: HomeState, direction: Int): ReduceResult<HomeState, HomeEffect> {
        if (state.screen != LauncherScreen.HOME) return result(state)
        val focus = state.focus as? HomeFocus.Slot ?: return result(state)
        val index = (focus.id.ordinal + direction).coerceIn(0, SlotId.entries.lastIndex)
        val slot = SlotId.entries[index]
        return result(state.copy(focus = HomeFocus.Slot(slot), lastRowSlot = slot, initialized = true))
    }

    private fun moveVertically(state: HomeState, direction: Int): ReduceResult<HomeState, HomeEffect> {
        if (state.screen != LauncherScreen.HOME) return result(state)
        return when (val focus = state.focus) {
            is HomeFocus.Slot -> if (direction < 0) {
                result(state.copy(focus = HomeFocus.Utility(UtilityId.SETTINGS), lastRowSlot = focus.id, initialized = true))
            } else {
                result(state)
            }
            is HomeFocus.Utility -> {
                val next = focus.id.ordinal + direction
                when {
                    next < 0 -> result(state)
                    next > UtilityId.entries.lastIndex -> result(state.copy(focus = HomeFocus.Slot(state.lastRowSlot), initialized = true))
                    else -> result(state.copy(focus = HomeFocus.Utility(UtilityId.entries[next]), initialized = true))
                }
            }
        }
    }

    private fun select(state: HomeState): ReduceResult<HomeState, HomeEffect> {
        if (state.screen != LauncherScreen.HOME) return result(state)
        return when (val focus = state.focus) {
            is HomeFocus.Slot -> when (focus.id) {
                SlotId.CONTINUE -> state.settings.lastLaunched?.let {
                    result(state, HomeEffect.LaunchPackage(it.packageName))
                } ?: result(state, HomeEffect.ShowNotice("Aún no hay una aplicación reciente"))
                else -> state.settings.slots.firstOrNull { it.id == focus.id }?.assignment?.let {
                    result(state, HomeEffect.LaunchPackage(it.packageName))
                } ?: open(state, if (focus.id == SlotId.APPS) LauncherScreen.APPS else LauncherScreen.LIBRARY)
            }
            is HomeFocus.Utility -> open(state, when (focus.id) {
                UtilityId.HOME -> LauncherScreen.HOME
                UtilityId.SEARCH -> LauncherScreen.SEARCH
                UtilityId.LIBRARY -> LauncherScreen.LIBRARY
                UtilityId.SETTINGS -> LauncherScreen.SETTINGS
            })
        }
    }

    private fun open(state: HomeState, screen: LauncherScreen) =
        result(state.copy(screen = screen), HomeEffect.OpenScreen(screen))

    private fun result(state: HomeState, effect: HomeEffect = HomeEffect.None) = ReduceResult(state, effect)
}
