package com.laesperanza.backend.controller;

import com.laesperanza.backend.dto.CalificacionRequest;
import com.laesperanza.backend.dto.CalificacionResponse;
import com.laesperanza.backend.security.JwtTokenProvider;
import com.laesperanza.backend.service.CalificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Calificaciones
 */
@RestController
@RequestMapping("/calificaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Calificaciones", description = "Gestión de calificaciones de usuarios")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://alleneh.github.io","https://sistema-la-esperanza.online","https://www.sistema-la-esperanza.online"})
public class CalificacionController {

    private final CalificacionService calificacionService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "Crear calificación", description = "Registra una calificación para un usuario")
    public ResponseEntity<Void> crearCalificacion(
            @Valid @RequestBody CalificacionRequest request,
            @RequestHeader("Authorization") String bearerToken) {
        log.info("[API] POST /calificaciones");
        String token = bearerToken.substring(7);
        Long idCalificador = jwtTokenProvider.getUserIdFromToken(token);
        calificacionService.crearCalificacion(idCalificador, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Calificaciones de usuario", description = "Obtiene las calificaciones de un usuario")
    public ResponseEntity<List<CalificacionResponse>> obtenerCalificacionesUsuario(@PathVariable Long idUsuario) {
        log.info("[API] GET /calificaciones/usuario/{}", idUsuario);
        return ResponseEntity.ok(calificacionService.obtenerCalificacionesPorUsuario(idUsuario));
    }

    @GetMapping("/usuario/{idUsuario}/promedio")
    @Operation(summary = "Promedio de calificaciones", description = "Obtiene el promedio de calificaciones para un usuario")
    public ResponseEntity<Double> obtenerPromedio(@PathVariable Long idUsuario) {
        log.info("[API] GET /calificaciones/usuario/{}/promedio", idUsuario);
        return ResponseEntity.ok(calificacionService.obtenerPromedioCalificaciones(idUsuario));
    }
}
