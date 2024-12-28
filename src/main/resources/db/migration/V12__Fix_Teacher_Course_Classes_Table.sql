-- V12__Fix_Teacher_Course_Classes_Table.sql

-- First verify if table exists and create if it doesn't
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_tables
            WHERE schemaname = 'public'
              AND tablename = 'teacher_course_classes'
        ) THEN
            CREATE TABLE teacher_course_classes (
                                                    teacher_course_id BIGINT,
                                                    class_id BIGINT,
                                                    CONSTRAINT pk_teacher_course_classes PRIMARY KEY (teacher_course_id, class_id)
            );

            -- Add foreign key constraints
            ALTER TABLE teacher_course_classes
                ADD CONSTRAINT fk_tcc_teacher_course
                    FOREIGN KEY (teacher_course_id)
                        REFERENCES teacher_courses(id)
                        ON DELETE CASCADE;

            ALTER TABLE teacher_course_classes
                ADD CONSTRAINT fk_tcc_class
                    FOREIGN KEY (class_id)
                        REFERENCES classes(id)
                        ON DELETE CASCADE;

            -- Add indexes for better performance
            CREATE INDEX idx_tcc_teacher_course ON teacher_course_classes(teacher_course_id);
            CREATE INDEX idx_tcc_class ON teacher_course_classes(class_id);
        END IF;
    END $$;

-- Migrate existing relationships if needed
DO $$
    BEGIN
        -- Insert teacher-class relationships through teacher_courses
        INSERT INTO teacher_course_classes (teacher_course_id, class_id)
        SELECT DISTINCT tc.id, c.id
        FROM teacher_courses tc
                 CROSS JOIN classes c
        WHERE NOT EXISTS (
            SELECT 1
            FROM teacher_course_classes tcc
            WHERE tcc.teacher_course_id = tc.id
              AND tcc.class_id = c.id
        );
    END $$;