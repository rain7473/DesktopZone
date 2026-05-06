package com.david.tiendavirtual.ui.productos

import android.content.Context
import com.david.tiendavirtual.data.model.Producto
import org.json.JSONArray
import org.json.JSONObject

object FavoritosManager {
    private val ids       = mutableSetOf<Long>()
    private val productos = mutableListOf<Producto>()
    private var appContext: Context? = null
    private const val PREFS = "tienda_prefs"
    private const val KEY   = "favoritos_json"

    fun init(context: Context) {
        appContext = context.applicationContext
        cargarDesdePrefs()
    }

    fun alternar(producto: Producto) {
        if (ids.contains(producto.idProducto)) {
            ids.remove(producto.idProducto)
            productos.removeAll { it.idProducto == producto.idProducto }
        } else {
            ids.add(producto.idProducto)
            productos.add(producto)
        }
        guardarEnPrefs()
    }

    fun esFavorito(idProducto: Long) = ids.contains(idProducto)

    fun obtenerFavoritos(): List<Producto> = productos.toList()

    fun cantidad() = ids.size

    // ── Persistencia ──────────────────────────────────────────────────────

    private fun guardarEnPrefs() {
        val ctx = appContext ?: return
        val arr = JSONArray()
        productos.forEach { p ->
            arr.put(JSONObject().apply {
                put("idProducto",  p.idProducto)
                put("nombre",      p.nombre)
                put("unidad",      p.unidad)
                put("descripcion", p.descripcion)
                put("stock",       p.stock)
                put("precioCosto", p.precioCosto)
                put("precioVenta", p.precioVenta)
                put("imagen",      p.imagen ?: "")
                put("idCategoria", p.idCategoria)
                put("idMarca",     p.idMarca)
                put("categoria",   p.categoria ?: "")
                put("marca",       p.marca ?: "")
            })
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    private fun cargarDesdePrefs() {
        val ctx = appContext ?: return
        val json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        ids.clear(); productos.clear()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val p = Producto(
                idProducto  = o.getLong("idProducto"),
                nombre      = o.getString("nombre"),
                unidad      = o.getString("unidad"),
                descripcion = o.getString("descripcion"),
                stock       = o.getInt("stock"),
                precioCosto = o.getDouble("precioCosto"),
                precioVenta = o.getDouble("precioVenta"),
                imagen      = o.getString("imagen").takeIf { it.isNotEmpty() },
                idCategoria = o.getInt("idCategoria"),
                idMarca     = o.getInt("idMarca"),
                categoria   = o.getString("categoria").takeIf { it.isNotEmpty() },
                marca       = o.getString("marca").takeIf { it.isNotEmpty() }
            )
            ids.add(p.idProducto)
            productos.add(p)
        }
    }
}
