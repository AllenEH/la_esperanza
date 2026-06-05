package com.laesperanza.backend.repository;

import com.laesperanza.backend.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Producto
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Page<Producto> findByActivoAndDisponible(Boolean activo, Boolean disponible, Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCaseAndActivoAndDisponible(
        String nombre, Boolean activo, Boolean disponible, Pageable pageable);

    Page<Producto> findByCategoriaIdCategoria(Long idCategoria, Pageable pageable);

    Page<Producto> findByUsuarioIdUsuario(Long idUsuario, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.disponible = true " +
           "AND (p.nombre ILIKE CONCAT('%', :busqueda, '%') " +
           "OR p.descripcion ILIKE CONCAT('%', :busqueda, '%')) " +
           "ORDER BY p.fechaPublicacion DESC")
    Page<Producto> buscarProductos(@Param("busqueda") String busqueda, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.categoria.idCategoria = :idCategoria " +
           "AND p.activo = true AND p.disponible = true " +
           "ORDER BY p.fechaPublicacion DESC")
    List<Producto> findByCategoria(@Param("idCategoria") Long idCategoria);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.usuario.idUsuario = :idUsuario AND p.activo = true")
    Long countProductosByUsuario(@Param("idUsuario") Long idUsuario);
}
