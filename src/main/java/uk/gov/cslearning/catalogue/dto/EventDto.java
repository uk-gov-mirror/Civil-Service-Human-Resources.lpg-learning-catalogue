package uk.gov.cslearning.catalogue.dto;

import lombok.Data;
import uk.gov.cslearning.catalogue.domain.module.DateRange;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventDto {
    private String id;
    private ModuleDto module;
    private List<DateRange> dateRanges = new ArrayList<>();
    private String location;
    private String eventDate;
}
