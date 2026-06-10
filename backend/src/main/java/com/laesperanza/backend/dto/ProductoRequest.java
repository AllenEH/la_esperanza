package com.laesperanza.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoRequest {

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotNull(message = "Precio es requerido")
    @DecimalMin(value = "0.01", message = "Precio debe ser mayor a 0")
    private BigDecimal precio;

    @NotNull(message = "Cantidad es requerida")
    @Min(value = 0, message = "Cantidad no puede ser negativa")
    private Integer cantidad;

    @NotNull(message = "Categoría es requerida")
    private Long idCategoria;

    @Size(max = 500, message = "Descripción no puede exceder 500 caracteres")
    private String descripcion;

    private String imagen;
}
