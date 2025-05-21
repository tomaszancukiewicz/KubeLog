package com.kube.log.ui.compose.tab.content.search

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kube.log.ui.compose.component.IconButton
import com.kube.log.ui.compose.component.theme.ThemeProvider

val SEARCH_TYPE_ICONS = mapOf(
    SearchType.FILTER to Icons.Default.Search,
    SearchType.MARK to Icons.AutoMirrored.Filled.Rule,
)

@Composable
fun SearchTypePicker(
    value: SearchType,
    onValueChange: (SearchType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        tooltip = {
            Text("Search type")
        },
        modifier = Modifier
            .width(40.dp),
        onClick = { expanded = true }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SEARCH_TYPE_ICONS[value]?.let {
                Icon(
                    it,
                    value.toString(),
                    modifier = Modifier.size(20.dp)
                )
            }
            Icon(
                if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp, 16.dp)
            )
        }
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        SEARCH_TYPE_ICONS.forEach { (type, icon) ->
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            icon,
                            type.toString(),
                            Modifier.size(20.dp)
                        )
                        Text(text = type.toString())
                    }
                },
                onClick = {
                    expanded = false
                    onValueChange(type)
                }
            )
        }
    }
}

@Preview
@Composable
private fun SearchTypePickerPreview() {
    ThemeProvider {
        SearchTypePicker(
            SearchType.FILTER,
            {}
        )
    }
}