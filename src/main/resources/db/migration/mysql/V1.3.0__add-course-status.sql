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

ALTER TABLE `course` ADD COLUMN `status_id` BIGINT;

UPDATE `course` SET `status_id` = (SELECT id FROM `course_status` WHERE name = 'Draft');

ALTER TABLE `course` MODIFY COLUMN `status_id` BIGINT NOT NULL;

ALTER TABLE `course` ADD CONSTRAINT `fk_course_course_status` FOREIGN KEY (`status_id`) REFERENCES `course_status` (`id`);
