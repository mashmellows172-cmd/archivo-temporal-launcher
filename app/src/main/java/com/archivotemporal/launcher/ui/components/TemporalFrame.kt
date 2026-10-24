package com.archivotemporal.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.archivotemporal.launcher.preferences.TextureLevel
import com.archivotemporal.launcher.ui.theme.ArchiveColor

@Composable
fun TemporalFrame(texture: TextureLevel, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(ArchiveColor.soot)
            val grid = 64.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(ArchiveColor.technical.copy(alpha = 0.19f), Offset(x, 0f), Offset(x, size.height), 1f)
                x += grid
            }
            var y = 0f
            while (y < size.height) {
                drawLine(ArchiveColor.technical.copy(alpha = 0.19f), Offset(0f, y), Offset(size.width, y), 1f)
                y += grid
            }
            if (texture != TextureLevel.OFF) {
                val interval = if (texture == TextureLevel.MEDIUM) 4.dp.toPx() else 8.dp.toPx()
                var scan = 0f
                while (scan < size.height) {
                    drawLine(ArchiveColor.cream.copy(alpha = 0.012f), Offset(0f, scan), Offset(size.width, scan), 1f)
                    scan += interval
                }
                repeat(70) { index ->
                    val px = ((index * 977 + 13) % size.width.toInt().coerceAtLeast(1)).toFloat()
                    val py = ((index * 673 + 47) % size.height.toInt().coerceAtLeast(1)).toFloat()
                    drawRect(ArchiveColor.cream.copy(alpha = 0.035f), Offset(px, py), Size(1f, 1f))
                }
            }
            val insetX = size.width * 0.05f
            val insetY = size.height * 0.05f
            drawRect(
                ArchiveColor.orange.copy(alpha = 0.8f),
                topLeft = Offset(insetX, insetY),
                size = Size(size.width - insetX * 2, size.height - insetY * 2),
                style = Stroke(width = 1.dp.toPx()),
            )
            val mark = 13.dp.toPx()
            for (cornerX in listOf(insetX, size.width - insetX)) {
                for (cornerY in listOf(insetY, size.height - insetY)) {
                    drawLine(ArchiveColor.orangeEdge, Offset(cornerX - mark / 2, cornerY), Offset(cornerX + mark / 2, cornerY), 2f)
                    drawLine(ArchiveColor.orangeEdge, Offset(cornerX, cornerY - mark / 2), Offset(cornerX, cornerY + mark / 2), 2f)
                }
            }
        }
        content()
    }
}
