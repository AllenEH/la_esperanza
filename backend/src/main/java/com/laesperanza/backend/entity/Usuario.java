package com.laesperanza.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Entidad Usuario - Productor o Comprador
 * Cumple con OWASP A2 (Autenticación segura) y A5 (Control de acceso)
 */
@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_telefono", columnList = "telefono", unique = true),
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_rol", columnList = "rol")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 15)
    private String telefono;

    @Column(unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 13)
    private String dpi;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal reputacion = BigDecimal.valueOf(5.0);

    @Column(length = 50)
    private String foto;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column
    private LocalDateTime ultimoAcceso;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 15)
    private String ubicacion;

    // Relaciones
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE)
    private List<Producto> productos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE)
    private List<Pedido> pedidos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE)
    private List<Calificacion> calificaciones = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.reputacion = BigDecimal.valueOf(5.0);
        this.activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    public enum RolUsuario {
        PRODUCTOR("productor"),
        COMPRADOR("comprador"),
        ADMIN("admin");

        private final String valor;

        RolUsuario(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }
}
