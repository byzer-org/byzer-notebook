--liquibase formatted sql

--changeset jinghua.zhan:18
--labels:  update table
--tag: 1.2.1

ALTER TABLE `job_info` MODIFY msg LONGTEXT DEFAULT NULL;

--comments: config for each user