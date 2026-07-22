package uk.gov.cslearning.catalogue.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    private Long id;
    private String uid;
    private String title;
    private LearningTagDto learningTag;

    public CourseDto(String uid, String title) {
        this.uid = uid;
        this.title = title;
    }

    public CourseDto(String uid, String title, LearningTagDto learningTag) {
        this.uid = uid;
        this.title = title;
        this.learningTag = learningTag;
    }
}
