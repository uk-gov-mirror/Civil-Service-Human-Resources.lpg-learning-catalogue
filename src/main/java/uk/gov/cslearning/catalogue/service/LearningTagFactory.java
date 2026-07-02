package uk.gov.cslearning.catalogue.service;

import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.domain.LearningTag;
import uk.gov.cslearning.catalogue.domain.LearningTagDto;
import uk.gov.cslearning.catalogue.service.util.IUtilService;

import java.time.LocalDateTime;

@Service
public class LearningTagFactory {

    private final IUtilService utilService;

    public LearningTagFactory(IUtilService utilService) {
        this.utilService = utilService;
    }

    public LearningTagDto createDto(LearningTag tag) {
        LearningTag parent = tag.getParent();
        Long parentId = parent != null ? parent.getId() : null;
        String parentName = parent != null ? parent.getName() : null;
        return new LearningTagDto(
                tag.getId(), tag.getName(), tag.getDescription(), tag.getCode(),
                tag.getUrlSlug(), tag.isCategory(), tag.isArchived(), parentId, parentName,
                tag.getCreatedTimestamp(), tag.getUpdatedTimestamp(), tag.getArchivedTimestamp()
        );
    }

    public LearningTag create(LearningTagDto learningTagDto) {
        LocalDateTime now = utilService.getNowDateTime();
        return new LearningTag(learningTagDto.getName(), learningTagDto.getDescription(), learningTagDto.getCode(),
                learningTagDto.getUrlSlug(), learningTagDto.isCategory(), learningTagDto.isArchived(), null, now, now);
    }

    public LearningTag create(LearningTagDto learningTagDto, LearningTag parent) {
        LearningTag learningTag = create(learningTagDto);
        learningTag.setParent(parent);
        return learningTag;
    }

    public LearningTag update(LearningTag learningTag, LearningTagDto learningTagDto) {
        learningTag.setDescription(learningTagDto.getDescription());
        learningTag.setName(learningTagDto.getName());
        learningTag.setCode(learningTagDto.getCode());
        learningTag.setUrlSlug(learningTagDto.getUrlSlug());
        learningTag.setCategory(learningTagDto.isCategory());
        learningTag.setArchived(learningTagDto.isArchived());
        learningTag.setParent(null);
        learningTag.setUpdatedTimestamp(utilService.getNowDateTime());
        return learningTag;
    }

    public LearningTag update(LearningTag learningTag, LearningTagDto learningTagDto, LearningTag parent) {
        learningTag = update(learningTag, learningTagDto);
        learningTag.setParent(parent);
        return learningTag;
    }
}
