package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.ui.compose.component.CheckboxWithLabel
import com.payu.kube.log.ui.compose.component.ThemeProvider
import com.payu.kube.log.ui.compose.tab.SettingsState

@Composable
fun SettingsView(settingsState: SettingsState, onClear: () -> Unit) {
    var autoScroll by settingsState.autoscroll
    var isWrap by settingsState.isWrap

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row {
            CheckboxWithLabel(
                modifier = Modifier.requiredWidth(IntrinsicSize.Max),
                "Autoscroll",
                checked = autoScroll, onCheckedChange = { autoScroll = it },
            )
            CheckboxWithLabel(
                modifier = Modifier.requiredWidth(IntrinsicSize.Max),
                "Wrap",
                checked = isWrap, onCheckedChange = { isWrap = it },
            )
        }
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