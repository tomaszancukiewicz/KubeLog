package com.payu.kube.log.ui.compose.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun TextField(
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        textStyle = TextStyle.Default.copy(color = MaterialTheme.colors.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
        decorationBox = { innerTextField ->
            InputLayout {
                placeholder
                    ?.takeIf { value.isEmpty() }
                    ?.let {
                        Text(it, color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                        Spacer(Modifier.width(8.dp))
                    }
                innerTextField()
            }
        }
    )
}

@Preview
@ExperimentalComposeUiApi
@Composable
private fun TextFieldPreview() {
    ThemeProvider {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Placeholder",
            value = "",
        ) {}
    }
}