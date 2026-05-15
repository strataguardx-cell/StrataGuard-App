package com.strataguard.app.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
actual fun savePdfAndShare(bytes: ByteArray, filename: String) {
    val tempPath = NSTemporaryDirectory() + filename
    val fileUrl = NSURL.fileURLWithPath(tempPath)

    val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    NSFileManager.defaultManager.createFileAtPath(tempPath, contents = nsData, attributes = null)

    val activityVC = UIActivityViewController(
        activityItems = listOf(fileUrl),
        applicationActivities = null,
    )

    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootVC?.presentViewController(activityVC, animated = true, completion = null)
}
