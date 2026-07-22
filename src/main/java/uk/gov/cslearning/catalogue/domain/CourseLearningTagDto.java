package uk.gov.cslearning.catalogue.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseLearningTagDto {

    private Long id;
    private String uid;
    private String title;
    private LearningTagDto learningTag;

    public CourseLearningTagDto(String uid, String title) {
        this.uid = uid;
        this.title = title;
    }

    public CourseLearningTagDto(String uid, String title, LearningTagDto learningTag) {
        this.uid = uid;
        this.title = title;
        this.learningTag = learningTag;
    }
}
