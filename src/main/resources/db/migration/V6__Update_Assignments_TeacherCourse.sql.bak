-- V6__Fix_Assignments_Table.sql

-- First add the teacher_course_id column as nullable
ALTER TABLE assignments
    ADD COLUMN teacher_course_id BIGINT;

-- Create the foreign key constraint
ALTER TABLE assignments
    ADD CONSTRAINT fk_assignments_teacher_course
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
);

-- Make the column NOT NULL after data is populated
ALTER TABLE assignments
    ALTER COLUMN teacher_course_id SET NOT NULL;