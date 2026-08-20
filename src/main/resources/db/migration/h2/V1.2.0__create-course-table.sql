CREATE TABLE course
(
    id                 BIGINT AUTO_INCREMENT,
    uid                VARCHAR(255)            NOT NULL,
    title              VARCHAR(255)            NOT NULL,
    short_description  VARCHAR(160)            NOT NULL,
    status_id          BIGINT                  NOT NULL,

    CONSTRAINT pk_course PRIMARY KEY (id),
    CONSTRAINT uq_course_uid UNIQUE (uid),
    CONSTRAINT fk_course_course_status FOREIGN KEY (`status_id`) REFERENCES course_status (`id`)
);
