package com.laesperanza.backend.controller;

import com.laesperanza.backend.dto.ProductoRequest;
import com.laesperanza.backend.dto.ProductoResponse;
import com.laesperanza.backend.security.JwtTokenProvider;
import com.laesperanza.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Productos
 * Endpoints: GET /productos, POST /productos, PUT /productos/{id}, DELETE /productos/{id}
 */
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Productos", description = "Gestión de productos agrícolas")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://usuario.github.io"})
public class ProductoController {

    private final ProductoService productoService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Obtener productos disponibles", description = "Lista paginada de productos disponibles")
    public ResponseEntity<Page<ProductoResponse>> obtenerProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[API] GET /productos?page={}&size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productoService.obtenerProductos(pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos", description = "Busca productos por nombre o descripción")
    public ResponseEntity<Page<ProductoResponse>> buscarProductos(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[API] GET /productos/buscar?query={}", query);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productoService.buscarProductos(query, pageable));
    }

    @GetMapping("/categoria/{idCategoria}")
    @Operation(summary = "Productos por categoría", description = "Lista productos de una categoría específica")
    public ResponseEntity<Page<ProductoResponse>> obtenerPorCategoria(
            @PathVariable Long idCategoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[API] GET /productos/categoria/{}", idCategoria);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productoService.obtenerPorCategoria(idCategoria, pageable));
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Productos de un usuario", description = "Lista productos publicados por un usuario")
    public ResponseEntity<Page<ProductoResponse>> obtenerProductosUsuario(
            @PathVariable Long idUsuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[API] GET /productos/usuario/{}", idUsuario);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productoService.obtenerProductosUsuario(idUsuario, pageable));
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Publica un nuevo producto (solo productores)")
    public ResponseEntity<ProductoResponse> crearProducto(
            @Valid @RequestBody ProductoRequest request,
            @RequestHeader("Authorization") String bearerToken) {
        log.info("[API] POST /productos - {}", request.getNombre());
        String token = bearerToken.substring(7);
        Long idUsuario = jwtTokenProvider.getUserIdFromToken(token);
        ProductoResponse response = productoService.crearProducto(idUsuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{idProducto}")
    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Long idProducto,
            @Valid @RequestBody ProductoRequest request,
            @RequestHeader("Authorization") String bearerToken) {
        log.info("[API] PUT /productos/{}", idProducto);
        String token = bearerToken.substring(7);
        Long idUsuario = jwtTokenProvider.getUserIdFromToken(token);
        ProductoResponse response = productoService.actualizarProducto(idProducto, idUsuario, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{idProducto}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long idProducto,
            @RequestHeader("Authorization") String bearerToken) {
        log.info("[API] DELETE /productos/{}", idProducto);
        String token = bearerToken.substring(7);
        Long idUsuario = jwtTokenProvider.getUserIdFromToken(token);
        productoService.eliminarProducto(idProducto, idUsuario);
        return ResponseEntity.noContent().build();
    }
}
