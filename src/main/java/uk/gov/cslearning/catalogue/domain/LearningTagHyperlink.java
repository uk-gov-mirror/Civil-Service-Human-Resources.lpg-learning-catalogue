package uk.gov.cslearning.catalogue.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "learning_tag_hyperlink")
public class LearningTagHyperlink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_tag_id", referencedColumnName = "id", nullable = false)
    private LearningTag learningTag;

    @Column(nullable = false, length = 255)
    private String href;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(name = "created_timestamp", nullable = false)
    private LocalDateTime createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private LocalDateTime updatedTimestamp;

    public LearningTagHyperlink(LearningTag learningTag, String href, String title, String description) {
        this.learningTag = learningTag;
        this.href = href;
        this.title = title;
        this.description = description;
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = LocalDateTime.now();
    }
}
