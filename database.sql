
CREATE DATABASE student_management;

USE student_management;

CREATE TABLE students (
    roll_number INT PRIMARY KEY,          
    full_name   VARCHAR(100) NOT NULL,
    course      VARCHAR(100) NOT NULL,
    email       VARCHAR(150),            
    marks       DOUBLE NOT NULL          
);
