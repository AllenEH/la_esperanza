package com.laesperanza.backend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "La Esperanza API",
        version = "1.0.0",
        description = "REST API para Sistema de Gestión y Comercialización Agrícola. " +
            "Plataforma para productores y compradores en comunidades rurales.",
        contact = @Contact(
            name = "La Esperanza Community",
            url = "https://usuario.github.io/la-esperanza"
        ),
        license = @License(
            name = "MIT",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080/api",
            description = "Desarrollo"
        ),
        @Server(
            url = "https://api.laesperanza.com/api",
            description = "Producción"
        )
    }
)
public class LaEsperanzaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaEsperanzaApplication.class, args);
    }
}
