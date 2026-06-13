package com.xploremx.xploremx.models

data class Lugar(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val imagenUrl: String,
    val calificacion: Double,
    val idCategoria: Int
)