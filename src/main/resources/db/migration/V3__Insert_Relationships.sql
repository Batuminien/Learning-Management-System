-- V3__Insert_Relationships.sql

-- Insert Classes if they don't exist
INSERT INTO classes (id, name, description, teacher_id)
SELECT nextval('classes_id_seq'), '11-A-MF', '11-A MF',
       (SELECT id FROM app_users WHERE username = 'teacher1')
WHERE NOT EXISTS (
    SELECT 1 FROM classes WHERE name = '11-A-MF'
);

INSERT INTO classes (id, name, description, teacher_id)
SELECT nextval('classes_id_seq'), '11-B-TM', '11-B TM',
       (SELECT id FROM app_users WHERE username = 'teacher2')
WHERE NOT EXISTS (
    SELECT 1 FROM classes WHERE name = '11-B-TM'
);

-- Insert Students if they don't exist
INSERT INTO app_users (id, username, name, surname, email, password, role,
                       student_phone, student_tc, student_birth_date, student_registration_date,
                       student_parent_name, student_parent_phone, class_id_student)
SELECT nextval('app_users_seq'), 'student1', 'Alice', 'Brown', 'alice.brown@lms.com',
       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT',
       '5551234567', '12345678901', '2008-01-15', '2023-09-01',
       'Robert Brown', '5551234568',
       (SELECT id FROM classes WHERE name = '11-A-MF')
WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE email = 'alice.brown@lms.com'
);

-- Insert more students if they don't exist
INSERT INTO app_users (id, username, name, surname, email, password, role,
                       student_phone, student_tc, student_birth_date, student_registration_date,
                       student_parent_name, student_parent_phone, class_id_student)
SELECT nextval('app_users_seq'), 'student2', 'Bob', 'Wilson', 'bob.wilson@lms.com',
       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT',
       '5552345678', '12345678902', '2007-03-20', '2023-09-01',
       'Sarah Wilson', '5552345679',
       (SELECT id FROM classes WHERE name = '11-B-TM')
WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE email = 'bob.wilson@lms.com'
);

-- Insert Courses if they don't exist
INSERT INTO courses (id, name, description, code, credits, teacher_id)
SELECT nextval('courses_seq'), 'AYT-Mathematics', 'Foundational mathematics course', 'MAT-1', 4,
       (SELECT id FROM app_users WHERE username = 'teacher1')
WHERE NOT EXISTS (
    SELECT 1 FROM courses WHERE code = 'MAT-1'
);

INSERT INTO courses (id, name, description, code, credits, teacher_id)
SELECT nextval('courses_seq'), 'AYT-Physics', 'Introduction to physics', 'FIZ-1', 4,
       (SELECT id FROM app_users WHERE username = 'teacher2')
WHERE NOT EXISTS (
    SELECT 1 FROM courses WHERE code = 'FIZ-1'
);

INSERT INTO courses (id, name, description, code, credits, teacher_id)
SELECT nextval('courses_seq'), 'AYT-Literacy', 'Basic turkish literacy concepts', 'EDB-1', 4,
       (SELECT id FROM app_users WHERE username = 'teacher2')
WHERE NOT EXISTS (
    SELECT 1 FROM courses WHERE code = 'EDB-1'
);

-- Create Assignments if they don't exist
INSERT INTO assignments (id, title, description, due_date, assigned_by_teacher_id,
                         last_modified_by_id, class_id, course_id, assignment_date, last_modified_date)
SELECT nextval('assignments_seq'), 'Math Homework 1', 'Complete exercises 1-10', CURRENT_DATE + INTERVAL '7 days',
       (SELECT id FROM app_users WHERE username = 'teacher1'),
       (SELECT id FROM app_users WHERE username = 'teacher1'),
       (SELECT id FROM classes WHERE name = '11-A-MF'),
       (SELECT id FROM courses WHERE code = 'MAT-1'),
       CURRENT_DATE,
       CURRENT_DATE
WHERE NOT EXISTS (
    SELECT 1 FROM assignments WHERE title = 'Math Homework 1'
);

INSERT INTO assignments (id, title, description, due_date, assigned_by_teacher_id,
                         last_modified_by_id, class_id, course_id, assignment_date, last_modified_date)
SELECT nextval('assignments_seq'), 'Literacy Report', 'Write report on turkish literacy', CURRENT_DATE + INTERVAL '10 days',
       (SELECT id FROM app_users WHERE username = 'teacher2'),
       (SELECT id FROM app_users WHERE username = 'teacher2'),
       (SELECT id FROM classes WHERE name = '11-B-TM'),
       (SELECT id FROM courses WHERE code = 'EDB-1'),
       CURRENT_DATE,
       CURRENT_DATE
WHERE NOT EXISTS (
    SELECT 1 FROM assignments WHERE title = 'Literacy Report'
);

-- Create Assignment Documents if they don't exist
INSERT INTO assignment_documents (id, file_name, file_path, upload_time, file_type, file_size,
                                  uploaded_by, assignment_id)
SELECT nextval('assignment_docs_seq'),
       'math_homework.pdf',
       'uploads/assignments/math_homework.pdf',
       CURRENT_TIMESTAMP,
       'application/pdf',
       399516,
       (SELECT id FROM app_users WHERE username = 'teacher1'),
       (SELECT id FROM assignments WHERE title = 'Math Homework 1')
WHERE NOT EXISTS (
    SELECT 1 FROM assignment_documents WHERE file_name = 'math_homework.pdf'
);

INSERT INTO assignment_documents (id, file_name, file_path, upload_time, file_type, file_size,
                                  uploaded_by, assignment_id)
