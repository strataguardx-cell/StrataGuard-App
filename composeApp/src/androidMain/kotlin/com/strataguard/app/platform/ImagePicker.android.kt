package com.strataguard.app.platform

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberImagePickerHandler(
    onImagePicked: (ByteArray, Boolean) -> Unit,
): ImagePickerHandler {
    val ctx = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            ctx.contentResolver.openInputStream(it)?.use { stream ->
                onImagePicked(stream.readBytes(), false)
            }
        }
    }

    val tempFile = remember { File(ctx.cacheDir, "sg_capture.jpg") }
    val cameraUri: Uri = remember {
        FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", tempFile)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            ctx.contentResolver.openInputStream(cameraUri)?.use { stream ->
                onImagePicked(stream.readBytes(), true)
            }
        }
    }

    var awaitingCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && awaitingCameraPermission) {
            awaitingCameraPermission = false
            cameraLauncher.launch(cameraUri)
        }
    }

    return remember(galleryLauncher, cameraLauncher, permissionLauncher) {
        object : ImagePickerHandler {
            override fun pickFromGallery() {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }

            override fun captureFromCamera() {
                val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                if (granted == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(cameraUri)
                } else {
                    awaitingCameraPermission = true
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}
