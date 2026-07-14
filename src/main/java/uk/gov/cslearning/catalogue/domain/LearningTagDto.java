package uk.gov.cslearning.catalogue.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.cslearning.catalogue.api.validators.url.Slug;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LearningTagDto {

    private Long id;
    @NotNull
    @Size(min = 1, max = 50)
    private String name;
    @Size(max = 255)
    private String description;
    @NotNull
    @Size(min = 1, max = 50)
    private String code;
    @NotNull
    @Size(min = 1, max = 50)
    @Slug
    private String urlSlug;
    @NotNull
    private boolean isCategory;
    private boolean isArchived = false;
    private Long parentId;
    private String parentName;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
    private LocalDateTime archivedTimestamp;

}
