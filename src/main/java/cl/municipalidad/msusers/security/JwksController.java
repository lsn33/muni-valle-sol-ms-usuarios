package cl.municipalidad.msusers.security;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

/**
 * Controlador que expone la clave pública RSA en formato JWKS.
 *
 * <p>Implementa el endpoint estándar {@code /.well-known/jwks.json} del
 * protocolo OAuth2/OIDC. Los demás microservicios (MS-Reportes, MS-Brigadas,
 * BFF) pueden consultar esta URL para obtener la clave pública y verificar
 * la firma de los tokens JWT sin necesidad de la clave privada.</p>
 *
 * <p><b>Flujo de verificación:</b>
 * <pre>
 * MS-Reportes recibe token JWT del BFF
 *     ↓
 * Consulta GET /.well-known/jwks.json en MS-Usuarios
 *     ↓
 * Obtiene la clave pública RSA
 *     ↓
 * Verifica la firma del token localmente
 * </pre></p>
 *
 * <p>Este endpoint es público y no requiere autenticación, ya que
 * la clave pública no es información sensible.</p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 * @see cl.municipalidad.msusers.config.JwtKeyConfig
 */
@RestController
public class JwksController {

    private final RSAKey rsaKey;

    /**
     * Constructor con inyección del par de claves RSA.
     *
     * @param rsaKey Par de claves RSA configurado en
     *               {@link cl.municipalidad.msusers.config.JwtKeyConfig}.
     */
    public JwksController(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
    }

    /**
     * Retorna el conjunto de claves públicas en formato JWKS (JSON Web Key Set).
     *
     * <p><b>GET</b> {@code /.well-known/jwks.json}</p>
     *
     * <p>Solo expone la clave pública ({@code toPublicJWK()}), nunca la privada.</p>
     *
     * @return Mapa con la representación JSON de las claves públicas del sistema.
     */
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}