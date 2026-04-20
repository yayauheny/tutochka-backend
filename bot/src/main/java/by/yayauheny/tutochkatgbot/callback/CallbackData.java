package by.yayauheny.tutochkatgbot.callback;

/**
 * Utility class for encoding/decoding callback data
 */
public final class CallbackData {
    
    private CallbackData() {}
    
    public static String detail(String id) { 
        return "detail:" + id; 
    }
    
    public static String backToList() { 
        return "back:list"; 
    }
    
    public static String radius(int meters) { 
        return "radius:" + meters; 
    }

    public static String route(String provider, String restroomId) {
        return "route:" + provider + ":" + restroomId;
    }

    public static boolean isType(String data, String type) { 
        return data.startsWith(type + ":"); 
    }
    
    public static String arg(String data) { 
        int i = data.indexOf(':'); 
        return i >= 0 ? data.substring(i + 1) : ""; 
    }

    public static String routeProvider(String data) {
        String[] parts = data.split(":", 3);
        return parts.length >= 2 ? parts[1] : "";
    }

    public static String routeRestroomId(String data) {
        String[] parts = data.split(":", 3);
        return parts.length == 3 ? parts[2] : "";
    }
}
