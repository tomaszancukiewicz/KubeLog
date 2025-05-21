package com.kube.log.ui.compose.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

val SnackbarState = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun SnackbarStateProvider(
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable () -> Unit
) {

    CompositionLocalProvider(
        SnackbarState provides snackBarHostState,
        content = content
    )
}