-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 26-03-2026 a las 22:56:57
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `ds9p1`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categoria`
--

CREATE TABLE `categoria` (
  `idCategoria` int(11) NOT NULL,
  `nombreCat` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `categoria`
--

INSERT INTO `categoria` (`idCategoria`, `nombreCat`) VALUES
(1, 'discos duros'),
(2, 'fuentes de poder'),
(3, 'memorias ram'),
(4, 'procesadores'),
(5, 'tarjetas madres');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `empleado`
--

CREATE TABLE `empleado` (
  `usuario` varchar(20) NOT NULL,
  `nombre` varchar(25) DEFAULT NULL,
  `apellido` varchar(25) DEFAULT NULL,
  `rol` int(11) NOT NULL,
  `contrasena` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `empleado`
-- Contraseñas hasheadas con BCrypt (cost=12):
--   admin    → admin123
--   empleado → empleado123
--

INSERT INTO `empleado` (`usuario`, `nombre`, `apellido`, `rol`, `contrasena`) VALUES
('admin',    'admin',    'user', 1, '$2y$12$zr7AY.pRLFrzRFZty59sWuag2RCmFHvgfWNarQtRGWcg./qoeymD2'),
('empleado', 'empleado', 'user', 2, '$2y$12$kfT7s1QN5ekBXUy/QTmSqenTJVjAWyk3xzs8mM3pXCtVCzcmU25/u');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `factura`
--

CREATE TABLE `factura` (
  `idFactura` int(11) NOT NULL,
  `idTarjeta` int(11) NOT NULL,
  `subtotal` decimal(20,4) NOT NULL,
  `itbms` decimal(20,4) NOT NULL,
  `total` decimal(20,4) NOT NULL,
  `fecha` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `factura_detalle`
--

CREATE TABLE `factura_detalle` (
  `idFacDet` int(11) NOT NULL,
  `idFactura` int(11) NOT NULL,
  `idProducto` bigint(20) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `precio_unitario` decimal(20,4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `marca`
--

CREATE TABLE `marca` (
  `idMarca` int(11) NOT NULL,
  `nombreMarc` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `marca`
--

INSERT INTO `marca` (`idMarca`, `nombreMarc`) VALUES
(1, 'KINGSTON'),
(2, 'CRUCIAL'),
(3, 'XYZ'),
(4, 'G-SKILL'),
(5, 'INTEL'),
(6, 'AMD'),
(7, 'GIGABYTE'),
(8, 'MSI');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

CREATE TABLE `productos` (
  `idProducto` bigint(20) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `unidad` varchar(20) NOT NULL,
  `descripcion` varchar(250) NOT NULL,
  `stock` int(11) NOT NULL,
  `precioCosto` decimal(10,2) NOT NULL,
  `precioVenta` decimal(10,2) NOT NULL,
  `imagen` varchar(500) DEFAULT NULL,
  `idCategoria` int(11) NOT NULL,
  `idMarca` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`idProducto`, `nombre`, `unidad`, `descripcion`, `stock`, `precioCosto`, `precioVenta`, `imagen`, `idCategoria`, `idMarca`) VALUES
(7701010100010, 'Kingston KC600 512GB', 'UND', 'SSD 2.5 de 512GB, SATA III, TLC 3D NAND, lectura 550MB/s y escritura 520MB/s, incluye Acronis.', 10, 127.42, 149.90, 'kingston_kc600_512gb.jpg', 1, 1),
(7701010100027, 'Kingston KC600 256GB', 'UND', 'SSD 2.5 de 256GB, SATA III, TLC 3D NAND, lectura 550MB/s y escritura 500MB/s, incluye Acronis.', 10, 93.42, 109.90, 'kingston_kc600_256gb.jpg', 1, 1),
(7701010300014, 'Kingston SEDC600M 1.92TB', 'UND', 'SSD empresarial de 1.92TB, lectura 560MB/s, escritura 530MB/s, TLC 3D, SATA 3 6Gb/s.', 10, 594.91, 699.90, 'kingston_sedc600m_192tb.jpg', 1, 1),
(7701020200014, 'Crucial T700 Pro 2TB', 'UND', 'SSD M.2 de 2TB, PCIe Gen5, lectura 11800MB/s y escritura 12400MB/s, incluye disipador.', 10, 305.91, 359.90, 'crucial_t700_pro_2tb.jpg', 1, 2),
(7701020200021, 'Crucial T705 1TB', 'UND', 'SSD M.2 de 1TB, PCIe Gen5, lectura 13600MB/s y escritura 10200MB/s, incluye 1 mes de Creative Cloud.', 10, 178.41, 209.90, 'crucial_t705_1tb.jpg', 1, 2),
(7702030100011, 'XYZ PCX 550W', 'UND', 'Fuente de poder de 550W, color negro.', 10, 24.99, 29.40, 'xyz_pcx_550w.jpg', 2, 3),
(7702030100028, 'XYZ PCX 450W', 'UND', 'Fuente de poder de 450W, color negro.', 10, 21.25, 25.00, 'xyz_pcx_450w.jpg', 2, 3),
(7702030200018, 'XYZ Volt One 1000W', 'UND', 'Fuente de poder 1000W, 80 Plus Bronze, 1x24Pin, 1x4+4Pin, 4x PCI-E 6+, 8x SATA.', 10, 62.43, 73.45, 'xyz_volt_one_1000w.jpg', 2, 3),
(7702030200025, 'XYZ Volt One 850W', 'UND', 'Fuente de poder 850W, 80 Plus Bronze, 1x24Pin, 1x4+4Pin, 4x PCI-E 6+, 8x SATA.', 10, 45.77, 53.85, 'xyz_volt_one_850w.jpg', 2, 3),
(7702030200032, 'XYZ Volt One 750W', 'UND', 'Fuente de poder 750W, 80 Plus Bronze, 1x24Pin, 1x4+4Pin, 4x PCI-E 6+, 8x SATA.', 10, 42.07, 49.50, 'xyz_volt_one_750w.jpg', 2, 3),
(7703010100014, 'Kingston Value RAM 8GB DDR4', 'UND', 'Memoria RAM de 8GB DDR4 3200MHz, PC4-25600.', 10, 59.41, 69.89, 'kingston_value_ram_8gb_ddr4.jpg', 3, 1),
(7703010100021, 'Kingston KCP432NS6/8', 'UND', 'Memoria RAM DDR4 de 8GB a 3200MT/s, CL22, UDIMM, Non-ECC, 1Rx16, 1.2V.', 10, 59.42, 69.90, 'kingston_kcp432ns6_8.jpg', 3, 1),
(7703010100038, 'Kingston KCP556US6-8', 'UND', 'Memoria RAM DDR5 de 8GB a 5600MT/s, CL46, UDIMM, Non-ECC, 1Rx16, 1.1V.', 10, 110.42, 129.90, 'kingston_kcp556us6_8.jpg', 3, 1),
(7703010100045, 'Kingston KVR56U46BS8-16', 'UND', 'Memoria RAM DDR5 de 16GB a 5600MT/s, CL46, UDIMM, Non-ECC, 1.1V.', 10, 195.41, 229.90, 'kingston_kvr56u46bs8_16.jpg', 3, 1),
(7703040400016, 'G-Skill Trident Z5 Royal 32GB', 'UND', 'Memoria RAM DDR5 32GB (2x16GB) a 7200MT/s, CL34-45-45-115, UDIMM, Intel XMP 3.0, 1.10V.', 10, 509.91, 599.90, 'gskill_trident_z5_royal_32gb.jpg', 3, 4),
(7704050500017, 'Intel Core Ultra 5 245K', 'UND', 'Procesador 4.20GHz, hasta 5.20GHz Max Boost, Arrow Lake, LGA 1851, 14 nucleos, 14 hilos, 24MB cache, Intel Xe Cores.', 10, 219.30, 258.00, 'intel_core_ultra_5_245k.jpg', 4, 5),
(7704050500024, 'Intel Core i7-14700', 'UND', 'Procesador 4.2GHz, Raptor Lake 14th Gen, LGA1700, 20 nucleos, 28 hilos, 33MB cache, Intel UHD 770.', 10, 339.91, 399.90, 'intel_core_i7_14700.jpg', 4, 5),
(7704060600011, 'AMD Ryzen 5 7600X', 'UND', 'Procesador 4.7GHz, hasta 5.3GHz Max, socket AM5, 6 nucleos, 12 hilos, 38MB cache, no incluye disipador.', 10, 195.41, 229.89, 'amd_ryzen_5_7600x.jpg', 4, 6),
(7704060600028, 'AMD Ryzen 9 9900X3D', 'UND', 'Procesador socket AM5, 12 nucleos, 24 hilos, 140MB cache, no incluye disipador.', 10, 565.25, 665.00, 'amd_ryzen_9_9900x3d.jpg', 4, 6),
(7704060600035, 'AMD Ryzen 7 9800X3D', 'UND', 'Procesador socket AM5, 8 nucleos, 16 hilos, 104MB cache, no incluye disipador.', 10, 474.72, 558.50, 'amd_ryzen_7_9800x3d.jpg', 4, 6),
(7705070700012, 'Gigabyte Z890 UD WIFI6E', 'UND', 'Tarjeta madre ATX LGA 1851, 4 slots DDR5 hasta 9200MHz+ OC, 3 M.2, PCIe Gen5, 2.5GbE LAN, WiFi 6E, BT 5.3.', 10, 225.80, 265.65, 'gigabyte_z890_ud_wifi6e.jpg', 5, 7),
(7705070700029, 'Gigabyte Z790 UD AX', 'UND', 'Tarjeta madre ATX LGA 1700, 4 slots DDR5 hasta 7600MHz+ OC, 2.5G LAN, WiFi 6E, Bluetooth 5.3, PCIe Gen5, 3 M.2.', 10, 284.69, 334.93, 'gigabyte_z790_ud_ax.jpg', 5, 7),
(7705080800016, 'MSI Pro H610M-A DDR4', 'UND', 'Tarjeta madre M-ATX LGA 1700, 2 slots DDR4, 1 M.2, PCIe Gen4, 2.5G LAN, HDMI y DisplayPort.', 10, 76.47, 89.97, 'msi_pro_h610m_a_ddr4.jpg', 5, 8),
(7705080800023, 'MSI H510M Plus II', 'UND', 'Tarjeta madre M-ATX LGA 1200, compatible 10ma y 11va gen, 2 slots DDR4, 1 M.2, PCIe Gen3, HDMI y VGA.', 10, 62.42, 73.44, 'msi_h510m_plus_ii.jpg', 5, 8),
(7705080800030, 'MSI Pro B760M-E', 'UND', 'Tarjeta madre M-ATX LGA 1700, 2 slots DDR5, PCIe Gen4, 1 M.2, 1G LAN, HDMI y VGA.', 10, 111.02, 130.61, 'msi_pro_b760m_e.jpg', 5, 8);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tarjeta`
--

