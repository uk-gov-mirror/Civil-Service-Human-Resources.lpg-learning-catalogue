CREATE TABLE course
(
    id                 BIGINT AUTO_INCREMENT,
    uid                varchar(255)            NOT NULL,
    title              varchar(255)            NOT NULL,
    status_id          BIGINT                  NOT NULL,

    CONSTRAINT pk_course PRIMARY KEY (id),
    CONSTRAINT uq_course_uid UNIQUE (uid),
    CONSTRAINT fk_course_course_status FOREIGN KEY (`status_id`) REFERENCES course_status (`id`)
);
