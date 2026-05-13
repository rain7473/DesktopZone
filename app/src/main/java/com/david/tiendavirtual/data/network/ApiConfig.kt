package com.david.tiendavirtual.data.network

import com.david.tiendavirtual.utils.Constants

object ApiConfig {
    val LOGIN           = Constants.BASE_URL + "login.php"
    val REGISTER        = Constants.BASE_URL + "register.php"
    val PRODUCTOS       = Constants.BASE_URL + "productos.php"
    val TARJETA         = Constants.BASE_URL + "tarjeta.php"
    val FACTURA         = Constants.BASE_URL + "factura.php"
    val PEDIDOS         = Constants.BASE_URL + "pedidos.php"
    val CATALOGO        = Constants.BASE_URL + "catalogo.php"
    val ADMIN_PRODUCTO  = Constants.BASE_URL + "admin_producto.php"
    val ADMIN_DASHBOARD = Constants.BASE_URL + "admin_dashboard.php"
    val ADMIN_FINANZAS  = Constants.BASE_URL + "admin_finanzas.php"
    val ADMIN_EMPLEADOS = Constants.BASE_URL + "admin_empleados.php"
    val ADMIN_VENTAS    = Constants.BASE_URL + "admin_ventas.php"
    val ADMIN_CATEGORIA = Constants.BASE_URL + "admin_categoria.php"
    val MIS_TARJETAS    = Constants.BASE_URL + "mis_tarjetas.php"
}
