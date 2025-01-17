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
SELECT setval('course_schedules_seq', (SELECT MAX(id) FROM course_schedules));