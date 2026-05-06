package com.david.tiendavirtual.data.repository

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.david.tiendavirtual.data.model.CarritoItem
import com.david.tiendavirtual.data.model.PedidoDB
import com.david.tiendavirtual.data.model.PedidoItemDB
import com.david.tiendavirtual.data.model.TarjetaGuardada
import com.david.tiendavirtual.data.network.ApiConfig
import org.json.JSONArray
import org.json.JSONObject

/**
 * FacturaRepository
 * Maneja toda la comunicación con el backend para:
 *  - Buscar / crear una tarjeta          → tarjeta.php
 *  - Crear una factura con transacción   → factura.php
 *  - Obtener el historial de pedidos     → pedidos.php
 */
class FacturaRepository(context: Context) {

    private val queue = Volley.newRequestQueue(context.applicationContext)

    // ─────────────────────────────────────────────────────────────────────
    // 1. Obtener o crear tarjeta
    // ─────────────────────────────────────────────────────────────────────
    fun obtenerOCrearTarjeta(
        digitos:      String,
        codSeguridad: String,
        fechaVence:   String,          // Formato MM/AA
        tipo:         String = "VISA",
        onSuccess:    (idTarjeta: Int, saldo: Double) -> Unit,
        onError:      (String) -> Unit
    ) {
        val request = object : StringRequest(
            Request.Method.POST,
            ApiConfig.TARJETA,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("ok")) {
                        onSuccess(
                            json.getInt("idTarjeta"),
                            json.getDouble("saldo")
                        )
                    } else {
                        onError(json.optString("mensaje", "Error al procesar la tarjeta"))
                    }
                } catch (e: Exception) {
                    onError("Respuesta inesperada del servidor (tarjeta)")
                }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {
            override fun getParams() = mapOf(
                "digitos"      to digitos,
                "codSeguridad" to codSeguridad,
                "fechaVence"   to fechaVence,
                "tipo"         to tipo
            )
        }
        queue.add(request)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. Crear factura + detalles + descontar stock (transacción en servidor)
    // ─────────────────────────────────────────────────────────────────────
    fun crearFactura(
        idTarjeta: Int,
        subtotal:  Double,
        itbms:     Double,
        total:     Double,
        items:     List<CarritoItem>,
        onSuccess: (idFactura: Int) -> Unit,
        onError:   (String) -> Unit
    ) {
        // Construir JSON de detalles
        val detalles = JSONArray()
        items.forEach { item ->
            detalles.put(JSONObject().apply {
                put("idProducto",     item.producto.idProducto)
                put("cantidad",       item.cantidad)
                put("precio_unitario", item.producto.precioVenta)
            })
        }

        val request = object : StringRequest(
            Request.Method.POST,
            ApiConfig.FACTURA,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("ok")) {
                        onSuccess(json.getInt("idFactura"))
                    } else {
                        onError(json.optString("mensaje", "Error al procesar la compra"))
                    }
                } catch (e: Exception) {
                    onError("Respuesta inesperada del servidor (factura)")
                }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {
            override fun getParams() = mapOf(
                "idTarjeta" to idTarjeta.toString(),
                "subtotal"  to subtotal.toString(),
                "itbms"     to itbms.toString(),
                "total"     to total.toString(),
                "detalles"  to detalles.toString()
            )
        }
        queue.add(request)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. Obtener historial de pedidos desde la BD
    // ─────────────────────────────────────────────────────────────────────
    fun obtenerPedidos(
        onSuccess: (List<PedidoDB>) -> Unit,
        onError:   (String) -> Unit
    ) {
        val request = object : StringRequest(
            Request.Method.GET,
            ApiConfig.PEDIDOS,
            { response ->
                try {
                    val arr     = JSONArray(response)
                    val pedidos = mutableListOf<PedidoDB>()

                    for (i in 0 until arr.length()) {
                        val obj      = arr.getJSONObject(i)
                        val detArr   = obj.getJSONArray("detalles")
                        val detalles = mutableListOf<PedidoItemDB>()

                        for (j in 0 until detArr.length()) {
                            val det = detArr.getJSONObject(j)
                            detalles.add(
                                PedidoItemDB(
                                    idFacDet       = det.getInt("idFacDet"),
                                    idProducto     = det.getLong("idProducto"),
                                    cantidad       = det.getInt("cantidad"),
                                    precioUnitario = det.getDouble("precio_unitario"),
                                    nombre         = det.getString("nombre")
                                )
                            )
                        }

                        pedidos.add(
                            PedidoDB(
                                idFactura = obj.getInt("idFactura"),
                                fecha     = obj.optString("fecha", ""),
                                subtotal  = obj.getDouble("subtotal"),
                                itbms     = obj.getDouble("itbms"),
                                total     = obj.getDouble("total"),
                                detalles  = detalles
                            )
                        )
                    }
                    onSuccess(pedidos)
                } catch (e: Exception) {
                    onError("Error al leer pedidos: ${e.message}")
                }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {}
        queue.add(request)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. Obtener tarjetas guardadas en el sistema
    // ─────────────────────────────────────────────────────────────────────
    fun obtenerTarjetasGuardadas(
        onSuccess: (List<TarjetaGuardada>) -> Unit,
        onError:   (String) -> Unit
    ) {
        val request = object : StringRequest(
            Request.Method.GET,
            ApiConfig.MIS_TARJETAS,
            { response ->
                try {
                    val arr = JSONArray(response)
                    val lista = mutableListOf<TarjetaGuardada>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        // Convertir fechaVence de YYYY-MM-DD → MM/AA
                        val fechaRaw = obj.optString("fechaVence", "")
                        val fechaMes = try {
                            val parts = fechaRaw.split("-")
                            "${parts[1]}/${parts[0].takeLast(2)}"
                        } catch (e: Exception) { fechaRaw }
                        lista.add(
                            TarjetaGuardada(
                                idTarjeta = obj.getInt("idTarjeta"),
                                tipo      = obj.optString("tipo", "VISA"),
                                ultimos4  = obj.optString("ultimos4", "****"),
                                digitos   = obj.optString("digitos", ""),
                                fechaVence = fechaMes,
                                saldo     = obj.getDouble("saldo")
                            )
                        )
                    }
                    onSuccess(lista)
                } catch (e: Exception) {
                    onError("Error al leer tarjetas: ${e.message}")
                }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {}
        queue.add(request)
    }
}



