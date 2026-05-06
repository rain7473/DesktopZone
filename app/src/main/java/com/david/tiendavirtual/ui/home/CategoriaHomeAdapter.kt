package com.david.tiendavirtual.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.Categoria
import com.david.tiendavirtual.databinding.ItemCategoriaHomeBinding

class CategoriaHomeAdapter(
    private var categorias: List<Categoria>,
    private val onCategoria: (Categoria?) -> Unit
) : RecyclerView.Adapter<CategoriaHomeAdapter.VH>() {

    private var seleccionado: Int = -1  // -1 = "Todas"

    inner class VH(val b: ItemCategoriaHomeBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCategoriaHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = categorias.size + 1

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ctx = holder.b.root.context

        if (position == 0) {
            holder.b.tvCategoriaNombre.text = "Todas"
            val icono0 = R.drawable.ic_home
            holder.b.ivCategoriaIcon.setImageResource(icono0)
            aplicarEstilo(holder, seleccionado == -1, ctx, icono0)
            holder.b.root.setOnClickListener {
                val prev = seleccionado; seleccionado = -1
                notifyItemChanged(0)
                if (prev >= 0) notifyItemChanged(prev + 1)
                onCategoria(null)
            }
        } else {
            val cat = categorias[position - 1]
            val icono = iconoPorNombre(cat.nombre)
            holder.b.tvCategoriaNombre.text = cat.nombre
            holder.b.ivCategoriaIcon.setImageResource(icono)
            aplicarEstilo(holder, seleccionado == position - 1, ctx, icono)
            holder.b.root.setOnClickListener {
                val prev = seleccionado; seleccionado = position - 1
                notifyItemChanged(0)
                if (prev >= 0) notifyItemChanged(prev + 1)
                notifyItemChanged(position)
                onCategoria(cat)
            }
        }
    }

    private val fotoDrawables = setOf(
        R.drawable.procesador,
        R.drawable.ram,
        R.drawable.mother,
        R.drawable.discosduros,
        R.drawable.fuente
    )

    private fun aplicarEstilo(holder: VH, selected: Boolean, ctx: android.content.Context, iconoRes: Int) {
        if (selected) {
            holder.b.root.strokeColor = ctx.getColor(R.color.green_primary)
            holder.b.root.strokeWidth = 2
            holder.b.tvCategoriaNombre.setTextColor(ctx.getColor(R.color.green_primary))
        } else {
            holder.b.root.strokeColor = ctx.getColor(R.color.card_stroke)
            holder.b.root.strokeWidth = 1
            holder.b.tvCategoriaNombre.setTextColor(ctx.getColor(R.color.text_primary))
        }
        // Solo aplicar tinte verde a íconos vectoriales, no a fotos reales
        if (iconoRes in fotoDrawables) {
            holder.b.ivCategoriaIcon.clearColorFilter()
        } else {
            holder.b.ivCategoriaIcon.setColorFilter(ctx.getColor(R.color.green_primary))
        }
    }

    private fun iconoPorNombre(nombre: String): Int {
        val n = nombre.lowercase()
        return when {
            n.contains("procesador") || n.contains("cpu") -> R.drawable.procesador
            n.contains("memoria") || n.contains("ram") -> R.drawable.ram
            n.contains("madre") || n.contains("tarjeta madre") || n.contains("mainboard") -> R.drawable.mother
            n.contains("disco") || n.contains("ssd") || n.contains("hdd") -> R.drawable.discosduros
            n.contains("fuente") || n.contains("poder") || n.contains("psu") -> R.drawable.fuente
            else -> R.drawable.ic_inventory_box
        }
    }

    fun actualizar(nuevas: List<Categoria>) {
        categorias = nuevas
        seleccionado = -1
        notifyDataSetChanged()
    }

    fun seleccionarTodas() {
        val prev = seleccionado
        seleccionado = -1
        notifyItemChanged(0)
        if (prev >= 0) notifyItemChanged(prev + 1)
    }
}
