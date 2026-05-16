package cl.municipalidad.msusers.service;

import cl.municipalidad.msusers.dto.UserDTO;
import cl.municipalidad.msusers.model.User;
import cl.municipalidad.msusers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Pruebas unitarias")
class UserServiceTest {

    @Mock
    private UserRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new User();
        usuarioMock.setId(1L);
        usuarioMock.setNombre("Juan Pérez");
        usuarioMock.setEmail("juan@municipalidad.cl");
        usuarioMock.setPassword("hashed_password");
        usuarioMock.setRol("FUNCIONARIO");
        usuarioMock.setActivo(true);
    }

    @Test
    @DisplayName("registrar: debe crear usuario correctamente cuando el email no existe")
    void registrar_emailNuevo_retornaDTO() {
        when(usuarioRepository.existsByEmail("juan@municipalidad.cl")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(usuarioRepository.save(any(User.class))).thenReturn(usuarioMock);

        UserDTO resultado = userService.registrar(
                "Juan Pérez", "juan@municipalidad.cl", "password123", "FUNCIONARIO");

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nombre()).isEqualTo("Juan Pérez");
        assertThat(resultado.email()).isEqualTo("juan@municipalidad.cl");
        assertThat(resultado.rol()).isEqualTo("FUNCIONARIO");
        assertThat(resultado.activo()).isTrue();

        verify(usuarioRepository).existsByEmail("juan@municipalidad.cl");
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registrar: debe lanzar excepción cuando el email ya está registrado")
    void registrar_emailDuplicado_lanzaExcepcion() {
        when(usuarioRepository.existsByEmail("juan@municipalidad.cl")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.registrar("Juan Pérez", "juan@municipalidad.cl", "password123", "FUNCIONARIO"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El email ya está registrado");

        verify(usuarioRepository).existsByEmail("juan@municipalidad.cl");
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("registrar: la contraseña guardada debe estar encriptada")
    void registrar_passwordDebeEstarEncriptada() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("miClave123")).thenReturn("$2a$hashed");
        when(usuarioRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        userService.registrar("Ana Gómez", "ana@municipalidad.cl", "miClave123", "ADMIN");

        verify(passwordEncoder).encode("miClave123");
        verify(usuarioRepository).save(argThat(u -> "$2a$hashed".equals(u.getPassword())));
    }

    @Test
    @DisplayName("buscarPorEmail: debe retornar el usuario cuando el email existe")
    void buscarPorEmail_emailExistente_retornaOptional() {
        when(usuarioRepository.findByEmail("juan@municipalidad.cl"))
                .thenReturn(Optional.of(usuarioMock));

        Optional<User> resultado = userService.buscarPorEmail("juan@municipalidad.cl");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("juan@municipalidad.cl");
        verify(usuarioRepository).findByEmail("juan@municipalidad.cl");
    }

    @Test
    @DisplayName("buscarPorEmail: debe retornar Optional vacío cuando el email no existe")
    void buscarPorEmail_emailInexistente_retornaVacio() {
        when(usuarioRepository.findByEmail("noexiste@municipalidad.cl"))
                .thenReturn(Optional.empty());

        Optional<User> resultado = userService.buscarPorEmail("noexiste@municipalidad.cl");

        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findByEmail("noexiste@municipalidad.cl");
    }
}