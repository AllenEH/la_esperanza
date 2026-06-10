package com.laesperanza.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroRequest {

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "Teléfono es requerido")
    @Pattern(regexp = "^[\\d\\s\\-\\+\\(\\)]{7,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    @Pattern(regexp = "^\\d{13}$", message = "DPI debe tener 13 dígitos")
    private String dpi;

    @NotNull(message = "Rol es requerido")
    private String rol; // PRODUCTOR, COMPRADOR

    @Email(message = "Email debe ser válido")
    private String email;
}
