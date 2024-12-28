-- V12__Fix_Teacher_Course_Classes_Table.sql

-- Create sequence if not exists
CREATE SEQUENCE IF NOT EXISTS teacher_course_seq START WITH 1 INCREMENT BY 1;

-- Create the join table cleanly
DROP TABLE IF EXISTS teacher_course_classes;

CREATE TABLE teacher_course_classes (
                                        teacher_course_id BIGINT NOT NULL,
                                        class_id BIGINT NOT NULL,
                                        CONSTRAINT pk_teacher_course_classes PRIMARY KEY (teacher_course_id, class_id),
                                        CONSTRAINT fk_teacher_course_ref
                                            FOREIGN KEY (teacher_course_id)
                                                REFERENCES teacher_courses(id)
                                                ON DELETE CASCADE,
                                        CONSTRAINT fk_class_ref
                                            FOREIGN KEY (class_id)
                                                REFERENCES classes(id)
                                                ON DELETE CASCADE
);

-- Add performance indexes
CREATE INDEX idx_tcc_teacher_course ON teacher_course_classes(teacher_course_id);
CREATE INDEX idx_tcc_class ON teacher_course_classes(class_id);

-- Insert existing relationships
INSERT INTO teacher_course_classes (teacher_course_id, class_id)
SELECT tc.id, c.id
FROM teacher_courses tc
         CROSS JOIN classes c
WHERE NOT EXISTS (
    SELECT 1
    FROM teacher_course_classes tcc
    WHERE tcc.teacher_course_id = tc.id
      AND tcc.class_id = c.id
);

-- Verify the relationships
SELECT EXISTS (
    SELECT 1
    FROM teacher_course_classes
    LIMIT 1
) as has_relationships;