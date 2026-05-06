package com.david.tiendavirtual.data.model

data class FinanzasData(
    val ingresos: Double,
    val egresos:  Double,
    val utilidad: Double,
    val datos:    List<FinanzasMensual>
)

data class FinanzasMensual(
    val etiqueta: String,
    val ingresos: Double,
    val egresos:  Double,
    val utilidad: Double
)

