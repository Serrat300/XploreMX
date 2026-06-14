<?php

header("Content-Type: application/json");

$body = json_decode(
    file_get_contents("php://input"),
    true
);

$conn = mysqli_connect(
    "localhost",
    "root",
    "",
    "xploremx_db"
);

$id = $body["id"];

$sql = "
DELETE FROM recordatorios
WHERE id=$id
";

mysqli_query($conn,$sql);

echo json_encode([
    "success" => true
]);

mysqli_close($conn);