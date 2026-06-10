package cl.municipalidad.msusers.service;

import cl.municipalidad.msusers.dto.AuthResponse;
import cl.municipalidad.msusers.dto.LoginRequest;
import cl.municipalidad.msusers.dto.RegisterRequest;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link UserService}.
 *
 * <p>Usa {@link ExtendWith} con Mockito para aislar el servicio de sus
 * dependencias (repositorio, encoder, JwtService), verificando solo
 * la lógica de negocio de cada método.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Pruebas unitarias")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new User();
        usuarioMock.setId(1L);
        usuarioMock.setNombre("Juan Perez");
        usuarioMock.setEmail("juan@municipalidad.cl");
        usuarioMock.setPassword("hashed_password");
        usuarioMock.setRol("FUNCIONARIO");
        usuarioMock.setActivo(true);
    }

    @Test
    @DisplayName("registrar: debe crear usuario correctamente cuando el email no existe")
    void registrar_emailNuevo_retornaDTO() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(usuarioMock);

        RegisterRequest request = new RegisterRequest(
                "Juan Perez", "juan@municipalidad.cl", "password123", "FUNCIONARIO");

        UserDTO resultado = userService.registrar(request);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.email()).isEqualTo("juan@municipalidad.cl");
        assertThat(resultado.rol()).isEqualTo("FUNCIONARIO");
        assertThat(resultado.activo()).isTrue();

        verify(userRepository).existsByEmailIgnoreCase(anyString());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registrar: debe lanzar excepcion cuando el email ya esta registrado")
    void registrar_emailDuplicado_lanzaExcepcion() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        RegisterRequest request = new RegisterRequest(
                "Juan Perez", "juan@municipalidad.cl", "password123", "FUNCIONARIO");

        assertThatThrownBy(() -> userService.registrar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya esta registrado");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("registrar: la contrasena debe estar encriptada al guardar")
    void registrar_passwordEncriptada() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("miClave123")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        userService.registrar(new RegisterRequest(
                "Ana Gomez", "ana@municipalidad.cl", "miClave123", "ADMIN"));

        verify(userRepository).save(argThat(u -> "$2a$hashed".equals(u.getPassword())));
    }

    @Test
    @DisplayName("login: debe retornar AuthResponse con token cuando las credenciales son validas")
    void login_credencialesValidas_retornaToken() {
        when(userRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtService.generarAccessToken(usuarioMock)).thenReturn("jwt.token.mock");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(1800L);

        AuthResponse response = userService.login(
                new LoginRequest("juan@municipalidad.cl", "password123"));

        assertThat(response.accessToken()).isEqualTo("jwt.token.mock");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(1800L);
    }

    @Test
    @DisplayName("login: debe lanzar BadCredentialsException cuando el email no existe")
    void login_emailNoExiste_lanzaExcepcion() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(
                new LoginRequest("noexiste@municipalidad.cl", "password123")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login: debe lanzar BadCredentialsException cuando la contrasena es incorrecta")
    void login_passwordIncorrecta_lanzaExcepcion() {
        when(userRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("wrong", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(
                new LoginRequest("juan@municipalidad.cl", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login: debe lanzar BadCredentialsException cuando el usuario esta desactivado")
    void login_usuarioDesactivado_lanzaExcepcion() {
        usuarioMock.setActivo(false);
        when(userRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.of(usuarioMock));

        assertThatThrownBy(() -> userService.login(
                new LoginRequest("juan@municipalidad.cl", "password123")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("buscarPorEmail: debe retornar UserDTO cuando el usuario existe")
    void buscarPorEmail_existente_retornaDTO() {
        when(userRepository.findByEmailIgnoreCase("juan@municipalidad.cl"))
                .thenReturn(Optional.of(usuarioMock));

        UserDTO resultado = userService.buscarPorEmail("juan@municipalidad.cl");

        assertThat(resultado.email()).isEqualTo("juan@municipalidad.cl");
        assertThat(resultado.rol()).isEqualTo("FUNCIONARIO");
    }

    @Test
    @DisplayName("buscarPorEmail: debe lanzar excepcion cuando el usuario no existe")
    void buscarPorEmail_inexistente_lanzaExcepcion() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.buscarPorEmail("noexiste@municipalidad.cl"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}