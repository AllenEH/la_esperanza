package com.laesperanza.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponse {
    private Long idPedido;
    private Long idUsuario;
    private Long idProducto;
    private String nombreProducto;
    private Integer cantidadPedida;
    private String estado;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaEntrega;
    private String comentario;
}
