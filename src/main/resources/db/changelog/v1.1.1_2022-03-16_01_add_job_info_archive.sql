--liquibase formatted sql

--changeset jinghua.zhan:17
--labels:  add table
--tag: 1.1.1

ALTER TABLE `job_info` DROP COLUMN `console_log_offset`,
DROP COLUMN `job_progress`,
ADD COLUMN `job_progress` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL AFTER `msg`,
MODIFY COLUMN `status` smallint(4) NOT NULL DEFAULT 0 COMMENT '0: Running; 1: Success; 2: Failed; 3: Killed; 4-6 Waiting' AFTER `content`,
ADD INDEX `job_info_archive_ctime_status_k`(`create_time`, `status`) USING BTREE,
ADD INDEX `job_info_archive_ctime_k`(`create_time`) USING BTREE;

RENAME TABLE `job_info` TO `job_info_archive`;

CREATE TABLE `job_info` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
    `job_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
    `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
    `status` smallint(4) NOT NULL DEFAULT '0' COMMENT '0: Running; 1: Success; 2: Failed; 3: Killed; 4-6 Waiting',
    `user` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `notebook` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `engine` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `msg` text COLLATE utf8mb4_unicode_ci,
    `job_progress` text COLLATE utf8mb4_unicode_ci,
    `result` longtext COLLATE utf8mb4_unicode_ci,
    `console_log` longtext COLLATE utf8mb4_unicode_ci,
    `create_time` timestamp(3) NULL DEFAULT NULL,
    `finish_time` timestamp(3) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `job_info_uniq_job_id` (`job_id`),
    INDEX `job_info_ctime_status_k`(`create_time`, `status`) USING BTREE,
    INDEX `job_info_ctime_k`(`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--comments: job log archive