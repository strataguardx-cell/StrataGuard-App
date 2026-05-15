package com.strataguard.app.platform

import com.strataguard.app.data.evidence.EvidenceItem
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
private fun pendingDir(): NSURL {
    val docs = NSFileManager.defaultManager
        .URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        .firstOrNull() as? NSURL ?: error("No documents dir")
    val dir = docs.URLByAppendingPathComponent("pending_evidence") ?: error("Can't build path")
    NSFileManager.defaultManager.createDirectoryAtURL(
        dir, withIntermediateDirectories = true, attributes = null, error = null
    )
    return dir
}

@OptIn(ExperimentalForeignApi::class)
actual fun enqueuePendingEvidence(item: EvidenceItem) {
    val path = pendingDir().path?.let { "$it/${item.id}.json" } ?: return
    val bytes = Json.encodeToString(item).encodeToByteArray()
    val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    NSFileManager.defaultManager.createFileAtPath(path, contents = nsData, attributes = null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun dequeuePendingEvidence(): List<EvidenceItem> {
    val dir = pendingDir()
    val dirPath = dir.path ?: return emptyList()
    val files = NSFileManager.defaultManager.contentsOfDirectoryAtPath(dirPath, error = null)
        ?: return emptyList()
    return (files as List<*>).mapNotNull { name ->
        val filePath = "$dirPath/${name as? String ?: return@mapNotNull null}"
        val nsData = NSFileManager.defaultManager.contentsAtPath(filePath) ?: return@mapNotNull null
        val size = nsData.length.toInt()
        val bytes = ByteArray(size).also { buf ->
            buf.usePinned { pinned -> memcpy(pinned.addressOf(0), nsData.bytes, nsData.length) }
        }
        runCatching { Json.decodeFromString<EvidenceItem>(bytes.decodeToString()) }.getOrNull()
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun removePendingEvidence(id: String) {
    val path = pendingDir().path?.let { "$it/$id.json" } ?: return
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun pendingEvidenceCount(): Int {
    val path = pendingDir().path ?: return 0
    return (NSFileManager.defaultManager.contentsOfDirectoryAtPath(path, error = null) as? List<*>)?.size ?: 0
}
