--liquibase formatted sql

--changeset jinghua.zhan:2
--validCheckSum: 8:a3faeb7dd9a9eaa1b52dcb7561c1532b
--labels: add column
--tag: 1.0.0
ALTER TABLE `job_info` ADD COLUMN `console_log` LONGTEXT NULL AFTER `result`;
ALTER TABLE `job_info` ADD COLUMN `console_log_offset` int(11) NULL AFTER `console_log`;
--rollback ALTER TABLE `job_info` DROP COLUMN `console_log`, DROP COLUMN `console_log_offset`;

--comment: add console_log, console_log_offset for job_info

--changeset jinghua.zhan:3
--validCheckSum: 8:101e3ece68c0e1e2e3e38703ce4df79b
--labels: add index
--tag: 1.0.0
ALTER TABLE `connection_info` ADD UNIQUE INDEX `unique_cname`(`name`);
--rollback ALTER TABLE `connection_info` DROP INDEX `unique_cname`;

--comment: add unique index for connection name
