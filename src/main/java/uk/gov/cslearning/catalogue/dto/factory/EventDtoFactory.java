package uk.gov.cslearning.catalogue.dto.factory;

import org.springframework.stereotype.Component;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.module.DateRange;
import uk.gov.cslearning.catalogue.domain.module.Event;
import uk.gov.cslearning.catalogue.domain.module.FaceToFaceModule;
import uk.gov.cslearning.catalogue.dto.EventDto;
import uk.gov.cslearning.catalogue.service.util.DateRangeComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EventDtoFactory {
    private final ModuleDtoFactory moduleDtoFactory;

    public EventDtoFactory(ModuleDtoFactory moduleDtoFactory) {
        this.moduleDtoFactory = moduleDtoFactory;
    }

    public EventDto create(Event event, FaceToFaceModule module, Course course) {
        EventDto eventDto = new EventDto();

        eventDto.setId(event.getId());
        eventDto.setModule(moduleDtoFactory.create(module, course));
        if (event.getVenue() != null) {
            eventDto.setLocation(event.getVenue().getLocation());
        }

        Optional.ofNullable(event.getDateRanges())
                .ifPresent(eventDateRanges ->
                        eventDto.setEventDate(getEventDatesFromDateRanges(event.getDateRanges())));

        return eventDto;
    }

    public String getEventDatesFromDateRanges(List<DateRange> dateRangesList) {
        List<DateRange> dateRanges = new ArrayList<>(dateRangesList);
        Collections.sort(dateRanges, new DateRangeComparator());
        return dateRanges
                .stream()
                .map(date -> date.getDate().toString())
                .collect(Collectors.joining(","));
    }

}
