package com.david.tiendavirtual.ui.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.GraficoItem
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.repository.AdminDashboardRepository
import com.david.tiendavirtual.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: AdminDashboardRepository
    private var periodoActual = 7  // días seleccionados para el gráfico

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = AdminDashboardRepository(requireContext())

        // ── Acciones rápidas ──────────────────────────────────────────
        if (UserSession.isAdmin) {
            binding.btnAgregarProducto.setOnClickListener {
                startActivity(Intent(requireContext(), AdminProductoFormActivity::class.java))
            }

            binding.btnVerReportes.setOnClickListener {
                // Navegar a la pestaña Finanzas del BottomNav
                val activity = requireActivity() as? AdminMainActivity
                activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.adminBottomNav)
                    ?.selectedItemId = R.id.nav_finanzas
            }
        } else {
            // Modo empleado: ocultar acciones de escritura
            binding.btnAgregarProducto.visibility = View.GONE
            binding.btnVerReportes.visibility = View.GONE
        }

        // ── Selector de periodo del gráfico ──────────────────────────
        binding.chipPeriodo.setOnClickListener { view ->
            mostrarMenuPeriodo(view)
        }

        cargarDashboard()
    }

    private fun mostrarMenuPeriodo(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 7,   0, "Últimos 7 días")
        popup.menu.add(0, 15,  1, "Últimos 15 días")
        popup.menu.add(0, 30,  2, "Último mes")
        popup.menu.add(0, 90,  3, "Últimos 3 meses")
        popup.menu.add(0, 365, 4, "Último año")

        popup.setOnMenuItemClickListener { item ->
            periodoActual = item.itemId
            binding.tvChipPeriodo.text = item.title
            cargarDashboard()
            true
        }
        popup.show()
    }

    private fun cargarDashboard() {
        binding.progressDashboard.visibility      = View.VISIBLE
        binding.layoutDashboardContent.visibility = View.GONE

        repo.obtenerDashboard(
            periodo = periodoActual,
            onSuccess = { data ->
                binding.tvVentasHoy.text      = "B/. %.2f".format(data.ventasHoy)
                binding.tvVentasMes.text      = "B/. %.2f".format(data.ventasMes)
                binding.tvPedidosMes.text     = data.totalPedidosMes.toString()
                binding.tvTotalProductos.text = data.totalProductos.toString()
                binding.tvBajoStock.text      = data.bajoStock.toString()
                dibujarGrafico(data.graficoSemana)
                binding.progressDashboard.visibility      = View.GONE
                binding.layoutDashboardContent.visibility = View.VISIBLE
            },
            onError = { msg ->
                binding.progressDashboard.visibility = View.GONE
                Snackbar.make(binding.root, "⚠ $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun dibujarGrafico(datos: List<GraficoItem>) {
        val chart  = binding.chartSemana
        val labels = datos.map { it.etiqueta }

        // ── Dataset 1: Semana actual (línea sólida verde con relleno) ──
        val entriesActual = datos.mapIndexed { i, item ->
            Entry(i.toFloat(), item.ingresos.toFloat())
        }
        val dsActual = LineDataSet(entriesActual, "Semana actual").apply {
            color            = Color.parseColor("#2DC66B")
            lineWidth        = 2.5f
            setCircleColor(Color.parseColor("#2DC66B"))
            circleRadius     = 4f
            circleHoleRadius = 2f
            circleHoleColor  = Color.WHITE
            setDrawValues(true)
            valueTextColor   = Color.DKGRAY
            valueTextSize    = 9f
            mode             = LineDataSet.Mode.CUBIC_BEZIER
            // Relleno de área bajo la curva
            setDrawFilled(true)
            fillColor        = Color.parseColor("#2DC66B")
            fillAlpha        = 30
            highLightColor   = Color.parseColor("#1A8A45")
        }

        // ── Dataset 2: Semana pasada (línea punteada gris) ──
        val entriesPasada = datos.mapIndexed { i, item ->
            Entry(i.toFloat(), item.ingresosPasados.toFloat())
        }
        val dsPasada = LineDataSet(entriesPasada, "Semana pasada").apply {
            color            = Color.parseColor("#BDBDBD")
            lineWidth        = 1.5f
            setCircleColor(Color.parseColor("#BDBDBD"))
            circleRadius     = 3f
            circleHoleRadius = 1.5f
            circleHoleColor  = Color.WHITE
            setDrawValues(false)
            mode             = LineDataSet.Mode.CUBIC_BEZIER
            enableDashedLine(10f, 6f, 0f)
            setDrawFilled(false)
            highLightColor   = Color.parseColor("#9E9E9E")
        }

        chart.data = LineData(dsActual, dsPasada)

        // ── Tooltip interactivo ─────────────────────────────────────
        val marker = ChartMarkerView(
            context    = requireContext(),
            labels     = labels,
            dataPasada = datos.map { it.ingresosPasados }
        )
        marker.chartView = chart
        chart.marker = marker

        // ── Eje X ───────────────────────────────────────────────────
        chart.xAxis.apply {
            valueFormatter  = IndexAxisValueFormatter(labels)
            position        = XAxis.XAxisPosition.BOTTOM
            granularity     = 1f
            setDrawGridLines(false)
            textSize        = 10f
            textColor       = Color.parseColor("#9E9E9E")
        }

        // ── Eje Y izquierdo ─────────────────────────────────────────
        chart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor     = Color.parseColor("#F0F0F0")
            axisMinimum   = 0f
            textColor     = Color.parseColor("#9E9E9E")
            textSize      = 10f
        }

        // ── Config general ──────────────────────────────────────────
        chart.axisRight.isEnabled      = false
        chart.legend.isEnabled         = false
        chart.description.isEnabled    = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.setScaleEnabled(false)
        chart.animateY(700)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
