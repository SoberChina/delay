SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for execute_log
-- ----------------------------
DROP TABLE IF EXISTS `execute_log`;
CREATE TABLE `execute_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `plan_id` int(11) NOT NULL DEFAULT '0' COMMENT 'plan id',
  `execute_type` int(11) NOT NULL DEFAULT '0' COMMENT '执行类型0:自动执行1:手动执行',
  `request` varchar(1000) NOT NULL DEFAULT '' COMMENT '请求信息',
  `response` varchar(255) NOT NULL DEFAULT '' COMMENT '响应信息',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='执行日志';

-- ----------------------------
-- Table structure for plan
-- ----------------------------
DROP TABLE IF EXISTS `plan`;
CREATE TABLE `plan` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `plan_name` varchar(64) NOT NULL COMMENT '任务名称',
  `plan_code` varchar(32) NOT NULL COMMENT '任务编码',
  `plan_type` int(11) NOT NULL DEFAULT '1' COMMENT '调用类型:0内部调用,1外部调用',
  `state` int(11) NOT NULL DEFAULT '0' COMMENT '状态:0正常待执行,1执行完成,2执行失败,9取消(仅有待执行状态和执行失败可以取消执行)',
  `callback_url` varchar(120) NOT NULL COMMENT '回调地址',
  `callback_method` varchar(5) NOT NULL DEFAULT 'POST' COMMENT '回调方法',
  `params` varchar(1000) NOT NULL DEFAULT '' COMMENT '回调参数',
  `headers` varchar(1000) NOT NULL COMMENT '请求头信息',
  `retry_num` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '失败重试测次数',
  `flag` tinyint(11) NOT NULL DEFAULT '0' COMMENT '推送标识0未推送1已推送',
  `execute_time` timestamp NULL DEFAULT NULL COMMENT '执行时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_plancode` (`plan_code`) USING BTREE,
  KEY `idx_executetime` (`execute_time`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='计划信息表';

SET FOREIGN_KEY_CHECKS = 1;
