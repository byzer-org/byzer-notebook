--liquibase formatted sql

--changeset jinghua.zhan:18
--labels:  update table
--tag: 1.2.2

ALTER TABLE `cell_info` MODIFY content LONGTEXT DEFAULT NULL;

--comments: config for each user