-- First, remove the existing foreign key constraint from announcements table
ALTER TABLE announcements DROP CONSTRAINT IF EXISTS announcements_class_id_fkey;

-- Remove the class_id column as it's no longer needed
ALTER TABLE announcements DROP COLUMN IF EXISTS class_id;

-- Create the join table for many-to-many relationship
CREATE TABLE IF NOT EXISTS announcement_class (
                                                  announcement_id BIGINT REFERENCES announcements(id) ON DELETE CASCADE,
                                                  class_id BIGINT REFERENCES classes(id) ON DELETE CASCADE,
                                                  PRIMARY KEY (announcement_id, class_id)
);

-- Now insert the sample announcement after the table is created
INSERT INTO announcements (id, title, content, created_at)
SELECT
    nextval('announcement_seq'),
    'Welcome to New Semester',
    'Welcome to the new semester! We hope you have a great learning experience.',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM announcements WHERE title = 'Welcome to New Semester'
);

-- Now we can safely insert into announcement_class
INSERT INTO announcement_class (announcement_id, class_id)
SELECT
    (SELECT id FROM announcements WHERE title = 'Welcome to New Semester'),
    (SELECT id FROM classes WHERE name = '11-A-MF')
WHERE NOT EXISTS (
    SELECT 1 FROM announcement_class
    WHERE announcement_id = (SELECT id FROM announcements WHERE title = 'Welcome to New Semester')
      AND class_id = (SELECT id FROM classes WHERE name = '11-A-MF')
);

INSERT INTO announcement_class (announcement_id, class_id)
SELECT
    (SELECT id FROM announcements WHERE title = 'Welcome to New Semester'),
    (SELECT id FROM classes WHERE name = '11-B-TM')
WHERE NOT EXISTS (
    SELECT 1 FROM announcement_class
    WHERE announcement_id = (SELECT id FROM announcements WHERE title = 'Welcome to New Semester')
      AND class_id = (SELECT id FROM classes WHERE name = '11-B-TM')
);