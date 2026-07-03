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
INSERT INTO `ai_agent` VALUES (1007, 0, '测试2', '', 1, 'www', '', 0, '2026-06-04 13:10:53', 0, '2026-06-04 13:10:56', 1, 1, 0, 0, 1, '测试');
INSERT INTO `ai_agent` VALUES (1078, 1, 'aaaa', '', 1, '3333', '', 0, '2026-06-29 16:13:10', 0, '2026-06-29 16:15:55', 1, 1, 0, 0, 0, '默认');
INSERT INTO `ai_agent` VALUES (1079, 2, '1222444', '', 5, 'wqeqwe', 'www', 0, '2026-06-29 16:15:50', 0, '2026-06-29 16:19:53', 0, 1, 0, 0, 0, '默认');

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
INSERT INTO `ai_conversation` VALUES (1080, '你好', 0, '', 1079, 2, 'qwen-plus', 3, '2026-06-29 16:26:27', NULL, 1, '2026-06-29 16:25:28', 0, '2026-06-29 16:26:27', 0, 0);

-- ----------------------------
-- Table structure for ai_flow
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow`;
CREATE TABLE `ai_flow`  (
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
-- Records of ai_flow
-- ----------------------------
INSERT INTO `ai_flow` VALUES (1, 1, 'asd', 'asd', 'asdasd', 'asd', 1, 'v1.0.0', '', 1, '2026-06-29 17:02:49', 1, '2026-07-02 10:33:28', 1);
INSERT INTO `ai_flow` VALUES (2, 2, '111', '222', '', '333', 1, 'v1.0.0', '', 1, '2026-06-29 17:04:36', 0, '2026-06-29 17:05:03', 1);
INSERT INTO `ai_flow` VALUES (3, 2, '22', '33', '55', '44', 1, 'v1.0.0', '', 1, '2026-06-29 17:04:59', 1, '2026-07-02 22:52:13', 1);
INSERT INTO `ai_flow` VALUES (10002, 1, '测试01', 'TEST_001', '1111', '', 1, 'v1.0.0', 'v1.0.0', 1, '2026-07-02 10:33:44', 1, '2026-07-02 10:34:02', 0);
INSERT INTO `ai_flow` VALUES (10003, 1, '测试02', 'TEST_02', '22', '', 1, 'v1.0.2', 'v1.0.1', 1, '2026-07-02 22:52:36', 1, '2026-07-02 22:52:36', 0);
INSERT INTO `ai_flow` VALUES (10004, 1, '测试03', 'TEST_03', '', '', 1, 'v1.0.0', '', 1, '2026-07-03 10:38:10', 1, '2026-07-03 10:38:10', 0);

-- ----------------------------
-- Table structure for ai_flow_runtime
-- ----------------------------
DROP TABLE IF EXISTS `ai_flow_runtime`;
CREATE TABLE `ai_flow_runtime`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行实例ID',
  `flow_id` bigint(20) NOT NULL COMMENT '流程ID',
  `version_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行使用的语义化版本v1.0.0',
  `trace_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '全局追踪ID',
  `run_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '运行状态 0等待 1运行中 2成功 3失败 4终止',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `input_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '本次执行入参JSON',
  `output_result` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '全局输出结果',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
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
CREATE TABLE `ai_flow_runtime_node`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `runtime_id` bigint(20) NOT NULL COMMENT '关联运行实例ID',
  `node_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'VueFlow节点唯一id',
  `node_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节点类型(Start/LLM/Knowledge/Condition/End)',
  `run_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0未执行 1执行中 2成功 3失败 4跳过',
  `node_input` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点入参JSON',
  `node_output` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '节点输出JSON',
  `error_stack` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '异常堆栈信息',
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
CREATE TABLE `ai_flow_version`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `flow_id` bigint(20) NOT NULL COMMENT '关联ai_flow主键',
  `version_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语义化版本 v1.0.0',
  `version_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '版本别名/标题',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '修改备注',
  `flow_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'VueFlow画布完整数据JSON(nodes/edges/viewport)',
  `config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '流程全局配置(入参、超时、重试、权限等)',
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
-- Records of ai_flow_version
-- ----------------------------
INSERT INTO `ai_flow_version` VALUES (2, 10002, 'v1.0.0', '', '', '{\"nodes\":[{\"id\":\"start\",\"type\":\"Start\",\"initialized\":false,\"position\":{\"x\":100.0,\"y\":100.0},\"label\":\"开始\",\"data\":{\"label\":\"开始\",\"description\":\"流程入口\",\"inputParams\":null}},{\"id\":\"end\",\"type\":\"End\",\"initialized\":false,\"position\":{\"x\":900.0,\"y\":100.0},\"label\":\"结束\",\"data\":{\"label\":\"结束\",\"description\":\"工作流程完成。\",\"outputParams\":null}},{\"id\":\"node-Knowledge-1783003270546\",\"type\":\"Knowledge\",\"initialized\":false,\"position\":{\"x\":451.69491525423734,\"y\":113.5593220338983},\"label\":null,\"data\":{\"label\":\"知识库\",\"description\":\"\",\"knowledgeId\":null,\"topK\":3,\"inputParams\":[],\"outputParams\":[]}}],\"edges\":[{\"id\":\"e-start-node-Knowledge-1783003270546\",\"source\":\"start\",\"target\":\"node-Knowledge-1783003270546\",\"sourceHandle\":null,\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"markerEnd\":null,\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":342.9999694824219,\"sourceY\":145.0,\"targetX\":448.69491525423734,\"targetY\":213.3593250856561},{\"id\":\"e-node-Knowledge-1783003270546-end-1783003360114\",\"source\":\"node-Knowledge-1783003270546\",\"target\":\"end\",\"sourceHandle\":null,\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"markerEnd\":null,\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":694.6949152542373,\"sourceY\":213.3593250856561,\"targetX\":897.0,\"targetY\":158.0}],\"position\":[20.0,177.0],\"zoom\":1.18,\"viewport\":{\"x\":20.0,\"y\":177.0,\"zoom\":1.18}}', '{\"envVars\":[{\"name\":\"aaa\",\"type\":\"string\",\"description\":\"\",\"defaultValue\":\"\"}]}', 1, '2026-07-03 10:44:59', 1, 1, '2026-07-02 21:55:52', 1, '2026-07-02 22:42:40', 0);
INSERT INTO `ai_flow_version` VALUES (3, 3, 'v1.0.0', '', '', '{\"nodes\":[{\"id\":\"start\",\"type\":\"Start\",\"initialized\":false,\"position\":{\"x\":100.0,\"y\":100.0},\"label\":\"开始\",\"data\":{\"label\":\"开始\",\"description\":\"流程入口\",\"inputParams\":null}},{\"id\":\"end\",\"type\":\"End\",\"initialized\":false,\"position\":{\"x\":900.0,\"y\":100.0},\"label\":\"结束\",\"data\":{\"label\":\"结束\",\"description\":\"工作流程完成。\",\"outputParams\":null}},{\"id\":\"node-LLM-1783002819431\",\"type\":\"LLM\",\"initialized\":false,\"position\":{\"x\":492.37288135593224,\"y\":29.0},\"label\":null,\"data\":{\"label\":\"大模型\",\"description\":\"\",\"model\":1,\"temperature\":0.7,\"systemPrompt\":\"\",\"inputParams\":[],\"outputParams\":[],\"timeout\":60,\"retryCount\":0,\"errorHandling\":\"stop\",\"customErrorContent\":\"\"}}],\"edges\":[{\"id\":\"e-start-node-LLM-1783002819431\",\"source\":\"start\",\"target\":\"node-LLM-1783002819431\",\"sourceHandle\":null,\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"markerEnd\":null,\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":343.0,\"sourceY\":145.0,\"targetX\":489.3728482521187,\"targetY\":144.95000496557202},{\"id\":\"e-node-LLM-1783002819431-end-1783002821513\",\"source\":\"node-LLM-1783002819431\",\"target\":\"end\",\"sourceHandle\":null,\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"markerEnd\":null,\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":735.3728585970605,\"sourceY\":168.3999964827198,\"targetX\":897.0,\"targetY\":158.0}],\"position\":[67.0,199.0],\"zoom\":1.18,\"viewport\":{\"x\":67.0,\"y\":199.0,\"zoom\":1.18}}', '{\"envVars\":[{\"name\":\"bb\",\"type\":\"string\",\"description\":\"\",\"defaultValue\":\"\"}]}', 0, NULL, NULL, 1, '2026-07-02 22:22:50', 1, '2026-07-02 22:45:19', 0);
INSERT INTO `ai_flow_version` VALUES (4, 10003, 'v1.0.0', '', '', '{\"nodes\":[{\"id\":\"start\",\"type\":\"Start\",\"initialized\":false,\"position\":{\"x\":100.0,\"y\":100.0},\"label\":\"开始\",\"data\":{\"label\":\"开始\",\"description\":\"流程入口\",\"inputParams\":null}},{\"id\":\"end\",\"type\":\"End\",\"initialized\":false,\"position\":{\"x\":900.0,\"y\":100.0},\"label\":\"结束\",\"data\":{\"label\":\"结束\",\"description\":\"工作流程完成。\",\"outputParams\":null}},{\"id\":\"node-Condition-1783007108785\",\"type\":\"Condition\",\"initialized\":false,\"position\":{\"x\":502.54237288135585,\"y\":216.0},\"label\":null,\"data\":{\"label\":\"条件分支\",\"description\":\"\",\"conditions\":[{\"id\":\"condition-1783007108787-xf7abc\",\"logic\":\"and\",\"rules\":[{\"id\":\"rule-1783007108787-8r7i72\",\"variable\":{\"nodeId\":\"\",\"name\":\"\",\"type\":\"\"},\"operator\":\"==\",\"value\":\"\"}]}]}}],\"edges\":[{\"id\":\"e-start-node-Condition-1783007108785-1783042348560\",\"source\":\"start\",\"target\":\"node-Condition-1783007108785\",\"sourceHandle\":null,\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":343.0,\"sourceY\":145.0,\"targetX\":499.54237288135585,\"targetY\":306.0},{\"id\":\"e-node-Condition-1783007108785-end-1783042350124\",\"source\":\"node-Condition-1783007108785\",\"target\":\"end\",\"sourceHandle\":\"condition:condition-1783007108787-xf7abc\",\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":745.5423728813558,\"sourceY\":311.0,\"targetX\":897.0,\"targetY\":158.0}],\"position\":[20.0,177.0],\"zoom\":1.18,\"viewport\":{\"x\":20.0,\"y\":177.0,\"zoom\":1.18}}', NULL, 1, '2026-07-03 10:36:05', 1, 1, '2026-07-02 23:00:39', 1, '2026-07-03 09:32:31', 0);
INSERT INTO `ai_flow_version` VALUES (5, 10004, 'v1.0.0', '', '', '{\"nodes\":[{\"id\":\"start\",\"type\":\"Start\",\"initialized\":false,\"position\":{\"x\":100.0,\"y\":100.0},\"label\":\"开始\",\"data\":{\"label\":\"开始\",\"description\":\"流程入口\",\"inputParams\":null}},{\"id\":\"end\",\"type\":\"End\",\"initialized\":false,\"position\":{\"x\":900.0,\"y\":100.0},\"label\":\"结束\",\"data\":{\"label\":\"结束\",\"description\":\"工作流程完成。\",\"outputParams\":null}}],\"edges\":[],\"position\":[20.0,177.0],\"zoom\":1.18,\"viewport\":{\"x\":20.0,\"y\":177.0,\"zoom\":1.18}}', NULL, 0, NULL, NULL, 1, '2026-07-03 10:39:16', 1, '2026-07-03 13:36:33', 0);
INSERT INTO `ai_flow_version` VALUES (6, 10003, 'v1.0.1', '', '', '{\"nodes\":[{\"id\":\"start\",\"type\":\"Start\",\"initialized\":false,\"position\":{\"x\":100.0,\"y\":100.0},\"label\":\"开始\",\"data\":{\"label\":\"开始\",\"description\":\"流程入口\",\"inputParams\":null}},{\"id\":\"end\",\"type\":\"End\",\"initialized\":false,\"position\":{\"x\":899.1525423728815,\"y\":87.0},\"label\":\"结束\",\"data\":{\"label\":\"结束\",\"description\":\"工作流程完成。\",\"outputParams\":null}},{\"id\":\"node-Condition-1783007108785\",\"type\":\"Condition\",\"initialized\":false,\"position\":{\"x\":517.7966101694914,\"y\":55.0},\"label\":null,\"data\":{\"label\":\"条件分支\",\"description\":\"\",\"conditions\":[{\"id\":\"condition-1783007108787-xf7abc\",\"logic\":\"and\",\"rules\":[{\"id\":\"rule-1783007108787-8r7i72\",\"variable\":{\"nodeId\":\"\",\"name\":\"\",\"type\":\"\"},\"operator\":\"==\",\"value\":\"\"}]}]}}],\"edges\":[{\"id\":\"e-start-node-Condition-1783007108785-1783042348560\",\"source\":\"start\",\"target\":\"node-Condition-1783007108785\",\"sourceHandle\":null,\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":343.0,\"sourceY\":145.0,\"targetX\":514.7966101694914,\"targetY\":145.0},{\"id\":\"e-node-Condition-1783007108785-end-1783042350124\",\"source\":\"node-Condition-1783007108785\",\"target\":\"end\",\"sourceHandle\":\"condition:condition-1783007108787-xf7abc\",\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":760.7966101694914,\"sourceY\":150.0,\"targetX\":896.1525423728815,\"targetY\":145.0}],\"position\":[20.0,177.0],\"zoom\":1.18,\"viewport\":{\"x\":20.0,\"y\":177.0,\"zoom\":1.18}}', NULL, 1, '2026-07-03 10:41:38', 1, 1, '2026-07-03 10:40:15', 1, '2026-07-03 10:40:30', 0);
INSERT INTO `ai_flow_version` VALUES (7, 10003, 'v1.0.2', '', '', '{\"nodes\":[{\"id\":\"start\",\"type\":\"Start\",\"initialized\":false,\"position\":{\"x\":100.0,\"y\":100.0},\"label\":\"开始\",\"data\":{\"label\":\"开始\",\"description\":\"流程入口\",\"inputParams\":null}},{\"id\":\"end\",\"type\":\"End\",\"initialized\":false,\"position\":{\"x\":899.1525423728815,\"y\":87.0},\"label\":\"结束\",\"data\":{\"label\":\"结束\",\"description\":\"工作流程完成。\",\"outputParams\":null}},{\"id\":\"node-Condition-1783007108785\",\"type\":\"Condition\",\"initialized\":false,\"position\":{\"x\":516.101694915254,\"y\":157.54237288135593},\"label\":null,\"data\":{\"label\":\"条件分支\",\"description\":\"\",\"conditions\":[{\"id\":\"condition-1783007108787-xf7abc\",\"logic\":\"and\",\"rules\":[{\"id\":\"rule-1783007108787-8r7i72\",\"variable\":{\"nodeId\":\"\",\"name\":\"\",\"type\":\"\"},\"operator\":\"==\",\"value\":\"\"}]}]}}],\"edges\":[{\"id\":\"e-start-node-Condition-1783007108785-1783042348560\",\"source\":\"start\",\"target\":\"node-Condition-1783007108785\",\"sourceHandle\":null,\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":343.0,\"sourceY\":145.0,\"targetX\":513.101694915254,\"targetY\":247.54237288135593},{\"id\":\"e-node-Condition-1783007108785-end-1783042350124\",\"source\":\"node-Condition-1783007108785\",\"target\":\"end\",\"sourceHandle\":\"condition:condition-1783007108787-xf7abc\",\"targetHandle\":null,\"data\":{},\"animated\":false,\"type\":\"default\",\"label\":\"\",\"style\":{\"stroke\":\"var(--el-border-color-darker)\",\"strokeWidth\":1},\"labelBgStyle\":null,\"sourceX\":759.101694915254,\"sourceY\":252.54237288135593,\"targetX\":896.1525423728815,\"targetY\":145.0}],\"position\":[20.0,177.0],\"zoom\":1.18,\"viewport\":{\"x\":20.0,\"y\":177.0,\"zoom\":1.18}}', NULL, 0, NULL, NULL, 1, '2026-07-03 10:43:19', 0, '2026-07-03 10:43:19', 0);

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
INSERT INTO `ai_message` VALUES (1081, 1080, 'system', 'wqeqwe', NULL, 1079, 2, 0, 0, 0, 1, '2026-06-29 16:25:30', 0, NULL, 0, 0);
INSERT INTO `ai_message` VALUES (1082, 1080, 'user', '你好', NULL, 1079, 2, 0, 0, 0, 1, '2026-06-29 16:25:30', 0, NULL, 0, 0);
INSERT INTO `ai_message` VALUES (1083, 1080, 'assistant', '你好！很高兴见到你～😊 有什么问题、想法，或者需要帮忙的地方吗？', NULL, 1079, 2, 18, 19, 37, 1, '2026-06-29 16:25:31', 0, NULL, 0, 0);
INSERT INTO `ai_message` VALUES (1084, 1080, 'user', '今天天气怎么样', NULL, 1079, 2, 0, 0, 0, 1, '2026-06-29 16:25:49', 0, NULL, 0, 0);
INSERT INTO `ai_message` VALUES (1085, 1080, 'assistant', '我目前无法实时获取天气信息（因为没有联网权限，也无法访问你的设备定位或天气服务）。不过你可以：\n\n✅ 打开手机自带的天气App（如“天气”“墨迹天气”等）  \n✅ 询问智能音箱（如“小爱同学，今天天气怎么样？”）  \n✅ 在浏览器搜索“你所在城市 + 今日天气”，比如“北京今日天气”\n\n如果你告诉我你所在的城市（比如“上海”或“广州”），我还可以帮你分析：  \n🔹 当前季节典型的天气特点（如梅雨、秋高气爽、回南天等）  \n🔹 出行建议（是否需要带伞、防晒、添衣等）  \n🔹 小贴士（比如湿度高时如何防潮、温差大时如何穿衣）\n\n需要我帮你做这些吗？🌤️', NULL, 1079, 2, 50, 176, 226, 1, '2026-06-29 16:25:49', 0, NULL, 0, 0);
INSERT INTO `ai_message` VALUES (1086, 1080, 'user', '南京的天气怎么样', NULL, 1079, 2, 0, 0, 0, 1, '2026-06-29 16:26:27', 0, NULL, 0, 0);
INSERT INTO `ai_message` VALUES (1087, 1080, 'assistant', '虽然我无法实时联网获取最新天气数据，但根据**2024年6月中旬的南京典型气候特征**（结合历史气象规律和近期公开气象趋势），可以为你提供一个较可靠的参考：\n\n📍 **南京当前（6月中下旬）天气概况**：  \n🌤️ **总体特点**：进入梅雨季前期，气温升高、湿度增大，多阵性降水。  \n🌡️ **气温范围**：日间高温约 28–33℃，夜间低温约 22–25℃，体感闷热。  \n💧 **湿度与降水**：相对湿度常达70%–90%，午后易有短时雷阵雨或局地强对流天气（可能伴有雷电、短时大风）。  \n☁️ **天空状况**：多云为主，偶有阳光，但云层较厚，紫外线中等偏强（仍需防晒）。\n\n✅ **今日出行小贴士（适用于南京）**：  \n• 🌂 建议随身带折叠伞——既防雨，也遮烈日；  \n• 👕 穿透气速干的棉麻或功能性面料，避免闷汗；  \n• 🚲 雨天路滑，骑电动车/步行注意积水路段（尤其新街口、珠江路等低洼处）；  \n• 🌡️ 室内空调建议设在26–28℃，避免“空调病”，并适时开窗通风除湿。\n\n⚠️ 温馨提醒：南京梅雨期一般从**6月中旬持续至7月中旬**，后续降雨将更频繁、持续时间更长，可关注“南京气象”微信公众号或中国天气网获取实时预警。\n\n需要我帮你：  \n🔹 查看未来3天天气趋势摘要？  \n🔹 写一条适合发朋友圈的南京天气文案？  \n🔹 或生成一份「梅雨季居家防潮小指南」？  \n\n欢迎告诉我～ 😊', NULL, 1079, 2, 240, 421, 661, 1, '2026-06-29 16:26:27', 0, NULL, 0, 0);

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
INSERT INTO `id_generator` VALUES (1, 1087, 'chat', 1, 1);
INSERT INTO `id_generator` VALUES (2, 10004, 'flow', 1, 1);

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
