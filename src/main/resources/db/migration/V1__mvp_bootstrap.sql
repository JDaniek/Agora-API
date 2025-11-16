-- =========================================================
-- Agora DB - SCRIPT DEFINITIVO (Corregido con \d local)
-- =========================================================
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- =========================================================
-- ROLES
-- (BIGSERIAL es bigint, coincide con \d local)
-- =========================================================
CREATE TABLE IF NOT EXISTS roles (
                                     id      BIGSERIAL PRIMARY KEY,
                                     name    TEXT NOT NULL UNIQUE CHECK (name IN ('student','professor','admin'))
    );

INSERT INTO roles (name) VALUES
                             ('student'), ('professor'), ('admin')
    ON CONFLICT (name) DO NOTHING;

-- =========================================================
-- NOTIFICATION TYPES (CATALOG)
-- (SERIAL es integer, coincide con \d local que usa 'nextval...id_seq')
-- =========================================================
CREATE TABLE IF NOT EXISTS notification_type_id (
                                                    id    SERIAL PRIMARY KEY,
                                                    name  TEXT NOT NULL UNIQUE
);

INSERT INTO notification_type_id (id, name) VALUES
                                                (1, 'contact_request'),
                                                (2, 'new_chat_message'),
                                                (3, 'class_enrollment_request'),
                                                (4, 'class_enrollment_approved'),
                                                (5, 'class_enrollment_declined')
    ON CONFLICT (id) DO NOTHING;

-- =========================================================
-- USER ACCOUNTS (CUENTA BÁSICA)
-- (Coincide con \d local)
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

-- (Sintaxis de índice corregida, sin los paréntesis extra)
CREATE INDEX IF NOT EXISTS idx_user_accounts_fullname_trgm
    ON user_accounts
    USING gin ( (first_name || ' ' || COALESCE(second_name,'') || ' ' || last_name) gin_trgm_ops );

-- =========================================================
-- PROFILES (DATOS DE PERFIL post-registro)
-- (Coincide con \d local 'profiles')
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

