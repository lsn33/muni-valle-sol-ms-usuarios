package cl.municipalidad.msusers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.municipalidad.msusers.dto.AuthResponse;
import cl.municipalidad.msusers.dto.LoginRequest;
import cl.municipalidad.msusers.dto.RegisterRequest;
import cl.municipalidad.msusers.dto.UserDTO;
import cl.municipalidad.msusers.model.User;
import cl.municipalidad.msusers.repository.UserRepository;

/**
 * Servicio de lógica de negocio para la gestión y autenticación de usuarios.
 *
 * <p>Actúa como capa intermedia entre el controlador
 * ({@link cl.municipalidad.msusers.controller.UserController}) y el repositorio
 * ({@link UserRepository}), encapsulando las reglas de negocio:
 * validación de duplicados, encriptación de contraseña, verificación de
 * credenciales y generación de tokens JWT.</p>
 *
 * <p>Usa {@code @Transactional} para garantizar la integridad de los datos:
 * si algo falla durante el registro, la operación se revierte completamente.</p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 * @see UserRepository
 * @see JwtService
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Valida que el email no esté en uso (ignorando mayúsculas/minúsculas),
     * normaliza los datos (trim, lowercase en email, uppercase en rol) y
     * encripta la contraseña con BCrypt antes de persistir.</p>
     *
     * @param request DTO validado con los datos del nuevo usuario.
     * @return {@link UserDTO} con los datos públicos del usuario creado.
     * @throws IllegalArgumentException si el email ya está registrado.
     */
    @Transactional
    public UserDTO registrar(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }

        User user = new User();
        user.setNombre(request.nombre().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRol(request.rol().toUpperCase().trim());

        return toDTO(userRepository.save(user));
    }

    /**
     * Autentica un usuario y genera un token JWT de acceso.
     *
     * <p>Verifica que el email exista, que la cuenta esté activa y que
     * la contraseña coincida con el hash almacenado. Si alguna condición
     * falla, lanza {@link BadCredentialsException} con un mensaje genérico
     * para no revelar qué campo específico fue incorrecto.</p>
     *
     * @param request DTO validado con email y contraseña.
     * @return {@link AuthResponse} con el token JWT, tipo y tiempo de expiración.
     * @throws BadCredentialsException si el email no existe, la contraseña es
     *                                 incorrecta o la cuenta está desactivada.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!Boolean.TRUE.equals(user.getActivo())
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        String token = jwtService.generarAccessToken(user);
        return new AuthResponse(token, "Bearer", jwtService.getAccessTokenTtlSeconds());
    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * <p>Usado internamente por el BFF para validar la existencia de un
     * usuario. Opera dentro de la red privada del clúster.</p>
     *
     * @param email Correo electrónico del usuario a buscar.
     * @return {@link UserDTO} con los datos públicos del usuario.
     * @throws IllegalArgumentException si no existe un usuario con ese email.
     */
    @Transactional(readOnly = true)
    public UserDTO buscarPorEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));
    }

    /**
     * Convierte una entidad {@link User} a su representación pública {@link UserDTO}.
     *
     * <p>Método privado auxiliar que centraliza la conversión, evitando
     * duplicación de código. Nunca incluye la contraseña en el DTO.</p>
     *
     * @param user Entidad a convertir.
     * @return {@link UserDTO} con los datos públicos del usuario.
     */
    private UserDTO toDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getNombre(),
            user.getEmail(),
            user.getRol(),
            user.getActivo()
        );
    }
}