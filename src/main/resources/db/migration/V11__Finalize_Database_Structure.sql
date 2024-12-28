-- V11__Finalize_Database_Structure.sql

-- Handle each constraint/index in separate DO blocks for better error handling

-- 1. Handle unique constraint for teacher_courses
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.table_constraints
            WHERE constraint_name = 'unique_teacher_course'
              AND table_name = 'teacher_courses'
        ) THEN
            EXECUTE 'ALTER TABLE teacher_courses ADD CONSTRAINT unique_teacher_course UNIQUE (teacher_id, course_id)';
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Error adding unique constraint: %', SQLERRM;
    END $$;

-- 2. Handle indexes for teacher_courses
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_indexes
            WHERE tablename = 'teacher_courses' AND indexname = 'idx_teacher_courses_teacher'
        ) THEN
            CREATE INDEX IF NOT EXISTS idx_teacher_courses_teacher ON teacher_courses(teacher_id);
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Error creating teacher_id index: %', SQLERRM;
    END $$;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_indexes
            WHERE tablename = 'teacher_courses' AND indexname = 'idx_teacher_courses_course'
        ) THEN
            CREATE INDEX IF NOT EXISTS idx_teacher_courses_course ON teacher_courses(course_id);
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Error creating course_id index: %', SQLERRM;
    END $$;

-- 3. Handle teacher_course_classes foreign key
DO $$
    BEGIN
        ALTER TABLE IF EXISTS teacher_course_classes
            DROP CONSTRAINT IF EXISTS fk_tcc_teacher_course;

        ALTER TABLE teacher_course_classes
            ADD CONSTRAINT fk_tcc_teacher_course
                FOREIGN KEY (teacher_course_id)
                    REFERENCES teacher_courses(id)
                    ON DELETE CASCADE;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Error handling teacher_course_classes constraints: %', SQLERRM;
    END $$;

-- 4. Handle class_students indexes and constraints
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_indexes
            WHERE tablename = 'class_students' AND indexname = 'idx_class_students_student'
        ) THEN
            CREATE INDEX IF NOT EXISTS idx_class_students_student ON class_students(student_id);
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Error creating class_students index: %', SQLERRM;
    END $$;

-- 5. Validate assignments relationships
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_indexes
            WHERE tablename = 'assignments' AND indexname = 'idx_assignments_teacher_course'
        ) THEN
            CREATE INDEX IF NOT EXISTS idx_assignments_teacher_course ON assignments(teacher_course_id);
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE NOTICE 'Error creating assignments index: %', SQLERRM;
    END $$;