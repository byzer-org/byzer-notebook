--liquibase formatted sql

--changeset jinghua.zhan:7
--validCheckSum: 8:5f4d4d7b876a53696416599339f0451e

--labels: add records
--tag: 1.0.0

INSERT INTO `node_def_info` (`id`, `node_type`, `name`, `description`) VALUES (6, 'load', 'hdfs', NULL);

INSERT INTO `param_def_info` (`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (60, 6, 'data_type', NULL, 'PARAM_ENUM', NULL, 1, NULL, NULL, NULL);
INSERT INTO `param_def_info` (`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (61, 6, 'csv', NULL, 'ELEMENT', 'csv', NULL, NULL, 60, NULL);
INSERT INTO `param_def_info` (`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (62, 6, 'codec', NULL, 'STRING', NULL, 0, NULL, 61, NULL);
INSERT INTO `param_def_info` (`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (63, 6, 'dateFormat', NULL, 'STRING', NULL, 0, NULL, 61, NULL);
INSERT INTO `param_def_info` (`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (64, 6, 'delimiter', NULL, 'STRING', ',', 0, NULL, 61, NULL);
INSERT INTO `param_def_info` (`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (65, 6, 'escape', NULL, 'STRING', '/', 0, NULL, 61, NULL);
INSERT INTO `param_def_info` (`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (66, 6, 'header', NULL, 'BOOL', 'true', 0, NULL, 61, NULL);
INSERT INTO `param_def_info` (`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (67, 6, 'inferSchema', NULL, 'STRING', NULL, 0, NULL, 61, NULL);
INSERT INTO `param_def_info` (`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (68, 6, 'quote', NULL, 'STRING', '\"', 0, NULL, 61, NULL);
