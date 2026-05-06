package com.david.tiendavirtual.ui.productos

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.databinding.ActivityProductoDetalleBinding
import com.google.android.material.chip.Chip
import com.david.tiendavirtual.ui.carrito.CarritoActivity
import com.google.android.material.snackbar.Snackbar

class ProductoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductoDetalleBinding
    private lateinit var producto: Producto
    private var cantidadSeleccionada = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        producto = intent.getSerializableExtra("producto") as Producto

        setupUI()
    }

    private fun setupUI() {

        // ── Botón volver ─────────────────────────────────────────────────
        binding.btnVolverDetalle.setOnClickListener { finish() }

        // ── Imagen del producto con Glide ─────────────────────────────────
        val imgUrl = producto.imagen
        if (!imgUrl.isNullOrEmpty()) {
            binding.ivDetalleImagen.clearColorFilter()
            binding.ivDetalleImagen.alpha = 1f
            Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.ic_cart)
                .error(R.drawable.ic_cart)
                .centerCrop()
                .into(binding.ivDetalleImagen)
        } else {
            binding.ivDetalleImagen.setImageResource(R.drawable.ic_cart)
            binding.ivDetalleImagen.setColorFilter(
                ContextCompat.getColor(this, R.color.green_primary)
            )
            binding.ivDetalleImagen.alpha = 0.35f
        }

        // ── Nombre ───────────────────────────────────────────────────────
        binding.tvDetalleNombre.text = producto.nombre

        // ── Categoría · Marca ────────────────────────────────────────────
        val catMarca = listOfNotNull(
            producto.categoria?.takeIf { it.isNotEmpty() },
            producto.marca?.takeIf { it.isNotEmpty() }
        ).joinToString(" · ")
        binding.tvDetalleCategoria.text = catMarca


        // ── Rating interactivo ───────────────────────────────────────────
        binding.ratingBarDetalle.onRatingBarChangeListener =
            android.widget.RatingBar.OnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser && rating > 0) {
                    binding.tvSinResenas.text = "%.1f / 5".format(rating)
                    binding.resenaFormSection.visibility = View.VISIBLE
                } else if (rating == 0f) {
                    binding.tvSinResenas.text = "Sin reseñas aún"
                    binding.resenaFormSection.visibility = View.GONE
                }
            }

        binding.btnEnviarResena.setOnClickListener {
            val comentario = binding.etResena.text?.toString()?.trim() ?: ""
            val estrellas  = binding.ratingBarDetalle.rating
            val msg = if (comentario.isNotEmpty())
                "¡Gracias por tu reseña de ${"%.1f".format(estrellas)} ★!"
            else
                "¡Gracias por tu valoración de ${"%.1f".format(estrellas)} ★!"
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
            binding.etResena.text?.clear()
            binding.resenaFormSection.visibility = View.GONE
        }

        // ── Descripción ──────────────────────────────────────────────────
        binding.tvDetalleDescripcion.text = producto.descripcion

        // ── Precio ───────────────────────────────────────────────────────
        binding.tvDetallePrecio.text = "B/. %.2f".format(producto.precioVenta)

        // ── Stock ────────────────────────────────────────────────────────
        if (producto.stock > 0) {
            binding.tvDetalleStock.text = "${producto.stock} en stock"
        } else {
            binding.tvDetalleStock.text = "Agotado"
            binding.tvDetalleStock.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }


        // ── Variantes ────────────────────────────────────────────────────
        val variantes = extraerVariantes(producto.nombre)
        if (variantes.isNotEmpty()) {
            val colorVerde  = ContextCompat.getColor(this, R.color.green_primary)
            val colorBlanco = ContextCompat.getColor(this, R.color.white)
            val colorTexto  = ContextCompat.getColor(this, R.color.text_primary)

            val bgStates = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
            val bgColors = intArrayOf(colorVerde, colorBlanco)
            val bgList   = ColorStateList(bgStates, bgColors)

            val txtStates = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
            val txtColors = intArrayOf(colorBlanco, colorTexto)
            val txtList   = ColorStateList(txtStates, txtColors)

            variantes.forEachIndexed { index, spec ->
                val chip = Chip(this).apply {
                    text                = spec
                    isCheckable         = true
                    isChecked           = (index == 0)
                    chipBackgroundColor = bgList
                    setTextColor(txtList)
                    chipStrokeWidth     = 1.5f
                    setChipStrokeColorResource(R.color.green_primary)
                }
                binding.chipGroupVariantes.addView(chip)
            }
            binding.variantesSection.visibility = View.VISIBLE
        } else {
            binding.variantesSection.visibility = View.GONE
        }

        // ── Favorito ─────────────────────────────────────────────────────
        actualizarFavorito()
        binding.btnFavoritoDetalle.setOnClickListener {
            FavoritosManager.alternar(producto)
            actualizarFavorito()
        }

        // ── Compartir ────────────────────────────────────────────────────
        binding.btnCompartirDetalle.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,
                    "¡Mira este producto! ${producto.nombre} — B/. %.2f".format(producto.precioVenta))
            }
            startActivity(Intent.createChooser(sendIntent, "Compartir producto"))
        }

        // ── Selector de cantidad ─────────────────────────────────────────
        actualizarSelectorCantidad()
        binding.btnRestarCantidad.setOnClickListener {
            if (cantidadSeleccionada > 1) {
                cantidadSeleccionada--
                actualizarSelectorCantidad()
            }
        }
        binding.btnSumarCantidad.setOnClickListener {
            if (cantidadSeleccionada < producto.stock) {
                cantidadSeleccionada++
                actualizarSelectorCantidad()
            } else {
            android.widget.Toast.makeText(this, "No hay más stock disponible", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // ── Agregar al carrito ────────────────────────────────────────────
        binding.btnAgregarCarritoDetalle.setOnClickListener {
            if (producto.stock <= 0) {
                android.widget.Toast.makeText(this, "Producto sin stock disponible", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CarritoManager.agregarProducto(producto, cantidadSeleccionada)

            // Micro-animación feedback y luego ir al carrito
            binding.btnAgregarCarritoDetalle.animate()
                .scaleX(0.95f).scaleY(0.95f).setDuration(80)
                .withEndAction {
                    binding.btnAgregarCarritoDetalle.animate()
                        .scaleX(1f).scaleY(1f).setDuration(120)
                        .withEndAction {
                            startActivity(Intent(this, CarritoActivity::class.java))
                        }.start()
                }.start()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun actualizarFavorito() {
        val esFav = FavoritosManager.esFavorito(producto.idProducto)
        binding.ivFavoritoDetalle.setColorFilter(
            ContextCompat.getColor(
                this,
                if (esFav) R.color.green_primary else R.color.text_secondary
            )
        )
    }

    /** Actualiza el texto del selector de cantidad y el estado del botón - */
    private fun actualizarSelectorCantidad() {
        binding.tvCantidadDetalle.text = cantidadSeleccionada.toString()
        binding.btnRestarCantidad.alpha = if (cantidadSeleccionada <= 1) 0.4f else 1f
        binding.btnSumarCantidad.alpha  = if (cantidadSeleccionada >= producto.stock) 0.4f else 1f
    }

    /** Extrae specs clave del nombre del producto (GB, TB, W, MHz, DDR4/5). */
    private fun extraerVariantes(nombre: String): List<String> {
        val patterns = listOf(
            Regex("\\d+(?:\\.\\d+)?\\s?TB"),
            Regex("\\d+(?:\\.\\d+)?\\s?GB"),
            Regex("\\d+\\s?W(?=\\b)"),
            Regex("DDR[45]"),
            Regex("\\d+\\s?MHz"),
        )
        return patterns.mapNotNull { it.find(nombre)?.value?.trim() }
    }
}




