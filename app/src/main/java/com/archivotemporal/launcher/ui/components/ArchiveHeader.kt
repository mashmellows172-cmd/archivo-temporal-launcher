package com.archivotemporal.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.archivotemporal.launcher.ui.theme.ArchiveColor
import com.archivotemporal.launcher.ui.theme.ArchiveType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun ArchiveHeader(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(clockText()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = clockText()
            delay(5_000)
        }
    }
    Row(
        modifier = modifier.fillMaxWidth().height(72.dp).background(ArchiveColor.orange).padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("ARCHIVO TEMPORAL", style = ArchiveType.heading, color = ArchiveColor.ink)
        Text(time, style = ArchiveType.clock, color = ArchiveColor.ink)
    }
}

private fun clockText() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
