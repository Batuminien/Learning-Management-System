-- V11__Finalize_Database_Structure.sql

-- Wrap everything in a transaction
BEGIN;

-- First, validate and fix teacher_courses structure
DO $$
BEGIN
    -- Ensure teacher_courses has proper constraints
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'unique_teacher_course'
        AND table_name = 'teacher_courses'
    ) THEN
        ALTER TABLE teacher_courses
        ADD CONSTRAINT unique_teacher_course UNIQUE (teacher_id, course_id);
    END IF;

    -- Ensure proper indexes exist for performance
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'teacher_courses' AND indexname = 'idx_teacher_courses_teacher'
    ) THEN
        CREATE INDEX idx_teacher_courses_teacher ON teacher_courses(teacher_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'teacher_courses' AND indexname = 'idx_teacher_courses_course'
    ) THEN
        CREATE INDEX idx_teacher_courses_course ON teacher_courses(course_id);
    END IF;
END $$;

-- Validate and fix teacher_course_classes structure
DO $$
BEGIN
    -- Ensure proper cascade delete is set up
    ALTER TABLE teacher_course_classes
        DROP CONSTRAINT IF EXISTS fk_tcc_teacher_course;

    ALTER TABLE teacher_course_classes
        ADD CONSTRAINT fk_tcc_teacher_course
        FOREIGN KEY (teacher_course_id)
        REFERENCES teacher_courses(id)
        ON DELETE CASCADE;

    -- Add missing indexes if they don't exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'teacher_course_classes' AND indexname = 'idx_tcc_class'
    ) THEN
        CREATE INDEX idx_tcc_class ON teacher_course_classes(class_id);
    END IF;
END $$;

-- Validate and fix assignments structure
DO $$
BEGIN
    -- Ensure all assignments have valid teacher_course_id
    IF EXISTS (
        SELECT 1 FROM assignments
        WHERE teacher_course_id IS NULL
        OR teacher_course_id NOT IN (SELECT id FROM teacher_courses)
    ) THEN
        -- Update assignments with missing teacher_course_id
        UPDATE assignments a
        SET teacher_course_id = (
            SELECT tc.id
            FROM teacher_courses tc
            WHERE tc.teacher_id = a.assigned_by_teacher_id
            AND tc.course_id = a.course_id
            LIMIT 1
        )
        WHERE teacher_course_id IS NULL
        OR teacher_course_id NOT IN (SELECT id FROM teacher_courses);
    END IF;

    -- Add missing indexes for performance
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'assignments' AND indexname = 'idx_assignments_teacher_course'
    ) THEN
        CREATE INDEX idx_assignments_teacher_course ON assignments(teacher_course_id);
    END IF;
END $$;

-- Validate class_students structure
DO $$
BEGIN
    -- Ensure proper indexes exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'class_students' AND indexname = 'idx_class_students_student'
    ) THEN
        CREATE INDEX idx_class_students_student ON class_students(student_id);
    END IF;

    -- Validate foreign key constraints
    ALTER TABLE class_students
        DROP CONSTRAINT IF EXISTS fk_class_students_class;

    ALTER TABLE class_students
        ADD CONSTRAINT fk_class_students_class
        FOREIGN KEY (class_id)
        REFERENCES classes(id)
        ON DELETE CASCADE;
END $$;

-- Final validation
DO $$
BEGIN
    -- Verify all required sequences exist and are properly owned
    IF NOT EXISTS (SELECT 1 FROM pg_sequences WHERE sequencename = 'teacher_course_seq') THEN
        CREATE SEQUENCE teacher_course_seq START WITH 1 INCREMENT BY 1;
    END IF;

    -- Ensure sequence ownership
    ALTER SEQUENCE teacher_course_seq OWNED BY teacher_courses.id;
END $$;

COMMIT;