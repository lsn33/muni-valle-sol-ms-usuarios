package cl.municipalidad.msusers.glitchtip;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

/**
 * Punto central para enviar excepciones y mensajes a GlitchTip.
 *
 * <p>Se inyecta en el {@code GlobalExceptionHandler} para reportar los
 * errores no controlados (500) a la seccion "Issues" de GlitchTip, y puede
 * usarse tambien desde cualquier servicio para reportar manualmente.</p>
 */
@Service
public class GlitchTipErrorReporter {

	private static final Logger logger = LoggerFactory.getLogger(GlitchTipErrorReporter.class);

	public void captureException(Throwable throwable) {
		logger.error("Error capturado para envio a GlitchTip", throwable);
		Sentry.captureException(throwable);
	}

	public void captureException(Throwable throwable, String context) {
		logger.error("{}: {}", context, throwable.getMessage(), throwable);
		Sentry.withScope(scope -> {
			scope.setExtra("context", context);
			Sentry.captureException(throwable);
		});
	}

	public void captureMessage(String message, SentryLevel level) {
		logger.atLevel(toSlf4jLevel(level)).log(message);
		Sentry.captureMessage(message, level);
	}

	/** Fuerza el envio de eventos pendientes antes de apagar la app. */
	public void flush() {
		Sentry.flush(2000);
	}

	private static Level toSlf4jLevel(SentryLevel level) {
		return switch (level) {
			case DEBUG -> Level.DEBUG;
			case INFO -> Level.INFO;
			case WARNING -> Level.WARN;
			case ERROR, FATAL -> Level.ERROR;
			default -> Level.INFO;
		};
	}

}
