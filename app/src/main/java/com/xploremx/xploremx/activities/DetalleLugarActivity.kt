package com.xploremx.xploremx.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.xploremx.xploremx.databinding.ActivityDetalleLugarBinding
import com.xploremx.xploremx.utils.Constants

class DetalleLugarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleLugarBinding
    private val BASE_URL = Constants.BASE_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleLugarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir datos del Home
        val id = intent.getIntExtra("id", 0)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val direccion = intent.getStringExtra("direccion") ?: ""
        val calificacion = intent.getDoubleExtra("calificacion", 0.0)
        val imagenUrl = intent.getStringExtra("imagenUrl") ?: ""

        // Mostrar datos
        binding.txtNombreDetalle.text = nombre
        binding.txtDireccionDetalle.text = " $direccion"
        binding.txtCalificacionDetalle.text = " $calificacion"
        binding.txtDescripcionDetalle.text = descripcion

        // Cargar imagen
        Glide.with(this)
            .load("${Constants.BASE_URL}/imagenes/$imagenUrl")
            .placeholder(com.xploremx.xploremx.R.mipmap.ic_launcher)
            .into(binding.imgDetalle)

        // Botones
        binding.btnRuta.setOnClickListener {
            val intent = Intent(this, RutaActivity::class.java)
            intent.putExtra("nombre", nombre)
            intent.putExtra("direccion", direccion)
            startActivity(intent)
        }

        binding.btnVideo.setOnClickListener {
            val intent = Intent(this, VideoActivity::class.java)
            intent.putExtra("nombre", nombre)
            startActivity(intent)
        }

        binding.btnQR.setOnClickListener {
            startActivity(Intent(this, QRActivity::class.java))
        }

        binding.btnCompartir.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Visita $nombre en $direccion! Descarga XploreMX")
            startActivity(Intent.createChooser(shareIntent, "Compartir via"))
        }
    }
}