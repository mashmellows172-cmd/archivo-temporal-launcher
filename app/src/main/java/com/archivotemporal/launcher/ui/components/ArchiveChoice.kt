package com.archivotemporal.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.archivotemporal.launcher.ui.theme.ArchiveColor
import com.archivotemporal.launcher.ui.theme.ArchiveType

@Composable
fun ArchiveChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
) {
    val foreground = if (selected) ArchiveColor.ink else ArchiveColor.cream
    Column(
        modifier.fillMaxWidth()
            .semantics(mergeDescendants = true) { this.selected = selected; role = Role.Button; onClick { onClick(); true } }
            .background(if (selected) ArchiveColor.orange else ArchiveColor.panel)
            .border(if (selected) 3.dp else 1.dp, if (selected) ArchiveColor.cream else ArchiveColor.orange)
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Text(label, color = foreground, style = ArchiveType.slot, maxLines = 2)
        if (detail != null) Text(detail, color = foreground, style = ArchiveType.slot, maxLines = 1)
    }
}
