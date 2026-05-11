package com.strataguard.app.platform

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun ByteArray.toImageBitmap(): ImageBitmap =
    runCatching { BitmapFactory.decodeByteArray(this, 0, size).asImageBitmap() }
        .getOrElse { ImageBitmap(1, 1) }
