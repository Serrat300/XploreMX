package com.xploremx.xploremx.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xploremx.xploremx.databinding.ActivityVideoBinding
import com.xploremx.xploremx.utils.Constants

class VideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nombre = intent.getStringExtra("nombre") ?: "Video"
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""

        binding.txtNombreVideo.text = nombre
        binding.txtComentarioVideo.text = descripcion

        // Configurar MediaController
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)

        if (videoUrl.isEmpty()) {
            Toast.makeText(this, "Este lugar no tiene video disponible", Toast.LENGTH_SHORT).show()
        } else {
            val uri = Uri.parse("${Constants.BASE_URL}/videos/$videoUrl")
            binding.videoView.setVideoURI(uri)
            binding.videoView.setOnPreparedListener { mp -> mp.start() }
            binding.videoView.setOnErrorListener { _, _, _ ->
                Toast.makeText(this, "No se pudo cargar el video", Toast.LENGTH_SHORT).show()
                true
            }
        }

        // Ya no aplican con un solo video por lugar, los desactivamos
        binding.btnAnterior.isEnabled = false
        binding.btnSiguiente.isEnabled = false

        binding.btnCompartirVideo.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Mira este video de $nombre en XploreMX!")
            startActivity(Intent.createChooser(shareIntent, "Compartir via"))
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
        }
    }
}