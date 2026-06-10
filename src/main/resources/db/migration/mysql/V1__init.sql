CREATE TABLE learning_tag
(
    id                 BIGINT AUTO_INCREMENT,
    name               VARCHAR(30)             NOT NULL,
    description        VARCHAR(255)            NOT NULL,
    code               VARCHAR(10)             NOT NULL,
    url_slug           VARCHAR(30)             NOT NULL,
    is_category        BOOLEAN   DEFAULT FALSE NOT NULL,
    is_archived        BOOLEAN   DEFAULT FALSE NOT NULL,
    parent_id          BIGINT,
    created_timestamp  TIMESTAMP DEFAULT NOW() NOT NULL,
    updated_timestamp  TIMESTAMP DEFAULT NOW() NOT NULL,
    archived_timestamp TIMESTAMP,

    CONSTRAINT pk_learning_tag PRIMARY KEY (id),
    CONSTRAINT uq_learning_tag_name UNIQUE (name),
    CONSTRAINT uq_learning_tag_code UNIQUE (code),
    CONSTRAINT uq_learning_tag_url_slug UNIQUE (url_slug),
    CONSTRAINT fk_learning_tag_parent FOREIGN KEY (parent_id)
        REFERENCES learning_tag (id) ON DELETE SET NULL
);
