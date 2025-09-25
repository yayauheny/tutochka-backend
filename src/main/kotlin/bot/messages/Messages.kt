package yayauheny.by.bot.messages

/**
 * Bot messages and constants
 */
object Messages {
    // Button texts
    val BUTTON_SHARE_LOCATION = "📍 Поделиться геолокацией"
    val BUTTON_BACK_TO_LIST = "⬅️ Назад к списку"
    val BUTTON_OPEN_MAPS = "🗺 Открыть в картах"
    val BUTTON_SEARCH_AGAIN = "🔍 Поиск заново"
    val BUTTON_HELP = "❓ Помощь"

    // Callback data
    val CALLBACK_TOILET_DETAIL = "toilet_detail_"
    val CALLBACK_BACK_TO_LIST = "back_to_list"
    val CALLBACK_RADIUS_SELECT = "radius_select_"

    // Radius options
    val RADIUS_500M = "500м"
    val RADIUS_1KM = "1км"
    val RADIUS_2KM = "2км"
    val RADIUS_5KM = "5км"

    // Welcome messages
    val WELCOME_MESSAGE = "Привет! Поделись геолокацией для поиска туалетов."
    val HELP_MESSAGE =
        """
        🤖 Бот для поиска туалетов
        
        📍 Поделись геолокацией для поиска ближайших туалетов
        🔍 Используй кнопки для навигации
        ⬅️ Назад к списку - вернуться к списку туалетов
        🗺 Открыть в картах - показать туалет на карте
        
        Команды:
        /start - начать работу
        /help - показать эту справку
        """.trimIndent()

    // Search messages
    val NO_TOILETS_FOUND = "К сожалению, поблизости туалетов не найдено. Попробуйте увеличить радиус поиска."
    val TOILETS_FOUND = "Найдено {count} туалетов поблизости:"
    val LOCATION_NOT_FOUND = "Геолокация не найдена. Поделись геолокацией заново:"

    // Error messages
    val TOILET_NOT_FOUND = "Туалет не найден"
    val INVALID_TOILET_ID = "Ошибка: неверный ID туалета"
    val INTERNAL_ERROR = "Произошла внутренняя ошибка. Попробуйте позже."

    // Toilet details
    val TOILET_DETAILS =
        """
        🚻 {name}
        📍 {coordinates}
        📝 {description}
        """.trimIndent()

    // Location request
    val LOCATION_REQUEST = "Поделись геолокацией для поиска туалетов:"

    // Back to list
    val BACK_TO_LIST_MESSAGE = "Для поиска туалетов поделись геолокацией:"
}
