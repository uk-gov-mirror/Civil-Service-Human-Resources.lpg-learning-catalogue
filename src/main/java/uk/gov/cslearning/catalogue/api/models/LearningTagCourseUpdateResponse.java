package uk.gov.cslearning.catalogue.api.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LearningTagCourseUpdateResponse extends BulkUpdateResponse<Long> {
    private Long learningTagId;

    public LearningTagCourseUpdateResponse(Collection<Long> successfulIds, Collection<Long> failedIds, Long learningTagId) {
        super(successfulIds, failedIds);
        this.learningTagId = learningTagId;
    }
}
