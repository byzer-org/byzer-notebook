--liquibase formatted sql

--changeset jinghua.zhan:13
--validCheckSum: 8:1c0808d15f41866456994f421b041f80
--labels: add user
--tag: 1.0.0

INSERT IGNORE INTO `user_info` (`name`, `password`)VALUES ('ByzerRobot', 'a0a6ddc07f9be6a8332b004970e6ad74');

--comment: add user for execute schedule tasks

