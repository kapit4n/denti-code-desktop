-- Denti-Code Desktop — initial schema (SQLite).
-- Mirrors the entities under com.denticode.desktop.domain.model.

CREATE TABLE users (
    user_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    email            TEXT    NOT NULL UNIQUE,
    password_hash    TEXT    NOT NULL,
    display_name     TEXT,
    preferred_locale TEXT,
    avatar_url       TEXT,
    is_active        INTEGER NOT NULL DEFAULT 1,
    created_at       INTEGER NOT NULL DEFAULT ((CAST(strftime('%s','now') AS INTEGER) * 1000))
);

CREATE TABLE user_roles (
    user_id INTEGER NOT NULL,
    role    TEXT    NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE doctors (
    doctor_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER UNIQUE,
    first_name     TEXT NOT NULL,
    last_name      TEXT NOT NULL,
    email          TEXT NOT NULL,
    contact_phone  TEXT,
    license_number TEXT,
    office_room    TEXT,
    specialization TEXT,
    avatar_url     TEXT,
    is_active      INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
);

CREATE TABLE patients (
    patient_id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id                 INTEGER UNIQUE,
    first_name              TEXT NOT NULL,
    last_name               TEXT NOT NULL,
    date_of_birth           TEXT NOT NULL,
    gender                  TEXT,
    address                 TEXT,
    contact_phone           TEXT NOT NULL,
    email                   TEXT,
    avatar_url              TEXT,
    medical_history_summary TEXT,
    created_at              INTEGER NOT NULL DEFAULT ((CAST(strftime('%s','now') AS INTEGER) * 1000)),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE SET NULL
);

CREATE TABLE procedure_types (
    procedure_type_id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name                         TEXT NOT NULL,
    description                  TEXT,
    default_duration_minutes     INTEGER,
    standard_price               NUMERIC,
    requires_tooth_specification INTEGER NOT NULL DEFAULT 0,
    category                     TEXT,
    is_active                    INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE treatment_facilities (
    facility_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    facility_code TEXT NOT NULL UNIQUE,
    category_key  TEXT NOT NULL,
    display_name  TEXT NOT NULL,
    sort_order    INTEGER NOT NULL DEFAULT 0,
    is_active     INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE consultories (
    consultory_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL,
    short_code    TEXT,
    sort_order    INTEGER NOT NULL DEFAULT 0,
    is_active     INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE appointments (
    appointment_id              INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id                  INTEGER NOT NULL,
    primary_doctor_id           INTEGER NOT NULL,
    scheduled_at                TEXT    NOT NULL,
    estimated_duration_minutes  INTEGER,
    purpose                     TEXT,
    notes                       TEXT,
    status                      TEXT    NOT NULL,
    FOREIGN KEY (patient_id)       REFERENCES patients (patient_id) ON DELETE RESTRICT,
    FOREIGN KEY (primary_doctor_id) REFERENCES doctors  (doctor_id)  ON DELETE RESTRICT
);

CREATE INDEX idx_appointments_patient  ON appointments (patient_id);
CREATE INDEX idx_appointments_doctor   ON appointments (primary_doctor_id);
CREATE INDEX idx_appointments_when     ON appointments (scheduled_at);

CREATE TABLE performed_actions (
    performed_action_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    appointment_id        INTEGER NOT NULL,
    procedure_type_id     INTEGER NOT NULL,
    performing_doctor_id  INTEGER NOT NULL,
    action_at             TEXT    NOT NULL,
    tooth_involved        TEXT,
    surfaces_involved     TEXT,
    anesthesia_used       TEXT,
    facilities_used       TEXT,
    description_notes     TEXT,
    quantity              INTEGER NOT NULL DEFAULT 1,
    unit_price            NUMERIC NOT NULL DEFAULT 0,
    total_price           NUMERIC NOT NULL DEFAULT 0,
    FOREIGN KEY (appointment_id)       REFERENCES appointments     (appointment_id)    ON DELETE CASCADE,
    FOREIGN KEY (procedure_type_id)    REFERENCES procedure_types  (procedure_type_id) ON DELETE RESTRICT,
    FOREIGN KEY (performing_doctor_id) REFERENCES doctors          (doctor_id)         ON DELETE RESTRICT
);

CREATE INDEX idx_performed_actions_appointment ON performed_actions (appointment_id);

CREATE TABLE material_inventory_lines (
    line_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    consultory_id INTEGER NOT NULL,
    facility_id   INTEGER NOT NULL,
    quantity      INTEGER NOT NULL DEFAULT 0,
    UNIQUE (consultory_id, facility_id),
    FOREIGN KEY (consultory_id) REFERENCES consultories          (consultory_id) ON DELETE CASCADE,
    FOREIGN KEY (facility_id)   REFERENCES treatment_facilities  (facility_id)   ON DELETE CASCADE
);

CREATE TABLE inventory_movements (
    movement_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    consultory_id   INTEGER NOT NULL,
    facility_id     INTEGER NOT NULL,
    quantity_change INTEGER NOT NULL,
    type            TEXT    NOT NULL,
    note            TEXT,
    created_at      INTEGER NOT NULL DEFAULT ((CAST(strftime('%s','now') AS INTEGER) * 1000)),
    FOREIGN KEY (consultory_id) REFERENCES consultories          (consultory_id) ON DELETE CASCADE,
    FOREIGN KEY (facility_id)   REFERENCES treatment_facilities  (facility_id)   ON DELETE CASCADE
);

CREATE INDEX idx_inventory_movements_when ON inventory_movements (created_at);

CREATE TABLE payments (
    payment_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id     INTEGER NOT NULL,
    appointment_id INTEGER,
    amount         NUMERIC NOT NULL,
    method         TEXT,
    note           TEXT,
    paid_at        TEXT    NOT NULL,
    FOREIGN KEY (patient_id)     REFERENCES patients     (patient_id)     ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments (appointment_id) ON DELETE SET NULL
);

CREATE INDEX idx_payments_patient ON payments (patient_id);
