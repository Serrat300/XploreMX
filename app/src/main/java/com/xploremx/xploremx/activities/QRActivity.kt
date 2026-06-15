package com.xploremx.xploremx.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.xploremx.xploremx.classes.QRAnalyzer
import com.xploremx.xploremx.databinding.ActivityQrBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrBinding
    private lateinit var cameraExecutor: ExecutorService
    private var escaneando = false

    companion object {
        private const val TAG = "QRActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        binding.btnEscanear.setOnClickListener {
            escaneando = !escaneando
            binding.txtResultado.text = if (escaneando) "Escaneando..." else "Escaneo detenido"
            binding.btnEscanear.text = if (escaneando) "Detener" else "Escanear"
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalyzer.setAnalyzer(
            cameraExecutor,
            QRAnalyzer(isScanning = { escaneando }) { qrText ->
                runOnUiThread {
                    escaneando = false
                    binding.btnEscanear.text = "Escanear"
                    binding.txtResultado.text = ""

                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrText))
                        startActivity(intent)
                    } catch (e: Exception) {
                        binding.txtResultado.text = "Resultado: $qrText"
                        Toast.makeText(this, "No es un link válido", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.frameScanner.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e(TAG, "Error al vincular cámara", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) startCamera()
            else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}