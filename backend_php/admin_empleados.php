<?php
/**
 * admin_empleados.php  –  CRUD de empleados (solo ADMIN).
 * GET  ?usuario=X           → listar todos los empleados
 * POST action=agregar       → crear empleado
 * POST action=editar        → actualizar datos
 * POST action=eliminar      → eliminar empleado
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$method  = $_SERVER["REQUEST_METHOD"];
$usuario = trim($method === "GET" ? ($_GET["usuario"] ?? "") : ($_POST["usuario"] ?? ""));

if (!esAdmin($usuario, $conexion)) {
    echo json_encode(["ok" => false, "mensaje" => "Acceso denegado"]);
    exit;
}

if ($method === "GET") {
    listarEmpleados();
} else {
    $action = trim($_POST["action"] ?? "");
    switch ($action) {
        case "agregar":  agregarEmpleado();  break;
        case "editar":   editarEmpleado();   break;
        case "eliminar": eliminarEmpleado(); break;
        default: echo json_encode(["ok" => false, "mensaje" => "Acción inválida"]);
    }
}

// ── Listar ────────────────────────────────────────────────────────────────
function listarEmpleados() {
    global $conexion;
    $res  = mysqli_query($conexion, "SELECT usuario, nombre, apellido, rol FROM empleado ORDER BY nombre ASC");
    $list = [];
    while ($fila = mysqli_fetch_assoc($res)) {
        $list[] = [
            "usuario"  => $fila["usuario"],
            "nombre"   => $fila["nombre"],
            "apellido" => $fila["apellido"],
            "rol"      => intval($fila["rol"])
        ];
    }
    echo json_encode(["ok" => true, "empleados" => $list], JSON_UNESCAPED_UNICODE);
}

// ── Agregar ───────────────────────────────────────────────────────────────
function agregarEmpleado() {
    global $conexion;
    $usr   = trim($_POST["nuevoUsuario"] ?? "");
    $nom   = trim($_POST["nombre"]       ?? "");
    $ape   = trim($_POST["apellido"]     ?? "");
    $rol   = intval($_POST["rol"]        ?? 2);
    $pass  = trim($_POST["contrasena"]   ?? "");

    if ($usr === "" || $nom === "" || $pass === "") {
        echo json_encode(["ok" => false, "mensaje" => "Usuario, nombre y contraseña son requeridos"]); return;
    }
    if (strlen($usr) > 20)  { echo json_encode(["ok"=>false,"mensaje"=>"Usuario máx. 20 chars"]); return; }
    if (strlen($pass) > 72) { echo json_encode(["ok"=>false,"mensaje"=>"Contraseña máx. 72 chars"]); return; }

    // Hashear con BCrypt (costo 12) — nunca se guarda en texto plano
    $passHash = password_hash($pass, PASSWORD_BCRYPT, ["cost" => 12]);
    if ($passHash === false) {
        echo json_encode(["ok" => false, "mensaje" => "Error interno al hashear contraseña"]); return;
    }

    $stmt = mysqli_prepare($conexion,
        "INSERT INTO empleado (usuario, nombre, apellido, rol, contrasena) VALUES (?, ?, ?, ?, ?)");
    mysqli_stmt_bind_param($stmt, "sssis", $usr, $nom, $ape, $rol, $passHash);
    if (mysqli_stmt_execute($stmt)) {
        echo json_encode(["ok" => true, "mensaje" => "Empleado creado correctamente"]);
    } else {
        $e = mysqli_error($conexion);
        $msg = str_contains($e, "Duplicate") ? "El usuario o contraseña ya existe" : "Error: $e";
        echo json_encode(["ok" => false, "mensaje" => $msg]);
    }
}

// ── Editar ────────────────────────────────────────────────────────────────
function editarEmpleado() {
    global $conexion;
    $usr  = trim($_POST["nuevoUsuario"] ?? "");
    $nom  = trim($_POST["nombre"]       ?? "");
    $ape  = trim($_POST["apellido"]     ?? "");
    $rol  = intval($_POST["rol"]        ?? 2);
    $pass = trim($_POST["contrasena"]   ?? "");

    if ($usr === "" || $nom === "") {
        echo json_encode(["ok" => false, "mensaje" => "Usuario y nombre son requeridos"]); return;
    }

    if ($pass !== "") {
        // Actualizar con nueva contraseña hasheada
        $passHash = password_hash($pass, PASSWORD_BCRYPT, ["cost" => 12]);
        if ($passHash === false) {
            echo json_encode(["ok" => false, "mensaje" => "Error interno al hashear contraseña"]); return;
        }
        $stmt = mysqli_prepare($conexion,
            "UPDATE empleado SET nombre=?, apellido=?, rol=?, contrasena=? WHERE usuario=?");
        mysqli_stmt_bind_param($stmt, "ssiss", $nom, $ape, $rol, $passHash, $usr);
    } else {
        // Mantener contraseña actual
        $stmt = mysqli_prepare($conexion,
            "UPDATE empleado SET nombre=?, apellido=?, rol=? WHERE usuario=?");
        mysqli_stmt_bind_param($stmt, "ssis", $nom, $ape, $rol, $usr);
    }

    if (mysqli_stmt_execute($stmt)) {
        echo json_encode(["ok" => true, "mensaje" => "Empleado actualizado"]);
    } else {
        echo json_encode(["ok" => false, "mensaje" => "Error: " . mysqli_error($conexion)]);
    }
}

// ── Eliminar ──────────────────────────────────────────────────────────────
function eliminarEmpleado() {
    global $conexion;
    $usr    = trim($_POST["nuevoUsuario"] ?? "");
    $activo = trim($_POST["usuarioActivo"] ?? "");

    if ($usr === "") { echo json_encode(["ok" => false, "mensaje" => "Usuario requerido"]); return; }
    if ($usr === $activo) {
        echo json_encode(["ok" => false, "mensaje" => "No puedes eliminar tu propia cuenta"]); return;
    }

    $stmt = mysqli_prepare($conexion, "DELETE FROM empleado WHERE usuario = ?");
    mysqli_stmt_bind_param($stmt, "s", $usr);
    if (mysqli_stmt_execute($stmt) && mysqli_affected_rows($conexion) > 0) {
        echo json_encode(["ok" => true, "mensaje" => "Empleado eliminado"]);
    } else {
        echo json_encode(["ok" => false, "mensaje" => "No se encontró el empleado"]);
    }
}

// ── Helper ────────────────────────────────────────────────────────────────
function esAdmin(string $usr, $conn): bool {
    if ($usr === "") return false;
    $s = mysqli_prepare($conn, "SELECT rol FROM empleado WHERE usuario = ? LIMIT 1");
    mysqli_stmt_bind_param($s, "s", $usr);
    mysqli_stmt_execute($s);
    $r = mysqli_fetch_assoc(mysqli_stmt_get_result($s));
    return $r && intval($r["rol"]) === 1;
}

