package uk.gov.cslearning.catalogue.api;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Setter
@Getter
public class PageParameters {

    private Integer page;

    private Integer size;

    public Pageable getPageRequest() {
        if (page != null && size != null) {
            return PageRequest.of(page, size);
        }
        return PageRequest.of(0, 10);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("page", page)
                .append("size", size)
                .toString();
    }
}
