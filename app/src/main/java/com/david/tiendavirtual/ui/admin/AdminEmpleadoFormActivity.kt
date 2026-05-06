package com.david.tiendavirtual.ui.admin

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.david.tiendavirtual.data.model.EmpleadoAdmin
import com.david.tiendavirtual.data.repository.AdminEmpleadosRepository
import com.david.tiendavirtual.databinding.ActivityAdminEmpleadoFormBinding
import com.david.tiendavirtual.R
import com.google.android.material.snackbar.Snackbar

class AdminEmpleadoFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EMPLEADO = "extra_empleado"
    }

    private lateinit var binding:       ActivityAdminEmpleadoFormBinding
    private lateinit var repo:          AdminEmpleadosRepository
    private var empleadoEditar: EmpleadoAdmin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminEmpleadoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = AdminEmpleadosRepository(this)

        // Modo edición si llega el empleado como extra
        @Suppress("DEPRECATION")
        empleadoEditar = intent.getSerializableExtra(EXTRA_EMPLEADO) as? EmpleadoAdmin

        if (empleadoEditar != null) {
            val emp = empleadoEditar!!
            binding.tvTituloEmpForm.text = "Editar Empleado"
            binding.etEmpNombre.setText(emp.nombre)
            binding.etEmpApellido.setText(emp.apellido)
            binding.etEmpUsuario.setText(emp.usuario)
            binding.tilEmpUsuario.isEnabled  = false           // no cambiar usuario en edición
            binding.tilEmpPassword.helperText = "Dejar vacío para no cambiar contraseña"
            if (emp.isAdmin) binding.chipRolAdmin.isChecked    = true
            else              binding.chipRolEmpleado.isChecked = true
        }

        binding.btnVolverEmpForm.setOnClickListener { finish() }
        binding.btnGuardarEmp.setOnClickListener    { guardar() }

        // ── Filtros de entrada ────────────────────────────────────────────
        val soloLetras = android.text.InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*"))) null else ""
        }
        val soloUsuario = android.text.InputFilter { source, _, _, _, _, _ ->
            if (source.matches(Regex("[a-zA-Z0-9_.]*"))) null else ""
        }
        val sinEspacios = android.text.InputFilter { source, _, _, _, _, _ ->
            if (source.contains(" ")) "" else null
        }
        binding.etEmpNombre.filters   = arrayOf(soloLetras,  android.text.InputFilter.LengthFilter(40))
        binding.etEmpApellido.filters = arrayOf(soloLetras,  android.text.InputFilter.LengthFilter(40))
        binding.etEmpUsuario.filters  = arrayOf(soloUsuario, android.text.InputFilter.LengthFilter(30))
        binding.etEmpPassword.filters = arrayOf(sinEspacios, android.text.InputFilter.LengthFilter(50))

        // Limpiar errores al escribir
        binding.etEmpNombre.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { binding.tilEmpNombre.error = null }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.etEmpApellido.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { binding.tilEmpApellido.error = null }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.etEmpUsuario.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { binding.tilEmpUsuario.error = null }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.etEmpPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { binding.tilEmpPassword.error = null }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun validar(): Boolean {
        var ok = true
        val nombre   = binding.etEmpNombre.text.toString().trim()
        val apellido = binding.etEmpApellido.text.toString().trim()
        val usuario  = binding.etEmpUsuario.text.toString().trim()
        val password = binding.etEmpPassword.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.tilEmpNombre.error = "El nombre es obligatorio"
            ok = false
        } else if (nombre.length < 2) {
            binding.tilEmpNombre.error = "Mínimo 2 caracteres"
            ok = false
        } else binding.tilEmpNombre.error = null

        if (apellido.isNotEmpty() && apellido.length < 2) {
            binding.tilEmpApellido.error = "Mínimo 2 caracteres"
            ok = false
        } else binding.tilEmpApellido.error = null

        if (usuario.isEmpty()) {
            binding.tilEmpUsuario.error = "El usuario es obligatorio"
            ok = false
        } else if (usuario.length < 3) {
            binding.tilEmpUsuario.error = "Mínimo 3 caracteres"
            ok = false
        } else binding.tilEmpUsuario.error = null

        if (empleadoEditar == null && password.isEmpty()) {
            binding.tilEmpPassword.error = "La contraseña es obligatoria"
            ok = false
        } else if (password.isNotEmpty() && password.length < 6) {
            binding.tilEmpPassword.error = "Mínimo 6 caracteres"
            ok = false
        } else binding.tilEmpPassword.error = null

        return ok
    }

    private fun guardar() {
        if (!validar()) return

        val nombre   = binding.etEmpNombre.text.toString().trim()
        val apellido = binding.etEmpApellido.text.toString().trim()
        val usuario  = binding.etEmpUsuario.text.toString().trim()
        val password = binding.etEmpPassword.text.toString().trim()
        val rol = if (binding.chipGroupRol.checkedChipId == R.id.chipRolAdmin) 1 else 2


        binding.progressEmpForm.visibility = View.VISIBLE
        binding.btnGuardarEmp.isEnabled    = false

        val onSuccess: (String) -> Unit = { msg ->
            binding.progressEmpForm.visibility = View.GONE
            binding.btnGuardarEmp.isEnabled    = true
            setResult(Activity.RESULT_OK)
            Snackbar.make(binding.root, "✓ $msg", Snackbar.LENGTH_SHORT).show()
            finish()
        }
        val onError: (String) -> Unit = { msg ->
            binding.progressEmpForm.visibility = View.GONE
            binding.btnGuardarEmp.isEnabled    = true
            Snackbar.make(binding.root, "❌ $msg", Snackbar.LENGTH_LONG).show()
        }

        if (empleadoEditar == null) {
            repo.agregar(usuario, nombre, apellido, rol, password, onSuccess, onError)
        } else {
            repo.editar(usuario, nombre, apellido, rol, password, onSuccess, onError)
        }
    }
}

