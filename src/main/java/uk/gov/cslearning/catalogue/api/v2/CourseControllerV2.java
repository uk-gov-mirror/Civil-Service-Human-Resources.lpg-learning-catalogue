package uk.gov.cslearning.catalogue.api.v2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.cslearning.catalogue.api.PageResults;
import uk.gov.cslearning.catalogue.api.SearchResults;
import uk.gov.cslearning.catalogue.api.v2.model.CourseSearchParameters;
import uk.gov.cslearning.catalogue.api.v2.model.GetCoursesParameters;
import uk.gov.cslearning.catalogue.api.v2.model.RequiredLearningIdMap;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.repository.CourseRepository;
import uk.gov.cslearning.catalogue.service.CourseService;

@RestController
@RequestMapping("/v2/courses")
public class CourseControllerV2 {

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

    @GetMapping("/required-learning-map")
    @ResponseBody
    public RequiredLearningIdMap getRequiredLearningForDepartments() {
        return courseService.getDepartmentCodeToCourseIdRequiredLearningMap();
    }

    @PostMapping("/search")
    @ResponseBody
    public SearchResults searchCourses(@RequestBody CourseSearchParameters params, Pageable pageable) {
        return courseService.search(params, pageable);
    }
}
