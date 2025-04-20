<?php
include 'conexion.php';

$datos = json_decode(file_get_contents('php://input'), true);
$nombre = $datos['nombre'];
$email = $datos['email'];
$pass = password_hash($datos['password'], PASSWORD_DEFAULT);

$verificar = $conexion->prepare("SELECT id FROM usuarios WHERE email = ?");
$verificar->bind_param("s", $email);
$verificar->execute();
$resultado = $verificar->get_result();

if ($resultado->num_rows > 0) {
    echo json_encode(["exito" => false, "mensaje" => "Email ya registrado"]);
    exit;
}

$insertar = $conexion->prepare("INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)");
$insertar->bind_param("sss", $nombre, $email, $pass);

if ($insertar->execute()) {
    echo json_encode(["exito" => true]);
} else {
    echo json_encode(["exito" => false, "mensaje" => "Error en registro"]);
}

$insertar->close();
$conexion->close();
?>