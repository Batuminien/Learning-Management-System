-- Insert System Users (Admin & Coordinator)
INSERT INTO app_users (id, username, name, surname, email, password, role) VALUES
                                                                               (nextval('app_users_seq'), 'admin', 'Admin', 'User', 'admin@lms.com',
                                                                                '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_ADMIN'),
                                                                               (nextval('app_users_seq'), 'coordinator', 'Jane', 'Coordinator', 'coordinator@lms.com',
                                                                                '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_COORDINATOR');

-- Insert Teachers
INSERT INTO app_users (id, username, name, surname, email, password, role,
                       teacher_phone, teacher_tc, teacher_birth_date) VALUES
                                                                          (nextval('app_users_seq'), 'teacher1', 'John', 'Smith', 'john.smith@lms.com',
                                                                           '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER',
                                                                           '5551112233', '12345678907', '1980-05-15'),
                                                                          (nextval('app_users_seq'), 'teacher2', 'Mary', 'Johnson', 'mary.johnson@lms.com',
                                                                           '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER',
                                                                           '5551112234', '12345678908', '1982-08-20');

-- Insert Classes
INSERT INTO classes (id, name, description, teacher_id) VALUES
                                                            (nextval('classes_id_seq'), '11-A-MF', '11-A MF',
                                                             (SELECT id FROM app_users WHERE username = 'teacher1')),
                                                            (nextval('classes_id_seq'), '11-B-TM', '11-B TM',
                                                             (SELECT id FROM app_users WHERE username = 'teacher2'));

-- Insert Students
INSERT INTO app_users (id, username, name, surname, email, password, role,
                       student_phone, student_tc, student_birth_date, student_registration_date,
                       student_parent_name, student_parent_phone, class_id_student) VALUES
                                                                                        (nextval('app_users_seq'), 'student1', 'Alice', 'Brown', 'alice.brown@lms.com',
                                                                                         '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT',
                                                                                         '5551234567', '12345678901', '2008-01-15', '2023-09-01', 'Robert Brown', '5551234568', 1),
                                                                                        (nextval('app_users_seq'), 'student2', 'Bob', 'Wilson', 'bob.wilson@lms.com',
                                                                                         '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT',
                                                                                         '5552345678', '12345678902', '2007-03-20', '2023-09-01', 'Sarah Wilson', '5552345679', 2);

-- Insert Courses
INSERT INTO courses (id, name, description, code, credits, teacher_id) VALUES
                                                                           (nextval('courses_seq'), 'AYT-Mathematics', 'Foundational mathematics course', 'MAT-1', 4,
                                                                            (SELECT id FROM app_users WHERE username = 'teacher1')),
                                                                           (nextval('courses_seq'), 'AYT-Physics', 'Introduction to physics', 'FIZ-1', 4,
                                                                            (SELECT id FROM app_users WHERE username = 'teacher2')),
                                                                           (nextval('courses_seq'), 'AYT-Literacy', 'Basic turkish literacy concepts', 'EDB-1', 4,
                                                                            (SELECT id FROM app_users WHERE username = 'teacher2'));

-- Insert initial relationships and remaining data in V3