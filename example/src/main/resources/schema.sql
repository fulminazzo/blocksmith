CREATE TABLE IF NOT EXISTS users (
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    name     VARCHAR(50),
    lastname VARCHAR(50),
    email    VARCHAR(100) UNIQUE,
    age      INT
);
