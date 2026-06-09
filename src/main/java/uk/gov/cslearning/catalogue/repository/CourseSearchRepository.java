package uk.gov.cslearning.catalogue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.gov.cslearning.catalogue.api.OwnerParameters;
import uk.gov.cslearning.catalogue.api.SearchResults;
import uk.gov.cslearning.catalogue.api.v2.model.CourseSearchParameters;
import uk.gov.cslearning.catalogue.domain.Course;

public interface CourseSearchRepository {
    SearchResults search(Pageable pageable, CourseSearchParameters courseSearchParameters, String sortField, Sort.Direction sortDirection);

    SearchResults search(Pageable pageable, CourseSearchParameters courseSearchParameters);

    SearchResults search(Pageable pageable, CourseSearchParameters courseSearchParameters, OwnerParameters ownerParameters);

    Page<Course> findAllByOrganisationCode(String organisationalUnitCode, Pageable pageable);

    Page<Course> findAllBySupplier(String supplier, Pageable pageable);
}
