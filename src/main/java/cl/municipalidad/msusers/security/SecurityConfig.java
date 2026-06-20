package cl.municipalidad.msusers.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad HTTP para el microservicio de usuarios.
 *
 * <p>Define la política de autenticación y autorización usando Spring Security.
 * Al ser un servicio REST sin estado (stateless), se desactivan las sesiones
 * HTTP y la protección CSRF (no aplica para APIs JSON).</p>
 *
 * <p><b>Endpoints públicos</b> (sin token requerido):
 * <ul>
 *   <li>{@code POST /api/usuarios/register} — registro de nuevos usuarios</li>
 *   <li>{@code POST /api/usuarios/login} — autenticación y obtención de token</li>
 *   <li>{@code GET /api/usuarios/email/**} — consulta por email (uso interno del BFF)</li>
 *   <li>{@code GET /actuator/health} — health check del servicio</li>
 *   <li>{@code GET /.well-known/jwks.json} — clave pública RSA para verificar tokens</li>
 * </ul></p>
 *
 * <p>Cualquier otro endpoint no listado es denegado ({@code denyAll}).</p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define la cadena de filtros de seguridad HTTP.
     *
     * <p>Configuración aplicada:
     * <ul>
     *   <li>CSRF desactivado: no necesario en APIs REST sin sesión.</li>
     *   <li>Sesiones STATELESS: cada request debe ser autónomo con su token.</li>
     *   <li>Endpoints de autenticación y JWKS permitidos sin token.</li>
     *   <li>Todo lo demás denegado explícitamente.</li>
     * </ul></p>
     *
     * @param http Objeto de configuración de seguridad HTTP inyectado por Spring.
     * @return {@link SecurityFilterChain} construida con la configuración definida.
     * @throws Exception si ocurre un error en la configuración de seguridad.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST,
                    "/api/usuarios/register",
                    "/api/usuarios/login"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/usuarios/email/**",
                    "/actuator/health",
                    "/.well-known/jwks.json",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().denyAll()
            );
        return http.build();
    }

    /**
     * Bean de encriptación de contraseñas usando el algoritmo BCrypt.
     *
     * <p>BCrypt aplica un factor de trabajo (salt rounds) que hace que
     * la encriptación sea computacionalmente costosa, protegiéndose contra
     * ataques de fuerza bruta.</p>
     *
     * @return Instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}