package com.laesperanza.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            String jwt = getJwtFromRequest(request);

            log.info("[JWT] Token recibido: {}", jwt);

            if (StringUtils.hasText(jwt)) {

                boolean valido = jwtTokenProvider.validateToken(jwt);

                log.info("[JWT] Token válido: {}", valido);

                if (valido) {

                    Long userId =
                            jwtTokenProvider.getUserIdFromToken(jwt);

                    String nombre =
                            jwtTokenProvider.getNombreFromToken(jwt);

                    String rol =
                            jwtTokenProvider.getRolFromToken(jwt);

                    var authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + rol)
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    authorities
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    log.info(
                            "[JWT] Usuario autenticado: {} - {}",
                            nombre,
                            rol
                    );
                }
            }

        } catch (Exception ex) {

            log.error(
                    "[JWT] Error: {}",
                    ex.getMessage(),
                    ex
            );
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(
            HttpServletRequest request
    ) {

        String bearerToken =
                request.getHeader("Authorization");

        log.info(
                "[JWT] Authorization Header = {}",
                bearerToken
        );

        if (
                StringUtils.hasText(bearerToken)
                        && bearerToken.startsWith("Bearer ")
        ) {

            return bearerToken.substring(7);
        }

        return null;
    }
}