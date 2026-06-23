package cl.municipalidad.msusers.controller;

import cl.municipalidad.msusers.dto.AuthResponse;
import cl.municipalidad.msusers.dto.UserDTO;
import cl.municipalidad.msusers.exception.GlobalExceptionHandler;
import cl.municipalidad.msusers.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para {@link UserController}.
 *
 * <p>Usa MockMvc en modo standalone con {@link GlobalExceptionHandler}
 * registrado, para verificar tanto los status HTTP como el formato
 * de las respuestas de error sin levantar el contexto completo de Spring.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController - Pruebas de capa web")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("register: debe retornar 201 cuando el registro es exitoso")
    void register_exitoso_retorna201() throws Exception {
        UserDTO dto = new UserDTO(1L, "Juan Perez", "juan@municipalidad.cl", "FUNCIONARIO", true);
        when(userService.registrar(any())).thenReturn(dto);

        mockMvc.perform(post("/api/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "nombre", "Juan Perez",
                                "email", "juan@municipalidad.cl",
                                "password", "password123",
                                "rol", "FUNCIONARIO"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan@municipalidad.cl"))
                .andExpect(jsonPath("$.rol").value("FUNCIONARIO"));
    }

    @Test
    @DisplayName("register: debe retornar 400 cuando faltan campos requeridos")
    void register_camposFaltantes_retorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email", "juan@municipalidad.cl"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos de entrada invalidos"));
    }

    @Test
    @DisplayName("register: debe retornar 400 cuando el email tiene formato invalido")
    void register_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "nombre", "Juan Perez",
                                "email", "esto-no-es-email",
                                "password", "password123",
                                "rol", "FUNCIONARIO"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("register: debe retornar 400 cuando el rol es invalido")
    void register_rolInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "nombre", "Juan Perez",
                                "email", "juan@municipalidad.cl",
                                "password", "password123",
                                "rol", "SUPERUSUARIO"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("register: debe retornar 409 cuando el email ya existe")
    void register_emailDuplicado_retorna409() throws Exception {
        when(userService.registrar(any()))
                .thenThrow(new IllegalArgumentException("El email ya esta registrado"));

        mockMvc.perform(post("/api/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "nombre", "Juan Perez",
                                "email", "juan@municipalidad.cl",
                                "password", "password123",
                                "rol", "FUNCIONARIO"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El email ya esta registrado"));
    }

    @Test
    @DisplayName("login: debe retornar 200 con token cuando las credenciales son validas")
    void login_credencialesValidas_retorna200() throws Exception {
        when(userService.login(any()))
                .thenReturn(new AuthResponse("jwt.token.mock", "Bearer", 1800L));

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email", "juan@municipalidad.cl",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt.token.mock"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(1800));
    }

    @Test
    @DisplayName("login: debe retornar 401 cuando las credenciales son invalidas")
    void login_credencialesInvalidas_retorna401() throws Exception {
        when(userService.login(any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email", "juan@municipalidad.cl",
                                "password", "wrongpassword"

                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("login: debe retornar 400 cuando el email tiene formato invalido")
    void login_emailInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "email", "no-es-email",
                                "password", "password123"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("buscarPorEmail: debe retornar 200 con el usuario cuando existe")
    void buscarPorEmail_existente_retorna200() throws Exception {
        UserDTO dto = new UserDTO(1L, "Juan Perez", "juan@municipalidad.cl", "FUNCIONARIO", true);
        when(userService.buscarPorEmail("juan@municipalidad.cl")).thenReturn(dto);

        mockMvc.perform(get("/api/usuarios/email/juan@municipalidad.cl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@municipalidad.cl"));
    }

    @Test
    @DisplayName("buscarPorEmail: debe retornar 409 cuando el usuario no existe")
    void buscarPorEmail_inexistente_retorna409() throws Exception {
        when(userService.buscarPorEmail("noexiste@municipalidad.cl"))
                .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        mockMvc.perform(get("/api/usuarios/email/noexiste@municipalidad.cl"))
                .andExpect(status().isConflict());
    }
}