package com.strataguard.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun rememberImagePickerHandler(
    onImagePicked: (ByteArray, Boolean) -> Unit,
): ImagePickerHandler {
    return remember { IosImagePickerHandler(onImagePicked) }
}

private class IosImagePickerHandler(
    private val onImagePicked: (ByteArray, Boolean) -> Unit,
) : ImagePickerHandler {

    private val delegate = PickerDelegate { bytes, fromCamera -> onImagePicked(bytes, fromCamera) }

    override fun pickFromGallery() {
        presentPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary, isCamera = false)
    }

    override fun captureFromCamera() {
        presentPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera, isCamera = true)
    }

    private fun presentPicker(source: UIImagePickerControllerSourceType, isCamera: Boolean) {
        val picker = UIImagePickerController()
        picker.sourceType = source
        picker.delegate = delegate
        delegate.isCamera = isCamera
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.presentViewController(picker, animated = true, completion = null)
    }
}

private class PickerDelegate(
    private val onPicked: (ByteArray, Boolean) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    var isCamera: Boolean = false

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        image?.let {
            val data = UIImageJPEGRepresentation(it, 0.85)
            data?.let { nsData ->
                val bytes = ByteArray(nsData.length.toInt())
                nsData.bytes?.let { ptr ->
                    for (i in bytes.indices) bytes[i] = (ptr + i)!!.readBytes(1)[0]
                }
                onPicked(bytes, isCamera)
            }
        }
        picker.dismissViewControllerAnimated(true, null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, null)
    }
}
