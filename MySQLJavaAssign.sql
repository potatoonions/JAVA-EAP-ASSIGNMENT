CREATE DATABASE crs_db;
USE crs_db;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(100),
    email   VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    role    VARCHAR(20),
    status  VARCHAR(20)
);