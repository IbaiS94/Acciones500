<?php
header('Content-Type: image/jpeg');

if (!isset($_GET['email'])) {
        header('HTTP/1.0 404 Not Found');
        exit;
}

$email = $_GET['email'];
$nombre_archivo = md5($email) . '.jpg';
$ruta_archivo = 'imgPerfil/' . $nombre_archivo;

if (file_exists($ruta_archivo)) {
    readfile($ruta_archivo);
} else {
        header('HTTP/1.0 404 Not Found');
        exit;
}
?>