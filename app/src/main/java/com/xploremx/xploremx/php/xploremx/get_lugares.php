<?php
header("Content-Type: application/json");

$host = "localhost";
$user = "root";
$pass = "";
$db   = "xploremx_db";

$conn = mysqli_connect($host, $user, $pass, $db);

if (!$conn) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit();
}

$id_categoria = isset($_GET["id_categoria"]) ? $_GET["id_categoria"] : null;

if ($id_categoria) {
    $query = "SELECT * FROM lugares WHERE id_categoria = '$id_categoria'";
} else {
    $query = "SELECT * FROM lugares";
}

$result = mysqli_query($conn, $query);
$lugares = [];

while ($row = mysqli_fetch_assoc($result)) {
    $lugares[] = $row;
}

echo json_encode(["success" => true, "lugares" => $lugares]);

mysqli_close($conn);
?>