package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.ui.compose.component.CheckboxWithLabel
import com.payu.kube.log.ui.compose.component.ThemeProvider

class SettingsState {
    var autoscroll by mutableStateOf(true)
    var isWrap by mutableStateOf(true)
}

@Composable
fun SettingsView(settingsState: SettingsState, onClear: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CheckboxWithLabel(
            modifier = Modifier.requiredWidth(IntrinsicSize.Max),
            label = "Autoscroll",
            checked = settingsState.autoscroll, onCheckedChange = { settingsState.autoscroll = it },
        )
        CheckboxWithLabel(
            modifier = Modifier.requiredWidth(IntrinsicSize.Max),
            label = "Wrap",
            checked = settingsState.isWrap, onCheckedChange = { settingsState.isWrap = it },
        )
        Button(onClick = onClear, modifier = Modifier.requiredWidth(IntrinsicSize.Max)) {
            Text("Clear")
        }
    }
}

@Preview
@Composable
private fun SettingsViewPreview() {
    ThemeProvider {
        SettingsView(SettingsState()) {}
    }
}