-- First, disable foreign key checks to allow truncation
SET CONSTRAINTS ALL DEFERRED;

-- Clear existing data in correct order
TRUNCATE TABLE
    attendance,
    student_submissions,
    assignment_documents,
    assignments,
    teacher_course_classes,
    teacher_courses,
    course_classes,
    class_students,
    classes,
    courses,
    refresh_tokens,
    app_users,
    subject_results,
    student_exam_results,
    past_exams
CASCADE;

-- Reset sequences
ALTER SEQUENCE app_users_seq RESTART WITH 1;
ALTER SEQUENCE classes_id_seq RESTART WITH 1;
ALTER SEQUENCE assignments_seq RESTART WITH 1;
ALTER SEQUENCE courses_seq RESTART WITH 1;
ALTER SEQUENCE teacher_course_seq RESTART WITH 1;
ALTER SEQUENCE refresh_token_seq RESTART WITH 1;
ALTER SEQUENCE assignment_docs_seq RESTART WITH 1;
ALTER SEQUENCE submissions_seq RESTART WITH 1;
ALTER SEQUENCE attendance_id_seq RESTART WITH 1;
ALTER SEQUENCE past_exams_seq RESTART WITH 1;
ALTER SEQUENCE student_exam_results_seq RESTART WITH 1;
ALTER SEQUENCE subject_results_seq RESTART WITH 1;

-- Insert System Users (Admin & Coordinator)
INSERT INTO app_users (id, username, name, surname, email, password, role, profile_photo_url, profile_photo_filename) VALUES
                                                                                                                          (nextval('app_users_seq'), 'admin', 'Admin', 'User', 'admin@lms.com',
                                                                                                                           '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_ADMIN',
                                                                                                                           null, null),
                                                                                                                          (nextval('app_users_seq'), 'coordinator', 'Jane', 'Coordinator', 'coordinator@lms.com',
                                                                                                                           '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_COORDINATOR',
                                                                                                                           null, null);

-- Insert Teachers with TeacherDetails
INSERT INTO app_users (id, username, name, surname, email, password, role,
                       teacher_phone, teacher_tc, teacher_birth_date,
                       profile_photo_url, profile_photo_filename) VALUES
                                                                      (nextval('app_users_seq'), 'teacher1', 'John', 'Smith', 'john.smith@lms.com',
                                                                       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER',
                                                                       '5551112233', '12345678907', '1980-05-15',
                                                                       null, null),
                                                                      (nextval('app_users_seq'), 'teacher2', 'Mary', 'Johnson', 'mary.johnson@lms.com',
                                                                       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER',
                                                                       '5551112234', '12345678908', '1982-08-20',
                                                                       null, null);

-- Insert Classes
INSERT INTO classes (id, name, description) VALUES
                                                (nextval('classes_id_seq'), '11-A-MF', '11-A MF'),
                                                (nextval('classes_id_seq'), '11-B-TM', '11-B TM');

-- Insert Students with their class assignments
INSERT INTO app_users (id, username, name, surname, email, password, role,
                       student_phone, student_tc, student_birth_date, student_registration_date,
                       student_parent_name, student_parent_phone, class_id_student,
                       profile_photo_url, profile_photo_filename) VALUES
                                                                      (nextval('app_users_seq'), 'student1', 'Alice', 'Brown', 'alice.brown@lms.com',
                                                                       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT',
                                                                       '5551234567', '12345678901', '2008-01-15', '2023-09-01', 'Robert Brown', '5551234568',
                                                                       (SELECT id FROM classes WHERE name = '11-A-MF'),
                                                                       null, null),
                                                                      (nextval('app_users_seq'), 'student2', 'Bob', 'Wilson', 'bob.wilson@lms.com',
                                                                       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT',
                                                                       '5552345678', '12345678902', '2007-03-20', '2023-09-01', 'Sarah Wilson', '5552345679',
                                                                       (SELECT id FROM classes WHERE name = '11-B-TM'),
                                                                       null, null);

