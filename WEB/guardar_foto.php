<?php
header('Content-Type: application/json');

$datos = json_decode(file_get_contents('php://input'), true);

if (!isset($datos['email']) || !isset($datos['foto'])) {
    echo json_encode(array('exito' => false, 'mensaje' => 'Datos incompletos'));
    exit;
}

$email = $datos['email'];
$foto_base64 = $datos['foto'];

if (strpos($foto_base64, ',') !== false) {
    $foto_base64 = explode(',', $foto_base64)[1];
}

$foto_binaria = base64_decode($foto_base64);

if ($foto_binaria === false) {
    echo json_encode(array('exito' => false, 'mensaje' => 'Error al decodificar la imagen'));
    exit;
}

$directorio = __DIR__ . '/imgPerfil';

$nombre_archivo = md5($email) . '.jpg';
$ruta_archivo = $directorio . '/' . $nombre_archivo;

$resultado = file_put_contents($ruta_archivo, $foto_binaria);

if ($resultado) {
    echo json_encode(array('exito' => true, 'mensaje' => 'Foto guardada correctamente'));
} else {
    echo json_encode(array('exito' => false, 'mensaje' => 'Error al guardar la imagen en el servidor'));
}
?>