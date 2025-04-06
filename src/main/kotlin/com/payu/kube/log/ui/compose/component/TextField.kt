package com.payu.kube.log.ui.compose.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.payu.kube.log.ui.compose.component.theme.ThemeProvider

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val colors = OutlinedTextFieldDefaults.colors()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(
            if (isFocused) colors.focusedIndicatorColor
            else colors.unfocusedIndicatorColor
        ),
        decorationBox = { innerTextField ->
            TextFieldDecorationBox(
                innerTextField,
                isFocused,
                colors,
                placeholder = {
                    placeholder
                        ?.takeIf { value.isEmpty() }
                        ?.let { Text(it) }
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        }
    )
}

@Composable
private fun TextFieldDecorationBox(
    innerTextField: @Composable () -> Unit,
    isFocused: Boolean,
    colors: TextFieldColors,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {

    val borderColor =
        if (isFocused) colors.focusedIndicatorColor
        else colors.unfocusedIndicatorColor

    val placeholderColor =
        if (isFocused) colors.focusedPlaceholderColor
        else colors.unfocusedPlaceholderColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(if (isFocused) 2.dp else 1.dp, borderColor, MaterialTheme.shapes.small)
            .heightIn(min = 36.dp)
            .padding(horizontal = 12.dp)
    ) {
        if (leadingIcon != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                leadingIcon()
            }
        }
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.weight(1f)
        ) {
            innerTextField()
            CompositionLocalProvider(
                LocalContentColor provides placeholderColor,
                content = { placeholder?.invoke() }
            )
        }
        if (trailingIcon != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                trailingIcon()
            }
        }
    }
}

@Preview
@Composable
private fun TextFieldPreview() {
    ThemeProvider {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Placeholder",
            value = "",
            onValueChange = {},
            leadingIcon = { Box(Modifier.size(20.dp).background(Color.Red)) },
            trailingIcon = { Box(Modifier.size(20.dp).background(Color.Red)) }
        )
    }
}