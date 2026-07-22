package uk.gov.cslearning.catalogue.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.cslearning.catalogue.domain.LearningTagDto;
import uk.gov.cslearning.catalogue.service.CourseLearningTagService;

import javax.validation.Valid;

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

    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTagFromCourse(@PathVariable Long courseId, @PathVariable Long tagId) {
        log.debug("Removing learning tag {} from course {}", tagId, courseId);
        courseLearningTagService.removeTagFromCourse(courseId, tagId);
    }
}
