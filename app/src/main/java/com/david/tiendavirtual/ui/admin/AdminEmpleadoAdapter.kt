package com.david.tiendavirtual.ui.admin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.david.tiendavirtual.data.model.EmpleadoAdmin
import com.david.tiendavirtual.databinding.ItemEmpleadoBinding

class AdminEmpleadoAdapter(
    private var empleados:  List<EmpleadoAdmin>,
    private val onEditar:   (EmpleadoAdmin) -> Unit,
    private val onEliminar: (EmpleadoAdmin) -> Unit
) : RecyclerView.Adapter<AdminEmpleadoAdapter.EmpViewHolder>() {

    fun actualizarLista(nuevos: List<EmpleadoAdmin>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = empleados.size
            override fun getNewListSize() = nuevos.size
            override fun areItemsTheSame(o: Int, n: Int)    = empleados[o].usuario == nuevos[n].usuario
            override fun areContentsTheSame(o: Int, n: Int) = empleados[o] == nuevos[n]
        })
        empleados = nuevos
        diff.dispatchUpdatesTo(this)
    }

    inner class EmpViewHolder(private val b: ItemEmpleadoBinding) :
        RecyclerView.ViewHolder(b.root) {

        @SuppressLint("SetTextI18n")
        fun bind(emp: EmpleadoAdmin) {
            val inicial = (emp.nombre.firstOrNull() ?: emp.usuario.firstOrNull() ?: 'U')
                .uppercaseChar().toString()
            b.tvEmpInicial.text  = inicial
            b.tvEmpNombre.text   = emp.nombreCompleto
            b.tvEmpUsuario.text  = "@${emp.usuario}"
            b.tvEmpRol.text      = emp.rolNombre
            b.btnEditarEmp.setOnClickListener   { onEditar(emp) }
            b.btnEliminarEmp.setOnClickListener { onEliminar(emp) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EmpViewHolder(ItemEmpleadoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: EmpViewHolder, position: Int) = holder.bind(empleados[position])
    override fun getItemCount() = empleados.size
}


