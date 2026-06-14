<?php

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$host = "localhost";
$db   = "xploremx_db";
$user = "root";
$pass = "";

$id_usuario = isset($_GET['id_usuario']) ? intval($_GET['id_usuario']) : 0;

if ($id_usuario === 0) {
    http_response_code(400);
    echo json_encode(["error" => "id_usuario requerido"]);
    exit;
}

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8mb4", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => $e->getMessage()]);
    exit;
}


$sql = "SELECT id, titulo,
               DATE_FORMAT(fecha_hora, '%Y-%m-%d %H:%i:%s') AS fecha_hora,
               id_lugar
        FROM recordatorios
        WHERE id_usuario = :id
          AND fecha_hora >= NOW()
        ORDER BY fecha_hora ASC";

$stmt = $pdo->prepare($sql);
$stmt->execute([':id' => $id_usuario]);
$rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode($rows);