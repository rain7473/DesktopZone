package com.david.tiendavirtual.ui.admin

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Seguridad: solo ADMINs
        if (!UserSession.isAdmin) { finish(); return }

        configurarHeader()
        configurarNavegacion()

        if (savedInstanceState == null) {
            binding.adminBottomNav.selectedItemId = R.id.nav_dashboard
        }

        animarHeader()
    }

    private fun configurarHeader() {
        binding.tvAdminNombreMain.text = "${UserSession.nombreCompleto} • Administrador"
        binding.tvHeaderAvatarInicial.text = UserSession.inicial.toString()

        // Notificaciones (placeholder)
        binding.btnHeaderNotificaciones.setOnClickListener { v ->
            animarIcono(v)
            Toast.makeText(this, "Notificaciones próximamente", Toast.LENGTH_SHORT).show()
        }

        // Búsqueda (placeholder)
        binding.btnHeaderBusqueda.setOnClickListener { v ->
            animarIcono(v)
            Toast.makeText(this, "Búsqueda próximamente", Toast.LENGTH_SHORT).show()
        }

        // Avatar → Perfil admin
        binding.btnHeaderAvatar.setOnClickListener { v ->
            animarIcono(v)
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
        binding.adminBottomNav.setOnItemSelectedListener { item ->
            val (frag, titulo) = when (item.itemId) {
                R.id.nav_dashboard  -> DashboardFragment()  to "Dashboard"
                R.id.nav_inventario -> InventarioFragment() to "Inventario"
                R.id.nav_empleados  -> EmpleadosFragment()  to "Empleados"
                R.id.nav_finanzas   -> FinanzasFragment()   to "Finanzas"
                R.id.nav_ventas     -> VentasFragment()     to "Ventas"
                else                -> return@setOnItemSelectedListener false
            }
            cargarFragmento(frag, titulo)
            true
        }
    }

    private fun cargarFragmento(fragment: Fragment, titulo: String) {
        binding.tvAdminSeccion.text = titulo
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }

    /** Microanimación scale en íconos del header */
    private fun animarIcono(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.8f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.8f, 1.1f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 280
            interpolator = OvershootInterpolator(2f)
            start()
        }
    }

    /** Animación suave de entrada del header */
    private fun animarHeader() {
        val header = binding.tvAdminSeccion.parent as View
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

