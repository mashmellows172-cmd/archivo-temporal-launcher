package com.archivotemporal.launcher.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.archivotemporal.launcher.apps.InstalledApp
import com.archivotemporal.launcher.domain.HomeSlot
import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.preferences.TextureLevel
import com.archivotemporal.launcher.ui.components.ArchiveChoice
import com.archivotemporal.launcher.ui.components.SecondaryFrame
import com.archivotemporal.launcher.ui.theme.ArchiveColor
import com.archivotemporal.launcher.ui.theme.ArchiveType

@Composable
fun LibraryScreen(
    slots: List<HomeSlot>,
    apps: List<InstalledApp>,
    candidatePackage: String?,
    onAssign: (SlotId, String?) -> Unit,
    onBack: () -> Unit,
    texture: TextureLevel = TextureLevel.LOW,
) {
    var selectedSlot by remember { mutableStateOf<SlotId?>(null) }
    var slotIndex by remember { mutableIntStateOf(0) }
    var appIndex by remember { mutableIntStateOf(0) }
    var choosingApps by remember { mutableStateOf(false) }
    val appListState = rememberLazyListState()
    val orderedSlots = SlotId.entries.map { id -> slots.firstOrNull { it.id == id } ?: HomeSlot(id, id.name, null) }
    androidx.compose.runtime.LaunchedEffect(appIndex, choosingApps) {
        if (choosingApps && apps.isNotEmpty()) appListState.animateScrollToItem(appIndex)
    }

    fun chooseSlot(index: Int) {
        slotIndex = index
        val id = orderedSlots[index].id
        selectedSlot = id
        if (candidatePackage != null) onAssign(id, candidatePackage) else choosingApps = true
    }

    SecondaryFrame("LIBRARY", "library-screen", onBack, onDirection = { key ->
        when (key) {
            Key.DirectionLeft -> { if (!choosingApps) slotIndex = (slotIndex - 1).coerceAtLeast(0); true }
            Key.DirectionRight -> { if (!choosingApps) slotIndex = (slotIndex + 1).coerceAtMost(orderedSlots.lastIndex); true }
            Key.DirectionUp -> { if (choosingApps) appIndex = (appIndex - 1).coerceAtLeast(0); true }
            Key.DirectionDown -> { if (choosingApps && apps.isNotEmpty()) appIndex = (appIndex + 1).coerceAtMost(apps.lastIndex); true }
            Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                if (choosingApps) selectedSlot?.let { slot -> apps.getOrNull(appIndex)?.let { onAssign(slot, it.packageName) } }
                else chooseSlot(slotIndex)
                true
            }
            else -> false
        }
    }, texture = texture) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                if (candidatePackage == null) "1. ELIGE DESTINO   2. ELIGE APLICACIÓN" else "ELIGE EL DESTINO DEL ARCHIVO",
                color = ArchiveColor.muted,
                style = ArchiveType.slot,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                orderedSlots.forEachIndexed { index, slot ->
                    ArchiveChoice(
                        label = slotLabel(slot.id),
                        detail = slot.assignment?.label ?: "VACÍO",
                        selected = index == slotIndex || selectedSlot == slot.id,
                        onClick = { chooseSlot(index) },
                        modifier = Modifier.weight(1f).height(112.dp)
                            .testTag("library-slot-${slot.id.name.lowercase()}"),
                    )
                }
            }
            if (selectedSlot != null) {
                ArchiveChoice(
                    label = "LIMPIAR DESTINO",
                    selected = false,
                    onClick = { selectedSlot?.let { onAssign(it, null) } },
                    modifier = Modifier.testTag("library-clear"),
                )
            }
            if (choosingApps) {
                LazyColumn(state = appListState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                        ArchiveChoice(
                            label = app.label,
                            selected = index == appIndex,
                            onClick = { appIndex = index; selectedSlot?.let { onAssign(it, app.packageName) } },
                            modifier = Modifier.testTag("library-app-${app.packageName}"),
                        )
                    }
                }
            }
        }
    }
}

private fun slotLabel(id: SlotId) = when (id) {
    SlotId.CONTINUE -> "RECIENTE"
    SlotId.MOVIES -> "CINE"
    SlotId.TV -> "TV"
    SlotId.MUSIC -> "MÚSICA"
    SlotId.APPS -> "APPS"
}
