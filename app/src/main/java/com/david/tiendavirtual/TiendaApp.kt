package com.david.tiendavirtual

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.david.tiendavirtual.ui.productos.FavoritosManager

class TiendaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar persistencia de favoritos
        FavoritosManager.init(this)
        // Restaurar preferencia de tema oscuro
        val prefs = getSharedPreferences("tienda_prefs", MODE_PRIVATE)
        val modoOscuro = prefs.getBoolean("modo_oscuro", false)
        AppCompatDelegate.setDefaultNightMode(
            if (modoOscuro) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}

