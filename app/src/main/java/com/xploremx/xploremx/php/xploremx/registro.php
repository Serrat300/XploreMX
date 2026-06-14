<?php
header("Content-Type: application/json");

$conn = mysqli_connect("localhost", "root", "", "xploremx_db");

if (!$conn) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit();
}

$nombre    = $_POST["nombre"];
$usuario   = $_POST["usuario"];
$contrasena = md5($_POST["contrasena"]);

// Verificar si el usuario ya existe
$checkQuery = "SELECT id FROM usuarios WHERE usuario = '$usuario'";
$checkResult = mysqli_query($conn, $checkQuery);

if (mysqli_num_rows($checkResult) > 0) {
    echo json_encode(["success" => false, "message" => "Ese usuario ya existe"]);
    exit();
}

// Insertar nuevo usuario
$query = "INSERT INTO usuarios (nombre, usuario, contrasena) VALUES ('$nombre', '$usuario', '$contrasena')";

if (mysqli_query($conn, $query)) {
    echo json_encode(["success" => true, "message" => "Usuario registrado correctamente"]);
} else {
    echo json_encode(["success" => false, "message" => "Error al registrar"]);
}

mysqli_close($conn);
?>