package com.laesperanza.backend.repository;

import com.laesperanza.backend.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repositorio para Categoria
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombreCategoriaIgnoreCase(String nombreCategoria);

    List<Categoria> findByActivo(Boolean activo);
}
