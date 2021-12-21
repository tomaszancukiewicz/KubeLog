package com.payu.kube.log.ui.compose.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

@Composable
fun <T> Select(items: List<T>, value: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = modifier
            .border(0.5.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
            .onGloballyPositioned { coordinates ->
                textFieldSize = coordinates.size.toSize()
            }
            .clickable(
                onClick = {
                    expanded = true
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentWidth(unbounded = false)
        ) {
            Text(
                text = value.toString()
            )
            Icon(
                if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp, 20.dp),
                tint = MaterialTheme.colors.onSurface
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .requiredWidth(with(LocalDensity.current) { textFieldSize.width.toDp() })
                .background(MaterialTheme.colors.surface)
        ) {
            items.forEach { title ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelect(title)
                    }
                ) {
                    Text(text = title.toString())
                }
            }
        }
    }
}