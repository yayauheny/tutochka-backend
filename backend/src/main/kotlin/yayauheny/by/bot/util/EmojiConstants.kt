package yayauheny.by.bot.util

object EmojiConstants {
    const val RED_CIRCLE = "🔴"
    const val GREEN_CIRCLE = "🟢"
    const val BLUE_CIRCLE = "🔵"
    const val ORANGE_CIRCLE = "🟠"
    const val PURPLE_CIRCLE = "🟣"
    const val YELLOW_CIRCLE = "🟡"
    const val BROWN_CIRCLE = "🟤"
    const val WHITE_CIRCLE = "⚪"
    const val BLACK_CIRCLE = "⚫"
    const val METRO = "🚇"

    const val PIN = "📍"
    const val TAG = "🏷"
    const val ACCESSIBILITY = "♿"
    const val CLOCK = "⏰"
    const val BUILDING = "🏢"
    const val NOTE = "📝"
    const val ROUTE = "🧭"
    const val MAP = "🗺"
    const val FREE = "🆓"
    const val PAID = "💰"

    fun getEmojiForColor(hex: String?): String {
        if (hex == null) {
            return METRO
        }

        return when (hex.uppercase()) {
            "#EF161E", "#FF0000", "#D92B2C" -> RED_CIRCLE
            "#2DBE2C", "#00FF00", "#008000" -> GREEN_CIRCLE
            "#0078BF", "#0000FF", "#2665C5" -> BLUE_CIRCLE
            "#ED9121", "#FFA500" -> ORANGE_CIRCLE
            "#800080", "#A20A78" -> PURPLE_CIRCLE
            "#FFD702", "#FFFF00" -> YELLOW_CIRCLE
            "#8D5B2D", "#8E471C" -> BROWN_CIRCLE
            "#999999", "#808080" -> WHITE_CIRCLE
            "#000000" -> BLACK_CIRCLE
            else -> METRO
        }
    }
}
