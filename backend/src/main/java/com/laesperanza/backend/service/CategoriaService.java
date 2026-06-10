package com.laesperanza.backend.service;

import com.laesperanza.backend.entity.Categoria;
import com.laesperanza.backend.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para Categorías
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> obtenerCategoriasActivas() {
        log.debug("[CATEGORIA] Listando categorías activas");
        return categoriaRepository.findByActivo(true);
    }

    public Categoria crearCategoria(Categoria categoria) {
        log.info("[CATEGORIA] Creando categoría: {}", categoria.getNombreCategoria());
        return categoriaRepository.save(categoria);
    }
}
