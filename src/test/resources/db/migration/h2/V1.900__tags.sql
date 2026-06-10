INSERT INTO learning_tag (id, name, description, code, url_slug, is_category)
VALUES (1, 'Project management', 'Broad project management skills', 'PM', 'project-management', TRUE),
       (2, 'Tech', 'Technical skills', 'TECH', 'tech', TRUE);

INSERT INTO learning_tag (name, description, code, url_slug, parent_id, is_category)
VALUES ('Agile', 'Agile', 'AGILE', 'agile', 1, TRUE),
       ('Software Development', 'Designing and writing code ', 'SOFTDEV', 'software-development', 2, TRUE),
       ('Devops', 'Managing and deploying infrastructure', 'DEVOPS', 'devops', 2, TRUE);
