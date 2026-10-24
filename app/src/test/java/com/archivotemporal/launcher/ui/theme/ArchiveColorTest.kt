package com.archivotemporal.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchiveColorTest {
    @Test fun focusedLabelHasAtLeastFourPointFiveContrast() {
        val ratio = contrast(ArchiveColor.ink, ArchiveColor.orange)
        assertTrue("focused label contrast was $ratio", ratio >= 4.5)
    }

    private fun contrast(a: Color, b: Color): Double {
        fun channel(value: Float): Double {
            val x = value.toDouble()
            return if (x <= 0.04045) x / 12.92 else Math.pow((x + 0.055) / 1.055, 2.4)
        }
        fun luminance(color: Color) = 0.2126 * channel(color.red) +
            0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
        val first = luminance(a)
        val second = luminance(b)
        return (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
    }
}
