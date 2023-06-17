package com.payu.kube.log.ui.compose.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit,
    trailingIcon: @Composable () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        placeholder = {
            placeholder
                ?.takeIf { value.isEmpty() }
                ?.let {
                    Text(it)
                }
        },
        trailingIcon = trailingIcon
    )
}

@Preview
@Composable
private fun TextFieldPreview() {
    ThemeProvider {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Placeholder",
            value = "",
            onValueChange = {}
        )
    }
}