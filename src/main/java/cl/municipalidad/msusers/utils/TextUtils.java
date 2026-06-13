package cl.municipalidad.msusers.utils;

public class TextUtils {
    public static String clean(String txt) {
        return txt.trim();
    }

    /**
     * Limpia los espacios vacios de los extremos y deja el texto en minusculas
     * @param String txt
     * <pre>
     * {@code
     * TextUtils.toLower(" Juan") // Produce "juan"
     * }
     * </pre>
     * @return String
     */
    public static String toLower(String txt) {
        return TextUtils.clean(txt).toLowerCase();
    }

    public static String toUpper(String txt) {
        return TextUtils.clean(txt).toUpperCase();
    }
}
