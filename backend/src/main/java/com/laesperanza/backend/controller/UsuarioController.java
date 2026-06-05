package com.laesperanza.backend.controller;

import com.laesperanza.backend.dto.UsuarioPerfilResponse;
import com.laesperanza.backend.entity.Usuario;
import com.laesperanza.backend.repository.PedidoRepository;
import com.laesperanza.backend.repository.ProductoRepository;
import com.laesperanza.backend.repository.UsuarioRepository;
import com.laesperanza.backend.security.JwtTokenProvider;
import com.laesperanza.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Usuarios
 */
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión de perfiles y usuarios")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://usuario.github.io"})
public class UsuarioController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/me")
    @Operation(summary = "Perfil del usuario autenticado", description = "Devuelve los datos del usuario actual")
    public ResponseEntity<UsuarioPerfilResponse> obtenerPerfilPropio(
            @RequestHeader("Authorization") String bearerToken) {
        log.info("[API] GET /usuarios/me");
        String token = bearerToken.substring(7);
        Usuario usuario = authService.obtenerUsuarioDesdeToken(token);
        return ResponseEntity.ok(convertirAResponse(usuario));
    }

    @GetMapping("/{idUsuario}")
    @Operation(summary = "Perfil de usuario", description = "Devuelve datos públicos del perfil de un usuario")
    public ResponseEntity<UsuarioPerfilResponse> obtenerPerfil(@PathVariable Long idUsuario) {
        log.info("[API] GET /usuarios/{}", idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(convertirAResponse(usuario));
    }

    private UsuarioPerfilResponse convertirAResponse(Usuario usuario) {
        long totalProductos = productoRepository.countProductosByUsuario(usuario.getIdUsuario());
        long totalPedidos = pedidoRepository.countPedidosByUsuario(usuario.getIdUsuario());

        return UsuarioPerfilResponse.builder()
            .idUsuario(usuario.getIdUsuario())
            .nombre(usuario.getNombre())
            .telefono(usuario.getTelefono())
            .rol(usuario.getRol().toString().toLowerCase())
            .reputacion(usuario.getReputacion())
            .foto(usuario.getFoto())
            .fechaRegistro(usuario.getFechaRegistro())
            .totalProductos((int) totalProductos)
            .totalPedidos((int) totalPedidos)
            .ubicacion(usuario.getUbicacion())
            .build();
    }
}
