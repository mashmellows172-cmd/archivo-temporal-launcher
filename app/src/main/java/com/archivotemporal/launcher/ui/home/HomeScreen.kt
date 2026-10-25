package com.archivotemporal.launcher.ui.home

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.archivotemporal.launcher.character.MissMinutesCanvas
import com.archivotemporal.launcher.character.MissMinutesState
import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.home.HomeAction
import com.archivotemporal.launcher.home.HomeFocus
import com.archivotemporal.launcher.home.HomeState
import com.archivotemporal.launcher.home.UtilityId
import com.archivotemporal.launcher.ui.components.AppSlotCard
import com.archivotemporal.launcher.ui.components.ArchiveHeader
import com.archivotemporal.launcher.ui.components.TemporalFrame
import com.archivotemporal.launcher.ui.components.UtilityRail
import com.archivotemporal.launcher.ui.theme.ArchiveColor
import com.archivotemporal.launcher.ui.theme.ArchiveType

@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    missMinutesState: MissMinutesState = MissMinutesState(),
    notice: String? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val slotHeight = 126.dp + (64f * (LocalDensity.current.fontScale - 1f).coerceAtLeast(0f)).dp
    val activateSlot: (SlotId) -> Unit = { id ->
        val focus = state.focus
        if (focus is HomeFocus.Utility) {
            repeat(UtilityId.entries.lastIndex - focus.id.ordinal + 1) { onAction(HomeAction.Down) }
        }
        val current = (focus as? HomeFocus.Slot)?.id ?: state.lastRowSlot
        val distance = id.ordinal - current.ordinal
        repeat(kotlin.math.abs(distance)) {
            onAction(if (distance > 0) HomeAction.Right else HomeAction.Left)
        }
        onAction(HomeAction.Select)
    }
    val activateUtility: (UtilityId) -> Unit = { id ->
        val focus = state.focus
        if (focus is HomeFocus.Slot) onAction(HomeAction.Up)
        val current = (focus as? HomeFocus.Utility)?.id ?: UtilityId.SETTINGS
        val distance = id.ordinal - current.ordinal
        repeat(kotlin.math.abs(distance)) {
            onAction(if (distance > 0) HomeAction.Down else HomeAction.Up)
        }
        onAction(HomeAction.Select)
    }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    DisposableEffect(lifecycleOwner, focusRequester) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) focusRequester.requestFocus()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    TemporalFrame(
        texture = state.settings.textureLevel,
        modifier = Modifier.fillMaxSize().testTag("home")
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val action = when (event.key) {
                    Key.DirectionLeft -> HomeAction.Left
                    Key.DirectionRight -> HomeAction.Right
                    Key.DirectionUp -> HomeAction.Up
                    Key.DirectionDown -> HomeAction.Down
                    Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> HomeAction.Select
                    Key.Back, Key.Escape -> HomeAction.Back
                    else -> null
                }
                if (action == null) false else { onAction(action); true }
            }
            .focusable(),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val insetX = maxWidth * 0.05f
            val insetY = maxHeight * 0.05f
            Box(Modifier.fillMaxSize().padding(horizontal = insetX, vertical = insetY)) {
                ArchiveHeader(Modifier.align(Alignment.TopCenter))
                UtilityRail(
                    focused = (state.focus as? HomeFocus.Utility)?.id,
                    onSelect = activateUtility,
                    modifier = Modifier.align(Alignment.TopStart).padding(top = 88.dp).width(104.dp),
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(slotHeight),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SlotId.entries.forEach { id ->
                        AppSlotCard(
                            id = id,
                            label = when (id) {
                                SlotId.CONTINUE -> "Continuar"
                                SlotId.MOVIES -> "Películas"
                                SlotId.TV -> "TV"
                                SlotId.MUSIC -> "Música"
                                SlotId.APPS -> "Aplicaciones"
                            },
                            focused = (state.focus as? HomeFocus.Slot)?.id == id,
                            onSelect = { activateSlot(id) },
                            modifier = Modifier.weight(1f).fillMaxSize(),
                        )
                    }
                }
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(112.dp)
                        .offset(y = -(slotHeight + 12.dp)).testTag("miss-minutes"),
                ) {
                    MissMinutesCanvas(missMinutesState, Modifier.fillMaxSize())
                }
                Text(
                    text = notice.orEmpty(),
                    color = ArchiveColor.cream,
                    style = ArchiveType.utility,
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 84.dp).testTag("notice"),
                )
                Text(
                    "A.T. / ARCHIVO LOCAL                       01 — 05",
                    color = ArchiveColor.muted,
                    style = ArchiveType.micro,
                    modifier = Modifier.align(Alignment.BottomStart).offset(y = 18.dp),
                )
            }
        }
    }
}
