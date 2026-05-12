package com.strataguard.app.platform

import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

var androidAppContext: android.content.Context? = null

actual fun savePdfAndShare(bytes: ByteArray, filename: String) {
    val context = androidAppContext ?: return
    val file = File(context.cacheDir, filename)
    file.writeBytes(bytes)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
