<?php
/**
 * admin_dashboard.php  –  KPIs y datos para el dashboard del administrador.
 * Valida que el solicitante sea ADMIN antes de devolver datos.
 * GET param: usuario
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$usuario = trim($_GET["usuario"] ?? "");
if (!esAdmin($usuario, $conexion)) {
    echo json_encode(["ok" => false, "mensaje" => "Acceso denegado"]);
    exit;
}

// ── Ventas de hoy ─────────────────────────────────────────────────────────
$resHoy = mysqli_query($conexion,
    "SELECT COALESCE(SUM(subtotal), 0) AS total FROM factura WHERE DATE(fecha) = CURDATE()");
$ventasHoy = floatval(mysqli_fetch_assoc($resHoy)["total"]);

// ── Ventas del mes ────────────────────────────────────────────────────────
$resMes = mysqli_query($conexion,
    "SELECT COALESCE(SUM(subtotal), 0) AS total FROM factura
     WHERE MONTH(fecha) = MONTH(CURDATE()) AND YEAR(fecha) = YEAR(CURDATE())");
$ventasMes = floatval(mysqli_fetch_assoc($resMes)["total"]);

// ── Total pedidos del mes ─────────────────────────────────────────────────
$resPedidos = mysqli_query($conexion,
    "SELECT COUNT(*) AS total FROM factura
     WHERE MONTH(fecha) = MONTH(CURDATE()) AND YEAR(fecha) = YEAR(CURDATE())");
$totalPedidosMes = intval(mysqli_fetch_assoc($resPedidos)["total"]);

// ── Total productos ───────────────────────────────────────────────────────
$resProds = mysqli_query($conexion, "SELECT COUNT(*) AS total FROM productos");
$totalProductos = intval(mysqli_fetch_assoc($resProds)["total"]);

// ── Productos con bajo stock (≤ 5) ────────────────────────────────────────
$resBajo = mysqli_query($conexion, "SELECT COUNT(*) AS total FROM productos WHERE stock <= 5 AND stock > 0");
$bajoStock = intval(mysqli_fetch_assoc($resBajo)["total"]);

// ── Agotados ──────────────────────────────────────────────────────────────
$resAgot = mysqli_query($conexion, "SELECT COUNT(*) AS total FROM productos WHERE stock = 0");
$agotados = intval(mysqli_fetch_assoc($resAgot)["total"]);

// ── Periodo del gráfico (7, 15, 30, 90, 365) ─────────────────────────────
$periodo = intval($_GET["periodo"] ?? 7);
if (!in_array($periodo, [7, 15, 30, 90, 365])) $periodo = 7;

$grafico = [];

if ($periodo <= 30) {
    // ── Agrupación DIARIA ─────────────────────────────────────────────
    $intervalo = $periodo - 1;

    $resPeriodo = mysqli_query($conexion,
        "SELECT DATE(f.fecha) AS dia, COALESCE(SUM(f.subtotal), 0) AS ingresos,
                COALESCE(SUM(fd.cantidad * p.precioCosto), 0) AS egresos
         FROM factura f
         LEFT JOIN factura_detalle fd ON fd.idFactura = f.idFactura
         LEFT JOIN productos p        ON p.idProducto  = fd.idProducto
         WHERE f.fecha >= DATE_SUB(CURDATE(), INTERVAL $intervalo DAY)
         GROUP BY DATE(f.fecha)
         ORDER BY dia ASC");

    $mapaVentas = [];
    while ($fila = mysqli_fetch_assoc($resPeriodo)) {
        $mapaVentas[$fila["dia"]] = [
            "ingresos" => floatval($fila["ingresos"]),
            "egresos"  => floatval($fila["egresos"])
        ];
    }

    // Periodo anterior para comparación
    $resPasado = mysqli_query($conexion,
        "SELECT DATE(f.fecha) AS dia, COALESCE(SUM(f.subtotal), 0) AS ingresos
         FROM factura f
         WHERE f.fecha >= DATE_SUB(CURDATE(), INTERVAL " . (2 * $periodo - 1) . " DAY)
           AND f.fecha <  DATE_SUB(CURDATE(), INTERVAL $intervalo DAY)
         GROUP BY DATE(f.fecha)
         ORDER BY dia ASC");

    $mapaPasado = [];
    while ($fila = mysqli_fetch_assoc($resPasado)) {
        $mapaPasado[$fila["dia"]] = floatval($fila["ingresos"]);
    }

    $diasEs = ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"];
    for ($i = $intervalo; $i >= 0; $i--) {
        $fechaActual = date("Y-m-d", strtotime("-$i days"));
        $fechaPasada = date("Y-m-d", strtotime("-" . ($i + $periodo) . " days"));

        if ($periodo <= 7) {
            $etiq = $diasEs[(int)date("w", strtotime($fechaActual))];
        } else {
            $etiq = date("d/m", strtotime($fechaActual));
        }

        $datos     = $mapaVentas[$fechaActual] ?? ["ingresos" => 0.0, "egresos" => 0.0];
        $ingPasada = $mapaPasado[$fechaPasada] ?? 0.0;

        $grafico[] = [
            "etiqueta"        => $etiq,
            "ingresos"        => $datos["ingresos"],
            "egresos"         => $datos["egresos"],
            "ingresosPasados" => $ingPasada
        ];
    }
} else {
    // ── Agrupación MENSUAL (90 = 3 meses, 365 = 12 meses) ────────────
    $meses = ($periodo == 90) ? 3 : 12;

    $resPeriodo = mysqli_query($conexion,
        "SELECT DATE_FORMAT(f.fecha, '%Y-%m') AS mes,
                COALESCE(SUM(f.subtotal), 0) AS ingresos,
                COALESCE(SUM(fd.cantidad * p.precioCosto), 0) AS egresos
         FROM factura f
         LEFT JOIN factura_detalle fd ON fd.idFactura = f.idFactura
         LEFT JOIN productos p        ON p.idProducto  = fd.idProducto
         WHERE f.fecha >= DATE_SUB(CURDATE(), INTERVAL $meses MONTH)
         GROUP BY DATE_FORMAT(f.fecha, '%Y-%m')
         ORDER BY mes ASC");

    $mapaVentas = [];
    while ($fila = mysqli_fetch_assoc($resPeriodo)) {
        $mapaVentas[$fila["mes"]] = [
            "ingresos" => floatval($fila["ingresos"]),
            "egresos"  => floatval($fila["egresos"])
        ];
    }

    // Periodo anterior para comparación
    $resPasado = mysqli_query($conexion,
        "SELECT DATE_FORMAT(f.fecha, '%Y-%m') AS mes,
                COALESCE(SUM(f.subtotal), 0) AS ingresos
         FROM factura f
         WHERE f.fecha >= DATE_SUB(CURDATE(), INTERVAL " . (2 * $meses) . " MONTH)
           AND f.fecha <  DATE_SUB(CURDATE(), INTERVAL $meses MONTH)
         GROUP BY DATE_FORMAT(f.fecha, '%Y-%m')
         ORDER BY mes ASC");

    $mapaPasado = [];
    while ($fila = mysqli_fetch_assoc($resPasado)) {
        $mapaPasado[$fila["mes"]] = floatval($fila["ingresos"]);
    }

    $mesesEs = ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"];
    for ($i = $meses - 1; $i >= 0; $i--) {
        $mesActual = date("Y-m", strtotime("-$i months"));
        $mesPasado = date("Y-m", strtotime("-" . ($i + $meses) . " months"));
        $etiq      = $mesesEs[(int)date("n", strtotime($mesActual . "-01")) - 1];

        $datos     = $mapaVentas[$mesActual] ?? ["ingresos" => 0.0, "egresos" => 0.0];
        $ingPasada = $mapaPasado[$mesPasado] ?? 0.0;

        $grafico[] = [
            "etiqueta"        => $etiq,
            "ingresos"        => $datos["ingresos"],
            "egresos"         => $datos["egresos"],
            "ingresosPasados" => $ingPasada
        ];
    }
}

echo json_encode([
    "ok"              => true,
    "ventasHoy"       => $ventasHoy,
    "ventasMes"       => $ventasMes,
    "totalPedidosMes" => $totalPedidosMes,
    "totalProductos"  => $totalProductos,
    "bajoStock"       => $bajoStock,
    "agotados"        => $agotados,
    "graficoSemana"   => $grafico
], JSON_UNESCAPED_UNICODE);

// ── Helper ────────────────────────────────────────────────────────────────
function esAdmin(string $usr, $conn): bool {
    if ($usr === "") return false;
    $s = mysqli_prepare($conn, "SELECT rol FROM empleado WHERE usuario = ? LIMIT 1");
    mysqli_stmt_bind_param($s, "s", $usr);
    mysqli_stmt_execute($s);
    $r = mysqli_fetch_assoc(mysqli_stmt_get_result($s));
    return $r && intval($r["rol"]) === 1;
}
