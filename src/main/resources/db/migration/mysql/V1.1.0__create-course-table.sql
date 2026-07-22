CREATE TABLE course
(
    id                 smallint unsigned AUTO_INCREMENT,
    uid                varchar(255)            NOT NULL,
    title              varchar(255)            NOT NULL,

    CONSTRAINT pk_course PRIMARY KEY (id),
    CONSTRAINT uq_course_uid UNIQUE (uid)
);
