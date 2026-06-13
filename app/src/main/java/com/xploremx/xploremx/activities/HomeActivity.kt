package com.xploremx.xploremx.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import com.xploremx.xploremx.R
import com.xploremx.xploremx.adapters.LugarAdapter
import com.xploremx.xploremx.databinding.ActivityHomeBinding
import com.xploremx.xploremx.models.Lugar
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val listaLugares = mutableListOf<Lugar>()
    private val BASE_URL = "http://192.168.100.56/xploremx"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val nombreUsuario = intent.getStringExtra("nombre") ?: "user"
        binding.txtSaludo.text = "Hola, $nombreUsuario"

        // Buscador
        binding.etBuscar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLugares(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Menú lateral
        binding.toolbar.setNavigationIcon(R.drawable.baseline_menu_24)
        binding.toolbar.setNavigationOnClickListener {
            val drawer = findViewById<DrawerLayout>(R.id.drawerLayout)
            val navView = findViewById<NavigationView>(R.id.navigationView)
            drawer.openDrawer(navView)
        }

        // RecyclerView vacío al inicio
        binding.recyclerLugares.layoutManager = LinearLayoutManager(this)
        binding.recyclerLugares.adapter = LugarAdapter(this, listaLugares) { lugar ->
            abrirDetalle(lugar)
        }

        cargarCategorias()
        cargarLugares(null)
    }

    private fun abrirDetalle(lugar: Lugar) {
        val intent = Intent(this, DetalleLugarActivity::class.java)
        intent.putExtra("id", lugar.id)
        intent.putExtra("nombre", lugar.nombre)
        intent.putExtra("descripcion", lugar.descripcion)
        intent.putExtra("direccion", lugar.direccion)
        intent.putExtra("calificacion", lugar.calificacion)
        intent.putExtra("imagenUrl", lugar.imagenUrl)
        startActivity(intent)
    }

    private fun mostrarLugares(lista: List<Lugar>) {
        binding.recyclerLugares.adapter = LugarAdapter(this, lista) { lugar ->
            abrirDetalle(lugar)
        }
    }

    private fun filtrarLugares(texto: String) {
        val listaFiltrada = listaLugares.filter { lugar ->
            lugar.nombre.contains(texto, ignoreCase = true) ||
                    lugar.descripcion.contains(texto, ignoreCase = true)
        }
        mostrarLugares(listaFiltrada)
    }

    private fun cargarCategorias() {
        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(
            Request.Method.GET, "$BASE_URL/get_categorias.php",
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    val categorias = json.getJSONArray("categorias")
                    binding.layoutCategorias.removeAllViews()

                    val params = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.marginEnd = 8

// Botón "Todos"
                    val btnTodos = Button(this)
                    btnTodos.text = "Todos"
                    btnTodos.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.terracota))
                    btnTodos.setTextColor(getColor(R.color.blanco))
                    btnTodos.setPadding(24, 24, 24, 24)
                    btnTodos.layoutParams = params
                    btnTodos.setOnClickListener { cargarLugares(null) }
                    binding.layoutCategorias.addView(btnTodos)

// Botones de categorías
                    for (i in 0 until categorias.length()) {
                        val cat = categorias.getJSONObject(i)
                        val btn = Button(this)
                        btn.text = cat.getString("nombre")
                        btn.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.verde_profundo))
                        btn.setTextColor(getColor(R.color.blanco))
                        btn.setPadding(24, 24, 24, 24)
                        btn.layoutParams = params
                        btn.setOnClickListener {
                            cargarLugares(cat.getInt("id"))
                        }
                        binding.layoutCategorias.addView(btn)
                    }
                }
            },
            { _ ->
                Toast.makeText(this, "Error al cargar categorías", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }


    private fun cargarLugares(idCategoria: Int?) {
        val queue = Volley.newRequestQueue(this)
        val url = if (idCategoria != null)
            "$BASE_URL/get_lugares.php?id_categoria=$idCategoria"
        else
            "$BASE_URL/get_lugares.php"

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    listaLugares.clear()
                    val lugares = json.getJSONArray("lugares")
                    for (i in 0 until lugares.length()) {
                        val l = lugares.getJSONObject(i)
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
                    mostrarLugares(listaLugares)
                    mostrarLugares(listaLugares)
                }
            },
            { _ ->
                Toast.makeText(this, "Error al cargar lugares", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}