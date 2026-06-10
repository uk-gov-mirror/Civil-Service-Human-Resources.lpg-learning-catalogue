package uk.gov.cslearning.catalogue.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.cslearning.catalogue.domain.module.Audience;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.service.AudienceService;
import uk.gov.cslearning.catalogue.service.CourseService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/courses/{courseId}/audiences")
@Slf4j
public class AudienceController {

    private final CourseRepository courseRepository;
    private final CourseService courseService;
    private final AudienceService audienceService;

    public AudienceController(CourseRepository courseRepository, CourseService courseService, AudienceService audienceService) {
        this.courseRepository = courseRepository;
        this.courseService = courseService;
        this.audienceService = audienceService;
    }

    @PostMapping
    @PreAuthorize("(hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_CREATE, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity<Void> createAudience(@PathVariable String courseId, @RequestBody Audience audience, UriComponentsBuilder builder, Authentication authentication) {
        log.debug("Adding audience to course with ID {}", courseId);

        if (!audienceService.isPermitted(courseId, authentication)) {
            return ResponseEntity.status(403).build();
        }

        audience = audienceService.setDefaults(authentication, audience);

        audienceService.save(courseId, audience);

        log.info("Saved audience {}", audience.toString());

        return ResponseEntity.created(builder.path("/courses/{courseId}/audiences/{audienceId}").build(courseId, audience.getId())).build();
    }

    @GetMapping("/{audienceId}")
    public ResponseEntity<Audience> getAudience(@PathVariable String courseId, @PathVariable String audienceId) {
        log.debug("Getting audience {} of course {}", audienceId, courseId);

        Optional<Audience> result = audienceService.find(courseId, audienceId);

        return result.map(audience -> new ResponseEntity<>(audience, OK))
                .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @PutMapping("/{audienceId}")
    @PreAuthorize("(hasPermission(#courseId, 'write') and hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_EDIT, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity updateAudience(@PathVariable String courseId, @PathVariable String audienceId, @RequestBody Audience newAudience) {
        log.debug("Updating audience {} in course {}", audienceId, courseId);

        return courseService.findById(courseId)
                .map(course -> audienceService.find(course.getId(), audienceId)
                        .map(audience -> {
                            audienceService.updateAudience(course, newAudience, audience);
                            return new ResponseEntity<>(NO_CONTENT);
                        }).orElseGet(() -> new ResponseEntity<>(BAD_REQUEST)))
                .orElseGet(() -> new ResponseEntity<>(BAD_REQUEST));
    }

    @DeleteMapping("/{audienceId}")
    @PreAuthorize("(hasPermission(#courseId, 'write') and hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_DELETE, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity deleteAudience(@PathVariable String courseId, @PathVariable String audienceId, Authentication authentication) {
        log.debug("Deleting audience, course ID {}, audience ID {}", courseId, audienceId);

        courseRepository.findById(courseId)
                .map(course -> audienceService.find(course, audienceId)
                        .map(audience -> {
                            if (!audienceService.isPermitted(courseId, authentication)) {
                                return ResponseEntity.badRequest().build();
                            }
                            course.deleteAudience(audience);
                            return courseRepository.save(course);
                        })
                        .orElseThrow(ResourceNotFoundException::resourceNotFoundException)
                )
                .orElseThrow(ResourceNotFoundException::resourceNotFoundException);

        return ResponseEntity.noContent().build();
    }

}
