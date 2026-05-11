package com.strataguard.app.platform

import androidx.compose.runtime.Composable

interface ImagePickerHandler {
    fun pickFromGallery()
    fun captureFromCamera()
}

@Composable
expect fun rememberImagePickerHandler(
    onImagePicked: (bytes: ByteArray, isFromCamera: Boolean) -> Unit,
): ImagePickerHandler
