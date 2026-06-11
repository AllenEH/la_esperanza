package com.laesperanza.backend.controller;

import com.laesperanza.backend.dto.PedidoRequest;
import com.laesperanza.backend.dto.PedidoResponse;
import com.laesperanza.backend.security.JwtTokenProvider;
import com.laesperanza.backend.service.AuthService;
import com.laesperanza.backend.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Pedidos
 */
@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos", description = "Gestión de pedidos y estados")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://alleneh.github.io","https://sistema-la-esperanza.online","https://www.sistema-la-esperanza.online"})
public class PedidoController {

    private final PedidoService pedidoService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "Crear pedido", description = "Crea un pedido para el usuario autenticado")
    public ResponseEntity<PedidoResponse> crearPedido(
            @Valid @RequestBody PedidoRequest request,
            @RequestHeader("Authorization") String bearerToken) {
        log.info("[API] POST /pedidos");
        String token = bearerToken.substring(7);
        Long idUsuario = jwtTokenProvider.getUserIdFromToken(token);
        PedidoResponse response = pedidoService.crearPedido(idUsuario, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/mis-pedidos")
    @Operation(summary = "Historial de pedidos", description = "Obtiene los pedidos del usuario autenticado")
    public ResponseEntity<List<PedidoResponse>> obtenerMisPedidos(
            @RequestHeader("Authorization") String bearerToken) {
        log.info("[API] GET /pedidos/mis-pedidos");
        String token = bearerToken.substring(7);
        Long idUsuario = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(pedidoService.obtenerHistorialUsuario(idUsuario));
    }

    @PutMapping("/{idPedido}/estado")
    @Operation(summary = "Cambiar estado de pedido", description = "Actualiza el estado de un pedido existente")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Long idPedido,
            @RequestParam String estado,
            @RequestHeader("Authorization") String bearerToken) {
        log.info("[API] PUT /pedidos/{}/estado?estado={}", idPedido, estado);
        String token = bearerToken.substring(7);
        Long idUsuario = jwtTokenProvider.getUserIdFromToken(token);
        return ResponseEntity.ok(pedidoService.cambiarEstadoPedido(idUsuario, idPedido, estado));
    }
}
