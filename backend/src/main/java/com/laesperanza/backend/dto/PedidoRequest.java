package com.laesperanza.backend.dto;
 
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.*;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoRequest {
 
    @NotNull(message = "ID de producto es requerido")
    private Long idProducto;
 
    @JsonAlias({"cantidadPedida", "cantidad"})
    @NotNull(message = "Cantidad es requerida")
    @Min(value = 1, message = "Cantidad mínima es 1")
    private Integer cantidad;
 
    private String comentario;
}
 