# 🛒 Tienda Virtual — Kotlin Android + PHP

Aplicación móvil de tienda virtual de componentes de PC desarrollada en **Kotlin (Android)** con backend en **PHP** y base de datos **MySQL (MariaDB)** servida desde **XAMPP**.

---

## 📱 Características

### Vista Cliente (sin login)
- Catálogo de productos con búsqueda y filtros por categoría, marca y rango de precio
- Detalle de producto con imagen, descripción y stock
- Carrito de compras con contador de unidades en tiempo real
- Proceso de pago con selección de método (Visa / Mastercard — Crédito / Débito)
- Historial de pedidos propio del dispositivo (sin cuenta requerida)
- Sección de favoritos

### Panel Administrador
- Dashboard con métricas: ventas, ingresos, stock bajo, empleados
- Gráfico de ingresos por período (2 semanas / mes / 3 meses / año)
- CRUD completo de productos con imagen (local o URL externa)
- CRUD completo de empleados con contraseñas hasheadas (BCrypt)
- Gestión de categorías
- Historial de ventas con detalle de factura

### Panel Empleado
- Vista de inventario (solo lectura)
- Detalle de productos

---

## 🗂️ Estructura del proyecto

```
tienda_virtual_kotlin_android/
├── app/src/main/
│   ├── java/com/david/tiendavirtual/
│   │   ├── data/
│   │   │   ├── model/          # Data classes (Producto, CarritoItem, PedidoDB…)
│   │   │   ├── network/        # ApiConfig (URLs del backend)
│   │   │   └── repository/     # Repositorios (Volley + PHP)
│   │   ├── ui/
│   │   │   ├── admin/          # Panel administrador (fragmentos + actividades)
│   │   │   ├── carrito/        # CarritoActivity
│   │   │   ├── empleado/       # Panel empleado
│   │   │   ├── favoritos/      # FavoritosActivity
│   │   │   ├── home/           # MainActivity (catálogo cliente)
│   │   │   ├── login/          # LoginActivity
│   │   │   ├── pago/           # PagoActivity + ConfirmacionPagoActivity
│   │   │   ├── pedidos/        # OrderHistoryActivity
│   │   │   ├── perfil/         # ProfileActivity
│   │   │   ├── productos/      # Adapters + ProductoDetalleActivity
│   │   │   ├── search/         # SearchActivity + SearchFilterSheet
│   │   │   └── settings/       # SettingsActivity
│   │   └── utils/
│   │       ├── Constants.kt        # BASE_URL e IMAGES_URL
│   │       └── LocalOrderStore.kt  # Pedidos locales en SharedPreferences
│   └── res/
│       ├── layout/             # 35 layouts XML
│       └── drawable/           # 68 drawables (íconos y fondos)
│
└── backend_php/
    ├── conexion.php            # ⚠️ Credenciales BD (NO subir al repo)
    ├── login.php
    ├── productos.php
    ├── catalogo.php
    ├── factura.php
    ├── pedidos.php
    ├── tarjeta.php
    ├── admin_producto.php
    ├── admin_empleados.php
    ├── admin_dashboard.php
    ├── admin_finanzas.php
    ├── admin_ventas.php
    ├── admin_categoria.php
    └── mis_tarjetas.php
```

---

## ⚙️ Requisitos

| Herramienta | Versión mínima |
|---|---|
| Android Studio | Hedgehog 2023.1+ |
| Kotlin | 1.9+ |
| Android SDK (compileSdk) | 34 |
| Android mínimo (minSdk) | 24 (Android 7.0) |
| XAMPP | 8.x (Apache + MySQL) |
| PHP | 8.2+ |
| MariaDB | 10.4+ |

---

## 🚀 Instalación y configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/tienda_virtual_kotlin_android.git
```

### 2. Configurar el backend

1. Copia la carpeta `backend_php/` dentro de `C:\xampp\htdocs\tienda_virtual_kotlin_android\`
2. Crea el archivo `backend_php/conexion.php` (está en el `.gitignore`):

```php
<?php
$servidor   = "localhost";
$usuario_bd = "root";
$contrasena = "";          // tu contraseña de MySQL
$base_datos = "ds9p1";

$conexion = mysqli_connect($servidor, $usuario_bd, $contrasena, $base_datos);

if (!$conexion) {
    http_response_code(500);
    die(json_encode(["error" => "Error de conexión: " . mysqli_connect_error()]));
}
mysqli_set_charset($conexion, "utf8mb4");
```

3. Importa el esquema de base de datos en phpMyAdmin:
   - Abre `http://localhost/phpmyadmin`
   - Crea la base de datos `ds9p1`
   - Importa el archivo `backend_php/ds9p1.sql`

### 3. Configurar la URL del backend en Android

No edites `Constants.kt`. La app toma `BASE_URL` desde `BuildConfig`.

Define la URL en `local.properties` (no se sube al repo):

```properties
BACKEND_BASE_URL=http://TU_IP_LOCAL/tienda_virtual_kotlin_android/backend_php/
```

Opcionalmente puedes usar variable de entorno `BACKEND_BASE_URL` (tiene prioridad sobre `local.properties`).

> **¿Cómo saber tu IP?** Ejecuta `ipconfig` y usa la IPv4 de tu red WiFi/Ethernet.

### 4. Agregar la IP al Network Security Config

En `app/src/main/res/xml/network_security_config.xml`, agrega tu IP:

```xml
<domain includeSubdomains="true">TU_IP_LOCAL</domain>
```

### 5. Compilar y ejecutar

- Abre el proyecto en **Android Studio**
- Espera que sincronice el Gradle
- Conecta tu dispositivo Android o usa el emulador
- Presiona ▶ **Run**

---

## 👥 Roles de usuario

| Rol | Acceso |
|---|---|
| **Invitado** | Catálogo, carrito, pago, mis pedidos |
| **Empleado** (rol = 2) | Panel empleado: inventario y detalle de productos |
| **Administrador** (rol = 1) | Panel admin completo: productos, empleados, finanzas, ventas, categorías |

### Credenciales de prueba por defecto
```
Admin:    usuario: admin      contraseña: admin123
Empleado: usuario: empleado   contraseña: empleado123
```

---

## 📦 Dependencias principales

| Librería | Uso |
|---|---|
| `Volley 1.2.1` | Peticiones HTTP al backend PHP |
| `Glide 4.16.0` | Carga de imágenes (local y URLs externas) |
| `MPAndroidChart 3.1.0` | Gráfico de ingresos en finanzas admin |
| `Material Components 1.12.0` | UI: chips, cards, bottom sheets, snackbars |
| `ViewBinding` | Acceso seguro a vistas en XML |

---

## 🔐 Seguridad

- Las contraseñas de empleados se almacenan con **BCrypt** (cost = 12) — nunca en texto plano
- El archivo `conexion.php` está excluido del repositorio (`.gitignore`)
- Los pedidos de clientes invitados se identifican por `SharedPreferences` locales del dispositivo (sin necesidad de cuenta)

---

## 🖼️ Imágenes de productos

Las imágenes pueden ser:
- **Archivos locales**: guarda el `.jpg/.png` en `backend_php/productos/` y pon solo el nombre del archivo en la BD (`kingston_kc600.jpg`)
- **URL externa**: pega la URL directa de la imagen en la BD (`https://i.postimg.cc/xxx/imagen.jpg`)

> ⚠️ Usa el **direct link** de postimg (`i.postimg.cc`), no la página de galería (`postimg.cc`).

---

## 📄 Licencia

Proyecto académico — uso educativo.
