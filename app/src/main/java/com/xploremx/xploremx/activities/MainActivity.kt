package com.xploremx.xploremx.activities

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.xploremx.xploremx.R
import com.xploremx.xploremx.fragments.AlarmasFragment
import com.xploremx.xploremx.fragments.ExperienciasFragment
import com.xploremx.xploremx.fragments.HomeFragment
import com.xploremx.xploremx.fragments.NotificacionesFragment
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var nombreUsuario = "user"
    private var idUsuario = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nombreUsuario =
            intent.getStringExtra("nombre") ?: "user"

        idUsuario =
            intent.getIntExtra("idUsuario", 0)


        val toolbar =
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        drawerLayout =
            findViewById(R.id.drawerLayout)

        navigationView =
            findViewById(R.id.navigationView)

        val header = navigationView.getHeaderView(0)
        val txtBienvenido = header.findViewById<TextView>(R.id.txtBienvenido)

        txtBienvenido.text = "Bienvenido, $nombreUsuario"


        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        )
        toggle.drawerArrowDrawable.color = getColor(R.color.terracota)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {

            if (
                intent.getBooleanExtra(
                    "abrirAlarmas",
                    false
                )
            ) {

                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        AlarmasFragment()
                    )
                    .commit()

            } else {

                val homeFragment = HomeFragment().apply {
                    arguments = Bundle().apply {
                        putString("nombre", nombreUsuario)
                    }
                }

                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        homeFragment
                    )
                    .commit()
            }
        }

        navigationView.setNavigationItemSelectedListener {

            when (it.itemId) {

                R.id.nav_sesion -> {
                    getSharedPreferences("xploremx_prefs", MODE_PRIVATE)
                        .edit().clear().apply()

                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

                    val intent = android.content.Intent(this, LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                R.id.nav_experiencias -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            ExperienciasFragment()
                        )
                        .commit()
                }

                R.id.nav_notificaciones -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            NotificacionesFragment()
                        )
                        .commit()
                }

                R.id.nav_alarmas -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.fragmentContainer,
                            AlarmasFragment()
                        )
                        .commit()
                }
            }

            drawerLayout.closeDrawers()
            true
        }
    }
}