<?php

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["error" => "Método no permitido"]);
    exit;
}

// Verificar que llegó el archivo
if (!isset($_FILES['video']) || $_FILES['video']['error'] !== UPLOAD_ERR_OK) {
    http_response_code(400);
    echo json_encode(["error" => "No se recibió el video o hubo un error"]);
    exit;
}

// Carpeta donde se guardarán los videos 
$carpeta = __DIR__ . "/../videos/";
if (!is_dir($carpeta)) {
    mkdir($carpeta, 0755, true);
}

$nombreArchivo = "vid_" . time() . "_" . bin2hex(random_bytes(4)) . ".mp4";
$rutaDestino   = $carpeta . $nombreArchivo;

// Mover el archivo subido a la carpeta
if (move_uploaded_file($_FILES['video']['tmp_name'], $rutaDestino)) {
    echo json_encode(["url" => "videos/" . $nombreArchivo]);
} else {
    http_response_code(500);
    echo json_encode(["error" => "No se pudo guardar el video"]);
}