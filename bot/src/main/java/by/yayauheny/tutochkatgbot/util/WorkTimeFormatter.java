package by.yayauheny.tutochkatgbot.util;

import java.util.Map;
import java.util.Optional;
import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonElementKt;
import kotlinx.serialization.json.JsonObject;
import kotlinx.serialization.json.JsonPrimitive;

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

    public static String formatWorkTime(JsonObject workTime) {
        if (workTime == null || workTime.isEmpty()) {
            return "Время работы не указано";
        }

        if (isTrue(workTime.get("is_24x7"))) {
            return "Круглосуточно";
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

        if (firstDay && result.length() == 0) {
            return "Время работы не указано";
        }

        return result.toString();
    }

    public static String getSimplifiedWorkTime(JsonObject workTime) {
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

    private static String extractComment(JsonObject workTime) {
        return asString(workTime.get("comment"));
    }

    private static String formatDaySchedule(JsonElement dayData) {
        if (!(dayData instanceof JsonObject dayMap)) {
            return "не указано";
        }

        JsonElement workingHoursObj = dayMap.get("working_hours");
        if (workingHoursObj instanceof JsonArray workingHours && !workingHours.isEmpty()) {
            StringBuilder dayResult = new StringBuilder();
            boolean firstPeriod = true;
            for (JsonElement period : workingHours) {
                if (period instanceof JsonObject periodMap) {
                    String from = asString(periodMap.get("from"));
                    String to = asString(periodMap.get("to"));
                    if (from == null || to == null || from.isBlank() || to.isBlank()) {
                        continue;
                    }
                    if (!firstPeriod) {
                        dayResult.append(", ");
                    }
                    dayResult.append(is24x7(from, to) ? "00:00-24:00" : from + "-" + to);
                    firstPeriod = false;
                }
            }
            if (dayResult.length() > 0) {
                return dayResult.toString();
            }
        }

        if (dayMap.containsKey("from") && dayMap.containsKey("to")) {
            String from = asString(dayMap.get("from"));
            String to = asString(dayMap.get("to"));
            if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
                return is24x7(from, to) ? "00:00-24:00" : from + "-" + to;
            }
        }
        return "не указано";
    }

    private static String asString(JsonElement value) {
        if (value instanceof JsonPrimitive primitive) {
            return primitive.getContent();
        }
        return value == null ? null : value.toString();
    }

    private static boolean isTrue(JsonElement value) {
        if (!(value instanceof JsonPrimitive primitive)) {
            return false;
        }
        Boolean booleanValue = JsonElementKt.getBooleanOrNull(primitive);
        return Boolean.TRUE.equals(booleanValue);
    }

    private static boolean is24x7(String from, String to) {
        return ("00:00".equals(from) && "24:00".equals(to)) ||
               ("00:00".equals(from) && "00:00".equals(to));
    }
}
