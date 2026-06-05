package com.laesperanza.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO para Login - Validación de entrada (OWASP A1)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Teléfono es requerido")
    @Pattern(regexp = "^[\\d\\s\\-\\+\\(\\)]{7,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    @NotBlank(message = "Código SMS es requerido")
    @Pattern(regexp = "^\\d{4,6}$", message = "Código debe ser 4-6 dígitos")
    private String codigo;
}

/**
 * DTO para Registro - Validación de entrada
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class RegistroRequest {

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "Teléfono es requerido")
    @Pattern(regexp = "^[\\d\\s\\-\\+\\(\\)]{7,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    @Pattern(regexp = "^\\d{13}$", message = "DPI debe tener 13 dígitos")
    private String dpi;

    @NotNull(message = "Rol es requerido")
    private String rol; // PRODUCTOR, COMPRADOR

    @Email(message = "Email debe ser válido")
    private String email;
}

/**
 * DTO para respuesta de autenticación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AuthResponse {
    private Long idUsuario;
    private String nombre;
    private String rol;
    private String token;
    private String refreshToken;
    private Long expiresIn;
}

/**
 * DTO para crear/actualizar Producto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProductoRequest {

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotNull(message = "Precio es requerido")
    @DecimalMin(value = "0.01", message = "Precio debe ser mayor a 0")
    private java.math.BigDecimal precio;

    @NotNull(message = "Cantidad es requerida")
    @Min(value = 0, message = "Cantidad no puede ser negativa")
    private Integer cantidad;

    @NotNull(message = "Categoría es requerida")
    private Long idCategoria;

    @Size(max = 500, message = "Descripción no puede exceder 500 caracteres")
    private String descripcion;

    private String imagen;
}

/**
 * DTO para crear Pedido
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PedidoRequest {

    @NotNull(message = "ID de producto es requerido")
    private Long idProducto;

    @NotNull(message = "Cantidad es requerida")
    @Min(value = 1, message = "Cantidad mínima es 1")
    private Integer cantidad;

    private String comentario;
}

/**
 * DTO para crear Calificación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CalificacionRequest {

    @NotNull(message = "ID de usuario es requerido")
    private Long idUsuario;

    @NotNull(message = "Puntuación es requerida")
    @Min(value = 1, message = "Puntuación mínima es 1")
    @Max(value = 5, message = "Puntuación máxima es 5")
    private Integer puntuacion;

    @Size(max = 500, message = "Comentario no puede exceder 500 caracteres")
    private String comentario;
}

/**
 * DTO para respuesta de Producto (lectura)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProductoResponse {
    private Long idProducto;
    private String nombre;
    private java.math.BigDecimal precio;
    private Integer cantidad;
    private String imagen;
    private String descripcion;
    private String nombreCategoria;
    private Long idCategoria;
    private Long idUsuario;
    private String nombreUsuario;
    private java.time.LocalDateTime fechaPublicacion;
    private Boolean disponible;
}

/**
 * DTO para respuesta de Pedido (lectura)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PedidoResponse {
    private Long idPedido;
    private Long idUsuario;
    private Long idProducto;
    private String nombreProducto;
    private Integer cantidadPedida;
    private String estado;
    private java.time.LocalDateTime fechaPedido;
    private java.time.LocalDateTime fechaEntrega;
    private String comentario;
}

/**
 * DTO para respuesta de Calificación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CalificacionResponse {
    private Long idCalificacion;
    private Integer puntuacion;
    private String comentario;
    private java.time.LocalDateTime fechaCalificacion;
    private Long idUsuario;
    private Long idCalificador;
    private String nombreUsuario;
    private String nombreCalificador;
}

/**
 * DTO para perfil de Usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UsuarioPerfilResponse {
    private Long idUsuario;
    private String nombre;
    private String telefono;
    private String rol;
    private Double reputacion;
    private String foto;
    private java.time.LocalDateTime fechaRegistro;
    private Integer totalProductos;
    private Integer totalPedidos;
    private String ubicacion;
}
