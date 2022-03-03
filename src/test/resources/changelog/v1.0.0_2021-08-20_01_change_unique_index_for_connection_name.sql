--liquibase formatted sql

--changeset jinghua.zhan:4
--validCheckSum: 8:97cac1c285cb21b6124ef3b208809f30

--labels: update index

--tag: 1.0.0

ALTER TABLE `connection_info`
DROP INDEX `unique_cname`;
ALTER TABLE `connection_info`
ADD INDEX `unique_cname_uname`(`name`, `user`);
--rollback ALTER TABLE `connection_info` DROP INDEX `unique_cname_uname`;

--comment: add unique index for connection name
