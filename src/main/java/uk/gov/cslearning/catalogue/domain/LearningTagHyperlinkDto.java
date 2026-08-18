package uk.gov.cslearning.catalogue.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LearningTagHyperlinkDto {
    private Long id;
    private String title;
    private String description;
    private String href;
}
