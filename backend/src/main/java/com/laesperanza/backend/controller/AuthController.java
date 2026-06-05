package com.laesperanza.backend.controller;

import com.laesperanza.backend.dto.*;
import com.laesperanza.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Autenticación
 * Endpoints: POST /auth/login, POST /auth/registrar
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints de login y registro")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://usuario.github.io"})
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login con teléfono y código SMS", description = "Autentica usuario y devuelve JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("[API] POST /auth/login");
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea nuevo usuario productor o comprador")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        log.info("[API] POST /auth/registrar - Usuario: {}", request.getNombre());
        AuthResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/validar-token")
    @Operation(summary = "Validar token JWT", description = "Verifica que el token es válido")
    public ResponseEntity<Boolean> validarToken(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.substring(7);
        boolean valido = authService.validarToken(token);
        return ResponseEntity.ok(valido);
    }
}
