package com.david.tiendavirtual.data.model

import java.io.Serializable

data class EmpleadoAdmin(
    val usuario:  String,
    val nombre:   String,
    val apellido: String,
    val rol:      Int       // 1 = ADMIN, 2 = EMPLEADO
) : Serializable {
    val nombreCompleto get() = "$nombre $apellido".trim()
    val rolNombre      get() = if (rol == 1) "ADMIN" else "EMPLEADO"
    val isAdmin        get() = rol == 1
}

