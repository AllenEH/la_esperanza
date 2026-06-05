package com.laesperanza.backend.service;

import com.laesperanza.backend.dto.PedidoRequest;
import com.laesperanza.backend.dto.PedidoResponse;
import com.laesperanza.backend.entity.Pedido;
import com.laesperanza.backend.entity.Pedido.EstadoPedido;
import com.laesperanza.backend.entity.Producto;
import com.laesperanza.backend.entity.Usuario;
import com.laesperanza.backend.repository.PedidoRepository;
import com.laesperanza.backend.repository.ProductoRepository;
import com.laesperanza.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para Pedidos
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public PedidoResponse crearPedido(Long idUsuario, PedidoRequest request) {
        log.info("[PEDIDO] Creando pedido para usuario {} producto {}", idUsuario, request.getIdProducto());

        Usuario comprador = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(request.getIdProducto())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (!producto.getActivo() || !producto.getDisponible()) {
            throw new RuntimeException("Producto no disponible");
        }

        if (producto.getCantidad() < request.getCantidad()) {
            throw new RuntimeException("Cantidad solicitada excede disponibilidad");
        }

        Pedido pedido = Pedido.builder()
            .usuario(comprador)
            .producto(producto)
            .cantidadPedida(request.getCantidad())
            .comentario(request.getComentario())
            .build();

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        auditoriaService.registrarIntento("PEDIDO_CREADO", idUsuario.toString(), "Pedido creado para producto " + producto.getNombre());

        return convertirAResponse(pedidoGuardado);
    }

    public List<PedidoResponse> obtenerHistorialUsuario(Long idUsuario) {
        log.info("[PEDIDO] Obteniendo historial para usuario {}", idUsuario);
        return pedidoRepository.findHistorialUsuario(idUsuario).stream()
            .map(this::convertirAResponse)
            .collect(Collectors.toList());
    }

    public PedidoResponse cambiarEstadoPedido(Long idUsuario, Long idPedido, String nuevoEstado) {
        log.info("[PEDIDO] Usuario {} cambia estado de pedido {} a {}", idUsuario, idPedido, nuevoEstado);

        Pedido pedido = pedidoRepository.findById(idPedido)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        EstadoPedido estado;
        try {
            estado = EstadoPedido.valueOf(nuevoEstado.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Estado de pedido inválido");
        }

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new RuntimeException("No se puede cambiar un pedido ya entregado");
        }

        boolean esComprador = pedido.getUsuario().getIdUsuario().equals(idUsuario);
        boolean esProductor = pedido.getProducto().getUsuario().getIdUsuario().equals(idUsuario);

        if (esProductor) {
            if (estado == EstadoPedido.ACEPTADO || estado == EstadoPedido.RECHAZADO || estado == EstadoPedido.ENTREGADO) {
                pedido.setEstado(estado);
                if (estado == EstadoPedido.ENTREGADO) {
                    pedido.setFechaEntrega(java.time.LocalDateTime.now());
                }
            } else {
                throw new RuntimeException("Productor no puede cambiar a ese estado");
            }
        } else if (esComprador) {
            if (estado == EstadoPedido.CANCELADO && pedido.getEstado() == EstadoPedido.PENDIENTE) {
                pedido.setEstado(EstadoPedido.CANCELADO);
            } else {
                throw new RuntimeException("Comprador no puede cambiar a ese estado");
            }
        } else {
            throw new RuntimeException("No tienes permiso para cambiar el estado de este pedido");
        }

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        auditoriaService.registrarIntento("PEDIDO_ESTADO_CAMBIADO", idUsuario.toString(), "Pedido " + idPedido + " -> " + estado);

        return convertirAResponse(pedidoActualizado);
    }

    private PedidoResponse convertirAResponse(Pedido pedido) {
        return PedidoResponse.builder()
            .idPedido(pedido.getIdPedido())
            .idUsuario(pedido.getUsuario().getIdUsuario())
            .idProducto(pedido.getProducto().getIdProducto())
            .nombreProducto(pedido.getProducto().getNombre())
            .cantidadPedida(pedido.getCantidadPedida())
            .estado(pedido.getEstado().getLabel())
            .fechaPedido(pedido.getFechaPedido())
            .fechaEntrega(pedido.getFechaEntrega())
            .comentario(pedido.getComentario())
            .build();
    }
}
