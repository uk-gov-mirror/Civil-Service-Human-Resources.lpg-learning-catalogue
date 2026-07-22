package uk.gov.cslearning.catalogue.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "course_tags")
@IdClass(CourseLearningTagId.class)
public class CourseTagEntity {

    @Id
    @ManyToOne
    @JoinColumn(name = "learning_tag_id", referencedColumnName = "id")
    private LearningTag learningTag;

    @Id
    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private CourseEntity course;

    public CourseTagEntity(LearningTag learningTag, CourseEntity course) {
        this.learningTag = learningTag;
        this.course = course;
    }
}
