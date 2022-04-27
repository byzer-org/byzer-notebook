--liquibase formatted sql

--changeset tianqing.guo:12
--validCheckSum: 8:2a27a2ad4a81e31338a2c617459bc9b8

--labels: add table
--tag: 1.0.0

# Discretize
INSERT IGNORE INTO et_params_def VALUES(1508, 9, 'relativeError', '', false, 'Normal', 'FLOAT', '', '', true, '', '', "{min:0,max:1}");
# JsonExpandExt
INSERT IGNORE INTO et_params_def VALUES(1701, 14, 'samplingRatio', '', true, 'Normal', 'FLOAT', '1.0', '', false, '', '', "{min:0,max:1}");

--comment: add et param coverage

