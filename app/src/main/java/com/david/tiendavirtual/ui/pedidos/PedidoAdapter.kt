package com.david.tiendavirtual.ui.pedidos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.david.tiendavirtual.data.model.PedidoDB
import com.david.tiendavirtual.data.model.PedidoItemDB
import com.david.tiendavirtual.databinding.ItemOrderBinding

class PedidoAdapter(
    private var pedidos: List<PedidoDB>,
    private val onClickPedido: (PedidoDB) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    fun actualizarLista(nuevos: List<PedidoDB>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = pedidos.size
            override fun getNewListSize() = nuevos.size
            override fun areItemsTheSame(old: Int, new: Int) =
                pedidos[old].idFactura == nuevos[new].idFactura
            override fun areContentsTheSame(old: Int, new: Int) =
                pedidos[old] == nuevos[new]
        })
        pedidos = nuevos
        diff.dispatchUpdatesTo(this)
    }

    inner class PedidoViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pedido: PedidoDB) {
            binding.tvOrdenId.text      = "Pedido #${pedido.idFactura}"
            binding.tvOrdenFecha.text   = formatearFechaCompacta(pedido.fecha)
            binding.tvOrdenResumen.text = resumenProductos(pedido.detalles)
            binding.tvOrdenTotal.text   = "B/. %.2f".format(pedido.total)

            binding.root.setOnClickListener { onClickPedido(pedido) }
        }

        // "AMD Ryzen 5 7600X +1 más"  ó  "2 productos"
        private fun resumenProductos(detalles: List<PedidoItemDB>): String {
            if (detalles.isEmpty()) return "Sin productos"
            val primero = detalles[0].nombre
            val resto   = detalles.size - 1
            return if (resto == 0) primero else "$primero +$resto más"
        }

        // "2026-04-09 17:38:00"  →  "09 Abr 2026 • 5:38 PM"
        private fun formatearFechaCompacta(raw: String): String {
            return try {
                val partes    = raw.trim().split(" ")
                val dateParts = partes[0].split("-")
                val timePart  = if (partes.size > 1) partes[1] else "00:00:00"
                val meses     = listOf(
                    "", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                    "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
                )
                val mes  = meses[dateParts[1].toInt()]
                val hh0  = timePart.split(":")[0].toInt()
                val mm   = timePart.split(":")[1]
                val ampm = if (hh0 >= 12) "PM" else "AM"
                val hh12 = when {
                    hh0 == 0  -> 12
                    hh0 > 12  -> hh0 - 12
                    else      -> hh0
                }
                "${dateParts[2]} $mes ${dateParts[0]} • $hh12:$mm $ampm"
            } catch (_: Exception) { raw }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val b = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(b)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) =
        holder.bind(pedidos[position])

    override fun getItemCount() = pedidos.size
}
