package com.xploremx.xploremx.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.xploremx.xploremx.databinding.ActivityQrBinding

class QRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrBinding

    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            binding.txtResultado.text = "Resultado: ${result.contents}"
            Toast.makeText(this, "QR leído: ${result.contents}", Toast.LENGTH_LONG).show()
        } else {
            binding.txtResultado.text = "Escaneo cancelado"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEscanear.setOnClickListener {
            escanearQR()
        }
    }

    private fun escanearQR() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Apunta la cámara al código QR")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        scanLauncher.launch(options)
    }
}