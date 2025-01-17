-- Create Assignments
INSERT INTO assignments (id, title, description, due_date, assigned_by_teacher_id,
                         last_modified_by_id, class_id, course_id, teacher_course_id,
                         assignment_date, last_modified_date)
VALUES
    -- 9th Grade Assignments
    (nextval('assignments_seq'), 'Basic Math Quiz', 'Complete exercises on basic algebra', CURRENT_DATE + INTERVAL '7 days',
     (SELECT id FROM app_users WHERE username = 'teacher1'),
     (SELECT id FROM app_users WHERE username = 'teacher1'),
     (SELECT id FROM classes WHERE name = '9-A'),
     (SELECT id FROM courses WHERE code = 'MAT-9'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher1' AND c.code = 'MAT-9'),
     CURRENT_DATE,
     CURRENT_DATE),

    (nextval('assignments_seq'), 'Physics Lab Report', 'Write report on force and motion experiment', CURRENT_DATE + INTERVAL '10 days',
     (SELECT id FROM app_users WHERE username = 'teacher3'),
     (SELECT id FROM app_users WHERE username = 'teacher3'),
     (SELECT id FROM classes WHERE name = '9-B'),
     (SELECT id FROM courses WHERE code = 'FIZ-9'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher3' AND c.code = 'FIZ-9'),
     CURRENT_DATE,
     CURRENT_DATE),

    -- 10th Grade Assignments
    (nextval('assignments_seq'), 'Chemistry Project', 'Research on periodic table trends', CURRENT_DATE + INTERVAL '14 days',
     (SELECT id FROM app_users WHERE username = 'teacher4'),
     (SELECT id FROM app_users WHERE username = 'teacher4'),
     (SELECT id FROM classes WHERE name = '10-A-MF'),
     (SELECT id FROM courses WHERE code = 'KIM-10'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher4' AND c.code = 'KIM-10'),
     CURRENT_DATE,
     CURRENT_DATE),

    (nextval('assignments_seq'), 'Turkish Literature Essay', 'Analysis of classic Turkish literature', CURRENT_DATE + INTERVAL '10 days',
     (SELECT id FROM app_users WHERE username = 'teacher6'),
     (SELECT id FROM app_users WHERE username = 'teacher6'),
     (SELECT id FROM classes WHERE name = '10-C-TM'),
     (SELECT id FROM courses WHERE code = 'TUR-10'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher6' AND c.code = 'TUR-10'),
     CURRENT_DATE,
     CURRENT_DATE),

    -- 11th Grade Assignments
    (nextval('assignments_seq'), 'Advanced Math Problems', 'Solve calculus problems 1-20', CURRENT_DATE + INTERVAL '7 days',
     (SELECT id FROM app_users WHERE username = 'teacher2'),
     (SELECT id FROM app_users WHERE username = 'teacher2'),
     (SELECT id FROM classes WHERE name = '11-A-MF'),
     (SELECT id FROM courses WHERE code = 'MAT-11'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher2' AND c.code = 'MAT-11'),
     CURRENT_DATE,
     CURRENT_DATE),

    (nextval('assignments_seq'), 'Biology Research Paper', 'Research on cellular biology', CURRENT_DATE + INTERVAL '14 days',
     (SELECT id FROM app_users WHERE username = 'teacher5'),
     (SELECT id FROM app_users WHERE username = 'teacher5'),
     (SELECT id FROM classes WHERE name = '11-B-MF'),
     (SELECT id FROM courses WHERE code = 'BIO-11'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher5' AND c.code = 'BIO-11'),
     CURRENT_DATE,
     CURRENT_DATE),

    -- 12th Grade Assignments
    (nextval('assignments_seq'), 'History Final Project', 'Research on Turkish history', CURRENT_DATE + INTERVAL '21 days',
     (SELECT id FROM app_users WHERE username = 'teacher8'),
     (SELECT id FROM app_users WHERE username = 'teacher8'),
     (SELECT id FROM classes WHERE name = '12-C-TM'),
     (SELECT id FROM courses WHERE code = 'TAR-11'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher8' AND c.code = 'TAR-11'),
     CURRENT_DATE,
     CURRENT_DATE),

    (nextval('assignments_seq'), 'English Presentation', 'Prepare presentation on global issues', CURRENT_DATE + INTERVAL '10 days',
     (SELECT id FROM app_users WHERE username = 'teacher7'),
     (SELECT id FROM app_users WHERE username = 'teacher7'),
     (SELECT id FROM classes WHERE name = '12-A-MF'),
     (SELECT id FROM courses WHERE code = 'ENG-11'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher7' AND c.code = 'ENG-11'),
     CURRENT_DATE,
     CURRENT_DATE);

-- Create Teacher Assignment Documents
INSERT INTO assignment_documents (id, file_name, file_path, upload_time, file_type, file_size,
                                  uploaded_by, assignment_id)
VALUES
    (nextval('assignment_docs_seq'),
     'basic_math_quiz.pdf',
     'uploads/assignments/basic_math_quiz.pdf',
     '2024-11-24 09:00:00',
     'application/pdf',
     250000,
     (SELECT id FROM app_users WHERE username = 'teacher1'),
     (SELECT id FROM assignments WHERE title = 'Basic Math Quiz')),

    (nextval('assignment_docs_seq'),
     'physics_lab_template.docx',
     'uploads/assignments/physics_lab_template.docx',
     '2024-11-24 10:00:00',
     'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
     150000,
     (SELECT id FROM app_users WHERE username = 'teacher3'),
     (SELECT id FROM assignments WHERE title = 'Physics Lab Report')),

    (nextval('assignment_docs_seq'),
     'chemistry_project_guide.pdf',
     'uploads/assignments/chemistry_project_guide.pdf',
     '2024-11-24 11:00:00',
     'application/pdf',
     350000,
     (SELECT id FROM app_users WHERE username = 'teacher4'),
     (SELECT id FROM assignments WHERE title = 'Chemistry Project')),

    (nextval('assignment_docs_seq'),
     'advanced_math_problems.pdf',
     'uploads/assignments/advanced_math_problems.pdf',
     '2024-11-24 12:00:00',
     'application/pdf',
     400000,
     (SELECT id FROM app_users WHERE username = 'teacher2'),
     (SELECT id FROM assignments WHERE title = 'Advanced Math Problems'));

-- Create Student Submissions
INSERT INTO student_submissions (id, student_id, status, assignment_id, submission_date, submission_comment)
VALUES
    (nextval('submissions_seq'),
     (SELECT id FROM app_users WHERE username = 'student9a1'),
     'SUBMITTED',
     (SELECT id FROM assignments WHERE title = 'Basic Math Quiz'),
     '2024-11-25',
     'Completed all exercises'),

    (nextval('submissions_seq'),
     (SELECT id FROM app_users WHERE username = 'student9b1'),
     'SUBMITTED',
     (SELECT id FROM assignments WHERE title = 'Physics Lab Report'),
     '2024-11-26',
     'Lab report completed with experiment results'),

    (nextval('submissions_seq'),
     (SELECT id FROM app_users WHERE username = 'student10a1'),
     'SUBMITTED',
     (SELECT id FROM assignments WHERE title = 'Chemistry Project'),
     '2024-11-27',
     'Project completed with research findings'),

    (nextval('submissions_seq'),
     (SELECT id FROM app_users WHERE username = 'student11a1'),
     'SUBMITTED',
     (SELECT id FROM assignments WHERE title = 'Advanced Math Problems'),
     '2024-11-28',
     'All problems solved with detailed solutions');