-- Insert Courses
INSERT INTO courses (id, name, description, code, credits) VALUES
                                                               (nextval('courses_seq'), 'AYT-Mathematics', 'Foundational mathematics course', 'MAT-1', 4),
                                                               (nextval('courses_seq'), 'AYT-Physics', 'Introduction to physics', 'FIZ-1', 4),
                                                               (nextval('courses_seq'), 'AYT-Literacy', 'Basic turkish literacy concepts', 'EDB-1', 4);

-- Insert TeacherCourses
INSERT INTO teacher_courses (id, teacher_id, course_id) VALUES
                                                            (nextval('teacher_course_seq'),
                                                             (SELECT id FROM app_users WHERE username = 'teacher1'),
                                                             (SELECT id FROM courses WHERE code = 'MAT-1')),
                                                            (nextval('teacher_course_seq'),
                                                             (SELECT id FROM app_users WHERE username = 'teacher2'),
                                                             (SELECT id FROM courses WHERE code = 'FIZ-1')),
                                                            (nextval('teacher_course_seq'),
                                                             (SELECT id FROM app_users WHERE username = 'teacher2'),
                                                             (SELECT id FROM courses WHERE code = 'EDB-1'));

-- Link Classes with Courses
INSERT INTO course_classes (course_id, class_id) VALUES
                                                     ((SELECT id FROM courses WHERE code = 'MAT-1'),
                                                      (SELECT id FROM classes WHERE name = '11-A-MF')),
                                                     ((SELECT id FROM courses WHERE code = 'FIZ-1'),
                                                      (SELECT id FROM classes WHERE name = '11-B-TM')),
                                                     ((SELECT id FROM courses WHERE code = 'EDB-1'),
                                                      (SELECT id FROM classes WHERE name = '11-A-MF'));

-- Link TeacherCourses with Classes
INSERT INTO teacher_course_classes (teacher_course_id, class_id) VALUES
                                                                     ((SELECT tc.id FROM teacher_courses tc
                                                                                             JOIN app_users au ON tc.teacher_id = au.id
                                                                                             JOIN courses c ON tc.course_id = c.id
                                                                       WHERE au.username = 'teacher1' AND c.code = 'MAT-1'),
                                                                      (SELECT id FROM classes WHERE name = '11-A-MF')),
                                                                     ((SELECT tc.id FROM teacher_courses tc
                                                                                             JOIN app_users au ON tc.teacher_id = au.id
                                                                                             JOIN courses c ON tc.course_id = c.id
                                                                       WHERE au.username = 'teacher2' AND c.code = 'FIZ-1'),
                                                                      (SELECT id FROM classes WHERE name = '11-B-TM'));

-- Populate it from existing relationships
INSERT INTO class_students (class_id, student_id)
SELECT class_id_student, id
FROM app_users
WHERE role = 'ROLE_STUDENT' AND class_id_student IS NOT NULL;

-- Create Assignments
INSERT INTO assignments (id, title, description, due_date, assigned_by_teacher_id,
                         last_modified_by_id, class_id, course_id, teacher_course_id,
                         assignment_date, last_modified_date) VALUES
                                                                  (nextval('assignments_seq'), 'Math Homework 1', 'Complete exercises 1-10', CURRENT_DATE + INTERVAL '7 days',
                                                                   (SELECT id FROM app_users WHERE username = 'teacher1'),
                                                                   (SELECT id FROM app_users WHERE username = 'teacher1'),
                                                                   (SELECT id FROM classes WHERE name = '11-A-MF'),
                                                                   (SELECT id FROM courses WHERE code = 'MAT-1'),
                                                                   (SELECT tc.id FROM teacher_courses tc
                                                                                          JOIN app_users au ON tc.teacher_id = au.id
                                                                                          JOIN courses c ON tc.course_id = c.id
                                                                    WHERE au.username = 'teacher1' AND c.code = 'MAT-1'),
                                                                   CURRENT_DATE,
                                                                   CURRENT_DATE),
                                                                  (nextval('assignments_seq'), 'Literacy Report', 'Write report on turkish literacy', CURRENT_DATE + INTERVAL '10 days',
                                                                   (SELECT id FROM app_users WHERE username = 'teacher2'),
                                                                   (SELECT id FROM app_users WHERE username = 'teacher2'),
                                                                   (SELECT id FROM classes WHERE name = '11-B-TM'),
                                                                   (SELECT id FROM courses WHERE code = 'EDB-1'),
                                                                   (SELECT tc.id FROM teacher_courses tc
                                                                                          JOIN app_users au ON tc.teacher_id = au.id
                                                                                          JOIN courses c ON tc.course_id = c.id
                                                                    WHERE au.username = 'teacher2' AND c.code = 'EDB-1'),
                                                                   CURRENT_DATE,
                                                                   CURRENT_DATE);

