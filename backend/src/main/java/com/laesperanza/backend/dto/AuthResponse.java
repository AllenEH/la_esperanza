package com.laesperanza.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long idUsuario;
    private String nombre;
    private String rol;
    private String token;
    private String refreshToken;
    private Long expiresIn;
}
