-- Insert Attendance Records
INSERT INTO attendance (id, student_id, date_a, attendance, comment, class_id, course_id)
VALUES
    -- 9th Grade Attendance (9-A class, Mathematics)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student9a1'),
     '2024-11-24',
     'PRESENT',
     'Active participation in class discussion',
     (SELECT id FROM classes WHERE name = '9-A'),
     (SELECT id FROM courses WHERE code = 'MAT-9')),

    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student9a2'),
     '2024-11-24',
     'ABSENT',
     'Medical leave - provided doctors note',
     (SELECT id FROM classes WHERE name = '9-A'),
     (SELECT id FROM courses WHERE code = 'MAT-9')),

    -- 9th Grade Attendance (9-B class, Physics)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student9b1'),
     '2024-11-24',
     'PRESENT',
     'Completed lab experiment successfully',
     (SELECT id FROM classes WHERE name = '9-B'),
     (SELECT id FROM courses WHERE code = 'FIZ-9')),

    -- 10th Grade Attendance (10-A-MF class, Chemistry)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student10a1'),
     '2024-11-24',
     'PRESENT',
     'Good performance in group work',
     (SELECT id FROM classes WHERE name = '10-A-MF'),
     (SELECT id FROM courses WHERE code = 'KIM-10')),

    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student10a2'),
     '2024-11-24',
     'LATE',
     'Arrived 10 minutes late - traffic delay',
     (SELECT id FROM classes WHERE name = '10-A-MF'),
     (SELECT id FROM courses WHERE code = 'KIM-10')),

    -- 11th Grade Attendance (11-A-MF class, Advanced Mathematics)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student11a1'),
     '2024-11-24',
     'PRESENT',
     'Solved complex problems on the board',
     (SELECT id FROM classes WHERE name = '11-A-MF'),
     (SELECT id FROM courses WHERE code = 'MAT-11')),

    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student11a2'),
     '2024-11-24',
     'EXCUSED',
     'School sports competition',
     (SELECT id FROM classes WHERE name = '11-A-MF'),
     (SELECT id FROM courses WHERE code = 'MAT-11')),

    -- 11th Grade Attendance (11-B-MF class, Biology)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student11b1'),
     '2024-11-24',
     'PRESENT',
     'Excellent participation in lab work',
     (SELECT id FROM classes WHERE name = '11-B-MF'),
     (SELECT id FROM courses WHERE code = 'BIO-11')),

    -- 12th Grade Attendance (12-A-MF class, English)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student12a1'),
     '2024-11-24',
     'PRESENT',
     'Led class presentation effectively',
     (SELECT id FROM classes WHERE name = '12-A-MF'),
     (SELECT id FROM courses WHERE code = 'ENG-11')),

    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student12a2'),
     '2024-11-24',
     'ABSENT',
     'No notification received',
     (SELECT id FROM classes WHERE name = '12-A-MF'),
     (SELECT id FROM courses WHERE code = 'ENG-11'));

-- Add attendance records for another day
INSERT INTO attendance (id, student_id, date_a, attendance, comment, class_id, course_id)
VALUES
    -- 9th Grade Attendance (9-A class, Turkish)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student9a1'),
     '2024-11-25',
     'PRESENT',
     'Participated in literature discussion',
     (SELECT id FROM classes WHERE name = '9-A'),
     (SELECT id FROM courses WHERE code = 'TUR-9')),

    -- 10th Grade Attendance (10-B-MF class, Physics)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student10b1'),
     '2024-11-25',
     'PRESENT',
     'Completed physics experiment successfully',
     (SELECT id FROM classes WHERE name = '10-B-MF'),
     (SELECT id FROM courses WHERE code = 'FIZ-10')),

    -- 11th Grade Attendance (11-C-TM class, History)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student11c1'),
     '2024-11-25',
     'PRESENT',
     'Good contribution to class debate',
     (SELECT id FROM classes WHERE name = '11-C-TM'),
     (SELECT id FROM courses WHERE code = 'TAR-11')),

    -- 12th Grade Attendance (12-B-MF class, Mathematics)
    (nextval('attendance_id_seq'),
     (SELECT id FROM app_users WHERE username = 'student12b1'),
     '2024-11-25',
     'PRESENT',
     'Helped other students with problems',
     (SELECT id FROM classes WHERE name = '12-B-MF'),
     (SELECT id FROM courses WHERE code = 'MAT-12'));