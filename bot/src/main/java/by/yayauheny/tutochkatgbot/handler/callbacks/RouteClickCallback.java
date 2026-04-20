package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.handler.CallbackHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.metrics.BotMetricLabelWhitelist;
import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import by.yayauheny.tutochkatgbot.service.SearchService;
import by.yayauheny.tutochkatgbot.util.Links;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Order(2)
public class RouteClickCallback implements CallbackHandler {
    private final MessageSender sender;
    private final SearchService searchService;
    private final BotMetrics botMetrics;

    public RouteClickCallback(MessageSender sender, SearchService searchService, BotMetrics botMetrics) {
        this.sender = sender;
        this.searchService = searchService;
        this.botMetrics = botMetrics;
    }

    @Override
    public String prefix() {
        return "route";
    }

    @Override
    public boolean canHandle(String callbackData) {
        return CallbackData.isType(callbackData, "route");
    }

    @Override
    public void handle(Update update, UpdateContext ctx) {
        String provider = BotMetricLabelWhitelist.normalizeProvider(CallbackData.routeProvider(ctx.callbackData()));
        botMetrics.incrementRouteClick("telegram_bot", provider);

        if ("unknown".equals(provider)) {
            sender.safeReply(ctx, "Не удалось открыть маршрут, попробуйте снова.");
            return;
        }

        String restroomId = CallbackData.routeRestroomId(ctx.callbackData());
        var restroom = searchService.getById(restroomId);
        if (restroom.isEmpty()) {
            sender.safeReply(ctx, "Не удалось открыть маршрут, попробуйте снова.");
            return;
        }

        double lat = restroom.get().coordinates().lat();
        double lon = restroom.get().coordinates().lon();
        String link = buildProviderLink(provider, lat, lon, restroom.get().externalMaps());
        sender.sendText(ctx.chatId(), "Маршрут: " + link);
    }

    private String buildProviderLink(String provider, double lat, double lon, Map<String, Object> externalMaps) {
        return switch (provider) {
            case "google" -> Links.googleMaps(lat, lon);
            case "2gis" -> extractTwoGisBranchId(externalMaps)
                .map(Links::twoGisById)
                .orElseGet(() -> Links.twoGis(lat, lon));
            case "apple" -> Links.appleMaps(lat, lon);
            case "yandex" -> Links.yandexMaps(lat, lon);
            default -> throw new IllegalStateException("Unexpected provider after normalization: " + provider);
        };
    }

    private Optional<String> extractTwoGisBranchId(Map<String, Object> externalMaps) {
        if (externalMaps == null) {
            return Optional.empty();
        }

        Object twoGis = externalMaps.get("2gis");
        if (!(twoGis instanceof Map<?, ?> twoGisMap)) {
            return Optional.empty();
        }

        Object branchId = twoGisMap.get("branch_id");
        return (branchId instanceof String s && !s.isBlank())
            ? Optional.of(s)
            : Optional.empty();
    }
}
