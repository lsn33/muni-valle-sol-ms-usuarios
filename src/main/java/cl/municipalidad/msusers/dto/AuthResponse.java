package cl.municipalidad.msusers.dto;

/**
 * DTO de salida para la respuesta de autenticación exitosa.
 *
 * <p>Implementa el patrón <b>Record</b> de Java para inmutabilidad.
 * Encapsula el token JWT generado junto con metadatos necesarios
 * para que el cliente configure correctamente las peticiones
 * autenticadas.</p>
 *
 * <p><b>Ejemplo de respuesta JSON:</b>
 * <pre>{@code
 * {
 *   "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 1800
 * }
 * }</pre></p>
 *
 * <p><b>Patrón aplicado:</b> Data Transfer Object (DTO) con Java Record.</p>
 *
 * @param accessToken Token JWT firmado con RSA-256. Se incluye en el header
 *                    {@code Authorization: Bearer <token>} en cada petición.
 * @param tokenType   Tipo de token. Siempre {@code "Bearer"} según el estándar OAuth2.
 * @param expiresIn   Tiempo de vida del token en segundos (ej: 1800 = 30 minutos).
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 */
public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {}