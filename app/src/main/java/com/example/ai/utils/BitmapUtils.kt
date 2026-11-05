package com.example.ai.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import kotlin.math.max

object BitmapUtils {

    fun processImageProxy(imageProxy: ImageProxy, maxDimension: Int = 1280): Bitmap {
        val originalBitmap = imageProxy.toBitmap()
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }

        val rotatedBitmap = Bitmap.createBitmap(
            originalBitmap, 0, 0,
            originalBitmap.width, originalBitmap.height,
            matrix, true
        )

        // Scale down while maintaining aspect ratio
        val width = rotatedBitmap.width
        val height = rotatedBitmap.height
        val scale = maxDimension.toFloat() / max(width, height)

        return if (scale < 1.0f) {
            Bitmap.createScaledBitmap(
                rotatedBitmap,
                (width * scale).toInt(),
                (height * scale).toInt(),
                true
            )
        } else {
            rotatedBitmap
        }
    }
}