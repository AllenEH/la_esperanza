package com.laesperanza.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioPerfilResponse {
    private Long idUsuario;
    private String nombre;
    private String telefono;
    private String rol;
    private BigDecimal reputacion;
    private String foto;
    private LocalDateTime fechaRegistro;
    private Integer totalProductos;
    private Integer totalPedidos;
    private String ubicacion;
}
