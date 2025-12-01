package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.dto.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.util.DistanceFormat;
import by.yayauheny.tutochkatgbot.util.Links;
import by.yayauheny.tutochkatgbot.util.Text;
import by.yayauheny.tutochkatgbot.util.WorkTimeFormatter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class FormatterService {

    public String toiletDetails(RestroomResponseDto toilet) {
        String name = Optional.ofNullable(toilet.getName()).orElse("Туалет");
        String address = Optional.ofNullable(toilet.getAddress()).orElse("Адрес не указан");
        
        String description = Optional.ofNullable(toilet.getDescription())
            .filter(desc -> !desc.trim().isEmpty())
            .orElseGet(() -> "Описание не указано");
        
        return Text.substitute(Messages.TOILET_DETAILS, Map.of(
            "name", name,
            "address", address,
            "description", description
        ));
    }

    public String toiletListItem(NearestRestroomResponseDto toilet) {
        String name = Optional.ofNullable(toilet.getName()).orElse("Туалет");
        String distance = DistanceFormat.meters(toilet.getDistanceMeters());
        String feeType = toilet.getFeeType() == by.yayauheny.shared.enums.FeeType.FREE ? "Бесплатный" : "Платный";
        
        return String.format("🐥 %s (%s) — %s", name, feeType, distance);
    }

    public String toiletsFound(int count) {
        return Text.replace(Messages.TOILETS_FOUND, "count", String.valueOf(count));
    }

    public String generateMapsLink(RestroomResponseDto toilet) {
        return Links.getDefaultMapsLink(toilet.getCoordinates().getLat(), toilet.getCoordinates().getLon());
    }

    public String generateMapsLink(NearestRestroomResponseDto toilet) {
        return Links.getDefaultMapsLink(toilet.getCoordinates().getLat(), toilet.getCoordinates().getLon());
    }
}