-- Create Teacher Assignment Documents
INSERT INTO assignment_documents (id, file_name, file_path, upload_time, file_type, file_size,
                                  uploaded_by, assignment_id) VALUES
    (nextval('assignment_docs_seq'),
     'math_homework.pdf',
     'uploads/assignments/math_homework.pdf',
     '2024-11-24 09:00:00',
     'application/pdf',
     399516,
     (SELECT id FROM app_users WHERE username = 'teacher1'),
     (SELECT id FROM assignments WHERE title = 'Math Homework 1'));

-- Create Student Submissions
INSERT INTO student_submissions (id, student_id, status, assignment_id, submission_date, submission_comment) VALUES
    (nextval('submissions_seq'),
     (SELECT id FROM app_users WHERE username = 'student1'),
     'SUBMITTED',
     (SELECT id FROM assignments WHERE title = 'Math Homework 1'),
     '2024-11-25',
     'Here is my completed homework');

-- Insert Attendance Records
INSERT INTO attendance (id, student_id, date_a, attendance, comment, class_id, course_id) VALUES
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student1'),
     '2024-11-24',
     'PRESENT',
     'Participated actively in class',
     (SELECT id FROM classes WHERE name = '11-A-MF'),
     (SELECT id FROM courses WHERE code = 'MAT-1'));

-- Insert sample TYT exam
INSERT INTO past_exams (id, name, exam_type, overall_average) VALUES
    (nextval('past_exams_seq'), '2024 Ocak TYT Deneme', 'TYT', 65.75);

-- Insert student exam results for student1 (Alice Brown)
INSERT INTO student_exam_results (id, exam_id, student_id) VALUES
    (nextval('student_exam_results_seq'),
     (SELECT id FROM past_exams WHERE name = '2024 Ocak TYT Deneme'),
     (SELECT id FROM app_users WHERE username = 'student1'));

-- Insert subject results for student1's TYT exam
INSERT INTO subject_results (id, exam_result_id, subject_name, correct_answers, incorrect_answers, blank_answers, net_score) VALUES
                                                                                                                                 -- Turkish Language
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')),
                                                                                                                                  'Türkçe', 28, 8, 4, 26.0),
                                                                                                                                 -- Social Sciences (History, Geography, Religion, Philosophy)
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')),
                                                                                                                                  'Sosyal Bilimler', 15, 3, 2, 14.25),
                                                                                                                                 -- Basic Mathematics
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')),
                                                                                                                                  'Temel Matematik', 32, 4, 4, 31.0),
                                                                                                                                 -- Science (Physics, Chemistry, Biology)
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')),
                                                                                                                                  'Fen Bilimleri', 18, 2, 0, 17.5);

-- Insert student exam results for student2 (Bob Wilson)
INSERT INTO student_exam_results (id, exam_id, student_id) VALUES
    (nextval('student_exam_results_seq'),
     (SELECT id FROM past_exams WHERE name = '2024 Ocak TYT Deneme'),
     (SELECT id FROM app_users WHERE username = 'student2'));

