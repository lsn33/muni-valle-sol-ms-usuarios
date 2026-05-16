package cl.municipalidad.msusers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import cl.municipalidad.msusers.dto.UserDTO;
import cl.municipalidad.msusers.model.User;
import cl.municipalidad.msusers.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

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

        return new UserDTO(
            guardado.getId(),
            guardado.getNombre(),
            guardado.getEmail(),
            guardado.getRol(),
            guardado.getActivo()
        );
    }

    public Optional<User> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}