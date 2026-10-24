package com.archivotemporal.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.archivotemporal.launcher.home.UtilityId
import com.archivotemporal.launcher.ui.theme.ArchiveColor
import com.archivotemporal.launcher.ui.theme.ArchiveType

@Composable
fun UtilityRail(focused: UtilityId?, onSelect: (UtilityId) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UtilityId.entries.forEach { id ->
            val selected = focused == id
            val ink = if (selected) ArchiveColor.ink else ArchiveColor.muted
            Column(
                Modifier.fillMaxWidth().height(56.dp)
                    .testTag("utility-${id.name.lowercase()}")
                    .semantics { this.selected = selected; role = Role.Button; onClick { onSelect(id); true } }
                    .background(if (selected) ArchiveColor.orange else ArchiveColor.panel)
                    .border(if (selected) 2.dp else 1.dp, if (selected) ArchiveColor.cream else ArchiveColor.technical)
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Canvas(Modifier.height(27.dp).fillMaxWidth()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val s = 8.dp.toPx()
                    val stroke = 1.5.dp.toPx()
                    when (id) {
                        UtilityId.HOME -> {
                            drawLine(ink, Offset(cx - s, cy), Offset(cx, cy - s), stroke)
                            drawLine(ink, Offset(cx, cy - s), Offset(cx + s, cy), stroke)
                            drawRect(ink, Offset(cx - s * 0.7f, cy), androidx.compose.ui.geometry.Size(s * 1.4f, s), style = Stroke(stroke))
                        }
                        UtilityId.SEARCH -> {
                            drawCircle(ink, s * 0.65f, Offset(cx - s * 0.2f, cy - s * 0.2f), style = Stroke(stroke))
                            drawLine(ink, Offset(cx + s * 0.3f, cy + s * 0.3f), Offset(cx + s, cy + s), stroke)
                        }
                        UtilityId.LIBRARY -> repeat(3) { index ->
                            val xx = cx - s + index * s * 0.8f
                            drawRect(ink, Offset(xx, cy - s), androidx.compose.ui.geometry.Size(s * 0.65f, s * 2), style = Stroke(stroke))
                        }
                        UtilityId.SETTINGS -> {
                            drawCircle(ink, s, Offset(cx, cy), style = Stroke(stroke))
                            drawCircle(ink, s * 0.3f, Offset(cx, cy), style = Stroke(stroke))
                        }
                    }
                }
                Text(when (id) { UtilityId.HOME -> "INICIO"; UtilityId.SEARCH -> "BUSCAR"; UtilityId.LIBRARY -> "BIBLIOTECA"; UtilityId.SETTINGS -> "AJUSTES" },
                    style = ArchiveType.micro, color = ink, maxLines = 1)
            }
        }
    }
}
