<?php
/**
 * admin_finanzas.php  –  Datos financieros con filtros: mes | 3meses | anio
 * GET params: usuario, filtro
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$usuario = trim($_GET["usuario"] ?? "");
$filtro  = trim($_GET["filtro"]  ?? "mes");

if (!esAdmin($usuario, $conexion)) {
    echo json_encode(["ok" => false, "mensaje" => "Acceso denegado"]);
    exit;
}

// ── Calcular rango de fechas ──────────────────────────────────────────────
switch ($filtro) {
    case "2semanas":
        $desde    = date("Y-m-d", strtotime("-13 days"));
        $groupBy  = "DATE(f.fecha)";
        $labelFmt = "%d/%m";
        break;
    case "3meses":
        $desde    = date("Y-m-01", strtotime("-2 months"));
        $groupBy  = "DATE_FORMAT(f.fecha,'%Y-%m')";
        $labelFmt = "%b %Y";
        break;
    case "anio":
        $desde    = date("Y-01-01");
        $groupBy  = "DATE_FORMAT(f.fecha,'%Y-%m')";
        $labelFmt = "%b";
        break;
    default: // mes
        $desde    = date("Y-m-01");
        $groupBy  = "DATE(f.fecha)";
        $labelFmt = "%d/%m";
        break;
}

// ── Totales globales del período ──────────────────────────────────────────
$sqlTotal = "
    SELECT
        COALESCE(SUM(f.subtotal), 0) AS ingresos,
        COALESCE(SUM(fd.cantidad * p.precioCosto), 0) AS egresos
    FROM factura f
    LEFT JOIN factura_detalle fd ON fd.idFactura = f.idFactura
    LEFT JOIN productos p        ON p.idProducto  = fd.idProducto
    WHERE f.fecha >= ?";
$stmtT = mysqli_prepare($conexion, $sqlTotal);
mysqli_stmt_bind_param($stmtT, "s", $desde);
mysqli_stmt_execute($stmtT);
$rowT     = mysqli_fetch_assoc(mysqli_stmt_get_result($stmtT));
$ingresos = floatval($rowT["ingresos"]);
$egresos  = floatval($rowT["egresos"]);
$utilidad = $ingresos - $egresos;

// ── Datos agrupados (para gráfico) ────────────────────────────────────────
$sqlGraf = "
    SELECT
        $groupBy AS periodo,
        DATE_FORMAT(MIN(f.fecha), '$labelFmt') AS etiqueta,
        COALESCE(SUM(f.subtotal), 0) AS ingresos,
        COALESCE(SUM(fd.cantidad * p.precioCosto), 0) AS egresos
    FROM factura f
    LEFT JOIN factura_detalle fd ON fd.idFactura = f.idFactura
    LEFT JOIN productos p        ON p.idProducto  = fd.idProducto
    WHERE f.fecha >= '$desde'
    GROUP BY $groupBy
    ORDER BY periodo ASC";

$res   = mysqli_query($conexion, $sqlGraf);
$datos = [];
while ($fila = mysqli_fetch_assoc($res)) {
    $ing = floatval($fila["ingresos"]);
    $eg  = floatval($fila["egresos"]);
    $datos[] = [
        "etiqueta" => $fila["etiqueta"],
        "ingresos" => $ing,
        "egresos"  => $eg,
        "utilidad" => $ing - $eg
    ];
}

echo json_encode([
    "ok"       => true,
    "ingresos" => $ingresos,
    "egresos"  => $egresos,
    "utilidad" => $utilidad,
    "datos"    => $datos
], JSON_UNESCAPED_UNICODE);

function esAdmin(string $usr, $conn): bool {
    if ($usr === "") return false;
    $s = mysqli_prepare($conn, "SELECT rol FROM empleado WHERE usuario = ? LIMIT 1");
    mysqli_stmt_bind_param($s, "s", $usr);
    mysqli_stmt_execute($s);
    $r = mysqli_fetch_assoc(mysqli_stmt_get_result($s));
    return $r && intval($r["rol"]) === 1;
}

