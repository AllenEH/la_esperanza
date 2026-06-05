# Backend REST API - La Esperanza

Sistema REST API para gestión de comercialización agrícola. Backend basado en Spring Boot 3.2 con Java 17.

## 📋 Requisitos Previos

- **Java**: 17 o superior
- **Maven**: 3.9+
- **Docker**: 20.10+ (para containerización)
- **PostgreSQL**: 14+ (local) o usar Docker Compose
- **Git**: Para control de versiones

## 🚀 Inicio Rápido

### 1. Desarrollo Local (sin Docker)

```bash
# Clonar repositorio
git clone https://github.com/usuario/la-esperanza.git
cd la-esperanza/backend

# Copiar configuración
cp .env.example .env
# Editar .env con tus valores

# Compilar
mvn clean package

# Ejecutar
java -jar target/la-esperanza-backend-1.0.0.jar
```

API estará disponible en: `http://localhost:8080/api`
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### 2. Con Docker Compose

```bash
# Desde directorio /backend
docker-compose up -d

# Ver logs
docker-compose logs -f backend

# Detener
docker-compose down
```

**Servicios levantados:**
- Backend API: `http://localhost:8080/api`
- PostgreSQL: `localhost:5432` (usuario: postgres, password: postgres)
- pgAdmin: `http://localhost:5050` (usuario: admin@laesperanza.com, password: admin)
- Nginx Frontend: `http://localhost` (opcional)

## 📁 Estructura del Proyecto

```
backend/
├── src/main/java/com/laesperanza/backend/
│   ├── LaEsperanzaApplication.java          # Clase principal
│   ├── controller/                          # Controladores REST
│   │   ├── AuthController.java              # Login/Registro
│   │   └── ProductoController.java          # CRUD Productos
│   ├── entity/                              # Entidades JPA
│   │   ├── Usuario.java
│   │   ├── Producto.java
│   │   ├── Pedido.java
│   │   ├── Categoria.java
│   │   └── Calificacion.java
│   ├── repository/                          # Repositorios JPA
│   ├── service/                             # Lógica de negocio
│   │   ├── AuthService.java                 # Autenticación
│   │   ├── ProductoService.java             # Gestión de productos
│   │   └── AuditoriaService.java            # Logs de auditoría
│   ├── security/                            # Seguridad JWT
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthenticationFilter.java
│   ├── config/                              # Configuración
│   │   ├── SecurityConfig.java
│   │   └── OpenApiConfig.java
│   └── dto/                                 # Data Transfer Objects
├── src/main/resources/
│   ├── application.properties               # Configuración Spring
│   └── application-dev.properties           # Config desarrollo
├── src/test/java/                           # Tests unitarios
├── pom.xml                                  # Dependencias Maven
├── Dockerfile                               # Imagen Docker
├── docker-compose.yml                       # Orquestación contenedores
├── init-db.sql                              # Script inicialización BD
├── nginx.conf                               # Configuración Nginx
├── .env.example                             # Variables de entorno ejemplo
└── README.md                                # Este archivo
```

## 🔌 Endpoints de la API

### Autenticación
```
POST /auth/login                    # Login con teléfono + código SMS
POST /auth/registrar                # Registrar nuevo usuario
GET  /auth/validar-token            # Validar JWT token
```

### Productos
```
GET    /productos                   # Listar productos disponibles
GET    /productos/buscar?query=...  # Buscar por nombre/descripción
GET    /productos/categoria/{id}    # Productos por categoría
GET    /productos/usuario/{id}      # Productos de un usuario
POST   /productos                   # Crear producto (requiere JWT)
PUT    /productos/{id}              # Actualizar producto
DELETE /productos/{id}              # Eliminar producto
```

### Categorías
```
GET /categorias                     # Listar categorías
GET /categorias/{id}                # Obtener categoría
```

### Pedidos
```
GET    /pedidos                     # Listar mis pedidos
POST   /pedidos                     # Crear pedido
PUT    /pedidos/{id}/estado         # Cambiar estado de pedido
```

### Usuarios
```
GET  /usuarios/{id}                 # Perfil público
PUT  /usuarios/{id}                 # Actualizar mi perfil
```

### Admin
```
GET /admin/auditoría                # Ver logs de auditoría
GET /admin/estadísticas             # Estadísticas del sistema
```

## 🔐 Seguridad

### OWASP Top 10 - Medidas Implementadas

✅ **A1: Inyección SQL** - Prepared statements automáticos (JPA)
✅ **A2: Autenticación Rota** - JWT con expiración + validación
✅ **A3: Exposición de Datos** - HTTPS, no guardar datos sensibles
✅ **A5: Control de Acceso** - Autorización por roles
✅ **A6: Configuración Insegura** - Validación de entrada con Bean Validation
✅ **A9: Componentes Inseguros** - Dependencias actualizadas
✅ **A10: Registro Insuficiente** - Auditoría implementada

### Headers de Seguridad

