package cl.municipalidad.msusers.controller;

import cl.municipalidad.msusers.dto.UserDTO;
import cl.municipalidad.msusers.model.User;
import cl.municipalidad.msusers.security.JwtUtil;
import cl.municipalidad.msusers.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("UserController - Pruebas unitarias (capa web)")
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("register: debe retornar 201 cuando el registro es exitoso")
    void register_exitoso_retorna201() throws Exception {
        UserDTO dto = new UserDTO(1L, "Juan Pérez", "juan@municipalidad.cl", "FUNCIONARIO", true);
        when(userService.registrar(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(dto);

        Map<String, String> body = Map.of(
                "nombre", "Juan Pérez",
                "email", "juan@municipalidad.cl",
                "password", "password123",
                "rol", "FUNCIONARIO"
        );

        mockMvc().perform(post("/api/usuarios/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.rol").value("FUNCIONARIO"));
    }

    @Test
    @DisplayName("login: debe retornar 200 con token cuando las credenciales son válidas")
    void login_credencialesValidas_retorna200() throws Exception {
        User usuario = new User();
        usuario.setEmail("juan@municipalidad.cl");
        usuario.setPassword("hashed_password");
        usuario.setRol("FUNCIONARIO");

        when(userService.buscarPorEmail("juan@municipalidad.cl"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtUtil.generarToken("juan@municipalidad.cl", "FUNCIONARIO"))
                .thenReturn("jwt.token.mock");

        Map<String, String> body = Map.of(
                "email", "juan@municipalidad.cl",
                "password", "password123"
        );

        mockMvc().perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.mock"))
                .andExpect(jsonPath("$.rol").value("FUNCIONARIO"));
    }

    @Test
    @DisplayName("login: debe retornar 401 cuando el email no existe")
    void login_emailNoExiste_retorna401() throws Exception {
        when(userService.buscarPorEmail("noexiste@municipalidad.cl"))
                .thenReturn(Optional.empty());

        Map<String, String> body = Map.of(
                "email", "noexiste@municipalidad.cl",
                "password", "password123"
        );

        mockMvc().perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("login: debe retornar 401 cuando la contraseña es incorrecta")
    void login_passwordIncorrecta_retorna401() throws Exception {
        User usuario = new User();
        usuario.setEmail("juan@municipalidad.cl");
        usuario.setPassword("hashed_password");
        usuario.setRol("FUNCIONARIO");

        when(userService.buscarPorEmail("juan@municipalidad.cl"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrong", "hashed_password")).thenReturn(false);

        Map<String, String> body = Map.of(
                "email", "juan@municipalidad.cl",
                "password", "wrong"
        );

        mockMvc().perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}