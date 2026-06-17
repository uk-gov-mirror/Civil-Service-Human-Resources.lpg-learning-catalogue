package uk.gov.cslearning.catalogue.service.util;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UtilService implements IUtilService {

    private final Clock clock;

    @Override
    public LocalDateTime getNowDateTime() {
        return LocalDateTime.now(clock);
    }

    @Override
    public String generateUrlSlugFromString(String string) {
        return string
                .toLowerCase()
                .replaceAll("'", "")
                .replaceAll("&", "and")
                .replaceAll(" ", "-");
    }
}
