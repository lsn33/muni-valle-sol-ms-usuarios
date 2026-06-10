package cl.municipalidad.msusers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para la autenticación de un usuario.
 *
 * <p>Implementa el patrón <b>Record</b> de Java para inmutabilidad.
 * Contiene solo los campos necesarios para el login, separando
 * responsabilidades del {@link RegisterRequest}.</p>
 *
 * <p><b>Patrón aplicado:</b> Data Transfer Object (DTO) con Java Record + Bean Validation.</p>
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
    @Size(max = 150, message = "El email no puede superar los 150 caracteres")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    String password

) {}