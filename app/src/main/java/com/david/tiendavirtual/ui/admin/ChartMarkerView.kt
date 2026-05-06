package com.david.tiendavirtual.ui.admin

import android.content.Context
import android.widget.TextView
import com.david.tiendavirtual.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class ChartMarkerView(
    context: Context,
    layoutResource: Int = R.layout.layout_chart_marker,
    private val labels: List<String> = emptyList(),
    private val prefix: String = "B/. ",
    private val dataPasada: List<Double> = emptyList()
) : MarkerView(context, layoutResource) {

    private val tvLabel: TextView       = findViewById(R.id.tvMarkerLabel)
    private val tvValue: TextView       = findViewById(R.id.tvMarkerValue)
    private val tvValuePasada: TextView = findViewById(R.id.tvMarkerValuePasada)

    private val diasCompletos = mapOf(
        "Dom" to "Domingo", "Lun" to "Lunes", "Mar" to "Martes",
        "Mié" to "Miércoles", "Jue" to "Jueves", "Vie" to "Viernes", "Sáb" to "Sábado"
    )

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val idx    = e?.x?.toInt() ?: 0
        val abrev  = if (idx in labels.indices) labels[idx] else ""
        val nombre = diasCompletos[abrev] ?: abrev

        tvLabel.text = nombre
        tvValue.text = "Semana actual: $prefix%,.0f".format(e?.y ?: 0f)

        if (dataPasada.isNotEmpty() && idx in dataPasada.indices) {
            tvValuePasada.visibility = TextView.VISIBLE
            tvValuePasada.text = "Semana pasada: $prefix%,.0f".format(dataPasada[idx])
        } else {
            tvValuePasada.visibility = TextView.GONE
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat() - 10f)
    }
}
