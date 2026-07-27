/*
 Navicat MySQL Dump SQL

 Source Server         : agent-plus
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : 127.0.0.1:3306
 Source Schema         : agent-plus

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 27/07/2026 15:14:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for id_generator
-- ----------------------------
DROP TABLE IF EXISTS `id_generator`;
CREATE TABLE `id_generator`  (
  `type` int(11) NOT NULL AUTO_INCREMENT COMMENT '类型',
  `uid` int(11) NOT NULL COMMENT '自增ID',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '名称',
  `step_min` int(11) NOT NULL DEFAULT 1 COMMENT '最小步长',
  `step_max` int(11) NOT NULL DEFAULT 1 COMMENT '最大步长',
  PRIMARY KEY (`type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of id_generator
-- ----------------------------
INSERT INTO `id_generator` VALUES (1, 100000245, 'chat', 1, 1);
INSERT INTO `id_generator` VALUES (2, 200000009, 'flow', 1, 1);
INSERT INTO `id_generator` VALUES (3, 300000001, 'knowledge_base', 1, 1);
INSERT INTO `id_generator` VALUES (4, 400000001, 'knowledge_document', 1, 1);
INSERT INTO `id_generator` VALUES (5, 500000006, 'knowledge_chunk', 1, 1);
INSERT INTO `id_generator` VALUES (6, 600000000, 'ai_model', 1, 1);
INSERT INTO `id_generator` VALUES (11, 700000003, 'tool', 1, 1);
INSERT INTO `id_generator` VALUES (12, 800000002, 'plugin', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
