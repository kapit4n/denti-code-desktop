-- Static reference catalogs. Idempotent enough for a fresh database.

INSERT INTO procedure_types (name, description, default_duration_minutes, standard_price, requires_tooth_specification, category, is_active) VALUES
    ('Routine cleaning',         'Prophylaxis and polishing',                   30, 60.00,  0, 'Preventive', 1),
    ('Periodic exam',            'Comprehensive oral evaluation',               20, 50.00,  0, 'Diagnostic', 1),
    ('Composite filling',        'Tooth-coloured restoration',                  45, 120.00, 1, 'Restorative', 1),
    ('Root canal — molar',       'Endodontic treatment',                        90, 650.00, 1, 'Endodontics', 1),
    ('Tooth extraction',         'Simple extraction',                           30, 180.00, 1, 'Surgery', 1),
    ('Crown placement',          'Single-unit ceramic crown',                   60, 850.00, 1, 'Prosthetics', 1),
    ('Teeth whitening',          'In-office whitening session',                 60, 300.00, 0, 'Cosmetic', 1),
    ('Orthodontic adjustment',   'Bracket / wire adjustment',                   30, 95.00,  0, 'Orthodontics', 1),
    ('Pediatric sealant',        'Sealant on permanent molars',                 20, 55.00,  1, 'Pediatric', 1),
    ('Periodontal scaling',      'Scaling and root planing per quadrant',       60, 220.00, 0, 'Periodontics', 1);

INSERT INTO treatment_facilities (facility_code, category_key, display_name, sort_order, is_active) VALUES
    ('GLOVES',         'PPE',         'Exam gloves',            10, 1),
    ('MASKS',          'PPE',         'Surgical masks',         20, 1),
    ('ANESTHESIA',     'INJECTION',   'Local anesthesia',       30, 1),
    ('SUTURES',        'SURGERY',     'Sutures',                40, 1),
    ('COMPOSITE',      'RESTORATIVE', 'Composite resin',        50, 1),
    ('ETCHING_GEL',    'RESTORATIVE', 'Etching gel',            60, 1),
    ('XRAY_FILM',      'IMAGING',     'X-ray film / sensor',    70, 1),
    ('FLUORIDE',       'PREVENTIVE',  'Fluoride varnish',       80, 1),
    ('SEALANT',        'PREVENTIVE',  'Pit & fissure sealant',  90, 1),
    ('CROWN_PORCELAIN','PROSTHETIC',  'Porcelain crown',       100, 1);

INSERT INTO consultories (name, short_code, sort_order, is_active) VALUES
    ('Consultory 1', 'C1', 10, 1),
    ('Consultory 2', 'C2', 20, 1);
