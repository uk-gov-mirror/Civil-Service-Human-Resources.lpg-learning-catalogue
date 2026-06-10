package uk.gov.cslearning.catalogue.domain.module;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.elasticsearch.annotations.Field;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.data.elasticsearch.annotations.FieldType.Date;

@Setter
@Getter
public class DateRange {

    @NotNull
    @Field(type = Date, format = {}, pattern = "uuuu-MM-dd")
    private LocalDate date;

    @NotNull
    @Field(type = Date, format = {}, pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull
    @Field(type = Date, format = {}, pattern = "HH:mm")
    private LocalTime endTime;

    public DateRange() {
    }

    public DateRange(@NotNull LocalDate date, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DateRange dateRange = (DateRange) o;

        return new EqualsBuilder()
                .append(date, dateRange.date)
                .append(startTime, dateRange.startTime)
                .append(endTime, dateRange.endTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(date)
                .append(startTime)
                .append(endTime)
                .toHashCode();
    }
}
