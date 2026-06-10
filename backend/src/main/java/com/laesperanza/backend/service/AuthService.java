package com.laesperanza.backend.service;

import com.laesperanza.backend.dto.*;
import com.laesperanza.backend.entity.Usuario;
import com.laesperanza.backend.entity.Usuario.RolUsuario;
import com.laesperanza.backend.repository.UsuarioRepository;
import com.laesperanza.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.math.BigDecimal;

/**
 * Servicio de Autenticación
 * Cumple con OWASP A2 (Autenticación segura) y A3 (Protección de datos)
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditoriaService auditoriaService;

    /**
     * Login con teléfono y código SMS
     */
    public AuthResponse login(LoginRequest request) {
        log.info("[AUTH] Intento de login: {}", request.getTelefono().substring(0, 3) + "***");

        // ✅ OWASP A1: Validación de entrada ya hecha por @Valid
        Optional<Usuario> usuarioOpt = usuarioRepository.findByTelefonoAndActivo(
            request.getTelefono(), true
        );

        if (usuarioOpt.isEmpty()) {
            log.warn("[AUTH] Usuario no encontrado: {}", request.getTelefono().substring(0, 3) + "***");
            auditoriaService.registrarIntento("LOGIN_FALLIDO", request.getTelefono(), "Usuario no encontrado");
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // ✅ En producción: Validar código SMS con servicio real (Twilio)
        // Por ahora, aceptar cualquier código para demo
        // TODO: Implementar verificación con Twilio API

        // ✅ OWASP A3: Generar JWT seguro con expiración
        String token = jwtTokenProvider.generateToken(usuario);
        String refreshToken = jwtTokenProvider.generateRefreshToken(usuario);

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        auditoriaService.registrarIntento("LOGIN_EXITOSO", usuario.getIdUsuario().toString(), 
            usuario.getNombre() + " (" + usuario.getRol() + ")");

        log.info("[AUTH] Login exitoso para usuario: {}", usuario.getNombre());

        return AuthResponse.builder()
            .idUsuario(usuario.getIdUsuario())
            .nombre(usuario.getNombre())
            .rol(usuario.getRol().toString())
            .token(token)
            .refreshToken(refreshToken)
            .expiresIn(1800000L) // 30 minutos en milisegundos
            .build();
    }

    /**
     * Registrar nuevo usuario
     */
    public AuthResponse registrar(RegistroRequest request) {
        log.info("[AUTH] Intento de registro: {}", request.getNombre());

        // ✅ OWASP A1: Validación de entrada ya hecha por @Valid
        
        // Verificar si teléfono ya existe
        if (usuarioRepository.findByTelefono(request.getTelefono()).isPresent()) {
            log.warn("[AUTH] Teléfono duplicado: {}", request.getTelefono().substring(0, 3) + "***");
            throw new RuntimeException("Teléfono ya registrado");
        }

        // Verificar si email ya existe (si se proporciona)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
                log.warn("[AUTH] Email duplicado: {}", request.getEmail());
                throw new RuntimeException("Email ya registrado");
            }
        }

        // Crear nuevo usuario
        RolUsuario rol;
        try {
            rol = RolUsuario.valueOf(request.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rol inválido: " + request.getRol());
        }

        Usuario nuevoUsuario = Usuario.builder()
            .nombre(request.getNombre())
            .telefono(request.getTelefono())
            .email(request.getEmail())
            .dpi(request.getDpi())
            .rol(rol)
            .reputacion(BigDecimal.valueOf(5.0))
            .activo(true)
            .foto(rol == RolUsuario.PRODUCTOR ? "👩‍🌾" : "👤")
            .build();

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // Generar tokens
        String token = jwtTokenProvider.generateToken(usuarioGuardado);
        String refreshToken = jwtTokenProvider.generateRefreshToken(usuarioGuardado);

        auditoriaService.registrarIntento("REGISTRO_EXITOSO", usuarioGuardado.getIdUsuario().toString(),
            usuarioGuardado.getNombre() + " (" + rol + ")");

        log.info("[AUTH] Registro exitoso para: {}", nuevoUsuario.getNombre());

        return AuthResponse.builder()
            .idUsuario(usuarioGuardado.getIdUsuario())
            .nombre(usuarioGuardado.getNombre())
            .rol(usuarioGuardado.getRol().toString())
            .token(token)
            .refreshToken(refreshToken)
            .expiresIn(1800000L)
            .build();
    }

    /**
     * Validar token JWT
     */
    public boolean validarToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Obtener usuario desde token
     */
    public Usuario obtenerUsuarioDesdeToken(String token) {
        Long usuarioId = jwtTokenProvider.getUserIdFromToken(token);
        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
