package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.desktop.ui.tooling.preview.Preview
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
import com.payu.kube.log.model.PodState
import com.payu.kube.log.ui.compose.component.ThemeProvider
import java.time.Instant

@Composable
fun NewestPodView(newestPodInfo: PodInfo, openPod: (PodInfo) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
            .fillMaxWidth()
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

@Preview
@Composable
private fun NewestPodViewPreview() {
    ThemeProvider {
        NewestPodView(
            PodInfo(
                "", "name", "name", "", "namespace", "image",
                "", "", 0, 0, 0, 0,
                PodState.Running, "", Instant.now(), null
            )
        ) {}
    }
}