-- init-db.sql - Script de inicialización de Base de Datos
-- La Esperanza - PostgreSQL

-- ============================================================
-- Crear extensiones
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- Tabla: usuarios
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    dpi VARCHAR(13) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('PRODUCTOR', 'COMPRADOR', 'ADMIN')),
    reputacion DECIMAL(3, 2) DEFAULT 5.0,
    foto VARCHAR(50),
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP,
    descripcion VARCHAR(500),
    ubicacion VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuarios_telefono ON usuarios(telefono);
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_usuarios_activo ON usuarios(activo);

-- ============================================================
-- Tabla: categorias
-- ============================================================
CREATE TABLE IF NOT EXISTS categorias (
    id_categoria BIGSERIAL PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NOT NULL,
    icono VARCHAR(10),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_categorias_nombre ON categorias(nombre_categoria);
CREATE INDEX idx_categorias_activo ON categorias(activo);

-- ============================================================
-- Tabla: productos
-- ============================================================
CREATE TABLE IF NOT EXISTS productos (
    id_producto BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    imagen VARCHAR(2),
    precio DECIMAL(10, 2) NOT NULL,
    cantidad INTEGER NOT NULL,
    descripcion VARCHAR(500),
    disponible BOOLEAN DEFAULT TRUE,
    activo BOOLEAN DEFAULT TRUE,
    fecha_publicacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    fecha_eliminacion TIMESTAMP,
    id_usuario BIGINT NOT NULL,
    id_categoria BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria) ON DELETE RESTRICT
);

CREATE INDEX idx_productos_usuario ON productos(id_usuario);
CREATE INDEX idx_productos_categoria ON productos(id_categoria);
CREATE INDEX idx_productos_nombre ON productos(nombre);
CREATE INDEX idx_productos_activo ON productos(activo);

-- ============================================================
-- Tabla: pedidos
-- ============================================================
CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido BIGSERIAL PRIMARY KEY,
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'ACEPTADO', 'RECHAZADO', 'ENTREGADO', 'CANCELADO')),
    cantidad_pedida INTEGER NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_entrega TIMESTAMP,
    comentario VARCHAR(500),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    id_usuario BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
);

CREATE INDEX idx_pedidos_usuario ON pedidos(id_usuario);
CREATE INDEX idx_pedidos_producto ON pedidos(id_producto);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_fecha ON pedidos(fecha_pedido);

-- ============================================================
-- Tabla: calificaciones
-- ============================================================
CREATE TABLE IF NOT EXISTS calificaciones (
    id_calificacion BIGSERIAL PRIMARY KEY,
    puntuacion INTEGER NOT NULL CHECK (puntuacion >= 1 AND puntuacion <= 5),
    comentario VARCHAR(500),
    fecha_calificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_usuario BIGINT NOT NULL,
    id_calificador BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_calificacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_calificacion_calificador FOREIGN KEY (id_calificador) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

CREATE INDEX idx_calificaciones_usuario ON calificaciones(id_usuario);
CREATE INDEX idx_calificaciones_calificador ON calificaciones(id_calificador);

-- ============================================================
-- Inserts de datos iniciales
-- ============================================================

-- Categorías
INSERT INTO categorias (nombre_categoria, descripcion, icono, activo) VALUES
('Verduras', 'Verduras frescas', '🥦', TRUE),
('Frutas', 'Frutas frescas', '🍎', TRUE),
('Granos', 'Granos y cereales', '🌽', TRUE),
('Hierbas', 'Hierbas y especias', '🌿', TRUE),
('Raíces', 'Tubérculos', '🥕', TRUE)
ON CONFLICT DO NOTHING;

-- Usuarios demo
INSERT INTO usuarios (nombre, telefono, email, dpi, rol, reputacion, foto, activo) VALUES
('María López', '50212345678', 'maria@laesperanza.com', '1234567890101', 'PRODUCTOR', 4.8, '👩‍🌾', TRUE),
('Juan Pérez', '50287654321', 'juan@laesperanza.com', '9876543210101', 'COMPRADOR', 4.5, '👨‍🌾', TRUE),
('Ana García', '50211112222', 'ana@laesperanza.com', '1122334455667', 'PRODUCTOR', 4.9, '👩‍🌾', TRUE)
ON CONFLICT (telefono) DO NOTHING;

-- Productos demo
INSERT INTO productos (nombre, imagen, precio, cantidad, descripcion, id_usuario, id_categoria) VALUES
('Tomates Frescos', '🍅', 15.00, 50, 'Tomates maduros del día', 1, 1),
('Maíz Amarillo', '🌽', 8.50, 200, 'Maíz cosechado esta semana', 1, 3),
('Aguacate Hass', '🥑', 25.00, 30, 'Aguacates cremosos', 3, 2),
('Zanahorias', '🥕', 10.00, 80, 'Zanahorias frescas', 3, 5),
('Cilantro Fresco', '🌿', 5.00, 100, 'Cilantro aromático', 1, 4)
ON CONFLICT DO NOTHING;

-- ============================================================
-- Crear función para actualizar updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para actualizar timestamp
CREATE TRIGGER usuarios_update_timestamp BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER categorias_update_timestamp BEFORE UPDATE ON categorias
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER productos_update_timestamp BEFORE UPDATE ON productos
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER pedidos_update_timestamp BEFORE UPDATE ON pedidos
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER calificaciones_update_timestamp BEFORE UPDATE ON calificaciones
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

-- ============================================================
-- Crear usuario para aplicación
-- ============================================================
-- Este usuario será usado por la aplicación para conectarse
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO app_user;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO app_user;

COMMIT;
