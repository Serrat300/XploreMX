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

$usuario    = $_POST["usuario"];
$contrasena = md5($_POST["contrasena"]);

$query = "SELECT * FROM usuarios WHERE usuario = '$usuario' AND contrasena = '$contrasena'";
$result = mysqli_query($conn, $query);

if (mysqli_num_rows($result) > 0) {
    $row = mysqli_fetch_assoc($result);
    echo json_encode([
        "success" => true,
        "id"      => $row["id"],
        "nombre"  => $row["nombre"],
        "usuario" => $row["usuario"]
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Usuario o contraseña incorrectos"]);
}

mysqli_close($conn);
?>