-- Insert subject results for student2's TYT exam
INSERT INTO subject_results (id, exam_result_id, subject_name, correct_answers, incorrect_answers, blank_answers, net_score) VALUES
                                                                                                                                 -- Turkish Language
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student2')),
                                                                                                                                  'Türkçe', 25, 10, 5, 22.5),
                                                                                                                                 -- Social Sciences
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student2')),
                                                                                                                                  'Sosyal Bilimler', 12, 5, 3, 10.75),
                                                                                                                                 -- Basic Mathematics
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student2')),
                                                                                                                                  'Temel Matematik', 28, 8, 4, 26.0),
                                                                                                                                 -- Science
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student2')),
                                                                                                                                  'Fen Bilimleri', 16, 4, 0, 15.0);

-- Insert sample AYT exam
INSERT INTO past_exams (id, name, exam_type, overall_average) VALUES
    (nextval('past_exams_seq'), '2024 Ocak AYT Deneme', 'AYT', 58.25);

-- Insert student exam results for student1 (Alice Brown) - AYT
INSERT INTO student_exam_results (id, exam_id, student_id) VALUES
    (nextval('student_exam_results_seq'),
     (SELECT id FROM past_exams WHERE name = '2024 Ocak AYT Deneme'),
     (SELECT id FROM app_users WHERE username = 'student1'));

-- Insert subject results for student1's AYT exam (MF track)
INSERT INTO subject_results (id, exam_result_id, subject_name, correct_answers, incorrect_answers, blank_answers, net_score) VALUES
                                                                                                                                 -- Advanced Mathematics
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')
                                                                                                                                                                         AND exam_id = (SELECT id FROM past_exams WHERE name = '2024 Ocak AYT Deneme')),
                                                                                                                                  'İleri Matematik', 28, 8, 4, 26.0),
                                                                                                                                 -- Physics
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')
                                                                                                                                                                         AND exam_id = (SELECT id FROM past_exams WHERE name = '2024 Ocak AYT Deneme')),
                                                                                                                                  'Fizik', 10, 2, 1, 9.5),
                                                                                                                                 -- Chemistry
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')
                                                                                                                                                                         AND exam_id = (SELECT id FROM past_exams WHERE name = '2024 Ocak AYT Deneme')),
                                                                                                                                  'Kimya', 8, 3, 2, 7.25),
                                                                                                                                 -- Biology
                                                                                                                                 (nextval('subject_results_seq'),
                                                                                                                                  (SELECT id FROM student_exam_results WHERE student_id = (SELECT id FROM app_users WHERE username = 'student1')
                                                                                                                                                                         AND exam_id = (SELECT id FROM past_exams WHERE name = '2024 Ocak AYT Deneme')),
                                                                                                                                  'Biyoloji', 9, 2, 2, 8.5);

-- Update sequences to current max values
SELECT setval('app_users_seq', (SELECT MAX(id) FROM app_users));
SELECT setval('classes_id_seq', (SELECT MAX(id) FROM classes));
SELECT setval('assignments_seq', (SELECT MAX(id) FROM assignments));
SELECT setval('courses_seq', (SELECT MAX(id) FROM courses));
SELECT setval('teacher_course_seq', (SELECT MAX(id) FROM teacher_courses));
SELECT setval('refresh_token_seq', COALESCE((SELECT MAX(id) FROM refresh_tokens), 1));
SELECT setval('assignment_docs_seq', (SELECT MAX(id) FROM assignment_documents));
SELECT setval('submissions_seq', (SELECT MAX(id) FROM student_submissions));
SELECT setval('attendance_id_seq', (SELECT MAX(id) FROM attendance));
SELECT setval('past_exams_seq', (SELECT MAX(id) FROM past_exams));
SELECT setval('student_exam_results_seq', (SELECT MAX(id) FROM student_exam_results));
SELECT setval('subject_results_seq', (SELECT MAX(id) FROM subject_results));