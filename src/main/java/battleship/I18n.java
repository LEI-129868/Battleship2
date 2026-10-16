package battleship;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.ULocale;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for internationalization (i18n) of game messages using ICU4J.
 */
public final class I18n {

    public static final Locale PORTUGUESE = Locale.of("pt", "PT");
    public static final Locale ENGLISH = Locale.of("en", "US");

    private static Locale currentLocale = PORTUGUESE;
    private static ResourceBundle currentBundle;
    private static final Map<String, MessageFormat> formatCache = new ConcurrentHashMap<>();

    static {
        loadBundle();
    }

    private I18n() {
        // utility class
    }

    /**
     * Updates the active locale and reloads the resource bundle.
     *
     * @param locale the target locale
     */
    public static synchronized void setLocale(Locale locale) {
        if (locale == null) {
            locale = PORTUGUESE;
        }
        currentLocale = locale;
        formatCache.clear();
        loadBundle();
    }

    /**
     * Updates the active locale based on language code string (e.g. "pt", "en").
     *
     * @param langCode the language code
     */
    public static void setLocale(String langCode) {
        if (langCode == null || langCode.isBlank()) {
            setLocale(PORTUGUESE);
            return;
        }
        String normalized = langCode.trim().toLowerCase();
        if (normalized.startsWith("en")) {
            setLocale(ENGLISH);
        } else {
            setLocale(PORTUGUESE);
        }
    }

    /**
     * Returns the active locale.
     *
     * @return current locale
     */
    public static Locale getLocale() {
        return currentLocale;
    }

    private static void loadBundle() {
        try {
            currentBundle = ResourceBundle.getBundle("messages", currentLocale);
        } catch (MissingResourceException e) {
            currentBundle = ResourceBundle.getBundle("messages", Locale.ROOT);
        }
    }

    /**
     * Retrieves the localized string for a key, applying arguments formatting using ICU4J MessageFormat.
     *
     * @param key  the message key in properties
     * @param args the arguments for the pattern
     * @return the localized and formatted string
     */
    public static String get(String key, Object... args) {
        if (currentBundle == null) {
            loadBundle();
        }
        String pattern;
        try {
            pattern = currentBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }

        if (args == null || args.length == 0) {
            return pattern;
        }

        ULocale uLocale = ULocale.forLocale(currentLocale);
        MessageFormat formatter = formatCache.computeIfAbsent(
                currentLocale.toLanguageTag() + ":" + pattern,
                k -> new MessageFormat(pattern, uLocale)
        );

        synchronized (formatter) {
            return formatter.format(args);
        }
    }

    /**
     * Returns the localized name of a ship category.
     *
     * @param category the ship category code/name
     * @return localized ship category name
     */
    public static String getShipCategory(String category) {
        if (category == null) {
            return "";
        }
        return switch (category.toLowerCase()) {
            case "barca", "barge" -> get("ship.barge");
            case "caravela", "caravel" -> get("ship.caravel");
            case "nau", "carrack" -> get("ship.carrack");
            case "fragata", "frigate" -> get("ship.frigate");
            case "galeao", "galeão", "galleon" -> get("ship.galleon");
            default -> category;
        };
    }
}
