package uk.gov.cslearning.catalogue.service;

import org.springframework.stereotype.Service;
import uk.gov.cslearning.catalogue.domain.LearningTag;
import uk.gov.cslearning.catalogue.domain.LearningTagDto;

@Service
public class LearningTagDtoFactory {

    public LearningTagDto create(LearningTag tag) {
        LearningTag parent = tag.getParent();
        Long parentId = parent != null ? parent.getId() : null;
        String parentName = parent != null ? parent.getName() : null;
        return new LearningTagDto(
                tag.getId(), tag.getName(), tag.getDescription(), tag.getCode(),
                tag.getUrlSlug(), tag.isCategory(), tag.isArchived(), parentId, parentName,
                tag.getCreatedTimestamp(), tag.getUpdatedTimestamp(), tag.getArchivedTimestamp()
        );
    }

}
