package cl.municipalidad.msusers.glitchtip;

import io.sentry.Sentry;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Envoltorio sobre SLF4J que ademas envia el mensaje explicitamente a la
 * seccion "Logs" de GlitchTip via {@code Sentry.logger()}.
 *
 * <p>Uso:</p>
 * <pre>{@code
 * private static final Logger logger = LoggerFactory.getLogger(MiClase.class);
 *
 * @Autowired
 * private GlitchTipLogger glitchTipLogger;
 *
 * glitchTipLogger.info(logger, "Operacion completada");
 * }</pre>
 */
@Component
public class GlitchTipLogger {

	public void debug(Logger logger, String message, Object... args) {
		logger.debug(message, args);
		Sentry.logger().debug(message, args);
	}

	public void info(Logger logger, String message, Object... args) {
		logger.info(message, args);
		Sentry.logger().info(message, args);
	}

	public void warn(Logger logger, String message, Object... args) {
		logger.warn(message, args);
		Sentry.logger().warn(message, args);
	}

	public void error(Logger logger, String message, Object... args) {
		logger.error(message, args);
		Sentry.logger().error(message, args);
	}

	public void error(Logger logger, String message, Throwable throwable) {
		logger.error(message, throwable);
		Sentry.logger().error("{}: {}", message, throwable.getMessage());
	}

}
