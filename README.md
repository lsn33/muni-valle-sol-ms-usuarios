# MS-Usuarios — Municipalidad Valle del Sol
Microservicio de gestión de usuarios y autenticación JWT para la plataforma de reportes de incendios de la Municipalidad Valle del Sol.


## Tecnologías
- Java 25
- Spring Boot 4.0.6
- Spring Security + JWT (jjwt 0.11.5)
- Spring Data JPA + PostgreSQL
- Flyway (migraciones de BD)
- Maven


## Patrones de diseño implementados

### 1. Repository Pattern
`UserRepository` define una interfaz que extiende `JpaRepository`, desacoplando el acceso a datos de la lógica de negocio. Permite realizar operaciones CRUD sobre la entidad `User` sin exponer detalles de implementación, facilitando el mantenimiento y la escalabilidad.

### 2. DTO con Records (Java 25)
`UserDTO` es un record Java inmutable que transfiere datos entre capas sin exponer la contraseña ni datos sensibles del modelo de dominio.


## Casos de uso
| Caso de uso | Descripción |
|-------------|-------------|
| Registrar usuario | Un ciudadano, brigadista o funcionario se registra con nombre, email, contraseña y rol |
| Autenticar usuario | El sistema valida credenciales y genera un token JWT con el rol del usuario |
| Buscar usuario por email | El sistema busca un usuario por su email para validar login |


## Requisitos
- Java 25
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
## Ejecutar pruebas unitarias

```bash
./mvnw test
```


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
Roles disponibles: `CIUDADANO`, `BRIGADISTA`, `FUNCIONARIO`

### Login
POST /api/usuarios/login
Content-Type: application/json
{
"email": "lucas@test.com",
"password": "123456"
}
Retorna un token JWT y el rol del usuario.


## Migraciones de BD (Flyway)
| Versión | Archivo | Descripción |
|---------|---------|-------------|
| V2 | V2__crear_tabla_usuario.sql | Crea tabla usuario con campos id, nombre, email, password, rol, activo, fecha_creacion |


## Estrategia de Branching (Git Flow)
- `main` → código estable y probado
- `qa` → ambiente de validación previa a producción
- `develop` → integración de features
- `feature/*` → desarrollo de funcionalidades


## Estructura del proyecto
src/main/java/cl/municipalidad/msusers/
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── repository/
│   └── UserRepository.java
├── model/
│   └── User.java
├── dto/
│   └── UserDTO.java
├── security/
│   ├── JwtUtil.java
│   └── SecurityConfig.java
└── MsUserApplication.java


## Pruebas unitarias
Las pruebas están organizadas por capa y cubren los casos principales del sistema:

| Clase de prueba | Qué prueba |
|----------------|------------|
| `UserServiceTest` | Registro exitoso, email duplicado, búsqueda por email |
| `UserControllerTest` | Endpoints de register y login con respuestas HTTP correctas |
| `JwtUtilTest` | Generación y validación de tokens JWT |

**Herramientas usadas:**
- **JUnit 5** → framework de pruebas
- **Mockito** → simula dependencias (Repository, PasswordEncoder) sin tocar la BD real

**Ejemplo de prueba:**
```java
@Test
void registrar_emailDuplicado_lanzaExcepcion() {
    when(userRepository.existsByEmail("test@test.com")).thenReturn(true);
    assertThrows(RuntimeException.class, () ->
        userService.registrar("Lucas", "test@test.com", "123456", "CIUDADANO")
    );
}
```