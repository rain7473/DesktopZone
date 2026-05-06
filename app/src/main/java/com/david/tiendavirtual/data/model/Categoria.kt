package com.david.tiendavirtual.data.model

data class Categoria(val id: Int, val nombre: String) {
    override fun toString() = nombre   // Spinner usa toString()
}

