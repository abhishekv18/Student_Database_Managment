-- =============================================================
--  Student Database Management System - database setup script
-- =============================================================
--  Run this script ONCE in MySQL before starting the application.
--  The application never creates or deletes the database itself.
--
--  How to run (from a terminal in the project folder):
--      mysql -u root -p < database.sql
--  Or open this file in MySQL Workbench and click the lightning (Execute) button.
-- =============================================================

CREATE DATABASE student_management;

USE student_management;

CREATE TABLE students (
    roll_number INT PRIMARY KEY,          -- unique number for each student
    full_name   VARCHAR(100) NOT NULL,
    course      VARCHAR(100) NOT NULL,
    email       VARCHAR(150),             -- optional
    marks       DOUBLE NOT NULL           -- the application only allows 0 to 100
);
