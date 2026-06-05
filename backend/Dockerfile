# Dockerfile - Backend Spring Boot
# La Esperanza - Multi-stage build

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copiar archivos de compilación
COPY pom.xml .
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="La Esperanza Community"
LABEL version="1.0.0"
LABEL description="REST API para Sistema de Gestión Agrícola"

WORKDIR /app

# Copiar JAR desde stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV DB_HOST=postgres
ENV DB_PORT=5432
ENV DB_NAME=la_esperanza
ENV DB_USER=postgres
ENV JWT_SECRET=tu-clave-secreta-muy-larga-y-segura-min-32-caracteres

# Puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
