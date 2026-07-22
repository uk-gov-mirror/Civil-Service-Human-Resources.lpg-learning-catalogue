package uk.gov.cslearning.catalogue.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.cslearning.catalogue.domain.CourseLearningTagDto;
import uk.gov.cslearning.catalogue.domain.LearningTagDto;
import uk.gov.cslearning.catalogue.service.CourseLearningTagService;

@RestController
@RequestMapping("/courses/{courseId}/learning-tags")
public class CourseLearningTagController {

    private final CourseLearningTagService courseLearningTagService;

    public CourseLearningTagController(CourseLearningTagService courseLearningTagService) {
        this.courseLearningTagService = courseLearningTagService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseLearningTagDto addLearningTagToCourse(@PathVariable Long courseId, @RequestBody LearningTagDto learningTagDto) {
        return courseLearningTagService.addLearningTagToCourse(courseId, learningTagDto);
    }

    @DeleteMapping("/{learningTagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLearningTagFromCourse(@PathVariable Long courseId, @PathVariable Long learningTagId) {
        courseLearningTagService.removeLearningTagFromCourse(courseId, learningTagId);
    }
}
