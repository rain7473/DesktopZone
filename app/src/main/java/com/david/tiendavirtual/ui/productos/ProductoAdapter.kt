package com.david.tiendavirtual.ui.productos

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.databinding.ItemProductoBinding

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onAgregarClick: (Producto) -> Unit,
    private val onFavoritoClick: ((Producto) -> Unit)? = null,
    private val onItemClick: ((Producto) -> Unit)? = null,
    private val onCantidadCambiada: (() -> Unit)? = null
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    private val skeletonColor = ColorDrawable(Color.parseColor("#F0F0F0"))

    /** Handler compartido para todos los temporizadores del adapter */
    private val handler = Handler(Looper.getMainLooper())

    inner class ProductoViewHolder(private val binding: ItemProductoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var ocultarRunnable: Runnable? = null

        @SuppressLint("ClickableViewAccessibility")
        fun bind(producto: Producto) {
            binding.tvNombre.text = producto.nombre
            binding.tvPrecio.text = "B/. %.2f".format(producto.precioVenta)

            val cat = producto.categoria?.trim()
            if (!cat.isNullOrEmpty()) {
                binding.tvCategoria.text       = cat.uppercase()
                binding.tvCategoria.visibility = View.VISIBLE
            } else {
                binding.tvCategoria.visibility = View.GONE
            }

            if (producto.stock > 0) {
                binding.tvStock.text = "✓  ${producto.stock} en stock"
                binding.tvStock.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green_primary))
            } else {
                binding.tvStock.text = "✗  Agotado"
                binding.tvStock.setTextColor(Color.parseColor("#E53935"))
            }

            // ── Imagen ────────────────────────────────────────────────────
            binding.viewSkeleton.visibility = View.VISIBLE
            val imgUrl = producto.imagen
            if (!imgUrl.isNullOrEmpty()) {
                binding.ivProducto.clearColorFilter()
                binding.ivProducto.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                Glide.with(binding.ivProducto.context)
                    .load(imgUrl)
                    .placeholder(skeletonColor).error(skeletonColor)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .fitCenter()
                    .listener(glideListener())
                    .into(binding.ivProducto)
            } else {
                binding.ivProducto.setImageResource(R.drawable.ic_cart)
                binding.ivProducto.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.green_primary))
                binding.ivProducto.setPadding(dpToPx(28), dpToPx(28), dpToPx(28), dpToPx(28))
                binding.viewSkeleton.visibility = View.GONE
            }

            // ── Interacción con la card ───────────────────────────────────
            binding.root.setOnClickListener { onItemClick?.invoke(producto) }
            binding.root.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
                false
            }

            // ── Estado inicial (sin animación) ────────────────────────────
            val enCarrito = CarritoManager.obtenerCantidad(producto.idProducto)
            setEstadoInicial(enCarrito)

            // ── Botón + agregar ───────────────────────────────────────────
            binding.btnAgregar.setOnClickListener {
                if (producto.stock <= 0) return@setOnClickListener
                CarritoManager.agregarProducto(producto)
                onAgregarClick(producto)
                mostrarSelector(CarritoManager.obtenerCantidad(producto.idProducto))
                programarOcultamiento(producto)
                onCantidadCambiada?.invoke()
            }

            // ── Selector: + ───────────────────────────────────────────────
            binding.btnMasItem.setOnClickListener {
                val actual = CarritoManager.obtenerCantidad(producto.idProducto)
                if (actual >= producto.stock) {
                    pulsarBoton(binding.btnMasItem, alcanzado = true)
                    return@setOnClickListener
                }
                CarritoManager.incrementar(producto.idProducto)
                binding.tvCantidadItem.text = CarritoManager.obtenerCantidad(producto.idProducto).toString()
                pulsarBoton(binding.btnMasItem)
                reiniciarTemporizador(producto)
                onCantidadCambiada?.invoke()
            }

            // ── Selector: - ───────────────────────────────────────────────
            binding.btnMenosItem.setOnClickListener {
                CarritoManager.decrementar(producto.idProducto)
                val nueva = CarritoManager.obtenerCantidad(producto.idProducto)
                if (nueva == 0) {
                    cancelarTemporizador()
                    ocultarSelector()
                } else {
                    binding.tvCantidadItem.text = nueva.toString()
                    pulsarBoton(binding.btnMenosItem)
                    reiniciarTemporizador(producto)
                }
                onCantidadCambiada?.invoke()
            }

            // ── Favorito ──────────────────────────────────────────────────
            actualizarIconoFavorito(producto)
            binding.btnFavorito.setOnClickListener {
                FavoritosManager.alternar(producto)
                actualizarIconoFavorito(producto)
                binding.btnFavorito.animate().scaleX(1.35f).scaleY(1.35f).setDuration(120)
                    .withEndAction {
                        binding.btnFavorito.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80)
                            .withEndAction {
                                binding.btnFavorito.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                            }.start()
                    }.start()
                onFavoritoClick?.invoke(producto)
            }
        }

        // ── Estado sin animación (bind inicial) ───────────────────────────
        private fun setEstadoInicial(cantidad: Int) {
            cancelarTemporizador()
            if (cantidad > 0) {
                binding.tvCantidadItem.text = cantidad.toString()
                binding.btnAgregar.visibility    = View.GONE
                binding.layoutCantidad.visibility = View.VISIBLE
                binding.btnAgregar.scaleX = 1f ; binding.btnAgregar.scaleY = 1f
                binding.layoutCantidad.scaleX = 1f ; binding.layoutCantidad.scaleY = 1f
                binding.layoutCantidad.alpha = 1f
            } else {
                binding.btnAgregar.visibility    = View.VISIBLE
                binding.layoutCantidad.visibility = View.GONE
                binding.btnAgregar.scaleX = 1f ; binding.btnAgregar.scaleY = 1f
                binding.layoutCantidad.scaleX = 1f ; binding.layoutCantidad.scaleY = 1f
            }
        }

        // ── Mostrar selector con animación ────────────────────────────────
        private fun mostrarSelector(cantidad: Int) {
            binding.tvCantidadItem.text = cantidad.toString()
            binding.btnAgregar.animate()
                .scaleX(0f).scaleY(0f).alpha(0f).setDuration(160)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    binding.btnAgregar.visibility = View.GONE
                    binding.btnAgregar.alpha = 1f
                    binding.layoutCantidad.apply {
                        visibility = View.VISIBLE
                        scaleX = 0.4f ; scaleY = 0.4f ; alpha = 0f
                        animate().scaleX(1f).scaleY(1f).alpha(1f)
                            .setDuration(220)
                            .setInterpolator(OvershootInterpolator(1.5f))
                            .start()
                    }
                }.start()
        }

        // ── Ocultar selector → volver al botón + ─────────────────────────
        private fun ocultarSelector() {
            binding.layoutCantidad.animate()
                .scaleX(0f).scaleY(0f).alpha(0f).setDuration(160)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    binding.layoutCantidad.visibility = View.GONE
                    binding.layoutCantidad.alpha = 1f
                    binding.btnAgregar.apply {
                        visibility = View.VISIBLE
                        scaleX = 0.4f ; scaleY = 0.4f ; alpha = 0f
                        animate().scaleX(1f).scaleY(1f).alpha(1f)
                            .setDuration(200)
                            .setInterpolator(OvershootInterpolator(1.2f))
                            .start()
                    }
                }.start()
        }

        // ── Temporizador 2.5 s ────────────────────────────────────────────
        private fun programarOcultamiento(producto: Producto) {
            cancelarTemporizador()
            ocultarRunnable = Runnable {
                // Solo ocultar si aún hay cantidad en el carrito
                if (CarritoManager.obtenerCantidad(producto.idProducto) > 0
                    && binding.layoutCantidad.visibility == View.VISIBLE) {
                    ocultarSelector()
                }
            }
            handler.postDelayed(ocultarRunnable!!, 2500L)
        }

        private fun reiniciarTemporizador(producto: Producto) = programarOcultamiento(producto)

        private fun cancelarTemporizador() {
            ocultarRunnable?.let { handler.removeCallbacks(it) }
            ocultarRunnable = null
        }

        // ── Animación de pulso en los botones ─/+ ────────────────────────
        private fun pulsarBoton(v: View, alcanzado: Boolean = false) {
            val scaleTarget = if (alcanzado) 1.2f else 0.72f
            v.animate().scaleX(scaleTarget).scaleY(scaleTarget).setDuration(70)
                .withEndAction { v.animate().scaleX(1f).scaleY(1f).setDuration(110).start() }
                .start()
        }

        private fun glideListener() =
            object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(e: com.bumptech.glide.load.engine.GlideException?, model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                    isFirstResource: Boolean): Boolean { binding.viewSkeleton.visibility = View.GONE; return false }
                override fun onResourceReady(resource: android.graphics.drawable.Drawable, model: Any,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                    dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                    binding.viewSkeleton.visibility = View.GONE; return false }
            }

        private fun actualizarIconoFavorito(producto: Producto) {
            binding.btnFavorito.setColorFilter(ContextCompat.getColor(binding.root.context,
                if (FavoritosManager.esFavorito(producto.idProducto)) R.color.green_primary else R.color.text_secondary))
        }

        private fun dpToPx(dp: Int) = (dp * binding.root.context.resources.displayMetrics.density).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProductoViewHolder(ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) = holder.bind(productos[position])

    override fun getItemCount() = productos.size

    fun actualizarLista(nuevaLista: List<Producto>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = productos.size
            override fun getNewListSize() = nuevaLista.size
            override fun areItemsTheSame(o: Int, n: Int) = productos[o].idProducto == nuevaLista[n].idProducto
            override fun areContentsTheSame(o: Int, n: Int) = productos[o] == nuevaLista[n]
        })
        productos = nuevaLista
        diff.dispatchUpdatesTo(this)
    }
}
