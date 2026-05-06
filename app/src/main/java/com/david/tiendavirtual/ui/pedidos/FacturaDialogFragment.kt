package com.david.tiendavirtual.ui.pedidos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.PedidoDB
import com.david.tiendavirtual.databinding.DialogFacturaBinding
import com.david.tiendavirtual.databinding.ItemFacturaProductoBinding

class FacturaDialogFragment : DialogFragment() {

    private var _binding: DialogFacturaBinding? = null
    private val binding get() = _binding!!

    private lateinit var pedido: PedidoDB

    companion object {
        private const val ARG_PEDIDO = "arg_pedido"

        fun newInstance(pedido: PedidoDB): FacturaDialogFragment =
            FacturaDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PEDIDO, pedido)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        pedido = requireArguments().getSerializable(ARG_PEDIDO) as PedidoDB
        // Estilo sin título y fondo transparente definido en themes.xml
        setStyle(STYLE_NO_TITLE, R.style.DialogFactura)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFacturaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Header ──────────────────────────────────────────────────────
        binding.tvDialogPedidoId.text = "Pedido #${pedido.idFactura}"
        binding.tvDialogFecha.text    = formatearFechaDialog(pedido.fecha)

        // ── Productos (filas dinámicas) ──────────────────────────────────
        binding.llProductos.removeAllViews()
        pedido.detalles.forEach { item ->
            val row = ItemFacturaProductoBinding.inflate(
                layoutInflater, binding.llProductos, false
            )
            row.tvProdNombre.text  = item.nombre
            row.tvProdTotal.text   = "B/. %.2f".format(item.subtotal())
            row.tvProdDetalle.text = "${item.cantidad} × B/. %.2f".format(item.precioUnitario)
            binding.llProductos.addView(row.root)
        }

        // ── Resumen ──────────────────────────────────────────────────────
        binding.tvDialogSubtotal.text = "B/. %.2f".format(pedido.subtotal)
        binding.tvDialogItbms.text    = "B/. %.2f".format(pedido.itbms)
        binding.tvDialogTotal.text    = "B/. %.2f".format(pedido.total)

        // ── Cerrar ───────────────────────────────────────────────────────
        binding.btnCerrar.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Fondo transparente para que la card muestre sus esquinas redondeadas
            setBackgroundDrawableResource(android.R.color.transparent)
            // Ancho = 90 % de la pantalla, alto = automático
            val w = (resources.displayMetrics.widthPixels * 0.90).toInt()
            setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
            // Animación scale + fade
            setWindowAnimations(R.style.DialogAnimation)
            // Dim del fondo (equivalente a android:windowDimAmount en XML)
            setDimAmount(0.5f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Formateador de fecha para el dialog ─────────────────────────────
    // Entrada: "2026-04-07 11:36:00"  →  "07 Abril 2026 • 11:36 AM"
    private fun formatearFechaDialog(raw: String): String {
        return try {
            val partes = raw.trim().split(" ")
            val dateParts = partes[0].split("-")
            val timePart  = if (partes.size > 1) partes[1] else "00:00:00"
            val meses = listOf(
                "", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
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

