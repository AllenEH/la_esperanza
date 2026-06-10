package com.laesperanza.backend.dto;
 
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.*;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalificacionRequest {
 
    @JsonAlias({"idUsuarioCalificado", "idUsuario"})
    @NotNull(message = "ID de usuario es requerido")
    private Long idUsuario;
 
    @NotNull(message = "Puntuación es requerida")
    @Min(value = 1, message = "Puntuación mínima es 1")
    @Max(value = 5, message = "Puntuación máxima es 5")
    private Integer puntuacion;
 
    @Size(max = 500, message = "Comentario no puede exceder 500 caracteres")
    private String comentario;
}