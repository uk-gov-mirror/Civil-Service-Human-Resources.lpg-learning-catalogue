CREATE TABLE learning_tag_hyperlink
(
    id                              BIGINT AUTO_INCREMENT,
    learning_tag_id                 BIGINT                  NOT NULL,
    href                            VARCHAR(255)           NOT NULL,
    title                           VARCHAR(50)            NOT NULL,
    description                     VARCHAR(255),
    created_timestamp               TIMESTAMP DEFAULT NOW() NOT NULL,
    updated_timestamp               TIMESTAMP DEFAULT NOW() NOT NULL,

    CONSTRAINT pk_learning_tag_hyperlink PRIMARY KEY (id),
    CONSTRAINT uq_learning_tag_hyperlink_learning_tag_id_href UNIQUE (learning_tag_id, href),
    CONSTRAINT fk_learning_tag_hyperlink_learning_tag FOREIGN KEY (learning_tag_id) REFERENCES learning_tag (id)
);
