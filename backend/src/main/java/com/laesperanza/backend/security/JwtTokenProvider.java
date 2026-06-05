package com.laesperanza.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.laesperanza.backend.entity.Usuario;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Proveedor de Tokens JWT
 * Cumple con OWASP A2 (Autenticación segura)
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        byte[] decodedKey = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * Generar JWT token
     */
    public String generateToken(Usuario usuario) {
        return createToken(usuario, jwtExpirationMs);
    }

    /**
     * Generar refresh token (larga duración)
     */
    public String generateRefreshToken(Usuario usuario) {
        return createToken(usuario, refreshExpirationMs);
    }

    /**
     * Crear token JWT
     */
    private String createToken(Usuario usuario, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
            .subject(usuario.getIdUsuario().toString())
            .claim("nombre", usuario.getNombre())
            .claim("email", usuario.getEmail())
            .claim("rol", usuario.getRol().toString())
            .claim("telefono", usuario.getTelefono().substring(0, 3) + "***") // No guardar teléfono completo
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    /**
     * Obtener ID de usuario del token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Obtener nombre del token
     */
    public String getNombreFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("nombre");
    }

    /**
     * Obtener rol del token
     */
    public String getRolFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("rol");
    }

    /**
     * Obtener todos los claims del token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getPayload();
    }

    /**
     * Validar token JWT
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("[JWT] Token JWT inválido: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("[JWT] Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("[JWT] Token JWT no soportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("[JWT] JWT claims vacío: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.error("[JWT] Firma JWT inválida: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Verificar si el token está expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
