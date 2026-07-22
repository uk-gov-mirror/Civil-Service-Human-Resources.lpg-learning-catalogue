package uk.gov.cslearning.catalogue.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.cslearning.catalogue.domain.LearningTagDto;
import uk.gov.cslearning.catalogue.service.CourseLearningTagService;

@Slf4j
@RestController
@RequestMapping("/courses/{courseId}/learning-tags")
public class CourseLearningTagController {

    private final CourseLearningTagService courseLearningTagService;

    public CourseLearningTagController(CourseLearningTagService courseLearningTagService) {
        this.courseLearningTagService = courseLearningTagService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LearningTagDto addTagToCourse(@PathVariable Long courseId, @RequestBody LearningTagDto learningTagDto) {
        log.debug("Adding learning tag {} to course {}", learningTagDto.getId(), courseId);
        return courseLearningTagService.addTagToCourse(courseId, learningTagDto);
    }

    @DeleteMapping("/{learningTagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTagFromCourse(@PathVariable Long courseId, @PathVariable Long learningTagId) {
        log.debug("Removing learning tag {} from course {}", learningTagId, courseId);
        courseLearningTagService.removeTagFromCourse(courseId, learningTagId);
    }
}
