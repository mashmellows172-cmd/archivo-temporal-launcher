package com.archivotemporal.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.archivotemporal.launcher.preferences.TextureLevel
import com.archivotemporal.launcher.ui.theme.ArchiveColor
import com.archivotemporal.launcher.ui.theme.ArchiveType

@Composable
fun SecondaryFrame(
    title: String,
    testTag: String,
    onBack: () -> Unit,
    onDirection: (Key) -> Boolean,
    texture: TextureLevel = TextureLevel.LOW,
    requestInitialFocus: Boolean = true,
    content: @Composable () -> Unit,
) {
    val requester = remember { FocusRequester() }
    LaunchedEffect(requester, requestInitialFocus) {
        if (requestInitialFocus) requester.requestFocus()
    }
    TemporalFrame(
        texture = texture,
        modifier = Modifier.fillMaxSize().testTag(testTag)
            .focusRequester(requester)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.Back, Key.Escape -> { onBack(); true }
                    else -> onDirection(event.key)
                }
            }
            .focusable(),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 64.dp, vertical = 42.dp)) {
            Row(
                Modifier.fillMaxWidth().background(ArchiveColor.orange)
                    .border(2.dp, ArchiveColor.cream).padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, color = ArchiveColor.ink, style = ArchiveType.heading, modifier = Modifier.weight(1f))
                Text("ATRÁS  ←", color = ArchiveColor.ink, style = ArchiveType.slot)
            }
            Box(Modifier.fillMaxSize().padding(top = 24.dp)) { content() }
        }
    }
}
