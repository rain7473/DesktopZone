package com.david.tiendavirtual.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.FinanzasMensual
import com.david.tiendavirtual.data.repository.AdminDashboardRepository
import com.david.tiendavirtual.databinding.FragmentFinanzasBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar

class FinanzasFragment : Fragment() {

    private enum class FiltroFinanzas(
        val apiValue: String,
        val tooltipValueTitle: String,
        val expandirDiasTooltip: Boolean
    ) {
        DOS_SEMANAS("2semanas", "Día seleccionado", true),
        ESTE_MES("mes", "Ingreso del día", true),
        TRES_MESES("3meses", "Ingreso del mes", false),
        ESTE_ANIO("anio", "Ingreso del mes", false)
    }

    private var _binding: FragmentFinanzasBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: AdminDashboardRepository
    private var filtroActual = FiltroFinanzas.ESTE_MES

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanzasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = AdminDashboardRepository(requireContext())

        binding.chipGroupFiltro.setOnCheckedStateChangeListener { _, checkedIds ->
            filtroActual = when (checkedIds.firstOrNull()) {
                R.id.chip2Semanas -> FiltroFinanzas.DOS_SEMANAS
                R.id.chip3Meses   -> FiltroFinanzas.TRES_MESES
                R.id.chipAnio     -> FiltroFinanzas.ESTE_ANIO
                else              -> FiltroFinanzas.ESTE_MES
            }
            cargarFinanzas()
        }

        cargarFinanzas()
    }

    private fun cargarFinanzas() {
        binding.progressFinanzas.visibility = View.VISIBLE

        repo.obtenerFinanzas(
            filtro = filtroActual.apiValue,
            onSuccess = { data ->
                binding.tvIngresos.text = "B/. %.2f".format(data.ingresos)
                dibujarGrafico(data.datos)
                binding.progressFinanzas.visibility = View.GONE
            },
            onError = { msg ->
                binding.progressFinanzas.visibility = View.GONE
                Snackbar.make(binding.root, "⚠ $msg", Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun dibujarGrafico(datos: List<FinanzasMensual>) {
        val chart = binding.chartFinanzas
        val labels = datos.map { it.etiqueta }

        val entries = datos.mapIndexed { i, d -> BarEntry(i.toFloat(), d.ingresos.toFloat()) }

        val dataset = BarDataSet(entries, "Ingresos").apply {
            color = Color.parseColor("#2DC66B")
            valueTextSize = 0f
            highLightColor = Color.parseColor("#1A8A45")
            highLightAlpha = 120
        }

        val barData = BarData(dataset).apply { barWidth = 0.55f }
        chart.data = barData

        val marker = ChartMarkerView(
            context = requireContext(),
            labels = labels,
            valueTitle = filtroActual.tooltipValueTitle,
            labelFormatter = { label ->
                if (filtroActual.expandirDiasTooltip) {
                    EXPANSION_DIAS[label] ?: label
                } else {
                    label
                }
            }
        )
        marker.chartView = chart
        chart.marker = marker

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textSize = 10f
            textColor = Color.parseColor("#9E9E9E")
            setCenterAxisLabels(false)
            axisMinimum = -0.5f
            axisMaximum = datos.size.toFloat() - 0.5f
        }
        chart.axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(true)
            gridColor = Color.parseColor("#F0F0F0")
            textColor = Color.parseColor("#9E9E9E")
            textSize = 10f
        }
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setPinchZoom(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.setScaleEnabled(false)

        chart.setFitBars(true)
        chart.animateY(700)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val EXPANSION_DIAS = mapOf(
            "Dom" to "Domingo", "Lun" to "Lunes", "Mar" to "Martes",
            "Mié" to "Miércoles", "Mie" to "Miércoles", "Jue" to "Jueves",
            "Vie" to "Viernes", "Sáb" to "Sábado", "Sab" to "Sábado",
            "Sun" to "Sunday", "Mon" to "Monday", "Tue" to "Tuesday",
            "Wed" to "Wednesday", "Thu" to "Thursday", "Fri" to "Friday", "Sat" to "Saturday"
        )
    }
}
