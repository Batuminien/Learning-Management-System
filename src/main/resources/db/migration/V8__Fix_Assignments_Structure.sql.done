-- V8__Fix_Assignments_Structure.sql

-- First add the teacher_course_id column as nullable if it doesn't exist
DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name='assignments' AND column_name='teacher_course_id') THEN
            ALTER TABLE assignments ADD COLUMN teacher_course_id BIGINT;
        END IF;
    END $$;

-- Update existing assignments with appropriate teacher_course_id
UPDATE assignments a
SET teacher_course_id = (
    SELECT tc.id
    FROM teacher_courses tc
    WHERE tc.teacher_id = a.assigned_by_teacher_id
      AND tc.course_id = a.course_id
    LIMIT 1
);

-- Drop the constraint if it exists
DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_name='fk_assignments_teacher_course') THEN
            ALTER TABLE assignments DROP CONSTRAINT fk_assignments_teacher_course;
        END IF;
    END $$;

-- Create the foreign key constraint
ALTER TABLE assignments
    ADD CONSTRAINT fk_assignments_teacher_course
        FOREIGN KEY (teacher_course_id)
            REFERENCES teacher_courses(id);

-- Make the column NOT NULL after data is populated
ALTER TABLE assignments
    ALTER COLUMN teacher_course_id SET NOT NULL;