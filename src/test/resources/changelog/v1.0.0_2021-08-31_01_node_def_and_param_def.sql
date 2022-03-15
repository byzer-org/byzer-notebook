--liquibase formatted sql

--changeset jinghua.zhan:5
--validCheckSum: 8:96c667d1686bf172f09c877a5cfbdb93
--labels: add table
--tag: 1.0.0

CREATE TABLE `node_def_info`  (
  `id` int(32) UNSIGNED NOT NULL AUTO_INCREMENT,
  `node_type` varchar(40) NULL,
  `name` varchar(255) NULL,
  `description` varchar(255) NULL,
  PRIMARY KEY (`id`),
  INDEX `index_nt`(`node_type`)
);
INSERT INTO `node_def_info`(`id`, `node_type`, `name`, `description`) VALUES (1, 'train', 'KMeans', NULL);
INSERT INTO `node_def_info`(`id`, `node_type`, `name`, `description`) VALUES (2, 'train', 'NaiveBayes', NULL);
INSERT INTO `node_def_info`(`id`, `node_type`, `name`, `description`) VALUES (3, 'train', 'RandomForest', NULL);
INSERT INTO `node_def_info`(`id`, `node_type`, `name`, `description`) VALUES (4, 'train', 'LinearRegression', NULL);
INSERT INTO `node_def_info`(`id`, `node_type`, `name`, `description`) VALUES (5, 'train', 'LogisticRegression', NULL);

CREATE TABLE `param_def_info`  (
  `id` int(32) UNSIGNED NOT NULL AUTO_INCREMENT,
  `node_def_id` int(32) UNSIGNED NULL,
  `name` varchar(255) NULL,
  `description` varchar(255) NULL,
  `value_type` varchar(255) NULL,
  `default_value` varchar(255) NULL,
  `required` tinyint(1) NULL,
  `constrain` text NULL,
  `bind` int(32) UNSIGNED NULL,
  `is_group_param` tinyint(1) NULL,
  PRIMARY KEY (`id`),
  INDEX `index_ndid`(`node_def_id`)
);

INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (1, 3, 'evaluateTable', NULL, 'STRING', NULL, 0, NULL, NULL, 0);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (2, 3, 'keepVersion', NULL, 'BOOL', 'true', 1, NULL, NULL, 0);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (3, 3, 'featuresCol', NULL, 'STRING', NULL, 1, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (4, 3, 'labelCol', NULL, 'STRING', NULL, 1, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (5, 3, 'featureSubsetStrategy', NULL, 'ENUM', 'auto', 0, '[\"auto\", \"all\", \"onethird\", \"sqrt\", \"log2\"]', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (6, 3, 'impurity', NULL, 'ENUM', 'gini', 0, '[\"entropy\",\"gini\"]', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (7, 3, 'predictionCol', NULL, 'STRING', NULL, 0, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (8, 3, 'numClasses', NULL, 'ARRAY', NULL, 0, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (9, 3, 'numClasses__Element', NULL, 'INT', NULL, NULL, '{\"min\":1}', 8, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (10, 3, 'maxBins', NULL, 'ARRAY', NULL, 0, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (11, 3, 'maxBins__Element', NULL, 'INT', '32', NULL, '{\"min\":1}', 10, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (12, 3, 'maxDepth', NULL, 'ARRAY', NULL, 1, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (13, 3, 'maxDepth__Element', NULL, 'INT', '4', NULL, '{\"min\":1}', 12, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (14, 3, 'numTrees', NULL, 'ARRAY', NULL, 0, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (15, 3, 'numTrees__Element', NULL, 'INT', NULL, NULL, '{\"min\":1}', 14, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (16, 3, 'seed', NULL, 'ARRAY', NULL, 0, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (17, 3, 'seed__Element', NULL, 'INT', NULL, NULL, '{\"min\":0}', 16, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (20, 1, 'evaluateTable', NULL, 'STRING', NULL, 0, NULL, NULL, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (21, 1, 'keepVersion', NULL, 'BOOL', 'true', 1, NULL, NULL, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (22, 1, 'featuresCol', NULL, 'STRING', NULL, 1, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (23, 1, 'predictionCol', NULL, 'STRING', NULL, 0, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (24, 1, 'distanceMeasure', NULL, 'ENUM', 'euclidean', 1, '[\"euclidean\",\"cosine\"]', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (25, 1, 'k', NULL, 'ARRAY', NULL, 1, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (26, 1, 'k__Element', NULL, 'INT', '4', NULL, '{\"min\":1}', 25, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (27, 1, 'maxIter', NULL, 'ARRAY', NULL, 0, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (28, 1, 'maxIter__Element', NULL, 'INT', NULL, NULL, '{\"min\":1}', 27, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (30, 2, 'evaluateTable', NULL, 'STRING', NULL, 0, NULL, NULL, 0);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (31, 2, 'keepVersion', NULL, 'BOOL', 'true', 1, NULL, NULL, 0);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (32, 2, 'featuresCol', NULL, 'STRING', NULL, 1, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (33, 2, 'labelcol', NULL, 'STRING', NULL, 1, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (34, 2, 'predictionCol', NULL, 'STRING', NULL, 0, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (35, 2, 'modelType', NULL, 'ENUM', 'multinomial', 0, '[\"multinomial\",\"bernoulli\"]', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (40, 4, 'evaluateTable', NULL, 'STRING', NULL, 0, NULL, NULL, 0);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (41, 4, 'keepVersion', NULL, 'BOOL', 'true', 1, NULL, NULL, 0);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (42, 4, 'featuresCol', NULL, 'STRING', NULL, 1, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (43, 4, 'labelcol', NULL, 'STRING', NULL, 1, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (44, 4, 'predictionCol', NULL, 'STRING', NULL, 0, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (46, 4, 'maxIter', NULL, 'ARRAY', NULL, 0, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (47, 4, 'maxIter__Element', NULL, 'INT', NULL, NULL, '{\"min\":1}', 46, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (50, 5, 'evaluateTable', NULL, 'STRING', NULL, 0, NULL, NULL, 0);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (51, 5, 'keepVersion', NULL, 'BOOL', 'true', 1, NULL, NULL, 0);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (52, 5, 'featuresCol', NULL, 'STRING', NULL, 1, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (53, 5, 'labelcol', NULL, 'STRING', NULL, 1, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (54, 5, 'predictionCol', NULL, 'STRING', NULL, 0, NULL, NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (55, 5, 'family', NULL, 'ENUM', 'auto', 0, '[\"auto\",\"binomial\",\"multinomial\"]', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (56, 5, 'threshold', NULL, 'ARRAY', NULL, 1, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (57, 5, 'threshold__Element', NULL, 'FLOAT', '0.5', NULL, '{\"min\":0, \"max\":1}', 56, NULL);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (58, 5, 'maxIter', NULL, 'ARRAY', NULL, 0, '3', NULL, 1);
INSERT INTO `param_def_info`(`id`, `node_def_id`, `name`, `description`, `value_type`, `default_value`, `required`, `constrain`, `bind`, `is_group_param`) VALUES (59, 5, 'maxIter__Element', NULL, 'INT', '100', NULL, '{\"min\":1}', 58, NULL);
