package com.strataguard.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.darwin.NSObject
import platform.posix.memcpy

private class IosPdfPickerDelegate : NSObject(), UIDocumentPickerDelegateProtocol {

    var onPicked: ((ByteArray, String) -> Unit)? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        val path = url.path ?: return
        val data = NSFileManager.defaultManager.contentsAtPath(path) ?: return
        val size = data.length.toInt()
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
        val filename = url.lastPathComponent ?: "document.pdf"
        onPicked?.invoke(bytes, filename)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {}
}

@Composable
actual fun rememberDocumentPickerHandler(
    onDocumentPicked: (ByteArray, String) -> Unit,
): DocumentPickerHandler {
    val delegate = remember { IosPdfPickerDelegate() }
    delegate.onPicked = onDocumentPicked
    return remember {
        object : DocumentPickerHandler {
            override fun pickPdf() {
                val picker = UIDocumentPickerViewController(
                    forOpeningContentTypes = listOf(UTTypePDF),
                    asCopy = true,
                )
                picker.delegate = delegate
                picker.allowsMultipleSelection = false
                UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
