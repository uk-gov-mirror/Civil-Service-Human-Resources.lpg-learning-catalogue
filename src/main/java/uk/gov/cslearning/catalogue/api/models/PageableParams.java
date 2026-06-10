package uk.gov.cslearning.catalogue.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.Max;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageableParams {
    int page = 0;

    @Max(200)
    int size = 20;

    public Pageable getAsPageable() {
        return PageRequest.of(page, size);
    }
}
