package com.david.tiendavirtual.data.repository

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.david.tiendavirtual.data.model.Categoria
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.network.ApiConfig
import org.json.JSONObject

/**
 * AdminCategoriaRepository
 * Gestiona las operaciones CRUD de categorías contra admin_categoria.php.
 * Todas las acciones requieren rol Administrador (verificado también en el backend).
 */
class AdminCategoriaRepository(context: Context) {

    private val queue = Volley.newRequestQueue(context.applicationContext)

    // ── Listar categorías ─────────────────────────────────────────────────
    fun listar(
        onSuccess: (List<Categoria>) -> Unit,
        onError:   (String) -> Unit
    ) {
        val url = "${ApiConfig.ADMIN_CATEGORIA}?action=listar&usuario=${UserSession.usuario}"
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("ok")) {
                        val arr = json.getJSONArray("categorias")
                        val lista = (0 until arr.length()).map { i ->
                            val o = arr.getJSONObject(i)
                            Categoria(o.getInt("id"), o.getString("nombre"))
                        }
                        onSuccess(lista)
                    } else {
                        onError(json.optString("mensaje", "Error desconocido"))
                    }
                } catch (e: Exception) {
                    onError("Error al leer respuesta: ${e.message}")
                }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        )
        queue.add(request)
    }

    // ── Agregar categoría ─────────────────────────────────────────────────
    fun agregar(
        nombre:    String,
        onSuccess: (String, Int) -> Unit,   // mensaje, nuevo id
        onError:   (String) -> Unit
    ) {
        ejecutar(
            params    = mapOf("action" to "agregar", "usuario" to UserSession.usuario, "nombre" to nombre),
            onSuccess = { json ->
                onSuccess(
                    json.optString("mensaje", "Categoría agregada"),
                    json.optInt("id", 0)
                )
            },
            onError = onError
        )
    }

    // ── Editar categoría ──────────────────────────────────────────────────
    fun editar(
        id:        Int,
        nombre:    String,
        onSuccess: (String) -> Unit,
        onError:   (String) -> Unit
    ) {
        ejecutar(
            params    = mapOf("action" to "editar", "usuario" to UserSession.usuario,
                              "id" to id.toString(), "nombre" to nombre),
            onSuccess = { json -> onSuccess(json.optString("mensaje", "Categoría actualizada")) },
            onError   = onError
        )
    }

    // ── Eliminar categoría ────────────────────────────────────────────────
    fun eliminar(
        id:        Int,
        onSuccess: (String) -> Unit,
        onError:   (String) -> Unit
    ) {
        ejecutar(
            params    = mapOf("action" to "eliminar", "usuario" to UserSession.usuario,
                              "id" to id.toString()),
            onSuccess = { json -> onSuccess(json.optString("mensaje", "Categoría eliminada")) },
            onError   = onError
        )
    }

    // ── Helper POST ───────────────────────────────────────────────────────
    private fun ejecutar(
        params:    Map<String, String>,
        onSuccess: (JSONObject) -> Unit,
        onError:   (String) -> Unit
    ) {
        val request = object : StringRequest(
            Method.POST,
            ApiConfig.ADMIN_CATEGORIA,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("ok")) onSuccess(json)
                    else onError(json.optString("mensaje", "Error desconocido"))
                } catch (e: Exception) {
                    onError("Respuesta inesperada: ${e.message}")
                }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {
            override fun getParams() = params
        }
        queue.add(request)
    }
}

