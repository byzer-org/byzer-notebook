--liquibase formatted sql

--changeset jinghua.zhan:18
--labels:  update table
--tag: 1.2.0

ALTER TABLE `system_config`
MODIFY COLUMN `id` int(11) NOT NULL AUTO_INCREMENT FIRST,
ADD COLUMN `user` varchar(255) NULL AFTER `id`,
ADD UNIQUE INDEX `sys_conf_uniq_uname`(`user`) USING HASH;

--comments: config for each user