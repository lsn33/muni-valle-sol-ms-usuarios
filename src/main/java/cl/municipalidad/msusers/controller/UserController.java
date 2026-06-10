package cl.municipalidad.msusers.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.municipalidad.msusers.dto.AuthResponse;
import cl.municipalidad.msusers.dto.LoginRequest;
import cl.municipalidad.msusers.dto.RegisterRequest;
import cl.municipalidad.msusers.dto.UserDTO;
import cl.municipalidad.msusers.service.UserService;

/**
 * Controlador REST para la gestión y autenticación de usuarios.
 *
 * <p>Expone los endpoints de registro, login y consulta de usuarios.
 * Delega toda la lógica al {@link UserService} y usa {@code @Valid}
 * para validar los DTOs de entrada automáticamente antes de procesarlos.</p>
 *
 * <p>Todos los endpoints son públicos, configurados en
 * {@link cl.municipalidad.msusers.security.SecurityConfig}.</p>
 *
 * <p><b>Base URL:</b> {@code /api/usuarios}</p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 * @see UserService
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p><b>POST</b> {@code /api/usuarios/register}</p>
     *
     * <p>Ejemplo de cuerpo de solicitud:
     * <pre>{@code
     * {
     *   "nombre": "Juan Pérez",
     *   "email": "juan@municipalidad.cl",
     *   "password": "miClave123",
     *   "rol": "FUNCIONARIO"
     * }
     * }</pre></p>
     *
     * @param request DTO validado con los datos del nuevo usuario.
     * @return {@link UserDTO} con los datos del usuario creado y HTTP 201.
     *         HTTP 400 si algún campo no pasa la validación.
     *         HTTP 409 si el email ya está registrado.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registrar(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registrar(request));
    }

    /**
     * Autentica un usuario y retorna un token JWT firmado con RS256.
     *
     * <p><b>POST</b> {@code /api/usuarios/login}</p>
     *
     * <p>Ejemplo de cuerpo de solicitud:
     * <pre>{@code
     * {
     *   "email": "juan@municipalidad.cl",
     *   "password": "miClave123"
     * }
     * }</pre></p>
     *
     * <p>Ejemplo de respuesta exitosa:
     * <pre>{@code
     * {
     *   "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
     *   "tokenType": "Bearer",
     *   "expiresIn": 1800
     * }
     * }</pre></p>
     *
     * @param request DTO validado con email y contraseña.
     * @return HTTP 200 con {@link AuthResponse} si las credenciales son válidas.
     *         HTTP 400 si algún campo no pasa la validación.
     *         HTTP 401 si las credenciales son incorrectas o la cuenta está inactiva.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * <p><b>GET</b> {@code /api/usuarios/email/{email}}</p>
     *
     * <p>Usado por el BFF para verificar la existencia de un usuario antes
     * de procesar operaciones que requieren identificación. Opera dentro
     * de la red privada del clúster, no expuesto al exterior.</p>
     *
     * @param email Correo electrónico del usuario a buscar (path variable).
     * @return HTTP 200 con {@link UserDTO} si existe.
     *         HTTP 409 si el usuario no se encuentra.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> buscarPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.buscarPorEmail(email));
    }
}