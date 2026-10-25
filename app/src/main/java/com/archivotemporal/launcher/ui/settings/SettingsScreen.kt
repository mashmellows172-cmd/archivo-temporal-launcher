package com.archivotemporal.launcher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.archivotemporal.launcher.preferences.LauncherSettings
import com.archivotemporal.launcher.preferences.MotionMode
import com.archivotemporal.launcher.preferences.TextureLevel
import com.archivotemporal.launcher.ui.components.ArchiveChoice
import com.archivotemporal.launcher.ui.components.SecondaryFrame

@Composable
fun SettingsScreen(
    settings: LauncherSettings,
    onMotionMode: (MotionMode) -> Unit,
    onTextureLevel: (TextureLevel) -> Unit,
    onHintsVisible: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    var selected by remember { mutableIntStateOf(0) }
    val activate = {
        when (selected) {
            0 -> onMotionMode(if (settings.motionMode == MotionMode.FULL) MotionMode.REDUCED else MotionMode.FULL)
            1 -> onTextureLevel(TextureLevel.entries[(settings.textureLevel.ordinal + 1) % TextureLevel.entries.size])
            else -> onHintsVisible(!settings.hintsVisible)
        }
    }
    SecondaryFrame("AJUSTES", "settings-screen", onBack, onDirection = { key ->
        when (key) {
            Key.DirectionUp -> { selected = (selected - 1).coerceAtLeast(0); true }
            Key.DirectionDown -> { selected = (selected + 1).coerceAtMost(2); true }
            Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> { activate(); true }
            else -> false
        }
    }, texture = settings.textureLevel) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ArchiveChoice(
                "MOVIMIENTO — ${if (settings.motionMode == MotionMode.FULL) "COMPLETO" else "REDUCIDO"}",
                selected == 0,
                { selected = 0; onMotionMode(if (settings.motionMode == MotionMode.FULL) MotionMode.REDUCED else MotionMode.FULL) },
                Modifier.testTag("setting-motion"),
            )
            ArchiveChoice(
                "TEXTURA — ${settings.textureLevel.name}",
                selected == 1,
                { selected = 1; onTextureLevel(TextureLevel.entries[(settings.textureLevel.ordinal + 1) % TextureLevel.entries.size]) },
                Modifier.testTag("setting-texture"),
            )
            ArchiveChoice(
                "MENSAJES — ${if (settings.hintsVisible) "VISIBLES" else "OCULTOS"}",
                selected == 2,
                { selected = 2; onHintsVisible(!settings.hintsVisible) },
                Modifier.testTag("setting-hints"),
            )
        }
    }
}
