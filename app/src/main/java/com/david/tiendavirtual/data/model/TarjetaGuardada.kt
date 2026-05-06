package com.david.tiendavirtual.data.model

data class TarjetaGuardada(
    val idTarjeta: Int,
    val tipo: String,          // VISA, MASTERCARD, etc.
    val ultimos4: String,      // últimos 4 dígitos
    val digitos: String,       // número completo (16 dígitos)
    val fechaVence: String,    // YYYY-MM-DD
    val saldo: Double
)

