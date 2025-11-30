package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.integration.dto.NearestRestroomResponseDto;
import by.yayauheny.tutochkatgbot.integration.dto.RestroomResponseDto;
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
        String name = Optional.ofNullable(toilet.name()).orElse("Туалет");
        String address = Optional.ofNullable(toilet.address()).orElse("Адрес не указан");
        
        String description = Optional.ofNullable(toilet.description())
            .filter(desc -> !desc.trim().isEmpty())
            .orElseGet(() -> {
                if (toilet.workTime() != null && !toilet.workTime().isEmpty()) {
                    String workTime = WorkTimeFormatter.formatWorkTime(toilet.workTime());
                    if (!"Время работы не указано".equals(workTime)) {
                        return workTime;
                    }
                }
                return "Описание не указано";
            });
        
        return Text.substitute(Messages.TOILET_DETAILS, Map.of(
            "name", name,
            "address", address,
            "description", description
        ));
    }

    public String toiletListItem(NearestRestroomResponseDto toilet) {
        String name = Optional.ofNullable(toilet.name()).orElse("Туалет");
        String distance = DistanceFormat.meters(toilet.distanceMeters());
        String feeType = toilet.feeType() == by.yayauheny.tutochkatgbot.integration.dto.FeeType.FREE ? "Бесплатный" : "Платный";
        
        String workTimeInfo = "";
        if (toilet.workTime() != null && !toilet.workTime().isEmpty()) {
            String workTime = WorkTimeFormatter.getSimplifiedWorkTime(toilet.workTime());
            if (workTime != null && !workTime.trim().isEmpty()) {
                workTimeInfo = " • " + workTime;
            }
        }
        
        return String.format("🐥 %s (%s) — %s%s", name, feeType, distance, workTimeInfo);
    }

    public String toiletsFound(int count) {
        return Text.replace(Messages.TOILETS_FOUND, "count", String.valueOf(count));
    }

    public String generateMapsLink(RestroomResponseDto toilet) {
        return Links.getDefaultMapsLink(toilet.lat(), toilet.lon());
    }

    public String generateMapsLink(NearestRestroomResponseDto toilet) {
        return Links.getDefaultMapsLink(toilet.lat(), toilet.lon());
    }
}