package com.payu.kube.log.ui.compose.tab

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodState
import com.payu.kube.log.ui.compose.component.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

@Composable
fun CustomTab(podInfoState: StateFlow<PodInfo>, selected: Boolean, onClick: () -> Unit, onClose: () -> Unit) {
    val podInfo by podInfoState.collectAsState()
    Tab(
        selected = selected,
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(podInfo.name, style = MaterialTheme.typography.titleMedium)
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

@Preview
@Composable
private fun CustomTabPreview() {
    ThemeProvider {
        CustomTab(
            MutableStateFlow(PodInfo(
                "", "name", "name", "", "", "",
                "", "", 0, 0, 0, 0,
                PodState.Running, "", Instant.now(), null
            )), true, {}
        ) {}
    }
}