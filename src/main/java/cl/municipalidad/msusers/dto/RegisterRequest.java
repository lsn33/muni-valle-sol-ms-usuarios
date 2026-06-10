package cl.municipalidad.msusers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el registro de un nuevo usuario.
 *
 * <p>Implementa el patrón <b>Record</b> de Java para garantizar inmutabilidad.
 * Las anotaciones de Jakarta Bean Validation se ejecutan automáticamente
 * al usar {@code @Valid} en el controlador, rechazando datos inválidos
 * antes de llegar a la capa de servicio.</p>
 *
 * <p><b>Patrón aplicado:</b> Data Transfer Object (DTO) con Java Record + Bean Validation.</p>
 *
 * @param nombre   Nombre completo del usuario. Entre 2 y 100 caracteres.
 * @param email    Correo electrónico válido y único. Máximo 150 caracteres.
 * @param password Contraseña en texto plano. Entre 8 y 72 caracteres.
 * @param rol      Rol del usuario. Solo acepta: ADMIN, FUNCIONARIO, CIUDADANO.
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 */
public record RegisterRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 150, message = "El email no puede superar los 150 caracteres")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    String password,

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(
        regexp = "ADMIN|FUNCIONARIO|CIUDADANO",
        message = "El rol debe ser ADMIN, FUNCIONARIO o CIUDADANO"
    )
    String rol

) {}