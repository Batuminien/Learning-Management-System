-- V12__Fix_Teacher_Course_Classes_Table.sql

-- Step 1: Create the base table
CREATE TABLE teacher_course_classes (
                                        teacher_course_id BIGINT NOT NULL,
                                        class_id BIGINT NOT NULL
);

-- Step 2: Add primary key
ALTER TABLE teacher_course_classes
    ADD CONSTRAINT pk_teacher_course_classes
        PRIMARY KEY (teacher_course_id, class_id);

-- Step 3: Add foreign key for teacher_courses
ALTER TABLE teacher_course_classes
    ADD CONSTRAINT fk_teacher_course_ref
        FOREIGN KEY (teacher_course_id)
            REFERENCES teacher_courses(id)
            ON DELETE CASCADE;

-- Step 4: Add foreign key for classes
ALTER TABLE teacher_course_classes
    ADD CONSTRAINT fk_class_ref
        FOREIGN KEY (class_id)
            REFERENCES classes(id)
            ON DELETE CASCADE;

-- Step 5: Add performance indexes
CREATE INDEX idx_tcc_teacher_course
    ON teacher_course_classes(teacher_course_id);
CREATE INDEX idx_tcc_class
    ON teacher_course_classes(class_id);

-- Step 6: Insert existing relationships
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