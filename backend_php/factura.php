<?php
/**
 * factura.php
 * Crea una factura completa con transacción:
 *  1. Valida saldo de la tarjeta
 *  2. Inserta en `factura`
 *  3. Inserta cada línea en `factura_detalle`
 *  4. Descuenta stock en `productos`
 *  5. Descuenta saldo en `tarjeta`
 *  Si cualquier paso falla → ROLLBACK completo
 *
 * POST params:
 *   idTarjeta  int
 *   subtotal   decimal
 *   itbms      decimal
 *   total      decimal
 *   detalles   JSON array: [{idProducto, cantidad, precio_unitario}, ...]
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$idTarjeta = intval($_POST["idTarjeta"] ?? 0);
$subtotal  = floatval($_POST["subtotal"] ?? 0);
$itbms     = floatval($_POST["itbms"]   ?? 0);
$total     = floatval($_POST["total"]   ?? 0);
$detalles  = json_decode($_POST["detalles"] ?? "[]", true);

if ($idTarjeta === 0 || empty($detalles) || $total <= 0) {
    echo json_encode(["ok" => false, "mensaje" => "Datos de compra incompletos"]);
    exit;
}

// ── Validar saldo suficiente ──────────────────────────────────────────────
$stmtSaldo = mysqli_prepare($conexion, "SELECT saldo FROM tarjeta WHERE idTarjeta = ? LIMIT 1");
mysqli_stmt_bind_param($stmtSaldo, "i", $idTarjeta);
mysqli_stmt_execute($stmtSaldo);
$rowSaldo = mysqli_fetch_assoc(mysqli_stmt_get_result($stmtSaldo));

if (!$rowSaldo) {
    echo json_encode(["ok" => false, "mensaje" => "Tarjeta no encontrada"]);
    exit;
}
if (floatval($rowSaldo["saldo"]) < $total) {
    echo json_encode(["ok" => false, "mensaje" => "Saldo insuficiente en la tarjeta"]);
    exit;
}

// ── Transacción ───────────────────────────────────────────────────────────
mysqli_begin_transaction($conexion);

try {
    // 1. Insertar cabecera de factura
    $stmtFac = mysqli_prepare(
        $conexion,
        "INSERT INTO factura (idTarjeta, subtotal, itbms, total, fecha)
         VALUES (?, ?, ?, ?, NOW())"
    );
    mysqli_stmt_bind_param($stmtFac, "iddd", $idTarjeta, $subtotal, $itbms, $total);
    if (!mysqli_stmt_execute($stmtFac)) {
        throw new Exception("Error al crear la factura: " . mysqli_error($conexion));
    }
    $idFactura = intval(mysqli_insert_id($conexion));

    // 2. Insertar detalles y descontar stock
    foreach ($detalles as $det) {
        $idProducto = intval($det["idProducto"]     ?? 0);
        $cantidad   = intval($det["cantidad"]        ?? 0);
        $precioUnit = floatval($det["precio_unitario"] ?? 0);

        if ($idProducto <= 0 || $cantidad <= 0) {
            throw new Exception("Detalle de producto inválido");
        }

        // Insertar detalle
        $stmtDet = mysqli_prepare(
            $conexion,
            "INSERT INTO factura_detalle (idFactura, idProducto, cantidad, precio_unitario)
             VALUES (?, ?, ?, ?)"
        );
        mysqli_stmt_bind_param($stmtDet, "iiid", $idFactura, $idProducto, $cantidad, $precioUnit);
        if (!mysqli_stmt_execute($stmtDet)) {
            throw new Exception("Error al insertar detalle del producto $idProducto");
        }

        // Descontar stock (solo si hay suficiente)
        $stmtStock = mysqli_prepare(
            $conexion,
            "UPDATE productos SET stock = stock - ? WHERE idProducto = ? AND stock >= ?"
        );
        mysqli_stmt_bind_param($stmtStock, "iii", $cantidad, $idProducto, $cantidad);
        if (!mysqli_stmt_execute($stmtStock) || mysqli_affected_rows($conexion) === 0) {
            throw new Exception("Stock insuficiente para el producto #$idProducto");
        }
    }

    // 3. Descontar saldo de la tarjeta
    $stmtDescontar = mysqli_prepare(
        $conexion,
        "UPDATE tarjeta SET saldo = saldo - ? WHERE idTarjeta = ?"
    );
    mysqli_stmt_bind_param($stmtDescontar, "di", $total, $idTarjeta);
    if (!mysqli_stmt_execute($stmtDescontar)) {
        throw new Exception("Error al actualizar el saldo de la tarjeta");
    }

    mysqli_commit($conexion);
    echo json_encode(["ok" => true, "idFactura" => $idFactura]);

} catch (Exception $e) {
    mysqli_rollback($conexion);
    echo json_encode(["ok" => false, "mensaje" => $e->getMessage()]);
}

