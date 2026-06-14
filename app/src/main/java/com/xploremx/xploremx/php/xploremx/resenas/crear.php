<?php

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["error" => "Método no permitido"]);
    exit;
}

$body = json_decode(file_get_contents("php://input"), true);

if (!$body) {
    http_response_code(400);
    echo json_encode(["error" => "Body JSON inválido"]);
    exit;
}

$host = "localhost";
$db   = "xploremx_db";
$user = "root";
$pass = "";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8mb4", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => "No se pudo conectar a la BD: " . $e->getMessage()]);
    exit;
}

$comentario  = $body['comentario']  ?? "";
$video_url   = $body['video_url']   ?? "";
$id_usuario  = $body['id_usuario']  ?? null;
$id_lugar    = $body['id_lugar']    ?? null;

// 
if ($id_lugar === 0) $id_lugar = null;

$sql = "INSERT INTO resenas (comentario, video_url, id_usuario, id_lugar)
        VALUES (:comentario, :video_url, :id_usuario, :id_lugar)";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':comentario' => $comentario,
    ':video_url'  => $video_url,
    ':id_usuario' => $id_usuario,
    ':id_lugar'   => $id_lugar,
]);

echo json_encode([
    "ok" => true,
    "id" => $pdo->lastInsertId()
]);