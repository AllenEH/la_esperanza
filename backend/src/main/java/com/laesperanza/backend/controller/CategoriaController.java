package com.laesperanza.backend.controller;
 
import com.laesperanza.backend.entity.Categoria;
import com.laesperanza.backend.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
/**
 * Controlador de Categorías
 */
@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categorías", description = "Gestión de categorías de productos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://alleneh.github.io"})
public class CategoriaController {
 
    private final CategoriaService categoriaService;
 
    @GetMapping
    @Operation(summary = "Listar categorías", description = "Obtiene las categorías activas disponibles")
    public ResponseEntity<List<Categoria>> listarCategorias() {
        log.info("[API] GET /categorias");
        return ResponseEntity.ok(categoriaService.obtenerCategoriasActivas());
    }
 
    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría")
    public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody Categoria categoria) {
        log.info("[API] POST /categorias - {}", categoria.getNombreCategoria());
        Categoria nueva = categoriaService.crearCategoria(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }
}
