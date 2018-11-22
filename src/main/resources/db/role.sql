DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(11) unsigned NOT NULL COMMENT '用户id',
  `name` varchar(32) NOT NULL COMMENT '用户角色名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id_and_name` (`user_id`,`name`) USING BTREE,
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COMMENT='用户角色表';

-- ----------------------------
--  Records of `role`
-- ----------------------------
BEGIN;
INSERT INTO `role` VALUES ('1', '1', 'USER'), ('2', '2', 'ADMIN'), ('3', '3', 'USER'), ('4', '4', 'USER'), ('5', '5', 'USER'), ('6', '6', 'USER'), ('7', '7', 'USER'), ('8', '8', 'USER');
COMMIT;