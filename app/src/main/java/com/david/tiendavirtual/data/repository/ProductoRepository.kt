package com.david.tiendavirtual.data.repository

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.data.network.ApiConfig
import com.david.tiendavirtual.utils.Constants
import org.json.JSONArray

class ProductoRepository(context: Context) {
    private val queue = Volley.newRequestQueue(context.applicationContext)

    fun obtenerProductos(onSuccess: (List<Producto>) -> Unit, onError: (String) -> Unit) {
        val request = JsonArrayRequest(
            Request.Method.GET,
            ApiConfig.PRODUCTOS,
            null,
            { response -> onSuccess(parseProductos(response)) },
            { error -> onError(error.message ?: "No se pudieron cargar los productos") }
        )
        queue.add(request)
    }

    private fun parseProductos(array: JSONArray): List<Producto> {
        val productos = mutableListOf<Producto>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            // Construir URL completa de imagen
            val imagenRaw = obj.optString("imagen", "").trim()
            val imagenUrl = when {
                imagenRaw.isEmpty()            -> null
                imagenRaw.startsWith("http")   -> imagenRaw
                else                           -> Constants.IMAGES_URL + imagenRaw
            }

            productos.add(
                Producto(
                    idProducto  = obj.getLong("idProducto"),
                    nombre      = obj.getString("nombre"),
                    unidad      = obj.getString("unidad"),
                    descripcion = obj.getString("descripcion"),
                    stock       = obj.getInt("stock"),
                    precioCosto = obj.getDouble("precioCosto"),
                    precioVenta = obj.getDouble("precioVenta"),
                    imagen      = imagenUrl,
                    idCategoria = obj.getInt("idCategoria"),
                    idMarca     = obj.getInt("idMarca"),
                    categoria   = obj.optString("categoria", ""),
                    marca       = obj.optString("marca", "")
                )
            )
        }
        return productos
    }
}
