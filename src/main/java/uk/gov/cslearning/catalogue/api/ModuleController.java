package uk.gov.cslearning.catalogue.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.cslearning.catalogue.domain.module.Module;
import uk.gov.cslearning.catalogue.service.CourseService;
import uk.gov.cslearning.catalogue.service.ModuleService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/courses/{courseId}/modules")
@Slf4j
public class ModuleController {

    private final CourseService courseService;
    private final ModuleService moduleService;

    public ModuleController(CourseService courseService, ModuleService moduleService) {
        this.courseService = courseService;
        this.moduleService = moduleService;
    }

    @PutMapping
    @PreAuthorize("(hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_EDIT, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity<Void> updateCourseModules(@PathVariable String courseId, @RequestBody List<Module> modules) {
        courseService.updateCourseModules(courseId, modules);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("(hasPermission(#courseId, 'write') and hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_CREATE, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity<Void> createModule(@PathVariable String courseId, @RequestBody Module module, UriComponentsBuilder builder) {
        log.info("Adding module to course with ID {}", courseId);

        Module saved = moduleService.save(courseId, module);

        log.info("Saved module {}", saved);

        return ResponseEntity.created(builder.path("/courses/{courseId}/modules/{moduleId}").build(courseId, saved.getId())).build();
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<Module> getModule(@PathVariable String courseId, @PathVariable String moduleId) {
        log.debug("Getting module {} of course {}", moduleId, courseId);

        Optional<Module> result = moduleService.find(courseId, moduleId);

        return result.map(module -> new ResponseEntity<>(module, OK))
                .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @DeleteMapping("/{moduleId}")
    @PreAuthorize("(hasPermission(#courseId, 'write') and hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_DELETE, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity deleteModule(@PathVariable String courseId, @PathVariable String moduleId) {
        log.debug("Deleting module, course ID {}, module ID {}", courseId, moduleId);

        moduleService.deleteModule(courseId, moduleId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{moduleId}")
    @PreAuthorize("(hasPermission(#courseId, 'write') and hasAnyAuthority(T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_EDIT, T(uk.gov.cslearning.catalogue.domain.Roles).LEARNING_MANAGER, T(uk.gov.cslearning.catalogue.domain.Roles).CSL_AUTHOR))")
    public ResponseEntity updateModule(@PathVariable String courseId, @PathVariable String moduleId, @RequestBody Module module) {
        log.debug("Updating module {} in course {}", moduleId, courseId);

        Optional<Module> optionalModule = moduleService.find(courseId, moduleId);

        return optionalModule
                .map(m -> {
                    moduleService.updateModule(courseId, module);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> new ResponseEntity<>(BAD_REQUEST));
    }


}
