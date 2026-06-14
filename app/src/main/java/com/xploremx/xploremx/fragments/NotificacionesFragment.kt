package com.xploremx.xploremx.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xploremx.xploremx.R
import com.xploremx.xploremx.utils.Constants
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class Notificacion(
    val id: Int,
    val titulo: String,
    val fechaHora: String
)

class NotificacionesAdapter(private val items: List<Notificacion>) :
    RecyclerView.Adapter<NotificacionesAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloNotificacion)
        val txtFecha: TextView  = view.findViewById(R.id.txtFechaNotificacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_notificacion, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.txtTitulo.text = items[position].titulo
        holder.txtFecha.text  = items[position].fechaHora
    }

    override fun getItemCount() = items.size
}

class NotificacionesFragment : Fragment() {

    private val BASE_URL = Constants.BASE_URL
    private lateinit var recycler: RecyclerView
    private val notificaciones = mutableListOf<Notificacion>()
    private lateinit var adapter: NotificacionesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_notificaciones, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificacionesAdapter(notificaciones)
        recycler = view.findViewById(R.id.recyclerNotificaciones)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        cargarNotificaciones()
    }

    private fun cargarNotificaciones() {
        val idUsuario = obtenerIdUsuario()

        Thread {
            try {
                val url  = URL("$BASE_URL/recordatorios/proximos.php?id_usuario=$idUsuario")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod  = "GET"
                    connectTimeout = 5000
                    readTimeout    = 5000
                }

                val code  = conn.responseCode
                Log.d("NOTIF", "HTTP $code desde proximos.php")

                if (code == HttpURLConnection.HTTP_OK) {
                    val texto = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    Log.d("NOTIF", "Respuesta: $texto")

                    if (texto.isBlank() || texto.trim() == "[]") {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(),
                                "No hay recordatorios próximos", Toast.LENGTH_SHORT).show()
                        }
                        return@Thread
                    }

                    val arr   = JSONArray(texto)
                    val lista = mutableListOf<Notificacion>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        lista.add(Notificacion(
                            id        = o.getInt("id"),
                            titulo    = o.getString("titulo"),
                            fechaHora = o.getString("fecha_hora")
                        ))
                    }

                    requireActivity().runOnUiThread {
                        notificaciones.clear()
                        notificaciones.addAll(lista)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    val error = conn.errorStream?.bufferedReader()?.readText() ?: "sin detalle"
                    conn.disconnect()
                    Log.e("NOTIF", "Error $code: $error")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error servidor: $code", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("NOTIF", "Excepción: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun obtenerIdUsuario(): Int =
        requireContext().getSharedPreferences("xploremx_prefs", 0).getInt("id_usuario", 1)
}