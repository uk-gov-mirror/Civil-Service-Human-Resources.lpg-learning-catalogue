INSERT INTO course (id, uid, title, status_id, short_description)
VALUES (1, 'ABC', 'Course ABC', 2, 'Course ABC short description'),
       (2, 'DEF', 'Course DEF', 2, 'Course DEF short description'),
       (3, 'GHI', 'Course GHI', 2, 'Course GHI short description');

INSERT INTO course_tags (learning_tag_id, course_id)
VALUES (1, 1),
       (1, 2),
       (1, 3);

INSERT INTO learning_tag_hyperlink
    (id, learning_tag_id, href, title, description)
VALUES (1, 1, 'https://www.fake-site.co.uk', 'Fake site', 'A fake website'),
       (2, 1, 'https://www.another-fake-site.co.uk', 'Another fake site', 'Another fake website');
