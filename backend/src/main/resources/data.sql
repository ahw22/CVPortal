-- Testuser fuer Entwicklung

MERGE INTO users (id, username, email, password, role, active, created_at)
KEY (username)
VALUES (
    '01a023c5-659b-7a0b-a9c1-e9c15fcf0d03',
    'admin',
    'admin@cvportal.at',
    '$argon2id$v=19$m=16384,t=2,p=1$/DM/rhjjUPjl1n+O4XzjVg$R8X/a+AAAkcBECtnLE/6j33AOdxa+7b6S636a3mLSnQ',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP
);

MERGE INTO users (id, username, email, password, role, active, created_at)
KEY (username)
VALUES (
    '01a023c5-659b-7931-8ab0-828070640f4d',
    'muster',
    'muster@cvportal.at',
    '$argon2id$v=19$m=16384,t=2,p=1$Ok3eJTagQstyqYaecJyDrA$jWrMaxEqF5csthFVZEwfXwNExwwx37RdHP2iszFj3Co',
    'TEILNEHMER',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Leerer Lebenslauf fuer "muster", öffentlich sichtbar (Testfall 5 und 6).
MERGE INTO curriculum_vitae (user_id, public_visible, last_updated)
KEY (user_id)
VALUES (
    '01a023c5-659b-7931-8ab0-828070640f4d',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Demo-Teilnehmerin mit vollständigem Lebenslauf (100% Vollständigkeit).
-- Für die Visitenkarte, die öffentliche CV-Ansicht und die Beraterübersicht.
-- Passwort wie bei "muster": muster12345

MERGE INTO users (id, username, email, password, role, active, created_at)
KEY (username)
VALUES (
    '01a06bb9-a527-7c01-a4a1-2be49e5aae66',
    'beispiel',
    'maria.beispiel@cvportal.at',
    '$argon2id$v=19$m=16384,t=2,p=1$Ok3eJTagQstyqYaecJyDrA$jWrMaxEqF5csthFVZEwfXwNExwwx37RdHP2iszFj3Co',
    'TEILNEHMER',
    TRUE,
    CURRENT_TIMESTAMP
);

MERGE INTO curriculum_vitae (user_id, first_name, last_name, job_title, phone, address,
                             birth_date, summary, public_visible, last_updated)
KEY (user_id)
VALUES (
    '01a06bb9-a527-7c01-a4a1-2be49e5aae66',
    'Maria',
    'Beispiel',
    'Applikationsentwicklerin',
    '+43 660 1234567',
    'Beispielweg 12, 8020 Graz',
    DATE '1995-01-01',
    'Applikationsentwicklerin mit Schwerpunkt Java und Spring Boot. Umschulung über das BBRZ, davor fünf Jahre im technischen Support.',
    TRUE,
    CURRENT_TIMESTAMP
);

-- Kindtabellen haben keinen fachlichen Schlüssel fur MERGE. data.sql läuft bei
-- jedem Start, deshalb erst löschen, dann neu einfügen betrifft nur "beispiel".
DELETE FROM work_experience WHERE cv_id IN
    (SELECT cv.id FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel');
DELETE FROM education WHERE cv_id IN
    (SELECT cv.id FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel');
DELETE FROM skill WHERE cv_id IN
    (SELECT cv.id FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel');
DELETE FROM cv_language WHERE cv_id IN
    (SELECT cv.id FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel');

INSERT INTO work_experience (cv_id, company, position, start_date, end_date, description, sort_order)
SELECT cv.id, 'BBRZ Graz', 'Applikationsentwicklerin (Umschulung)',
       DATE '2025-09-01', NULL,
       'Java, Spring Boot, REST-Schnittstellen, Thymeleaf. Abschlussprojekt: webbasiertes Lebenslaufportal.', 0
FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel';

INSERT INTO work_experience (cv_id, company, position, start_date, end_date, description, sort_order)
SELECT cv.id, 'Technikhandel Steiermark GmbH', 'Mitarbeiterin technischer Support',
       DATE '2020-04-01', DATE '2025-06-30',
       'Erstlevel-Support, Ticketsystem, Schulung von Neukundinnen und Neukunden.', 1
FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel';

INSERT INTO education (cv_id, institution, degree, field_of_study, start_date, end_date, sort_order)
SELECT cv.id, 'BBRZ Österreich', 'Applikationsentwicklung - Coding', 'Softwareentwicklung',
       DATE '2025-09-01', NULL, 0
FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel';

INSERT INTO education (cv_id, institution, degree, field_of_study, start_date, end_date, sort_order)
SELECT cv.id, 'HTL Bulme Graz-Gösting', 'Matura', 'Elektronik und technische Informatik',
       DATE '2010-09-01', DATE '2015-06-30', 1
FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel';

INSERT INTO skill (cv_id, name, level, sort_order)
SELECT cv.id, 'Java', 'FORTGESCHRITTEN', 0
FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel';

INSERT INTO skill (cv_id, name, level, sort_order)
SELECT cv.id, 'Spring Boot', 'FORTGESCHRITTEN', 1
FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel';

INSERT INTO skill (cv_id, name, level, sort_order)
SELECT cv.id, 'SQL', 'GRUNDKENNTNISSE', 2
FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel';

INSERT INTO cv_language (cv_id, language_name, level, sort_order)
SELECT cv.id, 'Deutsch', 'MUTTERSPRACHE', 0
FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel';

INSERT INTO cv_language (cv_id, language_name, level, sort_order)
SELECT cv.id, 'Englisch', 'B2', 1
FROM curriculum_vitae cv JOIN users u ON u.id = cv.user_id WHERE u.username = 'beispiel';