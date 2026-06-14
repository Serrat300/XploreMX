-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 13, 2026 at 06:51 PM
-- Server version: 11.7.2-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `xploremx_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `categorias`
--

CREATE TABLE `categorias` (
  `id` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `icono` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `categorias`
--

INSERT INTO `categorias` (`id`, `nombre`, `icono`) VALUES
(1, 'Restaurantes', 'ic_restaurant'),
(2, 'Museos', 'ic_museum'),
(3, 'Parques', 'ic_park'),
(4, 'Tours', 'ic_tour');

-- --------------------------------------------------------

--
-- Table structure for table `lugares`
--

CREATE TABLE `lugares` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `latitud` decimal(10,8) DEFAULT NULL,
  `longitud` decimal(11,8) DEFAULT NULL,
  `imagen_url` varchar(255) DEFAULT NULL,
  `video_url` varchar(255) DEFAULT NULL,
  `calificacion` decimal(2,1) DEFAULT 0.0,
  `id_categoria` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `lugares`
--

INSERT INTO `lugares` (`id`, `nombre`, `descripcion`, `direccion`, `latitud`, `longitud`, `imagen_url`, `video_url`, `calificacion`, `id_categoria`) VALUES
(1, 'Catedral Metropolitana', 'Icónica catedral ubicada en el centro histórico de Guadalajara', 'Av. Alcalde 10, Zona Centro, 44100 Guadalajara', 20.67520000, -103.34740000, 'catedral.jpg', NULL, 4.8, 2),
(2, 'Teatro Degollado', 'Teatro neoclásico símbolo de la cultura tapatía', 'C. Degollado s/n, Zona Centro, 44100 Guadalajara', 20.67380000, -103.34420000, 'teatro.jpg', NULL, 4.7, 2),
(3, 'Tlaquepaque', 'Pueblo mágico famoso por su artesanía y gastronomía', 'El Carmen, 45500 Tlaquepaque', 20.64190000, -103.31280000, 'tlaquepaque.jpg', NULL, 4.9, 1),
(4, 'Bosque Colomos', 'Parque urbano con jardines, lagos y áreas de descanso', 'Circunvalación Jorge Álvarez del Castillo, Guadalajara', 20.68970000, -103.40120000, 'colomos.jpg', NULL, 4.6, 3),
(5, 'Tour Tequila', 'Recorrido por la cuna del tequila mexicano', 'Tequila, Jalisco', 20.87970000, -103.83580000, 'tequila.jpg', NULL, 4.9, 4);

-- --------------------------------------------------------

--
-- Table structure for table `recordatorios`
--

CREATE TABLE `recordatorios` (
  `id` int(11) NOT NULL,
  `titulo` varchar(100) NOT NULL,
  `fecha_hora` datetime NOT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `id_lugar` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `resenas`
--

CREATE TABLE `resenas` (
  `id` int(11) NOT NULL,
  `comentario` text DEFAULT NULL,
  `video_url` varchar(255) DEFAULT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `id_lugar` int(11) DEFAULT NULL,
  `fecha` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `contrasena` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `usuario`, `contrasena`) VALUES
(1, 'Karla Navarro', 'karla', '81dc9bdb52d04dc20036dbd8313ed055'),
(2, 'maria', 'mari', '827ccb0eea8a706c4c34a16891f84e7b');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `lugares`
--
ALTER TABLE `lugares`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_categoria` (`id_categoria`);

--
-- Indexes for table `recordatorios`
--
ALTER TABLE `recordatorios`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `id_lugar` (`id_lugar`);

--
-- Indexes for table `resenas`
--
ALTER TABLE `resenas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `id_lugar` (`id_lugar`);

--
-- Indexes for table `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `usuario` (`usuario`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `lugares`
--
ALTER TABLE `lugares`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `recordatorios`
--
ALTER TABLE `recordatorios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `resenas`
--
ALTER TABLE `resenas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `lugares`
--
ALTER TABLE `lugares`
  ADD CONSTRAINT `lugares_ibfk_1` FOREIGN KEY (`id_categoria`) REFERENCES `categorias` (`id`);

--
-- Constraints for table `recordatorios`
--
ALTER TABLE `recordatorios`
  ADD CONSTRAINT `recordatorios_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `recordatorios_ibfk_2` FOREIGN KEY (`id_lugar`) REFERENCES `lugares` (`id`);

--
-- Constraints for table `resenas`
--
ALTER TABLE `resenas`
  ADD CONSTRAINT `resenas_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `resenas_ibfk_2` FOREIGN KEY (`id_lugar`) REFERENCES `lugares` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
