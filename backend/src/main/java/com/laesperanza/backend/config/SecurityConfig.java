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
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .cors(cors -> cors.configurationSource(request -> {

                    CorsConfiguration config = new CorsConfiguration();

                    config.setAllowedOrigins(List.of(
                            "http://localhost:3000",
                            "http://localhost:5173",
                            "https://alleneh.github.io",
                            "https://web-collapse-assured-periods.trycloudflare.com"
                    ));

                    config.setAllowedMethods(List.of(
                            "GET",
                            "POST",
                            "PUT",
                            "DELETE",
                            "PATCH",
                            "OPTIONS"
                    ));

                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);

                    return config;
                }))

                .authorizeHttpRequests(auth -> auth

                        // AUTH
                        .requestMatchers(
                                "/auth/login",
                                "/auth/registrar"
                        ).permitAll()

                        // SWAGGER
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // ACTUATOR
                        .requestMatchers("/actuator/health")
                        .permitAll()

                        // CATEGORIAS
                        .requestMatchers(HttpMethod.GET, "/categorias/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/categorias")
                        .authenticated()  // o .hasRole("ADMIN") si solo admin puede crear

                        // PRODUCTOS
                        .requestMatchers(HttpMethod.GET, "/productos/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST, "/productos")
                        .authenticated()

                        .requestMatchers(HttpMethod.PUT, "/productos/**")
                        .authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/productos/**")
                        .authenticated()

                        // USUARIOS
                        .requestMatchers(HttpMethod.GET, "/usuarios/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.PUT, "/usuarios/**")
                        .authenticated()

                        // PEDIDOS
                        .requestMatchers("/pedidos/**")
                        .authenticated()

                        // CALIFICACIONES
                        .requestMatchers(HttpMethod.GET, "/calificaciones/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST, "/calificaciones/**")
                        .authenticated()

                        // ADMIN
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }
}