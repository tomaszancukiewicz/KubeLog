package com.payu.kube.log.ui.compose.tab.content

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodState
import com.payu.kube.log.ui.compose.component.ReadyIndicator
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider
import com.payu.kube.log.util.DateUtils.fullFormat
import java.time.Instant

@Composable
fun PodInfoView(podInfo: PodInfo, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        ReadyIndicator(podInfo.isReady)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${podInfo.namespace} - ${podInfo.containerImage}",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2
            )
            Text(
                "${podInfo.state.long()} " +
                        "${podInfo.readyCount}/${podInfo.startedCount}/${podInfo.containerCount} " +
                        "R:${podInfo.restarts}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
        Column {
            Text(
                "C:${podInfo.creationTimestamp.fullFormat()}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            podInfo.deletionTimestamp?.let {
                Text(
                    "D:${it.fullFormat()}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
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
            )
        )
    }
}