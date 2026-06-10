package uk.gov.cslearning.catalogue.domain.module;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class Venue {
    @NotNull
    private String location;

    private String address;

    private Integer capacity;

    private Integer minCapacity;

    private Integer availability;

    public Venue() {
    }

    public Venue(@NotNull String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Venue venue = (Venue) o;

        return new EqualsBuilder()
                .append(location, venue.location)
                .append(address, venue.address)
                .append(capacity, venue.capacity)
                .append(minCapacity, venue.minCapacity)
                .append(availability, venue.availability)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(location)
                .append(address)
                .append(capacity)
                .append(minCapacity)
                .append(availability)
                .toHashCode();
    }

}
