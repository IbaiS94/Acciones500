<?php
header('Content-Type: application/json');
include 'conexion.php';

try {
    $datos = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($datos['email']) || !isset($datos['password'])) {
        throw new Exception("Datos incompletos");
    }

    $email = $datos['email'];
    $pass = $datos['password'];

    $consulta = $conexion->prepare("SELECT nombre, password FROM usuarios WHERE email = ?");
    $consulta->bind_param("s", $email);
    $consulta->execute();
    $resultado = $consulta->get_result();

    if ($resultado->num_rows === 0) {
        echo json_encode(["exito" => false, "mensaje" => "Usuario no existe"]);
        exit;
    }

    $usuario = $resultado->fetch_assoc();
    
    if (password_verify($pass, $usuario['password'])) {
        echo json_encode([
            "exito" => true,
            "nombre" => $usuario['nombre']
        ]);
    } else {
        echo json_encode(["exito" => false, "mensaje" => "Contraseña inválida"]);
    }

} catch (Exception $e) {
    echo json_encode([
        "exito" => false,
        "mensaje" => "Error: " . $e->getMessage()
    ]);
} finally {
    if (isset($consulta)) $consulta->close();
    $conexion->close();
}
?>