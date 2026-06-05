package cl.municipalidad.msusers.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import cl.municipalidad.msusers.dto.CreateUserRequest;
import cl.municipalidad.msusers.dto.LoginRequest;
import cl.municipalidad.msusers.dto.UserDTO;
import cl.municipalidad.msusers.security.JwtUtil;
import cl.municipalidad.msusers.service.UserService;

import java.util.Map;

/**
 * Controlador REST para la gestión de usuarios.
 *
 * <p>Expone los endpoints de registro, autenticación y consulta de usuarios.
 * Actúa como punto de entrada HTTP, delegando la lógica de negocio al
 * {@link UserService} y la generación de tokens al {@link JwtUtil}.</p>
 *
 * <p>Usa {@code @Valid} para activar Bean Validation en los DTOs de entrada,
 * retornando HTTP 400 automáticamente si algún campo no cumple las restricciones
 * antes de llegar a la capa de servicio.</p>
 *
 * <p>Todos los endpoints de esta clase son públicos (sin autenticación requerida),
 * ya que corresponden al flujo de acceso inicial del sistema. Esto se configura
 * en {@link cl.municipalidad.msusers.security.SecurityConfig}.</p>
 *
 * <p><b>Base URL:</b> {@code /api/usuarios}</p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 * @see UserService
 * @see JwtUtil
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService usuarioService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

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
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registrar(@Valid @RequestBody CreateUserRequest request) {
        UserDTO dto = usuarioService.registrar(
            request.nombre(),
            request.email(),
            request.password(),
            request.rol()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Autentica un usuario y retorna un token JWT.
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
     *   "token": "eyJhbGciOiJIUzI1...",
     *   "rol": "FUNCIONARIO"
     * }
     * }</pre></p>
     *
     * @param request DTO validado con email y contraseña.
     * @return HTTP 200 con token y rol si las credenciales son válidas,
     *         HTTP 401 si el usuario no existe o la contraseña es incorrecta.
     *         HTTP 400 si algún campo no pasa la validación.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.buscarPorEmail(request.email())
            .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
            .map(u -> {
                String token = jwtUtil.generarToken(u.getEmail(), u.getRol());
                return ResponseEntity.ok(Map.of("token", token, "rol", u.getRol()));
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * <p><b>GET</b> {@code /api/usuarios/email/{email}}</p>
     *
     * <p>Usado principalmente por el BFF para validar la existencia de un
     * usuario antes de procesar operaciones que requieren identificación.
     * Este endpoint opera dentro de la red privada del clúster y no está
     * expuesto directamente al exterior.</p>
     *
     * @param email Correo electrónico del usuario a buscar (path variable).
     * @return HTTP 200 con {@link UserDTO} si existe, HTTP 404 si no se encuentra.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
            .map(u -> ResponseEntity.ok(new UserDTO(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getRol(),
                u.getActivo()
            )))
            .orElse(ResponseEntity.notFound().build());
    }
}