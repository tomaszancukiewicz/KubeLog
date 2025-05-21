package com.kube.log.ui.compose.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kube.log.ui.compose.component.shortcut.Shortcut

@Composable
fun IconButton(
    image: ImageVector,
    description: String,
    shortcut : Shortcut? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        tooltip = {
            Text(description)
            if (shortcut != null) {
                Text("($shortcut)")
            }
        },
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            image,
            description,
            Modifier.size(20.dp),
        )
    }
}

@Composable
fun IconButton(
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Tooltip(
        tooltip = tooltip
    ) {
        FilledIconButton(
            modifier = modifier
                .size(24.dp),
            onClick = onClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Transparent,
            ),
            shape = MaterialTheme.shapes.small,
        ) {
            content()
        }
    }
}