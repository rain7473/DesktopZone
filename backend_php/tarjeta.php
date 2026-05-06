<?php
/**
 * tarjeta.php
 * Busca una tarjeta por número. Si no existe la crea con saldo demo.
 * POST params: digitos, codSeguridad, fechaVence (MM/AA), tipo
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$digitos      = trim($_POST["digitos"]      ?? "");
$codSeguridad = trim($_POST["codSeguridad"] ?? "");
$fechaVence   = trim($_POST["fechaVence"]   ?? "");   // MM/AA
$tipo         = strtoupper(trim($_POST["tipo"] ?? "VISA"));

// ── Validaciones básicas ──────────────────────────────────────────────────
if (strlen($digitos) !== 16 || !ctype_digit($digitos)) {
    echo json_encode(["ok" => false, "mensaje" => "Número de tarjeta inválido (debe tener 16 dígitos)"]);
    exit;
}

if (empty($codSeguridad)) {
    echo json_encode(["ok" => false, "mensaje" => "CVV requerido"]);
    exit;
}

// ── Buscar tarjeta existente por número ───────────────────────────────────
$stmt = mysqli_prepare($conexion, "SELECT idTarjeta, saldo FROM tarjeta WHERE digitos = ? LIMIT 1");
mysqli_stmt_bind_param($stmt, "s", $digitos);
mysqli_stmt_execute($stmt);
$res  = mysqli_stmt_get_result($stmt);
$fila = mysqli_fetch_assoc($res);
mysqli_stmt_close($stmt);

if ($fila) {
    // Tarjeta ya registrada → devolver su id y saldo actual
    echo json_encode([
        "ok"        => true,
        "idTarjeta" => intval($fila["idTarjeta"]),
        "saldo"     => floatval($fila["saldo"])
    ]);
    exit;
}

// ── No existe → crear con saldo demo generoso ──────────────
$fechaDate = "2030-12-01";
if (preg_match('/^(\d{2})\/(\d{2})$/', $fechaVence, $m)) {
    $anio      = intval("20" . $m[2]);
    $mes       = intval($m[1]);
    $fechaDate = sprintf("%04d-%02d-01", $anio, $mes);
}

$saldoDemo = 99999.99;
$ins = mysqli_prepare(
    $conexion,
    "INSERT INTO tarjeta (tipo, digitos, fechaVence, codSeguridad, saldo, saldoMaximo)
     VALUES (?, ?, ?, ?, ?, ?)"
);
mysqli_stmt_bind_param($ins, "ssssdd", $tipo, $digitos, $fechaDate, $codSeguridad, $saldoDemo, $saldoDemo);

if (mysqli_stmt_execute($ins)) {
    echo json_encode([
        "ok"        => true,
        "idTarjeta" => intval(mysqli_insert_id($conexion)),
        "saldo"     => $saldoDemo
    ]);
} else {
    echo json_encode(["ok" => false, "mensaje" => "Error al registrar la tarjeta: " . mysqli_error($conexion)]);
}

