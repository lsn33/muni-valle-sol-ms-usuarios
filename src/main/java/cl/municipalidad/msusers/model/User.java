package cl.municipalidad.msusers.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un usuario del sistema municipal.
 *
 * <p>Mapeada a la tabla {@code usuario} en PostgreSQL. La contraseña
 * siempre se almacena encriptada con BCrypt — nunca en texto plano.</p>
 *
 * <p><b>Nota:</b> Esta entidad no se expone directamente al frontend.
 * Se convierte a {@link cl.municipalidad.msusers.dto.UserDTO} antes
 * de cualquier respuesta HTTP para evitar filtrar datos sensibles
 * como la contraseña.</p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 * @see cl.municipalidad.msusers.dto.UserDTO
 */
@Data
@Entity
@Table(name = "usuario")
public class User {

    /**
     * Identificador único generado automáticamente por la base de datos (BIGSERIAL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre completo del usuario. Máximo 100 caracteres, obligatorio.
     */
    @Column(nullable = false, length = 100)
    private String nombre;

    /**
     * Correo electrónico del usuario. Debe ser único en el sistema.
     * Se usa como identificador de inicio de sesión.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Contraseña encriptada con BCrypt.
     * Nunca se devuelve en respuestas HTTP.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Rol del usuario en el sistema.
     * Valores esperados: {@code ADMIN}, {@code FUNCIONARIO}, {@code CIUDADANO}.
     */
    @Column(nullable = false, length = 50)
    private String rol;

    /**
     * Indica si la cuenta está activa. Por defecto {@code true} al crear el usuario.
     * Una cuenta inactiva no puede iniciar sesión.
     */
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Fecha y hora en que se creó el registro.
     * Se asigna automáticamente al instanciar la entidad.
     */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}