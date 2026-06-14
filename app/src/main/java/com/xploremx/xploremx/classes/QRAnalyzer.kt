package com.xploremx.xploremx.classes

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class QRAnalyzer(
    private val isScanning: () -> Boolean,
    private val onQRCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()

    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning()) {
            imageProxy.close()
            return
        }

        val buffer =
            imageProxy.planes[0].buffer

        val bytes =
            ByteArray(buffer.remaining())

        buffer.get(bytes)

        val source = PlanarYUVLuminanceSource(
            bytes,
            imageProxy.width,
            imageProxy.height,
            0,
            0,
            imageProxy.width,
            imageProxy.height,
            false
        )

        val bitmap = BinaryBitmap(
            HybridBinarizer(source)
        )

        try {

            val result =
                reader.decode(bitmap)

            onQRCodeDetected(result.text)

        } catch (_: Exception) {
        } finally {

            reader.reset()

            imageProxy.close()
        }
    }
}