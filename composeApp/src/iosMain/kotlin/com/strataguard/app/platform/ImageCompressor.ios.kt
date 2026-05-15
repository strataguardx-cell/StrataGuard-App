package com.strataguard.app.platform

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect

actual fun ByteArray.toThumbnailBytes(maxDim: Int): ByteArray = runCatching {
    val image = Image.makeFromEncoded(this)
    val scale = maxDim.toFloat() / maxOf(image.width, image.height).coerceAtLeast(1)
    val w = (image.width * scale).toInt().coerceAtLeast(1)
    val h = (image.height * scale).toInt().coerceAtLeast(1)
    val bitmap = Bitmap()
    bitmap.allocN32Pixels(w, h)
    Canvas(bitmap).drawImageRect(image, Rect.makeWH(w.toFloat(), h.toFloat()))
    Image.makeFromBitmap(bitmap).encodeToData(EncodedImageFormat.JPEG, 70)!!.bytes
}.getOrElse { this }
