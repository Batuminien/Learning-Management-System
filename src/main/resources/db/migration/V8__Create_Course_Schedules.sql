-- Insert Course Schedules
INSERT INTO course_schedules (id, teacher_course_id, class_id, day_of_week, start_time, end_time, location)
VALUES
    -- 9th Grade Schedules
    -- 9-A Mathematics (Teacher1)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher1' AND c.code = 'MAT-9'),
     (SELECT id FROM classes WHERE name = '9-A'),
     'MONDAY',
     '08:30:00',
     '10:10:00',
     'Room 101'),

    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher1' AND c.code = 'MAT-9'),
     (SELECT id FROM classes WHERE name = '9-A'),
     'WEDNESDAY',
     '10:30:00',
     '12:10:00',
     'Room 101'),

    -- 9-A Physics (Teacher3)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher3' AND c.code = 'FIZ-9'),
     (SELECT id FROM classes WHERE name = '9-A'),
     'TUESDAY',
     '08:30:00',
     '10:10:00',
     'Physics Lab'),

    -- 9-A Chemistry (Teacher4)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher4' AND c.code = 'KIM-9'),
     (SELECT id FROM classes WHERE name = '9-A'),
     'THURSDAY',
     '10:30:00',
     '12:10:00',
     'Chemistry Lab'),

    -- 10th Grade Schedules
    -- 10-A-MF Advanced Mathematics (Teacher1)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher1' AND c.code = 'MAT-10'),
     (SELECT id FROM classes WHERE name = '10-A-MF'),
     'MONDAY',
     '10:30:00',
     '12:10:00',
     'Room 201'),

    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher1' AND c.code = 'MAT-10'),
     (SELECT id FROM classes WHERE name = '10-A-MF'),
     'THURSDAY',
     '08:30:00',
     '10:10:00',
     'Room 201'),

    -- 10-A-MF Biology (Teacher5)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher5' AND c.code = 'BIO-10'),
     (SELECT id FROM classes WHERE name = '10-A-MF'),
     'WEDNESDAY',
     '13:30:00',
     '15:10:00',
     'Biology Lab'),

    -- 11th Grade Schedules
    -- 11-A-MF Advanced Mathematics (Teacher2)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher2' AND c.code = 'MAT-11'),
     (SELECT id FROM classes WHERE name = '11-A-MF'),
     'MONDAY',
     '13:30:00',
     '15:10:00',
     'Room 301'),

    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher2' AND c.code = 'MAT-11'),
     (SELECT id FROM classes WHERE name = '11-A-MF'),
     'WEDNESDAY',
     '08:30:00',
     '10:10:00',
     'Room 301'),

    -- 11-A-MF Physics (Teacher3)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher3' AND c.code = 'FIZ-11'),
     (SELECT id FROM classes WHERE name = '11-A-MF'),
     'TUESDAY',
     '10:30:00',
     '12:10:00',
     'Physics Lab'),

    -- 11-C-TM Turkish Literature (Teacher6)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher6' AND c.code = 'TUR-11'),
     (SELECT id FROM classes WHERE name = '11-C-TM'),
     'MONDAY',
     '08:30:00',
     '10:10:00',
     'Room 302'),

    -- 12th Grade Schedules
    -- 12-A-MF Mathematics (Teacher2)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher2' AND c.code = 'MAT-12'),
     (SELECT id FROM classes WHERE name = '12-A-MF'),
     'TUESDAY',
     '13:30:00',
     '15:10:00',
     'Room 401'),

    -- 12-A-MF Chemistry (Teacher4)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher4' AND c.code = 'KIM-11'),
     (SELECT id FROM classes WHERE name = '12-A-MF'),
     'WEDNESDAY',
     '10:30:00',
     '12:10:00',
     'Chemistry Lab'),

    -- Common Courses
    -- English Classes (Teacher7)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher7' AND c.code = 'ENG-11'),
     (SELECT id FROM classes WHERE name = '11-A-MF'),
     'FRIDAY',
     '10:30:00',
     '12:10:00',
     'Room 303'),

    -- History Classes (Teacher8)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher8' AND c.code = 'TAR-11'),
     (SELECT id FROM classes WHERE name = '11-C-TM'),
     'THURSDAY',
     '13:30:00',
     '15:10:00',
     'Room 304'),

    -- Geography Classes (Teacher9)
    (nextval('course_schedules_seq'),
     (SELECT tc.id FROM teacher_courses tc
                            JOIN app_users au ON tc.teacher_id = au.id
                            JOIN courses c ON tc.course_id = c.id
      WHERE au.username = 'teacher9' AND c.code = 'COG-11'),
     (SELECT id FROM classes WHERE name = '11-C-TM'),
     'FRIDAY',
     '08:30:00',
     '10:10:00',
     'Room 305');