SELECT nextval('assignment_docs_seq'),
       'literature_hw.pdf',
       'uploads/assignments/literature_hw.pdf',
       CURRENT_TIMESTAMP,
       'application/pdf',
       591446,
       (SELECT id FROM app_users WHERE username = 'teacher2'),
       (SELECT id FROM assignments WHERE title = 'Literacy Report')
WHERE NOT EXISTS (
    SELECT 1 FROM assignment_documents WHERE file_name = 'literature_hw.pdf'
);

-- Create Student Submissions if they don't exist
INSERT INTO student_submissions (id, student_id, status, assignment_id, submission_date, submission_comment)
SELECT nextval('submissions_seq'),
       (SELECT id FROM app_users WHERE username = 'student1'),
       'SUBMITTED',
       (SELECT id FROM assignments WHERE title = 'Math Homework 1'),
       CURRENT_TIMESTAMP,
       'Here is my completed homework'
WHERE NOT EXISTS (
    SELECT 1 FROM student_submissions
    WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')
      AND assignment_id = (SELECT id FROM assignments WHERE title = 'Math Homework 1')
);

-- Link Classes with Courses if not already linked
INSERT INTO class_courses (class_id, course_id)
SELECT
    (SELECT id FROM classes WHERE name = '11-A-MF'),
    (SELECT id FROM courses WHERE code = 'MAT-1')
WHERE NOT EXISTS (
    SELECT 1 FROM class_courses
    WHERE class_id = (SELECT id FROM classes WHERE name = '11-A-MF')
      AND course_id = (SELECT id FROM courses WHERE code = 'MAT-1')
);

INSERT INTO class_courses (class_id, course_id)
SELECT
    (SELECT id FROM classes WHERE name = '11-B-TM'),
    (SELECT id FROM courses WHERE code = 'FIZ-1')
WHERE NOT EXISTS (
    SELECT 1 FROM class_courses
    WHERE class_id = (SELECT id FROM classes WHERE name = '11-B-TM')
      AND course_id = (SELECT id FROM courses WHERE code = 'FIZ-1')
);

INSERT INTO class_courses (class_id, course_id)
SELECT
    (SELECT id FROM classes WHERE name = '11-A-MF'),
    (SELECT id FROM courses WHERE code = 'FIZ-1')
WHERE NOT EXISTS (
    SELECT 1 FROM class_courses
    WHERE class_id = (SELECT id FROM classes WHERE name = '11-A-MF')
      AND course_id = (SELECT id FROM courses WHERE code = 'FIZ-1')
);

INSERT INTO class_courses (class_id, course_id)
SELECT
    (SELECT id FROM classes WHERE name = '11-A-MF'),
    (SELECT id FROM courses WHERE code = 'EDB-1')
WHERE NOT EXISTS (
    SELECT 1 FROM class_courses
    WHERE class_id = (SELECT id FROM classes WHERE name = '11-A-MF')
      AND course_id = (SELECT id FROM courses WHERE code = 'EDB-1')
);

-- Insert Teacher-Class Relationships if they don't exist
INSERT INTO teacher_classes (user_id, class_id)
SELECT
    (SELECT id FROM app_users WHERE username = 'teacher1'),
    (SELECT id FROM classes WHERE name = '11-A-MF')
WHERE NOT EXISTS (
    SELECT 1 FROM teacher_classes
    WHERE user_id = (SELECT id FROM app_users WHERE username = 'teacher1')
      AND class_id = (SELECT id FROM classes WHERE name = '11-A-MF')
);

INSERT INTO teacher_classes (user_id, class_id)
SELECT
    (SELECT id FROM app_users WHERE username = 'teacher2'),
    (SELECT id FROM classes WHERE name = '11-B-TM')
WHERE NOT EXISTS (
    SELECT 1 FROM teacher_classes
    WHERE user_id = (SELECT id FROM app_users WHERE username = 'teacher2')
      AND class_id = (SELECT id FROM classes WHERE name = '11-B-TM')
);

-- Assign Students to Classes if not already assigned
INSERT INTO class_students (class_id, student_id)
SELECT
    (SELECT id FROM classes WHERE name = '11-A-MF'),
    (SELECT id FROM app_users WHERE username = 'student1')
WHERE NOT EXISTS (
    SELECT 1 FROM class_students
    WHERE class_id = (SELECT id FROM classes WHERE name = '11-A-MF')
      AND student_id = (SELECT id FROM app_users WHERE username = 'student1')
);

INSERT INTO class_students (class_id, student_id)
SELECT
    (SELECT id FROM classes WHERE name = '11-B-TM'),
    (SELECT id FROM app_users WHERE username = 'student2')
WHERE NOT EXISTS (
    SELECT 1 FROM class_students
    WHERE class_id = (SELECT id FROM classes WHERE name = '11-B-TM')
      AND student_id = (SELECT id FROM app_users WHERE username = 'student2')
);

-- Insert Attendance Records if they don't exist
INSERT INTO attendance (id, student_id, date_a, attendance, comment, class_id, course_id)
SELECT nextval('attendance_id_seq'),
       (SELECT id FROM app_users WHERE username = 'student1'),
       CURRENT_DATE,
       'PRESENT',
       'Participated actively in class',
       (SELECT id FROM classes WHERE name = '11-A-MF'),
       (SELECT id FROM courses WHERE code = 'MAT-1')
WHERE NOT EXISTS (
    SELECT 1 FROM attendance
    WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')
      AND date_a = CURRENT_DATE
);