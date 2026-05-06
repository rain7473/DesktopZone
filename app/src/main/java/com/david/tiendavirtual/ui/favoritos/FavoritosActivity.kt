package com.david.tiendavirtual.ui.favoritos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.david.tiendavirtual.databinding.ActivityFavoritosBinding
import com.david.tiendavirtual.ui.home.MainActivity
import com.david.tiendavirtual.ui.productos.CarritoManager
import com.david.tiendavirtual.ui.productos.FavoritosManager
import com.david.tiendavirtual.ui.productos.ProductoAdapter
import com.david.tiendavirtual.ui.productos.ProductoDetalleActivity
import com.google.android.material.snackbar.Snackbar

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private lateinit var adapter: ProductoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVolverFavoritos.setOnClickListener { finish() }

        adapter = ProductoAdapter(
            emptyList(),
            onAgregarClick = { producto ->
                CarritoManager.agregarProducto(producto)
                Snackbar.make(binding.root, "✓  ${producto.nombre} agregado", Snackbar.LENGTH_SHORT).show()
            },
            onFavoritoClick = { _ ->
                cargarFavoritos()
            },
            onItemClick = { producto ->
                val intent = Intent(this, ProductoDetalleActivity::class.java)
                intent.putExtra("producto", producto)
                startActivity(intent)
            }
        )
        binding.recyclerFavoritos.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerFavoritos.adapter = adapter

        binding.btnIrAComprarFav.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        cargarFavoritos()
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }

    private fun cargarFavoritos() {
        val lista = FavoritosManager.obtenerFavoritos()
        adapter.actualizarLista(lista)
        binding.tvTotalFavoritos.text = "${lista.size} producto${if (lista.size != 1) "s" else ""}"

        if (lista.isEmpty()) {
            binding.layoutVacioFav.visibility   = View.VISIBLE
            binding.recyclerFavoritos.visibility = View.GONE
            binding.tvTotalFavoritos.visibility  = View.GONE
        } else {
            binding.layoutVacioFav.visibility   = View.GONE
            binding.recyclerFavoritos.visibility = View.VISIBLE
            binding.tvTotalFavoritos.visibility  = View.VISIBLE
        }
    }
}





