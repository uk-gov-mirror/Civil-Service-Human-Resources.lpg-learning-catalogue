package uk.gov.cslearning.catalogue.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.cslearning.catalogue.domain.Course;
import uk.gov.cslearning.catalogue.domain.module.Event;
import uk.gov.cslearning.catalogue.domain.module.FaceToFaceModule;
import uk.gov.cslearning.catalogue.domain.module.Module;
import uk.gov.cslearning.catalogue.exception.ResourceNotFoundException;
import uk.gov.cslearning.catalogue.repository.elastic.CourseRepository;
import uk.gov.cslearning.catalogue.service.EventService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/courses/{courseId}/modules/{moduleId}/events")
@Slf4j
public class EventController {

    private final EventService eventService;
    private final CourseRepository courseRepository;

    public EventController(EventService eventService, CourseRepository courseRepository) {
        this.eventService = eventService;
        this.courseRepository = courseRepository;
    }

    @PostMapping
    @PreAuthorize("(hasPermission(#courseId, 'write') and hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_CREATE, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity createEvent(@PathVariable String courseId, @PathVariable String moduleId, @RequestBody Event event, UriComponentsBuilder builder) {
        log.debug("Adding event to module with ID {}", moduleId);

        Event saved = eventService.save(courseId, moduleId, event);

        log.info("Saved event {}", saved);

        return ResponseEntity.created(builder.path("/courses/{courseId}/modules/{moduleId}/events/{eventId}").build(courseId, moduleId, saved.getId())).build();
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEvent(@PathVariable String courseId, @PathVariable String moduleId, @PathVariable String eventId) {
        log.debug("Getting event {} of module {} of course {}", eventId, moduleId, courseId);

        Optional<Event> result = eventService.find(courseId, moduleId, eventId);

        return result.map(event -> new ResponseEntity<>(event, OK)).orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("(hasPermission(#courseId, 'write') and hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_EDIT, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity<Event> updateEvent(@PathVariable String courseId, @PathVariable String moduleId, @PathVariable String eventId, @RequestBody Event newEvent) {
        log.debug("Updating event with ID {}", eventId);

        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.badRequest().build();
        }
        if (courseRepository.findById(courseId).get().getModuleById(moduleId) == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Course> result = courseRepository.findById(courseId);

        return result.map(course -> {
            Module module = course.getModuleById(moduleId)
                    .orElseThrow(ResourceNotFoundException::new);

            if (module instanceof FaceToFaceModule) {
                FaceToFaceModule faceToFaceModule = (FaceToFaceModule) module;

                Event event = faceToFaceModule.getEventById(eventId);
                event.setStatus(newEvent.getStatus());
                event.setCancellationReason(newEvent.getCancellationReason());
                event.setDateRanges(newEvent.getDateRanges());
                event.setJoiningInstructions(newEvent.getJoiningInstructions());

                Optional.ofNullable(newEvent.getVenue()).ifPresent(event::setVenue);

                courseRepository.save(course);

                return ResponseEntity.ok().body(event);
            }

            return ResponseEntity.badRequest().body(newEvent);
        }).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("(hasPermission(#courseId, 'write') and hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_DELETE, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity deleteEvent(@PathVariable String courseId, @PathVariable String moduleId, @PathVariable String eventId) {
        log.debug("Deleting event with id {}", eventId);
        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.badRequest().build();
        }
        if (courseRepository.findById(courseId).get().getModuleById(moduleId) == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Course> result = courseRepository.findById(courseId);

        return result.map(course -> {
            Module module = course.getModuleById(moduleId).orElseThrow(ResourceNotFoundException::new);

            if (module instanceof FaceToFaceModule) {
                FaceToFaceModule faceToFaceModule = (FaceToFaceModule) module;

                Event event = faceToFaceModule.getEventById(eventId);

                faceToFaceModule.removeEvent(event);

                courseRepository.save(course);

                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.badRequest().build();
        }).orElseGet(() -> ResponseEntity.badRequest().build());
    }

}
