package com.laesperanza.backend.repository;

import com.laesperanza.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repositorio para Usuario
 * Usa Prepared Statements automáticamente (previene SQL Injection)
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByTelefono(String telefono);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByTelefonoAndActivo(String telefono, Boolean activo);

    List<Usuario> findByRolAndActivo(Usuario.RolUsuario rol, Boolean activo);

    @Query("SELECT u FROM Usuario u WHERE u.activo = true ORDER BY u.reputacion DESC")
    List<Usuario> findAllActivosByReputacion();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol AND u.activo = true")
    Long countByRolAndActivo(Usuario.RolUsuario rol);
}
