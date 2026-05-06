package com.david.tiendavirtual.ui.empleado

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.data.repository.AdminProductoRepository
import com.david.tiendavirtual.data.repository.ProductoRepository
import com.david.tiendavirtual.databinding.FragmentEmpleadoDashboardBinding
import com.david.tiendavirtual.ui.admin.AdminProductoAdapter
import com.google.android.material.snackbar.Snackbar

class EmpleadoDashboardFragment : Fragment() {

    private var _binding: FragmentEmpleadoDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repoProductos: ProductoRepository
    private lateinit var repoCatalogo: AdminProductoRepository
    private lateinit var adapterBajoStock: AdminProductoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmpleadoDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repoProductos = ProductoRepository(requireContext())
        repoCatalogo  = AdminProductoRepository(requireContext())

        adapterBajoStock = AdminProductoAdapter(
            productos   = emptyList(),
            onEditar    = { },
            onEliminar  = { },
            soloLectura = true
        )
        binding.recyclerBajoStockEmp.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBajoStockEmp.adapter = adapterBajoStock

        cargarDatos()
    }

    private fun cargarDatos() {
        binding.progressEmpleadoDash.visibility      = View.VISIBLE
        binding.layoutEmpleadoDashContent.visibility = View.GONE

        // Cargar categorías para el contador
        repoCatalogo.obtenerCatalogo(
            onSuccess = { categorias, _ ->
                binding.tvEmpCategorias.text = categorias.size.toString()
            },
            onError = { /* No crítico */ }
        )

        // Cargar productos
        repoProductos.obtenerProductos(
            onSuccess = { productos ->
                poblarDashboard(productos)
            },
            onError = { msg ->
                binding.progressEmpleadoDash.visibility = View.GONE
                Snackbar.make(binding.root, "⚠ $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun poblarDashboard(productos: List<Producto>) {
        val total    = productos.size
        val bajoStock = productos.filter { it.stock in 1..4 }
        val agotados  = productos.filter { it.stock <= 0 }

        binding.tvEmpTotalProductos.text = total.toString()
        binding.tvEmpBajoStock.text      = bajoStock.size.toString()
        binding.tvEmpAgotados.text       = agotados.size.toString()

        if (bajoStock.isEmpty()) {
            binding.recyclerBajoStockEmp.visibility = View.GONE
            binding.tvEmpSinBajoStock.visibility    = View.VISIBLE
        } else {
            binding.recyclerBajoStockEmp.visibility = View.VISIBLE
            binding.tvEmpSinBajoStock.visibility    = View.GONE
            adapterBajoStock.actualizarLista(bajoStock)
        }

        binding.progressEmpleadoDash.visibility      = View.GONE
        binding.layoutEmpleadoDashContent.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


