-- V5__Update_Class_Course_Structure.sql

-- Drop old relationship tables and constraints
DROP TABLE IF EXISTS teacher_classes;
DROP TABLE IF EXISTS class_courses;
ALTER TABLE classes DROP CONSTRAINT IF EXISTS classes_teacher_id_fkey;
ALTER TABLE classes DROP COLUMN IF EXISTS teacher_id;
ALTER TABLE courses DROP COLUMN IF EXISTS teacher_id;

-- Create teacher_course table
CREATE SEQUENCE IF NOT EXISTS teacher_course_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE teacher_courses (
                                 id BIGINT PRIMARY KEY,
                                 teacher_id BIGINT REFERENCES app_users(id),
                                 course_id BIGINT REFERENCES courses(id),
                                 CONSTRAINT unique_teacher_course UNIQUE (teacher_id, course_id)
);

-- Create join table for teacher_course_classes
CREATE TABLE teacher_course_classes (
                                        teacher_course_id BIGINT REFERENCES teacher_courses(id) ON DELETE CASCADE,
                                        class_id BIGINT REFERENCES classes(id) ON DELETE CASCADE,
                                        PRIMARY KEY (teacher_course_id, class_id)
);

-- Migrate existing teacher-course relationships
INSERT INTO teacher_courses (id, teacher_id, course_id)
SELECT nextval('teacher_course_seq'), au.id, c.id
FROM app_users au
         CROSS JOIN courses c
WHERE au.role = 'ROLE_TEACHER';

-- Migrate existing class assignments
INSERT INTO teacher_course_classes (teacher_course_id, class_id)
SELECT tc.id, c.id
FROM teacher_courses tc
         CROSS JOIN classes c;