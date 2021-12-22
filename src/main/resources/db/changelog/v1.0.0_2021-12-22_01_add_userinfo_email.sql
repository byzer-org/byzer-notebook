--liquibase formatted sql

--changeset tianqing.guo:15
--labels: update record
--tag: 1.0.0

ALTER TABLE user_info ADD email varchar(256) NOT NULL after password;

--comment: alter user_info add email