# 🌾 Sistema de Gestión y Comercialización La Esperanza

Aplicación web para una comunidad agrícola rural que permite a productores publicar productos y compradores realizar pedidos.

## 🌐 Demo en vivo
> Despliega en GitHub Pages: `https://tu-usuario.github.io/la-esperanza/`

## 📱 Características
- **Mobile First**: Diseñado primero para móvil, funciona perfecto en escritorio
- **Sin backend**: Datos simulados en JSON, listo para GitHub Pages
- **Fácil de usar**: Iconos grandes, poco texto, para usuarios con bajo nivel digital
- **5 pantallas**: Login · Catálogo · Publicar · Historial · Perfil

## 🚀 Despliegue en GitHub Pages

```bash
git init
git add .
git commit -m "🌱 Sistema La Esperanza - primer commit"
git remote add origin https://github.com/TU_USUARIO/la-esperanza.git
git push -u origin main
```

Luego en GitHub: **Settings → Pages → Branch: main → / (root) → Save**

## 🏗️ Estructura del proyecto

```
la-esperanza/
├── index.html          # App principal (SPA)
├── css/
│   └── styles.css      # Estilos completos
├── js/
│   └── app.js          # Lógica de la aplicación
├── data/
│   └── db.json         # Base de datos simulada
└── img/                # Imágenes (se usan emojis como placeholder)
```

## 🗃️ Modelo de Datos

| Entidad      | Campos principales |
|-------------|-------------------|
| Usuario     | id, nombre, telefono, dpi, rol, reputacion |
| Producto    | id, nombre, imagen, precio, cantidad, categoria, usuario |
| Categoría   | id, nombre, descripcion, icono |
| Pedido      | id, fecha, estado, usuario, producto |
| Calificación| id, puntuacion, comentario, usuario |

## 🎨 Paleta de colores

| Color          | Hex       | Uso |
|---------------|-----------|-----|
| Verde oscuro   | `#2d6a4f` | Headers, botones principales |
| Verde principal| `#40916c` | Acentos, activos |
| Verde suave    | `#b7e4c7` | Fondos secundarios |
| Verde pálido   | `#d8f3dc` | Chips, badges |
| Amarillo       | `#f9c74f` | Botón registro |
| Blanco         | `#ffffff` | Fondo tarjetas |

## 📋 Flujo de uso

1. **Ingreso**: Teléfono + código SMS (demo: cualquier teléfono, código `1234`)
2. **Menú principal**: Acceso rápido a todas las secciones
3. **Catálogo**: Filtrar por categoría, buscar, solicitar productos
4. **Publicar**: Subir foto, precio, cantidad, categoría
5. **Historial**: Ver estados de pedidos (Pendiente/Aceptado/Rechazado/Entregado)
6. **Perfil**: Datos personales y calificación promedio

## 🛠️ Tecnologías
- HTML5 · CSS3 · JavaScript puro (Vanilla)
- Google Fonts: Nunito + Fredoka One
- Sin dependencias externas · Sin build step

## 📄 Licencia
MIT — Comunidad Agrícola La Esperanza
