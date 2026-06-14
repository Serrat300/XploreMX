package com.xploremx.xploremx.fragments

import android.app.AlertDialog
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xploremx.xploremx.R
import com.xploremx.xploremx.activities.MainActivity
import com.xploremx.xploremx.receivers.AlarmaReceiver
import com.xploremx.xploremx.utils.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


const val CANAL_ALARMAS_ID   = "alarmas"
const val CANAL_ALARMAS_NAME = "Alarmas y recordatorios"


data class Alarma(
    val id: Int,
    val titulo: String,
    val fechaHora: String,
    val idLugar: Int = 0
)


class AlarmasAdapter(
    private val items: MutableList<Alarma>,
    private val onEditar: (Alarma) -> Unit,
    private val onEliminar: (Alarma) -> Unit
) : RecyclerView.Adapter<AlarmasAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val check: CheckBox        = view.findViewById(R.id.checkAlarma)
        val txtTitulo: TextView    = view.findViewById(R.id.txtTituloAlarma)
        val txtFecha: TextView     = view.findViewById(R.id.txtFechaAlarma)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditarAlarma)
        val btnBorrar: ImageButton = view.findViewById(R.id.btnEliminarAlarma)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_alarma, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.txtTitulo.text  = item.titulo
        holder.txtFecha.text   = item.fechaHora
        holder.check.isChecked = false
        holder.btnEditar.setOnClickListener { onEditar(item) }
        holder.btnBorrar.setOnClickListener { onEliminar(item) }
    }

    override fun getItemCount() = items.size
}


class AlarmasFragment : Fragment(), SensorEventListener {

    private val BASE_URL = Constants.BASE_URL
    private lateinit var sensorManager: SensorManager
    private var sensorLuz: Sensor? = null
    private var notificacionSensorMostrada = false

    private lateinit var recycler: RecyclerView
    private val alarmas = mutableListOf<Alarma>()
    private lateinit var adapter: AlarmasAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_alarmas, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crearCanalNotificaciones()

