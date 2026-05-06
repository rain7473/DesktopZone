package com.david.tiendavirtual.ui.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.david.tiendavirtual.data.model.EmpleadoAdmin
import com.david.tiendavirtual.data.repository.AdminEmpleadosRepository
import com.david.tiendavirtual.databinding.FragmentEmpleadosBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class EmpleadosFragment : Fragment() {

    private var _binding: FragmentEmpleadosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdminEmpleadoAdapter
    private lateinit var repo:    AdminEmpleadosRepository

    private var listaCompleta: List<EmpleadoAdmin> = emptyList()

    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) cargarEmpleados()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmpleadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = AdminEmpleadosRepository(requireContext())

        adapter = AdminEmpleadoAdapter(
            empleados  = emptyList(),
            onEditar   = { emp -> abrirFormulario(emp) },
            onEliminar = { emp -> confirmarEliminar(emp) }
        )
        binding.recyclerEmpleados.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEmpleados.adapter = adapter

        binding.etBuscarEmp.addTextChangedListener { text ->
            val q = text.toString().trim().lowercase()
            val filtrado = if (q.isEmpty()) listaCompleta
            else listaCompleta.filter {
                it.nombreCompleto.lowercase().contains(q) ||
                it.usuario.lowercase().contains(q) ||
                it.rolNombre.lowercase().contains(q)
            }
            adapter.actualizarLista(filtrado)
            binding.tvEmpleadosVacio.visibility = if (filtrado.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAgregarEmp.setOnClickListener { abrirFormulario(null) }

        cargarEmpleados()
    }

    private fun cargarEmpleados() {
        binding.progressEmpleados.visibility = View.VISIBLE
        binding.tvEmpleadosVacio.visibility  = View.GONE

        repo.listar(
            onSuccess = { list ->
                listaCompleta = list
                adapter.actualizarLista(list)
                binding.progressEmpleados.visibility = View.GONE
                binding.tvEmpleadosVacio.visibility  = if (list.isEmpty()) View.VISIBLE else View.GONE
            },
            onError = { msg ->
                binding.progressEmpleados.visibility = View.GONE
                binding.tvEmpleadosVacio.visibility  = View.VISIBLE
                Snackbar.make(binding.root, "⚠ $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun abrirFormulario(emp: EmpleadoAdmin?) {
        val intent = Intent(requireContext(), AdminEmpleadoFormActivity::class.java)
        if (emp != null) intent.putExtra(AdminEmpleadoFormActivity.EXTRA_EMPLEADO, emp)
        formLauncher.launch(intent)
    }

    private fun confirmarEliminar(emp: EmpleadoAdmin) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar empleado")
            .setMessage("¿Eliminar a ${emp.nombreCompleto}?\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                repo.eliminar(
                    usuarioTarget = emp.usuario,
                    onSuccess = { msg ->
                        Snackbar.make(binding.root, "✓ $msg", Snackbar.LENGTH_SHORT).show()
                        cargarEmpleados()
                    },
                    onError = { msg ->
                        Snackbar.make(binding.root, "❌ $msg", Snackbar.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

