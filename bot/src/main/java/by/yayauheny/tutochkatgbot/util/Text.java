package by.yayauheny.tutochkatgbot.util;

import java.util.Map;

/**
 * Utility class for text template substitution
 */
public final class Text {
    
    private Text() {}
    
    /**
     * Replace placeholders in template with values
     * @param template template string with {key} placeholders
     * @param values map of key-value pairs
     * @return processed string
     */
    public static String substitute(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
    
    /**
     * Simple replacement for single placeholder
     * @param template template string
     * @param placeholder placeholder to replace
     * @param value replacement value
     * @return processed string
     */
    public static String replace(String template, String placeholder, String value) {
        return template.replace("{" + placeholder + "}", value);
    }
}
