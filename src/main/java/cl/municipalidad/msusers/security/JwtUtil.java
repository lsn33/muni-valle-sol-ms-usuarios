package cl.municipalidad.msusers.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

/**
 * Utilidad para la generación y validación de tokens JWT (JSON Web Token).
 *
 * <p>Centraliza toda la lógica relacionada con JWT en el microservicio de usuarios:
 * creación de tokens firmados, extracción del email del subject y validación
 * de integridad/expiración.</p>
 *
 * <p>Los tokens se firman con el algoritmo <b>HMAC-SHA256 (HS256)</b> usando
 * una clave secreta inyectada desde variables de entorno. El email del usuario
 * se almacena como {@code subject} y el rol como claim personalizado.</p>
 *
 * <p><b>Configuración requerida en {@code application.properties}:</b>
 * <pre>
 * jwt.secret=${JWT_SECRET}
 * jwt.expiration=${JWT_EXPIRATION}
 * </pre></p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 */
@Component
public class JwtUtil {

    /** Clave secreta para firmar los tokens. Se carga desde variable de entorno. */
    @Value("${jwt.secret}")
    private String secret;

    /** Tiempo de expiración del token en milisegundos. */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Construye la clave criptográfica a partir del secreto configurado.
     *
     * @return {@link Key} HMAC-SHA256 lista para firmar o verificar tokens.
     */
    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Genera un token JWT firmado con el email y rol del usuario.
     *
     * <p>El token incluye:
     * <ul>
     *   <li>{@code sub} (subject): email del usuario</li>
     *   <li>{@code rol}: claim personalizado con el rol</li>
     *   <li>{@code iat}: fecha de emisión</li>
     *   <li>{@code exp}: fecha de expiración</li>
     * </ul></p>
     *
     * @param email Correo electrónico del usuario autenticado.
     * @param rol   Rol del usuario (ej: ADMIN, FUNCIONARIO).
     * @return Token JWT firmado como {@code String}.
     */
    public String generarToken(String email, String rol) {
        return Jwts.builder()
                .setSubject(email)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el email (subject) de un token JWT válido.
     *
     * @param token Token JWT del cual extraer el email.
     * @return Email del usuario contenido en el token.
     * @throws JwtException si el token es inválido o está expirado.
     */
    public String obtenerEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Valida si un token JWT es auténtico y no ha expirado.
     *
     * <p>Retorna {@code false} ante cualquier error: firma incorrecta,
     * token expirado, formato inválido, etc. No lanza excepciones.</p>
     *
     * @param token Token JWT a validar.
     * @return {@code true} si el token es válido, {@code false} en caso contrario.
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}