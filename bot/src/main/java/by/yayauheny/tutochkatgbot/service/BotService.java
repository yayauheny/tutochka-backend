package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.messages.Messages;
import org.springframework.stereotype.Service;

/**
 * Service class for bot business logic
 */
@Service
public class BotService {

    /**
     * Process location request
     * @param chatId chat ID
     * @return response message
     */
    public String processLocationRequest(long chatId) {
        return Messages.LOCATION_REQUEST;
    }

    /**
     * Process location data
     * @param latitude latitude
     * @param longitude longitude
     * @param chatId chat ID
     * @return response message
     */
    public String processLocation(double latitude, double longitude, long chatId) {
        return "Геолокация получена: " + latitude + ", " + longitude;
    }

    /**
     * Process callback query
     * @param callbackData callback data
     * @param chatId chat ID
     * @return response message
     */
    public String processCallback(String callbackData, long chatId) {
        if (callbackData.startsWith(Messages.CALLBACK_TOILET_DETAIL)) {
            String toiletId = callbackData.substring(Messages.CALLBACK_TOILET_DETAIL.length());
            return getToiletDetails(toiletId);
        } else if (callbackData.equals(Messages.CALLBACK_BACK_TO_LIST)) {
            return Messages.BACK_TO_LIST_MESSAGE;
        }
        return Messages.INTERNAL_ERROR;
    }

    /**
     * Get toilet details by ID
     * @param toiletId toilet ID
     * @return toilet details
     */
    private String getToiletDetails(String toiletId) {
        return Messages.TOILET_DETAILS
                .replace("{name}", "Туалет #" + toiletId)
                .replace("{coordinates}", "55.7558, 37.6176")
                .replace("{description}", "Описание туалета");
    }
}

