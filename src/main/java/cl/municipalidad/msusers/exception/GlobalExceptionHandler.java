package cl.municipalidad.msusers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

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
 * <p><b>Excepciones manejadas:</b>
 * <ul>
 *   <li>{@link MethodArgumentNotValidException} → HTTP 400 (errores de validación @Valid)</li>
 *   <li>{@link IllegalArgumentException} → HTTP 400 (argumentos inválidos)</li>
 *   <li>{@link RuntimeException} → HTTP 409 (reglas de negocio, ej: email duplicado)</li>
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
     * restricciones definidas ({@code @NotBlank}, {@code @Email}, etc.).
     * Recopila todos los errores de campo y los devuelve en un solo mensaje.</p>
     *
     * <p>Ejemplo de respuesta:
     * <pre>{@code
     * {
     *   "error": "email: El email no tiene un formato válido, nombre: El nombre es obligatorio",
     *   "timestamp": "2025-06-01T12:00:00"
     * }
     * }</pre></p>
     *
     * @param ex Excepción lanzada por Spring cuando falla la validación.
     * @return HTTP 400 con los mensajes de error de cada campo inválido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(mensaje));
    }

    /**
     * Maneja excepciones de reglas de negocio (email duplicado, etc.).
     *
     * <p>Se activa cuando el servicio lanza una {@link RuntimeException}
     * por una violación de regla de negocio, como intentar registrar
     * un email que ya existe en el sistema.</p>
     *
     * <p>Ejemplo de respuesta:
     * <pre>{@code
     * {
     *   "error": "El email ya está registrado",
     *   "timestamp": "2025-06-01T12:00:00"
     * }
     * }</pre></p>
     *
     * @param ex Excepción lanzada por el servicio.
     * @return HTTP 409 Conflict con el mensaje descriptivo del error.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildError(ex.getMessage()));
    }

    /**
     * Maneja argumentos inválidos pasados directamente al servicio.
     *
     * @param ex Excepción lanzada por lógica interna.
     * @return HTTP 400 con el mensaje del error.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.getMessage()));
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
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError("Error interno del servidor. Intente más tarde."));
    }

    /**
     * Construye la estructura estándar de respuesta de error.
     *
     * <p>Centraliza el formato de error para que todas las respuestas
     * de excepción tengan la misma estructura JSON.</p>
     *
     * @param mensaje Descripción del error ocurrido.
     * @return Mapa con los campos {@code error} y {@code timestamp}.
     */
    private Map<String, Object> buildError(String mensaje) {
        return Map.of(
                "error", mensaje,
                "timestamp", LocalDateTime.now().toString()
        );
    }
}