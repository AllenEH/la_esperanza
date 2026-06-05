package com.laesperanza.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Auditoría
 * Cumple con OWASP A10 (Registro y monitoreo insuficiente)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    private final List<AuditoriaLog> logs = new ArrayList<>();
    private static final int MAX_LOGS = 1000;

    /**
     * Registrar intento de autenticación/acción
     */
    public void registrarIntento(String accion, String usuarioId, String detalles) {
        AuditoriaLog log = AuditoriaLog.builder()
            .timestamp(LocalDateTime.now())
            .accion(accion)
            .usuarioId(usuarioId)
            .detalles(detalles)
            .build();

        logs.add(log);

        // Mantener últimos N logs en memoria
        if (logs.size() > MAX_LOGS) {
            logs.remove(0);
        }

        // Log en consola
        log.info("[AUDIT] {} | Usuario: {} | {}", accion, usuarioId, detalles);
    }

    /**
     * Obtener últimos logs
     */
    public List<AuditoriaLog> obtenerLogs(int cantidad) {
        int inicio = Math.max(0, logs.size() - cantidad);
        return new ArrayList<>(logs.subList(inicio, logs.size()));
    }

    /**
     * Obtener logs por acción
     */
    public List<AuditoriaLog> obtenerLogsPorAccion(String accion) {
        return logs.stream()
            .filter(l -> l.getAccion().contains(accion))
            .toList();
    }

    public static class AuditoriaLog {
        public LocalDateTime timestamp;
        public String accion;
        public String usuarioId;
        public String detalles;

        public static AuditoriaLogBuilder builder() {
            return new AuditoriaLogBuilder();
        }

        public static class AuditoriaLogBuilder {
            private LocalDateTime timestamp;
            private String accion;
            private String usuarioId;
            private String detalles;

            public AuditoriaLogBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public AuditoriaLogBuilder accion(String accion) {
                this.accion = accion;
                return this;
            }

            public AuditoriaLogBuilder usuarioId(String usuarioId) {
                this.usuarioId = usuarioId;
                return this;
            }

            public AuditoriaLogBuilder detalles(String detalles) {
                this.detalles = detalles;
                return this;
            }

            public AuditoriaLog build() {
                AuditoriaLog log = new AuditoriaLog();
                log.timestamp = this.timestamp;
                log.accion = this.accion;
                log.usuarioId = this.usuarioId;
                log.detalles = this.detalles;
                return log;
            }
        }
    }
}
