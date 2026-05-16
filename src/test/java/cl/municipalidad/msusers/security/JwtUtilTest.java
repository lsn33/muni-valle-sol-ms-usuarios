package cl.municipalidad.msusers.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil - Pruebas unitarias")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "clave-super-secreta-para-pruebas-unitarias-municipalidad-2024";
    private static final long EXPIRATION = 3_600_000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    @DisplayName("generarToken: debe retornar un token no nulo y no vacío")
    void generarToken_retornaTokenNoVacio() {
        String token = jwtUtil.generarToken("admin@municipalidad.cl", "ADMIN");
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generarToken: el token debe tener formato JWT con tres partes")
    void generarToken_tieneFormatoJWT() {
        String token = jwtUtil.generarToken("admin@municipalidad.cl", "ADMIN");
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("obtenerEmail: debe extraer el email correcto del token")
    void obtenerEmail_retornaEmailCorrecto() {
        String email = "funcionario@municipalidad.cl";
        String token = jwtUtil.generarToken(email, "FUNCIONARIO");

        String emailExtraido = jwtUtil.obtenerEmail(token);

        assertThat(emailExtraido).isEqualTo(email);
    }

    @Test
    @DisplayName("validarToken: debe retornar true para un token válido")
    void validarToken_tokenValido_retornaTrue() {
        String token = jwtUtil.generarToken("juan@municipalidad.cl", "FUNCIONARIO");
        assertThat(jwtUtil.validarToken(token)).isTrue();
    }

    @Test
    @DisplayName("validarToken: debe retornar false para un token manipulado")
    void validarToken_tokenManipulado_retornaFalse() {
        String token = jwtUtil.generarToken("juan@municipalidad.cl", "FUNCIONARIO");
        assertThat(jwtUtil.validarToken(token + "tampered")).isFalse();
    }

    @Test
    @DisplayName("validarToken: debe retornar false para un string aleatorio")
    void validarToken_stringAleatorio_retornaFalse() {
        assertThat(jwtUtil.validarToken("esto.no.es.jwt")).isFalse();
    }

    @Test
    @DisplayName("generarToken + validarToken: ciclo completo consistente")
    void cicloCompleto_generarYValidar() {
        String email = "ciclo@municipalidad.cl";
        String token = jwtUtil.generarToken(email, "ADMIN");

        assertThat(jwtUtil.validarToken(token)).isTrue();
        assertThat(jwtUtil.obtenerEmail(token)).isEqualTo(email);
    }
}