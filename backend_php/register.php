<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$nombre     = trim($_POST["nombre"]     ?? "");
$apellido   = trim($_POST["apellido"]   ?? "");
$usuario    = trim($_POST["usuario"]    ?? "");
$contrasena = trim($_POST["contrasena"] ?? "");
$correo     = strtolower(trim($_POST["correo"] ?? ""));

// Validar campos obligatorios
if ($nombre === "" || $apellido === "" || $usuario === "" || $contrasena === "" || $correo === "") {
    echo json_encode(["ok" => false, "mensaje" => "Todos los campos son obligatorios"]);
    exit;
}

// Validar longitudes
if (strlen($usuario) > 20) {
    echo json_encode(["ok" => false, "mensaje" => "El usuario no puede tener más de 20 caracteres"]);
    exit;
}
if (strlen($contrasena) > 72) {
    // BCrypt tiene un límite interno de 72 bytes; advertimos al usuario
    echo json_encode(["ok" => false, "mensaje" => "La contraseña no puede superar los 72 caracteres"]);
    exit;
}

// ── Determinar rol según dominio del correo ───────────────────────────────
// @admin o @admin.com → rol = 1 (ADMINISTRADOR)
// cualquier otro dominio → rol = 2 (EMPLEADO)
$rol = (str_ends_with($correo, "@admin") || str_ends_with($correo, "@admin.com")) ? 1 : 2;

// Verificar si el usuario ya existe
$stmt = mysqli_prepare($conexion, "SELECT usuario FROM empleado WHERE usuario = ? LIMIT 1");
mysqli_stmt_bind_param($stmt, "s", $usuario);
mysqli_stmt_execute($stmt);
mysqli_stmt_store_result($stmt);

if (mysqli_stmt_num_rows($stmt) > 0) {
    echo json_encode(["ok" => false, "mensaje" => "El nombre de usuario ya está en uso"]);
    mysqli_stmt_close($stmt);
    exit;
}
mysqli_stmt_close($stmt);

// ── Hashear la contraseña con BCrypt (costo 12) ───────────────────────────
// password_hash genera automáticamente un salt aleatorio y seguro.
// Nunca se almacena la contraseña en texto plano.
$hashContrasena = password_hash($contrasena, PASSWORD_BCRYPT, ["cost" => 12]);

if ($hashContrasena === false) {
    echo json_encode(["ok" => false, "mensaje" => "Error interno al procesar la contraseña"]);
    exit;
}

// Insertar nuevo usuario con contraseña hasheada y rol calculado
$insert = mysqli_prepare(
    $conexion,
    "INSERT INTO empleado (usuario, nombre, apellido, rol, contrasena) VALUES (?, ?, ?, ?, ?)"
);
mysqli_stmt_bind_param($insert, "sssis", $usuario, $nombre, $apellido, $rol, $hashContrasena);

if (mysqli_stmt_execute($insert)) {
    $rolNombre = ($rol === 1) ? "Administrador" : "Empleado";
    echo json_encode([
        "ok"      => true,
        "mensaje" => "Cuenta creada exitosamente. Rol asignado: $rolNombre",
        "rol"     => $rol
    ]);
} else {
    echo json_encode(["ok" => false, "mensaje" => "Error al crear la cuenta: " . mysqli_error($conexion)]);
}
mysqli_stmt_close($insert);

