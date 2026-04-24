package uk.gov.cslearning.catalogue.api.v2;

import org.elasticsearch.common.collect.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.cslearning.catalogue.api.PageResults;
import uk.gov.cslearning.catalogue.api.SearchResults;
import uk.gov.cslearning.catalogue.api.v2.model.CourseIdAudienceAttributeMap;
import uk.gov.cslearning.catalogue.api.v2.model.CourseSearchParameters;
import uk.gov.cslearning.catalogue.api.v2.model.GetCoursesParameters;
import uk.gov.cslearning.catalogue.api.v2.model.RequiredLearningIdMap;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.repository.CourseRepository;
import uk.gov.cslearning.catalogue.service.CourseService;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;

@RestController
@RequestMapping("/v2/courses")
public class CourseControllerV2 {

    private String[] SORTABLE_FIELDS = {
            "title"
    };

    private final CourseRepository courseRepository;
    private final CourseService courseService;

    public CourseControllerV2(CourseRepository courseRepository, CourseService courseService) {
        this.courseRepository = courseRepository;
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<PageResults<Course>> list(GetCoursesParameters parameters, Pageable pageable) {
        Page<Course> results = courseRepository.findSuggested(parameters, pageable);
        return ResponseEntity.ok(new PageResults<>(results, pageable));
    }

    @GetMapping("/audience-attribute-map")
    @ResponseBody
    public CourseIdAudienceAttributeMap getCourseIdAudienceAttributeMap() {
        return courseService.getCourseIdAudienceAttributeMap();
    }


    @GetMapping("/required-learning-map")
    @ResponseBody
    public RequiredLearningIdMap getRequiredLearningForDepartments() {
        return courseService.getDepartmentCodeToCourseIdRequiredLearningMap();
    }

    @PostMapping("/search")
    @ResponseBody
    public SearchResults searchCourses(@RequestBody CourseSearchParameters params,
                                       @RequestParam(value = "sort.field", required = false) String field,
                                       @RequestParam(value = "sort.direction", required = false) Sort.Direction direction,
                                       Pageable pageable) {
        if (field != null && !Arrays.asList(SORTABLE_FIELDS).contains(field)) {
            throw new ConstraintViolationException(String.format("'%s' is not a valid sortable field, valid fields are: %s", field, Arrays.toString(SORTABLE_FIELDS)), Set.of());
        }
        return courseService.search(params, pageable, field, direction);
    }
}
