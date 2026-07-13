package uk.gov.cslearning.catalogue.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.cslearning.catalogue.Utils;
import uk.gov.cslearning.catalogue.domain.CivilServant.CivilServant;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.Status;
import uk.gov.cslearning.catalogue.exception.CourseCannotByDeletedException;
import uk.gov.cslearning.catalogue.mapping.DaysMapper;
import uk.gov.cslearning.catalogue.service.CourseService;
import uk.gov.cslearning.catalogue.service.RegistryService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/courses")
@Slf4j
public class CourseController {
    private static final String COURSE_STATUS = "Published";


    private final CourseService courseService;

    private final RegistryService registryService;

    @Autowired
    public CourseController(CourseService courseService, RegistryService registryService) {
        this.courseService = courseService;
        this.registryService = registryService;
    }

    @PostMapping
    @PreAuthorize("(hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_CREATE, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity<Void> create(@RequestBody Course course, UriComponentsBuilder builder, Authentication authentication) {
        log.debug("Creating course {}", course);

        Course newCourse = courseService.createCourse(course, authentication);

        return ResponseEntity.created(builder.path("/courses/{courseId}").build(newCourse.getId())).build();
    }

    @GetMapping
    public ResponseEntity<PageResults<Course>> list(@RequestParam(name = "status", defaultValue = COURSE_STATUS) String status,
                                                    Pageable pageable) {
        Page<Course> results = courseService.findAllByStatusIn(
                Arrays.stream(status.split(",")).map(Status::forValue).collect(Collectors.toList()), pageable);
        return ResponseEntity.ok(new PageResults<>(results, pageable));
    }

    @GetMapping(params = {"mandatory", "days"})
    public ResponseEntity<Map<String, List<Course>>> listMandatoryByDueDays(@RequestParam(value = "days", defaultValue = "1") String days) {
        log.debug("Listing mandatory courses");
        List<Course> courses = courseService.fetchMandatoryCoursesByDueDate(DaysMapper.convertDaysFromTextToNumeric(days));

        return ResponseEntity.ok(courseService.groupByOrganisationCode(courses));
    }

    @GetMapping(value = "/management")
    public ResponseEntity<PageResults<Course>> listForOrganisation(Authentication authentication,
                                                                   Pageable pageable) {

        CivilServant civilServant = registryService.getCurrentCivilServant();
        ResponseEntity<PageResults<Course>> response = new ResponseEntity<>(new PageResults<>(Page.empty(), pageable), OK);

        if (Utils.hasRoles(new String[]{"CSL_AUTHOR", "LEARNING_MANAGER"})) {
            Page<Course> results = courseService.findAllCourses(pageable);
            response = new ResponseEntity<>(new PageResults<>(results, pageable), OK);
        } else if (Utils.hasRole("ORGANISATION_AUTHOR")) {
            Optional<String> orgCodeOpt = civilServant.getOrganisationalUnitCode();
            if (orgCodeOpt.isPresent()) {
                Page<Course> results = courseService.findCoursesByOrganisationalUnit(orgCodeOpt.get(), pageable);
                response = new ResponseEntity<>(new PageResults<>(results, pageable), OK);
            }
        } else if (Utils.hasRoles(new String[]{"KPMG_SUPPLIER_AUTHOR"})) {
            Page<Course> results = courseService.findCoursesBySupplier(authentication, pageable);
            response = new ResponseEntity<>(new PageResults<>(results, pageable), OK);
        } else {
            response = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return response;
    }

    @GetMapping(params = "courseId")
    public ResponseEntity<Iterable<Course>> get(@RequestParam("courseId") List<String> courseIds) {
        log.debug("Getting courses with IDs {}", courseIds);
        Iterable<Course> result = courseService.findAllById(courseIds);
        return new ResponseEntity<>(result, OK);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Course> get(@PathVariable String courseId,
                                      @RequestParam(value = "includeAvailability", required = false, defaultValue = "false") boolean includeAvailability) {
        log.debug("Getting course with ID {}", courseId);

        Optional<Course> result = courseService.findById(courseId, includeAvailability);

        return result
                .map(course -> new ResponseEntity<>(course, OK))
                .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @PutMapping(path = "/{courseId}")
    public ResponseEntity<Void> update(@PathVariable String courseId, @RequestBody Course newCourse) {
        log.debug("Updating course {}", newCourse);
        courseService.updateCourse(courseId, newCourse);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping(path = "/{courseId}")
    @PreAuthorize("(hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_DELETE, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity<Void> delete(@PathVariable String courseId) {
        try {
            log.info("Deleting course with ID {} ", courseId);
            courseService.deleteCourseById(courseId);
            log.info("Course with ID {} deleted successfully.", courseId);
            return ResponseEntity.ok(null);
        } catch (CourseCannotByDeletedException e) {
            log.error("Exception thrown while trying to delete course with ID {}: {}", courseId, e.getMessage());
            return ResponseEntity.status(CONFLICT).build();
        }

    }

}
