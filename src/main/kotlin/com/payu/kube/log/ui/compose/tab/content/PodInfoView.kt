package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.ui.compose.ReadyIndicator
import com.payu.kube.log.ui.compose.component.CheckboxWithLabel
import com.payu.kube.log.ui.compose.tab.SettingsState
import com.payu.kube.log.util.DateUtils.fullFormat

@Composable
fun PodInfoView(podInfo: PodInfo, settingsState: SettingsState, onClear: () -> Unit) {
    var autoScroll by settingsState.autoscroll
    var isWrap by settingsState.isWrap

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
    ) {
        ReadyIndicator(podInfo.isReady)
        Column(modifier = Modifier.weight(1f)) {
            Text("${podInfo.namespace} - ${podInfo.containerImage}", maxLines = 1)
            Text(
                "${podInfo.state.long()} " +
                        "${podInfo.readyCount}/${podInfo.startedCount}/${podInfo.containerCount} " +
                        "R:${podInfo.restarts}",
                maxLines = 1
            )
        }
        Column {
            Text("C:${podInfo.creationTimestamp.fullFormat()}")
            podInfo.deletionTimestamp?.let {
                Text("D:${it.fullFormat()}")
            }
        }
        Row {
            CheckboxWithLabel(
                "Autoscroll",
                checked = autoScroll, onCheckedChange = { autoScroll = it },
                modifier = Modifier.requiredWidth(IntrinsicSize.Max)
            )
            CheckboxWithLabel(
                "Wrap",
                checked = isWrap, onCheckedChange = { isWrap = it },
                modifier = Modifier.requiredWidth(IntrinsicSize.Max)
            )
        }
        Button(onClick = onClear, modifier = Modifier.requiredWidth(IntrinsicSize.Max)) {
            Text("Clear")
        }
    }
}