-- Índices (Coinciden con \d local 'profiles')
CREATE INDEX IF NOT EXISTS idx_profiles_state_code ON profiles (state_code);
CREATE INDEX IF NOT EXISTS idx_profiles_level      ON profiles (level);
CREATE INDEX IF NOT EXISTS idx_profiles_city_trgm  ON profiles USING gin (city gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_profiles_specialties_gin ON profiles USING gin (specialties);

-- =========================================================
-- SPECIALTIES (Materias)
-- (¡AÑADIDO! Basado en \d local, usamos SERIAL para 'id')
-- =========================================================
CREATE TABLE IF NOT EXISTS specialty (
                                         id          SERIAL PRIMARY KEY, -- SERIAL es 'integer'
                                         name        TEXT NOT NULL UNIQUE
);

-- =========================================================
-- USER_SPECIALTIES (Tabla de Unión)
-- (¡AÑADIDO! Basado en \d local, 'specialty_id' es INT)
-- =========================================================
CREATE TABLE IF NOT EXISTS user_specialties (
                                                user_id     BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    specialty_id INT NOT NULL REFERENCES specialty(id),
    PRIMARY KEY (user_id, specialty_id)
    );

-- =========================================================
-- CLASSES (EVENTOS)
-- (¡CORREGIDO! Cambiado 'specialty_tag' por 'specialty_id' INT)
-- =========================================================
CREATE TABLE IF NOT EXISTS classes (
                                       id                 BIGSERIAL PRIMARY KEY,
                                       tutor_id           BIGINT NOT NULL REFERENCES user_accounts(id),
    title              VARCHAR(50) NOT NULL,
    description        TEXT,
    class_date         DATE NOT NULL,
    capacity_per_slot  SMALLINT NOT NULL DEFAULT 1 CHECK (capacity_per_slot > 0),
    specialty_id       INT NOT NULL REFERENCES specialty(id), -- <-- CORREGIDO
    is_active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_classes_tutor_id      ON classes (tutor_id);
CREATE INDEX IF NOT EXISTS idx_classes_class_date    ON classes (class_date);
CREATE INDEX IF NOT EXISTS idx_classes_specialty_id  ON classes (specialty_id); -- <-- CORREGIDO

-- =========================================================
-- CLASS ENROLLMENTS (INSCRIPCIONES A EVENTOS)
-- (¡CORREGIDO! Añadido 'student_id')
-- =========================================================
CREATE TABLE IF NOT EXISTS class_enrollments (
                                                 user_id    BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    class_id   BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE, -- <-- AÑADIDO
    status     TEXT NOT NULL DEFAULT 'confirmed' CHECK (status IN ('pending','confirmed','canceled')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, class_id) -- (Tu \d local dice esto, aunque PK(student_id, class_id) tendría más sentido)
    );

CREATE INDEX IF NOT EXISTS idx_class_enrollments_class_id ON class_enrollments (class_id);
CREATE INDEX IF NOT EXISTS idx_class_enrollments_user_id  ON class_enrollments (user_id);

-- =========================================================
-- CHATS & MESSAGES
-- (Coincide con \d local)
-- =========================================================
CREATE TABLE IF NOT EXISTS chats (
                                     id          BIGSERIAL PRIMARY KEY,
                                     chat_name   TEXT,
                                     created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS chat_members (
                                            chat_id  BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    user_id  BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, user_id)
    );

CREATE TABLE IF NOT EXISTS chat_messages (
                                             message_id  BIGSERIAL PRIMARY KEY,
                                             chat_id     BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id   BIGINT NOT NULL REFERENCES user_accounts(id),
    body        TEXT NOT NULL,
    sent_at     TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_chat_messages_chat_id ON chat_messages (chat_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_id ON chat_messages (sender_id);

-- =========================================================
-- MEDIA (Para subida de fotos)
-- (¡AÑADIDO! Basado en \d local, usamos SERIAL para 'id')
-- =========================================================
CREATE TABLE IF NOT EXISTS media (
                                     id          SERIAL PRIMARY KEY,
                                     url         TEXT NOT NULL,
                                     uploader_id BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now() -- (Usamos TIMESTAMPTZ por consistencia)
    );

-- =========================================================
-- REVIEWS (Calificaciones)
-- (¡AÑADIDO! Basado en \d local)
-- =========================================================
CREATE TABLE IF NOT EXISTS reviews (
                                       id          BIGSERIAL PRIMARY KEY,
                                       student_id  BIGINT NOT NULL REFERENCES user_accounts(id),
    teacher_id  BIGINT NOT NULL REFERENCES user_accounts(id),
    rating      INT NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- =========================================================
-- NOTIFICATIONS
-- (Corregido para usar SERIAL y coincidir con \d local)
-- =========================================================
CREATE TABLE IF NOT EXISTS notifications (
                                             id                     SERIAL PRIMARY KEY,
                                             recipient_id           BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    sender_id              BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    notification_type_id   INT NOT NULL REFERENCES notification_type_id(id),
    chat_id                BIGINT NULL REFERENCES chats(id) ON DELETE CASCADE,
    message                TEXT,
    status                 VARCHAR(20) NOT NULL DEFAULT 'pending'
    CHECK (status IN ('pending', 'read', 'unread', 'accepted', 'declined', 'archived')),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_id ON notifications (recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_sender_id ON notifications (sender_id);

-- =========================================================
-- VISTA: DOCENTES ALEATORIOS
-- (Sin cambios)
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

-- Comentarios (Actualizados)
COMMENT ON TABLE specialty IS 'Catálogo de materias (ej. Matemáticas, Idiomas)';
COMMENT ON TABLE user_specialties IS 'Tabla de unión entre usuarios (profesores) y sus materias';
COMMENT ON TABLE classes IS 'Eventos. specialty_id reemplaza al specialty_tag de MVP.';
COMMENT ON TABLE media IS 'Almacena URLs de archivos subidos (ej. fotos de perfil)';
COMMENT ON TABLE reviews IS 'Calificaciones y comentarios de estudiantes a profesores.';