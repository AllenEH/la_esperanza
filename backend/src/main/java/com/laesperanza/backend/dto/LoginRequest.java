package com.laesperanza.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO para Login - Validación de entrada (OWASP A1)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Teléfono es requerido")
    @Pattern(regexp = "^[\\d\\s\\-\\+\\(\\)]{7,20}$", message = "Formato de teléfono inválido")
    private String telefono;

    @NotBlank(message = "Código SMS es requerido")
    @Pattern(regexp = "^\\d{4,6}$", message = "Código debe ser 4-6 dígitos")
    private String codigo;
}
