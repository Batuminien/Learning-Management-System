-- Insert TeacherCourses for Mathematics Teachers
INSERT INTO teacher_courses (id, teacher_id, course_id)
VALUES
    -- Teacher1 (Mathematics)
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher1'),
     (SELECT id FROM courses WHERE code = 'MAT-9')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher1'),
     (SELECT id FROM courses WHERE code = 'MAT-10')),
    -- Teacher2 (Mathematics)
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher2'),
     (SELECT id FROM courses WHERE code = 'MAT-11')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher2'),
     (SELECT id FROM courses WHERE code = 'MAT-12')),

    -- Teacher3 (Physics)
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher3'),
     (SELECT id FROM courses WHERE code = 'FIZ-9')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher3'),
     (SELECT id FROM courses WHERE code = 'FIZ-10')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher3'),
     (SELECT id FROM courses WHERE code = 'FIZ-11')),

    -- Teacher4 (Chemistry)
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher4'),
     (SELECT id FROM courses WHERE code = 'KIM-9')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher4'),
     (SELECT id FROM courses WHERE code = 'KIM-10')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher4'),
     (SELECT id FROM courses WHERE code = 'KIM-11')),

    -- Teacher5 (Biology)
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher5'),
     (SELECT id FROM courses WHERE code = 'BIO-9')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher5'),
     (SELECT id FROM courses WHERE code = 'BIO-10')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher5'),
     (SELECT id FROM courses WHERE code = 'BIO-11')),

    -- Teacher6 (Turkish)
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher6'),
     (SELECT id FROM courses WHERE code = 'TUR-9')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher6'),
     (SELECT id FROM courses WHERE code = 'TUR-10')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher6'),
     (SELECT id FROM courses WHERE code = 'TUR-11')),

    -- Teacher7 (English)
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher7'),
     (SELECT id FROM courses WHERE code = 'ENG-9')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher7'),
     (SELECT id FROM courses WHERE code = 'ENG-10')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher7'),
     (SELECT id FROM courses WHERE code = 'ENG-11')),

    -- Teacher8 (History)
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher8'),
     (SELECT id FROM courses WHERE code = 'TAR-9')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher8'),
     (SELECT id FROM courses WHERE code = 'TAR-10')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher8'),
     (SELECT id FROM courses WHERE code = 'TAR-11')),

    -- Teacher9 (Geography)
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher9'),
     (SELECT id FROM courses WHERE code = 'COG-9')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher9'),
     (SELECT id FROM courses WHERE code = 'COG-10')),
    (nextval('teacher_course_seq'),
     (SELECT id FROM app_users WHERE username = 'teacher9'),
     (SELECT id FROM courses WHERE code = 'COG-11'));

-- Link Classes with Courses
INSERT INTO course_classes (course_id, class_id)
SELECT c.id, cl.id
FROM courses c
         CROSS JOIN classes cl
WHERE
    (c.code LIKE '%-9' AND cl.name LIKE '9-%') OR
    (c.code LIKE '%-10' AND cl.name LIKE '10-%') OR
    (c.code LIKE '%-11' AND cl.name LIKE '11-%') OR
    (c.code LIKE '%-12' AND cl.name LIKE '12-%');

-- Link TeacherCourses with Classes
INSERT INTO teacher_course_classes (teacher_course_id, class_id)
SELECT tc.id, cc.class_id
FROM teacher_courses tc
         JOIN courses c ON tc.course_id = c.id
         JOIN course_classes cc ON cc.course_id = c.id
WHERE
    (c.code LIKE '%-9' AND EXISTS (SELECT 1 FROM classes cl WHERE cl.id = cc.class_id AND cl.name LIKE '9-%')) OR
    (c.code LIKE '%-10' AND EXISTS (SELECT 1 FROM classes cl WHERE cl.id = cc.class_id AND cl.name LIKE '10-%')) OR
    (c.code LIKE '%-11' AND EXISTS (SELECT 1 FROM classes cl WHERE cl.id = cc.class_id AND cl.name LIKE '11-%')) OR
    (c.code LIKE '%-12' AND EXISTS (SELECT 1 FROM classes cl WHERE cl.id = cc.class_id AND cl.name LIKE '12-%'));