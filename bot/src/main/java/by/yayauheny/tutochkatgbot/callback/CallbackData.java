package by.yayauheny.tutochkatgbot.callback;

/**
 * Utility class for encoding/decoding callback data
 */
public final class CallbackData {
    
    private CallbackData() {}
    
    public static String detail(String id) { 
        return "t:" + id; 
    }
    
    public static String moreDetails(String id) { 
        return "m:" + id; 
    }
    
    public static String hideDetails(String id) { 
        return "h:" + id; 
    }
    
    public static String backToList() { 
        return "b:list"; 
    }
    
    public static String radius(int meters) { 
        return "r:" + meters; 
    }

    public static boolean isType(String data, String type) { 
        return data.startsWith(type + ":"); 
    }
    
    public static String arg(String data) { 
        int i = data.indexOf(':'); 
        return i >= 0 ? data.substring(i + 1) : ""; 
    }
}
