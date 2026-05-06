<?php
/**
 * catalogo.php  –  Devuelve categorías y marcas disponibles para los spinners del formulario de producto.
 * GET → { "categorias": [...], "marcas": [...] }
 */
header("Content-Type: application/json; charset=UTF-8");
require_once "conexion.php";

$categorias = [];
$resCat = mysqli_query($conexion, "SELECT idCategoria, nombreCat FROM categoria ORDER BY nombreCat ASC");
while ($fila = mysqli_fetch_assoc($resCat)) {
    $categorias[] = ["id" => intval($fila["idCategoria"]), "nombre" => $fila["nombreCat"]];
}

$marcas = [];
$resMar = mysqli_query($conexion, "SELECT idMarca, nombreMarc FROM marca ORDER BY nombreMarc ASC");
while ($fila = mysqli_fetch_assoc($resMar)) {
    $marcas[] = ["id" => intval($fila["idMarca"]), "nombre" => $fila["nombreMarc"]];
}

echo json_encode(["categorias" => $categorias, "marcas" => $marcas], JSON_UNESCAPED_UNICODE);

