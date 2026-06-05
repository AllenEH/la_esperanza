package com.laesperanza.backend.service;

import com.laesperanza.backend.dto.CalificacionRequest;
import com.laesperanza.backend.dto.CalificacionResponse;
import com.laesperanza.backend.entity.Calificacion;
import com.laesperanza.backend.entity.Usuario;
import com.laesperanza.backend.repository.CalificacionRepository;
import com.laesperanza.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para Calificaciones
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public void crearCalificacion(Long calificadorId, CalificacionRequest request) {
        log.info("[CALIFICACION] Usuario {} califica a {}", calificadorId, request.getIdUsuario());

        Usuario calificador = usuarioRepository.findById(calificadorId)
            .orElseThrow(() -> new RuntimeException("Usuario calificador no encontrado"));

        Usuario usuarioCalificado = usuarioRepository.findById(request.getIdUsuario())
            .orElseThrow(() -> new RuntimeException("Usuario a calificar no encontrado"));

        if (calificador.getIdUsuario().equals(usuarioCalificado.getIdUsuario())) {
            throw new RuntimeException("No puedes calificarte a ti mismo");
        }

        Calificacion calificacion = Calificacion.builder()
            .puntuacion(request.getPuntuacion())
            .comentario(request.getComentario())
            .usuario(usuarioCalificado)
            .calificador(calificador)
            .build();

        calificacionRepository.save(calificacion);

        Double promedio = calificacionRepository.obtenerPromedioCalificaciones(usuarioCalificado.getIdUsuario());
        if (promedio != null) {
            usuarioCalificado.setReputacion(promedio);
            usuarioRepository.save(usuarioCalificado);
        }

        auditoriaService.registrarIntento("CALIFICACION_CREADA", calificadorId.toString(), "Calificó a " + usuarioCalificado.getNombre());
    }

    public List<CalificacionResponse> obtenerCalificacionesPorUsuario(Long idUsuario) {
        log.info("[CALIFICACION] Obteniendo calificaciones para usuario {}", idUsuario);
        return calificacionRepository.findByUsuarioIdUsuario(idUsuario).stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
    }

    public Double obtenerPromedioCalificaciones(Long idUsuario) {
        return calificacionRepository.obtenerPromedioCalificaciones(idUsuario);
    }

    private CalificacionResponse convertirAResponse(Calificacion calificacion) {
        return CalificacionResponse.builder()
            .idCalificacion(calificacion.getIdCalificacion())
            .puntuacion(calificacion.getPuntuacion())
            .comentario(calificacion.getComentario())
            .fechaCalificacion(calificacion.getFechaCalificacion())
            .idUsuario(calificacion.getUsuario().getIdUsuario())
            .idCalificador(calificacion.getCalificador().getIdUsuario())
            .nombreUsuario(calificacion.getUsuario().getNombre())
            .nombreCalificador(calificacion.getCalificador().getNombre())
            .build();
    }
}
