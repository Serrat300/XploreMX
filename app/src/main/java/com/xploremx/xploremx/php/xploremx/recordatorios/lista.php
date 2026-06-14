<?php

header("Content-Type: application/json");

$host = "localhost";
$user = "root";
$pass = "";
$db   = "xploremx_db";

$conn = mysqli_connect($host,$user,$pass,$db);

$id_usuario = $_GET["id_usuario"] ?? 0;

$sql = "
SELECT *
FROM recordatorios
WHERE id_usuario = $id_usuario
ORDER BY fecha_hora ASC
";

$result = mysqli_query($conn,$sql);

$datos = [];

while($row = mysqli_fetch_assoc($result)){
    $datos[] = $row;
}

echo json_encode($datos);

mysqli_close($conn);