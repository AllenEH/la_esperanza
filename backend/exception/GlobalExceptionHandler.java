package com.laesperanza.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador Global de Excepciones
 * Convierte RuntimeExceptions en respuestas HTTP con mensajes claros
 * en lugar del 403 genérico de Spring Security.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────
    // Errores de negocio (RuntimeException)
    // ─────────────────────────────────────────────

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        String mensaje = ex.getMessage();
        HttpStatus status = resolverEstado(mensaje);

        log.warn("[ERROR] {} - {} {}", status.value(), request.getMethod(), request.getRequestURI());

        return ResponseEntity
                .status(status)
                .body(ApiError.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .mensaje(mensaje)
                        .ruta(request.getRequestURI())
                        .build());
    }

    // ─────────────────────────────────────────────
    // Errores de validación (@Valid)
    // ─────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> campos = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            campos.put(fe.getField(), fe.getDefaultMessage());
        }

        log.warn("[VALIDACION] Campos inválidos: {} - {}", request.getRequestURI(), campos);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Validación fallida")
                        .mensaje("Revisa los campos enviados")
                        .ruta(request.getRequestURI())
                        .campos(campos)
                        .build());
    }

    // ─────────────────────────────────────────────
    // Cualquier otro error inesperado
    // ─────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(
            Exception ex,
            HttpServletRequest request) {

        log.error("[ERROR INTERNO] {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Error interno del servidor")
                        .mensaje("Ocurrió un error inesperado. Revisa los logs.")
                        .ruta(request.getRequestURI())
                        .build());
    }

    // ─────────────────────────────────────────────
    // Mapeo de mensajes → códigos HTTP
    // ─────────────────────────────────────────────

    private HttpStatus resolverEstado(String mensaje) {
        if (mensaje == null) return HttpStatus.INTERNAL_SERVER_ERROR;

        return switch (mensaje.toLowerCase()) {
            case "usuario no encontrado",
                 "producto no encontrado",
                 "categoría no encontrada",
                 "pedido no encontrado"       -> HttpStatus.NOT_FOUND;              // 404

            case "teléfono ya registrado",
                 "email ya registrado"        -> HttpStatus.CONFLICT;               // 409

            case "solo productores pueden crear productos",
                 "no tienes permiso para actualizar este producto",
                 "no tienes permiso para eliminar este producto" -> HttpStatus.FORBIDDEN; // 403

            default -> {
                if (mensaje.toLowerCase().contains("no encontrado"))  yield HttpStatus.NOT_FOUND;
                if (mensaje.toLowerCase().contains("ya registrado"))  yield HttpStatus.CONFLICT;
                if (mensaje.toLowerCase().contains("no tienes permiso")) yield HttpStatus.FORBIDDEN;
                if (mensaje.toLowerCase().contains("inválido"))       yield HttpStatus.BAD_REQUEST;
                yield HttpStatus.BAD_REQUEST;
            }
        };
    }
}
