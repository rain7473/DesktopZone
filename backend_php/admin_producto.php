<?php
/**
 * admin_producto.php  –  CRUD de productos (solo accesible para rol ADMIN).
 * POST param "action": agregar | editar | eliminar
 * POST param "usuario": nombre de usuario que realiza la acción (se valida rol=1)
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$action  = trim($_POST["action"]  ?? "");
$usuarioSolicitante = trim($_POST["usuario"] ?? "");

// ── Validar que el solicitante es ADMIN ───────────────────────────────────
if ($usuarioSolicitante === "") {
    echo json_encode(["ok" => false, "mensaje" => "Acceso no autorizado"]);
    exit;
}
$stmtRol = mysqli_prepare($conexion, "SELECT rol FROM empleado WHERE usuario = ? LIMIT 1");
mysqli_stmt_bind_param($stmtRol, "s", $usuarioSolicitante);
mysqli_stmt_execute($stmtRol);
$resRol = mysqli_stmt_get_result($stmtRol);
$rowRol = mysqli_fetch_assoc($resRol);

if (!$rowRol || intval($rowRol["rol"]) !== 1) {
    echo json_encode(["ok" => false, "mensaje" => "Solo los administradores pueden realizar esta acción"]);
    exit;
}

// ── Despachar acción ──────────────────────────────────────────────────────
switch ($action) {
    case "agregar":   agregarProducto();  break;
    case "editar":    editarProducto();   break;
    case "eliminar":  eliminarProducto(); break;
    default:
        echo json_encode(["ok" => false, "mensaje" => "Acción no válida: '$action'"]);
}

// ─────────────────────────────────────────────────────────────────────────
function agregarProducto() {
    global $conexion;

    $idProducto  = trim($_POST["idProducto"]  ?? "");
    $nombre      = trim($_POST["nombre"]      ?? "");
    $unidad      = trim($_POST["unidad"]      ?? "UND");
    $descripcion = trim($_POST["descripcion"] ?? "");
    $stock       = intval($_POST["stock"]     ?? 0);
    $precioCosto = floatval($_POST["precioCosto"] ?? 0);
    $precioVenta = floatval($_POST["precioVenta"] ?? 0);
    $imagen      = trim($_POST["imagen"]      ?? "");
    $idCategoria = intval($_POST["idCategoria"] ?? 0);
    $idMarca     = intval($_POST["idMarca"]    ?? 0);

    if ($nombre === "" || $idCategoria <= 0 || $idMarca <= 0) {
        echo json_encode(["ok" => false, "mensaje" => "Nombre, categoría y marca son requeridos"]);
        return;
    }

    if ($idProducto !== "") {
        // ID explícito proporcionado por el admin
        $stmt = mysqli_prepare($conexion,
            "INSERT INTO productos (idProducto, nombre, unidad, descripcion, stock, precioCosto, precioVenta, imagen, idCategoria, idMarca)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );
        mysqli_stmt_bind_param($stmt, "ssssiddsii",
            $idProducto, $nombre, $unidad, $descripcion,
            $stock, $precioCosto, $precioVenta, $imagen,
            $idCategoria, $idMarca
        );
    } else {
        // Sin ID → AUTO_INCREMENT
        $stmt = mysqli_prepare($conexion,
            "INSERT INTO productos (nombre, unidad, descripcion, stock, precioCosto, precioVenta, imagen, idCategoria, idMarca)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );
        mysqli_stmt_bind_param($stmt, "sssiddsii",
            $nombre, $unidad, $descripcion,
            $stock, $precioCosto, $precioVenta, $imagen,
            $idCategoria, $idMarca
        );
    }

    if (mysqli_stmt_execute($stmt)) {
        $nuevoId = mysqli_insert_id($conexion);
        echo json_encode(["ok" => true, "mensaje" => "Producto agregado correctamente", "idProducto" => $nuevoId]);
    } else {
        echo json_encode(["ok" => false, "mensaje" => "Error al agregar: " . mysqli_error($conexion)]);
    }
}

function editarProducto() {
    global $conexion;

    $idProducto  = trim($_POST["idProducto"]  ?? "");
    $nombre      = trim($_POST["nombre"]      ?? "");
    $unidad      = trim($_POST["unidad"]      ?? "UND");
    $descripcion = trim($_POST["descripcion"] ?? "");
    $stock       = intval($_POST["stock"]     ?? 0);
    $precioCosto = floatval($_POST["precioCosto"] ?? 0);
    $precioVenta = floatval($_POST["precioVenta"] ?? 0);
    $imagen      = trim($_POST["imagen"]      ?? "");
    $idCategoria = intval($_POST["idCategoria"] ?? 0);
    $idMarca     = intval($_POST["idMarca"]    ?? 0);

    if ($idProducto === "" || $nombre === "") {
        echo json_encode(["ok" => false, "mensaje" => "ID y nombre son requeridos"]);
        return;
    }

    $stmt = mysqli_prepare($conexion,
        "UPDATE productos
         SET nombre=?, unidad=?, descripcion=?, stock=?, precioCosto=?, precioVenta=?, imagen=?, idCategoria=?, idMarca=?
         WHERE idProducto=?"
    );
    mysqli_stmt_bind_param($stmt, "sssiddsiis",
        $nombre, $unidad, $descripcion, $stock,
        $precioCosto, $precioVenta, $imagen,
        $idCategoria, $idMarca, $idProducto
    );

    if (mysqli_stmt_execute($stmt)) {
        echo json_encode(["ok" => true, "mensaje" => "Producto actualizado correctamente"]);
    } else {
        echo json_encode(["ok" => false, "mensaje" => "Error al editar: " . mysqli_error($conexion)]);
    }
}

function eliminarProducto() {
    global $conexion;

    $idProducto = trim($_POST["idProducto"] ?? "");

    if ($idProducto === "") {
        echo json_encode(["ok" => false, "mensaje" => "ID de producto requerido"]);
        return;
    }

    // Intentar eliminar; si hay FK en factura_detalle MySQL lo rechazará
    $stmt = mysqli_prepare($conexion, "DELETE FROM productos WHERE idProducto = ?");
    mysqli_stmt_bind_param($stmt, "s", $idProducto);

    if (mysqli_stmt_execute($stmt)) {
        if (mysqli_affected_rows($conexion) > 0) {
            echo json_encode(["ok" => true, "mensaje" => "Producto eliminado correctamente"]);
        } else {
            echo json_encode(["ok" => false, "mensaje" => "No se encontró el producto"]);
        }
    } else {
        $err = mysqli_error($conexion);
        // Detectar violación de FK (producto tiene pedidos)
        $msg = str_contains($err, "foreign key") || str_contains($err, "1451")
            ? "No se puede eliminar: el producto tiene pedidos registrados"
            : "Error al eliminar: $err";
        echo json_encode(["ok" => false, "mensaje" => $msg]);
    }
}




