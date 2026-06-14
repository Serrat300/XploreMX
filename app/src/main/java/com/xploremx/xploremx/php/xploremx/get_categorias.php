<?php
header("Content-Type: application/json");

$conn = mysqli_connect("localhost", "root", "", "xploremx_db");

if (!$conn) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit();
}

$result = mysqli_query($conn, "SELECT * FROM categorias");
$categorias = [];

while ($row = mysqli_fetch_assoc($result)) {
    $categorias[] = $row;
}

echo json_encode(["success" => true, "categorias" => $categorias]);
mysqli_close($conn);
?>