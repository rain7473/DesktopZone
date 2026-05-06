package com.david.tiendavirtual.ui.perfil

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.repository.FacturaRepository
import com.david.tiendavirtual.databinding.ActivityProfileBinding
import com.david.tiendavirtual.ui.favoritos.FavoritosActivity
import com.david.tiendavirtual.ui.login.LoginActivity
import com.david.tiendavirtual.ui.pedidos.OrderHistoryActivity
import com.david.tiendavirtual.ui.settings.SettingsActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var repo: FacturaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = FacturaRepository(this)

        binding.btnVolverPerfil.setOnClickListener { finish() }

        // Datos del usuario
        val rolNombre = if (UserSession.isAdmin) "Administrador" else "Empleado"
        binding.tvInicial.text       = UserSession.inicial.toString()
        binding.tvNombreUsuario.text  = UserSession.nombreCompleto
        binding.tvRolUsuario.text     = rolNombre
        binding.tvStatRol.text        = rolNombre
        binding.tvStatPedidos.text    = "…"   // se actualiza desde BD

        // Navegación
        binding.cardMisPedidos.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }
        binding.cardFavoritos.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }
        binding.cardConfiguracion.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.cardCerrarSesion.setOnClickListener {
            UserSession.limpiar()
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorPedidos()
    }

    /** Consulta la BD para obtener el número real de pedidos */
    private fun actualizarContadorPedidos() {
        repo.obtenerPedidos(
            onSuccess = { pedidos ->
                binding.tvStatPedidos.text = pedidos.size.toString()
            },
            onError = {
                binding.tvStatPedidos.text = "0"
            }
        )
    }
}
