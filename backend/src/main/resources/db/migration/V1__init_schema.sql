CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    dpi VARCHAR(13) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    reputacion NUMERIC(5,2) NOT NULL DEFAULT 5.0,
    foto VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_registro TIMESTAMP NOT NULL DEFAULT now(),
    ultimo_acceso TIMESTAMP,
    descripcion VARCHAR(500),
    ubicacion VARCHAR(15)
);

CREATE INDEX IF NOT EXISTS idx_telefono ON usuarios(telefono);
CREATE INDEX IF NOT EXISTS idx_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_rol ON usuarios(rol);

CREATE TABLE IF NOT EXISTS categorias (
    id_categoria BIGSERIAL PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200) NOT NULL,
    icono VARCHAR(10),
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    fecha_modificacion TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_nombre_categoria ON categorias(nombre_categoria);

CREATE TABLE IF NOT EXISTS productos (
    id_producto BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio NUMERIC(12,2) NOT NULL,
    cantidad INTEGER NOT NULL DEFAULT 0,
    descripcion VARCHAR(500),
    imagen VARCHAR(255),
    disponible BOOLEAN NOT NULL DEFAULT true,
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_publicacion TIMESTAMP NOT NULL DEFAULT now(),
    fecha_modificacion TIMESTAMP,
    id_categoria BIGINT NOT NULL REFERENCES categorias(id_categoria),
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario)
);

CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(id_categoria);
CREATE INDEX IF NOT EXISTS idx_productos_usuario ON productos(id_usuario);

CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido BIGSERIAL PRIMARY KEY,
    fecha_pedido TIMESTAMP NOT NULL DEFAULT now(),
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE',
    cantidad_pedida INTEGER NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    fecha_entrega TIMESTAMP,
    comentario VARCHAR(500),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    fecha_modificacion TIMESTAMP,
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    id_producto BIGINT NOT NULL REFERENCES productos(id_producto)
);

CREATE INDEX IF NOT EXISTS idx_pedidos_usuario ON pedidos(id_usuario);
CREATE INDEX IF NOT EXISTS idx_pedidos_producto ON pedidos(id_producto);
CREATE INDEX IF NOT EXISTS idx_pedidos_estado ON pedidos(estado);
CREATE INDEX IF NOT EXISTS idx_pedidos_fecha ON pedidos(fecha_pedido);

CREATE TABLE IF NOT EXISTS calificaciones (
    id_calificacion BIGSERIAL PRIMARY KEY,
    puntuacion INTEGER NOT NULL CHECK (puntuacion >= 1 AND puntuacion <= 5),
    comentario VARCHAR(500),
    fecha_calificacion TIMESTAMP NOT NULL DEFAULT now(),
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    id_calificador BIGINT NOT NULL REFERENCES usuarios(id_usuario)
);

CREATE INDEX IF NOT EXISTS idx_calificaciones_usuario ON calificaciones(id_usuario);
CREATE INDEX IF NOT EXISTS idx_calificaciones_calificador ON calificaciones(id_calificador);
