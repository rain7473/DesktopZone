package com.david.tiendavirtual.data.model

data class Marca(val id: Int, val nombre: String) {
    override fun toString() = nombre   // Spinner usa toString()
}

