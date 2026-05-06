package com.david.tiendavirtual.ui.pedidos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.david.tiendavirtual.data.repository.FacturaRepository
import com.david.tiendavirtual.databinding.ActivityOrderHistoryBinding
import com.david.tiendavirtual.ui.home.MainActivity
import com.david.tiendavirtual.utils.LocalOrderStore
import com.google.android.material.snackbar.Snackbar

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var adapter: PedidoAdapter
    private lateinit var repo:    FacturaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo    = FacturaRepository(this)
        adapter = PedidoAdapter(emptyList()) { pedido ->
            // Evitar abrir duplicados si el usuario toca muy rápido
            if (supportFragmentManager.findFragmentByTag("factura_dialog") == null) {
                FacturaDialogFragment.newInstance(pedido)
                    .show(supportFragmentManager, "factura_dialog")
            }
        }

        binding.recyclerPedidos.layoutManager = LinearLayoutManager(this)
        binding.recyclerPedidos.adapter = adapter

        binding.btnVolverPedidos.setOnClickListener { irAMain() }

        binding.btnIrAComprar.setOnClickListener { irAMain() }

        // Botón atrás del sistema → también va al Home, no al carrito
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { irAMain() }
        })
    }

    override fun onResume() {
        super.onResume()
        cargarPedidos()
    }

    // ── Carga pedidos desde la BD a través del backend PHP ────────────────
    private fun cargarPedidos() {
        mostrarCargando(true)

        repo.obtenerPedidos(
            onSuccess = { pedidos ->
                mostrarCargando(false)

                // Filtrar solo los pedidos realizados en ESTE dispositivo
                val misIds      = LocalOrderStore.obtenerIds(this)
                val misPedidos  = if (misIds.isEmpty()) emptyList()
                                  else pedidos.filter { it.idFactura in misIds }

                adapter.actualizarLista(misPedidos)

                if (misPedidos.isEmpty()) {
                    binding.layoutVacio.visibility     = View.VISIBLE
                    binding.recyclerPedidos.visibility = View.GONE
                } else {
                    binding.layoutVacio.visibility     = View.GONE
                    binding.recyclerPedidos.visibility = View.VISIBLE
                }
            },
            onError   = { msg ->
                mostrarCargando(false)
                binding.layoutVacio.visibility     = View.VISIBLE
                binding.recyclerPedidos.visibility = View.GONE
                Snackbar.make(binding.root, "⚠ $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun mostrarCargando(loading: Boolean) {
        binding.progressPedidos.visibility =
            if (loading) View.VISIBLE else View.GONE
        if (loading) {
            binding.layoutVacio.visibility     = View.GONE
            binding.recyclerPedidos.visibility = View.GONE
        }
    }

    /** Navega al Home limpiando el back stack para no volver al carrito ni a la confirmación */
    private fun irAMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )
        finish()
    }
}
