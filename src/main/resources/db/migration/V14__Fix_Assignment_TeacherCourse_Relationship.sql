-- V14__Fix_Assignment_TeacherCourse_Relationship.sql

DO $$
    BEGIN
        -- Drop existing column if it exists (to clean up any partial state)
        ALTER TABLE assignments
            DROP COLUMN IF EXISTS teacher_course_id;

        -- Add the column back properly
        ALTER TABLE assignments
            ADD COLUMN teacher_course_id BIGINT;

        -- Create index before adding foreign key
        CREATE INDEX IF NOT EXISTS idx_assignments_teacher_course
            ON assignments(teacher_course_id);

        -- Add foreign key constraint
        ALTER TABLE assignments
            ADD CONSTRAINT fk_assignments_teacher_course
                FOREIGN KEY (teacher_course_id)
                    REFERENCES teacher_courses(id);

        -- Populate existing assignments
        UPDATE assignments a
        SET teacher_course_id = (
            SELECT tc.id
            FROM teacher_courses tc
            WHERE tc.teacher_id = a.assigned_by_teacher_id
              AND tc.course_id = a.course_id
            LIMIT 1
        );

        -- Set NOT NULL constraint
        ALTER TABLE assignments
            ALTER COLUMN teacher_course_id SET NOT NULL;

    END $$;