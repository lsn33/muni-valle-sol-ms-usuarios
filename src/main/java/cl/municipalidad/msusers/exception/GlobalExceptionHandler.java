package cl.municipalidad.msusers.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para el microservicio de usuarios.
 *
 * <p>Intercepta las excepciones lanzadas en cualquier controlador y las
 * transforma en respuestas HTTP estructuradas y legibles, evitando que
 * Spring devuelva stack traces o mensajes genéricos al cliente.</p>
 *
 * <p>Centraliza el manejo de errores siguiendo el principio de
 * responsabilidad única: los controladores no necesitan try-catch,
 * esta clase se encarga de todo.</p>
 *
 * <p><b>Formato estándar de respuesta de error:</b>
 * <pre>{@code
 * {
 *   "timestamp": "2025-06-01T16:00:00Z",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "mensaje": "Datos de entrada invalidos",
 *   "detalle": {
 *     "email": "El email no tiene un formato válido",
 *     "password": "La contraseña debe tener entre 8 y 72 caracteres"
 *   }
 * }
 * }</pre></p>
 *
 * <p><b>Excepciones manejadas:</b>
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} → HTTP 400 (errores de validación @Valid)</li>
 *   <li>{@link IllegalArgumentException} → HTTP 409 (email duplicado u otras reglas de negocio)</li>
 *   <li>{@link BadCredentialsException} → HTTP 401 (credenciales inválidas en login)</li>
 *   <li>{@link Exception} → HTTP 500 (errores inesperados)</li>
 * </ul></p>
 *
 * @author Municipalidad Valle del Sol
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de Bean Validation ({@code @Valid}).
     *
     * <p>Se activa cuando un campo de un DTO de entrada no cumple las
     * restricciones definidas. Recopila todos los errores por campo
     * y los devuelve en el campo {@code detalle}.</p>
     *
     * @param ex Excepción lanzada por Spring cuando falla la validación.
     * @return HTTP 400 con mapa de errores por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalido",
                        (a, b) -> a
                ));
        return buildError(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", errores);
    }

    /**
     * Maneja violaciones de reglas de negocio (ej: email duplicado).
     *
     * @param ex Excepción lanzada por el servicio.
     * @return HTTP 409 Conflict con el mensaje descriptivo del error.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /**
     * Maneja credenciales inválidas durante el login.
     *
     * <p>El mensaje es siempre genérico ("Credenciales invalidas") para no
     * revelar si fue el email o la contraseña lo que falló.</p>
     *
     * @param ex Excepción lanzada por el servicio cuando las credenciales son incorrectas.
     * @return HTTP 401 Unauthorized con mensaje genérico.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", null);
    }

    /**
     * Maneja cualquier excepción no contemplada por los handlers anteriores.
     *
     * <p>Actúa como red de seguridad para errores inesperados, evitando
     * exponer detalles internos del sistema al cliente.</p>
     *
     * @param ex Excepción genérica no manejada.
     * @return HTTP 500 con mensaje genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
    }

    /**
     * Construye la estructura estándar de respuesta de error.
     *
     * <p>Usa {@link LinkedHashMap} para mantener el orden de los campos
     * en el JSON de respuesta.</p>
     *
     * @param status  Código de estado HTTP.
     * @param mensaje Descripción del error ocurrido.
     * @param detalle Información adicional opcional (ej: mapa de errores por campo).
     * @return {@link ResponseEntity} con el cuerpo estructurado.
     */
    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String mensaje, Object detalle) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        if (detalle != null) {
            body.put("detalle", detalle);
        }
        return ResponseEntity.status(status).body(body);
    }
}