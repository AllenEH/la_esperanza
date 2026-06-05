package com.laesperanza.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Producto
 * Cumple con validaciones OWASP: entrada validada, inyección SQL prevenida con prepared statements
 */
@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_usuario", columnList = "id_usuario"),
    @Index(name = "idx_categoria", columnList = "id_categoria"),
    @Index(name = "idx_nombre", columnList = "nombre"),
    @Index(name = "idx_activo", columnList = "activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Nombre es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @Column(length = 2)
    private String imagen;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Precio es requerido")
    @DecimalMin(value = "0.01", message = "Precio debe ser mayor a 0")
    private BigDecimal precio;

    @Column(nullable = false)
    @NotNull(message = "Cantidad es requerida")
    @Min(value = 0, message = "Cantidad no puede ser negativa")
    private Integer cantidad;

    @Column(length = 500)
    @Size(max = 500, message = "Descripción no puede exceder 500 caracteres")
    private String descripcion;

    @Column(nullable = false)
    private Boolean disponible = true;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    @Column
    private LocalDateTime fechaModificacion;

    @Column
    private LocalDateTime fechaEliminacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @PrePersist
    protected void onCreate() {
        this.fechaPublicacion = LocalDateTime.now();
        this.disponible = true;
        this.activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
