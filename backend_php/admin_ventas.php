<?php
/**
 * admin_ventas.php  –  Historial de ventas para el administrador (con auth).
 * GET param: usuario
 * Devuelve facturas con detalles, igual que pedidos.php pero con validación de rol.
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$usuario = trim($_GET["usuario"] ?? "");
if (!esAdmin($usuario, $conexion)) {
    echo json_encode(["ok" => false, "mensaje" => "Acceso denegado"]);
    exit;
}

$sql = "
    SELECT
        f.idFactura, f.fecha, f.subtotal, f.itbms, f.total,
        fd.idFacDet, fd.idProducto, fd.cantidad, fd.precio_unitario,
        p.nombre AS nombreProducto
    FROM factura f
    INNER JOIN factura_detalle fd ON fd.idFactura = f.idFactura
    INNER JOIN productos p        ON p.idProducto  = fd.idProducto
    ORDER BY f.idFactura DESC, fd.idFacDet ASC";

$res = mysqli_query($conexion, $sql);
if (!$res) {
    http_response_code(500);
    echo json_encode(["ok" => false, "mensaje" => mysqli_error($conexion)]);
    exit;
}

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

echo json_encode(["ok" => true, "ventas" => array_values($facturas)], JSON_UNESCAPED_UNICODE);

function esAdmin(string $usr, $conn): bool {
    if ($usr === "") return false;
    $s = mysqli_prepare($conn, "SELECT rol FROM empleado WHERE usuario = ? LIMIT 1");
    mysqli_stmt_bind_param($s, "s", $usr);
    mysqli_stmt_execute($s);
    $r = mysqli_fetch_assoc(mysqli_stmt_get_result($s));
    return $r && intval($r["rol"]) === 1;
}

