package com.david.tiendavirtual.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.david.tiendavirtual.data.model.Categoria
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.repository.AdminCategoriaRepository
import com.david.tiendavirtual.databinding.ActivityAdminCategoriasBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * AdminCategoriasActivity
 * Pantalla de gestión de categorías (solo Administrador).
 * Permite listar, buscar, agregar, editar y eliminar categorías.
 */
class AdminCategoriasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminCategoriasBinding
    private lateinit var adapter: AdminCategoriaAdapter
    private lateinit var repo:    AdminCategoriaRepository

    private var listaCompleta: List<Categoria> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCategoriasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Seguridad: solo administradores
        if (!UserSession.isAdmin) { finish(); return }

        // Avatar del admin → navega al perfil
        binding.tvAdminAvatarCat.text = UserSession.inicial.toString()
        binding.frameAvatarAdmin.setOnClickListener {
            startActivity(android.content.Intent(this, AdminProfileActivity::class.java))
        }

        repo = AdminCategoriaRepository(this)

        // RecyclerView
        adapter = AdminCategoriaAdapter(
            categorias = emptyList(),
            onEditar   = { cat -> mostrarDialogoEditar(cat) },
            onEliminar = { cat -> confirmarEliminar(cat) }
        )
        binding.recyclerCategorias.layoutManager = LinearLayoutManager(this)
        binding.recyclerCategorias.adapter = adapter

        binding.etBuscarCat.addTextChangedListener { text -> filtrar(text.toString()) }
        binding.btnVolverCat.setOnClickListener { finish() }
        binding.fabAgregarCat.setOnClickListener { mostrarDialogoAgregar() }

        cargarCategorias()
    }

    // ── Carga lista desde la API ──────────────────────────────────────────
    private fun cargarCategorias() {
        mostrarEstado(Estado.CARGANDO)
        repo.listar(
            onSuccess = { lista ->
                listaCompleta = lista
                filtrar(binding.etBuscarCat.text.toString())
                actualizarSubtitulo(lista.size)
                mostrarEstado(if (lista.isEmpty()) Estado.VACIO else Estado.LISTA)
            },
            onError = { msg ->
                mostrarEstado(Estado.VACIO)
                snack("⚠ $msg")
            }
        )
    }

    private fun filtrar(texto: String) {
        val q = texto.trim().lowercase()
        val resultado = if (q.isEmpty()) listaCompleta
                        else listaCompleta.filter { it.nombre.lowercase().contains(q) }
        adapter.actualizarLista(resultado)
        actualizarSubtitulo(resultado.size)
        mostrarEstado(if (resultado.isEmpty()) Estado.VACIO else Estado.LISTA)
    }

    private fun actualizarSubtitulo(cantidad: Int) {
        binding.tvCatSubtitulo.text = if (cantidad == 1) "1 categoría" else "$cantidad categorías"
    }

    // ── Diálogo AGREGAR ───────────────────────────────────────────────────
    private fun mostrarDialogoAgregar() {
        mostrarDialogoNombre(titulo = "Nueva categoría", valorInicial = "", botonOk = "Agregar") { nombre ->
            repo.agregar(
                nombre    = nombre,
                onSuccess = { msg, _ -> snack("✓ $msg"); cargarCategorias() },
                onError   = { msg -> snack("❌ $msg") }
            )
        }
    }

    // ── Diálogo EDITAR ────────────────────────────────────────────────────
    private fun mostrarDialogoEditar(cat: Categoria) {
        mostrarDialogoNombre(titulo = "Editar categoría", valorInicial = cat.nombre, botonOk = "Guardar") { nombre ->
            repo.editar(
                id        = cat.id,
                nombre    = nombre,
                onSuccess = { msg -> snack("✓ $msg"); cargarCategorias() },
                onError   = { msg -> snack("❌ $msg") }
            )
        }
    }

    /**
     * Diálogo reutilizable con TextInputLayout para nombre de categoría.
     * Mantiene el botón positivo activo solo si el campo no está vacío.
     */
    private fun mostrarDialogoNombre(
        titulo:      String,
        valorInicial: String,
        botonOk:     String,
        onConfirmar: (String) -> Unit
    ) {
        val til = TextInputLayout(this).apply {
            hint = "Nombre de la categoría *"
            setPaddingRelative(48, 16, 48, 0)
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxCornerRadii(14f, 14f, 14f, 14f)
        }
        val et = TextInputEditText(til.context).apply {
            setText(valorInicial)
            filters   = arrayOf(android.text.InputFilter.LengthFilter(25))
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setSingleLine()
        }
        til.addView(et)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(titulo)
            .setView(til)
            .setPositiveButton(botonOk, null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            et.setSelection(et.text?.length ?: 0)
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = et.text.toString().trim()
                if (nombre.isEmpty()) { til.error = "El nombre no puede estar vacío"; return@setOnClickListener }
                til.error = null
                dialog.dismiss()
                onConfirmar(nombre)
            }
        }
        dialog.show()
        et.requestFocus()
    }

    // ── Confirmar ELIMINAR ────────────────────────────────────────────────
    private fun confirmarEliminar(cat: Categoria) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar categoría")
            .setMessage("¿Eliminar «${cat.nombre}»?\n\nNo se puede eliminar si hay productos asociados.")
            .setPositiveButton("Eliminar") { _, _ ->
                repo.eliminar(
                    id        = cat.id,
                    onSuccess = { msg -> snack("✓ $msg"); cargarCategorias() },
                    onError   = { msg -> snack("❌ $msg") }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── Estados UI ────────────────────────────────────────────────────────
    private enum class Estado { CARGANDO, VACIO, LISTA }

    private fun mostrarEstado(estado: Estado) {
        binding.progressCat.visibility        = if (estado == Estado.CARGANDO) View.VISIBLE else View.GONE
        binding.layoutCatVacio.visibility     = if (estado == Estado.VACIO)    View.VISIBLE else View.GONE
        binding.recyclerCategorias.visibility = if (estado == Estado.LISTA)    View.VISIBLE else View.GONE
    }

    private fun snack(msg: String) =
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
}


