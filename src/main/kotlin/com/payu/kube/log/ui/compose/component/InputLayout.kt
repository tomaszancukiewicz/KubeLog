package com.payu.kube.log.ui.compose.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InputLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
    ) {
        Box(Modifier.padding(all = 12.dp)) {
            content()
        }
    }
}