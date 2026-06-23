# ── ETAPA 1: Compilación ──────────────────────────────────────────────────
# Usamos Eclipse Temurin JDK 21 con Alpine Linux (imagen liviana ~200MB)
# Java 21 es la versión LTS que usa el proyecto
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copiamos primero solo los archivos de configuración de Maven
# Truco de caché: si el código cambia pero pom.xml no,
# Docker reutiliza la capa de dependencias y el build es más rápido
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .

# Damos permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Descargamos todas las dependencias sin compilar el código fuente
# -B = batch mode (sin colores, ideal para CI/CD)
# dependency:go-offline = descarga todo lo necesario para compilar offline
RUN ./mvnw dependency:go-offline -B

# Ahora copiamos el código fuente
COPY src/ src/

# Compilamos y generamos el JAR ejecutable
# -DskipTests = omitimos tests para acelerar el build de la imagen
# Los tests se corren por separado en el pipeline de CI
RUN ./mvnw package -DskipTests -B

# ── ETAPA 2: Runtime ──────────────────────────────────────────────────────
# Imagen limpia solo con JRE (no necesitamos el JDK completo para ejecutar)
FROM eclipse-temurin:21-jre-alpine AS runtime

# Creamos un usuario no-root por seguridad
# Nunca se debe correr una aplicación como root en producción
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiamos solo el JAR desde la etapa builder
# Sin código fuente, sin Maven, sin herramientas de compilación
COPY --from=builder /workspace/target/*.jar app.jar

# Directorio para las claves RSA
# En producción se montan como volumen o secret de Kubernetes
RUN mkdir -p /app/keys && chown -R appuser:appgroup /app

# Cambiamos al usuario no-root
USER appuser

# Documentamos el puerto que expone la aplicación
EXPOSE 8081

# Opciones de JVM optimizadas para contenedores:
# UseContainerSupport: respeta los límites de CPU/RAM del contenedor
# MaxRAMPercentage: usa máximo el 75% de la RAM asignada al contenedor
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Variables de entorno con valores por defecto vacíos
# Se sobreescriben al correr el contenedor con -e o docker-compose
ENV DB_URL=""
ENV DB_USERNAME=""
ENV DB_PASSWORD=""
ENV JWT_PRIVATE_KEY_PATH="/app/keys/private_key.pem"
ENV JWT_PUBLIC_KEY_PATH="/app/keys/public_key.pem"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]