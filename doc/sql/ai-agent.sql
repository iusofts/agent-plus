/*
 Navicat MySQL Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50717 (5.7.17-log)
 Source Host           : localhost:3306
 Source Schema         : ai-agent

 Target Server Type    : MySQL
 Target Server Version : 50717 (5.7.17-log)
 File Encoding         : 65001

 Date: 04/06/2026 15:04:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_agent
-- ----------------------------
DROP TABLE IF EXISTS `ai_agent`;
CREATE TABLE `ai_agent`  (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '类型 1.问候型 2.销售型 3.鉴别型',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '新对话' COMMENT '智能体名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '代码',
  `max_rounds` int(11) NOT NULL DEFAULT 1 COMMENT '最大轮次',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设定描述',
  `transfer_human` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '转人工',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认智能体 0:否 1:是',
  `is_system` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否系统预制 0:自定义 1:系统预制',
  `industry_id` bigint(20) NULL DEFAULT 0 COMMENT '行业id',
  `industry_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '默认' COMMENT '行业名称',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_agent_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai智能体' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_agent
-- ----------------------------
INSERT INTO `ai_agent` VALUES (1002, 2, '测试客服', '', 5, '你是一个商场导购员，根据顾客需求帮助顾客指引楼层,要主动询问\n\n楼层分布：\n一层是黄金、国际大牌；\n二楼是女装；\n三楼是男装和童装；\n四楼五楼是餐饮；\n六楼是电影院和KTV;\n\n\n要求：话语简洁，每次回复不要超过50个字。', '是是是是', 0, '2026-06-04 11:29:27', 0, '2026-06-04 14:54:41', 0, 1, 0, 0, 1, '测试');

-- ----------------------------
-- Table structure for ai_call_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_call_log`;
CREATE TABLE `ai_call_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `business_type` int(11) NOT NULL DEFAULT 0 COMMENT '业务类型',
  `business_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '业务ID',
  `agent_id` bigint(20) NULL DEFAULT NULL COMMENT '智能体ID',
  `agent_type` int(11) NULL DEFAULT NULL COMMENT '智能体类型',
  `input_tokens` int(11) NULL DEFAULT 0 COMMENT '输入消耗token数',
  `output_tokens` int(11) NULL DEFAULT 0 COMMENT '输出消耗token数',
  `total_tokens` int(11) NULL DEFAULT 0 COMMENT '总消耗token数',
  `duration` int(11) NOT NULL DEFAULT 0 COMMENT '调用时长(ms)',
  `time_sign` date NOT NULL COMMENT '日期',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_message_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai对话消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_call_log
-- ----------------------------

-- ----------------------------
-- Table structure for ai_conversation
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation`  (
  `id` bigint(20) NOT NULL COMMENT '会话id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `business_type` int(11) NOT NULL DEFAULT 0 COMMENT '业务类型 0.测试 1.默认应用  2.抖音私信',
  `business_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '业务id',
  `agent_id` bigint(20) NOT NULL COMMENT '智能体id',
  `agent_type` int(11) NOT NULL COMMENT '智能体类型 1.迎宾型 2.销售型 3.鉴别型',
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'qwen-plus' COMMENT '使用模型',
  `current_rounds` int(11) NOT NULL DEFAULT 1 COMMENT '当前轮次',
  `last_chat_time` datetime NULL DEFAULT NULL COMMENT '最后聊天时间',
  `test_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '测试数据',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business_id`(`business_id`) USING BTREE,
  INDEX `idx_ai_conversation_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai对话会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_conversation
-- ----------------------------

-- ----------------------------
-- Table structure for ai_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message`  (
  `id` bigint(20) NOT NULL COMMENT '消息id',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话id',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色(user/assistant)',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容',
  `struct_res` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '结构返回值',
  `agent_id` bigint(20) NULL DEFAULT NULL COMMENT '智能体ID',
  `agent_type` int(11) NULL DEFAULT NULL COMMENT '智能体类型',
  `input_tokens` int(11) NULL DEFAULT 0 COMMENT '输入消耗token数',
  `output_tokens` int(11) NULL DEFAULT 0 COMMENT '输出消耗token数',
  `total_tokens` int(11) NULL DEFAULT 0 COMMENT '总消耗token数',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_ai_message_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai对话消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_message
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of id_generator
-- ----------------------------
INSERT INTO `id_generator` VALUES (1, 1077, 'chat', 1, 1);

-- ----------------------------
-- Table structure for t_industry
-- ----------------------------
DROP TABLE IF EXISTS `t_industry`;
CREATE TABLE `t_industry`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '行业名称',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态（1.启用 2.停用）',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `delete_flag` int(11) NOT NULL DEFAULT 0 COMMENT '软删除标记（0：未删除；1：已删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '行业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of t_industry
-- ----------------------------
INSERT INTO `t_industry` VALUES (1, '测试', 0, 1, 1, '2026-06-04 11:28:49', NULL, '2026-06-04 11:28:49', 0);

SET FOREIGN_KEY_CHECKS = 1;
