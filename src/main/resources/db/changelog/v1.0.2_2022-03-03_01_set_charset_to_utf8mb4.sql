--liquibase formatted sql

--changeset jinghua.zhan:16
--labels: charset
--tag: 1.0.2

ALTER TABLE `cell_info`
MODIFY COLUMN `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `notebook_id`,
MODIFY COLUMN `last_job_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `content`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `connection_info`
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `id`,
MODIFY COLUMN `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `name`,
MODIFY COLUMN `driver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `user`,
MODIFY COLUMN `datasource` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `driver`,
MODIFY COLUMN `url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `datasource`,
MODIFY COLUMN `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `url`,
MODIFY COLUMN `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `username`,
MODIFY COLUMN `parameter` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `password`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;


ALTER TABLE `et_params_def`
MODIFY COLUMN `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `et_id`,
MODIFY COLUMN `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `name`,
MODIFY COLUMN `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `optional`,
MODIFY COLUMN `value_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `type`,
MODIFY COLUMN `default_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `value_type`,
MODIFY COLUMN `enum_values` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `default_value`,
MODIFY COLUMN `depends` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `required`,
MODIFY COLUMN `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `depends`,
MODIFY COLUMN `constraint` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `label`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;


ALTER TABLE `job_info`
MODIFY COLUMN `job_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `id`,
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `job_id`,
MODIFY COLUMN `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `name`,
MODIFY COLUMN `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `status`,
MODIFY COLUMN `notebook` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `user`,
MODIFY COLUMN `engine` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `notebook`,
MODIFY COLUMN `msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `engine`,
MODIFY COLUMN `job_progress` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `msg`,
MODIFY COLUMN `result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `job_progress`,
MODIFY COLUMN `console_log` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `result`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;


ALTER TABLE `model_info`
MODIFY COLUMN `algorithm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `id`,
MODIFY COLUMN `path` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `algorithm`,
MODIFY COLUMN `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `workflow_id`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;


ALTER TABLE `node_def_info`
MODIFY COLUMN `node_type` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `id`,
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `node_type`,
MODIFY COLUMN `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `name`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `notebook_folder`
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `id`,
MODIFY COLUMN `absolute_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `name`,
MODIFY COLUMN `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `absolute_path`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `notebook_info`
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `id`,
MODIFY COLUMN `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `name`,
MODIFY COLUMN `cell_list` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `user`,
MODIFY COLUMN `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `folder_id`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `param_def_info`
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `node_def_id`,
MODIFY COLUMN `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `name`,
MODIFY COLUMN `value_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `description`,
MODIFY COLUMN `default_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `value_type`,
MODIFY COLUMN `constrain` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `required`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;


ALTER TABLE `register_et`
MODIFY COLUMN `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `id`,
MODIFY COLUMN `caption` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `name`,
MODIFY COLUMN `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `caption`,
MODIFY COLUMN `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `category`,
MODIFY COLUMN `et_usage` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `description`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;


ALTER TABLE `system_config`
MODIFY COLUMN `engine` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `timeout`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `usage_template`
MODIFY COLUMN `usage` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `id`,
MODIFY COLUMN `template` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `usage`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `user_action`
MODIFY COLUMN `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `id`,
MODIFY COLUMN `opened_notebooks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `user`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `user_info`
MODIFY COLUMN `name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `id`,
MODIFY COLUMN `password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `name`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `workflow_info`
MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `id`,
MODIFY COLUMN `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `name`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `workflow_node`
MODIFY COLUMN `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `workflow_id`,
MODIFY COLUMN `user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL AFTER `content`,
MODIFY COLUMN `position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `user`,
MODIFY COLUMN `input` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `position`,
MODIFY COLUMN `output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `input`,
MODIFY COLUMN `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL AFTER `output`,
CHARACTER SET = utf8mb4, COLLATE = utf8mb4_unicode_ci;

--comment: charset update to utf8mb4
