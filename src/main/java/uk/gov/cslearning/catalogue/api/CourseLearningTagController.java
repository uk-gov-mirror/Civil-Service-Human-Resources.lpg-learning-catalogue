package uk.gov.cslearning.catalogue.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.cslearning.catalogue.service.CourseLearningTagService;

@RestController
@RequestMapping("/courses/{courseUid}/learning-tags")
public class CourseLearningTagController {

    private final CourseLearningTagService courseLearningTagService;

    public CourseLearningTagController(CourseLearningTagService courseLearningTagService) {
        this.courseLearningTagService = courseLearningTagService;
    }

    @DeleteMapping("/{learningTagCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLearningTagFromCourse(@PathVariable String courseUid, @PathVariable String learningTagCode) {
        courseLearningTagService.removeLearningTagFromCourse(courseUid, learningTagCode);
    }
}
