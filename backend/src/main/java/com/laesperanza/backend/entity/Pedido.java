package com.laesperanza.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad Pedido
 * Flujo: Pendiente → Aceptado → Entregado (o Rechazado)
 */
@Entity
@Table(name = "pedidos", indexes = {
    @Index(name = "idx_usuario", columnList = "id_usuario"),
    @Index(name = "idx_producto", columnList = "id_producto"),
    @Index(name = "idx_estado", columnList = "estado"),
    @Index(name = "idx_fecha", columnList = "fecha_pedido")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    @Column(nullable = false)
    private LocalDateTime fechaPedido = LocalDateTime.now();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(nullable = false)
    private Integer cantidadPedida;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column
    private LocalDateTime fechaEntrega;

    @Column(length = 500)
    private String comentario;

    @Column
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column
    private LocalDateTime fechaModificacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @PrePersist
    protected void onCreate() {
        this.fechaPedido = LocalDateTime.now();
        this.estado = EstadoPedido.PENDIENTE;
        this.activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    public enum EstadoPedido {
        PENDIENTE("Pendiente"),
        ACEPTADO("Aceptado"),
        RECHAZADO("Rechazado"),
        ENTREGADO("Entregado"),
        CANCELADO("Cancelado");

        private final String label;

        EstadoPedido(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
