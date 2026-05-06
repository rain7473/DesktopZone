package com.david.tiendavirtual.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.android.volley.Request.Method
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.network.ApiConfig
import com.david.tiendavirtual.databinding.ActivityLoginBinding
import com.david.tiendavirtual.ui.admin.AdminMainActivity
import com.david.tiendavirtual.ui.empleado.EmpleadoMainActivity
import com.david.tiendavirtual.ui.home.MainActivity
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.green_primary)
        WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = false

        // ── Filtros de entrada ────────────────────────────────────────────
        val sinEspacios = android.text.InputFilter { source, _, _, _, _, _ ->
            if (source.contains(" ")) "" else null
        }
        binding.etUsuario.filters    = arrayOf(sinEspacios, android.text.InputFilter.LengthFilter(30))
        binding.etContrasena.filters = arrayOf(sinEspacios, android.text.InputFilter.LengthFilter(50))

        // ── Login ─────────────────────────────────────────────────────────
        binding.btnLogin.setOnClickListener {
            if (validarLogin()) {
                val usuario    = binding.etUsuario.text.toString().trim()
                val contrasena = binding.etContrasena.text.toString().trim()
                iniciarSesion(usuario, contrasena)
            }
        }

        // Limpiar errores al escribir
        binding.etUsuario.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { binding.tilUsuario.error = null }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.etContrasena.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { binding.tilContrasena.error = null }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // ── Continuar sin login (modo cliente) ───────────────────────────
        binding.btnContinuarSinLogin.setOnClickListener {
            UserSession.limpiar()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // ── Validación login ─────────────────────────────────────────────────
    private fun validarLogin(): Boolean {
        var ok = true
        val usuario    = binding.etUsuario.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString().trim()

        if (usuario.isEmpty()) {
            binding.tilUsuario.error = "Ingresa tu usuario"; ok = false
        } else if (usuario.length < 3) {
            binding.tilUsuario.error = "El usuario debe tener al menos 3 caracteres"; ok = false
        } else { binding.tilUsuario.error = null }

        if (contrasena.isEmpty()) {
            binding.tilContrasena.error = "Ingresa tu contraseña"; ok = false
        } else if (contrasena.length < 4) {
            binding.tilContrasena.error = "Contraseña demasiado corta"; ok = false
        } else { binding.tilContrasena.error = null }

        return ok
    }

    // ── Login: validar y redirigir según rol ─────────────────────────────
    private fun iniciarSesion(usuario: String, contrasena: String) {
        val queue = Volley.newRequestQueue(this)
        val request = object : StringRequest(
            Method.POST, ApiConfig.LOGIN,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("ok")) {
                        val u = json.getJSONObject("usuario")
                        UserSession.usuario  = u.optString("usuario",  "")
                        UserSession.nombre   = u.optString("nombre",   "")
                        UserSession.apellido = u.optString("apellido", "")
                        UserSession.rol      = u.optString("rol",      "2")

                        val destino: Class<*> = when {
                            UserSession.isAdmin    -> AdminMainActivity::class.java
                            UserSession.isEmpleado -> EmpleadoMainActivity::class.java
                            else                   -> MainActivity::class.java
                        }
                        startActivity(Intent(this, destino))
                        finish()
                    } else {
                        Toast.makeText(this, json.getString("mensaje"), Toast.LENGTH_SHORT).show()
                    }
                } catch (_: Exception) {
                    Toast.makeText(this, "Error procesando respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            { error -> Toast.makeText(this, error.message ?: "Error de red", Toast.LENGTH_SHORT).show() }
        ) {
            override fun getParams() = mapOf("usuario" to usuario, "contrasena" to contrasena)
        }
        queue.add(request)
    }
}
