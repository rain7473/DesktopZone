package com.david.tiendavirtual.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.databinding.ActivitySettingsBinding
import com.david.tiendavirtual.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVolverSettings.setOnClickListener { finish() }

        // Datos del usuario
        actualizarHeaderUsuario()

        // Notificaciones
        binding.switchNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            Snackbar.make(
                binding.root,
                if (isChecked) "🔔 Notificaciones activadas" else "🔕 Notificaciones desactivadas",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Tema oscuro funcional
        val prefs = getSharedPreferences("tienda_prefs", Context.MODE_PRIVATE)
        val modoOscuroActual = prefs.getBoolean("modo_oscuro", false)
        binding.switchTema.isChecked = modoOscuroActual

        binding.switchTema.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("modo_oscuro", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Cambiar contraseña / Editar perfil
        binding.cardCambiarPass.setOnClickListener { mostrarDialogoEditarPerfil() }

        // Idioma
        binding.cardIdioma.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Idioma")
                .setItems(arrayOf("🇪🇸 Español", "🇺🇸 English (próximamente)")) { _, which ->
                    if (which == 0)
                        Snackbar.make(binding.root, "Idioma: Español (activo)", Snackbar.LENGTH_SHORT).show()
                    else
                        Snackbar.make(binding.root, "English próximamente", Snackbar.LENGTH_SHORT).show()
                }
                .show()
        }

        // Acerca de
        binding.cardAcercaDe.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Tienda Virtual")
                .setMessage("Versión 1.0\n\nDesarrollada con Kotlin + Material3.\n© 2026")
                .setPositiveButton("OK", null)
                .show()
        }

        // Cerrar sesión con confirmación
        binding.cardCerrarSesionSettings.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas salir?")
                .setPositiveButton("Salir") { _, _ ->
                    UserSession.limpiar()
                    startActivity(
                        Intent(this, LoginActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun actualizarHeaderUsuario() {
        binding.tvSettingsUsuario.text = UserSession.usuario
        binding.tvSettingsNombre.text  = UserSession.nombreCompleto
        binding.tvSettingsRol.text     = if (UserSession.isAdmin) "Administrador" else "Empleado"
    }

    private fun mostrarDialogoEditarPerfil() {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, 0)
        }
        val etNombre = EditText(this).apply {
            hint = "Nombre"
            setText(UserSession.nombre)
        }
        val etApellido = EditText(this).apply {
            hint = "Apellido"
            setText(UserSession.apellido)
        }
        dialogView.addView(etNombre)
        dialogView.addView(etApellido)

        AlertDialog.Builder(this)
            .setTitle("Editar perfil")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre   = etNombre.text.toString().trim()
                val apellido = etApellido.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    UserSession.nombre   = nombre
                    UserSession.apellido = apellido
                    actualizarHeaderUsuario()
                    Snackbar.make(binding.root, "✓ Perfil actualizado", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "El nombre no puede estar vacío", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
