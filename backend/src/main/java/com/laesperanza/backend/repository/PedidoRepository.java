package com.laesperanza.backend.repository;
 
import com.laesperanza.backend.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
 
/**
 * Repositorio para Pedido
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
 
    Page<Pedido> findByUsuarioIdUsuario(Long idUsuario, Pageable pageable);
 
    Page<Pedido> findByEstado(Pedido.EstadoPedido estado, Pageable pageable);
 
    @Query("SELECT p FROM Pedido p WHERE p.usuario.idUsuario = :idUsuario " +
           "ORDER BY p.fechaPedido DESC")
    List<Pedido> findHistorialUsuario(@Param("idUsuario") Long idUsuario);
 
    @Query("SELECT p FROM Pedido p WHERE p.producto.usuario.idUsuario = :idProductor " +
           "ORDER BY p.fechaPedido DESC")
    List<Pedido> findPedidosByProductor(@Param("idProductor") Long idProductor);
 
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.usuario.idUsuario = :idUsuario")
    Long countPedidosByUsuario(@Param("idUsuario") Long idUsuario);
 
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.estado = :estado")
    Long countByEstado(@Param("estado") Pedido.EstadoPedido estado);
}
