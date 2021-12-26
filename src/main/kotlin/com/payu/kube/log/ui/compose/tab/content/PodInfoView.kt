package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodState
import com.payu.kube.log.ui.compose.ReadyIndicator
import com.payu.kube.log.ui.compose.component.CheckboxWithLabel
import com.payu.kube.log.ui.compose.component.ThemeProvider
import com.payu.kube.log.ui.compose.tab.SettingsState
import com.payu.kube.log.util.DateUtils.fullFormat
import java.time.Instant

@Composable
fun PodInfoView(podInfo: PodInfo, settingsState: SettingsState, onClear: () -> Unit) {
    var autoScroll by settingsState.autoscroll
    var isWrap by settingsState.isWrap

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
    ) {
        ReadyIndicator(podInfo.isReady)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${podInfo.namespace} - ${podInfo.containerImage}",
                style = MaterialTheme.typography.subtitle1,
                maxLines = 2
            )
            Text(
                "${podInfo.state.long()} " +
                        "${podInfo.readyCount}/${podInfo.startedCount}/${podInfo.containerCount} " +
                        "R:${podInfo.restarts}",
                style = MaterialTheme.typography.body2,
                maxLines = 1
            )
        }
        Column {
            Text(
                "C:${podInfo.creationTimestamp.fullFormat()}",
                style = MaterialTheme.typography.body2
            )
            podInfo.deletionTimestamp?.let {
                Text(
                    "D:${it.fullFormat()}",
                    style = MaterialTheme.typography.body2
                )
            }
        }
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
private fun PodInfoViewPreview() {
    ThemeProvider {
        PodInfoView(
            PodInfo(
                "", "name", "name", "", "namespace", "image",
                "", "", 0, 0, 0, 0,
                PodState.Running, "", Instant.now(), null
            ),
            SettingsState()
        ) {}
    }
}