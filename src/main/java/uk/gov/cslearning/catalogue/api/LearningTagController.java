package uk.gov.cslearning.catalogue.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.cslearning.catalogue.api.models.*;
import uk.gov.cslearning.catalogue.domain.LearningTagBulkStateDto;
import uk.gov.cslearning.catalogue.domain.LearningTagDto;
import uk.gov.cslearning.catalogue.domain.LearningTagHyperlinkDto;
import uk.gov.cslearning.catalogue.dto.BulkUpdateDto;
import uk.gov.cslearning.catalogue.service.LearningTagService;

import javax.validation.Valid;

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

    @GetMapping("/{learningTagId}/courses")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public SimplePage<CourseLearningTagResponse> getCoursesByTag(@PathVariable Long learningTagId, PageableParams pageable) {
        return learningTagService.getCoursesByLearningTagId(learningTagId, pageable.getAsPageable());
    }

    @GetMapping("/{learningTagId}/hyperlinks")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public SimplePage<LearningTagHyperlinkDto> getHyperlinksByTag(@PathVariable Long learningTagId, PageableParams pageable) {
        return learningTagService.getHyperlinksByLearningTagId(learningTagId, pageable.getAsPageable());
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public LearningTagDto createTag(@Valid @RequestBody LearningTagDto learningTag) {
        return learningTagService.createLearningTag(learningTag);
    }

    @PostMapping("/{learningTagId}/hyperlink")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public LearningTagHyperlinkDto createHyperlink(@PathVariable Long learningTagId, @Valid @RequestBody LearningTagHyperlinkDto dto) {
        return learningTagService.createLearningTagHyperlink(learningTagId, dto);
    }

    @PutMapping("/{learningTagId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public LearningTagDto updateTag(@PathVariable Long learningTagId, @Valid @RequestBody LearningTagDto learningTag) {
        return learningTagService.updateLearningTag(learningTagId, learningTag);
    }

    @PutMapping("/state")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BulkUpdateDto updateTagState(@Valid @RequestBody LearningTagBulkStateDto dto) {
        return learningTagService.updateLearningTagState(dto);
    }

    @DeleteMapping("/{learningTagId}/courses")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BulkUpdateResponse<String> removeCoursesFromTag(@PathVariable Long learningTagId, @RequestBody IdsDto<String> courseIdsDto) {
        return learningTagService.removeCoursesFromLearningTag(learningTagId, courseIdsDto);
    }

    @DeleteMapping("/{learningTagId}/hyperlinks")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BulkUpdateResponse<Long> removeHyperlinksFromTag(@PathVariable Long learningTagId, @RequestBody IdsDto<Long> hyperlinkIdsDto) {
        return learningTagService.removeHyperlinksFromLearningTag(learningTagId, hyperlinkIdsDto);
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignCoursesToTag(@Valid @RequestBody LearningTagCourseBulkRequest request) {
        learningTagService.assignCoursesToTag(request);
    }
}
