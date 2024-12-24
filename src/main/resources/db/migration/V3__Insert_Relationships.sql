-- Create Assignments
INSERT INTO assignments (id, title, description, due_date, assigned_by_teacher_id,
                         last_modified_by_id, class_id, course_id, assignment_date, last_modified_date) VALUES
                                                                                                            (nextval('assignments_seq'), 'Math Homework 1', 'Complete exercises 1-10', CURRENT_DATE + INTERVAL '7 days',
                                                                                                             (SELECT id FROM app_users WHERE username = 'teacher1'),
                                                                                                             (SELECT id FROM app_users WHERE username = 'teacher1'),
                                                                                                             (SELECT id FROM classes WHERE name = '11-A-MF'),
                                                                                                             (SELECT id FROM courses WHERE code = 'MAT-1'),
                                                                                                             CURRENT_DATE,
                                                                                                             CURRENT_DATE),
                                                                                                            (nextval('assignments_seq'), 'Literacy Report', 'Write report on turkish literacy', CURRENT_DATE + INTERVAL '10 days',
                                                                                                             (SELECT id FROM app_users WHERE username = 'teacher2'),
                                                                                                             (SELECT id FROM app_users WHERE username = 'teacher2'),
                                                                                                             (SELECT id FROM classes WHERE name = '11-B-TM'),
                                                                                                             (SELECT id FROM courses WHERE code = 'EDB-1'),
                                                                                                             CURRENT_DATE,
                                                                                                             CURRENT_DATE);

-- Create Assignment Documents
INSERT INTO assignment_documents (id, file_name, file_path, upload_time, file_type, file_size,
                                  uploaded_by, assignment_id) VALUES
                                                                  (nextval('assignment_docs_seq'),
                                                                   'math_homework.pdf',
                                                                   'uploads/assignments/math_homework.pdf',
                                                                   CURRENT_TIMESTAMP,
                                                                   'application/pdf',
                                                                   399516,
                                                                   (SELECT id FROM app_users WHERE username = 'teacher1'),
                                                                   (SELECT id FROM assignments WHERE title = 'Math Homework 1')),
                                                                  (nextval('assignment_docs_seq'),
                                                                   'literature_hw.pdf',
                                                                   'uploads/assignments/literature_hw.pdf',
                                                                   CURRENT_TIMESTAMP,
                                                                   'application/pdf',
                                                                   591446,
                                                                   (SELECT id FROM app_users WHERE username = 'teacher2'),
                                                                   (SELECT id FROM assignments WHERE title = 'Literacy Report'));

-- Create Student Submissions
INSERT INTO student_submissions (id, student_id, status, assignment_id, submission_date, submission_comment) VALUES
    (nextval('submissions_seq'),
     (SELECT id FROM app_users WHERE username = 'student1'),
     'SUBMITTED',
     (SELECT id FROM assignments WHERE title = 'Math Homework 1'),
     CURRENT_TIMESTAMP,
     'Here is my completed homework');

-- Link Classes with Courses
INSERT INTO class_courses (class_id, course_id) VALUES
                                                    ((SELECT id FROM classes WHERE name = '11-A-MF'),
                                                     (SELECT id FROM courses WHERE code = 'MAT-1')),
                                                    ((SELECT id FROM classes WHERE name = '11-B-TM'),
                                                     (SELECT id FROM courses WHERE code = 'FIZ-1')),
                                                    ((SELECT id FROM classes WHERE name = '11-A-MF'),
                                                     (SELECT id FROM courses WHERE code = 'FIZ-1')),
                                                    ((SELECT id FROM classes WHERE name = '11-A-MF'),
                                                     (SELECT id FROM courses WHERE code = 'EDB-1'));

-- Insert Teacher-Class Relationships
INSERT INTO teacher_classes (user_id, class_id) VALUES
                                                    ((SELECT id FROM app_users WHERE username = 'teacher1'),
                                                     (SELECT id FROM classes WHERE name = '11-A-MF')),
                                                    ((SELECT id FROM app_users WHERE username = 'teacher2'),
                                                     (SELECT id FROM classes WHERE name = '11-B-TM'));

-- Assign Students to Classes
INSERT INTO class_students (class_id, student_id) VALUES
                                                      ((SELECT id FROM classes WHERE name = '11-A-MF'),
                                                       (SELECT id FROM app_users WHERE username = 'student1')),
                                                      ((SELECT id FROM classes WHERE name = '11-B-TM'),
                                                       (SELECT id FROM app_users WHERE username = 'student2'));

-- Insert Attendance Records
INSERT INTO attendance (id, student_id, date_a, attendance, comment, class_id, course_id) VALUES
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student1'),
     CURRENT_DATE,
     'PRESENT',
     'Participated actively in class',
     (SELECT id FROM classes WHERE name = '11-A-MF'),
     (SELECT id FROM courses WHERE code = 'MAT-1'));