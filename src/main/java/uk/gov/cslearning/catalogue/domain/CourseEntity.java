package uk.gov.cslearning.catalogue.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "course")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uid;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private CourseStatusEntity status;

    public CourseEntity(String uid, String title) {
        this.uid = uid;
        this.title = title;
    }

    public CourseEntity(String uid, String title, CourseStatusEntity status) {
        this.uid = uid;
        this.title = title;
        this.status = status;
    }
}