        adapter = AlarmasAdapter(alarmas,
            onEditar   = { mostrarDialogoAlarma(it) },
            onEliminar = { confirmarEliminar(it) }
        )
        recycler = view.findViewById(R.id.recyclerAlarmas)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        view.findViewById<Button>(R.id.btnAnadirAlarma).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!am.canScheduleExactAlarms()) {
                    pedirPermisoAlarmaExacta()
                    return@setOnClickListener
                }
            }
            mostrarDialogoAlarma(null)
        }

        cargarAlarmas()

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorLuz     = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onResume() {
        super.onResume()
        sensorLuz?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        val lux = event?.values?.get(0) ?: return
        if (lux < 20f && !notificacionSensorMostrada) {
            notificacionSensorMostrada = true
            mostrarNotificacionSensor()
        }
        if (lux > 30f) notificacionSensorMostrada = false
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ALARMAS_ID,
                CANAL_ALARMAS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Alarmas y recordatorios de XploreMX" }

            requireContext()
                .getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }
    }

    private fun mostrarNotificacionSensor() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            putExtra("abrirNotificaciones", true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            requireContext(), 999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(requireContext(), CANAL_ALARMAS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Modo oscuro detectado")
            .setContentText("¿Deseas revisar tus recordatorios?")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        requireContext().getSystemService(NotificationManager::class.java).notify(100, notif)
    }

    private fun pedirPermisoAlarmaExacta() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permiso necesario")
            .setMessage("Para programar alarmas exactas activa \"Alarmas y recordatorios\" en los ajustes de la app.")
            .setPositiveButton("Ir a ajustes") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startActivity(Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:${requireContext().packageName}")
                    ))
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarAlarmas() {
        Thread {
            try {
                val url  = URL("$BASE_URL/recordatorios/lista.php?id_usuario=${obtenerIdUsuario()}")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod  = "GET"
                    connectTimeout = 5000
                    readTimeout    = 5000
                }
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val texto = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    Log.d("ALARMAS", "lista → $texto")

                    val arr   = JSONArray(texto)
                    val lista = mutableListOf<Alarma>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        lista.add(Alarma(o.getInt("id"), o.getString("titulo"),
                            o.getString("fecha_hora"), o.optInt("id_lugar", 0)))
                    }
                    requireActivity().runOnUiThread {
                        alarmas.clear(); alarmas.addAll(lista)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    conn.disconnect()
                    mostrarToast("Error servidor: ${conn.responseCode}")
                }
            } catch (e: Exception) { mostrarToast("Error al cargar: ${e.message}") }
        }.start()
    }

    private fun crearAlarma(titulo: String, fechaHora: String) {
        Thread {
            try {
                val conn = postJson("$BASE_URL/recordatorios/crear.php",
                    JSONObject().apply {
                        put("titulo",     titulo)
                        put("fecha_hora", fechaHora)
                        put("id_usuario", obtenerIdUsuario())
                        put("id_lugar",   JSONObject.NULL)
                    })
                val code     = conn.responseCode
                val respuesta = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                Log.d("ALARMAS", "crear → $code | $respuesta")

                if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED) {
                    // CORRECCIÓN 5: usar el id devuelto por el servidor como requestCode
                    val idNueva = try { JSONObject(respuesta).getInt("id") }
                    catch (e: Exception) { titulo.hashCode() }
                    requireActivity().runOnUiThread {
                        programarAlarma(idNueva, titulo, fechaHora)
                        cargarAlarmas()
                    }
                } else mostrarToast("Error al crear: $code")
            } catch (e: Exception) { mostrarToast("Error: ${e.message}") }
        }.start()
    }

    private fun editarAlarma(id: Int, titulo: String, fechaHora: String) {
        Thread {
            try {
                val conn = postJson("$BASE_URL/recordatorios/editar.php",
                    JSONObject().apply { put("id", id); put("titulo", titulo); put("fecha_hora", fechaHora) })
                val code = conn.responseCode
                conn.disconnect()
                if (code == HttpURLConnection.HTTP_OK) {
                    requireActivity().runOnUiThread {
                        programarAlarma(id, titulo, fechaHora)
                        cargarAlarmas()
                    }
                } else mostrarToast("Error al editar: $code")
            } catch (e: Exception) { mostrarToast("Error: ${e.message}") }
        }.start()
    }

    private fun eliminarAlarma(id: Int) {
        Thread {
            try {
                val conn = postJson("$BASE_URL/recordatorios/eliminar.php",
                    JSONObject().apply { put("id", id) })
                val code = conn.responseCode
                conn.disconnect()
                if (code == HttpURLConnection.HTTP_OK) {
                    cancelarAlarma(id)
                    requireActivity().runOnUiThread { cargarAlarmas() }
                } else mostrarToast("Error al eliminar: $code")
            } catch (e: Exception) { mostrarToast("Error: ${e.message}") }
        }.start()
    }

    private fun postJson(endpoint: String, body: JSONObject): HttpURLConnection {
        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput       = true
            connectTimeout = 5000
        }
        OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
        return conn
    }

    private fun programarAlarma(id: Int, titulo: String, fechaHora: String) {
        val fechaCorregida = if (fechaHora.length == 16) "$fechaHora:00" else fechaHora
        val fecha = try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(fechaCorregida)
        } catch (e: Exception) { null } ?: run { mostrarToast("Fecha inválida"); return }

        if (fecha.time <= System.currentTimeMillis()) {
            mostrarToast("La fecha debe ser futura")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) { pedirPermisoAlarmaExacta(); return }
        }

        val pi = PendingIntent.getBroadcast(
            requireContext(), id,
            Intent(requireContext(), AlarmaReceiver::class.java).apply { putExtra("titulo", titulo) },
            // FLAG_UPDATE_CURRENT: actualiza el extra "titulo" si ya existía este PendingIntent
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            (requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                .setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fecha.time, pi)
            Log.d("ALARMAS", "Programada: $titulo @ $fechaCorregida (id=$id)")
            mostrarToast("Alarma programada ✓")
        } catch (e: SecurityException) {
            Log.e("ALARMAS", "SecurityException: ${e.message}")
            mostrarToast("Sin permiso para alarmas exactas")
        }
    }

    private fun cancelarAlarma(id: Int) {
        val pi = PendingIntent.getBroadcast(
            requireContext(), id,
            Intent(requireContext(), AlarmaReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)
    }

    private fun mostrarDialogoAlarma(alarma: Alarma?) {
        val esEdicion  = alarma != null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_alarma, null)
        val etTitulo   = dialogView.findViewById<EditText>(R.id.etDialogTitulo)
        val btnFecha   = dialogView.findViewById<Button>(R.id.btnSeleccionarFecha)
        val btnHora    = dialogView.findViewById<Button>(R.id.btnSeleccionarHora)

        var fecha = ""; var hora = ""

        if (esEdicion) {
            etTitulo.setText(alarma!!.titulo)
            val p = alarma.fechaHora.split(" ")
            if (p.size >= 2) {
                fecha = p[0]; hora = p[1].take(5)
                btnFecha.text = fecha; btnHora.text = hora
            }
        }

        btnFecha.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                fecha = "%04d-%02d-%02d".format(y, m + 1, d); btnFecha.text = fecha
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        btnHora.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, min ->
                hora = "%02d:%02d".format(h, min); btnHora.text = hora
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (esEdicion) "Editar recordatorio" else "Nueva alarma")
            .setView(dialogView)
            .setPositiveButton(if (esEdicion) "Guardar" else "Crear") { _, _ ->
                val titulo = etTitulo.text.toString().trim()
                if (titulo.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
                    Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (esEdicion) editarAlarma(alarma!!.id, titulo, "$fecha $hora")
                else           crearAlarma(titulo, "$fecha $hora")
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun confirmarEliminar(alarma: Alarma) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar recordatorio")
            .setMessage("¿Deseas eliminar \"${alarma.titulo}\"?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarAlarma(alarma.id) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun mostrarToast(msg: String) =
        requireActivity().runOnUiThread { Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }

    private fun obtenerIdUsuario(): Int {
        val id = requireContext().getSharedPreferences("xploremx_prefs", 0).getInt("id_usuario", 1)
        Log.d("ALARMAS", "id_usuario=$id"); return id
    }
}