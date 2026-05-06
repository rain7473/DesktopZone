package com.david.tiendavirtual.utils

import android.content.Context

/**
 * Almacena localmente (SharedPreferences) los IDs de facturas generadas
 * en ESTE dispositivo, para que los invitados solo vean SUS pedidos.
 */
object LocalOrderStore {

    private const val PREFS_NAME = "local_orders"
    private const val KEY_IDS    = "order_ids"

    /** Guarda un nuevo idFactura asociado a este dispositivo. */
    fun guardarPedido(context: Context, idFactura: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val actuales = prefs.getStringSet(KEY_IDS, mutableSetOf()) ?: mutableSetOf()
        val nuevos   = actuales.toMutableSet().apply { add(idFactura.toString()) }
        prefs.edit().putStringSet(KEY_IDS, nuevos).apply()
    }

    /** Devuelve el conjunto de IDs de facturas de este dispositivo. */
    fun obtenerIds(context: Context): Set<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_IDS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    /** Limpia el historial local (útil al limpiar datos de la app). */
    fun limpiar(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}

