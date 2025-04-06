package com.payu.kube.log.ui.compose.tab

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.model.PodState
import com.payu.kube.log.ui.compose.component.IconButton
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider
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
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(podInfo.name)

            IconButton(
                Icons.Default.Close,
                "Close",
                onClick = onClose
            )
        }
    }
}

@Preview
@Composable
private fun CustomTabPreview() {
    ThemeProvider {
        CustomTab(
            MutableStateFlow(
                PodInfo(
                    "", "name", "name", "", "", "",
                    "", "", 0, 0, 0, 0,
                    PodState.Running, "", Instant.now(), null
                )
            ), true, {}
        ) {}
    }
}