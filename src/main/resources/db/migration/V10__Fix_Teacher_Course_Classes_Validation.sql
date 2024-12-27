DO $$
    DECLARE
        tc_table_exists boolean;
        c_table_exists boolean;
        tc_id_exists boolean;
        c_id_exists boolean;
    BEGIN
        -- Check table existence
        SELECT EXISTS (
            SELECT FROM pg_tables
            WHERE schemaname = 'public' AND tablename = 'teacher_courses'
        ) INTO tc_table_exists;

        SELECT EXISTS (
            SELECT FROM pg_tables
            WHERE schemaname = 'public' AND tablename = 'classes'
        ) INTO c_table_exists;

        -- Check id columns existence
        SELECT EXISTS (
            SELECT FROM information_schema.columns
            WHERE table_name = 'teacher_courses' AND column_name = 'id'
        ) INTO tc_id_exists;

        SELECT EXISTS (
            SELECT FROM information_schema.columns
            WHERE table_name = 'classes' AND column_name = 'id'
        ) INTO c_id_exists;

        -- Log the state
        RAISE NOTICE 'teacher_courses table exists: %, id column exists: %', tc_table_exists, tc_id_exists;
        RAISE NOTICE 'classes table exists: %, id column exists: %', c_table_exists, c_id_exists;

        -- Validate prerequisites
        IF NOT (tc_table_exists AND c_table_exists AND tc_id_exists AND c_id_exists) THEN
            RAISE EXCEPTION 'Prerequisites not met: teacher_courses or classes tables/columns missing';
        END IF;

        -- If we get here, all prerequisites are met
        DROP TABLE IF EXISTS teacher_course_classes;

        -- Create base table without constraints
        CREATE TABLE teacher_course_classes (
                                                teacher_course_id BIGINT,
                                                class_id BIGINT
        );

        -- Add primary key first
        ALTER TABLE teacher_course_classes
            ADD PRIMARY KEY (teacher_course_id, class_id);

        -- Verify tables again before adding foreign keys
        RAISE NOTICE 'Adding foreign key constraints...';

        -- Add foreign keys
        ALTER TABLE teacher_course_classes
            ADD CONSTRAINT fk_tcc_teacher_course
                FOREIGN KEY (teacher_course_id)
                    REFERENCES teacher_courses(id) ON DELETE CASCADE;

        ALTER TABLE teacher_course_classes
            ADD CONSTRAINT fk_tcc_class
                FOREIGN KEY (class_id)
                    REFERENCES classes(id) ON DELETE CASCADE;

        -- Reinsert data
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

        RAISE NOTICE 'Migration completed successfully';
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Error occurred: %', SQLERRM;
            RAISE;
    END $$;