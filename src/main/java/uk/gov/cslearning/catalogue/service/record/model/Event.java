package uk.gov.cslearning.catalogue.service.record.model;

import lombok.Data;

@Data
public class Event {
    private Integer id;

    private String uid;

    private String path;

    private String status;

    private String cancellationReason;

    private Integer activeBookingCount;

    public Event() {
    }
}

