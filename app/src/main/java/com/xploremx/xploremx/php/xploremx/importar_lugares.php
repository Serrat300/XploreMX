<?php
header("Content-Type: application/json");

$conn = mysqli_connect("localhost", "root", "", "xploremx_db");

if (!$conn) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit();
}


try {
// Parques
    $query = '
[out:json][timeout:240];

(
    node["leisure"="park"](around:1000,20.6767,-103.3475);
    way["leisure"="park"](around:1000,20.6767,-103.3475);
);

out center tags;
';

// Parques 2
/*
$query = '
[out:json][timeout:240];

(
    relation["leisure"="park"](around:1000,20.6767,-103.3475);
);

out center tags;
';
*/
// Museos
/*
    $query = '
[out:json][timeout:240];

(
    node["tourism"="museum"](around:1000,20.6767,-103.3475);
    way["tourism"="museum"](around:1000,20.6767,-103.3475);
);

out center tags;
';
*/

// Restaurantes
/*
    $query = '
[out:json][timeout:240];

node["amenity"="restaurant"](around:1000,20.6767,-103.3475);

out body;
';
*/

$ch = curl_init("https://overpass.kumi.systems/api/interpreter");

curl_setopt_array($ch, [
    CURLOPT_POST => true,

    CURLOPT_POSTFIELDS => http_build_query([
        'data' => $query
    ]),

    CURLOPT_RETURNTRANSFER => true,

    CURLOPT_TIMEOUT => 240,

    CURLOPT_USERAGENT => 'XploreMX/1.0 (Proyecto Universitario)',

    CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,

    CURLOPT_IPRESOLVE => CURL_IPRESOLVE_V4
]);

$response = curl_exec($ch);

if (curl_errno($ch)) {
    throw new Exception(
        "Error cURL: " . curl_error($ch)
    );
}

$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

curl_close($ch);

if ($httpCode !== 200) {
    throw new Exception(
        "HTTP Error: " . $httpCode
    );
}

$data = json_decode($response, true);

if (!isset($data["elements"])) {
    throw new Exception(
        "No se recibieron elementos."
    );
}

    $insertados = 0;
    $omitidos = 0;

    foreach ($data["elements"] as $element) {

        $tags = $element["tags"] ?? [];

        $nombre = $tags["name"] ?? null;

        if ($nombre === null) {
            continue;
        }

        if (isset($element["lat"])) {
            $latitud = $element["lat"];
            $longitud = $element["lon"];
        }
        elseif (isset($element["center"])) {
            $latitud = $element["center"]["lat"];
            $longitud = $element["center"]["lon"];
        }
        else {
            continue;
        }

        if ($latitud === null || $longitud === null) {
            continue;
        }

        // Categoría
        $idCategoria = null;

        if (($tags["amenity"] ?? "") === "restaurant") {
            $idCategoria = 1;
        }
        elseif (($tags["tourism"] ?? "") === "museum") {
            $idCategoria = 2;
        }
        elseif (($tags["leisure"] ?? "") === "park") {
            $idCategoria = 3;
        }

        if ($idCategoria === null) {
            continue;
        }

        // Dirección
        $direccion = "";

        if (isset($tags["addr:street"])) {

            $direccion = $tags["addr:street"];

            if (isset($tags["addr:housenumber"])) {
                $direccion .= " " . $tags["addr:housenumber"];
            }
        }

        // Descripción temporal
        switch ($idCategoria) {

            case 1:
                $descripcion = "Restaurante ubicado en Jalisco.";
                break;

            case 2:
                $descripcion = "Museo ubicado en Jalisco.";
                break;

            case 3:
                $descripcion = "Parque ubicado en Jalisco.";
                break;

            default:
                $descripcion = "";
        }

        // Verificar duplicado

        $stmt = mysqli_prepare(
            $conn,
            "SELECT id
            FROM lugares
            WHERE nombre = ?
            AND latitud = ?
            AND longitud = ?"
        );

        mysqli_stmt_bind_param(
            $stmt,
            "sdd",
            $nombre,
            $latitud,
            $longitud
        );

        mysqli_stmt_execute($stmt);

        $result = mysqli_stmt_get_result($stmt);

        if (mysqli_num_rows($result) > 0) {
            $omitidos++;
            mysqli_stmt_close($stmt);
            continue;
        }

        mysqli_stmt_close($stmt);

        // Insertar
        $stmt = mysqli_prepare(
            $conn,
            "INSERT INTO lugares
            (
                nombre,
                descripcion,
                direccion,
                latitud,
                longitud,
                imagen_url,
                video_url,
                calificacion,
                id_categoria
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );

        mysqli_stmt_bind_param(
            $stmt,
            "sssddssdi",
            $nombre,
            $descripcion,
            $direccion,
            $latitud,
            $longitud,
            $imagenUrl,
            $videoUrl,
            $calificacion,
            $idCategoria
        );

        $restaurantes = 0;
        $museos = 0;
        $parques = 0;

        foreach ($data["elements"] as $element) {

            $tags = $element["tags"] ?? [];

            if (($tags["amenity"] ?? "") === "restaurant")
                $restaurantes++;

            if (($tags["tourism"] ?? "") === "museum")
                $museos++;

            if (($tags["leisure"] ?? "") === "park")
                $parques++;
        }

        mysqli_stmt_execute($stmt);

    mysqli_stmt_close($stmt);

        $insertados++;
    }

    echo json_encode([
        "success" => true,
        "insertados" => $insertados,
        "omitidos" => $omitidos
    ]);

}
catch (Exception $e) {

    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ]);
}
?>