-- Create sequences first
CREATE SEQUENCE IF NOT EXISTS app_users_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS classes_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS assignments_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS courses_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS teacher_course_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS refresh_token_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS assignment_docs_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS submissions_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS attendance_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS past_exams_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS student_exam_results_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS subject_results_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS course_schedules_seq START WITH 1 INCREMENT BY 1;

-- Create enum types
CREATE TYPE role_enum AS ENUM ('ROLE_ADMIN', 'ROLE_COORDINATOR', 'ROLE_TEACHER', 'ROLE_STUDENT');
CREATE TYPE school_level_enum AS ENUM ('PRIMARY_SCHOOL', 'MIDDLE_SCHOOL', 'HIGH_SCHOOL');
CREATE TYPE attendance_status_enum AS ENUM ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED');
CREATE TYPE assignment_status_enum AS ENUM ('ASSIGNED', 'SUBMITTED', 'GRADED', 'LATE', 'NOT_SUBMITTED');

-- Create tables
CREATE TABLE app_users (
                           id BIGINT PRIMARY KEY,
                           username VARCHAR(50) UNIQUE NOT NULL,
                           name VARCHAR(50) NOT NULL,
                           surname VARCHAR(50) NOT NULL,
                           email VARCHAR(100) UNIQUE NOT NULL,
                           password VARCHAR(100) NOT NULL,
                           role role_enum NOT NULL,
                           school_level school_level_enum,
                           enabled BOOLEAN DEFAULT true,
                           teacher_phone VARCHAR(20),
                           teacher_tc VARCHAR(11),
                           teacher_birth_date DATE,
                           student_phone VARCHAR(20),
                           student_tc VARCHAR(11),
                           student_birth_date DATE,
                           student_registration_date DATE,
                           student_parent_name VARCHAR(100),
                           student_parent_phone VARCHAR(20),
                           class_id_student BIGINT
);

CREATE TABLE classes (
                         id BIGINT PRIMARY KEY,
                         name VARCHAR(50) UNIQUE NOT NULL,
                         description VARCHAR(255)
);

ALTER TABLE app_users
    ADD CONSTRAINT fk_student_class
        FOREIGN KEY (class_id_student) REFERENCES classes(id);

CREATE TABLE courses (
                         id BIGINT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         description TEXT,
                         code VARCHAR(20) UNIQUE NOT NULL,
                         credits INTEGER NOT NULL
);

CREATE TABLE teacher_courses (
                                 id BIGINT PRIMARY KEY,
                                 teacher_id BIGINT REFERENCES app_users(id),
                                 course_id BIGINT REFERENCES courses(id)
);

CREATE TABLE course_classes (
                                course_id BIGINT REFERENCES courses(id),
                                class_id BIGINT REFERENCES classes(id),
                                PRIMARY KEY (course_id, class_id)
);

CREATE TABLE class_students (
                                class_id BIGINT REFERENCES classes(id),
                                student_id BIGINT REFERENCES app_users(id),
                                PRIMARY KEY (class_id, student_id)
);

CREATE TABLE assignments (
                             id BIGINT PRIMARY KEY,
                             title VARCHAR(255) NOT NULL,
                             description TEXT,
                             due_date TIMESTAMP NOT NULL,
                             assigned_by_teacher_id BIGINT REFERENCES app_users(id),
                             last_modified_by_id BIGINT REFERENCES app_users(id),
                             class_id BIGINT REFERENCES classes(id),
                             course_id BIGINT REFERENCES courses(id),
                             teacher_course_id BIGINT REFERENCES teacher_courses(id),
                             assignment_date TIMESTAMP NOT NULL,
                             last_modified_date TIMESTAMP NOT NULL
);

CREATE TABLE assignment_documents (
                                      id BIGINT PRIMARY KEY,
                                      file_name VARCHAR(255) NOT NULL,
                                      file_path VARCHAR(255) NOT NULL,
                                      upload_time TIMESTAMP NOT NULL,
                                      file_type VARCHAR(100),
                                      file_size BIGINT,
                                      uploaded_by BIGINT REFERENCES app_users(id),
                                      assignment_id BIGINT REFERENCES assignments(id)
);

CREATE TABLE student_submissions (
                                     id BIGINT PRIMARY KEY,
                                     student_id BIGINT REFERENCES app_users(id),
                                     status assignment_status_enum NOT NULL,
                                     assignment_id BIGINT REFERENCES assignments(id),
                                     submission_date TIMESTAMP,
                                     submission_comment TEXT
);

CREATE TABLE attendance (
                            id BIGINT PRIMARY KEY,
                            student_id BIGINT REFERENCES app_users(id),
                            date_a DATE NOT NULL,
                            attendance attendance_status_enum NOT NULL,
                            comment TEXT,
                            class_id BIGINT REFERENCES classes(id),
                            course_id BIGINT REFERENCES courses(id)
);

CREATE TABLE past_exams (
                            id BIGINT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            exam_date TIMESTAMP NOT NULL,
                            exam_type VARCHAR(50) NOT NULL,
                            overall_average DECIMAL(5,2)
);

CREATE TABLE student_exam_results (
                                      id BIGINT PRIMARY KEY,
                                      exam_id BIGINT REFERENCES past_exams(id),
                                      student_id BIGINT REFERENCES app_users(id)
);

CREATE TABLE subject_results (
                                 id BIGINT PRIMARY KEY,
                                 exam_result_id BIGINT REFERENCES student_exam_results(id),
                                 subject_name VARCHAR(100) NOT NULL,
                                 correct_answers INTEGER NOT NULL,
                                 incorrect_answers INTEGER NOT NULL,
                                 blank_answers INTEGER NOT NULL,
                                 net_score DECIMAL(5,2) NOT NULL
);

CREATE TABLE refresh_tokens (
                                id BIGINT PRIMARY KEY,
                                token VARCHAR(255) UNIQUE NOT NULL,
                                user_id BIGINT REFERENCES app_users(id),
                                expiry_date TIMESTAMP NOT NULL
);

CREATE TABLE teacher_course_classes (
                                        teacher_course_id BIGINT REFERENCES teacher_courses(id),
                                        class_id BIGINT REFERENCES classes(id),
                                        PRIMARY KEY (teacher_course_id, class_id)
);

CREATE TABLE course_schedules (
                                  id BIGINT PRIMARY KEY,
                                  teacher_course_id BIGINT REFERENCES teacher_courses(id),
                                  class_id BIGINT REFERENCES classes(id),
                                  day_of_week VARCHAR(10) NOT NULL,
                                  start_time TIME NOT NULL,
                                  end_time TIME NOT NULL,
                                  location VARCHAR(100)
);