package by.yayauheny.tutochkatgbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "bot.admin")
public record AdminProperties(List<String> ids) {

    public Set<Long> asLongSet() {
        if (ids == null) {
            return Set.of();
        }

        return ids.stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .flatMap(value -> parseLongSafely(value).stream())
            .collect(Collectors.toUnmodifiableSet());
    }

    private static java.util.Optional<Long> parseLongSafely(String value) {
        try {
            return java.util.Optional.of(Long.parseLong(value));
        } catch (NumberFormatException ex) {
            return java.util.Optional.empty();
        }
    }
}
