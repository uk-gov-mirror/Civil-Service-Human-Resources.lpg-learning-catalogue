package uk.gov.cslearning.catalogue.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class LearningTagDto {

    private Long id;
    private String name;
    private String description;
    private String code;
    private String urlSlug;
    private boolean isCategoryTag;
    private boolean isArchived;
    private Long parentId;
    private String parentName;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
    private LocalDateTime archivedTimestamp;

}
