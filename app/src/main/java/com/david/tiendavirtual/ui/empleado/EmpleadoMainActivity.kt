package com.david.tiendavirtual.ui.empleado

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.databinding.ActivityEmpleadoMainBinding
import com.david.tiendavirtual.ui.admin.AdminProfileActivity
import com.david.tiendavirtual.ui.admin.InventarioFragment

class EmpleadoMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmpleadoMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpleadoMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Seguridad: solo EMPLEADOS
        if (!UserSession.isEmpleado) { finish(); return }

        configurarHeader()
        configurarNavegacion()

        if (savedInstanceState == null) {
            binding.empleadoBottomNav.selectedItemId = R.id.nav_dashboard
        }

        animarHeader()
    }

    private fun configurarHeader() {
        binding.tvEmpleadoNombreMain.text = "${UserSession.nombreCompleto} • Empleado"
        binding.tvHeaderAvatarInicial.text = UserSession.inicial.toString()

        // Avatar → Perfil (reutiliza AdminProfileActivity)
        binding.btnHeaderAvatar.setOnClickListener { v ->
            v.postDelayed({
                val intent = Intent(this, AdminProfileActivity::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                startActivity(intent, options.toBundle())
            }, 150)
        }
    }

    private fun configurarNavegacion() {
        binding.empleadoBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard  -> cargarFragmento(EmpleadoDashboardFragment(), "Dashboard")
                R.id.nav_inventario -> cargarFragmento(InventarioFragment(),         "Inventario")
                else                -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    private fun cargarFragmento(fragment: Fragment, titulo: String) {
        binding.tvEmpleadoSeccion.text = titulo
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.empleadoFragmentContainer, fragment)
            .commit()
    }

    /** Animación suave de entrada del header */
    private fun animarHeader() {
        val header = binding.tvEmpleadoSeccion.parent as View
        header.translationY = -40f
        header.alpha = 0f
        ObjectAnimator.ofFloat(header, View.TRANSLATION_Y, -40f, 0f).apply {
            duration = 400
            start()
        }
        ObjectAnimator.ofFloat(header, View.ALPHA, 0f, 1f).apply {
            duration = 400
            start()
        }
    }
}

