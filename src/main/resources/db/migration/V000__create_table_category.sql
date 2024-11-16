CREATE TABLE CATEGORY (
    ID UNIQUEIDENTIFIER DEFAULT NewId() PRIMARY KEY,
    NAME VARCHAR(50) NOT NULL UNIQUE,
    DESCRIPTION VARCHAR(100),
    CREATED_BY VARCHAR(50) NOT NULL,
    CREATED_ON DATETIME NOT NULL,
    UPDATED_BY VARCHAR(50),
    UPDATED_ON DATETIME
);