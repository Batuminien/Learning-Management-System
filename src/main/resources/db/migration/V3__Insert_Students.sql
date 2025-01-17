-- Insert Students with their class assignments
INSERT INTO app_users (id, username, name, surname, email, password, role, school_level, enabled,
                       student_phone, student_tc, student_birth_date, student_registration_date,
                       student_parent_name, student_parent_phone, class_id_student)
VALUES
    -- 9-A Students (2009 birth year)
    (nextval('app_users_seq'), 'student9a1', 'Emma', 'Davis', 'emma.davis@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234501', '12345678001', '2009-03-15', '2023-09-01', 'James Davis', '5551234502',
     (SELECT id FROM classes WHERE name = '9-A')),
    (nextval('app_users_seq'), 'student9a2', 'Lucas', 'Garcia', 'lucas.garcia@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234503', '12345678002', '2009-05-20', '2023-09-01', 'Maria Garcia', '5551234504',
     (SELECT id FROM classes WHERE name = '9-A')),
    (nextval('app_users_seq'), 'student9a3', 'Sophia', 'Martinez', 'sophia.martinez@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234505', '12345678003', '2009-07-10', '2023-09-01', 'Carlos Martinez', '5551234506',
     (SELECT id FROM classes WHERE name = '9-A')),

    -- 9-B Students
    (nextval('app_users_seq'), 'student9b1', 'Ethan', 'Anderson', 'ethan.anderson@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234507', '12345678004', '2009-04-25', '2023-09-01', 'Michael Anderson', '5551234508',
     (SELECT id FROM classes WHERE name = '9-B')),
    (nextval('app_users_seq'), 'student9b2', 'Olivia', 'Taylor', 'olivia.taylor@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234509', '12345678005', '2009-06-30', '2023-09-01', 'William Taylor', '5551234510',
     (SELECT id FROM classes WHERE name = '9-B')),
    (nextval('app_users_seq'), 'student9b3', 'Noah', 'Robinson', 'noah.robinson@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234511', '12345678006', '2009-08-15', '2023-09-01', 'Kevin Robinson', '5551234512',
     (SELECT id FROM classes WHERE name = '9-B')),

    -- 9-C Students
    (nextval('app_users_seq'), 'student9c1', 'Aiden', 'Thomas', 'aiden.thomas@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234513', '12345678007', '2009-08-12', '2023-09-01', 'Robert Thomas', '5551234514',
     (SELECT id FROM classes WHERE name = '9-C')),
    (nextval('app_users_seq'), 'student9c2', 'Isabella', 'White', 'isabella.white@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234515', '12345678008', '2009-09-05', '2023-09-01', 'Jennifer White', '5551234516',
     (SELECT id FROM classes WHERE name = '9-C')),
    (nextval('app_users_seq'), 'student9c3', 'Mason', 'King', 'mason.king@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234517', '12345678009', '2009-10-20', '2023-09-01', 'Charles King', '5551234518',
     (SELECT id FROM classes WHERE name = '9-C')),

    -- 10-A-MF Students (2008 birth year)
    (nextval('app_users_seq'), 'student10a1', 'Mason', 'Harris', 'mason.harris@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234519', '12345678010', '2008-02-18', '2023-09-01', 'Daniel Harris', '5551234520',
     (SELECT id FROM classes WHERE name = '10-A-MF')),
    (nextval('app_users_seq'), 'student10a2', 'Charlotte', 'Clark', 'charlotte.clark@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234521', '12345678011', '2008-04-22', '2023-09-01', 'Elizabeth Clark', '5551234522',
     (SELECT id FROM classes WHERE name = '10-A-MF')),
    (nextval('app_users_seq'), 'student10a3', 'James', 'Turner', 'james.turner@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234523', '12345678012', '2008-06-10', '2023-09-01', 'Margaret Turner', '5551234524',
     (SELECT id FROM classes WHERE name = '10-A-MF')),

    -- 10-B-MF Students
    (nextval('app_users_seq'), 'student10b1', 'Sebastian', 'Lewis', 'sebastian.lewis@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234525', '12345678013', '2008-06-14', '2023-09-01', 'Christopher Lewis', '5551234526',
     (SELECT id FROM classes WHERE name = '10-B-MF')),
    (nextval('app_users_seq'), 'student10b2', 'Amelia', 'Lee', 'amelia.lee@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234527', '12345678014', '2008-07-30', '2023-09-01', 'David Lee', '5551234528',
     (SELECT id FROM classes WHERE name = '10-B-MF')),
    (nextval('app_users_seq'), 'student10b3', 'Elijah', 'Baker', 'elijah.baker@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234529', '12345678015', '2008-09-05', '2023-09-01', 'Donald Baker', '5551234530',
     (SELECT id FROM classes WHERE name = '10-B-MF')),

    -- 10-C-TM Students
    (nextval('app_users_seq'), 'student10c1', 'Henry', 'Walker', 'henry.walker@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234531', '12345678016', '2008-08-25', '2023-09-01', 'Richard Walker', '5551234532',
     (SELECT id FROM classes WHERE name = '10-C-TM')),
    (nextval('app_users_seq'), 'student10c2', 'Victoria', 'Hall', 'victoria.hall@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234533', '12345678017', '2008-10-05', '2023-09-01', 'Patricia Hall', '5551234534',
     (SELECT id FROM classes WHERE name = '10-C-TM')),
    (nextval('app_users_seq'), 'student10c3', 'Benjamin', 'Young', 'benjamin.young@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234535', '12345678018', '2008-11-15', '2023-09-01', 'Steven Young', '5551234536',
     (SELECT id FROM classes WHERE name = '10-C-TM')),

    -- 11-A-MF Students (2007 birth year)
    (nextval('app_users_seq'), 'student11a1', 'Alice', 'Brown', 'alice.brown@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234537', '12345678019', '2007-01-15', '2023-09-01', 'Robert Brown', '5551234538',
     (SELECT id FROM classes WHERE name = '11-A-MF')),
    (nextval('app_users_seq'), 'student11a2', 'Jack', 'Young', 'jack.young@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234539', '12345678020', '2007-03-28', '2023-09-01', 'Thomas Young', '5551234540',
     (SELECT id FROM classes WHERE name = '11-A-MF')),
    (nextval('app_users_seq'), 'student11a3', 'Scarlett', 'Miller', 'scarlett.miller@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234541', '12345678021', '2007-05-20', '2023-09-01', 'Gregory Miller', '5551234542',
     (SELECT id FROM classes WHERE name = '11-A-MF')),

    -- 11-B-MF Students
    (nextval('app_users_seq'), 'student11b1', 'William', 'King', 'william.king@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234543', '12345678022', '2006-06-14', '2023-09-01', 'Andrew Adams', '5551234544',
     (SELECT id FROM classes WHERE name = '12-B-MF')),
    (nextval('app_users_seq'), 'student12b2', 'Ava', 'Phillips', 'ava.phillips@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234545', '12345678023', '2006-08-22', '2023-09-01', 'Michelle Phillips', '5551234546',
     (SELECT id FROM classes WHERE name = '12-B-MF')),

    -- 12-C-TM Students
    (nextval('app_users_seq'), 'student12c1', 'Oliver', 'Campbell', 'oliver.campbell@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234547', '12345678024', '2006-09-30', '2023-09-01', 'Edward Campbell', '5551234548',
     (SELECT id FROM classes WHERE name = '12-C-TM')),
    (nextval('app_users_seq'), 'student12c2', 'Emily', 'Ross', 'emily.ross@lms.com',
     '$2a$12$KI8ugVXiXKu6Q7VthcY2u.JGVmh0OQ6wtx6NnK31G1TnGbEbSTgzG', 'ROLE_STUDENT', 'HIGH_SCHOOL', true,
     '5551234549', '12345678025', '2006-11-15', '2023-09-01', 'Katherine Ross', '5551234550',
     (SELECT id FROM classes WHERE name = '12-C-TM'));

-- Populate class_students from existing relationships
INSERT INTO class_students (class_id, student_id)
SELECT class_id_student, id
FROM app_users
WHERE role = 'ROLE_STUDENT' AND class_id_student IS NOT NULL;