package com.xploremx.xploremx.activities

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xploremx.xploremx.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoBinding
    private val BASE_URL = "http://192.168.100.56/xploremx"

    // Videos de prueba de YouTube (URLs directas no funcionan, usamos URLs locales)
    private val videos = listOf(
        mapOf(
            "titulo" to "Catedral Metropolitana",
            "url" to "$BASE_URL/videos/catedral.mp4",
            "comentario" to "Recorrido por la icónica Catedral Metropolitana de Guadalajara"
        ),
        mapOf(
            "titulo" to "Teatro Degollado",
            "url" to "$BASE_URL/videos/teatro.mp4",
            "comentario" to "Conoce la historia del Teatro Degollado, símbolo cultural de Jalisco"
        ),
        mapOf(
            "titulo" to "Tour Tequila",
            "url" to "$BASE_URL/videos/tequila.mp4",
            "comentario" to "Descubre la cuna del tequila mexicano"
        )
    )

    private var indiceActual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nombre = intent.getStringExtra("nombre") ?: "Video"
        binding.txtNombreVideo.text = nombre

        // Configurar MediaController
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)

        // Cargar primer video
        cargarVideo(indiceActual)

        // Botones
        binding.btnAnterior.setOnClickListener {
            if (indiceActual > 0) {
                indiceActual--
                cargarVideo(indiceActual)
            } else {
                Toast.makeText(this, "Es el primer video", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSiguiente.setOnClickListener {
            if (indiceActual < videos.size - 1) {
                indiceActual++
                cargarVideo(indiceActual)
            } else {
                Toast.makeText(this, "Es el último video", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCompartirVideo.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Mira este video de ${videos[indiceActual]["titulo"]} en XploreMX! 🌵"
            )
            startActivity(Intent.createChooser(shareIntent, "Compartir via"))
        }
    }

    private fun cargarVideo(indice: Int) {
        val video = videos[indice]
        binding.txtNombreVideo.text = video["titulo"]
        binding.txtComentarioVideo.text = video["comentario"]

        val uri = Uri.parse(video["url"])
        binding.videoView.setVideoURI(uri)
        binding.videoView.setOnPreparedListener { mp ->
            mp.start()
        }
        binding.videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "No se pudo cargar el video", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
        }
    }
}