```
Strict-Transport-Security: max-age=31536000
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
```

### Autenticación JWT

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "telefono": "50212345678",
    "codigo": "1234"
  }'

# Respuesta incluye token
# {
#   "token": "eyJhbGc...",
#   "expiresIn": 1800000
# }

# Usar token en requests
curl -X GET http://localhost:8080/api/productos \
  -H "Authorization: Bearer eyJhbGc..."
```

## 📊 Base de Datos

### Schema

```sql
-- Usuarios (productores, compradores, admins)
-- Categorías de productos
-- Productos (con validaciones)
-- Pedidos (flujo: Pendiente → Aceptado → Entregado)
-- Calificaciones (sistema de reputación)
```

### Inicialización

El archivo `init-db.sql` se ejecuta automáticamente con Docker Compose.

Para ejecutar manualmente:
```bash
psql -U postgres -d la_esperanza -f init-db.sql
```

## 🧪 Testing

```bash
# Ejecutar tests unitarios
mvn test

# Con cobertura
mvn test jacoco:report

# Verificar reporte en target/site/jacoco/index.html
```

## 📦 Compilación y Empaquetado

```bash
# Compilación estándar
mvn clean package

# Con perfil de producción
mvn clean package -P production

# Saltar tests
mvn clean package -DskipTests
```

Genera JAR ejecutable en: `target/la-esperanza-backend-1.0.0.jar`

## 🐳 Docker

### Construir imagen

```bash
docker build -t la-esperanza/backend:1.0.0 .
```

### Ejecutar contenedor

```bash
docker run -d \
  -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_PASSWORD=tu-password \
  -e JWT_SECRET=tu-secret-seguro \
  --name backend \
  la-esperanza/backend:1.0.0
```

### Con Docker Compose (recomendado)

```bash
# Desarrollo
docker-compose up -d

# Producción (con pgAdmin)
docker-compose --profile dev up -d

# Ver logs
docker-compose logs -f backend

# Detener
docker-compose down

# Limpiar volúmenes
docker-compose down -v
```

## 🚢 Despliegue en VPS

### 1. Preparar servidor

```bash
# SSH a tu VPS
ssh usuario@tu-vps.com

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 2. Desplegar aplicación

```bash
# Clonar repositorio
git clone https://github.com/usuario/la-esperanza.git
cd la-esperanza/backend

# Configurar variables de entorno
cp .env.example .env
nano .env  # Editar con valores de producción

# Iniciar servicios
docker-compose up -d

# Verificar estado
docker-compose ps
```

### 3. Configurar certificado SSL

```bash
# Instalar Certbot
sudo apt-get install certbot python3-certbot-nginx

# Obtener certificado (Let's Encrypt)
sudo certbot certonly --nginx -d api.laesperanza.com -d laesperanza.com
```

### 4. Configurar Nginx (proxy inverso)

```nginx
# /etc/nginx/sites-available/la-esperanza
upstream backend {
    server 127.0.0.1:8080;
}

server {
    listen 443 ssl http2;
    server_name api.laesperanza.com;

    ssl_certificate /etc/letsencrypt/live/api.laesperanza.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.laesperanza.com/privkey.pem;

    location / {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 📈 Monitoreo

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/api/actuator/health

# Métricas
curl http://localhost:8080/api/actuator/metrics

# Info de la aplicación
curl http://localhost:8080/api/actuator/info
```

### Logs

```bash
# Ver logs en Docker
docker-compose logs -f backend

# Con tail
docker exec -f la-esperanza-api tail -f /var/log/app.log
```

## 🔧 Troubleshooting

### Conexión a BD rechazada
```bash
# Verificar que Postgres está corriendo
docker-compose ps

# Ver logs de Postgres
docker-compose logs postgres
```

### Puerto 8080 ya en uso
```bash
# Cambiar puerto en docker-compose.yml
ports:
  - "8081:8080"  # 8081 es el nuevo puerto
```

### JWT token inválido
- Verificar que `JWT_SECRET` es el mismo
- Verificar que token no está expirado
- Verificar formato: `Bearer <token>`

## 📝 Variables de Entorno

Ver [.env.example](.env.example) para referencia completa.

Principales:
- `DB_HOST`: Host de PostgreSQL
- `DB_USER`/`DB_PASSWORD`: Credenciales BD
- `JWT_SECRET`: Clave secreta para firmar tokens (mínimo 32 caracteres)
- `CORS_ORIGINS`: Orígenes permitidos
- `JAVA_OPTS`: Opciones JVM

## 📚 Documentación Adicional

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Docker Docs](https://docs.docker.com/)

## 📄 Licencia

MIT - Libre para usar y modificar

## 👥 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Fork el repositorio
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📞 Soporte

- Documentación: [README.md](../README.md)
- Estándares: [STANDARDS.md](../STANDARDS.md)
- Problemas: [GitHub Issues](https://github.com/usuario/la-esperanza/issues)
