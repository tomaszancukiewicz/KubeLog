package com.payu.kube.log.ui.compose.tab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CustomTab(podInfoState: StateFlow<PodInfo>, selected: Boolean, onClick: () -> Unit, onClose: () -> Unit) {
    val podInfo by podInfoState.collectAsState()
    Tab(
        selected = selected,
        onClick = onClick
    ) {
        Row {
            Text(podInfo.name)
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp)
                    .clickable { onClose() }
            )
        }
    }
}