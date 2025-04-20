<?php
include 'conexion.php';

$token = isset($_POST['token']) ? $_POST['token'] : '';
$email = isset($_POST['email']) ? $_POST['email'] : '';

if (empty($token) || empty($email)) {
    echo json_encode(['success' => false, 'message' => 'Faltan parámetros']);
    exit;
}

$s = $conexion->prepare("INSERT INTO tokens (email, token) VALUES (?, ?) ON DUPLICATE KEY UPDATE token = ?");
$s->bind_param("sss", $email, $token, $token);
$ondo = $s->execute();
$s->close();
$conexion->close();

echo json_encode(['success' => $ondo]);
?>