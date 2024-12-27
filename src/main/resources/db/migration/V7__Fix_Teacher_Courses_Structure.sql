-- V7__Fix_Teacher_Courses_Structure.sql

-- Drop old relationship tables and constraints (keep these the same)
DROP TABLE IF EXISTS teacher_classes;
DROP TABLE IF EXISTS class_courses;
ALTER TABLE classes DROP CONSTRAINT IF EXISTS classes_teacher_id_fkey;
ALTER TABLE classes DROP COLUMN IF EXISTS teacher_id;
ALTER TABLE courses DROP COLUMN IF EXISTS teacher_id;

-- Create join table (keep this the same)
CREATE TABLE IF NOT EXISTS teacher_course_classes (
                                                      teacher_course_id BIGINT REFERENCES teacher_courses(id) ON DELETE CASCADE,
                                                      class_id BIGINT REFERENCES classes(id) ON DELETE CASCADE,
                                                      PRIMARY KEY (teacher_course_id, class_id)
);

-- Modified INSERT statements with WHERE NOT EXISTS checks
INSERT INTO teacher_courses (id, teacher_id, course_id)
SELECT nextval('teacher_course_seq'), au.id, c.id
FROM app_users au
         CROSS JOIN courses c
WHERE au.role = 'ROLE_TEACHER'
  AND NOT EXISTS (
    SELECT 1 FROM teacher_courses
    WHERE teacher_id = au.id AND course_id = c.id
);

-- Modified class assignments insert
INSERT INTO teacher_course_classes (teacher_course_id, class_id)
SELECT tc.id, c.id
FROM teacher_courses tc
         CROSS JOIN classes c
WHERE NOT EXISTS (
    SELECT 1 FROM teacher_course_classes
    WHERE teacher_course_id = tc.id AND class_id = c.id
);