package com.xploremx.xploremx.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.xploremx.xploremx.databinding.ActivityRegistroBinding
import org.json.JSONObject
import com.xploremx.xploremx.utils.Constants

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private val URL = "${Constants.BASE_URL}/registro.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val usuario = binding.etUsuarioReg.text.toString().trim()
            val contrasena = binding.etContrasenaReg.text.toString().trim()
            val confirmar = binding.etConfirmarContrasena.text.toString().trim()

            // Validaciones
            if (nombre.isEmpty() || usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena != confirmar) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena.length < 4) {
                Toast.makeText(this, "La contraseña debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registrar(nombre, usuario, contrasena)
        }

        // Ir al Login
        binding.txtIrLogin.setOnClickListener {
            finish()
        }
    }

    private fun registrar(nombre: String, usuario: String, contrasena: String) {
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, URL,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    Toast.makeText(this, "¡Cuenta creada exitosamente!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            { _ ->
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "nombre" to nombre,
                    "usuario" to usuario,
                    "contrasena" to contrasena
                )
            }
        }

        queue.add(request)
    }
}