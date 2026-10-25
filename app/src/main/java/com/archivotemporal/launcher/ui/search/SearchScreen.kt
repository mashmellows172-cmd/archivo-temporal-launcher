package com.archivotemporal.launcher.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
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
fun SearchScreen(
    apps: List<InstalledApp>,
    onLaunch: (InstalledApp) -> Unit,
    onBack: () -> Unit,
    texture: TextureLevel = TextureLevel.LOW,
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableIntStateOf(0) }
    val inputRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val results = remember(apps, query) {
        val needle = query.trim()
        if (needle.isEmpty()) apps else apps.filter { it.label.contains(needle, ignoreCase = true) }
    }
    LaunchedEffect(inputRequester) { inputRequester.requestFocus() }
    LaunchedEffect(results.size) {
        selected = selected.coerceAtMost(results.lastIndex.coerceAtLeast(0))
    }
    LaunchedEffect(selected, results.size) {
        if (results.isNotEmpty()) listState.animateScrollToItem(selected)
    }

    SecondaryFrame("SEARCH", "search-screen", onBack, onDirection = { key ->
        when (key) {
            Key.DirectionUp -> { selected = (selected - 1).coerceAtLeast(0); true }
            Key.DirectionDown -> { if (results.isNotEmpty()) selected = (selected + 1).coerceAtMost(results.lastIndex); true }
            Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> { results.getOrNull(selected)?.let(onLaunch); true }
            else -> false
        }
    }, texture = texture, requestInitialFocus = false) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BasicTextField(
                value = query,
                onValueChange = { query = it; selected = 0 },
                modifier = Modifier.fillMaxWidth().height(64.dp).testTag("search-input")
                    .focusRequester(inputRequester).background(ArchiveColor.panel)
                    .border(3.dp, ArchiveColor.cream).padding(horizontal = 18.dp, vertical = 12.dp),
                textStyle = ArchiveType.slot.copy(color = ArchiveColor.cream),
                cursorBrush = SolidColor(ArchiveColor.orange),
                singleLine = true,
                decorationBox = { inner ->
                    if (query.isEmpty()) androidx.tv.material3.Text(
                        "ESCRIBE EL NOMBRE DE UNA APLICACIÓN",
                        color = ArchiveColor.muted,
                        style = ArchiveType.slot,
                    )
                    inner()
                },
            )
            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(results, key = { _, app -> app.packageName }) { index, app ->
                    ArchiveChoice(
                        label = app.label,
                        selected = index == selected,
                        onClick = { selected = index; onLaunch(app) },
                        modifier = Modifier.testTag("search-item-${app.packageName}"),
                    )
                }
            }
        }
    }
}
