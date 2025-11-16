-- =========================================================
-- Agora DB - MVP bootstrap (Auth + Profiles + Classes)
-- =========================================================
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
-- (NUEVO) NOTIFICATION TYPES (CATALOG)
-- Es un catálogo, como 'roles'. Lo ponemos al inicio.
-- =========================================================
CREATE TABLE IF NOT EXISTS notification_type_id (
                                                    id    INT PRIMARY KEY,
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

CREATE INDEX IF NOT EXISTS idx_user_accounts_fullname_trgm
    ON user_accounts
    USING gin (( (first_name || ' ' || COALESCE(second_name,'') || ' ' || last_name) gin_trgm_ops ));

-- =========================================================
-- PROFILES (DATOS DE PERFIL post-registro)
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
CREATE INDEX IF NOT EXISTS idx_profiles_specialties_gin ON profiles USING gin (specialties);

-- =========================================================
-- CLASSES (EVENTOS)
-- =========================================================
CREATE TABLE IF NOT EXISTS classes (
                                       id                 BIGSERIAL PRIMARY KEY,
                                       tutor_id           BIGINT NOT NULL REFERENCES user_accounts(id),
    title              VARCHAR(50) NOT NULL,
    description        TEXT,
    class_date         DATE NOT NULL,
    capacity_per_slot  SMALLINT NOT NULL DEFAULT 1 CHECK (capacity_per_slot > 0),
    specialty_tag      TEXT,
    is_active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- Índices para filtrar listados de clases
CREATE INDEX IF NOT EXISTS idx_classes_tutor_id      ON classes (tutor_id);
CREATE INDEX IF NOT EXISTS idx_classes_class_date    ON classes (class_date);
CREATE INDEX IF NOT EXISTS idx_classes_specialty_tag ON classes (specialty_tag);

-- =========================================================
-- CLASS ENROLLMENTS (INSCRIPCIONES A EVENTOS)
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
-- (NUEVO) CHATS & MESSAGES
-- Necesarios para que la tabla 'notifications' funcione.
-- =========================================================
CREATE TABLE IF NOT EXISTS chats (
                                     id          BIGSERIAL PRIMARY KEY,
                                     chat_name   TEXT, -- (opcional, para chats grupales futuros)
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

-- Índices para chats
CREATE INDEX IF NOT EXISTS idx_chat_messages_chat_id ON chat_messages (chat_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_id ON chat_messages (sender_id);

-- =========================================================
-- (NUEVO) NOTIFICATIONS
-- La tabla principal que une todo.
-- =========================================================
CREATE TABLE IF NOT EXISTS notifications (
                                             id                     BIGSERIAL PRIMARY KEY,
                                             recipient_id           BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    sender_id              BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    notification_type_id   INT NOT NULL REFERENCES notification_type_id(id),
    chat_id                BIGINT NULL REFERENCES chats(id) ON DELETE CASCADE,
    message                TEXT, -- (Opcional: para notificaciones simples sin lógica)
    status                 VARCHAR(20) NOT NULL DEFAULT 'pending'
    CHECK (status IN ('pending', 'read', 'unread', 'accepted', 'declined', 'archived')),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- Índices para buscar notificaciones rápidamente
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_id ON notifications (recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_sender_id ON notifications (sender_id);

-- =========================================================
-- VISTA: DOCENTES ALEATORIOS (para la landing de 20 cards)
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

-- =========================================================
-- Comentarios (ayuda futura)
-- =========================================================
COMMENT ON TABLE user_accounts IS 'Cuentas básicas (login). El perfil va en profiles.';
COMMENT ON COLUMN profiles.specialties IS 'Lista controlada por el frontend para MVP; normalizar en siguiente iteración.';
COMMENT ON COLUMN profiles.level IS 'Nivel educativo: controlado por el frontend (ej. Bachiller, Universidad).';
COMMENT ON COLUMN profiles.state_code IS 'Clave del estado de la república proporcionada por el frontend.';
COMMENT ON TABLE classes IS 'Eventos sin hora; fecha en class_date. specialty_tag es textual en MVP.';
COMMENT ON TABLE class_enrollments IS 'Inscripción a eventos; estado simple (pending/confirmed/canceled).';
COMMENT ON TABLE notifications IS 'Tabla central de notificaciones (solicitudes de contacto, mensajes, etc).';
COMMENT ON COLUMN notifications.chat_id IS 'Referencia al chat, si la notificación es de un mensaje nuevo.';
COMMENT ON COLUMN notifications.status IS 'Estado de la notificación (pending para acciones, read/unread para informativas).';