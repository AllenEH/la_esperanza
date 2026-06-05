package com.laesperanza.backend.config;

import com.laesperanza.backend.security.JwtAuthenticationFilter;
import com.laesperanza.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Seguridad Spring Security con JWT
 * Cumple con OWASP Top 10
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ✅ OWASP: Deshabilitar sesiones (stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ✅ OWASP A1: CSRF deshabilitado (API stateless no necesita CSRF)
            .csrf(csrf -> csrf.disable())

            // ✅ OWASP A7: Configuración de CORS
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(java.util.Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "https://usuario.github.io"
                ));
                corsConfig.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                corsConfig.setAllowedHeaders(java.util.Arrays.asList("*"));
                corsConfig.setAllowCredentials(true);
                corsConfig.setMaxAge(3600L);
                return corsConfig;
            }))

            // ✅ Configurar autorización de endpoints
            .authorizeHttpRequests(authz -> authz
                // Públicos (login, registro, documentación)
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/registrar").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Productos: GET público, POST/PUT/DELETE requieren autenticación
                .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/productos").authenticated()
                .requestMatchers(HttpMethod.PUT, "/productos/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/productos/**").authenticated()

                // Pedidos: Requieren autenticación
                .requestMatchers("/pedidos/**").authenticated()

                // Usuarios: GET público (perfil), el resto requiere autenticación
                .requestMatchers(HttpMethod.GET, "/usuarios/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/usuarios/**").authenticated()

                // Calificaciones: Requieren autenticación
                .requestMatchers("/calificaciones/**").authenticated()

                // Admin
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Lo demás requiere autenticación
                .anyRequest().authenticated()
            )

            // ✅ Headers de seguridad
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .xssProtection(xss -> xss.enable())
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )

            // Agregar filtro JWT
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
