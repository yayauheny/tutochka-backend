package by.yayauheny.tutochkatgbot.util;

public final class EmojiConstants {
    private EmojiConstants() {}

    public static final String RED_CIRCLE = "🔴";
    public static final String GREEN_CIRCLE = "🟢";
    public static final String BLUE_CIRCLE = "🔵";
    public static final String ORANGE_CIRCLE = "🟠";
    public static final String PURPLE_CIRCLE = "🟣";
    public static final String YELLOW_CIRCLE = "🟡";
    public static final String BROWN_CIRCLE = "🟤";
    public static final String WHITE_CIRCLE = "⚪";
    public static final String BLACK_CIRCLE = "⚫";
    public static final String METRO = "🚇";

    public static final String PIN = "📍";
    public static final String TAG = "🏷";
    public static final String ACCESSIBILITY = "♿";
    public static final String CLOCK = "⏰";
    public static final String BUILDING = "🏢";
    public static final String NOTE = "📝";
    public static final String ROUTE = "🧭";
    public static final String MAP = "🗺";
    public static final String FREE = "🆓";
    public static final String PAID = "💸";

    public static String getEmojiForColor(String hex) {
        if (hex == null) {
            return METRO;
        }

        String upperHex = hex.toUpperCase();
        switch (upperHex) {
            case "#EF161E":
            case "#FF0000":
            case "#D92B2C":
                return RED_CIRCLE;
            case "#2DBE2C":
            case "#00FF00":
            case "#008000":
                return GREEN_CIRCLE;
            case "#0078BF":
            case "#0000FF":
            case "#2665C5":
                return BLUE_CIRCLE;
            case "#ED9121":
            case "#FFA500":
                return ORANGE_CIRCLE;
            case "#800080":
            case "#A20A78":
                return PURPLE_CIRCLE;
            case "#FFD702":
            case "#FFFF00":
                return YELLOW_CIRCLE;
            case "#8D5B2D":
            case "#8E471C":
                return BROWN_CIRCLE;
            case "#999999":
            case "#808080":
                return WHITE_CIRCLE;
            case "#000000":
                return BLACK_CIRCLE;
            default:
                return METRO;
        }
    }
}
