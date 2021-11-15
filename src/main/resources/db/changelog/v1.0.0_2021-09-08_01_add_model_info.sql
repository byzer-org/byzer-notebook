--liquibase formatted sql

--changeset jinghua.zhan:6
--validCheckSum: 8:f5b87a78691a5f72746322148d1ad2dd

--labels: add table
--tag: 1.0.0

CREATE TABLE `model_info`  (
  `id` int(32) UNSIGNED NOT NULL AUTO_INCREMENT,
  `algorithm` varchar(255) NULL,
  `path` text NULL,
  `group_size` tinyint(4) NULL,
  `node_id` int(32) UNSIGNED NULL,
  `workflow_id` int(32) UNSIGNED NULL,
  `user_name` varchar(255) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_node_id` (`node_id`) USING HASH
);


--comment: add model_info table
