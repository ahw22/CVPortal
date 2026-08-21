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

-- Leerer Lebenslauf fuer "muster", oeffentlich sichtbar (Testfall 5 und 6).
MERGE INTO curriculum_vitae (user_id, public_visible, last_updated)
KEY (user_id)
VALUES (
    '01a023c5-659b-7931-8ab0-828070640f4d',
    TRUE,
    CURRENT_TIMESTAMP
);