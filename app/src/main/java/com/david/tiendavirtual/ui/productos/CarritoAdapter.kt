package com.david.tiendavirtual.ui.productos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.CarritoItem
import com.david.tiendavirtual.databinding.ItemCarritoBinding

class CarritoAdapter(
    private val onCambio: () -> Unit,
    private val onEliminar: (CarritoItem) -> Unit = {}
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    private var items: List<CarritoItem> = emptyList()

    fun actualizarLista(nuevaLista: List<CarritoItem>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = nuevaLista.size
            override fun areItemsTheSame(old: Int, new: Int) =
                items[old].producto.idProducto == nuevaLista[new].producto.idProducto
            override fun areContentsTheSame(old: Int, new: Int) =
                items[old].cantidad == nuevaLista[new].cantidad
        })
        items = nuevaLista
        diff.dispatchUpdatesTo(this)
    }

    inner class CarritoViewHolder(private val binding: ItemCarritoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CarritoItem) {
            binding.tvNombre.text   = item.producto.nombre
            binding.tvPrecio.text   = "B/. %.2f".format(item.producto.precioVenta)
            binding.tvCantidad.text = item.cantidad.toString()
            binding.tvSubtotal.text = "Total: B/. %.2f".format(item.subtotal())

            // Cargar imagen del producto
            val imgUrl = item.producto.imagen
            if (!imgUrl.isNullOrEmpty()) {
                binding.ivProductoCarrito.clearColorFilter()
                binding.ivProductoCarrito.setPadding(0, 0, 0, 0)
                Glide.with(binding.ivProductoCarrito)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_cart)
                    .error(R.drawable.ic_cart)
                    .centerCrop()
                    .into(binding.ivProductoCarrito)
            } else {
                val pad = (14 * binding.root.context.resources.displayMetrics.density).toInt()
                binding.ivProductoCarrito.setPadding(pad, pad, pad, pad)
                binding.ivProductoCarrito.setImageResource(R.drawable.ic_cart)
                binding.ivProductoCarrito.setColorFilter(
                    androidx.core.content.ContextCompat.getColor(
                        binding.root.context, R.color.green_primary
                    )
                )
            }

            binding.btnMas.setOnClickListener {
                CarritoManager.incrementar(item.producto.idProducto)
                items = CarritoManager.obtenerItems()
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos)
                onCambio()
            }

            binding.btnMenos.setOnClickListener {
                CarritoManager.decrementar(item.producto.idProducto)
                items = CarritoManager.obtenerItems()
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos)
                onCambio()
            }

            binding.btnEliminar.setOnClickListener {
                val itemEliminado = item.copy()
                CarritoManager.eliminar(item.producto.idProducto)
                actualizarLista(CarritoManager.obtenerItems())
                onCambio()
                onEliminar(itemEliminado)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val binding = ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}
