package yayauheny.by.bot.util

object Text {
    fun substitute(template: String, values: Map<String, String>): String {
        var result = template
        for ((key, value) in values) {
            result = result.replace("{$key}", value)
        }
        return result
    }

    fun replace(template: String, placeholder: String, value: String): String {
        return template.replace("{$placeholder}", value)
    }

    fun escapeHtml(value: String?): String? {
        if (value == null) return null
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}

object TextUtil {
    private const val LITERAL_NULL = "null"

    fun normalizeNullableText(s: String?): String? {
        if (s == null) return null
        val t = s.trim()
        if (t.isEmpty()) return null
        if (LITERAL_NULL.equals(t, ignoreCase = true)) return null
        
        val compact = t.replace(" ", "")
        if ("null,null".equals(compact, ignoreCase = true)) return null
        return t
    }
}

object DistanceFormat {
    fun meters(meters: Double): String {
        return if (meters < 1000) {
            String.format("%.0f м", meters)
        } else {
            val kilometers = meters / 1000.0
            if (kilometers < 10) {
                String.format("%.1f км", kilometers)
            } else {
                String.format("%.0f км", kilometers)
            }
        }
    }

    fun shortFormat(meters: Double): String {
        return if (meters < 1000) {
            String.format("%.0fм", meters)
        } else {
            val kilometers = meters / 1000.0
            String.format("%.1fкм", kilometers)
        }
    }
}
