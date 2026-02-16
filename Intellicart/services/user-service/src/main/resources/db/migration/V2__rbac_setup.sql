-- Eliminar la columna role antigua de la tabla users
ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Crear tabla de roles si no existe
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
    );

-- Crear tabla intermedia para la relación Many-to-Many
CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
    );

-- Insertar roles iniciales
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN') ON CONFLICT DO NOTHING;