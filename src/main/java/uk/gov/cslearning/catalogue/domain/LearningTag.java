package uk.gov.cslearning.catalogue.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LearningTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false, length = 50)
    String name;

    @Column
    String description;

    @Column(unique = true, nullable = false, length = 10)
    private String code;

    @Column(unique = true, nullable = false, length = 50)
    private String urlSlug;

    @Column(nullable = false)
    private boolean isCategory;

    @Column(nullable = false)
    private boolean isArchived;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonBackReference
    LearningTag parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    Set<LearningTag> children = new HashSet<>();

    @OneToMany(mappedBy = "learningTag", fetch = FetchType.LAZY)
    private Set<CourseLearningTagEntity> courses = new HashSet<>();

    @OneToMany(mappedBy = "learningTag", fetch = FetchType.LAZY)
    private Set<LearningTagHyperlink> hyperlinks = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdTimestamp;

    @Column(nullable = false)
    private LocalDateTime updatedTimestamp;

    @Column
    private LocalDateTime archivedTimestamp;

    public LearningTag(String name, String description, String code, String urlSlug, boolean isCategory,
                       boolean isArchived, LearningTag parent, LocalDateTime createdTimestamp, LocalDateTime updatedTimestamp) {
        this.name = name;
        this.description = description;
        this.code = code;
        this.urlSlug = urlSlug;
        this.isCategory = isCategory;
        this.isArchived = isArchived;
        this.parent = parent;
        this.createdTimestamp = createdTimestamp;
        this.updatedTimestamp = updatedTimestamp;
    }
}
