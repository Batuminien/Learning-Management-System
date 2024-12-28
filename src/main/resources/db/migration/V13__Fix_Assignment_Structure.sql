-- V13__Fix_Assignment_Structure.sql

-- First verify if column exists
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'assignments'
              AND column_name = 'teacher_course_id'
        ) THEN
            -- Add the column if it doesn't exist
            ALTER TABLE assignments
                ADD COLUMN teacher_course_id BIGINT;

            -- Add foreign key constraint
            ALTER TABLE assignments
                ADD CONSTRAINT fk_assignment_teacher_course
                    FOREIGN KEY (teacher_course_id)
                        REFERENCES teacher_courses(id);

            -- Update existing assignments with appropriate teacher_course_id
            UPDATE assignments a
            SET teacher_course_id = (
                SELECT tc.id
                FROM teacher_courses tc
                WHERE tc.teacher_id = a.assigned_by_teacher_id
                  AND tc.course_id = a.course_id
                LIMIT 1
            )
            WHERE teacher_course_id IS NULL;

            -- Make the column NOT NULL after data migration
            ALTER TABLE assignments
                ALTER COLUMN teacher_course_id SET NOT NULL;
        END IF;
    END $$;

-- Add index for better performance
CREATE INDEX IF NOT EXISTS idx_assignments_teacher_course
    ON assignments(teacher_course_id);