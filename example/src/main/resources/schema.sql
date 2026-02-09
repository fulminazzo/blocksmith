CREATE TABLE IF NOT EXISTS users (
    id       INT PRIMARY KEY,
    name     VARCHAR(50),
    lastname VARCHAR(50),
    email    VARCHAR(100) UNIQUE,
    age      INT
);
