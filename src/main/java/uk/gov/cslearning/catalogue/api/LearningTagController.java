package uk.gov.cslearning.catalogue.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.cslearning.catalogue.api.models.PageableParams;
import uk.gov.cslearning.catalogue.api.models.SimplePage;
import uk.gov.cslearning.catalogue.domain.LearningTagDto;
import uk.gov.cslearning.catalogue.service.LearningTagService;

@Slf4j
@RestController
@RequestMapping("/learning-tags")
public class LearningTagController {

    private final LearningTagService learningTagService;

    public LearningTagController(LearningTagService learningTagService) {
        this.learningTagService = learningTagService;
    }

    @GetMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public SimplePage<LearningTagDto> getTags(PageableParams pageable) {
        return learningTagService.getLearningTags(pageable.getAsPageable());
    }

}
