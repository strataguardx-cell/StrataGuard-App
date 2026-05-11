package com.strataguard.app.platform

import androidx.compose.ui.graphics.ImageBitmap

// iOS image display goes through AsyncImage (Coil) rather than ImageBitmap decode.
actual fun ByteArray.toImageBitmap(): ImageBitmap = ImageBitmap(1, 1)
