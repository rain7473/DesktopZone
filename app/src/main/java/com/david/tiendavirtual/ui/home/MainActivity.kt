package com.david.tiendavirtual.ui.home

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.david.tiendavirtual.data.model.Categoria
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.repository.AdminProductoRepository
import com.david.tiendavirtual.data.repository.ProductoRepository
import com.david.tiendavirtual.databinding.ActivityMainBinding
import com.david.tiendavirtual.ui.carrito.CarritoActivity
import com.david.tiendavirtual.ui.favoritos.FavoritosActivity
import com.david.tiendavirtual.ui.login.LoginActivity
import com.david.tiendavirtual.ui.perfil.ProfileActivity
import com.david.tiendavirtual.ui.productos.CarritoManager
import com.david.tiendavirtual.ui.productos.ProductoAdapter
import com.david.tiendavirtual.ui.productos.ProductoDetalleActivity
import com.david.tiendavirtual.ui.search.SearchActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductoAdapter
    private lateinit var categoriaAdapter: CategoriaHomeAdapter
    private var listaCompleta: List<Producto> = emptyList()
    private var categoriaFiltro: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── Saludo personalizado ─────────────────────────────────────────
        val nombre = if (UserSession.isGuest) "invitado" else UserSession.nombreCompleto
        binding.tvHolaNombre.text = "Hola, $nombre 👋"

        // ── Adapter productos ────────────────────────────────────────────
        adapter = ProductoAdapter(
            productos = emptyList(),
            onAgregarClick = { _ ->
                actualizarBadgeCarrito()
            },
            onItemClick = { producto ->
                val intent = Intent(this, ProductoDetalleActivity::class.java)
                intent.putExtra("producto", producto)
                startActivity(intent)
            },
            onCantidadCambiada = {
                actualizarBadgeCarrito()
                actualizarBarraCarrito()
            }
        )
        binding.recyclerProductos.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerProductos.adapter = adapter

        // ── Adapter categorías ────────────────────────────────────────────
        categoriaAdapter = CategoriaHomeAdapter(emptyList()) { cat ->
            categoriaFiltro = cat?.nombre
            filtrarProductos(binding.etBuscar.text.toString())
        }
        binding.recyclerCategorias.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerCategorias.adapter = categoriaAdapter

        // ── Búsqueda en tiempo real ───────────────────────────────────────
        binding.etBuscar.addTextChangedListener { text ->
            filtrarProductos(text.toString())
        }

        // ── Avatar header ─────────────────────────────────────────────
        binding.btnHeaderAvatarCliente.setOnClickListener {
            if (UserSession.isGuest) {
                Toast.makeText(this, "Inicia sesión para ver tu perfil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }

        // ── Barra flotante carrito ───────────────────────────────────────
        binding.barraCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        // ── Bottom nav ───────────────────────────────────────────────────
        binding.navInicio.setOnClickListener {
            // Recargar datos y volver al tope
            binding.etBuscar.text?.clear()
            categoriaFiltro = null
            categoriaAdapter.seleccionarTodas()
            cargarCategorias()
            cargarProductos()
            findNestedScrollView()?.smoothScrollTo(0, 0)
        }
        binding.navCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }
        binding.navPerfil.setOnClickListener {
            if (UserSession.isGuest) {
                Toast.makeText(this, "Inicia sesión para ver tu perfil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }
        binding.navBuscar.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        binding.navFavoritos.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }

        // ── Cargar datos ─────────────────────────────────────────────────
        cargarCategorias()
        cargarProductos()

        // ── Empieza ahora → scroll a productos ───────────────────────────
        binding.btnArmaTuPc.setOnClickListener {
            binding.recyclerProductos.post {
                val scrollView = findNestedScrollView()
                scrollView?.smoothScrollTo(0, binding.recyclerProductos.top)
            }
        }

        // ── Ver todo (categorías) → quitar filtro + scroll a productos ───
        binding.tvVerTodoCats.setOnClickListener {
            categoriaFiltro = null
            categoriaAdapter.seleccionarTodas()
            filtrarProductos(binding.etBuscar.text.toString())
            binding.recyclerProductos.post {
                val scrollView = findNestedScrollView()
                scrollView?.smoothScrollTo(0, binding.recyclerProductos.top)
            }
        }
    }

    private fun findNestedScrollView(): androidx.core.widget.NestedScrollView? {
        var v: android.view.View? = binding.recyclerProductos.parent as? android.view.View
        while (v != null) {
            if (v is androidx.core.widget.NestedScrollView) return v
            v = v.parent as? android.view.View
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        actualizarBadgeCarrito()
        actualizarBarraCarrito()
    }

    private fun actualizarBadgeCarrito() {
        val total = CarritoManager.totalItems()
        if (total > 0) {
            binding.badgeCarrito.visibility = android.view.View.VISIBLE
            binding.badgeCarrito.text = if (total > 99) "99+" else total.toString()
        } else {
            binding.badgeCarrito.visibility = android.view.View.GONE
        }
    }

    private val barraHandler = Handler(Looper.getMainLooper())
    private var barraRunnable: Runnable? = null

    private fun actualizarBarraCarrito() {
        val items = CarritoManager.totalItems()
        val total = CarritoManager.calcularTotal()

        if (items > 0) {
            binding.tvBarraCantidad.text = items.toString()
            binding.tvBarraTotal.text    = "B/. %.2f".format(total)

            if (binding.barraCarrito.visibility != android.view.View.VISIBLE) {
                binding.barraCarrito.visibility  = android.view.View.VISIBLE
                binding.barraCarrito.translationY = 120f
                binding.barraCarrito.animate()
                    .translationY(0f)
                    .setDuration(300)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }

            // Reiniciar temporizador de 5 s cada vez que cambia el carrito
            barraRunnable?.let { barraHandler.removeCallbacks(it) }
            barraRunnable = Runnable { ocultarBarraCarrito() }
            barraHandler.postDelayed(barraRunnable!!, 5000L)

        } else {
            ocultarBarraCarrito()
        }
    }

    private fun ocultarBarraCarrito() {
        barraRunnable?.let { barraHandler.removeCallbacks(it) }
        barraRunnable = null
        if (binding.barraCarrito.visibility == android.view.View.VISIBLE) {
            binding.barraCarrito.animate()
                .translationY(120f)
                .setDuration(250)
                .withEndAction {
                    binding.barraCarrito.visibility = android.view.View.GONE
                }.start()
        }
    }

    private fun cargarCategorias() {
        AdminProductoRepository(this).obtenerCatalogo(
            onSuccess = { categorias, _ ->
                categoriaAdapter.actualizar(categorias)
            },
            onError = { /* silencioso */ }
        )
    }

    private fun cargarProductos() {
        ProductoRepository(this).obtenerProductos(
            onSuccess = { productos ->
                listaCompleta = productos
                filtrarProductos(binding.etBuscar.text.toString())
            },
            onError = { error ->
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun filtrarProductos(texto: String) {
        val q = texto.trim().lowercase()
        var resultado = listaCompleta

        if (categoriaFiltro != null) {
            resultado = resultado.filter {
                (it.categoria ?: "").equals(categoriaFiltro, ignoreCase = true)
            }
        }
        if (q.isNotEmpty()) {
            resultado = resultado.filter {
                it.nombre.lowercase().contains(q) ||
                (it.categoria ?: "").lowercase().contains(q) ||
                (it.marca ?: "").lowercase().contains(q)
            }
        }

        adapter.actualizarLista(resultado)
        binding.tvContadorProductos.text = "${resultado.size} productos"
    }
}
