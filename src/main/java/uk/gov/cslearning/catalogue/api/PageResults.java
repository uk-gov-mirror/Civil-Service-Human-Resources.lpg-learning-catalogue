package uk.gov.cslearning.catalogue.api;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class PageResults<T> {

    private List<T> results;

    private Integer page;

    private Long totalResults;

    private Integer size;

    public PageResults(Page<T> page, Pageable pageable) {
        this.results = page.getContent();
        this.page = pageable.getPageNumber();
        this.size = pageable.getPageSize();
        this.totalResults = page.getTotalElements();
    }

}
