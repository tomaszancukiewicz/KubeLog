package com.payu.kube.log.ui.compose.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodState
import com.payu.kube.log.ui.compose.component.theme.LocalCustomColorScheme
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider
import com.payu.kube.log.util.DateUtils.fullFormat
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodInfoViewCell(
    item: PodInfo,
    onPodClick: (PodInfo) -> Unit
) {
    val customColorScheme = LocalCustomColorScheme.current
    val cardColors =
        if (item.isReady)
            CardDefaults.cardColors(
                containerColor = customColorScheme.greenContainer,
                contentColor = customColorScheme.onGreenContainer
            )
        else
            CardDefaults.cardColors(
                containerColor = customColorScheme.redContainer,
                contentColor = customColorScheme.onRedContainer
            )
    Card(
        onClick = { onPodClick(item) },
        modifier = Modifier.fillMaxWidth(),
        colors = cardColors
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${item.state.short()} " +
                        "${item.readyCount}/${item.startedCount}/${item.containerCount} " +
                        "R:${item.restarts}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(item.creationTimestamp.fullFormat(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
private fun PodInfoViewCellPreview() {
    ThemeProvider {
        PodInfoViewCell(
            PodInfo(
                "", "name", "name", "", "", "",
                "", "", 0, 0, 0, 0,
                PodState.Running, "", Instant.now(), null
            )
        ) {}
    }
}