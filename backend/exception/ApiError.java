package com.laesperanza.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estructura estándar de respuesta para errores de la API
 *
 * Ejemplo de respuesta:
 * {
 *   "timestamp": "2026-06-10T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "mensaje": "Usuario no encontrado",
 *   "ruta": "/api/auth/login"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Omite campos null en el JSON
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String ruta;

    // Solo aparece en errores de validación (@Valid)
    private Map<String, String> campos;
}
