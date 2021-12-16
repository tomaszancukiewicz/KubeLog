package com.payu.kube.log.ui.compose.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.ui.compose.ReadyIndicator
import com.payu.kube.log.util.DateUtils.fullFormat

@Composable
fun PodInfoViewCell(
    item: PodInfo,
    onPodClick: (PodInfo) -> Unit
) {
    Row(modifier = Modifier
        .clickable { onPodClick(item) }
        .padding(horizontal = 6.dp, vertical = 2.dp)
        .fillMaxWidth()
    ) {
        ReadyIndicator(item.isReady, modifier = Modifier.padding(end = 6.dp, top = 6.dp))
        Column {
            Text(item.name, style = MaterialTheme.typography.subtitle1)
            Text(
                "${item.state.short()} " +
                        "${item.readyCount}/${item.startedCount}/${item.containerCount} " +
                        "R:${item.restarts}",
                style = MaterialTheme.typography.caption
            )
            Text(item.creationTimestamp.fullFormat(), style = MaterialTheme.typography.caption)
        }
    }
}