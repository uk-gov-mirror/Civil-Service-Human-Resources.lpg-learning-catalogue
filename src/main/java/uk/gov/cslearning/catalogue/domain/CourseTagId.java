package uk.gov.cslearning.catalogue.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTagId implements Serializable {
    private Long learningTag;
    private Long course;
}
