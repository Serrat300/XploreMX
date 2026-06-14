package com.xploremx.xploremx.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.xploremx.xploremx.R
import com.xploremx.xploremx.databinding.ActivityLoginBinding
import org.json.JSONObject
import com.xploremx.xploremx.utils.Constants

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 100
    private val URL = "${Constants.BASE_URL}/login.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Login normal
        binding.btnLogin.setOnClickListener {
            val usuario = binding.etUsuario.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            iniciarSesion(usuario, contrasena)
        }

        // Google Sign-In
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // Ir al Registro
        binding.txtIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error con Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val nombre = user?.displayName ?: "Usuario"
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("nombre", nombre)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun iniciarSesion(usuario: String, contrasena: String) {
        val queue = Volley.newRequestQueue(this)
        val request = object : StringRequest(
            Request.Method.POST, URL,
            { response ->
                val json = JSONObject(response)
                if (json.getBoolean("success")) {
                    val prefs = getSharedPreferences("xploremx_prefs", MODE_PRIVATE)

                    prefs.edit()
                        .putInt("id_usuario", json.getInt("id"))
                        .putString("nombre", json.getString("nombre"))
                        .apply()

                    Toast.makeText(this, "Bienvenid@ ${json.getString("nombre")}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("nombre", json.getString("nombre"))
                    intent.putExtra("idUsuario", json.getInt("id"))
                    startActivity(intent)
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
                    "usuario" to usuario,
                    "contrasena" to contrasena
                )
            }
        }
        queue.add(request)
    }
}