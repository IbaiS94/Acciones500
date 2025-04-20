<?php
include 'conexion.php';

$ticker = isset($_POST['ticker']) ? $_POST['ticker'] : '';
$mensaje = isset($_POST['mensaje']) ? $_POST['mensaje'] : '';
$remitente = isset($_POST['remitente']) ? $_POST['remitente'] : '';
$ts = time() * 1000;

if (empty($ticker) || empty($mensaje) || empty($remitente)) {
    echo json_encode(['success' => false, 'message' => 'Faltan parámetros']);
    exit;
}

$s = $conexion->prepare("INSERT INTO mensajes (ticker, mensaje, remitente, timestamp) VALUES (?, ?, ?, ?)");
$s->bind_param("sssi", $ticker, $mensaje, $remitente, $ts);
$ondo = $s->execute();
$id = $s->insert_id;
$s->close();

$fcm = [
    'message' => [
        'topic' => 'stock_' . $ticker,
        'notification' => [
            'title' => 'Nuevo mensaje en ' . $ticker,
            'body' => $remitente . ': ' . $mensaje
        ],
        'data' => [
            'ticker' => $ticker,
            'mensaje' => $mensaje,
            'remitente' => $remitente
        ]
    ]
];

$rutaCuentaServicio = 'acciones500.json';
$tokenAcceso = obtenerTokenAcceso($rutaCuentaServicio);

$cabeceras = [
    'Authorization: Bearer ' . $tokenAcceso,
    'Content-Type: application/json'
];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/v1/projects/acciones500-1da7b/messages:send');
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, $cabeceras);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fcm));

$resultado = curl_exec($ch);
$httpCod = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curlErr  = curl_error($ch);
curl_close($ch);

echo json_encode([
    'success'      => $ondo,
    'id'           => $id,
    'timestamp'    => $ts,
    'http_code'    => $httpCod,
    'curl_error'   => $curlErr,
    'fcm_response' => json_decode($resultado, true)
]);

function base64url_encode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function obtenerTokenAcceso($rutaCuentaServicio) {
    $cuentaServicio = json_decode(file_get_contents($rutaCuentaServicio), true);
    $ahora = time();

    $cabecera = base64url_encode(json_encode(['alg' => 'RS256', 'typ' => 'JWT']));
    $payload = base64url_encode(json_encode([
        'iss' => $cuentaServicio['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => 'https://oauth2.googleapis.com/token',
        'exp' => $ahora + 3600,
        'iat' => $ahora
    ]));

    $jwtSinFirmar = $cabecera . '.' . $payload;
    openssl_sign($jwtSinFirmar, $firma, $cuentaServicio['private_key'], 'SHA256');
    $firmaJwt = base64url_encode($firma);

    $jwt = $jwtSinFirmar . '.' . $firmaJwt;

    $ch = curl_init('https://oauth2.googleapis.com/token');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
        'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' => $jwt
    ]));

    $respuesta = curl_exec($ch);
    curl_close($ch);
    $datosRespuesta = json_decode($respuesta, true);
    return $datosRespuesta['access_token'] ?? null;
}
?>