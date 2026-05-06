package com.david.tiendavirtual.data.model

object UserSession {
    var usuario: String = ""
    var nombre:   String = ""
    var apellido: String = ""
    var rol:      String = ""

    val nombreCompleto get() = if (nombre.isNotEmpty()) "$nombre $apellido".trim() else usuario
    val inicial get() = (nombre.firstOrNull() ?: usuario.firstOrNull() ?: 'U').uppercaseChar()

    /** true si el usuario tiene rol de Administrador (rol == "1") */
    val isAdmin: Boolean get() = rol == "1"
    /** true si el usuario tiene rol de Empleado (rol == "2") */
    val isEmpleado: Boolean get() = rol == "2"
    /** true si hay sesión activa (admin o empleado) */
    val isLoggedIn: Boolean get() = usuario.isNotEmpty()
    /** true si es invitado (sin login) */
    val isGuest: Boolean get() = !isLoggedIn

    fun limpiar() {
        usuario  = ""
        nombre   = ""
        apellido = ""
        rol      = ""
    }
}

