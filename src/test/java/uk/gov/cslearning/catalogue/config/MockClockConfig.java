package uk.gov.cslearning.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Configuration
public class MockClockConfig {

    @Bean
    @Primary
    public Clock getMockClock() {
        return MockClockConfig.getClock();
    }

    public static Clock getClock() {
        return Clock.fixed(Instant.parse("2025-01-01T10:00:00.000Z"), ZoneId.of("UTC"));
    }
}
