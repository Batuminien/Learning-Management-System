-- Insert System Users (Admin & Coordinator)
INSERT INTO app_users (id, username, name, surname, email, password, role, school_level, enabled)
VALUES
    (nextval('app_users_seq'), 'admin', 'Admin', 'User', 'admin@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_ADMIN', null, true),
    (nextval('app_users_seq'), 'coordinator', 'Jane', 'Coordinator', 'coordinator@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_COORDINATOR', 'HIGH_SCHOOL', true);

-- Insert Teachers
INSERT INTO app_users (id, username, name, surname, email, password, role, school_level, enabled,
                       teacher_phone, teacher_tc, teacher_birth_date)
VALUES
    -- Math Teachers
    (nextval('app_users_seq'), 'teacher1', 'John', 'Smith', 'john.smith@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER', 'HIGH_SCHOOL', true,
     '5551112233', '12345678907', '1980-05-15'),
    (nextval('app_users_seq'), 'teacher2', 'Mary', 'Johnson', 'mary.johnson@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER', 'HIGH_SCHOOL', true,
     '5551112234', '12345678908', '1982-08-20'),
    -- Science Teachers
    (nextval('app_users_seq'), 'teacher3', 'Elsa', 'Frozen', 'elsa.frozen@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER', 'HIGH_SCHOOL', true,
     '5551112235', '12345678909', '1981-07-07'),
    (nextval('app_users_seq'), 'teacher4', 'Robert', 'Physics', 'robert.physics@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER', 'HIGH_SCHOOL', true,
     '5551112236', '12345678910', '1979-03-22'),
    (nextval('app_users_seq'), 'teacher5', 'Sarah', 'Chemistry', 'sarah.chemistry@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER', 'HIGH_SCHOOL', true,
     '5551112237', '12345678911', '1983-11-15'),
    -- Language Teachers
    (nextval('app_users_seq'), 'teacher6', 'Emma', 'Turkish', 'emma.turkish@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER', 'HIGH_SCHOOL', true,
     '5551112238', '12345678912', '1984-06-28'),
    (nextval('app_users_seq'), 'teacher7', 'James', 'English', 'james.english@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER', 'HIGH_SCHOOL', true,
     '5551112239', '12345678913', '1981-09-14'),
    -- Social Sciences Teachers
    (nextval('app_users_seq'), 'teacher8', 'Michael', 'History', 'michael.history@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER', 'HIGH_SCHOOL', true,
     '5551112240', '12345678914', '1978-12-03'),
    (nextval('app_users_seq'), 'teacher9', 'Oliver', 'Geography', 'oliver.geography@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER', 'HIGH_SCHOOL', true,
     '5551112241', '12345678915', '1980-08-19');

-- Insert Classes
INSERT INTO classes (id, name, description)
VALUES
    -- 9th Grade Classes
    (nextval('classes_id_seq'), '9-A', '9th Grade Section A'),
    (nextval('classes_id_seq'), '9-B', '9th Grade Section B'),
    (nextval('classes_id_seq'), '9-C', '9th Grade Section C'),
    -- 10th Grade Classes
    (nextval('classes_id_seq'), '10-A-MF', '10th Grade MF Section A'),
    (nextval('classes_id_seq'), '10-B-MF', '10th Grade MF Section B'),
    (nextval('classes_id_seq'), '10-C-TM', '10th Grade TM Section'),
    -- 11th Grade Classes
    (nextval('classes_id_seq'), '11-A-MF', '11th Grade MF Section A'),
    (nextval('classes_id_seq'), '11-B-MF', '11th Grade MF Section B'),
    (nextval('classes_id_seq'), '11-C-TM', '11th Grade TM Section'),
    -- 12th Grade Classes
    (nextval('classes_id_seq'), '12-A-MF', '12th Grade MF Section A'),
    (nextval('classes_id_seq'), '12-B-MF', '12th Grade MF Section B'),
    (nextval('classes_id_seq'), '12-C-TM', '12th Grade TM Section');

-- Insert Courses
INSERT INTO courses (id, name, description, code, credits)
VALUES
    -- Mathematics Courses
    (nextval('courses_seq'), 'Basic Mathematics', 'Foundational mathematics course', 'MAT-9', 4),
    (nextval('courses_seq'), 'Advanced Mathematics', 'Advanced mathematics concepts', 'MAT-10', 4),
    (nextval('courses_seq'), 'AYT Mathematics', 'University preparation mathematics', 'MAT-11', 6),
    (nextval('courses_seq'), 'TYT Mathematics', 'Basic exam preparation mathematics', 'MAT-12', 6),

    -- Science Courses
    (nextval('courses_seq'), 'Physics-9', 'Introduction to physics', 'FIZ-9', 4),
    (nextval('courses_seq'), 'Physics-10', 'Intermediate physics', 'FIZ-10', 4),
    (nextval('courses_seq'), 'AYT Physics', 'Advanced physics for university exam', 'FIZ-11', 4),
    (nextval('courses_seq'), 'Chemistry-9', 'Basic chemistry concepts', 'KIM-9', 4),
    (nextval('courses_seq'), 'Chemistry-10', 'Intermediate chemistry', 'KIM-10', 4),
    (nextval('courses_seq'), 'AYT Chemistry', 'Advanced chemistry', 'KIM-11', 4),
    (nextval('courses_seq'), 'Biology-9', 'Introduction to biology', 'BIO-9', 4),
    (nextval('courses_seq'), 'Biology-10', 'Intermediate biology', 'BIO-10', 4),
    (nextval('courses_seq'), 'AYT Biology', 'Advanced biology concepts', 'BIO-11', 4),

    -- Language Courses
    (nextval('courses_seq'), 'Turkish Language-9', 'Basic turkish language', 'TUR-9', 4),
    (nextval('courses_seq'), 'Turkish Literature-10', 'Turkish literature and composition', 'TUR-10', 4),
    (nextval('courses_seq'), 'AYT Turkish Literature', 'Advanced turkish literature', 'TUR-11', 4),
    (nextval('courses_seq'), 'English-9', 'Basic english language', 'ENG-9', 4),
    (nextval('courses_seq'), 'English-10', 'Intermediate english', 'ENG-10', 4),
    (nextval('courses_seq'), 'English-11', 'Advanced english', 'ENG-11', 4),

    -- Social Sciences
    (nextval('courses_seq'), 'History-9', 'World history', 'TAR-9', 4),
    (nextval('courses_seq'), 'History-10', 'Turkish history', 'TAR-10', 4),
    (nextval('courses_seq'), 'AYT History', 'Advanced history concepts', 'TAR-11', 4),
    (nextval('courses_seq'), 'Geography-9', 'Basic geography', 'COG-9', 4),
    (nextval('courses_seq'), 'Geography-10', 'World geography', 'COG-10', 4),
    (nextval('courses_seq'), 'AYT Geography', 'Advanced geography concepts', 'COG-11', 4);

SELECT COUNT(*) FROM app_users WHERE role = 'ROLE_TEACHER';