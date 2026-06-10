package cl.municipalidad.msusers.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Configuración de claves RSA para la firma y verificación de tokens JWT.
 *
 * <p>Carga las claves RSA desde archivos {@code .pem} externos al proyecto
 * (nunca dentro del código fuente o del repositorio). Expone los beans
 * necesarios para que {@link cl.municipalidad.msusers.service.JwtService}
 * pueda firmar tokens con RS256.</p>
 *
 * <p><b>¿Por qué RSA en vez de HMAC?</b><br>
 * Con HMAC (HS256), la misma clave sirve para firmar y verificar, lo que
 * obliga a compartirla entre todos los microservicios. Con RSA (RS256),
 * solo este MS tiene la clave privada para firmar; los demás MS usan la
 * clave pública (expuesta en {@code /.well-known/jwks.json}) solo para
 * verificar. Esto reduce el riesgo de compromiso.</p>
 *
 * <p><b>Archivos requeridos</b> (generados con OpenSSL, nunca en GitHub):
 * <pre>
 * openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private_key.pem
 * openssl rsa -pubout -in private_key.pem -out public_key.pem
 * </pre></p>
 *
 * <p><b>Configuración requerida en {@code application.yml}:</b>
 * <pre>
 * security:
 *   jwt:
 *     private-key-path: ${JWT_PRIVATE_KEY_PATH:../private_key.pem}
 *     public-key-path:  ${JWT_PUBLIC_KEY_PATH:../public_key.pem}
 * </pre></p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 */
@Configuration
public class JwtKeyConfig {

    /**
     * Carga la clave privada RSA desde un archivo PEM.
     *
     * <p>Elimina los headers/footers del PEM y decodifica el contenido
     * Base64 para construir la clave usando {@link KeyFactory}.</p>
     *
     * @param path Ruta al archivo {@code private_key.pem}, inyectada desde variable de entorno.
     * @return {@link RSAPrivateKey} lista para firmar tokens JWT.
     * @throws IllegalStateException si el archivo no existe o tiene un formato inválido.
     */
    @Bean
    RSAPrivateKey privateKey(@Value("${security.jwt.private-key-path}") String path) {
        try {
            String pem = Files.readString(Path.of(path))
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(pem);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("No se pudo cargar la clave privada desde: " + path, e);
        }
    }

    /**
     * Carga la clave pública RSA desde un archivo PEM.
     *
     * <p>La clave pública se comparte con los demás microservicios a través
     * del endpoint {@code /.well-known/jwks.json} para que puedan verificar
     * los tokens sin necesidad de la clave privada.</p>
     *
     * @param path Ruta al archivo {@code public_key.pem}, inyectada desde variable de entorno.
     * @return {@link RSAPublicKey} lista para verificar tokens JWT.
     * @throws IllegalStateException si el archivo no existe o tiene un formato inválido.
     */
    @Bean
    RSAPublicKey publicKey(@Value("${security.jwt.public-key-path}") String path) {
        try {
            String pem = Files.readString(Path.of(path))
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(pem);
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(decoded));
        } catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("No se pudo cargar la clave publica desde: " + path, e);
        }
    }

    /**
     * Construye el par de claves RSA ({@link RSAKey}) usado por Nimbus JOSE.
     *
     * @param publicKey  Clave pública RSA cargada por {@link #publicKey(String)}.
     * @param privateKey Clave privada RSA cargada por {@link #privateKey(String)}.
     * @return {@link RSAKey} con ambas claves y el ID {@code "muni-key-1"}.
     */
    @Bean
    RSAKey rsaKey(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("muni-key-1")
                .build();
    }

    /**
     * Expone el conjunto de claves JWK como fuente para el encoder JWT.
     *
     * @param rsaKey Par de claves RSA construido por {@link #rsaKey(RSAPublicKey, RSAPrivateKey)}.
     * @return {@link JWKSource} inmutable con el par de claves.
     */
    @Bean
    JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    /**
     * Bean de codificación JWT usando Nimbus con el par de claves RSA.
     *
     * <p>Usado por {@link cl.municipalidad.msusers.service.JwtService}
     * para firmar los tokens con el algoritmo RS256.</p>
     *
     * @param jwkSource Fuente de claves JWK construida por {@link #jwkSource(RSAKey)}.
     * @return {@link JwtEncoder} listo para firmar tokens JWT.
     */
    @Bean
    JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
}