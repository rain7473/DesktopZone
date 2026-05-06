package com.david.tiendavirtual.data.repository

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.david.tiendavirtual.data.model.DashboardData
import com.david.tiendavirtual.data.model.FinanzasData
import com.david.tiendavirtual.data.model.FinanzasMensual
import com.david.tiendavirtual.data.model.GraficoItem
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.network.ApiConfig
import org.json.JSONObject

class AdminDashboardRepository(context: Context) {

    private val queue = Volley.newRequestQueue(context.applicationContext)

    // ── Dashboard KPIs ────────────────────────────────────────────────────
    fun obtenerDashboard(
        periodo:   Int = 7,
        onSuccess: (DashboardData) -> Unit,
        onError:   (String) -> Unit
    ) {
        val url = "${ApiConfig.ADMIN_DASHBOARD}?usuario=${UserSession.usuario}&periodo=$periodo"
        val req = object : StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val j = JSONObject(response)
                    if (!j.getBoolean("ok")) {
                        onError(j.optString("mensaje"))
                    } else {
                        val grafArr = j.getJSONArray("graficoSemana")
                        val grafico = (0 until grafArr.length()).map { i ->
                            val g = grafArr.getJSONObject(i)
                            GraficoItem(g.getString("etiqueta"), g.getDouble("ingresos"), g.getDouble("egresos"), g.optDouble("ingresosPasados", 0.0))
                        }
                        onSuccess(DashboardData(
                            ventasHoy       = j.getDouble("ventasHoy"),
                            ventasMes       = j.getDouble("ventasMes"),
                            totalPedidosMes = j.getInt("totalPedidosMes"),
                            totalProductos  = j.getInt("totalProductos"),
                            bajoStock       = j.getInt("bajoStock"),
                            agotados        = j.getInt("agotados"),
                            graficoSemana   = grafico
                        ))
                    }
                } catch (e: Exception) { onError("Error al procesar dashboard: ${e.message}") }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {}
        queue.add(req)
    }

    // ── Finanzas ──────────────────────────────────────────────────────────
    fun obtenerFinanzas(
        filtro:    String = "mes",      // mes | 3meses | anio
        onSuccess: (FinanzasData) -> Unit,
        onError:   (String) -> Unit
    ) {
        val url = "${ApiConfig.ADMIN_FINANZAS}?usuario=${UserSession.usuario}&filtro=$filtro"
        val req = object : StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val j = JSONObject(response)
                    if (!j.getBoolean("ok")) {
                        onError(j.optString("mensaje"))
                    } else {
                        val arr   = j.getJSONArray("datos")
                        val datos = (0 until arr.length()).map { i ->
                            val d = arr.getJSONObject(i)
                            FinanzasMensual(
                                etiqueta = d.getString("etiqueta"),
                                ingresos = d.getDouble("ingresos"),
                                egresos  = d.getDouble("egresos"),
                                utilidad = d.getDouble("utilidad")
                            )
                        }
                        onSuccess(FinanzasData(
                            ingresos = j.getDouble("ingresos"),
                            egresos  = j.getDouble("egresos"),
                            utilidad = j.getDouble("utilidad"),
                            datos    = datos
                        ))
                    }
                } catch (e: Exception) { onError("Error al procesar finanzas: ${e.message}") }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {}
        queue.add(req)
    }
}
