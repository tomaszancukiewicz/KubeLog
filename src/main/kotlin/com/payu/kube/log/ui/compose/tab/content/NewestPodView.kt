package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo

@Composable
fun NewestPodView(newestPodInfo: PodInfo, openPod: (PodInfo) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(10.dp).fillMaxWidth()
    ) {
        Text(
            "There is newer pod(${newestPodInfo.name}) with this app(${newestPodInfo.calculatedAppName})",
            modifier = Modifier.weight(1.0f, false)
        )
        Button(onClick = { openPod(newestPodInfo) }) {
            Text("Open")
        }
    }
}