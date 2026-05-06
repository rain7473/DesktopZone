package com.david.tiendavirtual.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.david.tiendavirtual.R
import com.david.tiendavirtual.databinding.LayoutFilterSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

data class FiltroState(
    val sortType:   String = "relevancia",  // relevancia | precio_asc | precio_desc
    val priceRange: String = "todos",       // todos | 0-25 | 25-50 | 50-100 | 100+
    val nombre:     String = "",
    val marca:      String = "",
    val categoria:  String = ""
)

class SearchFilterSheet(
    private val estadoActual:        FiltroState,
    private val categoriasDisponibles: List<String> = emptyList(),
    private val onAplicar:           (FiltroState) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: LayoutFilterSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = LayoutFilterSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restaurar estado de ordenamiento
        when (estadoActual.sortType) {
            "precio_asc"  -> binding.chipPrecioAsc.isChecked  = true
            "precio_desc" -> binding.chipPrecioDesc.isChecked = true
            else          -> binding.chipRelevancia.isChecked = true
        }

        // Restaurar rango de precio
        when (estadoActual.priceRange) {
            "0-25"   -> binding.chipPrecioA.isChecked = true
            "25-50"  -> binding.chipPrecioB.isChecked = true
            "50-100" -> binding.chipPrecioC.isChecked = true
            "100+"   -> binding.chipPrecioD.isChecked = true
            else     -> binding.chipPrecioTodos.isChecked = true
        }
        binding.etFiltroNombre.setText(estadoActual.nombre)
        binding.etFiltroMarca.setText(estadoActual.marca)

        // Construir chips de categorías dinámicamente
        if (categoriasDisponibles.isNotEmpty()) {
            binding.tvLabelCategoria.visibility    = View.VISIBLE
            binding.chipGroupCategoria.visibility  = View.VISIBLE
            binding.chipGroupCategoria.removeAllViews()

            // Chip "Todas"
            val chipTodas = Chip(requireContext()).apply {
                text      = "Todas"
                isCheckable = true
                isChecked = estadoActual.categoria.isEmpty()
                setChipBackgroundColorResource(R.color.bg_screen)
                setTextColor(resources.getColorStateList(android.R.color.tab_indicator_text, null))
                setChipStrokeColorResource(R.color.card_stroke)
                chipStrokeWidth = 1.5f
            }
            binding.chipGroupCategoria.addView(chipTodas)

            categoriasDisponibles.forEach { cat ->
                val chip = Chip(requireContext()).apply {
                    text        = cat
                    isCheckable = true
                    isChecked   = estadoActual.categoria == cat
                    setChipBackgroundColorResource(R.color.bg_screen)
                    setTextColor(resources.getColorStateList(android.R.color.tab_indicator_text, null))
                    setChipStrokeColorResource(R.color.card_stroke)
                    chipStrokeWidth = 1.5f
                }
                binding.chipGroupCategoria.addView(chip)
            }
        } else {
            binding.tvLabelCategoria.visibility   = View.GONE
            binding.chipGroupCategoria.visibility = View.GONE
        }

        // Cerrar
        binding.btnCerrarSheet.setOnClickListener { dismiss() }

        // Limpiar
        binding.btnLimpiarFiltro.setOnClickListener {
            binding.chipRelevancia.isChecked  = true
            binding.chipPrecioTodos.isChecked = true
            binding.etFiltroNombre.text?.clear()
            binding.etFiltroMarca.text?.clear()
            // Seleccionar "Todas" en categorías
            if (binding.chipGroupCategoria.childCount > 0) {
                (binding.chipGroupCategoria.getChildAt(0) as? Chip)?.isChecked = true
            }
        }

        // Aplicar
        binding.btnAplicarFiltro.setOnClickListener {
            val sort = when (binding.chipGroupSort.checkedChipId) {
                binding.chipPrecioAsc.id  -> "precio_asc"
                binding.chipPrecioDesc.id -> "precio_desc"
                else                      -> "relevancia"
            }
            val price = when (binding.chipGroupPrecio.checkedChipId) {
                binding.chipPrecioA.id -> "0-25"
                binding.chipPrecioB.id -> "25-50"
                binding.chipPrecioC.id -> "50-100"
                binding.chipPrecioD.id -> "100+"
                else                   -> "todos"
            }

            // Obtener categoría seleccionada
            val categoriaSeleccionada = run {
                val checkedId = binding.chipGroupCategoria.checkedChipId
                if (checkedId == View.NO_ID) return@run ""
                val chip = binding.chipGroupCategoria.findViewById<Chip>(checkedId)
                val texto = chip?.text?.toString() ?: ""
                if (texto == "Todas") "" else texto
            }

            onAplicar(
                FiltroState(
                    sortType   = sort,
                    priceRange = price,
                    nombre     = binding.etFiltroNombre.text.toString().trim(),
                    marca      = binding.etFiltroMarca.text.toString().trim(),
                    categoria  = categoriaSeleccionada
                )
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
