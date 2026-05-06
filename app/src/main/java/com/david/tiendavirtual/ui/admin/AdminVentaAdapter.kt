package com.david.tiendavirtual.ui.admin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.david.tiendavirtual.data.model.PedidoDB
import com.david.tiendavirtual.databinding.ItemOrderBinding

class AdminVentaAdapter(
    private var ventas:   List<PedidoDB>,
    private val onClick: (PedidoDB) -> Unit
) : RecyclerView.Adapter<AdminVentaAdapter.VentaVH>() {

    @SuppressLint("NotifyDataSetChanged")
    fun actualizar(nuevas: List<PedidoDB>) {
        ventas = nuevas
        notifyDataSetChanged()
    }

    inner class VentaVH(private val b: ItemOrderBinding) : RecyclerView.ViewHolder(b.root) {
        @SuppressLint("SetTextI18n")
        fun bind(p: PedidoDB) {
            b.tvOrdenId.text    = "Pedido #${p.idFactura}"
            b.tvOrdenFecha.text = p.fecha

            // Resumen de productos
            val resumen = when {
                p.detalles.isEmpty() -> "Sin detalle"
                p.detalles.size == 1 -> "${p.detalles[0].nombre} x${p.detalles[0].cantidad}"
                else -> "${p.detalles[0].nombre} +${p.detalles.size - 1} más"
            }
            b.tvOrdenResumen.text = resumen

            b.tvOrdenTotal.text = "B/. %.2f".format(p.total)

            b.root.setOnClickListener { onClick(p) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VentaVH(ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VentaVH, position: Int) = holder.bind(ventas[position])
    override fun getItemCount() = ventas.size
}

