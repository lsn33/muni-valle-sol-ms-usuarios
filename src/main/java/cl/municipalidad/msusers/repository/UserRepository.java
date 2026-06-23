package cl.municipalidad.msusers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.municipalidad.msusers.model.User;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link User}.
 *
 * <p>Implementa el patrón <b>Repository</b>: define una interfaz de acceso
 * a datos sin exponer detalles de implementación (SQL, JDBC, etc.).
 * Spring Data JPA genera automáticamente la implementación en tiempo de
 * ejecución a partir de los nombres de los métodos.</p>
 *
 * <p>Extiende {@link JpaRepository} para heredar operaciones CRUD básicas:
 * {@code save()}, {@code findById()}, {@code findAll()}, {@code deleteById()},
 * entre otras.</p>
 *
 * <p>Los métodos {@code IgnoreCase} garantizan que
 * {@code Juan@Mail.cl} y {@code juan@mail.cl} se traten como el mismo email,
 * evitando registros duplicados por diferencia de capitalización.</p>
 *
 * <p><b>Patrón aplicado:</b> Repository Pattern (Martin Fowler).</p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 * @see User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su correo electrónico ignorando mayúsculas/minúsculas.
     *
     * @param email Correo electrónico a buscar.
     * @return {@link Optional} con el usuario si existe, vacío si no.
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Verifica si ya existe un usuario con el correo dado, ignorando capitalización.
     * Usado para evitar registros duplicados durante el registro.
     *
     * @param email Correo electrónico a verificar.
     * @return {@code true} si el email ya está en uso, {@code false} si está disponible.
     */
    boolean existsByEmailIgnoreCase(String email);
}