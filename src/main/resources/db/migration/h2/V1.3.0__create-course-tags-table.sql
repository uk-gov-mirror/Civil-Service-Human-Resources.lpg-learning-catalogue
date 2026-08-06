CREATE TABLE course_tags
(
    learning_tag_id        BIGINT                  NOT NULL,
    course_id              BIGINT                  NOT NULL,

    PRIMARY KEY (learning_tag_id,course_id),
    CONSTRAINT fk_course_tags_learning_tag FOREIGN KEY (learning_tag_id) REFERENCES learning_tag (id),
    CONSTRAINT fk_course_tags_course FOREIGN KEY (course_id) REFERENCES course (id)
);
