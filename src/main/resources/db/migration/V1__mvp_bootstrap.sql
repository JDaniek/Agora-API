-- =========================================================
-- Agora DB - MVP bootstrap (Auth + Profiles + Classes)
-- =========================================================
-- Requisitos opcionales (mejoran búsquedas):
--   EXTENSION pg_trgm   -> búsquedas por nombre (ILIKE) más rápidas
--   EXTENSION btree_gin -> combinaciones y arrays (no imprescindible)
--   EXTENSION uuid-ossp -> si después quieres UUIDs
-- Puedes habilitarlas aquí si lo deseas:
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- =========================================================
-- ROLES
-- =========================================================
CREATE TABLE IF NOT EXISTS roles (
  id      BIGSERIAL PRIMARY KEY,
  name    TEXT NOT NULL UNIQUE CHECK (name IN ('student','professor','admin'))
);

INSERT INTO roles (name) VALUES
  ('student'),
  ('professor'),
  ('admin')
ON CONFLICT (name) DO NOTHING;

-- =========================================================
-- USER ACCOUNTS (CUENTA BÁSICA)
-- email único, password_hash obligatorio, role_id FK
-- =========================================================
CREATE TABLE IF NOT EXISTS user_accounts (
  id             BIGSERIAL PRIMARY KEY,
  first_name     TEXT NOT NULL,
  second_name    TEXT NULL,
  last_name      TEXT NOT NULL,
  role_id        BIGINT NOT NULL REFERENCES roles(id),
  email          TEXT NOT NULL UNIQUE,
  password_hash  TEXT NOT NULL,
  is_active      BOOLEAN NOT NULL DEFAULT TRUE,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices útiles (búsquedas por nombre completo)
-- Para nombre, usaremos un índice trigram sobre una expresión que concatena nombres;
-- optimiza ILIKE '%texto%' en búsquedas de docentes por nombre.
CREATE INDEX IF NOT EXISTS idx_user_accounts_fullname_trgm
  ON user_accounts
  USING gin (( (first_name || ' ' || COALESCE(second_name,'') || ' ' || last_name) gin_trgm_ops ));

-- =========================================================
-- PROFILES (DATOS DE PERFIL post-registro)
-- Para MVP: catálogos (estado/nivel/especialidades) los controla el FRONT.
-- Guardamos:
--   - state_code TEXT  (clave del estado elegida del front)
--   - level TEXT       (p.ej. 'Bachiller', 'Universidad', etc.)
--   - specialties TEXT[] (lista de materias/áreas)
--   - city TEXT        (para filtrar por ciudad)
-- =========================================================
CREATE TABLE IF NOT EXISTS profiles (
  user_id      BIGINT PRIMARY KEY REFERENCES user_accounts(id) ON DELETE CASCADE,
  description  TEXT,
  photo_url    TEXT,
  state_code   TEXT,
  level        TEXT,
  specialties  TEXT[],
  city         TEXT
);

-- Índices para filtros
CREATE INDEX IF NOT EXISTS idx_profiles_state_code ON profiles (state_code);
CREATE INDEX IF NOT EXISTS idx_profiles_level      ON profiles (level);
CREATE INDEX IF NOT EXISTS idx_profiles_city_trgm  ON profiles USING gin (city gin_trgm_ops);
-- Búsquedas por especialidades (array) con GIN
CREATE INDEX IF NOT EXISTS idx_profiles_specialties_gin ON profiles USING gin (specialties);

-- =========================================================
-- CLASSES (EVENTOS)
-- Requisitos del MVP:
--   - evento con día de impartición (sin hora)
--   - tutor/profesor que la imparte
--   - opcional: specialty_id aún no normalizado en MVP: guardaremos un tag textual
--     para no frenar (puedes normalizarlo después).
-- =========================================================
CREATE TABLE IF NOT EXISTS classes (
  id                 BIGSERIAL PRIMARY KEY,
  tutor_id           BIGINT NOT NULL REFERENCES user_accounts(id),
  title              VARCHAR(50) NOT NULL,
  description        TEXT,
  class_date         DATE NOT NULL,                 -- sin hora (requisito)
  capacity_per_slot  SMALLINT NOT NULL DEFAULT 1 CHECK (capacity_per_slot > 0),
  specialty_tag      TEXT,                          -- MVP: etiqueta textual (desde front)
  is_active          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices para filtrar listados de clases
CREATE INDEX IF NOT EXISTS idx_classes_tutor_id      ON classes (tutor_id);
CREATE INDEX IF NOT EXISTS idx_classes_class_date    ON classes (class_date);
CREATE INDEX IF NOT EXISTS idx_classes_specialty_tag ON classes (specialty_tag);

-- =========================================================
-- CLASS ENROLLMENTS (INSCRIPCIONES A EVENTOS)
-- MVP: estado simple; confirmado por defecto
-- =========================================================
CREATE TABLE IF NOT EXISTS class_enrollments (
  user_id   BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
  class_id  BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  status    TEXT NOT NULL DEFAULT 'confirmed' CHECK (status IN ('pending','confirmed','canceled')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, class_id)
);

CREATE INDEX IF NOT EXISTS idx_class_enrollments_class_id ON class_enrollments (class_id);
CREATE INDEX IF NOT EXISTS idx_class_enrollments_user_id  ON class_enrollments (user_id);

-- =========================================================
-- VISTA: DOCENTES ALEATORIOS (para la landing de 20 cards)
-- Devuelve únicamente usuarios activos con rol 'professor' y su perfil (si existe).
-- NOTA: Para "20 aleatorios", usarás ORDER BY random() LIMIT 20 a la hora de consultar.
-- =========================================================
CREATE OR REPLACE VIEW v_professors AS
SELECT
  ua.id               AS user_id,
  ua.first_name,
  ua.second_name,
  ua.last_name,
  (ua.first_name || ' ' || COALESCE(ua.second_name,'') || ' ' || ua.last_name) AS full_name,
  ua.is_active,
  p.photo_url,
  p.description,
  p.state_code,
  p.level,
  p.specialties,
  p.city
FROM user_accounts ua
JOIN roles r ON r.id = ua.role_id AND r.name = 'professor'
LEFT JOIN profiles p ON p.user_id = ua.id
WHERE ua.is_active = TRUE;

-- Ejemplo de consulta para la landing (20 docentes aleatorios):
-- SELECT * FROM v_professors ORDER BY random() LIMIT 20;

-- Ejemplo de filtro por nombre/ciudad/especialidad/nivel:
-- SELECT *
-- FROM v_professors
-- WHERE full_name ILIKE '%' || :q || '%'
--   AND (:city IS NULL OR city ILIKE '%' || :city || '%')
--   AND (:level IS NULL OR level = :level)
--   AND (:spec IS NULL OR :spec = ANY (specialties));

-- =========================================================
-- Sugerencia de seed mínimo (opcional)
-- Un profesor de prueba y un alumno
-- =========================================================
-- INSERT INTO user_accounts (first_name, last_name, role_id, email, password_hash)
-- VALUES ('Ada','Lovelace', (SELECT id FROM roles WHERE name='professor'),
--         'ada@example.com', '$2a$12$hash_bcrypt_aca_va') -- reemplaza con hash real
-- ON CONFLICT (email) DO NOTHING;
--
-- INSERT INTO profiles (user_id, description, photo_url, state_code, level, specialties, city)
-- VALUES (
--   (SELECT id FROM user_accounts WHERE email='ada@example.com'),
--   'Clases de matemáticas y lógica.',
--   'https://cdn.example.com/ada.jpg',
--   'CDMX',
--   'Universidad',
--   ARRAY['Matemáticas','Lógica'],
--   'Ciudad de México'
-- );

-- =========================================================
-- Comentarios (ayuda futura)
-- =========================================================
COMMENT ON TABLE user_accounts IS 'Cuentas básicas (login). El perfil va en profiles.';
COMMENT ON COLUMN profiles.specialties IS 'Lista controlada por el frontend para MVP; normalizar en siguiente iteración.';
COMMENT ON COLUMN profiles.level IS 'Nivel educativo: controlado por el frontend (ej. Bachiller, Universidad).';
COMMENT ON COLUMN profiles.state_code IS 'Clave del estado de la república proporcionada por el frontend.';
COMMENT ON TABLE classes IS 'Eventos sin hora; fecha en class_date. specialty_tag es textual en MVP.';
COMMENT ON TABLE class_enrollments IS 'Inscripción a eventos; estado simple (pending/confirmed/canceled).';
