-- =============================================================
-- DATA SEED: Personal del Colegio Acrópolis
-- Ejecutado automáticamente al iniciar la aplicación.
-- MinutosContratoReemplazo = 0 por defecto (ajustar manualmente)
-- =============================================================

INSERT INTO personal (run, nombre, apellidos, cargo, email_institucional, minutos_contrato_reemplazo)
VALUES
    ('11111111-1', 'EUNICES', 'ALLENDE', 'Docente', 'allende.eunices@test.acropolis.cl', 0),
    ('11111111-2', 'PAULINA', 'ARAYA', 'Docente', 'araya.paulina@test.acropolis.cl', 0),
    ('11111111-3', 'PAULA', 'CONTRERAS', 'Docente', 'contreras.paula@test.acropolis.cl', 0),
    ('11111111-4', 'CAROLINA', 'DIAZ', 'Docente', 'diaz.carolina@test.acropolis.cl', 0),
    ('11111111-5', 'MARCELA', 'ESPINOZA', 'Docente', 'espinoza.marcela@test.acropolis.cl', 0),
    ('11111111-6', 'JORGE', 'FERNANDEZ', 'Docente', 'fernandez.jorge@test.acropolis.cl', 0),
    ('11111111-7', 'ANDREA', 'GONZALEZ', 'Docente', 'gonzalez.andrea@test.acropolis.cl', 0),
    ('11111111-8', 'CARLOS', 'HENRIQUEZ', 'Docente', 'henriquez.carlos@test.acropolis.cl', 0),
    ('11111111-9', 'ROBERTO', 'IBARRA', 'Docente', 'ibarra.roberto@test.acropolis.cl', 0),
    ('11111112-0', 'CLAUDIA', 'JARA', 'Docente', 'jara.claudia@test.acropolis.cl', 0),
    ('11111112-1', 'FRANCISCO', 'LAGOS', 'Docente', 'lagos.francisco@test.acropolis.cl', 0),
    ('11111112-2', 'MARIA', 'MARTINEZ', 'Docente', 'martinez.maria@test.acropolis.cl', 0),
    ('11111112-3', 'PEDRO', 'NAVARRO', 'Docente', 'navarro.pedro@test.acropolis.cl', 0),
    ('11111112-4', 'LUCIA', 'OLIVARES', 'Docente', 'olivares.lucia@test.acropolis.cl', 0),
    ('11111112-5', 'DANIEL', 'PEREZ', 'Docente', 'perez.daniel@test.acropolis.cl', 0),
    ('11111112-6', 'ROSA', 'QUIROGA', 'Docente', 'quiroga.rosa@test.acropolis.cl', 0),
    ('11111112-7', 'VICTOR', 'RIOS', 'Docente', 'rios.victor@test.acropolis.cl', 0),
    ('11111112-8', 'SANDRA', 'SOTO', 'Docente', 'soto.sandra@test.acropolis.cl', 0),
    ('11111112-9', 'JOSE', 'TORRES', 'Docente', 'torres.jose@test.acropolis.cl', 0),
    ('11111113-0', 'VERONICA', 'URRUTIA', 'Docente', 'urrutia.veronica@test.acropolis.cl', 0)
ON CONFLICT DO NOTHING;
