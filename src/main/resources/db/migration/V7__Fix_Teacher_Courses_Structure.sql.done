-- V7__Fix_Teacher_Courses_Structure.sql

DO $$
    DECLARE
        tc_exists boolean;
        c_exists boolean;
    BEGIN
        -- Check if tables and columns exist
        SELECT EXISTS (
            SELECT FROM information_schema.columns
            WHERE table_name = 'teacher_courses'
              AND column_name = 'id'
        ) INTO tc_exists;

        SELECT EXISTS (
            SELECT FROM information_schema.columns
            WHERE table_name = 'classes'
              AND column_name = 'id'
        ) INTO c_exists;

        RAISE NOTICE 'teacher_courses.id exists: %', tc_exists;
        RAISE NOTICE 'classes.id exists: %', c_exists;

        -- If either table is missing, recreate them
        IF NOT tc_exists THEN
            CREATE TABLE IF NOT EXISTS teacher_courses (
                                                           id BIGINT PRIMARY KEY,
                                                           teacher_id BIGINT REFERENCES app_users(id),
                                                           course_id BIGINT REFERENCES courses(id)
            );
        END IF;

        -- Drop old stuff
        DROP TABLE IF EXISTS teacher_classes;
        DROP TABLE IF EXISTS class_courses;
        DROP TABLE IF EXISTS teacher_course_classes;

        -- Create the join table with inline constraints (no separate ALTER TABLE)
        CREATE TABLE teacher_course_classes (
                                                teacher_course_id BIGINT REFERENCES teacher_courses(id) ON DELETE CASCADE,
                                                class_id BIGINT REFERENCES classes(id) ON DELETE CASCADE,
                                                PRIMARY KEY (teacher_course_id, class_id)
        );

        -- Insert data
        INSERT INTO teacher_course_classes (teacher_course_id, class_id)
        SELECT DISTINCT tc.id, c.id
        FROM teacher_courses tc
                 CROSS JOIN classes c
        WHERE NOT EXISTS (
            SELECT 1 FROM teacher_course_classes
            WHERE teacher_course_id = tc.id AND class_id = c.id
        );

        RAISE NOTICE 'Migration completed successfully';
    END $$;