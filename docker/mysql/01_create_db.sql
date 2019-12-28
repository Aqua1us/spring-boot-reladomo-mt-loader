CREATE DATABASE IF NOT EXISTS `testdb` CHARACTER SET utf8 COLLATE utf8_general_ci;
GRANT ALL ON testdb.* TO 'testuser'@'%';
FLUSH PRIVILEGES;

USE testdb;
CREATE TABLE object_sequence
(
    sequence_name VARCHAR(64) NOT NULL,
    next_value    BIGINT
);
ALTER TABLE object_sequence
    ADD CONSTRAINT object_sequence_pk PRIMARY KEY (sequence_name);

CREATE TABLE customer
(
    customer_id          INT                                      NOT NULL,
    NAME                 VARCHAR(64)                              NOT NULL,
    country              VARCHAR(48)                              NOT NULL,
    business_date_from   DATETIME(3)                              NOT NULL,
    business_date_to     DATETIME(3)                              NOT NULL,
    processing_date_from DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) NOT NULL,
    processing_date_to   DATETIME(3)                              NOT NULL
);
ALTER TABLE customer
    ADD CONSTRAINT customer_pk PRIMARY KEY (customer_id, business_date_to, processing_date_to);
