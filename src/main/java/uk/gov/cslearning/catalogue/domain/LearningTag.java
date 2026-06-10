package uk.gov.cslearning.catalogue.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class LearningTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false, length = 30)
    String name;

    @Column(nullable = false)
    String description;

    @Column(unique = true, nullable = false, length = 10)
    private String code;

    @Column(unique = true, nullable = false, length = 30)
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

    @Column(nullable = false)
    private LocalDateTime createdTimestamp;

    @Column(nullable = false)
    private LocalDateTime updatedTimestamp;

    @Column
    private LocalDateTime archivedTimestamp;
}
