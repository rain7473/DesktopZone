package com.david.tiendavirtual.ui.admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.databinding.ItemProductoAdminBinding

class AdminProductoAdapter(
    private var productos:   List<Producto>,
    private val onEditar:    (Producto) -> Unit,
    private val onEliminar:  (Producto) -> Unit,
    private val soloLectura: Boolean = false,
    private val onVerDetalle: (Producto) -> Unit = {}
) : RecyclerView.Adapter<AdminProductoAdapter.AdminViewHolder>() {

    private val skeleton = ColorDrawable(Color.parseColor("#F0F0F0"))

    fun actualizarLista(nuevos: List<Producto>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = productos.size
            override fun getNewListSize() = nuevos.size
            override fun areItemsTheSame(o: Int, n: Int) =
                productos[o].idProducto == nuevos[n].idProducto
            override fun areContentsTheSame(o: Int, n: Int) = productos[o] == nuevos[n]
        })
        productos = nuevos
        diff.dispatchUpdatesTo(this)
    }

    /** Elimina un item de la lista con animación del RecyclerView */
    fun eliminarItem(idProducto: Long) {
        val idx = productos.indexOfFirst { it.idProducto == idProducto }
        if (idx >= 0) {
            productos = productos.toMutableList().also { it.removeAt(idx) }
            notifyItemRemoved(idx)
        }
    }

    inner class AdminViewHolder(private val b: ItemProductoAdminBinding) :
        RecyclerView.ViewHolder(b.root) {

        @SuppressLint("SetTextI18n")
        fun bind(p: Producto) {
            b.tvAdminNombre.text    = p.nombre
            b.tvAdminPrecio.text    = "B/. %.2f".format(p.precioVenta)
            b.tvAdminCategoria.text = p.categoria?.uppercase() ?: ""
            b.tvAdminMarca.text     = p.marca ?: ""

            if (p.stock > 0) {
                b.tvAdminStock.text = "Stock: ${p.stock}"
                b.tvAdminStock.setTextColor(ContextCompat.getColor(b.root.context, R.color.green_primary))
            } else {
                b.tvAdminStock.text = "Agotado"
                b.tvAdminStock.setTextColor(Color.parseColor("#E53935"))
            }

            // Imagen
            b.viewAdminSkeleton.visibility = View.VISIBLE
            if (!p.imagen.isNullOrEmpty()) {
                b.ivAdminProducto.clearColorFilter()
                Glide.with(b.ivAdminProducto.context)
                    .load(p.imagen)
                    .placeholder(skeleton)
                    .error(skeleton)
                    .transition(DrawableTransitionOptions.withCrossFade(250))
                    .fitCenter()
                    .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                        override fun onLoadFailed(e: com.bumptech.glide.load.engine.GlideException?, model: Any?,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                            isFirstResource: Boolean): Boolean { b.viewAdminSkeleton.visibility = View.GONE; return false }
                        override fun onResourceReady(resource: android.graphics.drawable.Drawable, model: Any,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                            dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                            b.viewAdminSkeleton.visibility = View.GONE; return false }
                    })
                    .into(b.ivAdminProducto)
            } else {
                b.ivAdminProducto.setImageResource(R.drawable.ic_cart)
                b.ivAdminProducto.setColorFilter(ContextCompat.getColor(b.root.context, R.color.green_primary))
                b.viewAdminSkeleton.visibility = View.GONE
            }

            // Tap en toda la card → ver detalles
            b.root.setOnClickListener { onVerDetalle(p) }

            // Modo solo lectura (empleado): ocultar botones de acción
            if (soloLectura) {
                b.btnEditarProducto.visibility   = View.GONE
                b.btnEliminarProducto.visibility = View.GONE
            } else {
                b.btnEditarProducto.visibility   = View.VISIBLE
                b.btnEliminarProducto.visibility = View.VISIBLE
                b.btnEditarProducto.setOnClickListener  { onEditar(p) }
                b.btnEliminarProducto.setOnClickListener { onEliminar(p) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AdminViewHolder(ItemProductoAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) = holder.bind(productos[position])
    override fun getItemCount() = productos.size
}
