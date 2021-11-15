--liquibase formatted sql

--changeset jinghua.zhan:8
--validCheckSum: 8:2c61c190caab149f8747b1cbf0cae743

--labels: add records
--tag: 1.0.0

UPDATE `param_def_info` SET constrain = '{\"min\":2}' WHERE id = 11;

--comment: maxBins value should not less than 2