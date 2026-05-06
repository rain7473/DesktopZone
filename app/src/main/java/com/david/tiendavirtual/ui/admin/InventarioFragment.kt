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
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.repository.AdminProductoRepository
import com.david.tiendavirtual.data.repository.ProductoRepository
import com.david.tiendavirtual.databinding.FragmentInventarioBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter:   AdminProductoAdapter
    private lateinit var repoAdmin: AdminProductoRepository
    private lateinit var repoProds: ProductoRepository

    private var listaCompleta:  List<Producto> = emptyList()
    private var categoriaFiltro: String?       = null

    // Refresca si se hizo alguna acción desde el detalle (editar/eliminar)
    private val detalleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) cargarProductos()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repoAdmin = AdminProductoRepository(requireContext())
        repoProds = ProductoRepository(requireContext())

        val esEmpleado = !UserSession.isAdmin

        adapter = AdminProductoAdapter(
            productos    = emptyList(),
            onEditar     = { producto -> if (!esEmpleado) abrirFormulario(producto) },
            onEliminar   = { producto -> if (!esEmpleado) confirmarEliminar(producto) },
            soloLectura  = esEmpleado,
            onVerDetalle = { producto -> abrirDetalle(producto) }
        )
        binding.recyclerInventario.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInventario.adapter = adapter

        binding.etBuscarInv.addTextChangedListener { text ->
            filtrar(text.toString(), categoriaFiltro)
        }

        // Ocultar FAB para empleados (solo lectura)
        if (esEmpleado) {
            binding.fabAgregarInv.visibility = View.GONE
            binding.btnGestionarCategorias.visibility = View.GONE
        } else {
            binding.fabAgregarInv.setOnClickListener { abrirFormulario(null) }
            binding.btnGestionarCategorias.visibility = View.VISIBLE
            binding.btnGestionarCategorias.setOnClickListener {
                startActivity(Intent(requireContext(), AdminCategoriasActivity::class.java))
            }
        }

        cargarProductos()
    }

    override fun onResume() {
        super.onResume()
        if (listaCompleta.isNotEmpty()) cargarProductos()
    }

    private fun cargarProductos() {
        binding.progressInventario.visibility = View.VISIBLE
        binding.recyclerInventario.visibility = View.GONE
        binding.tvInventarioVacio.visibility  = View.GONE

        repoProds.obtenerProductos(
            onSuccess = { productos ->
                listaCompleta = productos
                poblarChipsCategorias(productos)
                filtrar(binding.etBuscarInv.text.toString(), categoriaFiltro)
                binding.progressInventario.visibility = View.GONE
            },
            onError = { msg ->
                binding.progressInventario.visibility = View.GONE
                binding.tvInventarioVacio.visibility  = View.VISIBLE
                Snackbar.make(binding.root, "⚠ $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun poblarChipsCategorias(productos: List<Producto>) {
        val chipGroup = binding.chipGroupCategoria
        chipGroup.removeAllViews()

        val categorias = listOf("Todos") + productos.mapNotNull { it.categoria }.distinct().sorted()

        for (cat in categorias) {
            val chip = Chip(requireContext()).apply {
                text        = cat
                isCheckable = true
                isChecked   = (cat == "Todos" && categoriaFiltro == null) ||
                              (cat == categoriaFiltro)
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    categoriaFiltro = if (cat == "Todos") null else cat
                    filtrar(binding.etBuscarInv.text.toString(), categoriaFiltro)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun filtrar(texto: String, categoria: String?) {
        val q = texto.trim().lowercase()
        val resultado = listaCompleta.filter { p ->
            val matchTexto = q.isEmpty() ||
                p.nombre.lowercase().contains(q) ||
                (p.categoria ?: "").lowercase().contains(q) ||
                (p.marca ?: "").lowercase().contains(q)
            val matchCat = categoria == null || p.categoria == categoria
            matchTexto && matchCat
        }
        adapter.actualizarLista(resultado)
        binding.tvInventarioVacio.visibility  = if (resultado.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerInventario.visibility = if (resultado.isEmpty()) View.GONE    else View.VISIBLE
    }

    private fun abrirFormulario(producto: Producto?) {
        val intent = Intent(requireContext(), AdminProductoFormActivity::class.java)
        if (producto != null) intent.putExtra(AdminProductoFormActivity.EXTRA_PRODUCTO, producto)
        startActivity(intent)
    }

    private fun abrirDetalle(producto: Producto) {
        val intent = Intent(requireContext(), DetalleProductoEmpleadoActivity::class.java)
        intent.putExtra(DetalleProductoEmpleadoActivity.EXTRA_PRODUCTO, producto)
        detalleLauncher.launch(intent)
    }

    private fun confirmarEliminar(producto: Producto) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Eliminar producto?")
            .setMessage("«${producto.nombre}»\n\nEsta acción no se puede deshacer.")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Eliminar") { dialog, _ -> dialog.dismiss(); ejecutarEliminar(producto) }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun ejecutarEliminar(producto: Producto) {
        // 1. Animación inmediata: quitar el item de la lista visualmente
        adapter.eliminarItem(producto.idProducto)
        listaCompleta = listaCompleta.filter { it.idProducto != producto.idProducto }

        // 2. Snackbar con opción de deshacer durante 4 s
        val snack = Snackbar.make(binding.root, "✅ Producto eliminado correctamente", 4000)
        var deshecho = false

        snack.setAction("Deshacer") {
            deshecho = true
            cargarProductos()   // restaura la lista desde el servidor
        }
        snack.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(sb: Snackbar?, event: Int) {
                if (!deshecho) {
                    // 3. Llamada real al backend solo si no se deshizo
                    repoAdmin.eliminarProducto(
                        idProducto = producto.idProducto,
                        onSuccess  = { /* ya eliminado visualmente */ },
                        onError    = { msg ->
                            // Si falla el backend, restaurar
                            Snackbar.make(binding.root, "❌ $msg", Snackbar.LENGTH_LONG).show()
                            cargarProductos()
                        }
                    )
                }
            }
        })
        snack.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

