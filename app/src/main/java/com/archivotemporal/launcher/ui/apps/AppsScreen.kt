package com.archivotemporal.launcher.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.archivotemporal.launcher.apps.InstalledApp
import com.archivotemporal.launcher.preferences.TextureLevel
import com.archivotemporal.launcher.ui.components.ArchiveChoice
import com.archivotemporal.launcher.ui.components.SecondaryFrame
import com.archivotemporal.launcher.ui.theme.ArchiveColor
import com.archivotemporal.launcher.ui.theme.ArchiveType

@Composable
fun AppsScreen(
    apps: List<InstalledApp>,
    onAssign: (InstalledApp) -> Unit,
    onBack: () -> Unit,
    texture: TextureLevel = TextureLevel.LOW,
) {
    var selected by remember(apps) { mutableIntStateOf(0) }
    val gridState = rememberLazyGridState()
    LaunchedEffect(selected) {
        if (apps.isNotEmpty()) gridState.animateScrollToItem(selected)
    }
    fun move(delta: Int) {
        if (apps.isNotEmpty()) selected = (selected + delta).coerceIn(0, apps.lastIndex)
    }
    SecondaryFrame("APLICACIONES", "apps-screen", onBack, onDirection = { key ->
        when (key) {
            Key.DirectionLeft -> { move(-1); true }
            Key.DirectionRight -> { move(1); true }
            Key.DirectionUp -> { move(-3); true }
            Key.DirectionDown -> { move(3); true }
            Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                apps.getOrNull(selected)?.let(onAssign); true
            }
            else -> false
        }
    }, texture = texture) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("ELIGE UNA APLICACIÓN PARA ARCHIVAR", color = ArchiveColor.muted, style = ArchiveType.slot)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                    ArchiveChoice(
                        label = app.label,
                        detail = app.packageName,
                        selected = index == selected,
                        onClick = { selected = index; onAssign(app) },
                        modifier = Modifier.height(96.dp).testTag("apps-item-${app.packageName}"),
                    )
                }
            }
        }
    }
}
