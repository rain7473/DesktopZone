package com.david.tiendavirtual.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.david.tiendavirtual.data.model.PedidoDB
import com.david.tiendavirtual.data.repository.AdminEmpleadosRepository
import com.david.tiendavirtual.databinding.FragmentVentasBinding
import com.david.tiendavirtual.ui.pedidos.FacturaDialogFragment
import com.google.android.material.snackbar.Snackbar

class VentasFragment : Fragment() {

    private var _binding: FragmentVentasBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo:    AdminEmpleadosRepository
    private lateinit var adapter: AdminVentaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVentasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = AdminEmpleadosRepository(requireContext())

        adapter = AdminVentaAdapter(emptyList()) { pedido -> mostrarDetalle(pedido) }
        binding.recyclerVentas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerVentas.adapter = adapter

        cargarVentas()
    }

    private fun cargarVentas() {
        binding.progressVentas.visibility = View.VISIBLE
        binding.recyclerVentas.visibility = View.GONE
        binding.tvVentasVacio.visibility  = View.GONE

        repo.obtenerVentas(
            onSuccess = { list ->
                binding.progressVentas.visibility = View.GONE
                if (list.isEmpty()) {
                    binding.tvVentasVacio.visibility = View.VISIBLE
                } else {
                    adapter.actualizar(list)
                    binding.recyclerVentas.visibility = View.VISIBLE
                }
            },
            onError = { msg ->
                binding.progressVentas.visibility = View.GONE
                binding.tvVentasVacio.visibility  = View.VISIBLE
                Snackbar.make(binding.root, "⚠ $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun mostrarDetalle(pedido: PedidoDB) {
        if (childFragmentManager.findFragmentByTag("factura_dialog") == null) {
            FacturaDialogFragment.newInstance(pedido)
                .show(childFragmentManager, "factura_dialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

