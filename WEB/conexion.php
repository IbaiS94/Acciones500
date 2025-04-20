<?php
$servidor = "localhost";
$usuario = "Xisologuestoa001";
$clave = "0bXD515P2";
$basedatos = "Xisologuestoa001_acciones500";

$conexion = new mysqli($servidor, $usuario, $clave, $basedatos);

if ($conexion->connect_error) {
    die("Error: " . $conexion->connect_error);
}

$conexion->set_charset("utf8");
?>