package com.laesperanza.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponse {
    private Long idProducto;
    private String nombre;
    private BigDecimal precio;
    private Integer cantidad;
    private String imagen;
    private String descripcion;
    private String nombreCategoria;
    private Long idCategoria;
    private Long idUsuario;
    private String nombreUsuario;
    private LocalDateTime fechaPublicacion;
    private Boolean disponible;
}
