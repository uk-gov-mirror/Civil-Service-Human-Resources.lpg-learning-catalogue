package uk.gov.cslearning.catalogue.api.models;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

public class SimplePage<T> extends PageImpl<T> {

    @JsonCreator
    public SimplePage(@JsonProperty("content") final List<T> content,
                      @JsonProperty("totalElements") final long totalElements,
                      @JsonProperty("totalPages") final int totalPages,
                      @JsonProperty("page") final int page,
                      @JsonProperty("size") final int size,
                      @JsonProperty("sort") final List<String> sort) {

        super(content, PageRequest.of(page, size, Sort.by(sort.stream()
                .map(el -> el.split(","))
                .map(ar -> new Sort.Order(Sort.Direction.fromString(ar[1]), ar[0]))
                .collect(Collectors.toList()))), totalElements);
    }

    public SimplePage(final List<T> content, final long totalElements, final Pageable pageable) {
        super(content, pageable, totalElements);
    }

    public int getPage() {
        return getNumber();
    }

    public long getTotalElements() {
        return super.getTotalElements();
    }

    public long getTotalResults() {
        return getTotalElements();
    }

    @JsonProperty("sort")
    public List<String> getSortList() {
        return getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name())
                .collect(Collectors.toList());
    }
}
