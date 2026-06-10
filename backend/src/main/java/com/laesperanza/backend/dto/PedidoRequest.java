package com.laesperanza.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoRequest {

    @NotNull(message = "ID de producto es requerido")
    private Long idProducto;

    @NotNull(message = "Cantidad es requerida")
    @Min(value = 1, message = "Cantidad mínima es 1")
    private Integer cantidad;

    private String comentario;
}
