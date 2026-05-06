<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$sql = "SELECT p.*, c.nombreCat AS categoria, m.nombreMarc AS marca
        FROM productos p
        INNER JOIN categoria c ON c.idCategoria = p.idCategoria
        INNER JOIN marca m ON m.idMarca = p.idMarca
        ORDER BY p.nombre ASC";

$resultado = mysqli_query($conexion, $sql);
$datos = [];

while ($fila = mysqli_fetch_assoc($resultado)) {
    $datos[] = $fila;
}

echo json_encode($datos, JSON_UNESCAPED_UNICODE);
