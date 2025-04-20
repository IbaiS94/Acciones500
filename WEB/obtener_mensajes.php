<?php

include 'conexion.php';


$ticker = isset($_POST['ticker']) ? $_POST['ticker'] : '';

if (empty($ticker)) {
    echo json_encode([]);
    exit;
}

$s = $conexion->prepare("SELECT id, mensaje, remitente, timestamp FROM mensajes WHERE ticker = ? ORDER BY timestamp ASC");
$s->bind_param("s", $ticker);
$s->execute();
$resultado = $s->get_result();

$mensajes = [];
while ($fila = $resultado->fetch_assoc()) {
    $mensajes[] = $fila;
}

$s->close();
$conexion->close();

header('Content-Type: application/json');
echo json_encode($mensajes);
?>