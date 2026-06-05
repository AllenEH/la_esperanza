package com.laesperanza.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad Categoría de Productos
 */
@Entity
@Table(name = "categorias", indexes = {
    @Index(name = "idx_nombre_categoria", columnList = "nombre_categoria")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    @Column(nullable = false, unique = true, length = 50)
    private String nombreCategoria;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(length = 10)
    private String icono;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
