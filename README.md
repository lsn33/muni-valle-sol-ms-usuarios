# MS-Usuarios

Microservicio de autenticación y gestión de usuarios para la Municipalidad Valle del Sol. Gestiona el registro, login y emisión de tokens JWT firmados con RSA RS256.

## Tabla Técnica

| Ítem | Detalle |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Base de datos | NeonDB (PostgreSQL serverless) |
| ORM | Spring Data JPA + Hibernate |
| Migraciones | Flyway |
| Seguridad | Spring Security + JWT RSA RS256 |
| Documentación API | SpringDoc OpenAPI 3.0.3 (Swagger UI) |
| Testing | JUnit 5 + Mockito + JaCoCo |
| Cobertura | 82% |
| Puerto | 8081 |
| Patrones de diseño | Repository Pattern, Singleton (Spring Beans) |

## Librerías principales

- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-oauth2-resource-server`
- `spring-boot-starter-validation`
- `spring-boot-starter-flyway`
- `flyway-database-postgresql`
- `postgresql`
- `lombok`
- `springdoc-openapi-starter-webmvc-ui:3.0.3`
- `jacoco-maven-plugin:0.8.14`

## Requisitos

- Java 21
- Maven (incluido con `./mvnw`)
- Claves RSA: `private_key.pem` y `public_key.pem` en la raíz del proyecto

## Instalación y ejecución

```bash
# Clonar el repositorio
git clone https://github.com/lsn33/muni-valle-sol-ms-usuarios.git
cd muni-valle-sol-ms-usuarios

# Ejecutar
./mvnw clean spring-boot:run
```

El servicio estará disponible en `http://localhost:8081`

## Variables de entorno

| Variable | Descripción | Default |
|---|---|---|
| `DB_URL` | URL de conexión a NeonDB | URL de Neon incluida |
| `DB_USERNAME` | Usuario de la BD | `neondb_owner` |
| `DB_PASSWORD` | Contraseña de la BD | — |
| `JWT_PRIVATE_KEY_PATH` | Ruta a la clave privada RSA | `./private_key.pem` |
| `JWT_PUBLIC_KEY_PATH` | Ruta a la clave pública RSA | `./public_key.pem` |

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/usuarios/register` | Registrar nuevo usuario |
| POST | `/api/usuarios/login` | Autenticar y obtener JWT |
| GET | `/api/usuarios/email/{email}` | Buscar usuario por email |
| GET | `/.well-known/jwks.json` | Clave pública RSA (JWKS) |

## Swagger UI

```
http://localhost:8081/swagger-ui.html
```

## Ejecutar pruebas y cobertura

```bash
# Ejecutar tests
./mvnw test

# Generar reporte de cobertura JaCoCo
./mvnw clean verify

# Ver reporte en:
# target/site/jacoco/index.html
```

## Docker

```bash
# Build
docker build -t ms-usuarios .

# Run
docker run -p 8081:8081 \
  -e DB_URL="..." \
  -e DB_USERNAME="neondb_owner" \
  -e DB_PASSWORD="..." \
  -v ./private_key.pem:/app/keys/private_key.pem \
  -v ./public_key.pem:/app/keys/public_key.pem \
  ms-usuarios
```