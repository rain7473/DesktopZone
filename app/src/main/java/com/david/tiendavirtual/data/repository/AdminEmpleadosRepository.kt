package com.david.tiendavirtual.data.repository

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.david.tiendavirtual.data.model.EmpleadoAdmin
import com.david.tiendavirtual.data.model.PedidoDB
import com.david.tiendavirtual.data.model.PedidoItemDB
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.network.ApiConfig
import org.json.JSONArray
import org.json.JSONObject

class AdminEmpleadosRepository(context: Context) {

    private val queue = Volley.newRequestQueue(context.applicationContext)

    // ── Listar empleados ──────────────────────────────────────────────────
    fun listar(
        onSuccess: (List<EmpleadoAdmin>) -> Unit,
        onError:   (String) -> Unit
    ) {
        val url = "${ApiConfig.ADMIN_EMPLEADOS}?usuario=${UserSession.usuario}"
        val req = object : StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val j = JSONObject(response)
                    if (!j.getBoolean("ok")) {
                        onError(j.optString("mensaje"))
                    } else {
                        val arr  = j.getJSONArray("empleados")
                        val list = (0 until arr.length()).map { i ->
                            val e = arr.getJSONObject(i)
                            EmpleadoAdmin(
                                usuario  = e.getString("usuario"),
                                nombre   = e.getString("nombre"),
                                apellido = e.optString("apellido", ""),
                                rol      = e.getInt("rol")
                            )
                        }
                        onSuccess(list)
                    }
                } catch (e: Exception) { onError("Error: ${e.message}") }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {}
        queue.add(req)
    }

    // ── Agregar empleado ──────────────────────────────────────────────────
    fun agregar(
        usuario:    String, nombre: String, apellido: String,
        rol:        Int,    contrasena: String,
        onSuccess:  (String) -> Unit,
        onError:    (String) -> Unit
    ) = ejecutar(
        mapOf("action" to "agregar", "usuario" to UserSession.usuario,
              "nuevoUsuario" to usuario, "nombre" to nombre, "apellido" to apellido,
              "rol" to rol.toString(), "contrasena" to contrasena),
        onSuccess, onError
    )

    // ── Editar empleado ───────────────────────────────────────────────────
    fun editar(
        usuario:   String, nombre: String, apellido: String,
        rol:       Int,    contrasena: String,
        onSuccess: (String) -> Unit,
        onError:   (String) -> Unit
    ) = ejecutar(
        mapOf("action" to "editar", "usuario" to UserSession.usuario,
              "nuevoUsuario" to usuario, "nombre" to nombre, "apellido" to apellido,
              "rol" to rol.toString(), "contrasena" to contrasena),
        onSuccess, onError
    )

    // ── Eliminar empleado ─────────────────────────────────────────────────
    fun eliminar(
        usuarioTarget: String,
        onSuccess: (String) -> Unit,
        onError:   (String) -> Unit
    ) = ejecutar(
        mapOf("action" to "eliminar", "usuario" to UserSession.usuario,
              "nuevoUsuario" to usuarioTarget, "usuarioActivo" to UserSession.usuario),
        onSuccess, onError
    )

    private fun ejecutar(
        params:    Map<String, String>,
        onSuccess: (String) -> Unit,
        onError:   (String) -> Unit
    ) {
        val req = object : StringRequest(Request.Method.POST, ApiConfig.ADMIN_EMPLEADOS,
            { response ->
                try {
                    val j = JSONObject(response)
                    if (j.getBoolean("ok")) onSuccess(j.optString("mensaje", "OK"))
                    else onError(j.optString("mensaje", "Error"))
                } catch (e: Exception) { onError("Error: ${e.message}") }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {
            override fun getParams() = params
        }
        queue.add(req)
    }

    // ── Ventas para el admin ──────────────────────────────────────────────
    fun obtenerVentas(
        onSuccess: (List<PedidoDB>) -> Unit,
        onError:   (String) -> Unit
    ) {
        val url = "${ApiConfig.ADMIN_VENTAS}?usuario=${UserSession.usuario}"
        val req = object : StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val j = JSONObject(response)
                    if (!j.getBoolean("ok")) {
                        onError(j.optString("mensaje"))
                    } else {
                        val arr  = j.getJSONArray("ventas")
                        val list = (0 until arr.length()).map { i ->
                            val obj  = arr.getJSONObject(i)
                            val dArr = obj.getJSONArray("detalles")
                            val dets = (0 until dArr.length()).map { k ->
                                val d = dArr.getJSONObject(k)
                                PedidoItemDB(d.getInt("idFacDet"), d.getLong("idProducto"),
                                    d.getInt("cantidad"), d.getDouble("precio_unitario"),
                                    d.getString("nombre"))
                            }
                            PedidoDB(obj.getInt("idFactura"), obj.optString("fecha", ""),
                                obj.getDouble("subtotal"), obj.getDouble("itbms"),
                                obj.getDouble("total"), dets)
                        }
                        onSuccess(list)
                    }
                } catch (e: Exception) { onError("Error: ${e.message}") }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {}
        queue.add(req)
    }
}

