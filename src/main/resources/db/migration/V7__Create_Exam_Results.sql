-- Insert Mock Exams
INSERT INTO past_exams (id, name, exam_date, exam_type, overall_average)
VALUES
    -- TYT Mock Exams
    (nextval('past_exams_seq'), '2024 Ekim TYT Deneme', '2023-10-15 09:00:00', 'TYT', 62.50),
    (nextval('past_exams_seq'), '2024 Kasım TYT Deneme', '2023-11-15 09:00:00', 'TYT', 64.25),
    (nextval('past_exams_seq'), '2024 Aralık TYT Deneme', '2023-12-15 09:00:00', 'TYT', 65.00),
    (nextval('past_exams_seq'), '2024 Ocak TYT Deneme', '2024-01-15 09:00:00', 'TYT', 65.75),

    -- AYT Mock Exams
    (nextval('past_exams_seq'), '2024 Ekim AYT Deneme', '2023-10-15 13:00:00', 'AYT', 55.50),
    (nextval('past_exams_seq'), '2024 Kasım AYT Deneme', '2023-11-15 13:00:00', 'AYT', 56.75),
    (nextval('past_exams_seq'), '2024 Aralık AYT Deneme', '2023-12-15 13:00:00', 'AYT', 57.50),
    (nextval('past_exams_seq'), '2024 Ocak AYT Deneme', '2024-01-15 13:00:00', 'AYT', 58.25);

-- Function to insert TYT results for a student
DO $$
    DECLARE
        exam_id_oct int;
        exam_id_nov int;
        exam_id_dec int;
        exam_id_jan int;
        student_result_id int;
    BEGIN
        -- Get exam IDs
        SELECT id INTO exam_id_oct FROM past_exams WHERE name = '2024 Ekim TYT Deneme';
        SELECT id INTO exam_id_nov FROM past_exams WHERE name = '2024 Kasım TYT Deneme';
        SELECT id INTO exam_id_dec FROM past_exams WHERE name = '2024 Aralık TYT Deneme';
        SELECT id INTO exam_id_jan FROM past_exams WHERE name = '2024 Ocak TYT Deneme';

        -- Insert results for student11a1 (MF Track)
        INSERT INTO student_exam_results (id, exam_id, student_id)
        VALUES (nextval('student_exam_results_seq'), exam_id_jan,
                (SELECT id FROM app_users WHERE username = 'student11a1'))
        RETURNING id INTO student_result_id;

        -- Insert subject results
        INSERT INTO subject_results (id, exam_result_id, subject_name, correct_answers, incorrect_answers, blank_answers, net_score)
        VALUES
            (nextval('subject_results_seq'), student_result_id, 'Türkçe', 28, 8, 4, 26.0),
            (nextval('subject_results_seq'), student_result_id, 'Sosyal Bilimler', 15, 3, 2, 14.25),
            (nextval('subject_results_seq'), student_result_id, 'Temel Matematik', 32, 4, 4, 31.0),
            (nextval('subject_results_seq'), student_result_id, 'Fen Bilimleri', 18, 2, 0, 17.5);

        -- Insert results for student11b1 (MF Track)
        INSERT INTO student_exam_results (id, exam_id, student_id)
        VALUES (nextval('student_exam_results_seq'), exam_id_jan,
                (SELECT id FROM app_users WHERE username = 'student11b1'))
        RETURNING id INTO student_result_id;

        INSERT INTO subject_results (id, exam_result_id, subject_name, correct_answers, incorrect_answers, blank_answers, net_score)
        VALUES
            (nextval('subject_results_seq'), student_result_id, 'Türkçe', 25, 10, 5, 22.5),
            (nextval('subject_results_seq'), student_result_id, 'Sosyal Bilimler', 12, 5, 3, 10.75),
            (nextval('subject_results_seq'), student_result_id, 'Temel Matematik', 28, 8, 4, 26.0),
            (nextval('subject_results_seq'), student_result_id, 'Fen Bilimleri', 16, 4, 0, 15.0);

        -- Insert results for student11c1 (TM Track)
        INSERT INTO student_exam_results (id, exam_id, student_id)
        VALUES (nextval('student_exam_results_seq'), exam_id_jan,
                (SELECT id FROM app_users WHERE username = 'student11c1'))
        RETURNING id INTO student_result_id;

        INSERT INTO subject_results (id, exam_result_id, subject_name, correct_answers, incorrect_answers, blank_answers, net_score)
        VALUES
            (nextval('subject_results_seq'), student_result_id, 'Türkçe', 30, 5, 5, 28.75),
            (nextval('subject_results_seq'), student_result_id, 'Sosyal Bilimler', 18, 2, 0, 17.5),
            (nextval('subject_results_seq'), student_result_id, 'Temel Matematik', 25, 10, 5, 22.5),
            (nextval('subject_results_seq'), student_result_id, 'Fen Bilimleri', 14, 6, 0, 12.5);

    END $$;

