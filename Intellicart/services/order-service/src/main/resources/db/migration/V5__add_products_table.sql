CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    price DECIMAL(19, 2),
    stock INTEGER
);
