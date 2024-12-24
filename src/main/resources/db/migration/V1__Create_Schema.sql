CREATE SEQUENCE IF NOT EXISTS app_users_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS classes_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS assignments_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS courses_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS refresh_token_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS assignment_docs_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS submissions_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS attendance_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS announcement_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS app_users (
                                         id BIGINT PRIMARY KEY,
                                         username VARCHAR(60) NOT NULL UNIQUE,
                                         name VARCHAR(50) NOT NULL,
                                         surname VARCHAR(50) NOT NULL,
                                         email VARCHAR(100) NOT NULL UNIQUE,
                                         password VARCHAR(100) NOT NULL,
                                         role VARCHAR(50) NOT NULL,
                                         student_phone VARCHAR(255),
                                         student_tc VARCHAR(255),
                                         student_birth_date DATE,
                                         student_registration_date DATE,
                                         student_parent_name VARCHAR(255),
                                         student_parent_phone VARCHAR(255),
                                         class_id_student BIGINT,
                                         teacher_phone VARCHAR(255),
                                         teacher_tc VARCHAR(255),
                                         teacher_birth_date DATE
);

CREATE TABLE IF NOT EXISTS classes (
                                       id BIGINT PRIMARY KEY,
                                       name VARCHAR(100) NOT NULL,
                                       description VARCHAR(500),
                                       teacher_id BIGINT NOT NULL REFERENCES app_users(id)
);

CREATE TABLE IF NOT EXISTS courses (
                                       id BIGINT PRIMARY KEY,
                                       name VARCHAR(255) NOT NULL,
                                       description TEXT,
                                       code VARCHAR(255) UNIQUE,
                                       credits INTEGER,
                                       teacher_id BIGINT REFERENCES app_users(id)
);

CREATE TABLE IF NOT EXISTS assignments (
                                           id BIGINT PRIMARY KEY,
                                           title VARCHAR(255) NOT NULL,
                                           description TEXT,
                                           due_date DATE NOT NULL,
                                           assigned_by_teacher_id BIGINT NOT NULL REFERENCES app_users(id),
                                           last_modified_by_id BIGINT NOT NULL REFERENCES app_users(id),
                                           class_id BIGINT NOT NULL REFERENCES classes(id),
                                           course_id BIGINT NOT NULL REFERENCES courses(id),
                                           assignment_date DATE NOT NULL,
                                           last_modified_date DATE NOT NULL,
                                           teacher_document_id BIGINT
);

CREATE TABLE IF NOT EXISTS assignment_documents (
                                                    id BIGINT PRIMARY KEY,
                                                    file_name VARCHAR(255) NOT NULL,
                                                    file_path VARCHAR(255) NOT NULL,
                                                    upload_time TIMESTAMP NOT NULL,
                                                    file_type VARCHAR(255),
                                                    file_size BIGINT,
                                                    uploaded_by BIGINT NOT NULL REFERENCES app_users(id),
                                                    assignment_id BIGINT NOT NULL REFERENCES assignments(id)
);

CREATE TABLE IF NOT EXISTS student_submissions (
                                                   id BIGINT PRIMARY KEY,
                                                   student_id BIGINT NOT NULL REFERENCES app_users(id),
                                                   status VARCHAR(50),
                                                   assignment_id BIGINT NOT NULL REFERENCES assignments(id),
                                                   document_id BIGINT REFERENCES assignment_documents(id),
                                                   submission_date TIMESTAMP,
                                                   submission_comment TEXT,
                                                   grade DOUBLE PRECISION,
                                                   feedback TEXT
);

CREATE TABLE IF NOT EXISTS attendance (
                                          id BIGINT PRIMARY KEY,
                                          student_id BIGINT NOT NULL REFERENCES app_users(id),
                                          date_a DATE NOT NULL,
                                          attendance VARCHAR(50) NOT NULL,
                                          comment TEXT,
                                          class_id BIGINT REFERENCES classes(id),
                                          course_id BIGINT REFERENCES courses(id)
);

CREATE TABLE IF NOT EXISTS class_courses (
                                             class_id BIGINT REFERENCES classes(id),
                                             course_id BIGINT REFERENCES courses(id),
                                             PRIMARY KEY (class_id, course_id)
);

CREATE TABLE IF NOT EXISTS teacher_classes (
                                               user_id BIGINT REFERENCES app_users(id),
                                               class_id BIGINT REFERENCES classes(id),
                                               PRIMARY KEY (user_id, class_id)
);

CREATE TABLE IF NOT EXISTS class_students (
                                              class_id BIGINT REFERENCES classes(id),
                                              student_id BIGINT REFERENCES app_users(id),
                                              PRIMARY KEY (class_id, student_id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id BIGINT PRIMARY KEY,
                                              user_id BIGINT REFERENCES app_users(id),
                                              token VARCHAR(255) NOT NULL UNIQUE,
                                              expiry_date TIMESTAMP NOT NULL,
                                              remember_me BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS device_tokens (
                                             id BIGSERIAL PRIMARY KEY,
                                             user_id BIGINT NOT NULL REFERENCES app_users(id),
                                             device_token VARCHAR(255) NOT NULL,
                                             device_type VARCHAR(50) NOT NULL,
                                             device_name VARCHAR(255),
                                             os_version VARCHAR(255),
                                             app_version VARCHAR(255),
                                             created_at TIMESTAMP NOT NULL,
                                             updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     token VARCHAR(255) NOT NULL UNIQUE,
                                                     user_id BIGINT NOT NULL REFERENCES app_users(id),
                                                     expiry_date TIMESTAMP NOT NULL,
                                                     used BOOLEAN NOT NULL,
                                                     created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS announcements (
                                             id BIGINT PRIMARY KEY,
                                             title VARCHAR(255),
                                             content TEXT,
                                             class_id BIGINT REFERENCES classes(id),
                                             created_at TIMESTAMP
);

-- V2__Insert_Initial_Data.sql will follow in the next update