CREATE TABLE `tarjeta` (
  `idTarjeta` int(11) NOT NULL,
  `tipo` varchar(7) NOT NULL,
  `digitos` varchar(16) NOT NULL,
  `fechaVence` date NOT NULL,
  `codSeguridad` varchar(4) NOT NULL,
  `saldo` decimal(10,2) NOT NULL,
  `saldoMaximo` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `categoria`
--
ALTER TABLE `categoria`
  ADD PRIMARY KEY (`idCategoria`);

--
-- Indices de la tabla `empleado`
--
ALTER TABLE `empleado`
  ADD PRIMARY KEY (`usuario`),
  ADD UNIQUE KEY `contrasena` (`contrasena`);

--
-- Indices de la tabla `factura`
--
ALTER TABLE `factura`
  ADD PRIMARY KEY (`idFactura`),
  ADD KEY `fk_idTarjeta` (`idTarjeta`);

--
-- Indices de la tabla `factura_detalle`
--
ALTER TABLE `factura_detalle`
  ADD PRIMARY KEY (`idFacDet`),
  ADD KEY `fk_idFactura` (`idFactura`),
  ADD KEY `fk_idProducto` (`idProducto`);

--
-- Indices de la tabla `marca`
--
ALTER TABLE `marca`
  ADD PRIMARY KEY (`idMarca`);

--
-- Indices de la tabla `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`idProducto`),
  ADD KEY `fk_producto_categoria` (`idCategoria`),
  ADD KEY `fk_producto_marca` (`idMarca`);

--
-- Indices de la tabla `tarjeta`
--
ALTER TABLE `tarjeta`
  ADD PRIMARY KEY (`idTarjeta`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `categoria`
--
ALTER TABLE `categoria`
  MODIFY `idCategoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `factura`
--
ALTER TABLE `factura`
  MODIFY `idFactura` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `factura_detalle`
--
ALTER TABLE `factura_detalle`
  MODIFY `idFacDet` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `marca`
--
ALTER TABLE `marca`
  MODIFY `idMarca` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `tarjeta`
--
ALTER TABLE `tarjeta`
  MODIFY `idTarjeta` int(11) NOT NULL AUTO_INCREMENT;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `factura`
--
ALTER TABLE `factura`
  ADD CONSTRAINT `fk_idTarjeta` FOREIGN KEY (`idTarjeta`) REFERENCES `tarjeta` (`idTarjeta`);

--
-- Filtros para la tabla `factura_detalle`
--
ALTER TABLE `factura_detalle`
  ADD CONSTRAINT `fk_idFactura` FOREIGN KEY (`idFactura`) REFERENCES `factura` (`idFactura`),
  ADD CONSTRAINT `fk_idProducto` FOREIGN KEY (`idProducto`) REFERENCES `productos` (`idProducto`);

--
-- Filtros para la tabla `productos`
--
ALTER TABLE `productos`
  ADD CONSTRAINT `fk_producto_categoria` FOREIGN KEY (`idCategoria`) REFERENCES `categoria` (`idCategoria`),
  ADD CONSTRAINT `fk_producto_marca` FOREIGN KEY (`idMarca`) REFERENCES `marca` (`idMarca`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
