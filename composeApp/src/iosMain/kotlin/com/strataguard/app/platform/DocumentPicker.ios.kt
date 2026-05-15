package com.strataguard.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberDocumentPickerHandler(
    onDocumentPicked: (ByteArray, String) -> Unit,
): DocumentPickerHandler = remember {
    object : DocumentPickerHandler {
        override fun pickPdf() {
            // TODO: iOS document picker (post-MVP)
        }
    }
}
