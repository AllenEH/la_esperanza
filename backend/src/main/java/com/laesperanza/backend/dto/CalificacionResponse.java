package com.laesperanza.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalificacionResponse {
    private Long idCalificacion;
    private Integer puntuacion;
    private String comentario;
    private LocalDateTime fechaCalificacion;
    private Long idUsuario;
    private Long idCalificador;
    private String nombreUsuario;
    private String nombreCalificador;
}
