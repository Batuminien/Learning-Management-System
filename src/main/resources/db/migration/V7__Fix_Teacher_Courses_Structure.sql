-- V7__Fix_Teacher_Courses_Structure.sql

-- First, let's log the state before we start
DO $$
    BEGIN
        -- Log table existence
        RAISE NOTICE 'teacher_courses exists: %', (
            SELECT EXISTS (
                SELECT FROM pg_tables
                WHERE schemaname = 'public'
                  AND tablename = 'teacher_courses'
            )
        );

        RAISE NOTICE 'classes exists: %', (
            SELECT EXISTS (
                SELECT FROM pg_tables
                WHERE schemaname = 'public'
                  AND tablename = 'classes'
            )
        );

        -- Log column existence
        RAISE NOTICE 'teacher_courses.id exists: %', (
            SELECT EXISTS (
                SELECT FROM information_schema.columns
                WHERE table_name = 'teacher_courses'
                  AND column_name = 'id'
            )
        );

        RAISE NOTICE 'classes.id exists: %', (
            SELECT EXISTS (
                SELECT FROM information_schema.columns
                WHERE table_name = 'classes'
                  AND column_name = 'id'
            )
        );
    END $$;

-- Now proceed with the actual migration
DO $$
    BEGIN
        -- Drop old stuff first
        DROP TABLE IF EXISTS teacher_classes;
        DROP TABLE IF EXISTS class_courses;
        ALTER TABLE classes DROP CONSTRAINT IF EXISTS classes_teacher_id_fkey;
        ALTER TABLE classes DROP COLUMN IF EXISTS teacher_id;
        ALTER TABLE courses DROP COLUMN IF EXISTS teacher_id;

        -- Drop and recreate the join table
        DROP TABLE IF EXISTS teacher_course_classes;

        EXECUTE 'CREATE TABLE teacher_course_classes (
        teacher_course_id BIGINT,
        class_id BIGINT,
        PRIMARY KEY (teacher_course_id, class_id)
    )';

        -- Add constraints separately
        EXECUTE 'ALTER TABLE teacher_course_classes
        ADD CONSTRAINT fk_tcc_teacher_course
        FOREIGN KEY (teacher_course_id)
        REFERENCES teacher_courses(id) ON DELETE CASCADE';

        EXECUTE 'ALTER TABLE teacher_course_classes
        ADD CONSTRAINT fk_tcc_class
        FOREIGN KEY (class_id)
        REFERENCES classes(id) ON DELETE CASCADE';

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