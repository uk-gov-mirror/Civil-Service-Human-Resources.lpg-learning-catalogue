package uk.gov.cslearning.catalogue.service.record.model;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.time.Instant;

@Setter
@Getter
public class Booking {
    private Integer id;

    private String learner;

    private String learnerEmail;

    private URI event;

    private BookingStatus status;

    private Instant bookingTime;

    private URI paymentDetails;

    public Booking() {
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", learner='" + learner + '\'' +
                ", learnerEmail='" + learnerEmail + '\'' +
                ", event=" + event +
                ", status='" + status + '\'' +
                ", bookingTime=" + bookingTime +
                ", paymentDetails=" + paymentDetails +
                '}';
    }
}
