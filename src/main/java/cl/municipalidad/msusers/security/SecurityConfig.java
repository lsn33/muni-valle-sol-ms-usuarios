package cl.municipalidad.msusers.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Al ser un servicio REST sin estado (stateless), se desactivan las sesiones HTTP
 * y la protección CSRF (no aplica para APIs JSON).</p>
 *
 * <p><b>Endpoints públicos</b> (sin token requerido):
 * <ul>
 *   <li>{@code POST /api/usuarios/register} — registro de nuevos usuarios</li>
 *   <li>{@code POST /api/usuarios/login} — autenticación y obtención de token</li>
 *   <li>{@code GET /api/usuarios/email/**} — consulta de usuario por email (para el BFF)</li>
 * </ul></p>
 *
 * <p>Cualquier otro endpoint requiere autenticación JWT válida.</p>
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
     *   <li>Endpoints de autenticación permitidos sin token.</li>
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
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/usuarios/register",
                    "/api/usuarios/login",
                    "/api/usuarios/email/**"
                ).permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }

    /**
     * Bean de encriptación de contraseñas usando el algoritmo BCrypt.
     *
     * <p>BCrypt aplica un factor de trabajo (salt rounds) que hace que
     * la encriptación sea computacionalmente costosa, protegiéndose contra
     * ataques de fuerza bruta. Spring Security usa este bean automáticamente
     * al llamar a {@code passwordEncoder.encode()} y {@code passwordEncoder.matches()}.</p>
     *
     * @return Instancia de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}