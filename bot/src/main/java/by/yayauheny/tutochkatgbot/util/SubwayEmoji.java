package by.yayauheny.tutochkatgbot.util;

/**
 * Utility class for mapping subway line colors to emoji icons
 */
public final class SubwayEmoji {
    
    private SubwayEmoji() {}
    
    /**
     * Maps hex color code to emoji icon for subway line visualization
     * 
     * @param hex hex color code in format #RRGGBB (e.g., "#FF0000")
     * @return emoji string representing the color
     */
    public static String getEmojiForColor(String hex) {
        if (hex == null) {
            return "🚇";
        }
        
        String upperHex = hex.toUpperCase();
        
        switch (upperHex) {
            case "#EF161E":
            case "#FF0000":
            case "#D92B2C":
                return "🔴"; // Красная
            
            case "#2DBE2C":
            case "#00FF00":
            case "#008000":
                return "🟢"; // Зеленая
            
            case "#0078BF":
            case "#0000FF":
            case "#2665C5":
                return "🔵"; // Синяя
            
            case "#ED9121":
            case "#FFA500":
                return "🟠"; // Оранжевая
            
            case "#800080":
            case "#A20A78":
                return "🟣"; // Фиолетовая
            
            case "#FFD702":
            case "#FFFF00":
                return "🟡"; // Желтая
            
            case "#8D5B2D":
            case "#8E471C":
                return "🟤"; // Коричневая
            
            case "#999999":
            case "#808080":
                return "⚪"; // Серая
            
            case "#000000":
                return "⚫"; // Черная
            
            default:
                return "🚇"; // Дефолтный значок метро, если цвет редкий
        }
    }
}
