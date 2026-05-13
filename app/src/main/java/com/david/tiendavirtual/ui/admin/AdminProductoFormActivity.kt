package com.david.tiendavirtual.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
        private val UNIDADES = listOf("UND", "PAR", "KIT")
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

        configurarDropdownUnidad()
        configurarFiltrosPrecios()

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
        val unidadValida = if (UNIDADES.contains(p.unidad)) p.unidad else "UND"
        (binding.etUnidad as AutoCompleteTextView).setText(unidadValida, false)
        binding.etDescripcion.setText(p.descripcion)
        binding.etStock.setText(p.stock.toString())
        binding.etPrecioCosto.setText(p.precioCosto.toString())
        binding.etPrecioVenta.setText(p.precioVenta.toString())
        binding.etImagen.setText(p.imagen ?: "")
    }

    // ── Configura el dropdown de unidad ──────────────────────────────────
    private fun configurarDropdownUnidad() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, UNIDADES)
        (binding.etUnidad as AutoCompleteTextView).apply {
            setAdapter(adapter)
            setOnClickListener { showDropDown() }
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }
        }
    }

    // ── Filtros de formato para precios: máx 7 enteros + 2 decimales ─────
    private fun configurarFiltrosPrecios() {
        listOf(binding.etPrecioCosto, binding.etPrecioVenta).forEach { et ->
            et.addTextChangedListener(object : TextWatcher {
                private var editando = false
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (editando || s == null) return
                    val txt = s.toString()
                    val punto = txt.indexOf('.')
                    var corregido = txt
                    if (punto >= 0) {
                        val enteros  = txt.substring(0, punto)
                        val decimales = txt.substring(punto + 1)
                        corregido = enteros.take(7) + "." + decimales.take(2)
                    } else {
                        corregido = txt.take(7)
                    }
                    if (corregido != txt) {
                        editando = true
                        et.setText(corregido)
                        et.setSelection(corregido.length)
                        editando = false
                    }
                }
            })
        }
    }

    // ── Limpiar errores inline ────────────────────────────────────────────
    private fun limpiarErrores() {
        binding.tilNombreProducto.error = null
        binding.tilUnidad.error         = null
        binding.tilStock.error          = null
        binding.tilPrecioCosto.error    = null
        binding.tilPrecioVenta.error    = null
    }

    // ── Guardar (agregar o editar) ────────────────────────────────────────
    private fun guardar() {
        limpiarErrores()

        val nombre      = binding.etNombreProducto.text.toString().trim()
        val unidad      = (binding.etUnidad as AutoCompleteTextView).text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val stockStr    = binding.etStock.text.toString().trim()
        val costoStr    = binding.etPrecioCosto.text.toString().trim()
        val ventaStr    = binding.etPrecioVenta.text.toString().trim()
        val imagen      = binding.etImagen.text.toString().trim()

        var hayError = false

        // Nombre: obligatorio, máx 40 caracteres
        if (nombre.isEmpty()) {
            binding.tilNombreProducto.error = "El nombre es obligatorio"
            hayError = true
        } else if (nombre.length > 40) {
            binding.tilNombreProducto.error = "Máximo 40 caracteres"
            hayError = true
        }

        // Unidad: debe ser UND, PAR o KIT
        if (!UNIDADES.contains(unidad)) {
            binding.tilUnidad.error = "Selecciona UND, PAR o KIT"
            hayError = true
        }

        // Stock: obligatorio, entero entre 0 y 999
        val stock = stockStr.toIntOrNull()
        if (stockStr.isEmpty()) {
            binding.tilStock.error = "El stock es obligatorio"
            hayError = true
        } else if (stock == null || stock < 0) {
            binding.tilStock.error = "Debe ser un número entero ≥ 0"
            hayError = true
        }

        // Precio costo: opcional, pero si se pone debe ser ≥ 0
        val precioCosto = costoStr.toDoubleOrNull()
        if (costoStr.isNotEmpty() && (precioCosto == null || precioCosto < 0)) {
            binding.tilPrecioCosto.error = "Debe ser un valor ≥ 0"
            hayError = true
        }

        // Precio venta: obligatorio, > 0
        val precioVenta = ventaStr.toDoubleOrNull()
        if (ventaStr.isEmpty()) {
            binding.tilPrecioVenta.error = "El precio de venta es obligatorio"
            hayError = true
        } else if (precioVenta == null || precioVenta <= 0) {
            binding.tilPrecioVenta.error = "Debe ser mayor a 0"
            hayError = true
        }

        // Costo no puede superar al precio de venta
        if (!hayError && precioCosto != null && precioVenta != null && precioCosto > precioVenta) {
            binding.tilPrecioCosto.error = "El costo no puede superar el precio de venta"
            hayError = true
        }

        if (categorias.isEmpty() || marcas.isEmpty()) {
            Snackbar.make(binding.root, "Espera a que carguen categorías y marcas", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (hayError) return

        val catSeleccionada = categorias[binding.spinnerCategoria.selectedItemPosition]
        val marSeleccionada = marcas[binding.spinnerMarca.selectedItemPosition]

        setLoading(true)

        if (productoEditar == null) {
            val idStr = binding.etIdProducto.text.toString().trim()
            repo.agregarProducto(
                idProducto  = idStr,
                nombre      = nombre,
                unidad      = unidad,
                descripcion = descripcion,
                stock       = stock!!,
                precioCosto = precioCosto ?: 0.0,
                precioVenta = precioVenta!!,
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
            repo.editarProducto(
                producto    = productoEditar!!,
                nombre      = nombre,
                unidad      = unidad,
                descripcion = descripcion,
                stock       = stock!!,
                precioCosto = precioCosto ?: 0.0,
                precioVenta = precioVenta!!,
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

