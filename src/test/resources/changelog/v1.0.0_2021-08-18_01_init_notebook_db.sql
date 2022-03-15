--liquibase formatted sql

--changeset jinghua.zhan:1
--validCheckSum: 8:aa76d3f86d3fb6a8d6dd6a5609dff5f1
--labels: init
--tag: 1.0.0

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
--  Table structure for `cell_info`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `cell_info` (
                             `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                             `notebook_id` int(11) DEFAULT NULL,
                             `content` text,
                             `last_job_id` varchar(255) DEFAULT NULL,
                             `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                             `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             KEY `idx_noteboo_id` (`notebook_id`)
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `job_info`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `job_info` (
                            `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                            `job_id` varchar(64) NOT NULL,
                            `name` varchar(255) NOT NULL,
                            `content` longtext NOT NULL,
                            `status` smallint(4) NOT NULL DEFAULT '0' COMMENT '0: Running; 1: Success; 2: Failed; 3: Killed',
                            `user` varchar(255) DEFAULT NULL,
                            `notebook` varchar(255) DEFAULT NULL,
                            `engine` varchar(255) DEFAULT NULL,
                            `msg` text,
                            `job_progress` text,
                            `result` longtext,
                            `create_time` timestamp(3) NULL DEFAULT NULL,
                            `finish_time` timestamp(3) NULL DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uniq_job_id` (`job_id`)
) ENGINE=InnoDB AUTO_INCREMENT=237 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `notebook_folder`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `notebook_folder` (
                                   `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                                   `name` varchar(255) DEFAULT NULL,
                                   `absolute_path` varchar(255) DEFAULT NULL,
                                   `user` varchar(255) NOT NULL,
                                   `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                   `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`),
                                   KEY `idx_user` (`user`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `notebook_info`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `notebook_info` (
                                 `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                                 `name` varchar(255) NOT NULL,
                                 `user` varchar(255) NOT NULL,
                                 `cell_list` longtext,
                                 `folder_id` int(11) DEFAULT NULL,
                                 `type` varchar(255) DEFAULT NULL,
                                 `create_time` timestamp(3) NULL DEFAULT NULL,
                                 `update_time` timestamp(3) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3),
                                 PRIMARY KEY (`id`),
                                 KEY `notebook_info_idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `system_config`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `system_config` (
                                 `id` int(11) NOT NULL DEFAULT '1',
                                 `timeout` int(11) DEFAULT NULL,
                                 `engine` varchar(255) DEFAULT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `user_action`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_action` (
                               `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                               `user` varchar(255) NOT NULL,
                               `opened_notebooks` text,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uniq_user` (`user`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `user_info`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user_info` (
                             `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                             `name` varchar(256) NOT NULL,
                             `password` varchar(256) NOT NULL,
                             `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uniq_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `workflow_node`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `workflow_node` (
                             `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                             `workflow_id` int(11) DEFAULT NULL,
                             `content` longtext,
                             `user` varchar(255) NOT NULL,
                             `position` varchar(255) DEFAULT NULL,
                             `input` text,
                             `output` text,
                             `type` varchar(255) DEFAULT NULL,
                             `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                             `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             KEY `idx_workflow_id` (`workflow_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `workflow_info`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `workflow_info` (
                             `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                             `name` varchar(255) NOT NULL,
                             `user` varchar(255) NOT NULL,
                             `folder_id` int(11) unsigned DEFAULT NULL,
                             `create_time` timestamp(3) NULL DEFAULT NULL,
                             `update_time` timestamp(3) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3),
                             PRIMARY KEY (`id`),
                             KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `connection_info`
-- ----------------------------
CREATE TABLE IF NOT EXISTS `connection_info` (
                             `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                             `name` varchar(255) DEFAULT NULL,
                             `user` varchar(255) NOT NULL,
                             `driver` varchar(255) DEFAULT NULL,
                             `datasource` varchar(255) DEFAULT NULL,
                             `url` text,
                             `username` varchar(255) DEFAULT NULL,
                             `password` varchar(255) DEFAULT NULL,
                             `parameter` longtext,
                             `create_time` timestamp(3) NULL DEFAULT NULL,
                             `update_time` timestamp(3) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3),
                             PRIMARY KEY (`id`),
                             KEY `connection_info_idx_user` (`user`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT IGNORE INTO `user_info` VALUES ('1', 'admin', 'a0a6ddc07f9be6a8332b004970e6ad74', '2021-03-30 13:43:48');
INSERT IGNORE INTO `system_config` VALUES ('0', '2880', 'default');

SET FOREIGN_KEY_CHECKS = 1;

--comments: init notebook metadata