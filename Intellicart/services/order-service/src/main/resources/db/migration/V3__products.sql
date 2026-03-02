CREATE TABLE products
(
    id    BIGINT NOT NULL,
    name  VARCHAR(255),
    price DECIMAL(19, 2),
    stock INTEGER,
    description VARCHAR(255),
    CONSTRAINT pk_products PRIMARY KEY (id)
);