package com.xploremx.xploremx.fragments

/*
 * ══════════════════════════════════════════════════════════════════
 *  PROBLEMAS QUE SE CORRIGIERON:
 * ══════════════════════════════════════════════════════════════════
 *
 *  1. videoViewPreview estaba declarado como PreviewView pero debía
 *     ser VideoView para reproducir el video grabado.
 *     → Ahora hay DOS referencias separadas:
 *       · previewCamera  (PreviewView) → muestra la cámara EN VIVO mientras graba
 *       · videoViewPreview (VideoView) → reproduce el video YA GRABADO
 *
 *  2. La cámara se iniciaba sin conectar el Preview al PreviewView,
 *     así el usuario no veía nada mientras grababa.
 *     → Se agrega un use-case Preview y se vincula al previewCamera.
 *
 *  3. mostrarPreviewVideo() llamaba métodos de VideoView en un PreviewView
 *     (setVideoURI, setOnPreparedListener), lo cual crasheaba.
 *     → Ahora se oculta previewCamera y se muestra videoViewPreview.
 *
 *  4. descartarVideo() no ocultaba el previewCamera ni limpiaba el estado
 *     de la cámara correctamente.
 *     → Se resetea la visibilidad de ambas vistas.
 *
 *  5. El FrameLayout layoutVideoPreview se quedaba GONE después de grabar
 *     porque la lógica de visibilidad no era consistente.
 *     → Ahora: GONE al inicio, VISIBLE cuando se graba (muestra cámara),
 *       y sigue VISIBLE tras grabar (muestra el video).
 * ══════════════════════════════════════════════════════════════════
 */

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.widget.MediaController
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.xploremx.xploremx.R
import com.xploremx.xploremx.utils.Constants
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ExperienciasFragment : Fragment() {

    private val BASE_URL = Constants.BASE_URL

    private lateinit var btnGrabarVideo: Button
    private lateinit var layoutVideoPreview: FrameLayout
    private lateinit var previewCamera: PreviewView   // ← muestra la cámara EN VIVO
    private lateinit var videoViewPreview: VideoView  // ← reproduce el video grabado
    private lateinit var btnQuitarVideo: ImageButton
    private lateinit var etComentario: EditText
    private lateinit var btnPublicar: Button

    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService

    private var videoUri: Uri? = null
    private var isRecording = false

    private val permisosLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val camaraOk = permisos[Manifest.permission.CAMERA] == true
        val audioOk  = permisos[Manifest.permission.RECORD_AUDIO] == true
        if (camaraOk && audioOk) {
            iniciarCamara()
        } else {
            Toast.makeText(
                requireContext(),
                "Se necesitan permisos de cámara y micrófono",
                Toast.LENGTH_LONG
            ).show()
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_experiencias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        btnGrabarVideo    = view.findViewById(R.id.btnGrabarVideo)
        layoutVideoPreview = view.findViewById(R.id.layoutVideoPreview)
        previewCamera     = view.findViewById(R.id.previewCamera)
        videoViewPreview  = view.findViewById(R.id.videoViewPreview)
        btnQuitarVideo    = view.findViewById(R.id.btnQuitarVideo)
        etComentario      = view.findViewById(R.id.etComentario)
        btnPublicar       = view.findViewById(R.id.btnPublicar)

        btnGrabarVideo.setOnClickListener {
            if (tienePermisos()) {
                if (videoCapture == null) {

                    iniciarCamara()
                } else {

                    toggleGrabacion()
                }
            } else {
                solicitarPermisos()
            }
        }

        btnQuitarVideo.setOnClickListener { descartarVideo() }
        btnPublicar.setOnClickListener    { publicarResena() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    private fun iniciarCamara() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewCamera.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    videoCapture
                )


                layoutVideoPreview.visibility = View.VISIBLE
                previewCamera.visibility      = View.VISIBLE
                videoViewPreview.visibility   = View.GONE

                toggleGrabacion()

            } catch (e: Exception) {
                Log.e("ExperienciasFragment", "Error iniciando cámara: ${e.message}")
                Toast.makeText(requireContext(), "Error al abrir la cámara", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun toggleGrabacion() {
        if (isRecording) {
            activeRecording?.stop()
            activeRecording = null
        } else {
            iniciarGrabacion()
        }
    }


    private fun iniciarGrabacion() {
        val vc = videoCapture ?: return

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Permiso de micrófono requerido", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "VID_$nombre.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        }

        val outputOptions = MediaStoreOutputOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            .setContentValues(contentValues)
            .build()

        activeRecording = vc.output
            .prepareRecording(requireContext(), outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(requireContext())) { evento ->
                when (evento) {
                    is VideoRecordEvent.Start -> {
                        isRecording = true
                        requireActivity().runOnUiThread {
                            btnGrabarVideo.text = "Detener"
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        isRecording = false
                        requireActivity().runOnUiThread {
                            btnGrabarVideo.text = "Grabar video"
                        }

                        if (evento.hasError()) {
                            Log.e("ExperienciasFragment", "Error de grabación: ${evento.error}")
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "Error al grabar", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            videoUri = evento.outputResults.outputUri
                            Log.d("ExperienciasFragment", "Video guardado en: $videoUri")
                            requireActivity().runOnUiThread {
                                mostrarPreviewVideo()
                            }
                        }
                    }
                    else -> {}
                }
            }
    }


    private fun mostrarPreviewVideo() {
        val uri = videoUri ?: return

        previewCamera.visibility    = View.GONE
        videoViewPreview.visibility = View.VISIBLE


        videoViewPreview.setVideoURI(uri)

        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoViewPreview)
        videoViewPreview.setMediaController(mediaController)

        videoViewPreview.setOnPreparedListener { mp ->
            mp.isLooping = true
            videoViewPreview.start()
        }

        videoViewPreview.setOnErrorListener { _, what, extra ->
            Log.e("ExperienciasFragment", "Error reproduciendo video: what=$what extra=$extra")
            Toast.makeText(requireContext(), "No se pudo reproducir el video", Toast.LENGTH_SHORT).show()
            true
        }
    }


    private fun descartarVideo() {
        videoUri = null
        videoCapture = null   // forzar reinicio de cámara la próxima vez
        videoViewPreview.stopPlayback()
        videoViewPreview.visibility   = View.GONE
        previewCamera.visibility      = View.GONE
        layoutVideoPreview.visibility = View.GONE
        btnGrabarVideo.text           = "Grabar video"
    }


    private fun publicarResena() {
        val comentario = etComentario.text.toString().trim()

        if (comentario.isEmpty() && videoUri == null) {
            Toast.makeText(requireContext(), "Agrega un comentario o un video", Toast.LENGTH_SHORT).show()
            return
        }

        btnPublicar.isEnabled = false
        btnPublicar.text      = "Publicando..."

        Thread {
            try {
                val urlVideo = if (videoUri != null) subirVideo(videoUri!!) else ""
                guardarResena(comentario, urlVideo)
            } catch (e: Exception) {
                Log.e("ExperienciasFragment", "Error publicando: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    btnPublicar.isEnabled = true
                    btnPublicar.text      = "Publicar"
                }
            }
        }.start()
    }


    private fun subirVideo(uri: Uri): String {
        val boundary = "----FormBoundary${System.currentTimeMillis()}"
        val url  = URL("$BASE_URL/resenas/subir_video.php")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        conn.doOutput       = true
        conn.connectTimeout = 15000
        conn.readTimeout    = 30000

        DataOutputStream(conn.outputStream).use { out ->
            out.writeBytes("--$boundary\r\n")
            out.writeBytes("Content-Disposition: form-data; name=\"video\"; filename=\"video.mp4\"\r\n")
            out.writeBytes("Content-Type: video/mp4\r\n\r\n")

            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(4096)
                var bytes: Int
                while (input.read(buffer).also { bytes = it } != -1) {
                    out.write(buffer, 0, bytes)
                }
            }

            out.writeBytes("\r\n--$boundary--\r\n")
            out.flush()
        }

        return if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val respuesta = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            JSONObject(respuesta).optString("url", "")
        } else {
            conn.disconnect()
            ""
        }
    }


    private fun guardarResena(comentario: String, videoUrl: String) {
        val body = JSONObject().apply {
            put("comentario", comentario)
            put("video_url",  videoUrl)
            put("id_usuario", obtenerIdUsuario())
            put("id_lugar",   0)   // ajusta al id_lugar que corresponda
        }.toString()

        val url  = URL("$BASE_URL/resenas/crear.php")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput       = true
        conn.connectTimeout = 10000

        OutputStreamWriter(conn.outputStream).use { w ->
            w.write(body)
            w.flush()
        }

        val codigo = conn.responseCode
        conn.disconnect()

        requireActivity().runOnUiThread {
            if (codigo == HttpURLConnection.HTTP_OK || codigo == HttpURLConnection.HTTP_CREATED) {
                Toast.makeText(requireContext(), "¡Reseña publicada!", Toast.LENGTH_SHORT).show()
                etComentario.setText("")
                descartarVideo()
            } else {
                Toast.makeText(requireContext(), "Error del servidor: $codigo", Toast.LENGTH_SHORT).show()
            }
            btnPublicar.isEnabled = true
            btnPublicar.text      = "Publicar"
        }
    }


    private fun tienePermisos() =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED

    private fun solicitarPermisos() {
        permisosLauncher.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
    }

    private fun obtenerIdUsuario(): Int {
        val prefs = requireContext().getSharedPreferences("xploremx_prefs", 0)
        return prefs.getInt("id_usuario", 1)
    }
}