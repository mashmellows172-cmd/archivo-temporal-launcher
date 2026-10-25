package com.archivotemporal.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.ui.theme.ArchiveColor
import com.archivotemporal.launcher.ui.theme.ArchiveType

@Composable
fun AppSlotCard(id: SlotId, label: String, focused: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val foreground = if (focused) ArchiveColor.ink else ArchiveColor.cream
    Column(
        modifier = modifier
            .testTag("slot-${id.name.lowercase()}")
            .semantics { selected = focused; role = Role.Button; onClick { onSelect(); true } }
            .background(if (focused) ArchiveColor.orange else ArchiveColor.panel)
            .border(if (focused) 3.dp else 1.dp, if (focused) ArchiveColor.cream else ArchiveColor.orange)
            .padding(if (focused) 5.dp else 7.dp)
            .border(if (focused) 2.dp else 0.dp, if (focused) ArchiveColor.ink else ArchiveColor.panel)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start,
    ) {
        SlotGlyph(id, foreground, Modifier.weight(1f))
        Text(label, color = foreground, style = ArchiveType.slot, maxLines = 2)
    }
}

@Composable
private fun SlotGlyph(id: SlotId, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width.coerceAtMost(48.dp.toPx())
        val h = size.height.coerceAtMost(38.dp.toPx())
        val x = 3.dp.toPx()
        val y = 2.dp.toPx()
        val stroke = 2.dp.toPx()
        when (id) {
            SlotId.CONTINUE -> {
                drawCircle(color, h * 0.38f, Offset(x + h * 0.43f, y + h * 0.48f), style = Stroke(stroke))
                drawLine(color, Offset(x + h * 0.43f, y + h * 0.48f), Offset(x + h * 0.43f, y + h * 0.23f), stroke)
                drawLine(color, Offset(x + h * 0.43f, y + h * 0.48f), Offset(x + h * 0.64f, y + h * 0.58f), stroke)
            }
            SlotId.MOVIES -> {
                drawRect(color, Offset(x, y + h * 0.23f), androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.62f), style = Stroke(stroke))
                drawLine(color, Offset(x, y + h * 0.36f), Offset(x + w * 0.8f, y + h * 0.36f), stroke)
            }
            SlotId.TV -> {
                drawRect(color, Offset(x, y + h * 0.1f), androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.62f), style = Stroke(stroke))
                drawLine(color, Offset(x + w * 0.28f, y + h * 0.85f), Offset(x + w * 0.52f, y + h * 0.85f), stroke)
            }
            SlotId.MUSIC -> {
                drawLine(color, Offset(x + w * 0.45f, y + h * 0.15f), Offset(x + w * 0.45f, y + h * 0.68f), stroke)
                drawLine(color, Offset(x + w * 0.45f, y + h * 0.15f), Offset(x + w * 0.73f, y + h * 0.08f), stroke)
                drawCircle(color, h * 0.12f, Offset(x + w * 0.34f, y + h * 0.72f))
            }
            SlotId.APPS -> repeat(4) { index ->
                drawRect(color, Offset(x + (index % 2) * w * 0.3f, y + (index / 2) * h * 0.42f),
                    androidx.compose.ui.geometry.Size(w * 0.19f, h * 0.24f), style = Stroke(stroke))
            }
        }
    }
}
