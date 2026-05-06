<?php
/**
 * admin_categoria.php
 * ─────────────────────────────────────────────────────────────────────────────
 * CRUD de categorías. Solo accesible por usuarios con rol = 1 (Administrador).
 *
 * POST params:
 *   action   → listar | agregar | editar | eliminar
 *   usuario  → usuario del admin (verificación de rol)
 *   id       → id de la categoría (editar / eliminar)
 *   nombre   → nombre de la categoría (agregar / editar)
 * ─────────────────────────────────────────────────────────────────────────────
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$action  = trim($_POST["action"]  ?? $_GET["action"] ?? "");
$usuario = trim($_POST["usuario"] ?? "");

// ── Verificar que el usuario es administrador ─────────────────────────────
if ($usuario !== "") {
    $chk = mysqli_prepare($conexion, "SELECT rol FROM empleado WHERE usuario = ? LIMIT 1");
    mysqli_stmt_bind_param($chk, "s", $usuario);
    mysqli_stmt_execute($chk);
    $resChk = mysqli_stmt_get_result($chk);
    $filaChk = mysqli_fetch_assoc($resChk);
    mysqli_stmt_close($chk);

    if (!$filaChk || intval($filaChk["rol"]) !== 1) {
        echo json_encode(["ok" => false, "mensaje" => "Acceso denegado: se requiere rol Administrador"]);
        exit;
    }
}

switch ($action) {

    // ── LISTAR ────────────────────────────────────────────────────────────
    case "listar":
        $res = mysqli_query($conexion, "SELECT idCategoria AS id, nombreCat AS nombre FROM categoria ORDER BY nombreCat ASC");
        if (!$res) {
            echo json_encode(["ok" => false, "mensaje" => "Error al obtener categorías: " . mysqli_error($conexion)]);
            exit;
        }
        $categorias = [];
        while ($fila = mysqli_fetch_assoc($res)) {
            $categorias[] = ["id" => intval($fila["id"]), "nombre" => $fila["nombre"]];
        }
        echo json_encode(["ok" => true, "categorias" => $categorias]);
        break;

    // ── AGREGAR ───────────────────────────────────────────────────────────
    case "agregar":
        $nombre = trim($_POST["nombre"] ?? "");

        if ($nombre === "") {
            echo json_encode(["ok" => false, "mensaje" => "El nombre de la categoría es obligatorio"]);
            exit;
        }
        if (strlen($nombre) > 25) {
            echo json_encode(["ok" => false, "mensaje" => "El nombre no puede superar los 25 caracteres"]);
            exit;
        }

        // Verificar duplicado (case-insensitive)
        $dup = mysqli_prepare($conexion, "SELECT idCategoria FROM categoria WHERE LOWER(nombreCat) = LOWER(?) LIMIT 1");
        mysqli_stmt_bind_param($dup, "s", $nombre);
        mysqli_stmt_execute($dup);
        mysqli_stmt_store_result($dup);
        if (mysqli_stmt_num_rows($dup) > 0) {
            echo json_encode(["ok" => false, "mensaje" => "Ya existe una categoría con ese nombre"]);
            mysqli_stmt_close($dup);
            exit;
        }
        mysqli_stmt_close($dup);

        $ins = mysqli_prepare($conexion, "INSERT INTO categoria (nombreCat) VALUES (?)");
        mysqli_stmt_bind_param($ins, "s", $nombre);
        if (mysqli_stmt_execute($ins)) {
            $nuevoId = mysqli_insert_id($conexion);
            echo json_encode(["ok" => true, "mensaje" => "Categoría agregada correctamente", "id" => intval($nuevoId)]);
        } else {
            echo json_encode(["ok" => false, "mensaje" => "Error al agregar: " . mysqli_error($conexion)]);
        }
        mysqli_stmt_close($ins);
        break;

    // ── EDITAR ────────────────────────────────────────────────────────────
    case "editar":
        $id     = intval($_POST["id"]     ?? 0);
        $nombre = trim($_POST["nombre"]   ?? "");

        if ($id <= 0 || $nombre === "") {
            echo json_encode(["ok" => false, "mensaje" => "ID y nombre son obligatorios"]);
            exit;
        }
        if (strlen($nombre) > 25) {
            echo json_encode(["ok" => false, "mensaje" => "El nombre no puede superar los 25 caracteres"]);
            exit;
        }

        // Verificar duplicado excluyendo la propia categoría
        $dup = mysqli_prepare($conexion, "SELECT idCategoria FROM categoria WHERE LOWER(nombreCat) = LOWER(?) AND idCategoria <> ? LIMIT 1");
        mysqli_stmt_bind_param($dup, "si", $nombre, $id);
        mysqli_stmt_execute($dup);
        mysqli_stmt_store_result($dup);
        if (mysqli_stmt_num_rows($dup) > 0) {
            echo json_encode(["ok" => false, "mensaje" => "Ya existe una categoría con ese nombre"]);
            mysqli_stmt_close($dup);
            exit;
        }
        mysqli_stmt_close($dup);

        $upd = mysqli_prepare($conexion, "UPDATE categoria SET nombreCat = ? WHERE idCategoria = ?");
        mysqli_stmt_bind_param($upd, "si", $nombre, $id);
        if (mysqli_stmt_execute($upd) && mysqli_stmt_affected_rows($upd) > 0) {
            echo json_encode(["ok" => true, "mensaje" => "Categoría actualizada correctamente"]);
        } else {
            echo json_encode(["ok" => false, "mensaje" => "No se encontró la categoría o no hubo cambios"]);
        }
        mysqli_stmt_close($upd);
        break;

    // ── ELIMINAR ──────────────────────────────────────────────────────────
    case "eliminar":
        $id = intval($_POST["id"] ?? 0);

        if ($id <= 0) {
            echo json_encode(["ok" => false, "mensaje" => "ID inválido"]);
            exit;
        }

        // Verificar que no haya productos usando esta categoría
        $uso = mysqli_prepare($conexion, "SELECT idProducto FROM productos WHERE idCategoria = ? LIMIT 1");
        mysqli_stmt_bind_param($uso, "i", $id);
        mysqli_stmt_execute($uso);
        mysqli_stmt_store_result($uso);
        if (mysqli_stmt_num_rows($uso) > 0) {
            echo json_encode(["ok" => false, "mensaje" => "No se puede eliminar: hay productos asociados a esta categoría"]);
            mysqli_stmt_close($uso);
            exit;
        }
        mysqli_stmt_close($uso);

        $del = mysqli_prepare($conexion, "DELETE FROM categoria WHERE idCategoria = ?");
        mysqli_stmt_bind_param($del, "i", $id);
        if (mysqli_stmt_execute($del) && mysqli_stmt_affected_rows($del) > 0) {
            echo json_encode(["ok" => true, "mensaje" => "Categoría eliminada correctamente"]);
        } else {
            echo json_encode(["ok" => false, "mensaje" => "No se encontró la categoría"]);
        }
        mysqli_stmt_close($del);
        break;

    default:
        echo json_encode(["ok" => false, "mensaje" => "Acción no reconocida"]);
        break;
}

