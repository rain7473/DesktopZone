package com.david.tiendavirtual.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.repository.ProductoRepository
import com.david.tiendavirtual.databinding.ActivitySearchBinding
import com.david.tiendavirtual.ui.productos.CarritoManager
import com.david.tiendavirtual.ui.productos.ProductoAdapter
import com.david.tiendavirtual.ui.productos.ProductoDetalleActivity
import com.google.android.material.snackbar.Snackbar

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: ProductoAdapter

    private var listaCompleta: List<Producto> = emptyList()
    private var categoriasDisponibles: List<String> = emptyList()
    private var filtroActual: FiltroState = FiltroState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvAvatarSearch.text = UserSession.inicial.toString()
        binding.btnVolverSearch.setOnClickListener { finish() }

        adapter = ProductoAdapter(
            productos = emptyList(),
            onAgregarClick = { producto ->
                CarritoManager.agregarProducto(producto)
                Snackbar.make(binding.root, "✓  ${producto.nombre} agregado", Snackbar.LENGTH_SHORT).show()
            },
            onItemClick = { producto ->
                startActivity(
                    Intent(this, ProductoDetalleActivity::class.java)
                        .putExtra("producto", producto)
                )
            }
        )
        binding.recyclerSearch.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerSearch.adapter = adapter

        binding.etBuscarSearch.addTextChangedListener { text ->
            aplicarFiltros(texto = text.toString())
        }

        binding.btnFiltroSearch.setOnClickListener {
            SearchFilterSheet(filtroActual, categoriasDisponibles) { nuevoFiltro ->
                filtroActual = nuevoFiltro
                aplicarFiltros(binding.etBuscarSearch.text.toString())
                actualizarBadgeFiltro()
            }.show(supportFragmentManager, "filtros")
        }

        cargarProductos()
    }

    private fun cargarProductos() {
        binding.progressSearch.visibility = View.VISIBLE
        binding.recyclerSearch.visibility = View.GONE
        binding.layoutSearchVacio.visibility = View.GONE

        ProductoRepository(this).obtenerProductos(
            onSuccess = { productos ->
                binding.progressSearch.visibility = View.GONE
                listaCompleta = productos
                categoriasDisponibles = productos
                    .mapNotNull { it.categoria?.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()
                aplicarFiltros(binding.etBuscarSearch.text.toString())
            },
            onError = { error ->
                binding.progressSearch.visibility = View.GONE
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun aplicarFiltros(texto: String = "") {
        var resultado = listaCompleta.toMutableList()

        // 1. Texto de búsqueda
        val textoBusqueda = texto.trim().lowercase()
        if (textoBusqueda.isNotEmpty()) {
            resultado = resultado.filter {
                it.nombre.lowercase().contains(textoBusqueda) ||
                (it.categoria ?: "").lowercase().contains(textoBusqueda) ||
                (it.marca ?: "").lowercase().contains(textoBusqueda)
            }.toMutableList()
        }

        // 2. Filtro por nombre (sheet)
        if (filtroActual.nombre.isNotEmpty()) {
            resultado = resultado.filter {
                it.nombre.lowercase().contains(filtroActual.nombre.lowercase())
            }.toMutableList()
        }

        // 3. Filtro por marca (sheet)
        if (filtroActual.marca.isNotEmpty()) {
            resultado = resultado.filter {
                (it.marca ?: "").lowercase().contains(filtroActual.marca.lowercase())
            }.toMutableList()
        }

        // 4. Filtro por categoría
        if (filtroActual.categoria.isNotEmpty()) {
            resultado = resultado.filter {
                (it.categoria ?: "").equals(filtroActual.categoria, ignoreCase = true)
            }.toMutableList()
        }

        // 5. Rango de precio
        resultado = when (filtroActual.priceRange) {
            "0-25"   -> resultado.filter { it.precioVenta in 0.0..25.0 }.toMutableList()
            "25-50"  -> resultado.filter { it.precioVenta in 25.0..50.0 }.toMutableList()
            "50-100" -> resultado.filter { it.precioVenta in 50.0..100.0 }.toMutableList()
            "100+"   -> resultado.filter { it.precioVenta > 100.0 }.toMutableList()
            else     -> resultado
        }

        // 6. Ordenar
        resultado = when (filtroActual.sortType) {
            "precio_asc"  -> resultado.sortedBy { it.precioVenta }.toMutableList()
            "precio_desc" -> resultado.sortedByDescending { it.precioVenta }.toMutableList()
            else          -> resultado
        }

        adapter.actualizarLista(resultado)
        binding.tvResultadosCount.text =
            "${resultado.size} resultado${if (resultado.size != 1) "s" else ""}"

        if (resultado.isEmpty()) {
            binding.layoutSearchVacio.visibility = View.VISIBLE
            binding.recyclerSearch.visibility    = View.GONE
        } else {
            binding.layoutSearchVacio.visibility = View.GONE
            binding.recyclerSearch.visibility    = View.VISIBLE
        }
    }

    private fun actualizarBadgeFiltro() {
        val hayFiltros = filtroActual != FiltroState()
        binding.btnFiltroSearch.setCardBackgroundColor(
            getColor(if (hayFiltros) R.color.green_dark else R.color.green_primary)
        )
    }
}
