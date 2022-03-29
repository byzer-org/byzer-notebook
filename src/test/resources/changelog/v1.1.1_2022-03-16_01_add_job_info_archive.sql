--liquibase formatted sql

--changeset jinghua.zhan:17
--labels:  add table
--tag: 1.1.1

DROP TABLE `job_info`;

CREATE TABLE `job_info` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
    `job_id` varchar(64) NOT NULL,
    `name` varchar(255) NOT NULL,
    `content` longtext NOT NULL,
    `status` smallint(4) NOT NULL DEFAULT '0' COMMENT '0: Running; 1: Success; 2: Failed; 3: Killed; 4-6 Waiting',
    `user` varchar(255) DEFAULT NULL,
    `notebook` varchar(255) DEFAULT NULL,
    `engine` varchar(255) DEFAULT NULL,
    `msg` text,
    `job_progress` text,
    `result` longtext,
    `console_log` longtext,
    `create_time` timestamp(3) NULL DEFAULT NULL,
    `finish_time` timestamp(3) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `job_info_uniq_job_id` (`job_id`),
    KEY `job_info_ctime_status_k`(`create_time`, `status`),
    KEY `job_info_ctime_k`(`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `job_info_archive` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
    `job_id` varchar(64) NOT NULL,
    `name` varchar(255) NOT NULL,
    `content` longtext NOT NULL,
    `status` smallint(4) NOT NULL DEFAULT '0' COMMENT '0: Running; 1: Success; 2: Failed; 3: Killed; 4-6 Waiting',
    `user` varchar(255) DEFAULT NULL,
    `notebook` varchar(255) DEFAULT NULL,
    `engine` varchar(255) DEFAULT NULL,
    `msg` text,
    `job_progress` text,
    `result` longtext,
    `console_log` longtext,
    `create_time` timestamp(3) NULL DEFAULT NULL,
    `finish_time` timestamp(3) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `job_info_archive_uniq_job_id` (`job_id`),
    KEY `job_info_archive_ctime_status_k`(`create_time`, `status`),
    KEY `job_info_archive_ctime_k`(`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--comments: job log archive