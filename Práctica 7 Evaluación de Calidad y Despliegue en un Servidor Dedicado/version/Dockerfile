# Etapa de construcción
FROM maven:3.9-eclipse-temurin-21 AS builder

# Directorio de trabajo
WORKDIR /build

# Copiar archivos de configuración primero
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
COPY mvnw.cmd .

# Descargar todas las dependencias
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src/ src/

# Construir la aplicación omitiendo tests
RUN mvn clean package -DskipTests

# Etapa final con imagen más ligera
FROM eclipse-temurin:21-jre-alpine

# Establecer directorio de trabajo
WORKDIR /app

# Crear usuario no root
RUN addgroup -S spring && \
    adduser -S spring -G spring && \
    mkdir -p /app/uploads/profiles && \
    chown -R spring:spring /app

# Copiar el jar desde la etapa de construcción
COPY --from=builder /build/target/*.jar app.jar
RUN chown spring:spring app.jar

# Cambiar al usuario no root
USER spring

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8083
ENV TZ=UTC
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

# Puerto y health check
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget -q --spider http://localhost:8083/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
