-- V2__Insert_Initial_Data.sql
-- Insert System Users (Admin & Coordinator) if they don't exist
INSERT INTO app_users (id, username, name, surname, email, password, role)
SELECT nextval('app_users_seq'), 'admin', 'Admin', 'User', 'admin@lms.com',
       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE email = 'admin@lms.com'
);

INSERT INTO app_users (id, username, name, surname, email, password, role)
SELECT nextval('app_users_seq'), 'coordinator', 'Jane', 'Coordinator', 'coordinator@lms.com',
       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_COORDINATOR'
WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE email = 'coordinator@lms.com'
);

-- Insert Teachers if they don't exist
INSERT INTO app_users (id, username, name, surname, email, password, role,
                       teacher_phone, teacher_tc, teacher_birth_date)
SELECT nextval('app_users_seq'), 'teacher1', 'John', 'Smith', 'john.smith@lms.com',
       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER',
       '5551112233', '12345678907', '1980-05-15'
WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE email = 'john.smith@lms.com'
);

INSERT INTO app_users (id, username, name, surname, email, password, role,
                       teacher_phone, teacher_tc, teacher_birth_date)
SELECT nextval('app_users_seq'), 'teacher2', 'Mary', 'Johnson', 'mary.johnson@lms.com',
       '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_TEACHER',
       '5551112234', '12345678908', '1982-08-20'
WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE email = 'mary.johnson@lms.com'
);