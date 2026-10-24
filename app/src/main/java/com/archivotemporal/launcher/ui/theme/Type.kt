package com.archivotemporal.launcher.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

object ArchiveType {
    private val headingFamily = FontFamily.SansSerif
    private val archiveFamily = FontFamily.Monospace
    val heading = TextStyle(fontFamily = headingFamily, fontWeight = FontWeight.Black, fontSize = 36.sp, letterSpacing = 2.sp)
    val clock = TextStyle(fontFamily = archiveFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp)
    val slot = TextStyle(fontFamily = headingFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp)
    val utility = TextStyle(fontFamily = archiveFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 1.sp)
    val micro = TextStyle(fontFamily = archiveFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 1.sp)
}
