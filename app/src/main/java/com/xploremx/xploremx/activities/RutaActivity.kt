package com.xploremx.xploremx.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.xploremx.xploremx.databinding.ActivityRutaBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class RutaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRutaBinding
    private val PERMISSION_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar OSMDroid
        Configuration.getInstance().userAgentValue = packageName

        binding = ActivityRutaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nombre = intent.getStringExtra("nombre") ?: "Destino"
        val direccion = intent.getStringExtra("direccion") ?: ""
        binding.txtNombreRuta.text = "$nombre"

        // Pedir permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST
            )
        } else {
            iniciarMapa()
        }

        binding.btnMostrarRuta.setOnClickListener {
            Toast.makeText(this, "Navega a: $direccion", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iniciarMapa() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)

        // Coordenadas de Guadalajara por default
        val guadalajara = GeoPoint(20.6597, -103.3496)
        binding.mapView.controller.setZoom(15.0)
        binding.mapView.controller.setCenter(guadalajara)

        // Marcador en el centro
        val marker = Marker(binding.mapView)
        marker.position = guadalajara
        marker.title = intent.getStringExtra("nombre") ?: "Destino"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        binding.mapView.overlays.add(marker)
        binding.mapView.invalidate()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarMapa()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
}