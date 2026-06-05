package cl.municipalidad.msusers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import cl.municipalidad.msusers.dto.UserDTO;
import cl.municipalidad.msusers.model.User;
import cl.municipalidad.msusers.repository.UserRepository;

import java.util.Optional;

/**
 * Servicio de lógica de negocio para la gestión de usuarios.
 *
 * <p>Actúa como capa intermedia entre el controlador ({@link cl.municipalidad.msusers.controller.UserController})
 * y el repositorio ({@link UserRepository}), encapsulando las reglas de negocio:
 * validación de duplicados, encriptación de contraseña y conversión a DTO.</p>
 *
 * <p>Utiliza {@code @RequiredArgsConstructor} de Lombok para inyección de
 * dependencias por constructor, siguiendo las buenas prácticas de Spring.</p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 * @see UserRepository
 * @see cl.municipalidad.msusers.dto.UserDTO
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Valida que el correo no esté en uso, encripta la contraseña con BCrypt
     * y persiste el usuario. Retorna un DTO sin datos sensibles.</p>
     *
     * @param nombre   Nombre completo del usuario.
     * @param email    Correo electrónico (debe ser único).
     * @param password Contraseña en texto plano (se encripta antes de guardar).
     * @param rol      Rol asignado al usuario (ej: ADMIN, FUNCIONARIO).
     * @return {@link UserDTO} con los datos del usuario creado.
     * @throws RuntimeException si el correo ya está registrado.
     */
    public UserDTO registrar(String nombre, String email, String password, String rol) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }

        User usuario = new User();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);

        User guardado = usuarioRepository.save(usuario);

        return toDTO(guardado);
    }

    /**
     * Busca un usuario por su correo electrónico.
     *
     * <p>Retorna la entidad completa (incluyendo contraseña encriptada) para que
     * el controlador pueda validar credenciales durante el login.</p>
     *
     * @param email Correo electrónico del usuario a buscar.
     * @return {@link Optional} con la entidad {@link User} si existe, vacío si no.
     */
    public Optional<User> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Convierte una entidad {@link User} a su representación pública {@link UserDTO}.
     *
     * <p>Método privado auxiliar que centraliza la conversión, evitando duplicación
     * de código en los distintos métodos del servicio.</p>
     *
     * @param usuario Entidad a convertir.
     * @return {@link UserDTO} con los datos públicos del usuario.
     */
    private UserDTO toDTO(User usuario) {
        return new UserDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getRol(),
            usuario.getActivo()
        );
    }
}