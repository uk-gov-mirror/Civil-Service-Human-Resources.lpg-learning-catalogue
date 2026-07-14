package uk.gov.cslearning.catalogue.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Data
@Configuration
public class ClockConfig {

    @Value("${time.zoneId}")
    private String zoneId;

    @Bean
    public Clock getClock() {
        if (StringUtils.isBlank(zoneId)) {
            return Clock.systemDefaultZone();
        }
        return Clock.system(ZoneId.of(zoneId.trim()));
    }
}
