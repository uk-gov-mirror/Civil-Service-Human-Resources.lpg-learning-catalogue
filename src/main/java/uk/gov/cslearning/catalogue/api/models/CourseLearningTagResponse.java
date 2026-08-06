package uk.gov.cslearning.catalogue.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseLearningTagResponse {
    private String id;
    private String title;
    private String status;
}
