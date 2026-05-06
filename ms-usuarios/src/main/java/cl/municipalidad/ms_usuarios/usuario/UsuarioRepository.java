package cl.municipalidad.ms_usuarios.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    
    Boolean existsByEmail(String email);
}

//repositorypattern se cumple aqui, ya que se define una interfaz (UsuarioRepository) que extiende JpaRepository, 
// lo que permite realizar operaciones CRUD sobre la entidad Usuario sin exponer detalles de implementación. Esto facilita 
// el mantenimiento y la escalabilidad del código.