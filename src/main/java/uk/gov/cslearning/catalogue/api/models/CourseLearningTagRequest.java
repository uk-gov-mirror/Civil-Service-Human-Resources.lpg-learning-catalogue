package uk.gov.cslearning.catalogue.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseLearningTagRequest {
    @NotBlank
    private String uid;
    @NotBlank
    private String title;
    @NotBlank
    private String status;
}
