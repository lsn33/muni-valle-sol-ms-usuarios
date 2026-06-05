package cl.municipalidad.msusers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para la autenticación de un usuario.
 *
 * <p>Implementa el patrón <b>Record</b> de Java para inmutabilidad.
 * Contiene solo los campos necesarios para el login, separando
 * responsabilidades del {@link CreateUserRequest}.</p>
 *
 * @param email    Correo electrónico del usuario. Debe tener formato válido.
 * @param password Contraseña en texto plano para verificar contra el hash BCrypt.
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 */
public record LoginRequest(

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password

) {}