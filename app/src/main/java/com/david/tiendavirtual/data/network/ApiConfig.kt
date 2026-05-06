package com.david.tiendavirtual.data.network

import com.david.tiendavirtual.utils.Constants

object ApiConfig {
    const val LOGIN           = Constants.BASE_URL + "login.php"
    const val REGISTER        = Constants.BASE_URL + "register.php"
    const val PRODUCTOS       = Constants.BASE_URL + "productos.php"
    const val TARJETA         = Constants.BASE_URL + "tarjeta.php"
    const val FACTURA         = Constants.BASE_URL + "factura.php"
    const val PEDIDOS         = Constants.BASE_URL + "pedidos.php"
    const val CATALOGO        = Constants.BASE_URL + "catalogo.php"
    const val ADMIN_PRODUCTO  = Constants.BASE_URL + "admin_producto.php"
    const val ADMIN_DASHBOARD = Constants.BASE_URL + "admin_dashboard.php"
    const val ADMIN_FINANZAS  = Constants.BASE_URL + "admin_finanzas.php"
    const val ADMIN_EMPLEADOS = Constants.BASE_URL + "admin_empleados.php"
    const val ADMIN_VENTAS    = Constants.BASE_URL + "admin_ventas.php"
    const val ADMIN_CATEGORIA = Constants.BASE_URL + "admin_categoria.php"
    const val MIS_TARJETAS    = Constants.BASE_URL + "mis_tarjetas.php"
}
