--liquibase formatted sql

--changeset jinghua.zhan:9
--validCheckSum: 8:2c80928ab422533dd2814b28e8cda7db

--labels: add column
--tag: 1.0.0

ALTER TABLE `user_action` ADD COLUMN `uploaded_files` text NULL AFTER `opened_notebooks`;
--comment: add uploaded_files in table user_action