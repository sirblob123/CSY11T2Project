-- Makes the tables for databasemanager (initialize database)
CREATE TABLE IF NOT EXISTS climbs (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    date        TEXT    NOT NULL,
    grade       INTEGER NOT NULL,
    setting     TEXT    NOT NULL CHECK(setting IN ('Indoors', 'Outdoors')),
    location    TEXT    NOT NULL,
    climb_type  TEXT    NOT NULL,
    notes       TEXT
);

-- add climbs to database, used in database manager (addclimb)
INSERT INTO climbs (date, grade, setting, location, climb_type, notes)
VALUES (?, ?, ?, ?, ?, ?);

-- select all climbs, used in database manager (getallclimbs)
SELECT id, date, grade, setting, location, climb_type, notes
FROM climbs
ORDER BY date DESC;


-- sorting by grade from highest to lowest, used in database manager (getclimbssortedby)
SELECT id, date, grade, setting, location, climb_type, notes
FROM climbs
ORDER BY grade DESC, date DESC;


-- sorting by date from highest to lowest, used in database manager (getclimbssortedby)
SELECT id, date, grade, setting, location, climb_type, notes
FROM climbs
ORDER BY date DESC;

-- sorting by location from A to Z, used in database manager (getclimbssortedby)
SELECT id, date, grade, setting, location, climb_type, notes
FROM climbs
ORDER BY location ASC, date DESC;

-- filtering by setting, used in database manager (getclimbssortedby)
SELECT id, date, grade, setting, location, climb_type, notes
FROM climbs
WHERE setting = ?
ORDER BY date DESC;


-- filtering by climb type, used in database manager (getclimbssortedby)
SELECT id, date, grade, setting, location, climb_type, notes
FROM climbs
WHERE climb_type = ?
ORDER BY date DESC;


-- filtering by grade, used in database manager (getclimbssortedby)
SELECT id, date, grade, setting, location, climb_type, notes
FROM climbs
WHERE grade = ?
ORDER BY date DESC;


-- shows the stats for the climbs, used in database manager (getClimbStats)
SELECT
    COUNT(*)                        AS total_climbs,
    MAX(grade)                      AS highest_grade,
    MIN(grade)                      AS lowest_grade,
    ROUND(AVG(grade), 1)            AS average_grade,
    SUM(CASE WHEN setting = 'Indoors'  THEN 1 ELSE 0 END) AS indoor_count,
    SUM(CASE WHEN setting = 'Outdoors' THEN 1 ELSE 0 END) AS outdoor_count
FROM climbs;

-- deletes a climb, used in database manager (deleteclimb)
DELETE FROM climbs WHERE id = ?;

-- selects a climb by id, used in database manager (getClimbById)
SELECT id, date, grade, setting, location, climb_type, notes
FROM climbs
WHERE id = ?;
