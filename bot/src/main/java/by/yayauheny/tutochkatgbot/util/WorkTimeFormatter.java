package by.yayauheny.tutochkatgbot.util;

import java.util.Map;
import java.util.Optional;

public final class WorkTimeFormatter {
    
    private WorkTimeFormatter() {}
    
    private static final Map<String, String> DAY_NAMES = Map.of(
        "Mon", "Пн",
        "Tue", "Вт", 
        "Wed", "Ср",
        "Thu", "Чт",
        "Fri", "Пт",
        "Sat", "Сб",
        "Sun", "Вс"
    );
    
    private static final String[] DAY_ORDER = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    
    public static String formatWorkTime(Map<String, Object> workTime) {
        if (workTime == null || workTime.isEmpty()) {
            return "Время работы не указано";
        }
        
        StringBuilder result = new StringBuilder();
        
        String comment = extractComment(workTime);
        if (comment != null && !comment.trim().isEmpty()) {
            result.append(comment).append("\n");
        }
        
        boolean firstDay = true;
        
        for (String day : DAY_ORDER) {
            if (workTime.containsKey(day)) {
                if (!firstDay) {
                    result.append("\n");
                }
                
                String dayName = DAY_NAMES.get(day);
                String daySchedule = formatDaySchedule(workTime.get(day));
                result.append(dayName).append(": ").append(daySchedule);
                firstDay = false;
            }
        }
        
        return result.toString();
    }
    
    public static String getSimplifiedWorkTime(Map<String, Object> workTime) {
        if (workTime == null || workTime.isEmpty()) {
            return null;
        }
        
        String firstDaySchedule = null;
        boolean allDaysSame = true;
        
        for (String day : DAY_ORDER) {
            if (workTime.containsKey(day)) {
                String daySchedule = formatDaySchedule(workTime.get(day));
                if (firstDaySchedule == null) {
                    firstDaySchedule = daySchedule;
                } else if (!firstDaySchedule.equals(daySchedule)) {
                    allDaysSame = false;
                    break;
                }
            }
        }
        
        if (allDaysSame && firstDaySchedule != null) {
            return "Ежедневно: " + firstDaySchedule;
        }
        
        for (String day : DAY_ORDER) {
            if (workTime.containsKey(day)) {
                String dayName = DAY_NAMES.get(day);
                String daySchedule = formatDaySchedule(workTime.get(day));
                return dayName + ": " + daySchedule;
            }
        }
        
        return null;
    }
    
    private static String extractComment(Map<String, Object> workTime) {
        return Optional.ofNullable(workTime.get("comment"))
            .map(Object::toString)
            .orElse(null);
    }
    
    @SuppressWarnings("unchecked")
    private static String formatDaySchedule(Object dayData) {
        if (!(dayData instanceof Map)) {
            return "не указано";
        }
        
        Map<String, Object> dayMap = (Map<String, Object>) dayData;
        Object workingHoursObj = dayMap.get("working_hours");
        
        if (!(workingHoursObj instanceof java.util.List)) {
            return "не указано";
        }
        
        java.util.List<?> workingHours = (java.util.List<?>) workingHoursObj;
        if (workingHours.isEmpty()) {
            return "не указано";
        }
        
        StringBuilder dayResult = new StringBuilder();
        boolean firstPeriod = true;
        
        for (Object period : workingHours) {
            if (period instanceof Map) {
                Map<String, Object> periodMap = (Map<String, Object>) period;
                String from = safeToString(periodMap.get("from"));
                String to = safeToString(periodMap.get("to"));
                
                if (from == null || to == null || from.isBlank() || to.isBlank()) {
                    continue;
                }
                
                if (!firstPeriod) {
                    dayResult.append(", ");
                }
                
                if (is24x7(from, to)) {
                    dayResult.append("00:00-24:00");
                } else {
                    dayResult.append(from).append("-").append(to);
                }
                firstPeriod = false;
            }
        }
        
        return dayResult.length() > 0 ? dayResult.toString() : "не указано";
    }
    
    private static String safeToString(Object value) {
        return value == null ? null : value.toString();
    }
    
    private static boolean is24x7(String from, String to) {
        return ("00:00".equals(from) && "24:00".equals(to)) || 
               ("00:00".equals(from) && "00:00".equals(to));
    }
}