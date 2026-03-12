package by.yayauheny.tutochkatgbot.util;

public final class TextUtil {

    private static final String LITERAL_NULL = "null";

    private TextUtil() {
    }

    /**
     * Normalizes nullable display text: null, blank, and literal "null" (and variants) become null.
     * "null, null" / "null,null" (with or without spaces) treated as empty.
     */
    public static String normalizeNullableText(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        if (LITERAL_NULL.equalsIgnoreCase(t)) {
            return null;
        }
        String compact = t.replace(" ", "");
        if ("null,null".equalsIgnoreCase(compact)) {
            return null;
        }
        return t;
    }
}
