package com.david.tiendavirtual.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.david.tiendavirtual.data.model.Categoria
import com.david.tiendavirtual.data.model.Marca
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.repository.AdminProductoRepository
import com.david.tiendavirtual.databinding.ActivityAdminProductoFormBinding
import com.google.android.material.snackbar.Snackbar

class AdminProductoFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCTO = "extra_producto"
    }

    private lateinit var binding:    ActivityAdminProductoFormBinding
    private lateinit var repo:       AdminProductoRepository
    private var productoEditar:      Producto? = null

    private var categorias: List<Categoria> = emptyList()
    private var marcas:     List<Marca>     = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProductoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Seguridad
        if (!UserSession.isAdmin) { finish(); return }

        repo = AdminProductoRepository(this)

        // Determinar si es edición o creación
        @Suppress("DEPRECATION")
        productoEditar = intent.getSerializableExtra(EXTRA_PRODUCTO) as? Producto

        if (productoEditar != null) {
            binding.tvTituloForm.text = "Editar Producto"
            binding.tilIdProducto.visibility = View.GONE   // No editar ID
            rellenarCampos(productoEditar!!)
        } else {
            binding.tvTituloForm.text = "Agregar Producto"
            binding.tilIdProducto.visibility = View.VISIBLE
        }

        binding.btnVolverForm.setOnClickListener { finish() }
        binding.btnGuardarProducto.setOnClickListener { guardar() }

        cargarCatalogo()
    }

    // ── Cargar categorías y marcas para los spinners ──────────────────────
    private fun cargarCatalogo() {
        binding.progressForm.visibility = View.VISIBLE
        binding.btnGuardarProducto.isEnabled = false

        repo.obtenerCatalogo(
            onSuccess = { cats, mars ->
                categorias = cats
                marcas     = mars

                binding.spinnerCategoria.adapter = ArrayAdapter(
                    this, android.R.layout.simple_spinner_item, cats
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                binding.spinnerMarca.adapter = ArrayAdapter(
                    this, android.R.layout.simple_spinner_item, mars
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                // Si es edición, preseleccionar categoría y marca
                productoEditar?.let { p ->
                    val catIdx = cats.indexOfFirst { it.id == p.idCategoria }
                    val marIdx = mars.indexOfFirst { it.id == p.idMarca }
                    if (catIdx >= 0) binding.spinnerCategoria.setSelection(catIdx)
                    if (marIdx >= 0) binding.spinnerMarca.setSelection(marIdx)
                }

                binding.progressForm.visibility = View.GONE
                binding.btnGuardarProducto.isEnabled = true
            },
            onError = { msg ->
                binding.progressForm.visibility = View.GONE
                binding.btnGuardarProducto.isEnabled = true
                Snackbar.make(binding.root, "⚠ No se pudo cargar catálogo: $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    // ── Pre-rellenar campos en modo edición ───────────────────────────────
    private fun rellenarCampos(p: Producto) {
        binding.etNombreProducto.setText(p.nombre)
        binding.etUnidad.setText(p.unidad)
        binding.etDescripcion.setText(p.descripcion)
        binding.etStock.setText(p.stock.toString())
        binding.etPrecioCosto.setText(p.precioCosto.toString())
        binding.etPrecioVenta.setText(p.precioVenta.toString())
        binding.etImagen.setText(p.imagen ?: "")
    }

    // ── Guardar (agregar o editar) ────────────────────────────────────────
    private fun guardar() {
        val nombre      = binding.etNombreProducto.text.toString().trim()
        val unidad      = binding.etUnidad.text.toString().trim().ifEmpty { "UND" }
        val descripcion = binding.etDescripcion.text.toString().trim()
        val stockStr    = binding.etStock.text.toString().trim()
        val costoStr    = binding.etPrecioCosto.text.toString().trim()
        val ventaStr    = binding.etPrecioVenta.text.toString().trim()
        val imagen      = binding.etImagen.text.toString().trim()

        // Validación básica
        if (nombre.isEmpty()) {
            Snackbar.make(binding.root, "El nombre es obligatorio", Snackbar.LENGTH_SHORT).show()
            binding.etNombreProducto.requestFocus(); return
        }
        if (ventaStr.isEmpty()) {
            Snackbar.make(binding.root, "El precio de venta es obligatorio", Snackbar.LENGTH_SHORT).show()
            binding.etPrecioVenta.requestFocus(); return
        }
        if (categorias.isEmpty() || marcas.isEmpty()) {
            Snackbar.make(binding.root, "Espera a que carguen las categorías y marcas", Snackbar.LENGTH_SHORT).show()
            return
        }

        val stock       = stockStr.toIntOrNull() ?: 0
        val precioCosto = costoStr.toDoubleOrNull() ?: 0.0
        val precioVenta = ventaStr.toDoubleOrNull() ?: 0.0

        val catSeleccionada = categorias[binding.spinnerCategoria.selectedItemPosition]
        val marSeleccionada = marcas[binding.spinnerMarca.selectedItemPosition]

        setLoading(true)

        if (productoEditar == null) {
            // ── AGREGAR ──────────────────────────────────────────────────
            val idStr = binding.etIdProducto.text.toString().trim()
            repo.agregarProducto(
                idProducto  = idStr,
                nombre      = nombre,
                unidad      = unidad,
                descripcion = descripcion,
                stock       = stock,
                precioCosto = precioCosto,
                precioVenta = precioVenta,
                imagen      = imagen,
                idCategoria = catSeleccionada.id,
                idMarca     = marSeleccionada.id,
                onSuccess   = { msg ->
                    setLoading(false)
                    Snackbar.make(binding.root, "✓ $msg", Snackbar.LENGTH_SHORT).show()
                    finish()
                },
                onError     = { msg ->
                    setLoading(false)
                    Snackbar.make(binding.root, "❌ $msg", Snackbar.LENGTH_LONG).show()
                }
            )
        } else {
            // ── EDITAR ───────────────────────────────────────────────────
            repo.editarProducto(
                producto    = productoEditar!!,
                nombre      = nombre,
                unidad      = unidad,
                descripcion = descripcion,
                stock       = stock,
                precioCosto = precioCosto,
                precioVenta = precioVenta,
                imagen      = imagen,
                idCategoria = catSeleccionada.id,
                idMarca     = marSeleccionada.id,
                onSuccess   = { msg ->
                    setLoading(false)
                    Snackbar.make(binding.root, "✓ $msg", Snackbar.LENGTH_SHORT).show()
                    finish()
                },
                onError     = { msg ->
                    setLoading(false)
                    Snackbar.make(binding.root, "❌ $msg", Snackbar.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnGuardarProducto.isEnabled = !loading
        binding.progressForm.visibility = if (loading) View.VISIBLE else View.GONE
    }
}

