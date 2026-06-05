package com.laesperanza.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad Calificación
 * Sistema de reputación para usuarios (productores)
 */
@Entity
@Table(name = "calificaciones", indexes = {
    @Index(name = "idx_usuario", columnList = "id_usuario"),
    @Index(name = "idx_calificador", columnList = "id_calificador")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCalificacion;

    @Column(nullable = false)
    @NotNull(message = "Puntuación es requerida")
    @Min(value = 1, message = "Puntuación mínima es 1")
    @Max(value = 5, message = "Puntuación máxima es 5")
    private Integer puntuacion;

    @Column(length = 500)
    @Size(max = 500, message = "Comentario no puede exceder 500 caracteres")
    private String comentario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCalificacion = LocalDateTime.now();

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario; // Usuario a quien se califica

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_calificador", nullable = false)
    private Usuario calificador; // Usuario que califica

    @PrePersist
    protected void onCreate() {
        this.fechaCalificacion = LocalDateTime.now();
    }
}
