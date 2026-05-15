package com.strataguard.app.platform

import androidx.compose.runtime.Composable

interface DocumentPickerHandler {
    fun pickPdf()
}

@Composable
expect fun rememberDocumentPickerHandler(
    onDocumentPicked: (bytes: ByteArray, filename: String) -> Unit,
): DocumentPickerHandler
