package com.david.tiendavirtual.ui.carrito

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.david.tiendavirtual.databinding.ActivityCarritoBinding
import com.david.tiendavirtual.ui.pago.PagoActivity
import com.david.tiendavirtual.ui.productos.CarritoAdapter
import com.david.tiendavirtual.ui.productos.CarritoManager
import com.google.android.material.snackbar.Snackbar

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var adapter: CarritoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CarritoAdapter(
            onCambio = { actualizarUI() },
            onEliminar = { item ->
                Snackbar.make(binding.root, "${item.producto.nombre} eliminado", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer") {
                        CarritoManager.agregarProducto(item.producto, item.cantidad)
                        adapter.actualizarLista(CarritoManager.obtenerItems())
                        actualizarUI()
                    }
                    .setActionTextColor(getColor(com.david.tiendavirtual.R.color.green_primary))
                    .show()
            }
        )
        binding.recyclerCarrito.layoutManager = LinearLayoutManager(this)
        binding.recyclerCarrito.adapter = adapter

        binding.btnVolver.setOnClickListener { finish() }

        binding.btnPagar.setOnClickListener {
            if (CarritoManager.obtenerItems().isEmpty()) {
                Snackbar.make(binding.root, "Tu carrito está vacío", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, PagoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.actualizarLista(CarritoManager.obtenerItems())
        actualizarUI()
    }

    private fun actualizarUI() {
        val items    = CarritoManager.obtenerItems()
        val subtotal = CarritoManager.calcularSubtotal()
        val itbms    = CarritoManager.calcularItbms()
        val total    = CarritoManager.calcularTotal()

        binding.tvSubtotal.text = "B/. %.2f".format(subtotal)
        binding.tvItbms.text    = "B/. %.2f".format(itbms)
        binding.tvTotal.text    = "B/. %.2f".format(total)

        if (items.isEmpty()) {
            binding.layoutCarritoVacio.visibility = View.VISIBLE
            binding.recyclerCarrito.visibility    = View.GONE
            binding.cardResumen.visibility        = View.GONE
        } else {
            binding.layoutCarritoVacio.visibility = View.GONE
            binding.recyclerCarrito.visibility    = View.VISIBLE
            binding.cardResumen.visibility        = View.VISIBLE
        }
    }
}
