package cl.municipalidad.msusers.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import cl.municipalidad.msusers.model.User;

/**
 * Servicio de generación de tokens JWT firmados con RSA-256.
 *
 * <p>Reemplaza al anterior {@code JwtUtil} basado en HMAC-SHA256.
 * La diferencia clave es el algoritmo de firma:</p>
 *
 * <ul>
 *   <li><b>HMAC (anterior):</b> una sola clave secreta compartida para firmar y verificar.
 *       Todos los MS necesitaban tener esa clave, lo que aumenta el riesgo.</li>
 *   <li><b>RSA (actual):</b> clave privada para firmar (solo este MS la tiene) y
 *       clave pública para verificar (cualquier MS puede tenerla sin riesgo).
 *       Esto sigue el estándar OAuth2/OIDC de la industria.</li>
 * </ul>
 *
 * <p>Los tokens generados incluyen los claims estándar JWT más claims
 * personalizados del dominio (nombre, rol).</p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 * @see cl.municipalidad.msusers.config.JwtKeyConfig
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenMinutes;

    /**
     * Constructor con inyección de dependencias y valores de configuración.
     *
     * @param jwtEncoder         Encoder JWT configurado con las claves RSA.
     * @param issuer             Identificador del emisor del token (URL del MS).
     * @param accessTokenMinutes Tiempo de vida del token en minutos (default: 30).
     */
    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-token-minutes:30}") long accessTokenMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    /**
     * Genera un token JWT de acceso firmado con RS256 para el usuario dado.
     *
     * <p><b>Claims incluidos en el token:</b>
     * <ul>
     *   <li>{@code jti} — ID único del token (UUID v4)</li>
     *   <li>{@code iss} — emisor: URL de ms-usuarios</li>
     *   <li>{@code iat} — fecha de emisión</li>
     *   <li>{@code exp} — fecha de expiración</li>
     *   <li>{@code sub} — email del usuario</li>
     *   <li>{@code nombre} — nombre completo del usuario</li>
     *   <li>{@code rol} — rol del usuario (ADMIN, FUNCIONARIO, CIUDADANO)</li>
     *   <li>{@code aud} — audiencia: {@code ["ms-usuarios"]}</li>
     * </ul></p>
     *
     * @param user Entidad del usuario autenticado para extraer sus datos.
     * @return Token JWT firmado como {@code String} en formato Base64url.
     */
    public String generarAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiracion = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiracion)
                .subject(user.getEmail())
                .claim("nombre", user.getNombre())
                .claim("rol", user.getRol())
                .audience(List.of("ms-usuarios"))
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId("muni-key-1")
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * Retorna el tiempo de vida del token en segundos.
     *
     * <p>Usado por {@link cl.municipalidad.msusers.dto.AuthResponse}
     * para informar al cliente cuándo expirará el token.</p>
     *
     * @return Segundos de vida del token (ej: 30 minutos = 1800 segundos).
     */
    public long getAccessTokenTtlSeconds() {
        return accessTokenMinutes * 60;
    }
}