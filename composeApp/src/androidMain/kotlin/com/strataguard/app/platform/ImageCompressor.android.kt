package com.strataguard.app.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual fun ByteArray.toThumbnailBytes(maxDim: Int): ByteArray = runCatching {
    val original = BitmapFactory.decodeByteArray(this, 0, size) ?: return@runCatching this
    val scale = maxDim.toFloat() / maxOf(original.width, original.height).coerceAtLeast(1)
    val w = (original.width * scale).toInt().coerceAtLeast(1)
    val h = (original.height * scale).toInt().coerceAtLeast(1)
    val resized = Bitmap.createScaledBitmap(original, w, h, true)
    val out = ByteArrayOutputStream()
    resized.compress(Bitmap.CompressFormat.JPEG, 70, out)
    out.toByteArray()
}.getOrElse { this }
