package uk.gov.cslearning.catalogue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cslearning.catalogue.api.OwnerParameters;
import uk.gov.cslearning.catalogue.api.SearchResults;
import uk.gov.cslearning.catalogue.api.v2.model.CourseSearchParameters;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Status;

import java.util.Collection;
import java.util.List;

@Repository
public interface CourseRepository extends ElasticsearchRepository<Course, String>, CourseSearchRepository, CourseSuggestionsRepository {

    @Query("{\n" +
            "    \"bool\": {\n" +
            "        \"must\": [\n" +
            "            {\n" +
            "                \"match\": {\n" +
            "                    \"status\": \"PUBLISHED\"\n" +
            "                }\n" +
            "            },\n" +
            "            {\n" +
            "                \"nested\": {\n" +
            "                    \"path\": \"audiences\",\n" +
            "                    \"query\": {\n" +
            "                        \"bool\": {\n" +
            "                            \"must\": [\n" +
            "                                {\n" +
            "                                    \"match\": {\n" +
            "                                        \"audiences.type\": \"REQUIRED_LEARNING\"\n" +
            "                                    }\n" +
            "                                }\n" +
            "                            ]\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}")
    List<Course> findAllPublishedRequiredLearning(Pageable pageable);

    @Query("{\n" +
            "        \"bool\": {\n" +
            "            \"must\": [\n" +
            "                {\n" +
            "                    \"match\": {\n" +
            "                        \"status\": \"?1\"\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"nested\": {\n" +
            "                        \"path\": \"audiences\",\n" +
            "                        \"query\": {\n" +
            "                            \"bool\": {\n" +
            "                                \"must\": [\n" +
            "                                    {\n" +
            "                                        \"match\": {\n" +
            "                                            \"audiences.type\": \"REQUIRED_LEARNING\"\n" +
            "                                        }\n" +
            "                                    },\n" +
            "                                    {\n" +
            "                                        \"match\": {\n" +
            "                                            \"audiences.departments\": \"[?0]\"\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }")
    List<Course> findMandatoryOfMultipleDepts(List<String> department, String status, Pageable pageable);

    SearchResults search(Pageable pageable, CourseSearchParameters courseSearchParameters, OwnerParameters ownerParameters);

    Page<Course> findAllByStatusIn(Collection<Status> status, Pageable pageable);

    Page<Course> findAllByIdIn(Collection<String> courseIds, Pageable pageable);

    Page<Course> findAllByOrganisationCode(String organisationalUnitCode, Pageable pageable);

    Page<Course> findAllByProfessionId(String professionId, Pageable pageable);

    Page<Course> findAllBySupplier(String supplier, Pageable pageable);

    void deleteCourseById(String id);
}
