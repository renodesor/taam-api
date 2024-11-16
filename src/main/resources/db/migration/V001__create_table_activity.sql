CREATE TABLE ACTIVITY (
    ID UNIQUEIDENTIFIER DEFAULT NewId() PRIMARY KEY,
    NAME VARCHAR(50) NOT NULL UNIQUE,
    DESCRIPTION VARCHAR(100),
    CATEGORY_ID UNIQUEIDENTIFIER NOT NULL,
    CREATED_BY VARCHAR(50) NOT NULL,
    CREATED_ON DATETIME NOT NULL,
    UPDATED_BY VARCHAR(50),
    UPDATED_ON DATETIME,
    CONSTRAINT activity_category_fk FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORY(ID)
);