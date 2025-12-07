package by.yayauheny.tutochkatgbot.messages;

public class Messages {
    
    public static final String BUTTON_SHARE_LOCATION = "📍 Поделиться геолокацией";
    public static final String BUTTON_BACK_TO_LIST = "⬅️ Назад к списку";
    public static final String BUTTON_OPEN_MAPS = "🗺 Открыть в картах";
    public static final String BUTTON_SEARCH_AGAIN = "🔍 Поиск заново";
    public static final String BUTTON_HELP = "❓ Помощь";

    @Deprecated
    public static final String CALLBACK_TOILET_DETAIL = "toilet_detail_";
    @Deprecated
    public static final String CALLBACK_BACK_TO_LIST = "back_to_list";
    @Deprecated
    public static final String CALLBACK_RADIUS_SELECT = "radius_select_";

    public static final String RADIUS_500M = "500м";
    public static final String RADIUS_1KM = "1км";
    public static final String RADIUS_2KM = "2км";
    public static final String RADIUS_5KM = "5км";

    public static final String WELCOME_MESSAGE = "Привет! Поделись геолокацией для поиска туалетов.";
    public static final String HELP_MESSAGE = """
        🤖 Бот для поиска туалетов
        
        📍 Способы поиска:
        • Отправьте текущую геолокацию
        • Выберите точку на карте (скрепка → Локация → выбрать место)
        • Отправьте место с адресом (Venue)
        
        🔍 Используй кнопки для навигации
        ⬅️ Назад к списку - вернуться к списку туалетов
        🗺 Открыть в картах - показать туалет на карте
        
        Команды:
        /start - начать работу
        /help - показать эту справку
        """;

    public static final String NO_TOILETS_FOUND = "К сожалению, поблизости туалетов не найдено. " +
            "Попробуйте увеличить радиус поиска или отправьте точку на карте (скрепка → Локация → выбрать место) " +
            "или место с адресом.";
    public static final String TOILETS_FOUND = "Найдено {count} туалетов поблизости:";
    public static final String LOCATION_NOT_FOUND = "Геолокация не найдена. Поделись геолокацией заново:";

    public static final String TOILET_NOT_FOUND = "Туалет не найден";
    public static final String INVALID_TOILET_ID = "Ошибка: неверный ID туалета";
    public static final String INTERNAL_ERROR = "Произошла внутренняя ошибка. Попробуйте позже.";
    public static final String SOMETHING_WENT_WRONG = "Упс, что-то пошло не так. Попробуй ещё раз.";

    public static final String TOILET_DETAILS = """
        <b>{name}</b>
        📍 {address}
        
        🏷 Тип: {placeType}
        {feeIcon} Оплата: {feeText}
        ♿ Доступность: {accessibility}
        
        ⏰ Время работы:
        {workTime}
        
        🏢 Здание: {buildingInfo}
        🚇 Метро: {subwayInfo}
        
        📝 Заметка: {accessNote}
        🧭 Маршрут: {directionGuide}
        
        🗺 Карта: {mapsLink}
        """;

    public static final String LOCATION_REQUEST = "Поделись геолокацией для поиска туалетов:";

    public static final String BACK_TO_LIST_MESSAGE = "Для поиска туалетов поделись геолокацией:";
}