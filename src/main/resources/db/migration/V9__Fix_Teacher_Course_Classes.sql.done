-- V9__Fix_Teacher_Course_Classes.sql

-- First ensure sequences exist
CREATE SEQUENCE IF NOT EXISTS teacher_course_seq START WITH 1 INCREMENT BY 1;

-- Make sure base tables exist with correct structure
DO $$
    BEGIN
        -- Check if teacher_courses table needs to be created
        IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'teacher_courses') THEN
            CREATE TABLE teacher_courses (
                                             id BIGINT PRIMARY KEY,
                                             teacher_id BIGINT REFERENCES app_users(id),
                                             course_id BIGINT REFERENCES courses(id),
                                             CONSTRAINT unique_teacher_course UNIQUE (teacher_id, course_id)
            );
        END IF;

        -- Drop the join table if it exists
        DROP TABLE IF EXISTS teacher_course_classes;

        -- Create the join table
        CREATE TABLE teacher_course_classes (
                                                teacher_course_id BIGINT,
                                                class_id BIGINT,
                                                PRIMARY KEY (teacher_course_id, class_id)
        );

        -- Add foreign key constraints separately
        ALTER TABLE teacher_course_classes
            ADD CONSTRAINT fk_tcc_teacher_course
                FOREIGN KEY (teacher_course_id)
                    REFERENCES teacher_courses(id) ON DELETE CASCADE;

        ALTER TABLE teacher_course_classes
            ADD CONSTRAINT fk_tcc_class
                FOREIGN KEY (class_id)
                    REFERENCES classes(id) ON DELETE CASCADE;

    END $$;

-- Re-insert the relationships
INSERT INTO teacher_course_classes (teacher_course_id, class_id)
SELECT DISTINCT tc.id, c.id
FROM teacher_courses tc
         CROSS JOIN classes c
WHERE NOT EXISTS (
    SELECT 1 FROM teacher_course_classes
    WHERE teacher_course_id = tc.id AND class_id = c.id
);