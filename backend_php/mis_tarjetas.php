<?php
/**
 * mis_tarjetas.php
 * Devuelve todas las tarjetas registradas en el sistema (últimas 10 usadas).
 * GET: sin parámetros
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$stmt = mysqli_prepare(
    $conexion,
    "SELECT idTarjeta, tipo, digitos, fechaVence, saldo
     FROM tarjeta
     ORDER BY idTarjeta DESC
     LIMIT 10"
);
mysqli_stmt_execute($stmt);
$res  = mysqli_stmt_get_result($stmt);

$tarjetas = [];
while ($fila = mysqli_fetch_assoc($res)) {
    $tarjetas[] = [
        "idTarjeta"  => intval($fila["idTarjeta"]),
        "tipo"       => $fila["tipo"],
        "ultimos4"   => substr($fila["digitos"], -4),
        "digitos"    => $fila["digitos"],
        "fechaVence" => $fila["fechaVence"],  // YYYY-MM-DD
        "saldo"      => floatval($fila["saldo"])
    ];
}
mysqli_stmt_close($stmt);

echo json_encode($tarjetas);

