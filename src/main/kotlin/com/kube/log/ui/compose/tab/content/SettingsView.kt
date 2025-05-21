package com.kube.log.ui.compose.tab.content

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.kube.log.ui.compose.component.IconButton
import com.kube.log.ui.compose.component.IconToggleButton
import com.kube.log.ui.compose.component.shortcut.CLEAR_LOGS_SHORTCUT
import com.kube.log.ui.compose.component.shortcut.TOGGLE_SEARCH_SHORTCUT
import com.kube.log.ui.compose.component.theme.ThemeProvider
import com.kube.log.ui.compose.tab.content.search.SearchState

class SettingsState {
    var autoscroll by mutableStateOf(true)
    var isWrap by mutableStateOf(true)
}

@Composable
fun SettingsView(
    settingsState: SettingsState,
    searchState: SearchState,
    onClear: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconToggleButton(
            Icons.Default.VerticalAlignBottom,
            description = "Autoscroll",
            checked = settingsState.autoscroll, onCheckedChange = { settingsState.autoscroll = it },
        )
        IconToggleButton(
            Icons.AutoMirrored.Filled.WrapText,
            description = "Wrap",
            checked = settingsState.isWrap, onCheckedChange = { settingsState.isWrap = it },
        )
        IconToggleButton(
            Icons.Default.Search,
            description = "Search",
            shortcut = TOGGLE_SEARCH_SHORTCUT,
            checked = searchState.isVisible, onCheckedChange = { searchState.toggleVisible() },
        )
        IconButton(
            Icons.Default.Delete,
            description = "Clear",
            shortcut = CLEAR_LOGS_SHORTCUT,
            onClick = onClear
        )
    }
}

@Preview
@Composable
private fun SettingsViewPreview() {
    ThemeProvider {
        SettingsView(
            SettingsState(),
            SearchState(),
            onClear = {}
        )
    }
}