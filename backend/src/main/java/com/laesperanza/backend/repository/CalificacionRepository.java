package com.laesperanza.backend.repository;

import com.laesperanza.backend.entity.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio para Calificacion
 */
@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    List<Calificacion> findByUsuarioIdUsuario(Long idUsuario);

    @Query("SELECT AVG(c.puntuacion) FROM Calificacion c WHERE c.usuario.idUsuario = :idUsuario")
    Double obtenerPromedioCalificaciones(@Param("idUsuario") Long idUsuario);

    @Query("SELECT COUNT(c) FROM Calificacion c WHERE c.usuario.idUsuario = :idUsuario")
    Long countCalificacionesByUsuario(@Param("idUsuario") Long idUsuario);
}
