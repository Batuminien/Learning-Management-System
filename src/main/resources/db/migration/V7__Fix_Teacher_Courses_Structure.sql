-- V7__Fix_Teacher_Courses_Structure.sql

DO $$
    BEGIN
        -- Verify the tables exist and have id columns
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'teacher_courses'
              AND column_name = 'id'
        ) THEN
            RAISE EXCEPTION 'teacher_courses table or id column does not exist';
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'classes'
              AND column_name = 'id'
        ) THEN
            RAISE EXCEPTION 'classes table or id column does not exist';
        END IF;

        -- Drop old relationship tables and constraints if they exist
        DROP TABLE IF EXISTS teacher_classes;
        DROP TABLE IF EXISTS class_courses;

        -- Drop constraints if they exist
        ALTER TABLE classes DROP CONSTRAINT IF EXISTS classes_teacher_id_fkey;
        ALTER TABLE classes DROP COLUMN IF EXISTS teacher_id;
        ALTER TABLE courses DROP COLUMN IF EXISTS teacher_id;

        -- Drop the join table if it exists and recreate it
        DROP TABLE IF EXISTS teacher_course_classes;

        -- Create join table with explicit checks
        CREATE TABLE teacher_course_classes (
                                                teacher_course_id BIGINT,
                                                class_id BIGINT,
                                                PRIMARY KEY (teacher_course_id, class_id)
        );

        -- Add foreign keys separately
        ALTER TABLE teacher_course_classes
            ADD CONSTRAINT fk_teacher_course_classes_teacher_course
                FOREIGN KEY (teacher_course_id)
                    REFERENCES teacher_courses(id) ON DELETE CASCADE;

        ALTER TABLE teacher_course_classes
            ADD CONSTRAINT fk_teacher_course_classes_class
                FOREIGN KEY (class_id)
                    REFERENCES classes(id) ON DELETE CASCADE;

        -- Insert data with EXISTS check
        INSERT INTO teacher_course_classes (teacher_course_id, class_id)
        SELECT DISTINCT tc.id, c.id
        FROM teacher_courses tc
                 CROSS JOIN classes c
        WHERE NOT EXISTS (
            SELECT 1 FROM teacher_course_classes
            WHERE teacher_course_id = tc.id AND class_id = c.id
        );
    END $$;