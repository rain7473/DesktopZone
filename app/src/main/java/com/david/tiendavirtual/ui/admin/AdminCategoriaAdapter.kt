package com.david.tiendavirtual.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.Categoria
import com.david.tiendavirtual.databinding.ItemCategoriaAdminBinding

class AdminCategoriaAdapter(
    private var categorias: List<Categoria>,
    private val onEditar:   (Categoria) -> Unit,
    private val onEliminar: (Categoria) -> Unit
) : RecyclerView.Adapter<AdminCategoriaAdapter.CatViewHolder>() {

    fun actualizarLista(nuevas: List<Categoria>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = categorias.size
            override fun getNewListSize() = nuevas.size
            override fun areItemsTheSame(o: Int, n: Int)    = categorias[o].id == nuevas[n].id
            override fun areContentsTheSame(o: Int, n: Int) = categorias[o] == nuevas[n]
        })
        categorias = nuevas
        diff.dispatchUpdatesTo(this)
    }

    inner class CatViewHolder(private val b: ItemCategoriaAdminBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(cat: Categoria) {
            b.tvCategoriaNombre.text = cat.nombre
            b.tvCategoriaId.text     = "ID ${cat.id}"

            // Misma lógica de imagen que CategoriaHomeAdapter
            val iconoRes = iconoPorNombre(cat.nombre)
            b.ivCategoriaIconAdmin.setImageResource(iconoRes)

            // Fotos reales no llevan tinte; íconos vectoriales sí
            val fotoDrawables = setOf(
                R.drawable.procesador,
                R.drawable.ram,
                R.drawable.mother,
                R.drawable.discosduros,
                R.drawable.fuente
            )
            if (iconoRes in fotoDrawables) {
                b.ivCategoriaIconAdmin.clearColorFilter()
            } else {
                b.ivCategoriaIconAdmin.setColorFilter(
                    b.root.context.getColor(R.color.green_primary)
                )
            }

            b.btnEditarCategoria.setOnClickListener   { onEditar(cat) }
            b.btnEliminarCategoria.setOnClickListener { onEliminar(cat) }
        }
    }

    /** Misma función que CategoriaHomeAdapter para consistencia visual */
    private fun iconoPorNombre(nombre: String): Int {
        val n = nombre.lowercase()
        return when {
            n.contains("procesador") || n.contains("cpu")                          -> R.drawable.procesador
            n.contains("memoria")    || n.contains("ram")                          -> R.drawable.ram
            n.contains("madre")      || n.contains("mainboard")                    -> R.drawable.mother
            n.contains("disco")      || n.contains("ssd") || n.contains("hdd")    -> R.drawable.discosduros
            n.contains("fuente")     || n.contains("poder") || n.contains("psu")  -> R.drawable.fuente
            else                                                                   -> R.drawable.ic_inventory_box
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CatViewHolder(
            ItemCategoriaAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) =
        holder.bind(categorias[position])

    override fun getItemCount() = categorias.size
}
