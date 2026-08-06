CREATE TABLE `course_status`
(
    `id`    BIGINT AUTO_INCREMENT,
    `name`  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_course_status PRIMARY KEY (id),
    CONSTRAINT uq_course_status_name UNIQUE (name)
);

INSERT INTO `course_status` (name) VALUES ('Draft');
INSERT INTO `course_status` (name) VALUES ('Published');
INSERT INTO `course_status` (name) VALUES ('Archived');
