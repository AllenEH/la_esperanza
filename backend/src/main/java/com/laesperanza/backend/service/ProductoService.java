package com.laesperanza.backend.service;

import com.laesperanza.backend.dto.ProductoRequest;
import com.laesperanza.backend.dto.ProductoResponse;
import com.laesperanza.backend.entity.Producto;
import com.laesperanza.backend.entity.Categoria;
import com.laesperanza.backend.entity.Usuario;
import com.laesperanza.backend.repository.ProductoRepository;
import com.laesperanza.backend.repository.CategoriaRepository;
import com.laesperanza.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Productos
 * Cumple con validaciones OWASP A1 (entrada) y control de acceso
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    /**
     * Obtener productos disponibles con paginación
     */
    public Page<ProductoResponse> obtenerProductos(Pageable pageable) {
        Page<Producto> productos = productoRepository.findByActivoAndDisponible(true, true, pageable);
        return productos.map(this::convertirAResponse);
    }

    /**
     * Buscar productos por nombre o descripción
     */
    public Page<ProductoResponse> buscarProductos(String busqueda, Pageable pageable) {
        Page<Producto> productos = productoRepository.buscarProductos(busqueda, pageable);
        return productos.map(this::convertirAResponse);
    }

    /**
     * Obtener productos por categoría
     */
    public Page<ProductoResponse> obtenerPorCategoria(Long idCategoria, Pageable pageable) {
        Page<Producto> productos = productoRepository.findByCategoriaIdCategoria(idCategoria, pageable);
        return productos.map(this::convertirAResponse);
    }

    /**
     * Obtener productos de un usuario (productor)
     */
    public Page<ProductoResponse> obtenerProductosUsuario(Long idUsuario, Pageable pageable) {
        Page<Producto> productos = productoRepository.findByUsuarioIdUsuario(idUsuario, pageable);
        return productos.map(this::convertirAResponse);
    }

    /**
     * Crear nuevo producto (solo productores)
     */
    public ProductoResponse crearProducto(Long idUsuario, ProductoRequest request) {
        log.info("[PRODUCTO] Creando producto: {}", request.getNombre());

        // ✅ OWASP A5: Verificar que usuario es productor
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getRol().toString().equals("PRODUCTOR")) {
            log.warn("[PRODUCTO] Usuario {} no es productor", idUsuario);
            auditoriaService.registrarIntento("CREAR_PRODUCTO_DENEGADO", idUsuario.toString(), 
                "No es productor");
            throw new RuntimeException("Solo productores pueden crear productos");
        }

        // Obtener categoría
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Crear producto
        Producto producto = Producto.builder()
            .nombre(request.getNombre())
            .precio(request.getPrecio())
            .cantidad(request.getCantidad())
            .descripcion(request.getDescripcion())
            .imagen(request.getImagen())
            .usuario(usuario)
            .categoria(categoria)
            .disponible(true)
            .activo(true)
            .build();

        Producto productoGuardado = productoRepository.save(producto);

        auditoriaService.registrarIntento("PRODUCTO_CREADO", idUsuario.toString(),
            "Producto: " + productoGuardado.getNombre());

        log.info("[PRODUCTO] Producto creado: {} por usuario: {}", productoGuardado.getNombre(), usuario.getNombre());

        return convertirAResponse(productoGuardado);
    }

    /**
     * Actualizar producto (solo propietario o admin)
     */
    public ProductoResponse actualizarProducto(Long idProducto, Long idUsuario, ProductoRequest request) {
        log.info("[PRODUCTO] Actualizando producto: {}", idProducto);

        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // ✅ OWASP A5: Verificar permisos
        if (!producto.getUsuario().getIdUsuario().equals(idUsuario)) {
            log.warn("[PRODUCTO] Usuario {} sin permiso para actualizar producto {}", idUsuario, idProducto);
            throw new RuntimeException("No tienes permiso para actualizar este producto");
        }

        // Actualizar campos
        producto.setNombre(request.getNombre());
        producto.setPrecio(request.getPrecio());
        producto.setCantidad(request.getCantidad());
        producto.setDescripcion(request.getDescripcion());

        if (request.getIdCategoria() != null) {
            Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }

        Producto productoActualizado = productoRepository.save(producto);

        auditoriaService.registrarIntento("PRODUCTO_ACTUALIZADO", idUsuario.toString(),
            "Producto: " + productoActualizado.getNombre());

        return convertirAResponse(productoActualizado);
    }

    /**
     * Eliminar producto (solo propietario o admin)
     */
    public void eliminarProducto(Long idProducto, Long idUsuario) {
        log.info("[PRODUCTO] Eliminando producto: {}", idProducto);

        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // ✅ OWASP A5: Verificar permisos
        if (!producto.getUsuario().getIdUsuario().equals(idUsuario)) {
            log.warn("[PRODUCTO] Usuario {} sin permiso para eliminar producto {}", idUsuario, idProducto);
            throw new RuntimeException("No tienes permiso para eliminar este producto");
        }

        producto.setActivo(false);
        productoRepository.save(producto);

        auditoriaService.registrarIntento("PRODUCTO_ELIMINADO", idUsuario.toString(),
            "Producto: " + producto.getNombre());

        log.info("[PRODUCTO] Producto eliminado: {}", idProducto);
    }

    /**
     * Convertir entidad a DTO
     */
    private ProductoResponse convertirAResponse(Producto producto) {
        return ProductoResponse.builder()
            .idProducto(producto.getIdProducto())
            .nombre(producto.getNombre())
            .precio(producto.getPrecio())
            .cantidad(producto.getCantidad())
            .imagen(producto.getImagen())
            .descripcion(producto.getDescripcion())
            .nombreCategoria(producto.getCategoria().getNombreCategoria())
            .idCategoria(producto.getCategoria().getIdCategoria())
            .idUsuario(producto.getUsuario().getIdUsuario())
            .nombreUsuario(producto.getUsuario().getNombre())
            .fechaPublicacion(producto.getFechaPublicacion())
            .disponible(producto.getDisponible())
            .build();
    }
}
