package uk.gov.cslearning.catalogue.domain;

import lombok.Getter;

@Getter
public enum LearningTagState {
    ARCHIVE("ARCHIVE"),
    UNARCHIVE("UNARCHIVE");

    private final String name;

    LearningTagState(String name) {
        this.name = name;
    }
}
