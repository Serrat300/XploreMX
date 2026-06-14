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

$titulo     = $body["titulo"];
$fechaHora  = $body["fecha_hora"];
$idUsuario  = $body["id_usuario"];
$idLugar = null;

$sql = "
INSERT INTO recordatorios
(
titulo,
fecha_hora,
id_usuario,
id_lugar
)
VALUES
(
'$titulo',
'$fechaHora',
'$idUsuario',
".($idLugar === null ? "NULL" : $idLugar)."
)
";

mysqli_query($conn,$sql);

echo json_encode([
    "success" => true
]);

mysqli_close($conn);