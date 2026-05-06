<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$usuario    = trim($_POST["usuario"]    ?? "");
$contrasena = trim($_POST["contrasena"] ?? "");

// Validar que los campos no estén vacíos
if ($usuario === "" || $contrasena === "") {
    echo json_encode(["ok" => false, "mensaje" => "Datos incompletos"]);
    exit;
}

// ── Buscar el usuario por nombre (sin comparar contraseña en SQL) ─────────
// La contraseña NUNCA se compara en texto plano; se recupera el hash
// y se verifica con password_verify() para proteger contra ataques de
// temporización y exposición de datos.
$stmt = mysqli_prepare(
    $conexion,
    "SELECT usuario, nombre, apellido, rol, contrasena FROM empleado WHERE usuario = ? LIMIT 1"
);
mysqli_stmt_bind_param($stmt, "s", $usuario);
mysqli_stmt_execute($stmt);
$resultado = mysqli_stmt_get_result($stmt);
$fila      = mysqli_fetch_assoc($resultado);
mysqli_stmt_close($stmt);

// Verificar existencia del usuario y validar contraseña contra el hash BCrypt
if ($fila && password_verify($contrasena, $fila["contrasena"])) {

    // Guardar el hash para rehash si es necesario (antes de eliminarlo de la respuesta)
    $hashActual = $fila["contrasena"];

    // Eliminar el hash antes de devolver los datos al cliente
    unset($fila["contrasena"]);

    // Actualizar el hash si PHP recomienda un algoritmo más fuerte o distinto costo
    if (password_needs_rehash($hashActual, PASSWORD_BCRYPT, ["cost" => 12])) {
        $nuevoHash = password_hash($contrasena, PASSWORD_BCRYPT, ["cost" => 12]);
        $upd = mysqli_prepare($conexion, "UPDATE empleado SET contrasena = ? WHERE usuario = ?");
        mysqli_stmt_bind_param($upd, "ss", $nuevoHash, $usuario);
        mysqli_stmt_execute($upd);
        mysqli_stmt_close($upd);
    }

    echo json_encode(["ok" => true, "usuario" => $fila], JSON_UNESCAPED_UNICODE);
} else {
    // Respuesta genérica para no revelar si el usuario existe o no
    echo json_encode(["ok" => false, "mensaje" => "Credenciales incorrectas"]);
}
