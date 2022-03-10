--liquibase formatted sql

--changeset jinghua.zhan:15
--labels: add table
--tag: 1.0.1

DROP TABLE IF EXISTS `cell_commit`;
CREATE TABLE `cell_commit` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `cell_id` int(11) unsigned NOT NULL,
  `notebook_id` int(11) unsigned NOT NULL,
  `content` text,
  `commit_id` varchar(64) NOT NULL,
  `last_job_id` varchar(255) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `cell_notebook_idx_commit` (`notebook_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `shared_file`;
CREATE TABLE `shared_file` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `owner` varchar(255) DEFAULT NULL,
  `entity_id` int(11) DEFAULT NULL,
  `entity_type` varchar(15) DEFAULT NULL,
  `commit_id` varchar(40) NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_entity` (`entity_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `node_commit`;
CREATE TABLE `node_commit` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `node_id` int(11) unsigned NOT NULL,
  `workflow_id` int(11) NOT NULL,
  `content` text,
  `commit_id` varchar(64) NOT NULL,
  `user` varchar(255) NOT NULL,
  `position` varchar(255) DEFAULT NULL,
  `input` text,
  `output` text,
  `type` varchar(255) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `node_commit_idx_commit` (`workflow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `notebook_commit`;
CREATE TABLE `notebook_commit` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `notebook_id` int(11) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `commit_id` varchar(64) NOT NULL,
  `cell_list` longtext,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `notebook_commit_idx_commit` (`notebook_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `workflow_commit`;
CREATE TABLE `workflow_commit` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `workflow_id` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `commit_id` varchar(64) NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_commit` (`workflow_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;

--comment: add support for version control and demo update
