-- V12__Fix_Teacher_Course_Structure.sql

-- First ensure sequences exist and are properly configured
DO $$
    BEGIN
        -- Create sequence if it doesn't exist
        IF NOT EXISTS (SELECT 1 FROM pg_sequences WHERE schemaname = 'public' AND sequencename = 'teacher_course_seq') THEN
            CREATE SEQUENCE teacher_course_seq START WITH 1 INCREMENT BY 1;
        END IF;
    END $$;

-- Recreate teacher_courses table with proper structure
DO $$
    BEGIN
        -- Drop existing table if exists
        DROP TABLE IF EXISTS teacher_course_classes;
        DROP TABLE IF EXISTS teacher_courses;

        -- Create teacher_courses table
        CREATE TABLE teacher_courses (
                                         id BIGINT DEFAULT nextval('teacher_course_seq') PRIMARY KEY,
                                         teacher_id BIGINT REFERENCES app_users(id),
                                         course_id BIGINT REFERENCES courses(id),
                                         CONSTRAINT unique_teacher_course UNIQUE (teacher_id, course_id)
        );

        -- Create teacher_course_classes table
        CREATE TABLE teacher_course_classes (
                                                teacher_course_id BIGINT,
                                                class_id BIGINT,
                                                PRIMARY KEY (teacher_course_id, class_id),
                                                FOREIGN KEY (teacher_course_id) REFERENCES teacher_courses(id) ON DELETE CASCADE,
                                                FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
        );

        -- Create indexes
        CREATE INDEX idx_teacher_courses_teacher ON teacher_courses(teacher_id);
        CREATE INDEX idx_teacher_courses_course ON teacher_courses(course_id);
        CREATE INDEX idx_tcc_teacher_course ON teacher_course_classes(teacher_course_id);
        CREATE INDEX idx_tcc_class ON teacher_course_classes(class_id);
    END $$;

-- Migrate existing teacher-course relationships
INSERT INTO teacher_courses (id, teacher_id, course_id)
SELECT nextval('teacher_course_seq'),
       u.id as teacher_id,
       c.id as course_id
FROM app_users u
         CROSS JOIN courses c
WHERE u.role = 'ROLE_TEACHER'
  AND NOT EXISTS (
    SELECT 1 FROM teacher_courses tc
    WHERE tc.teacher_id = u.id AND tc.course_id = c.id
);

-- Migrate existing class relationships
INSERT INTO teacher_course_classes (teacher_course_id, class_id)
SELECT tc.id, c.id
FROM teacher_courses tc
         CROSS JOIN classes c
WHERE NOT EXISTS (
    SELECT 1 FROM teacher_course_classes tcc
    WHERE tcc.teacher_course_id = tc.id AND tcc.class_id = c.id
);