package com.david.tiendavirtual.data.repository

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.david.tiendavirtual.data.model.Categoria
import com.david.tiendavirtual.data.model.Marca
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.network.ApiConfig
import org.json.JSONObject

/**
 * AdminProductoRepository
 * Realiza operaciones CRUD de productos contra admin_producto.php y
 * carga categorías/marcas desde catalogo.php.
 */
class AdminProductoRepository(context: Context) {

    private val queue = Volley.newRequestQueue(context.applicationContext)

    // ── Catálogo (categorías + marcas) ────────────────────────────────────
    fun obtenerCatalogo(
        onSuccess: (categorias: List<Categoria>, marcas: List<Marca>) -> Unit,
        onError:   (String) -> Unit
    ) {
        val request = object : StringRequest(
            Request.Method.GET,
            ApiConfig.CATALOGO,
            { response ->
                try {
                    val json  = JSONObject(response)
                    val catArr = json.getJSONArray("categorias")
                    val marArr = json.getJSONArray("marcas")

                    val categorias = (0 until catArr.length()).map { i ->
                        val o = catArr.getJSONObject(i)
                        Categoria(o.getInt("id"), o.getString("nombre"))
                    }
                    val marcas = (0 until marArr.length()).map { i ->
                        val o = marArr.getJSONObject(i)
                        Marca(o.getInt("id"), o.getString("nombre"))
                    }
                    onSuccess(categorias, marcas)
                } catch (e: Exception) {
                    onError("Error al leer catálogo: ${e.message}")
                }
            },
            { error -> onError(error.message ?: "Error de conexión") }
        ) {}
        queue.add(request)
    }

    // ── Agregar producto ──────────────────────────────────────────────────
    fun agregarProducto(
        idProducto:  String,
        nombre:      String,
        unidad:      String,
        descripcion: String,
        stock:       Int,
        precioCosto: Double,
        precioVenta: Double,
        imagen:      String,
        idCategoria: Int,
        idMarca:     Int,
        onSuccess:   (String) -> Unit,
        onError:     (String) -> Unit
    ) {
        ejecutarAccion(
            params = buildMap {
                put("action",      "agregar")
                put("usuario",     UserSession.usuario)
                put("idProducto",  idProducto)
                put("nombre",      nombre)
                put("unidad",      unidad)
                put("descripcion", descripcion)
                put("stock",       stock.toString())
                put("precioCosto", precioCosto.toString())
                put("precioVenta", precioVenta.toString())
                put("imagen",      imagen)
                put("idCategoria", idCategoria.toString())
                put("idMarca",     idMarca.toString())
            },
            onSuccess = onSuccess,
            onError   = onError
        )
    }

    // ── Editar producto ───────────────────────────────────────────────────
    fun editarProducto(
        producto:    Producto,
        nombre:      String,
        unidad:      String,
        descripcion: String,
        stock:       Int,
        precioCosto: Double,
        precioVenta: Double,
        imagen:      String,
        idCategoria: Int,
        idMarca:     Int,
        onSuccess:   (String) -> Unit,
        onError:     (String) -> Unit
    ) {
        ejecutarAccion(
            params = buildMap {
                put("action",      "editar")
                put("usuario",     UserSession.usuario)
                put("idProducto",  producto.idProducto.toString())
                put("nombre",      nombre)
                put("unidad",      unidad)
                put("descripcion", descripcion)
                put("stock",       stock.toString())
                put("precioCosto", precioCosto.toString())
                put("precioVenta", precioVenta.toString())
                put("imagen",      imagen)
                put("idCategoria", idCategoria.toString())
                put("idMarca",     idMarca.toString())
            },
            onSuccess = onSuccess,
            onError   = onError
        )
    }

    // ── Eliminar producto ─────────────────────────────────────────────────
    fun eliminarProducto(
        idProducto: Long,
        onSuccess:  (String) -> Unit,
        onError:    (String) -> Unit
    ) {
        ejecutarAccion(
            params = mapOf(
                "action"     to "eliminar",
                "usuario"    to UserSession.usuario,
                "idProducto" to idProducto.toString()
            ),
            onSuccess = onSuccess,
            onError   = onError
        )
    }

    // ── Helper interno ────────────────────────────────────────────────────
    private fun ejecutarAccion(
        params:    Map<String, String>,
        onSuccess: (String) -> Unit,
        onError:   (String) -> Unit
    ) {
        val request = object : StringRequest(
            Request.Method.POST,
            ApiConfig.ADMIN_PRODUCTO,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("ok")) {
                        onSuccess(json.optString("mensaje", "Operación exitosa"))
                    } else {
                        onError(json.optString("mensaje", "Error desconocido"))
                    }
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

