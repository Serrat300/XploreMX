package com.xploremx.xploremx.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.xploremx.xploremx.R
import com.xploremx.xploremx.activities.DetalleLugarActivity
import com.xploremx.xploremx.adapters.LugarAdapter
import com.xploremx.xploremx.databinding.FragmentHomeBinding
import com.xploremx.xploremx.models.Lugar
import org.json.JSONObject
import com.xploremx.xploremx.utils.Constants

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val listaLugares = mutableListOf<Lugar>()

    private val BASE_URL =
        Constants.BASE_URL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentHomeBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val nombreUsuario =
            arguments?.getString("nombre") ?: "user"

        binding.txtSaludo.text =
            "Hola, $nombreUsuario"

        binding.etBuscar.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    filtrarLugares(s.toString())
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        binding.recyclerLugares.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerLugares.adapter =
            LugarAdapter(
                requireContext(),
                listaLugares
            ) { lugar ->
                abrirDetalle(lugar)
            }

        cargarCategorias()
        cargarLugares(null)
    }

    private fun abrirDetalle(
        lugar: Lugar
    ) {

        val intent =
            Intent(
                requireContext(),
                DetalleLugarActivity::class.java
            )

        intent.putExtra("id", lugar.id)
        intent.putExtra("nombre", lugar.nombre)
        intent.putExtra("descripcion", lugar.descripcion)
        intent.putExtra("direccion", lugar.direccion)
        intent.putExtra("calificacion", lugar.calificacion)
        intent.putExtra("imagenUrl", lugar.imagenUrl)

        startActivity(intent)
    }

    private fun mostrarLugares(
        lista: List<Lugar>
    ) {

        binding.recyclerLugares.adapter =
            LugarAdapter(
                requireContext(),
                lista
            ) { lugar ->
                abrirDetalle(lugar)
            }
    }

    private fun filtrarLugares(
        texto: String
    ) {

        val listaFiltrada =
            listaLugares.filter { lugar ->

                lugar.nombre.contains(
                    texto,
                    ignoreCase = true
                ) ||
                        lugar.descripcion.contains(
                            texto,
                            ignoreCase = true
                        )
            }

        mostrarLugares(listaFiltrada)
    }

    private fun cargarCategorias() {

        val queue =
            Volley.newRequestQueue(
                requireContext()
            )

        val request =
            StringRequest(
                Request.Method.GET,
                "$BASE_URL/get_categorias.php",

                { response ->

                    val json =
                        JSONObject(response)

                    if (json.getBoolean("success")) {

                        val categorias =
                            json.getJSONArray("categorias")

                        binding.layoutCategorias.removeAllViews()

                        val params =
                            android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                        params.marginEnd = 8

                        val btnTodos =
                            Button(requireContext())

                        btnTodos.text = "Todos"

                        btnTodos.backgroundTintList =
                            android.content.res.ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.terracota
                                )
                            )

                        btnTodos.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.blanco
                            )
                        )

                        btnTodos.layoutParams =
                            params

                        btnTodos.setOnClickListener {
                            cargarLugares(null)
                        }

                        binding.layoutCategorias.addView(
                            btnTodos
                        )

                        for (i in 0 until categorias.length()) {

                            val cat =
                                categorias.getJSONObject(i)

                            val btn =
                                Button(requireContext())

                            btn.text =
                                cat.getString("nombre")

                            btn.backgroundTintList =
                                android.content.res.ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.verde_profundo
                                    )
                                )

                            btn.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.blanco
                                )
                            )

                            btn.layoutParams =
                                params

                            btn.setOnClickListener {

                                cargarLugares(
                                    cat.getInt("id")
                                )
                            }

                            binding.layoutCategorias.addView(
                                btn
                            )
                        }
                    }
                },

                {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar categorías",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

        queue.add(request)
    }

    private fun cargarLugares(
        idCategoria: Int?
    ) {

        val queue =
            Volley.newRequestQueue(
                requireContext()
            )

        val url =
            if (idCategoria != null)
                "$BASE_URL/get_lugares.php?id_categoria=$idCategoria"
            else
                "$BASE_URL/get_lugares.php"

        val request =
            StringRequest(
                Request.Method.GET,
                url,

                { response ->

                    val json =
                        JSONObject(response)

                    if (json.getBoolean("success")) {

                        listaLugares.clear()

                        val lugares =
                            json.getJSONArray("lugares")

                        for (i in 0 until lugares.length()) {

                            val l =
                                lugares.getJSONObject(i)

                            listaLugares.add(
                                Lugar(
                                    id = l.getInt("id"),
                                    nombre = l.getString("nombre"),
                                    descripcion = if (l.isNull("descripcion")) "" else l.getString("descripcion"),
                                    direccion = if (l.isNull("direccion")) "" else l.getString("direccion"),
                                    latitud = l.getDouble("latitud"),
                                    longitud = l.getDouble("longitud"),
                                    imagenUrl = if (l.isNull("imagen_url")) "" else l.getString("imagen_url"),
                                    calificacion = l.getDouble("calificacion"),
                                    idCategoria = l.getInt("id_categoria")
                                )
                            )
                        }

                        mostrarLugares(
                            listaLugares
                        )
                    }
                },

                {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar lugares",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

        queue.add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}