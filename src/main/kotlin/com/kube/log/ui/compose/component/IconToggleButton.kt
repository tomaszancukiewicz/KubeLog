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
fun IconToggleButton(
    image: ImageVector,
    description: String,
    shortcut : Shortcut? = null,
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val desc = buildString {
        append(description)
        append(" - ")
        if (checked) {
            append("On")
        } else {
            append("Off")
        }
    }

    Tooltip(
        tooltip = {
            Text(desc)
            if (shortcut != null) {
                Text("($shortcut)")
            }
        },
    ) {
        FilledIconToggleButton(
            modifier = modifier
                .size(24.dp),
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = IconButtonDefaults.filledIconToggleButtonColors(
                containerColor = Color.Transparent,
                contentColor = contentColorFor(Color.Transparent),
                checkedContainerColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.small,
        ) {
            Icon(
                image,
                desc,
                Modifier.size(20.dp),
            )
        }
    }
}