--liquibase formatted sql

--changeset jinghua.zhan:14
--labels: update record
--tag: 1.0.0
UPDATE `user_info` set `password` = '96fadf4b7b0807c3140dfd93dcfe4c6f' WHERE `id` = 1;
--comment: update ADMIN default password

