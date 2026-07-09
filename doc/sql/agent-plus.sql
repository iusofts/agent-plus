/*
 Navicat MySQL Dump SQL

 Source Server         : agent-plus
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : 121.40.203.82:3306
 Source Schema         : agent-plus

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 03/07/2026 15:46:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_agent
-- ----------------------------
DROP TABLE IF EXISTS `ai_agent`;
CREATE TABLE `ai_agent` (
  `id` bigint(20) NOT NULL COMMENT '编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '新对话' COMMENT '智能体名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '功能介绍',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设定描述',
  `model_id` bigint(20) NULL DEFAULT NULL COMMENT '使用模型ID',
  `workflow_ids` json NULL DEFAULT NULL COMMENT '绑定工作流ID列表(JSON数组存储)',
  `opening_statement` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开场白文案',
  `opening_questions` json NULL DEFAULT NULL COMMENT '开场白预置问题(JSON数组存储)',
  `knowledge_base_ids` json NULL DEFAULT NULL COMMENT '绑定知识库ID列表(JSON数组存储)',
  `retrieval_top_k` int(11) NULL DEFAULT 3 COMMENT '知识库召回条数',
  `temperature` decimal(3,2) NOT NULL DEFAULT 0.70 COMMENT '生成随机性(temperature)',
  `context_rounds` int(11) NOT NULL DEFAULT 5 COMMENT '携带上下文轮数',
  `max_reply_length` int(11) NULL DEFAULT 2000 COMMENT '最大回复长度',
  `max_inference_length` int(11) NULL DEFAULT 4000 COMMENT '最大推理回答长度',
  `enable_question_suggestion` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用用户问题建议 0:否 1:是',
  `custom_suggestion_prompt` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自定义建议提示词 0:否 1:是',
  `suggestion_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自定义建议提示词',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 1 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_agent_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai智能体' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_agent
-- ----------------------------

-- ----------------------------
-- Table structure for ai_conversation
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation` (
  `id` bigint(20) NOT NULL COMMENT '会话id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `agent_id` bigint(20) NOT NULL COMMENT '智能体id',
  `model_id` bigint(20) NULL DEFAULT NULL COMMENT '使用模型ID',
  `current_rounds` int(11) NOT NULL DEFAULT 1 COMMENT '当前轮次',
  `last_chat_time` datetime NULL DEFAULT NULL COMMENT '最后聊天时间',
  `test_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '测试数据',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
  `delete_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常 1:已删除)',
  `org_id` int(11) NOT NULL DEFAULT 0 COMMENT '所属组织ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_conversation_org_id`(`org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'ai对话会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_conversation
-- ----------------------------

-- ----------------------------
-- Table structure for ai_flow
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow`;
CREATE TABLE `ai_flow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '类型 1:工作流Workflow 2:对话流Chatflow',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '未命名流程' COMMENT '流程名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程唯一编码',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '流程描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '图标地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '启用状态 0:禁用 1:启用',
  `latest_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'v1.0.0' COMMENT '当前最新版本号(v1.0.0格式)',
  `online_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '线上发布版本号，空=未发布',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '最后更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除 0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code_delete`(`code`, `delete_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI流程主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_flow_runtime
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_runtime`;
CREATE TABLE `ai_flow_runtime` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行实例ID',
  `flow_id` bigint(20) NOT NULL COMMENT '流程ID',
  `version_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行使用的语义化版本v1.0.0',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '全局追踪ID',
  `run_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '运行状态 0等待 1运行中 2成功 3失败 4终止',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `input_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '本次执行入参JSON',
  `output_result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '全局输出结果',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息',
  `cost_ms` bigint(20) NULL DEFAULT 0 COMMENT '耗时毫秒',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '触发人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新操作人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_trace_id`(`trace_id`) USING BTREE,
  INDEX `idx_flow_run`(`flow_id`, `run_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程运行实例' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_flow_runtime
-- ----------------------------

-- ----------------------------
-- Table structure for ai_flow_runtime_node
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_runtime_node`;
CREATE TABLE `ai_flow_runtime_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `runtime_id` bigint(20) NOT NULL COMMENT '关联运行实例ID',
  `node_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'VueFlow节点唯一id',
  `node_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点类型(Start/LLM/Knowledge/Condition/End)',
  `run_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0未执行 1执行中 2成功 3失败 4跳过',
  `node_input` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '节点入参JSON',
  `node_output` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '节点输出JSON',
  `error_stack` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '异常堆栈信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '节点开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '节点结束时间',
  `cost_ms` bigint(20) NULL DEFAULT 0 COMMENT '节点耗时(毫秒)',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_runtime_id`(`runtime_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '运行节点明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_flow_runtime_node
-- ----------------------------

-- ----------------------------
-- Table structure for ai_flow_version
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_version`;
CREATE TABLE `ai_flow_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `flow_id` bigint(20) NOT NULL COMMENT '关联ai_flow主键',
  `version_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语义化版本 v1.0.0',
  `version_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '版本别名/标题',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '修改备注',
  `flow_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'VueFlow画布完整数据JSON(nodes/edges/viewport)',
  `config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '流程全局配置(入参、超时、重试、权限等)',
  `publishing_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发布状态 0草稿 1已发布 2待审核',
  `publishing_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `publishing_by` bigint(20) NULL DEFAULT NULL COMMENT '发布人ID',
  `create_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '保存操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '版本生成时间',
  `update_by` bigint(20) NOT NULL DEFAULT 0 COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flow_version`(`flow_id`, `version_no`, `delete_flag`) USING BTREE,
  INDEX `idx_flow_id`(`flow_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI流程版本画布表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message` (
  `id` bigint(20) NOT NULL COMMENT '消息id',
  `conversation_id` bigint(20) NOT NULL COMMENT '会话id',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色(user/assistant)',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息内容',
  `struct_res` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '结构返回值',
  `agent_id` bigint(20) NULL DEFAULT NULL COMMENT '智能体ID',
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
CREATE TABLE `id_generator` (
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
INSERT INTO `id_generator` VALUES (1, 1087, 'chat', 1, 1);
INSERT INTO `id_generator` VALUES (2, 10004, 'flow', 1, 1);

-- ----------------------------
-- Table structure for t_industry
-- ----------------------------
DROP TABLE IF EXISTS `t_industry`;
CREATE TABLE `t_industry` (
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
