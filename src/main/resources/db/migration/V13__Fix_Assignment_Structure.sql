-- V13__Fix_Assignment_Structure.sql

DO $$
    DECLARE
        null_assignments INTEGER;
    BEGIN
        -- First check if column exists
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = 'assignments'
              AND column_name = 'teacher_course_id'
        ) THEN
            -- Add the column if it doesn't exist
            ALTER TABLE assignments
                ADD COLUMN teacher_course_id BIGINT;
        END IF;

        -- Check for NULL values before update
        SELECT COUNT(*) INTO null_assignments
        FROM assignments
        WHERE teacher_course_id IS NULL;

        RAISE NOTICE 'Found % assignments with NULL teacher_course_id', null_assignments;

        -- Update assignments without teacher_course_id
        WITH assignment_updates AS (
            SELECT
                a.id as assignment_id,
                tc.id as teacher_course_id
            FROM assignments a
                     JOIN teacher_courses tc ON tc.teacher_id = a.assigned_by_teacher_id
                AND tc.course_id = a.course_id
            WHERE a.teacher_course_id IS NULL
        )
        UPDATE assignments a
        SET teacher_course_id = au.teacher_course_id
        FROM assignment_updates au
        WHERE a.id = au.assignment_id;

        -- Verify update
        SELECT COUNT(*) INTO null_assignments
        FROM assignments
        WHERE teacher_course_id IS NULL;

        IF null_assignments = 0 THEN
            -- Add foreign key constraint if it doesn't exist
            IF NOT EXISTS (
                SELECT 1
                FROM information_schema.table_constraints
                WHERE constraint_name = 'fk_assignment_teacher_course'
            ) THEN
                ALTER TABLE assignments
                    ADD CONSTRAINT fk_assignment_teacher_course
                        FOREIGN KEY (teacher_course_id)
                            REFERENCES teacher_courses(id);
            END IF;

            -- Make it NOT NULL
            ALTER TABLE assignments
                ALTER COLUMN teacher_course_id SET NOT NULL;

            RAISE NOTICE 'Successfully updated all assignments and set NOT NULL constraint';
        ELSE
            RAISE NOTICE '% assignments still have NULL teacher_course_id', null_assignments;

            -- Show problematic assignments
            RAISE NOTICE 'Problematic assignments:';
            FOR r IN (
                SELECT id, title, assigned_by_teacher_id, course_id
                FROM assignments
                WHERE teacher_course_id IS NULL
            ) LOOP
                    RAISE NOTICE 'Assignment ID: %, Title: %, Teacher: %, Course: %',
                        r.id, r.title, r.assigned_by_teacher_id, r.course_id;
                END LOOP;
        END IF;

    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Error in migration: %', SQLERRM;
    END $$;