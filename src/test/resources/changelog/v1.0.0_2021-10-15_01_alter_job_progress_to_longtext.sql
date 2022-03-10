--liquibase formatted sql


--changeset tianqing.guo:10
--validCheckSum: 8:1f3204bf941832bec580dbce4c2b3c72

--labels: add column
--tag: 1.0.0

ALTER TABLE `job_info` MODIFY job_progress LONGTEXT DEFAULT NULL;
--comment: alter job_info job_progress to longtext