-- Function to insert AYT results for a student
DO $$
    DECLARE
        exam_id_oct int;
        exam_id_nov int;
        exam_id_dec int;
        exam_id_jan int;
        student_result_id int;
    BEGIN
        -- Get exam IDs
        SELECT id INTO exam_id_oct FROM past_exams WHERE name = '2024 Ekim AYT Deneme';
        SELECT id INTO exam_id_nov FROM past_exams WHERE name = '2024 Kasım AYT Deneme';
        SELECT id INTO exam_id_dec FROM past_exams WHERE name = '2024 Aralık AYT Deneme';
        SELECT id INTO exam_id_jan FROM past_exams WHERE name = '2024 Ocak AYT Deneme';

        -- Insert results for student11a1 (MF Track)
        INSERT INTO student_exam_results (id, exam_id, student_id)
        VALUES (nextval('student_exam_results_seq'), exam_id_jan,
                (SELECT id FROM app_users WHERE username = 'student11a1'))
        RETURNING id INTO student_result_id;

        -- Insert subject results for MF track
        INSERT INTO subject_results (id, exam_result_id, subject_name, correct_answers, incorrect_answers, blank_answers, net_score)
        VALUES
            (nextval('subject_results_seq'), student_result_id, 'İleri Matematik', 28, 8, 4, 26.0),
            (nextval('subject_results_seq'), student_result_id, 'Fizik', 10, 2, 1, 9.5),
            (nextval('subject_results_seq'), student_result_id, 'Kimya', 8, 3, 2, 7.25),
            (nextval('subject_results_seq'), student_result_id, 'Biyoloji', 9, 2, 2, 8.5);

        -- Insert results for student11b1 (MF Track)
        INSERT INTO student_exam_results (id, exam_id, student_id)
        VALUES (nextval('student_exam_results_seq'), exam_id_jan,
                (SELECT id FROM app_users WHERE username = 'student11b1'))
        RETURNING id INTO student_result_id;

        -- Insert subject results for MF track
        INSERT INTO subject_results (id, exam_result_id, subject_name, correct_answers, incorrect_answers, blank_answers, net_score)
        VALUES
            (nextval('subject_results_seq'), student_result_id, 'İleri Matematik', 25, 10, 5, 22.5),
            (nextval('subject_results_seq'), student_result_id, 'Fizik', 12, 3, 0, 11.25),
            (nextval('subject_results_seq'), student_result_id, 'Kimya', 10, 2, 1, 9.5),
            (nextval('subject_results_seq'), student_result_id, 'Biyoloji', 8, 4, 1, 7.0);

        -- Insert results for student11c1 (TM Track)
        INSERT INTO student_exam_results (id, exam_id, student_id)
        VALUES (nextval('student_exam_results_seq'), exam_id_jan,
                (SELECT id FROM app_users WHERE username = 'student11c1'))
        RETURNING id INTO student_result_id;

        -- Insert subject results for TM track
        INSERT INTO subject_results (id, exam_result_id, subject_name, correct_answers, incorrect_answers, blank_answers, net_score)
        VALUES
            (nextval('subject_results_seq'), student_result_id, 'İleri Matematik', 20, 12, 8, 17.0),
            (nextval('subject_results_seq'), student_result_id, 'Türk Dili ve Edebiyatı', 28, 6, 6, 26.5),
            (nextval('subject_results_seq'), student_result_id, 'Tarih', 15, 3, 2, 14.25),
            (nextval('subject_results_seq'), student_result_id, 'Coğrafya', 12, 4, 4, 11.0);

    END $$;