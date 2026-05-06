<?php
/**
 * pedidos.php
 * Devuelve el historial de facturas con sus detalles y nombres de productos.
 * GET → JSON array de objetos factura, cada uno con array "detalles".
 *
 * Respuesta ejemplo:
 * [
 *   {
 *     "idFactura": 1,
 *     "fecha": "2026-04-06 14:32:00",
 *     "subtotal": 100.0,
 *     "itbms": 7.0,
 *     "total": 107.0,
 *     "detalles": [
 *       { "idFacDet": 1, "idProducto": 7701010100010, "cantidad": 2,
 *         "precio_unitario": 149.90, "nombre": "Kingston KC600 512GB" }
 *     ]
 *   }
 * ]
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$sql = "
    SELECT
        f.idFactura,
        f.fecha,
        f.subtotal,
        f.itbms,
        f.total,
        fd.idFacDet,
        fd.idProducto,
        fd.cantidad,
        fd.precio_unitario,
        p.nombre AS nombreProducto
    FROM factura f
    INNER JOIN factura_detalle fd ON fd.idFactura = f.idFactura
    INNER JOIN productos p        ON p.idProducto  = fd.idProducto
    ORDER BY f.idFactura DESC, fd.idFacDet ASC
";

$res = mysqli_query($conexion, $sql);
if (!$res) {
    http_response_code(500);
    echo json_encode(["error" => mysqli_error($conexion)]);
    exit;
}

// Agrupar filas por idFactura
$facturas = [];
while ($fila = mysqli_fetch_assoc($res)) {
    $id = intval($fila["idFactura"]);
    if (!isset($facturas[$id])) {
        $facturas[$id] = [
            "idFactura" => $id,
            "fecha"     => $fila["fecha"] ?? "",
            "subtotal"  => floatval($fila["subtotal"]),
            "itbms"     => floatval($fila["itbms"]),
            "total"     => floatval($fila["total"]),
            "detalles"  => []
        ];
    }
    $facturas[$id]["detalles"][] = [
        "idFacDet"        => intval($fila["idFacDet"]),
        "idProducto"      => intval($fila["idProducto"]),
        "cantidad"        => intval($fila["cantidad"]),
        "precio_unitario" => floatval($fila["precio_unitario"]),
        "nombre"          => $fila["nombreProducto"]
    ];
}

echo json_encode(array_values($facturas), JSON_UNESCAPED_UNICODE);

