package yayauheny.by.bot.messages

import yayauheny.by.bot.util.EmojiConstants

object Messages {

    const val BUTTON_SHARE_LOCATION = "${EmojiConstants.PIN} Поделиться геолокацией"
    const val BUTTON_BACK_TO_LIST = "⬅️ Назад к списку"
    @Deprecated("Will be removed in later refactoring")
    const val BUTTON_OPEN_MAPS = "${EmojiConstants.MAP} Открыть в картах"
    const val BUTTON_OPEN_YANDEX = "${EmojiConstants.MAP} Яндекс Карты"
    const val BUTTON_OPEN_GOOGLE = "${EmojiConstants.MAP} Google Maps"
    const val BUTTON_OPEN_2GIS = "${EmojiConstants.MAP} 2ГИС"
    const val BUTTON_OPEN_APPLE = "${EmojiConstants.MAP} Apple Maps"
    const val BUTTON_SEARCH_AGAIN = "🔍 Поиск заново"
    const val BUTTON_HELP = "❓ Помощь"
    const val BUTTON_BACK = "← Назад"

    @Deprecated("Will be removed in later refactoring")
    const val CALLBACK_TOILET_DETAIL = "toilet_detail_"
    @Deprecated("Will be removed in later refactoring")
    const val CALLBACK_BACK_TO_LIST = "back_to_list"

    const val WELCOME_MESSAGE = "Привет! Поделись геолокацией для поиска туалетов."
    val HELP_MESSAGE = """
        🤖 Бот для поиска туалетов
        
        ${EmojiConstants.PIN} Способы поиска:
        • Отправьте текущую геолокацию
        • Выберите точку на карте (скрепка → Локация → выбрать место)
        • Отправьте место с адресом (Venue)
        
        🔍 Используй кнопки для навигации
        ⬅️ Назад к списку - вернуться к списку туалетов
        ${EmojiConstants.MAP} Кнопки карт - открыть туалет в Яндекс, Google, 2ГИС или Apple картах
        
        Команды:
        /start - начать работу
        /help - показать эту справку
        """.trimIndent()

    const val NO_TOILETS_FOUND = "Пока в твоём городе мы не нашли туалеты в базе. " +
            "Мы уже работаем над этим и добавим ближайшие точки в самое ближайшее время."
    const val TOILETS_FOUND = "Найдено {count} туалетов поблизости:"
    const val LOCATION_NOT_FOUND = "Геолокация не найдена. Поделись геолокацией заново:"

    const val TOILET_NOT_FOUND = "Туалет не найден"
    const val INVALID_TOILET_ID = "Ошибка: неверный ID туалета"
    const val INTERNAL_ERROR = "Произошла внутренняя ошибка. Попробуйте позже."
    const val SOMETHING_WENT_WRONG = "Упс, что-то пошло не так. Попробуй ещё раз."
    const val UNKNOWN_COMMAND = "Неизвестная команда. Напиши /help для помощи."
    const val UNKNOWN_MESSAGE = "Не понял, что ты имеешь в виду. Поделись геолокацией для поиска туалетов или используй /help для помощи."

    @Deprecated("Will be removed in later refactoring")
    val TOILET_DETAILS = """
        <b>{name}</b>
        ${EmojiConstants.PIN} {address}
        
        ${EmojiConstants.TAG} Тип: {placeType}
        {feeIcon} Оплата: {feeText}{paymentMethods}
        ${EmojiConstants.ACCESSIBILITY} Доступность: {accessibility}
        
        ${EmojiConstants.CLOCK} Время работы:
        {workTime}
        
        ${EmojiConstants.BUILDING} Здание: {buildingInfo}
        ${EmojiConstants.METRO} Метро: {subwayInfo}
        
        ${EmojiConstants.NOTE} Заметка: {accessNote}
        ${EmojiConstants.ROUTE} Маршрут: {directionGuide}
        
        ${EmojiConstants.MAP} Карта: {mapsLink}
        """.trimIndent()

    const val TOILET_DETAILS_COMPACT = """
        <b>{name}</b>
        
        {addressLine}
        
        <b>{tags}</b>
        
        {availabilityLine}
        <b>Время работы:</b>
        {workTimeLine}
        
        <b>Тип:</b> {placeType}
        
        {howToFindLine}{landmarkLine}
        """

    const val LOCATION_REQUEST = "Поделись геолокацией для поиска туалетов:"

    const val BACK_TO_LIST_MESSAGE = "Для поиска туалетов поделись геолокацией:"
}
