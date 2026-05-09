# MS-Usuarios — Municipalidad Valle del Sol

Microservicio de gestión de usuarios y autenticación JWT para la plataforma de reportes de incendios.

## Tecnologías
- Java 21
- Spring Boot 4.0.6
- Spring Security + JWT (jjwt 0.11.5)
- Spring Data JPA + PostgreSQL
- Flyway (migraciones de BD)
- Maven

## Patrones de diseño implementados
- **Repository Pattern**: `UsuarioRepository` desacopla el acceso a datos de la lógica de negocio
- **DTO con Records**: `UsuarioDTO` como objeto inmutable para transferencia de datos

## Requisitos
- Java 21
- Maven
- Cuenta en Neon.tech (PostgreSQL en la nube)

## Configuración

Crea el archivo `src/main/resources/application.yml` con tus credenciales:

```yaml
spring:
  application:
    name: ms-usuarios
  datasource:
    url: jdbc:postgresql://<host>/ms-usuarios?sslmode=require&channelBinding=require
    username: <usuario>
    password: <password>
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: 8081

jwt:
  secret: <clave-secreta>
  expiration: 86400000
```

## Ejecutar el proyecto

```bash
./mvnw spring-boot:run
```

El servidor arranca en `http://localhost:8081`

## Endpoints

### Registro de usuario

POST /api/usuarios/register
Content-Type: application/json


{
"nombre": "Lucas",
"email": "lucas@test.com",
"password": "123456",
"rol": "CIUDADANO"
}

### Login
POST /api/usuarios/login
Content-Type: application/json

{
"email": "lucas@test.com",
"password": "123456"
}

Retorna un token JWT y el rol del usuario.

## Estrategia de Branching
Se utiliza Git Flow:
- `main` → código estable y probado
- `develop` → integración de features
- `feature/*` → desarrollo de funcionalidades

## Estructura del proyecto

src/main/java/cl/municipalidad/ms_usuarios/
├── usuario/
│   ├── Usuario.java
│   ├── UsuarioDTO.java
│   ├── UsuarioRepository.java
│   ├── UsuarioService.java
│   └── UsuarioController.java
└── security/
├── JwtUtil.java
└── SecurityConfig.java