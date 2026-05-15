package com.strataguard.app.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDocumentPickerHandler(
    onDocumentPicked: (ByteArray, String) -> Unit,
): DocumentPickerHandler {
    val ctx = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val filename = ctx.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            if (idx >= 0) cursor.getString(idx) else "document.pdf"
        } ?: "document.pdf"
        ctx.contentResolver.openInputStream(uri)?.use { stream ->
            onDocumentPicked(stream.readBytes(), filename)
        }
    }

    return remember(launcher) {
        object : DocumentPickerHandler {
            override fun pickPdf() = launcher.launch("application/pdf")
        }
    }
}
